package com.raishxn.gtna.common.machine.multiblock.module.steamElevator;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;

import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;

import net.minecraft.network.chat.Component;
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

    /** Bitmask of the enabled effects; {@code -1} means "not configured yet" (use the default). */
    @Persisted
    @DescSynced
    private int effectMask = -1;

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

    /** GTNL: at most {@code mTier + 2} of the configured effects are active. */
    private int maxEffects() {
        return Math.min(ALL_EFFECTS.size(), getModuleTier() + 2);
    }

    /** The default selection: the first {@link #maxEffects()} effects. */
    private int defaultMask() {
        return (1 << maxEffects()) - 1;
    }

    private int activeMask() {
        return effectMask < 0 ? defaultMask() : effectMask;
    }

    private boolean isEffectOn(int index) {
        return (activeMask() & (1 << index)) != 0;
    }

    private void setEffect(int index, boolean on) {
        effectMask = toggleEffect(activeMask(), index, on, maxEffects());
        markDirty();
    }

    /**
     * Returns the new effect mask after toggling {@code index}; a toggle that would exceed
     * {@code maxEffects} is ignored. Static so the cap can be gametested.
     */
    public static int toggleEffect(int mask, int index, boolean on, int maxEffects) {
        boolean currentlyOn = (mask & (1 << index)) != 0;
        if (currentlyOn == on) return mask;
        if (on && Integer.bitCount(mask) >= maxEffects) return mask;
        return on ? (mask | (1 << index)) : (mask & ~(1 << index));
    }

    private int effectCount() {
        return Integer.bitCount(activeMask());
    }

    @Override
    public long getSteamUpkeep() {
        // GTNL: machineEffectsCount * V[3] * max(1, maxEffectLevel * 2).
        return (long) effectCount() * GTValues.V[3] * Math.max(1, getModuleTier() * 2);
    }

    private List<MobEffect> effects() {
        List<MobEffect> effects = new ArrayList<>();
        int mask = activeMask();
        for (int i = 0; i < ALL_EFFECTS.size(); i++) {
            if ((mask & (1 << i)) != 0) {
                effects.add(ALL_EFFECTS.get(i));
            }
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
        WidgetGroup group = screenGroup(150, 100);
        group.addWidget(new LabelWidget(5, 4,
                () -> "Beacon tier §b" + getModuleTier() + " §r| effects §b" + effectCount() + "/" + maxEffects() +
                        " §r| §b" + getSteamUpkeep() + " mB/t"));
        for (int i = 0; i < ALL_EFFECTS.size(); i++) {
            final int index = i;
            int x = 5 + (i % 2) * 72;
            int y = 18 + (i / 2) * 13;
            group.addWidget(new ButtonWidget(x, y, 70, 12, GuiTextures.BUTTON, clickData -> {
                if (!clickData.isRemote) {
                    setEffect(index, !isEffectOn(index));
                }
            }));
            group.addWidget(new LabelWidget(x + 2, y + 2,
                    () -> (isEffectOn(index) ? "§a" : "§7") +
                            Component.translatable(ALL_EFFECTS.get(index).getDescriptionId()).getString()));
        }
        return group;
    }
}
