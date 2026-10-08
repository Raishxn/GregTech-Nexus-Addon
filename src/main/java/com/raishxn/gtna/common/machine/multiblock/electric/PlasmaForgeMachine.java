package com.raishxn.gtna.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.CoilWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;

import com.raishxn.gtna.common.data.material.GodforgeChainMaterials;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * GTNH {@code MTEPlasmaForge} (Dimensionally Transcendent Plasma Forge). Heat from the coils like an EBF (GTCEu's
 * {@code ebfOverclock}) and the catalyst discount: every tick of recipe time adds to the running time; after 8 hours
 * the Excited DT catalysts cost up to 50% less, and while no recipe runs the time drains 100 ticks per tick.
 * GTNH's Convergence (perfect overclocks with the Transdimensional Alignment Matrix) is not ported yet.
 */
public class PlasmaForgeMachine extends CoilWorkableElectricMultiblockMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            PlasmaForgeMachine.class, CoilWorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    /** 3600 s × 8 h × 20 ticks. */
    public static final double MAX_EFFICIENCY_TICKS = 3600d * 8d * 20d;
    public static final double MAXIMUM_DISCOUNT = 0.5d;
    public static final long DECAY_PER_TICK = 100;

    @Persisted
    @DescSynced
    private long runningTime;

    public PlasmaForgeMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) subscribeServerTick(this::decay);
    }

    private void decay() {
        if (!getRecipeLogic().isWorking() && runningTime > 0) runningTime = Math.max(0, runningTime - DECAY_PER_TICK);
    }

    @Override
    public boolean beforeWorking(GTRecipe recipe) {
        if (!super.beforeWorking(recipe)) return false;
        if (recipe != null) runningTime = Math.min((long) MAX_EFFICIENCY_TICKS * 2, runningTime + recipe.duration);
        return true;
    }

    public long getRunningTime() {
        return runningTime;
    }

    public void setRunningTime(long runningTime) {
        this.runningTime = Math.max(0, runningTime);
    }

    /** 1 = no discount, 0.5 = maximum. */
    public double getDiscount() {
        return discount(runningTime);
    }

    public static double discount(long runningTime) {
        double progress = Math.min(runningTime / MAX_EFFICIENCY_TICKS, 1.0d);
        return Math.max(MAXIMUM_DISCOUNT, 1 - progress * 0.5);
    }

    private static Set<Fluid> catalysts() {
        return Set.of(GodforgeChainMaterials.ExcitedDTCC.getFluid(), GodforgeChainMaterials.ExcitedDTPC.getFluid(),
                GodforgeChainMaterials.ExcitedDTRC.getFluid(), GodforgeChainMaterials.ExcitedDTEC.getFluid(),
                GodforgeChainMaterials.ExcitedDTSC.getFluid());
    }

    /** Recipe modifier: scales the Excited catalyst inputs by the current discount. */
    public static @NotNull ModifierFunction catalystDiscount(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof PlasmaForgeMachine forge)) return ModifierFunction.IDENTITY;
        double discount = forge.getDiscount();
        if (discount >= 1) return ModifierFunction.IDENTITY;
        var fuels = catalysts();
        return input -> {
            var copy = input.copy();
            var contents = copy.inputs.get(FluidRecipeCapability.CAP);
            if (contents == null) return copy;
            List<Content> scaled = new ArrayList<>(contents.size());
            for (Content content : contents) {
                var ingredient = FluidRecipeCapability.CAP.of(content.content);
                var stacks = ingredient.getStacks();
                if (stacks.length > 0 && fuels.contains(stacks[0].getFluid())) {
                    var reduced = ingredient.copy();
                    reduced.setAmount(Math.max(1, (int) Math.round(ingredient.getAmount() * discount)));
                    scaled.add(new Content(reduced, content.chance, content.maxChance, content.tierChanceBoost));
                } else {
                    scaled.add(content);
                }
            }
            copy.inputs.put(FluidRecipeCapability.CAP, scaled);
            return copy;
        };
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (!isFormed()) return;
        textList.add(Component.translatable("gtna.machine.plasma_forge.discount",
                String.format("%.1f", 100 * (1 - getDiscount())),
                String.format("%.1f", Math.min(runningTime, MAX_EFFICIENCY_TICKS) / 72_000d)));
    }
}
