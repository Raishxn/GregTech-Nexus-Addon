package com.raishxn.gtna.common.machine.multiblock.part.ae;

import com.lowdragmc.lowdraglib.syncdata.IContentChangeAware;
import com.lowdragmc.lowdraglib.syncdata.ITagSerializable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;

import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import org.jetbrains.annotations.Nullable;

public class GTNAPatternBufferSlotConfig implements ITagSerializable<CompoundTag>, IContentChangeAware {

    private Runnable onContentsChanged = () -> {};

    public Runnable getOnContentsChanged() {
        return onContentsChanged;
    }

    private String specialItemId = "";
    private int specialItemCount = 1;
    private String specialFluidId = "";
    private int specialFluidAmount = 1000;
    private int circuitConfig = -1;
    private String preferredModeId = "";
    private String cachedRecipeId = "";

    public void setOnContentsChanged(Runnable onContentsChanged) {
        this.onContentsChanged = onContentsChanged == null ? () -> {} : onContentsChanged;
    }

    public void onContentsChanged() {
        onContentsChanged.run();
    }

    public String getSpecialItemId() {
        return specialItemId;
    }

    public void setSpecialItemId(String specialItemId) {
        this.specialItemId = specialItemId == null ? "" : specialItemId.trim();
        onContentsChanged();
    }

    public int getSpecialItemCount() {
        return specialItemCount;
    }

    public void setSpecialItemCount(int specialItemCount) {
        this.specialItemCount = Math.max(1, specialItemCount);
        onContentsChanged();
    }

    public String getSpecialFluidId() {
        return specialFluidId;
    }

    public void setSpecialFluidId(String specialFluidId) {
        this.specialFluidId = specialFluidId == null ? "" : specialFluidId.trim();
        onContentsChanged();
    }

    public int getSpecialFluidAmount() {
        return specialFluidAmount;
    }

    public void setSpecialFluidAmount(int specialFluidAmount) {
        this.specialFluidAmount = Math.max(1, specialFluidAmount);
        onContentsChanged();
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
        onContentsChanged();
    }

    public void clearSpecialization() {
        this.specialItemId = "";
        this.specialItemCount = 1;
        this.specialFluidId = "";
        this.specialFluidAmount = 1000;
        this.circuitConfig = -1;
        this.preferredModeId = "";
        this.cachedRecipeId = "";
        onContentsChanged();
    }

    public boolean hasSpecialItem() {
        return !specialItemId.isBlank() && getSpecialItem().isPresent();
    }

    public boolean hasSpecialFluid() {
        return !specialFluidId.isBlank() && getSpecialFluidStack().isPresent();
    }

    public boolean hasCircuit() {
        return circuitConfig >= 0;
    }

    public LazyOptional<ItemStack> getSpecialItem() {
        if (specialItemId.isBlank()) {
            return LazyOptional.empty();
        }
        ResourceLocation id = ResourceLocation.tryParse(specialItemId);
        if (id == null) {
            return LazyOptional.empty();
        }
        Item item = ForgeRegistries.ITEMS.getValue(id);
        if (item == null) {
            return LazyOptional.empty();
        }
        return LazyOptional.of(() -> new ItemStack(item, specialItemCount));
    }

    public LazyOptional<net.minecraftforge.fluids.FluidStack> getSpecialFluidStack() {
        if (specialFluidId.isBlank()) {
            return LazyOptional.empty();
        }
        ResourceLocation id = ResourceLocation.tryParse(specialFluidId);
        if (id == null) {
            return LazyOptional.empty();
        }
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(id);
        if (fluid == null) {
            return LazyOptional.empty();
        }
        return LazyOptional.of(() -> new net.minecraftforge.fluids.FluidStack(fluid, specialFluidAmount));
    }

    public @Nullable ItemStack getCircuitStack() {
        return hasCircuit() ? IntCircuitBehaviour.stack(circuitConfig) : null;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        if (!specialItemId.isBlank()) {
            tag.putString("specialItemId", specialItemId);
        }
        tag.putInt("specialItemCount", specialItemCount);
        if (!specialFluidId.isBlank()) {
            tag.putString("specialFluidId", specialFluidId);
        }
        tag.putInt("specialFluidAmount", specialFluidAmount);
        tag.putInt("circuitConfig", circuitConfig);
        if (!preferredModeId.isBlank()) {
            tag.putString("preferredModeId", preferredModeId);
        }
        if (!cachedRecipeId.isBlank()) {
            tag.putString("cachedRecipeId", cachedRecipeId);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        this.specialItemId = tag.getString("specialItemId");
        this.specialItemCount = Math.max(1, tag.getInt("specialItemCount"));
        this.specialFluidId = tag.getString("specialFluidId");
        this.specialFluidAmount = Math.max(1, tag.getInt("specialFluidAmount"));
        this.circuitConfig = tag.contains("circuitConfig") ? tag.getInt("circuitConfig") : -1;
        this.preferredModeId = tag.getString("preferredModeId");
        this.cachedRecipeId = tag.getString("cachedRecipeId");
    }
}
