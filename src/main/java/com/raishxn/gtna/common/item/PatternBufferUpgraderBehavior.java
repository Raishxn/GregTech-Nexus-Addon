package com.raishxn.gtna.common.item;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.common.item.TooltipBehavior;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import com.raishxn.gtna.common.machine.multiblock.part.ae.GTNAMEPatternBufferPartMachine;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class PatternBufferUpgraderBehavior extends TooltipBehavior implements IInteractionItem {

    private final Supplier<MachineDefinition> upgradeTo;

    public PatternBufferUpgraderBehavior(Supplier<MachineDefinition> upgradeTo) {
        super(defaultTooltips());
        this.upgradeTo = upgradeTo;
    }

    private static Consumer<List<Component>> defaultTooltips() {
        return lines -> {
            lines.add(Component.translatable("item.gtna.pattern_buffer_upgrader.tooltip.use"));
            lines.add(Component.translatable("item.gtna.pattern_buffer_upgrader.tooltip.keep_data")
                    .withStyle(ChatFormatting.GRAY));
        };
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (!(level.getBlockEntity(pos) instanceof MetaMachineBlockEntity machineBlockEntity) ||
                !(machineBlockEntity.getMetaMachine() instanceof GTNAMEPatternBufferPartMachine machine)) {
            return InteractionResult.PASS;
        }

        MachineDefinition targetDefinition = upgradeTo.get();
        BlockState oldState = level.getBlockState(pos);
        BlockState newState = copySharedProperties(oldState, targetDefinition.defaultBlockState());

        if (machine.getDefinition().equals(targetDefinition)) {
            return InteractionResult.PASS;
        }

        GTNAMEPatternBufferPartMachine probe = (GTNAMEPatternBufferPartMachine) targetDefinition
                .createMetaMachine(machineBlockEntity);
        if (probe.getMaxPatternCount() <= machine.getMaxPatternCount()) {
            return InteractionResult.PASS;
        }

        CompoundTag machineData = new CompoundTag();
        machine.saveToItem(machineData);

        if (!level.setBlock(pos, newState, 3)) {
            return InteractionResult.PASS;
        }

        if (!(level.getBlockEntity(pos) instanceof MetaMachineBlockEntity upgradedBlockEntity) ||
                !(upgradedBlockEntity.getMetaMachine() instanceof GTNAMEPatternBufferPartMachine upgradedMachine)) {
            return InteractionResult.PASS;
        }

        upgradedMachine.loadFromItem(machineData);
        upgradedBlockEntity.setChanged();
        level.sendBlockUpdated(pos, oldState, newState, 3);

        if (player instanceof ServerPlayer serverPlayer && !serverPlayer.getAbilities().instabuild) {
            stack.shrink(1);
        }
        level.playSound(null, pos, newState.getSoundType(level, pos, player).getPlaceSound(), SoundSource.BLOCKS,
                1.0F, 1.0F);
        return InteractionResult.CONSUME;
    }

    private static BlockState copySharedProperties(BlockState source, BlockState target) {
        BlockState copied = target;
        for (Property<?> property : source.getProperties()) {
            if (target.hasProperty(property)) {
                copied = copyPropertyValue(source, copied, property);
            }
        }
        return copied;
    }

    private static <T extends Comparable<T>> BlockState copyPropertyValue(BlockState source, BlockState target,
                                                                          Property<T> property) {
        return target.setValue(property, source.getValue(property));
    }
}
