package com.raishxn.gtna.config;

import com.gregtechceu.gtceu.api.GTValues;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.raishxn.gtna.GTNACORE;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Pack-owned fluid/origin programs. Loaded at startup, before machine and recipe registration. */
public final class VoidFluidDrillConfig {

    public static final class Program {

        public String id;
        public String fluid;
        public String dimension;
        public String remoteUpgrade = "terrestrial";
        public int minimumTier = GTValues.EV;
        public int duration = 400;
        public long euPerTick = 1920;
        public int drillingFluid = 100;
        public int nitrogen = 0;
        public int filters = 0;
        public int output = 2000;

        public Program() {}

        public Program(String id, String fluid, int output) {
            this.id = id;
            this.fluid = "gtceu:" + fluid;
            this.dimension = "minecraft:overworld";
            this.output = output;
        }
    }

    public static final class Upgrade {

        public int tier;
        public List<String> researchItems = new ArrayList<>();

        public Upgrade() {}

        public Upgrade(int tier, String... items) {
            this.tier = tier;
            this.researchItems.addAll(List.of(items));
        }
    }

    public boolean enabled = true;
    public int minimumTier = GTValues.EV;
    public int controllerRigTier = GTValues.MV;
    public int controllerSensorTier = GTValues.HV;
    public int maxOutputPerOperation = 1_000_000;
    public Map<String, Integer> maxParallelByTier = new LinkedHashMap<>();
    public List<Program> programs = new ArrayList<>();
    public Map<String, Upgrade> upgrades = new LinkedHashMap<>();
    private static VoidFluidDrillConfig instance = defaults();

    public static VoidFluidDrillConfig get() {
        return instance;
    }

    public static VoidFluidDrillConfig defaults() {
        VoidFluidDrillConfig config = new VoidFluidDrillConfig();
        for (int tier = GTValues.EV; tier <= GTValues.MAX; tier++) {
            config.maxParallelByTier.put(GTValues.VN[tier], 1 << Math.min(tier - GTValues.EV, 10));
        }
        config.programs.add(new Program("oil", "oil", 2000));
        config.programs.add(new Program("heavy_oil", "oil_heavy", 1500));
        config.programs.add(new Program("light_oil", "oil_light", 2500));
        Program gas = new Program("natural_gas", "natural_gas", 1500);
        gas.nitrogen = 100;
        config.programs.add(gas);
        config.programs.add(new Program("salt_water", "salt_water", 4000));
        config.programs.add(new Program("raw_oil", "oil_medium", 2000));
        config.upgrades.put("terrestrial", new Upgrade(GTValues.IV, "gtna:overworld_data"));
        config.upgrades.put("t1", new Upgrade(GTValues.IV, "gtna:planet_data_chip_moon"));
        config.upgrades.put("t2", new Upgrade(GTValues.LuV, "gtna:planet_data_chip_mars"));
        config.upgrades.put("t3",
                new Upgrade(GTValues.LuV, "gtna:planet_data_chip_venus", "gtna:planet_data_chip_mercury"));
        // Later planets are supplied by the pack. Empty research lists disable their crafts.
        config.upgrades.put("t4", new Upgrade(GTValues.LuV));
        config.upgrades.put("t5", new Upgrade(GTValues.ZPM));
        return config;
    }

    public static void init() {
        var path = FMLPaths.CONFIGDIR.get().resolve("gtna/balance/void_fluid_drill.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            Files.createDirectories(path.getParent());
            if (Files.notExists(path)) {
                Files.writeString(path, gson.toJson(instance));
            } else {
                try (var reader = Files.newBufferedReader(path)) {
                    VoidFluidDrillConfig loaded = gson.fromJson(reader, VoidFluidDrillConfig.class);
                    if (loaded == null) throw new IllegalArgumentException("Empty config");
                    loaded.validate();
                    instance = loaded;
                }
            }
        } catch (Exception exception) {
            // Fail closed: malformed configuration must not silently unlock another profile.
            throw new IllegalStateException("Invalid Void Fluid Drill config: " + path, exception);
        }
        instance.validate();
        GTNACORE.LOGGER.info("Void Fluid Drill: {} programs, enabled={}", instance.programs.size(), instance.enabled);
    }

    public void validate() {
        if (minimumTier < GTValues.EV || minimumTier > GTValues.MAX ||
                controllerRigTier < GTValues.MV || controllerRigTier > GTValues.EV ||
                controllerSensorTier < GTValues.HV || controllerSensorTier > GTValues.EV ||
                maxOutputPerOperation < 1 || maxOutputPerOperation > 1_000_000 ||
                programs == null || upgrades == null || maxParallelByTier == null) {
            throw new IllegalArgumentException("Invalid machine limits");
        }
        var ids = new java.util.HashSet<String>();
        var origins = new java.util.HashSet<String>();
        for (Program p : programs) {
            if (p == null || p.id == null || !p.id.matches("[a-z0-9_]+") || !ids.add(p.id) ||
                    ResourceLocation.tryParse(p.fluid == null ? "" : p.fluid) == null ||
                    ResourceLocation.tryParse(p.dimension == null ? "" : p.dimension) == null ||
                    !origins.add(p.dimension + "/" + p.fluid) || !upgrades.containsKey(p.remoteUpgrade) ||
                    p.minimumTier < minimumTier || p.minimumTier > GTValues.MAX ||
                    p.duration < 1 || p.duration > 72000 || p.euPerTick < GTValues.VA[p.minimumTier] ||
                    p.euPerTick > GTValues.V[GTValues.MAX] ||
                    p.drillingFluid < 1 || p.drillingFluid > 1_000_000 || p.nitrogen < 0 || p.nitrogen > 1_000_000 ||
                    p.filters < 0 || p.filters > 64 || p.output < 1 || p.output > maxOutputPerOperation) {
                throw new IllegalArgumentException("Invalid/duplicate fluid program: " + (p == null ? "null" : p.id));
            }
        }
        for (var entry : upgrades.entrySet()) {
            Upgrade u = entry.getValue();
            if (!List.of("terrestrial", "t1", "t2", "t3", "t4", "t5").contains(entry.getKey()) ||
                    u == null || u.tier < GTValues.IV || u.tier > GTValues.UV || u.researchItems == null ||
                    u.researchItems.size() > 6 || u.researchItems.stream().anyMatch(
                            id -> id == null || ResourceLocation.tryParse(id) == null)) {
                throw new IllegalArgumentException("Invalid remote upgrade: " + entry.getKey());
            }
        }
        maxParallelByTier.forEach((tier, count) -> {
            if (!List.of(GTValues.VN).contains(tier) || count == null || count < 1 || count > 1024) {
                throw new IllegalArgumentException("Invalid parallel cap: " + tier);
            }
        });
    }

    public Program program(String id) {
        return programs.stream().filter(p -> p.id.equals(id)).findFirst().orElse(null);
    }

    public Program program(String dimension, String fluid) {
        return programs.stream().filter(p -> p.dimension.equals(dimension) && p.fluid.equals(fluid)).findFirst()
                .orElse(null);
    }
}
