package com.raishxn.gtna.common.machine.multiblock.noenergy;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
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
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.common.machine.trait.GTNABatchRecipeLogic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EyeOfWoodMachine extends WorkableMultiblockMachine implements IDisplayUIMachine, IFancyUIMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            EyeOfWoodMachine.class, WorkableMultiblockMachine.MANAGED_FIELD_HOLDER);

    private static final int STANDARD_WATER = 256_000;
    private static final int STANDARD_LAVA = 256_000;
    private static final int DURATION = 1200;
    private static final int SUCCESS_BASE = 7500;
    private static final double SUCCESS_SUBSTRATE = Math.pow(2_000_000_000d, 1d / 256d);
    private static final Fluid[] WATER_FLUIDS = new Fluid[] { Fluids.WATER, GTMaterials.Water.getFluid() };
    private static final Fluid[] LAVA_FLUIDS = new Fluid[] { Fluids.LAVA, GTMaterials.Lava.getFluid() };

    private static final OutputEntry[] OUTPUT_POOL = new OutputEntry[] {
            new OutputEntry(12, GTMaterials.Iron, new Material[] { GTMaterials.Nickel, GTMaterials.Tin }),
            new OutputEntry(12, GTMaterials.Copper, new Material[] { GTMaterials.Gold, GTMaterials.Nickel }),
            new OutputEntry(10, GTMaterials.Gold, new Material[] { GTMaterials.Copper, GTMaterials.Silver }),
            new OutputEntry(10, GTMaterials.Tin, new Material[] { GTMaterials.Iron, GTMaterials.Copper }),
            new OutputEntry(9, GTMaterials.Cobalt, new Material[] { GTMaterials.Iron, GTMaterials.Nickel }),
            new OutputEntry(9, GTMaterials.Redstone, new Material[] { GTMaterials.Cinnabar, GTMaterials.Ruby }),
            new OutputEntry(8, GTMaterials.Lapis, new Material[] { GTMaterials.Sodalite, GTMaterials.Lazurite }),
            new OutputEntry(8, GTMaterials.Coal, new Material[] { GTMaterials.Diamond }),
            new OutputEntry(6, GTMaterials.Diamond, new Material[] { GTMaterials.Coal, GTMaterials.Graphite }),
            new OutputEntry(6, GTMaterials.Emerald, new Material[] { GTMaterials.Beryllium, GTMaterials.Aluminium }),
            new OutputEntry(5, GTMaterials.Ruby, new Material[] { GTMaterials.Chromium, GTMaterials.Redstone }),
            new OutputEntry(5, GTMaterials.Sapphire, new Material[] { GTMaterials.Aluminium, GTMaterials.GreenSapphire }),
            new OutputEntry(4, GTMaterials.Silver, new Material[] { GTMaterials.Gold, GTMaterials.Lead }),
            new OutputEntry(4, GTMaterials.Lead, new Material[] { GTMaterials.Silver, GTMaterials.Sulfur })
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
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            subscribeServerTick(this::updateInternalFluidStorage);
        }
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
        // Compatibility: allows "minecraft:overworld" and the "deepspace:deep_space"
        // dimension from the Sky of Grind / #deepspace modpack.  Any other dimension
        // returns null so the Eye of Wood cannot be exploited outside those worlds.
        if (getLevel() == null) {
            successChance = 0;
            return null;
        }
        var dimLoc = getLevel().dimension().location();
        if (!dimLoc.equals(Level.OVERWORLD.location()) &&
            !(dimLoc.getNamespace().equals("deepspace") && dimLoc.getPath().equals("deep_space"))) {
            successChance = 0;
            return null;
        }

        if (storedWater < STANDARD_WATER || storedLava < STANDARD_LAVA) {
            successChance = 0;
            return null;
        }

        successChance = calculateSuccessChance();
        lastRollSucceeded = GTValues.RNG.nextInt(10_000) < successChance;
        storedWater = 0;
        storedLava = 0;

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

    private void updateInternalFluidStorage() {
        if (!isFormed() || getOffsetTimer() % 20 != 0) {
            return;
        }
        int drainedWater = drainFluidFromInputs(STANDARD_WATER - storedWater, WATER_FLUIDS);
        int drainedLava = drainFluidFromInputs(STANDARD_LAVA - storedLava, LAVA_FLUIDS);
        storedWater += drainedWater;
        storedLava += drainedLava;
        successChance = storedWater >= STANDARD_WATER && storedLava >= STANDARD_LAVA ? calculateSuccessChance() : 0;
        if (drainedWater > 0 || drainedLava > 0) {
            GTNACORE.LOGGER.debug("Eye of Wood at {} drained water={}, lava={}, storedWater={}, storedLava={}, chance={}",
                    getPos(), drainedWater, drainedLava, storedWater, storedLava, successChance);
        }
    }

    private int drainFluidFromInputs(int maxAmount, Fluid... fluids) {
        if (maxAmount <= 0) {
            return 0;
        }
        int collected = 0;
        boolean foundFluidHandler = false;

        for (var part : getParts()) {
            for (var handlerList : part.getRecipeHandlers()) {
                if (!handlerList.isValid(IO.IN)) {
                    continue;
                }
                for (Object handlerObj : handlerList.getCapability(FluidRecipeCapability.CAP)) {
                    if (handlerObj instanceof NotifiableFluidTank tank) {
                        foundFluidHandler = true;
                        collected += drainFromTank(tank, maxAmount - collected, fluids);
                    } else if (handlerObj instanceof IFluidHandler handler) {
                        foundFluidHandler = true;
                        collected += drainFromHandler(handler, maxAmount - collected, fluids);
                    } else {
                        GTNACORE.LOGGER.debug("Eye of Wood at {} found non-fluid input handler {}",
                                getPos(), handlerObj.getClass().getName());
                        continue;
                    }
                    if (collected >= maxAmount) {
                        break;
                    }
                }
                if (collected >= maxAmount) {
                    break;
                }
            }
            if (collected >= maxAmount) {
                break;
            }
        }
        if (!foundFluidHandler) {
            GTNACORE.LOGGER.debug("Eye of Wood at {} found no IFluidHandler input hatches in formed parts", getPos());
        } else if (collected == 0) {
            GTNACORE.LOGGER.debug("Eye of Wood at {} found input fluid handlers, but drained no matching fluid",
                    getPos());
        }
        return collected;
    }

    private int drainFromTank(NotifiableFluidTank tank, int maxAmount, Fluid... fluids) {
        int collected = 0;
        for (int index = 0; index < tank.getTanks() && collected < maxAmount; index++) {
            FluidStack stored = tank.getFluidInTank(index);
            if (stored.isEmpty() || !matchesAny(stored, fluids)) {
                continue;
            }
            FluidStack request = stored.copy();
            request.setAmount(Math.min(stored.getAmount(), maxAmount - collected));
            FluidStack drained = tank.drainInternal(request, IFluidHandler.FluidAction.EXECUTE);
            if (!drained.isEmpty()) {
                collected += drained.getAmount();
            }
        }
        return collected;
    }

    private int drainFromHandler(IFluidHandler handler, int maxAmount, Fluid... fluids) {
        int collected = 0;
        for (int index = 0; index < handler.getTanks() && collected < maxAmount; index++) {
            FluidStack stored = handler.getFluidInTank(index);
            if (stored.isEmpty() || !matchesAny(stored, fluids)) {
                continue;
            }
            FluidStack request = stored.copy();
            request.setAmount(Math.min(stored.getAmount(), maxAmount - collected));
            FluidStack drained = handler.drain(request,
                    IFluidHandler.FluidAction.EXECUTE);
            if (!drained.isEmpty()) {
                collected += drained.getAmount();
            }
        }
        return collected;
    }

    private boolean matchesAny(FluidStack stack, Fluid... fluids) {
        for (Fluid fluid : fluids) {
            if (stack.getFluid().isSame(fluid)) {
                return true;
            }
        }
        return false;
    }

    private int calculateSuccessChance() {
        if (storedWater == STANDARD_WATER && storedLava == STANDARD_LAVA) {
            return SUCCESS_BASE;
        }

        int waterDifference = Math.abs(storedWater - STANDARD_WATER) / 1000;
        int lavaDifference = Math.abs(storedLava - STANDARD_LAVA) / 1000;
        if (waterDifference >= STANDARD_WATER / 1000 || lavaDifference >= STANDARD_LAVA / 1000) {
            return 1;
        }

        double waterMultiplier = 1d / Math.pow(SUCCESS_SUBSTRATE, waterDifference);
        double lavaMultiplier = 1d / Math.pow(SUCCESS_SUBSTRATE, lavaDifference);
        return Math.max(1, (int) (SUCCESS_BASE - 7499 * (1d - waterMultiplier * lavaMultiplier)));
    }

    private int getFailSteamOutput() {
        return 36_000 * Math.max(1, successChance);
    }

    private List<ItemStack> generateOutputs() {
        List<ItemStack> outputs = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            OutputEntry entry = rollEntry();
            addOreStyleOutputs(outputs, entry);
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

    private void addOreStyleOutputs(List<ItemStack> outputs, OutputEntry entry) {
        addMaterialStack(outputs, TagPrefix.dust, entry.primary(), 64);
        for (Material byproduct : entry.byproducts()) {
            addMaterialStack(outputs, TagPrefix.dust, byproduct, entry.byproducts().length == 1 ? 48 : 32);
        }
        if (hasMaterialItem(TagPrefix.gem, entry.primary())) {
            if (hasMaterialItem(TagPrefix.gemExquisite, entry.primary())) {
                addMaterialStack(outputs, TagPrefix.gemExquisite, entry.primary(), 16);
                addMaterialStack(outputs, TagPrefix.gemFlawless, entry.primary(), 32);
                addMaterialStack(outputs, TagPrefix.gem, entry.primary(), 32);
            } else {
                addMaterialStack(outputs, TagPrefix.gem, entry.primary(), 64);
            }
        }
    }

    private boolean hasMaterialItem(TagPrefix prefix, Material material) {
        ItemStack stack = ChemicalHelper.get(prefix, material);
        return stack != null && !stack.isEmpty();
    }

    private void addMaterialStack(List<ItemStack> outputs, TagPrefix prefix, Material material, int amount) {
        ItemStack stack = ChemicalHelper.get(prefix, material);
        if (stack == null || stack.isEmpty()) {
            return;
        }
        int remaining = amount;
        while (remaining > 0) {
            int split = Math.min(64, remaining);
            outputs.add(copyWithCount(stack, split));
            remaining -= split;
        }
    }

    private ItemStack copyWithCount(ItemStack stack, int count) {
        ItemStack copy = stack.copy();
        copy.setCount(count);
        return copy;
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

    private record OutputEntry(int weight, Material primary, Material[] byproducts) {}

    public boolean didLastRollSucceed() {
        return lastRollSucceeded;
    }

    public boolean shouldRenderEyeModel() {
        return isFormed() && getRecipeLogic().isActive();
    }
}
