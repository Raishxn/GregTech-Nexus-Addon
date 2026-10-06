package com.raishxn.gtna.common.world.personalspace;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.network.packet.SPersonalSpaceWorldList;

/** Port of PersonalSpace's {@code PortalTileEntity}: link, teleport, relink and settings updates. */
public final class PersonalSpacePortalEntity extends BlockEntity {

    public static final Direction DEFAULT_FACING = Direction.NORTH;
    private static final RandomSource RAND = RandomSource.create();

    // Client-side book animation state, as in the enchanting table.
    public int tickCount;
    public float pagePosition;
    public float prevPagePosition;
    public float pageTarget;
    public float pageVelocity;

    private boolean active = false;
    private ResourceLocation targetDimension = Level.OVERWORLD.location();
    private BlockPos targetPos = new BlockPos(8, 8, 8);
    private Direction targetFacing = DEFAULT_FACING;
    private Direction facing = DEFAULT_FACING;

    public PersonalSpacePortalEntity(BlockPos pos, BlockState state) {
        super(PersonalSpacePortalRegistry.PORTAL_ENTITY.get(), pos, state);
    }

    public boolean isActive() {
        return active;
    }

    public ResourceLocation targetDimension() {
        return targetDimension;
    }

    public ResourceKey<Level> targetKey() {
        return ResourceKey.create(Registries.DIMENSION, targetDimension);
    }

    /** PersonalSpace ID of the target, or 0 when the portal leads to a non-personal dimension. */
    public int targetPersonalId() {
        return PersonalSpaceWorlds.id(targetDimension);
    }

    public BlockPos targetPos() {
        return targetPos;
    }

    public Direction facing() {
        return facing;
    }

    public Direction targetFacing() {
        return targetFacing;
    }

    public void setFacing(Direction facing) {
        this.facing = facing.getAxis().isHorizontal() ? facing : DEFAULT_FACING;
        changed();
    }

    public void setTarget(ResourceLocation dimension, BlockPos pos, Direction facing) {
        active = true;
        targetDimension = dimension;
        targetPos = pos.immutable();
        targetFacing = facing;
        changed();
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, PersonalSpacePortalEntity portal) {
        portal.prevPagePosition = portal.pagePosition;
        if (RAND.nextInt(40) == 0) {
            float previous = portal.pageTarget;
            do {
                portal.pageTarget += (float) (RAND.nextInt(4) - RAND.nextInt(4));
            } while (previous == portal.pageTarget);
        }
        portal.tickCount++;
        float delta = Mth.clamp((portal.pageTarget - portal.pagePosition) * 0.4F, -0.2F, 0.2F);
        portal.pageVelocity += (delta - portal.pageVelocity) * 0.9F;
        portal.pagePosition += portal.pageVelocity;
    }

    public BlockPos teleportPos() {
        return targetPos.relative(targetFacing);
    }

    public void transport(ServerPlayer player) {
        if (!(level instanceof ServerLevel serverLevel) || !active || player == null) return;
        ServerLevel target = PersonalSpaceWorlds.level(serverLevel.getServer(), targetKey());
        if (target == null) {
            GTNACORE.LOGGER.warn("Player {} attempted to teleport to dimension {} which is not registered",
                    player.getGameProfile().getName(), targetDimension);
            return;
        }
        BlockPos destination = teleportPos();
        double dX = targetPos.getX() - destination.getX();
        double dZ = targetPos.getZ() - destination.getZ();
        double distance = Math.sqrt(dX * dX + dZ * dZ);
        float yaw = player.getYRot();
        if (distance > 0) {
            double newYaw = Math.acos(dX / distance) * 180 / Math.PI - 90;
            if (dZ < 0) newYaw -= 180;
            yaw = (float) newYaw;
        }
        PersonalSpaceWorlds.teleport(player, target, destination.getX() + 0.5, destination.getY() + 0.1,
                destination.getZ() + 0.5, yaw);
    }

    /** Finds or places the portal at the destination and points it back here. */
    public void linkOtherPortal(boolean spawnNewPortal, ServerPlayer player) {
        if (!active || !(level instanceof ServerLevel serverLevel)) return;
        ServerLevel otherWorld = PersonalSpaceWorlds.level(serverLevel.getServer(), targetKey());
        if (otherWorld == null) {
            GTNACORE.LOGGER.error("Couldn't initialize world {}", targetDimension);
            return;
        }
        BlockPos found = null;
        search:
        for (int x = targetPos.getX() - 1; x <= targetPos.getX() + 1; x++) {
            for (int y = targetPos.getY() - 1; y <= targetPos.getY() + 1; y++) {
                if (y < otherWorld.getMinBuildHeight() || y >= otherWorld.getMaxBuildHeight()) continue;
                for (int z = targetPos.getZ() - 1; z <= targetPos.getZ() + 1; z++) {
                    BlockPos candidate = new BlockPos(x, y, z);
                    if (otherWorld.getBlockState(candidate).getBlock() instanceof PersonalSpacePortalBlock) {
                        found = candidate;
                        break search;
                    }
                }
            }
        }
        PersonalSpacePortalEntity otherPortal = null;
        if (found != null) {
            if (otherWorld.getBlockEntity(found) instanceof PersonalSpacePortalEntity portal) otherPortal = portal;
        } else if (spawnNewPortal) {
            found = targetPos;
            otherWorld.setBlock(found, PersonalSpacePortalRegistry.PORTAL.get().defaultBlockState(), 3);
            if (otherWorld.getBlockEntity(found) instanceof PersonalSpacePortalEntity portal) otherPortal = portal;
        }
        if (otherPortal == null) return;
        otherPortal.active = true;
        int otherTargetId = otherPortal.targetPersonalId();
        boolean otherTargetKnown = otherTargetId > 0 &&
                PersonalSpaceDirectory.get(serverLevel.getServer()).settings(otherTargetId) != null;
        if (!otherPortal.targetDimension.equals(serverLevel.dimension().location()) && otherTargetKnown) {
            if (player != null) player.sendSystemMessage(Component.translatable("chat.personalWorld.relinked.error"));
            return;
        }
        otherPortal.setTarget(serverLevel.dimension().location(), worldPosition, facing);
        if (player != null) {
            player.sendSystemMessage(Component.translatable("chat.personalWorld.relinked", dimensionLabel()));
        }
        GTNACORE.LOGGER.info("Linked portal at {}:{} to {}:{}", targetDimension, found,
                serverLevel.dimension().location(), worldPosition);
    }

    private String dimensionLabel() {
        int id = targetPersonalId();
        return id > 0 ? Integer.toString(id) : targetDimension.toString();
    }

    /** Server-side validation and application of the editor's settings, as in {@code updateSettings}. */
    public void updateSettings(ServerPlayer player, PersonalSpaceSettings unsafe) {
        if (!(level instanceof ServerLevel serverLevel) || player == null) return;
        var server = serverLevel.getServer();
        if (!serverLevel.mayInteract(player, worldPosition)) {
            deny(player, "spawn protection");
            return;
        }
        if (PersonalSpaceConfig.useBlockEventChecks()) {
            var fakeBreak = new BlockEvent.BreakEvent(serverLevel, worldPosition, getBlockState(), player);
            if (MinecraftForge.EVENT_BUS.post(fakeBreak)) {
                deny(player, "block permission");
                return;
            }
        }
        if (!PersonalSpaceSettings.canUseLayers(unsafe.getLayersAsString(), PersonalSpaceConfig.allowedBlocks())) {
            player.sendSystemMessage(Component.translatable("chat.personalWorld.badLayers"));
            GTNACORE.LOGGER.warn("Player {} used forbidden PersonalSpace layers at {}", player.getGameProfile()
                    .getName(), worldPosition);
            return;
        }
        if (!decorationsAllowed(unsafe)) {
            player.sendSystemMessage(Component.translatable("chat.personalWorld.badBlocks"));
            return;
        }
        if (!PersonalSpaceSettings.canUseBiome(unsafe.getBiomeId(), PersonalSpaceConfig.allowedBiomes())) {
            unsafe.setBiomeId(PersonalSpaceSettings.DEFAULT_BIOME);
        }
        PersonalSpaceSettings sanitized = new PersonalSpaceSettings();
        sanitized.copyFrom(unsafe, true, true);

        int targetId = PersonalSpaceWorlds.id(serverLevel.dimension());
        if (targetId == 0 && active) targetId = targetPersonalId();
        boolean created = false;
        boolean changed = true;
        if (targetId > 0) {
            PersonalSpaceSettings real = PersonalSpaceDirectory.get(server).settings(targetId);
            if (real == null) return;
            changed = real.copyFrom(sanitized, true, real.getAllowGenerationChanges());
            real.setAllowGenerationChanges(false);
            PersonalSpaceWorlds.settingsChanged(server, targetId);
        } else {
            if (!serverLevel.dimension().equals(Level.OVERWORLD)) return;
            sanitized.setAllowGenerationChanges(false);
            int id = PersonalSpaceWorlds.create(server, sanitized);
            active = true;
            targetDimension = PersonalSpaceWorlds.key(id).location();
            targetPos = new BlockPos(targetPos.getX(), sanitized.getGroundLevel() + 1, targetPos.getZ());
            changed();
            created = true;
            linkOtherPortal(true, player);
        }
        SPersonalSpaceWorldList.syncAll(server);
        if (created) {
            player.sendSystemMessage(Component.translatable("chat.personalWorld.created"));
        } else if (changed) {
            player.sendSystemMessage(Component.translatable("chat.personalWorld.updated"));
        }
    }

    private static boolean decorationsAllowed(PersonalSpaceSettings settings) {
        var boundary = PersonalSpaceConfig.allowedBoundaryBlocks();
        var gap = PersonalSpaceConfig.allowedGapBlocks();
        var center = PersonalSpaceConfig.allowedCenterBlocks();
        return boundary.containsAll(settings.decorationBlocks(PersonalSpaceSettings.Category.BOUNDARY)) &&
                gap.containsAll(settings.decorationBlocks(PersonalSpaceSettings.Category.GAP)) &&
                center.containsAll(settings.decorationBlocks(PersonalSpaceSettings.Category.CENTER));
    }

    private void deny(ServerPlayer player, String reason) {
        player.sendSystemMessage(Component.translatable("chat.personalWorld.denied"));
        GTNACORE.LOGGER.warn("Player {} tried to modify PersonalSpace settings at {} ({}), denied - {}.",
                player.getGameProfile().getName(), worldPosition, level.dimension().location(), reason);
    }

    /** Item NBT keeps the link but not the placement facing, like the original's dropped portal. */
    public void saveToItem(ItemStack stack) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        tag.remove("facing");
        stack.getOrCreateTag().put("BlockEntityTag", tag);
    }

    private void changed() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("active", active);
        tag.putString("targetDimension", targetDimension.toString());
        tag.putIntArray("target", new int[] { targetPos.getX(), targetPos.getY(), targetPos.getZ() });
        tag.putInt("facing", facing.get3DDataValue());
        tag.putInt("targetFacing", targetFacing.get3DDataValue());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("active")) active = tag.getBoolean("active");
        if (tag.contains("targetDimension")) {
            ResourceLocation dimension = ResourceLocation.tryParse(tag.getString("targetDimension"));
            if (dimension != null) targetDimension = dimension;
        }
        int[] target = tag.getIntArray("target");
        if (target.length >= 3) targetPos = new BlockPos(target[0], target[1], target[2]);
        if (tag.contains("facing")) facing = horizontal(tag.getInt("facing"));
        if (tag.contains("targetFacing")) targetFacing = horizontal(tag.getInt("targetFacing"));
    }

    private static Direction horizontal(int value) {
        Direction direction = Direction.from3DDataValue(value);
        return direction.getAxis().isHorizontal() ? direction : DEFAULT_FACING;
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection network, ClientboundBlockEntityDataPacket packet) {
        if (packet.getTag() != null) load(packet.getTag());
        if (level != null && level.isClientSide) PersonalSpaceClientHooks.closePortalGui(this);
    }
}
