package com.raishxn.gtna.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.network.chat.Component;

import com.raishxn.gtna.api.machine.multiblock.IGTNAModuleHost;
import com.raishxn.gtna.utils.NumberUtils;

import java.util.List;

import javax.annotation.Nullable;

/**
 * GTOCore {@code luv_kuangbiao_one_giant_nuclear_fusion_reactor} ("Advanced Fusion Reactor MK-I"), the GTNA
 * replacement for GTNH's Fusion MK4/MK5. The extensions are registered in {@code GTNASubPatterns}. It runs every GTCEu
 * fusion recipe in parallel/threaded form, starting at
 * LuV; each of the four extension structures formed in order raises the tier by one (up to UEV), and the fifth
 * extension (cross-recipe) holds the Thread and Overclock hatches.
 *
 * <p>
 * As in GTO (and GTCEu's fusion), an internal buffer of {@code energyInputs × 2^(tier − LuV) × 10⁷} EU is filled
 * from the energy hatches; a recipe starts only if its {@code eu_to_start} fits the buffer, and the missing heat
 * is paid from it. Heat cools by 10,000 per tick while idle.
 */
public class AdvancedFusionReactorMachine extends WorkableElectricMultipleRecipesMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            AdvancedFusionReactorMachine.class, WorkableElectricMultipleRecipesMachine.MANAGED_FIELD_HOLDER);

    /** The first four registered extensions (kuangbiao2..5) are the tier ones; the fifth is cross-recipe. */
    private static final int TIER_EXTENSIONS = 4;

    @Persisted
    @DescSynced
    private long heat;
    @Persisted
    private final NotifiableEnergyContainer buffer;
    @Nullable
    private com.gregtechceu.gtceu.api.machine.TickableSubscription heatSubscription;

    public AdvancedFusionReactorMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
        this.buffer = new NotifiableEnergyContainer(this, 0, 0, 0, 0, 0);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    /** Extensions formed in order from the first; a gap stops the count, as in GTO. */
    public int getBonusTier() {
        long mask = ((IGTNAModuleHost) (Object) this).gtna$formedModuleMask();
        int bonus = 0;
        while (bonus < TIER_EXTENSIONS && (mask & (1L << bonus)) != 0) bonus++;
        return bonus;
    }

    @Override
    public int getTier() {
        return GTValues.LuV + getBonusTier();
    }

    public static long bufferCapacity(int tier, int energyInputs) {
        return energyInputs * (1L << (tier - GTValues.LuV)) * 10_000_000L;
    }

    public long getHeat() {
        return heat;
    }

    public long getBufferCapacity() {
        return buffer.getEnergyCapacity();
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        int inputs = 0;
        for (var handler : getCapabilitiesFlat(IO.IN,
                com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability.CAP)) {
            if (handler instanceof IEnergyContainer) inputs++;
        }
        buffer.resetBasicInfo(bufferCapacity(getTier(), inputs), 0, 0, 0, 0);
        if (buffer.getEnergyStored() > buffer.getEnergyCapacity()) buffer.setEnergyStored(buffer.getEnergyCapacity());
        if (!isRemote() && heatSubscription == null) heatSubscription = subscribeServerTick(this::updateHeat);
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        heat = 0;
        buffer.resetBasicInfo(0, 0, 0, 0, 0);
        buffer.setEnergyStored(0);
        if (heatSubscription != null) {
            heatSubscription.unsubscribe();
            heatSubscription = null;
        }
    }

    private void updateHeat() {
        var logic = getRecipeLogic();
        if (heat > 0 &&
                (logic.isIdle() || !logic.isWorkingEnabled() || (logic.isWaiting() && logic.getProgress() == 0))) {
            heat = Math.max(0, heat - 10_000);
        }
        long room = buffer.getEnergyCapacity() - buffer.getEnergyStored();
        if (isFormed() && room > 0 && energyContainer != null && energyContainer.getEnergyStored() > 0) {
            buffer.addEnergy(energyContainer.removeEnergy(Math.min(room, energyContainer.getEnergyStored())));
        }
    }

    @Override
    public RecipeModifier getRecipeModifier() {
        RecipeModifier parallel = super.getRecipeModifier();
        return (machine, recipe) -> {
            if (!payStartHeat(recipe)) return ModifierFunction.NULL;
            return parallel.getModifier(machine, recipe);
        };
    }

    /** GTCEu FusionReactorMachine heat rule against the GTO buffer. */
    private boolean payStartHeat(GTRecipe recipe) {
        long start = recipe.data.getLong("eu_to_start");
        if (start > buffer.getEnergyCapacity()) return false;
        long missing = start - heat;
        if (missing <= 0) return true;
        if (buffer.getEnergyStored() < missing) return false;
        buffer.removeEnergy(missing);
        heat += missing;
        return true;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (!isFormed()) return;
        textList.add(Component.translatable("gtna.machine.advanced_fusion_reactor.tier",
                GTValues.VNF[getTier()], getBonusTier()));
        textList.add(Component.translatable("gtceu.multiblock.fusion_reactor.energy",
                NumberUtils.formatLong(buffer.getEnergyStored()), NumberUtils.formatLong(buffer.getEnergyCapacity())));
        textList.add(Component.translatable("gtceu.multiblock.fusion_reactor.heat", NumberUtils.formatLong(heat)));
    }
}
