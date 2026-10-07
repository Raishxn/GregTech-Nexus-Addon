package com.raishxn.gtna.common.machine.multiblock.godforge.gui;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Textures and small widgets shared by the Forge of Gods pages (GTNH {@code ForgeOfGodsGuiUtil}). */
public final class GodforgeGui {

    private GodforgeGui() {}

    public static ResourceTexture tex(String name) {
        return new ResourceTexture("gtna:textures/gui/godforge/" + name + ".png");
    }

    public static final ResourceTexture BACKGROUND_STAR = tex("background_star");
    public static final ResourceTexture BACKGROUND_SPACE = tex("background_space");
    public static final ResourceTexture BUTTON_CELESTIAL = tex("button_celestial");
    public static final ResourceTexture BUTTON_SPACE = tex("button_purple");
    public static final ResourceTexture BUTTON_SPACE_PRESSED = tex("button_purple_pressed");
    public static final ResourceTexture LOGO = tex("picture_gorge_logo");
    public static final ResourceTexture THANKS = tex("picture_thanks");
    public static final ResourceTexture HEAT_SINK = tex("picture_heat_sink_16x8");
    public static final ResourceTexture RAINBOW_SQUARE = tex("picture_rainbow_square");
    public static final ResourceTexture SELECTOR = tex("picture_green_selector");
    public static final ResourceTexture BAR_BACKGROUND = tex("progressbar_godforge_progressbar_background");

    public static final ResourceTexture OVERLAY_FLAG = tex("overlay_button_flag");
    public static final ResourceTexture OVERLAY_HEAT = tex("overlay_button_heat_on");
    public static final ResourceTexture OVERLAY_BATTERY_ON = tex("overlay_button_battery_on");
    public static final ResourceTexture OVERLAY_BATTERY_OFF = tex("overlay_button_battery_off");
    public static final ResourceTexture OVERLAY_RAINBOW = tex("overlay_button_rainbow_spiral");
    public static final ResourceTexture OVERLAY_UPGRADES = tex("overlay_button_arrow_blue_up");
    public static final ResourceTexture OVERLAY_REFRESH = tex("overlay_button_cyclic_blue");
    public static final ResourceTexture OVERLAY_STATISTICS = tex("overlay_button_statistics");
    public static final ResourceTexture OVERLAY_EJECT = tex("overlay_button_eject");
    public static final ResourceTexture OVERLAY_EJECT_LOCKED = tex("overlay_button_eject_disabled");
    public static final ResourceTexture OVERLAY_HEART = tex("overlay_button_heart");

    public static final DecimalFormat DECIMAL = new DecimalFormat("#,##0.###");

    /** The four GTNH milestones: title key, progress unit key, glow background, symbol, bar textures. */
    public enum Milestone {

        CHARGE("powermilestone", "power", "charge", "red"),
        CONVERSION("recipemilestone", "recipes", "conversion", "purple"),
        CATALYST("fuelmilestone", "fuelconsumed", "catalyst", "blue"),
        COMPOSITION("purchasablemilestone", "extensions", "composition", "rainbow");

        public final String titleKey;
        public final String progressKey;
        public final ResourceTexture symbol;
        public final ResourceTexture glow;
        public final ResourceTexture bar;
        public final ResourceTexture barInverted;

        Milestone(String title, String progress, String symbol, String bar) {
            this.titleKey = "gtna.godforge.gui." + title;
            this.progressKey = "gtna.godforge.gui." + progress;
            this.symbol = tex("picture_milestone_" + symbol);
            this.glow = tex("picture_milestone_" + symbol + "_glow");
            this.bar = tex("progressbar_godforge_progressbar_" + bar);
            this.barInverted = tex("progressbar_godforge_progressbar_" + bar + "_inverted");
        }
    }

    public static ResourceTexture glow(com.raishxn.gtna.api.machine.feature.godforge.GodforgeUpgrade.Color color) {
        return tex("background_" + color.name().toLowerCase(java.util.Locale.ROOT) + "_glow");
    }

    public static ResourceTexture connector(com.raishxn.gtna.api.machine.feature.godforge.GodforgeUpgrade.Color color,
                                            boolean opaque) {
        return tex("picture_connector_" + color.name().toLowerCase(java.util.Locale.ROOT) + (opaque ? "_opaque" : ""));
    }

    public static ResourceTexture symbol(com.raishxn.gtna.api.machine.feature.godforge.GodforgeUpgrade.Symbol symbol) {
        return tex("picture_milestone_" + symbol.name().toLowerCase(java.util.Locale.ROOT));
    }

    /** A 16x16 celestial button with an overlay, like the GTNH side buttons. */
    public static WidgetGroup celestialButton(int x, int y, Supplier<IGuiTexture> overlay,
                                              Consumer<ClickData> onClick, Supplier<List<Component>> tooltip) {
        WidgetGroup group = new WidgetGroup(x, y, 16, 16);
        group.addWidget(new ImageWidget(0, 0, 16, 16, BUTTON_CELESTIAL));
        group.addWidget(new ImageWidget(0, 0, 16, 16, overlay));
        group.addWidget(new ButtonWidget(0, 0, 16, 16, IGuiTexture.EMPTY, onClick));
        group.addWidget(new TooltipArea(0, 0, 16, 16, tooltip));
        return group;
    }

    public static TextTexture text(String key, int color) {
        return new TextTexture(key, color).setDropShadow(false);
    }

    public static String format(long value) {
        return DECIMAL.format(value);
    }

    public static String format(BigInteger value) {
        return value.toString().length() > 15 ? new java.math.BigDecimal(value).round(new java.math.MathContext(4))
                .toString().replace("E+", "e") : DECIMAL.format(value);
    }

    public static String format(double value) {
        return DECIMAL.format(value);
    }

    /** Hover area with a dynamic tooltip, drawn in the foreground over its siblings. */
    public static class TooltipArea extends Widget {

        private final Supplier<List<Component>> tooltip;

        public TooltipArea(int x, int y, int width, int height, Supplier<List<Component>> tooltip) {
            super(x, y, width, height);
            this.tooltip = tooltip;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            if (!isMouseOverElement(mouseX, mouseY) || gui == null || gui.getModularUIGui() == null) return;
            List<Component> lines = tooltip.get();
            if (lines != null && !lines.isEmpty()) {
                gui.getModularUIGui().setHoverTooltip(lines, ItemStack.EMPTY, null, null);
            }
        }
    }

    /** Horizontal milestone bar filled from the left (GTNH progress bars). */
    public static class BarWidget extends Widget {

        private final Supplier<IGuiTexture> fill;
        private final Supplier<Float> progress;

        private final boolean fromRight;
        private final boolean background;

        public BarWidget(int x, int y, int width, int height, Supplier<IGuiTexture> fill, Supplier<Float> progress) {
            this(x, y, width, height, fill, progress, false, true);
        }

        public BarWidget(int x, int y, int width, int height, Supplier<IGuiTexture> fill, Supplier<Float> progress,
                         boolean fromRight, boolean background) {
            super(x, y, width, height);
            this.fill = fill;
            this.progress = progress;
            this.fromRight = fromRight;
            this.background = background;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            var pos = getPosition();
            var size = getSize();
            if (background) BAR_BACKGROUND.draw(graphics, mouseX, mouseY, pos.x, pos.y, size.width, size.height);
            float value = Math.max(0, Math.min(1, progress.get()));
            if (value <= 0) return;
            int filled = Math.round(size.width * value);
            if (fromRight) {
                graphics.enableScissor(pos.x + size.width - filled, pos.y, pos.x + size.width, pos.y + size.height);
            } else {
                graphics.enableScissor(pos.x, pos.y, pos.x + filled, pos.y + size.height);
            }
            fill.get().draw(graphics, mouseX, mouseY, pos.x, pos.y, size.width, size.height);
            graphics.disableScissor();
        }
    }

    /** Word-wrapped text (GTNH IKey widgets with a fixed width), optionally centered. */
    public static class WrappedText extends Widget {

        private final Supplier<Component> text;
        private final boolean centered;
        private final float scale;

        public WrappedText(int x, int y, int width, int height, Supplier<Component> text, boolean centered,
                           float scale) {
            super(x, y, width, height);
            this.text = text;
            this.centered = centered;
            this.scale = scale;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            var font = net.minecraft.client.Minecraft.getInstance().font;
            var pos = getPosition();
            int width = (int) (getSize().width / scale);
            graphics.pose().pushPose();
            graphics.pose().translate(pos.x, pos.y, 0);
            graphics.pose().scale(scale, scale, 1);
            int y = 0;
            for (var line : font.split(text.get(), width)) {
                int x = centered ? (width - font.width(line)) / 2 : 0;
                graphics.drawString(font, line, x, y, 0xFFFFFF, false);
                y += font.lineHeight;
            }
            graphics.pose().popPose();
        }
    }

    /**
     * All upgrade tree connectors in one widget, drawn before the nodes so the lines stay under them (GTNH draws
     * them on the background layer). Each line is a 6 px band from one node centre to the other.
     */
    public static class ConnectorLayer extends Widget {

        public record Line(int fromX, int fromY, int toX, int toY,
                           com.raishxn.gtna.api.machine.feature.godforge.GodforgeUpgrade from,
                           com.raishxn.gtna.api.machine.feature.godforge.GodforgeUpgrade to,
                           com.raishxn.gtna.api.machine.feature.godforge.GodforgeUpgrade.Color color) {}

        private final List<Line> lines;
        private final java.util.function.Predicate<com.raishxn.gtna.api.machine.feature.godforge.GodforgeUpgrade> active;

        public ConnectorLayer(int width, int height, List<Line> lines,
                              java.util.function.Predicate<com.raishxn.gtna.api.machine.feature.godforge.GodforgeUpgrade> active) {
            super(0, 0, width, height);
            this.lines = lines;
            this.active = active;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            var origin = getPosition();
            for (Line line : lines) {
                float dx = line.toX() - line.fromX(), dy = line.toY() - line.fromY();
                float length = (float) Math.sqrt(dx * dx + dy * dy);
                boolean opaque = active.test(line.from()) && active.test(line.to());
                var texture = new net.minecraft.resources.ResourceLocation("gtna", "textures/gui/godforge/" +
                        "picture_connector_" + line.color().name().toLowerCase(java.util.Locale.ROOT) +
                        (opaque ? "_opaque" : "") + ".png");
                graphics.pose().pushPose();
                graphics.pose().translate(origin.x + line.fromX(), origin.y + line.fromY(), 0);
                graphics.pose().mulPose(com.mojang.math.Axis.ZP.rotation((float) Math.atan2(dy, dx)));
                com.mojang.blaze3d.systems.RenderSystem.enableBlend();
                graphics.blit(texture, 0, -3, (int) length, 6, 0, 0, 1, 1, 1, 1);
                graphics.pose().popPose();
            }
        }
    }

    /** A fluid's still texture tinted with its colour, used for the GTNH fuel type buttons. */
    public static class FluidIcon extends Widget {

        private final Supplier<net.minecraft.world.level.material.Fluid> fluid;

        public FluidIcon(int x, int y, int width, int height,
                         Supplier<net.minecraft.world.level.material.Fluid> fluid) {
            super(x, y, width, height);
            this.fluid = fluid;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            var value = fluid.get();
            if (value == null) return;
            var ext = net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions.of(value);
            var sprite = net.minecraft.client.Minecraft.getInstance()
                    .getTextureAtlas(net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS)
                    .apply(ext.getStillTexture());
            int color = ext.getTintColor();
            var pos = getPosition();
            var size = getSize();
            com.mojang.blaze3d.systems.RenderSystem.setShaderColor(((color >> 16) & 255) / 255f,
                    ((color >> 8) & 255) / 255f, (color & 255) / 255f, 1f);
            graphics.blit(pos.x, pos.y, 0, size.width, size.height, sprite);
            com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }
    }

    /** Horizontal slider over a gradient (GTNH {@code SliderWidget} with a gradient background), client only. */
    public static class ColorSlider extends Widget {

        private final Supplier<int[]> gradient;
        private final Supplier<Double> value;
        private final java.util.function.DoubleConsumer setter;
        private final double max;
        private boolean dragging;

        /** {@code gradient} gives the start and end ARGB colours, or more stops for a hue bar. */
        public ColorSlider(int x, int y, int width, int height, double max, Supplier<int[]> gradient,
                           Supplier<Double> value, java.util.function.DoubleConsumer setter) {
            super(x, y, width, height);
            this.max = max;
            this.gradient = gradient;
            this.value = value;
            this.setter = setter;
            setClientSideWidget();
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            var pos = getPosition();
            var size = getSize();
            int[] stops = gradient.get();
            for (int px = 0; px < size.width; px++) {
                float t = size.width <= 1 ? 0 : (float) px / (size.width - 1);
                float scaled = t * (stops.length - 1);
                int idx = Math.min(stops.length - 2, (int) scaled);
                graphics.fill(pos.x + px, pos.y, pos.x + px + 1, pos.y + size.height,
                        lerpColor(stops[idx], stops[idx + 1], scaled - idx));
            }
            int marker = pos.x + (int) Math.round(Math.max(0, Math.min(1, value.get() / max)) * (size.width - 2));
            graphics.fill(marker, pos.y - 1, marker + 2, pos.y + size.height + 1, 0xFFFFFFFF);
        }

        private static int lerpColor(int a, int b, float t) {
            int r = (int) (((a >> 16) & 255) + (((b >> 16) & 255) - ((a >> 16) & 255)) * t);
            int g = (int) (((a >> 8) & 255) + (((b >> 8) & 255) - ((a >> 8) & 255)) * t);
            int bl = (int) ((a & 255) + ((b & 255) - (a & 255)) * t);
            return 0xFF000000 | r << 16 | g << 8 | bl;
        }

        private void set(double mouseX) {
            var pos = getPosition();
            double t = (mouseX - pos.x) / Math.max(1, getSize().width - 1);
            setter.accept(Math.max(0, Math.min(1, t)) * max);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!isMouseOverElement(mouseX, mouseY)) return false;
            dragging = true;
            set(mouseX);
            return true;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            if (!dragging) return false;
            set(mouseX);
            return true;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            dragging = false;
            return false;
        }
    }

    /** Client-only text field bound to editor state; refreshed from the state while not focused. */
    public static class EditorField extends com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget {

        private final Supplier<String> source;

        public EditorField(int x, int y, int width, int height, Supplier<String> source,
                           java.util.function.Consumer<String> sink) {
            super(x, y, width, height, null, sink);
            this.source = source;
            setClientSideWidget();
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void updateScreen() {
            super.updateScreen();
            if (!isFocus()) {
                String value = source.get();
                if (!value.equals(getCurrentString())) setCurrentString(value);
            }
        }
    }

    /** A button that sends a client-built string to the server (GTNH sync actions with a payload). */
    public static class PayloadButton extends Widget {

        private final Supplier<String> payload;
        private final java.util.function.Consumer<String> server;
        private final Runnable client;

        public PayloadButton(int x, int y, int width, int height, Supplier<String> payload,
                             java.util.function.Consumer<String> server, Runnable client) {
            super(x, y, width, height);
            this.payload = payload;
            this.server = server;
            this.client = client;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!isMouseOverElement(mouseX, mouseY)) return false;
            String value = payload.get();
            if (value != null) writeClientAction(1, buf -> buf.writeUtf(value, Short.MAX_VALUE));
            if (client != null) client.run();
            playButtonClickSound();
            return true;
        }

        @Override
        public void handleClientAction(int id, net.minecraft.network.FriendlyByteBuf buffer) {
            super.handleClientAction(id, buffer);
            if (id == 1) server.accept(buffer.readUtf(Short.MAX_VALUE));
        }
    }

    /** Swatch of a star colour: rainbow picture, four-corner gradient or flat first colour, as GTNH draws them. */
    public static class StarColorSwatch extends Widget {

        private final Supplier<com.raishxn.gtna.api.machine.feature.godforge.GodforgeStarColor> color;

        public StarColorSwatch(int x, int y, int size,
                               Supplier<com.raishxn.gtna.api.machine.feature.godforge.GodforgeStarColor> color) {
            super(x, y, size, size);
            this.color = color;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            var value = color.get();
            if (value == null || value.numColors() == 0) return;
            var pos = getPosition();
            int s = getSize().width;
            if (value == com.raishxn.gtna.api.machine.feature.godforge.GodforgeStarColor.RAINBOW) {
                RAINBOW_SQUARE.draw(graphics, mouseX, mouseY, pos.x, pos.y, s, s);
                return;
            }
            int[] corners = new int[4];
            if (value == com.raishxn.gtna.api.machine.feature.godforge.GodforgeStarColor.CLOUDS_PICK) {
                corners = new int[] { 0xFFFFFF00, 0xFF000000, 0xFF000000, 0xFF00FFFF };
            } else if (value == com.raishxn.gtna.api.machine.feature.godforge.GodforgeStarColor.MAYAS_PICK) {
                corners = new int[] { 0xFFFFACD2, 0xFFFFFFFF, 0xFF000000, 0xFF6DC9E1 };
            } else {
                var c = value.color(0);
                int rgb = 0xFF000000 | c.r() << 16 | c.g() << 8 | c.b();
                graphics.fill(pos.x, pos.y, pos.x + s, pos.y + s, rgb);
                return;
            }
            // top-left, top-right, bottom-left, bottom-right
            for (int py = 0; py < s; py++) {
                float ty = (float) py / Math.max(1, s - 1);
                int left = ColorSlider.lerpColor(corners[0], corners[2], ty);
                int right = ColorSlider.lerpColor(corners[1], corners[3], ty);
                for (int px = 0; px < s; px++) {
                    graphics.fill(pos.x + px, pos.y + py, pos.x + px + 1, pos.y + py + 1,
                            ColorSlider.lerpColor(left, right, (float) px / Math.max(1, s - 1)));
                }
            }
        }
    }

    /**
     * Server-backed text field that only reports edits the player types. LDLib's field otherwise answers its own
     * initial/refresh text (an empty field becomes the validator minimum), which reset star size and spin to 0.
     */
    public static class ServerField extends com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget {

        public ServerField(int x, int y, int width, int height, Supplier<String> supplier,
                           java.util.function.Consumer<String> responder) {
            super(x, y, width, height, supplier, responder);
        }

        @Override
        protected void onTextChanged(String newTextString) {
            if (isFocus()) super.onTextChanged(newTextString);
        }
    }
}
