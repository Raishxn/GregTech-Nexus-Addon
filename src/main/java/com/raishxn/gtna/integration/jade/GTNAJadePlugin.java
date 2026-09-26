package com.raishxn.gtna.integration.jade;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.raishxn.gtna.integration.jade.provider.GTNABlockStatsProvider;
import com.raishxn.gtna.integration.jade.provider.GTNAGTOStatusProvider;
import com.raishxn.gtna.integration.jade.provider.GTNAIsaMillProvider;
import com.raishxn.gtna.integration.jade.provider.GTNAMultipleRecipesProvider;
import com.raishxn.gtna.integration.jade.provider.GTNAPatternBufferProvider;
import com.raishxn.gtna.integration.jade.provider.GTNASolarBoilerProvider;
import com.raishxn.gtna.integration.jade.provider.GTNAWirelessSteamProvider;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class GTNAJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(GTNAMultipleRecipesProvider.INSTANCE, BlockEntity.class);
        registration.registerBlockDataProvider(GTNAIsaMillProvider.INSTANCE, BlockEntity.class);
        registration.registerBlockDataProvider(GTNAGTOStatusProvider.INSTANCE, BlockEntity.class);
        registration.registerBlockDataProvider(GTNAPatternBufferProvider.INSTANCE, BlockEntity.class);
        registration.registerBlockDataProvider(GTNASolarBoilerProvider.INSTANCE, BlockEntity.class);
        registration.registerBlockDataProvider(GTNAWirelessSteamProvider.INSTANCE, BlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(GTNABlockStatsProvider.INSTANCE, Block.class);
        registration.registerBlockComponent(GTNAMultipleRecipesProvider.INSTANCE, Block.class);
        registration.registerBlockComponent(GTNAIsaMillProvider.INSTANCE, Block.class);
        registration.registerBlockComponent(GTNAGTOStatusProvider.INSTANCE, Block.class);
        registration.registerBlockComponent(GTNAPatternBufferProvider.INSTANCE, Block.class);
        registration.registerBlockComponent(GTNASolarBoilerProvider.INSTANCE, Block.class);
        registration.registerBlockComponent(GTNAWirelessSteamProvider.INSTANCE, Block.class);
    }
}
