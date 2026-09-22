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

import java.util.ArrayList;
import java.util.List;

/**
 * GTNL {@code SteamBeaconModule} port (LGPLv3, original by ScienceNotLeisure).
 *
 * <p>
 * GTNL lets the player configure up to {@code tier + 2} effects through a dedicated window and pays
 * with an iron/gold/diamond/emerald. GTNA keeps the tier-scaled effect range, the per-effect steam
 * upkeep and the "higher tier unlocks more effects" progression, but exposes them as a fixed,
 * documented set instead of porting GTNL's config window (whose mod-specific effects — Warp Ward,
 * Feather Feet, Vis Regen — have no 1.20.1 equivalent):
 * <ul>
 * <li>tier I: Speed, Haste</li>
 * <li>tier II: + Resistance, Regeneration</li>
 * <li>tier III: + Strength, Night Vision</li>
 * </ul>
 */
public class SteamBeaconModule extends SteamElevatorModuleMachine {

    private static final int EFFECT_DURATION = 300;

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

    private int effectCount() {
        return Math.min(6, getModuleTier() * 2);
    }

    @Override
    public long getSteamUpkeep() {
        // GTNL: machineEffectsCount * V[3] * max(1, maxEffectLevel * 2).
        return (long) effectCount() * GTValues.V[3] * Math.max(1, getModuleTier() * 2);
    }

    private List<MobEffect> effects() {
        List<MobEffect> effects = new ArrayList<>();
        effects.add(MobEffects.MOVEMENT_SPEED);
        effects.add(MobEffects.DIG_SPEED);
        if (getModuleTier() >= 2) {
            effects.add(MobEffects.DAMAGE_RESISTANCE);
            effects.add(MobEffects.REGENERATION);
        }
        if (getModuleTier() >= 3) {
            effects.add(MobEffects.DAMAGE_BOOST);
            effects.add(MobEffects.NIGHT_VISION);
        }
        return effects;
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
