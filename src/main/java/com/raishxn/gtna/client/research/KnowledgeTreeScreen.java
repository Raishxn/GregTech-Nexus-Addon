package com.raishxn.gtna.client.research;

import com.gregtechceu.gtceu.api.gui.GuiTextures;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import com.raishxn.gtna.network.GTNANetworkHandler;
import com.raishxn.gtna.network.packet.CKnowledgePurchase;
import com.raishxn.gtna.research.ClientKnowledge;
import com.raishxn.gtna.research.KnowledgeNode;
import com.raishxn.gtna.research.KnowledgeTreeLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * The research tree, drawn with GTCEu's GUI textures so it sits next to the machine screens: a grey panel,
 * the tree on a dark display that can be dragged and scrolled, the team's research points per area in the
 * title bar, and a side panel for the selected node with its cost, requirement, eureka, discovery text and a
 * Research button. Reads only {@link ClientKnowledge}, which the server keeps up to date, so it refreshes while
 * open; buying sends {@link CKnowledgePurchase} and the server checks everything again.
 * <p>
 * Packs can skin it: {@code <node namespace>:textures/gui/research/frame_<kind>.png} (24x24) frames a node of
 * that kind, and {@code <area namespace>:textures/gui/research/area_<area path>.png} (16x16) is an area's icon.
 * Missing textures fall back to the GTCEu slot and to the area's name.
 */
@OnlyIn(Dist.CLIENT)
public class KnowledgeTreeScreen extends Screen {

    private static final int NODE = 24;
    private static final int COLUMN_WIDTH = 78;
    private static final int ROW_HEIGHT = 48;
    private static final int MARGIN = 12;
    private static final int HEADER_HEIGHT = 18;

    private static final int PANEL_PADDING = 6;
    private static final int TITLE_HEIGHT = 20;
    private static final int SIDE_WIDTH = 132;
    private static final int BUTTON_HEIGHT = 18;

    private static final int GREEN = 0xFF55FF55;
    private static final int YELLOW = 0xFFFFD84A;
    private static final int GREY = 0xFF5A5A5A;
    private static final int LABEL = 0xFF404040;

    private enum State {
        UNLOCKED,
        AVAILABLE,
        LOCKED
    }

    private static final Map<ResourceLocation, Boolean> TEXTURE_EXISTS = new HashMap<>();

    private KnowledgeTreeLayout.Layout layout = KnowledgeTreeLayout.Layout.EMPTY;
    private int layoutSize = -1;
    private double offsetX;
    private double offsetY;
    private double dragged;
    @Nullable
    private ResourceLocation selected;

    // panel, tree viewport and side panel, recomputed in init()
    private int left, top, right, bottom;
    private int viewLeft, viewTop, viewRight, viewBottom;
    private int sideLeft;

    public KnowledgeTreeScreen() {
        super(Component.translatable("gtna.research.screen.title"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        super.init();
        left = 8;
        top = 8;
        right = width - 8;
        bottom = height - 8;
        viewLeft = left + PANEL_PADDING;
        viewTop = top + TITLE_HEIGHT;
        sideLeft = right - PANEL_PADDING - SIDE_WIDTH;
        viewRight = sideLeft - 4;
        viewBottom = bottom - PANEL_PADDING;
        relayout();
        offsetX = 0;
        offsetY = 0;
        TEXTURE_EXISTS.clear();
    }

    private void relayout() {
        layout = KnowledgeTreeLayout.of(ClientKnowledge.nodes());
        layoutSize = ClientKnowledge.nodes().size();
    }

    // ------------------------------------------------------------------ input

    private boolean inView(double mouseX, double mouseY) {
        return mouseX >= viewLeft && mouseX < viewRight && mouseY >= viewTop && mouseY < viewBottom;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        dragged = 0;
        if (button == 0 && buttonActive() && overButton(mouseX, mouseY)) {
            var node = selectedView();
            if (node.isPresent()) {
                GTNANetworkHandler.CHANNEL.sendToServer(new CKnowledgePurchase(node.get().id()));
                Minecraft.getInstance().getSoundManager()
                        .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && dragged < 3 && inView(mouseX, mouseY)) {
            var node = nodeAt(mouseX, mouseY);
            if (node != null) selected = node.id();
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button != 0 || !inView(mouseX, mouseY)) return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        dragged += Math.abs(dragX) + Math.abs(dragY);
        offsetX += dragX;
        offsetY += dragY;
        clampOffset();
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!inView(mouseX, mouseY)) return super.mouseScrolled(mouseX, mouseY, delta);
        if (hasShiftDown()) offsetX += delta * 24;
        else offsetY += delta * 24;
        clampOffset();
        return true;
    }

    private void clampOffset() {
        double contentWidth = layout.columns() * COLUMN_WIDTH + 2 * MARGIN;
        double contentHeight = layout.rows() * ROW_HEIGHT + HEADER_HEIGHT + 2 * MARGIN + 18;
        int viewWidth = viewRight - viewLeft, viewHeight = viewBottom - viewTop;
        offsetX = Math.min(0, Math.max(offsetX, Math.min(0, viewWidth - contentWidth)));
        offsetY = Math.min(0, Math.max(offsetY, Math.min(0, viewHeight - contentHeight)));
    }

    // ------------------------------------------------------------------ geometry and state

    private int nodeX(KnowledgeTreeLayout.Slot slot) {
        return viewLeft + (int) offsetX + MARGIN + slot.column() * COLUMN_WIDTH + (COLUMN_WIDTH - NODE) / 2;
    }

    private int nodeY(KnowledgeTreeLayout.Slot slot) {
        return viewTop + (int) offsetY + MARGIN + HEADER_HEIGHT + 10 + slot.row() * ROW_HEIGHT;
    }

    @Nullable
    private ClientKnowledge.NodeView nodeAt(double mouseX, double mouseY) {
        for (var node : ClientKnowledge.nodes()) {
            var slot = layout.slots().get(node.id());
            if (slot == null) continue;
            int x = nodeX(slot), y = nodeY(slot);
            if (mouseX >= x && mouseX < x + NODE && mouseY >= y && mouseY < y + NODE) return node;
        }
        return null;
    }

    private Optional<ClientKnowledge.NodeView> selectedView() {
        return selected == null ? Optional.empty() : ClientKnowledge.view(selected);
    }

    private static State stateOf(ClientKnowledge.NodeView node) {
        if (ClientKnowledge.isUnlocked(node.id())) return State.UNLOCKED;
        boolean ready = node.prerequisites().stream().allMatch(ClientKnowledge::isUnlocked);
        return ready ? State.AVAILABLE : State.LOCKED;
    }

    /** The node's cost after the eureka discount, mirroring {@link KnowledgeNode#cost(boolean)}. */
    private static Map<ResourceLocation, Integer> costOf(ClientKnowledge.NodeView node) {
        if (node.eureka().isEmpty() || !ClientKnowledge.eurekaMet(node.id())) return node.cost();
        double keep = 1.0 - node.eureka().get().reduction();
        Map<ResourceLocation, Integer> reduced = new LinkedHashMap<>();
        node.cost().forEach((area, amount) -> {
            int value = (int) Math.ceil(amount * keep - 1e-9);
            if (value > 0) reduced.put(area, value);
        });
        return reduced;
    }

    private static boolean affordable(Map<ResourceLocation, Integer> cost) {
        return cost.entrySet().stream()
                .allMatch(e -> ClientKnowledge.points().getOrDefault(e.getKey(), 0L) >= e.getValue());
    }

    private static boolean requirementOk(ClientKnowledge.NodeView node) {
        return node.triggerItem().isEmpty() || ClientKnowledge.requirementMet(node.id());
    }

    /** True when the server would accept a purchase, as far as the client knows. */
    private static boolean canBuy(ClientKnowledge.NodeView node) {
        return node.purchasable() && stateOf(node) == State.AVAILABLE && requirementOk(node) &&
                affordable(costOf(node));
    }

    // ------------------------------------------------------------------ rendering

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (ClientKnowledge.nodes().size() != layoutSize) {
            relayout();
            clampOffset();
        }
        renderBackground(graphics);
        GuiTextures.BACKGROUND.draw(graphics, mouseX, mouseY, left, top, right - left, bottom - top);
        graphics.drawString(font, title, left + PANEL_PADDING + 2, top + 7, LABEL, false);
        drawPoints(graphics);

        GuiTextures.DISPLAY.draw(graphics, mouseX, mouseY, viewLeft, viewTop, viewRight - viewLeft,
                viewBottom - viewTop);
        ClientKnowledge.NodeView hovered = null;
        if (layout.slots().isEmpty()) {
            graphics.drawCenteredString(font, Component.translatable("gtna.research.screen.empty"),
                    (viewLeft + viewRight) / 2, (viewTop + viewBottom) / 2, 0xFFAAAAAA);
        } else {
            graphics.enableScissor(viewLeft + 1, viewTop + 1, viewRight - 1, viewBottom - 1);
            drawBands(graphics);
            drawLines(graphics);
            for (var node : ClientKnowledge.nodes()) {
                var slot = layout.slots().get(node.id());
                if (slot == null) continue;
                drawNode(graphics, node, nodeX(slot), nodeY(slot));
            }
            graphics.disableScissor();
            if (inView(mouseX, mouseY)) hovered = nodeAt(mouseX, mouseY);
        }

        drawSidePanel(graphics, mouseX, mouseY);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (hovered != null) graphics.renderComponentTooltip(font, tooltip(hovered), mouseX, mouseY);
    }

    /** Areas the team has points in, plus every area some node costs, so a zero balance still shows. */
    private static List<ResourceLocation> areas() {
        Set<ResourceLocation> areas = new LinkedHashSet<>(ClientKnowledge.points().keySet());
        ClientKnowledge.nodes().forEach(node -> areas.addAll(node.cost().keySet()));
        List<ResourceLocation> sorted = new ArrayList<>(areas);
        sorted.sort(Comparator.comparing(ResourceLocation::toString));
        return sorted;
    }

    private void drawPoints(GuiGraphics graphics) {
        int x = right - PANEL_PADDING;
        List<ResourceLocation> areas = areas();
        for (int i = areas.size() - 1; i >= 0; i--) {
            ResourceLocation area = areas.get(i);
            String amount = Long.toString(ClientKnowledge.points().getOrDefault(area, 0L));
            x -= font.width(amount);
            graphics.drawString(font, amount, x, top + 7, LABEL, false);
            x -= 2;
            x -= drawAreaIcon(graphics, area, x, top + 3, true);
            x -= 8;
        }
    }

    /**
     * Draws the area's icon ending at {@code x} (when {@code alignRight}) or starting there.
     *
     * @return the width used.
     */
    private int drawAreaIcon(GuiGraphics graphics, ResourceLocation area, int x, int y, boolean alignRight) {
        ResourceLocation icon = new ResourceLocation(area.getNamespace(),
                "textures/gui/research/area_" + area.getPath().replace('/', '_') + ".png");
        if (exists(icon)) {
            graphics.blit(icon, alignRight ? x - 16 : x, y, 0, 0, 16, 16, 16, 16);
            return 16;
        }
        String name = KnowledgeNode.areaName(area).getString();
        int w = font.width(name);
        graphics.drawString(font, name, alignRight ? x - w : x, y + 4, LABEL, false);
        return w;
    }

    private static boolean exists(ResourceLocation texture) {
        return TEXTURE_EXISTS.computeIfAbsent(texture,
                id -> Minecraft.getInstance().getResourceManager().getResource(id).isPresent());
    }

    private void drawBands(GuiGraphics graphics) {
        for (var band : layout.bands()) {
            int bandLeft = viewLeft + (int) offsetX + MARGIN + band.firstColumn() * COLUMN_WIDTH + 2;
            int bandRight = viewLeft + (int) offsetX + MARGIN + (band.firstColumn() + band.columnCount()) *
                    COLUMN_WIDTH - 2;
            int bandTop = viewTop + (int) offsetY + MARGIN;
            graphics.fill(bandLeft, bandTop, bandRight, bandTop + HEADER_HEIGHT - 4, 0x55000000);
            graphics.hLine(bandLeft, bandRight - 1, bandTop + HEADER_HEIGHT - 4, 0xFF3C3C3C);
            graphics.drawString(font, Component.translatable("gtna.research.tier", band.tier()), bandLeft + 4,
                    bandTop + 3, 0xFFD0D0D0, true);
        }
    }

    private void drawLines(GuiGraphics graphics) {
        for (var node : ClientKnowledge.nodes()) {
            var to = layout.slots().get(node.id());
            if (to == null) continue;
            for (ResourceLocation prerequisite : node.prerequisites()) {
                var from = layout.slots().get(prerequisite);
                if (from == null) continue;
                boolean fromDone = ClientKnowledge.isUnlocked(prerequisite);
                int color = fromDone && ClientKnowledge.isUnlocked(node.id()) ? GREEN : fromDone ? YELLOW : GREY;
                int x1 = nodeX(from) + NODE, y1 = nodeY(from) + NODE / 2;
                int x2 = nodeX(to), y2 = nodeY(to) + NODE / 2;
                int midX = (x1 + x2) / 2;
                graphics.hLine(Math.min(x1, midX), Math.max(x1, midX), y1, color);
                graphics.vLine(midX, Math.min(y1, y2), Math.max(y1, y2), color);
                graphics.hLine(Math.min(midX, x2), Math.max(midX, x2), y2, color);
            }
        }
    }

    private void drawNode(GuiGraphics graphics, ClientKnowledge.NodeView node, int x, int y) {
        State state = stateOf(node);
        int outline = node.id().equals(selected) ? 0xFFFFFFFF : switch (state) {
            case UNLOCKED -> GREEN;
            case AVAILABLE -> canBuy(node) || !node.purchasable() ? YELLOW : 0xFFB08A2A;
            case LOCKED -> 0;
        };
        if (outline != 0) graphics.renderOutline(x - 1, y - 1, NODE + 2, NODE + 2, outline);
        ResourceLocation frame = new ResourceLocation(node.id().getNamespace(),
                "textures/gui/research/frame_" + node.kind().name().toLowerCase(Locale.ROOT) + ".png");
        if (exists(frame)) graphics.blit(frame, x, y, 0, 0, NODE, NODE, NODE, NODE);
        else GuiTextures.SLOT.draw(graphics, 0, 0, x, y, NODE, NODE);
        graphics.renderFakeItem(iconOf(node), x + (NODE - 16) / 2, y + (NODE - 16) / 2);
        if (state == State.LOCKED) {
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 200);
            graphics.fill(x, y, x + NODE, y + NODE, 0xAA000000);
            graphics.pose().popPose();
        }
        Component name = KnowledgeNode.displayName(node.id());
        String text = font.plainSubstrByWidth(name.getString(), COLUMN_WIDTH - 4);
        graphics.drawCenteredString(font, text, x + NODE / 2, y + NODE + 3,
                state == State.LOCKED ? 0xFF808080 : 0xFFFFFFFF);
    }

    private static ItemStack iconOf(ClientKnowledge.NodeView node) {
        var id = node.icon().or(node::triggerItem);
        if (id.isPresent()) {
            var item = ForgeRegistries.ITEMS.getValue(id.get());
            if (item != null && item != Items.AIR) return new ItemStack(item);
        }
        return new ItemStack(Items.ENCHANTED_BOOK);
    }

    private static Component itemName(ResourceLocation item) {
        var found = ForgeRegistries.ITEMS.getValue(item);
        return found == null || found == Items.AIR ? Component.literal(item.toString()) : found.getDescription();
    }

    // ------------------------------------------------------------------ side panel

    private int buttonTop() {
        return viewBottom - BUTTON_HEIGHT;
    }

    private boolean overButton(double mouseX, double mouseY) {
        return mouseX >= sideLeft && mouseX < sideLeft + SIDE_WIDTH && mouseY >= buttonTop() &&
                mouseY < buttonTop() + BUTTON_HEIGHT;
    }

    private boolean buttonActive() {
        return selectedView().map(KnowledgeTreeScreen::canBuy).orElse(false);
    }

    private void drawSidePanel(GuiGraphics graphics, int mouseX, int mouseY) {
        int infoBottom = buttonTop() - 4;
        GuiTextures.DISPLAY.draw(graphics, mouseX, mouseY, sideLeft, viewTop, SIDE_WIDTH, infoBottom - viewTop);
        int x = sideLeft + 5, width = SIDE_WIDTH - 10;
        int y = viewTop + 5;
        var view = selectedView();
        if (view.isEmpty()) {
            drawWrapped(graphics, Component.translatable("gtna.research.screen.select"), x, y, width, 0xFFAAAAAA,
                    infoBottom);
            return;
        }
        var node = view.get();
        State state = stateOf(node);

        graphics.renderFakeItem(iconOf(node), x, y);
        y = drawWrapped(graphics, KnowledgeNode.displayName(node.id()).copy().withStyle(ChatFormatting.BOLD),
                x + 20, y + 4, width - 20, 0xFFFFFFFF, infoBottom);
        y = Math.max(y, viewTop + 5 + 18) + 2;
        Component kind = Component.translatable("gtna.research.kind." + node.kind().name().toLowerCase(Locale.ROOT));
        y = drawLine(graphics, Component.translatable("gtna.research.tier", node.tier()).append(" · ").append(kind),
                x, y, 0xFFAAAAAA);
        y = drawLine(graphics, switch (state) {
            case UNLOCKED -> Component.translatable("gtna.research.state.unlocked").withStyle(ChatFormatting.GREEN);
            case AVAILABLE -> Component.translatable("gtna.research.state.available")
                    .withStyle(ChatFormatting.YELLOW);
            case LOCKED -> Component.translatable("gtna.research.state.locked").withStyle(ChatFormatting.RED);
        }, x, y, 0xFFFFFFFF) + 4;

        if (state != State.UNLOCKED) {
            if (node.purchasable()) {
                y = drawLine(graphics, Component.translatable("gtna.research.screen.cost"), x, y, 0xFFD0D0D0);
                Map<ResourceLocation, Integer> cost = costOf(node);
                if (cost.isEmpty()) {
                    y = drawLine(graphics, Component.translatable("gtna.research.screen.free"), x + 4, y, GREEN);
                }
                for (var entry : cost.entrySet()) {
                    long have = ClientKnowledge.points().getOrDefault(entry.getKey(), 0L);
                    int used = drawAreaIconDark(graphics, entry.getKey(), x + 4, y - 4);
                    String text = entry.getValue() + " (" + have + ")";
                    graphics.drawString(font, text, x + 8 + used, y, have >= entry.getValue() ? GREEN : 0xFFFF5555,
                            true);
                    y += 16;
                }
                y += 2;
            }
            if (node.triggerItem().isPresent()) {
                Component item = itemName(node.triggerItem().get());
                if (node.purchasable()) {
                    boolean met = ClientKnowledge.requirementMet(node.id());
                    y = drawWrapped(graphics, Component.translatable("gtna.research.screen.requirement", item)
                            .withStyle(met ? ChatFormatting.GREEN : ChatFormatting.AQUA), x, y, width, 0xFFFFFFFF,
                            infoBottom) + 2;
                } else {
                    y = drawWrapped(graphics, Component.translatable("gtna.research.tooltip.obtain", item)
                            .withStyle(ChatFormatting.AQUA), x, y, width, 0xFFFFFFFF, infoBottom) + 2;
                }
            }
            if (node.eureka().isPresent()) {
                var eureka = node.eureka().get();
                int percent = (int) Math.round(eureka.reduction() * 100);
                Component line = ClientKnowledge.eurekaMet(node.id()) ?
                        Component.translatable("gtna.research.screen.eureka.found", percent)
                                .withStyle(ChatFormatting.LIGHT_PURPLE) :
                        Component.translatable("gtna.research.screen.eureka.hint", itemName(eureka.item()), percent)
                                .withStyle(ChatFormatting.DARK_PURPLE);
                y = drawWrapped(graphics, line, x, y, width, 0xFFFFFFFF, infoBottom) + 2;
            }
            var missing = node.prerequisites().stream().filter(p -> !ClientKnowledge.isUnlocked(p)).toList();
            if (!missing.isEmpty()) {
                var names = Component.empty();
                for (int i = 0; i < missing.size(); i++) {
                    if (i > 0) names.append(", ");
                    names.append(KnowledgeNode.displayName(missing.get(i)));
                }
                y = drawWrapped(graphics, Component.translatable("gtna.research.tooltip.requires", names), x, y,
                        width, 0xFFFF8888, infoBottom) + 2;
            }
        }
        if (!node.recipes().isEmpty()) {
            y = drawWrapped(graphics, Component.translatable("gtna.research.tooltip.recipes", node.recipes().size()),
                    x, y, width, 0xFFFFAA00, infoBottom) + 2;
        }
        String descriptionKey = KnowledgeNode.nameKey(node.id()) + ".desc";
        if (I18n.exists(descriptionKey)) {
            y += 2;
            graphics.hLine(x, x + width - 1, y, 0xFF3C3C3C);
            y += 4;
            drawWrapped(graphics, Component.translatable(descriptionKey).withStyle(ChatFormatting.ITALIC), x, y,
                    width, 0xFFB8B8B8, infoBottom);
        }

        if (node.purchasable() && state != State.UNLOCKED) drawButton(graphics, mouseX, mouseY);
    }

    private int drawAreaIconDark(GuiGraphics graphics, ResourceLocation area, int x, int y) {
        ResourceLocation icon = new ResourceLocation(area.getNamespace(),
                "textures/gui/research/area_" + area.getPath().replace('/', '_') + ".png");
        if (exists(icon)) {
            graphics.blit(icon, x, y, 0, 0, 16, 16, 16, 16);
            return 16;
        }
        Component name = KnowledgeNode.areaName(area);
        graphics.drawString(font, name, x, y + 4, 0xFFD0D0D0, true);
        return font.width(name);
    }

    private void drawButton(GuiGraphics graphics, int mouseX, int mouseY) {
        boolean active = buttonActive();
        int y = buttonTop();
        GuiTextures.BUTTON.draw(graphics, mouseX, mouseY, sideLeft, y, SIDE_WIDTH, BUTTON_HEIGHT);
        if (!active) graphics.fill(sideLeft, y, sideLeft + SIDE_WIDTH, y + BUTTON_HEIGHT, 0x88000000);
        else if (overButton(mouseX, mouseY)) {
            graphics.fill(sideLeft, y, sideLeft + SIDE_WIDTH, y + BUTTON_HEIGHT, 0x33FFFFFF);
        }
        graphics.drawCenteredString(font, Component.translatable("gtna.research.screen.research"),
                sideLeft + SIDE_WIDTH / 2, y + 5, active ? 0xFFFFFFFF : 0xFF909090);
    }

    private int drawLine(GuiGraphics graphics, Component text, int x, int y, int color) {
        graphics.drawString(font, text, x, y, color, true);
        return y + 10;
    }

    /** Draws wrapped text, stopping at {@code limit}; returns the y below the last line. */
    private int drawWrapped(GuiGraphics graphics, Component text, int x, int y, int width, int color, int limit) {
        for (FormattedCharSequence line : font.split(text, width)) {
            if (y + 9 > limit) break;
            graphics.drawString(font, line, x, y, color, true);
            y += 10;
        }
        return y;
    }

    private List<Component> tooltip(ClientKnowledge.NodeView node) {
        State state = stateOf(node);
        List<Component> lines = new ArrayList<>();
        lines.add(KnowledgeNode.displayName(node.id()).copy().withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        lines.add(switch (state) {
            case UNLOCKED -> Component.translatable("gtna.research.state.unlocked").withStyle(ChatFormatting.GREEN);
            case AVAILABLE -> Component.translatable("gtna.research.state.available").withStyle(ChatFormatting.YELLOW);
            case LOCKED -> Component.translatable("gtna.research.state.locked").withStyle(ChatFormatting.RED);
        });
        if (!node.id().equals(selected)) {
            lines.add(Component.translatable("gtna.research.screen.click").withStyle(ChatFormatting.DARK_GRAY));
        }
        return lines;
    }

    /** Opens the screen if the player is in a world and nothing else is open. */
    public static void open() {
        var minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.screen == null) minecraft.setScreen(new KnowledgeTreeScreen());
    }
}
