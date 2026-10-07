package com.raishxn.gtna.common.machine.multiblock.godforge.gui;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import com.raishxn.gtna.api.machine.feature.godforge.GodforgeData;
import com.raishxn.gtna.api.machine.feature.godforge.GodforgeMath;
import com.raishxn.gtna.api.machine.feature.godforge.GodforgeModuleStats;
import com.raishxn.gtna.api.machine.feature.godforge.GodforgeUpgrade;
import com.raishxn.gtna.common.data.GTNAMaterials;
import com.raishxn.gtna.common.machine.multiblock.godforge.ForgeOfGodsMachine;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static com.raishxn.gtna.common.machine.multiblock.godforge.gui.GodforgeGui.*;

/**
 * Forge of Gods controller UI, ported from GTNH {@code MTEForgeOfGodsGui} and its panels (GT5-Unofficial a3e1e112).
 * Each GTNH panel is a side tab; values are read from {@link ForgeOfGodsMachine#view()} and every action runs on the
 * server through the controller.
 */
public final class ForgeOfGodsUI {

    private static final String GUI = "gtna.godforge.gui.";
    private static final int TREE_BUTTON_W = 40;
    private static final int TREE_BUTTON_H = 15;

    private ForgeOfGodsUI() {}

    // ---------------------------------------------------------------- main page

    /** Which GTNH panel is open over the main screen; kept on both sides by the button callbacks. */
    static final class Navigator {

        String open;
        GodforgeUpgrade insertion = GodforgeUpgrade.START;

        void toggle(String panel) {
            open = panel.equals(open) ? null : panel;
        }
    }

    private static final int UI_W = 400, UI_H = 300;
    private static final int MAIN_W = 196, MAIN_H = 214;

    /**
     * GTNH {@code MTEForgeOfGodsGui} layout: terminal with the contributors and general info icons, three buttons
     * over the inventory (module refresh, statistics, shard ejection) and five on its right (milestones, fuel,
     * battery, cosmetics, upgrade tree). Each panel opens over the main window, as the GTNH panels do.
     */
    public static com.lowdragmc.lowdraglib.gui.modular.ModularUI createUI(ForgeOfGodsMachine machine,
                                                                          net.minecraft.world.entity.player.Player player) {
        Navigator nav = new Navigator();
        WidgetGroup root = new WidgetGroup(0, 0, UI_W, UI_H);
        int mx = (UI_W - MAIN_W) / 2, my = (UI_H - MAIN_H) / 2;
        WidgetGroup main = new WidgetGroup(mx, my, MAIN_W, MAIN_H);
        main.setBackground(GuiTextures.BACKGROUND);
        var screen = new WidgetGroup(6, 6, 184, 100);
        screen.setBackground(GuiTextures.DISPLAY);
        screen.addWidget(new WrappedText(4, 18, 176, 10, () -> machine.isFormed() ?
                Component.translatable(GUI + (machine.view().internalBattery != 0 ? "storedfuel" :
                        "storedstartupfuel")) :
                Component.translatable("gtceu.multiblock.invalid_structure").withStyle(ChatFormatting.RED), true, 1f));
        screen.addWidget(new WrappedText(4, 32, 176, 10, () -> {
            if (!machine.isFormed()) return Component.empty();
            GodforgeData data = machine.view();
            return Component.literal(data.internalBattery != 0 ?
                    format(data.internalBattery) + "/" + format(data.maxBatteryCharge) :
                    format(data.stellarFuelAmount) + "/" + format(data.neededStartupFuel));
        }, true, 1f));
        screen.addWidget(com.raishxn.gtna.client.renderer.GTNATextures.logo(164, 2));
        // Terminal corners: contributors (left) and general info (right).
        screen.addWidget(iconButton(4, 80, 16, 16, OVERLAY_HEART, () -> nav.toggle("thanks"),
                "gtna.godforge.button.thanks.tooltip"));
        screen.addWidget(iconButton(162, 76, 20, 20, LOGO, () -> nav.toggle("info"), GUI + "clickhere"));
        main.addWidget(screen);
        // Gap row over the inventory.
        main.addWidget(celestialButton(6, 110, () -> OVERLAY_REFRESH, click -> {
            if (!click.isRemote) machine.refreshModules();
        }, () -> List.of(Component.translatable("gtna.godforge.button.structurecheck.tooltip"))));
        main.addWidget(celestialButton(24, 110, () -> OVERLAY_STATISTICS, click -> nav.toggle("statistics"),
                () -> List.of(Component.translatable("gtna.godforge.button.statistics.tooltip"))));
        main.addWidget(new WidgetGroup(42, 110, 16, 16) {

            @Override
            public boolean isVisible() {
                return machine.view().isUpgradeActive(GodforgeUpgrade.END);
            }
        }.addWidget(celestialButton(0, 0, () -> machine.view().gravitonShardEjection ? OVERLAY_EJECT :
                OVERLAY_EJECT_LOCKED, click -> {
                    if (!click.isRemote) machine.toggleShardEjection();
                }, () -> List.of(Component.translatable("gtna.godforge.button.ejection.tooltip")))));
        main.addWidget(UITemplate.bindPlayerInventory(player.getInventory(), GuiTextures.SLOT, 6, 130, true));
        // Button column right of the inventory.
        main.addWidget(celestialButton(174, 110, () -> OVERLAY_FLAG, click -> nav.toggle("milestones"),
                () -> List.of(Component.translatable("gtna.godforge.button.milestones.tooltip"))));
        main.addWidget(celestialButton(174, 128, () -> OVERLAY_HEAT, click -> nav.toggle("fuel"),
                () -> List.of(Component.translatable("gtna.godforge.button.fuelconfig.tooltip"))));
        main.addWidget(celestialButton(174, 146, () -> machine.view().batteryCharging ? OVERLAY_BATTERY_ON :
                OVERLAY_BATTERY_OFF, click -> {
                    if (click.button == 1) {
                        if (machine.view().isUpgradeActive(GodforgeUpgrade.REC)) nav.toggle("battery");
                    } else if (!click.isRemote) {
                        machine.toggleBatteryCharging();
                    }
                }, () -> List.of(Component.translatable("gtna.godforge.button.battery.tooltip.01"),
                        Component.translatable("gtna.godforge.button.battery.tooltip.02")
                                .withStyle(ChatFormatting.GRAY))));
        main.addWidget(celestialButton(174, 164, () -> OVERLAY_RAINBOW, click -> nav.toggle("cosmetics"),
                () -> List.of(Component.translatable("gtna.godforge.button.color.tooltip"))));
        main.addWidget(celestialButton(174, 182, () -> OVERLAY_UPGRADES, click -> nav.toggle("tree"),
                () -> List.of(Component.translatable("gtna.godforge.button.upgradetree.tooltip"))));
        root.addWidget(main);

        // Panels, centred over the main window (manual insertion over the terminal so the inventory stays usable).
        root.addWidget(overlay(nav, "tree", upgradeTree(machine, nav), null));
        root.addWidget(overlay(nav, "milestones", milestones(machine), null));
        // GTNH attaches the fuel panel to the right edge of the main window; here it can also be dragged.
        var fuel = new com.lowdragmc.lowdraglib.gui.widget.DraggableWidgetGroup(mx + MAIN_W - 3, my, 78, 138);
        fuel.addWidget(fuelConfig(machine));
        root.addWidget(overlay(nav, "fuel", fuel, null));
        root.addWidget(overlay(nav, "battery", batteryConfig(machine), null));
        var editor = new GodforgeCosmeticsUI.Editor();
        root.addWidget(overlay(nav, "cosmetics", GodforgeCosmeticsUI.cosmetics(machine, nav, editor), null));
        root.addWidget(overlay(nav, "customcolor", GodforgeCosmeticsUI.customColor(machine, nav, editor),
                "cosmetics"));
        root.addWidget(overlay(nav, "importcolor", GodforgeCosmeticsUI.importColor(nav, editor), "customcolor"));
        root.addWidget(overlay(nav, "statistics", statistics(machine), null));
        root.addWidget(overlay(nav, "info", generalInfo(machine), null));
        root.addWidget(overlay(nav, "thanks", thanks(), null));
        Widget insertion = manualInsertion(machine, () -> nav.insertion);
        insertion.setSelfPosition(new com.lowdragmc.lowdraglib.utils.Position(mx + (MAIN_W - 190) / 2, my + 4));
        root.addWidget(overlay(nav, "insertion", insertion, "tree"));
        return new com.lowdragmc.lowdraglib.gui.modular.ModularUI(UI_W, UI_H, machine, player).widget(root);
    }

    /** A panel shown while {@code nav.open} is its name; blocks clicks to the window below and has a close box. */
    static Widget overlay(Navigator nav, String name, Widget content, String closeTo) {
        var size = content.getSize();
        var pos = content.getSelfPosition();
        if (pos.x == 0 && pos.y == 0) {
            content.setSelfPosition(new com.lowdragmc.lowdraglib.utils.Position((UI_W - size.width) / 2,
                    (UI_H - size.height) / 2));
            pos = content.getSelfPosition();
        }
        WidgetGroup group = new WidgetGroup(0, 0, UI_W, UI_H) {

            @Override
            public boolean isVisible() {
                return name.equals(nav.open);
            }
        };
        if (content instanceof com.lowdragmc.lowdraglib.gui.widget.DraggableWidgetGroup draggable) {
            // A dragged panel carries its own close box; it handles its clicks itself.
            draggable.addWidget(new ButtonWidget(size.width - 12, 2, 10, 10, new TextTexture("✕", 0x404040),
                    click -> nav.open = closeTo));
            group.addWidget(content);
            return group;
        }
        group.addWidget(new ClickBlocker(pos.x, pos.y, size.width, size.height));
        group.addWidget(content);
        group.addWidget(new ButtonWidget(pos.x + size.width - 12, pos.y + 2, 10, 10,
                new TextTexture("✕", 0xFFFFFF), click -> nav.open = closeTo));
        return group;
    }

    private static Widget iconButton(int x, int y, int w, int h, IGuiTexture icon, Runnable action, String tooltip) {
        WidgetGroup group = new WidgetGroup(x, y, w, h);
        group.addWidget(new ImageWidget(0, 0, w, h, icon));
        group.addWidget(new ButtonWidget(0, 0, w, h, IGuiTexture.EMPTY, click -> action.run()));
        group.addWidget(new TooltipArea(0, 0, w, h, () -> List.of(Component.translatable(tooltip))));
        return group;
    }

    /** Swallows clicks on an open panel so nothing below it reacts. */
    private static final class ClickBlocker extends Widget {

        ClickBlocker(int x, int y, int w, int h) {
            super(x, y, w, h);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return isMouseOverElement(mouseX, mouseY);
        }
    }

    // ---------------------------------------------------------------- upgrade tree

    /** Selection shared by the tree and the upgrade detail view (one instance per side of the open UI). */
    private static final class TreeState {

        GodforgeUpgrade selected;
    }

    private static Widget upgradeTree(ForgeOfGodsMachine machine, Navigator nav) {
        WidgetGroup page = new WidgetGroup(0, 0, 300, 300);
        page.setBackground(BACKGROUND_STAR);
        TreeState state = new TreeState();
        var tree = new DraggableScrollableWidgetGroup(4, 4, 292, 292);
        tree.setYScrollBarWidth(3).setYBarStyle(IGuiTexture.EMPTY, new ColorRectTexture(0x80FFFFFF));
        String[] links = { "BLUE START IGCC",
                "BLUE IGCC STEM", "BLUE IGCC CFCE", "BLUE STEM GISS", "BLUE STEM FDIM", "BLUE CFCE FDIM",
                "BLUE CFCE SA", "BLUE FDIM GPCI", "BLUE GPCI GEM", "RED GISS REC", "RED GPCI REC", "RED SA CTCDD",
                "RED GPCI CTCDD", "BLUE REC QGPIU", "BLUE CTCDD QGPIU", "ORANGE QGPIU TCT", "ORANGE TCT EPEC",
                "ORANGE EPEC POS", "ORANGE POS NGMS", "PURPLE QGPIU SEFCP", "PURPLE SEFCP CNTI", "PURPLE CNTI NDPE",
                "PURPLE NDPE NGMS", "PURPLE CNTI DOP", "GREEN QGPIU GGEBE", "GREEN GGEBE IMKG", "GREEN IMKG DOR",
                "GREEN DOR NGMS", "GREEN GGEBE TPTP", "BLUE NGMS SEDS", "BLUE SEDS PA", "BLUE PA CD", "BLUE CD TSE",
                "BLUE TSE TBF", "BLUE TBF EE", "BLUE EE END" };
        List<ConnectorLayer.Line> lines = new ArrayList<>();
        for (String link : links) {
            String[] parts = link.split(" ");
            GodforgeUpgrade from = GodforgeUpgrade.valueOf(parts[1]), to = GodforgeUpgrade.valueOf(parts[2]);
            lines.add(new ConnectorLayer.Line(from.treeX() + TREE_BUTTON_W / 2, from.treeY() + TREE_BUTTON_H / 2,
                    to.treeX() + TREE_BUTTON_W / 2, to.treeY() + TREE_BUTTON_H / 2, from, to,
                    GodforgeUpgrade.Color.valueOf(parts[0])));
        }
        // Lines first, nodes on top (GTNH draws the connectors on the background layer).
        tree.addWidget(new ConnectorLayer(292, 960, lines, upgrade -> machine.view().isUpgradeActive(upgrade)));
        for (GodforgeUpgrade upgrade : GodforgeUpgrade.VALUES) {
            int x = upgrade.treeX(), y = upgrade.treeY();
            tree.addWidget(new ImageWidget(x, y, TREE_BUTTON_W, TREE_BUTTON_H,
                    () -> machine.view().isUpgradeActive(upgrade) ? BUTTON_SPACE_PRESSED : BUTTON_SPACE));
            tree.addWidget(new ImageWidget(x, y, TREE_BUTTON_W, TREE_BUTTON_H,
                    new TextTexture(upgrade.shortNameKey(), 0xFFAA00).setDropShadow(true)));
            tree.addWidget(new ButtonWidget(x, y, TREE_BUTTON_W, TREE_BUTTON_H, IGuiTexture.EMPTY, click -> {
                if (click.button == 1) {
                    if (!click.isRemote) machine.respecUpgrade(upgrade);
                    return;
                }
                if (click.isShiftClick) {
                    if (!click.isRemote) machine.unlockUpgrade(upgrade);
                    return;
                }
                state.selected = upgrade;
            }));
            tree.addWidget(new TooltipArea(x, y, TREE_BUTTON_W, TREE_BUTTON_H,
                    () -> List.of(Component.translatable(upgrade.nameKey()))));
        }
        // Secret upgrade, left of START.
        int sx = GodforgeUpgrade.START.treeX() - 60, sy = GodforgeUpgrade.START.treeY();
        tree.addWidget(new ImageWidget(sx, sy, TREE_BUTTON_W, TREE_BUTTON_H,
                () -> machine.view().secretUpgrade ? BUTTON_SPACE_PRESSED : IGuiTexture.EMPTY));
        tree.addWidget(new ImageWidget(sx, sy, TREE_BUTTON_W, TREE_BUTTON_H,
                () -> machine.view().secretUpgrade ?
                        new TextTexture("gtna.godforge.upgrade.tt.short.secret", 0xFFAA00) : IGuiTexture.EMPTY));
        tree.addWidget(new ImageWidget(sx + 40, sy + 4, 20, 6, () -> machine.view().secretUpgrade ?
                GodforgeGui.connector(GodforgeUpgrade.Color.BLUE, true) : IGuiTexture.EMPTY));
        tree.addWidget(new ButtonWidget(sx, sy, TREE_BUTTON_W, TREE_BUTTON_H, IGuiTexture.EMPTY, click -> {
            if (!click.isRemote) machine.toggleSecretUpgrade();
        }));
        tree.addWidget(new TooltipArea(sx, sy, TREE_BUTTON_W, TREE_BUTTON_H,
                () -> List.of(Component.translatable("gtna.godforge.upgrade.tt.secret"))));
        page.addWidget(tree);
        page.addWidget(upgradeDetail(machine, state, nav));
        return page;
    }

    /**
     * GTNH {@code IndividualUpgradePanel}: 250 x 250 (300 for START and END) on the upgrade's glow, the milestone
     * symbol dimmed by the colour overlay, name, body and lore, shard cost and available shards in the bottom
     * corners, and the extra cost and Construct/Respec buttons between them.
     */
    private static Widget upgradeDetail(ForgeOfGodsMachine machine, TreeState state, Navigator nav) {
        WidgetGroup detail = new WidgetGroup(0, 0, 300, 300) {

            @Override
            public boolean isVisible() {
                return state.selected != null;
            }
        };
        detail.addWidget(new ClickBlocker(0, 0, 300, 300));
        detail.addWidget(new ImageWidget(0, 0, 300, 300, new ColorRectTexture(0x90000000)));
        for (boolean large : new boolean[] { false, true }) {
            int size = large ? 300 : 250;
            int o = (300 - size) / 2;
            int bodyH = large ? 55 : 80, loreH = large ? 170 : 115;
            WidgetGroup panel = new WidgetGroup(o, o, size, size) {

                @Override
                public boolean isVisible() {
                    return state.selected != null && state.selected.largePanel() == large;
                }
            };
            panel.addWidget(new ImageWidget(0, 0, size, size, () -> state.selected == null ? IGuiTexture.EMPTY :
                    glow(state.selected.color())));
            int symbolH = size / 2;
            panel.addWidget(new ImageWidget((size - symbolH) / 2, (size - symbolH) / 2, symbolH, symbolH,
                    () -> state.selected == null ? IGuiTexture.EMPTY : symbol(state.selected.symbol())));
            panel.addWidget(new ImageWidget((size - symbolH) / 2, (size - symbolH) / 2, symbolH, symbolH,
                    () -> state.selected == null ? IGuiTexture.EMPTY : tex("picture_overlay_" +
                            state.selected.color().name().toLowerCase(java.util.Locale.ROOT))));
            int textW = size - 16;
            panel.addWidget(new WrappedText(8, 16, textW, 12, () -> state.selected == null ? Component.empty() :
                    Component.translatable(state.selected.nameKey()).withStyle(ChatFormatting.GOLD), true, 1f));
            panel.addWidget(new WrappedText(8, 35, textW, bodyH, () -> state.selected == null ?
                    Component.empty() : Component.translatable(state.selected.bodyKey())
                            .withStyle(ChatFormatting.WHITE),
                    true, 1f));
            panel.addWidget(new WrappedText(8, 40 + bodyH, textW, loreH, () -> state.selected == null ?
                    Component.empty() : Component.translatable(state.selected.loreKey())
                            .withStyle(style -> style.withItalic(true).withColor(0xBBBDBD)),
                    true, 1f));
            int bottom = size - 22;
            panel.addWidget(new WrappedText(8, bottom, 70, 15, () -> state.selected == null ? Component.empty() :
                    Component.translatable(GUI + "shardcost").withStyle(style -> style.withColor(0x9C9C9C))
                            .append(" ").append(Component.literal(String.valueOf(state.selected.shardCost()))
                                    .withStyle(ChatFormatting.BLUE)),
                    true, 0.7f));
            panel.addWidget(new WrappedText(size - 78, bottom, 70, 15, () -> {
                if (state.selected == null) return Component.empty();
                int available = machine.view().gravitonShardsAvailable;
                return Component.translatable(GUI + "availableshards").withStyle(style -> style.withColor(0x9C9C9C))
                        .append(" ").append(Component.literal(String.valueOf(available)).withStyle(
                                available >= state.selected.shardCost() ? ChatFormatting.GREEN :
                                        ChatFormatting.RED));
            }, true, 0.7f));
            // Buttons row, centred: extra cost (when the upgrade has one) and Construct/Respec.
            int rowX = (size - 78) / 2, rowY = size - 22;
            WidgetGroup cost = new WidgetGroup(rowX + 4, rowY, 15, 15) {

                @Override
                public boolean isVisible() {
                    return state.selected != null && state.selected.hasExtraCost();
                }
            };
            cost.addWidget(new ImageWidget(0, 0, 15, 15, () -> state.selected != null &&
                    machine.view().upgrades.isCostPaid(state.selected) ? tex("button_boxed_checkmark") :
                            tex("button_boxed_exclamation_point")));
            cost.addWidget(new ButtonWidget(0, 0, 15, 15, IGuiTexture.EMPTY, click -> {
                if (state.selected != null && state.selected.hasExtraCost()) {
                    nav.insertion = state.selected;
                    nav.open = "insertion";
                }
            }));
            cost.addWidget(new TooltipArea(0, 0, 15, 15, () -> {
                if (state.selected == null) return List.of();
                return List.of(Component.translatable(machine.view().upgrades.isCostPaid(state.selected) ?
                        "gtna.godforge.button.materialrequirementsmet.tooltip" :
                        "gtna.godforge.button.materialrequirements.tooltip"),
                        Component.translatable("gtna.godforge.button.materialrequirements.tooltip.clickhere")
                                .withStyle(ChatFormatting.GRAY));
            }));
            panel.addWidget(cost);
            panel.addWidget(new ImageWidget(rowX + 23, rowY, 40, 15, () -> state.selected != null &&
                    machine.view().isUpgradeActive(state.selected) ? GodforgeCosmeticsUI.OUTLINE_PRESSED :
                            GodforgeCosmeticsUI.OUTLINE));
            panel.addWidget(new WrappedText(rowX + 23, rowY + 4, 40, 10, () -> Component.translatable(
                    state.selected != null && machine.view().isUpgradeActive(state.selected) ?
                            "gtna.godforge.upgrade.respec" : "gtna.godforge.upgrade.confirm"),
                    true, 0.7f));
            panel.addWidget(new ButtonWidget(rowX + 23, rowY, 40, 15, IGuiTexture.EMPTY, click -> {
                GodforgeUpgrade upgrade = state.selected;
                if (upgrade == null || click.isRemote) return;
                if (machine.view().isUpgradeActive(upgrade)) machine.respecUpgrade(upgrade);
                else machine.unlockUpgrade(upgrade);
            }));
            panel.addWidget(new ButtonWidget(size - 14, 4, 10, 10, new TextTexture("✕", 0xFFFFFF),
                    click -> state.selected = null));
            detail.addWidget(panel);
        }
        return detail;
    }

    // ---------------------------------------------------------------- manual insertion

    /** GTNH {@code ManualInsertionPanel}: the extra cost of one upgrade and 16 input slots. */
    private static Widget manualInsertion(ForgeOfGodsMachine machine,
                                          java.util.function.Supplier<GodforgeUpgrade> selected) {
        WidgetGroup page = new WidgetGroup(0, 0, 190, 124);
        page.setBackground(GuiTextures.BACKGROUND_INVERSE);
        page.addWidget(new WrappedText(5, 5, 180, 10, () -> Component.translatable(GUI + "payUpgradeCosts")
                .append(" - ").append(Component.translatable(selected.get().shortNameKey())), true, 1f));
        // Required items and remaining amounts (GTNH cost rows, three columns of four).
        for (int i = 0; i < 12; i++) {
            final int index = i;
            int x = 5 + (i / 4) * 36, y = 16 + (i % 4) * 18;
            page.addWidget(new ImageWidget(x, y, 18, 18, GuiTextures.SLOT));
            page.addWidget(new ImageWidget(x + 1, y + 1, 16, 16, () -> {
                var costs = selected.get().extraCost();
                if (index >= costs.size()) return IGuiTexture.EMPTY;
                var item = BuiltInRegistries.ITEM.get(new ResourceLocation(costs.get(index).item()));
                return new ItemStackTexture(new ItemStack(item));
            }));
            page.addWidget(new ImageWidget(x + 18, y, 18, 18, new TextTexture(() -> {
                var costs = selected.get().extraCost();
                if (index >= costs.size()) return "";
                short paid = machine.view().upgrades.paidCosts(selected.get())[index];
                int left = costs.get(index).amount() - paid;
                return left <= 0 ? "§a✔" : (paid == 0 ? "§c" : "§e") + "x" + left;
            }).setColor(0xFFFFFF)));
            page.addWidget(new TooltipArea(x, y, 18, 18, () -> {
                var costs = selected.get().extraCost();
                if (index >= costs.size()) return List.of();
                var item = BuiltInRegistries.ITEM.get(new ResourceLocation(costs.get(index).item()));
                return List.of(new ItemStack(item).getHoverName());
            }));
        }
        for (int i = 0; i < 16; i++) {
            page.addWidget(new SlotWidget(machine.upgradeWindow(), i, 113 + (i % 4) * 18, 16 + (i / 4) * 18, true,
                    true).setBackground(GuiTextures.SLOT));
        }
        page.addWidget(new ButtonWidget(5, 98, 180, 18, GuiTextures.BUTTON, click -> {
            if (!click.isRemote) machine.payUpgradeCost(selected.get());
        }));
        page.addWidget(new ImageWidget(5, 98, 180, 18, text(GUI + "consumeUpgradeMats", 0x404040)));
        return page;
    }

    // ---------------------------------------------------------------- milestones

    /**
     * GTNH {@code MilestonePanel}: 400 x 300 on the space background, one 130 x 100 cell per corner with the glowing
     * symbol, the title and the progress bar across its middle (inverted bar filling from the right once inversion
     * is active).
     */
    private static Widget milestones(ForgeOfGodsMachine machine) {
        WidgetGroup page = new WidgetGroup(0, 0, 400, 300);
        page.setBackground(BACKGROUND_SPACE);
        Milestone[] all = Milestone.values();
        int[][] glowSize = { { 80, 100 }, { 70, 98 }, { 100, 100 }, { 100, 100 } };
        for (int i = 0; i < all.length; i++) {
            Milestone milestone = all[i];
            final int index = i;
            int cellX = i % 2 == 0 ? 37 : 400 - 37 - 130;
            int cellY = i < 2 ? 24 : 300 - 24 - 100;
            int gw = glowSize[i][0], gh = glowSize[i][1];
            page.addWidget(new ImageWidget(cellX + (130 - gw) / 2, cellY, gw, gh, milestone.glow));
            page.addWidget(new BarWidget(cellX, cellY + 46, 130, 7, () -> milestone.bar,
                    () -> percentage(machine.view(), index, false), false, true));
            page.addWidget(new BarWidget(cellX, cellY + 46, 130, 7, () -> milestone.barInverted,
                    () -> machine.view().inversion ? percentage(machine.view(), index, true) : 0f, true, false));
            page.addWidget(new ImageWidget(cellX, cellY + 35, 130, 10, text(milestone.titleKey, 0xFFAA00)));
            page.addWidget(new TooltipArea(cellX + (130 - gw) / 2, cellY, gw, gh, () -> {
                List<Component> lines = new ArrayList<>(milestoneLines(machine.view(), milestone, index));
                lines.add(Component.translatable(GUI + "milestoneinfo").withStyle(ChatFormatting.GRAY));
                return lines;
            }));
        }
        return page;
    }

    private static float percentage(GodforgeData data, int index, boolean inverted) {
        return switch (index) {
            case 0 -> inverted ? data.invertedPowerMilestonePercentage : data.powerMilestonePercentage;
            case 1 -> inverted ? data.invertedRecipeMilestonePercentage : data.recipeMilestonePercentage;
            case 2 -> inverted ? data.invertedFuelMilestonePercentage : data.fuelMilestonePercentage;
            default -> inverted ? data.invertedStructureMilestonePercentage : data.structureMilestonePercentage;
        };
    }

    /** GTNH {@code IndividualMilestonePanel}: totals, level, next threshold and shards gained. */
    private static List<Component> milestoneLines(GodforgeData data, Milestone milestone, int index) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable(milestone.titleKey).withStyle(ChatFormatting.GOLD));
        String total = switch (milestone) {
            case CHARGE -> format(data.totalPowerConsumed);
            case CONVERSION -> format(data.totalRecipesProcessed);
            case CATALYST -> format(data.totalFuelConsumed);
            case COMPOSITION -> format(data.totalExtensionsBuilt);
        };
        lines.add(Component.translatable(GUI + "totalprogress").append(": ").append(Component.literal(total)
                .withStyle(ChatFormatting.GRAY)).append(" ").append(Component.translatable(milestone.progressKey)));
        int rawLevel = data.milestoneProgress[index];
        int level = data.inversion ? rawLevel : Math.min(rawLevel, 7);
        lines.add(Component.translatable(GUI + "milestoneprogress").append(": ")
                .append(Component.literal(String.valueOf(level)).withStyle(ChatFormatting.GRAY)));
        if (level >= 7 && !data.inversion) {
            lines.add(Component.translatable(GUI + "milestonecomplete"));
        } else {
            String next = switch (milestone) {
                case CHARGE -> format(data.inversion ?
                        GodforgeData.POWER_MILESTONE_T7_CONSTANT.multiply(BigInteger.valueOf(level - 5)) :
                        BigInteger.valueOf(9).pow(level).multiply(BigInteger.TEN.pow(15)));
                case CONVERSION -> format(data.inversion ? GodforgeData.RECIPE_MILESTONE_T7_CONSTANT * (level - 5) :
                        (long) Math.pow(4, level) * 10_000_000L);
                case CATALYST -> format(data.inversion ? GodforgeData.FUEL_MILESTONE_T7_CONSTANT * (level - 5) :
                        (long) Math.pow(3, level) * 10_000L);
                case COMPOSITION -> String.valueOf(level + 1);
            };
            lines.add(Component.translatable(GUI + "progress").append(": ").append(Component.literal(next)
                    .withStyle(ChatFormatting.GRAY)).append(" ").append(Component.translatable(milestone.progressKey)));
        }
        lines.add(Component.translatable(GUI + "shardgain").append(": ").append(Component.literal(
                String.valueOf(level * (level + 1) / 2)).withStyle(ChatFormatting.GRAY)));
        return lines;
    }

    // ---------------------------------------------------------------- fuel and battery

    /** GTNH {@code FuelConfigPanel}: 78 x 138, attached to the right of the main window. */
    private static Widget fuelConfig(ForgeOfGodsMachine machine) {
        WidgetGroup page = new WidgetGroup(0, 0, 78, 138);
        page.setBackground(GuiTextures.BACKGROUND);
        page.addWidget(new WrappedText(2, 5, 74, 27, () -> Component.translatable(GUI + "fuelconsumption")
                .withStyle(style -> style.withColor(0x404040)), true, 0.8f));
        page.addWidget(new ImageWidget(64, 24, 10, 10, new TextTexture("§9ⓘ")));
        page.addWidget(new TooltipArea(64, 24, 10, 10, () -> {
            List<Component> lines = new ArrayList<>();
            for (int i = 0; i <= 5; i++) lines.add(Component.translatable(GUI + "fuelinfo." + i));
            return lines;
        }));
        page.addWidget(new ServerField(4, 36, 70, 14,
                () -> String.valueOf(machine.view().fuelConsumptionFactor),
                value -> machine.setFuelFactor(parseInt(value, 1))).setNumbersOnly(1, Integer.MAX_VALUE));
        page.addWidget(new WrappedText(2, 56, 74, 18, () -> Component.translatable(GUI + "fueltype")
                .withStyle(style -> style.withColor(0x404040)), true, 0.8f));
        java.util.function.Supplier<?>[] fluids = {
                () -> GTNAMaterials.DimensionallyTranscendentResidue.getFluid(),
                () -> GTNAMaterials.RawStarMatter.getFluid(),
                () -> GTNAMaterials.MagnetohydrodynamicallyConstrainedStarMatter.getFluid() };
        String[] names = { GTNAMaterials.DimensionallyTranscendentResidue.getUnlocalizedName(),
                GTNAMaterials.RawStarMatter.getUnlocalizedName(),
                GTNAMaterials.MagnetohydrodynamicallyConstrainedStarMatter.getUnlocalizedName() };
        for (int i = 0; i < 3; i++) {
            final int type = i;
            int x = 4 + i * 25;
            @SuppressWarnings("unchecked")
            var fluid = (java.util.function.Supplier<net.minecraft.world.level.material.Fluid>) fluids[i];
            page.addWidget(new FluidIcon(x + 1, 79, 16, 16, fluid));
            page.addWidget(new ImageWidget(x, 78, 18, 18, () -> machine.view().selectedFuelType == type ? SELECTOR :
                    IGuiTexture.EMPTY));
            page.addWidget(new ButtonWidget(x, 78, 18, 18, IGuiTexture.EMPTY, click -> {
                if (!click.isRemote) machine.setFuelType(type);
            }));
            page.addWidget(new TooltipArea(x, 78, 18, 18, () -> List.of(Component.translatable(names[type]))));
        }
        page.addWidget(new WrappedText(2, 102, 74, 10, () -> Component.translatable(GUI + "fuelusage")
                .withStyle(style -> style.withColor(0x404040)), true, 0.8f));
        page.addWidget(new WrappedText(2, 113, 74, 20, () -> {
            GodforgeData data = machine.view();
            return Component.literal(format((long) Math.max(GodforgeMath.fuelConsumption(data) * 5 *
                    (data.batteryCharging ? 2 : 1), 1)) + " L/5s").withStyle(ChatFormatting.DARK_GRAY);
        }, true, 0.8f));
        return page;
    }

    private static Widget batteryConfig(ForgeOfGodsMachine machine) {
        WidgetGroup page = new WidgetGroup(0, 0, 140, 50);
        page.setBackground(GuiTextures.BACKGROUND_INVERSE);
        page.addWidget(new WrappedText(6, 6, 128, 10, () -> Component.translatable(GUI + "batteryinfo"), true, 1f));
        page.addWidget(new ServerField(6, 26, 128, 14, () -> String.valueOf(machine.view().maxBatteryCharge),
                value -> machine.setMaxBatteryCharge(parseLong(value, 1))).setNumbersOnly(1L, Long.MAX_VALUE));
        return page;
    }

    // ---------------------------------------------------------------- statistics

    private static final class StatisticsState {

        int factor = -1;
        /** GTNH {@code Formatters}: 0 NONE, 1 COMMA, 2 EXPONENT. */
        int formatter = 2;
    }

    private static final GodforgeModuleStats.Type[] MODULE_TYPES = GodforgeModuleStats.Type.values();
    private static final String[] MODULE_KEYS = { "powerforge", "meltingcore", "plasmafab", "exoticizer" };
    private static final String[] STAT_KEYS = { "heat", "effectiveheat", "parallel", "speedbonus", "energydiscount",
            "ocdivisor", "processingvoltage" };
    private static final String[] STAT_TOOLTIPS = { "heat", "effectiveheat", "parallel", "speedbonus",
            "energydiscount", "ocdivisor", "processingvoltage" };

    /**
     * GTNH {@code StatisticsPanel}: a 7 x 4 grid (cyan rows, yellow columns) of module statistics on the dark
     * background, with a preview fuel factor applied by the Apply button and reset by the refresh icon.
     */
    private static Widget statistics(ForgeOfGodsMachine machine) {
        int w = 320, h = 296;
        WidgetGroup page = new WidgetGroup(0, 0, w, h);
        page.setBackground(tex("background_white_glow"));
        StatisticsState state = new StatisticsState();
        int[] pending = { -1 };
        int labelW = 74, colW = 58, gridX = 8, headerY = 32, rowH = 29, top = 52;
        page.addWidget(new ImageWidget(0, 12, w, 12, text(GUI + "modulestats", 0xFFAA00)));
        // Grid lines: yellow column separators, cyan row separators.
        for (int c = 0; c <= MODULE_TYPES.length; c++) {
            int x = gridX + labelW + c * colW;
            if (c < MODULE_TYPES.length) page.addWidget(new ImageWidget(x, headerY, 1, top + 7 * rowH - headerY,
                    new ColorRectTexture(0xFFE6D21E)));
        }
        for (int r = 0; r <= 7; r++) {
            page.addWidget(new ImageWidget(gridX, top + r * rowH - 2, labelW + MODULE_TYPES.length * colW, 1,
                    new ColorRectTexture(0xFF1EC8C8)));
        }
        for (int m = 0; m < MODULE_TYPES.length; m++) {
            Component name = Component.translatable(GUI + MODULE_KEYS[m]).withStyle(ChatFormatting.GOLD);
            page.addWidget(new WrappedText(gridX + labelW + m * colW + 2, headerY + 2, colW - 4, 18, () -> name,
                    true, 0.75f));
        }
        for (int s = 0; s < STAT_KEYS.length; s++) {
            final int stat = s;
            int y = top + s * rowH;
            Component label = Component.translatable(GUI + STAT_KEYS[s]).withStyle(ChatFormatting.GOLD);
            page.addWidget(new WrappedText(gridX + 2, y + 6, labelW - 4, 20, () -> label, true, 1f));
            page.addWidget(new TooltipArea(gridX, y, labelW, rowH,
                    () -> List.of(Component.translatable("gtna.godforge.text.tooltip." + STAT_TOOLTIPS[stat]))));
            for (int m = 0; m < MODULE_TYPES.length; m++) {
                final GodforgeModuleStats.Type type = MODULE_TYPES[m];
                page.addWidget(new WrappedText(gridX + labelW + m * colW + 2, y + 10, colW - 4, 10,
                        () -> Component.literal(statistic(machine.view(), type, stat, state.factor,
                                state.formatter))
                                .withStyle(ChatFormatting.GREEN),
                        true, 1f));
            }
        }
        int by = top + 7 * rowH + 8;
        page.addWidget(iconButton(gridX, by + 2, 12, 12, OVERLAY_REFRESH,
                () -> state.formatter = (state.formatter + 1) % 3, "gtna.godforge.button.formatting.tooltip"));
        page.addWidget(new WrappedText(gridX + 70, by + 4, 110, 10,
                () -> Component.translatable(GUI + "factorpreview").withStyle(ChatFormatting.GOLD), false, 1f));
        page.addWidget(new ServerField(gridX + 182, by, 70, 14,
                () -> String.valueOf(machine.view().fuelConsumptionFactor), value -> pending[0] = parseInt(value, 1))
                .setValidator(value -> {
                    pending[0] = parseInt(value, 1);
                    return value;
                }));
        page.addWidget(new TooltipArea(gridX + 182, by, 70, 14,
                () -> List.of(Component.translatable("gtna.godforge.text.tooltip.factorpreview"))));
        page.addWidget(new ButtonWidget(gridX + 256, by - 1, 44, 16, GuiTextures.BUTTON, click -> {
            if (pending[0] > 0) state.factor = pending[0];
        }));
        page.addWidget(new ImageWidget(gridX + 256, by - 1, 44, 16, text("gtna.godforge.cosmetics.applycolor",
                0x404040)));
        return page;
    }

    /** GTNH {@code Formatters.EXPONENT} style: 2.02e5 above 10^5, two decimals at most below. */
    private static String compact(double value) {
        if (Math.abs(value) >= 100_000) {
            int exponent = (int) Math.floor(Math.log10(Math.abs(value)));
            return String.format(java.util.Locale.ROOT, "%.2fe%d", value / Math.pow(10, exponent), exponent);
        }
        return new java.text.DecimalFormat("0.##", java.text.DecimalFormatSymbols.getInstance(java.util.Locale.ROOT))
                .format(value);
    }

    /** GTNH {@code Formatters.format}: plain, thousands separators, or exponent above 1000. */
    private static String formatWhole(long value, int formatter) {
        return switch (formatter) {
            case 0 -> Long.toString(value);
            case 1 -> format(value);
            default -> value > 1_000L ? compact(value) : Long.toString(value);
        };
    }

    private static String statistic(GodforgeData data, GodforgeModuleStats.Type type, int stat, int factorPreview,
                                    int formatter) {
        int factor = factorPreview < 0 ? data.fuelConsumptionFactor : factorPreview;
        GodforgeModuleStats module = new GodforgeModuleStats(type);
        GodforgeMath.maxHeat(module, data, factor);
        GodforgeMath.maxParallel(module, data, factor);
        GodforgeMath.speedBonus(module, data);
        GodforgeMath.energyDiscount(module, data);
        GodforgeMath.miscParameters(module, data);
        GodforgeMath.processingVoltage(module, data, factor);
        return switch (stat) {
            case 0 -> formatWhole(module.heat, formatter);
            case 1 -> formatWhole(module.heatForOC, formatter);
            case 2 -> formatWhole(module.calculatedMaxParallel, formatter);
            case 3 -> compact(module.speedBonus);
            case 4 -> compact(module.energyDiscount);
            case 5 -> compact(module.overclockTimeFactor);
            default -> compact(module.processingVoltage);
        };
    }

    // ---------------------------------------------------------------- info and thanks

    /** One line block of the GTNH {@code GeneralInfoPanel}: lang key, style and layout. */
    private record InfoEntry(String key, int kind) {

        static final int HEADER = 0, TEXT = 1, FORMULA = 2, MODULE = 3;
    }

    private static final String[][] INFO = {
            { "fuel", "fuelinfotext.1", "fuelinfotext.2", "=fuelinfotext.3", "fuelinfotext.4", "fuelinfotext.5",
                    "fuelinfotext.6" },
            { "modules", "moduleinfotext.1", "moduleinfotext.2", "#moduleinfotext.forge.1",
                    "moduleinfotext.forge.2", "#moduleinfotext.core.1", "moduleinfotext.core.2",
                    "#moduleinfotext.fab.1", "moduleinfotext.fab.2", "#moduleinfotext.exotic.1",
                    "moduleinfotext.exotic.2", "moduleinfotext.exotic.3", "moduleinfotext.exotic.4",
                    "moduleinfotext.exotic.5", "moduleinfotext.exotic.6" },
            { "upgrades", "upgradeinfotext.1", "upgradeinfotext.2", "upgradeinfotext.3", "=upgradeinfotext.4",
                    "upgradeinfotext.5", "upgradeinfotext.6" },
            { "milestones", "milestoneinfotext.1", "milestoneinfotext.2", "milestoneinfotext.3",
                    "milestoneinfotext.4", "milestoneinfotext.5", "milestoneinfotext.6", "milestoneinfotext.7" },
            { "inversion", "inversioninfotext.1", "inversioninfotext.2", "inversioninfotext.3" } };

    private static Component infoText(String key, int kind) {
        var text = Component.translatable(GUI + key);
        return switch (kind) {
            case InfoEntry.HEADER -> text.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD,
                    ChatFormatting.UNDERLINE);
            case InfoEntry.FORMULA -> text.withStyle(ChatFormatting.GREEN);
            case InfoEntry.MODULE -> text.withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD);
            default -> text.withStyle(ChatFormatting.GOLD);
        };
    }

    /** Wrapped height of a text at the given width; only the client can measure, the server just counts. */
    private static int infoHeight(ForgeOfGodsMachine machine, Component text, int width) {
        if (!machine.isRemote()) return 9;
        var font = net.minecraft.client.Minecraft.getInstance().font;
        return Math.max(1, font.split(text, width).size()) * font.lineHeight;
    }

    /**
     * GTNH {@code GeneralInfoPanel}: dark background, purple section headers, gold text, green formulas and a
     * clickable table of contents that scrolls to each section.
     */
    private static Widget generalInfo(ForgeOfGodsMachine machine) {
        WidgetGroup page = new WidgetGroup(0, 0, 300, 300);
        page.setBackground(tex("background_white_glow"));
        int width = 280;
        var scroll = new DraggableScrollableWidgetGroup(6, 6, 288, 288);
        scroll.setYScrollBarWidth(3).setYBarStyle(IGuiTexture.EMPTY, new ColorRectTexture(0x80FFFFFF));
        boolean inversion = machine.view().inversion;
        int sections = inversion ? INFO.length : INFO.length - 1;
        int y = 2;
        // Introduction.
        Component intro = infoText("introduction", InfoEntry.HEADER);
        scroll.addWidget(new WrappedText(0, y, width, 12, () -> intro, true, 1f));
        y += 16;
        Component introText = infoText("introductioninfotext", InfoEntry.TEXT);
        int h = infoHeight(machine, introText, width);
        scroll.addWidget(new WrappedText(0, y, width, h, () -> introText, false, 1f));
        y += h + 8;
        Component toc = Component.translatable(GUI + "tableofcontents").withStyle(ChatFormatting.AQUA,
                ChatFormatting.BOLD);
        scroll.addWidget(new WrappedText(0, y, width, 10, () -> toc, false, 1f));
        y += 16;
        int tocY = y;
        y += sections * 14 + 6;
        int[] headerY = new int[sections];
        for (int sct = 0; sct < sections; sct++) {
            String[] section = INFO[sct];
            headerY[sct] = y;
            Component header = infoText(section[0], InfoEntry.HEADER);
            scroll.addWidget(new WrappedText(0, y, width, 12, () -> header, true, 1f));
            y += 16;
            for (int k = 1; k < section.length; k++) {
                String key = section[k];
                int kind = key.startsWith("=") ? InfoEntry.FORMULA : key.startsWith("#") ? InfoEntry.MODULE :
                        InfoEntry.TEXT;
                Component text = infoText(kind == InfoEntry.TEXT ? key : key.substring(1), kind);
                h = infoHeight(machine, text, width);
                scroll.addWidget(new WrappedText(0, y, width, h, () -> text, false, 1f));
                y += h + 8;
            }
        }
        for (int sct = 0; sct < sections; sct++) {
            final int target = headerY[sct];
            Component entry = Component.translatable(GUI + INFO[sct][0]).withStyle(ChatFormatting.AQUA,
                    ChatFormatting.BOLD);
            int rowY = tocY + sct * 14;
            scroll.addWidget(new WrappedText(0, rowY, width, 10, () -> entry, false, 1f));
            scroll.addWidget(new ButtonWidget(0, rowY - 2, 120, 12, IGuiTexture.EMPTY,
                    click -> scroll.setScrollYOffset(target)));
        }
        page.addWidget(scroll);
        return page;
    }

    /** GTNH {@code SpecialThanksPanel}: 200 x 200, rainbow glow, symbol centred, credits on the left. */
    private static Widget thanks() {
        WidgetGroup page = new WidgetGroup(0, 0, 200, 200);
        page.setBackground(tex("background_rainbow_glow"));
        page.addWidget(new ImageWidget(50, 50, 100, 100, THANKS));
        page.addWidget(new WrappedText(0, 7, 200, 10, () -> Component.translatable(GUI + "contributors")
                .withStyle(ChatFormatting.GOLD), true, 1f));
        Object[][] sections = {
                { "lead", new Object[] { "cloud", ChatFormatting.AQUA } },
                { "programming", new Object[] { "serenibyss", null },
                        new Object[] { "teg", ChatFormatting.DARK_AQUA } },
                { "textures", new Object[] { "ant", ChatFormatting.GREEN } },
                { "rendering", new Object[] { "bucket", ChatFormatting.WHITE } },
                { "lore", new Object[] { "deleno", ChatFormatting.WHITE } },
                { "playtesting", new Object[] { "misi", null } } };
        int y = 30;
        for (Object[] section : sections) {
            String title = (String) section[0];
            page.addWidget(new WrappedText(7, y, 120, 8, () -> Component.translatable(GUI + title)
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.UNDERLINE), false, 0.8f));
            y += 9;
            for (int k = 1; k < section.length; k++) {
                Object[] entry = (Object[]) section[k];
                String name = (String) entry[0];
                ChatFormatting color = (ChatFormatting) entry[1];
                page.addWidget(new WrappedText(7, y, 120, 8, () -> {
                    var text = Component.translatable(GUI + name);
                    if ("misi".equals(name)) return text.withStyle(style -> style.withColor(0xFFC26F));
                    return color == null ? text : text.withStyle(color);
                }, false, 0.8f));
                y += 9;
            }
            y += 5;
        }
        page.addWidget(new WrappedText(90, 140, 100, 60, () -> Component.translatable(GUI + "thanks")
                .withStyle(style -> style.withItalic(true).withColor(0xBBBDBD)), true, 0.8f));
        return page;
    }

    // ---------------------------------------------------------------- helpers

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static long parseLong(String value, long fallback) {
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static float parseFloat(String value, float fallback) {
        try {
            return Float.parseFloat(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
