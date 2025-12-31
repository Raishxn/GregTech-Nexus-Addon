package com.raishxn.gtna.common.machine.multiMachineBase;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic.OCParams;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic.OCResult;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.raishxn.gtna.api.capability.SteamWirelessNetworkManager;
import com.raishxn.gtna.common.machine.multiblock.part.steam.WirelessSteamInputHatch;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.UUID;
public abstract class SteamMultiMachineBase extends WorkableMultiblockMachine implements IDisplayUIMachine {

    private final boolean isSteel;

    public SteamMultiMachineBase(IMachineBlockEntity holder, boolean isSteel, Object... args) {
        super(holder, args);
        this.isSteel = isSteel;
    }
    protected double getSteamConversionRate() {
        return 1.0;
    }
    @Nullable
    public static GTRecipe recipeModifier(IMachineBlockEntity machine, GTRecipe recipe, OCParams params, OCResult result) {
        if (machine instanceof IMachineBlockEntity && machine.getMetaMachine() instanceof SteamMultiMachineBase steamMachine) {
            long eut = RecipeHelper.getRealEUt(recipe).getTotalEU();
            long duration = recipe.duration;
            if (eut <= 0) return recipe;
            long totalSteamNeeded = (long) (eut * duration * steamMachine.getSteamConversionRate());
            if (steamMachine.consumeSteam(totalSteamNeeded, true)) {
                steamMachine.consumeSteam(totalSteamNeeded, false);
                return recipe;
            } else {
                return null;
            }
        }
        return null;
    }

    public boolean consumeSteam(long amount, boolean simulate) {
        UUID ownerId = this.getOwnerUUID();
        if (ownerId != null) {
            boolean hasWirelessHatch = false;
            for (IMultiPart part : this.getParts()) {
                if (part instanceof WirelessSteamInputHatch) {
                    hasWirelessHatch = true;
                    break;
                }
            }
            if (hasWirelessHatch) {
                if (SteamWirelessNetworkManager.extractSteam(this.getLevel(), ownerId, amount, simulate)) {
                    return true;
                }
            }
        }
        long remainingNeeded = amount;
        for (IMultiPart part : this.getParts()) {
            var cap = part.self().getHolder().getCapability(ForgeCapabilities.FLUID_HANDLER).resolve();
            if (cap.isPresent()) {
                IFluidHandler handler = cap.get();
                FluidStack toDrain = GTMaterials.Steam.getFluid((int) remainingNeeded);
                FluidStack drained = handler.drain(toDrain, IFluidHandler.FluidAction.SIMULATE);
                if (drained != null && drained.getAmount() > 0) {
                    if (!simulate) {
                        handler.drain(toDrain, IFluidHandler.FluidAction.EXECUTE);
                    }
                    remainingNeeded -= drained.getAmount();
                }
                if (remainingNeeded <= 0) {
                    return true;
                }
            }
        }
        return false;
    }
    public IGuiTexture getScreenTexture() {
        return GuiTextures.DISPLAY_STEAM.get(isSteel);
    }
    @Override
    public ModularUI createUI(Player entityPlayer) {
        var screen = new DraggableScrollableWidgetGroup(7, 4, 162, 121)
                .setBackground(getScreenTexture());
        screen.addWidget(new LabelWidget(4, 5, self().getBlockState().getBlock().getDescriptionId()));
        screen.addWidget(new ComponentPanelWidget(4, 17, this::addDisplayText)
                .setMaxWidthLimit(150)
                .clickHandler(this::handleDisplayClick));
        return new ModularUI(176, 216, this, entityPlayer)
                .background(GuiTextures.BACKGROUND_STEAM.get(isSteel)) // Usa a textura correta (Steel/Bronze)
                .widget(screen)
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(),
                        GuiTextures.SLOT_STEAM.get(isSteel), 7, 134, true));
    }
    @Override
    public void addDisplayText(List<Component> textList) {
        IDisplayUIMachine.super.addDisplayText(textList);
        if (isFormed()) {
            if (this.getOwnerUUID() != null) {
                if (!this.getLevel().isClientSide) {
                    try {
                        long stored = SteamWirelessNetworkManager.getUserSteam((ServerLevel) this.getLevel(), this.getOwnerUUID());
                        if (stored > 0) {
                            textList.add(Component.literal("Wireless Steam: " + stored + " mB")
                                    .withStyle(ChatFormatting.AQUA));
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }
}