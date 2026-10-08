package com.raishxn.gtna.gametest;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import com.raishxn.gtna.common.data.GTNAMachines2;
import com.raishxn.gtna.common.machine.multiblock.part.ae.GTNAMEExportBufferPartMachine;

import java.util.ArrayList;
import java.util.List;

@PrefixGameTestTemplate(false)
@GameTestHolder("gtna")
public final class GTNAMEExportBufferGameTests {

    private GTNAMEExportBufferGameTests() {}

    /** Recipe outputs never block and fluids pass the int ceiling the GTCEu ME output hatch has. */
    @GameTest(template = "empty_12", timeoutTicks = 20)
    public static void exportBufferAcceptsUnboundedOutputs(GameTestHelper helper) {
        if (GTNAMachines2.ME_EXPORT_BUFFER == null) {
            helper.fail("me_export_buffer must be enabled");
            return;
        }
        BlockPos pos = new BlockPos(2, 2, 2);
        helper.setBlock(pos, GTNAMachines2.ME_EXPORT_BUFFER.getBlock());
        if (!(helper.getBlockEntity(pos) instanceof MetaMachineBlockEntity entity) ||
                !(entity.getMetaMachine() instanceof GTNAMEExportBufferPartMachine buffer)) {
            helper.fail("the ME export buffer did not instantiate");
            return;
        }
        var water = GTMaterials.Water.getFluid();
        for (int i = 0; i < 3; i++) {
            List<FluidIngredient> fluids = new ArrayList<>(List.of(FluidIngredient.of(water, 2_000_000_000)));
            helper.assertTrue(buffer.tank.handleRecipeInner(IO.OUT, null, new ArrayList<>(fluids), true) == null,
                    "a simulated fluid output must always fit");
            buffer.tank.handleRecipeInner(IO.OUT, null, fluids, false);
        }
        long stored = buffer.getBufferedAmount(AEFluidKey.of(water));
        helper.assertTrue(stored == 6_000_000_000L, "expected 6,000,000,000 mB buffered, got " + stored);

        for (int i = 0; i < 2; i++) {
            List<Ingredient> items = new ArrayList<>(
                    List.of(SizedIngredient.create(Ingredient.of(Items.COBBLESTONE), Integer.MAX_VALUE)));
            helper.assertTrue(buffer.getInventory().handleRecipeInner(IO.OUT, null, items, false) == null,
                    "an item output must always fit");
        }
        long cobble = buffer.getBufferedAmount(AEItemKey.of(new ItemStack(Items.COBBLESTONE)));
        helper.assertTrue(cobble == 2L * Integer.MAX_VALUE, "expected 2 * int max cobblestone, got " + cobble);
        helper.succeed();
    }
}
