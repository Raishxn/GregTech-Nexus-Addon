package com.raishxn.gtna.common.world.personalspace;

import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;

/**
 * Own day time and weather for a personal dimension, persisted in its settings. The original replaces the
 * {@code DerivedWorldInfo} of each personal world with a standalone copy for the same reason.
 */
public final class PersonalSpaceLevelData extends DerivedLevelData {

    private final PersonalSpaceSettings settings;
    private final Runnable markDirty;
    private int clearWeatherTime;

    public PersonalSpaceLevelData(WorldData worldData, ServerLevelData overworld, PersonalSpaceSettings settings,
                                  Runnable markDirty) {
        super(worldData, overworld);
        this.settings = settings;
        this.markDirty = markDirty;
        if (!settings.isTimeDataPersisted()) {
            settings.setTimeData(overworld.getDayTime(), overworld.isRaining(), overworld.getRainTime(),
                    overworld.isThundering(), overworld.getThunderTime());
            markDirty.run();
        }
    }

    public PersonalSpaceSettings settings() {
        return settings;
    }

    /**
     * A fixed sun or moon keeps the sky at noon or midnight while the stored world time keeps running, which is how
     * the original's celestial-angle override behaves.
     */
    @Override
    public long getDayTime() {
        return switch (settings.getDaylightCycle()) {
            case SUN -> 6000L;
            case MOON -> 18000L;
            case CYCLE -> settings.getWorldTime();
        };
    }

    /** Advances the stored world time by one tick; called because Infiniverse levels do not tick time. */
    public void tickDayTime() {
        settings.setWorldTime(settings.getWorldTime() + 1L);
    }

    @Override
    public void setDayTime(long time) {
        settings.setWorldTime(time);
        markDirty.run();
    }

    @Override
    public boolean isRaining() {
        return settings.isWeatherEnabled() && settings.isRaining();
    }

    @Override
    public void setRaining(boolean raining) {
        settings.setRaining(raining);
        markDirty.run();
    }

    @Override
    public int getRainTime() {
        return settings.getRainTime();
    }

    @Override
    public void setRainTime(int time) {
        settings.setRainTime(time);
    }

    @Override
    public boolean isThundering() {
        return settings.isWeatherEnabled() && settings.isThundering();
    }

    @Override
    public void setThundering(boolean thundering) {
        settings.setThundering(thundering);
        markDirty.run();
    }

    @Override
    public int getThunderTime() {
        return settings.getThunderTime();
    }

    @Override
    public void setThunderTime(int time) {
        settings.setThunderTime(time);
    }

    @Override
    public int getClearWeatherTime() {
        return clearWeatherTime;
    }

    @Override
    public void setClearWeatherTime(int time) {
        clearWeatherTime = time;
    }
}
