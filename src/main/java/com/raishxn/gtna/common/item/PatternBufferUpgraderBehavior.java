package com.raishxn.gtna.common.item;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.common.item.TooltipBehavior;

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
import com.raishxn.gtna.config.GTNABalance;
import com.raishxn.gtna.utils.GTNATooltips;

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
            lines.add(GTNATooltips.important("item.gtna.pattern_buffer_upgrader.tooltip.use"));
            lines.add(GTNATooltips.structure("item.gtna.pattern_buffer_upgrader.tooltip.keep_data"));
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

        // Compare configured capacities without instantiating a probe machine on this holder:
        // creating a machine attaches its managed storage to the block entity, and its default
        // fields would overwrite the real machine's values in the saved tag.
        int targetSlots = GTNABalance.getPatternBufferSlotsOrDefault(targetDefinition.getId().getPath(),
                machine.getMaxPatternCount());
        if (targetSlots <= machine.getMaxPatternCount()) {
            return InteractionResult.PASS;
        }

        CompoundTag machineData = new CompoundTag();
        // Full persistent state: patterns, shared inventories, slot configs and internal slots.
        // The drop-oriented saveToItem only carries @DropSaved fields and would lose patterns.
        machineBlockEntity.saveManagedPersistentData(machineData, false);

        // Level#setBlock removes the old block entity, whose onMachineRemoved would spill both
        // inventories saved above and duplicate every stack once the data is reloaded below.
        machine.gtna$preserveInventoryOnUpgrade();
        if (!level.setBlock(pos, newState, 3)) {
            machine.gtna$cancelUpgradeSwap();
            return InteractionResult.PASS;
        }

        if (!(level.getBlockEntity(pos) instanceof MetaMachineBlockEntity upgradedBlockEntity) ||
                !(upgradedBlockEntity.getMetaMachine() instanceof GTNAMEPatternBufferPartMachine)) {
            return InteractionResult.PASS;
        }

        upgradedBlockEntity.loadManagedPersistentData(machineData);
        // The transferred NBT carries the old block's render state, owned by the smaller
        // definition; the upgraded block must render with its own model.
        upgradedBlockEntity.setRenderState(upgradedBlockEntity.getDefinition().defaultRenderState());
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
