package com.raishxn.gtna.common.machine.multiblock.noenergy;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;

import com.raishxn.gtna.common.machine.trait.GTNABatchRecipeLogic;
import com.raishxn.gtna.utils.MachineIO;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EyeOfWoodMachine extends WorkableMultiblockMachine implements IDisplayUIMachine, IFancyUIMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            EyeOfWoodMachine.class, WorkableMultiblockMachine.MANAGED_FIELD_HOLDER);

    private static final int STANDARD_WATER = 256_000;
    private static final int STANDARD_LAVA = 256_000;
    private static final int MAX_BONUS_FLUID = 256_000;
    private static final int DURATION = 1200;
    private static final int SUCCESS_BASE = 7500;
    private static final double SUCCESS_SUBSTRATE = Math.pow(2_000_000_000d, 1d / 256d);

    private static final OutputEntry[] OUTPUT_POOL = new OutputEntry[] {
            new OutputEntry(12, new OutputStackSpec[] {
                    new OutputStackSpec(48, GTMaterials.Gold, GTMaterials.Copper) }),
            new OutputEntry(12, new OutputStackSpec[] {
                    new OutputStackSpec(64, GTMaterials.Copper, GTMaterials.Gold) }),
            new OutputEntry(12, new OutputStackSpec[] {
                    new OutputStackSpec(64, GTMaterials.Iron, GTMaterials.Nickel) }),
            new OutputEntry(10, new OutputStackSpec[] {
                    new OutputStackSpec(48, GTMaterials.Cobalt, GTMaterials.Iron) }),
            new OutputEntry(9, new OutputStackSpec[] {
                    new OutputStackSpec(64, GTMaterials.Coal, null) }),
            new OutputEntry(9, new OutputStackSpec[] {
                    new OutputStackSpec(48, GTMaterials.Tin, GTMaterials.Iron) }),
            new OutputEntry(8, new OutputStackSpec[] {
                    new OutputStackSpec(64, GTMaterials.Redstone, null) }),
            new OutputEntry(8, new OutputStackSpec[] {
                    new OutputStackSpec(48, GTMaterials.Lapis, null) })
    };

    @Persisted
    @DescSynced
    private int storedWater;
    @Persisted
    @DescSynced
    private int storedLava;
    @Persisted
    @DescSynced
    private int successChance;
    @Persisted
    @DescSynced
    private boolean lastRollSucceeded;

    public EyeOfWoodMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    protected @NotNull RecipeLogic createRecipeLogic(Object... args) {
        return new GTNABatchRecipeLogic(this, this::buildRecipe);
    }

    @Override
    public @NotNull GTNABatchRecipeLogic getRecipeLogic() {
        return (GTNABatchRecipeLogic) super.getRecipeLogic();
    }

    private @Nullable GTRecipe buildRecipe() {
        if (!isFormed() || !isWorkingEnabled()) {
            return null;
        }

        captureFluids();
        if (storedWater < STANDARD_WATER || storedLava < STANDARD_LAVA) {
            successChance = 0;
            return null;
        }

        successChance = calculateSuccessChance();
        lastRollSucceeded = GTValues.RNG.nextInt(10_000) < successChance;

        GTRecipeBuilder builder = GTRecipeBuilder.ofRaw().recipeType(GTRecipeTypes.DUMMY_RECIPES).duration(DURATION);
        if (lastRollSucceeded) {
            for (ItemStack output : generateOutputs()) {
                builder.outputItems(output);
            }
        } else {
            builder.outputFluids(GTMaterials.Steam.getFluid(getFailSteamOutput()));
        }
        return builder.buildRawRecipe();
    }

    private void captureFluids() {
        storedWater = drainBonusFluid(Fluids.WATER, STANDARD_WATER);
        storedLava = drainBonusFluid(Fluids.LAVA, STANDARD_LAVA);
    }

    private int drainBonusFluid(net.minecraft.world.level.material.Fluid fluid, int required) {
        int collected = 0;
        while (collected < required && MachineIO.inputFluid(this, FluidStack.create(fluid, 1000))) {
            collected += 1000;
        }
        while (collected < required + MAX_BONUS_FLUID && MachineIO.inputFluid(this, FluidStack.create(fluid, 1000))) {
            collected += 1000;
        }
        return collected;
    }

    private int calculateSuccessChance() {
        int extraWater = Math.max(0, storedWater - STANDARD_WATER) / 1000;
        int extraLava = Math.max(0, storedLava - STANDARD_LAVA) / 1000;
        double bonusFactor = 1.0 - 1.0 / Math.pow(SUCCESS_SUBSTRATE, extraWater + extraLava);
        int bonus = (int) Math.floor(2499.0 * bonusFactor);
        return Math.min(9999, SUCCESS_BASE + bonus);
    }

    private int getFailSteamOutput() {
        return 36_000 * Math.max(1, successChance / 1000);
    }

    private List<ItemStack> generateOutputs() {
        List<ItemStack> outputs = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            OutputEntry entry = rollEntry();
            for (OutputStackSpec spec : entry.outputs()) {
                addDust(outputs, spec.primary(), spec.primaryAmount());
                if (spec.secondary() != null) {
                    addDust(outputs, spec.secondary(), Math.max(16, spec.primaryAmount() / 2));
                }
            }
        }
        return outputs;
    }

    private OutputEntry rollEntry() {
        int totalWeight = 0;
        for (OutputEntry entry : OUTPUT_POOL) {
            totalWeight += entry.weight();
        }
        int roll = GTValues.RNG.nextInt(totalWeight);
        int cursor = 0;
        for (OutputEntry entry : OUTPUT_POOL) {
            cursor += entry.weight();
            if (roll < cursor) {
                return entry;
            }
        }
        return OUTPUT_POOL[0];
    }

    private void addDust(List<ItemStack> outputs, com.gregtechceu.gtceu.api.data.chemical.material.Material material,
                         int amount) {
        var dust = com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper.get(
                com.gregtechceu.gtceu.api.data.tag.TagPrefix.dust, material);
        if (dust == null) {
            return;
        }
        int remaining = amount;
        while (remaining > 0) {
            int split = Math.min(64, remaining);
            outputs.add(dust.copyWithCount(split));
            remaining -= split;
        }
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(198, 208, this, entityPlayer)
                .widget(new FancyMachineUIWidget(this, 198, 208));
    }

    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 190, 125);
        var screen = new DraggableScrollableWidgetGroup(4, 4, 182, 117)
                .setBackground(GuiTextures.DISPLAY);
        screen.addWidget(new LabelWidget(4, 5, self().getBlockState().getBlock().getDescriptionId()));
        screen.addWidget(new ComponentPanelWidget(4, 17, this::addDisplayText).setMaxWidthLimit(170));
        group.addWidget(screen);
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    @Override
    public void addDisplayText(@NotNull List<Component> textList) {
        MultiblockDisplayText.builder(textList, isFormed())
                .setWorkingStatus(recipeLogic.isWorkingEnabled(), recipeLogic.isActive())
                .addWorkingStatusLine()
                .addProgressLine(recipeLogic)
                .addCustom(text -> {
                    text.add(Component.translatable("gtna.machine.eye_of_wood.water", storedWater, STANDARD_WATER)
                            .withStyle(ChatFormatting.BLUE));
                    text.add(Component.translatable("gtna.machine.eye_of_wood.lava", storedLava, STANDARD_LAVA)
                            .withStyle(ChatFormatting.RED));
                    text.add(Component.translatable("gtna.machine.eye_of_wood.chance", successChance)
                            .withStyle(ChatFormatting.GOLD));
                    text.add(Component.translatable("gtna.machine.eye_of_wood.last_result",
                                    Component.translatable(lastRollSucceeded ?
                                            "gtna.machine.eye_of_wood.result.success" :
                                            "gtna.machine.eye_of_wood.result.fail"))
                            .withStyle(lastRollSucceeded ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY));
                })
                .addOutputLines(recipeLogic.getLastRecipe());
    }

    private record OutputEntry(int weight, OutputStackSpec[] outputs) {}

    private record OutputStackSpec(int primaryAmount,
                                   com.gregtechceu.gtceu.api.data.chemical.material.Material primary,
                                   com.gregtechceu.gtceu.api.data.chemical.material.Material secondary) {}
}
