package com.raishxn.gtna.common.machine.multiblock.module.steamElevator;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * GTNL {@code SteamBeaconModule} port (LGPLv3, original by ScienceNotLeisure).
 *
 * <p>
 * GTNL lets the player configure up to {@code tier + 2} effects through a dedicated window and pays
 * with an iron/gold/diamond/emerald. GTNA keeps the tier-scaled effect range, the
 * {@code activeEffects * V[3] * max(1, level*2)} upkeep and the "higher tier unlocks more effects"
 * progression, using the 10 GTNL effects that exist in 1.20.1 (Warp Ward and Vis Regen are
 * Thaumcraft-only, and Feather Feet is mapped to Slow Falling):
 * Speed, Strength, Jump Boost, Resistance, Regeneration, Night Vision, Haste, Fire Resistance,
 * Water Breathing, Slow Falling. GTNL's in-GUI effect picker is not ported yet.
 */
public class SteamBeaconModule extends SteamElevatorModuleMachine {

    private static final int EFFECT_DURATION = 300;

    /** GTNL's 12 effects minus the three with no 1.20.1 equivalent. */
    private static final List<MobEffect> ALL_EFFECTS = List.of(
            MobEffects.MOVEMENT_SPEED,
            MobEffects.DAMAGE_BOOST,
            MobEffects.JUMP,
            MobEffects.DAMAGE_RESISTANCE,
            MobEffects.REGENERATION,
            MobEffects.NIGHT_VISION,
            MobEffects.DIG_SPEED,
            MobEffects.FIRE_RESISTANCE,
            MobEffects.WATER_BREATHING,
            MobEffects.SLOW_FALLING);

    private int counter;

    public SteamBeaconModule(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
    }

    @Override
    public int getEffectRange() {
        return switch (getModuleTier()) {
            case 1 -> 64;
            case 2 -> 128;
            case 3 -> 256;
            default -> 0;
        };
    }

    /** GTNL: {@code mTier + 2} of the configured effects are active. */
    private int effectCount() {
        return Math.min(ALL_EFFECTS.size(), getModuleTier() + 2);
    }

    @Override
    public long getSteamUpkeep() {
        // GTNL: machineEffectsCount * V[3] * max(1, maxEffectLevel * 2).
        return (long) effectCount() * GTValues.V[3] * Math.max(1, getModuleTier() * 2);
    }

    private List<MobEffect> effects() {
        return ALL_EFFECTS.subList(0, effectCount());
    }

    @Override
    public void onElevatorTick(SteamElevator elevator) {
        if (!consumeSteam(getSteamUpkeep())) return;
        if (!(getLevel() instanceof ServerLevel level)) return;
        if (++counter % 40 != 0) return;

        double range = getEffectRange();
        if (range <= 0) return;
        AABB box = new AABB(getPos()).inflate(range);
        int amplifier = Math.max(0, getModuleTier() - 1);
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, box)) {
            for (MobEffect effect : effects()) {
                player.addEffect(new MobEffectInstance(effect, EFFECT_DURATION, amplifier, true, true));
            }
        }
    }

    @Override
    protected Widget createModuleUIWidget() {
        WidgetGroup group = screenGroup(150, 52);
        group.addWidget(
                new LabelWidget(5, 5, () -> "Beacon tier: §b" + getModuleTier() + " §r| Range: §b" + getEffectRange()));
        group.addWidget(new LabelWidget(5, 18,
                () -> "Effects: §b" + effectCount() + " §r| Upkeep: §b" + getSteamUpkeep() + " mB/t"));
        return group;
    }
}
