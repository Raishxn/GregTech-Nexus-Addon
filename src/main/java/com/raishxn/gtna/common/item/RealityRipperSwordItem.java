package com.raishxn.gtna.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.List;

import com.raishxn.gtna.common.data.GTNADamageTypes;

public class RealityRipperSwordItem extends Item {

    public RealityRipperSwordItem(Properties properties) {
        super(properties.stacksTo(1).fireResistant());
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.level().isClientSide) {
            realityRip(target, attacker);
        }
        return true;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget,
                                                  net.minecraft.world.InteractionHand usedHand) {
        if (!player.level().isClientSide) {
            realityRip(interactionTarget, player);
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (!player.level().isClientSide) {
            realityRip(entity, player);
        }
        return true;
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Reality-ripping strike.")
                .withStyle(ChatFormatting.DARK_RED));
        tooltip.add(Component.literal("Kills through armor, invulnerability and creative mode.")
                .withStyle(ChatFormatting.GOLD));
    }

    private static void realityRip(Entity target, LivingEntity attacker) {
        var source = GTNADamageTypes.realityRip(attacker.level(), attacker);

        if (target instanceof ServerPlayer player) {
            boolean oldInvulnerable = player.getAbilities().invulnerable;
            player.getAbilities().invulnerable = false;
            player.onUpdateAbilities();
            player.setHealth(0.0F);
            player.hurt(source, Float.MAX_VALUE);
            player.die(source);
            return;
        }

        if (target instanceof LivingEntity living) {
            living.setHealth(0.0F);
            living.hurt(source, Float.MAX_VALUE);
            living.die(source);
            return;
        }

        target.kill();
    }
}
