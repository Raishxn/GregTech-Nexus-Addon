package com.raishxn.gtna.client.hud;

import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.config.ConfigHolder;

/**
 * Wireless steam network HUD (GTOCore {@code WirelessEnergyHUD} parity for the steam pool).
 *
 * <p>
 * Shows the local player's pool balance, the last second of in/out flow, the connected hatch
 * counts and a balance sparkline. Like GTOCore's HUD it is <b>off by default</b> and toggled in the
 * client config ({@code wirelessSteamHud}); position and history length are config too. It only
 * appears once the server has sent a snapshot with an actual network (a hatch, a balance or flow).
 *
 * <p>
 * Client-only by construction: this class implements a {@code net.minecraftforge.client} interface
 * and is only registered on the client side of the mod bus.
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = GTNACORE.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class WirelessSteamHudOverlay implements IGuiOverlay {

    public static final WirelessSteamHudOverlay INSTANCE = new WirelessSteamHudOverlay();
    public static final String OVERLAY_ID = "wireless_steam_hud";

    private static final int GRAPH_WIDTH = 96;
    private static final int GRAPH_HEIGHT = 24;
    private static final int PADDING = 4;
    private static final int LINE_HEIGHT = 10;

    private static final int COLOR_BACKGROUND = 0x8A404040;
    private static final int COLOR_BORDER = 0xCC000000;
    private static final int COLOR_GRAPH_BACKGROUND = 0x40000000;
    private static final int COLOR_GRAPH_LINE = 0xFF4FC3F7;
    private static final int COLOR_GRAPH_FILL = 0x402ECC71;
    private static final int COLOR_TEXT = 0xFFFFFF;

    private WirelessSteamHudOverlay() {}

    @SubscribeEvent
    public static void onRegisterGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll(OVERLAY_ID, INSTANCE);
    }

    @Override
    public void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        ConfigHolder config = ConfigHolder.INSTANCE;
        if (config == null || !config.client.wirelessSteamHud) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.options.hideGui || mc.options.renderDebug) return;
        if (!WirelessSteamHudState.hasData() || !WirelessSteamHudState.hasNetwork()) return;

        Font font = mc.font;
        Component title = Component.translatable("gtna.hud.wireless_steam.balance",
                Component.literal(FormattingUtil.formatNumbers(WirelessSteamHudState.getBalance()))
                        .withStyle(ChatFormatting.AQUA));
        Component flow = Component.translatable("gtna.hud.wireless_steam.flow",
                Component.literal(FormattingUtil.formatNumbers(WirelessSteamHudState.getAddedPerSecond()))
                        .withStyle(ChatFormatting.GREEN),
                Component.literal(FormattingUtil.formatNumbers(WirelessSteamHudState.getConsumedPerSecond()))
                        .withStyle(ChatFormatting.RED));
        Component hatches = Component.translatable("gtna.hud.wireless_steam.hatches",
                WirelessSteamHudState.getInputHatches(), WirelessSteamHudState.getOutputHatches());

        Component[] lines = { title, flow, hatches };
        int textWidth = 0;
        for (Component line : lines) {
            textWidth = Math.max(textWidth, font.width(line));
        }
        int contentWidth = Math.max(GRAPH_WIDTH, textWidth);
        int historySeconds = config.client.wirelessSteamHudHistorySeconds;
        boolean drawGraph = historySeconds > 0;
        int graphHeight = drawGraph ? GRAPH_HEIGHT + 2 : 0;
        int width = contentWidth + PADDING * 2;
        int height = lines.length * LINE_HEIGHT + graphHeight + PADDING * 2;

        int x = Math.max(0, config.client.wirelessSteamHudX) * Math.max(0, screenWidth - width) / 100;
        int y = Math.max(0, config.client.wirelessSteamHudY) * Math.max(0, screenHeight - height) / 100;

        graphics.fill(x, y, x + width, y + height, COLOR_BACKGROUND);
        drawBorder(graphics, x, y, width, height, COLOR_BORDER);

        int textY = y + PADDING;
        graphics.drawString(font, title, x + PADDING, textY, COLOR_TEXT, true);
        textY += LINE_HEIGHT;
        graphics.drawString(font, flow, x + PADDING, textY, COLOR_TEXT, true);
        textY += LINE_HEIGHT;
        graphics.drawString(font, hatches, x + PADDING, textY, COLOR_TEXT, true);

        if (drawGraph) {
            int graphX = x + PADDING;
            int graphY = textY + LINE_HEIGHT - 2;
            graphics.fill(graphX, graphY, graphX + contentWidth, graphY + GRAPH_HEIGHT, COLOR_GRAPH_BACKGROUND);
            drawSparkline(graphics, graphX, graphY, contentWidth, GRAPH_HEIGHT,
                    WirelessSteamHudState.history(historySeconds));
            drawBorder(graphics, graphX, graphY, contentWidth, GRAPH_HEIGHT, COLOR_BORDER);
        }
    }

    /** Area/line sparkline of the balance history, scaled to the samples' own min/max. */
    private static void drawSparkline(GuiGraphics graphics, int x, int y, int width, int height, long[] samples) {
        if (samples.length < 2 || width <= 0 || height <= 0) return;
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        for (long value : samples) {
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        long range = max - min;
        int bottom = y + height - 1;
        int columns = samples.length;
        for (int i = 0; i < columns; i++) {
            int columnX = x + (int) ((long) i * (width - 1) / Math.max(1, columns - 1));
            int nextX = i == columns - 1 ? x + width :
                    x + (int) ((long) (i + 1) * (width - 1) / Math.max(1, columns - 1)) + 1;
            int valueY = range <= 0 ? y + height / 2 :
                    bottom - (int) ((samples[i] - min) * (height - 1) / range);
            graphics.fill(columnX, valueY, Math.max(columnX + 1, nextX), bottom, COLOR_GRAPH_FILL);
            graphics.fill(columnX, valueY, Math.max(columnX + 1, nextX), valueY + 1, COLOR_GRAPH_LINE);
        }
    }

    private static void drawBorder(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.hLine(x, x + width - 1, y, color);
        graphics.hLine(x, x + width - 1, y + height - 1, color);
        graphics.vLine(x, y, y + height - 1, color);
        graphics.vLine(x + width - 1, y, y + height - 1, color);
    }
}
