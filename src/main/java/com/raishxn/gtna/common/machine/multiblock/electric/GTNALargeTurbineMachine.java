package com.raishxn.gtna.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.ITurbineMachine;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.common.machine.multiblock.part.RotorHolderPartMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.api.machine.multiblock.IGTNAModuleHost;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * GTOCore non-mega large-turbine base: the {@code mega = false} path of GTO's
 * {@code TurbineMachine}, shared by every non-mega turbine port (the Rocket Large Turbine and the
 * Supercritical Steam Turbine). The concrete machines only choose the construction tier, the
 * {@code special} output multiplier and the language id used by their display keys.
 *
 * <p>
 * Ported faithfully from {@code com.gtocore.common.machine.multiblock.generator.TurbineMachine}:
 * base output is {@code V[tier] * (special ? 2.5 : 2)} EU/t, the recipe search is capped by
 * {@code min(energy hatch voltage, rotor voltage * (speed / maxSpeed)^2)} and runs as many
 * parallels as fit under that cap, the duration is multiplied by the rotor holder's total
 * efficiency and the machine voids its EU/fluid outputs. A rotor must be installed (GTO's
 * {@code matchRecipeInput} gate) and the rotor holder must face outwards with a clear front (the
 * pattern predicate ports {@code GTOPredicates.RotorBlockFacingOutwards}).
 *
 * <p>
 * Adaptations, all documented in the class and in the ledger:
 * <ul>
 * <li>GTO's class extends gtolib's closed {@code ElectricMultiblockMachine} and uses
 * {@code RecipeHandlerUnit}/{@code accurateContentParallel}; this port extends the GTCEu
 * {@link WorkableElectricMultiblockMachine} and derives the recipe in {@code getRealRecipe},
 * approximating the content parallel with GTCEu's {@link ParallelLogic}.</li>
 * <li>GTO's {@code ItemPartMachine} rotor auto-insert hatch is a GTO-only part and is not ported.</li>
 * <li>No glass tier: the rotor damage multiplier is fixed (there is no {@code GLASS_TIER} data
 * key), so the mega-only {@code damageBase} adjustment is absent.</li>
 * <li>The High-Speed Mode uses GTO's normal-config values (3x output, 10 rotor damage per second,
 * 8x maintenance fault) with a copied GTO GUI icon; the expert-mode adjustment panel is not
 * ported because GTNA has no expert mode.</li>
 * <li>A formed auxiliary module keeps GTO's non-mega bonus: 2x output, +20% efficiency and a 2x
 * rotor damage multiplier for the high-speed mode.</li>
 * </ul>
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GTNALargeTurbineMachine extends WorkableElectricMultiblockMachine implements ITurbineMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            GTNALargeTurbineMachine.class, WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    /** GTOCore normal-config high-speed values ({@code TurbineMachine}). */
    private static final float HIGH_SPEED_OUTPUT_MULTIPLIER = 3.0F;
    private static final int HIGH_SPEED_ROTOR_DAMAGE = 10;
    private static final float HIGH_SPEED_MACHINE_FAULT = 8.0F;

    /** GTOCore {@code gui/overlay/high_speed_mode.png} (CC BY-NC-SA 4.0), two 16x16 states. */
    private static final ResourceTexture HIGH_SPEED_MODE = new ResourceTexture(
            GTNACORE.id("textures/gui/overlay/high_speed_mode.png"));

    private final String machineId;
    private final long baseEUOutput;
    private final int turbineTier;
    private final List<RotorHolderPartMachine> rotorHolderMachines = new ArrayList<>();

    private long energyPerTick;
    @Persisted
    private boolean highSpeedMode;
    private float accumulatedDamage;
    private double extraOutput = 1;
    private double extraDamage = 1;
    private double extraEfficiency = 1;

    public GTNALargeTurbineMachine(IMachineBlockEntity holder, int tier, boolean special, String machineId) {
        super(holder);
        this.turbineTier = tier;
        this.machineId = machineId;
        this.baseEUOutput = (long) (GTValues.V[tier] * (special ? 2.5 : 2));
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    //////////////////////////////////////
    // ****** Structure ******//
    //////////////////////////////////////

    @Override
    public void onStructureFormed() {
        rotorHolderMachines.clear();
        super.onStructureFormed();
        for (IMultiPart part : getParts()) {
            if (part instanceof RotorHolderPartMachine rotorHolder) {
                rotorHolderMachines.add(rotorHolder);
            }
        }
        // GTO: formedAmount > 0 (a turbine module matched) grants the non-mega module bonus.
        if (gtna$formedModuleCount() > 0) {
            extraOutput = 2;
            extraDamage = 2;
            extraEfficiency = 1.2;
        } else {
            extraOutput = 1;
            extraDamage = 1;
            extraEfficiency = 1;
        }
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        rotorHolderMachines.clear();
        extraOutput = 1;
        extraDamage = 1;
        extraEfficiency = 1;
    }

    private int gtna$formedModuleCount() {
        return ((IGTNAModuleHost) (Object) this).gtna$formedModuleCount();
    }

    @Nullable
    private RotorHolderPartMachine getRotorHolder() {
        for (RotorHolderPartMachine part : rotorHolderMachines) {
            return part;
        }
        return null;
    }

    //////////////////////////////////////
    // ****** Recipe Logic *******//
    //////////////////////////////////////

    @Override
    @Nullable
    protected GTRecipe getRealRecipe(GTRecipe recipe) {
        // GTOCore TurbineMachine#matchRecipeInput: every rotor holder must carry a rotor.
        for (RotorHolderPartMachine part : rotorHolderMachines) {
            if (part.getRotorStack().isEmpty()) return null;
        }
        RotorHolderPartMachine rotorHolder = getRotorHolder();
        if (rotorHolder == null) return null;
        long EUt = recipe.getOutputEUt().voltage();
        if (EUt <= 0) return null;
        int rotorSpeed = rotorHolder.getRotorSpeed();
        if (rotorSpeed < 0) return null;
        int maxSpeed = rotorHolder.getMaxRotorHolderSpeed();
        long turbineMaxVoltage = Math.min(getOverclockVoltage(),
                (long) (getVoltage() * Math.pow((double) Math.min(maxSpeed, rotorSpeed) / maxSpeed, 2)));
        int parallels = 1;
        long maxParallel = turbineMaxVoltage / EUt;
        if (maxParallel > 1) {
            // GTO uses gtolib's accurateContentParallel; GTCEu's content parallel is the closest API.
            parallels = ParallelLogic.getParallelAmount(this, recipe,
                    (int) Math.min(maxParallel, Integer.MAX_VALUE));
            if (parallels <= 0) return null;
        }
        GTRecipe modified = recipe.copy(ContentModifier.multiplier(parallels), false);
        modified.parallels = parallels;
        long eut = Math.min(turbineMaxVoltage, parallels * EUt);
        energyPerTick = eut;
        // GTO's parallel modifier never scales the duration; the efficiency scales it once.
        modified.duration = (int) (recipe.duration * rotorHolder.getTotalEfficiency() * extraEfficiency / 100);
        EURecipeCapability.putEUContent(modified.tickOutputs, new EnergyStack(eut));
        return modified;
    }

    @Override
    public boolean canVoidRecipeOutputs(RecipeCapability<?> capability) {
        return true;
    }

    /** GTOCore {@code getVoltage()}: the rotor's gross output before the speed factor. */
    private long getVoltage() {
        RotorHolderPartMachine rotorHolder = getRotorHolder();
        if (rotorHolder != null && rotorHolder.hasRotor()) {
            return (long) (baseEUOutput * rotorHolder.getTotalPower() *
                    (highSpeedMode ? HIGH_SPEED_OUTPUT_MULTIPLIER : 1L) / 100 * extraOutput);
        }
        return 0;
    }

    //////////////////////////////////////
    // ****** Working / Upkeep ******//
    //////////////////////////////////////

    @Override
    public boolean onWorking() {
        // GTOCore high-speed mode: every second the active rotor holders take 10 damage per unit.
        if (highSpeedMode && self().getOffsetTimer() % 20 == 0) {
            accumulatedDamage += HIGH_SPEED_ROTOR_DAMAGE;
            if (accumulatedDamage >= 1) {
                int damageToApply = (int) accumulatedDamage;
                accumulatedDamage -= damageToApply;
                for (RotorHolderPartMachine part : rotorHolderMachines) {
                    part.damageRotor(damageToApply);
                }
            }
        }
        return super.onWorking();
    }

    @Override
    public void afterWorking() {
        energyPerTick = 0;
        if (highSpeedMode) {
            GTRecipe recipe = getRecipeLogic().getLastRecipe();
            if (recipe != null) {
                for (IMultiPart part : getParts()) {
                    if (part instanceof IMaintenanceMachine maintenanceMachine) {
                        maintenanceMachine.calculateMaintenance(maintenanceMachine,
                                (int) (HIGH_SPEED_MACHINE_FAULT * recipe.duration * extraDamage));
                        break;
                    }
                }
            }
        }
        super.afterWorking();
    }

    //////////////////////////////////////
    // ******* GUI ********//
    //////////////////////////////////////

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        super.attachConfigurators(configuratorPanel);
        configuratorPanel.attachConfigurators(new IFancyConfiguratorButton.Toggle(
                HIGH_SPEED_MODE.getSubTexture(0, 0.5, 1, 0.5),
                HIGH_SPEED_MODE.getSubTexture(0, 0, 1, 0.5),
                () -> highSpeedMode,
                (clickData, pressed) -> {
                    for (RotorHolderPartMachine part : rotorHolderMachines) {
                        part.setRotorSpeed(0);
                    }
                    highSpeedMode = pressed;
                }).setTooltipsSupplier(pressed -> List.of(
                        Component.translatable("gtna.machine." + machineId + ".high_speed_mode")
                                .append(Component.translatable(pressed ?
                                        "gtna.machine." + machineId + ".high_speed_enabled" :
                                        "gtna.machine." + machineId + ".high_speed_disabled")))));
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (!isFormed()) return;
        long voltage = getVoltage();
        textList.add(Component.translatable("gtna.machine." + machineId + ".estimated_output",
                FormattingUtil.formatNumbers(voltage)));
        RotorHolderPartMachine rotorHolder = getRotorHolder();
        if (rotorHolder == null || rotorHolder.getRotorEfficiency() <= 0) return;
        long speedBoost = highSpeedMode ? (long) HIGH_SPEED_OUTPUT_MULTIPLIER : 1L;
        textList.add(Component.translatable("gtceu.multiblock.turbine.rotor_speed",
                FormattingUtil.formatNumbers(getRotorSpeed() * speedBoost * extraOutput),
                FormattingUtil.formatNumbers(rotorHolder.getMaxRotorHolderSpeed() * speedBoost * extraOutput)));
        textList.add(Component.translatable("gtceu.multiblock.turbine.efficiency",
                rotorHolder.getTotalEfficiency() * extraEfficiency));
        if (isActive()) {
            String voltageName = GTValues.VNF[GTUtil.getTierByVoltage(energyPerTick)];
            textList.add(Component.translatable("gtceu.multiblock.turbine.energy_per_tick",
                    FormattingUtil.formatNumbers(energyPerTick), voltageName));
        }
        int rotorDurability = rotorHolder.getRotorDurabilityPercent();
        textList.add(Component.translatable("gtceu.multiblock.turbine.rotor_durability", rotorDurability)
                .setStyle(rotorDurability > 10 ? Style.EMPTY : Style.EMPTY.withColor(ChatFormatting.RED)));
    }

    //////////////////////////////////////
    // ******* Turbine info *******//
    //////////////////////////////////////

    /** GTO's controller reports its construction tier, not the energy hatch tier. */
    @Override
    public int getTier() {
        return turbineTier;
    }

    @Override
    public boolean hasRotor() {
        RotorHolderPartMachine rotorHolder = getRotorHolder();
        return rotorHolder != null && rotorHolder.hasRotor();
    }

    @Override
    public int getRotorSpeed() {
        RotorHolderPartMachine rotorHolder = getRotorHolder();
        return rotorHolder != null ? rotorHolder.getRotorSpeed() : 0;
    }

    @Override
    public int getMaxRotorHolderSpeed() {
        RotorHolderPartMachine rotorHolder = getRotorHolder();
        return rotorHolder != null ? rotorHolder.getMaxRotorHolderSpeed() : 0;
    }

    @Override
    public int getTotalEfficiency() {
        RotorHolderPartMachine rotorHolder = getRotorHolder();
        return rotorHolder != null ? rotorHolder.getTotalEfficiency() : -1;
    }

    @Override
    public long getCurrentProduction() {
        return isActive() && getRecipeLogic().getLastRecipe() != null ?
                getRecipeLogic().getLastRecipe().getOutputEUt().voltage() : 0;
    }

    @Override
    public int getRotorDurabilityPercent() {
        RotorHolderPartMachine rotorHolder = getRotorHolder();
        return rotorHolder != null ? rotorHolder.getRotorDurabilityPercent() : -1;
    }
}
