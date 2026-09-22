package com.raishxn.gtna.integration.jade.provider;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.SteamHatchPartMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

import com.raishxn.gtna.api.capability.SteamWirelessNetworkManager;
import com.raishxn.gtna.common.machine.multiblock.part.steam.WirelessSteamInputHatch;
import com.raishxn.gtna.common.machine.multiblock.part.steam.WirelessSteamOutputHatch;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.UUID;

/**
 * Jade/Waila readout for the wireless steam hatches (GTNL {@code getWailaNBTData} parity: the
 * reference shows the network balance in WAILA).
 *
 * <p>
 * Shows the owner's pool balance, this hatch's tank level and its last transfer, so a pool stuck
 * at 0 mB can be told apart from a hatch that is not moving steam.
 */
public class GTNAWirelessSteamProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    public static final GTNAWirelessSteamProvider INSTANCE = new GTNAWirelessSteamProvider();
    private static final ResourceLocation UID = new ResourceLocation("gtna", "wireless_steam_network");

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (!(accessor.getLevel() instanceof ServerLevel serverLevel)) return;
        if (!(accessor.getBlockEntity() instanceof MetaMachineBlockEntity metaBlockEntity)) return;
        MetaMachine machine = metaBlockEntity.getMetaMachine();
        if (!(machine instanceof WirelessSteamInputHatch) && !(machine instanceof WirelessSteamOutputHatch)) {
            return;
        }

        UUID owner = machine.getOwnerUUID();
        if (owner == null) return;
        SteamHatchPartMachine hatch = (SteamHatchPartMachine) machine;
        long lastTransfer = machine instanceof WirelessSteamInputHatch input ?
                input.getLastTransferAmount() : ((WirelessSteamOutputHatch) machine).getLastTransferAmount();

        data.putLong("Balance", SteamWirelessNetworkManager.getUserSteam(serverLevel, owner));
        data.putLong("TankAmount", hatch.tank.getFluidInTank(0).getAmount());
        data.putLong("TankCapacity", hatch.tank.getTankCapacity(0));
        data.putLong("LastTransfer", lastTransfer);
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (!data.contains("Balance")) {
            return;
        }
        long balance = data.getLong("Balance");
        tooltip.add(Component.translatable("gtna.jade.wireless_steam.balance",
                FormattingUtil.formatNumbers(balance))
                .withStyle(balance > 0 ? ChatFormatting.AQUA : ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("gtna.jade.wireless_steam.tank",
                FormattingUtil.formatNumbers(data.getLong("TankAmount")),
                FormattingUtil.formatNumbers(data.getLong("TankCapacity")))
                .withStyle(ChatFormatting.GRAY));

        long lastTransfer = data.getLong("LastTransfer");
        if (lastTransfer > 0) {
            tooltip.add(Component.translatable("gtna.jade.wireless_steam.last_push",
                    FormattingUtil.formatNumbers(lastTransfer)).withStyle(ChatFormatting.GREEN));
        } else if (lastTransfer < 0) {
            tooltip.add(Component.translatable("gtna.jade.wireless_steam.last_pull",
                    FormattingUtil.formatNumbers(-lastTransfer)).withStyle(ChatFormatting.BLUE));
        } else {
            tooltip.add(Component.translatable("gtna.jade.wireless_steam.idle")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
