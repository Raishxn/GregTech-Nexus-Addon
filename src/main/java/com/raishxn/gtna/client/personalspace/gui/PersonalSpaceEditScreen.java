package com.raishxn.gtna.client.personalspace.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import com.raishxn.gtna.common.world.personalspace.PersonalSpaceBlocks;
import com.raishxn.gtna.common.world.personalspace.PersonalSpaceClientState;
import com.raishxn.gtna.common.world.personalspace.PersonalSpacePortalEntity;
import com.raishxn.gtna.common.world.personalspace.PersonalSpaceSettings;
import com.raishxn.gtna.common.world.personalspace.PersonalSpaceSettings.CenterDirection;
import com.raishxn.gtna.common.world.personalspace.PersonalSpaceSettings.DaylightCycle;
import com.raishxn.gtna.common.world.personalspace.PersonalSpaceSettings.GapPreset;
import com.raishxn.gtna.common.world.personalspace.PersonalSpaceSettings.Layer;
import com.raishxn.gtna.common.world.personalspace.PersonalSpaceSettings.SkyType;
import com.raishxn.gtna.network.GTNANetworkHandler;
import com.raishxn.gtna.network.packet.CPersonalSpaceChangeSettings;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

/** Port of PersonalSpace's {@code GuiEditWorld}: visual settings, layers, presets, boundaries, roads, center. */
public class PersonalSpaceEditScreen extends Screen {

    public static final int MAX_VISIBLE_PRESETS = 5;
    public static final int MAX_VISIBLE_LAYERS = 6;

    public final PersonalSpacePortalEntity tile;
    public int xSize, ySize, guiLeft, guiTop;
    public final PersonalSpaceSettings desiredConfig = new PersonalSpaceSettings();

    private WSlider skyRed, skyGreen, skyBlue, starBrightness;
    private WTextField biome;
    private int biomeCycle = 0;
    private WButton biomeEditButton;
    private WToggleButton enableWeather, enableClouds, generateTrees, generateVegetation;
    private WCycleButton enableDaylightCycle;
    private WButton skyType, save, cancel;
    private WTextField presetEntry;
    private final List<WButton> presetButtons = new ArrayList<>();

    private WBlockDropdown boundaryBlockADropdown, boundaryBlockBDropdown;
    private WButton boundaryChunkXMinus, boundaryChunkXPlus, boundaryChunkZMinus, boundaryChunkZPlus;
    private WTextField boundaryChunkXField, boundaryChunkZField;
    private WButton gapWidthMinus, gapWidthPlus, gapPresetButton;
    private WTextField gapWidthField;
    private WBlockDropdown gapBlockADropdown, gapBlockBDropdown, gapBlockCDropdown;
    private WToggleButton applyToAllSurfaceLayersToggle, centerEnabledToggle;
    private WButton centerDirectionButton;
    private WBlockDropdown centerBlockDropdown;

    private int currentPage = 0;
    private Widget page1Container, page2Container;
    private WButton moreSettingsButton, presetScrollLeft, presetScrollRight, backToMainButton;
    private int presetScrollOffset = 0;
    private WPreviewPanel previewPanel;
    private Widget presetEditor;
    private Widget rootWidget = new Widget();
    private String voidPresetName = "";

    private WBlockDropdown layersBlockDropdown;
    private int layerListScrollOffset = 0;
    private boolean layerScrollbarDragging = false;

    private List<String> allowedBoundaryBlocks = List.of();
    private List<String> allowedGapBlocks = List.of();
    private List<String> allowedCenterBlocks = List.of();
    private List<WBlockDropdown.BlockEntry> boundaryBlockEntries = new ArrayList<>();
    private List<WBlockDropdown.BlockEntry> gapBlockEntries = new ArrayList<>();
    private List<WBlockDropdown.BlockEntry> centerBlockEntries = new ArrayList<>();
    private List<WBlockDropdown.BlockEntry> layerBlockEntries = new ArrayList<>();

    public PersonalSpaceEditScreen(PersonalSpacePortalEntity tile) {
        super(Component.translatable("block.gtna.personal_space_portal"));
        this.tile = tile;
        var level = Minecraft.getInstance().level;
        PersonalSpaceSettings current = level == null ? null : PersonalSpaceClientState.settings(level.dimension());
        if (current != null) {
            desiredConfig.copyFrom(current, true, true);
        } else if (tile.isActive() && tile.targetPersonalId() > 0) {
            PersonalSpaceSettings target = PersonalSpaceClientState.settings(tile.targetPersonalId());
            if (target != null) desiredConfig.copyFrom(target, true, true);
            else desiredConfig.setAllowGenerationChanges(true);
        } else {
            desiredConfig.setAllowGenerationChanges(true);
        }
        reloadBoundaryRules();
        desiredConfig.setBoundaryChunkIntervalX(desiredConfig.getBoundaryChunkIntervalX());
        desiredConfig.setGapWidth(desiredConfig.getGapWidth());
    }

    private static String tr(String key, Object... args) {
        return I18n.get(key, args);
    }

    private void reloadBoundaryRules() {
        allowedBoundaryBlocks = PersonalSpaceClientState.allowedBoundaryBlocks();
        allowedGapBlocks = PersonalSpaceClientState.allowedGapBlocks();
        allowedCenterBlocks = PersonalSpaceClientState.allowedCenterBlocks();
        boundaryBlockEntries = WBlockDropdown.buildEntries(allowedBoundaryBlocks, true);
        gapBlockEntries = WBlockDropdown.buildEntries(allowedGapBlocks, true);
        centerBlockEntries = WBlockDropdown.buildEntries(allowedCenterBlocks, true);
        layerBlockEntries = WBlockDropdown.buildEntries(PersonalSpaceClientState.allowedBlocks(), true);
        sanitizeExtendedBlockSelections();
    }

    private void sanitizeExtendedBlockSelections() {
        if (!desiredConfig.getAllowGenerationChanges()) return;
        sanitize(allowedBoundaryBlocks, desiredConfig::getBoundaryBlockA, desiredConfig::setBoundaryBlockA);
        sanitize(allowedBoundaryBlocks, desiredConfig::getBoundaryBlockB, desiredConfig::setBoundaryBlockB);
        sanitize(allowedGapBlocks, desiredConfig::getGapBlockA, desiredConfig::setGapBlockA);
        sanitize(allowedGapBlocks, desiredConfig::getGapBlockB, desiredConfig::setGapBlockB);
        sanitize(allowedGapBlocks, desiredConfig::getGapBlockC, desiredConfig::setGapBlockC);
        sanitize(allowedCenterBlocks, desiredConfig::getCenterBlock, desiredConfig::setCenterBlock);
    }

    private static void sanitize(Collection<String> allowed, Supplier<String> get, Consumer<String> set) {
        String block = get.get();
        if (block == null || block.isEmpty() || !allowed.contains(block)) set.accept("");
    }

    @Override
    public void tick() {
        rootWidget.update();
        WBlockDropdown.updateOpenDropdown();
        if (minecraft != null && minecraft.player != null && !minecraft.player.isAlive()) onClose();
    }

    private void addWidget(Widget widget) {
        rootWidget.addChild(widget);
        ySize += widget.position.height + 1;
    }

    private void updateSkyTypeButton() {
        SkyType type = desiredConfig.getSkyType();
        skyType.setText(type.buttonText());
        skyType.tooltip = tr("gui.personalWorld.skyType." + type.name());
    }

    private String centerDirectionText() {
        return tr("gui.personalWorld.center.dir." + desiredConfig.getCenterDirection().name());
    }

    private String gapPresetText() {
        return tr("gui.personalWorld.gap.preset." + desiredConfig.getGapPreset().name());
    }

    private void updateCenterButtons() {
        boolean visible = desiredConfig.isCenterEnabled();
        if (centerEnabledToggle != null) centerEnabledToggle.setValue(visible);
        if (centerDirectionButton != null) {
            centerDirectionButton.visible = visible;
            centerDirectionButton.setText(centerDirectionText());
        }
        if (centerBlockDropdown != null) {
            centerBlockDropdown.visible = visible;
            centerBlockDropdown.setSelectedIndex(WBlockDropdown.findEntryIndex(centerBlockEntries,
                    desiredConfig.getCenterBlock()));
        }
    }

    private void updateBoundaryButtons() {
        if (boundaryBlockADropdown != null) {
            boundaryBlockADropdown.setSelectedIndex(WBlockDropdown.findEntryIndex(boundaryBlockEntries,
                    desiredConfig.getBoundaryBlockA()));
        }
        if (boundaryBlockBDropdown != null) {
            boundaryBlockBDropdown.setSelectedIndex(WBlockDropdown.findEntryIndex(boundaryBlockEntries,
                    desiredConfig.getBoundaryBlockB()));
        }
    }

    private void updateGapButtons() {
        boolean solid = desiredConfig.getGapPreset() == GapPreset.SOLID;
        if (gapBlockADropdown != null) {
            gapBlockADropdown.setSelectedIndex(WBlockDropdown.findEntryIndex(gapBlockEntries,
                    desiredConfig.getGapBlockA()));
        }
        if (gapBlockBDropdown != null) {
            gapBlockBDropdown.visible = !solid;
            gapBlockBDropdown.setSelectedIndex(WBlockDropdown.findEntryIndex(gapBlockEntries,
                    desiredConfig.getGapBlockB()));
        }
        if (gapBlockCDropdown != null) {
            gapBlockCDropdown.visible = !solid;
            gapBlockCDropdown.setSelectedIndex(WBlockDropdown.findEntryIndex(gapBlockEntries,
                    desiredConfig.getGapBlockC()));
        }
        if (gapPresetButton != null) gapPresetButton.setText(gapPresetText());
    }

    private void updateApplyToAllSurfaceLayersButton() {
        if (applyToAllSurfaceLayersToggle != null) {
            applyToAllSurfaceLayersToggle.setValue(desiredConfig.isApplyToAllSurfaceLayers());
        }
    }

    private static int parseIntOrDefault(String text, int fallback) {
        try {
            return Integer.parseInt(text.trim());
        } catch (RuntimeException exception) {
            return fallback;
        }
    }

    private static boolean focused(WTextField field) {
        return field != null && field.isFocused();
    }

    private void syncExtendedInputsFromDesiredConfig() {
        if (!focused(boundaryChunkXField)) {
            boundaryChunkXField.setText(Integer.toString(desiredConfig.getBoundaryChunkIntervalX()));
        }
        if (!focused(boundaryChunkZField)) {
            boundaryChunkZField.setText(Integer.toString(desiredConfig.getBoundaryChunkIntervalZ()));
        }
        if (!focused(gapWidthField)) gapWidthField.setText(Integer.toString(desiredConfig.getGapWidth()));
    }

    private void clearExtendedPresetInputs() {
        desiredConfig.setBoundaryChunkIntervalX(0);
        desiredConfig.setBoundaryChunkIntervalZ(0);
        desiredConfig.setBoundaryBlockA("");
        desiredConfig.setBoundaryBlockB("");
        desiredConfig.setGapWidth(0);
        desiredConfig.setGapBlockA("");
        desiredConfig.setGapBlockB("");
        desiredConfig.setGapBlockC("");
        desiredConfig.setApplyToAllSurfaceLayers(false);
        desiredConfig.setCenterEnabled(false);
        desiredConfig.setCenterBlock("");
        if (boundaryChunkXField != null) boundaryChunkXField.setText("0");
        if (boundaryChunkZField != null) boundaryChunkZField.setText("0");
        if (gapWidthField != null) gapWidthField.setText("0");
        updateBoundaryButtons();
        updateGapButtons();
        updateApplyToAllSurfaceLayersButton();
        updateCenterButtons();
    }

    private void syncPresetFromExtendedInputsIfChanged(int prevX, int prevZ, int prevGap) {
        if (focused(presetEntry)) return;
        if (prevX != desiredConfig.getBoundaryChunkIntervalX() || prevZ != desiredConfig.getBoundaryChunkIntervalZ() ||
                prevGap != desiredConfig.getGapWidth()) {
            configToPreset();
        }
    }

    private void updatePresetButtonPositions() {
        int total = presetButtons.size();
        int maxOffset = Math.max(0, total - MAX_VISIBLE_PRESETS);
        presetScrollOffset = Mth.clamp(presetScrollOffset, 0, maxOffset);
        int px = 0;
        for (int index = 0; index < total; index++) {
            WButton button = presetButtons.get(index);
            boolean visible = index >= presetScrollOffset && index < presetScrollOffset + MAX_VISIBLE_PRESETS;
            button.visible = visible;
            if (visible) {
                button.position = new Rectangle(px, button.position.y, 24, 18);
                px += 26;
            }
        }
        if (total > MAX_VISIBLE_PRESETS) {
            presetScrollLeft.visible = true;
            presetScrollLeft.position = new Rectangle(px, presetScrollLeft.position.y, 12, 18);
            presetScrollLeft.enabled = presetScrollOffset > 0;
            px += 14;
            presetScrollRight.visible = true;
            presetScrollRight.position = new Rectangle(px, presetScrollRight.position.y, 12, 18);
            presetScrollRight.enabled = presetScrollOffset < maxOffset;
        } else {
            presetScrollLeft.visible = false;
            presetScrollRight.visible = false;
        }
        int moreW = 80;
        moreSettingsButton.position = new Rectangle(258 - moreW, moreSettingsButton.position.y, moreW, 18);
    }

    private void switchPage(int page) {
        currentPage = page;
        if (page1Container != null) page1Container.visible = page == 0;
        if (page2Container != null) page2Container.visible = page == 1;
        if (save != null) save.visible = page == 0;
        if (cancel != null) cancel.visible = page == 0;
    }

    private static int clampBoundaryChunk(int value) {
        return Mth.clamp(value, 0, 20);
    }

    private static int clampGapWidth(int value) {
        return Mth.clamp(value, 0, 5);
    }

    @Override
    protected void init() {
        reloadBoundaryRules();
        if (previewPanel != null) previewPanel.close();
        presetButtons.clear();

        Widget realRoot = new Widget();
        page1Container = new Widget();
        page2Container = new Widget();

        // ---- Page 1 ----
        rootWidget = page1Container;
        ySize = 0;
        addWidget(new WLabel(0, ySize, tr("gui.personalWorld.skyColor"), false));
        skyRed = new WSlider(new Rectangle(0, ySize, 128, 12), tr("gui.personalWorld.skyColor.red") + "%.0f", 0.0,
                255.0, desiredConfig.getSkyColor() >> 16 & 0xFF, 1.0, false, 0xFFFFFF, null, null);
        addWidget(skyRed);
        skyGreen = new WSlider(new Rectangle(0, ySize, 128, 12), tr("gui.personalWorld.skyColor.green") + "%.0f", 0.0,
                255.0, desiredConfig.getSkyColor() >> 8 & 0xFF, 1.0, false, 0xFFFFFF, null, null);
        addWidget(skyGreen);
        skyBlue = new WSlider(new Rectangle(0, ySize, 128, 12), tr("gui.personalWorld.skyColor.blue") + "%.0f", 0.0,
                255.0, desiredConfig.getSkyColor() & 0xFF, 1.0, false, 0xFFFFFF, null, null);
        addWidget(skyBlue);
        ySize += 4;

        enableDaylightCycle = new WCycleButton(new Rectangle(130, ySize, 18, 18), "", false, 0,
                List.of(new WCycleButton.ButtonState(DaylightCycle.SUN, Icons.SUN),
                        new WCycleButton.ButtonState(DaylightCycle.MOON, Icons.MOON),
                        new WCycleButton.ButtonState(DaylightCycle.CYCLE, Icons.SUN_MOON)),
                desiredConfig.getDaylightCycle().ordinal(),
                () -> desiredConfig.setDaylightCycle(enableDaylightCycle.getState()));
        rootWidget.addChild(enableDaylightCycle);
        skyType = new WButton(new Rectangle(150, ySize, 18, 18), "?", true, WButton.DEFAULT_COLOR, null, () -> {
            int count = SkyType.values().length;
            int next = (desiredConfig.getSkyType().ordinal() + 1) % count;
            while (!SkyType.fromOrdinal(next).isLoaded()) next = (next + 1) % count;
            desiredConfig.setSkyType(SkyType.fromOrdinal(next));
            updateSkyTypeButton();
        });
        rootWidget.addChild(skyType);
        updateSkyTypeButton();

        addWidget(new WLabel(0, ySize, tr("gui.personalWorld.starBrightness"), false));
        starBrightness = new WSlider(new Rectangle(0, ySize, 128, 12), "%.2f", 0.0, 1.0,
                desiredConfig.getStarBrightness(), 0.01, false, 0xFFFFFF, null, null);
        addWidget(starBrightness);

        ySize += 4;
        addWidget(new WLabel(0, ySize, tr("gui.personalWorld.biome"), false));
        biome = new WTextField(new Rectangle(0, ySize, 142, 18), desiredConfig.getBiomeId());
        biomeEditButton = new WButton(new Rectangle(144, 0, 18, 18), "", false, 0, Icons.PENCIL, () -> {
            List<String> biomes = PersonalSpaceClientState.allowedBiomes();
            if (biomes.isEmpty()) return;
            biomeCycle = biomeEditButton.lastButton == 0 ? biomeCycle + 1 : biomeCycle + biomes.size() - 1;
            biomeCycle = biomeCycle % biomes.size();
            biome.setText(biomes.get(biomeCycle));
        });
        biome.addChild(biomeEditButton);
        addWidget(biome);
        ySize += 4;
        generateTrees = new WToggleButton(new Rectangle(0, ySize, 18, 18), "", false, 0,
                desiredConfig.isGeneratingTrees(), () -> desiredConfig.setGeneratingTrees(generateTrees.getValue()));
        generateTrees.addChild(new WLabel(24, 4, tr("gui.personalWorld.trees"), false));
        addWidget(generateTrees);
        enableWeather = new WToggleButton(new Rectangle(90, generateTrees.position.y, 18, 18), "", false, 0,
                desiredConfig.isWeatherEnabled(), () -> desiredConfig.setWeatherEnabled(enableWeather.getValue()));
        enableWeather.addChild(new WLabel(24, 4, tr("gui.personalWorld.weather"), false));
        rootWidget.addChild(enableWeather);

        generateVegetation = new WToggleButton(new Rectangle(0, ySize, 18, 18), "", false, 0,
                desiredConfig.isGeneratingVegetation(),
                () -> desiredConfig.setGeneratingVegetation(generateVegetation.getValue()));
        generateVegetation.addChild(new WLabel(24, 4, tr("gui.personalWorld.vegetation"), false));
        addWidget(generateVegetation);
        enableClouds = new WToggleButton(new Rectangle(90, generateVegetation.position.y, 18, 18), "", false, 0,
                desiredConfig.isCloudsEnabled(), () -> desiredConfig.setCloudsEnabled(enableClouds.getValue()));
        enableClouds.addChild(new WLabel(24, 4, tr("gui.personalWorld.clouds"), false));
        rootWidget.addChild(enableClouds);

        voidPresetName = tr("gui.personalWorld.voidWorld");
        ySize += 6;
        presetEntry = new WTextField(new Rectangle(0, ySize, 168, 20), desiredConfig.getFullPresetString());
        if (presetEntry.getText().isEmpty()) presetEntry.setText(voidPresetName);
        addWidget(presetEntry);
        ySize += 2;
        addWidget(new WLabel(0, ySize, tr("gui.personalWorld.presets"), false));

        int number = 1;
        for (String preset : PersonalSpaceClientState.defaultPresets()) {
            String finalPreset = preset.isEmpty() ? voidPresetName : preset;
            WButton button = new WButton(new Rectangle(0, ySize, 24, 18), Integer.toString(number), true,
                    WButton.DEFAULT_COLOR, null, () -> {
                        presetEntry.setText(finalPreset);
                        presetEntry.textField.moveCursorToStart();
                        if (!PersonalSpaceSettings.hasExtendedSettings(finalPreset)) clearExtendedPresetInputs();
                    });
            presetButtons.add(button);
            rootWidget.addChild(button);
            number++;
        }
        presetScrollLeft = new WButton(new Rectangle(0, ySize, 12, 18), "<", true, WButton.DEFAULT_COLOR, null, () -> {
            if (presetScrollOffset > 0) presetScrollOffset--;
            updatePresetButtonPositions();
        });
        rootWidget.addChild(presetScrollLeft);
        presetScrollRight = new WButton(new Rectangle(0, ySize, 12, 18), ">", true, WButton.DEFAULT_COLOR, null,
                () -> {
                    int maxOffset = Math.max(0, presetButtons.size() - MAX_VISIBLE_PRESETS);
                    if (presetScrollOffset < maxOffset) presetScrollOffset++;
                    updatePresetButtonPositions();
                });
        rootWidget.addChild(presetScrollRight);
        moreSettingsButton = new WButton(new Rectangle(0, ySize, 80, 18), tr("gui.personalWorld.moreSettings"), true,
                WButton.DEFAULT_COLOR, null, () -> switchPage(1));
        moreSettingsButton.tooltip = tr("gui.personalWorld.moreSettings.tooltip");
        rootWidget.addChild(moreSettingsButton);
        updatePresetButtonPositions();
        ySize += 20;

        presetEditor = new Widget();
        presetEditor.position = new Rectangle(192, 0, 1, 1);
        rootWidget.addChild(presetEditor);
        layersBlockDropdown = new WBlockDropdown(new Rectangle(-20, 0, 20, 20), true, layerBlockEntries, 0, entry -> {
            String block = entry.blockName().isEmpty() ? "minecraft:air" : entry.blockName();
            if (PersonalSpaceBlocks.block(block) == null) return;
            desiredConfig.getMutableLayers().add(new Layer(block, 1));
            desiredConfig.setLayers(desiredConfig.getLayersAsString());
            configToPreset();
        });
        layersBlockDropdown.setLabel(tr("gui.personalWorld.button.plus"));
        layersBlockDropdown.setGuiRelativePos(172, 0);
        presetEditor.addChild(layersBlockDropdown);
        regeneratePresetEditor();

        // ---- Page 2 ----
        rootWidget = page2Container;
        ySize = 0;
        backToMainButton = new WButton(new Rectangle(0, ySize, 150, 18), tr("gui.personalWorld.backToMain"), true,
                WButton.DEFAULT_COLOR, null, () -> switchPage(0));
        backToMainButton.tooltip = tr("gui.personalWorld.backToMain.tooltip");
        rootWidget.addChild(backToMainButton);
        ySize += 20;

        addWidget(new WLabel(0, ySize, tr("gui.personalWorld.boundary.chunks"), false));
        boundaryChunkXMinus = stepButton(0, () -> stepBoundaryX(-1), "gui.personalWorld.button.minus");
        boundaryChunkXField = new WTextField(new Rectangle(20, ySize, 32, 18),
                Integer.toString(desiredConfig.getBoundaryChunkIntervalX()));
        rootWidget.addChild(boundaryChunkXField);
        boundaryChunkXPlus = stepButton(54, () -> stepBoundaryX(1), "gui.personalWorld.button.plus");
        rootWidget.addChild(new WLabel(78, ySize + 4, tr("gui.personalWorld.multiply"), false));
        boundaryChunkZMinus = stepButton(90, () -> stepBoundaryZ(-1), "gui.personalWorld.button.minus");
        boundaryChunkZField = new WTextField(new Rectangle(110, ySize, 32, 18),
                Integer.toString(desiredConfig.getBoundaryChunkIntervalZ()));
        rootWidget.addChild(boundaryChunkZField);
        boundaryChunkZPlus = stepButton(144, () -> stepBoundaryZ(1), "gui.personalWorld.button.plus");
        ySize += 21;

        addWidget(new WLabel(0, ySize, tr("gui.personalWorld.boundary"), false));
        int boundaryRowY = ySize;
        boundaryBlockADropdown = selector(0, boundaryRowY, boundaryBlockEntries, desiredConfig.getBoundaryBlockA(),
                "gui.personalWorld.boundary.a.short", entry -> {
                    desiredConfig.setBoundaryBlockA(entry.blockName());
                    updateBoundaryButtons();
                    configToPreset();
                });
        boundaryBlockBDropdown = selector(42, boundaryRowY, boundaryBlockEntries, desiredConfig.getBoundaryBlockB(),
                "gui.personalWorld.boundary.b.short", entry -> {
                    desiredConfig.setBoundaryBlockB(entry.blockName());
                    updateBoundaryButtons();
                    configToPreset();
                });
        ySize += 24;
        updateBoundaryButtons();

        addWidget(new WLabel(0, ySize, tr("gui.personalWorld.gap"), false));
        gapWidthMinus = stepButton(0, () -> stepGapWidth(-1), "gui.personalWorld.button.minus");
        gapWidthField = new WTextField(new Rectangle(20, ySize, 32, 18), Integer.toString(desiredConfig.getGapWidth()));
        rootWidget.addChild(gapWidthField);
        gapWidthPlus = stepButton(54, () -> stepGapWidth(1), "gui.personalWorld.button.plus");
        gapPresetButton = new WButton(new Rectangle(76, ySize, 74, 18), gapPresetText(), true, WButton.DEFAULT_COLOR,
                null, () -> {
                    int next = (desiredConfig.getGapPreset().ordinal() + 1) % GapPreset.values().length;
                    desiredConfig.setGapPreset(GapPreset.fromOrdinal(next));
                    gapPresetButton.setText(gapPresetText());
                    updateGapButtons();
                    configToPreset();
                });
        rootWidget.addChild(gapPresetButton);
        ySize += 21;

        int gapRowY = ySize;
        gapBlockADropdown = selector(0, gapRowY, gapBlockEntries, desiredConfig.getGapBlockA(),
                "gui.personalWorld.gap.a.short", entry -> {
                    desiredConfig.setGapBlockA(entry.blockName());
                    updateGapButtons();
                    configToPreset();
                });
        gapBlockBDropdown = selector(42, gapRowY, gapBlockEntries, desiredConfig.getGapBlockB(),
                "gui.personalWorld.gap.b.short", entry -> {
                    desiredConfig.setGapBlockB(entry.blockName());
                    updateGapButtons();
                    configToPreset();
                });
        gapBlockCDropdown = selector(84, gapRowY, gapBlockEntries, desiredConfig.getGapBlockC(),
                "gui.personalWorld.gap.c.short", entry -> {
                    desiredConfig.setGapBlockC(entry.blockName());
                    updateGapButtons();
                    configToPreset();
                });
        ySize += 24;

        applyToAllSurfaceLayersToggle = new WToggleButton(new Rectangle(0, ySize, 18, 18), "", false, 0,
                desiredConfig.isApplyToAllSurfaceLayers(), () -> {
                    desiredConfig.setApplyToAllSurfaceLayers(applyToAllSurfaceLayersToggle.getValue());
                    configToPreset();
                });
        applyToAllSurfaceLayersToggle.addChild(new WLabel(24, 4, tr("gui.personalWorld.applyToAllSurfaceLayers"),
                false));
        addWidget(applyToAllSurfaceLayersToggle);
        updateGapButtons();

        centerEnabledToggle = new WToggleButton(new Rectangle(0, ySize, 18, 18), "", false, 0,
                desiredConfig.isCenterEnabled(), () -> {
                    desiredConfig.setCenterEnabled(centerEnabledToggle.getValue());
                    updateCenterButtons();
                    configToPreset();
                });
        centerEnabledToggle.addChild(new WLabel(24, 4, tr("gui.personalWorld.center.enable"), false));
        addWidget(centerEnabledToggle);

        centerBlockDropdown = new WBlockDropdown(new Rectangle(0, ySize, 20, 20), false, centerBlockEntries,
                WBlockDropdown.findEntryIndex(centerBlockEntries, desiredConfig.getCenterBlock()), entry -> {
                    desiredConfig.setCenterBlock(entry.blockName());
                    updateCenterButtons();
                    configToPreset();
                });
        centerBlockDropdown.setLabel(tr("gui.personalWorld.center.block.short"));
        centerBlockDropdown.setGuiRelativePos(0, ySize);
        addWidget(centerBlockDropdown);
        centerDirectionButton = new WButton(new Rectangle(42, centerBlockDropdown.position.y, 80, 18),
                centerDirectionText(), true, WButton.DEFAULT_COLOR, null, () -> {
                    int next = (desiredConfig.getCenterDirection().ordinal() + 1) % CenterDirection.values().length;
                    desiredConfig.setCenterDirection(CenterDirection.fromOrdinal(next));
                    updateCenterButtons();
                    configToPreset();
                });
        rootWidget.addChild(centerDirectionButton);
        ySize += 2;
        updateCenterButtons();

        previewPanel = new WPreviewPanel(new Rectangle(166, 20, 130, 130), desiredConfig);
        rootWidget.addChild(previewPanel);

        // ---- Root ----
        rootWidget = realRoot;
        realRoot.addChild(page1Container);
        realRoot.addChild(page2Container);
        int saveY = 244 - 16 - 22 + 5;
        save = new WButton(new Rectangle(0, saveY, 128, 20), tr("gui.done"), true, WButton.DEFAULT_COLOR,
                Icons.CHECKMARK, () -> {
                    if (minecraft != null && minecraft.level != null) {
                        GTNANetworkHandler.INSTANCE.sendToServer(new CPersonalSpaceChangeSettings(
                                minecraft.level.dimension().location(), tile.getBlockPos(), desiredConfig.copy()));
                    }
                    onClose();
                });
        rootWidget.addChild(save);
        cancel = new WButton(new Rectangle(130, saveY, 128, 20), tr("gui.cancel"), true, WButton.DEFAULT_COLOR,
                Icons.CROSS, this::onClose);
        rootWidget.addChild(cancel);
        switchPage(currentPage);

        xSize = 320 - 16;
        ySize = 244 - 16;
        guiLeft = (width - xSize) / 2;
        guiTop = (height - ySize) / 2;
    }

    private WButton stepButton(int x, Runnable action, String key) {
        WButton button = new WButton(new Rectangle(x, ySize, 18, 18), tr(key), true, WButton.DEFAULT_COLOR, null,
                action);
        rootWidget.addChild(button);
        return button;
    }

    private WBlockDropdown selector(int x, int y, List<WBlockDropdown.BlockEntry> entries, String selected,
                                    String labelKey, Consumer<WBlockDropdown.BlockEntry> onSelect) {
        WBlockDropdown dropdown = new WBlockDropdown(new Rectangle(x, y, 20, 20), false, entries,
                WBlockDropdown.findEntryIndex(entries, selected), onSelect);
        dropdown.setLabel(tr(labelKey));
        dropdown.setGuiRelativePos(x, y);
        rootWidget.addChild(dropdown);
        return dropdown;
    }

    private void stepBoundaryX(int delta) {
        int value = clampBoundaryChunk(parseIntOrDefault(boundaryChunkXField.getText(),
                desiredConfig.getBoundaryChunkIntervalX()) + delta);
        desiredConfig.setBoundaryChunkIntervalX(value);
        boundaryChunkXField.setText(Integer.toString(value));
        configToPreset();
    }

    private void stepBoundaryZ(int delta) {
        int value = clampBoundaryChunk(parseIntOrDefault(boundaryChunkZField.getText(),
                desiredConfig.getBoundaryChunkIntervalZ()) + delta);
        desiredConfig.setBoundaryChunkIntervalZ(value);
        boundaryChunkZField.setText(Integer.toString(value));
        configToPreset();
    }

    private void stepGapWidth(int delta) {
        int value = clampGapWidth(parseIntOrDefault(gapWidthField.getText(), desiredConfig.getGapWidth()) + delta);
        desiredConfig.setGapWidth(value);
        gapWidthField.setText(Integer.toString(value));
        configToPreset();
    }

    private void regeneratePresetEditor() {
        boolean generationEnabled = desiredConfig.getAllowGenerationChanges();
        presetEditor.children.removeIf(child -> child != layersBlockDropdown);
        if (layersBlockDropdown != null) layersBlockDropdown.enabled = generationEnabled;
        int curX = 24;
        int curY = 0;
        presetEditor.addChild(new WLabel(curX, curY, tr("gui.personalWorld.layers"), false));
        curY += 10;
        List<Layer> layers = desiredConfig.getLayers();
        int end = Math.min(layers.size(), layerListScrollOffset + MAX_VISIBLE_LAYERS);
        for (int visibleIndex = layerListScrollOffset; visibleIndex < end; visibleIndex++) {
            int index = layers.size() - 1 - visibleIndex;
            if (index < 0) break;
            Layer layer = layers.get(index);
            WButton block = new WButton(new Rectangle(curX + 12, curY, 20, 28), "", false, 0, null, null);
            block.enabled = false;
            Block gameBlock = PersonalSpaceBlocks.block(layer.block());
            if (gameBlock == null || gameBlock == Blocks.AIR) {
                block.setText("-");
                WLabel airCount = new WLabel(0, 18, Integer.toString(layer.count()), false);
                airCount.color = 0xFFFFFF;
                airCount.position.x = Math.max(0, block.position.width - airCount.position.width - 1);
                block.addChild(airCount);
                block.tooltip = Blocks.AIR.getName().getString();
            } else {
                block.itemStack = new ItemStack(gameBlock.asItem());
                block.itemStackText = Integer.toString(layer.count());
                block.tooltip = block.itemStack.isEmpty() ? gameBlock.getName().getString() :
                        block.itemStack.getHoverName().getString();
            }
            presetEditor.addChild(block);
            final int finalIndex = index;
            if (index < layers.size() - 1) {
                block.addChild(new WButton(new Rectangle(-12, 0, 10, 10), "", false, 0, Icons.SMALL_UP, () -> {
                    Collections.swap(desiredConfig.getMutableLayers(), finalIndex, finalIndex + 1);
                    configToPreset();
                }));
            }
            block.addChild(new WButton(new Rectangle(-12, 9, 10, 10), "", false, 0, Icons.SMALL_CROSS, () -> {
                desiredConfig.getMutableLayers().remove(finalIndex);
                configToPreset();
            }));
            if (index > 0) {
                block.addChild(new WButton(new Rectangle(-12, 18, 10, 10), "", false, 0, Icons.SMALL_DOWN, () -> {
                    Collections.swap(desiredConfig.getMutableLayers(), finalIndex, finalIndex - 1);
                    configToPreset();
                }));
            }
            IntConsumer plusMinus = sign -> {
                Layer original = desiredConfig.getMutableLayers().get(finalIndex);
                int amount = Screen.hasControlDown() ? 64 : Screen.hasShiftDown() ? 10 : 1;
                int count = Mth.clamp(original.count() + amount * sign, 1, 255);
                desiredConfig.getMutableLayers().set(finalIndex, new Layer(original.block(), count));
                desiredConfig.setLayers(desiredConfig.getLayersAsString());
                configToPreset();
            };
            block.addChild(new WButton(new Rectangle(21, 5, 18, 18), "", false, 0,
                    generationEnabled ? Icons.PLUS : Icons.LOCK, () -> plusMinus.accept(1)));
            block.addChild(new WButton(new Rectangle(40, 5, 18, 18), "", false, 0,
                    generationEnabled ? Icons.MINUS : Icons.LOCK, () -> plusMinus.accept(-1)));
            for (Widget child : block.children) child.enabled = generationEnabled;
            curY += 30;
        }
        if (layers.size() <= MAX_VISIBLE_LAYERS) layerListScrollOffset = 0;
        else layerListScrollOffset = Mth.clamp(layerListScrollOffset, 0, layers.size() - MAX_VISIBLE_LAYERS);
    }

    private void configToPreset() {
        String preset = desiredConfig.getFullPresetString();
        if (preset.isEmpty()) preset = voidPresetName;
        if (!preset.equals(presetEntry.getText())) {
            presetEntry.setText(preset);
            presetEntry.textField.moveCursorToStart();
        }
    }

    private void updateLayerScrollFromMouse(int mouseY) {
        int size = desiredConfig.getLayers().size();
        if (size <= MAX_VISIBLE_LAYERS) return;
        int trackH = MAX_VISIBLE_LAYERS * 30;
        int thumbH = Math.max(20, trackH * MAX_VISIBLE_LAYERS / size);
        int range = trackH - thumbH;
        if (range <= 0) return;
        float ratio = Mth.clamp((float) (mouseY - 10 - thumbH / 2) / range, 0, 1);
        layerListScrollOffset = Math.round(ratio * (size - MAX_VISIBLE_LAYERS));
    }

    private boolean biomeExists(String id) {
        ResourceLocation location = ResourceLocation.tryParse(id);
        return location != null && minecraft != null && minecraft.level != null &&
                minecraft.level.registryAccess().registryOrThrow(Registries.BIOME).containsKey(location);
    }

    private static void colorField(WTextField field, int color, String tooltip) {
        field.textField.setTextColor(color);
        field.textField.setTextColorUneditable(color);
        field.tooltip = tooltip;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        boolean inputsValid = true;
        renderBackground(graphics);
        graphics.pose().pushPose();
        graphics.pose().translate(guiLeft, guiTop, 0.0F);
        mouseX -= guiLeft;
        mouseY -= guiTop;
        Icons.GUI_BG.draw9Patch(graphics, -8, -8, xSize + 16, ySize + 16);

        int red = Mth.clamp(skyRed.getValueInt(), 0, 255);
        int green = Mth.clamp(skyGreen.getValueInt(), 0, 255);
        int blue = Mth.clamp(skyBlue.getValueInt(), 0, 255);
        desiredConfig.setSkyColor(red << 16 | green << 8 | blue);
        desiredConfig.setStarBrightness((float) starBrightness.getValue());
        boolean generationEnabled = desiredConfig.getAllowGenerationChanges();
        generateTrees.enabled = generationEnabled;
        generateVegetation.enabled = generationEnabled;
        for (WButton button : presetButtons) button.enabled = generationEnabled;
        presetScrollLeft.enabled = generationEnabled && presetScrollOffset > 0;
        presetScrollRight.enabled = generationEnabled &&
                presetScrollOffset < Math.max(0, presetButtons.size() - MAX_VISIBLE_PRESETS);
        biome.enabled = generationEnabled;
        biomeEditButton.enabled = generationEnabled;
        biomeEditButton.buttonIcon = generationEnabled ? Icons.PENCIL : Icons.LOCK;
        presetEntry.enabled = generationEnabled;
        boundaryBlockADropdown.enabled = generationEnabled;
        boundaryBlockBDropdown.enabled = generationEnabled;
        boundaryChunkXMinus.enabled = generationEnabled;
        boundaryChunkXPlus.enabled = generationEnabled;
        boundaryChunkZMinus.enabled = generationEnabled;
        boundaryChunkZPlus.enabled = generationEnabled;
        boundaryChunkXField.enabled = generationEnabled;
        boundaryChunkZField.enabled = generationEnabled;
        boolean gapIsSolid = desiredConfig.getGapPreset() == GapPreset.SOLID;
        boolean boundaryIsZero = desiredConfig.getBoundaryChunkIntervalX() == 0 &&
                desiredConfig.getBoundaryChunkIntervalZ() == 0;
        boolean gapEnabled = generationEnabled && !boundaryIsZero;
        gapWidthMinus.enabled = gapEnabled;
        gapWidthPlus.enabled = gapEnabled;
        gapWidthField.enabled = gapEnabled;
        gapPresetButton.enabled = gapEnabled;
        gapBlockADropdown.enabled = gapEnabled && !gapIsSolid;
        gapBlockBDropdown.enabled = gapEnabled && !gapIsSolid;
        gapBlockCDropdown.enabled = gapEnabled && !gapIsSolid;
        applyToAllSurfaceLayersToggle.enabled = generationEnabled;
        boolean centerCanEnable = generationEnabled && !boundaryIsZero;
        centerEnabledToggle.enabled = centerCanEnable;
        boolean centerActive = centerCanEnable && desiredConfig.isCenterEnabled();
        centerDirectionButton.enabled = centerActive;
        centerBlockDropdown.enabled = centerActive;

        String actualText = presetEntry.getText();
        if (voidPresetName.equals(actualText)) actualText = "";
        String layersPart = PersonalSpaceSettings.extractLayersPart(actualText);
        if (!generationEnabled) {
            colorField(presetEntry, 0x909090, null);
        } else if (!PersonalSpaceSettings.PRESET_VALIDATION_PATTERN.matcher(layersPart).matches()) {
            colorField(presetEntry, 0xFF0000, tr("gui.personalWorld.invalidSyntax"));
            inputsValid = false;
        } else if (!PersonalSpaceSettings.canUseLayers(layersPart, PersonalSpaceClientState.allowedBlocks())) {
            colorField(presetEntry, 0xFFFF00, tr("gui.personalWorld.notAllowed"));
            inputsValid = false;
        } else {
            colorField(presetEntry, 0xA0FFA0, null);
            desiredConfig.setLayers(layersPart);
            if (PersonalSpaceSettings.hasExtendedSettings(actualText)) {
                desiredConfig.applyExtendedSettings(actualText);
                syncExtendedInputsFromDesiredConfig();
                updateBoundaryButtons();
                updateGapButtons();
                updateApplyToAllSurfaceLayersButton();
                updateCenterButtons();
            } else {
                clearExtendedPresetInputs();
            }
            regeneratePresetEditor();
        }

        int prevBoundaryX = desiredConfig.getBoundaryChunkIntervalX();
        int prevBoundaryZ = desiredConfig.getBoundaryChunkIntervalZ();
        int prevGapWidth = desiredConfig.getGapWidth();

        desiredConfig.setBiomeId(biome.getText());
        if (!generationEnabled) {
            colorField(biome, 0x909090, null);
        } else if (!biomeExists(desiredConfig.getBiomeId())) {
            colorField(biome, 0xFF0000, tr("gui.personalWorld.invalidSyntax"));
            inputsValid = false;
        } else if (!PersonalSpaceSettings.canUseBiome(desiredConfig.getBiomeId(),
                PersonalSpaceClientState.allowedBiomes())) {
                    colorField(biome, 0xFFFF00, tr("gui.personalWorld.notAllowed"));
                    inputsValid = false;
                } else {
                    colorField(biome, 0xA0FFA0, null);
                }

        if (generationEnabled) {
            int bx = parseIntOrDefault(boundaryChunkXField.getText(), 0);
            if (bx < 0 || bx > 20) {
                colorField(boundaryChunkXField, 0xFF0000, tr("gui.personalWorld.boundary.range"));
                inputsValid = false;
            } else {
                colorField(boundaryChunkXField, 0xA0FFA0, tr("gui.personalWorld.boundary.range"));
                desiredConfig.setBoundaryChunkIntervalX(bx);
            }
            int bz = parseIntOrDefault(boundaryChunkZField.getText(), 0);
            if (bz < 0 || bz > 20) {
                colorField(boundaryChunkZField, 0xFF0000, tr("gui.personalWorld.boundary.range"));
                inputsValid = false;
            } else {
                colorField(boundaryChunkZField, 0xA0FFA0, tr("gui.personalWorld.boundary.range"));
                desiredConfig.setBoundaryChunkIntervalZ(bz);
            }
            if (!allowedOrEmpty(allowedBoundaryBlocks, desiredConfig.getBoundaryBlockA()) ||
                    !allowedOrEmpty(allowedBoundaryBlocks, desiredConfig.getBoundaryBlockB())) {
                inputsValid = false;
            }
            updateBoundaryButtons();

            boolean gapBoundaryZero = desiredConfig.getBoundaryChunkIntervalX() == 0 &&
                    desiredConfig.getBoundaryChunkIntervalZ() == 0;
            if (!gapBoundaryZero) {
                int gw = parseIntOrDefault(gapWidthField.getText(), 0);
                if (gw < 0 || gw > 5) {
                    colorField(gapWidthField, 0xFF0000, tr("gui.personalWorld.gap.widthRange"));
                    inputsValid = false;
                } else {
                    colorField(gapWidthField, 0xA0FFA0, tr("gui.personalWorld.gap.widthRange"));
                    desiredConfig.setGapWidth(gw);
                }
                if (!allowedOrEmpty(allowedGapBlocks, desiredConfig.getGapBlockA())) inputsValid = false;
                if (!gapIsSolid && !allowedOrEmpty(allowedGapBlocks, desiredConfig.getGapBlockB())) {
                    inputsValid = false;
                }
            } else {
                colorField(gapWidthField, 0x909090, gapWidthField.tooltip);
            }
            updateGapButtons();
            if (!boundaryIsZero && desiredConfig.isCenterEnabled() &&
                    !allowedOrEmpty(allowedCenterBlocks, desiredConfig.getCenterBlock())) {
                inputsValid = false;
            }
            updateCenterButtons();
            syncPresetFromExtendedInputsIfChanged(prevBoundaryX, prevBoundaryZ, prevGapWidth);
        } else {
            colorField(boundaryChunkXField, 0x909090, null);
            colorField(boundaryChunkZField, 0x909090, null);
            colorField(gapWidthField, 0x909090, null);
        }
        save.enabled = inputsValid;

        rootWidget.draw(graphics, mouseX, mouseY, partialTicks);

        if (currentPage == 0) {
            int size = desiredConfig.getLayers().size();
            if (size > MAX_VISIBLE_LAYERS) {
                int trackH = MAX_VISIBLE_LAYERS * 30;
                int maxScroll = size - MAX_VISIBLE_LAYERS;
                int thumbH = Math.max(20, trackH * MAX_VISIBLE_LAYERS / size);
                int thumbY = 10 + (int) ((float) (trackH - thumbH) * layerListScrollOffset / maxScroll);
                graphics.fill(288, 10, 294, 10 + trackH, 0xFF404040);
                graphics.fill(288, thumbY, 294, thumbY + thumbH, 0xFFA0A0A0);
            }
            int swatchY = skyRed.position.y;
            int swatchH = 3 * (skyRed.position.height + 1);
            graphics.fill(130, swatchY, 162, swatchY + swatchH, 0xFF000000);
            graphics.fill(131, swatchY + 1, 161, swatchY + swatchH - 1, 0xFF000000 | desiredConfig.getSkyColor());
            graphics.setColor(1, 1, 1, desiredConfig.getStarBrightness());
            Icons.STAR.drawAt(graphics, 132, swatchY + 2);
            Icons.STAR.drawAt(graphics, 145, swatchY + 12);
            Icons.STAR.drawAt(graphics, 134, swatchY + 21);
            graphics.setColor(1, 1, 1, 1);
        }

        WBlockDropdown.setScreenDimensions(width, height, guiLeft, guiTop);
        WBlockDropdown.drawOpenDropdown(graphics, mouseX, mouseY, partialTicks);
        rootWidget.drawForeground(graphics, mouseX, mouseY, partialTicks);
        graphics.pose().popPose();
    }

    private static boolean allowedOrEmpty(Collection<String> allowed, String block) {
        return block.isEmpty() || allowed.contains(block);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (WBlockDropdown.isAnyDropdownOpen()) {
            if (key == 256) {
                WBlockDropdown.closeDropdown();
                return true;
            }
            WBlockDropdown.handleOpenDropdownKeyPressed(key, scanCode, modifiers);
            return true;
        }
        if (rootWidget.keyPressed(key, scanCode, modifiers)) return true;
        if (key != 256 && anyFieldFocused()) return true;
        return super.keyPressed(key, scanCode, modifiers);
    }

    private boolean anyFieldFocused() {
        return focused(presetEntry) || focused(biome) || focused(boundaryChunkXField) ||
                focused(boundaryChunkZField) || focused(gapWidthField);
    }

    @Override
    public boolean charTyped(char character, int modifiers) {
        if (WBlockDropdown.isAnyDropdownOpen()) {
            return WBlockDropdown.handleOpenDropdownCharTyped(character, modifiers);
        }
        return rootWidget.charTyped(character, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = (int) mouseX - guiLeft;
        int y = (int) mouseY - guiTop;
        if (WBlockDropdown.handleOpenDropdownClick(x, y, button)) return true;
        if (currentPage == 0 && button == 0 && desiredConfig.getLayers().size() > MAX_VISIBLE_LAYERS &&
                x >= 288 && x < 294 && y >= 10 && y < 10 + MAX_VISIBLE_LAYERS * 30) {
            layerScrollbarDragging = true;
            updateLayerScrollFromMouse(y);
            return true;
        }
        rootWidget.mouseClicked(x, y, button);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        int x = (int) mouseX - guiLeft;
        int y = (int) mouseY - guiTop;
        WBlockDropdown.handleOpenDropdownMouseUp();
        layerScrollbarDragging = false;
        rootWidget.mouseReleased(x, y, button);
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        int x = (int) mouseX - guiLeft;
        int y = (int) mouseY - guiTop;
        if (WBlockDropdown.handleOpenDropdownDrag(x, y)) return true;
        if (layerScrollbarDragging) {
            updateLayerScrollFromMouse(y);
            return true;
        }
        rootWidget.mouseDragged(x, y, button);
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int x = (int) mouseX - guiLeft;
        int y = (int) mouseY - guiTop;
        if (WBlockDropdown.handleOpenDropdownScroll(x, y, delta)) return true;
        if (currentPage == 0 && presetButtons.size() > MAX_VISIBLE_PRESETS) {
            int presetY = presetButtons.get(0).position.y;
            if (y >= presetY && y < presetY + 18 && x >= 0 && x < 168) {
                int maxOffset = Math.max(0, presetButtons.size() - MAX_VISIBLE_PRESETS);
                presetScrollOffset = delta > 0 ? Math.max(0, presetScrollOffset - 1) :
                        Math.min(maxOffset, presetScrollOffset + 1);
                updatePresetButtonPositions();
            }
        }
        if (currentPage == 0 && presetEditor != null) {
            int peX = presetEditor.position.x;
            int peY = presetEditor.position.y;
            if (x >= peX && x < peX + 200 && y >= peY && y < peY + 220) {
                int maxScroll = Math.max(0, desiredConfig.getLayers().size() - MAX_VISIBLE_LAYERS);
                layerListScrollOffset = delta > 0 ? Math.max(0, layerListScrollOffset - 1) :
                        Math.min(maxScroll, layerListScrollOffset + 1);
            }
        }
        return true;
    }

    @Override
    public void removed() {
        WBlockDropdown.closeDropdown();
        if (previewPanel != null) previewPanel.close();
        super.removed();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
