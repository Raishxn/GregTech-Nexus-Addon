package com.raishxn.gtna.integration.ftb;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import com.raishxn.gtna.GTNACORE;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;

/** Registers GTNA's FTB Quests task types. Call only when FTB Quests is loaded. */
public final class GTNAQuestTypes {

    public static final ResourceLocation RESEARCH_NODE_ID = GTNACORE.id("research_node");

    private GTNAQuestTypes() {}

    public static void init() {
        if (ResearchNodeTask.type != null) return;
        ResearchNodeTask.type = TaskTypes.register(RESEARCH_NODE_ID, ResearchNodeTask::new,
                () -> ItemIcon.getItemIcon(Items.ENCHANTED_BOOK))
                .setDisplayName(Component.translatable("gtna.research.task.type"));
    }
}
