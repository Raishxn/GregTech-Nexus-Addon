package com.raishxn.gtna.gametest;

import com.lowdragmc.lowdraglib.utils.BlockInfo;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtna.common.data.EyeOfHarmonyLegacyMappings;
import com.raishxn.gtna.common.data.GTNAEyeOfHarmonyContent;
import com.raishxn.gtna.common.data.GTNAItems;
import com.raishxn.gtna.common.item.terminal.NexusAutoBuilder;
import com.raishxn.gtna.common.item.terminal.ui.BlockSelectionConfigWidget;
import com.raishxn.gtna.common.item.terminal.ui.BlockSelectionConfigWidget.BlockCategory;
import com.raishxn.gtna.common.item.terminal.ui.NexusTerminalUIFactory;

import java.util.List;

@GameTestHolder("gtna")
@PrefixGameTestTemplate(false)
public final class NexusTerminalGameTests {

    @GameTest(template = "empty_16", timeoutTicks = 40)
    public static void nexusTerminalSelectionsKeepLegacyAndRejectIncompatibleCells(GameTestHelper helper) {
        ItemStack terminal = GTNAItems.NEXUS_STRUCTURE_TERMINAL.asStack();
        var batteries = BlockSelectionConfigWidget.getEntries(BlockCategory.BATTERY);
        helper.assertTrue(
                !batteries.isEmpty() && batteries.size() == com.gregtechceu.gtceu.api.GTCEuAPI.PSS_BATTERIES.size() &&
                        batteries.stream()
                                .allMatch(item -> item.getItem() instanceof net.minecraft.world.item.BlockItem block &&
                                        block.getBlock() instanceof com.gregtechceu.gtceu.common.block.BatteryBlock),
                "Battery selector must contain only registered substation capacitors, never battery-buffer machines");
        var coils = BlockSelectionConfigWidget.getEntries(BlockCategory.COILS);
        terminal.getOrCreateTag().putInt("SelectedCoil", 1);
        helper.assertTrue(ItemStack
                .isSameItem(BlockSelectionConfigWidget.getSelectedBlock(terminal, BlockCategory.COILS), coils.get(1)),
                "old index NBT must still resolve the same coil");
        var chosen = GTNAEyeOfHarmonyContent.COMPRESSION_FIELDS[8].asStack();
        BlockSelectionConfigWidget.select(terminal, BlockCategory.EOH_COMPRESSION, chosen);
        helper.assertTrue(
                ItemStack.isSameItem(
                        BlockSelectionConfigWidget.getSelectedBlock(terminal, BlockCategory.EOH_COMPRESSION), chosen),
                "new selection must persist by registry ID");
        var compatible = BlockSelectionConfigWidget.applySelections(new BlockInfo[] {
                new BlockInfo(GTNAEyeOfHarmonyContent.COMPRESSION_FIELDS[0].get().defaultBlockState()),
                new BlockInfo(GTNAEyeOfHarmonyContent.COMPRESSION_FIELDS[8].get().defaultBlockState()) }, terminal);
        helper.assertTrue(compatible.size() == 1 && ItemStack.isSameItem(compatible.get(0), chosen),
                "selection must constrain compatible cells");
        var stone = new BlockInfo[] { new BlockInfo(Blocks.STONE.defaultBlockState()) };
        helper.assertTrue(BlockSelectionConfigWidget.applySelections(stone, terminal).get(0).is(Items.STONE),
                "unrelated cells must retain their native candidates");
        BlockSelectionConfigWidget.select(terminal, BlockCategory.EOH_COMPRESSION, chosen);
        helper.assertTrue(BlockSelectionConfigWidget.getSelectedBlock(terminal, BlockCategory.EOH_COMPRESSION) == null,
                "second click must clear the choice");
        BlockSelectionConfigWidget.clearAll(terminal);
        helper.assertTrue(BlockSelectionConfigWidget.getSelectedBlock(terminal, BlockCategory.COILS) == null,
                "clear all must remove legacy choices too");
        terminal.getOrCreateTag().putInt("Repetitions", Integer.MAX_VALUE);
        terminal.getOrCreateTag().putInt("ModuleBuild", -5);
        var settings = NexusTerminalUIFactory.AutoBuildSetting.getSetting(terminal);
        helper.assertTrue(settings.getRepetitions() == 1000 && settings.getModuleBuild() == 0,
                "NBT must obey the same numeric limits as the UI");
        var glass = BlockSelectionConfigWidget.getEntries(BlockCategory.GLASS);
        helper.assertTrue(glass.size() > 1, "fixture requires two registered glass choices");
        BlockSelectionConfigWidget.select(terminal, BlockCategory.GLASS, glass.get(0));
        var glassCandidates = new BlockInfo[] {
                new BlockInfo(
                        ((net.minecraft.world.item.BlockItem) glass.get(0).getItem()).getBlock().defaultBlockState()),
                new BlockInfo(
                        ((net.minecraft.world.item.BlockItem) glass.get(1).getItem()).getBlock().defaultBlockState()) };
        helper.assertTrue(BlockSelectionConfigWidget.applySelections(glassCandidates, terminal).size() == 2,
                "hidden legacy glass choices must no longer constrain building candidates");
        helper.succeed();
    }

    @GameTest(template = "empty_48", timeoutTicks = 600)
    public static void nexusTerminalReplacesEyeFieldsWithoutReplacingControllerOrIgnoredCells(GameTestHelper helper) {
        var assembly = EyeOfHarmonyStructureGameTests.build(helper, Direction.NORTH, 0, 0, 0);
        var machine = assembly.machine();
        helper.assertTrue(com.raishxn.gtna.api.machine.multiblock.GTNAStructureRefresh.refresh(machine, true),
                "baseline structure must form before replacing its fields");
        var originalHolder = helper.getLevel().getBlockEntity(machine.getPos());
        var originalParts = machine.getParts().stream().collect(java.util.stream.Collectors.toMap(
                part -> part.self().getPos(), part -> helper.getLevel().getBlockEntity(part.self().getPos())));
        for (var holder : originalParts.values()) holder.getPersistentData().putString("NexusReplaceQA", "keep");

        var ignored = machine.getPos().relative(Direction.SOUTH, 16).above(5);
        helper.getLevel().setBlock(ignored, Blocks.DIAMOND_BLOCK.defaultBlockState(), 3);
        var player = net.minecraftforge.common.util.FakePlayerFactory.get(helper.getLevel(),
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "NexusTerminalQA"));
        player.setGameMode(GameType.CREATIVE);
        ItemStack terminal = GTNAItems.NEXUS_STRUCTURE_TERMINAL.asStack();
        player.setItemInHand(InteractionHand.MAIN_HAND, terminal);
        terminal.getOrCreateTag().putBoolean("ReplaceMode", true);
        terminal.getOrCreateTag().putBoolean("DemolitionMode", true);
        terminal.getOrCreateTag().putBoolean("MirrorBuild", true);
        BlockSelectionConfigWidget.select(terminal, BlockCategory.EOH_COMPRESSION,
                GTNAEyeOfHarmonyContent.COMPRESSION_FIELDS[8].asStack());
        NexusAutoBuilder.autoBuild(player, machine, terminal);
        var state = machine.getMultiblockState();
        var changed = helper.absolutePos(assembly.cells().get('F').get(0));
        for (int replay = 0; replay < 100; replay++)
            state.onBlockStateChanged(changed, helper.getLevel().getBlockState(changed));
        helper.assertTrue(machine.getCompressionTier() == 0 &&
                com.raishxn.gtna.common.item.terminal.NexusBuildCheckGuard.skips(state, changed),
                "Forge notifications after returning from autoBuild must defer checks, retaining old tiers");
        helper.assertTrue(!com.raishxn.gtna.common.item.terminal.NexusBuildCheckGuard.skips(state, machine.getPos()),
                "controller removal must never be suppressed");
        com.raishxn.gtna.common.item.terminal.NexusBuildCheckGuard.flushPending();
        helper.assertTrue(machine.isFormed() && machine.getCompressionTier() == 8 &&
                !com.raishxn.gtna.common.item.terminal.NexusBuildCheckGuard.skips(state, changed),
                "one final refresh must form the new fields and release suppression");
        helper.assertTrue(helper.getLevel().getBlockEntity(machine.getPos()) == originalHolder,
                "replace must preserve the original controller and its stored state");
        helper.assertTrue(helper.getLevel().getBlockState(ignored).is(Blocks.DIAMOND_BLOCK),
                "demolition must preserve GTNH's ignored inner cells");
        var counts = assembly.cells().get('F').stream().map(helper::absolutePos)
                .collect(java.util.stream.Collectors.groupingBy(
                        pos -> BuiltInRegistries.BLOCK.getKey(helper.getLevel().getBlockState(pos).getBlock()),
                        java.util.stream.Collectors.counting()));
        for (var localPos : assembly.cells().get('F')) helper.assertTrue(
                helper.getLevel().getBlockState(helper.absolutePos(localPos))
                        .is(GTNAEyeOfHarmonyContent.COMPRESSION_FIELDS[8].get()),
                "all compression fields must use the selected tier: " + counts + "; first mismatch " +
                        helper.absolutePos(localPos) +
                        "; selection " +
                        BlockSelectionConfigWidget.getSelectedBlock(terminal, BlockCategory.EOH_COMPRESSION));
        for (boolean noHatch : new boolean[] { false, true }) {
            terminal.getOrCreateTag().putBoolean("NoHatchMode", noHatch);
            NexusAutoBuilder.autoBuild(player, machine, terminal);
            com.raishxn.gtna.common.item.terminal.NexusBuildCheckGuard.flushPending();
            helper.assertTrue(machine.isFormed() && machine.getParts().size() == originalParts.size(),
                    "Replace must preserve required part counts regardless of No Hatch");
            for (var entry : originalParts.entrySet()) helper.assertTrue(
                    helper.getLevel().getBlockEntity(entry.getKey()) == entry.getValue() &&
                            entry.getValue().getPersistentData().getString("NexusReplaceQA").equals("keep"),
                    "Replace must preserve the exact installed hatch/bus block entity and its NBT");
        }
        var broken = helper.absolutePos(assembly.cells().get('A').get(0));
        helper.getLevel().setBlock(broken, Blocks.AIR.defaultBlockState(), 3);
        state.onBlockStateChanged(broken, Blocks.AIR.defaultBlockState());
        helper.assertTrue(!machine.isFormed(), "ordinary later damage must invalidate the structure normally");
        helper.succeed();
    }

    @GameTest(template = "empty_16", timeoutTicks = 80)
    public static void nexusTerminalMirrorAndDemolitionUseBoundedPatternCells(GameTestHelper helper) {
        var local = new net.minecraft.core.BlockPos(8, 8, 8);
        helper.setBlock(local, com.raishxn.gtna.common.data.GTNAMachines.EYE_OF_HARMONY.getBlock());
        var machine = (com.raishxn.gtna.common.machine.multiblock.noenergy.EyeOfHarmonyMachine) ((com.gregtechceu.gtceu.api.machine.IMachineBlockEntity) helper
                .getBlockEntity(local)).getMetaMachine();
        machine.setFrontFacing(Direction.NORTH);
        var original = helper.getLevel().getBlockEntity(machine.getPos());
        var raw = com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern.start().aisle("~aA")
                .where('~', com.gregtechceu.gtceu.api.pattern.Predicates.controller(
                        com.gregtechceu.gtceu.api.pattern.Predicates.blocks(machine.getBlockState().getBlock())))
                .where('a', com.gregtechceu.gtceu.api.pattern.Predicates.air())
                .where('A', com.gregtechceu.gtceu.api.pattern.Predicates.blocks(Blocks.GOLD_BLOCK)).build();
        var pattern = com.raishxn.gtna.common.item.terminal.NexusBlockPattern.fromBlockPattern(raw);
        var player = net.minecraftforge.common.util.FakePlayerFactory.get(helper.getLevel(),
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "NexusModesQA"));
        player.setGameMode(GameType.CREATIVE);
        var terminal = GTNAItems.NEXUS_STRUCTURE_TERMINAL.asStack();
        terminal.getOrCreateTag().putBoolean("DemolitionMode", true);
        helper.getLevel().setBlock(machine.getPos().west(), Blocks.STONE.defaultBlockState(), 3);
        helper.getLevel().setBlock(machine.getPos().above(), Blocks.BEDROCK.defaultBlockState(), 3);
        com.raishxn.gtna.common.item.terminal.NexusBuildCheckGuard.run(machine.getMultiblockState(),
                () -> pattern.autoBuild(player, machine.getMultiblockState(),
                        NexusTerminalUIFactory.AutoBuildSetting.getSetting(terminal), terminal));
        helper.assertTrue(helper.getLevel().getBlockState(machine.getPos().west()).isAir() &&
                helper.getLevel().getBlockState(machine.getPos().west(2)).is(Blocks.GOLD_BLOCK),
                "demolition must clear required air while normal placement builds the native side");
        terminal.getOrCreateTag().putBoolean("MirrorBuild", true);
        helper.getLevel().setBlock(machine.getPos().east(), Blocks.STONE.defaultBlockState(), 3);
        com.raishxn.gtna.common.item.terminal.NexusBuildCheckGuard.run(machine.getMultiblockState(),
                () -> pattern.autoBuild(player, machine.getMultiblockState(),
                        NexusTerminalUIFactory.AutoBuildSetting.getSetting(terminal), terminal));
        helper.assertTrue(helper.getLevel().getBlockState(machine.getPos().east()).isAir() &&
                helper.getLevel().getBlockState(machine.getPos().east(2)).is(Blocks.GOLD_BLOCK),
                "mirror must build and clear the opposite side");
        helper.assertTrue(helper.getLevel().getBlockEntity(machine.getPos()) == original &&
                helper.getLevel().getBlockState(machine.getPos().above()).is(Blocks.BEDROCK),
                "both modes must preserve controller and unrelated cells");
        helper.succeed();
    }

    @GameTest(template = "empty_16", timeoutTicks = 40)
    public static void eyeRetiredBlocksHaveNoRegistryEntriesAndHaveMigrationTargets(GameTestHelper helper) {
        for (String id : List.of("dimensional_bridge_casing", "dimensional_stability_casing",
                "spacetime_compression_field_generator")) {
            var key = new net.minecraft.resources.ResourceLocation("gtna", id);
            helper.assertTrue(!BuiltInRegistries.BLOCK.getKey(BuiltInRegistries.BLOCK.get(key)).equals(key) &&
                    !BuiltInRegistries.ITEM.getKey(BuiltInRegistries.ITEM.get(key)).equals(key),
                    "duplicate aliases must not own a canonical block or item entry: " + key);
            helper.assertTrue(EyeOfHarmonyLegacyMappings.replacement(id) != null,
                    "old save mappings must have a live replacement");
        }
        helper.succeed();
    }
}
