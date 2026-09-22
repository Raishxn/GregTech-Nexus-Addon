package com.raishxn.gtna.common.machine.multiblock.part.steam;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.gui.widget.TankWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.machine.multiblock.part.SteamHatchPartMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import com.raishxn.gtna.api.capability.SteamWirelessNetworkManager;
import com.raishxn.gtna.config.ConfigHolder;

import java.util.UUID;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WirelessSteamInputHatch extends SteamHatchPartMachine {

    private final long transferRate;
    private final boolean isSteel;

    public WirelessSteamInputHatch(IMachineBlockEntity holder, boolean isSteel, Object... args) {
        super(holder, args);
        this.isSteel = isSteel;
        this.transferRate = isSteel ? ConfigHolder.INSTANCE.wirelessSteam.steelTransferRate :
                ConfigHolder.INSTANCE.wirelessSteam.bronzeTransferRate;
        this.setWorkingEnabled(false);
        if (this.isSteel) {
            if (this.tank.getStorages().length > 0) {
                this.tank.getStorages()[0].setCapacity(ConfigHolder.INSTANCE.wirelessSteam.steelBuffer);
            }
        } else if (this.tank.getStorages().length > 0) {
            this.tank.getStorages()[0].setCapacity(ConfigHolder.INSTANCE.wirelessSteam.bronzeBuffer);
        }
    }

    @Override
    public boolean isWorkingEnabled() {
        return ConfigHolder.INSTANCE.wirelessSteam.enabled && super.isWorkingEnabled();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() != null && !getLevel().isClientSide) {
            this.subscribeServerTick(this::updateWireless);
        }
    }

    @Override
    protected NotifiableFluidTank createTank(int initialCapacity, int slots, Object... args) {
        int configuredCapacity = isSteel ? ConfigHolder.INSTANCE.wirelessSteam.steelBuffer :
                ConfigHolder.INSTANCE.wirelessSteam.bronzeBuffer;
        return new NotifiableFluidTank(this, 1, configuredCapacity, IO.IN)
                .setFilter(fluidStack -> fluidStack.getFluid().is(GTMaterials.Steam.getFluidTag()));
    }

    /** The configured per-tick cap for this hatch; {@link Integer#MAX_VALUE} means "whole buffer". */
    public long getTransferRate() {
        return transferRate <= 0 ? Integer.MAX_VALUE : transferRate;
    }

    /** Whether this hatch throttles below its buffer (false = the GTNL "fill the whole tank" mode). */
    public boolean isTransferLimited() {
        return getTransferRate() < tank.getTankCapacity(0);
    }

    private String rateText() {
        long rate = getTransferRate();
        if (rate >= Integer.MAX_VALUE) {
            return Component.translatable("gtna.machine.wireless_steam.transfer_rate.unlimited").getString();
        }
        return Component.translatable("gtna.machine.wireless_steam.transfer_rate",
                FormattingUtil.formatNumbers(rate)).getString();
    }

    private void updateWireless() {
        if (!ConfigHolder.INSTANCE.wirelessSteam.enabled) {
            return;
        }
        if (getLevel() instanceof ServerLevel serverLevel) {
            UUID ownerId = getOwnerUUID();
            if (ownerId == null) return;
            SteamWirelessNetworkManager.reportConnection(serverLevel, ownerId,
                    GlobalPos.of(serverLevel.dimension(), getPos()), true, isSteel);
            long currentSteam = tank.getFluidInTank(0).getAmount();
            long capacity = tank.getTankCapacity(0);
            long spaceNeeded = capacity - currentSteam;

            if (spaceNeeded > 0) {
                // GTNL tryFetchingSteam: clamp the request to what the network actually holds. The
                // network consume is all-or-nothing, so asking for the full transfer rate would make
                // the pull fail whenever the network holds less than one tick's worth — the reported
                // "input hatch shows no steam" bug.
                long networkAvailable = SteamWirelessNetworkManager.getUserSteam(serverLevel, ownerId);
                if (networkAvailable <= 0) return;
                // The whole buffer/free space is the default pull; the config rate is only an
                // optional throttle (GTNL WirelessSteamEnergyHatch has no per-tick cap).
                long request = Math.min(Math.min(spaceNeeded, getTransferRate()), networkAvailable);
                int toPull = (int) Math.min(request, Integer.MAX_VALUE);
                if (toPull <= 0) return;

                // GTNL robustness: simulate the fill first, charge the network for exactly what the
                // tank can accept, then execute the fill. Never consume more than we can store, so
                // a full/odd tank can never void steam pulled from the network.
                FluidStack requestStack = GTMaterials.Steam.getFluid(toPull);
                int accepted = tank.fill(requestStack, IFluidHandler.FluidAction.SIMULATE);
                if (accepted <= 0) return;

                if (SteamWirelessNetworkManager.consumeSteamFromGlobalMap(serverLevel, ownerId, accepted)) {
                    tank.fill(GTMaterials.Steam.getFluid(accepted), IFluidHandler.FluidAction.EXECUTE);
                }
            }
        }
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(176, 166, this, entityPlayer)
                .background(GuiTextures.BACKGROUND_STEAM.get(isSteel))
                .widget(new ImageWidget(7, 16, 81, 55, GuiTextures.DISPLAY_STEAM.get(isSteel)))
                .widget(new LabelWidget(11, 20, "gtceu.gui.fluid_amount"))
                .widget(new LabelWidget(11, 30, () -> tank.getFluidInTank(0).getAmount() + "").setTextColor(-1)
                        .setDropShadow(true))
                .widget(new LabelWidget(11, 42, this::rateText).setTextColor(-1).setDropShadow(true))
                .widget(new LabelWidget(6, 6, getBlockState().getBlock().getDescriptionId()))
                .widget(new TankWidget(tank.getStorages()[0], 90, 35, true, true)
                        .setBackground(GuiTextures.FLUID_SLOT))
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(),
                        GuiTextures.SLOT_STEAM.get(isSteel), 7, 84, true));
    }
}
