package com.raishxn.gtna.common.world.personalspace;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

/** Port of PersonalSpace's {@code PortalBlock}: an obsidian pedestal with a floating book. */
public final class PersonalSpacePortalBlock extends BaseEntityBlock {

    private static final VoxelShape SHAPE = box(0, 0, 0, 16, 12, 16);

    public PersonalSpacePortalBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(25.0F, 6000000.0F)
                .requiresCorrectToolForDrops().noOcclusion());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PersonalSpacePortalEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return level.isClientSide ? createTickerHelper(type, PersonalSpacePortalRegistry.PORTAL_ENTITY.get(),
                PersonalSpacePortalEntity::clientTick) : null;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof PersonalSpacePortalEntity portal)) return InteractionResult.PASS;
        if (level.isClientSide) {
            if (!portal.isActive() && !level.dimension().equals(Level.OVERWORLD)) {
                player.displayClientMessage(Component.translatable("chat.overworldPersonalDimension"), false);
            } else if (!portal.isActive() || player.isShiftKeyDown()) {
                PersonalSpaceClientHooks.openPortalGui(pos);
            }
            return InteractionResult.SUCCESS;
        }
        if (portal.isActive() && !player.isShiftKeyDown() && player instanceof ServerPlayer serverPlayer) {
            portal.transport(serverPlayer);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (!(level.getBlockEntity(pos) instanceof PersonalSpacePortalEntity portal) || placer == null) return;
        double dx = placer.getX() - pos.getX();
        double dz = placer.getZ() - pos.getZ();
        if (Math.abs(dx) > Math.abs(dz)) {
            portal.setFacing(dx > 0 ? Direction.EAST : Direction.WEST);
        } else {
            portal.setFacing(dz > 0 ? Direction.SOUTH : Direction.NORTH);
        }
        if (level.isClientSide) return;
        if (stack.getTagElement("BlockEntityTag") != null) {
            portal.linkOtherPortal(false, placer instanceof ServerPlayer player ? player : null);
        }
        portal.setChanged();
    }

    private static ItemStack itemFor(BlockEntity blockEntity) {
        ItemStack stack = new ItemStack(PersonalSpacePortalRegistry.PORTAL_ITEM.get());
        if (blockEntity instanceof PersonalSpacePortalEntity portal) portal.saveToItem(stack);
        return stack;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        var tool = params.getOptionalParameter(LootContextParams.TOOL);
        if (tool == null || !tool.isCorrectToolForDrops(state)) return List.of();
        return List.of(itemFor(params.getOptionalParameter(LootContextParams.BLOCK_ENTITY)));
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return itemFor(level.getBlockEntity(pos));
    }
}
