package com.raishxn.gtna.gametest;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtna.api.capability.WirelessEnergyManager;
import com.raishxn.gtna.common.data.GTNABlocks;
import com.raishxn.gtna.common.data.GTNAMachines;
import com.raishxn.gtna.common.data.GTNARecipeType;
import com.raishxn.gtna.common.machine.multiblock.noenergy.EyeOfHarmonyMachine;
import com.raishxn.gtna.utils.datastructure.Int128;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

@GameTestHolder("gtna")
@PrefixGameTestTemplate(false)
public final class PlayerReportRegressionTests {

    @GameTest(template = "empty_16", timeoutTicks = 40)
    public static void eyeOfHarmonyDisplayHandlesZeroAnd128BitEnergy(GameTestHelper helper) throws Exception {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, GTNAMachines.EYE_OF_HARMONY.getBlock());
        var holder = (MetaMachineBlockEntity) helper.getBlockEntity(pos);
        var machine = (EyeOfHarmonyMachine) holder.getMetaMachine();
        // Isolate the display's formed branch; structure matching is outside this regression.
        var formed = MultiblockControllerMachine.class.getDeclaredField("isFormed");
        formed.setAccessible(true);
        formed.setBoolean(machine, true);
        var lines = new ArrayList<Component>();
        machine.addDisplayText(lines);
        helper.assertTrue(lines.size() >= 5, "formed display must show zero energy without throwing");
        machine.createUIWidget();

        var owner = UUID.randomUUID();
        var ownerField = EyeOfHarmonyMachine.class.getDeclaredField("networkOwner");
        ownerField.setAccessible(true);
        ownerField.set(machine, owner);
        var overclock = EyeOfHarmonyMachine.class.getDeclaredField("overclockLevel");
        overclock.setAccessible(true);
        overclock.setInt(machine, 24);
        BigInteger balance = BigInteger.ONE.shiftLeft(100).add(BigInteger.valueOf(123));
        WirelessEnergyManager.setEnergy(helper.getLevel(), owner, Int128.fromBigInteger(balance));
        try {
            lines.clear();
            machine.addDisplayText(lines);
            helper.assertTrue(
                    lines.stream().anyMatch(line -> hasExactEnergyTooltip(line, balance)),
                    "network display must preserve integers above Long.MAX_VALUE");
            helper.assertTrue(
                    machine.getStartupEnergy().toBigInteger().compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0,
                    "fixture must exercise startup energy above Long.MAX_VALUE");
            helper.assertTrue(lines.stream().anyMatch(line -> hasExactEnergyTooltip(line,
                    machine.getStartupEnergy().toBigInteger())),
                    "startup display must preserve its full integer value");
            machine.createUIWidget();
        } finally {
            WirelessEnergyManager.setEnergy(helper.getLevel(), owner, Int128.ZERO());
            formed.setBoolean(machine, false);
        }
        helper.succeed();
    }

    @GameTest(template = "empty_16", timeoutTicks = 40)
    public static void controllerRecipesKeepBlockAndExternalItemIngredients(GameTestHelper helper) {
        assertInput(helper, GTNARecipeType.HYDRAULIC_MANUFACTURING, "steam_elevator",
                GTNABlocks.STEAM_COMPACT_PIPE_CASING.asItem(), 4);
        assertInput(helper, GTNARecipeType.HYDRAULIC_MANUFACTURING, "steam_elevator", Blocks.BRICKS.asItem(), 64);
        assertInput(helper, GTNARecipeType.HYDRAULIC_MANUFACTURING, "steam_lava_maker",
                GTNABlocks.STRONZE_WRAPPED_CASING.asItem(), 1);
        assertInput(helper, GTNARecipeType.HYDRAULIC_MANUFACTURING, "stone_superheater_controller",
                GTNABlocks.STRONZE_WRAPPED_CASING.asItem(), 1);
        assertInput(helper, GTRecipeTypes.ASSEMBLER_RECIPES, "gtna_me_advanced_pattern_buffer",
                item("expatternprovider:ex_pattern_provider"), 3);
        assertInput(helper, GTRecipeTypes.ASSEMBLER_RECIPES, "gtna_me_advanced_pattern_buffer",
                item("expatternprovider:ex_interface"), 3);
        assertInput(helper, GTRecipeTypes.ASSEMBLER_RECIPES, "gtna_me_ultimate_pattern_buffer",
                item("ae2:quantum_ring"), 4);
        assertInput(helper, GTRecipeTypes.ASSEMBLER_RECIPES, "gtna_me_craft_pattern_hatch",
                item("expatternprovider:assembler_matrix_crafter"), 2);
        assertInput(helper, GTRecipeTypes.ASSEMBLY_LINE_RECIPES, "nexus_molecular_forge",
                item("expatternprovider:assembler_matrix_pattern"), 32);
        assertInput(helper, GTRecipeTypes.ROCK_BREAKER_RECIPES, "steam_basalt_gen", Items.BLUE_ICE, 1);
        assertInput(helper, GTRecipeTypes.ROCK_BREAKER_RECIPES, "steam_deepslate_gen", Items.MAGMA_BLOCK, 1);
        for (String path : new String[] { "steam_basalt_gen", "steam_deepslate_gen" }) {
            var recipe = helper.getLevel().getRecipeManager().getAllRecipesFor(GTRecipeTypes.ROCK_BREAKER_RECIPES)
                    .stream().filter(candidate -> candidate.id.getPath().endsWith("/" + path) ||
                            candidate.id.getPath().equals(path))
                    .findFirst().orElseThrow();
            helper.assertTrue(recipe.getInputContents(ItemRecipeCapability.CAP).stream()
                    .allMatch(content -> content.chance == 0), "rock breaker catalysts must not be consumed");
        }
        helper.succeed();
    }

    private static Item item(String id) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
    }

    private static void assertInput(GameTestHelper helper, GTRecipeType type, String path, Item item, int count) {
        GTRecipe recipe = helper.getLevel().getRecipeManager().getAllRecipesFor(type).stream()
                .filter(candidate -> candidate.id.getPath().endsWith("/" + path) || candidate.id.getPath().equals(path))
                .findFirst().orElse(null);
        helper.assertTrue(recipe != null, "recipe must load: " + path);
        helper.assertTrue(item != Blocks.AIR.asItem(), "fixture ingredient must exist: " + path);
        boolean found = recipe.getInputContents(ItemRecipeCapability.CAP).stream()
                .anyMatch(content -> content.content instanceof Ingredient ingredient &&
                        Arrays.stream(ingredient.getItems())
                                .anyMatch(stack -> stack.is(item) && stack.getCount() == count));
        helper.assertTrue(found, path + " must require " + count + " of " + BuiltInRegistries.ITEM.getKey(item));
    }

    @GameTest(template = "empty_16", timeoutTicks = 40)
    public static void nexusDirectDebitHistoryPreserves128BitValuesAndRejectsFailedSpending(GameTestHelper helper) {
        var owner = UUID.randomUUID();
        var network = com.raishxn.gtna.common.data.NexusEnergyNetwork.get(helper.getLevel());
        var capacity = Int128.fromBigInteger(BigInteger.ONE.shiftLeft(100));
        var amount = Int128.fromBigInteger(BigInteger.ONE.shiftLeft(80).add(BigInteger.valueOf(123)));
        network.setMatrixStats(owner, 1, com.gregtechceu.gtceu.api.GTValues.LV, 1, capacity, true);
        network.setMatrixDimension(owner, helper.getLevel().dimension());
        network.setEnergy(owner, capacity);
        var source = net.minecraft.core.GlobalPos.of(helper.getLevel().dimension(), helper.absolutePos(BlockPos.ZERO));
        helper.assertTrue(
                network.consumeDirectEnergy(owner, amount, source, "block.gtna.eye_of_harmony", helper.getLevel()),
                "direct startup debit must accept amounts above Long.MAX_VALUE");
        var remaining = capacity.copy();
        remaining.subtract(amount);
        helper.assertTrue(network.getEnergy(owner).equals(remaining), "direct debit must subtract exactly once");
        var restored = new com.raishxn.gtna.common.data.NexusEnergyNetwork(
                network.save(new net.minecraft.nbt.CompoundTag()));
        helper.assertTrue(restored.getLastDirectDebit(owner).amount().equals(amount) &&
                restored.getLastDirectDebit(owner).source().equals(source),
                "transaction source and full 128-bit amount must survive serialization");
        var lines = new ArrayList<Component>();
        com.raishxn.gtna.common.item.NexusDirectDebitDisplay.append(lines, restored, owner);
        helper.assertTrue(lines.size() == 1 && hasExactEnergyTooltip(lines.get(0), amount.toBigInteger()),
                "both network panels share an exact 128-bit withdrawal tooltip");
        helper.assertTrue(!restored.consumeDirectEnergy(owner, capacity, source, "failed", helper.getLevel()) &&
                restored.getLastDirectDebit(owner).machineType().equals("block.gtna.eye_of_harmony") &&
                restored.getEnergy(owner).equals(remaining),
                "failed spending must neither change transaction history nor debit energy");
        helper.succeed();
    }

    private static boolean hasExactEnergyTooltip(Component line, BigInteger energy) {
        var hover = line.getStyle().getHoverEvent();
        var tooltip = hover == null ? null : hover.getValue(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT);
        if (tooltip == null) return false;
        String exact = FormattingUtil.formatNumbers(energy);
        return tooltip.getString().contains(exact) ||
                tooltip.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents translated &&
                        java.util.Arrays.stream(translated.getArgs()).anyMatch(exact::equals);
    }
}
