package com.raishxn.gtna.common.machine.multiblock.steam;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.raishxn.gtna.common.data.GTNAMaterials;
import com.raishxn.gtna.common.machine.multiMachineBase.SteamMultiMachineBase;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;

public class VoidMinerSteamGateAged extends SteamMultiMachineBase implements IDisplayUIMachine, IFancyUIMachine {

    public VoidMinerSteamGateAged(IMachineBlockEntity holder, Object... args) {
        super(holder, false, args);
    }

    @Nullable
    public static ModifierFunction recipeModifier(MetaMachine machine, @Nonnull GTRecipe recipe) {
        if (!(machine instanceof VoidMinerSteamGateAged voidMiner)) return ModifierFunction.NULL;

        long outputMult = 1;
        double timeFactor = 1.0;
        double energyFactor = 1.0;

        Fluid insanelySteam = GTNAMaterials.InsanelySupercriticalSteam.getFluid();
        Fluid superHeatedSteam = GTNAMaterials.SuperHeatedSteam.getFluid();
        Fluid denseSteam = GTNAMaterials.DenseSupercriticalSteam.getFluid();

        boolean foundTier = false;

        for (IMultiPart part : voidMiner.getParts()) {
            IFluidHandler handler = part.self().getFluidHandlerCap(null, true);
            if (handler != null) {
                for (int i = 0; i < handler.getTanks(); i++) {
                    FluidStack fluidInTank = handler.getFluidInTank(i);
                    if (!fluidInTank.isEmpty()) {
                        Fluid fluid = fluidInTank.getFluid();

                        // -- Lógica de Tiers --

                        // Tier 3: Insanely Supercritical
                        if (fluid.isSame(insanelySteam)) {
                            outputMult = 5;
                            timeFactor = 0.2; // 5x mais rápido
                            energyFactor = 4.0;
                            foundTier = true;
                            break;
                        }
                        // Tier 2: SuperHeated
                        else if (fluid.isSame(superHeatedSteam)) {
                            if (outputMult < 3) {
                                outputMult = 3;
                                timeFactor = 0.333; // ~3x mais rápido
                                energyFactor = 2.0;
                            }
                        }
                        // Tier 1: Dense Supercritical
                        else if (fluid.isSame(denseSteam)) {
                            if (outputMult < 2) {
                                outputMult = 2;
                                timeFactor = 0.5; // 2x mais rápido
                                energyFactor = 1.5;
                            }
                        }
                    }
                }
            }
            if (foundTier) break;
        }
        if (outputMult == 1) return ModifierFunction.IDENTITY;
        long finalOutputMult = outputMult;
        double finalTimeFactor = timeFactor;
        double finalEnergyFactor = energyFactor;
        return r -> ModifierFunction.builder()
                .outputModifier(ContentModifier.multiplier(finalOutputMult))
                .durationMultiplier(finalTimeFactor)
                .eutMultiplier(finalEnergyFactor)
                .build()
                .apply(r);
    }

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
        screen.addWidget(new ComponentPanelWidget(4, 17, this::addDisplayText).setMaxWidthLimit(170));

        group.addWidget(screen);
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        MultiblockDisplayText.builder(textList, isFormed())
                .setWorkingStatus(recipeLogic.isWorkingEnabled(), recipeLogic.isActive())
                .addWorkingStatusLine()
                .addProgressLine(recipeLogic)
                .addCustom(tl -> tl.add(Component.translatable("gtna.machine.void_miner.steam_tier")
                        .append(": ")
                        .append(Component.literal(detectSteamTier()).withStyle(ChatFormatting.GOLD))))
                .addOutputLines(recipeLogic.getLastRecipe());
    }

    private String detectSteamTier() {
        if (!isFormed()) return "Structure Incomplete";

        Fluid insanelySteam = GTNAMaterials.InsanelySupercriticalSteam.getFluid();
        Fluid superHeatedSteam = GTNAMaterials.SuperHeatedSteam.getFluid();
        Fluid denseSteam = GTNAMaterials.DenseSupercriticalSteam.getFluid();

        int currentTier = 0;

        for (IMultiPart part : getParts()) {
            IFluidHandler handler = part.self().getFluidHandlerCap(null, true);
            if (handler != null) {
                for (int i = 0; i < handler.getTanks(); i++) {
                    FluidStack fs = handler.getFluidInTank(i);
                    if (fs.isEmpty()) continue;

                    if (fs.getFluid().isSame(insanelySteam)) return "Insanely (5x Items, 5x Speed)";
                    if (fs.getFluid().isSame(superHeatedSteam)) currentTier = Math.max(currentTier, 2);
                    if (fs.getFluid().isSame(denseSteam)) currentTier = Math.max(currentTier, 1);
                }
            }
        }

        return switch (currentTier) {
            case 2 -> "SuperHeated (3x Items, 3x Speed)";
            case 1 -> "Dense (2x Items, 2x Speed)";
            default -> "Normal Steam";
        };
    }
}