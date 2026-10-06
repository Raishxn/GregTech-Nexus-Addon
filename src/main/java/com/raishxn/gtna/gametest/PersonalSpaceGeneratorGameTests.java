package com.raishxn.gtna.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtna.common.world.personalspace.PersonalSpaceChunkGenerator;
import com.raishxn.gtna.common.world.personalspace.PersonalSpaceConfig;
import com.raishxn.gtna.common.world.personalspace.PersonalSpaceDirectory;
import com.raishxn.gtna.common.world.personalspace.PersonalSpaceLevelData;
import com.raishxn.gtna.common.world.personalspace.PersonalSpacePortalEntity;
import com.raishxn.gtna.common.world.personalspace.PersonalSpacePortalRegistry;
import com.raishxn.gtna.common.world.personalspace.PersonalSpaceSettings;
import com.raishxn.gtna.common.world.personalspace.PersonalSpaceWorlds;

@GameTestHolder("gtna")
@PrefixGameTestTemplate(false)
public final class PersonalSpaceGeneratorGameTests {

    private static final String ROADS_2X2 = "minecraft:bedrock;minecraft:dirt*3;minecraft:grass_block" +
            "|B,minecraft:yellow_wool,minecraft:black_wool,2,2" +
            "|G,1,0,minecraft:gray_concrete,minecraft:white_concrete,minecraft:yellow_concrete|S,0" +
            "|C,1,0,minecraft:red_wool";

    private static PersonalSpaceChunkGenerator generator(GameTestHelper helper, String preset) {
        var biome = helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME)
                .getHolderOrThrow(Biomes.PLAINS);
        return new PersonalSpaceChunkGenerator(new FixedBiomeSource(biome), 999,
                PersonalSpaceSettings.fromPreset(preset));
    }

    private static boolean is(PersonalSpaceChunkGenerator generator, int x, int y, int z, Block block) {
        return generator.stateAt(x, y, z).is(block);
    }

    @GameTest(template = "empty_16", timeoutTicks = 40)
    public static void personalSpaceRoadsFollowOriginalChunkCoordinates(GameTestHelper helper) {
        var g = generator(helper, ROADS_2X2);
        helper.assertTrue(is(g, 8, 0, 8, Blocks.BEDROCK), "bottom layer is bedrock");
        helper.assertTrue(is(g, 8, 3, 8, Blocks.DIRT), "third dirt layer remains dirt");
        helper.assertTrue(is(g, 40, 4, 40, Blocks.GRASS_BLOCK) || is(g, 20, 4, 20, Blocks.GRASS_BLOCK),
                "lot surface is grass");
        helper.assertTrue(is(g, 16, 4, 16, Blocks.RED_WOOL), "SE center marker replaces surface");
        helper.assertTrue(is(g, 0, 4, 4, Blocks.YELLOW_WOOL) && is(g, 0, 4, 5, Blocks.BLACK_WOOL),
                "lot boundary alternates A/B stripes");
        helper.assertTrue(is(g, 32, 4, 0, Blocks.WHITE_CONCRETE), "road edge uses stripe B");
        helper.assertTrue(is(g, 39, 4, 0, Blocks.YELLOW_CONCRETE), "road dash uses stripe C");
        helper.assertTrue(is(g, 39, 4, 4, Blocks.GRAY_CONCRETE), "road background uses A");
        helper.assertTrue(is(g, -9, 4, 0, Blocks.YELLOW_CONCRETE), "negative chunks repeat road pattern");
        helper.assertTrue(is(g, 32, 4, 32, Blocks.WHITE_CONCRETE), "crossing has corner stripes");
        helper.assertTrue(is(g, 39, 4, 39, Blocks.GRAY_CONCRETE), "crossing interior uses A");
        var platform = g.stateAt(8, 5, 8);
        helper.assertTrue(platform.is(Blocks.SMOOTH_STONE_SLAB) &&
                platform.getValue(SlabBlock.TYPE) == SlabType.DOUBLE, "spawn platform is a double stone slab");
        helper.assertTrue(g.stateAt(5, 5, 8).isAir() && g.stateAt(10, 5, 10).is(Blocks.SMOOTH_STONE_SLAB),
                "GTNA platform is 5x5 from x=6 to 10");

        var nw = generator(helper, ROADS_2X2.replace("|C,1,0,", "|C,1,3,"));
        helper.assertTrue(is(nw, 15, 4, 15, Blocks.RED_WOOL), "NW center marker is shifted by -1/-1");

        var airTop = generator(helper, "minecraft:bedrock;minecraft:air|B,minecraft:yellow_wool," +
                "minecraft:black_wool,2,2|G,1,0,minecraft:gray_concrete,minecraft:white_concrete," +
                "minecraft:yellow_concrete|S,0");
        helper.assertTrue(airTop.stateAt(8, 1, 8).isAir(), "air above plot remains air");
        helper.assertTrue(is(airTop, 32, 1, 0, Blocks.WHITE_CONCRETE),
                "road on an air top layer, as in the original ground-level rule");

        var noGapA = generator(helper, ROADS_2X2.replace("minecraft:gray_concrete", ""));
        helper.assertTrue(is(noGapA, 32, 4, 0, Blocks.GRASS_BLOCK), "without gap block A the gap keeps its layer");
        helper.succeed();
    }

    @GameTest(template = "empty_16", timeoutTicks = 40)
    public static void personalSpaceVoidAndSurfaceLayersMatchOriginal(GameTestHelper helper) {
        var empty = generator(helper, "|B,minecraft:yellow_wool,minecraft:black_wool,2,2|G,1,0," +
                "minecraft:gray_wool,minecraft:white_wool,minecraft:yellow_wool|S,0");
        helper.assertTrue(is(empty, 32, 127, 0, Blocks.WHITE_WOOL), "void world draws roads at ground level 128");
        helper.assertTrue(is(empty, 8, 128, 8, Blocks.SMOOTH_STONE_SLAB), "void platform stays at y=128");

        String layered = "minecraft:stone*2;minecraft:air*2;minecraft:stone" +
                "|B,minecraft:yellow_wool,minecraft:black_wool,2,2|G,0,0,,,|S,";
        var top = generator(helper, layered + "0");
        helper.assertTrue(is(top, 0, 4, 4, Blocks.YELLOW_WOOL), "top surface gets the boundary");
        helper.assertTrue(is(top, 0, 1, 4, Blocks.STONE), "lower surface untouched without S");
        var all = generator(helper, layered + "1");
        helper.assertTrue(is(all, 0, 4, 4, Blocks.YELLOW_WOOL) && is(all, 0, 1, 4, Blocks.YELLOW_WOOL),
                "S,1 marks every exposed surface layer");
        helper.assertTrue(all.stateAt(0, 2, 4).isAir(), "air gap between surfaces stays air");
        helper.succeed();
    }

    @GameTest(template = "empty_16", timeoutTicks = 40)
    public static void personalSpacePresetStringsRoundTripAndAcceptLegacyNames(GameTestHelper helper) {
        var roads = PersonalSpaceSettings.roads();
        var copy = PersonalSpaceSettings.fromPreset(roads.getFullPresetString());
        helper.assertTrue(copy.getFullPresetString().equals(roads.getFullPresetString()),
                "full preset string round-trips");
        var legacy = PersonalSpaceSettings.fromPreset("minecraft:bedrock;minecraft:dirt*3;minecraft:grass" +
                "|B,minecraft:wool:4,minecraft:wool:15,2,3|G,1,1,minecraft:wool:15,minecraft:wool:0," +
                "minecraft:wool:0|C,1,3,minecraft:wool:14");
        helper.assertTrue(legacy.getLayersAsString().equals(PersonalSpaceSettings.PRESET_UW_GARDEN),
                "legacy grass becomes grass_block");
        helper.assertTrue(legacy.getBoundaryBlockA().equals("minecraft:yellow_wool") &&
                legacy.getBoundaryChunkIntervalZ() == 3 &&
                legacy.getGapPreset() == PersonalSpaceSettings.GapPreset.SOLID,
                "legacy boundary and gap sections are applied");
        helper.assertTrue(legacy.isCenterEnabled() && legacy.getCenterBlock().equals("minecraft:red_wool") &&
                legacy.getCenterDirection() == PersonalSpaceSettings.CenterDirection.NW,
                "legacy center section is applied");
        helper.assertTrue(!legacy.isApplyToAllSurfaceLayers(), "strings without S default to the top layer");
        var atomic = PersonalSpaceSettings.fromPreset(PersonalSpaceSettings.PRESET_UW_GARDEN +
                "|B,minecraft:red_wool,minecraft:red_wool,4,4|G,9");
        helper.assertTrue(atomic.getBoundaryChunkIntervalX() == 0 &&
                atomic.getBoundaryBlockA().equals("minecraft:yellow_wool"),
                "an invalid section leaves every extended setting unchanged");
        helper.assertTrue(PersonalSpaceSettings.parseLayers("minecraft:stone*999").get(0).count() == 255,
                "layer height is clamped to 255");
        helper.assertTrue(PersonalSpaceSettings.parseLayers("minecraft:stone:3").isEmpty(),
                "unknown legacy metadata is refused");

        var allowed = PersonalSpaceConfig.allowedBlocks();
        helper.assertTrue(PersonalSpaceSettings.canUseLayers(PersonalSpaceSettings.PRESET_UW_MINING, allowed),
                "upstream presets are always allowed");
        helper.assertTrue(PersonalSpaceSettings.canUseLayers("minecraft:bedrock;minecraft:netherrack*3", allowed),
                "configured blocks are allowed");
        helper.assertTrue(!PersonalSpaceSettings.canUseLayers("minecraft:diamond_block", allowed),
                "blocks outside the allow-list are refused");
        helper.assertTrue(PersonalSpaceConfig.allowedGapBlocks().contains("minecraft:black_concrete") &&
                PersonalSpaceConfig.allowedGapBlocks().contains("minecraft:magenta_wool"),
                "default decoration list has wools and concrete");
        helper.assertTrue(PersonalSpaceSettings.canUseBiome("Plains", java.util.List.of()) &&
                PersonalSpaceSettings.normalizeBiome("Extreme Hills").equals("minecraft:windswept_hills"),
                "legacy biome names are converted");
        helper.succeed();
    }

    @GameTest(template = "empty_16", timeoutTicks = 200)
    public static void personalSpaceCreationKeepsIdAndOnlyNewChunksSeeWorldgenChanges(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        var settings = PersonalSpaceSettings.roads();
        int id = PersonalSpaceWorlds.create(server, settings);
        ServerLevel world = PersonalSpaceWorlds.load(server, id);
        helper.assertTrue(world != null && world.dimension().equals(PersonalSpaceWorlds.key(id)),
                "created dimension has its stable personal key");
        helper.assertTrue(world.getMinBuildHeight() == 0 && world.getMaxBuildHeight() == 256,
                "personal dimension type spans y=0..255 like 1.7.10");
        helper.assertTrue(PersonalSpaceDirectory.get(server).settings(id).equals(settings),
                "dimension settings are kept by overworld SavedData");
        var restored = PersonalSpaceDirectory.load(PersonalSpaceDirectory.get(server).save(new CompoundTag()));
        helper.assertTrue(restored.settings(id).equals(settings) && restored.nextId() > id,
                "ID and settings survive SavedData roundtrip");
        helper.assertTrue(world.getBlockState(new BlockPos(128, 63, 0)).is(Blocks.CYAN_CONCRETE),
                "first road chunk uses the configured border block");
        helper.assertTrue(world.getBlockState(new BlockPos(8, 64, 8)).is(Blocks.SMOOTH_STONE_SLAB) &&
                world.getBlockState(new BlockPos(20, 53, 20)).is(Blocks.WHITE_CONCRETE) &&
                world.getBlockState(new BlockPos(20, 54, 20)).isAir(),
                "spawn platform is generated");

        PersonalSpaceSettings live = PersonalSpaceDirectory.get(server).settings(id);
        live.setLayers(PersonalSpaceSettings.PRESET_UW_MINING);
        PersonalSpaceWorlds.settingsChanged(server, id);
        helper.assertTrue(world.getBlockState(new BlockPos(128, 63, 0)).is(Blocks.CYAN_CONCRETE),
                "existing chunks are not regenerated");
        helper.assertTrue(world.getBlockState(new BlockPos(4096 + 8, 10, 4096 + 8)).is(Blocks.STONE),
                "chunks generated after a change use the new layers");

        helper.assertTrue(world.getLevelData() instanceof PersonalSpaceLevelData,
                "personal dimension has its own time and weather data");
        live.setWorldTime(1234L);
        helper.assertTrue(world.getDayTime() == 1234L, "world time is stored per dimension");
        live.setDaylightCycle(PersonalSpaceSettings.DaylightCycle.MOON);
        helper.assertTrue(world.getDayTime() == 18000L && live.getWorldTime() == 1234L,
                "fixed moon shows midnight while the stored time is kept");
        live.setDaylightCycle(PersonalSpaceSettings.DaylightCycle.CYCLE);
        helper.assertTrue(!world.isRaining(), "weather is disabled by default");
        helper.succeed();
    }

    @GameTest(template = "empty_16", timeoutTicks = 200)
    public static void personalSpacePortalEditorCreatesLinkedDimension(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, PersonalSpacePortalRegistry.PORTAL.get());
        var portal = (PersonalSpacePortalEntity) helper.getBlockEntity(pos);
        var player = net.minecraftforge.common.util.FakePlayerFactory.getMinecraft(helper.getLevel());

        var forbidden = PersonalSpaceSettings.fromPreset("minecraft:diamond_block*3");
        portal.updateSettings(player, forbidden);
        helper.assertTrue(!portal.isActive(), "forbidden layers do not create a dimension");

        var wanted = PersonalSpaceSettings.flat();
        wanted.setSkyColor(0x102030);
        portal.updateSettings(player, wanted);
        helper.assertTrue(portal.isActive() && portal.targetPersonalId() > 0, "editor creates and links a dimension");
        int id = portal.targetPersonalId();
        var server = helper.getLevel().getServer();
        var live = PersonalSpaceDirectory.get(server).settings(id);
        helper.assertTrue(live.getSkyColor() == 0x102030 && !live.getAllowGenerationChanges(),
                "settings are stored and worldgen is locked after creation");
        helper.assertTrue(portal.targetPos().equals(new BlockPos(8, 6, 8)), "target is (8, ground + 1, 8)");
        ServerLevel target = PersonalSpaceWorlds.load(server, id);
        helper.assertTrue(target.getBlockEntity(new BlockPos(8, 6, 8)) instanceof PersonalSpacePortalEntity back &&
                back.isActive() && back.targetDimension().equals(Level.OVERWORLD.location()) &&
                back.targetPos().equals(helper.absolutePos(pos)), "return portal points back to the overworld portal");

        var locked = PersonalSpaceSettings.mining();
        locked.setSkyColor(0x405060);
        portal.updateSettings(player, locked);
        helper.assertTrue(live.getSkyColor() == 0x405060 &&
                live.getLayersAsString().equals(PersonalSpaceSettings.PRESET_UW_GARDEN),
                "locked worldgen keeps layers while visual settings change");
        live.setAllowGenerationChanges(true);
        portal.updateSettings(player, locked);
        helper.assertTrue(live.getLayersAsString().equals(PersonalSpaceSettings.PRESET_UW_MINING) &&
                !live.getAllowGenerationChanges(), "allow-worldgen-change permits exactly one change");

        var stack = new ItemStack(PersonalSpacePortalRegistry.PORTAL_ITEM.get());
        portal.saveToItem(stack);
        var drops = Block.getDrops(portal.getBlockState(), helper.getLevel(), helper.absolutePos(pos), portal,
                helper.makeMockPlayer(), new ItemStack(Items.DIAMOND_PICKAXE));
        CompoundTag dropTag = drops.size() == 1 ? drops.get(0).getTagElement("BlockEntityTag") : null;
        helper.assertTrue(dropTag != null && dropTag.getBoolean("active") && !dropTag.contains("facing") &&
                ResourceLocation.tryParse(dropTag.getString("targetDimension"))
                        .equals(PersonalSpaceWorlds.key(id).location()),
                "mined portal keeps its link but not its facing");
        helper.assertTrue(Block.getDrops(portal.getBlockState(), helper.getLevel(), helper.absolutePos(pos), portal,
                helper.makeMockPlayer(), ItemStack.EMPTY).isEmpty(), "portal needs a pickaxe to drop");
        var restored = new PersonalSpacePortalEntity(pos, PersonalSpacePortalRegistry.PORTAL.get().defaultBlockState());
        restored.load(stack.getTagElement("BlockEntityTag"));
        helper.assertTrue(restored.isActive() && restored.targetPersonalId() == id,
                "portal item restores the PersonalSpace link");
        helper.succeed();
    }
}
