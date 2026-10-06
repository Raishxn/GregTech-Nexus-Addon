package com.raishxn.gtna.common.data;

import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.common.item.TooltipBehavior;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.common.block.EyeOfHarmonyBlockItem;
import com.raishxn.gtna.common.block.EyeOfHarmonyFieldBlock;
import com.raishxn.gtna.common.block.EyeOfHarmonyFieldBlock.Family;
import com.raishxn.gtna.common.block.EyeOfHarmonyPlanetBlock;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.ItemEntry;

import static com.gregtechceu.gtceu.common.data.GTItems.attach;
import static com.raishxn.gtna.api.registry.GTNARegistry.REGISTRATE;

/** GTNH content foundation. Existing controllers and old casing IDs keep their current behavior. */
public final class GTNAEyeOfHarmonyContent {

    public static final String[] TIER_NAMES = {
            "Crude", "Primitive", "Stable", "Advanced", "Superb", "Exotic", "Perfect", "Tipler", "Gallifreyan"
    };

    static {
        REGISTRATE.creativeModeTab(() -> GTNACreativeModeTabs.BLOCKS);
    }

    public static final BlockEntry<EyeOfHarmonyFieldBlock>[] COMPRESSION_FIELDS = fields(Family.COMPRESSION,
            "spacetime_compression_field_generator");
    public static final BlockEntry<EyeOfHarmonyFieldBlock>[] ACCELERATION_FIELDS = fields(Family.ACCELERATION,
            "time_acceleration_field_generator");
    public static final BlockEntry<EyeOfHarmonyFieldBlock>[] STABILISATION_FIELDS = fields(Family.STABILISATION,
            "stabilisation_field_generator");
    public static final BlockEntry<Block> BOUNDARY_CASING = casing("infinite_spacetime_energy_boundary_casing",
            "Infinite Spacetime Energy Boundary Casing", "boundary");
    public static final BlockEntry<Block> SPATIAL_CASING = casing("reinforced_spatial_structure_casing",
            "Reinforced Spatial Structure Casing", "spatial");
    public static final BlockEntry<Block> TEMPORAL_CASING = casing("reinforced_temporal_structure_casing",
            "Reinforced Temporal Structure Casing", "temporal");
    public static final BlockEntry<EyeOfHarmonyPlanetBlock> OVERWORLD_PLANET = planet("overworld");
    public static final BlockEntry<EyeOfHarmonyPlanetBlock> NETHER_PLANET = planet("nether");
    public static final BlockEntry<EyeOfHarmonyPlanetBlock> END_PLANET = planet("end");

    public static final ItemEntry<ComponentItem> ASTRAL_ARRAY_FABRICATOR;
    static {
        REGISTRATE.creativeModeTab(() -> GTNACreativeModeTabs.ITEMS);
        ASTRAL_ARRAY_FABRICATOR = REGISTRATE.item("astral_array_fabricator", ComponentItem::create)
                .lang("Astral Array Fabricator")
                .onRegister(attach(new TooltipBehavior(lines -> {
                    lines.add(Component.translatable("gtna.eoh.astral_array"));
                    lines.add(Component.translatable("gtna.eoh.component_stage"));
                    lines.add(GTNASources.line(GTNASources.GTNH));
                })))
                .model((ctx, prov) -> prov.generated(ctx, GTNACORE.id("item/astral_array_fabricator")))
                .register();
        REGISTRATE.creativeModeTab(() -> GTNACreativeModeTabs.BLOCKS);
    }

    private GTNAEyeOfHarmonyContent() {}

    @SuppressWarnings("unchecked")
    private static BlockEntry<EyeOfHarmonyFieldBlock>[] fields(Family family, String prefix) {
        BlockEntry<EyeOfHarmonyFieldBlock>[] entries = new BlockEntry[9];
        for (int tier = 0; tier < entries.length; tier++) {
            final int index = tier;
            String name = prefix + "_tier_" + tier;
            entries[tier] = REGISTRATE.block(name, props -> new EyeOfHarmonyFieldBlock(props, family, index))
                    .initialProperties(() -> Blocks.IRON_BLOCK)
                    .properties(props -> props.mapColor(MapColor.METAL).strength(5.0f, 6.0f)
                            .sound(SoundType.METAL).requiresCorrectToolForDrops()
                            .isValidSpawn((state, level, pos, entity) -> false))
                    .lang(TIER_NAMES[tier] + " " + family.englishName + " Field Generator")
                    .addLayer(() -> RenderType::solid)
                    .blockstate((ctx, prov) -> prov.simpleBlock(ctx.get(), prov.models().cubeAll(ctx.getName(),
                            GTNACORE.id("block/eye_of_harmony/" + family.id + "/tier_" + index))))
                    .tag(GTToolType.WRENCH.harvestTags.get(0), BlockTags.MINEABLE_WITH_PICKAXE)
                    .item(EyeOfHarmonyBlockItem::new).build().register();
        }
        return entries;
    }

    private static BlockEntry<Block> casing(String name, String englishName, String texture) {
        return REGISTRATE.block(name, Block::new).initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(props -> props.mapColor(MapColor.METAL).strength(5.0f, 6.0f)
                        .sound(SoundType.METAL).requiresCorrectToolForDrops()
                        .isValidSpawn((state, level, pos, entity) -> false))
                .lang(englishName).addLayer(() -> RenderType::solid)
                .blockstate((ctx, prov) -> prov.simpleBlock(ctx.get(), prov.models().cubeAll(ctx.getName(),
                        GTNACORE.id("block/eye_of_harmony/" + texture))))
                .tag(GTToolType.WRENCH.harvestTags.get(0), BlockTags.MINEABLE_WITH_PICKAXE)
                .item(EyeOfHarmonyBlockItem::new).build().register();
    }

    private static BlockEntry<EyeOfHarmonyPlanetBlock> planet(String world) {
        return REGISTRATE.block("eye_of_harmony_planet_" + world,
                props -> new EyeOfHarmonyPlanetBlock(props,
                        ResourceLocation.parse("minecraft:" + ("overworld".equals(world) ? world : "the_" + world))))
                .initialProperties(() -> Blocks.STONE)
                .properties(props -> props.strength(1.5f, 6.0f).requiresCorrectToolForDrops())
                .lang(switch (world) {
                    case "overworld" -> "Overworld Planet Block";
                    case "nether" -> "Nether Planet Block";
                    default -> "End Planet Block";
                })
                .addLayer(() -> RenderType::solid)
                .blockstate((ctx, prov) -> prov.simpleBlock(ctx.get(), prov.models().cube(ctx.getName(),
                        planetTexture(world, "bottom"), planetTexture(world, "top"),
                        planetTexture(world, "back"), planetTexture(world, "front"),
                        planetTexture(world, "left"), planetTexture(world, "right"))
                        .texture("particle", planetTexture(world, "front"))))
                .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .item(EyeOfHarmonyBlockItem::new).build().register();
    }

    private static ResourceLocation planetTexture(String world, String face) {
        return GTNACORE.id("block/eye_of_harmony/planet/" + world + "_" + face);
    }

    public static void init() {}
}
