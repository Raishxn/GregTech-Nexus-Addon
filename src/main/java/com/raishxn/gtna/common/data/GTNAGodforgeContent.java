package com.raishxn.gtna.common.data;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GlassBlock;

import com.raishxn.gtna.GTNACORE;
import com.tterrag.registrate.util.entry.BlockEntry;

import static com.raishxn.gtna.api.registry.GTNARegistry.REGISTRATE;

/**
 * Forge of Gods blocks from GTNH {@code BlockGodforgeCasings} (meta 0-7) and {@code BlockGodforgeGlass}. Textures
 * come from GTOCore's 1.20.1 port (CC BY-NC-SA 4.0).
 */
public final class GTNAGodforgeContent {

    static {
        REGISTRATE.creativeModeTab(() -> GTNACreativeModeTabs.BLOCKS);
    }

    /** Meta 0: structure letter B; module slots J use it too. */
    public static final BlockEntry<Block> SINGULARITY_SHIELDING_CASING = casing(
            "singularity_reinforced_stellar_shielding_casing");
    /** Meta 1: letter C. */
    public static final BlockEntry<Block> GUIDANCE_CASING = casing("celestial_matter_guidance_casing");
    /** Meta 2: letter D. */
    public static final BlockEntry<Block> BOUNDLESS_STRUCTURE_CASING = casing(
            "boundless_gravitationally_severed_structure_casing");
    /** Meta 3: letter E; bus/hatch slots A use it too. */
    public static final BlockEntry<Block> MAGNETIC_CONFINEMENT_CASING = casing(
            "transcendentally_amplified_magnetic_confinement_casing");
    /** Meta 4: letter F. */
    public static final BlockEntry<Block> STELLAR_ENERGY_SIPHON_CASING = casing("stellar_energy_siphon_casing");
    /** Meta 5: letter G, first ring. */
    public static final BlockEntry<Block> REMOTE_GRAVITON_FLOW_MODULATOR = modulator("remote_graviton_flow_modulator");
    /** Meta 6: letter K, second ring. */
    public static final BlockEntry<Block> MEDIAL_GRAVITON_FLOW_MODULATOR = modulator("medial_graviton_flow_modulator");
    /** Meta 7: letter I, third ring. */
    public static final BlockEntry<Block> CENTRAL_GRAVITON_FLOW_MODULATOR = modulator(
            "central_graviton_flow_modulator");
    /** Meta 8: module structure core of the Molten, Plasma and Exotic modules. */
    public static final BlockEntry<Block> HARMONIC_PHONON_TRANSMISSION_CONDUIT = casing(
            "harmonic_phonon_transmission_conduit");
    /**
     * GTNH Hypogen heating coil ({@code HeatingCoilLevel.UXV}, 12601K in GTNH; 12600K on the GTCEu scale). The
     * Smelting Module core; also a regular heating coil for every coil multiblock.
     */
    public static final BlockEntry<com.gregtechceu.gtceu.common.block.CoilBlock> HYPOGEN_COIL = coil("hypogen_coil",
            HypogenCoil.INSTANCE);
    /** GTNH Eternal heating coil, one step (900 K) above Hypogen; reaches the DTPF's 13,500 K recipes. */
    public static final BlockEntry<com.gregtechceu.gtceu.common.block.CoilBlock> ETERNAL_COIL = coil("eternal_coil",
            EternalCoil.INSTANCE);
    /** Letter H. */
    public static final BlockEntry<Block> GRAVITATIONAL_LENS = GTNABlocks.createCasingBlock(
            "spatially_transcendent_gravitational_lens", GlassBlock::new,
            GTNACORE.id("block/casings/godforge/spatially_transcendent_gravitational_lens"), () -> Blocks.GLASS,
            () -> RenderType::cutout);

    private GTNAGodforgeContent() {}

    /** The Hypogen coil tier. Level/discount continue GTCEu's doubling after Tritanium (16/8). */
    public enum HypogenCoil implements com.gregtechceu.gtceu.api.block.ICoilType {

        INSTANCE;

        @Override
        public String getName() {
            return "hypogen";
        }

        @Override
        public int getCoilTemperature() {
            return 12600;
        }

        @Override
        public int getLevel() {
            return 32;
        }

        @Override
        public int getEnergyDiscount() {
            return 16;
        }

        /** One past Tritanium (ordinal 7) in GTCEu's coil order. */
        @Override
        public int getTier() {
            return 8;
        }

        @Override
        public com.gregtechceu.gtceu.api.data.chemical.material.Material getMaterial() {
            return com.gregtechceu.gtceu.common.data.GTMaterials.Neutronium;
        }

        @Override
        public net.minecraft.resources.ResourceLocation getTexture() {
            return GTNACORE.id("block/casings/coils/machine_coil_hypogen");
        }
    }

    /** GTNH Eternal coil (Modernity-GTNH textures). */
    public enum EternalCoil implements com.gregtechceu.gtceu.api.block.ICoilType {

        INSTANCE;

        @Override
        public String getName() {
            return "eternal";
        }

        @Override
        public int getCoilTemperature() {
            return 13500;
        }

        @Override
        public int getLevel() {
            return 64;
        }

        @Override
        public int getEnergyDiscount() {
            return 32;
        }

        @Override
        public int getTier() {
            return 9;
        }

        @Override
        public com.gregtechceu.gtceu.api.data.chemical.material.Material getMaterial() {
            return com.raishxn.gtna.common.data.material.GodforgeChainMaterials.Eternity;
        }

        @Override
        public net.minecraft.resources.ResourceLocation getTexture() {
            return GTNACORE.id("block/casings/coils/machine_coil_eternal");
        }
    }

    private static BlockEntry<com.gregtechceu.gtceu.common.block.CoilBlock> coil(String name,
                                                                                 com.gregtechceu.gtceu.api.block.ICoilType type) {
        var block = REGISTRATE
                .block(name, p -> new com.gregtechceu.gtceu.common.block.CoilBlock(p, type))
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
                .addLayer(() -> RenderType::cutoutMipped)
                .blockstate(com.gregtechceu.gtceu.common.data.models.GTModels.createCoilModel(type))
                .tag(com.gregtechceu.gtceu.data.recipe.CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH)
                .item(net.minecraft.world.item.BlockItem::new)
                .build()
                .register();
        com.gregtechceu.gtceu.api.GTCEuAPI.HEATING_COILS.put(type, block);
        return block;
    }

    private static BlockEntry<Block> casing(String name) {
        return GTNABlocks.createCasingBlock(name, GTNACORE.id("block/casings/godforge/" + name));
    }

    /** Graviton flow modulators: modulator texture on the sides, outer casing on top and bottom (GTNH). */
    private static BlockEntry<Block> modulator(String name) {
        return REGISTRATE.block(name, Block::new)
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(p -> p.mapColor(net.minecraft.world.level.material.MapColor.METAL).strength(5.0f, 6.0f)
                        .sound(net.minecraft.world.level.block.SoundType.METAL).requiresCorrectToolForDrops()
                        .isValidSpawn((state, level, pos, ent) -> false))
                .addLayer(() -> RenderType::solid)
                .blockstate((ctx, prov) -> prov.simpleBlock(ctx.get(), prov.models().cubeColumn(ctx.getName(),
                        GTNACORE.id("block/casings/godforge/" + name),
                        GTNACORE.id("block/casings/godforge/transcendentally_amplified_magnetic_confinement_casing"))))
                .tag(com.gregtechceu.gtceu.api.item.tool.GTToolType.WRENCH.harvestTags.get(0),
                        net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE)
                .item(com.raishxn.gtna.common.block.GTNABlockItem::new)
                .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), prov.modLoc("block/" + ctx.getName())))
                .build()
                .register();
    }

    private static final java.util.Set<String> GODFORGE_BLOCKS = java.util.Set.of(
            "singularity_reinforced_stellar_shielding_casing", "celestial_matter_guidance_casing",
            "boundless_gravitationally_severed_structure_casing",
            "transcendentally_amplified_magnetic_confinement_casing", "stellar_energy_siphon_casing",
            "remote_graviton_flow_modulator", "medial_graviton_flow_modulator", "central_graviton_flow_modulator",
            "harmonic_phonon_transmission_conduit", "spatially_transcendent_gravitational_lens");

    public static boolean isGodforgeBlock(String id) {
        return GODFORGE_BLOCKS.contains(id);
    }

    public static void init() {}
}
