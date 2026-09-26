package com.raishxn.gtna.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.utils.GTMath;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

import com.raishxn.gtna.common.data.GTNARecipeDataKeys;
import com.raishxn.gtna.common.machine.multiblock.part.BallHatchPartMachine;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * GTOCore ISA Mill: grinds ores/raw ores into {@code MILLED} products behind a grinding-ball gate.
 * The recipe data key {@code grindball} (1 = Soapstone, 2 = Aluminium) must match the ball stored
 * in the {@link BallHatchPartMachine}; every started operation consumes
 * {@code parallels / (Unbreaking level + 1) + 1} durability and the ball is destroyed when the
 * damage reaches its maximum.
 *
 * <p>
 * Deviation from GTOCore: GTO applies the durability damage inside {@code getRealRecipe}. GTCEu's
 * recipe search calls {@code fullModifyRecipe} once per candidate recipe, so damaging there would
 * wear the ball for recipes that never start. The tier gate stays in {@code getRealRecipe} (no
 * matching ball = no recipe) and the identical damage formula is applied in {@code beforeWorking},
 * which runs exactly once per started recipe.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class IsaMillMachine extends WorkableElectricMultiblockMachine {

    private BallHatchPartMachine ballHatch;

    public boolean isGrindBallMissing() {
        return isFormed() && (ballHatch == null || ballHatch.getBallStack().isEmpty());
    }

    public IsaMillMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        ballHatch = null;
        for (IMultiPart part : getParts()) {
            if (part instanceof BallHatchPartMachine hatch) {
                ballHatch = hatch;
                break;
            }
        }
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        ballHatch = null;
    }

    @Override
    @Nullable
    protected GTRecipe getRealRecipe(GTRecipe recipe) {
        GTRecipe modified = super.getRealRecipe(recipe);
        if (modified == null) return null;
        if (ballHatch == null || grindBallTier(ballHatch.getBallStack()) !=
                modified.data.getInt(GTNARecipeDataKeys.GRINDBALL)) {
            return null;
        }
        return modified;
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        if (recipe == null || !consumeGrindBall(recipe)) {
            return false;
        }
        return super.beforeWorking(recipe);
    }

    /** Whether the ball currently in the hatch matches the recipe's required grinding tier. */
    private static int grindBallTier(ItemStack ball) {
        if (ball.isEmpty()) return 0;
        return BallHatchPartMachine.GRINDBALL.getOrDefault(ball.getItem(), 0);
    }

    /** GTOCore damage formula: {@code durability + parallels / (Unbreaking + 1) + 1}. */
    private boolean consumeGrindBall(GTRecipe recipe) {
        if (ballHatch == null) return false;
        ItemStack ball = ballHatch.getBallStack();
        if (grindBallTier(ball) != recipe.data.getInt(GTNARecipeDataKeys.GRINDBALL)) {
            return false;
        }
        int level = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.UNBREAKING, ball) + 1;
        int damage = ball.getDamageValue() + GTMath.saturatedCast(recipe.parallels / level) + 1;
        if (damage < ball.getMaxDamage()) {
            ball.setDamageValue(damage);
        } else {
            ballHatch.setBallStack(ItemStack.EMPTY);
        }
        return true;
    }
}
