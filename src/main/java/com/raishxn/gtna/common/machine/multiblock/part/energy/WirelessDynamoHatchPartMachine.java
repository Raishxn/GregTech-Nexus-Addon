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
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class WirelessDynamoHatchPartMachine extends TieredIOPartMachine implements IMachineLife {

    @Persisted
    private UUID networkOwner = null;

    public final int amperage;
    public final NotifiableEnergyContainer energyContainer;

    public WirelessDynamoHatchPartMachine(IMachineBlockEntity holder, int tier, int amperage, Object... args) {
        super(holder, tier, IO.OUT);
        this.amperage = amperage;
        this.energyContainer = createEnergyContainer();
    }

    protected NotifiableEnergyContainer createEnergyContainer() {
        long tierVoltage = GTValues.V[getTier()];
        // GTNA uses 64x buffer — larger than vanilla GT as a mod differentiator
        long capacity = tierVoltage * 64L * amperage;

        NotifiableEnergyContainer container = NotifiableEnergyContainer.emitterContainer(
                this, capacity, tierVoltage, amperage);
        container.setSideOutputCondition(s -> s == getFrontFacing());
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
                player.sendSystemMessage(Component.literal("§6⚡ Wireless Dynamo Hatch §7| §aBound to: §f" + ownerName)
                        .withStyle(ChatFormatting.GOLD));
            } else {
                player.sendSystemMessage(Component.literal("§6⚡ Wireless Dynamo Hatch §7| §cNot bound")
                        .withStyle(ChatFormatting.RED));
            }
        }
        return false;
    }

    // ── Auto-bind on placement (GTMThings pattern) ──

    @Override
    public void onMachinePlaced(@Nullable LivingEntity player, ItemStack stack) {
        if (player != null) {
            setNetworkOwner(player.getUUID());
        }
    }

    // ── Wireless sync logic ──

    @Override
    public void onLoad() {
        super.onLoad();
        if (!getLevel().isClientSide) {
            if (this.networkOwner == null && getOwnerUUID() != null) {
                setNetworkOwner(getOwnerUUID());
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

            // Do not push energy into an unformed network — the matrix has not yet
            // registered a capacity, so any energy sent would be rejected by
            // NexusEnergyNetwork.addEnergy() and lost from the container.
            com.raishxn.gtna.common.data.NexusEnergyNetwork network =
                    com.raishxn.gtna.common.data.NexusEnergyNetwork.get(serverLevel);
            if (!network.isMatrixFormed(networkOwner)) return;

            long storage = container.getEnergyStored();
            long amountTransferred = 0;
            if (storage > 0) {
                long maxPushAmount = GTValues.V[getTier()] * amperage;
                long pushAmount = Math.min(storage, maxPushAmount);

                Int128 accepted = WirelessEnergyManager.addEnergy(serverLevel, networkOwner, new Int128(pushAmount));
                if (!accepted.isZero()) {
                    long actualAccepted = accepted.toLong();
                    container.removeEnergy(actualAccepted);
                    amountTransferred = actualAccepted;
                }
            }

            WirelessEnergyManager.reportConnection(serverLevel, networkOwner,
                    net.minecraft.core.GlobalPos.of(serverLevel.dimension(), getPos()),
                    true, getTier(), amperage, "Wireless Dynamo", new Int128(amountTransferred));
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
            var sp = serverLevel.getServer().getPlayerList().getPlayer(uuid);
            if (sp != null) return sp.getName().getString();
        }
        return uuid.toString().substring(0, 8) + "...";
    }
}
