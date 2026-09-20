package com.raishxn.gtna.common.machine.multiblock.part.ae;

import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;

import com.lowdragmc.lowdraglib.misc.FluidStorage;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.syncdata.IContentChangeAware;
import com.lowdragmc.lowdraglib.syncdata.ITagSerializable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GTNAPatternBufferSlotConfig implements ITagSerializable<CompoundTag>, IContentChangeAware {

    private static final int GHOST_GRID_SIZE = 9;
    private static final int CATALYST_GRID_SIZE = 9;
    private static final String SPECIAL_ITEMS_TAG = "specialItems";
    private static final String SPECIAL_FLUIDS_TAG = "specialFluids";
    private static final String CATALYST_ITEMS_TAG = "catalystItems";
    private static final String CATALYST_FLUIDS_TAG = "catalystFluids";

    private Runnable onContentsChanged = () -> {};

    private final ItemStackTransfer specialItems = new ItemStackTransfer(GHOST_GRID_SIZE);
    private final FluidStorage[] specialFluids = new FluidStorage[GHOST_GRID_SIZE];
    private final ItemStackTransfer catalystItems = new ItemStackTransfer(CATALYST_GRID_SIZE);
    private final FluidStorage[] catalystFluids = new FluidStorage[CATALYST_GRID_SIZE];

    private int circuitConfig = -1;
    private String preferredModeId = "";
    private String derivedModeId = "";
    private String cachedRecipeId = "";
    /**
     * GTLCore {@code cacheRecipe[]} parity: when enabled the slot's resolved recipe is remembered
     * and reused for routing; when disabled the recipe is always resolved fresh (slower, but never
     * stale). Defaults to {@code true} like the reference.
     */
    private boolean cacheRecipe = true;

    public GTNAPatternBufferSlotConfig() {
        specialItems.setOnContentsChanged(this::onContentsChanged);
        for (int i = 0; i < GHOST_GRID_SIZE; i++) {
            specialFluids[i] = new FluidStorage(Integer.MAX_VALUE);
            specialFluids[i].setOnContentsChanged(this::onContentsChanged);
        }
        catalystItems.setOnContentsChanged(this::onContentsChanged);
        for (int i = 0; i < CATALYST_GRID_SIZE; i++) {
            catalystFluids[i] = new FluidStorage(Integer.MAX_VALUE);
            catalystFluids[i].setOnContentsChanged(this::onContentsChanged);
        }
    }

    public Runnable getOnContentsChanged() {
        return onContentsChanged;
    }

    public void setOnContentsChanged(Runnable onContentsChanged) {
        if (onContentsChanged == null) {
            return;
        }
        Runnable previous = this.onContentsChanged;
        this.onContentsChanged = () -> {
            previous.run();
            onContentsChanged.run();
        };
    }

    public void onContentsChanged() {
        onContentsChanged.run();
    }

    public ItemStackTransfer getSpecialItems() {
        return specialItems;
    }

    public FluidStorage[] getSpecialFluids() {
        return specialFluids;
    }

    public int getCircuitConfig() {
        return circuitConfig;
    }

    public void setCircuitConfig(int circuitConfig) {
        this.circuitConfig = circuitConfig < 0 ? -1 : circuitConfig;
        onContentsChanged();
    }

    public String getPreferredModeId() {
        return preferredModeId;
    }

    public void setPreferredModeId(String preferredModeId) {
        this.preferredModeId = preferredModeId == null ? "" : preferredModeId.trim();
        onContentsChanged();
    }

    public String getCachedRecipeId() {
        return cachedRecipeId;
    }

    public void setCachedRecipeId(String cachedRecipeId) {
        this.cachedRecipeId = cachedRecipeId == null ? "" : cachedRecipeId.trim();
    }

    public boolean isCacheRecipe() {
        return cacheRecipe;
    }

    public void setCacheRecipe(boolean cacheRecipe) {
        if (this.cacheRecipe != cacheRecipe) {
            this.cacheRecipe = cacheRecipe;
            if (!cacheRecipe) {
                this.cachedRecipeId = "";
                this.derivedModeId = "";
            }
            onContentsChanged();
        }
    }

    public String getDerivedModeId() {
        return derivedModeId;
    }

    public void setDerivedModeId(String derivedModeId) {
        this.derivedModeId = derivedModeId == null ? "" : derivedModeId.trim();
    }

    public void clearRecipeCache() {
        clearRecipeCacheSilently();
    }

    public void clearRecipeCacheSilently() {
        this.cachedRecipeId = "";
        this.derivedModeId = "";
    }

    public void clearSpecialization() {
        for (int i = 0; i < specialItems.getSlots(); i++) {
            specialItems.setStackInSlot(i, ItemStack.EMPTY);
        }
        for (FluidStorage specialFluid : specialFluids) {
            specialFluid.setFluid(FluidStack.empty());
        }
        clearCatalysts();
        this.circuitConfig = -1;
        this.preferredModeId = "";
        this.derivedModeId = "";
        this.cachedRecipeId = "";
        onContentsChanged();
    }

    /**
     * Catalyst inventories (GTLCore parity): ghost-config insumos that make the slot accept a
     * recipe which consumes them, without ever consuming the catalyst itself. Unlike
     * {@code specialItems}/{@code specialFluids} (virtual supply that matches by test), catalyst
     * entries block automatic recipe cache assignment for the recipe types they shadow — the
     * buffer will not pick a recipe whose inputs intersect the catalyst set unless the catalyst
     * key is also present in the slot's actual pushed contents.
     */
    public ItemStackTransfer getCatalystItems() {
        return catalystItems;
    }

    public FluidStorage[] getCatalystFluids() {
        return catalystFluids;
    }

    public boolean hasCatalysts() {
        for (int i = 0; i < catalystItems.getSlots(); i++) {
            if (!catalystItems.getStackInSlot(i).isEmpty()) return true;
        }
        for (FluidStorage catalystFluid : catalystFluids) {
            if (!catalystFluid.getFluid().isEmpty()) return true;
        }
        return false;
    }

    public void clearCatalysts() {
        for (int i = 0; i < catalystItems.getSlots(); i++) {
            catalystItems.setStackInSlot(i, ItemStack.EMPTY);
        }
        for (FluidStorage catalystFluid : catalystFluids) {
            catalystFluid.setFluid(FluidStack.empty());
        }
        onContentsChanged();
    }

    public List<ItemStack> getCatalystItemStacks() {
        List<ItemStack> stacks = new ArrayList<>(catalystItems.getSlots());
        for (int i = 0; i < catalystItems.getSlots(); i++) {
            ItemStack stack = catalystItems.getStackInSlot(i);
            if (!stack.isEmpty()) stacks.add(stack.copy());
        }
        return stacks;
    }

    public List<net.minecraftforge.fluids.FluidStack> getCatalystFluidStacks() {
        List<net.minecraftforge.fluids.FluidStack> stacks = new ArrayList<>(catalystFluids.length);
        for (FluidStorage catalystFluid : catalystFluids) {
            FluidStack fluidStack = catalystFluid.getFluid();
            if (!fluidStack.isEmpty()) stacks.add(toForgeFluid(fluidStack));
        }
        return stacks;
    }

    public boolean hasSpecialItem() {
        for (int i = 0; i < specialItems.getSlots(); i++) {
            if (!specialItems.getStackInSlot(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasSpecialFluid() {
        for (FluidStorage specialFluid : specialFluids) {
            if (!specialFluid.getFluid().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasCircuit() {
        return circuitConfig >= 0;
    }

    public @Nullable ItemStack getCircuitStack() {
        return hasCircuit() ? IntCircuitBehaviour.stack(circuitConfig) : null;
    }

    public List<ItemStack> getVirtualItemStacks() {
        List<ItemStack> stacks = new ArrayList<>(specialItems.getSlots() + 1);
        for (int i = 0; i < specialItems.getSlots(); i++) {
            ItemStack stack = specialItems.getStackInSlot(i);
            if (!stack.isEmpty()) {
                stacks.add(stack.copy());
            }
        }
        ItemStack circuitStack = getCircuitStack();
        if (circuitStack != null && !circuitStack.isEmpty()) {
            stacks.add(circuitStack);
        }
        return stacks;
    }

    public List<net.minecraftforge.fluids.FluidStack> getVirtualFluidStacks() {
        List<net.minecraftforge.fluids.FluidStack> stacks = new ArrayList<>(specialFluids.length);
        for (FluidStorage specialFluid : specialFluids) {
            FluidStack fluidStack = specialFluid.getFluid();
            if (!fluidStack.isEmpty()) {
                stacks.add(toForgeFluid(fluidStack));
            }
        }
        return stacks;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put(SPECIAL_ITEMS_TAG, specialItems.serializeNBT());
        ListTag fluidTag = new ListTag();
        for (int i = 0; i < specialFluids.length; i++) {
            CompoundTag entry = specialFluids[i].serializeNBT();
            entry.putInt("slot", i);
            fluidTag.add(entry);
        }
        tag.put(SPECIAL_FLUIDS_TAG, fluidTag);
        tag.put(CATALYST_ITEMS_TAG, catalystItems.serializeNBT());
        ListTag catalystFluidTag = new ListTag();
        for (int i = 0; i < catalystFluids.length; i++) {
            CompoundTag entry = catalystFluids[i].serializeNBT();
            entry.putInt("slot", i);
            catalystFluidTag.add(entry);
        }
        tag.put(CATALYST_FLUIDS_TAG, catalystFluidTag);
        tag.putInt("circuitConfig", circuitConfig);
        tag.putBoolean("cacheRecipe", cacheRecipe);
        if (!preferredModeId.isBlank()) {
            tag.putString("preferredModeId", preferredModeId);
        }
        if (!derivedModeId.isBlank()) {
            tag.putString("derivedModeId", derivedModeId);
        }
        if (!cachedRecipeId.isBlank()) {
            tag.putString("cachedRecipeId", cachedRecipeId);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains(SPECIAL_ITEMS_TAG, Tag.TAG_COMPOUND)) {
            specialItems.deserializeNBT(tag.getCompound(SPECIAL_ITEMS_TAG));
        } else {
            specialItems.deserializeNBT(new CompoundTag());
            migrateLegacySingleItem(tag);
        }

        for (FluidStorage specialFluid : specialFluids) {
            specialFluid.deserializeNBT(new CompoundTag());
        }
        if (tag.contains(SPECIAL_FLUIDS_TAG, Tag.TAG_LIST)) {
            ListTag fluidTag = tag.getList(SPECIAL_FLUIDS_TAG, Tag.TAG_COMPOUND);
            for (Tag entry : fluidTag) {
                if (entry instanceof CompoundTag compoundTag) {
                    int slot = compoundTag.getInt("slot");
                    if (slot >= 0 && slot < specialFluids.length) {
                        specialFluids[slot].deserializeNBT(compoundTag);
                    }
                }
            }
        } else {
            migrateLegacySingleFluid(tag);
        }

        catalystItems.deserializeNBT(
                tag.contains(CATALYST_ITEMS_TAG, Tag.TAG_COMPOUND) ? tag.getCompound(CATALYST_ITEMS_TAG) :
                        new CompoundTag());
        for (FluidStorage catalystFluid : catalystFluids) {
            catalystFluid.deserializeNBT(new CompoundTag());
        }
        if (tag.contains(CATALYST_FLUIDS_TAG, Tag.TAG_LIST)) {
            ListTag catalystFluidTag = tag.getList(CATALYST_FLUIDS_TAG, Tag.TAG_COMPOUND);
            for (Tag entry : catalystFluidTag) {
                if (entry instanceof CompoundTag compoundTag) {
                    int slot = compoundTag.getInt("slot");
                    if (slot >= 0 && slot < catalystFluids.length) {
                        catalystFluids[slot].deserializeNBT(compoundTag);
                    }
                }
            }
        }

        this.circuitConfig = tag.contains("circuitConfig") ? tag.getInt("circuitConfig") : -1;
        this.cacheRecipe = !tag.contains("cacheRecipe") || tag.getBoolean("cacheRecipe");
        this.preferredModeId = tag.getString("preferredModeId");
        this.derivedModeId = tag.getString("derivedModeId");
        this.cachedRecipeId = tag.getString("cachedRecipeId");
    }

    private void migrateLegacySingleItem(CompoundTag tag) {
        String specialItemId = tag.getString("specialItemId");
        if (specialItemId.isBlank()) {
            return;
        }
        ResourceLocation id = ResourceLocation.tryParse(specialItemId);
        if (id == null) {
            return;
        }
        Item item = ForgeRegistries.ITEMS.getValue(id);
        if (item == null) {
            return;
        }
        int count = Math.max(1, tag.getInt("specialItemCount"));
        specialItems.setStackInSlot(0, new ItemStack(item, count));
    }

    private void migrateLegacySingleFluid(CompoundTag tag) {
        String specialFluidId = tag.getString("specialFluidId");
        if (specialFluidId.isBlank()) {
            return;
        }
        ResourceLocation id = ResourceLocation.tryParse(specialFluidId);
        if (id == null) {
            return;
        }
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(id);
        if (fluid == null) {
            return;
        }
        int amount = Math.max(1, tag.getInt("specialFluidAmount"));
        specialFluids[0].setFluid(FluidStack.create(fluid, amount));
    }

    private static net.minecraftforge.fluids.FluidStack toForgeFluid(FluidStack stack) {
        return new net.minecraftforge.fluids.FluidStack(stack.getFluid(), (int) Math.min(Integer.MAX_VALUE,
                stack.getAmount()), stack.hasTag() ? stack.getTag().copy() : null);
    }
}
