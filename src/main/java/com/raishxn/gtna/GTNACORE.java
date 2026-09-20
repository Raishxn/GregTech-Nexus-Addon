package com.raishxn.gtna;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

import com.raishxn.gtna.client.ClientProxy;
import com.raishxn.gtna.common.CommonProxy;
import com.raishxn.gtna.common.data.condition.RestrictedItemsEnabledForgeCondition;
import com.raishxn.gtna.config.GTNAConfigBootstrap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(GTNACORE.MOD_ID)
public class GTNACORE {

    public static final String MOD_ID = "gtna";
    public static final Logger LOGGER = LogManager.getLogger();

    public GTNACORE() {
        GTNAConfigBootstrap.init();
        RestrictedItemsEnabledForgeCondition.register();
        // NOTE: the config screen factory is registered in ClientProxy, NOT here. ConfigScreenHandler
        // is client-only, and referencing it from this class (even inside a lambda) makes
        // RuntimeDistCleaner reject net.minecraft.client.Minecraft on a dedicated server, which
        // crashed mod loading outright.
        DistExecutor.unsafeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
