package com.raishxn.gtna.common.machine.multiblock.module.steamElevator;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.server.level.ServerLevel;

/**
 * GTNL {@code SteamWeatherModule} port (LGPLv3, original by ScienceNotLeisure).
 *
 * <p>
 * GTNL consumes Natura clouds + Thaumcraft crystals to force clear / rain / thunder. Those mods do
 * not exist in 1.20.1, so GTNA exposes the same three weather states selected by a controller circuit
 * placed in the module structure's <b>input bus</b> (1 = clear, 2 = rain, 3 = thunder) and pays for a
 * change with a large one-off steam cost; the forced weather then lasts one in-game hour
 * ({@value #WEATHER_TIME} ticks) and the module UI shows the time left. While the circuit keeps
 * requesting the same weather, no further steam is charged until the hour runs out.
 */
public class SteamWeatherModule extends SteamElevatorModuleMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            SteamWeatherModule.class, SteamElevatorModuleMachine.MANAGED_FIELD_HOLDER);

    public static final int MODE_OFF = -1;
    public static final int MODE_CLEAR = 0;
    public static final int MODE_RAIN = 1;
    public static final int MODE_THUNDER = 2;

    /** One in-game hour. */
    public static final int WEATHER_TIME = 72000;
    /** The GTNL tooltip's "large" cost per weather change. */
    public static final long WEATHER_STEAM_COST = 1_000_000L;

    /** The weather currently being forced, or {@link #MODE_OFF} when nothing is active. */
    @Persisted
    @DescSynced
    private int activeMode = MODE_OFF;

    /** Ticks left on the forced weather; refreshed when a change is paid for. */
    @Persisted
    @DescSynced
    private int weatherTicksLeft;

    public SteamWeatherModule(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    /** The weather selected by the circuit in the input bus: 1 = clear, 2 = rain, 3 = thunder, else off. */
    public int selectedMode() {
        return modeForCircuit(findCircuit());
    }

    /** Static mapping so the circuit contract can be gametested without a formed machine. */
    public static int modeForCircuit(int circuit) {
        return switch (circuit) {
            case 1 -> MODE_CLEAR;
            case 2 -> MODE_RAIN;
            case 3 -> MODE_THUNDER;
            default -> MODE_OFF;
        };
    }

    public int getActiveMode() {
        return activeMode;
    }

    public int getWeatherTicksLeft() {
        return weatherTicksLeft;
    }

    @Override
    public long getSteamUpkeep() {
        // The weather is paid for in one lump when it is (re)applied, not per tick.
        return 0L;
    }

    @Override
    public void onElevatorTick(SteamElevator elevator) {
        if (!(getLevel() instanceof ServerLevel level)) return;
        if (weatherTicksLeft > 0) {
            weatherTicksLeft--;
        }

        int requested = selectedMode();
        boolean needsApplication = requested != MODE_OFF && (requested != activeMode || weatherTicksLeft <= 0);
        if (!needsApplication) return;
        if (!consumeSteam(WEATHER_STEAM_COST)) return;

        apply(level, requested);
        activeMode = requested;
        weatherTicksLeft = WEATHER_TIME;
        markDirty();
    }

    private void apply(ServerLevel level, int mode) {
        switch (mode) {
            case MODE_RAIN -> level.setWeatherParameters(0, WEATHER_TIME, true, false);
            case MODE_THUNDER -> level.setWeatherParameters(0, WEATHER_TIME, true, true);
            default -> level.setWeatherParameters(WEATHER_TIME, 0, false, false);
        }
    }

    @Override
    protected Widget createModuleUIWidget() {
        WidgetGroup group = screenGroup(150, 56);
        group.addWidget(new LabelWidget(5, 4, () -> "Weather: §b" + modeName(activeMode)));
        group.addWidget(new LabelWidget(5, 16, () -> "Circuit in the input bus: §b1=clear 2=rain 3=thunder"));
        group.addWidget(new LabelWidget(5, 28,
                () -> weatherTicksLeft > 0 ? "Time left: §b" + (weatherTicksLeft / 20) + " s" : "§7idle"));
        group.addWidget(new LabelWidget(5, 40,
                () -> "§7Cost on change: §b" + (WEATHER_STEAM_COST / 1000) + " B §7steam"));
        return group;
    }

    private static String modeName(int mode) {
        return switch (mode) {
            case MODE_RAIN -> "Rain";
            case MODE_THUNDER -> "Thunder";
            case MODE_CLEAR -> "Clear";
            default -> "Off";
        };
    }
}
