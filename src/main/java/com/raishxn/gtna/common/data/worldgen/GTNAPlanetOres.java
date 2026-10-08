package com.raishxn.gtna.common.data.worldgen;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.OreProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.GTLayerPattern;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.SimpleWorldGenLayer;
import com.gregtechceu.gtceu.api.data.worldgen.generator.indicators.SurfaceIndicatorGenerator;
import com.gregtechceu.gtceu.api.data.worldgen.generator.veins.DikeVeinGenerator;
import com.gregtechceu.gtceu.api.data.worldgen.generator.veins.VeinedVeinGenerator;
import com.gregtechceu.gtceu.common.data.GTOres;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.fml.loading.LoadingModList;

import com.raishxn.gtna.GTNACORE;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

/**
 * Real GTCEu ore veins on the five Ad Astra planets (Moon, Mars, Venus, Mercury, Glacio), only when Ad Astra is
 * loaded. Each planet stone gets its own ore prefix ({@code moon_stone}, ...) limited to the materials of that
 * planet's veins, so the registry gains about 140 ore blocks instead of one per ore material and planet.
 *
 * <p>
 * The 33 vein definitions are ported from GTOCore {@code GTOOres} (LGPL-3.0), translated to the GTCEu 7.5.3
 * builder API and restricted to these planets. The GTO-only veins (Celestine, Desh, Calorite, Ostrum, Zircon) are
 * left out; Ad Astra still generates its own Desh/Ostrum/Calorite ores.
 */
public final class GTNAPlanetOres {

    public static final String AD_ASTRA = "ad_astra";
    /** Every planet stone; the tag lists them as optional entries so it also loads without Ad Astra. */
    public static final TagKey<Block> PLANET_STONES = BlockTags.create(GTNACORE.id("planet_ore_replaceables"));

    public enum Planet {

        MOON("Moon"),
        MARS("Mars"),
        VENUS("Venus"),
        MERCURY("Mercury"),
        GLACIO("Glacio");

        public final String id;
        public final String englishName;
        public final ResourceKey<Level> dimension;
        private TagPrefix orePrefix;

        Planet(String englishName) {
            this.id = name().toLowerCase(java.util.Locale.ROOT);
            this.englishName = englishName;
            this.dimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(AD_ASTRA, id));
        }

        public ResourceLocation stone() {
            return new ResourceLocation(AD_ASTRA, id + "_stone");
        }

        /** Null without Ad Astra. */
        public TagPrefix orePrefix() {
            return orePrefix;
        }

        /** Ore materials of this planet's veins; every other material is ignored by its ore prefix. */
        public Set<Material> oreMaterials() {
            return switch (this) {
                case MOON -> Set.of(Aluminium, Asbestos, Bastnasite, Bauxite, Cassiterite, CassiteriteSand, Diatomite,
                        GarnetSand, GlauconiteSand, Gold, Ilmenite, Magnetite, Monazite, Neodymium, Pentlandite,
                        Pitchblende, Soapstone, Talc, Tin, Uraninite, VanadiumMagnetite);
                case MARS -> Set.of(Alunite, Amethyst, Apatite, BasalticMineralSand, Borax, Bornite, Chalcopyrite,
                        Cooperite, Copper, Diatomite, Electrotine, FullersEarth, GarnetRed, GarnetYellow, Goethite,
                        Gold, GraniticMineralSand, Gypsum, Hematite, Iron, Kyanite, Lepidolite, Lithium, Magnetite,
                        Malachite, Mica, Opal, Palladium, Platinum, Pollucite, Pyrite, Pyrochlore, RockSalt, Salt,
                        Saltpeter, Scheelite, Stibnite, Tetrahedrite, TricalciumPhosphate, Tungstate, VanadiumMagnetite,
                        YellowLimonite);
                case VENUS -> Set.of(Bentonite, Borax, Chromite, Coal, Diamond, Galena, GlauconiteSand, Goethite, Gold,
                        Graphite, Hematite, Lead, Lepidolite, Magnetite, Molybdenite, Molybdenum, Olivine, Powellite,
                        Pyrite, RockSalt, Salt, Silver, Sphalerite, Sulfur, VanadiumMagnetite, Wulfenite,
                        YellowLimonite);
                case MERCURY -> Set.of(Alunite, BlueTopaz, Bornite, Cassiterite, Chalcocite, Chalcopyrite, Cobaltite,
                        Cooperite, Diatomite, Electrotine, Garnierite, Grossular, Nickel, Palladium, Pentlandite,
                        Platinum, Pyrolusite, Realgar, Saltpeter, Spessartine, Tantalite, Topaz, Zeolite);
                case GLACIO -> Set.of(Asbestos, Bastnasite, Bornite, Calcite, Cassiterite, CassiteriteSand, Coal,
                        Cooperite, Diatomite, GarnetSand, Kyanite, Lapis, Lazurite, Lepidolite, Lithium, Mica, Monazite,
                        Neodymium, Oilsands, Palladium, Platinum, Pollucite, RockSalt, Salt, Scheelite, Sodalite,
                        Spodumene, Tin, Tungstate);
            };
        }
    }

    private static SimpleWorldGenLayer LAYER;
    private static RuleTest[] RULES;

    private GTNAPlanetOres() {}

    public static boolean enabled() {
        return LoadingModList.get() != null && LoadingModList.get().getModFileById(AD_ASTRA) != null;
    }

    /**
     * Borax is the main ore of the Mars/Venus borax vein but has no ore property in GTCEu 7.5.3 (GTO/GTNH have one).
     */
    public static void modifyMaterials() {
        if (enabled() && !Borax.hasProperty(PropertyKey.ORE)) Borax.setProperty(PropertyKey.ORE, new OreProperty());
    }

    /** {@code IGTAddon#registerTagPrefixes}. */
    public static void registerTagPrefixes() {
        if (!enabled()) return;
        for (Planet planet : Planet.values()) {
            planet.orePrefix = new PlanetOrePrefix(planet)
                    .defaultTagPath("ores/%s")
                    .prefixOnlyTagPath("ores_in_ground/%s")
                    .unformattedTagPath("ores")
                    .materialIconType(com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType.ore)
                    .miningToolTag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .unificationEnabled(true)
                    .blockConstructor(com.gregtechceu.gtceu.api.block.OreBlock::new)
                    .generationCondition(TagPrefix.Conditions.hasOreProperty)
                    .langValue(planet.englishName + " %s Ore")
                    .registerOre(() -> stoneState(planet), null,
                            BlockBehaviour.Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops()
                                    .strength(3.0F, 3.0F),
                            new ResourceLocation(AD_ASTRA, "block/" + planet.id + "_stone"));
        }
    }

    /** {@code IGTAddon#registerWorldgenLayers}. */
    public static void registerWorldgenLayers() {
        if (!enabled()) return;
        LAYER = new SimpleWorldGenLayer("gtna_planet_stone", () -> new TagMatchTest(PLANET_STONES),
                Arrays.stream(Planet.values()).map(p -> p.dimension.location()).collect(Collectors.toSet()));
        RULES = new RuleTest[] { new TagMatchTest(PLANET_STONES) };
    }

    private static net.minecraft.world.level.block.state.BlockState stoneState(Planet planet) {
        Block block = BuiltInRegistries.BLOCK.get(planet.stone());
        return block == null ? Blocks.AIR.defaultBlockState() : block.defaultBlockState();
    }

    private static Set<ResourceKey<Level>> dims(Planet... planets) {
        return Arrays.stream(planets).map(p -> p.dimension).collect(Collectors.toSet());
    }

    private static void vein(String name, Consumer<GTOreDefinition> config) {
        GTOres.create(GTNACORE.id("planet/" + name), config);
    }

    private static final Planet MOON = Planet.MOON, MARS = Planet.MARS, VENUS = Planet.VENUS,
            MERCURY = Planet.MERCURY, GLACIO = Planet.GLACIO;

    /** {@code IGTAddon#registerOreVeins}. */
    public static void registerOreVeins() {
        if (!enabled()) return;
        vein("bauxite_vein", vein -> vein
                .clusterSize(UniformInt.of(32, 40)).density(0.3f).weight(40)
                .layer(LAYER)
                .dimensions(dims(MOON))
                .heightRangeUniform(10, 80)
                .layeredVeinGenerator(generator -> generator
                        .withLayerPattern(() -> GTLayerPattern.builder(RULES)
                                .layer(l -> l.weight(2).mat(Bauxite).size(1, 4))
                                .layer(l -> l.weight(1).mat(Ilmenite).size(1, 2))
                                .layer(l -> l.weight(1).mat(Aluminium).size(1, 1))
                                .build()))
                .surfaceIndicatorGenerator(indicator -> indicator
                        .surfaceRock(Bauxite)
                        .placement(SurfaceIndicatorGenerator.IndicatorPlacement.ABOVE)));

        vein("chromite_vein", vein -> vein
                .clusterSize(UniformInt.of(38, 44)).density(0.15f).weight(30)
                .layer(LAYER)
                .dimensions(dims(VENUS))
                .heightRangeUniform(20, 80)
                .layeredVeinGenerator(generator -> generator
                        .withLayerPattern(() -> GTLayerPattern.builder(RULES)
                                .layer(l -> l.weight(3).mat(Magnetite).size(1, 3))
                                .layer(l -> l.weight(1).mat(VanadiumMagnetite).size(1, 1))
                                .layer(l -> l.weight(4).mat(Chromite).size(1, 4))
                                .build()))
                .surfaceIndicatorGenerator(indicator -> indicator
                        .surfaceRock(Magnetite)
                        .placement(SurfaceIndicatorGenerator.IndicatorPlacement.ABOVE)));

        vein("pitchblende_vein", vein -> vein
                .clusterSize(UniformInt.of(32, 64)).density(0.75f).weight(30)
                .layer(LAYER)
                .dimensions(dims(MOON))
                .heightRangeUniform(-20, 35)
                .dikeVeinGenerator(generator -> generator
                        .withBlock(new DikeVeinGenerator.DikeBlockDefinition(Uraninite, 2, -15, 30))
                        .withBlock(new DikeVeinGenerator.DikeBlockDefinition(Pitchblende, 3, -15, 35))
                        .withBlock(new DikeVeinGenerator.DikeBlockDefinition(Pitchblende, 2, -20, 30)))
                .surfaceIndicatorGenerator(indicator -> indicator
                        .surfaceRock(Pitchblende)
                        .placement(SurfaceIndicatorGenerator.IndicatorPlacement.ABOVE)));

        vein("borax_vein", vein -> vein
                .clusterSize(UniformInt.of(32, 64)).density(0.25f).weight(30)
                .layer(LAYER)
                .dimensions(dims(MARS, VENUS))
                .heightRangeUniform(5, 40)
                .dikeVeinGenerator(generator -> generator
                        .withBlock(new DikeVeinGenerator.DikeBlockDefinition(RockSalt, 1, 5, 30))
                        .withBlock(new DikeVeinGenerator.DikeBlockDefinition(Borax, 3, 5, 40))
                        .withBlock(new DikeVeinGenerator.DikeBlockDefinition(Salt, 1, 10, 30))
                        .withBlock(new DikeVeinGenerator.DikeBlockDefinition(Lepidolite, 1, 10, 30)))
                .surfaceIndicatorGenerator(indicator -> indicator
                        .surfaceRock(Borax)
                        .placement(SurfaceIndicatorGenerator.IndicatorPlacement.ABOVE)));

        vein("scheelite_vein", vein -> vein
                .clusterSize(UniformInt.of(50, 64)).density(0.7f).weight(20)
                .layer(LAYER)
                .dimensions(dims(MARS, GLACIO))
                .heightRangeUniform(20, 60)
                .dikeVeinGenerator(generator -> generator
                        .withBlock(new DikeVeinGenerator.DikeBlockDefinition(Scheelite, 3, 20, 60))
                        .withBlock(new DikeVeinGenerator.DikeBlockDefinition(Tungstate, 2, 35, 55))
                        .withBlock(new DikeVeinGenerator.DikeBlockDefinition(Lithium, 1, 20, 40)))
                .surfaceIndicatorGenerator(indicator -> indicator
                        .surfaceRock(Scheelite)
                        .placement(SurfaceIndicatorGenerator.IndicatorPlacement.ABOVE)));

        vein("sheldonite_vein", vein -> vein
                .clusterSize(UniformInt.of(25, 29)).density(0.2f).weight(10)
                .layer(LAYER)
                .dimensions(dims(MARS, MERCURY, GLACIO))
                .heightRangeUniform(5, 50)
                .layeredVeinGenerator(generator -> generator
                        .withLayerPattern(() -> GTLayerPattern.builder(RULES)
                                .layer(l -> l.weight(3).mat(Bornite).size(2, 4))
                                .layer(l -> l.weight(2).mat(Cooperite).size(1, 1))
                                .layer(l -> l.weight(2).mat(Platinum).size(1, 1))
                                .layer(l -> l.weight(1).mat(Palladium).size(1, 1))
                                .build()))
                .surfaceIndicatorGenerator(indicator -> indicator
                        .surfaceRock(Platinum)
                        .placement(SurfaceIndicatorGenerator.IndicatorPlacement.ABOVE)));

        vein("banded_iron_vein", vein -> vein
                .clusterSize(UniformInt.of(40, 52)).density(1.0f).weight(30)
                .layer(LAYER)
                .dimensions(dims(VENUS))
                .heightRangeUniform(20, 40)
                .veinedVeinGenerator(generator -> generator
                        .oreBlock(new VeinedVeinGenerator.VeinBlockDefinition(Goethite, 3))
                        .oreBlock(new VeinedVeinGenerator.VeinBlockDefinition(YellowLimonite, 2))
                        .oreBlock(new VeinedVeinGenerator.VeinBlockDefinition(Hematite, 2))
                        .rareBlock(new VeinedVeinGenerator.VeinBlockDefinition(Gold, 1))
                        .rareBlockChance(0.075f)
                        .veininessThreshold(0.01f)
                        .maxRichnessThreshold(0.175f)
                        .minRichness(0.7f)
                        .maxRichness(1.0f)
                        .edgeRoundoffBegin(3)
                        .maxEdgeRoundoff(0.1f))
                .surfaceIndicatorGenerator(indicator -> indicator
                        .surfaceRock(Goethite)
                        .placement(SurfaceIndicatorGenerator.IndicatorPlacement.ABOVE)));

        vein("manganese_vein", vein -> vein
                .clusterSize(UniformInt.of(50, 64)).density(0.75f).weight(20)
                .layer(LAYER)
                .dimensions(dims(MERCURY))
                .heightRangeUniform(-50, -5)
                .dikeVeinGenerator(generator -> generator
                        .withBlock(new DikeVeinGenerator.DikeBlockDefinition(Grossular, 3, -50, -5))
                        .withBlock(new DikeVeinGenerator.DikeBlockDefinition(Spessartine, 2, -40, -15))
                        .withBlock(new DikeVeinGenerator.DikeBlockDefinition(Pyrolusite, 2, -40, -15))
                        .withBlock(new DikeVeinGenerator.DikeBlockDefinition(Tantalite, 1, -30, -5)))
                .surfaceIndicatorGenerator(indicator -> indicator
                        .surfaceRock(Grossular)
                        .density(0.15f)
                        .radius(3)));

        vein("molybdenum_vein", vein -> vein
                .clusterSize(UniformInt.of(25, 29)).density(0.25f).weight(5)
                .layer(LAYER)
                .dimensions(dims(VENUS))
                .heightRangeUniform(20, 50)
                .layeredVeinGenerator(generator -> generator
                        .withLayerPattern(() -> GTLayerPattern.builder(RULES)
                                .layer(l -> l.weight(3).mat(Wulfenite).size(2, 4))
                                .layer(l -> l.weight(2).mat(Molybdenite).size(1, 1))
                                .layer(l -> l.weight(1).mat(Molybdenum).size(1, 1))
                                .layer(l -> l.weight(1).mat(Powellite).size(1, 1))
                                .build()))
                .surfaceIndicatorGenerator(indicator -> indicator
                        .surfaceRock(Molybdenum)
                        .placement(SurfaceIndicatorGenerator.IndicatorPlacement.ABOVE)));

        vein("monazite_vein", vein -> vein
                .clusterSize(UniformInt.of(25, 29)).density(0.25f).weight(30)
                .layer(LAYER)
                .dimensions(dims(MOON, GLACIO))
                .heightRangeUniform(20, 40)
                .layeredVeinGenerator(generator -> generator
                        .withLayerPattern(() -> GTLayerPattern.builder(RULES)
                                .layer(l -> l.weight(3).mat(Bastnasite).size(2, 4))
                                .layer(l -> l.weight(1).mat(Monazite).size(1, 1))
                                .layer(l -> l.weight(1).mat(Neodymium).size(1, 1))
                                .build()))
                .surfaceIndicatorGenerator(indicator -> indicator
                        .surfaceRock(Bastnasite)
                        .placement(SurfaceIndicatorGenerator.IndicatorPlacement.ABOVE)));

        vein("saltpeter_vein", vein -> vein
                .clusterSize(UniformInt.of(32, 40)).density(0.25f).weight(40)
                .layer(LAYER)
                .dimensions(dims(MARS, MERCURY))
                .heightRangeUniform(5, 45)
                .layeredVeinGenerator(generator -> generator
                        .withLayerPattern(() -> GTLayerPattern.builder(RULES)
                                .layer(l -> l.weight(3).mat(Saltpeter).size(2, 4))
                                .layer(l -> l.weight(2).mat(Diatomite).size(1, 1))
                                .layer(l -> l.weight(2).mat(Electrotine).size(1, 1))
                                .layer(l -> l.weight(1).mat(Alunite).size(1, 1))
                                .build()))
                .surfaceIndicatorGenerator(indicator -> indicator
                        .surfaceRock(Saltpeter)
                        .placement(SurfaceIndicatorGenerator.IndicatorPlacement.ABOVE)));

        vein("sulfur_vein", vein -> vein
                .clusterSize(UniformInt.of(32, 40)).density(0.2f).weight(100)
                .layer(LAYER)
                .dimensions(dims(VENUS))
                .heightRangeUniform(10, 30)
                .layeredVeinGenerator(generator -> generator
                        .withLayerPattern(() -> GTLayerPattern.builder(RULES)
                                .layer(l -> l.weight(3).mat(Sulfur).size(2, 4))
                                .layer(l -> l.weight(2).mat(Pyrite).size(1, 1))
                                .layer(l -> l.weight(1).mat(Sphalerite).size(1, 1))
                                .build()))
                .surfaceIndicatorGenerator(indicator -> indicator
                        .surfaceRock(Sulfur)
                        .placement(SurfaceIndicatorGenerator.IndicatorPlacement.ABOVE)));

        vein("tetrahedrite_vein", vein -> vein
                .clusterSize(UniformInt.of(40, 52)).density(1.0f).weight(70)
                .layer(LAYER)
                .dimensions(dims(MARS))
                .heightRangeUniform(20, 100)
                .veinedVeinGenerator(generator -> generator
                        .oreBlock(new VeinedVeinGenerator.VeinBlockDefinition(Tetrahedrite, 4))
                        .oreBlock(new VeinedVeinGenerator.VeinBlockDefinition(Copper, 2))
                        .rareBlock(new VeinedVeinGenerator.VeinBlockDefinition(Stibnite, 1))
                        .rareBlockChance(0.15f)
                        .veininessThreshold(0.01f)
                        .maxRichnessThreshold(0.175f)
                        .minRichness(0.7f)
                        .maxRichness(1.0f)
                        .edgeRoundoffBegin(3)
                        .maxEdgeRoundoff(0.1f))
                .surfaceIndicatorGenerator(indicator -> indicator
                        .surfaceRock(Tetrahedrite)
                        .placement(SurfaceIndicatorGenerator.IndicatorPlacement.BELOW)));

        vein("topaz_vein", vein -> vein
                .clusterSize(UniformInt.of(25, 29)).density(0.25f).weight(70)
                .layer(LAYER)
                .dimensions(dims(MERCURY))
                .heightRangeUniform(20, 70)
                .layeredVeinGenerator(generator -> generator
                        .withLayerPattern(() -> GTLayerPattern.builder(RULES)
                                .layer(l -> l.weight(3).mat(BlueTopaz).size(2, 4))
                                .layer(l -> l.weight(2).mat(Topaz).size(1, 1))
                                .layer(l -> l.weight(2).mat(Chalcocite).size(1, 1))
                                .layer(l -> l.weight(1).mat(Bornite).size(1, 1))
                                .build()))
                .surfaceIndicatorGenerator(indicator -> indicator
                        .surfaceRock(Topaz)
                        .placement(SurfaceIndicatorGenerator.IndicatorPlacement.BELOW)));

        vein("apatite_vein", vein -> vein
                .clusterSize(UniformInt.of(32, 40)).density(0.25f).weight(40)
                .layer(LAYER)
                .dimensions(dims(MARS))
                .heightRangeUniform(10, 80)
                .layeredVeinGenerator(generator -> generator
                        .withLayerPattern(() -> GTLayerPattern.builder(RULES)
                                .layer(l -> l.weight(3).mat(Apatite).size(2, 4))
                                .layer(l -> l.weight(2).mat(TricalciumPhosphate).size(1, 1))
                                .layer(l -> l.weight(1).mat(Pyrochlore).size(1, 1))
                                .build()))
                .surfaceIndicatorGenerator(indicator -> indicator
                        .surfaceRock(Apatite)
                        .placement(SurfaceIndicatorGenerator.IndicatorPlacement.ABOVE)));

        vein("cassiterite_vein", vein -> vein
                .clusterSize(UniformInt.of(40, 52)).density(1.0f).weight(80)
                .layer(LAYER)
                .dimensions(dims(MOON, GLACIO))
                .heightRangeUniform(10, 80)
                .veinedVeinGenerator(generator -> generator
                        .oreBlock(new VeinedVeinGenerator.VeinBlockDefinition(Tin, 4))
                        .rareBlock(new VeinedVeinGenerator.VeinBlockDefinition(Cassiterite, 2))
                        .rareBlockChance(0.33f)
                        .veininessThreshold(0.01f)
                        .maxRichnessThreshold(0.175f)
                        .minRichness(0.7f)
                        .maxRichness(1.0f)
                        .edgeRoundoffBegin(3)
                        .maxEdgeRoundoff(0.1f))
                .surfaceIndicatorGenerator(indicator -> indicator
                        .surfaceRock(Cassiterite)));

        vein("coal_vein", vein -> vein
                .clusterSize(UniformInt.of(38, 44)).density(0.25f).weight(80)
                .layer(LAYER)
                .dimensions(dims(GLACIO))
                .heightRangeUniform(10, 140)
                .layeredVeinGenerator(generator -> generator
                        .withLayerPattern(() -> GTLayerPattern.builder(RULES)
                                .layer(l -> l.weight(3).mat(Coal).size(2, 4))
                                .layer(l -> l.weight(3).mat(Coal).size(2, 4))
                                .build()))
                .surfaceIndicatorGenerator(indicator -> indicator
                        .surfaceRock(Coal)));

        vein("copper_tin_vein", vein -> vein
                .clusterSize(UniformInt.of(40, 52)).density(1.0f).weight(50)
                .layer(LAYER)
                .dimensions(dims(MERCURY))
                .heightRangeUniform(-10, 160)
                .veinedVeinGenerator(generator -> generator
                        .oreBlock(new VeinedVeinGenerator.VeinBlockDefinition(Chalcopyrite, 5))
                        .oreBlock(new VeinedVeinGenerator.VeinBlockDefinition(Zeolite, 2))
                        .oreBlock(new VeinedVeinGenerator.VeinBlockDefinition(Cassiterite, 2))
                        .rareBlock(new VeinedVeinGenerator.VeinBlockDefinition(Realgar, 1))
                        .rareBlockChance(0.1f)
                        .veininessThreshold(0.01f)
                        .maxRichnessThreshold(0.175f)
                        .minRichness(0.7f)
                        .maxRichness(1.0f)
                        .edgeRoundoffBegin(3)
                        .maxEdgeRoundoff(0.1f))
                .surfaceIndicatorGenerator(indicator -> indicator
                        .surfaceRock(Chalcopyrite)));

        vein("galena_vein", vein -> vein
                .clusterSize(UniformInt.of(32, 40)).density(0.25f).weight(40)
                .layer(LAYER)
                .dimensions(dims(VENUS))
                .heightRangeUniform(-15, 45)
                .layeredVeinGenerator(generator -> generator
                        .withLayerPattern(() -> GTLayerPattern.builder(RULES)
                                .layer(l -> l.weight(3).mat(Galena).size(2, 4))
                                .layer(l -> l.weight(2).mat(Silver).size(1, 1))
                                .layer(l -> l.weight(1).mat(Lead).size(1, 1))
                                .build()))
                .surfaceIndicatorGenerator(indicator -> indicator
                        .surfaceRock(Galena)));

        vein("garnet_tin_vein", vein -> vein
                .clusterSize(UniformInt.of(32, 40)).density(0.4f).weight(80)
                .layer(LAYER)
                .dimensions(dims(MOON, GLACIO))
                .heightRangeUniform(30, 60)
                .layeredVeinGenerator(generator -> generator
                        .withLayerPattern(() -> GTLayerPattern.builder(RULES)
                                .layer(l -> l.weight(3).mat(CassiteriteSand).size(2, 4))
                                .layer(l -> l.weight(2).mat(GarnetSand).size(1, 1))
                                .layer(l -> l.weight(2).mat(Asbestos).size(1, 1))
                                .layer(l -> l.weight(1).mat(Diatomite).size(1, 1))
                                .build()))
                .surfaceIndicatorGenerator(indicator -> indicator
                        .surfaceRock(GarnetSand)));

        vein("garnet_vein", vein -> vein
                .clusterSize(UniformInt.of(50, 64)).density(0.75f).weight(40)
                .layer(LAYER)
                .dimensions(dims(MARS))
                .heightRangeUniform(-10, 50)
                .dikeVeinGenerator(generator -> generator
                        .withBlock(new DikeVeinGenerator.DikeBlockDefinition(GarnetRed, 3, -10, 50))
                        .withBlock(new DikeVeinGenerator.DikeBlockDefinition(GarnetYellow, 2, -10, 50))
                        .withBlock(new DikeVeinGenerator.DikeBlockDefinition(Amethyst, 2, -10, 22))
                        .withBlock(new DikeVeinGenerator.DikeBlockDefinition(Opal, 1, 18, 50)))
                .surfaceIndicatorGenerator(indicator -> indicator
                        .surfaceRock(GarnetRed)
                        .placement(SurfaceIndicatorGenerator.IndicatorPlacement.ABOVE)));

        vein("iron_vein", vein -> vein
                .clusterSize(UniformInt.of(40, 52)).density(1.0f).weight(120)
                .layer(LAYER)
                .dimensions(dims(MARS))
                .heightRangeUniform(-10, 60)
                .veinedVeinGenerator(generator -> generator
                        .oreBlock(new VeinedVeinGenerator.VeinBlockDefinition(Goethite, 5))
                        .oreBlock(new VeinedVeinGenerator.VeinBlockDefinition(YellowLimonite, 2))
                        .oreBlock(new VeinedVeinGenerator.VeinBlockDefinition(Hematite, 2))
                        .oreBlock(new VeinedVeinGenerator.VeinBlockDefinition(Malachite, 1))
                        .veininessThreshold(0.01f)
                        .maxRichnessThreshold(0.175f)
                        .minRichness(0.7f)
                        .maxRichness(1.0f)
                        .edgeRoundoffBegin(3)
                        .maxEdgeRoundoff(0.1f))
                .surfaceIndicatorGenerator(indicator -> indicator
                        .surfaceRock(Goethite)));

        vein("lubricant_vein", vein -> vein
                .clusterSize(UniformInt.of(25, 29)).density(0.25f).weight(40)
                .layer(LAYER)
                .dimensions(dims(MOON))
                .heightRangeUniform(0, 50)
                .layeredVeinGenerator(generator -> generator
                        .withLayerPattern(() -> GTLayerPattern.builder(RULES)
                                .layer(l -> l.weight(3).mat(Soapstone).size(2, 4))
                                .layer(l -> l.weight(2).mat(Talc).size(1, 1))
                                .layer(l -> l.weight(2).mat(GlauconiteSand).size(1, 1))
                                .layer(l -> l.weight(1).mat(Pentlandite).size(1, 1))
                                .build()))
                .surfaceIndicatorGenerator(indicator -> indicator
                        .surfaceRock(Talc)));

        vein("magnetite_vein", vein -> vein
                .clusterSize(UniformInt.of(38, 44)).density(0.15f).weight(80)
                .layer(LAYER)
                .dimensions(dims(MOON, MARS))
                .heightRangeUniform(10, 60)
                .layeredVeinGenerator(generator -> generator
                        .withLayerPattern(() -> GTLayerPattern.builder(RULES)
                                .layer(l -> l.weight(3).mat(Magnetite).size(2, 4))
                                .layer(l -> l.weight(2).mat(VanadiumMagnetite).size(1, 1))
                                .layer(l -> l.weight(1).mat(Gold).size(1, 1))
                                .build()))
                .surfaceIndicatorGenerator(indicator -> indicator
                        .surfaceRock(Magnetite)));

        vein("mineral_sand_vein", vein -> vein
                .clusterSize(UniformInt.of(32, 40)).density(0.2f).weight(80)
                .layer(LAYER)
                .dimensions(dims(MARS))
                .heightRangeUniform(15, 60)
                .layeredVeinGenerator(generator -> generator
                        .withLayerPattern(() -> GTLayerPattern.builder(RULES)
                                .layer(l -> l.weight(3).mat(BasalticMineralSand).size(2, 4))
                                .layer(l -> l.weight(2).mat(GraniticMineralSand).size(1, 1))
                                .layer(l -> l.weight(2).mat(FullersEarth).size(1, 1))
                                .layer(l -> l.weight(1).mat(Gypsum).size(1, 1))
                                .build()))
                .surfaceIndicatorGenerator(indicator -> indicator
                        .surfaceRock(BasalticMineralSand)));

        vein("nickel_vein", vein -> vein
                .clusterSize(UniformInt.of(32, 40)).density(0.25f).weight(40)
                .layer(LAYER)
                .dimensions(dims(MERCURY))
                .heightRangeUniform(-10, 60)
                .layeredVeinGenerator(generator -> generator
                        .withLayerPattern(() -> GTLayerPattern.builder(RULES)
                                .layer(l -> l.weight(3).mat(Garnierite).size(2, 4))
                                .layer(l -> l.weight(2).mat(Nickel).size(1, 1))
                                .layer(l -> l.weight(2).mat(Cobaltite).size(1, 1))
                                .layer(l -> l.weight(1).mat(Pentlandite).size(1, 1))
                                .build()))
                .surfaceIndicatorGenerator(indicator -> indicator
                        .surfaceRock(Nickel)));

        vein("salts_vein", vein -> vein
                .clusterSize(UniformInt.of(32, 40)).density(0.2f).weight(50)
                .layer(LAYER)
                .dimensions(dims(GLACIO))
                .heightRangeUniform(30, 70)
                .layeredVeinGenerator(generator -> generator
                        .withLayerPattern(() -> GTLayerPattern.builder(RULES)
                                .layer(l -> l.weight(3).mat(RockSalt).size(2, 4))
                                .layer(l -> l.weight(2).mat(Salt).size(1, 1))
                                .layer(l -> l.weight(1).mat(Lepidolite).size(1, 1))
                                .layer(l -> l.weight(1).mat(Spodumene).size(1, 1))
                                .build()))
                .surfaceIndicatorGenerator(indicator -> indicator
                        .surfaceRock(Salt)));

        vein("oilsands_vein", vein -> vein
                .clusterSize(UniformInt.of(25, 29)).density(0.3f).weight(40)
                .layer(LAYER)
                .dimensions(dims(GLACIO))
                .heightRangeUniform(30, 80)
                .layeredVeinGenerator(generator -> generator
                        .withLayerPattern(() -> GTLayerPattern.builder(RULES)
                                .layer(l -> l.weight(3).mat(Oilsands).size(2, 4))
                                .layer(l -> l.weight(2).mat(Oilsands).size(1, 1))
                                .layer(l -> l.weight(1).mat(Oilsands).size(1, 1))
                                .layer(l -> l.weight(1).mat(Oilsands).size(1, 1))
                                .build()))
                .surfaceIndicatorGenerator(indicator -> indicator
                        .surfaceRock(Oilsands)));

        vein("copper_vein", vein -> vein
                .clusterSize(UniformInt.of(40, 52)).density(1.0f).weight(80)
                .layer(LAYER)
                .dimensions(dims(MARS))
                .heightRangeUniform(-40, 10)
                .veinedVeinGenerator(generator -> generator
                        .oreBlock(new VeinedVeinGenerator.VeinBlockDefinition(Chalcopyrite, 5))
                        .oreBlock(new VeinedVeinGenerator.VeinBlockDefinition(Iron, 2))
                        .oreBlock(new VeinedVeinGenerator.VeinBlockDefinition(Pyrite, 2))
                        .oreBlock(new VeinedVeinGenerator.VeinBlockDefinition(Copper, 2))
                        .veininessThreshold(0.01f)
                        .maxRichnessThreshold(0.175f)
                        .minRichness(0.7f)
                        .maxRichness(1.0f)
                        .edgeRoundoffBegin(3)
                        .maxEdgeRoundoff(0.1f))
                .surfaceIndicatorGenerator(indicator -> indicator
                        .surfaceRock(Copper)));

        vein("diamond_vein", vein -> vein
                .clusterSize(UniformInt.of(32, 40)).density(0.25f).weight(40)
                .layer(LAYER)
                .dimensions(dims(VENUS))
                .heightRangeUniform(-55, -30)
                .classicVeinGenerator(generator -> generator
                        .primary(b -> b.mat(Graphite).size(4))
                        .secondary(b -> b.mat(Graphite).size(3))
                        .between(b -> b.mat(Diamond).size(3))
                        .sporadic(b -> b.mat(Coal)))
                .surfaceIndicatorGenerator(indicator -> indicator
                        .surfaceRock(Diamond)
                        .density(0.1f)
                        .placement(SurfaceIndicatorGenerator.IndicatorPlacement.ABOVE)
                        .radius(2)));

        vein("lapis_vein", vein -> vein
                .clusterSize(UniformInt.of(40, 52)).density(0.75f).weight(40)
                .layer(LAYER)
                .dimensions(dims(GLACIO))
                .heightRangeUniform(-60, 10)
                .dikeVeinGenerator(generator -> generator
                        .withBlock(new DikeVeinGenerator.DikeBlockDefinition(Lazurite, 3, -60, 10))
                        .withBlock(new DikeVeinGenerator.DikeBlockDefinition(Sodalite, 2, -50, 0))
                        .withBlock(new DikeVeinGenerator.DikeBlockDefinition(Lapis, 2, -50, 0))
                        .withBlock(new DikeVeinGenerator.DikeBlockDefinition(Calcite, 1, -40, 10)))
                .surfaceIndicatorGenerator(indicator -> indicator
                        .surfaceRock(Lapis)
                        .density(0.15f)
                        .placement(SurfaceIndicatorGenerator.IndicatorPlacement.ABOVE)
                        .radius(3)));

        vein("mica_vein", vein -> vein
                .clusterSize(UniformInt.of(32, 40)).density(0.25f).weight(20)
                .layer(LAYER)
                .dimensions(dims(MARS, GLACIO))
                .heightRangeUniform(-40, -10)
                .layeredVeinGenerator(generator -> generator
                        .withLayerPattern(() -> GTLayerPattern.builder(RULES)
                                .layer(l -> l.weight(3).mat(Kyanite).size(2, 4))
                                .layer(l -> l.weight(2).mat(Mica).size(1, 1))
                                .layer(l -> l.weight(1).mat(Pollucite).size(1, 1))
                                .build()))
                .surfaceIndicatorGenerator(indicator -> indicator
                        .surfaceRock(Mica)
                        .radius(3)));

        vein("olivine_vein", vein -> vein
                .clusterSize(UniformInt.of(32, 40)).density(0.25f).weight(20)
                .layer(LAYER)
                .dimensions(dims(VENUS))
                .heightRangeUniform(-20, 10)
                .layeredVeinGenerator(generator -> generator
                        .withLayerPattern(() -> GTLayerPattern.builder(RULES)
                                .layer(l -> l.weight(3).mat(Bentonite).size(2, 4))
                                .layer(l -> l.weight(2).mat(Magnetite).size(1, 1))
                                .layer(l -> l.weight(2).mat(Olivine).size(1, 1))
                                .layer(l -> l.weight(1).mat(GlauconiteSand).size(1, 1))
                                .build()))
                .surfaceIndicatorGenerator(indicator -> indicator
                        .surfaceRock(Olivine)
                        .density(0.15f)
                        .radius(3)));
    }

    private static final class PlanetOrePrefix extends TagPrefix {

        private final Planet planet;

        PlanetOrePrefix(Planet planet) {
            super(planet.id + "_stone");
            this.planet = planet;
        }

        @Override
        public boolean isIgnored(Material material) {
            return super.isIgnored(material) || !planet.oreMaterials().contains(material);
        }
    }
}
