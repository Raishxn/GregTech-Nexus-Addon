package com.raishxn.gtna.config;

import dev.toma.configuration.Configuration;
import dev.toma.configuration.config.Config;
import dev.toma.configuration.config.Configurable;
import dev.toma.configuration.config.Configurable.Comment;
import dev.toma.configuration.config.Configurable.Range;
import dev.toma.configuration.config.format.ConfigFormats;

@Config(id = "gtna")
public class ConfigHolder {
    public static ConfigHolder INSTANCE;
    private static final Object LOCK = new Object();

    @Configurable
    @Comment({"En: Disable Drift", "Pt: Desativar Drift"})
    public boolean disableDrift = true;

    @Configurable
    @Range(min = 1, max = 100) // Corrigido: 1.0 -> 1, 100.0 -> 100
    @Comment("En: Energy Cost Multiplier for Accelerate Hatch.")
    public double accelerateHatchEnergyCost = 1.5;

    @Configurable
    @Range(min = 1000, max = 1000000)
    public int wirelessSteamTransferRate = 8192;

    // --- Mega Solar Boiler ---
    @Configurable
    @Range(min = 1, max = 100000)
    @Comment("En: Steam produced per sunlit block per operation.")
    public int megaSolarSteamPerBlock = 500;

    // --- Void Miner Steam Gate Aged ---

    // Tier 1: Dense Supercritical Steam
    @Configurable
    @Range(min = 1, max = 64)
    @Comment("En: Output Multiplier for Dense Steam.")
    public int voidMinerDenseOutputMult = 2;

    @Configurable
    @Range(min = 1, max = 128) // Corrigido: Removido decimais
    @Comment("En: Speed Multiplier for Dense Steam (e.g. 2.0 = 2x faster).")
    public double voidMinerDenseSpeedMult = 2.0;

    @Configurable
    @Range(min = 1, max = 128) // Corrigido
    @Comment("En: Energy Cost Multiplier for Dense Steam.")
    public double voidMinerDenseEnergyMult = 1.5;

    // Tier 2: SuperHeated Steam
    @Configurable
    @Range(min = 1, max = 64)
    @Comment("En: Output Multiplier for SuperHeated Steam.")
    public int voidMinerSuperHeatedOutputMult = 3;

    @Configurable
    @Range(min = 1, max = 128) // Corrigido
    @Comment("En: Speed Multiplier for SuperHeated Steam.")
    public double voidMinerSuperHeatedSpeedMult = 3.0;

    @Configurable
    @Range(min = 1, max = 128) // Corrigido
    @Comment("En: Energy Cost Multiplier for SuperHeated Steam.")
    public double voidMinerSuperHeatedEnergyMult = 2.0;

    // Tier 3: Insanely Supercritical Steam
    @Configurable
    @Range(min = 1, max = 64)
    @Comment("En: Output Multiplier for Insanely Steam.")
    public int voidMinerInsanelyOutputMult = 5;

    @Configurable
    @Range(min = 1, max = 128) // Corrigido
    @Comment("En: Speed Multiplier for Insanely Steam.")
    public double voidMinerInsanelySpeedMult = 5.0;

    @Configurable
    @Range(min = 1, max = 128) // Corrigido
    @Comment("En: Energy Cost Multiplier for Insanely Steam.")
    public double voidMinerInsanelyEnergyMult = 4.0;

    public static void init() {
        synchronized(LOCK) {
            if (INSTANCE == null) {
                INSTANCE = (ConfigHolder)Configuration.registerConfig(ConfigHolder.class, ConfigFormats.yaml()).getConfigInstance();
            }
        }
    }
}