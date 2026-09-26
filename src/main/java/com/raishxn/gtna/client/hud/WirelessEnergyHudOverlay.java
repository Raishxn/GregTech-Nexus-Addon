package com.raishxn.gtna.client.hud;

import net.minecraft.client.Minecraft;
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

/** Moveable, client-configurable readout of the player's Nexus Flux Matrix network. */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = GTNACORE.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class WirelessEnergyHudOverlay implements IGuiOverlay, IMoveableHud {

    public static final WirelessEnergyHudOverlay INSTANCE = new WirelessEnergyHudOverlay();
    private boolean dragging;
    private int startX;
    private int startY;
    private int pendingX;
    private int pendingY;

    private WirelessEnergyHudOverlay() {}

    @SubscribeEvent
    public static void register(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("wireless_energy_hud", INSTANCE);
        HudEditorScreen.register(INSTANCE);
    }

    @Override
    public void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int width, int height) {
        var mc = Minecraft.getInstance();
        if (isEnabled() && mc.level != null && !mc.options.hideGui && !mc.options.renderDebug &&
                WirelessEnergyHudState.hasNetwork())
            render(graphics, width, height);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("gtna.hud.wireless_energy.name");
    }

    @Override
    public void render(GuiGraphics graphics, int screenWidth, int screenHeight) {
        Rect2i bounds = getAnchorBounds(screenWidth, screenHeight);
        int x = bounds.getX();
        int y = bounds.getY();
        graphics.fill(x, y, x + bounds.getWidth(), y + bounds.getHeight(), 0x8A404040);
        IMoveableHud.drawOutline(graphics, bounds, 0xCC000000);
        var font = Minecraft.getInstance().font;
        Component[] lines = lines();
        for (int i = 0; i < lines.length; i++) graphics.drawString(font, lines[i], x + 4, y + 4 + i * 10, 0xFFFFFF);
    }

    private static Component[] lines() {
        return new Component[] {
                Component.translatable("gtna.hud.wireless_energy.balance", WirelessEnergyHudState.balance(),
                        WirelessEnergyHudState.capacity()),
                Component.translatable("gtna.hud.wireless_energy.flow", WirelessEnergyHudState.input(),
                        WirelessEnergyHudState.output()),
                Component.translatable("gtna.hud.wireless_energy.connections", WirelessEnergyHudState.connections()) };
    }

    @Override
    public Rect2i getBounds(int screenWidth, int screenHeight) {
        var font = Minecraft.getInstance().font;
        int width = 96;
        for (Component line : lines()) width = Math.max(width, font.width(line));
        width += 8;
        int height = 38;
        int x = Mth.clamp(ConfigHolder.INSTANCE.client.wirelessEnergyHudX, 0, 100) *
                Math.max(0, screenWidth - width) / 100;
        int y = Mth.clamp(ConfigHolder.INSTANCE.client.wirelessEnergyHudY, 0, 100) *
                Math.max(0, screenHeight - height) / 100;
        return new Rect2i(x, y, width, height);
    }

    @Override
    public Rect2i getAnchorBounds(int width, int height) {
        Rect2i bounds = getBounds(width, height);
        return new Rect2i(bounds.getX() + pendingX, bounds.getY() + pendingY,
                bounds.getWidth(), bounds.getHeight());
    }

    @Override
    public boolean isEnabled() {
        return ConfigHolder.INSTANCE.client.wirelessEnergyHud;
    }

    @Override
    public void setEnabled(boolean enabled) {
        HudConfigValues.set("wirelessEnergyHud", enabled);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || !isMouseOver(mouseX, mouseY)) return false;
        dragging = true;
        startX = (int) mouseX;
        startY = (int) mouseY;
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!dragging) return false;
        pendingX = (int) mouseX - startX;
        pendingY = (int) mouseY - startY;
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!dragging) return false;
        var window = Minecraft.getInstance().getWindow();
        Rect2i bounds = getBounds(window.getGuiScaledWidth(), window.getGuiScaledHeight());
        setTopLeftPosition(bounds.getX() + pendingX, bounds.getY() + pendingY,
                window.getGuiScaledWidth(), window.getGuiScaledHeight());
        dragging = false;
        pendingX = pendingY = 0;
        return true;
    }

    @Override
    public boolean isPositionDragging() {
        return dragging;
    }

    @Override
    public void setTopLeftPosition(int x, int y, int screenWidth, int screenHeight) {
        Rect2i bounds = getBounds(screenWidth, screenHeight);
        int maxX = Math.max(0, screenWidth - bounds.getWidth());
        int maxY = Math.max(0, screenHeight - bounds.getHeight());
        HudConfigValues.set("wirelessEnergyHudX", maxX == 0 ? 0 : Mth.clamp(x, 0, maxX) * 100 / maxX);
        HudConfigValues.set("wirelessEnergyHudY", maxY == 0 ? 0 : Mth.clamp(y, 0, maxY) * 100 / maxY);
    }
}
