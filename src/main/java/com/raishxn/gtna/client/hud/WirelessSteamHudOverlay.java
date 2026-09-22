package com.raishxn.gtna.client.hud;

import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
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
 * It is also an {@link IMoveableHud}: the HUD editor ({@link HudEditorScreen}, opened with the
 * GTNA keybind) can drag it around and toggles it, persisting both back to the config.
 *
 * <p>
 * Client-only by construction: this class implements {@code net.minecraftforge.client} interfaces
 * and is only registered on the client side of the mod bus.
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = GTNACORE.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class WirelessSteamHudOverlay implements IGuiOverlay, IMoveableHud {

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

    private boolean dragging;
    private int dragStartX;
    private int dragStartY;
    private int pendingX;
    private int pendingY;

    private WirelessSteamHudOverlay() {}

    @SubscribeEvent
    public static void onRegisterGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll(OVERLAY_ID, INSTANCE);
        HudEditorScreen.register(INSTANCE);
    }

    // ------------------------------------------------------------------
    // IGuiOverlay: the in-game render path.
    // ------------------------------------------------------------------

    @Override
    public void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        if (!isEnabled()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.options.hideGui || mc.options.renderDebug) return;
        if (!WirelessSteamHudState.hasData() || !WirelessSteamHudState.hasNetwork()) return;
        render(graphics, screenWidth, screenHeight);
    }

    // ------------------------------------------------------------------
    // IMoveableHud
    // ------------------------------------------------------------------

    @Override
    public Component getDisplayName() {
        return Component.translatable("gtna.hud.wireless_steam.name");
    }

    @Override
    public void render(GuiGraphics graphics, int screenWidth, int screenHeight) {
        ConfigHolder config = ConfigHolder.INSTANCE;
        if (config == null) return;
        Font font = Minecraft.getInstance().font;
        Component[] lines = lines();
        int contentWidth = contentWidth(font, lines);
        boolean drawGraph = config.client.wirelessSteamHudHistorySeconds > 0;
        int width = contentWidth + PADDING * 2;
        int height = panelHeight(config, lines.length);

        Rect2i base = getBounds(screenWidth, screenHeight);
        int x = base.getX() + (isPositionDragging() ? pendingX : 0);
        int y = base.getY() + (isPositionDragging() ? pendingY : 0);

        graphics.fill(x, y, x + width, y + height, COLOR_BACKGROUND);
        IMoveableHud.drawOutline(graphics, new Rect2i(x, y, width, height), COLOR_BORDER);

        int textY = y + PADDING;
        for (Component line : lines) {
            graphics.drawString(font, line, x + PADDING, textY, COLOR_TEXT, true);
            textY += LINE_HEIGHT;
        }

        if (drawGraph) {
            int graphX = x + PADDING;
            int graphY = textY + 2;
            graphics.fill(graphX, graphY, graphX + contentWidth, graphY + GRAPH_HEIGHT, COLOR_GRAPH_BACKGROUND);
            drawSparkline(graphics, graphX, graphY, contentWidth, GRAPH_HEIGHT,
                    WirelessSteamHudState.history(config.client.wirelessSteamHudHistorySeconds));
            IMoveableHud.drawOutline(graphics, new Rect2i(graphX, graphY, contentWidth, GRAPH_HEIGHT), COLOR_BORDER);
        }
    }

    @Override
    public Rect2i getBounds(int screenWidth, int screenHeight) {
        ConfigHolder config = ConfigHolder.INSTANCE;
        Minecraft mc = Minecraft.getInstance();
        if (config == null || mc.font == null) return new Rect2i(0, 0, 0, 0);
        Component[] lines = lines();
        int width = contentWidth(mc.font, lines) + PADDING * 2;
        int height = panelHeight(config, lines.length);
        int x = clampPercent(config.client.wirelessSteamHudX) * Math.max(0, screenWidth - width) / 100;
        int y = clampPercent(config.client.wirelessSteamHudY) * Math.max(0, screenHeight - height) / 100;
        return new Rect2i(x, y, width, height);
    }

    @Override
    public Rect2i getAnchorBounds(int screenWidth, int screenHeight) {
        Rect2i bounds = getBounds(screenWidth, screenHeight);
        return new Rect2i(bounds.getX() + pendingX, bounds.getY() + pendingY,
                bounds.getWidth(), bounds.getHeight());
    }

    @Override
    public boolean isEnabled() {
        return ConfigHolder.INSTANCE != null && ConfigHolder.INSTANCE.client.wirelessSteamHud;
    }

    @Override
    public void setEnabled(boolean enabled) {
        HudConfigValues.set("wirelessSteamHud", enabled);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isMouseOver(mouseX, mouseY)) {
            dragging = true;
            dragStartX = (int) mouseX;
            dragStartY = (int) mouseY;
            pendingX = 0;
            pendingY = 0;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!dragging) return false;
        pendingX = (int) (mouseX - dragStartX);
        pendingY = (int) (mouseY - dragStartY);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean moved = pendingX != 0 || pendingY != 0;
        boolean handled = dragging || moved;
        if (moved) {
            Minecraft mc = Minecraft.getInstance();
            int screenWidth = mc.getWindow().getGuiScaledWidth();
            int screenHeight = mc.getWindow().getGuiScaledHeight();
            Rect2i bounds = getBounds(screenWidth, screenHeight);
            setTopLeftPosition(bounds.getX() + pendingX, bounds.getY() + pendingY, screenWidth, screenHeight);
        }
        pendingX = 0;
        pendingY = 0;
        dragging = false;
        return handled;
    }

    @Override
    public boolean isPositionDragging() {
        return dragging || pendingX != 0 || pendingY != 0;
    }

    @Override
    public void setTopLeftPosition(int x, int y, int screenWidth, int screenHeight) {
        Rect2i bounds = getBounds(screenWidth, screenHeight);
        int maxX = Math.max(0, screenWidth - bounds.getWidth());
        int maxY = Math.max(0, screenHeight - bounds.getHeight());
        int clampedX = Mth.clamp(x, 0, maxX);
        int clampedY = Mth.clamp(y, 0, maxY);
        int percentX = maxX <= 0 ? 0 : (int) Math.round(clampedX * 100.0 / maxX);
        int percentY = maxY <= 0 ? 0 : (int) Math.round(clampedY * 100.0 / maxY);
        HudConfigValues.set("wirelessSteamHudX", Mth.clamp(percentX, 0, 100));
        HudConfigValues.set("wirelessSteamHudY", Mth.clamp(percentY, 0, 100));
    }

    // ------------------------------------------------------------------
    // Layout helpers.
    // ------------------------------------------------------------------

    private static Component[] lines() {
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
        return new Component[] { title, flow, hatches };
    }

    private static int contentWidth(Font font, Component[] lines) {
        int textWidth = 0;
        for (Component line : lines) {
            textWidth = Math.max(textWidth, font.width(line));
        }
        return Math.max(GRAPH_WIDTH, textWidth);
    }

    private static int panelHeight(ConfigHolder config, int lineCount) {
        int graphHeight = config.client.wirelessSteamHudHistorySeconds > 0 ? GRAPH_HEIGHT + 2 : 0;
        return lineCount * LINE_HEIGHT + graphHeight + PADDING * 2;
    }

    private static int clampPercent(int percent) {
        return Mth.clamp(percent, 0, 100);
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
}
