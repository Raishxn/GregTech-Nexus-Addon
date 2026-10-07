package com.raishxn.gtna.common.machine.multiblock.godforge;

import net.minecraft.nbt.CompoundTag;

import com.raishxn.gtna.api.machine.feature.godforge.GodforgeData;
import com.raishxn.gtna.api.machine.feature.godforge.GodforgeUpgrade;

import java.math.BigInteger;

/** NBT form of {@link GodforgeData}, using the GTNH key names where they exist. */
public final class GodforgeDataNbt {

    private GodforgeDataNbt() {}

    public static CompoundTag save(GodforgeData data) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("fuelConsumptionFactor", data.fuelConsumptionFactor);
        tag.putInt("selectedFuelType", data.selectedFuelType);
        tag.putLong("internalBattery", data.internalBattery);
        tag.putLong("batterySize", data.maxBatteryCharge);
        tag.putInt("gravitonShardsAvailable", data.gravitonShardsAvailable);
        tag.putInt("gravitonShardsSpent", data.gravitonShardsSpent);
        tag.putInt("ringAmount", data.ringAmount);
        tag.putInt("stellarFuelAmount", data.stellarFuelAmount);
        tag.putInt("neededStartupFuel", data.neededStartupFuel);
        tag.putLong("fuelConsumption", data.fuelConsumption);
        tag.putLong("totalRecipesProcessed", data.totalRecipesProcessed);
        tag.putLong("totalFuelConsumed", data.totalFuelConsumed);
        tag.putFloat("totalExtensionsBuilt", data.totalExtensionsBuilt);
        tag.putString("totalPowerConsumed", data.totalPowerConsumed.toString());
        tag.putIntArray("milestoneProgress", data.milestoneProgress);
        tag.putBoolean("batteryCharging", data.batteryCharging);
        tag.putBoolean("inversion", data.inversion);
        tag.putBoolean("gravitonShardEjection", data.gravitonShardEjection);
        tag.putBoolean("isRenderActive", data.renderActive);
        tag.putBoolean("isRendererDisabled", data.rendererDisabled);
        tag.putBoolean("secretUpgrade", data.secretUpgrade);
        tag.putString("selectedStarColor", data.selectedStarColor);
        var customColors = new net.minecraft.nbt.ListTag();
        for (String color : data.starColors.serializeCustom())
            customColors.add(net.minecraft.nbt.StringTag.valueOf(color));
        if (!customColors.isEmpty()) tag.put("customStarColors", customColors);
        tag.putInt("rotationSpeed", data.rotationSpeed);
        tag.putInt("starSize", data.starSize);
        CompoundTag upgrades = new CompoundTag();
        for (GodforgeUpgrade upgrade : GodforgeUpgrade.VALUES) {
            upgrades.putBoolean("upgrade" + upgrade.ordinal(), data.isUpgradeActive(upgrade));
            if (upgrade.hasExtraCost()) {
                CompoundTag cost = new CompoundTag();
                cost.putBoolean("paid", data.upgrades.isCostPaid(upgrade));
                short[] paid = data.upgrades.paidCosts(upgrade);
                for (int i = 0; i < paid.length; i++) cost.putShort("costPaid" + i, paid[i]);
                upgrades.put("extraCost" + upgrade.ordinal(), cost);
            }
        }
        tag.put("upgrades", upgrades);
        return tag;
    }

    public static void load(GodforgeData data, CompoundTag tag) {
        if (tag.isEmpty()) return;
        data.fuelConsumptionFactor = Math.max(1, tag.getInt("fuelConsumptionFactor"));
        data.selectedFuelType = tag.getInt("selectedFuelType");
        data.internalBattery = tag.getLong("internalBattery");
        data.maxBatteryCharge = tag.contains("batterySize") ? tag.getLong("batterySize") :
                GodforgeData.DEFAULT_MAX_BATTERY_CHARGE;
        data.gravitonShardsAvailable = tag.getInt("gravitonShardsAvailable");
        data.gravitonShardsSpent = tag.getInt("gravitonShardsSpent");
        data.ringAmount = Math.max(1, tag.getInt("ringAmount"));
        data.stellarFuelAmount = tag.getInt("stellarFuelAmount");
        data.neededStartupFuel = tag.getInt("neededStartupFuel");
        data.fuelConsumption = tag.getLong("fuelConsumption");
        data.totalRecipesProcessed = tag.getLong("totalRecipesProcessed");
        data.totalFuelConsumed = tag.getLong("totalFuelConsumed");
        data.totalExtensionsBuilt = tag.getFloat("totalExtensionsBuilt");
        String power = tag.getString("totalPowerConsumed");
        data.totalPowerConsumed = power.isEmpty() ? BigInteger.ZERO : new BigInteger(power);
        int[] progress = tag.getIntArray("milestoneProgress");
        System.arraycopy(progress, 0, data.milestoneProgress, 0, Math.min(4, progress.length));
        data.batteryCharging = tag.getBoolean("batteryCharging");
        data.inversion = tag.getBoolean("inversion");
        data.gravitonShardEjection = tag.getBoolean("gravitonShardEjection");
        data.renderActive = tag.getBoolean("isRenderActive");
        data.rendererDisabled = tag.getBoolean("isRendererDisabled");
        data.secretUpgrade = tag.getBoolean("secretUpgrade");
        if (tag.contains("selectedStarColor")) data.selectedStarColor = tag.getString("selectedStarColor");
        var customColors = tag.getList("customStarColors", net.minecraft.nbt.Tag.TAG_STRING);
        java.util.List<String> colors = new java.util.ArrayList<>();
        for (int i = 0; i < customColors.size(); i++) colors.add(customColors.getString(i));
        data.starColors.rebuild(colors);
        if (data.starColors.byName(data.selectedStarColor) == null) {
            data.selectedStarColor = com.raishxn.gtna.api.machine.feature.godforge.GodforgeStarColor.DEFAULT.name();
        }
        if (tag.contains("rotationSpeed")) data.rotationSpeed = tag.getInt("rotationSpeed");
        if (tag.contains("starSize")) data.starSize = tag.getInt("starSize");
        CompoundTag upgrades = tag.getCompound("upgrades");
        for (GodforgeUpgrade upgrade : GodforgeUpgrade.VALUES) {
            CompoundTag cost = upgrades.getCompound("extraCost" + upgrade.ordinal());
            short[] paid = new short[12];
            for (int i = 0; i < 12; i++) paid[i] = cost.getShort("costPaid" + i);
            data.upgrades.setRaw(upgrade, upgrades.getBoolean("upgrade" + upgrade.ordinal()), cost.getBoolean("paid"),
                    paid);
        }
    }
}
