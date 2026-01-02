package com.raishxn.gtna.common.data;

import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.raishxn.gtna.GTNACORE;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import java.util.function.Supplier;

import static com.raishxn.gtna.api.registry.GTNARegistry.REGISTRATE;

public class GTNABlocks {

    static {
        REGISTRATE.creativeModeTab(() -> GTNACreativeModeTabs.BLOCKS);
    }

    public static final BlockEntry<Block> BREEL_PIPE_CASING = createCasingBlock("breel_pipe_casing");
    public static final BlockEntry<Block> HYPER_PRESSURE_BREEL_CASING = createCasingBlock("hyper_pressure_breel_casing");
    public static final BlockEntry<Block> STEAM_COMPACT_PIPE_CASING = createCasingBlock("steam_compact_pipe_casing");
    public static final BlockEntry<Block> VIBRATION_SAFE_CASING = createCasingBlock("vibration_safe_casing");
    public static final BlockEntry<Block> BRONZE_REINFORCED_WOOD = createCasingBlock("bronze_reinforced_wood");
    public static final BlockEntry<Block> STEEL_REINFORCED_WOOD = createCasingBlock("steel_reinforced_wood");
    public static final BlockEntry<Block> IRON_REINFORCED_WOOD = createCasingBlock("iron_reinforced_wood");
    public static final BlockEntry<Block> SOLAR_BOILING_CELL = createSolarCasingBlock("solar_boiling_cell");
    public static final BlockEntry<Block> STRONZE_WRAPPED_CASING = createCasingBlock("stronze_wrapped_casing"); // Usando Bronze como base material
    public static final BlockEntry<Block> HYDRAULIC_ASSEMBLER_CASING = createCasingBlock("hydraulic_assembler_casing");
    public static final BlockEntry<Block> BREEL_PLATED_CASING = createCasingBlock("breel_plated_casing");
    public static final BlockEntry<Block> BOROSILICATE_GLASS_BLOCK = createGlassCasingBlock(
            "borosilicate_glass", GTNACORE.id("block/casings/borosilicate_glass"), () -> RenderType::cutoutMipped);












    public static BlockEntry<Block> createCasingBlock(String name) {
        return createCasingBlock(name, Block::new, GTNACORE.id("block/casings/" + name), () -> Blocks.IRON_BLOCK, () -> RenderType::solid);
    }

    @SuppressWarnings("all")
    public static BlockEntry<Block> createCasingBlock(String name,
                                                      NonNullFunction<BlockBehaviour.Properties, Block> blockSupplier,
                                                      ResourceLocation texture,
                                                      NonNullSupplier<? extends Block> properties,
                                                      Supplier<Supplier<RenderType>> type) {
        return REGISTRATE.block(name, blockSupplier)
                .initialProperties(properties)
                .properties(p -> p
                        .mapColor(MapColor.METAL)
                        .strength(5.0f, 6.0f)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .isValidSpawn((state, level, pos, ent) -> false))
                .addLayer(type)
                .blockstate((ctx, prov) -> prov.simpleBlock(ctx.get(), prov.models().cubeAll(ctx.getName(), texture)))
                .tag(GTToolType.WRENCH.harvestTags.get(0), BlockTags.MINEABLE_WITH_PICKAXE)
                .item(BlockItem::new)
                .build()
                .register();
    }
    private static BlockEntry<Block> createSolarCasingBlock(String name) {
        return REGISTRATE.block(name, Block::new)
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(p -> p.mapColor(MapColor.METAL).strength(5.0f, 6.0f).sound(SoundType.METAL).requiresCorrectToolForDrops())
                .addLayer(() -> RenderType::solid)
                .blockstate((ctx, prov) -> {
                    prov.simpleBlock(ctx.get(), prov.models().cube(ctx.getName(),
                            prov.modLoc("block/casings/" + name), // down
                            prov.modLoc("block/overlay/block/solar_boiling_cell_top_overlay"), // up
                            prov.modLoc("block/casings/" + name), // north
                            prov.modLoc("block/casings/" + name), // south
                            prov.modLoc("block/casings/" + name), // east
                            prov.modLoc("block/casings/" + name)  // west
                    ));
                })
                .tag(GTToolType.WRENCH.harvestTags.get(0), BlockTags.MINEABLE_WITH_PICKAXE)
                .item(BlockItem::new)

                .build()
                .register();
    }
    private static BlockEntry<Block> createGlassCasingBlock(String name, ResourceLocation texture,
                                                            Supplier<Supplier<RenderType>> type) {
        return createCasingBlock(name, GlassBlock::new, texture, () -> Blocks.GLASS, type);
    }
}