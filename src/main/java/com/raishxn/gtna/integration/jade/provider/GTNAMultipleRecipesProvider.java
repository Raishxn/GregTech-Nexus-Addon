package com.raishxn.gtna.integration.jade.provider;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.integration.jade.GTElementHelper;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.common.machine.multiblock.electric.WorkableElectricMultipleRecipesMachine;
import com.raishxn.gtna.common.machine.trait.GTNAMultipleRecipesLogic;
import com.raishxn.gtna.utils.GTNARecipeUtils;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.util.FluidTextHelper;

import java.util.ArrayList;
import java.util.List;

public class GTNAMultipleRecipesProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    public static final GTNAMultipleRecipesProvider INSTANCE = new GTNAMultipleRecipesProvider();
    private static final ResourceLocation UID = GTNACORE.id("multiple_recipes_provider");

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof MetaMachineBlockEntity metaBlockEntity) {
            MetaMachine metaMachine = metaBlockEntity.getMetaMachine();
            if (metaMachine instanceof WorkableElectricMultipleRecipesMachine machine) {
                GTNAMultipleRecipesLogic logic = machine.getRecipeLogic();
                List<GTNARecipeUtils.ActiveRecipe> recipes = logic.getActiveRecipes();
                data.putInt("ActiveCount", recipes.size());
                data.putInt("MaxThreads", logic.getMaxThreads());
                for (int i = 0; i < recipes.size(); i++) {
                    GTNARecipeUtils.ActiveRecipe activeRecipe = recipes.get(i);
                    GTRecipe recipe = activeRecipe.recipe;
                    String prefix = "T" + i;
                    // 1. Progresso
                    data.putInt(prefix + "P", activeRecipe.progress);
                    data.putInt(prefix + "M", activeRecipe.maxProgress);

                    if (recipe != null) {
                        // 2. Energia (EU/t) [CORRIGIDO]
                        long eut = RecipeHelper.getRealEUt(recipe).getTotalEU();
                        data.putLong(prefix + "EUt", eut);
                        // 3. Outputs de Item
                        var itemContents = recipe.getOutputContents(ItemRecipeCapability.CAP);
                        ListTag itemTags = new ListTag();
                        for (var content : itemContents) {
                            ItemStack[] stacks = ItemRecipeCapability.CAP.of(content.content).getItems();
                            if (stacks.length > 0 && !stacks[0].isEmpty()) {
                                ItemStack stack = stacks[0].copy();
                                CompoundTag itemTag = new CompoundTag();
                                stack.save(itemTag);
                                itemTags.add(itemTag);
                            }
                        }
                        if (!itemTags.isEmpty()) {
                            data.put(prefix + "OutItems", itemTags);
                        }
                        // 4. Outputs de Fluido
                        var fluidContents = recipe.getOutputContents(FluidRecipeCapability.CAP);
                        ListTag fluidTags = new ListTag();
                        for (var content : fluidContents) {
                            FluidStack[] stacks = FluidRecipeCapability.CAP.of(content.content).getStacks();
                            if (stacks.length > 0 && !stacks[0].isEmpty()) {
                                FluidStack stack = stacks[0].copy();
                                CompoundTag fluidTag = new CompoundTag();
                                stack.writeToNBT(fluidTag);
                                fluidTags.add(fluidTag);
                            }
                        }
                        if (!fluidTags.isEmpty()) {
                            data.put(prefix + "OutFluids", fluidTags);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!accessor.getServerData().contains("ActiveCount")) return;
        CompoundTag data = accessor.getServerData();
        int activeCount = data.getInt("ActiveCount");
        int maxThreads = data.getInt("MaxThreads");

        if (activeCount > 0) {
            tooltip.add(Component.literal("Threads: " + activeCount + " / " + maxThreads)
                    .withStyle(ChatFormatting.GRAY));
        }
        IElementHelper helper = tooltip.getElementHelper();
        for (int i = 0; i < activeCount; i++) {
            String prefix = "T" + i;
            // --- Header ---
            tooltip.add(Component.literal("Thread " + (i + 1) + ":")
                    .withStyle(ChatFormatting.GOLD));
            // --- Barra de Progresso ---
            int current = data.getInt(prefix + "P");
            int max = data.getInt(prefix + "M");
            float progress = max > 0 ? (float) current / max : 0;
            Component timeText;
            if (max < 20) {
                timeText = Component.translatable("gtceu.jade.progress_tick", current, max);
            } else {
                timeText = Component.translatable("gtceu.jade.progress_sec",
                        Math.round(current / 20.0F),
                        Math.round(max / 20.0F));
            }
            tooltip.add(helper.progress(
                    progress,
                    timeText,
                    helper.progressStyle().color(generateThreadColor(i)).textColor(-1),
                    Util.make(BoxStyle.DEFAULT, style -> style.borderColor = 0xFF555555),
                    true));
            // --- Energia e Tier ---
            long eut = data.getLong(prefix + "EUt");
            if (eut > 0) {
                int tierId = GTUtil.getTierByVoltage(eut);
                int safeTierId = Math.min(Math.max(0, tierId), GTValues.VC.length - 1);
                String tierName = GTValues.VNF[Math.min(tierId, GTValues.VNF.length - 1)];
                int colorValue = GTValues.VC[safeTierId];
                MutableComponent energyLine = Component.literal("Using: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(FormattingUtil.formatNumbers(eut)).withStyle(ChatFormatting.RED))
                        .append(Component.literal(" EU/t").withStyle(ChatFormatting.RED))
                        .append(Component.literal(" (").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal(tierName).withStyle(Style.EMPTY.withColor(colorValue))) // <-- MUDOU
                                                                                                          // AQUI
                        .append(Component.literal(")").withStyle(ChatFormatting.GRAY));

                tooltip.add(energyLine);
            }
            // --- Outputs ---
            List<ItemStack> items = new ArrayList<>();
            if (data.contains(prefix + "OutItems", Tag.TAG_LIST)) {
                ListTag list = data.getList(prefix + "OutItems", Tag.TAG_COMPOUND);
                for (Tag tag : list) {
                    items.add(ItemStack.of((CompoundTag) tag));
                }
            }
            List<FluidStack> fluids = new ArrayList<>();
            if (data.contains(prefix + "OutFluids", Tag.TAG_LIST)) {
                ListTag list = data.getList(prefix + "OutFluids", Tag.TAG_COMPOUND);
                for (Tag tag : list) {
                    fluids.add(FluidStack.loadFluidStackFromNBT((CompoundTag) tag));
                }
            }
            if (!items.isEmpty() || !fluids.isEmpty()) {
                tooltip.add(Component.literal("Recipe Output:").withStyle(ChatFormatting.WHITE));
                for (ItemStack stack : items) {
                    tooltip.add(helper.smallItem(stack));
                    tooltip.append(Component.literal(" " + stack.getCount() + "x ")
                            .withStyle(ChatFormatting.WHITE)
                            .append(stack.getHoverName()));
                }
                for (FluidStack stack : fluids) {
                    tooltip.add(GTElementHelper.smallFluid(
                            snownee.jade.api.fluid.JadeFluidObject.of(stack.getFluid(), stack.getAmount())));
                    tooltip.append(Component
                            .literal(" " + FluidTextHelper.getUnicodeMillibuckets(stack.getAmount(), true) + " ")
                            .withStyle(ChatFormatting.WHITE)
                            .append(stack.getDisplayName()));
                }
            }
        }
    }

    private static int generateThreadColor(int threadIndex) {
        float goldenRatio = 0.618033988749895f;
        float hue = ((threadIndex + 1) * goldenRatio) % 1.0f;
        float saturation = 0.85f;
        float value = 0.85f;
        return 0xFF000000 | java.awt.Color.HSBtoRGB(hue, saturation, value);
    }
}
