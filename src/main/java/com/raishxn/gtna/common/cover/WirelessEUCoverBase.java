package com.raishxn.gtna.common.cover;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.WorkableTieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import com.raishxn.gtna.common.data.NexusEnergyNetwork;
import com.raishxn.gtna.utils.GTNANetworkIdentityUtil;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Shared base class for WirelessEUReceiverCover and WirelessEUTransmitterCover.
 *
 * Stores a persisted network UUID, auto-binds to the player's FTB Teams party
 * UUID (or player UUID fallback) on placement, and manages the server tick
 * subscription lifecycle.
 */
public abstract class WirelessEUCoverBase extends CoverBehavior {

    @Persisted
    @DescSynced
    @Nullable
    private UUID networkOwner = null;

    public final int tier;
    public final int amperage;

    @Nullable
    protected TickableSubscription subscription;

    protected WirelessEUCoverBase(CoverDefinition definition, ICoverable coverHolder,
                                   Direction attachedSide, int tier, int amperage) {
        super(definition, coverHolder, attachedSide);
        this.tier = tier;
        this.amperage = amperage;
    }

    // ── canAttach ─────────────────────────────────────────────────────────────

    @Override
    public boolean canAttach() {
        if (!super.canAttach()) return false;

        MetaMachine machine = getMachine();
        if (!(machine instanceof WorkableTieredMachine wm)) return false;
        if (machine instanceof IMultiController) return false;
        if (wm.getTier() < this.tier) return false;

        IEnergyContainer container = getEnergyContainer();
        if (container == null) return false;

        return canAttachForDirection(container);
    }

    /**
     * Direction-specific attach check (and duplicate-cover guard) implemented
     * by each subclass.
     */
    protected abstract boolean canAttachForDirection(IEnergyContainer container);

    /**
     * Returns true if a cover of the given type is already installed on this
     * machine (any face), used to prevent duplicate-direction stacking.
     * Uses ICoverable.getCovers() which is defined directly on the interface.
     */
    protected boolean alreadyHasCoverOfType(Class<? extends WirelessEUCoverBase> type) {
        for (CoverBehavior cover : coverHolder.getCovers()) {
            if (type.isInstance(cover)) return true;
        }
        return false;
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    public void onAttached(ItemStack itemStack, ServerPlayer player) {
        super.onAttached(itemStack, player);
        if (player != null && this.networkOwner == null) {
            this.networkOwner = GTNANetworkIdentityUtil.resolveNetworkId(player.getUUID());
        }
        updateSubscription();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        updateSubscription();
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        if (subscription != null) {
            subscription.unsubscribe();
            subscription = null;
        }
    }

    protected void updateSubscription() {
        subscription = coverHolder.subscribeServerTick(subscription, this::onServerTick);
    }

    protected abstract void onServerTick();

    // ── Network helpers ───────────────────────────────────────────────────────

    @Nullable
    public UUID getNetworkOwner() {
        if (networkOwner != null) return networkOwner;
        MetaMachine m = getMachine();
        return (m != null) ? m.getOwnerUUID() : null;
    }

    public void setNetworkOwner(@Nullable UUID uuid) {
        this.networkOwner = uuid;
    }

    protected boolean isNetworkReady(ServerLevel level) {
        UUID id = getNetworkOwner();
        if (id == null) return false;
        return NexusEnergyNetwork.get(level).isMatrixFormed(id);
    }

    // ── Machine / container helpers ───────────────────────────────────────────

    @Nullable
    protected MetaMachine getMachine() {
        return MetaMachine.getMachine(coverHolder.getLevel(), coverHolder.getPos());
    }

    @Nullable
    protected IEnergyContainer getEnergyContainer() {
        return GTCapabilityHelper.getEnergyContainer(
                coverHolder.getLevel(), coverHolder.getPos(), attachedSide);
    }

    protected GlobalPos getGlobalPos(ServerLevel level) {
        return GlobalPos.of(level.dimension(), coverHolder.getPos());
    }

    protected long maxEnergyPerTick() {
        return GTValues.V[tier] * (long) amperage;
    }

    public static Component rateComponent(int tier, int amperage) {
        long euPerTick = GTValues.V[tier] * (long) amperage;
        return Component.literal(amperage + " A @ " + GTValues.VN[tier] + " / "
                + String.format("%,d", euPerTick) + " EU/t")
                .withStyle(ChatFormatting.YELLOW);
    }
}
