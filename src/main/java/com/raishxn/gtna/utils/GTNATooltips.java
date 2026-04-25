package com.raishxn.gtna.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Locale;

public final class GTNATooltips {

    private GTNATooltips() {}

    public static MutableComponent desc(String key, Object... args) {
        return translatable(key, ChatFormatting.GRAY, args);
    }

    public static MutableComponent info(String key, Object... args) {
        return translatable(key, ChatFormatting.AQUA, args);
    }

    public static MutableComponent benefit(String key, Object... args) {
        return translatable(key, ChatFormatting.GREEN, args);
    }

    public static MutableComponent important(String key, Object... args) {
        return translatable(key, ChatFormatting.GOLD, args);
    }

    public static MutableComponent warning(String key, Object... args) {
        return translatable(key, ChatFormatting.RED, args);
    }

    public static MutableComponent structure(String key, Object... args) {
        return translatable(key, ChatFormatting.DARK_GRAY, args);
    }

    public static MutableComponent descLiteral(String text) {
        return Component.literal(text).withStyle(ChatFormatting.GRAY);
    }

    public static MutableComponent benefitLiteral(String text) {
        return Component.literal(text).withStyle(ChatFormatting.GREEN);
    }

    public static MutableComponent importantLiteral(String text) {
        return Component.literal(text).withStyle(ChatFormatting.GOLD);
    }

    public static MutableComponent warningLiteral(String text) {
        return Component.literal(text).withStyle(ChatFormatting.RED);
    }

    public static MutableComponent structureLiteral(String text) {
        return Component.literal(text).withStyle(ChatFormatting.DARK_GRAY);
    }

    public static MutableComponent style(Component component, ChatFormatting color) {
        return component.copy().withStyle(color);
    }

    public static MutableComponent auto(String key, Object... args) {
        String normalized = key.toLowerCase(Locale.ROOT);
        if (matches(normalized, ".desc", ".tooltip", ".tooltip_0", ".tooltip_1")) {
            return desc(key, args);
        }
        if (contains(normalized, "speed", "parallel", "efficiency", "boost", "bonus", "output", "production",
                "power")) {
            return benefit(key, args);
        }
        if (contains(normalized, "capacity", "slots", "tier", "range", "size", "count", "amount", "sunlit")) {
            return info(key, args);
        }
        if (contains(normalized, "structure", "hint", "note", "max_size", "how_to_link", "break_persist",
                "specialization_pending", "middle_click")) {
            return structure(key, args);
        }
        if (contains(normalized, "warning", "not_bound", "out_of_range", "insufficient")) {
            return warning(key, args);
        }
        if (contains(normalized, "usage", "mode", "special", "assembly", "marker", "cost", "steam", "require",
                "main_function", "fluid_req")) {
            return important(key, args);
        }
        return info(key, args);
    }

    private static MutableComponent translatable(String key, ChatFormatting color, Object... args) {
        return Component.translatable(key, args).withStyle(color);
    }

    private static boolean matches(String value, String... suffixes) {
        for (String suffix : suffixes) {
            if (value.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    private static boolean contains(String value, String... snippets) {
        for (String snippet : snippets) {
            if (value.contains(snippet)) {
                return true;
            }
        }
        return false;
    }
}
