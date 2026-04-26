package com.raishxn.gtna.mixin.ae2;

import appeng.core.localization.Tooltips;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Locale;

@Mixin(value = Tooltips.class, remap = false)
public abstract class TooltipsMixin {

    private static final String[] GTNA_BYTE_UNITS = { "B", "KB", "MB", "GB", "TB", "PB", "EB" };

    /**
     * @author GTNA
     * @reason AE2 15.4.10 only ships byte tooltip units up to GB, which crashes the CPU screen for Nexus Hypercore CPUs.
     */
    @Overwrite
    public static Tooltips.Amount getByteAmount(long amount) {
        if (amount == Long.MAX_VALUE) {
            return new Tooltips.Amount("∞", "");
        }
        if (amount < 1000L) {
            return new Tooltips.Amount(Long.toString(amount), "");
        }

        double value = amount;
        int unit = 0;
        while (value >= 1000.0D && unit < GTNA_BYTE_UNITS.length - 1) {
            value /= 1024.0D;
            unit++;
        }

        return new Tooltips.Amount(formatAmount(value), GTNA_BYTE_UNITS[unit]);
    }

    private static String formatAmount(double value) {
        if (value >= 100.0D || Math.abs(value - Math.rint(value)) < 0.0001D) {
            return String.format(Locale.US, "%.0f", value);
        }
        if (value >= 10.0D) {
            return String.format(Locale.US, "%.1f", value);
        }
        return String.format(Locale.US, "%.2f", value);
    }
}
