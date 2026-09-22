package com.raishxn.gtna.common.machine.multiblock.module.steamElevator;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Locale;

/**
 * GTNL {@code SteamEntityCrusherModule} port (LGPLv3, original by ScienceNotLeisure).
 *
 * <p>
 * GTNL drives the "Extreme Extreme Entity Crusher" recipe map: an EnderIO powered spawner (a
 * <b>catalyst</b>, never consumed) is turned into that mob's drops, with a chance to double each
 * output: 2% plus 0.5% per identical spawner, capped at 34%, at the cost of doubled time and halved
 * power, and no overclocking. 1.20.1 has no EnderIO, so GTNA uses a vanilla spawner item carrying
 * the mob in its block-entity NBT and rolls the mob's own loot table — no external mod needed.
 *
 * <p>
 * The catalyst spawner is read from the module structure's <b>input bus</b> and the drops are pushed
 * to its <b>output bus</b> (the module controller keeps no inventory of its own).
 */
public class SteamEntityCrusherModule extends SteamElevatorModuleMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            SteamEntityCrusherModule.class, SteamElevatorModuleMachine.MANAGED_FIELD_HOLDER);

    /** GTNL: the default time is doubled (nominal 200) and the power halved (nominal 1024). */
    public static final int CYCLE_TICKS = 400;
    public static final long STEAM_UPKEEP = 512;

    private static final double BASE_DOUBLING_CHANCE = 2.0;
    private static final double CHANCE_PER_SPAWNER = 0.5;
    private static final double MAX_DOUBLING_CHANCE = 34.0;

    @Persisted
    @DescSynced
    private int progress;

    public SteamEntityCrusherModule(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public int getEffectRange() {
        // The crusher works on its stored spawner catalyst, not an area effect.
        return 0;
    }

    @Override
    public long getSteamUpkeep() {
        return STEAM_UPKEEP;
    }

    @Override
    public void onElevatorTick(SteamElevator elevator) {
        if (!consumeSteam(getSteamUpkeep())) return;
        if (!(getLevel() instanceof ServerLevel level)) return;
        if (++progress < CYCLE_TICKS) return;
        progress = 0;
        runCycle(level);
    }

    /** Samples the mob's loot table once and inserts the (possibly doubled) drops. */
    private void runCycle(ServerLevel level) {
        ItemStack catalyst = findCatalyst();
        if (catalyst.isEmpty()) return;
        EntityType<?> type = spawnerEntityType(catalyst);
        if (type == null) return;

        Entity probe = type.create(level);
        if (probe == null) return;
        probe.moveTo(Vec3.atCenterOf(getPos()));
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, probe)
                .withParameter(LootContextParams.ORIGIN, probe.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, level.damageSources().generic())
                .create(LootContextParamSets.ENTITY);
        LootTable lootTable = level.getServer().getLootData().getLootTable(type.getDefaultLootTable());
        List<ItemStack> drops = lootTable.getRandomItems(params);
        probe.discard();
        if (drops.isEmpty()) return;

        double chance = doublingChance(catalyst);
        for (ItemStack drop : drops) {
            if (level.random.nextDouble() * 100 < chance) {
                drop.setCount(Math.min(drop.getMaxStackSize(), drop.getCount() * 2));
            }
        }
        if (!canInsertItems(drops)) return;
        insertItems(drops);
        markDirty();
    }

    /** The first spawner catalyst in the module's input bus, or empty. */
    private ItemStack findCatalyst() {
        return findItem(stack -> spawnerEntityType(stack) != null);
    }

    /** GTNL: 2% + 0.5% per identical stored spawner, capped at 34%. */
    private double doublingChance(ItemStack catalyst) {
        EntityType<?> type = spawnerEntityType(catalyst);
        if (type == null) return BASE_DOUBLING_CHANCE;
        return doublingChanceFor(countItem(stack -> spawnerEntityType(stack) == type));
    }

    /** Static doubling-chance formula so the mechanic can be gametested. */
    public static double doublingChanceFor(int identicalSpawners) {
        return Math.min(MAX_DOUBLING_CHANCE, BASE_DOUBLING_CHANCE + CHANCE_PER_SPAWNER * identicalSpawners);
    }

    /** The mob stored in a vanilla spawner item's block-entity NBT, or {@code null}. */
    public static EntityType<?> spawnerEntityType(ItemStack stack) {
        if (!stack.is(Items.SPAWNER)) return null;
        CompoundTag blockEntity = stack.getTagElement("BlockEntityTag");
        if (blockEntity == null) return null;

        if (blockEntity.contains("SpawnData", Tag.TAG_COMPOUND)) {
            EntityType<?> type = entityFrom(blockEntity.getCompound("SpawnData"));
            if (type != null) return type;
        }
        if (blockEntity.contains("SpawnPotentials", Tag.TAG_LIST)) {
            ListTag potentials = blockEntity.getList("SpawnPotentials", Tag.TAG_COMPOUND);
            if (!potentials.isEmpty()) {
                return entityFrom(potentials.getCompound(0).getCompound("data"));
            }
        }
        return null;
    }

    private static EntityType<?> entityFrom(CompoundTag spawnData) {
        if (!spawnData.contains("entity", Tag.TAG_COMPOUND)) return null;
        String id = spawnData.getCompound("entity").getString("id");
        if (id.isEmpty()) return null;
        return EntityType.byString(id).orElse(null);
    }

    @Override
    protected Widget createModuleUIWidget() {
        WidgetGroup group = screenGroup(150, 64);
        group.addWidget(new LabelWidget(5, 4, () -> "Entity Crusher tier §b" + getModuleTier()));
        group.addWidget(new LabelWidget(5, 16, () -> "Doubling chance: §b" +
                String.format(Locale.ROOT, "%.1f", doublingChance(findCatalyst())) + "%%"));
        group.addWidget(new LabelWidget(5, 28,
                () -> "Progress: §b" + (progress * 100 / CYCLE_TICKS) + "%%"));
        group.addWidget(new LabelWidget(5, 40, () -> "§7Spawner: input bus"));
        group.addWidget(new LabelWidget(5, 52, () -> "§7Drops: output bus"));
        return group;
    }
}
