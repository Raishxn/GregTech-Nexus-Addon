package com.raishxn.gtna.config;

import com.gregtechceu.gtceu.api.GTValues;

import net.minecraftforge.fml.loading.FMLPaths;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.raishxn.gtna.GTNACORE;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class GTNABalance {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type STRING_INT_MAP = new TypeToken<LinkedHashMap<String, Integer>>() {}.getType();
    private static final Type STRING_DOUBLE_MAP = new TypeToken<LinkedHashMap<String, Double>>() {}.getType();
    private static final Path BASE_DIR = FMLPaths.CONFIGDIR.get().resolve("gtna").resolve("balance");

    private static HatchesBalance hatches = HatchesBalance.defaults();
    private static MachinesBalance machines = MachinesBalance.defaults();
    private static NexusFluxMatrixBalance nexusFluxMatrix = NexusFluxMatrixBalance.defaults();
    private static RestrictedItemsBalance restrictedItems = RestrictedItemsBalance.defaults();

    private GTNABalance() {}

    public static void init() {
        try {
            Files.createDirectories(BASE_DIR);
        } catch (IOException exception) {
            throw new RuntimeException("Failed to create GTNA balance config directory", exception);
        }

        hatches = load("hatches.json", HatchesBalance.class, HatchesBalance.defaults());
        machines = load("machines.json", MachinesBalance.class, MachinesBalance.defaults());
        nexusFluxMatrix = load("nexus_flux_matrix.json", NexusFluxMatrixBalance.class,
                NexusFluxMatrixBalance.defaults());
        restrictedItems = load("restricted_items.json", RestrictedItemsBalance.class,
                RestrictedItemsBalance.defaults());
    }

    private static <T extends DefaultsApplier<T>> T load(String fileName, Class<T> clazz, T defaults) {
        Path path = BASE_DIR.resolve(fileName);
        if (Files.notExists(path)) {
            writeDefaults(path, defaults);
            return defaults;
        }

        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            T loaded = GSON.fromJson(reader, clazz);
            if (loaded == null) {
                writeDefaults(path, defaults);
                return defaults;
            }
            loaded.applyDefaults(defaults);
            return loaded;
        } catch (IOException | JsonParseException exception) {
            GTNACORE.LOGGER.warn("Failed to load GTNA balance file {}. Rewriting defaults.", path, exception);
            writeDefaults(path, defaults);
            return defaults;
        }
    }

    private static void writeDefaults(Path path, Object defaults) {
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            GSON.toJson(defaults, writer);
        } catch (IOException exception) {
            throw new RuntimeException("Failed to write GTNA balance defaults to " + path, exception);
        }
    }

    public static HatchesBalance getHatches() {
        return hatches;
    }

    public static MachinesBalance getMachines() {
        return machines;
    }

    public static NexusFluxMatrixBalance getNexusFluxMatrix() {
        return nexusFluxMatrix;
    }

    public static RestrictedItemsBalance getRestrictedItems() {
        return restrictedItems;
    }

    public static int getOutputBoostMultiplier(int tier) {
        return getIntForTier(hatches.outputBoostHatch.multiplierByTier, tier, 25);
    }

    public static int getAccelerateBaseMinPercent(int tier) {
        return getIntForTier(hatches.accelerateHatch.baseMinDurationPercentByTier, tier,
                Math.max(1, 50 - (2 * (tier - 1))));
    }

    public static int getAcceleratePenaltyPerTierBelowMachine() {
        return hatches.accelerateHatch.penaltyPerTierBelowMachine;
    }

    public static int getAccelerateMinimumFinalPercent() {
        return hatches.accelerateHatch.minimumFinalPercent;
    }

    public static int getAccelerateMaximumFinalPercent() {
        return hatches.accelerateHatch.maximumFinalPercent;
    }

    public static double getOverclockDurationMultiplier(int tier) {
        return getDoubleForTier(hatches.overclockHatch.durationMultiplierByTier, tier, 1.0);
    }

    public static int getThreadCount(int tier) {
        return getIntForTier(hatches.threadHatch.extraThreadsByTier, tier,
                Math.max(0, (1 << Math.max(0, tier - 6)) - 1));
    }

    public static long getMegaSolarSteamPerBlock() {
        return machines.megaSolarBoiler.steamPerBlock;
    }

    public static int getMegaSolarTickInterval() {
        return machines.megaSolarBoiler.tickInterval;
    }

    public static int getMegaSolarMaxBackDistance() {
        return machines.megaSolarBoiler.maxBackDistance;
    }

    public static int getMegaSolarMaxSideDistance() {
        return machines.megaSolarBoiler.maxSideDistance;
    }

    public static boolean isMegaSolarClearSkyRequired() {
        return machines.megaSolarBoiler.requireClearSky;
    }

    public static VoidMinerSteamTierBalance getVoidMinerDenseSteam() {
        return machines.voidMinerSteamGateAged.denseSteam;
    }

    public static VoidMinerSteamTierBalance getVoidMinerSuperHeatedSteam() {
        return machines.voidMinerSteamGateAged.superHeatedSteam;
    }

    public static VoidMinerSteamTierBalance getVoidMinerInsanelySteam() {
        return machines.voidMinerSteamGateAged.insanelySupercriticalSteam;
    }

    public static long getNexusCapacitorCapacity(int tier, long fallback) {
        return getLongForTier(nexusFluxMatrix.tiers, tier, fallback, true);
    }

    public static String getNexusTransferLimit(int tier, String fallback) {
        NexusTierBalance balance = nexusFluxMatrix.tiers.get(tierKey(tier));
        if (balance == null || balance.transfer == null || balance.transfer.isBlank()) {
            return fallback;
        }
        return balance.transfer;
    }

    public static boolean isNexusCrossDimensionEnabled(int tier) {
        NexusTierBalance balance = nexusFluxMatrix.tiers.get(tierKey(tier));
        return balance != null && balance.crossDimension;
    }

    public static boolean isRestrictedGroupEnabled(String groupId) {
        RestrictedGroupBalance balance = restrictedItems.groups.get(groupId);
        return balance == null || balance.enabled;
    }

    public static boolean isRestrictedGroupHiddenFromJei(String groupId) {
        RestrictedGroupBalance balance = restrictedItems.groups.get(groupId);
        return balance != null && balance.hideFromJei;
    }

    private static int getIntForTier(Map<String, Integer> map, int tier, int fallback) {
        Integer value = map.get(tierKey(tier));
        return value != null ? value : fallback;
    }

    private static double getDoubleForTier(Map<String, Double> map, int tier, double fallback) {
        Double value = map.get(tierKey(tier));
        return value != null ? value : fallback;
    }

    private static long getLongForTier(Map<String, NexusTierBalance> map, int tier, long fallback, boolean capacity) {
        NexusTierBalance balance = map.get(tierKey(tier));
        if (balance == null) {
            return fallback;
        }
        String raw = capacity ? balance.capacity : balance.transfer;
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    public static String tierKey(int tier) {
        if (tier >= 0 && tier < GTValues.VN.length) {
            return GTValues.VN[tier];
        }
        return String.valueOf(tier);
    }

    public interface DefaultsApplier<T> {

        void applyDefaults(T defaults);
    }

    public static final class HatchesBalance implements DefaultsApplier<HatchesBalance> {

        public ThreadHatchBalance threadHatch = ThreadHatchBalance.defaults();
        public AccelerateHatchBalance accelerateHatch = AccelerateHatchBalance.defaults();
        public OverclockHatchBalance overclockHatch = OverclockHatchBalance.defaults();
        public OutputBoostHatchBalance outputBoostHatch = OutputBoostHatchBalance.defaults();

        public static HatchesBalance defaults() {
            return new HatchesBalance();
        }

        @Override
        public void applyDefaults(HatchesBalance defaults) {
            if (threadHatch == null) threadHatch = defaults.threadHatch;
            else threadHatch.applyDefaults(defaults.threadHatch);
            if (accelerateHatch == null) accelerateHatch = defaults.accelerateHatch;
            else accelerateHatch.applyDefaults(defaults.accelerateHatch);
            if (overclockHatch == null) overclockHatch = defaults.overclockHatch;
            else overclockHatch.applyDefaults(defaults.overclockHatch);
            if (outputBoostHatch == null) outputBoostHatch = defaults.outputBoostHatch;
            else outputBoostHatch.applyDefaults(defaults.outputBoostHatch);
        }
    }

    public static final class ThreadHatchBalance implements DefaultsApplier<ThreadHatchBalance> {

        public Map<String, Integer> extraThreadsByTier = defaultThreadMap();

        public static ThreadHatchBalance defaults() {
            return new ThreadHatchBalance();
        }

        @Override
        public void applyDefaults(ThreadHatchBalance defaults) {
            if (extraThreadsByTier == null) {
                extraThreadsByTier = defaults.extraThreadsByTier;
            } else {
                defaults.extraThreadsByTier.forEach(extraThreadsByTier::putIfAbsent);
            }
        }
    }

    public static final class AccelerateHatchBalance implements DefaultsApplier<AccelerateHatchBalance> {

        public Map<String, Integer> baseMinDurationPercentByTier = defaultAccelerateMap();
        public int penaltyPerTierBelowMachine = 20;
        public int minimumFinalPercent = 1;
        public int maximumFinalPercent = 100;

        public static AccelerateHatchBalance defaults() {
            return new AccelerateHatchBalance();
        }

        @Override
        public void applyDefaults(AccelerateHatchBalance defaults) {
            if (baseMinDurationPercentByTier == null) {
                baseMinDurationPercentByTier = defaults.baseMinDurationPercentByTier;
            } else {
                defaults.baseMinDurationPercentByTier.forEach(baseMinDurationPercentByTier::putIfAbsent);
            }
            if (penaltyPerTierBelowMachine <= 0) penaltyPerTierBelowMachine = defaults.penaltyPerTierBelowMachine;
            if (minimumFinalPercent <= 0) minimumFinalPercent = defaults.minimumFinalPercent;
            if (maximumFinalPercent <= 0) maximumFinalPercent = defaults.maximumFinalPercent;
        }
    }

    public static final class OverclockHatchBalance implements DefaultsApplier<OverclockHatchBalance> {

        public Map<String, Double> durationMultiplierByTier = defaultOverclockMap();

        public static OverclockHatchBalance defaults() {
            return new OverclockHatchBalance();
        }

        @Override
        public void applyDefaults(OverclockHatchBalance defaults) {
            if (durationMultiplierByTier == null) {
                durationMultiplierByTier = defaults.durationMultiplierByTier;
            } else {
                defaults.durationMultiplierByTier.forEach(durationMultiplierByTier::putIfAbsent);
            }
        }
    }

    public static final class OutputBoostHatchBalance implements DefaultsApplier<OutputBoostHatchBalance> {

        public Map<String, Integer> multiplierByTier = defaultOutputBoostMap();
        public boolean affectsItems = true;
        public boolean affectsFluids = true;
        public boolean stackMultipleHatches = true;

        public static OutputBoostHatchBalance defaults() {
            return new OutputBoostHatchBalance();
        }

        @Override
        public void applyDefaults(OutputBoostHatchBalance defaults) {
            if (multiplierByTier == null) {
                multiplierByTier = defaults.multiplierByTier;
            } else {
                defaults.multiplierByTier.forEach(multiplierByTier::putIfAbsent);
            }
        }
    }

    public static final class MachinesBalance implements DefaultsApplier<MachinesBalance> {

        public MegaSolarBalance megaSolarBoiler = MegaSolarBalance.defaults();
        public VoidMinerSteamGateBalance voidMinerSteamGateAged = VoidMinerSteamGateBalance.defaults();

        public static MachinesBalance defaults() {
            return new MachinesBalance();
        }

        @Override
        public void applyDefaults(MachinesBalance defaults) {
            if (megaSolarBoiler == null) megaSolarBoiler = defaults.megaSolarBoiler;
            else megaSolarBoiler.applyDefaults(defaults.megaSolarBoiler);
            if (voidMinerSteamGateAged == null) voidMinerSteamGateAged = defaults.voidMinerSteamGateAged;
            else voidMinerSteamGateAged.applyDefaults(defaults.voidMinerSteamGateAged);
        }
    }

    public static final class MegaSolarBalance implements DefaultsApplier<MegaSolarBalance> {

        public boolean enabled = true;
        public int steamPerBlock = 500;
        public int tickInterval = 20;
        public boolean requireClearSky = true;
        public int maxBackDistance = 32;
        public int maxSideDistance = 16;

        public static MegaSolarBalance defaults() {
            return new MegaSolarBalance();
        }

        @Override
        public void applyDefaults(MegaSolarBalance defaults) {
            if (steamPerBlock <= 0) steamPerBlock = defaults.steamPerBlock;
            if (tickInterval <= 0) tickInterval = defaults.tickInterval;
            if (maxBackDistance <= 0) maxBackDistance = defaults.maxBackDistance;
            if (maxSideDistance <= 0) maxSideDistance = defaults.maxSideDistance;
        }
    }

    public static final class VoidMinerSteamGateBalance implements DefaultsApplier<VoidMinerSteamGateBalance> {

        public boolean enabled = true;
        public VoidMinerSteamTierBalance denseSteam = new VoidMinerSteamTierBalance(2, 2.0, 1.5);
        public VoidMinerSteamTierBalance superHeatedSteam = new VoidMinerSteamTierBalance(3, 3.0, 2.0);
        public VoidMinerSteamTierBalance insanelySupercriticalSteam = new VoidMinerSteamTierBalance(5, 5.0, 4.0);

        public static VoidMinerSteamGateBalance defaults() {
            return new VoidMinerSteamGateBalance();
        }

        @Override
        public void applyDefaults(VoidMinerSteamGateBalance defaults) {
            if (denseSteam == null) denseSteam = defaults.denseSteam;
            else denseSteam.applyDefaults(defaults.denseSteam);
            if (superHeatedSteam == null) superHeatedSteam = defaults.superHeatedSteam;
            else superHeatedSteam.applyDefaults(defaults.superHeatedSteam);
            if (insanelySupercriticalSteam == null) insanelySupercriticalSteam = defaults.insanelySupercriticalSteam;
            else insanelySupercriticalSteam.applyDefaults(defaults.insanelySupercriticalSteam);
        }
    }

    public static final class VoidMinerSteamTierBalance implements DefaultsApplier<VoidMinerSteamTierBalance> {

        public int outputMultiplier;
        public double speedMultiplier;
        public double energyMultiplier;

        public VoidMinerSteamTierBalance() {}

        public VoidMinerSteamTierBalance(int outputMultiplier, double speedMultiplier, double energyMultiplier) {
            this.outputMultiplier = outputMultiplier;
            this.speedMultiplier = speedMultiplier;
            this.energyMultiplier = energyMultiplier;
        }

        @Override
        public void applyDefaults(VoidMinerSteamTierBalance defaults) {
            if (outputMultiplier <= 0) outputMultiplier = defaults.outputMultiplier;
            if (speedMultiplier <= 0) speedMultiplier = defaults.speedMultiplier;
            if (energyMultiplier <= 0) energyMultiplier = defaults.energyMultiplier;
        }
    }

    public static final class NexusFluxMatrixBalance implements DefaultsApplier<NexusFluxMatrixBalance> {

        public Map<String, NexusTierBalance> tiers = defaultNexusTierMap();
        public EfficiencyBalance efficiency = EfficiencyBalance.defaults();
        public SafeModeBalance safeMode = SafeModeBalance.defaults();
        public NexusLimitsBalance limits = NexusLimitsBalance.defaults();

        public static NexusFluxMatrixBalance defaults() {
            return new NexusFluxMatrixBalance();
        }

        @Override
        public void applyDefaults(NexusFluxMatrixBalance defaults) {
            if (tiers == null) {
                tiers = defaults.tiers;
            } else {
                defaults.tiers.forEach(tiers::putIfAbsent);
            }
            if (efficiency == null) efficiency = defaults.efficiency;
            else efficiency.applyDefaults(defaults.efficiency);
            if (safeMode == null) safeMode = defaults.safeMode;
            else safeMode.applyDefaults(defaults.safeMode);
            if (limits == null) limits = defaults.limits;
            else limits.applyDefaults(defaults.limits);
        }
    }

    public static final class NexusTierBalance {

        public String capacity;
        public String transfer;
        public boolean crossDimension;

        public NexusTierBalance() {}

        public NexusTierBalance(String capacity, String transfer, boolean crossDimension) {
            this.capacity = capacity;
            this.transfer = transfer;
            this.crossDimension = crossDimension;
        }
    }

    public static final class EfficiencyBalance implements DefaultsApplier<EfficiencyBalance> {

        public String mode = "AVERAGE";
        public double baseLossPercentAtLV = 15.0;
        public double minimumEfficiency = 0.85;
        public double maximumEfficiency = 1.0;

        public static EfficiencyBalance defaults() {
            return new EfficiencyBalance();
        }

        @Override
        public void applyDefaults(EfficiencyBalance defaults) {
            if (mode == null || mode.isBlank()) mode = defaults.mode;
            if (baseLossPercentAtLV < 0) baseLossPercentAtLV = defaults.baseLossPercentAtLV;
            if (minimumEfficiency <= 0) minimumEfficiency = defaults.minimumEfficiency;
            if (maximumEfficiency <= 0) maximumEfficiency = defaults.maximumEfficiency;
        }
    }

    public static final class SafeModeBalance implements DefaultsApplier<SafeModeBalance> {

        public boolean enabled = true;
        public int thresholdPercent = 10;
        public int recoveryPercent = 25;
        public int alertCooldownTicks = 1200;

        public static SafeModeBalance defaults() {
            return new SafeModeBalance();
        }

        @Override
        public void applyDefaults(SafeModeBalance defaults) {
            if (thresholdPercent <= 0) thresholdPercent = defaults.thresholdPercent;
            if (recoveryPercent <= 0) recoveryPercent = defaults.recoveryPercent;
            if (alertCooldownTicks <= 0) alertCooldownTicks = defaults.alertCooldownTicks;
        }
    }

    public static final class NexusLimitsBalance implements DefaultsApplier<NexusLimitsBalance> {

        public int maxConnectionsPerPlayer = 256;
        public int maxNetworksPerPlayer = 1;

        public static NexusLimitsBalance defaults() {
            return new NexusLimitsBalance();
        }

        @Override
        public void applyDefaults(NexusLimitsBalance defaults) {
            if (maxConnectionsPerPlayer <= 0) maxConnectionsPerPlayer = defaults.maxConnectionsPerPlayer;
            if (maxNetworksPerPlayer <= 0) maxNetworksPerPlayer = defaults.maxNetworksPerPlayer;
        }
    }

    public static final class RestrictedItemsBalance implements DefaultsApplier<RestrictedItemsBalance> {

        public Map<String, RestrictedGroupBalance> groups = defaultRestrictedGroups();

        public static RestrictedItemsBalance defaults() {
            return new RestrictedItemsBalance();
        }

        @Override
        public void applyDefaults(RestrictedItemsBalance defaults) {
            if (groups == null) {
                groups = defaults.groups;
            } else {
                defaults.groups.forEach(groups::putIfAbsent);
            }
        }
    }

    public static final class RestrictedGroupBalance {

        public boolean enabled;
        public boolean hideFromJei;
        public boolean disableRecipes;

        public RestrictedGroupBalance() {}

        public RestrictedGroupBalance(boolean enabled, boolean hideFromJei, boolean disableRecipes) {
            this.enabled = enabled;
            this.hideFromJei = hideFromJei;
            this.disableRecipes = disableRecipes;
        }
    }

    private static Map<String, Integer> defaultThreadMap() {
        LinkedHashMap<String, Integer> values = new LinkedHashMap<>();
        values.put("ZPM", 1);
        values.put("UV", 3);
        values.put("UHV", 7);
        values.put("UEV", 15);
        values.put("UIV", 31);
        values.put("UXV", 63);
        values.put("OpV", 127);
        values.put("MAX", 255);
        return values;
    }

    private static Map<String, Integer> defaultAccelerateMap() {
        LinkedHashMap<String, Integer> values = new LinkedHashMap<>();
        for (int tier = GTValues.LV; tier <= GTValues.MAX; tier++) {
            values.put(tierKey(tier), Math.max(1, 50 - (2 * (tier - 1))));
        }
        return values;
    }

    private static Map<String, Double> defaultOverclockMap() {
        LinkedHashMap<String, Double> values = new LinkedHashMap<>();
        values.put("UV", 0.50);
        values.put("UHV", 0.3333);
        values.put("UEV", 0.25);
        values.put("UIV", 0.20);
        values.put("UXV", 0.1667);
        values.put("OpV", 0.1429);
        values.put("MAX", 0.125);
        return values;
    }

    private static Map<String, Integer> defaultOutputBoostMap() {
        LinkedHashMap<String, Integer> values = new LinkedHashMap<>();
        values.put("ULV", 25);
        values.put("LV", 25);
        values.put("MV", 25);
        values.put("HV", 50);
        values.put("EV", 100);
        values.put("IV", 250);
        values.put("LuV", 500);
        values.put("ZPM", 1000);
        values.put("UV", 2500);
        values.put("UHV", 5000);
        values.put("UEV", 10000);
        values.put("UIV", 25000);
        values.put("UXV", 50000);
        values.put("OpV", 100000);
        values.put("MAX", 250000);
        return values;
    }

    private static Map<String, NexusTierBalance> defaultNexusTierMap() {
        LinkedHashMap<String, NexusTierBalance> values = new LinkedHashMap<>();
        values.put("LV", new NexusTierBalance("160000", "2000", false));
        values.put("MV", new NexusTierBalance("1500000", "8000", false));
        values.put("HV", new NexusTierBalance("10000000", "32000", false));
        values.put("EV", new NexusTierBalance("50000000", "128000", true));
        values.put("IV", new NexusTierBalance("250000000", "512000", true));
        values.put("LuV", new NexusTierBalance("1500000000", "2048000", true));
        values.put("ZPM", new NexusTierBalance("15000000000", "8192000", true));
        values.put("UV", new NexusTierBalance("150000000000", "32768000", true));
        values.put("UHV", new NexusTierBalance("3000000000000", "131072000", true));
        values.put("UEV", new NexusTierBalance("50000000000000", "524288000", true));
        values.put("UIV", new NexusTierBalance("900000000000000", "2097152000", true));
        values.put("UXV", new NexusTierBalance("15000000000000000", "8388608000", true));
        values.put("OpV", new NexusTierBalance("250000000000000000", "33554432000", true));
        values.put("MAX", new NexusTierBalance("5000000000000000000", "500000000000000000000000", true));
        return values;
    }

    private static Map<String, RestrictedGroupBalance> defaultRestrictedGroups() {
        LinkedHashMap<String, RestrictedGroupBalance> values = new LinkedHashMap<>();
        values.put("infinityCovers", new RestrictedGroupBalance(false, true, true));
        values.put("quantumCosmicNexusArmor", new RestrictedGroupBalance(false, true, true));
        values.put("realityRipper", new RestrictedGroupBalance(false, true, true));
        values.put("infiniteInputParts", new RestrictedGroupBalance(true, false, false));
        values.put("outputBoostParts", new RestrictedGroupBalance(true, false, false));
        return values;
    }
}
