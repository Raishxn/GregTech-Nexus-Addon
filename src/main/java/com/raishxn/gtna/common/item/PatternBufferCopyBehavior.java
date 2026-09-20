package com.raishxn.gtna.common.item;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.common.item.TooltipBehavior;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import com.raishxn.gtna.common.machine.multiblock.part.ae.GTNAMEPatternBufferPartMachine;
import com.raishxn.gtna.utils.GTNATooltips;

import java.util.List;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.item.tool.ToolHelper.getBehaviorsTag;

/**
 * Copy (and cut) of a pattern buffer's configuration (GTLCore
 * {@code MEPatternBufferCopyBehavior}/{@code MEPatternBufferCutBehavior} parity).
 *
 * <p>
 * Sneak-right-click a buffer to snapshot it into the item, then right-click another buffer to
 * apply. Cut behaves like copy but also strips the patterns from the source buffer afterwards.
 */
public class PatternBufferCopyBehavior extends TooltipBehavior implements IInteractionItem {

    private final boolean cut;

    public PatternBufferCopyBehavior(boolean cut) {
        super(defaultTooltips(cut));
        this.cut = cut;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (player == null || level.isClientSide) {
            return InteractionResult.PASS;
        }
        if (!(level.getBlockEntity(context.getClickedPos()) instanceof MetaMachineBlockEntity machineBlock) ||
                !(machineBlock.getMetaMachine() instanceof GTNAMEPatternBufferPartMachine buffer)) {
            return InteractionResult.PASS;
        }

        var tag = getBehaviorsTag(stack);
        if (player.isShiftKeyDown()) {
            // Snapshot phase.
            buffer.copyBufferToTag(tag);
            if (cut) {
                buffer.cutPatternsFromBuffer();
            }
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(
                        Component.translatable(cut ? "gtna.machine.pattern_buffer.copy.cut" :
                                "gtna.machine.pattern_buffer.copy.copied"),
                        true);
            }
            return InteractionResult.SUCCESS;
        }

        // Apply phase.
        if (tag.isEmpty()) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(
                        Component.translatable("gtna.machine.pattern_buffer.copy.empty"), true);
            }
            return InteractionResult.PASS;
        }
        int pasted = buffer.pasteBufferFromTag(tag);
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.displayClientMessage(
                    Component.translatable("gtna.machine.pattern_buffer.copy.pasted", pasted), true);
        }
        return InteractionResult.SUCCESS;
    }

    private static Consumer<List<Component>> defaultTooltips(boolean cut) {
        return lines -> {
            lines.add(GTNATooltips.important(cut ? "item.gtna.pattern_buffer_cut.tooltip.snapshot" :
                    "item.gtna.pattern_buffer_copy.tooltip.snapshot"));
            lines.add(GTNATooltips.structure(cut ? "item.gtna.pattern_buffer_cut.tooltip.apply" :
                    "item.gtna.pattern_buffer_copy.tooltip.apply"));
        };
    }
}
