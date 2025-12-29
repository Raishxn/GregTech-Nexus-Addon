package com.raishxn.gtna.common.data;

import com.gregtechceu.gtceu.api.item.ComponentItem;
// ElectricStats removido pois não funciona em GTToolItem
import com.raishxn.gtna.common.item.GTNAVajraItem;
import com.raishxn.gtna.common.item.StructureDetectBehavior;
import com.raishxn.gtna.common.item.StructureWriteBehavior;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import static com.gregtechceu.gtceu.common.data.GTItems.attach;
import static com.raishxn.gtna.api.registry.GTNARegistry.REGISTRATE;

public class GTNAItems {

    static {
        REGISTRATE.creativeModeTab(() -> GTNACreativeModeTabs.GTNA_CORE_ITEMS);
    }

    public static ItemEntry<GTNAVajraItem> VAJRA;
    public static ItemEntry<ComponentItem> DEBUG_STRUCTURE_WRITER;
    public static ItemEntry<ComponentItem> STRUCTURE_DETECT;

    public static void init() {

        VAJRA = REGISTRATE.item("vajra", GTNAVajraItem::new)
                .lang("Vajra")
                .properties(p -> p.stacksTo(1))
                .tag(net.minecraft.tags.ItemTags.PICKAXES)
                .tag(net.minecraft.tags.ItemTags.AXES)
                .tag(net.minecraft.tags.ItemTags.SHOVELS)
                .tag(net.minecraft.tags.ItemTags.HOES)
                .tag(com.gregtechceu.gtceu.api.data.tag.TagUtil.createItemTag("tools/wrench"))
                .tag(com.gregtechceu.gtceu.api.data.tag.TagUtil.createItemTag("tools/wire_cutters"))
                .tag(com.gregtechceu.gtceu.api.data.tag.TagUtil.createItemTag("tools/crowbar"))
                .tag(com.gregtechceu.gtceu.api.data.tag.TagUtil.createItemTag("tools/screwdriver"))
                .tag(com.gregtechceu.gtceu.api.data.tag.TagUtil.createItemTag("tools/mortar"))  // Essencial para o Mortar
                .tag(com.gregtechceu.gtceu.api.data.tag.TagUtil.createItemTag("tools/hammer"))
                .tag(com.gregtechceu.gtceu.api.data.tag.TagUtil.createItemTag("tools/file"))    // Essencial para o File
                .tag(com.gregtechceu.gtceu.api.data.tag.TagUtil.createItemTag("tools/saw"))
                .tag(com.gregtechceu.gtceu.api.data.tag.TagUtil.createItemTag("tools/knife"))
                .tag(com.gregtechceu.gtceu.api.data.tag.TagUtil.createItemTag("tools/mallet"))
                .register();

        DEBUG_STRUCTURE_WRITER = REGISTRATE
                .item("debug_structure_writer", ComponentItem::create)
                .lang("Debug Structure Writer")
                .onRegister(attach(StructureWriteBehavior.INSTANCE))
                .model(NonNullBiConsumer.noop())
                .register();

        STRUCTURE_DETECT = REGISTRATE
                .item("structure_detect", ComponentItem::create)
                .lang("Structure Detector")
                .properties(stack -> stack.stacksTo(1))
                .onRegister(attach(StructureDetectBehavior.INSTANCE))
                .model(NonNullBiConsumer.noop())
                .register();
    }
}