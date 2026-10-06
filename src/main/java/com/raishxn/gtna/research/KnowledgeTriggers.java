package com.raishxn.gtna.research;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Unlocks nodes whose trigger is {@code obtain_item}, or, for nodes bought with points, records that the
 * item requirement is met. A player counts as having obtained an item when
 * it is in their inventory, so crafting, machine output, trading and pickup all work. The unlock goes to
 * the player's scope, so one team member finding the item is enough for the whole team.
 */
public final class KnowledgeTriggers {

    /** How often the inventory is scanned, in ticks. Events below make crafting and pickup immediate. */
    static final int SCAN_INTERVAL = 20;

    private KnowledgeTriggers() {}

    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % SCAN_INTERVAL != 0) return;
        scan(player, ItemStack.EMPTY);
    }

    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            scan(player, event.getCrafting());
            count(player, ResearchSources.index().craft(), event.getCrafting());
        }
    }

    public static void onItemPickup(PlayerEvent.ItemPickupEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) scan(player, event.getStack());
    }

    public static void onItemSmelted(PlayerEvent.ItemSmeltedEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            scan(player, event.getSmelting());
            count(player, ResearchSources.index().smelt(), event.getSmelting());
        }
    }

    /** Pays the craft or smelt sources of the item, counting the whole stack. */
    public static void count(ServerPlayer player, Map<ResourceLocation, List<ResearchSource>> sources,
                             ItemStack stack) {
        if (sources.isEmpty() || stack.isEmpty()) return;
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        List<ResearchSource> matching = id == null ? null : sources.get(id);
        if (matching == null) return;
        ResearchSources.recordAll(player.getServer(), KnowledgeScope.of(player), matching, stack.getCount());
    }

    /** Pays the obtain sources of everything the player holds; each counts only the first time. */
    static void obtained(ServerPlayer player, ItemStack extra) {
        var obtain = ResearchSources.index().obtain();
        if (obtain.isEmpty()) return;
        Set<ResourceLocation> held = new HashSet<>();
        for (ItemStack stack : player.getInventory().items) addIfKnown(obtain, held, stack);
        for (ItemStack stack : player.getInventory().armor) addIfKnown(obtain, held, stack);
        for (ItemStack stack : player.getInventory().offhand) addIfKnown(obtain, held, stack);
        addIfKnown(obtain, held, extra);
        UUID scope = KnowledgeScope.of(player);
        for (ResourceLocation item : held) ResearchSources.recordAll(player.getServer(), scope, obtain.get(item), 1);
    }

    private static void addIfKnown(Map<ResourceLocation, List<ResearchSource>> sources, Set<ResourceLocation> held,
                                   ItemStack stack) {
        if (stack.isEmpty()) return;
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id != null && sources.containsKey(id)) held.add(id);
    }

    /**
     * Unlocks every node whose item the player holds (or just received in {@code extra}) and whose
     * prerequisites are met. Repeats until nothing changes, so a chain whose items are all held unlocks
     * in one pass.
     *
     * @return how many nodes were newly unlocked.
     */
    public static int scan(ServerPlayer player, ItemStack extra) {
        obtained(player, extra);
        KnowledgeGraph graph = KnowledgeRegistry.graph();
        if (graph.isEmpty() || !graph.hasItemTriggers()) return 0;
        Set<ResourceLocation> held = new HashSet<>();
        for (ItemStack stack : player.getInventory().items) addIfTriggering(graph, held, stack);
        for (ItemStack stack : player.getInventory().armor) addIfTriggering(graph, held, stack);
        for (ItemStack stack : player.getInventory().offhand) addIfTriggering(graph, held, stack);
        addIfTriggering(graph, held, extra);
        if (held.isEmpty()) return 0;

        UUID scope = KnowledgeScope.of(player);
        var server = player.getServer();
        int total = 0;
        boolean changed = true;
        while (changed) {
            changed = false;
            for (ResourceLocation item : held) {
                for (KnowledgeNode node : graph.triggeredByItem(item)) {
                    if (node.purchasable()) {
                        // A bought node only records that the item was held; the team still buys it.
                        if (KnowledgeService.markRequirement(server, scope, node.id())) requirementMet(player, node);
                        continue;
                    }
                    if (KnowledgeService.unlock(server, scope, node.id(), false) ==
                            KnowledgeService.UnlockResult.UNLOCKED) {
                        notify(player, node);
                        total++;
                        changed = true;
                    }
                }
            }
        }
        return total;
    }

    private static void addIfTriggering(KnowledgeGraph graph, Set<ResourceLocation> held, ItemStack stack) {
        if (stack.isEmpty()) return;
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id != null && !graph.triggeredByItem(id).isEmpty()) held.add(id);
    }

    private static void requirementMet(ServerPlayer player, KnowledgeNode node) {
        player.displayClientMessage(Component.translatable("gtna.research.requirement_met", node.displayName()), false);
    }

    private static void notify(ServerPlayer player, KnowledgeNode node) {
        player.displayClientMessage(Component.translatable("gtna.research.unlocked", node.displayName()), false);
        player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS,
                0.6F, 1.4F);
    }
}
