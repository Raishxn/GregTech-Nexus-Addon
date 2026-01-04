package com.raishxn.gtna.common.machine.multiblock.noenergy;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.common.data.GTMaterials;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class InfernalCokeOven extends WorkableMultiblockMachine implements IDisplayUIMachine, IFancyUIMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            InfernalCokeOven.class, WorkableMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Persisted @DescSynced
    private long continuousWorkingTicks = 0;

    // Constantes
    private static final int TICKS_PER_STAGE = 10 * 60 * 20; // 10 min
    private static final int BASE_PARALLEL = 8;
    private static final int PARALLEL_INCREASE = 16;
    private static final int MAX_PARALLEL_CAP = 256;

    private static final long BASE_STEAM_PER_TICK = 320;
    private static final long STEAM_INCREASE_PER_STAGE = 80;

    public InfernalCokeOven(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    protected @NotNull RecipeLogic createRecipeLogic(Object... args) {
        return new RecipeLogic(this);
    }

    // --- LÓGICA DE AQUECIMENTO E RESFRIAMENTO ---

    @Override
    public void onLoad() {
        super.onLoad();
        // Inscreve a função coolingTick para rodar a cada tick do servidor
        if (!isRemote()) {
            subscribeServerTick(this::coolingTick);
        }
    }

    // Lógica personalizada que roda SEMPRE, independente do RecipeLogic dormir
    private void coolingTick() {
        if (!recipeLogic.isWorking() && this.continuousWorkingTicks > 0) {
            long decay = (long) (this.continuousWorkingTicks * 0.0005); // 0.05% decay
            this.continuousWorkingTicks -= Math.max(1, decay);
        }
    }

    @Override
    public boolean onWorking() {
        boolean result = super.onWorking();
        // Se estiver trabalhando, aquece
        if (result && this.continuousWorkingTicks < Long.MAX_VALUE - 100) {
            this.continuousWorkingTicks++;
        }
        return result;
    }

    public int getCurrentStage() {
        return (int) (this.continuousWorkingTicks / TICKS_PER_STAGE);
    }

    public double getSpeedBonus() {
        double bonus = 1.0 + ((double) continuousWorkingTicks / 100.0) * 0.01;
        return Math.min(10.0, bonus);
    }

    @Nullable
    public static ModifierFunction recipeModifier(MetaMachine machine, @Nonnull GTRecipe recipe) {
        if (machine instanceof InfernalCokeOven ico) {
            int stage = ico.getCurrentStage();

            int maxPossibleParallel = Math.min(MAX_PARALLEL_CAP, BASE_PARALLEL + (stage * PARALLEL_INCREASE));
            int actualParallel = ParallelLogic.getParallelAmount(machine, recipe, maxPossibleParallel);

            if (actualParallel == 0) return ModifierFunction.NULL;

            double speedBonus = ico.getSpeedBonus();
            double durationMultiplier = 1.0 / speedBonus;
            long steamPerTick = BASE_STEAM_PER_TICK + ((long) stage * STEAM_INCREASE_PER_STAGE);

            var baseModifier = ModifierFunction.builder()
                    .parallels(actualParallel)
                    .modifyAllContents(ContentModifier.multiplier(actualParallel))
                    .durationMultiplier(durationMultiplier)
                    .build();

            return r -> {
                GTRecipe modified = baseModifier.apply(r);
                if (modified != null) {
                    long totalSteamLong = steamPerTick * modified.duration;
                    int totalSteamInt = (int) Math.min(Integer.MAX_VALUE, totalSteamLong);
                    FluidIngredient steamIng = FluidIngredient.of(GTMaterials.Steam.getFluid(totalSteamInt));

                    modified.inputs.computeIfAbsent(FluidRecipeCapability.CAP, k -> new ArrayList<>())
                            .add(new Content(steamIng, ChanceLogic.getMaxChancedValue(), ChanceLogic.getMaxChancedValue(), 0));
                }
                return modified;
            };
        }
        return ModifierFunction.NULL;
    }

    // --- INTERFACE (UI) ---
    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(198, 208, this, entityPlayer)
                .widget(new FancyMachineUIWidget(this, 198, 208));
    }

    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 182 + 8, 117 + 8);
        var screen = new DraggableScrollableWidgetGroup(4, 4, 182, 117)
                .setBackground(GuiTextures.DISPLAY);
        screen.addWidget(new LabelWidget(4, 5, self().getBlockState().getBlock().getDescriptionId()));
        screen.addWidget(new ComponentPanelWidget(4, 17, this::addDisplayText)
                .setMaxWidthLimit(170));
        group.addWidget(screen);
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    @Override
    public void addDisplayText(@NotNull List<Component> textList) {
        if (isFormed()) {
            int stage = getCurrentStage();
            long nextStageTicks = (long) (stage + 1) * TICKS_PER_STAGE - continuousWorkingTicks;
            long steamPerTick = BASE_STEAM_PER_TICK + ((long) stage * STEAM_INCREASE_PER_STAGE);

            int maxStageParallel = Math.min(MAX_PARALLEL_CAP, BASE_PARALLEL + (stage * PARALLEL_INCREASE));
            double speedPercent = (getSpeedBonus() * 100);

            textList.add(Component.translatable("gtna.multiblock.infernal_coke.speed",
                    Component.literal(String.format("%.0f%%", speedPercent)).withStyle(ChatFormatting.RED)));

            textList.add(Component.literal("Stage: ")
                    .append(Component.literal(String.valueOf(stage)).withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(" (Next in: " + (nextStageTicks/20) + "s)").withStyle(ChatFormatting.GRAY)));

            textList.add(Component.literal("Max Parallels: ")
                    .append(Component.literal(String.valueOf(maxStageParallel)).withStyle(ChatFormatting.BLUE)));

            textList.add(Component.literal("Steam Req: ")
                    .append(Component.literal(String.valueOf(steamPerTick * 20) + " L/s").withStyle(ChatFormatting.GRAY)));
        }
        MultiblockDisplayText.builder(textList, isFormed())
                .setWorkingStatus(recipeLogic.isWorkingEnabled(), recipeLogic.isActive())
                .addWorkingStatusLine()
                .addProgressLine(recipeLogic)
                .addOutputLines(recipeLogic.getLastRecipe());
    }
}