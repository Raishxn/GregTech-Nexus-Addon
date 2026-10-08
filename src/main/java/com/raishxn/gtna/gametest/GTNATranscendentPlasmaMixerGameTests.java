package com.raishxn.gtna.gametest;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.common.data.GTNATranscendentPlasmaMixer;
import com.raishxn.gtna.common.machine.multiblock.electric.TranscendentPlasmaMixerMachine;

import java.math.BigInteger;

@PrefixGameTestTemplate(false)
@GameTestHolder("gtna")
public final class GTNATranscendentPlasmaMixerGameTests {

    private GTNATranscendentPlasmaMixerGameTests() {}

    /** The DTSC recipe costs GTNH's 20 × 2,138,383,760 EU/t × 100 ticks, with no EU content in the recipe. */
    @GameTest(template = "empty_12", timeoutTicks = 40)
    public static void excitedDtscCostMatchesGtnh(GameTestHelper helper) {
        GTRecipe found = null;
        for (var recipe : helper.getLevel().getRecipeManager().getRecipes()) {
            if (recipe instanceof GTRecipe gt && GTNACORE.MOD_ID.equals(gt.id.getNamespace()) &&
                    gt.id.getPath().endsWith("godforge_chain/tpm_excited_dtsc"))
                found = gt;
        }
        helper.assertTrue(found != null, "the Excited DTSC mixer recipe exists");
        helper.assertTrue(TranscendentPlasmaMixerMachine.costPerRecipe(found)
                .equals(BigInteger.valueOf(2_138_383_760L).multiply(BigInteger.valueOf(2_000))),
                "cost was " + TranscendentPlasmaMixerMachine.costPerRecipe(found));
        helper.succeed();
    }

    /** Stage 6: every link of the Phonon / Tengam / superconductor / Shirabon chain is registered. */
    @GameTest(template = "empty_12", timeoutTicks = 40)
    public static void phononChainIsComplete(GameTestHelper helper) {
        var wanted = new java.util.HashSet<>(java.util.List.of("tengam_raw", "tengam_purified", "tengam_attuned",
                "dilithium", "magneto_resonatic_gem", "superconductor_uiv", "superconductor_umv",
                "phononic_seed_crystal", "phonon_crystal_solution", "phonon_medium", "shirabon", "infinity", "quantum",
                "dragonblood", "ichorium",
                "mutated_living_solder", "eternity"));
        for (var recipe : helper.getLevel().getRecipeManager().getRecipes()) {
            if (recipe instanceof GTRecipe gt && GTNACORE.MOD_ID.equals(gt.id.getNamespace()))
                wanted.removeIf(id -> gt.id.getPath().endsWith("godforge_chain/" + id));
        }
        helper.assertTrue(wanted.isEmpty(), "missing stage 6 recipes: " + wanted);
        helper.succeed();
    }

    /** The structure forms from its preview. */
    @GameTest(template = "empty_48", timeoutTicks = 400)
    public static void plasmaMixerForms(GameTestHelper helper) {
        var definition = GTNATranscendentPlasmaMixer.TRANSCENDENT_PLASMA_MIXER;
        var shape = definition.getMatchingShapes().get(0).getBlocks();
        TranscendentPlasmaMixerMachine machine = null;
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
                        machine = (TranscendentPlasmaMixerMachine) ((IMachineBlockEntity) helper.getBlockEntity(pos))
                                .getMetaMachine();
                    }
                }
            }
        }
        helper.assertTrue(machine != null, "the preview has a controller");
        helper.assertTrue(machine.checkPatternWithLock(),
                "the structure must form: " + machine.getMultiblockState().error);
        helper.succeed();
    }
}
