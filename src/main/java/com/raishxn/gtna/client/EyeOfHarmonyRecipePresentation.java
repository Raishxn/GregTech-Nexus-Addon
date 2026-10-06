package com.raishxn.gtna.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import com.raishxn.gtna.common.data.GTNAEyeOfHarmonyContent;
import com.raishxn.gtna.common.data.multiblock.EyeOfHarmonyDisplay.Page;

import java.text.NumberFormat;
import java.util.Locale;

/** Shared recipe information, with no dependency on a specific recipe viewer. */
public final class EyeOfHarmonyRecipePresentation {

    private EyeOfHarmonyRecipePresentation() {}

    public static int rowsForScreen() {
        return Math.max(4, Math.min(11, (Minecraft.getInstance().getWindow().getGuiScaledHeight() - 204) / 18));
    }

    public static void drawInfo(Page page, GuiGraphics graphics) {
        drawInfo(page, graphics, 0xff000000);
    }

    public static void drawInfo(Page page, GuiGraphics graphics, int color) {
        int y = 40 + page.rows() * 18;
        var program = page.catalog().program();
        text(graphics, color, Component.translatable("gtna.eoh.jei.page", page.number(), page.total()), 30, 8);
        text(graphics, color, Component.translatable("gtna.eoh.jei.time", exact(program.baseTicks())), 8, y);
        text(graphics, color, Component.translatable("gtna.eoh.jei.hydrogen", exactBuckets(program.hydrogen())), 8,
                y + 10);
        text(graphics, color, Component.translatable("gtna.eoh.jei.helium", exactBuckets(program.helium())), 8, y + 20);
        text(graphics, color, Component.translatable("gtna.eoh.jei.tier",
                Component.literal(GTNAEyeOfHarmonyContent.TIER_NAMES[program.requiredCompression()])
                        .withStyle(net.minecraft.ChatFormatting.BOLD)),
                8, y + 30);
        text(graphics, color, Component.translatable("gtna.eoh.jei.input", compact(program.startupEU())), 8, y + 40);
        text(graphics, color, Component.translatable("gtna.eoh.jei.output", compact(program.returnEU())), 8, y + 50);
        text(graphics, color, Component.translatable("gtna.eoh.jei.chance", (int) (program.baseChance() * 100)), 8,
                y + 60);
        text(graphics, color,
                Component.translatable("gtna.eoh.jei.efficiency",
                        Math.round(program.startupEU() == 0 ? 0 : 100D * program.returnEU() / program.startupEU())),
                8, y + 70);
        if (page.total() > 1 || page.products().size() > page.rows() * 9) text(graphics, color, Component
                .translatable("gtna.eoh.jei.warning", Math.min(page.products().size(), page.rows() * 9),
                        page.catalog().products().getList("items", 10).size() +
                                page.catalog().products().getList("fluids", 10).size())
                .withStyle(net.minecraft.ChatFormatting.RED), 8, y + 80);
        text(graphics, color, Component.translatable("gtna.eoh.jei.base"), 8, y + 94);
    }

    /** Tall EMI layout: planet and gases, full-width product grid, then program information. */
    public static void drawEmiInfo(Page page, GuiGraphics graphics) {
        int color = 0xff000000;
        text(graphics, color, Component.translatable("gtna.eoh.jei.page", page.number(), page.total()), 18, 29);
        int x = 8;
        int y = 52 + page.rows() * 18;
        var font = Minecraft.getInstance().font;
        for (var line : emiInfoLines(page)) {
            for (var wrapped : font.split(line, 182)) {
                graphics.drawString(font, wrapped, x, y, color, false);
                y += 10;
            }
        }
        int total = page.catalog().products().getList("items", 10).size() +
                page.catalog().products().getList("fluids", 10).size();
        var warning = (page.products().size() < total ?
                Component.translatable("gtna.eoh.jei.warning", page.products().size(), total) :
                Component.translatable("gtna.eoh.jei.warning_base"))
                .withStyle(net.minecraft.ChatFormatting.RED);
        for (var line : font.split(warning, 182)) {
            graphics.drawString(font, line, x, y, color, false);
            y += 10;
        }
    }

    /** Reserve actual translated line height, including room for the navigation buttons. */
    public static int emiInfoHeight(Page page) {
        var font = Minecraft.getInstance().font;
        int height = 52 + 22;
        for (var line : emiInfoLines(page)) height += font.split(line, 182).size() * 10;
        // Reserve the warning even when the registered page fits: a shorter screen can reflow it.
        var warning = Component.translatable("gtna.eoh.jei.warning", page.products().size(),
                page.catalog().products().getList("items", 10).size() +
                        page.catalog().products().getList("fluids", 10).size());
        return height + Math.max(font.split(warning, 182).size(),
                font.split(Component.translatable("gtna.eoh.jei.warning_base"), 182).size()) * 10;
    }

    private static Component[] emiInfoLines(Page page) {
        var program = page.catalog().program();
        return new Component[] {
                Component.translatable("gtna.eoh.jei.time", exact(program.baseTicks())),
                Component.translatable("gtna.eoh.jei.hydrogen", exactBuckets(program.hydrogen())),
                Component.translatable("gtna.eoh.jei.helium", exactBuckets(program.helium())),
                Component.translatable("gtna.eoh.jei.tier",
                        GTNAEyeOfHarmonyContent.TIER_NAMES[program.requiredCompression()]),
                Component.translatable("gtna.eoh.jei.input", compact(program.startupEU())),
                Component.translatable("gtna.eoh.jei.output", compact(program.returnEU())),
                Component.translatable("gtna.eoh.jei.chance", (int) (program.baseChance() * 100)),
                Component.translatable("gtna.eoh.jei.efficiency",
                        Math.round(program.startupEU() == 0 ? 0 : 100D * program.returnEU() / program.startupEU()))
        };
    }

    private static void text(GuiGraphics graphics, int color, Component text, int x, int y) {
        var font = Minecraft.getInstance().font;
        float scale = Math.min(1F, 164F / Math.max(1, font.width(text)));
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(scale, scale, 1);
        graphics.drawString(font, text, 0, 0, color, false);
        graphics.pose().popPose();
    }

    public static String compact(long amount) {
        String[] suffixes = { "", "k", "M", "G", "T", "P", "E" };
        double value = amount;
        int tier = 0;
        while (value >= 1000 && tier < suffixes.length - 1) {
            value /= 1000;
            tier++;
        }
        return tier == 0 ? Long.toString(amount) : String.format(Locale.ROOT, "%.3g%s", value, suffixes[tier]);
    }

    /** Bucket units in presentation only; indexed stacks and machine queues always retain exact mB. */
    public static String compactBuckets(long milliBuckets) {
        return compactDecimal(java.math.BigDecimal.valueOf(milliBuckets, 3)) + "B";
    }

    public static String exactBuckets(long milliBuckets) {
        var format = NumberFormat.getNumberInstance(Locale.US);
        format.setMaximumFractionDigits(3);
        return format.format(java.math.BigDecimal.valueOf(milliBuckets, 3));
    }

    private static String compactDecimal(java.math.BigDecimal value) {
        String[] suffixes = { "", "k", "M", "G", "T", "P", "E" };
        int tier = 0;
        while (value.compareTo(java.math.BigDecimal.valueOf(1000)) >= 0 && tier < suffixes.length - 1) {
            value = value.movePointLeft(3);
            tier++;
        }
        return value.round(new java.math.MathContext(3)).stripTrailingZeros().toPlainString() + suffixes[tier];
    }

    public static String exact(long amount) {
        return NumberFormat.getIntegerInstance(Locale.US).format(amount);
    }
}
