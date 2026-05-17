package com.raishxn.gtna.common.machine.multiblock.part.energy;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

import com.raishxn.gtna.api.capability.WirelessEnergyManager;
import com.raishxn.gtna.utils.datastructure.Int128;
import com.raishxn.gtna.utils.GTNANetworkIdentityUtil;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class WirelessEnergyHatchPartMachine extends TieredIOPartMachine implements IMachineLife {

    @Persisted
    private UUID networkOwner = null;

    public final int amperage;
    public final NotifiableEnergyContainer energyContainer;

    public WirelessEnergyHatchPartMachine(IMachineBlockEntity holder, int tier, int amperage, Object... args) {
        super(holder, tier, IO.IN);
        this.amperage = amperage;
        this.energyContainer = createEnergyContainer();
    }

    protected NotifiableEnergyContainer createEnergyContainer() {
        long tierVoltage = GTValues.V[getTier()];
        // GTNA uses 64x buffer — larger than vanilla GT (16x) as a mod differentiator
        long capacity = tierVoltage * 64L * amperage;

        NotifiableEnergyContainer container = NotifiableEnergyContainer.receiverContainer(
                this, capacity, tierVoltage, amperage);
        container.setSideInputCondition(s -> s == getFrontFacing());
        container.setCapabilityValidator(s -> s == null || s == getFrontFacing());
        return container;
    }

    // ── Show binding info when player right-clicks ──

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        if (!player.level().isClientSide) {
            UUID owner = getNetworkOwner();
            if (owner != null) {
                String ownerName = resolvePlayerName(owner);
                player.sendSystemMessage(Component.literal("§b⚡ Wireless Energy Hatch §7| §aBound to: §f" + ownerName)
                        .withStyle(ChatFormatting.AQUA));
            } else {
                player.sendSystemMessage(Component.literal("§b⚡ Wireless Energy Hatch §7| §cNot bound")
                        .withStyle(ChatFormatting.RED));
            }
        }
        return false;
    }

    // ── Auto-bind on placement (GTMThings pattern) ──

    @Override
    public void onMachinePlaced(@Nullable LivingEntity player, ItemStack stack) {
        if (player != null) {
            // Use FTB Teams party UUID if the player belongs to one, else player UUID
            setNetworkOwner(GTNANetworkIdentityUtil.resolveNetworkId(player.getUUID()));
        }
    }

    // ── Wireless sync logic ──

    @Override
    public void onLoad() {
        super.onLoad();
        if (!getLevel().isClientSide) {
            if (this.networkOwner == null && getOwnerUUID() != null) {
                setNetworkOwner(GTNANetworkIdentityUtil.resolveNetworkId(getOwnerUUID()));
            }
            this.subscribeServerTick(this::updateWireless);
        }
    }

    private void updateWireless() {
        if (getLevel() instanceof ServerLevel serverLevel) {
            if (getNetworkOwner() == null) setNetworkOwner(getOwnerUUID());
            if (networkOwner == null) return;

            NotifiableEnergyContainer container = this.energyContainer;
            if (container == null) return;

            // Do not pull energy from a network with no active matrix -- the
            // matrix is MISSING/OFFLINE so the network holds no usable energy.
            com.raishxn.gtna.common.data.NexusEnergyNetwork network =
                    com.raishxn.gtna.common.data.NexusEnergyNetwork.get(serverLevel);
            if (!network.isMatrixFormed(networkOwner)) return;

            long storage = container.getEnergyStored();
            long maxCapacity = container.getEnergyCapacity();
            long deficit = maxCapacity - storage;
            long amountTransferred = 0;

            if (deficit > 0) {
                long maxPullAmount = GTValues.V[getTier()] * amperage;
                long pullAmount = Math.min(deficit, maxPullAmount);

                // consumeEnergy() enforces the transfer limit internally and returns
                // the actual EU consumed (ZERO on failure/empty/safe-mode).
                // We credit the container with exactly what was returned — never the
                // raw requested amount — so no EU is created from nothing.
                Int128 consumed = WirelessEnergyManager.consumeEnergy(serverLevel, networkOwner,
                        new Int128(pullAmount));
                if (!consumed.isZero()) {
                    long consumedLong = consumed.toLong();
                    container.addEnergy(consumedLong);
                    amountTransferred = consumedLong;
                }
            }

            WirelessEnergyManager.reportConnection(serverLevel, networkOwner,
                    net.minecraft.core.GlobalPos.of(serverLevel.dimension(), getPos()),
                    false, getTier(), amperage, "Wireless Hatch", new Int128(amountTransferred));
        }
    }

    // ── Tint for voltage color ──

    @Override
    public int tintColor(int index) {
        if (index == 2) {
            return GTValues.VC[getTier()];
        }
        return super.tintColor(index);
    }

    // ── Getters/Setters ──

    public void setNetworkOwner(UUID uuid) {
        this.networkOwner = uuid;
    }

    public UUID getNetworkOwner() {
        return networkOwner != null ? networkOwner : getOwnerUUID();
    }

    /**
     * Resolve a UUID to a player name. Falls back to abbreviated UUID if offline.
     */
    private String resolvePlayerName(UUID uuid) {
        if (getLevel() instanceof ServerLevel serverLevel) {
            Player p = serverLevel.getPlayerByUUID(uuid);
            if (p != null) return p.getName().getString();
            // Try server-wide
            var sp = serverLevel.getServer().getPlayerList().getPlayer(uuid);
            if (sp != null) return sp.getName().getString();
        }
        return uuid.toString().substring(0, 8) + "...";
    }
}
