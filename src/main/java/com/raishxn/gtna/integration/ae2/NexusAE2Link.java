package com.raishxn.gtna.integration.ae2;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.raishxn.gtna.GTNACORE;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

/**
 * Handles AE2 Wireless Access Point linking for the Nexus Structure Terminal.
 * Uses reflection to avoid hard dependency — works safely when AE2 is absent.
 *
 * <p>
 * Pattern inspired by GTCEu-Terminals' WirelessTerminalHandler.
 * </p>
 */
public final class NexusAE2Link {

    private static final String NBT_ACCESS_POINT = "accessPoint";

    private static boolean ae2Available = false;
    private static boolean ae2Checked = false;

    private NexusAE2Link() {}

    // ═══════════════════════════════════════════════════════════════════════════
    // AE2 AVAILABILITY
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Check if AE2 is loaded. Result is cached after first check.
     */
    public static boolean isAE2Available() {
        if (!ae2Checked) {
            try {
                Class.forName("appeng.api.networking.IGrid");
                ae2Available = true;
                GTNACORE.LOGGER.info("[GTNA] AE2 detected — ME Network linking enabled");
            } catch (ClassNotFoundException e) {
                ae2Available = false;
                GTNACORE.LOGGER.info("[GTNA] AE2 not found — ME Network linking disabled");
            }
            ae2Checked = true;
        }
        return ae2Available;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // WIRELESS ACCESS POINT DETECTION
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Check if the block entity at the given position is an AE2 Wireless Access Point.
     */
    public static boolean isWirelessAccessPoint(BlockEntity be) {
        if (!isAE2Available() || be == null) return false;
        try {
            Class<?> wapClass = Class.forName(
                    "appeng.api.implementations.blockentities.IWirelessAccessPoint");
            return wapClass.isInstance(be);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // LINKING (stores WAP position in item NBT)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Link the terminal to a Wireless Access Point at the given position.
     */
    public static void linkToAccessPoint(ItemStack stack, Level level, BlockPos pos) {
        GlobalPos globalPos = GlobalPos.of(level.dimension(), pos);
        CompoundTag tag = stack.getOrCreateTag();
        GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, globalPos)
                .result()
                .ifPresent(nbt -> tag.put(NBT_ACCESS_POINT, nbt));
    }

    /**
     * Remove the WAP link from the terminal.
     */
    public static void unlinkAccessPoint(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            tag.remove(NBT_ACCESS_POINT);
        }
    }

    /**
     * Check if the terminal is linked to a Wireless Access Point.
     */
    public static boolean isLinked(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(NBT_ACCESS_POINT);
    }

    /**
     * Get the linked GlobalPos, or null if not linked.
     */
    @Nullable
    public static GlobalPos getLinkedPosition(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(NBT_ACCESS_POINT)) return null;
        return GlobalPos.CODEC.parse(NbtOps.INSTANCE, tag.get(NBT_ACCESS_POINT))
                .result()
                .orElse(null);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // RANGE CHECKING
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Check if the player is in range of the linked WAP.
     * Returns false if not linked, wrong dimension, WAP destroyed, or out of range.
     */
    public static boolean isInRange(ItemStack stack, Level level, Player player) {
        if (!isAE2Available() || !isLinked(stack)) return false;

        GlobalPos globalPos = getLinkedPosition(stack);
        if (globalPos == null) return false;

        // Dimension check
        if (!level.dimension().equals(globalPos.dimension())) return false;

        BlockEntity be = level.getBlockEntity(globalPos.pos());
        if (be == null) return false;

        try {
            Class<?> wapClass = Class.forName(
                    "appeng.api.implementations.blockentities.IWirelessAccessPoint");
            if (!wapClass.isInstance(be)) return false;

            // Check if WAP is active
            Object wap = wapClass.cast(be);
            boolean isActive = (boolean) wapClass.getMethod("isActive").invoke(wap);
            if (!isActive) return false;

            // Check range
            double range = (double) wapClass.getMethod("getRange").invoke(wap);
            BlockPos playerPos = player.blockPosition();
            double distanceSq = globalPos.pos().distSqr(playerPos);

            return distanceSq <= (range * range);
        } catch (Exception e) {
            GTNACORE.LOGGER.debug("[GTNA] AE2 range check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get a human-readable status string for tooltip display.
     * Must be called client-side only.
     */
    public static String getStatusString(ItemStack stack, Level level, Player player) {
        if (!isAE2Available()) return "ae2_unavailable";
        if (!isLinked(stack)) return "not_linked";
        if (level == null || player == null) return "not_linked";
        if (isInRange(stack, level, player)) return "in_range";
        return "out_of_range";
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ITEMS EXTRACTION
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Try to extract one of the candidate items from the ME network.
     * Returns the extracted ItemStack or null if unsuccessful.
     */
    @Nullable
    public static ItemStack extractItem(ItemStack terminalStack, Level level, Player player,
                                        java.util.List<ItemStack> candidates) {
        if (!isInRange(terminalStack, level, player)) return null;

        GlobalPos globalPos = getLinkedPosition(terminalStack);
        if (globalPos == null) return null;

        BlockEntity be = level.getBlockEntity(globalPos.pos());
        if (be == null) return null;

        try {
            // Get the WAP
            Class<?> wapClass = Class.forName("appeng.api.implementations.blockentities.IWirelessAccessPoint");
            Object wap = wapClass.cast(be);

            // Get IGrid from WAP
            Object grid = wapClass.getMethod("getGrid").invoke(wap);
            if (grid == null) return null;

            // Get IStorageService from Grid
            Class<?> gridClass = Class.forName("appeng.api.networking.IGrid");
            Class<?> storageServiceClass = Class.forName("appeng.api.networking.storage.IStorageService");
            Object storageService = gridClass.getMethod("getService", Class.class).invoke(grid, storageServiceClass);
            if (storageService == null) return null;

            // Get MEStorage (inventory)
            Class<?> meStorageClass = Class.forName("appeng.api.storage.MEStorage");
            Object inventory = storageServiceClass.getMethod("getInventory").invoke(storageService);
            if (inventory == null) return null;

            // Setup parameters
            Class<?> aeItemKeyClass = Class.forName("appeng.api.stacks.AEItemKey");
            Class<?> actionableClass = Class.forName("appeng.api.config.Actionable");
            Object modulateAction = actionableClass.getField("MODULATE").get(null);

            Class<?> actionSourceClass = Class.forName("appeng.api.networking.security.IActionSource");
            Class<?> playerSourceClass = Class.forName("appeng.me.helpers.PlayerSource");
            Object actionSource = playerSourceClass.getConstructor(net.minecraft.world.entity.player.Player.class,
                    appeng.api.networking.security.IActionHost.class).newInstance(player, null);

            // Try to extract each candidate
            for (ItemStack candidate : candidates) {
                if (candidate.isEmpty()) continue;

                // AEItemKey key = AEItemKey.of(candidate);
                Object key = aeItemKeyClass.getMethod("of", net.minecraft.world.item.ItemStack.class).invoke(null,
                        candidate);
                if (key == null) continue;

                // long extract(AEKey what, long amount, Actionable mode, IActionSource source);
                Method extractMethod = meStorageClass.getMethod("extract", Class.forName("appeng.api.stacks.AEKey"),
                        long.class, actionableClass, actionSourceClass);
                long extractedAmount = (long) extractMethod.invoke(inventory, key, 1L, modulateAction, actionSource);

                if (extractedAmount > 0) {
                    GTNACORE.LOGGER.debug("[GTNA] AE2 Extracted 1x {}", candidate.getItem().getDescriptionId());
                    ItemStack extractedStack = candidate.copy();
                    extractedStack.setCount(1);
                    return extractedStack;
                }
            }
        } catch (Exception e) {
            GTNACORE.LOGGER.error("[GTNA] AE2 Extraction failed: {}", e.getMessage());
        }

        return null;
    }
}
