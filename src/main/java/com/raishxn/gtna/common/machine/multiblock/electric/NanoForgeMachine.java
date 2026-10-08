package com.raishxn.gtna.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

import com.raishxn.gtna.api.machine.multiblock.IGTNAModuleHost;
import com.raishxn.gtna.common.data.GTNANanoForge;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * GTNH {@code MTENanoForge}, tiers 1-3. GTNH reads the tier from the nanite in the controller slot and the built
 * pieces; GTCEu controllers have no slot, so the nanite (Carbon, Neutronium or Transcendent Metal) sits in any input
 * bus, is never consumed, and the tier is the lower of the nanite tier and the built pieces (tier 2 and tier 3
 * extensions). Recipes of a lower tier than the machine overclock perfectly, as in GTNH. Tier 4 (MagMatter) is not
 * ported.
 */
public class NanoForgeMachine extends WorkableElectricMultiblockMachine {

    public static final String TIER = "nano_forge_tier";

    public NanoForgeMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    /** 1 + the tier extensions formed in order. */
    public int getStructureTier() {
        long mask = ((IGTNAModuleHost) (Object) this).gtna$formedModuleMask();
        int tier = 1;
        while (tier < 3 && (mask & (1L << (tier - 1))) != 0) tier++;
        return tier;
    }

    public int getNaniteTier() {
        Item[] nanites = GTNANanoForge.tierNanites();
        int best = 0;
        for (IMultiPart part : getParts()) {
            if (!(part instanceof ItemBusPartMachine bus)) continue;
            var storage = bus.getInventory().storage;
            for (int slot = 0; slot < storage.getSlots(); slot++) {
                var stack = storage.getStackInSlot(slot);
                for (int t = nanites.length; t > best; t--) {
                    if (stack.is(nanites[t - 1])) best = t;
                }
            }
        }
        return best;
    }

    public int getNanoTier() {
        return Math.min(getStructureTier(), getNaniteTier());
    }

    public static @NotNull ModifierFunction nanoTier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof NanoForgeMachine forge)) return ModifierFunction.NULL;
        int required = Math.max(1, recipe.data.getInt(TIER));
        int tier = forge.getNanoTier();
        if (tier < required) {
            return ModifierFunction.cancel(Component.translatable("gtna.machine.nano_forge.low_tier", required));
        }
        return (tier > required ? GTRecipeModifiers.OC_PERFECT : GTRecipeModifiers.OC_NON_PERFECT)
                .getModifier(machine, recipe);
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (!isFormed()) return;
        textList.add(Component.translatable("gtna.machine.nano_forge.tier", getNanoTier(), getStructureTier(),
                getNaniteTier()));
    }
}
