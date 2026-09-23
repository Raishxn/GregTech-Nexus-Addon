package com.raishxn.gtna.common.machine.multiblock.noenergy;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import com.raishxn.gtna.api.machine.IZeroEnergyMachine;
import com.raishxn.gtna.common.machine.multiblock.electric.WorkableElectricMultipleRecipesMachine;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * GTOCore {@code thermal_power_pump} port (LGPLv3, attribution via {@code GTNASources}): a primitive
 * no-energy multiblock that condenses steam back into water at a rate set by the biome it sits in
 * (oceans/rivers are the best, the Nether produces nothing). When it is raining in the biome the
 * output is boosted by 50%.
 *
 * <p>
 * GTOCore drives this through its custom recipe API; this port keeps the same numbers
 * ({@code production = biomeModifier << 8}, {@code * 3 / 2} in rain, one 20-tick cycle) with a
 * GTNA-native server tick over the structure's own fluid hatches.
 */
@ParametersAreNonnullByDefault
public class ThermalPowerPumpMachine extends WorkableElectricMultipleRecipesMachine
                                     implements IZeroEnergyMachine {

    private static final Fluid STEAM = GTMaterials.Steam.getFluid();
    private static final int CYCLE_TICKS = 20;

    private final List<NotifiableFluidTank> fluidInputs = new ArrayList<>();
    private final List<NotifiableFluidTank> fluidOutputs = new ArrayList<>();

    @Nullable
    private TickableSubscription pumpSubs;
    private int biomeModifier;
    private int progress;

    public ThermalPowerPumpMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public boolean onWorking() {
        // Zero-energy: no energy hatch / electric working checks.
        return true;
    }

    @Override
    public int getMaxParallel() {
        return 1;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        fluidInputs.clear();
        fluidOutputs.clear();
        for (IMultiPart part : getParts()) {
            boolean fluidInput = PartAbility.IMPORT_FLUIDS.isApplicable(part.self().getDefinition().getBlock());
            boolean fluidOutput = PartAbility.EXPORT_FLUIDS.isApplicable(part.self().getDefinition().getBlock());
            for (RecipeHandlerList handlerList : part.getRecipeHandlers()) {
                if (!handlerList.isValid(IO.IN) && !handlerList.isValid(IO.OUT)) continue;
                for (IRecipeHandler<?> handler : handlerList.getCapability(FluidRecipeCapability.CAP)) {
                    if (!(handler instanceof NotifiableFluidTank tank)) continue;
                    if (fluidInput && handlerList.isValid(IO.IN)) fluidInputs.add(tank);
                    if (fluidOutput && handlerList.isValid(IO.OUT)) fluidOutputs.add(tank);
                }
            }
        }
        biomeModifier = 0;
        progress = 0;
        pumpSubs = subscribeServerTick(pumpSubs, this::pumpTick);
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        if (pumpSubs != null) {
            pumpSubs.unsubscribe();
            pumpSubs = null;
        }
        fluidInputs.clear();
        fluidOutputs.clear();
    }

    private boolean isRainingInBiome() {
        if (getLevel() == null || !getLevel().isRaining()) return false;
        return getLevel().getBiome(getPos()).value().getPrecipitationAt(getPos()) != Biome.Precipitation.NONE;
    }

    /** GTOCore: {@code biomeModifier << 8}, boosted by 50% while it rains in the biome. */
    public int getFluidProduction() {
        int value = biomeModifier << 8;
        if (isRainingInBiome()) {
            value = value * 3 / 2;
        }
        return Math.max(0, value);
    }

    private void pumpTick() {
        if (getLevel() == null || getLevel().isClientSide || !isFormed()) return;
        if (biomeModifier == 0) {
            biomeModifier = GTUtil.getPumpBiomeModifier(getLevel().getBiome(getPos()));
        }
        if (biomeModifier <= 0) return;
        if (++progress < CYCLE_TICKS) return;
        progress = 0;

        int production = getFluidProduction();
        if (production <= 0) return;

        // Steam in.
        int remaining = production;
        for (NotifiableFluidTank tank : fluidInputs) {
            if (remaining <= 0) break;
            FluidStack drained = tank.drainInternal(new FluidStack(STEAM, remaining),
                    IFluidHandler.FluidAction.EXECUTE);
            remaining -= drained.getAmount();
        }
        int consumed = production - remaining;
        if (consumed <= 0) return;

        // Water out (nothing is voided: only what the output hatches accept is produced).
        int toFill = consumed;
        for (NotifiableFluidTank tank : fluidOutputs) {
            if (toFill <= 0) break;
            toFill -= tank.fillInternal(new FluidStack(Fluids.WATER, toFill), IFluidHandler.FluidAction.EXECUTE);
        }
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        MultiblockDisplayText.builder(textList, isFormed())
                .setWorkingStatus(recipeLogic.isWorkingEnabled(), pumpSubs != null)
                .addCustom(text -> {
                    if (!isFormed()) return;
                    text.add(Component.translatable("gtna.machine.thermal_power_pump.production", getFluidProduction())
                            .withStyle(ChatFormatting.AQUA));
                    if (isRainingInBiome()) {
                        text.add(Component.translatable("gtna.machine.thermal_power_pump.rain")
                                .withStyle(ChatFormatting.BLUE));
                    }
                });
    }
}
