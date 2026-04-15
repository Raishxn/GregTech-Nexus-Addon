package com.raishxn.gtna;

import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;

import com.raishxn.gtna.client.ClientProxy;
import com.raishxn.gtna.common.CommonProxy;
import com.raishxn.gtna.common.data.condition.RestrictedItemsEnabledForgeCondition;
import com.raishxn.gtna.config.GTNAConfigBootstrap;
import dev.toma.configuration.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(GTNACORE.MOD_ID)
@SuppressWarnings("removal")
public class GTNACORE {

    public static final String MOD_ID = "gtna";
    public static final Logger LOGGER = LogManager.getLogger();
    public static GTRegistrate EXAMPLE_REGISTRATE = GTRegistrate.create(GTNACORE.MOD_ID);

    public GTNACORE() {
        GTNAConfigBootstrap.init();
        RestrictedItemsEnabledForgeCondition.register();
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (mc, screen) -> Configuration.getConfigScreen(MOD_ID, screen)));

        DistExecutor.unsafeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
