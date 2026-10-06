package com.raishxn.gtna.research;

import net.minecraft.resources.ResourceLocation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** How a node becomes available to unlock. Prerequisites are checked separately. */
public sealed interface KnowledgeTrigger {

    String type();

    /** Unlocked by producing or holding the item (detection arrives in a later phase). */
    record ObtainItem(ResourceLocation item) implements KnowledgeTrigger {

        public static final String TYPE = "obtain_item";
        static final Codec<ObtainItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("item").forGetter(ObtainItem::item))
                .apply(instance, ObtainItem::new));

        @Override
        public String type() {
            return TYPE;
        }
    }

    /** Only unlocked by commands, quests or other code. */
    record Manual() implements KnowledgeTrigger {

        public static final String TYPE = "manual";
        static final Codec<Manual> CODEC = Codec.unit(new Manual());

        @Override
        public String type() {
            return TYPE;
        }
    }

    Codec<KnowledgeTrigger> CODEC = Codec.STRING.partialDispatch("type",
            trigger -> DataResult.success(trigger.type()),
            type -> switch (type) {
                case ObtainItem.TYPE -> DataResult.success(ObtainItem.CODEC);
                case Manual.TYPE -> DataResult.success(Manual.CODEC);
                default -> DataResult.error(() -> "Unknown research trigger type: " + type);
            });
}
