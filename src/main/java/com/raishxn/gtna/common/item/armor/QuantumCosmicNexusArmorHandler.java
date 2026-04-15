package com.raishxn.gtna.common.item.armor;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.common.data.GTNADamageTypes;
import com.raishxn.gtna.common.data.GTNAItems;

@Mod.EventBusSubscriber(modid = GTNACORE.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class QuantumCosmicNexusArmorHandler {

    private static final double COSMIC_STEP_HEIGHT = 1.0D;

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
            resetStepHeight(player);
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
            resetStepHeight(player);
            return;
        }

        player.setMaxUpStep((float) COSMIC_STEP_HEIGHT);
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 9, false, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.JUMP, 300, 4, false, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 300, 0, false, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 300, 0, false, false, false));
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
        boolean shouldManageFlight = isWearingFullSet(player) && !player.isSpectator() && !player.getAbilities().instabuild;
        if (shouldManageFlight) {
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
            }
            player.getAbilities().setFlyingSpeed(0.2F);
            player.onUpdateAbilities();
            return;
        }
        disableManagedFlight(player);
    }

    private static void disableManagedFlight(ServerPlayer player) {
        if (player.isSpectator() || player.getAbilities().instabuild) {
            return;
        }
        if (player.getAbilities().mayfly || player.getAbilities().flying) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.getAbilities().setFlyingSpeed(0.05F);
            player.onUpdateAbilities();
        }
    }

    private static void resetStepHeight(ServerPlayer player) {
        player.setMaxUpStep(0.6F);
    }

    private static void reflectDamage(@org.jetbrains.annotations.Nullable Entity attacker, Player defender, float amount) {
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
