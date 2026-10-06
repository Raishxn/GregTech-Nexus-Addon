package com.raishxn.gtna.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.google.gson.JsonParser;
import com.raishxn.gtna.common.block.EyeOfHarmonyBlockItem;
import com.raishxn.gtna.common.block.EyeOfHarmonyFieldBlock;
import com.raishxn.gtna.common.data.GTNAEyeOfHarmonyContent;
import com.tterrag.registrate.util.entry.BlockEntry;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;

@GameTestHolder("gtna")
@PrefixGameTestTemplate(false)
public final class EyeOfHarmonyContentGameTests {

    @GameTest(template = "empty_16", timeoutTicks = 40)
    public static void eyeOfHarmonyFieldsPreserveAll27TiersAndDrops(GameTestHelper helper) {
        var families = List.of(GTNAEyeOfHarmonyContent.COMPRESSION_FIELDS,
                GTNAEyeOfHarmonyContent.ACCELERATION_FIELDS, GTNAEyeOfHarmonyContent.STABILISATION_FIELDS);
        var ids = new HashSet<ResourceLocation>();
        for (int family = 0; family < families.size(); family++) {
            var entries = families.get(family);
            helper.assertTrue(entries.length == 9, "each field family must have nine physical tiers");
            for (int tier = 0; tier < entries.length; tier++) {
                var block = entries[tier].get();
                var pos = new BlockPos(tier + 1, 1, family * 3 + 1);
                helper.setBlock(pos, block);
                helper.assertTrue(block.getFamily() == EyeOfHarmonyFieldBlock.Family.values()[family] &&
                        block.getFieldTier() == tier, "field family and internal tier must remain distinct");
                helper.assertTrue(ids.add(BuiltInRegistries.BLOCK.getKey(block)),
                        "fields must have unique registry IDs");
                helper.assertTrue(block.defaultBlockState().is(BlockTags.MINEABLE_WITH_PICKAXE),
                        "fields must retain their mining tag");
                helper.assertTrue(block.asItem() instanceof EyeOfHarmonyBlockItem,
                        "fields must use the tier/source tooltip BlockItem");
                var drops = Block.getDrops(block.defaultBlockState(), helper.getLevel(), helper.absolutePos(pos),
                        null, helper.makeMockPlayer(), new ItemStack(Items.DIAMOND_PICKAXE));
                helper.assertTrue(drops.size() == 1 && drops.get(0).is(block.asItem()) && drops.get(0).getCount() == 1,
                        "each field must drop its own tier, not a shared casing");
                verifyModel(helper, entries[tier]);
            }
        }
        helper.assertTrue(ids.size() == 27, "all 27 field variants must exist");
        helper.succeed();
    }

    @GameTest(template = "empty_16", timeoutTicks = 40)
    public static void eyeOfHarmonyPlanetsAndComponentsKeepTheirIdentities(GameTestHelper helper) {
        var planets = List.of(GTNAEyeOfHarmonyContent.OVERWORLD_PLANET, GTNAEyeOfHarmonyContent.NETHER_PLANET,
                GTNAEyeOfHarmonyContent.END_PLANET);
        var dimensions = List.of("minecraft:overworld", "minecraft:the_nether", "minecraft:the_end");
        for (int i = 0; i < planets.size(); i++) {
            var block = planets.get(i).get();
            helper.setBlock(new BlockPos(i * 3 + 1, 1, 1), block);
            helper.assertTrue(block.getDimension().equals(ResourceLocation.parse(dimensions.get(i))),
                    "planet selector must preserve its real dimension ID");
            verifyModel(helper, planets.get(i));
        }
        verifyModel(helper, GTNAEyeOfHarmonyContent.BOUNDARY_CASING);
        verifyModel(helper, GTNAEyeOfHarmonyContent.SPATIAL_CASING);
        verifyModel(helper, GTNAEyeOfHarmonyContent.TEMPORAL_CASING);
        var astral = GTNAEyeOfHarmonyContent.ASTRAL_ARRAY_FABRICATOR.asStack();
        helper.assertTrue(!astral.isEmpty() && astral.getMaxStackSize() == 64,
                "Astral Array Fabricators must be real stackable upgrade items");
        helper.assertTrue(exists("assets/gtna/models/item/astral_array_fabricator.json"),
                "Astral Array item model must exist");
        verifyTranslations(helper, "item.gtna.astral_array_fabricator");
        helper.succeed();
    }

    private static void verifyModel(GameTestHelper helper, BlockEntry<? extends Block> entry) {
        String path = BuiltInRegistries.BLOCK.getKey(entry.get()).getPath();
        helper.assertTrue(exists("assets/gtna/blockstates/" + path + ".json"),
                "missing blockstate for " + path);
        helper.assertTrue(exists("assets/gtna/models/item/" + path + ".json"),
                "missing inventory model for " + path);
        verifyTranslations(helper, "block.gtna." + path);
        var stream = resource("assets/gtna/models/block/" + path + ".json");
        helper.assertTrue(stream != null, "missing block model for " + path);
        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            var textures = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonObject("textures");
            for (var texture : textures.entrySet()) {
                String value = texture.getValue().getAsString();
                if (value.startsWith("#")) continue;
                var id = ResourceLocation.parse(value);
                helper.assertTrue(
                        exists("assets/" + id.getNamespace() + "/textures/" + id.getPath() + ".png"),
                        "model texture must exist: " + value);
            }
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("cannot read model " + path, exception);
        }
    }

    private static void verifyTranslations(GameTestHelper helper, String key) {
        for (String language : List.of("en_us", "pt_br")) {
            try (var reader = new InputStreamReader(resource("assets/gtna/lang/" + language + ".json"),
                    StandardCharsets.UTF_8)) {
                var translations = JsonParser.parseReader(reader).getAsJsonObject();
                helper.assertTrue(translations.has(key) && !translations.get(key).getAsString().isBlank(),
                        "missing " + language + " content name: " + key);
            } catch (java.io.IOException exception) {
                throw new IllegalStateException("cannot read content translations", exception);
            }
        }
    }

    private static boolean exists(String path) {
        return EyeOfHarmonyContentGameTests.class.getClassLoader().getResource(path) != null;
    }

    private static java.io.InputStream resource(String path) {
        return EyeOfHarmonyContentGameTests.class.getClassLoader().getResourceAsStream(path);
    }
}
