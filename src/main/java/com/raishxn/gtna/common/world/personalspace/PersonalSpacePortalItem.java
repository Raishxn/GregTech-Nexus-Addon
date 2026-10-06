package com.raishxn.gtna.common.world.personalspace;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;

/** Port of PersonalSpace's {@code PortalItem}: keeps the link, refuses crowded placement, never despawns. */
public final class PersonalSpacePortalItem extends BlockItem {

    public PersonalSpacePortalItem(Block block, Properties properties) {
        super(block, properties);
    }

    private static CompoundTag link(ItemStack stack) {
        CompoundTag tag = stack.getTagElement("BlockEntityTag");
        return tag != null && tag.getIntArray("target").length >= 3 ? tag : null;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = link(stack);
        if (tag != null) {
            int[] target = tag.getIntArray("target");
            ResourceLocation dimension = ResourceLocation.tryParse(tag.getString("targetDimension"));
            int id = PersonalSpaceWorlds.id(dimension);
            String label = id > 0 ? "DIM" + id : String.valueOf(dimension);
            tooltip.add(Component.literal(String.format("%s: %d, %d, %d", label, target[0], target[1], target[2])));
        }
        tooltip.add(Component.translatable("gtna.tooltip.source",
                Component.translatable("gtna.source.personalspace")));
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return link(stack) != null;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clicked = context.getClickedPos();
        for (BlockPos pos : BlockPos.betweenClosed(clicked.offset(-2, -2, -2), clicked.offset(2, 2, 2))) {
            if (level.isLoaded(pos) && level.getBlockState(pos).getBlock() instanceof PersonalSpacePortalBlock) {
                if (!level.isClientSide && context.getPlayer() != null) {
                    context.getPlayer().sendSystemMessage(Component.translatable("chat.personalWorld.proximity"));
                }
                return InteractionResult.FAIL;
            }
        }
        return super.useOn(context);
    }

    @Override
    public int getEntityLifespan(ItemStack stack, Level level) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    /** Immune to fire, lava, cactus and explosions; only the void destroys it. */
    @Override
    public Entity createEntity(Level level, Entity location, ItemStack stack) {
        // A subclass: Forge only swaps plain ItemEntity instances, so returning one would recurse forever.
        ItemEntity entity = new ItemEntity(level, location.getX(), location.getY(), location.getZ(), stack) {};
        entity.setDeltaMovement(location.getDeltaMovement());
        entity.setYRot(location.getYRot());
        entity.setXRot(location.getXRot());
        entity.setPickUpDelay(10);
        entity.setInvulnerable(true);
        return entity;
    }
}
