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
import com.gregtechceu.gtceu.common.data.GTMaterials;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
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
 * A 35x43x35 modular multiblock. Following the author's model there is <b>no EU buffer</b> and the
 * elevator itself needs no steam/energy: it is always active once formed. The twelve fixed module
 * slots are scanned for fully formed {@link SteamElevatorModuleMachine}s, which consume steam
 * directly from the steam input hatches in their own structure and in the host structure.
 *
 * <p>
 * Deviations (documented):
 * <ul>
 * <li>The GTNL controller is a {@code SteamMultiMachineBase}; GTNA uses a plain
 * {@link WorkableMultiblockMachine} because the elevator does not process recipes and no longer
 * burns steam into an internal buffer.</li>
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

    private final List<SteamElevatorModuleMachine> modules = new ArrayList<>();
    private final List<NotifiableFluidTank> steamTanks = new ArrayList<>();

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
                    if (fluidHandler instanceof NotifiableFluidTank tank &&
                            tank.isFluidValid(0, GTMaterials.Steam.getFluid(1))) {
                        steamTanks.add(tank);
                    }
                }
            }
        }
        // GTNL: the elevator itself needs no steam/power; only the modules consume steam.
        scanModules();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        for (SteamElevatorModuleMachine module : new ArrayList<>(modules)) {
            module.disconnectFromHost();
        }
        modules.clear();
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
        // The modules themselves apply their effects (and pay their upkeep) on their own server
        // tick while bound, so a module keeps working even if the host tower is not being ticked.
        if (getOffsetTimer() % 20 == 0) {
            scanModules();
        }
    }

    // ------------------------------------------------------------------
    // Steam pool shared with the connected modules.
    // ------------------------------------------------------------------

    /** Total steam currently held by the structure's steam input hatches. */
    public long getAvailableSteam() {
        long total = 0;
        for (NotifiableFluidTank tank : steamTanks) {
            total += tank.getFluidInTank(0).getAmount();
        }
        return total;
    }

    /** Drains up to {@code amount} from the structure's steam input hatches. */
    public long drainSteam(long amount) {
        long remaining = amount;
        for (NotifiableFluidTank tank : steamTanks) {
            if (remaining <= 0) break;
            FluidStack drained = tank.drainInternal((int) Math.min(remaining, Integer.MAX_VALUE),
                    IFluidHandler.FluidAction.EXECUTE);
            remaining -= drained.getAmount();
        }
        return amount - remaining;
    }

    public int getModuleCount() {
        return modules.size();
    }

    /** The elevator is always active once formed (no recipe/energy state). */
    public boolean isElevatorRunning() {
        return isFormed();
    }

    @Override
    public boolean isActive() {
        return isFormed();
    }

    @Override
    public boolean isWorkingEnabled() {
        // No power switch semantics: the elevator always runs while formed.
        return true;
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
            textList.add(Component.translatable("gtna.machine.steam_elevator.steam", getAvailableSteam())
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
