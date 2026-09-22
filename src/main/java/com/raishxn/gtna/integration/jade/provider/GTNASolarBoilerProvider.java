package com.raishxn.gtna.integration.jade.provider;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.raishxn.gtna.common.machine.multiblock.steam.LargeSteamSolarBoilerMachine;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/**
 * Jade/Waila readout for the Large Steam Solar Boiler.
 *
 * <p>
 * GTCEu's stock {@code RecipeOutputProvider} shows the current recipe's output per craft — for the
 * solar boiler that is {@code sunlit * 200 mB} (e.g. {@code 312 B} for a 41x42 field), which reads
 * like a tiny production number even though it is dumped every 20-tick cycle. This provider shows the
 * boiler's actual production rate (mB/s) and its sunlit cell count so the Jade line matches the
 * machine display.
 */
public class GTNASolarBoilerProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    public static final GTNASolarBoilerProvider INSTANCE = new GTNASolarBoilerProvider();
    private static final ResourceLocation UID = new ResourceLocation("gtna", "solar_boiler_provider");

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof MetaMachineBlockEntity metaBlockEntity &&
                metaBlockEntity.getMetaMachine() instanceof LargeSteamSolarBoilerMachine boiler) {
            data.putInt("SunlitCells", boiler.getSunlitCells());
            data.putLong("SteamPerSecond", boiler.getSteamPerSecond());
            data.putLong("SteamPerCycle", boiler.getSteamPerCycle());
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (!data.contains("SteamPerSecond")) {
            return;
        }
        int sunlit = data.getInt("SunlitCells");
        long perSecond = data.getLong("SteamPerSecond");
        tooltip.add(Component.translatable("gtna.machine.large_steam_solar_boiler.sunlit",
                FormattingUtil.formatNumbers(sunlit)).withStyle(ChatFormatting.GRAY));
        if (perSecond > 0) {
            tooltip.add(Component.translatable("gtna.machine.large_steam_solar_boiler.production",
                    FormattingUtil.formatNumbers(perSecond)).withStyle(ChatFormatting.AQUA));
        } else {
            tooltip.add(Component.translatable("gtna.machine.large_steam_solar_boiler.idle")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
