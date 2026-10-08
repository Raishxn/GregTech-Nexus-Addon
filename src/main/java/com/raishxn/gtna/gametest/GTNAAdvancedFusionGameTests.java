package com.raishxn.gtna.gametest;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.common.data.GTNAAdvancedFusion;
import com.raishxn.gtna.common.machine.multiblock.electric.AdvancedFusionReactorMachine;

import java.util.ArrayList;
import java.util.List;

@PrefixGameTestTemplate(false)
@GameTestHolder("gtna")
public final class GTNAAdvancedFusionGameTests {

    private GTNAAdvancedFusionGameTests() {}

    /** Every Forge of Gods chain / Advanced Fusion recipe has only resolvable item and fluid ingredients. */
    @GameTest(template = "empty_12", timeoutTicks = 40)
    public static void advancedFusionAndChainRecipesHaveNoEmptyIngredient(GameTestHelper helper) {
        List<String> problems = new ArrayList<>();
        int checked = 0;
        for (var recipe : helper.getLevel().getRecipeManager().getRecipes()) {
            if (!(recipe instanceof GTRecipe gt) || !GTNACORE.MOD_ID.equals(gt.id.getNamespace())) continue;
            String path = gt.id.getPath();
            if (!path.contains("advanced_fusion/") && !path.contains("godforge_chain/") && !path.contains("high_tier/"))
                continue;
            checked++;
            for (var content : gt.getInputContents(ItemRecipeCapability.CAP)) {
                var items = ItemRecipeCapability.CAP.of(content.content).getItems();
                if (items.length == 0 || items[0].isEmpty()) problems.add(path + " item");
            }
            for (var content : gt.getInputContents(FluidRecipeCapability.CAP)) {
                var fluids = FluidRecipeCapability.CAP.of(content.content).getStacks();
                if (fluids.length == 0 || fluids[0].isEmpty()) problems.add(path + " fluid");
            }
        }
        helper.assertTrue(checked >= 50, "expected the stage 2 and Advanced Fusion recipes, found " + checked);
        helper.assertTrue(problems.isEmpty(), String.join("; ", problems));
        helper.succeed();
    }

    /** The base structure (GTO kuangbiao1) forms from its preview at LuV with no extension. */
    @GameTest(template = "empty_48", timeoutTicks = 400)
    public static void advancedFusionBaseFormsAtLuV(GameTestHelper helper) {
        var definition = GTNAAdvancedFusion.ADVANCED_FUSION_REACTOR;
        var shape = definition.getMatchingShapes().get(0).getBlocks();
        AdvancedFusionReactorMachine machine = null;
        for (int x = 0; x < shape.length; x++) {
            for (int y = 0; y < shape[x].length; y++) {
                for (int z = 0; z < shape[x][y].length; z++) {
                    var info = shape[x][y][z];
                    var pos = new BlockPos(x + 1, y + 1, z + 1);
                    if (info == null || info.getBlockState().isAir()) {
                        helper.setBlock(pos, Blocks.AIR);
                        continue;
                    }
                    helper.setBlock(pos, info.getBlockState());
                    if (info.getBlockState().is(definition.getBlock())) {
                        machine = (AdvancedFusionReactorMachine) ((IMachineBlockEntity) helper.getBlockEntity(pos))
                                .getMetaMachine();
                    }
                }
            }
        }
        helper.assertTrue(machine != null, "the preview has a controller");
        helper.assertTrue(machine.checkPatternWithLock(),
                "the GTO kuangbiao1 structure must form: " + machine.getMultiblockState().error);
        machine.onStructureFormed();
        helper.assertTrue(machine.getBonusTier() == 0 && machine.getTier() == GTValues.LuV,
                "no extension built, tier must be LuV, got " + GTValues.VN[machine.getTier()]);
        helper.assertTrue(AdvancedFusionReactorMachine.bufferCapacity(GTValues.UEV, 16) == 2_560_000_000L,
                "16 inputs at UEV buffer 2.56 billion EU");
        helper.succeed();
    }
}
