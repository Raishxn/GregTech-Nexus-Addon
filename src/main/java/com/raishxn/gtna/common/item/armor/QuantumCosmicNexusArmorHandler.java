package com.raishxn.gtna.common.item.armor;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.common.data.GTNADamageTypes;
import com.raishxn.gtna.common.data.GTNAItems;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = GTNACORE.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class QuantumCosmicNexusArmorHandler {

    private static final double COSMIC_STEP_HEIGHT = 1.0D;
    private static final float DEFAULT_FLYING_SPEED = 0.05F;
    private static final float QUANTUM_FLYING_SPEED = 0.2F;
    private static final int BOOT_EFFECT_DURATION = 300;
    private static final int BOOT_SPEED_AMPLIFIER = 9;
    private static final Map<UUID, FlightState> MANAGED_FLIGHT_STATES = new HashMap<>();
    private static final Map<UUID, BootState> MANAGED_BOOT_STATES = new HashMap<>();

    private QuantumCosmicNexusArmorHandler() {}

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }

        applyHelmetEffects(player);
        applyChestEffects(player);
        applyLegEffects(player);
        applyBootEffects(player);
        applyFlightState(player);

        if (isWearingFullSet(player)) {
            sustainFullSet(player);
        }
    }

    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof Player player && isWearingBoots(player)) {
            player.push(0.0D, 0.4D, 0.0D);
        }
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player player && isWearingFullSet(player) &&
                !GTNADamageTypes.isRealityRip(event.getSource())) {
            event.setCanceled(true);
            player.hurtTime = 0;
            player.deathTime = 0;
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player && isWearingFullSet(player) &&
                !GTNADamageTypes.isRealityRip(event.getSource())) {
            reflectDamage(event.getSource().getEntity(), player, event.getAmount());
            event.setAmount(0.0F);
            player.setHealth(player.getMaxHealth());
            player.hurtTime = 0;
            player.deathTime = 0;
            player.setDeltaMovement(0.0D, 0.0D, 0.0D);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player && isWearingFullSet(player) &&
                !GTNADamageTypes.isRealityRip(event.getSource())) {
            event.setCanceled(true);
            player.setHealth(player.getMaxHealth());
            player.hurtTime = 0;
            player.deathTime = 0;
        }
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player && isWearingFullSet(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingKnockback(LivingKnockBackEvent event) {
        if (event.getEntity() instanceof Player player && isWearingFullSet(player)) {
            event.setCanceled(true);
            player.setDeltaMovement(0.0D, player.getDeltaMovement().y, 0.0D);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            disableManagedFlight(player);
            restoreBootState(player);
            MANAGED_FLIGHT_STATES.remove(player.getUUID());
            MANAGED_BOOT_STATES.remove(player.getUUID());
        }
    }

    private static void applyHelmetEffects(ServerPlayer player) {
        if (!isWearingHelmet(player)) {
            return;
        }
        player.setAirSupply(player.getMaxAirSupply());
        player.getFoodData().setFoodLevel(20);
        player.getFoodData().setSaturation(20.0F);
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, false, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 300, 0, false, false, false));
    }

    private static void applyChestEffects(ServerPlayer player) {
        if (!isWearingChestplate(player)) {
            return;
        }

        player.setArrowCount(0);
        player.removeAllEffects();
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 4, false, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 300, 4, false, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300, 9, false, false, false));
    }

    private static void applyLegEffects(ServerPlayer player) {
        if (!isWearingLeggings(player)) {
            return;
        }
        player.clearFire();
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 300, 0, false, false, false));
    }

    private static void applyBootEffects(ServerPlayer player) {
        if (!isWearingBoots(player)) {
            restoreBootState(player);
            return;
        }

        MANAGED_BOOT_STATES.computeIfAbsent(player.getUUID(), ignored -> new BootState(
                player.maxUpStep(), copyEffect(player.getEffect(MobEffects.MOVEMENT_SPEED))));
        player.setMaxUpStep((float) COSMIC_STEP_HEIGHT);
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, BOOT_EFFECT_DURATION,
                BOOT_SPEED_AMPLIFIER, false, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.JUMP, BOOT_EFFECT_DURATION, 4, false, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, BOOT_EFFECT_DURATION, 0, false, false,
                false));
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, BOOT_EFFECT_DURATION, 0, false, false,
                false));
    }

    private static void sustainFullSet(ServerPlayer player) {
        player.setHealth(player.getMaxHealth());
        player.setAirSupply(player.getMaxAirSupply());
        player.getFoodData().setFoodLevel(20);
        player.getFoodData().setSaturation(20.0F);
        player.setRemainingFireTicks(0);
        if (player.tickCount % 20 == 0) {
            player.heal(player.getMaxHealth());
        }
    }

    private static void applyFlightState(ServerPlayer player) {
        boolean shouldManageFlight = isWearingFullSet(player) && !player.isSpectator() &&
                !player.getAbilities().instabuild;
        if (shouldManageFlight) {
            MANAGED_FLIGHT_STATES.computeIfAbsent(player.getUUID(), ignored -> captureFlightState(player));

            boolean changed = false;
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                changed = true;
            }
            if (Float.compare(player.getAbilities().getFlyingSpeed(), QUANTUM_FLYING_SPEED) != 0) {
                player.getAbilities().setFlyingSpeed(QUANTUM_FLYING_SPEED);
                changed = true;
            }
            if (changed) {
                player.onUpdateAbilities();
            }
            return;
        }
        disableManagedFlight(player);
    }

    private static void disableManagedFlight(ServerPlayer player) {
        if (player.isSpectator() || player.getAbilities().instabuild) {
            return;
        }

        FlightState previous = MANAGED_FLIGHT_STATES.remove(player.getUUID());
        if (previous != null) {
            boolean changed = player.getAbilities().mayfly != previous.mayfly() ||
                    player.getAbilities().flying != previous.flying() ||
                    Float.compare(player.getAbilities().getFlyingSpeed(), previous.flyingSpeed()) != 0;
            player.getAbilities().mayfly = previous.mayfly();
            player.getAbilities().flying = previous.flying();
            player.getAbilities().setFlyingSpeed(previous.flyingSpeed());
            if (changed) {
                player.onUpdateAbilities();
            }
            return;
        }

        // Compatibility cleanup for worlds where the old handler left the exact quantum speed behind
        // before this state tracking existed.
        boolean legacyQuantumSpeed = Float.compare(player.getAbilities().getFlyingSpeed(), QUANTUM_FLYING_SPEED) == 0;
        if (player.getAbilities().mayfly || player.getAbilities().flying || legacyQuantumSpeed) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.getAbilities().setFlyingSpeed(DEFAULT_FLYING_SPEED);
            player.onUpdateAbilities();
        }
    }

    private static void restoreBootState(ServerPlayer player) {
        BootState previous = MANAGED_BOOT_STATES.remove(player.getUUID());
        if (previous == null) {
            removeLegacyBootSpeedEffect(player);
            return;
        }

        player.setMaxUpStep(previous.maxUpStep());
        player.removeEffect(MobEffects.MOVEMENT_SPEED);
        if (previous.movementSpeed() != null) {
            player.addEffect(new MobEffectInstance(previous.movementSpeed()));
        }
    }

    private static void removeLegacyBootSpeedEffect(ServerPlayer player) {
        MobEffectInstance effect = player.getEffect(MobEffects.MOVEMENT_SPEED);
        if (effect != null && effect.getAmplifier() == BOOT_SPEED_AMPLIFIER &&
                effect.getDuration() <= BOOT_EFFECT_DURATION &&
                !effect.isAmbient() && !effect.isVisible() && !effect.showIcon()) {
            player.removeEffect(MobEffects.MOVEMENT_SPEED);
        }
    }

    private static FlightState captureFlightState(ServerPlayer player) {
        // The exact pair used by the old handler is treated as stale GTNA state when a world is
        // upgraded while the armor was already equipped.
        if (player.getAbilities().mayfly &&
                Float.compare(player.getAbilities().getFlyingSpeed(), QUANTUM_FLYING_SPEED) == 0) {
            return new FlightState(false, false, DEFAULT_FLYING_SPEED);
        }
        return new FlightState(player.getAbilities().mayfly, player.getAbilities().flying,
                player.getAbilities().getFlyingSpeed());
    }

    private static MobEffectInstance copyEffect(MobEffectInstance effect) {
        return effect == null ? null : new MobEffectInstance(effect);
    }

    private record FlightState(boolean mayfly, boolean flying, float flyingSpeed) {}

    private record BootState(float maxUpStep, MobEffectInstance movementSpeed) {}

    private static void reflectDamage(@org.jetbrains.annotations.Nullable Entity attacker, Player defender,
                                      float amount) {
        if (!(attacker instanceof LivingEntity livingAttacker) || attacker == defender) {
            return;
        }

        float reflectedDamage = Math.max(Float.MAX_VALUE / 4.0F, amount * 10000.0F);
        var source = GTNADamageTypes.realityRip(defender.level(), defender);

        if (livingAttacker instanceof ServerPlayer serverPlayer) {
            serverPlayer.getAbilities().invulnerable = false;
            serverPlayer.onUpdateAbilities();
        }

        livingAttacker.setHealth(0.0F);
        livingAttacker.hurt(source, reflectedDamage);
        livingAttacker.die(source);
    }

    public static boolean isWearingFullSet(Player player) {
        return isWearingHelmet(player) && isWearingChestplate(player) && isWearingLeggings(player) &&
                isWearingBoots(player);
    }

    private static boolean isWearingHelmet(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(GTNAItems.QUANTUM_COSMIC_NEXUS_HELMET.get());
    }

    private static boolean isWearingChestplate(Player player) {
        return player.getItemBySlot(EquipmentSlot.CHEST).is(GTNAItems.QUANTUM_COSMIC_NEXUS_CHESTPLATE.get());
    }

    private static boolean isWearingLeggings(Player player) {
        return player.getItemBySlot(EquipmentSlot.LEGS).is(GTNAItems.QUANTUM_COSMIC_NEXUS_LEGGINGS.get());
    }

    private static boolean isWearingBoots(Player player) {
        return player.getItemBySlot(EquipmentSlot.FEET).is(GTNAItems.QUANTUM_COSMIC_NEXUS_BOOTS.get());
    }
}
