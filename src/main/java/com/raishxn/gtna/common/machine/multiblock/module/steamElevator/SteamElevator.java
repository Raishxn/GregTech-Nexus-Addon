package com.raishxn.gtna.common.machine.multiblock.module.steamElevator;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * GTNA-native port of GTNL's {@code SteamElevator} (LGPLv3, original by ScienceNotLeisure).
 *
 * <p>
 * A 35x43x35 modular multiblock: it burns steam into a large internal EU buffer and distributes that
 * buffer evenly across every {@link ISteamElevatorModule} part installed in its twelve module slots.
 * The modules then apply their own capability (flight, weather, greenhouse, ...). This replaces
 * GTNL's module hatches ({@code mModuleHatches}) with GTCEu part machines and a dedicated
 * {@code steam_elevator_module} {@link PartAbility}.
 *
 * <p>
 * Deviations (documented):
 * <ul>
 * <li>The GTNL controller is a {@code SteamMultiMachineBase}; GTNA uses a plain
 * {@link WorkableMultiblockMachine} because the elevator does not process recipes. Steam is drained
 * directly from the structure's steam hatches at a 1 mB = 1 EU rate.</li>
 * <li>GTNL's wireless steam network modes (Ad Astra / GTNH wireless) are not ported; the structure
 * still requires a steam hatch (the GTNA {@code WirelessSteamInputHatch} satisfies it).</li>
 * <li>Player teleport (GTNL opened the Galacticraft celestial selection and moved the player
 * between planets) is a Fabric/Ad Astra concern in 1.20.1; see {@link SteamElevatorTeleport} for the
 * GTNA-native reimplementation.</li>
 * </ul>
 */
public class SteamElevator extends WorkableMultiblockMachine implements IDisplayUIMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(SteamElevator.class,
            WorkableMultiblockMachine.MANAGED_FIELD_HOLDER);

    /** GTNL {@code maxEUStore()}. */
    public static final long MAX_ENERGY = 256_000_000L;

    @Persisted
    @DescSynced
    private long energyBuffer;

    private final List<ISteamElevatorModule> modules = new ArrayList<>();
    private final List<NotifiableFluidTank> steamTanks = new ArrayList<>();

    private boolean wasRunning;

    @Nullable
    private TickableSubscription tickSubscription;

    public SteamElevator(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            tickSubscription = subscribeServerTick(this::elevatorTick);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (tickSubscription != null) {
            tickSubscription.unsubscribe();
            tickSubscription = null;
        }
    }

    @Override
    protected RecipeLogic createRecipeLogic(Object... args) {
        // The elevator is steam-driven and has no recipes; keep the default logic inert so the
        // default RecipeLogic never tries to index an empty recipe-type array.
        return new InertRecipeLogic(this);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        modules.clear();
        steamTanks.clear();
        for (IMultiPart part : getParts()) {
            if (part instanceof ISteamElevatorModule module) {
                modules.add(module);
            }
            if (!PartAbility.STEAM.isApplicable(part.self().getDefinition().getBlock())) {
                continue;
            }
            for (var handlerList : part.getRecipeHandlers()) {
                if (!handlerList.isValid(IO.IN)) continue;
                for (var fluidHandler : handlerList.getCapability(FluidRecipeCapability.CAP)) {
                    if (fluidHandler instanceof NotifiableFluidTank tank) {
                        steamTanks.add(tank);
                    }
                }
            }
        }
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        for (ISteamElevatorModule module : modules) {
            module.onElevatorStop();
        }
        wasRunning = false;
        modules.clear();
        steamTanks.clear();
    }

    private void elevatorTick() {
        if (isRemote() || !isFormed()) return;
        if (!isWorkingEnabled()) {
            if (wasRunning) {
                wasRunning = false;
                for (ISteamElevatorModule module : modules) {
                    module.onElevatorStop();
                }
            }
            return;
        }
        wasRunning = true;

        refillFromSteam();

        if (!modules.isEmpty()) {
            long share = energyBuffer / modules.size();
            for (ISteamElevatorModule module : modules) {
                long accepted = module.receiveEnergy(share);
                energyBuffer -= accepted;
            }
            for (ISteamElevatorModule module : modules) {
                module.onElevatorTick(this);
            }
        }
        if (getOffsetTimer() % 20 == 0) {
            markDirty();
        }
    }

    /** Drains the structure's steam hatches at 1 mB = 1 EU into the internal buffer. */
    private void refillFromSteam() {
        long space = MAX_ENERGY - energyBuffer;
        if (space <= 0) return;
        for (NotifiableFluidTank tank : steamTanks) {
            if (space <= 0) break;
            long request = Math.min(space, Integer.MAX_VALUE);
            // drainInternal bypasses the tank's capability IO gate (steam hatches are input-only).
            FluidStack drained = tank.drainInternal((int) request, IFluidHandler.FluidAction.EXECUTE);
            if (drained.isEmpty()) continue;
            energyBuffer += drained.getAmount();
            space -= drained.getAmount();
        }
    }

    public long getEnergyBuffer() {
        return energyBuffer;
    }

    public int getModuleCount() {
        return modules.size();
    }

    public boolean isElevatorRunning() {
        return isFormed() && isWorkingEnabled();
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        ModularUI ui = IDisplayUIMachine.super.createUI(entityPlayer);
        // "Set out": opens the Ad Astra planet selection, exactly like GTLCore's SpaceElevatorMachine.
        ui.widget(new ButtonWidget(151, 107, 16, 16, GuiTextures.BUTTON, clickData -> {
            if (!clickData.isRemote && entityPlayer instanceof ServerPlayer serverPlayer) {
                SteamElevatorTeleport.setOut(serverPlayer, this);
            }
        }));
        return ui;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        IDisplayUIMachine.super.addDisplayText(textList);
        if (isFormed()) {
            textList.add(Component.translatable("gtna.machine.steam_elevator.energy", energyBuffer, MAX_ENERGY)
                    .withStyle(ChatFormatting.AQUA));
            textList.add(Component.translatable("gtna.machine.steam_elevator.modules", modules.size())
                    .withStyle(ChatFormatting.GOLD));
            textList.add(Component.translatable("gtna.machine.steam_elevator.steam_hatches", steamTanks.size())
                    .withStyle(ChatFormatting.GRAY));
            if (modules.isEmpty()) {
                textList.add(Component.translatable("gtna.machine.steam_elevator.no_modules")
                        .withStyle(ChatFormatting.RED));
            }
        }
    }

    /**
     * A recipe logic that never looks at recipe maps. The definition registers only
     * {@code DUMMY_RECIPES}, so indexing {@code getRecipeType()} is safe but pointless.
     */
    public static class InertRecipeLogic extends RecipeLogic {

        public InertRecipeLogic(WorkableMultiblockMachine machine) {
            super(machine);
        }

        @Override
        public void serverTick() {
            // Driven by SteamElevator#elevatorTick.
        }

        @Override
        public void findAndHandleRecipe() {
            // No recipe maps.
        }
    }
}
