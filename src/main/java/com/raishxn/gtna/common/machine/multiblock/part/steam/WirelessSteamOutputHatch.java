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
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import com.raishxn.gtna.api.capability.SteamWirelessNetworkManager;
import com.raishxn.gtna.common.data.SteamNetworkData;
import com.raishxn.gtna.config.ConfigHolder;

import java.util.UUID;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Wireless steam output hatch: moves its whole tank into the placer's global steam pool every tick.
 *
 * <p>
 * GTNL {@code WirelessSteamDynamoHatch} parity: no per-tick cap by default (the config rate is an
 * optional throttle), simulate-drain → add exactly the drained amount → execute-drain, so the
 * network can never receive more than the tank gave up and the hatch can never duplicate steam.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WirelessSteamOutputHatch extends SteamHatchPartMachine {

    private final long transferRate;
    private final boolean isSteel;
    private long lastTransferAmount;
    private long lastTransferTick = -1L;

    public WirelessSteamOutputHatch(IMachineBlockEntity holder, boolean isSteel, Object... args) {
        // Pass the tier down through the varargs so createTank() (called from the super constructor)
        // can size the tank correctly before any field of this class is assigned.
        super(holder, withSteel(isSteel, args));
        this.isSteel = isSteel;
        this.transferRate = isSteel ? ConfigHolder.INSTANCE.wirelessSteam.steelTransferRate :
                ConfigHolder.INSTANCE.wirelessSteam.bronzeTransferRate;
    }

    private static Object[] withSteel(boolean isSteel, Object... args) {
        Object[] all = new Object[args.length + 1];
        all[0] = isSteel;
        System.arraycopy(args, 0, all, 1, args.length);
        return all;
    }

    @Override
    public boolean isWorkingEnabled() {
        // The hatch is wireless-only and has no AUTO IO (see the updateTankSubscription overrides);
        // the GTCEu workingEnabled field is the AUTO IO switch, not the network state, so mirroring
        // it here made Jade report "Working Disabled" for a hatch that is working fine.
        return ConfigHolder.INSTANCE.wirelessSteam.enabled;
    }

    @Override
    protected void updateTankSubscription() {
        // Never auto-export to adjacent fluid handlers: the wireless network is the only sink.
    }

    @Override
    protected void updateTankSubscription(Direction newFacing) {
        // Same as above; onRotated() calls this overload.
    }

    @Override
    protected NotifiableFluidTank createTank(int initialCapacity, int slots, Object... args) {
        boolean steel = args.length > 0 && args[0] instanceof Boolean value && value;
        int configuredCapacity = steel ? ConfigHolder.INSTANCE.wirelessSteam.steelBuffer :
                ConfigHolder.INSTANCE.wirelessSteam.bronzeBuffer;
        return new NotifiableFluidTank(this, 1, configuredCapacity, IO.OUT)
                .setFilter(fluidStack -> fluidStack.getFluid().is(GTMaterials.Steam.getFluidTag()));
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel serverLevel) {
            reportTank(serverLevel);
            this.subscribeServerTick(this::updateWireless);
        }
    }

    /** Registers this hatch with the inspection registry, including its current tank level. */
    private SteamNetworkData.ConnectionInfo reportTank(ServerLevel serverLevel) {
        UUID ownerId = getOwnerUUID();
        if (ownerId == null) return null;
        SteamNetworkData.ConnectionInfo info = SteamWirelessNetworkManager.reportConnection(serverLevel, ownerId,
                GlobalPos.of(serverLevel.dimension(), getPos()), false, isSteel);
        if (info != null) {
            info.tankAmount = tank.getFluidInTank(0).getAmount();
            info.tankCapacity = tank.getTankCapacity(0);
        }
        return info;
    }

    /** The configured per-tick cap for this hatch; {@link Integer#MAX_VALUE} means "whole buffer". */
    public long getTransferRate() {
        return transferRate <= 0 ? Integer.MAX_VALUE : transferRate;
    }

    /** Whether this hatch throttles below its buffer (false = the GTNL "move the whole tank" mode). */
    public boolean isTransferLimited() {
        return getTransferRate() < tank.getTankCapacity(0);
    }

    /** Signed mB moved by the last transfer: positive = pushed to the network, 0 = none yet. */
    public long getLastTransferAmount() {
        return lastTransferAmount;
    }

    /** Game tick of the last transfer, or {@code -1} when the hatch has not moved anything yet. */
    public long getLastTransferTick() {
        return lastTransferTick;
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
            SteamNetworkData.ConnectionInfo info = reportTank(serverLevel);

            long currentSteam = tank.getFluidInTank(0).getAmount();
            if (currentSteam <= 0) return;

            // GTNL WirelessSteamDynamoHatch parity: the whole tank moves into the network every
            // tick. The config rate is only an optional throttle and defaults to the buffer size,
            // so a boiler can never strand a recipe cycle behind a cap smaller than the tank.
            long limit = getTransferRate();
            int toPush = (int) Math.min(currentSteam, Math.min(limit, Integer.MAX_VALUE));
            if (toPush <= 0) return;

            // GTNL robustness: simulate the drain first so the network only receives what the
            // tank can actually give up. The drain result is what gets added, never a guess.
            FluidStack simulated = tank.drain(toPush, IFluidHandler.FluidAction.SIMULATE);
            int amount = simulated.getAmount();
            if (amount <= 0) return;

            if (SteamWirelessNetworkManager.addSteamToGlobalSteamMap(serverLevel, ownerId, amount)) {
                tank.drain(amount, IFluidHandler.FluidAction.EXECUTE);
                lastTransferAmount = amount;
                lastTransferTick = serverLevel.getGameTime();
                if (info != null) {
                    info.tankAmount = tank.getFluidInTank(0).getAmount();
                    info.lastTransferAmount = lastTransferAmount;
                    info.lastTransferTick = lastTransferTick;
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
