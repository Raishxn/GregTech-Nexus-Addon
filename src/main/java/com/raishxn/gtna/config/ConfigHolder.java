package com.raishxn.gtna.config;

import dev.toma.configuration.Configuration;
import dev.toma.configuration.config.Config;
import dev.toma.configuration.config.Configurable;
import dev.toma.configuration.config.Configurable.Range;
import dev.toma.configuration.config.format.ConfigFormats;

@Config(
        id = "gtna"
)
public class ConfigHolder {
    public static ConfigHolder INSTANCE;
    private static final Object LOCK = new Object();
    @Configurable
    public boolean disableDrift = true;
    @Configurable
    public int oreMultiplier = 4;
    @Configurable
    @Range(
            min = 1L
    )
    public double durationMultiplier = (double)1.0F;
    @Configurable
    @Range(
            min = 1L
    )
    public boolean enablePrimitiveVoidOre = false;
    public static void init() {
        synchronized(LOCK) {
            if (INSTANCE == null) {
                INSTANCE = (ConfigHolder)Configuration.registerConfig(ConfigHolder.class, ConfigFormats.yaml()).getConfigInstance();
            }

        }
    }
}
