package com.raishxn.gtna.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.common.data.GTNAItems;
import com.raishxn.gtna.config.ConfigHolder;

@Mod.EventBusSubscriber(modid = GTNACORE.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.side.isClient()) {
            Player player = event.player;
            if (player == Minecraft.getInstance().player) {
                // Only kill horizontal momentum for players wearing GTNA boots;
                // do not interfere with flight from Angel Rings or other mods.
                boolean wearingGtnaBoots = player.getItemBySlot(EquipmentSlot.FEET)
                        .is(GTNAItems.QUANTUM_COSMIC_NEXUS_BOOTS.get());
                if (ConfigHolder.INSTANCE.client.disableFlyInertia
                        && player.getAbilities().flying
                        && wearingGtnaBoots) {
                    if (player.zza == 0 && player.xxa == 0) {
                        player.setDeltaMovement(player.getDeltaMovement().multiply(0.0, 1.0, 0.0));
                    }
                }
            }
        }
    }
}
