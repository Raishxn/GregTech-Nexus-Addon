package com.raishxn.gtna.common.item.armor;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.common.data.GTNAItems;

@Mod.EventBusSubscriber(modid = GTNACORE.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DiscordNitroArmorHandler {

    private static final String SHIELD_COOLDOWN_TAG = "gtnaDiscordNitroShieldCooldown";
    private static final float MAX_SHIELD = 16.0F;
    private static final int SHIELD_REGEN_DELAY = 100;

    private DiscordNitroArmorHandler() {}

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }

        applyFlightState(player);
        applyPerPieceEffects(player);

        if (isWearingFullSet(player)) {
            rechargeShield(player);
        } else {
            resetShield(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) {
            return;
        }
        if (isWearingFullSet(player)) {
            player.getPersistentData().putInt(SHIELD_COOLDOWN_TAG, SHIELD_REGEN_DELAY);
        }
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player && isWearingFullSet(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            disableManagedFlight(player);
        }
    }

    private static void applyPerPieceEffects(ServerPlayer player) {
        if (isWearingHelmet(player)) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 220, 0, false, false, false));
        }
        if (isWearingLeggings(player)) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 220, 1, false, false, false));
        }
        if (isWearingBoots(player)) {
            player.addEffect(new MobEffectInstance(MobEffects.JUMP, 220, 1, false, false, false));
        }
        if (isWearingChestplate(player) && !isWearingFullSet(player)) {
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 40, 0, false, false, false));
        }
    }

    private static void rechargeShield(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        int cooldown = data.getInt(SHIELD_COOLDOWN_TAG);
        if (cooldown > 0) {
            data.putInt(SHIELD_COOLDOWN_TAG, cooldown - 1);
            return;
        }

        if (player.tickCount % 20 == 0 && player.getAbsorptionAmount() < MAX_SHIELD) {
            player.setAbsorptionAmount(Math.min(MAX_SHIELD, player.getAbsorptionAmount() + 2.0F));
        }
    }

    private static void resetShield(ServerPlayer player) {
        player.getPersistentData().remove(SHIELD_COOLDOWN_TAG);
        if (player.getAbsorptionAmount() > MAX_SHIELD) {
            player.setAbsorptionAmount(MAX_SHIELD);
        }
    }

    private static void applyFlightState(ServerPlayer player) {
        boolean shouldManageFlight = isWearingFullSet(player) && !player.isSpectator() && !player.getAbilities().instabuild;
        if (shouldManageFlight) {
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
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
        if (player.getAbilities().mayfly) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }
    }

    public static boolean isWearingFullSet(Player player) {
        return isWearingHelmet(player) && isWearingChestplate(player) && isWearingLeggings(player) &&
                isWearingBoots(player);
    }

    public static boolean shouldRenderWings(Player player) {
        return isWearingFullSet(player) && (player.getAbilities().flying || player.isFallFlying());
    }

    private static boolean isWearingHelmet(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(GTNAItems.DISCORD_NITRO_HELMET.get());
    }

    private static boolean isWearingChestplate(Player player) {
        return player.getItemBySlot(EquipmentSlot.CHEST).is(GTNAItems.DISCORD_NITRO_CHESTPLATE.get());
    }

    private static boolean isWearingLeggings(Player player) {
        return player.getItemBySlot(EquipmentSlot.LEGS).is(GTNAItems.DISCORD_NITRO_LEGGINGS.get());
    }

    private static boolean isWearingBoots(Player player) {
        return player.getItemBySlot(EquipmentSlot.FEET).is(GTNAItems.DISCORD_NITRO_BOOTS.get());
    }
}
