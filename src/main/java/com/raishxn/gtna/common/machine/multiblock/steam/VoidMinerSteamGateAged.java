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

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import com.raishxn.gtna.common.data.GTNAMaterials;
import com.raishxn.gtna.common.machine.multiMachineBase.SteamMultiMachineBase;
import com.raishxn.gtna.config.ConfigHolder;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.Nonnull;

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

        // Carrega configurações
        var cfg = ConfigHolder.INSTANCE;

        boolean foundTier = false;

        for (IMultiPart part : voidMiner.getParts()) {
            IFluidHandler handler = part.self().getFluidHandlerCap(null, true);
            if (handler != null) {
                for (int i = 0; i < handler.getTanks(); i++) {
                    FluidStack fluidInTank = handler.getFluidInTank(i);
                    if (!fluidInTank.isEmpty()) {
                        Fluid fluid = fluidInTank.getFluid();

                        // -- Lógica de Tiers (usando Config) --

                        // Tier 3: Insanely Supercritical
                        if (fluid.isSame(insanelySteam)) {
                            outputMult = cfg.voidMinerInsanelyOutputMult;
                            timeFactor = 1.0 / cfg.voidMinerInsanelySpeedMult; // Inverte para obter duração
                            energyFactor = cfg.voidMinerInsanelyEnergyMult;
                            foundTier = true;
                            break;
                        }
                        // Tier 2: SuperHeated
                        else if (fluid.isSame(superHeatedSteam)) {
                            if (outputMult < cfg.voidMinerSuperHeatedOutputMult) {
                                outputMult = cfg.voidMinerSuperHeatedOutputMult;
                                timeFactor = 1.0 / cfg.voidMinerSuperHeatedSpeedMult;
                                energyFactor = cfg.voidMinerSuperHeatedEnergyMult;
                            }
                        }
                        // Tier 1: Dense Supercritical
                        else if (fluid.isSame(denseSteam)) {
                            if (outputMult < cfg.voidMinerDenseOutputMult) {
                                outputMult = cfg.voidMinerDenseOutputMult;
                                timeFactor = 1.0 / cfg.voidMinerDenseSpeedMult;
                                energyFactor = cfg.voidMinerDenseEnergyMult;
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

        var cfg = ConfigHolder.INSTANCE;

        int currentTier = 0;

        for (IMultiPart part : getParts()) {
            IFluidHandler handler = part.self().getFluidHandlerCap(null, true);
            if (handler != null) {
                for (int i = 0; i < handler.getTanks(); i++) {
                    FluidStack fs = handler.getFluidInTank(i);
                    if (fs.isEmpty()) continue;

                    if (fs.getFluid().isSame(insanelySteam))
                        return String.format("Insanely (%dx Items, %.0fx Speed)", cfg.voidMinerInsanelyOutputMult,
                                cfg.voidMinerInsanelySpeedMult);

                    if (fs.getFluid().isSame(superHeatedSteam)) currentTier = Math.max(currentTier, 2);
                    if (fs.getFluid().isSame(denseSteam)) currentTier = Math.max(currentTier, 1);
                }
            }
        }

        return switch (currentTier) {
            case 2 -> String.format("SuperHeated (%dx Items, %.0fx Speed)", cfg.voidMinerSuperHeatedOutputMult,
                    cfg.voidMinerSuperHeatedSpeedMult);
            case 1 -> String.format("Dense (%dx Items, %.0fx Speed)", cfg.voidMinerDenseOutputMult,
                    cfg.voidMinerDenseSpeedMult);
            default -> "Normal Steam";
        };
    }
}
