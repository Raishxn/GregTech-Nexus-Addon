package com.raishxn.gtna.common.machine.multiblock.part.ae;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.AETextInputButtonWidget;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.slot.AEPatternViewSlotWidget;
import com.gregtechceu.gtceu.integration.ae2.machine.MEBusPartMachine;
import com.gregtechceu.gtceu.utils.ItemStackHashStrategy;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.ITagSerializable;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.pattern.EncodedPatternItem;
import appeng.crafting.pattern.ProcessingPatternItem;
import appeng.helpers.patternprovider.PatternContainer;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenCustomHashMap;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GTNACraftPatternPartMachine extends MEBusPartMachine implements ICraftingProvider, PatternContainer {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            GTNACraftPatternPartMachine.class, MEBusPartMachine.MANAGED_FIELD_HOLDER);

    @Getter
    private final int maxPatternCount;

    @Persisted
    @DescSynced
    @Setter
    private String customName = "";

    @Getter
    @Persisted
    private final CustomItemStackHandler patternInventory;

    @Getter
    private final InternalSlot[] internalInventory;

    private final BiMap<IPatternDetails, InternalSlot> detailsSlotMap;

    private Runnable onContentsChanged = () -> {};

    private final InternalInventory internalPatternInventory = new InternalInventory() {

        @Override
        public int size() {
            return maxPatternCount;
        }

        @Override
        public ItemStack getStackInSlot(int slotIndex) {
            return patternInventory.getStackInSlot(slotIndex);
        }

        @Override
        public void setItemDirect(int slotIndex, ItemStack stack) {
            patternInventory.setStackInSlot(slotIndex, stack);
            patternInventory.onContentsChanged(slotIndex);
            onPatternChange(slotIndex);
        }
    };

    public GTNACraftPatternPartMachine(IMachineBlockEntity holder, int maxPatternCount, Object... args) {
        super(holder, IO.IN, args);
        this.maxPatternCount = Math.max(1, maxPatternCount);
        this.patternInventory = new CustomItemStackHandler(this.maxPatternCount);
        this.patternInventory.setFilter(stack -> stack.getItem() instanceof EncodedPatternItem &&
                !(stack.getItem() instanceof ProcessingPatternItem));
        this.internalInventory = new InternalSlot[this.maxPatternCount];
        this.detailsSlotMap = HashBiMap.create(this.maxPatternCount);
        for (int i = 0; i < this.maxPatternCount; i++) {
            this.internalInventory[i] = new InternalSlot();
        }
        getMainNode().addService(ICraftingProvider.class, this);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(1, this::rebuildPatternMap));
        }
    }

    @Override
    public void addedToController(IMultiController controller) {
        super.addedToController(controller);
        notifyController();
    }

    @Override
    public void removedFromController(IMultiController controller) {
        super.removedFromController(controller);
        notifyController();
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        super.onMainNodeStateChanged(reason);
        notifyController();
    }

    public void setOnContentsChanged(Runnable onContentsChanged) {
        this.onContentsChanged = onContentsChanged == null ? () -> {} : onContentsChanged;
    }

    private void rebuildPatternMap() {
        detailsSlotMap.clear();
        for (int i = 0; i < patternInventory.getSlots(); i++) {
            ItemStack pattern = patternInventory.getStackInSlot(i);
            IPatternDetails details = PatternDetailsHelper.decodePattern(pattern, getLevel());
            if (details != null) {
                detailsSlotMap.forcePut(details, internalInventory[i]);
            }
        }
        notifyController();
    }

    private void onPatternChange(int index) {
        if (isRemote()) {
            return;
        }
        InternalSlot slot = internalInventory[index];
        ItemStack newPattern = patternInventory.getStackInSlot(index);
        IPatternDetails newPatternDetails = PatternDetailsHelper.decodePattern(newPattern, getLevel());
        IPatternDetails oldPatternDetails = detailsSlotMap.inverse().get(slot);
        if (oldPatternDetails != null && !oldPatternDetails.equals(newPatternDetails)) {
            slot.clear();
        }
        if (newPatternDetails == null) {
            detailsSlotMap.inverse().remove(slot);
        } else {
            detailsSlotMap.forcePut(newPatternDetails, slot);
        }
        notifyController();
    }

    private void notifyController() {
        onContentsChanged.run();
    }

    public int getLoadedPatternCount() {
        int count = 0;
        for (IPatternDetails details : detailsSlotMap.keySet()) {
            if (details != null) {
                count++;
            }
        }
        return count;
    }

    public long getPendingItemCount() {
        long total = 0L;
        for (InternalSlot slot : internalInventory) {
            total += slot.getPendingItemCount();
        }
        return total;
    }

    public void drainPendingOutputs(Object2LongOpenCustomHashMap<ItemStack> target) {
        for (InternalSlot slot : internalInventory) {
            slot.drainTo(target);
        }
        notifyController();
    }

    @Override
    public WidgetGroup createUIWidget() {
        int rows = Math.max(1, (int) Math.ceil(maxPatternCount / 9.0));
        int columns = Math.min(9, maxPatternCount);
        int gridWidth = 18 * columns + 16;
        int gridHeight = 18 * rows + 16;
        WidgetGroup group = new WidgetGroup(0, 0, gridWidth, gridHeight + 18);
        int index = 0;
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < 9 && index < maxPatternCount; x++) {
                PatternSlotWidget slotWidget = new PatternSlotWidget(patternInventory, index, 8 + x * 18, 14 + y * 18);
                int slotIndex = index++;
                slotWidget.setOccupiedTexture(GuiTextures.SLOT);
                slotWidget.setItemHook(stack -> {
                    if (!stack.isEmpty() && stack.getItem() instanceof EncodedPatternItem encodedPatternItem) {
                        ItemStack output = encodedPatternItem.getOutput(stack);
                        if (!output.isEmpty()) {
                            return output;
                        }
                    }
                    return stack;
                });
                slotWidget.setChangeListener(() -> onPatternChange(slotIndex));
                slotWidget.setBackground(GuiTextures.SLOT, GuiTextures.PATTERN_OVERLAY);
                group.addWidget(slotWidget);
            }
        }
        group.addWidget(new LabelWidget(
                8,
                2,
                () -> this.isOnline ? "gtceu.gui.me_network.online" : "gtceu.gui.me_network.offline"));
        group.addWidget(new AETextInputButtonWidget(Math.max(8, gridWidth - 70), 2, 70, 10)
                .setText(customName)
                .setOnConfirm(this::setCustomName)
                .setButtonTooltips(Component.translatable("gui.gtceu.rename.desc")));
        return group;
    }

    @Override
    public java.util.List<IPatternDetails> getAvailablePatterns() {
        return detailsSlotMap.keySet().stream().filter(Objects::nonNull).toList();
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        if (!isFormed() || !getMainNode().isActive() || !detailsSlotMap.containsKey(patternDetails) ||
                !checkInput(inputHolder)) {
            return false;
        }
        InternalSlot slot = detailsSlotMap.get(patternDetails);
        if (slot != null) {
            slot.pushPattern(patternDetails);
            return true;
        }
        return false;
    }

    @Override
    public boolean isBusy() {
        return false;
    }

    private boolean checkInput(KeyCounter[] inputHolder) {
        for (KeyCounter input : inputHolder) {
            boolean illegal = input.keySet().stream()
                    .map(AEKey::getType)
                    .map(AEKeyType::getId)
                    .anyMatch(id -> !id.equals(AEKeyType.items().getId()) && !id.equals(AEKeyType.fluids().getId()));
            if (illegal) {
                return false;
            }
        }
        return true;
    }

    @Override
    public @Nullable IGrid getGrid() {
        return getMainNode().getGrid();
    }

    @Override
    public InternalInventory getTerminalPatternInventory() {
        return internalPatternInventory;
    }

    @Override
    public PatternContainerGroup getTerminalGroup() {
        return new PatternContainerGroup(
                AEItemKey.of(getDefinition().asStack()),
                Component.translatable(getBlockState().getBlock().getDescriptionId()),
                java.util.List.of(
                        Component.translatable("gtna.machine.craft_pattern_hatch.slots", maxPatternCount),
                        Component.translatable("gtna.machine.craft_pattern_hatch.patterns")));
    }

    @Override
    public boolean shouldSyncME() {
        return false;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    private final class PatternSlotWidget extends AEPatternViewSlotWidget {

        private PatternSlotWidget(CustomItemStackHandler itemHandler, int slotIndex, int xPosition, int yPosition) {
            super(itemHandler, slotIndex, xPosition, yPosition);
        }
    }

    public final class InternalSlot implements ITagSerializable<CompoundTag> {

        private final Object2LongOpenCustomHashMap<ItemStack> outputInventory = new Object2LongOpenCustomHashMap<>(
                ItemStackHashStrategy.comparingAllButCount());

        public long getPendingItemCount() {
            long total = 0L;
            for (long value : outputInventory.values()) {
                total += value;
            }
            return total;
        }

        public void clear() {
            outputInventory.clear();
            notifyController();
        }

        public void drainTo(Object2LongOpenCustomHashMap<ItemStack> target) {
            if (outputInventory.isEmpty()) {
                return;
            }
            outputInventory.object2LongEntrySet().forEach(entry -> target.addTo(entry.getKey(), entry.getLongValue()));
            outputInventory.clear();
        }

        public void pushPattern(IPatternDetails patternDetails) {
            for (GenericStack output : patternDetails.getOutputs()) {
                if (output == null || !(output.what() instanceof AEItemKey itemKey)) {
                    continue;
                }
                ItemStack stack = itemKey.toStack();
                if (!stack.isEmpty() && output.amount() > 0L) {
                    outputInventory.addTo(stack, output.amount());
                }
            }
            notifyController();
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            ListTag itemsTag = new ListTag();
            for (var entry : outputInventory.object2LongEntrySet()) {
                CompoundTag itemTag = entry.getKey().serializeNBT();
                itemTag.putLong("real", entry.getLongValue());
                itemsTag.add(itemTag);
            }
            if (!itemsTag.isEmpty()) {
                tag.put("inventory", itemsTag);
            }
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            outputInventory.clear();
            ListTag items = tag.getList("inventory", Tag.TAG_COMPOUND);
            for (Tag entry : items) {
                if (!(entry instanceof CompoundTag compoundTag)) {
                    continue;
                }
                ItemStack stack = ItemStack.of(compoundTag);
                long amount = compoundTag.getLong("real");
                if (!stack.isEmpty() && amount > 0L) {
                    outputInventory.put(stack, amount);
                }
            }
            notifyController();
        }
    }
}
