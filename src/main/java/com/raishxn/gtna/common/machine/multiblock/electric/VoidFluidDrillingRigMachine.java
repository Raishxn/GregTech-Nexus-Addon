package com.raishxn.gtna.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IParallelHatch;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

import com.raishxn.gtna.common.data.GTNAItems;
import com.raishxn.gtna.common.item.DepositRecorderBehavior;
import com.raishxn.gtna.config.VoidFluidDrillConfig;

import java.util.List;

/** Discovered-fluid production; gates are checked at search, start and throughout each cycle. */
public final class VoidFluidDrillingRigMachine extends WorkableElectricMultiblockMachine {

    public VoidFluidDrillingRigMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    protected com.gregtechceu.gtceu.api.machine.trait.RecipeLogic createRecipeLogic(Object... args) {
        return new FluidProgramLogic(this);
    }

    private static final class FluidProgramLogic extends com.gregtechceu.gtceu.api.machine.trait.RecipeLogic {

        private final VoidFluidDrillingRigMachine rig;

        private FluidProgramLogic(VoidFluidDrillingRigMachine rig) {
            super(rig);
            this.rig = rig;
        }

        @Override
        public void handleRecipeWorking() {
            if (!rig.isProgramAllowed(lastRecipe)) {
                setWaiting(Component.translatable("gtna.fluid.rig.gated"));
                return;
            }
            super.handleRecipeWorking();
        }

        @Override
        public void onRecipeFinish() {
            if (!rig.isProgramAllowed(lastRecipe)) {
                setWaiting(Component.translatable("gtna.fluid.rig.gated"));
                return;
            }
            var result = com.gregtechceu.gtceu.api.recipe.RecipeHelper.handleRecipe(rig, lastRecipe, IO.OUT,
                    lastRecipe.outputs, chanceCaches, false, true);
            if (!result.isSuccess()) {
                setWaiting(result.reason());
                return;
            }
            super.onRecipeFinish();
        }
    }

    public int operatingTier() {
        return Math.min(GTValues.MAX, GTUtil.getFloorTierByVoltage(getOverclockVoltage()));
    }

    public int parallelCap() {
        return VoidFluidDrillConfig.get().maxParallelByTier.getOrDefault(GTValues.VN[operatingTier()], 1);
    }

    private boolean hasUpgrade(String upgrade) {
        var entry = GTNAItems.FLUID_REMOTE_UPGRADES.get(upgrade);
        if (entry == null) return false;
        return getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP).stream()
                .flatMap(handler -> handler.getContents().stream())
                .anyMatch(content -> content instanceof ItemStack stack && stack.is(entry.get()));
    }

    public boolean isProgramAllowed(GTRecipe recipe) {
        if (recipe == null || !(getLevel() instanceof ServerLevel level)) return false;
        var config = VoidFluidDrillConfig.get();
        var p = config.program(recipe.data.getString("gtna_fluid_program"));
        if (!config.enabled || p == null || operatingTier() < Math.max(config.minimumTier, p.minimumTier)) return false;
        var owner = getOwner();
        if (owner == null) return false;
        boolean card = getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP).stream()
                .flatMap(handler -> handler.getContents().stream())
                .anyMatch(content -> content instanceof ItemStack stack && DepositRecorderBehavior.validCard(level,
                        stack, DepositRecorderBehavior.scope(owner, getOwnerUUID()), p.dimension, p.fluid));
        if (!card) return false;
        if (p.dimension.equals(level.dimension().location().toString())) return true;
        var upgrade = config.upgrades.get(p.remoteUpgrade);
        return upgrade != null && operatingTier() >= upgrade.tier && hasUpgrade(p.remoteUpgrade);
    }

    public static ModifierFunction recipeModifier(MetaMachine machine, GTRecipe recipe) {
        if (!(machine instanceof VoidFluidDrillingRigMachine rig) || !rig.isProgramAllowed(recipe))
            return ModifierFunction.NULL;
        int hatch = rig.getParallelHatch().map(IParallelHatch::getCurrentParallel).orElse(1);
        // Reserve the energy budget for parallel first; the subsequent OC uses the remaining budget.
        int energyLimit = (int) Math.min(1024, rig.getMaxVoltage() / Math.max(1, recipe.getInputEUt().getTotalEU()));
        int limit = Math.min(energyLimit, Math.min(rig.parallelCap(), hatch));
        if (limit < 1) return ModifierFunction.NULL;
        int parallel = ParallelLogic.getParallelAmount(rig, recipe, limit);
        if (parallel < 1) return ModifierFunction.NULL;
        if (parallel == 1) return ModifierFunction.IDENTITY;
        return ModifierFunction.builder().modifyAllContents(ContentModifier.multiplier(parallel))
                .eutMultiplier(parallel).parallels(parallel).build();
    }

    @Override
    public boolean beforeWorking(GTRecipe recipe) {
        return isProgramAllowed(recipe) && super.beforeWorking(recipe);
    }

    @Override
    public boolean onWorking() {
        return isProgramAllowed(recipeLogic.getLastRecipe()) && super.onWorking();
    }

    @Override
    public boolean canVoidRecipeOutputs(RecipeCapability<?> capability) {
        return false;
    }

    @Override
    public void addDisplayText(List<Component> lines) {
        MultiblockDisplayText.builder(lines, isFormed())
                .setWorkingStatus(recipeLogic.isWorkingEnabled(), recipeLogic.isActive())
                .addWorkingStatusLine().addProgressLine(recipeLogic)
                .addCustom(text -> {
                    text.add(Component.translatable("gtna.fluid.rig.parallel", parallelCap()));
                    GTRecipe recipe = recipeLogic.getLastRecipe();
                    if (recipe == null) {
                        text.add(Component.translatable("gtna.fluid.rig.no_program"));
                    } else {
                        text.add(Component.translatable("gtna.fluid.rig.program",
                                recipe.data.getString("gtna_fluid_program")));
                        var p = VoidFluidDrillConfig.get().program(recipe.data.getString("gtna_fluid_program"));
                        if (p != null) text.add(Component.translatable("gtna.fluid.data.origin", p.dimension));
                        for (var content : recipe.getInputContents(
                                com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability.CAP)) {
                            var ingredient = com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability.CAP
                                    .of(content.content);
                            if (ingredient.getStacks().length > 0)
                                text.add(Component.translatable("gtna.fluid.rig.reagent",
                                        ingredient.getStacks()[0].getFluid().getFluidType().getDescription(),
                                        ingredient.getAmount()));
                        }
                        for (var content : recipe.getOutputContents(
                                com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability.CAP)) {
                            var ingredient = com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability.CAP
                                    .of(content.content);
                            text.add(Component.translatable("gtna.fluid.rig.rate",
                                    ingredient.getAmount() * 20.0 / recipe.duration));
                        }
                        text.add(Component.translatable("gtna.fluid.rig.cost", recipe.getInputEUt().getTotalEU(),
                                recipe.duration / 20.0, recipe.parallels));
                    }
                }).addOutputLines(recipeLogic.getLastRecipe());
    }
}
