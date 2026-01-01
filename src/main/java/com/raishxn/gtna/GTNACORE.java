package com.raishxn.gtna;


import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialEvent;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.raishxn.gtna.client.ClientProxy;
import com.raishxn.gtna.common.data.GTNAElements;
import com.raishxn.gtna.common.data.GTNAMaterials;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import com.raishxn.gtna.common.CommonProxy;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(GTNACORE.MOD_ID)
@SuppressWarnings("removal")
public class GTNACORE {

    public static final String MOD_ID = "gtna";
    public static final Logger LOGGER = LogManager.getLogger();
    public static GTRegistrate EXAMPLE_REGISTRATE = GTRegistrate.create(GTNACORE.MOD_ID);

    public GTNACORE() {
        DistExecutor.unsafeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
        MinecraftForge.EVENT_BUS.register(this);
    }
    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }


    }