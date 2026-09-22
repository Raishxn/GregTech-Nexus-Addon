package com.raishxn.gtna.common.machine.multiblock.module.steamElevator;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.List;

/**
 * GTNL {@code SteamMonsterRepellentModule} port (LGPLv3, original by ScienceNotLeisure).
 *
 * <p>
 * GTNL registers a spawn-event repellent with a radius of {@code 1 << (5 + tier)} so hostile mobs
 * never spawn in the area while the machine runs. GTNA does the same through
 * {@link SteamRepellentHandler}: the running module publishes its field here (refreshed every tick,
 * expiring quietly if the module unloads) and the spawn event is cancelled for hostile mobs inside
 * it. The radius and energy cost follow GTNL exactly.
 */
public class SteamMonsterRepellentModule extends SteamElevatorModuleMachine {

    /** Fields published by running modules, keyed by position within their level. */
    private static final List<Field> ACTIVE = new ArrayList<>();

    /** A published repellent field; stale entries (module unloaded) expire after this many ticks. */
    private static final long FIELD_TTL_TICKS = 40L;

    private record Field(ServerLevel level, BlockPos pos, int range, long tick) {}

    public SteamMonsterRepellentModule(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
    }

    @Override
    public int getEffectRange() {
        // GTNL SteamMonsterRepellentModule#mRange.
        return 1 << (5 + getModuleTier());
    }

    @Override
    public long getSteamUpkeep() {
        return getModuleTier() * GTValues.V[3];
    }

    @Override
    public void onElevatorTick(SteamElevator elevator) {
        if (!consumeSteam(getSteamUpkeep())) return;
        if (!(getLevel() instanceof ServerLevel level)) return;
        publish(level);
    }

    @Override
    public void onElevatorStop() {
        if (getLevel() instanceof ServerLevel level) {
            remove(level, getPos());
        }
    }

    private void publish(ServerLevel level) {
        remove(level, getPos());
        ACTIVE.add(new Field(level, getPos(), getEffectRange(), level.getGameTime()));
    }

    private static void remove(ServerLevel level, BlockPos pos) {
        ACTIVE.removeIf(field -> field.level() == level && field.pos().equals(pos));
    }

    /** Whether a spawn at {@code (x, y, z)} falls inside an active repellent field. */
    public static boolean blocksSpawn(ServerLevel level, double x, double y, double z) {
        long now = level.getGameTime();
        ACTIVE.removeIf(field -> now - field.tick() > FIELD_TTL_TICKS);
        for (Field field : ACTIVE) {
            if (field.level() != level) continue;
            if (field.pos().distToCenterSqr(x, y, z) <= (double) field.range() * field.range()) {
                return true;
            }
        }
        return false;
    }
}
