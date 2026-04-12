package com.raishxn.gtna.common.machine.multiblock.noenergy;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import com.raishxn.gtna.api.capability.WirelessEnergyManager;
import com.raishxn.gtna.utils.MachineIO;
import com.raishxn.gtna.utils.datastructure.Int128;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

public class EyeOfHarmonyMachine extends WorkableMultiblockMachine implements IDisplayUIMachine, IFancyUIMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            EyeOfHarmonyMachine.class, WorkableMultiblockMachine.MANAGED_FIELD_HOLDER);
    private static final long FLUID_BATCH = 100_000_000L;
    private static final long REQUIRED_GAS = 1_024_000_000L;
    private static final Int128 BASE_STARTUP = Int128.fromBigInteger(BigInteger.valueOf(5_277_655_810_867_200L));

    @Persisted
    @DescSynced
    private int overclockLevel = 0;
    @Persisted
    @DescSynced
    private long hydrogen = 0;
    @Persisted
    @DescSynced
    private long helium = 0;
    @Persisted
    @DescSynced
    private UUID networkOwner;

    public EyeOfHarmonyMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public void onMachinePlaced(@Nullable LivingEntity player, ItemStack stack) {
        if (player != null) {
            this.networkOwner = player.getUUID();
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            subscribeServerTick(this::updateStartupState);
        }
    }

    private void updateStartupState() {
        if (!isFormed() || getOffsetTimer() % 20 != 0) {
            return;
        }
        if (networkOwner == null) {
            networkOwner = getOwnerUUID();
        }

        overclockLevel = 0;
        if (MachineIO.inputFluid(this, com.lowdragmc.lowdraglib.side.fluid.FluidStack.create(
                GTMaterials.Hydrogen.getFluid((int) FLUID_BATCH).getFluid(), FLUID_BATCH))) {
            hydrogen += FLUID_BATCH;
        }
        if (MachineIO.inputFluid(this, com.lowdragmc.lowdraglib.side.fluid.FluidStack.create(
                GTMaterials.Helium.getFluid((int) FLUID_BATCH).getFluid(), FLUID_BATCH))) {
            helium += FLUID_BATCH;
        }
        if (MachineIO.notConsumableCircuit(this, 4)) {
            overclockLevel = 4;
        } else if (MachineIO.notConsumableCircuit(this, 3)) {
            overclockLevel = 3;
        } else if (MachineIO.notConsumableCircuit(this, 2)) {
            overclockLevel = 2;
        } else if (MachineIO.notConsumableCircuit(this, 1)) {
            overclockLevel = 1;
        }
    }

    public Int128 getStartupEnergy() {
        if (overclockLevel <= 0) {
            return Int128.ZERO();
        }
        return BASE_STARTUP.copy().multiply((long) Math.pow(8, overclockLevel - 1));
    }

    @Nullable
    public static ModifierFunction recipeModifier(MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof EyeOfHarmonyMachine harmonyMachine)) {
            return ModifierFunction.NULL;
        }
        if (!(harmonyMachine.getLevel() instanceof ServerLevel serverLevel) ||
                harmonyMachine.networkOwner == null ||
                harmonyMachine.hydrogen < REQUIRED_GAS ||
                harmonyMachine.helium < REQUIRED_GAS ||
                harmonyMachine.overclockLevel <= 0) {
            return ModifierFunction.NULL;
        }

        Int128 startupEnergy = harmonyMachine.getStartupEnergy();
        if (!WirelessEnergyManager.consumeEnergy(serverLevel, harmonyMachine.networkOwner, startupEnergy)) {
            return ModifierFunction.NULL;
        }

        harmonyMachine.hydrogen -= REQUIRED_GAS;
        harmonyMachine.helium -= REQUIRED_GAS;

        return ModifierFunction.builder()
                .durationMultiplier(4800.0 / Math.pow(2, harmonyMachine.overclockLevel) / recipe.duration)
                .build();
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        if (networkOwner == null) {
            networkOwner = player.getUUID();
        }
        return true;
    }

    @Override
    public InteractionResult onUse(BlockState state, Level world, net.minecraft.core.BlockPos pos, Player player,
                                   InteractionHand hand, BlockHitResult hit) {
        if (player.getItemInHand(hand).is(GTItems.TOOL_DATA_STICK.asItem())) {
            this.networkOwner = player.getUUID();
            if (!world.isClientSide) {
                player.sendSystemMessage(Component.translatable("gtna.machine.eye_of_harmony.rebound"));
            }
            return InteractionResult.sidedSuccess(world.isClientSide);
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(198, 208, this, entityPlayer)
                .widget(new FancyMachineUIWidget(this, 198, 208));
    }

    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 190, 125);
        var screen = new DraggableScrollableWidgetGroup(4, 4, 182, 117)
                .setBackground(GuiTextures.DISPLAY);
        screen.addWidget(new LabelWidget(4, 5, self().getBlockState().getBlock().getDescriptionId()));
        screen.addWidget(new ComponentPanelWidget(4, 17, this::addDisplayText).setMaxWidthLimit(170));
        group.addWidget(screen);
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    @Override
    public void addDisplayText(@NotNull List<Component> textList) {
        if (isFormed()) {
            String ownerName = networkOwner == null ? "-" : resolvePlayerName(networkOwner);
            Int128 stored = getLevel() instanceof ServerLevel serverLevel && networkOwner != null
                    ? WirelessEnergyManager.getEnergy(serverLevel, networkOwner)
                    : Int128.ZERO();
            textList.add(Component.translatable("gtna.machine.eye_of_harmony.owner", ownerName));
            textList.add(Component.translatable("gtna.machine.eye_of_harmony.network_eu",
                    FormattingUtil.formatNumbers(stored.toString())));
            textList.add(Component.translatable("gtna.machine.eye_of_harmony.startup_eu",
                    FormattingUtil.formatNumbers(getStartupEnergy().toString())));
            textList.add(Component.translatable("gtna.machine.eye_of_harmony.hydrogen",
                    FormattingUtil.formatNumbers(hydrogen)));
            textList.add(Component.translatable("gtna.machine.eye_of_harmony.helium",
                    FormattingUtil.formatNumbers(helium)));
        }
        MultiblockDisplayText.builder(textList, isFormed())
                .setWorkingStatus(recipeLogic.isWorkingEnabled(), recipeLogic.isActive())
                .addWorkingStatusLine()
                .addProgressLine(recipeLogic)
                .addOutputLines(recipeLogic.getLastRecipe());
    }

    private String resolvePlayerName(UUID uuid) {
        if (getLevel() instanceof ServerLevel serverLevel) {
            Player player = serverLevel.getServer().getPlayerList().getPlayer(uuid);
            if (player != null) {
                return player.getName().getString();
            }
        }
        return uuid.toString().substring(0, 8) + "...";
    }
}
