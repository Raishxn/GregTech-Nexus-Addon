package com.raishxn.gtna.client;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import com.raishxn.gtna.api.machine.multiblock.IGTNAModuleHost;

import java.util.HashMap;
import java.util.Map;

/**
 * Client-side half of {@code SModuleCountPacket}. Kept in a {@link OnlyIn}(CLIENT) class so the
 * packet class itself never references Minecraft's client classes: a direct reference would make
 * the dedicated-server GameTest crash while registering the network channel.
 */
@OnlyIn(Dist.CLIENT)
public final class ModuleCountClientHandler {

    private static final Map<BlockPos, Integer> PENDING = new HashMap<>();
    private static Level pendingLevel;

    private ModuleCountClientHandler() {}

    public static void apply(BlockPos pos, int count) {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        if (pendingLevel != level) {
            PENDING.clear();
            pendingLevel = level;
        }
        if (!applyToLoadedMachine(level, pos, count)) {
            PENDING.put(pos.immutable(), count);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Level level = Minecraft.getInstance().level;
        if (level != pendingLevel) {
            PENDING.clear();
            pendingLevel = level;
        }
        if (level == null || PENDING.isEmpty()) return;
        PENDING.entrySet().removeIf(entry -> applyToLoadedMachine(level, entry.getKey(), entry.getValue()));
    }

    private static boolean applyToLoadedMachine(Level level, BlockPos pos, int count) {
        if (level.getBlockEntity(pos) instanceof MetaMachineBlockEntity holder &&
                holder.getMetaMachine() instanceof IGTNAModuleHost host) {
            host.gtna$setFormedModuleCount(count);
            return true;
        }
        return false;
    }
}
