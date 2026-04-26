package com.raishxn.gtna.mixin.ae2;

import appeng.client.gui.widgets.CPUSelectionList;
import appeng.menu.me.crafting.CraftingStatusMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Locale;

@Mixin(value = CPUSelectionList.class, remap = false)
public abstract class CPUSelectionListMixin {

    private static final String[] GTNA_UNITS = { "B", "K", "M", "G", "T", "P", "E" };
    private static final String[] GTNA_COUNT_UNITS = { "", "K", "M", "B" };

    /**
     * @author GTNA
     * @reason AE2 15.4.10 renders CPU storage as raw KiB, which overflows the Nexus Hypercore row.
     */
    @Overwrite
    private String formatStorage(CraftingStatusMenu.CraftingCpuListEntry entry) {
        long storage = Math.max(0L, entry.storage());
        if (storage == Long.MAX_VALUE) {
            return "∞";
        }

        double value = storage;
        int unit = 0;
        while (value >= 1024.0D && unit < GTNA_UNITS.length - 1) {
            value /= 1024.0D;
            unit++;
        }

        if (value >= 100.0D || Math.abs(value - Math.rint(value)) < 0.0001D) {
            return String.format(Locale.US, "%.0f%s", value, GTNA_UNITS[unit]);
        }
        if (value >= 10.0D) {
            return String.format(Locale.US, "%.1f%s", value, GTNA_UNITS[unit]);
        }
        return String.format(Locale.US, "%.2f%s", value, GTNA_UNITS[unit]);
    }

    @Redirect(
            method = "drawBackgroundLayer",
            at = @At(value = "INVOKE", target = "Ljava/lang/String;valueOf(I)Ljava/lang/String;"))
    private String gtna$formatCoProcessors(int value) {
        return formatCount(value);
    }

    private static String formatCount(int value) {
        if (value == Integer.MAX_VALUE) {
            return "∞";
        }

        double readable = Math.max(0, value);
        int unit = 0;
        while (readable >= 1000.0D && unit < GTNA_COUNT_UNITS.length - 1) {
            readable /= 1000.0D;
            unit++;
        }

        if (readable >= 100.0D || Math.abs(readable - Math.rint(readable)) < 0.0001D) {
            return String.format(Locale.US, "%.0f%s", readable, GTNA_COUNT_UNITS[unit]);
        }
        if (readable >= 10.0D) {
            return String.format(Locale.US, "%.1f%s", readable, GTNA_COUNT_UNITS[unit]);
        }
        return String.format(Locale.US, "%.2f%s", readable, GTNA_COUNT_UNITS[unit]);
    }
}
