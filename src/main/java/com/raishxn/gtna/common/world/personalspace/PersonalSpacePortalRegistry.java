package com.raishxn.gtna.common.world.personalspace;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import com.raishxn.gtna.GTNACORE;
import com.tterrag.registrate.providers.ProviderType;

import static com.raishxn.gtna.api.registry.GTNARegistry.REGISTRATE;

public final class PersonalSpacePortalRegistry {

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,
            GTNACORE.MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
            GTNACORE.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister
            .create(ForgeRegistries.BLOCK_ENTITY_TYPES, GTNACORE.MOD_ID);

    public static final RegistryObject<PersonalSpacePortalBlock> PORTAL = BLOCKS.register("personal_space_portal",
            PersonalSpacePortalBlock::new);
    public static final RegistryObject<Item> PORTAL_ITEM = ITEMS.register("personal_space_portal",
            () -> new PersonalSpacePortalItem(PORTAL.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<BlockEntityType<PersonalSpacePortalEntity>> PORTAL_ENTITY = BLOCK_ENTITIES
            .register("personal_space_portal", () -> BlockEntityType.Builder
                    .of(PersonalSpacePortalEntity::new, PORTAL.get()).build(null));

    private PersonalSpacePortalRegistry() {}

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, provider -> provider
                .addTag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ResourceKey.create(Registries.BLOCK, GTNACORE.id("personal_space_portal"))));
    }
}
