package com.raishxn.gtna.common.machine.noenergy.platformdeployment;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.common.data.GTNAItems;
import com.raishxn.gtna.common.item.CoordinateCardBehavior;
import com.raishxn.gtna.network.GTNANetworkHandler;
import com.raishxn.gtna.network.packet.SRegionHighlightPacket;
import com.raishxn.gtna.network.packet.SStructureGhostPreviewPacket;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PlatformDeploymentMachine extends MetaMachine implements IFancyUIMachine, IMachineLife {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            PlatformDeploymentMachine.class, MetaMachine.MANAGED_FIELD_HOLDER);

    private static final int INTRODUCTION = 0;
    private static final int PRESET_SELECTION = 1;
    private static final int CONFIRM_CONSUMABLES = 2;
    private static final int ADJUST_SETTINGS = 3;
    private static final int TOTAL_STEP = 3;
    private static final int LANG_WIDTH = 282 - 8;
    private static final int GHOST_PREVIEW_LIMIT = 2048;
    private static final int GHOST_PREVIEW_COLOR = 0x4488D8FF;

    private static final List<List<MaterialValue>> ITEM_VALUE_HOLDERS = List.of(
            List.of(
                    new MaterialValue(() -> GTNAItems.INDUSTRIAL_COMPONENTS[0][2].asItem(), 5000),
                    new MaterialValue(() -> GTNAItems.INDUSTRIAL_COMPONENTS[0][1].asItem(), 1000),
                    new MaterialValue(() -> GTNAItems.INDUSTRIAL_COMPONENTS[0][0].asItem(), 200)),
            List.of(
                    new MaterialValue(() -> GTNAItems.INDUSTRIAL_COMPONENTS[1][2].asItem(), 5000),
                    new MaterialValue(() -> GTNAItems.INDUSTRIAL_COMPONENTS[1][1].asItem(), 1000),
                    new MaterialValue(() -> GTNAItems.INDUSTRIAL_COMPONENTS[1][0].asItem(), 200)),
            List.of(
                    new MaterialValue(() -> GTNAItems.INDUSTRIAL_COMPONENTS[2][2].asItem(), 5000),
                    new MaterialValue(() -> GTNAItems.INDUSTRIAL_COMPONENTS[2][1].asItem(), 1000),
                    new MaterialValue(() -> GTNAItems.INDUSTRIAL_COMPONENTS[2][0].asItem(), 200)));

    @Persisted
    private final NotifiableItemStackHandler inventory;

    private final List<PlatformBlockType.PlatformPreset> presets = PlatformTemplateStorage.initializePresets();
    private final int maxGroup;

    private TickableSubscription placementSubscription;
    private PlatformStructurePlacer placementJob;

    private int step = 0;

    @Persisted @DescSynced private boolean presetConfirm = false;
    @Persisted @DescSynced private int checkGroup = 0;
    @Persisted @DescSynced private int checkId = 0;
    @Persisted @DescSynced private int saveGroup = 0;
    @Persisted @DescSynced private int saveId = 0;
    @Persisted @DescSynced private boolean preview = false;
    @Persisted @DescSynced private boolean highlight = false;

    @Persisted @DescSynced private int offsetX = 0;
    @Persisted @DescSynced private int offsetZ = 0;
    @Persisted @DescSynced private int offsetY = -1;
    @Persisted @DescSynced private int adjustX = 0;
    @Persisted @DescSynced private int adjustZ = 0;
    @Persisted @DescSynced private int adjustY = 0;
    @Persisted @DescSynced private BlockPos pos1 = BlockPos.ZERO;
    @Persisted @DescSynced private BlockPos pos2 = BlockPos.ZERO;

    @Persisted private final int[] materialInventory = new int[] { 0, 0, 0 };
    @Persisted @DescSynced private boolean insufficient = false;

    @Persisted @DescSynced private boolean taskCompleted = true;
    @Persisted @DescSynced private boolean skipAir = true;
    @Persisted @DescSynced private boolean updateLight = true;
    @Persisted @DescSynced private int speed = 50;
    @Persisted @DescSynced private boolean xMirror = false;
    @Persisted @DescSynced private boolean zMirror = false;
    @Persisted @DescSynced private int rotation = 0;
    @Persisted @DescSynced private boolean canExport = false;
    @Persisted @DescSynced private int progress = 0;

    private static final Component EMPTY = Component.empty();

    public PlatformDeploymentMachine(IMachineBlockEntity holder) {
        super(holder);
        this.inventory = new NotifiableItemStackHandler(this, 27, IO.NONE, IO.BOTH);
        this.inventory.addChangedListener(this::examineMaterial);
        this.maxGroup = presets.size();
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        examineMaterial();
        posChanged();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        stopPlacementJob();
        stopGhostPreview();
    }

    @Override
    public void onMachineRemoved() {
        stopGhostPreview();
        stopPlacementJob();
        unloadingMaterial();
    }

    public static void highlightRegion(ResourceKey<Level> dimension, BlockPos start, BlockPos end, int color, int durationTicks) {
        GTNANetworkHandler.sendToAll(new SRegionHighlightPacket(
                start, end, dimension, color, System.currentTimeMillis() + durationTicks * 50L, false));
    }

    public static void stopHighlight(BlockPos start, BlockPos end, ResourceKey<Level> dimension) {
        GTNANetworkHandler.sendToAll(new SRegionHighlightPacket(start, end, dimension, 0, 0L, true));
    }

    public static void showGhostPreview(ResourceKey<Level> dimension, List<BlockPos> positions, int color, int durationTicks) {
        GTNANetworkHandler.sendToAll(new SStructureGhostPreviewPacket(
                dimension, positions, color, System.currentTimeMillis() + durationTicks * 50L, false));
    }

    public static void stopGhostPreview(ResourceKey<Level> dimension) {
        GTNANetworkHandler.sendToAll(new SStructureGhostPreviewPacket(dimension, List.of(), 0, 0L, true));
    }

    @Override
    public Widget createUIWidget() {
        int width = 336;
        int height = 160;
        WidgetGroup group = new WidgetGroup(0, 0, width + 8, height + 8);

        WidgetGroup titleGroup = new DraggableScrollableWidgetGroup(4, 4, 282, height)
                .setBackground(GuiTextures.DISPLAY);
        titleGroup.addWidget(new ComponentPanelWidget(4, 5, this::addDisplayTextTitle));
        group.addWidget(titleGroup);

        DraggableScrollableWidgetGroup mainContentGroup = new DraggableScrollableWidgetGroup(4, 20, 282, height - 16);
        mainContentGroup.addWidget(new ComponentPanelWidget(4, 0, this::addDisplayText)
                .clickHandler(this::handleDisplayClick)
                .setMaxWidthLimit(LANG_WIDTH));
        mainContentGroup.addWidget(new ComponentPanelWidget(4 + LANG_WIDTH / 2, 0, this::addDisplayText2)
                .clickHandler(this::handleDisplayClick)
                .setMaxWidthLimit(LANG_WIDTH / 2));
        mainContentGroup.addWidget(new ComponentPanelWidget(4 + LANG_WIDTH / 4, 0, this::addDisplayText3)
                .clickHandler(this::handleDisplayClick)
                .setMaxWidthLimit(LANG_WIDTH / 4 * 3));
        mainContentGroup.addWidget(new ComponentPanelWidget(4 + LANG_WIDTH / 4 * 3, 0, this::addDisplayText4)
                .clickHandler(this::handleDisplayClick)
                .setMaxWidthLimit(LANG_WIDTH / 4));
        mainContentGroup.addWidget(new ComponentPanelWidget(4 + 88, 0, this::addDisplayText5)
                .clickHandler(this::handleDisplayClick)
                .setMaxWidthLimit(LANG_WIDTH / 2));
        mainContentGroup.addWidget(new ComponentPanelWidget(4 + LANG_WIDTH / 2, 0, this::addDisplayText6)
                .clickHandler(this::handleDisplayClick)
                .setMaxWidthLimit(LANG_WIDTH / 2));
        mainContentGroup.addWidget(new ComponentPanelWidget(4 + LANG_WIDTH - 62, 0, this::addDisplayText7)
                .clickHandler(this::handleDisplayClick)
                .setMaxWidthLimit(LANG_WIDTH / 4));
        mainContentGroup.addWidget(new ImageWidget(282 - 100, 46, 100, 100, this::getIGuiTexture));
        group.addWidget(mainContentGroup);

        WidgetGroup startGroup = new DraggableScrollableWidgetGroup(width - 49, 4, 54, 105)
                .setBackground(GuiTextures.CLIPBOARD_PAPER_BACKGROUND);
        startGroup.addWidget(new ComponentPanelWidget(8, 4, this::addDisplayTextStep)
                .clickHandler((componentData, clickData) -> handleDisplayClickStep(componentData, mainContentGroup)));
        startGroup.addWidget(new ComponentPanelWidget(8, 20, this::addDisplayTextStart)
                .clickHandler(this::handleDisplayClickStart)
                .setMaxWidthLimit(38));
        group.addWidget(startGroup);

        WidgetGroup slotGroup = new DraggableScrollableWidgetGroup(width - 49, 110, 54, 54);
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 3; x++) {
                int slotIndex = y * 3 + x;
                slotGroup.addWidget(new SlotWidget(inventory, slotIndex, x * 18, y * 18, true, true)
                        .setBackground(GuiTextures.SLOT));
            }
        }
        group.addWidget(slotGroup);

        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    private void addDisplayTextTitle(List<Component> textList) {
        textList.add(Component.translatable("gtna.machine.industrial_platform_deployment_tools.title." + step));
    }

    private void addDisplayTextStep(List<Component> textList) {
        MutableComponent result = Component.empty();
        for (int i = 0; i <= TOTAL_STEP; i++) {
            result = result.append(ComponentPanelWidget.withButton(
                    Component.literal(step == i ? "[*]" : "[ ]"), "step_" + i));
        }
        textList.add(result);
    }

    private void handleDisplayClickStep(String componentData, DraggableScrollableWidgetGroup group) {
        String[] parts = componentData.split("_", 2);
        if (parts.length == 2 && "step".equals(parts[0])) {
            group.setScrollYOffset(0);
            step = Mth.clamp(Integer.parseInt(parts[1]), 0, TOTAL_STEP);
        }
    }

    private void addDisplayText(List<Component> textList) {
        switch (step) {
            case INTRODUCTION -> {
                textList.add(Component.translatable("gtna.machine.industrial_platform_deployment_tools.intro.0"));
                textList.add(Component.translatable("gtna.machine.industrial_platform_deployment_tools.intro.1"));
                textList.add(Component.translatable("gtna.machine.industrial_platform_deployment_tools.intro.2"));
                textList.add(EMPTY);
                textList.add(Component.translatable("gtna.machine.industrial_platform_deployment_tools.intro.3"));
                textList.add(Component.translatable("gtna.machine.industrial_platform_deployment_tools.intro.4"));
            }
            case PRESET_SELECTION -> {
                PlatformBlockType.PlatformPreset group = getPlatformPreset(checkGroup);
                PlatformBlockType.PlatformBlockStructure structure = getPlatformBlockStructure(checkGroup, checkId);

                Component leftBtn1 = ComponentPanelWidget.withButton(Component.literal(" [ < ] "), "previous_group_plas");
                Component leftBtn2 = ComponentPanelWidget.withButton(Component.literal(" [ < ] "), "previous_group");
                Component empty1 = Component.literal(" ".repeat(Math.max(0, 15 - ((checkGroup + 1) / 10 + maxGroup / 10 + 5) / 2)));
                textList.add(Component.empty().append(leftBtn1).append(leftBtn2).append(empty1)
                        .append(Component.literal("<" + (checkGroup + 1) + "/" + maxGroup + ">")));

                int totalIds = getPlatformPreset(checkGroup).structures().size();
                Component leftBtn3 = ComponentPanelWidget.withButton(Component.literal(" [ < ] "), "previous_id_plas");
                Component leftBtn4 = ComponentPanelWidget.withButton(Component.literal(" [ < ] "), "previous_id");
                Component empty2 = Component.literal(" ".repeat(Math.max(0, 15 - ((checkId + 1) / 10 + totalIds / 10 + 5) / 2)));
                textList.add(Component.empty().append(leftBtn3).append(leftBtn4).append(empty2)
                        .append(Component.literal("<" + (checkId + 1) + "/" + totalIds + ">")));

                textList.add(Component.translatable("gtna.machine.industrial_platform_deployment_tools.text.choose_this")
                        .append(ComponentPanelWidget.withButton(Component.literal("[OK]"), "choose_this")));

                textList.add(structure.preview()
                        ? Component.translatable("gtna.machine.industrial_platform_deployment_tools.text.preview")
                                .append(ComponentPanelWidget.withButton(Component.literal("[IMG]"), "preview"))
                        : EMPTY);

                textList.add(Component.translatable("gtna.machine.industrial_platform_deployment_tools.text.size",
                        structure.xSize(), structure.ySize(), structure.zSize(),
                        structure.xSize() >> 4, structure.zSize() >> 4));

                if (group.displayName() != null) textList.add(Component.literal(group.displayName()));
                if (group.description() != null) textList.add(Component.literal(group.description()));
                if (group.source() != null) {
                    textList.add(Component.translatable("gtna.machine.industrial_platform_deployment_tools.text.source", group.source()));
                }
                if (structure.displayName() != null) textList.add(Component.literal(structure.displayName()));
                if (structure.type() != null) textList.add(Component.literal(structure.type()));
                if (structure.description() != null) textList.add(Component.literal(structure.description()));
                if (structure.source() != null) {
                    textList.add(Component.translatable("gtna.machine.industrial_platform_deployment_tools.text.source", structure.source()));
                }
            }
            case CONFIRM_CONSUMABLES -> {
                textList.add(ComponentPanelWidget.withButton(
                        Component.translatable("gtna.machine.industrial_platform_deployment_tools.material.loading"),
                        "loading"));
                textList.add(Component.translatable("gtna.machine.industrial_platform_deployment_tools.material.reserves"));
                textList.add(EMPTY);

                if (!presetConfirm) {
                    textList.add(Component.translatable("gtna.machine.industrial_platform_deployment_tools.text.unselected"));
                } else {
                    PlatformBlockType.PlatformBlockStructure structure = getPlatformBlockStructure(saveGroup, saveId);
                    textList.add(Component.translatable("gtna.machine.industrial_platform_deployment_tools.material.demand"));
                    textList.add(EMPTY);
                    List<PlatformSupport.Counted<ItemStack>> extraMaterials = structure.extraMaterials();
                    if (!extraMaterials.isEmpty()) {
                        textList.add(Component.translatable("gtna.machine.industrial_platform_deployment_tools.material.extra_demand"));
                        extraMaterials.forEach(e -> textList.add(Component.literal("[")
                                .append(e.value().getDisplayName()).append("x" + e.amount() + "]")));
                    }
                    textList.add(Component.translatable(insufficient
                            ? "gtna.machine.industrial_platform_deployment_tools.material.adequate"
                            : "gtna.machine.industrial_platform_deployment_tools.material.insufficient"));
                }
            }
            case ADJUST_SETTINGS -> {
                textList.add(Component.translatable("gtna.machine.industrial_platform_deployment_tools.offset"));
                textList.add(Component.translatable("gtna.machine.industrial_platform_deployment_tools.offset.x", offsetX));
                textList.add(Component.translatable("gtna.machine.industrial_platform_deployment_tools.offset.y", offsetY));
                textList.add(Component.translatable("gtna.machine.industrial_platform_deployment_tools.offset.z", offsetZ));
                textList.add(EMPTY);

                if (!presetConfirm) {
                    textList.add(Component.translatable("gtna.machine.industrial_platform_deployment_tools.text.unselected"));
                } else {
                    textList.add(Component.translatable("gtna.machine.industrial_platform_deployment_tools.boundary"));
                    textList.add(Component.translatable("gtna.machine.industrial_platform_deployment_tools.offset.x", pos2.getX()));
                    textList.add(Component.translatable("gtna.machine.industrial_platform_deployment_tools.offset.y", pos2.getY()));
                    textList.add(Component.translatable("gtna.machine.industrial_platform_deployment_tools.offset.z", pos2.getZ()));
                }

                textList.add(EMPTY);
                textList.add(Component.translatable("gtna.machine.industrial_platform_deployment_tools.skipAir")
                        .append(ComponentPanelWidget.withButton(toggleText(skipAir), "skipAir")));
                textList.add(Component.translatable("gtna.machine.industrial_platform_deployment_tools.xMirror")
                        .append(ComponentPanelWidget.withButton(toggleText(xMirror), "xMirror")));
                textList.add(Component.translatable("gtna.machine.industrial_platform_deployment_tools.rotation")
                        .append(ComponentPanelWidget.withButton(Component.literal(String.valueOf(rotation)), "rotation")));
            }
            default -> {}
        }
    }

    private void addDisplayText2(List<Component> textList) {
        switch (step) {
            case PRESET_SELECTION -> {
                textList.add(EMPTY);
                textList.add(EMPTY);
                textList.add(presetConfirm
                        ? Component.translatable("gtna.machine.industrial_platform_deployment_tools.text.selected", saveGroup + 1, saveId + 1)
                        : Component.translatable("gtna.machine.industrial_platform_deployment_tools.text.unselected"));
                textList.add(Component.translatable("gtna.machine.industrial_platform_deployment_tools.text.highlight")
                        .append(ComponentPanelWidget.withButton(Component.literal("[HL]"), "highlight")));
            }
            case CONFIRM_CONSUMABLES -> {
                textList.add(ComponentPanelWidget.withButton(
                        Component.translatable("gtna.machine.industrial_platform_deployment_tools.material.unloading"),
                        "unloading"));
                textList.add(Component.translatable("gtna.machine.industrial_platform_deployment_tools.material.1"));
                textList.add(Component.literal(String.valueOf(materialInventory[1])));
                if (!presetConfirm) {
                    textList.add(EMPTY);
                } else {
                    int[] costMaterial = getPlatformBlockStructure(saveGroup, saveId).materials();
                    textList.add(Component.translatable("gtna.machine.industrial_platform_deployment_tools.material.1"));
                    textList.add(Component.literal(String.valueOf(costMaterial[1])));
                }
            }
            case ADJUST_SETTINGS -> {
                textList.add(EMPTY);
                textList.add(EMPTY);
                textList.add(EMPTY);
                textList.add(EMPTY);
                textList.add(EMPTY);
                if (!presetConfirm) {
                    textList.add(EMPTY);
                } else {
                    textList.add(EMPTY);
                    textList.add(Component.literal(String.valueOf(pos1.getX())));
                    textList.add(Component.literal(String.valueOf(pos1.getY())));
                    textList.add(Component.literal(String.valueOf(pos1.getZ())));
                }
                textList.add(EMPTY);
                textList.add(Component.translatable("gtna.machine.industrial_platform_deployment_tools.updateLight")
                        .append(ComponentPanelWidget.withButton(toggleText(updateLight), "updateLight")));
                textList.add(Component.translatable("gtna.machine.industrial_platform_deployment_tools.zMirror")
                        .append(ComponentPanelWidget.withButton(toggleText(zMirror), "zMirror")));
                textList.add(Component.translatable("gtna.machine.industrial_platform_deployment_tools.speed")
                        .append(String.valueOf(speed))
                        .append(ComponentPanelWidget.withButton(Component.literal("[+]"), "+speed"))
                        .append(ComponentPanelWidget.withButton(Component.literal("[-]"), "-speed")));
            }
            default -> {}
        }
    }

    private void addDisplayText3(List<Component> textList) {
        if (step == CONFIRM_CONSUMABLES) {
            textList.add(EMPTY);
            textList.add(Component.translatable("gtna.machine.industrial_platform_deployment_tools.material.0"));
            textList.add(Component.literal(String.valueOf(materialInventory[0])));
            if (!presetConfirm) {
                textList.add(EMPTY);
            } else {
                int[] costMaterial = getPlatformBlockStructure(saveGroup, saveId).materials();
                textList.add(Component.translatable("gtna.machine.industrial_platform_deployment_tools.material.0"));
                textList.add(Component.literal(String.valueOf(costMaterial[0])));
            }
        }
    }

    private void addDisplayText4(List<Component> textList) {
        if (step == CONFIRM_CONSUMABLES) {
            textList.add(EMPTY);
            textList.add(Component.translatable("gtna.machine.industrial_platform_deployment_tools.material.2"));
            textList.add(Component.literal(String.valueOf(materialInventory[2])));
            if (!presetConfirm) {
                textList.add(EMPTY);
            } else {
                int[] costMaterial = getPlatformBlockStructure(saveGroup, saveId).materials();
                textList.add(Component.translatable("gtna.machine.industrial_platform_deployment_tools.material.2"));
                textList.add(Component.literal(String.valueOf(costMaterial[2])));
            }
        }
    }

    private void addDisplayText5(List<Component> textList) {
        if (step == ADJUST_SETTINGS) {
            textList.add(EMPTY);
            textList.add(Component.empty()
                    .append(ComponentPanelWidget.withButton(Component.literal("[-]"), "x_minus"))
                    .append(ComponentPanelWidget.withButton(Component.literal("[+]"), "x_add")));
            textList.add(Component.empty()
                    .append(ComponentPanelWidget.withButton(Component.literal("[-]"), "y_minus"))
                    .append(ComponentPanelWidget.withButton(Component.literal("[+]"), "y_add")));
            textList.add(Component.empty()
                    .append(ComponentPanelWidget.withButton(Component.literal("[-]"), "z_minus"))
                    .append(ComponentPanelWidget.withButton(Component.literal("[+]"), "z_add")));
        }
    }

    private void addDisplayText6(List<Component> textList) {
        if (step == ADJUST_SETTINGS) {
            textList.add(EMPTY);
            textList.add(Component.empty()
                    .append(ComponentPanelWidget.withButton(Component.literal("[-]"), "adjust_x_minus"))
                    .append(ComponentPanelWidget.withButton(Component.literal("[-" + adjustX + "]"), "x_minus_plas"))
                    .append(ComponentPanelWidget.withButton(Component.literal("[+" + adjustX + "]"), "x_add_plas"))
                    .append(ComponentPanelWidget.withButton(Component.literal("[+]"), "adjust_x_add")));
            textList.add(Component.empty()
                    .append(ComponentPanelWidget.withButton(Component.literal("[-]"), "adjust_y_minus"))
                    .append(ComponentPanelWidget.withButton(Component.literal("[-" + adjustY + "]"), "y_minus_plas"))
                    .append(ComponentPanelWidget.withButton(Component.literal("[+" + adjustY + "]"), "y_add_plas"))
                    .append(ComponentPanelWidget.withButton(Component.literal("[+]"), "adjust_y_add")));
            textList.add(Component.empty()
                    .append(ComponentPanelWidget.withButton(Component.literal("[-]"), "adjust_z_minus"))
                    .append(ComponentPanelWidget.withButton(Component.literal("[-" + adjustZ + "]"), "z_minus_plas"))
                    .append(ComponentPanelWidget.withButton(Component.literal("[+" + adjustZ + "]"), "z_add_plas"))
                    .append(ComponentPanelWidget.withButton(Component.literal("[+]"), "adjust_z_add")));
        }
    }

    private void addDisplayText7(List<Component> textList) {
        if (step == PRESET_SELECTION) {
            textList.add(Component.empty()
                    .append(ComponentPanelWidget.withButton(Component.literal(" [ > ] "), "next_group"))
                    .append(ComponentPanelWidget.withButton(Component.literal(" [ > ] "), "next_group_plas")));
            textList.add(Component.empty()
                    .append(ComponentPanelWidget.withButton(Component.literal(" [ > ] "), "next_id"))
                    .append(ComponentPanelWidget.withButton(Component.literal(" [ > ] "), "next_id_plas")));
        }
    }

    private void handleDisplayClick(String componentData, ClickData clickData) {
        switch (step) {
            case PRESET_SELECTION -> {
                int maxId = getPlatformPreset(checkGroup).structures().size() - 1;
                switch (componentData) {
                    case "next_group" -> { checkGroup = Mth.clamp(checkGroup + 1, 0, maxGroup - 1); checkId = 0; }
                    case "previous_group" -> { checkGroup = Mth.clamp(checkGroup - 1, 0, maxGroup - 1); checkId = 0; }
                    case "next_group_plas" -> { checkGroup = Mth.clamp(checkGroup + 10, 0, maxGroup - 1); checkId = 0; }
                    case "previous_group_plas" -> { checkGroup = Mth.clamp(checkGroup - 10, 0, maxGroup - 1); checkId = 0; }
                    case "next_id" -> checkId = Mth.clamp(checkId + 1, 0, maxId);
                    case "previous_id" -> checkId = Mth.clamp(checkId - 1, 0, maxId);
                    case "next_id_plas" -> checkId = Mth.clamp(checkId + 5, 0, maxId);
                    case "previous_id_plas" -> checkId = Mth.clamp(checkId - 5, 0, maxId);
                    case "choose_this" -> {
                        saveGroup = checkGroup;
                        saveId = checkId;
                        presetConfirm = true;
                        examineMaterial();
                        posChanged();
                    }
                    case "preview" -> preview = !preview;
                    case "highlight" -> {
                        highlight = !highlight;
                        highlightArea(highlight);
                    }
                    default -> {}
                }
            }
            case CONFIRM_CONSUMABLES -> {
                if ("loading".equals(componentData)) {
                    loadingMaterial();
                    examineMaterial();
                } else if ("unloading".equals(componentData)) {
                    unloadingMaterial();
                    examineMaterial();
                }
            }
            case ADJUST_SETTINGS -> {
                switch (componentData) {
                    case "x_add" -> { offsetX++; posChanged(); }
                    case "x_minus" -> { offsetX--; posChanged(); }
                    case "z_add" -> { offsetZ++; posChanged(); }
                    case "z_minus" -> { offsetZ--; posChanged(); }
                    case "y_add" -> { offsetY++; posChanged(); }
                    case "y_minus" -> { offsetY--; posChanged(); }
                    case "x_add_plas" -> { offsetX += adjustX; posChanged(); }
                    case "x_minus_plas" -> { offsetX -= adjustX; posChanged(); }
                    case "adjust_x_add" -> adjustX = Math.max(0, adjustX + 1);
                    case "adjust_x_minus" -> adjustX = Math.max(0, adjustX - 1);
                    case "z_add_plas" -> { offsetZ += adjustZ; posChanged(); }
                    case "z_minus_plas" -> { offsetZ -= adjustZ; posChanged(); }
                    case "adjust_z_add" -> adjustZ = Math.max(0, adjustZ + 1);
                    case "adjust_z_minus" -> adjustZ = Math.max(0, adjustZ - 1);
                    case "y_add_plas" -> { offsetY += adjustY; posChanged(); }
                    case "y_minus_plas" -> { offsetY -= adjustY; posChanged(); }
                    case "adjust_y_add" -> adjustY = Math.max(0, adjustY + 1);
                    case "adjust_y_minus" -> adjustY = Math.max(0, adjustY - 1);
                    case "skipAir" -> skipAir = !skipAir;
                    case "updateLight" -> updateLight = !updateLight;
                    case "xMirror" -> xMirror = !xMirror;
                    case "zMirror" -> zMirror = !zMirror;
                    case "rotation" -> rotation = (rotation + 90) % 360;
                    case "+speed" -> speed = Mth.clamp(speed + 5, 10, 100);
                    case "-speed" -> speed = Mth.clamp(speed - 5, 10, 100);
                    default -> {}
                }
            }
            default -> {}
        }
    }

    private IGuiTexture getIGuiTexture() {
        if (step == PRESET_SELECTION && preview) {
            PlatformBlockType.PlatformBlockStructure structure = getPlatformBlockStructure(checkGroup, checkId);
            if (!structure.preview()) return IGuiTexture.EMPTY;
            ResourceLocation imageLocation = GTNACORE.id("textures/gui/industrial_platform_deployment_tools/" + structure.name() + ".png");
            return new ResourceTexture(imageLocation);
        }
        return IGuiTexture.EMPTY;
    }

    private void addDisplayTextStart(List<Component> textList) {
        if (canExport) {
            textList.add(ComponentPanelWidget.withButton(
                    Component.translatable("gtna.machine.industrial_platform_deployment_tools.export"), "export"));
        } else if (!presetConfirm) {
            textList.add(Component.translatable("gtna.machine.industrial_platform_deployment_tools.text.unselected"));
        } else if (!insufficient) {
            textList.add(Component.translatable("gtna.machine.industrial_platform_deployment_tools.material.insufficient"));
        } else if (!taskCompleted) {
            textList.add(Component.translatable("gtna.machine.industrial_platform_deployment_tools.doing", progress));
        } else {
            textList.add(ComponentPanelWidget.withButton(
                    Component.translatable("gtna.machine.industrial_platform_deployment_tools.start"), "start"));
        }
    }

    private void handleDisplayClickStart(String componentData, ClickData clickData) {
        if ("start".equals(componentData)) {
            start();
        } else if ("export".equals(componentData)) {
            getPlatform();
        }
    }

    private PlatformBlockType.PlatformPreset getPlatformPreset(int group) {
        try {
            return presets.get(group);
        } catch (Exception exception) {
            checkGroup = 0;
            saveGroup = 0;
            return presets.get(0);
        }
    }

    private PlatformBlockType.PlatformBlockStructure getPlatformBlockStructure(int group, int id) {
        try {
            return getPlatformPreset(group).structures().get(id);
        } catch (Exception exception) {
            checkId = 0;
            saveId = 0;
            return getPlatformPreset(group).structures().get(0);
        }
    }

    private void loadingMaterial() {
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            for (int k = 0; k < ITEM_VALUE_HOLDERS.size(); k++) {
                for (MaterialValue holder : ITEM_VALUE_HOLDERS.get(k)) {
                    if (holder.item() != null && holder.item().equals(stack.getItem())) {
                        materialInventory[k] += holder.value * stack.getCount();
                        inventory.setStackInSlot(i, ItemStack.EMPTY);
                        break;
                    }
                }
            }
        }
    }

    private void unloadingMaterial() {
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty()) continue;
            boolean filled = false;
            for (int k = 0; k < ITEM_VALUE_HOLDERS.size() && !filled; k++) {
                for (MaterialValue holder : ITEM_VALUE_HOLDERS.get(k)) {
                    Item item = holder.item();
                    if (item == null) continue;
                    int count = Math.min(materialInventory[k] / holder.value, 64);
                    if (count > 0) {
                        inventory.setStackInSlot(i, new ItemStack(item, count));
                        materialInventory[k] -= holder.value * count;
                        filled = true;
                        break;
                    }
                }
            }
        }
    }

    private void posChanged() {
        BlockPos pos = getPos();
        if (highlight) {
            highlight = false;
            highlightArea(false);
        }
        PlatformBlockType.PlatformBlockStructure structure = getPlatformBlockStructure(saveGroup, saveId);
        int sizeX = structure.xSize();
        int sizeZ = structure.zSize();
        int sizeY = structure.ySize();

        int chunkMinX = (pos.getX() >> 4) << 4;
        int chunkMinZ = (pos.getZ() >> 4) << 4;
        int centerOffsetX = (sizeX - 1) / 32;
        int centerOffsetZ = (sizeZ - 1) / 32;

        int startX = chunkMinX - centerOffsetX * 16 + offsetX * 16;
        int startZ = chunkMinZ - centerOffsetZ * 16 + offsetZ * 16;
        int startY = pos.getY() + offsetY;
        int maxX = startX + sizeX - 1;
        int maxZ = startZ + sizeZ - 1;
        int maxY = startY + sizeY - 1;

        pos1 = new BlockPos(startX, startY, startZ);
        pos2 = new BlockPos(maxX, maxY, maxZ);
    }

    private void examineMaterial() {
        if (!presetConfirm) {
            insufficient = false;
            canExport = false;
            return;
        }

        PlatformBlockType.PlatformBlockStructure structure = getPlatformBlockStructure(saveGroup, saveId);
        int[] costMaterial = structure.materials();
        boolean materialsSufficient = true;
        canExport = false;

        for (int i = 0; i < materialInventory.length; i++) {
            if (materialInventory[i] < costMaterial[i]) {
                materialsSufficient = false;
                break;
            }
        }

        Map<Item, Integer> inventoryCount = new HashMap<>();
        int coordinateCards = 0;
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            inventoryCount.merge(stack.getItem(), stack.getCount(), Integer::sum);
            if (GTNAItems.COORDINATE_CARD != null && stack.getItem() == GTNAItems.COORDINATE_CARD.asItem()) {
                BlockPos stored = CoordinateCardBehavior.getStoredCoordinates(stack);
                if (stored != null) {
                    if (coordinateCards == 0) pos1 = stored;
                    else pos2 = stored;
                }
                coordinateCards++;
            }
        }

        for (PlatformSupport.Counted<ItemStack> holder : structure.extraMaterials()) {
            int available = inventoryCount.getOrDefault(holder.value().getItem(), 0);
            if (available < holder.amount()) {
                materialsSufficient = false;
                break;
            }
        }

        canExport = coordinateCards > 1;
        insufficient = materialsSufficient;
    }

    private boolean consumeResources() {
        if (!presetConfirm || !insufficient) {
            return false;
        }

        PlatformBlockType.PlatformBlockStructure structure = getPlatformBlockStructure(saveGroup, saveId);
        int[] costMaterial = structure.materials();
        for (int i = 0; i < materialInventory.length; i++) {
            materialInventory[i] -= costMaterial[i];
        }

        for (PlatformSupport.Counted<ItemStack> holder : structure.extraMaterials()) {
            Item item = holder.value().getItem();
            int remaining = holder.amount();
            for (int i = 0; i < inventory.getSlots() && remaining > 0; i++) {
                ItemStack stack = inventory.getStackInSlot(i);
                if (stack.getItem() == item) {
                    int take = Math.min(stack.getCount(), remaining);
                    stack.shrink(take);
                    remaining -= take;
                    inventory.setStackInSlot(i, stack);
                }
            }
            if (remaining > 0) {
                GTNACORE.LOGGER.error("Failed to consume all required resources for platform deployment");
                return false;
            }
        }
        return true;
    }

    private static int[] transform(int lx, int ly, int lz, int sx, int sz, int rot, boolean zMir, boolean xMir) {
        int rx = lx;
        int rz = lz;
        switch (rot) {
            case 90 -> {
                int t = rx;
                rx = sz - 1 - rz;
                rz = t;
            }
            case 180 -> {
                rx = sx - 1 - rx;
                rz = sz - 1 - rz;
            }
            case 270 -> {
                int t = rx;
                rx = rz;
                rz = sx - 1 - t;
            }
            default -> {}
        }
        if (xMir) rx = sx - 1 - rx;
        if (zMir) rz = sz - 1 - rz;
        return new int[] { rx, ly, rz };
    }

    private static int[] calcOffsetsBy8Points(int sx, int sy, int sz, int rot, boolean zMir, boolean xMir) {
        int[][] corners = {
                { 0, 0, 0 }, { sx - 1, 0, 0 }, { 0, sy - 1, 0 }, { sx - 1, sy - 1, 0 },
                { 0, 0, sz - 1 }, { sx - 1, 0, sz - 1 }, { 0, sy - 1, sz - 1 }, { sx - 1, sy - 1, sz - 1 }
        };
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        for (int[] corner : corners) {
            int[] t = transform(corner[0], corner[1], corner[2], sx, sz, rot, zMir, xMir);
            minX = Math.min(minX, t[0]);
            minY = Math.min(minY, t[1]);
            minZ = Math.min(minZ, t[2]);
        }
        return new int[] { -minX, -minY, -minZ };
    }

    private void highlightArea(boolean light) {
        if (!(getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        ResourceKey<Level> dimension = serverLevel.dimension();
        if (!light) {
            stopGhostPreview(dimension);
        }

        if (canExport) {
            BlockPos first = null;
            BlockPos second = null;
            for (int i = 0; i < inventory.getSlots() && (first == null || second == null); i++) {
                ItemStack stack = inventory.getStackInSlot(i);
                if (GTNAItems.COORDINATE_CARD != null && stack.is(GTNAItems.COORDINATE_CARD.asItem())) {
                    if (first == null) first = CoordinateCardBehavior.getStoredCoordinates(stack);
                    else second = CoordinateCardBehavior.getStoredCoordinates(stack);
                }
            }
            if (first != null && second != null) {
                BlockPos min = new BlockPos(
                        Math.min(first.getX(), second.getX()),
                        Math.min(first.getY(), second.getY()),
                        Math.min(first.getZ(), second.getZ()));
                BlockPos max = new BlockPos(
                        Math.max(first.getX(), second.getX()),
                        Math.max(first.getY(), second.getY()),
                        Math.max(first.getZ(), second.getZ()));
                if (light) highlightRegion(dimension, min, max, 0x660099CC, 1200);
                else stopHighlight(min, max, dimension);
            }
            return;
        }

        if (!presetConfirm) return;

        PlatformBlockType.PlatformBlockStructure struct = getPlatformBlockStructure(saveGroup, saveId);
        int sx = struct.xSize();
        int sy = struct.ySize();
        int sz = struct.zSize();
        BlockPos start = pos1;

        boolean zMir = this.xMirror;
        boolean xMir = this.zMirror;
        int rot = this.rotation;
        int[] offsets = calcOffsetsBy8Points(sx, sy, sz, rot, zMir, xMir);
        int ox = offsets[0];
        int oy = offsets[1];
        int oz = offsets[2];

        int[][] corners = {
                { 0, 0, 0 }, { sx - 1, 0, 0 }, { 0, sy - 1, 0 }, { sx - 1, sy - 1, 0 },
                { 0, 0, sz - 1 }, { sx - 1, 0, sz - 1 }, { 0, sy - 1, sz - 1 }, { sx - 1, sy - 1, sz - 1 }
        };

        int minWX = Integer.MAX_VALUE;
        int minWY = Integer.MAX_VALUE;
        int minWZ = Integer.MAX_VALUE;
        int maxWX = Integer.MIN_VALUE;
        int maxWY = Integer.MIN_VALUE;
        int maxWZ = Integer.MIN_VALUE;
        for (int[] corner : corners) {
            int[] transformed = transform(corner[0], corner[1], corner[2], sx, sz, rot, zMir, xMir);
            int wx = start.getX() + transformed[0] + ox;
            int wy = start.getY() + transformed[1] + oy;
            int wz = start.getZ() + transformed[2] + oz;
            minWX = Math.min(minWX, wx);
            maxWX = Math.max(maxWX, wx);
            minWY = Math.min(minWY, wy);
            maxWY = Math.max(maxWY, wy);
            minWZ = Math.min(minWZ, wz);
            maxWZ = Math.max(maxWZ, wz);
        }

        BlockPos minPos = new BlockPos(minWX, minWY, minWZ);
        BlockPos maxPos = new BlockPos(maxWX, maxWY, maxWZ);
        if (light) {
            highlightRegion(dimension, minPos, maxPos, 0x2277FF77, 600);
            try {
                List<BlockPos> previewPositions = PlatformStructurePlacer.collectPreviewPositions(
                        pos1,
                        struct,
                        skipAir,
                        zMirror,
                        xMirror,
                        rotation,
                        GHOST_PREVIEW_LIMIT);
                if (!previewPositions.isEmpty()) {
                    showGhostPreview(dimension, previewPositions, GHOST_PREVIEW_COLOR, 600);
                }
            } catch (IOException exception) {
                GTNACORE.LOGGER.warn("Failed to build ghost preview for platform {}", struct.name(), exception);
            }
        } else {
            stopHighlight(minPos, maxPos, dimension);
        }
    }

    private void tickPlacement() {
        if (placementJob == null) {
            stopPlacementJob();
            return;
        }
        placementJob.tick();
        if (placementJob.isFinished()) {
            stopPlacementJob();
        }
    }

    private void stopPlacementJob() {
        if (placementSubscription != null) {
            placementSubscription.unsubscribe();
            placementSubscription = null;
        }
        placementJob = null;
    }

    private void start() {
        if (!taskCompleted || !(getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!consumeResources()) {
            return;
        }

        posChanged();
        PlatformBlockType.PlatformBlockStructure structure = getPlatformBlockStructure(saveGroup, saveId);
        progress = 0;
        taskCompleted = false;

        try {
            placementJob = PlatformStructurePlacer.create(
                    serverLevel,
                    getPos(),
                    pos1,
                    structure,
                    speed * 1000,
                    true,
                    skipAir,
                    updateLight,
                    zMirror,
                    xMirror,
                    rotation,
                    value -> this.progress = value,
                    () -> {
                        this.taskCompleted = true;
                        this.progress = 100;
                    });
            placementSubscription = subscribeServerTick(placementSubscription, this::tickPlacement);
        } catch (IOException exception) {
            GTNACORE.LOGGER.error(
                    "Industrial Platform Deployment Tool failed for preset {} / {} at {}",
                    getPlatformPreset(saveGroup).name(),
                    structure.name(),
                    structure.resource(),
                    exception);
            taskCompleted = true;
        }

        examineMaterial();
    }

    private void getPlatform() {
        if (!(getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        BlockPos first = null;
        BlockPos second = null;
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (GTNAItems.COORDINATE_CARD != null && stack.getItem() == GTNAItems.COORDINATE_CARD.asItem()) {
                if (first == null) first = CoordinateCardBehavior.getStoredCoordinates(stack);
                else second = CoordinateCardBehavior.getStoredCoordinates(stack);
            }
        }
        if (first != null && second != null) {
            PlatformCreators.exportStructureAsync(serverLevel, first, second, xMirror, zMirror, rotation);
        }
    }

    private static Component toggleText(boolean enabled) {
        return Component.literal(enabled ? "ON" : "OFF");
    }

    private void stopGhostPreview() {
        Level level = getLevel();
        if (level != null) {
            stopGhostPreview(level.dimension());
        }
    }

    private record MaterialValue(java.util.function.Supplier<Item> supplier, int value) {
        @Nullable
        private Item item() {
            try {
                return supplier.get();
            } catch (Exception ignored) {
                return null;
            }
        }
    }
}
