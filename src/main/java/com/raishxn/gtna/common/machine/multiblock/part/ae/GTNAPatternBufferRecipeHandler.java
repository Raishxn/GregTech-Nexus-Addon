package com.raishxn.gtna.common.machine.multiblock.part.ae;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IFilteredHandler;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerGroupDistinctness;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class GTNAPatternBufferRecipeHandler {

    @Getter
    private final List<RecipeHandlerList> slotHandlers;

    public GTNAPatternBufferRecipeHandler(GTNAMEPatternBufferPartMachine buffer,
                                          GTNAMEPatternBufferPartMachine.InternalSlot[] slots,
                                          GTNAPatternBufferSlotConfig[] configs) {
        this.slotHandlers = new ArrayList<>(slots.length);
        for (int i = 0; i < slots.length; i++) {
            slotHandlers.add(new SlotRHL(buffer, slots[i], configs[i], i));
        }
    }

    private static final class SlotRHL extends RecipeHandlerList {

        private SlotRHL(GTNAMEPatternBufferPartMachine buffer,
                        GTNAMEPatternBufferPartMachine.InternalSlot slot,
                        GTNAPatternBufferSlotConfig config,
                        int index) {
            super(IO.IN);
            addHandlers(buffer.getCircuitInventory(),
                    new SlotSpecialItemHandler(buffer, config, index),
                    new SlotSpecialFluidHandler(buffer, config, index),
                    new SlotItemRecipeHandler(buffer, slot, index),
                    new SlotFluidRecipeHandler(buffer, slot, index));
            setGroup(RecipeHandlerGroupDistinctness.BUS_DISTINCT);
        }

        @Override
        public boolean isDistinct() {
            return true;
        }

        @Override
        protected void setDistinct(boolean distinct, boolean notify) {}
    }

    @Getter
    private static final class SlotItemRecipeHandler extends NotifiableRecipeHandlerTrait<Ingredient> {

        private final GTNAMEPatternBufferPartMachine.InternalSlot slot;
        private final int priority;
        private final int size = 81;
        private final RecipeCapability<Ingredient> capability = ItemRecipeCapability.CAP;
        private final IO handlerIO = IO.IN;
        private final boolean isDistinct = true;

        private SlotItemRecipeHandler(GTNAMEPatternBufferPartMachine buffer,
                                      GTNAMEPatternBufferPartMachine.InternalSlot slot,
                                      int index) {
            super(buffer);
            this.slot = slot;
            this.priority = IFilteredHandler.HIGH + index + 1;
            slot.setOnContentsChanged(this::notifyListeners);
        }

        @Override
        public List<Ingredient> handleRecipeInner(IO io, GTRecipe recipe, List<Ingredient> left, boolean simulate) {
            if (io != IO.IN || slot.isItemEmpty()) return left;
            return slot.handleItemInternal(left, simulate);
        }

        @Override
        public @NotNull List<Object> getContents() {
            return new ArrayList<>(slot.getItems());
        }

        @Override
        public double getTotalContentAmount() {
            return slot.getItems().stream().mapToLong(ItemStack::getCount).sum();
        }
    }

    @Getter
    private static final class SlotFluidRecipeHandler extends NotifiableRecipeHandlerTrait<FluidIngredient> {

        private final GTNAMEPatternBufferPartMachine.InternalSlot slot;
        private final int priority;
        private final int size = 81;
        private final RecipeCapability<FluidIngredient> capability = FluidRecipeCapability.CAP;
        private final IO handlerIO = IO.IN;
        private final boolean isDistinct = true;

        private SlotFluidRecipeHandler(GTNAMEPatternBufferPartMachine buffer,
                                       GTNAMEPatternBufferPartMachine.InternalSlot slot,
                                       int index) {
            super(buffer);
            this.slot = slot;
            this.priority = IFilteredHandler.HIGH + index + 1;
            slot.setOnContentsChanged(this::notifyListeners);
        }

        @Override
        public List<FluidIngredient> handleRecipeInner(IO io, GTRecipe recipe, List<FluidIngredient> left,
                                                       boolean simulate) {
            if (io != IO.IN || slot.isFluidEmpty()) return left;
            return slot.handleFluidInternal(left, simulate);
        }

        @Override
        public @NotNull List<Object> getContents() {
            return new ArrayList<>(slot.getFluids());
        }

        @Override
        public double getTotalContentAmount() {
            return slot.getFluids().stream().mapToLong(FluidStack::getAmount).sum();
        }
    }

    @Getter
    private static final class SlotSpecialItemHandler extends NotifiableRecipeHandlerTrait<Ingredient> {

        private final GTNAPatternBufferSlotConfig config;
        private final int priority;
        private final int size = 10;
        private final RecipeCapability<Ingredient> capability = ItemRecipeCapability.CAP;
        private final IO handlerIO = IO.IN;
        private final boolean isDistinct = true;

        private SlotSpecialItemHandler(GTNAMEPatternBufferPartMachine buffer,
                                       GTNAPatternBufferSlotConfig config,
                                       int index) {
            super(buffer);
            this.config = config;
            this.priority = IFilteredHandler.HIGH + 1000 + index;
            config.setOnContentsChanged(this::notifyListeners);
        }

        @Override
        public List<Ingredient> handleRecipeInner(IO io, GTRecipe recipe, List<Ingredient> left, boolean simulate) {
            if (io != IO.IN || left == null || left.isEmpty()) {
                return left;
            }

            List<ItemStack> virtualStacks = getVirtualStacks();
            if (virtualStacks.isEmpty()) {
                return left;
            }

            for (var it = left.listIterator(); it.hasNext();) {
                Ingredient ingredient = it.next();
                if (ingredient == null || ingredient.isEmpty()) {
                    it.remove();
                    continue;
                }

                int amountLeft = extractAmount(ingredient);
                if (amountLeft <= 0) {
                    it.remove();
                    continue;
                }

                for (ItemStack stack : virtualStacks) {
                    if (stack.isEmpty() || !ingredient.test(stack)) {
                        continue;
                    }
                    amountLeft -= stack.getCount();
                    if (amountLeft <= 0) {
                        it.remove();
                        break;
                    }
                }

                if (amountLeft > 0) {
                    applyAmount(ingredient, amountLeft);
                }
            }

            return left.isEmpty() ? null : left;
        }

        @Override
        public @NotNull List<Object> getContents() {
            return new ArrayList<>(getVirtualStacks());
        }

        @Override
        public double getTotalContentAmount() {
            return getVirtualStacks().stream().mapToLong(ItemStack::getCount).sum();
        }

        private List<ItemStack> getVirtualStacks() {
            return new ArrayList<>(config.getVirtualItemStacks());
        }

        private static int extractAmount(Ingredient ingredient) {
            if (ingredient instanceof SizedIngredient sizedIngredient) {
                return sizedIngredient.getAmount();
            }
            ItemStack[] items = ingredient.getItems();
            return items.length > 0 ? items[0].getCount() : 0;
        }

        private static void applyAmount(Ingredient ingredient, int amount) {
            if (ingredient instanceof SizedIngredient sizedIngredient) {
                sizedIngredient.setAmount(amount);
                return;
            }
            ItemStack[] items = ingredient.getItems();
            if (items.length > 0) {
                items[0].setCount(amount);
            }
        }
    }

    @Getter
    private static final class SlotSpecialFluidHandler extends NotifiableRecipeHandlerTrait<FluidIngredient> {

        private final GTNAPatternBufferSlotConfig config;
        private final int priority;
        private final int size = 9;
        private final RecipeCapability<FluidIngredient> capability = FluidRecipeCapability.CAP;
        private final IO handlerIO = IO.IN;
        private final boolean isDistinct = true;

        private SlotSpecialFluidHandler(GTNAMEPatternBufferPartMachine buffer,
                                        GTNAPatternBufferSlotConfig config,
                                        int index) {
            super(buffer);
            this.config = config;
            this.priority = IFilteredHandler.HIGH + 2000 + index;
            config.setOnContentsChanged(this::notifyListeners);
        }

        @Override
        public List<FluidIngredient> handleRecipeInner(IO io, GTRecipe recipe, List<FluidIngredient> left,
                                                       boolean simulate) {
            List<FluidStack> configuredFluids = config.getVirtualFluidStacks();
            if (io != IO.IN || left == null || left.isEmpty() || configuredFluids.isEmpty()) {
                return left;
            }

            for (var it = left.listIterator(); it.hasNext();) {
                FluidIngredient ingredient = it.next();
                if (ingredient == null || ingredient.isEmpty()) {
                    it.remove();
                    continue;
                }

                int amountLeft = ingredient.getAmount();
                for (FluidStack configuredFluid : configuredFluids) {
                    if (configuredFluid.isEmpty() || !ingredient.test(configuredFluid)) {
                        continue;
                    }
                    amountLeft -= configuredFluid.getAmount();
                    if (amountLeft <= 0) {
                        break;
                    }
                }

                if (amountLeft <= 0) {
                    it.remove();
                } else {
                    ingredient.setAmount(amountLeft);
                }
            }

            return left.isEmpty() ? null : left;
        }

        @Override
        public @NotNull List<Object> getContents() {
            return new ArrayList<>(config.getVirtualFluidStacks());
        }

        @Override
        public double getTotalContentAmount() {
            return config.getVirtualFluidStacks().stream().mapToLong(FluidStack::getAmount).sum();
        }
    }
}
