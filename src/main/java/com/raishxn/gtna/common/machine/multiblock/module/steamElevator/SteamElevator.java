package com.raishxn.gtna.common.machine.multiblock.module.steamElevator;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
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
 * buffer evenly across every {@link SteamElevatorModuleMachine} installed in its twelve fixed module
 * slots. The modules are themselves small {@code 1x5x2} multiblocks that apply their own capability
 * (flight, weather, greenhouse, ...). This replaces GTNL's module hatches ({@code mModuleHatches})
 * with GTCEu multiblock modules and a fixed slot scan, so only a fully formed module is counted.
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

    /**
     * The twelve module-slot positions, expressed in the pattern's local frame relative to the
     * controller as {@code {up, left, forward}} (decoded from the {@code I} cells of
     * {@code pattern/steam_elevator.mbs}). {@link RelativeDirection#offsetPos} turns them into world
     * positions for the controller's facing, so a rotated or flipped elevator still finds its slots.
     */
    private static final int[][] MODULE_OFFSETS = {
            { 0, -8, -5 }, { 0, -8, -3 }, { 0, -8, -1 },
            { 0, -2, -11 }, { 0, -2, 5 },
            { 0, 0, -11 }, { 0, 0, 5 },
            { 0, 2, -11 }, { 0, 2, 5 },
            { 0, 8, -5 }, { 0, 8, -3 }, { 0, 8, -1 },
    };

    @Persisted
    @DescSynced
    private long energyBuffer;

    private final List<SteamElevatorModuleMachine> modules = new ArrayList<>();
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
        for (var part : getParts()) {
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
        // GTNL's checkHatch: the elevator needs at least one steam input hatch anywhere in the
        // structure (it is not tied to a specific cell), so enforce it here, not as a pattern limit.
        if (steamTanks.isEmpty()) {
            onStructureInvalid();
            return;
        }
        scanModules();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        for (SteamElevatorModuleMachine module : new ArrayList<>(modules)) {
            module.disconnectFromHost();
        }
        modules.clear();
        wasRunning = false;
        steamTanks.clear();
    }

    /**
     * The world positions of the twelve module slots, derived from the controller's position and
     * facing. The set is the exact {@code I} cells of the host pattern (see {@link #MODULE_OFFSETS}).
     */
    public BlockPos[] getModuleScanPositions() {
        Direction front = getFrontFacing();
        Direction upwards = getUpwardsFacing();
        boolean flipped = isFlipped();
        BlockPos origin = getPos();
        BlockPos[] positions = new BlockPos[MODULE_OFFSETS.length];
        for (int i = 0; i < MODULE_OFFSETS.length; i++) {
            int[] offset = MODULE_OFFSETS[i];
            positions[i] = RelativeDirection.offsetPos(origin, front, upwards, flipped,
                    offset[0], offset[1], offset[2]);
        }
        return positions;
    }

    /**
     * Rebuilds the connected-module set from the fixed slots. Only a fully formed
     * {@link SteamElevatorModuleMachine} counts; any other block or part in a slot is ignored.
     */
    private void scanModules() {
        Level level = getLevel();
        if (!(level instanceof ServerLevel)) return;
        List<SteamElevatorModuleMachine> found = new ArrayList<>();
        for (BlockPos pos : getModuleScanPositions()) {
            MetaMachine machine = MetaMachine.getMachine(level, pos);
            if (machine instanceof SteamElevatorModuleMachine module && module.isFormed() &&
                    !found.contains(module)) {
                found.add(module);
            }
        }
        for (SteamElevatorModuleMachine module : new ArrayList<>(modules)) {
            if (!found.contains(module)) {
                module.disconnectFromHost();
            }
        }
        for (SteamElevatorModuleMachine module : found) {
            module.connectToHost(this);
        }
        modules.clear();
        modules.addAll(found);
    }

    /** Called by a module when it unbinds; the periodic scan is the source of truth. */
    void removeModule(SteamElevatorModuleMachine module) {
        modules.remove(module);
    }

    private void elevatorTick() {
        if (isRemote() || !isFormed()) return;
        // Modules are separate multiblocks that may form after the host, so rescan the fixed slots.
        if (getOffsetTimer() % 20 == 0) {
            scanModules();
        }
        if (!isWorkingEnabled()) {
            if (wasRunning) {
                wasRunning = false;
                for (SteamElevatorModuleMachine module : new ArrayList<>(modules)) {
                    module.onElevatorStop();
                }
            }
            return;
        }
        wasRunning = true;

        refillFromSteam();

        if (!modules.isEmpty()) {
            long share = energyBuffer / modules.size();
            for (SteamElevatorModuleMachine module : new ArrayList<>(modules)) {
                long accepted = module.receiveEnergy(share);
                energyBuffer -= accepted;
            }
            for (SteamElevatorModuleMachine module : new ArrayList<>(modules)) {
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
