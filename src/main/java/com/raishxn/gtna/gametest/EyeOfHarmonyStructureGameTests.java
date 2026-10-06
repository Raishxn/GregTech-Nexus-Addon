package com.raishxn.gtna.gametest;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.machines.GTAEMachines;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtna.common.data.GTNAEyeOfHarmonyContent;
import com.raishxn.gtna.common.data.GTNAItems;
import com.raishxn.gtna.common.data.GTNAMachines;
import com.raishxn.gtna.common.data.GTNAMaterials;
import com.raishxn.gtna.common.data.multiblock.EyeOfHarmonyAisles;
import com.raishxn.gtna.common.item.terminal.NexusAutoBuilder;
import com.raishxn.gtna.common.machine.multiblock.noenergy.EyeOfHarmonyMachine;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

@GameTestHolder("gtna")
@PrefixGameTestTemplate(false)
public final class EyeOfHarmonyStructureGameTests {

    @GameTest(template = "empty_16", timeoutTicks = 40)
    public static void eyeOfHarmonyGeometryMatchesPinnedGtnhAndMaterialsHaveNoBootstrap(GameTestHelper helper) throws Exception {
        var rows = new ArrayList<String>();
        for (String[] aisle : EyeOfHarmonyAisles.AISLES) {
            if (aisle.length != 33) throw new AssertionError("EOH must have 33 rows per aisle");
            for (String row : aisle) {
                if (row.length() != 33) throw new AssertionError("EOH rows must be 33 blocks wide");
                rows.add(row);
            }
        }
        // Independently calculated from the pinned GTNH shape: original[y][32-z][x], then symbol map.
        String digest = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(String.join("\n", rows).getBytes(StandardCharsets.UTF_8)));
        helper.assertTrue(digest.equals("6dd122943140a9cc9b4d37bad365200d8d1f26018944944577d2f66a4796b29f"),
                "all 35,937 cells must match the GTNH reference, including ignored spaces and controller");
        var core = List.of(GTNAMaterials.SpaceTime, GTNAMaterials.WhiteDwarfMatter, GTNAMaterials.BlackDwarfMatter,
                GTNAMaterials.Universium, GTNAMaterials.RawStarMatter, GTNAMaterials.Time, GTNAMaterials.Space);
        var fluids = new HashSet<net.minecraft.world.level.material.Fluid>();
        for (Material material : core) {
            helper.assertTrue(fluids.add(material.getFluid()), "core materials must have distinct real fluids");
            helper.assertTrue(material.hasFlag(MaterialFlags.DISABLE_MATERIAL_RECIPES),
                    "automatic low-tier recipes must not bootstrap deferred endgame materials");
        }
        helper.assertTrue(GTNAMaterials.RawStarMatter.getFluid(FluidStorageKeys.PLASMA) == null,
                "RawStarMatter is a normal liquid, not a turbine-fuel plasma");
        for (Material material : core.subList(0, 4)) {
            for (TagPrefix prefix : List.of(TagPrefix.ingot, TagPrefix.dust, TagPrefix.plate, TagPrefix.plateDense,
                    TagPrefix.frameGt, TagPrefix.rodLong, TagPrefix.gear, TagPrefix.gearSmall, TagPrefix.screw)) {
                helper.assertTrue(!ChemicalHelper.get(prefix, material).isEmpty(),
                        "missing structural form " + prefix.name + " of " + material.getName());
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty_48", timeoutTicks = 600)
    public static void eyeOfHarmonyFormsWithIndependentTiersInAllFourDirections(GameTestHelper helper) {
        for (Direction facing : List.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)) {
            var assembly = build(helper, facing, 8, 2, 5);
            assertMatches(helper, assembly, true, "complete GTNH structure must form facing " + facing);
            assembly.machine.onStructureFormed();
            helper.assertTrue(assembly.machine.isFormed() && assembly.machine.getCompressionTier() == 8 &&
                    assembly.machine.getAccelerationTier() == 2 && assembly.machine.getStabilisationTier() == 5,
                    "the three families must retain independent internal tiers");
            helper.assertTrue(assembly.machine.getParts().size() == 5, "the exact five ports must attach");
            assembly.machine.createUIWidget();
            assembly.machine.onStructureInvalid();
            helper.assertTrue(assembly.machine.getCompressionTier() == -1 &&
                    assembly.machine.getAccelerationTier() == -1 && assembly.machine.getStabilisationTier() == -1,
                    "unformed structures must not expose stale tiers");
            for (var entry : assembly.cells.entrySet()) {
                if (entry.getKey() != ' ') for (var pos : entry.getValue()) helper.setBlock(pos, Blocks.AIR);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty_48", timeoutTicks = 600)
    public static void eyeOfHarmonyRejectsMixedFieldsWrongCasingsAndExtraPorts(GameTestHelper helper) {
        var assembly = build(helper, Direction.NORTH, 0, 0, 0);
        assertMatches(helper, assembly, true, "base tier fields must form");
        for (int port = 0; port < 5; port++) {
            var pos = assembly.cells.get('B').get(port);
            Block original = helper.getBlockState(pos).getBlock();
            helper.setBlock(pos, GTNAEyeOfHarmonyContent.BOUNDARY_CASING.get());
            assertMatches(helper, assembly, false, "missing port " + port + " must fail");
            helper.setBlock(pos, original);
            assertMatches(helper, assembly, true, "restoring required port " + port + " must recover");
        }
        var alternatives = Map.of('F', GTNAEyeOfHarmonyContent.COMPRESSION_FIELDS[1].get(),
                'E', GTNAEyeOfHarmonyContent.ACCELERATION_FIELDS[1].get(),
                'G', GTNAEyeOfHarmonyContent.STABILISATION_FIELDS[1].get());
        for (var entry : alternatives.entrySet()) {
            BlockPos pos = assembly.cells.get(entry.getKey()).get(0);
            Block original = helper.getBlockState(pos).getBlock();
            helper.setBlock(pos, entry.getValue());
            assertMatches(helper, assembly, false, "one mixed field tier must invalidate its family");
            helper.setBlock(pos, original);
            assertMatches(helper, assembly, true, "restoring a uniform family must recover without stale context");
        }
        BlockPos spatial = assembly.cells.get('A').get(0);
        helper.setBlock(spatial, Blocks.AIR);
        assertMatches(helper, assembly, false, "a missing spatial casing must fail");
        helper.setBlock(spatial, GTNAEyeOfHarmonyContent.SPATIAL_CASING.get());
        BlockPos boundary = assembly.cells.get('B').get(5);
        for (Block invalid : List.of(GTBlocks.HIGH_POWER_CASING.get(),
                GTMachines.ENERGY_INPUT_HATCH[GTValues.LV].getBlock(),
                GTMachines.ITEM_IMPORT_BUS[GTValues.LV].getBlock(),
                GTMachines.FLUID_IMPORT_HATCH[GTValues.LV].getBlock(),
                GTMachines.ITEM_EXPORT_BUS[GTValues.LV].getBlock(),
                GTMachines.FLUID_EXPORT_HATCH[GTValues.LV].getBlock())) {
            helper.setBlock(boundary, invalid);
            assertMatches(helper, assembly, false, "legacy casing, energy or extra port must fail: " + invalid);
        }
        helper.setBlock(boundary, GTNAEyeOfHarmonyContent.BOUNDARY_CASING.get());
        helper.setBlock(assembly.cells.get(' ').get(0), Blocks.GOLD_BLOCK);
        assertMatches(helper, assembly, true, "ignored spaces in the GTNH source must not become mandatory air");
        helper.succeed();
    }

    @GameTest(template = "empty_48", timeoutTicks = 600)
    public static void eyeOfHarmonyRejectsMissingStockingCraftingAndDualInputs(GameTestHelper helper) {
        var assembly = build(helper, Direction.NORTH, 0, 0, 0);
        var boundary = assembly.cells.get('B');
        BlockPos itemInput = boundary.get(0);
        for (Block invalid : List.of(Blocks.AIR, GTNAEyeOfHarmonyContent.BOUNDARY_CASING.get(),
                GTAEMachines.STOCKING_IMPORT_BUS_ME.getBlock(), GTAEMachines.ME_PATTERN_BUFFER.getBlock(),
                GTAEMachines.ME_PATTERN_BUFFER_PROXY.getBlock(),
                Arrays.stream(GTMachines.DUAL_IMPORT_HATCH).filter(Objects::nonNull).findFirst().orElseThrow()
                        .getBlock())) {
            helper.setBlock(itemInput, invalid);
            assertMatches(helper, assembly, false, "missing, stocking, crafting or dual bus must be rejected");
        }
        var normalPorts = new MachineDefinition[] { GTMachines.ITEM_IMPORT_BUS[GTValues.LV],
                GTMachines.FLUID_IMPORT_HATCH[GTValues.LV], GTMachines.FLUID_IMPORT_HATCH[GTValues.LV],
                GTMachines.ITEM_EXPORT_BUS[GTValues.LV], GTMachines.FLUID_EXPORT_HATCH[GTValues.LV] };
        var mePorts = new MachineDefinition[] { GTAEMachines.ITEM_IMPORT_BUS_ME,
                GTAEMachines.FLUID_IMPORT_HATCH_ME, GTAEMachines.FLUID_IMPORT_HATCH_ME,
                GTAEMachines.ITEM_EXPORT_BUS_ME, GTAEMachines.FLUID_EXPORT_HATCH_ME };
        for (int index = 0; index < normalPorts.length; index++) {
            for (int restore = 0; restore < mePorts.length; restore++)
                helper.setBlock(boundary.get(restore), mePorts[restore].getBlock());
            helper.setBlock(boundary.get(index), normalPorts[index].getBlock());
            assertMatches(helper, assembly, false, "ordinary port must fail at position " + index);
        }
        for (int restore = 0; restore < mePorts.length; restore++)
            helper.setBlock(boundary.get(restore), mePorts[restore].getBlock());
        helper.setBlock(itemInput, GTAEMachines.ITEM_IMPORT_BUS_ME.getBlock());
        helper.setBlock(boundary.get(1), GTAEMachines.STOCKING_IMPORT_HATCH_ME.getBlock());
        assertMatches(helper, assembly, false, "a stocking fluid input must be rejected");
        helper.setBlock(boundary.get(1), GTNAEyeOfHarmonyContent.BOUNDARY_CASING.get());
        assertMatches(helper, assembly, false, "two fluid input hatches are mandatory");
        helper.setBlock(boundary.get(1), GTAEMachines.FLUID_IMPORT_HATCH_ME.getBlock());
        assertMatches(helper, assembly, true, "restoring the ME ports must recover");
        helper.succeed();
    }

    @GameTest(template = "empty_48", timeoutTicks = 600)
    public static void eyeOfHarmonyPreviewCanActuallyFormTheMachine(GameTestHelper helper) {
        var definition = GTNAMachines.EYE_OF_HARMONY;
        var shapes = definition.getMatchingShapes();
        helper.assertTrue(shapes.size() == 1, "fixed 33-cube structure must have one matching preview");
        var shape = shapes.get(0).getBlocks();
        EyeOfHarmonyMachine machine = null;
        for (int x = 0; x < shape.length; x++) {
            for (int y = 0; y < shape[x].length; y++) {
                for (int z = 0; z < shape[x][y].length; z++) {
                    var info = shape[x][y][z];
                    if (info == null || info.getBlockState().isAir()) continue;
                    var pos = new BlockPos(x + 3, y + 3, z + 3);
                    helper.setBlock(pos, info.getBlockState());
                    if (info.getBlockState().is(definition.getBlock())) {
                        machine = (EyeOfHarmonyMachine) ((IMachineBlockEntity) helper.getBlockEntity(pos))
                                .getMetaMachine();
                    }
                }
            }
        }
        helper.assertTrue(machine != null, "preview must contain the actual controller");
        helper.assertTrue(machine.checkPatternWithLock(),
                "generated preview must satisfy exact hatch counts and uniform field tiers: " +
                        machine.getMultiblockState().error);
        machine.onStructureFormed();
        helper.assertTrue(machine.getCompressionTier() == 0 && machine.getAccelerationTier() == 0 &&
                machine.getStabilisationTier() == 0, "default preview must build the crude fields consistently");
        helper.succeed();
    }

    @GameTest(template = "empty_48", timeoutTicks = 600)
    public static void eyeOfHarmonyNexusTerminalBuildsAValidStructureInCreative(GameTestHelper helper) {
        var pos = new BlockPos(19, 19, 3);
        helper.setBlock(pos, GTNAMachines.EYE_OF_HARMONY.getBlock());
        var machine = (EyeOfHarmonyMachine) ((IMachineBlockEntity) helper.getBlockEntity(pos)).getMetaMachine();
        machine.setFrontFacing(Direction.NORTH);
        var player = net.minecraftforge.common.util.FakePlayerFactory.get(helper.getLevel(),
                new com.mojang.authlib.GameProfile(UUID.randomUUID(), "EOHStructureQA"));
        player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
        NexusAutoBuilder.autoBuild(player, machine, GTNAItems.NEXUS_STRUCTURE_TERMINAL.asStack());
        helper.assertTrue(machine.checkPatternWithLock(),
                "Nexus Terminal must build a valid structure with exactly five ports: " +
                        machine.getMultiblockState().error);
        machine.onStructureFormed();
        helper.assertTrue(machine.getParts().size() == 5 && machine.getCompressionTier() == 0 &&
                machine.getAccelerationTier() == 0 && machine.getStabilisationTier() == 0,
                "terminal defaults must produce uniform crude fields and all required ports");
        helper.succeed();
    }

    static Assembly build(GameTestHelper helper, Direction facing, int compression, int acceleration,
                          int stabilisation) {
        var center = new BlockPos(19, 19, 19);
        var controllerPos = center.relative(facing, 16);
        var right = facing.getClockWise();
        var back = facing.getOpposite();
        var cells = new HashMap<Character, List<BlockPos>>();
        for (int z = 0; z < 33; z++) {
            for (int y = 0; y < 33; y++) {
                for (int x = 0; x < 33; x++) {
                    char symbol = EyeOfHarmonyAisles.AISLES[z][y].charAt(x);
                    var pos = controllerPos.relative(right, x - 16).above(y - 16).relative(back, 32 - z);
                    cells.computeIfAbsent(symbol, ignored -> new ArrayList<>()).add(pos);
                    Block block = switch (symbol) {
                        case 'A' -> GTNAEyeOfHarmonyContent.SPATIAL_CASING.get();
                        case 'D' -> GTNAEyeOfHarmonyContent.TEMPORAL_CASING.get();
                        case 'B' -> GTNAEyeOfHarmonyContent.BOUNDARY_CASING.get();
                        case 'E' -> GTNAEyeOfHarmonyContent.ACCELERATION_FIELDS[acceleration].get();
                        case 'F' -> GTNAEyeOfHarmonyContent.COMPRESSION_FIELDS[compression].get();
                        case 'G' -> GTNAEyeOfHarmonyContent.STABILISATION_FIELDS[stabilisation].get();
                        case '~' -> GTNAMachines.EYE_OF_HARMONY.getBlock();
                        default -> Blocks.AIR;
                    };
                    if (symbol != ' ') helper.setBlock(pos, block);
                }
            }
        }
        MachineDefinition[] ports = { GTAEMachines.ITEM_IMPORT_BUS_ME,
                GTAEMachines.FLUID_IMPORT_HATCH_ME, GTAEMachines.FLUID_IMPORT_HATCH_ME,
                GTAEMachines.ITEM_EXPORT_BUS_ME, GTAEMachines.FLUID_EXPORT_HATCH_ME };
        for (int i = 0; i < ports.length; i++) helper.setBlock(cells.get('B').get(i), ports[i].getBlock());
        var machine = (EyeOfHarmonyMachine) ((IMachineBlockEntity) helper.getBlockEntity(controllerPos))
                .getMetaMachine();
        machine.setFrontFacing(facing);
        return new Assembly(machine, cells);
    }

    private static void assertMatches(GameTestHelper helper, Assembly assembly, boolean expected, String reason) {
        // The async assembler shares this match context; use the GTCEu controller lock.
        boolean matches = assembly.machine.checkPatternWithLock();
        helper.assertTrue(matches == expected, reason + ": " + assembly.machine.getMultiblockState().error);
    }

    record Assembly(EyeOfHarmonyMachine machine, Map<Character, List<BlockPos>> cells) {}
}
