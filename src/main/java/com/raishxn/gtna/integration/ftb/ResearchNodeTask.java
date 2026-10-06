package com.raishxn.gtna.integration.ftb;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;

import com.raishxn.gtna.research.KnowledgeRegistry;
import com.raishxn.gtna.research.KnowledgeScope;
import com.raishxn.gtna.research.KnowledgeService;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.AbstractBooleanTask;
import dev.ftb.mods.ftbquests.quest.task.TaskType;

/**
 * Completes when the player's research scope (team or player) has unlocked a node. Checked once a
 * second and on login, so the quest finishes by itself when the node does. Only loaded when FTB Quests
 * is installed; see {@link GTNAQuestTypes}.
 */
public final class ResearchNodeTask extends AbstractBooleanTask {

    static TaskType type;

    private String node = "";

    public ResearchNodeTask(long id, Quest quest) {
        super(id, quest);
    }

    /** True when the player's scope holds the node. Unknown or malformed ids never complete. */
    public static boolean isUnlockedFor(ServerPlayer player, String node) {
        ResourceLocation id = ResourceLocation.tryParse(node);
        return id != null && KnowledgeService.isUnlocked(player.getServer(), KnowledgeScope.of(player), id);
    }

    @Override
    public TaskType getType() {
        return type;
    }

    @Override
    public void writeData(CompoundTag nbt) {
        super.writeData(nbt);
        nbt.putString("node", node);
    }

    @Override
    public void readData(CompoundTag nbt) {
        super.readData(nbt);
        node = nbt.getString("node");
    }

    @Override
    public void writeNetData(FriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        buffer.writeUtf(node, 256);
    }

    @Override
    public void readNetData(FriendlyByteBuf buffer) {
        super.readNetData(buffer);
        node = buffer.readUtf(256);
    }

    @Override
    public void fillConfigGroup(ConfigGroup config) {
        super.fillConfigGroup(config);
        config.addString("node", node, value -> node = value, "");
    }

    @Override
    public Component getAltTitle() {
        ResourceLocation id = ResourceLocation.tryParse(node);
        Component name = id == null ? Component.literal(node) : KnowledgeRegistry.graph().get(id)
                .map(found -> found.displayName()).orElseGet(() -> Component.literal(node));
        return Component.translatable("gtna.research.task", name);
    }

    @Override
    public Icon getAltIcon() {
        return ItemIcon.getItemIcon(Items.ENCHANTED_BOOK);
    }

    @Override
    public int autoSubmitOnPlayerTick() {
        return 20;
    }

    @Override
    public boolean checkOnLogin() {
        return true;
    }

    @Override
    public boolean canSubmit(TeamData teamData, ServerPlayer player) {
        return isUnlockedFor(player, node);
    }
}
