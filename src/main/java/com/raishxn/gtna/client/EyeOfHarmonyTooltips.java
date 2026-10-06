package com.raishxn.gtna.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import com.mojang.blaze3d.platform.InputConstants;
import com.raishxn.gtna.common.data.GTNAMachines;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/** Hover-scoped paging; Z has no effect on this manual when another item is being inspected. */
public final class EyeOfHarmonyTooltips {

    private static int page;
    private static boolean wasPressed;

    private EyeOfHarmonyTooltips() {}

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        if (!event.getItemStack().is(GTNAMachines.EYE_OF_HARMONY.getItem())) return;
        event.getToolTip().add(Component.translatable("gtna.eoh.manual.hint").withStyle(ChatFormatting.YELLOW));
        var client = Minecraft.getInstance();
        var font = client.font;
        int width = Math.max(100, Math.min(420, client.getWindow().getGuiScaledWidth() - 36));
        List<Component> lines = new ArrayList<>();
        for (String key : List.of("fields", "compression", "acceleration", "stabilisation", "energy", "circuit",
                "gas", "ratio", "penalty", "failure", "pity", "ports", "delivery", "scope")) {
            var paragraph = Component.empty();
            String translated = Component.translatable("gtna.eoh.manual." + key).getString();
            int heading = translated.indexOf(':');
            if (heading >= 0 && heading < 30 && !key.equals("penalty")) {
                paragraph
                        .append(Component.literal(translated.substring(0, heading + 1)).withStyle(ChatFormatting.BLUE));
                translated = translated.substring(heading + 1);
            }
            var numbers = java.util.regex.Pattern.compile("[0-9]+(?:[.,][0-9]+)*(?:%|\\^[-0-9]+)?|ALL|TODOS")
                    .matcher(translated);
            int previous = 0;
            while (numbers.find()) {
                paragraph.append(Component.literal(translated.substring(previous, numbers.start()))
                        .withStyle(key.equals("ratio") || key.equals("penalty") ? ChatFormatting.GREEN :
                                ChatFormatting.GRAY));
                paragraph.append(Component.literal(numbers.group()).withStyle(ChatFormatting.RED));
                previous = numbers.end();
            }
            paragraph.append(Component.literal(translated.substring(previous)).withStyle(
                    key.equals("ratio") || key.equals("penalty") ? ChatFormatting.GREEN : ChatFormatting.GRAY));
            for (var line : font.split(paragraph, width)) {
                var styledLine = Component.empty();
                line.accept((index, style, codePoint) -> {
                    styledLine.append(Component.literal(new String(Character.toChars(codePoint))).setStyle(style));
                    return true;
                });
                lines.add(styledLine);
            }
            lines.add(Component.empty());
        }
        // Reserve room for the native item tooltip, header and JEI/Minecraft footer lines.
        int capacity = Math.max(4,
                Math.min(18, (client.getWindow().getGuiScaledHeight() - 65) / 10 - event.getToolTip().size()));
        int count = (lines.size() + capacity - 1) / capacity;
        boolean pressed = InputConstants.isKeyDown(client.getWindow().getWindow(), GLFW.GLFW_KEY_Z);
        if (pressed && !wasPressed) page++;
        wasPressed = pressed;
        page = Math.floorMod(page, count);
        event.getToolTip()
                .add(Component.translatable("gtna.eoh.manual.page", page + 1, count).withStyle(ChatFormatting.GOLD));
        event.getToolTip().addAll(lines.subList(page * capacity, Math.min(lines.size(), (page + 1) * capacity)));
    }
}
