package com.raishxn.gtna.gametest;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.common.data.GTBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.raishxn.gtna.common.data.GTNABlocks;
import com.raishxn.gtna.common.data.GTNAEnergyHatches;
import com.raishxn.gtna.common.data.NexusEnergyNetwork;
import com.raishxn.gtna.common.machine.multiblock.energy.NexusFluxMatrixMachine;
import com.raishxn.gtna.utils.datastructure.Int128;

import java.math.BigInteger;
import java.util.UUID;

@GameTestHolder("gtna")
@PrefixGameTestTemplate(false)
public final class NexusMatrixUnlimitedGameTests {

    @GameTest(template = "empty_48", timeoutTicks = 100)
    public static void nexus750MaxCapacitorsRequireCompleteRealMatrix(GameTestHelper helper) throws Exception {
        var controller = new BlockPos(10, 3, 10);
        for (int layer = 0; layer < 32; layer++) {
            for (int row = 0; row < 7; row++) for (int col = 0; col < 7; col++) {
                var pos = controller.offset(col - 3, layer, 6 - row);
                if (layer == 0 && row == 6 && col == 3)
                    helper.setBlock(pos, GTNAEnergyHatches.NEXUS_FLUX_MATRIX.getBlock());
                else if (layer == 0 || layer == 31 || ((row == 0 || row == 6) && (col == 0 || col == 6)))
                    helper.setBlock(pos, GTBlocks.CASING_STEEL_SOLID.get());
                else if (row == 0 || row == 6 || col == 0 || col == 6)
                    helper.setBlock(pos, GTNABlocks.BOROSILICATE_GLASS_BLOCK.get());
                else helper.setBlock(pos, GTNABlocks.NEXUS_CAPACITOR_MAX.get());
            }
        }
        var machine = (NexusFluxMatrixMachine) ((MetaMachineBlockEntity) helper.getBlockEntity(controller))
                .getMetaMachine();
        machine.setFrontFacing(Direction.NORTH);
        var owner = UUID.randomUUID();
        var ownerField = NexusFluxMatrixMachine.class.getDeclaredField("ownerUUID");
        ownerField.setAccessible(true);
        ownerField.set(machine, owner);
        helper.assertTrue(machine.checkPatternWithLock(), "750-capacitor real maximum pattern must form");
        machine.onStructureFormed();
        var network = NexusEnergyNetwork.get(helper.getLevel());
        helper.assertTrue(
                machine.isUnlimited() && network.isUnlimited(owner) && network.getTotalCapacitors(owner) == 750,
                "all 750 internal MAX capacitors activate unlimited storage/transfer");
        var changed = controller.offset(-2, 1, 5);
        helper.setBlock(changed, GTNABlocks.NEXUS_CAPACITOR_OPV.get());
        helper.assertTrue(machine.checkPatternWithLock(), "mixed-tier matrix remains valid");
        machine.onStructureFormed();
        helper.assertTrue(!machine.isUnlimited() && !network.isUnlimited(owner),
                "one lower-tier capacitor disables unlimited mode");
        helper.setBlock(changed, net.minecraft.world.level.block.Blocks.AIR);
        helper.setBlock(controller.offset(10, 1, 5), GTNABlocks.NEXUS_CAPACITOR_MAX.get());
        helper.assertTrue(machine.checkPatternWithLock(), "749-capacitor matrix remains valid");
        machine.onStructureFormed();
        helper.assertTrue(!machine.isUnlimited() && network.getTotalCapacitors(owner) == 749,
                "nearby external capacitor cannot substitute for missing internal capacitor");
        helper.setBlock(changed, GTNABlocks.NEXUS_CAPACITOR_MAX.get());
        helper.assertTrue(machine.checkPatternWithLock(), "repaired matrix reforms");
        machine.onStructureFormed();
        helper.assertTrue(network.isUnlimited(owner), "repair restores unlimited mode");
        machine.onStructureInvalid();
        helper.assertTrue(!network.isUnlimited(owner), "invalid structure disables unlimited mode");
        helper.succeed();
    }

    @GameTest(template = "empty_16", timeoutTicks = 40)
    public static void nexusUnlimitedStorageKeepsExactEnergyBeyond128BitsAndReload(GameTestHelper helper) {
        var owner = UUID.randomUUID();
        var network = new NexusEnergyNetwork();
        network.setMatrixStats(owner, 750, GTValues.MAX, 1, new Int128(1), true);
        network.setMatrixDimension(owner, helper.getLevel().dimension());
        network.setMaxCapacity(owner, new Int128(1000));
        network.setUnlimited(owner, true);
        var chunk = Int128.MAX_VALUE.copy();
        var before = network.save(new net.minecraft.nbt.CompoundTag());
        helper.assertTrue(network.quoteInsertion(owner, chunk, helper.getLevel()).equals(chunk) &&
                before.equals(network.save(new net.minecraft.nbt.CompoundTag())),
                "unlimited insertion quote is nonmutating");
        helper.assertTrue(network.addEnergy(owner, chunk, helper.getLevel()).equals(chunk) &&
                network.addEnergy(owner, chunk, helper.getLevel()).equals(chunk),
                "unlimited network accepts repeated maximum transactions");
        var expected = chunk.toBigInteger().multiply(BigInteger.TWO);
        helper.assertTrue(network.getExactEnergy(owner).equals(expected),
                "real balance exceeds Int128 without wrap or truncation");
        helper.assertTrue(network.getEnergy(owner).equals(Int128.MAX_VALUE),
                "compatibility view saturates without changing balance");
        var restored = new NexusEnergyNetwork(network.save(new net.minecraft.nbt.CompoundTag()));
        helper.assertTrue(restored.isUnlimited(owner) && restored.getExactEnergy(owner).equals(expected),
                "unlimited flag and BigInteger balance survive reload");
        helper.assertTrue(restored.consumeEnergy(owner, chunk, helper.getLevel()) &&
                restored.getExactEnergy(owner).equals(chunk.toBigInteger()),
                "withdrawal charges stored energy exactly");
        restored.setMatrixStats(owner, 749, GTValues.MAX, 1, new Int128(1), true);
        helper.assertTrue(!restored.isUnlimited(owner) && restored.getExactEnergy(owner).equals(chunk.toBigInteger()),
                "downgrade preserves deposited balance and removes unlimited mode");
        restored.setMaxCapacity(owner, new Int128(1000));
        helper.assertTrue(restored.addEnergy(owner, new Int128(1), helper.getLevel()).isZero(),
                "finite capacity applies after downgrade");
        helper.assertTrue(network.getCurrentLossPerTick(owner).isNegative() == false,
                "traffic counters cannot wrap negative");
        helper.assertTrue(
                com.raishxn.gtna.common.item.NexusNetworkDisplay.capacity(true, new Int128(1)).getContents().toString()
                        .contains("∞") &&
                        com.raishxn.gtna.common.item.NexusNetworkDisplay.transfer(true, new Int128(1)).getContents()
                                .toString()
                                .contains("∞"),
                "both shared UI lines explicitly show unlimited capacity/transfer");
        helper.succeed();
    }
}
