package com.raishxn.gtna.api.machine.feature.godforge;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

/** Purchased upgrades and paid item costs, as in GTNH {@code UpgradeStorage}. */
public final class GodforgeUpgradeStorage {

    /** Mutable stack seen by {@link #payCost}: an item ID and the amount still available. */
    public interface Payment {

        String item();

        int amount();

        void shrink(int by);
    }

    private static final class Data {

        boolean active;
        boolean costPaid;
        final short[] amountsPaid = new short[12];
    }

    private final EnumMap<GodforgeUpgrade, Data> upgrades = new EnumMap<>(GodforgeUpgrade.class);

    public GodforgeUpgradeStorage() {
        for (GodforgeUpgrade upgrade : GodforgeUpgrade.VALUES) upgrades.put(upgrade, new Data());
    }

    public boolean isUpgradeActive(GodforgeUpgrade upgrade) {
        return upgrades.get(upgrade).active;
    }

    public boolean isCostPaid(GodforgeUpgrade upgrade) {
        return upgrades.get(upgrade).costPaid;
    }

    public short[] paidCosts(GodforgeUpgrade upgrade) {
        return upgrades.get(upgrade).amountsPaid.clone();
    }

    /** Consumes matching items towards the extra cost. Does NOT handle graviton shards. */
    public void payCost(GodforgeUpgrade upgrade, List<? extends Payment> inputs) {
        Data data = upgrades.get(upgrade);
        if (!upgrade.hasExtraCost()) {
            data.costPaid = true;
            return;
        }
        List<GodforgeUpgrade.ExtraCost> cost = upgrade.extraCost();
        for (Payment input : inputs) {
            for (int j = 0; j < cost.size(); j++) {
                GodforgeUpgrade.ExtraCost entry = cost.get(j);
                int alreadyPaid = data.amountsPaid[j];
                if (alreadyPaid >= entry.amount() || input.amount() <= 0) continue;
                if (!entry.item().equals(input.item())) continue;
                int extract = Math.min(entry.amount() - alreadyPaid, input.amount());
                data.amountsPaid[j] += (short) extract;
                input.shrink(extract);
            }
        }
        for (int i = 0; i < cost.size(); i++) {
            if (data.amountsPaid[i] < cost.get(i).amount()) return;
        }
        data.costPaid = true;
    }

    void unlock(GodforgeUpgrade upgrade) {
        upgrades.get(upgrade).active = true;
    }

    void respec(GodforgeUpgrade upgrade) {
        upgrades.get(upgrade).active = false;
    }

    public boolean checkPrerequisites(GodforgeUpgrade upgrade) {
        GodforgeUpgrade[] prerequisites = upgrade.prerequisites();
        if (prerequisites.length == 0) return true;
        if (upgrade.requiresAllPrerequisites()) {
            return Arrays.stream(prerequisites).allMatch(this::isUpgradeActive);
        }
        return Arrays.stream(prerequisites).anyMatch(this::isUpgradeActive);
    }

    public boolean checkSplit(GodforgeUpgrade upgrade, int maxSplitUpgrades) {
        if (!GodforgeUpgrade.SPLIT_UPGRADES.contains(upgrade)) return true;
        return GodforgeUpgrade.SPLIT_UPGRADES.stream().filter(this::isUpgradeActive).count() < maxSplitUpgrades;
    }

    public boolean checkCost(GodforgeUpgrade upgrade, int availableShards) {
        if (upgrade.shardCost() > availableShards) return false;
        return !upgrade.hasExtraCost() || isCostPaid(upgrade);
    }

    /** @return false if removing {@code upgrade} would orphan an active dependent. */
    public boolean checkDependents(GodforgeUpgrade upgrade) {
        for (GodforgeUpgrade dependent : upgrade.dependents()) {
            if (!isUpgradeActive(dependent)) continue;
            if (dependent.requiresAllPrerequisites()) return false;
            if (Arrays.stream(dependent.prerequisites()).filter(this::isUpgradeActive).count() <= 1) return false;
        }
        return true;
    }

    public int totalActiveUpgrades() {
        return (int) upgrades.values().stream().filter(d -> d.active).count();
    }

    public boolean hasAnyProgress() {
        if (isUpgradeActive(GodforgeUpgrade.START)) return true;
        for (var entry : upgrades.entrySet()) {
            if (!entry.getKey().hasExtraCost()) continue;
            Data data = entry.getValue();
            if (data.costPaid) return true;
            for (short paid : data.amountsPaid) if (paid != 0) return true;
        }
        return false;
    }

    public void resetAll() {
        for (Data data : upgrades.values()) {
            data.active = false;
            data.costPaid = false;
        }
    }

    public void unlockAll() {
        for (Data data : upgrades.values()) data.active = true;
    }

    /** Raw state for NBT/network: active, costPaid and the twelve paid amounts. */
    public void setRaw(GodforgeUpgrade upgrade, boolean active, boolean costPaid, short[] paid) {
        Data data = upgrades.get(upgrade);
        data.active = active;
        data.costPaid = costPaid;
        System.arraycopy(paid, 0, data.amountsPaid, 0, Math.min(paid.length, 12));
    }
}
