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
 * Wireless steam input hatch: keeps its tank topped up from the placer's global steam pool.
 *
 * <p>
 * GTNL {@code WirelessSteamEnergyHatch} parity: every tick it clamps the pull to its free space,
 * its configured rate and the network balance, simulates the fill, charges the network for exactly
 * what the tank accepts and only then executes the fill — so a full tank can never void steam.
 *
 * <p>
 * One deliberate robustness deviation from the reference: the pull is additionally capped to the
 * hatch's <b>fair share</b> of the balance ({@code ceil(balance / inputs-with-space)}). Without it
 * every hatch asks for the whole balance and the first one in tick order drains it, so with a bank
 * of hatches (the reported 24-input setup) one hatch hoards everything, the pool always reads 0 mB
 * and every other machine starves. With the share, all inputs are served every tick.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WirelessSteamInputHatch extends SteamHatchPartMachine {

    private final long transferRate;
    private final boolean isSteel;
    private long lastTransferAmount;
    private long lastTransferTick = -1L;

    public WirelessSteamInputHatch(IMachineBlockEntity holder, boolean isSteel, Object... args) {
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
        // Never auto-import from adjacent fluid handlers: the wireless network is the only source.
    }

    @Override
    protected void updateTankSubscription(Direction newFacing) {
        // Same as above; onRotated() calls this overload.
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel serverLevel) {
            // Pre-register a loaded hatch so the fair-share denominator sees the whole bank before
            // the first tick; otherwise the first hatch in tick order could still take the pool.
            reportTank(serverLevel);
            this.subscribeServerTick(this::updateWireless);
        }
    }

    /** Registers this hatch with the inspection registry, including its current tank level. */
    private SteamNetworkData.ConnectionInfo reportTank(ServerLevel serverLevel) {
        UUID ownerId = getOwnerUUID();
        if (ownerId == null) return null;
        SteamNetworkData.ConnectionInfo info = SteamWirelessNetworkManager.reportConnection(serverLevel, ownerId,
                GlobalPos.of(serverLevel.dimension(), getPos()), true, isSteel);
        if (info != null) {
            info.tankAmount = tank.getFluidInTank(0).getAmount();
            info.tankCapacity = tank.getTankCapacity(0);
        }
        return info;
    }

    @Override
    protected NotifiableFluidTank createTank(int initialCapacity, int slots, Object... args) {
        boolean steel = args.length > 0 && args[0] instanceof Boolean value && value;
        int configuredCapacity = steel ? ConfigHolder.INSTANCE.wirelessSteam.steelBuffer :
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

    /** Signed mB moved by the last transfer: negative = pulled from the network, 0 = none yet. */
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
            long capacity = tank.getTankCapacity(0);
            long spaceNeeded = capacity - currentSteam;
            if (spaceNeeded <= 0) return;

            // GTNL tryFetchingSteam: clamp the request to what the network actually holds. The
            // network consume is all-or-nothing, so asking for the full transfer rate would make
            // the pull fail whenever the network holds less than one tick's worth — the reported
            // "input hatch shows no steam" bug.
            long networkAvailable = SteamWirelessNetworkManager.getUserSteam(serverLevel, ownerId);
            if (networkAvailable <= 0) return;
            // Fair share: never drain the whole pool while other inputs still have space, or the
            // first hatch in tick order monopolises the network and every other machine starves.
            int pullingInputs = Math.max(1,
                    SteamWirelessNetworkManager.getActiveInputCount(serverLevel, ownerId));
            long fairShare = (networkAvailable + pullingInputs - 1) / pullingInputs;
            // The whole buffer/free space is the default pull; the config rate is only an optional
            // throttle (GTNL WirelessSteamEnergyHatch has no per-tick cap).
            long request = Math.min(Math.min(spaceNeeded, getTransferRate()),
                    Math.min(fairShare, networkAvailable));
            int toPull = (int) Math.min(request, Integer.MAX_VALUE);
            if (toPull <= 0) return;

            // GTNL robustness: simulate the fill first, charge the network for exactly what the
            // tank can accept, then execute the fill. Never consume more than we can store, so a
            // full/odd tank can never void steam pulled from the network.
            FluidStack requestStack = GTMaterials.Steam.getFluid(toPull);
            int accepted = tank.fill(requestStack, IFluidHandler.FluidAction.SIMULATE);
            if (accepted <= 0) return;

            if (SteamWirelessNetworkManager.consumeSteamFromGlobalMap(serverLevel, ownerId, accepted)) {
                tank.fill(GTMaterials.Steam.getFluid(accepted), IFluidHandler.FluidAction.EXECUTE);
                lastTransferAmount = -accepted;
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
