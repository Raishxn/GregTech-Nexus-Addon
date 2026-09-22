package com.raishxn.gtna.common.machine.multiblock.module.steamElevator;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;

import net.minecraft.server.level.ServerLevel;

/**
 * GTNL {@code SteamWeatherModule} port (LGPLv3, original by ScienceNotLeisure).
 *
 * <p>
 * GTNL consumes Natura clouds + Thaumcraft crystals to force clear / rain / thunder (special values
 * 1/2/3). Those mods do not exist in 1.20.1, so GTNA exposes the same three weather states as a
 * toggled machine mode paid for with elevator energy; the effect is identical (the world weather is
 * forced for two in-game hours and refreshed while running).
 */
public class SteamWeatherModule extends SteamElevatorModuleMachine {

    public static final int MODE_CLEAR = 0;
    public static final int MODE_RAIN = 1;
    public static final int MODE_THUNDER = 2;

    private static final int WEATHER_TIME = 72000;

    @Persisted
    @DescSynced
    private int weatherMode = MODE_CLEAR;

    private int counter;

    public SteamWeatherModule(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
    }

    @Override
    public long getEnergyUsage() {
        // GTNL recipe: eut 0 in the fake map, so a flat upkeep of V[3] is used here.
        return getModuleTier() * GTValues.V[3];
    }

    public int getWeatherMode() {
        return weatherMode;
    }

    public void cycleWeatherMode() {
        weatherMode = (weatherMode + 1) % 3;
        markDirty();
    }

    @Override
    public void onElevatorTick(SteamElevator elevator) {
        if (!consumeEnergy(getEnergyUsage())) return;
        if (!(getLevel() instanceof ServerLevel level)) return;
        // Refresh the forced weather every second so it never expires while the module runs.
        if (++counter % 20 != 0) return;
        switch (weatherMode) {
            case MODE_RAIN -> level.setWeatherParameters(0, WEATHER_TIME, true, false);
            case MODE_THUNDER -> level.setWeatherParameters(0, WEATHER_TIME, true, true);
            default -> level.setWeatherParameters(WEATHER_TIME, 0, false, false);
        }
    }

    @Override
    protected Widget createModuleUIWidget() {
        WidgetGroup group = screenGroup(150, 52);
        group.addWidget(new LabelWidget(5, 5, () -> "Weather: §b" + modeName()));
        group.addWidget(new ButtonWidget(5, 20, 60, 16,
                new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture("Cycle")),
                clickData -> {
                    if (!clickData.isRemote) cycleWeatherMode();
                }));
        return group;
    }

    private String modeName() {
        return switch (weatherMode) {
            case MODE_RAIN -> "Rain";
            case MODE_THUNDER -> "Thunder";
            default -> "Clear";
        };
    }
}
