package com.raishxn.gtna.common.machine.multiblock.energy;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.ItemStackHashStrategy;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import com.raishxn.gtna.common.machine.multiblock.part.ae.GTNACraftPatternPartMachine;
import com.raishxn.gtna.common.machine.trait.GTNABatchRecipeLogic;
import it.unimi.dsi.fastutil.objects.Object2LongOpenCustomHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NexusMolecularForgeMachine extends WorkableElectricMultiblockMachine
                                        implements IDisplayUIMachine, IFancyUIMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            NexusMolecularForgeMachine.class, WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    private final List<GTNACraftPatternPartMachine> craftPatternParts = new ArrayList<>();
    private long activeBatchItemCount;
    private int activeBatchDistinctOutputs;
    private long activeBatchEUt;
    private int activeBatchDuration;

    public NexusMolecularForgeMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        craftPatternParts.clear();
        for (var part : getParts()) {
            if (part instanceof GTNACraftPatternPartMachine patternPart) {
                craftPatternParts.add(patternPart);
                patternPart.setOnContentsChanged(() -> getRecipeLogic().updateTickSubscription());
            }
        }
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        craftPatternParts.clear();
        clearActiveBatch();
    }

    public int getCraftPatternHatchCount() {
        return craftPatternParts.size();
    }

    public int getLoadedPatternCount() {
        int total = 0;
        for (GTNACraftPatternPartMachine part : craftPatternParts) {
            total += part.getLoadedPatternCount();
        }
        return total;
    }

    public long getQueuedItemCount() {
        long total = 0L;
        for (GTNACraftPatternPartMachine part : craftPatternParts) {
            total += part.getPendingItemCount();
        }
        return total;
    }

    private int getQueuedOutputTypes() {
        Object2LongOpenCustomHashMap<ItemStack> preview = new Object2LongOpenCustomHashMap<>(
                ItemStackHashStrategy.comparingAllButCount());
        for (GTNACraftPatternPartMachine part : craftPatternParts) {
            for (GTNACraftPatternPartMachine.InternalSlot slot : part.getInternalInventory()) {
                slot.serializeNBT().getList("inventory", 10).forEach(tag -> {
                    if (tag instanceof net.minecraft.nbt.CompoundTag compoundTag) {
                        ItemStack stack = ItemStack.of(compoundTag);
                        long amount = compoundTag.getLong("real");
                        if (!stack.isEmpty() && amount > 0L) {
                            preview.addTo(stack, amount);
                        }
                    }
                });
            }
        }
        return preview.size();
    }

    private void clearActiveBatch() {
        activeBatchItemCount = 0L;
        activeBatchDistinctOutputs = 0;
        activeBatchEUt = 0L;
        activeBatchDuration = 0;
    }

    private @Nullable GTRecipe buildBatchRecipe() {
        long maxEUt = getOverclockVoltage();
        if (maxEUt <= 0L) {
            clearActiveBatch();
            return null;
        }

        Object2LongOpenCustomHashMap<ItemStack> outputs = new Object2LongOpenCustomHashMap<>(
                ItemStackHashStrategy.comparingAllButCount());
        for (GTNACraftPatternPartMachine part : craftPatternParts) {
            part.drainPendingOutputs(outputs);
        }

        if (outputs.isEmpty()) {
            clearActiveBatch();
            return null;
        }

        long totalItems = 0L;
        GTRecipeBuilder builder = GTRecipeBuilder.ofRaw().recipeType(GTRecipeTypes.DUMMY_RECIPES);
        for (var entry : outputs.object2LongEntrySet()) {
            ItemStack stack = entry.getKey().copy();
            long amount = entry.getLongValue();
            totalItems += amount;
            while (amount > 0L) {
                int split = (int) Math.min(amount, Integer.MAX_VALUE);
                ItemStack output = stack.copy();
                output.setCount(split);
                builder.outputItems(output);
                amount -= split;
            }
        }

        long eut = Math.max(1L, Math.min(maxEUt, totalItems));
        int duration = Math.max(1, (int) Math.ceil(totalItems / (double) eut));

        activeBatchItemCount = totalItems;
        activeBatchDistinctOutputs = outputs.size();
        activeBatchEUt = eut;
        activeBatchDuration = duration;

        builder.EUt(eut).duration(duration);
        return builder.buildRawRecipe();
    }

    @Override
    protected @NotNull RecipeLogic createRecipeLogic(Object... args) {
        return new GTNABatchRecipeLogic(this, this::buildBatchRecipe) {
            @Override
            public void onRecipeFinish() {
                super.onRecipeFinish();
                clearActiveBatch();
            }
        };
    }

    @Override
    public @NotNull GTNABatchRecipeLogic getRecipeLogic() {
        return (GTNABatchRecipeLogic) super.getRecipeLogic();
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
        long queuedItems = getQueuedItemCount();
        int queuedTypes = getQueuedOutputTypes();

        MultiblockDisplayText.builder(textList, isFormed())
                .setWorkingStatus(recipeLogic.isWorkingEnabled(), recipeLogic.isActive())
                .addWorkingStatusLine()
                .addEnergyUsageLine(getEnergyContainer())
                .addCustom(text -> {
                    text.add(Component.literal("Craft Pattern Hatches: ").withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(String.valueOf(getCraftPatternHatchCount()))
                                    .withStyle(ChatFormatting.AQUA)));
                    text.add(Component.literal("Loaded Patterns: ").withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(String.valueOf(getLoadedPatternCount()))
                                    .withStyle(ChatFormatting.GREEN)));
                    text.add(Component.literal("Queued Outputs: ").withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(
                                    FormattingUtil.formatNumbers(queuedTypes) + " types / " +
                                            FormattingUtil.formatNumbers(queuedItems) + " items")
                                    .withStyle(ChatFormatting.GOLD)));

                    if (activeBatchItemCount > 0L) {
                        text.add(Component.literal("Active Batch: ").withStyle(ChatFormatting.GRAY)
                                .append(Component.literal(
                                        FormattingUtil.formatNumbers(activeBatchDistinctOutputs) + " types / " +
                                                FormattingUtil.formatNumbers(activeBatchItemCount) + " items")
                                        .withStyle(ChatFormatting.LIGHT_PURPLE)));
                        text.add(Component.literal("Batch Cost: ").withStyle(ChatFormatting.GRAY)
                                .append(Component.literal(String.format(Locale.US, "%,d EU/t x %d t",
                                        activeBatchEUt, activeBatchDuration))
                                        .withStyle(ChatFormatting.RED)));
                    } else {
                        text.add(Component.literal("Active Batch: ").withStyle(ChatFormatting.GRAY)
                                .append(Component.literal("Idle").withStyle(ChatFormatting.DARK_GRAY)));
                    }

                    text.add(Component.literal("Forge Ceiling: ").withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(String.format(Locale.US, "%,d EU/t", getOverclockVoltage()))
                                    .withStyle(ChatFormatting.WHITE)));
                })
                .addProgressLine(recipeLogic);
    }
}
