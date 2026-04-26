package com.raishxn.gtna.common.machine.multiblock.energy;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import com.raishxn.gtna.common.block.MEStorageCoreBlock;
import com.raishxn.gtna.common.data.GTNAItems;
import com.raishxn.gtna.common.machine.multiblock.part.ae.GTNAMEStorageAccessPartMachine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MEStorageMachine extends WorkableMultiblockMachine implements IDisplayUIMachine, IFancyUIMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            MEStorageMachine.class, WorkableMultiblockMachine.MANAGED_FIELD_HOLDER);

    private static final long INFINITE_THRESHOLD_BYTES = 1_000_000_000_000L;

    @Persisted
    private final NotifiableItemStackHandler machineStorage;

    @DescSynced
    private long capacityBytes;
    @DescSynced
    private boolean infinite;
    @DescSynced
    private long usedBytes;
    @DescSynced
    private int storedTypes;
    @DescSynced
    private boolean accessOnline;
    @DescSynced
    private String accessMode = "";

    @Nullable
    private GTNAMEStorageAccessPartMachine accessPart;

    public MEStorageMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
        this.machineStorage = new NotifiableItemStackHandler(this, 1, IO.IN, IO.IN);
        this.machineStorage.setFilter(stack -> stack.is(GTNAItems.INFINITE_CELL_COMPONENT.asItem()));
        this.machineStorage.storage.setOnContentsChanged(this::onMachineStorageChanged);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            subscribeServerTick(this::updateAccessStatus);
        }
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        configureAccessHatch();
    }

    @Override
    public void onStructureInvalid() {
        clearAccessHatch();
        super.onStructureInvalid();
    }

    @Override
    public void onUnload() {
        clearAccessHatch();
        super.onUnload();
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(198, 225, this, entityPlayer)
                .widget(new FancyMachineUIWidget(this, 198, 225));
    }

    @Override
    public Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(0, 0, 190, 142);
        var screen = new DraggableScrollableWidgetGroup(4, 4, 182, 96)
                .setBackground(GuiTextures.DISPLAY);
        screen.addWidget(new LabelWidget(4, 5, self().getBlockState().getBlock().getDescriptionId()));
        screen.addWidget(new ComponentPanelWidget(4, 17, this::addDisplayText).setMaxWidthLimit(170));
        group.addWidget(screen);

        WidgetGroup slotPanel = new WidgetGroup(4, 104, 182, 34);
        slotPanel.setBackground(GuiTextures.BACKGROUND_INVERSE);
        slotPanel.addWidget(new SlotWidget(machineStorage, 0, 8, 8, true, true)
                .setBackground(GuiTextures.SLOT));
        slotPanel.addWidget(new LabelWidget(32, 13, "gtna.machine.me_storage.infinite_cell_slot"));
        group.addWidget(slotPanel);
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    @Override
    public void addDisplayText(@NotNull List<Component> textList) {
        IDisplayUIMachine.super.addDisplayText(textList);
        if (!isFormed()) {
            textList.add(Component.translatable("gtna.machine.me_storage.unformed").withStyle(ChatFormatting.GRAY));
            return;
        }

        textList.add(Component.translatable("gtna.machine.me_storage.title")
                .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        if (accessPart == null) {
            textList.add(Component.translatable("gtna.machine.me_storage.no_access").withStyle(ChatFormatting.RED));
            return;
        }

        textList.add(Component.translatable("gtna.machine.me_storage.access", accessMode,
                accessOnline ? "Online" : "Offline").withStyle(accessOnline ? ChatFormatting.GREEN : ChatFormatting.RED));
        textList.add(Component.translatable("gtna.machine.me_storage.capacity",
                infinite ? "Infinite" : formatBytes(capacityBytes)).withStyle(ChatFormatting.GRAY));
        textList.add(Component.translatable("gtna.machine.me_storage.used",
                formatBytes(usedBytes), FormattingUtil.formatNumbers(storedTypes)).withStyle(ChatFormatting.GRAY));
        ItemStack componentStack = machineStorage.getStackInSlot(0);
        textList.add(Component.translatable("gtna.machine.me_storage.infinite_status",
                componentStack.getCount(), infinite ? "Enabled" : "Disabled").withStyle(
                        infinite ? ChatFormatting.GOLD : ChatFormatting.DARK_GRAY));
    }

    private void onMachineStorageChanged() {
        configureAccessHatch();
        onChanged();
    }

    private void configureAccessHatch() {
        if (isRemote() || !isFormed()) {
            return;
        }
        GTNAMEStorageAccessPartMachine foundAccessPart = findAccessPart();
        long capacity = calculateCapacity();
        boolean hasInfiniteStack = machineStorage.getStackInSlot(0).getCount() >= 64;
        boolean infiniteMode = capacity >= INFINITE_THRESHOLD_BYTES && hasInfiniteStack;

        clearAccessHatch();
        accessPart = foundAccessPart;
        capacityBytes = capacity;
        infinite = infiniteMode;
        if (accessPart != null) {
            accessPart.configureStorage(capacityBytes, infinite);
        }
        updateAccessStatus();
    }

    private void clearAccessHatch() {
        if (accessPart != null) {
            accessPart.clearStorageMount();
        }
        accessPart = null;
        accessOnline = false;
        accessMode = "";
    }

    private void updateAccessStatus() {
        if (accessPart == null && isFormed()) {
            accessPart = findAccessPart();
            if (accessPart != null) {
                accessPart.configureStorage(capacityBytes, infinite);
            }
        }
        if (accessPart == null) {
            usedBytes = 0L;
            storedTypes = 0;
            accessOnline = false;
            return;
        }
        usedBytes = accessPart.getUsedBytes();
        storedTypes = accessPart.getStoredTypes();
        accessOnline = accessPart.getMainNode().isOnline();
        accessMode = accessPart.getMode().label().getString();
    }

    @Nullable
    private GTNAMEStorageAccessPartMachine findAccessPart() {
        for (IMultiPart part : getParts()) {
            if (part instanceof GTNAMEStorageAccessPartMachine storageAccessPart) {
                return storageAccessPart;
            }
        }
        return null;
    }

    private long calculateCapacity() {
        if (getLevel() == null || getMultiblockState().cache == null) {
            return 0L;
        }
        long capacity = 0L;
        for (BlockPos pos : getMultiblockState().getCache()) {
            Block block = getLevel().getBlockState(pos).getBlock();
            if (block instanceof MEStorageCoreBlock storageCore && !storageCore.isCraftingCore()) {
                capacity = saturatedAdd(capacity, storageCore.getCapacity());
            }
        }
        return capacity;
    }

    private static long saturatedAdd(long a, long b) {
        long result = a + b;
        return result < 0L || result < a ? Long.MAX_VALUE : result;
    }

    private static String formatBytes(long bytes) {
        String[] units = { "B", "KiB", "MiB", "GiB", "TiB", "PiB", "EiB" };
        double value = bytes;
        int unit = 0;
        while (value >= 1024.0D && unit < units.length - 1) {
            value /= 1024.0D;
            unit++;
        }
        return String.format(java.util.Locale.US, value == Math.rint(value) ? "%.0f %s" : "%.2f %s", value,
                units[unit]);
    }
}
