package com.raishxn.gtna.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/** GTOCore Greenhouse: daylight gates growth and dim skylight slows an active crop. */
public final class GreenhouseMachine extends WorkableElectricMultiblockMachine {

    private int skyLight = 15;

    public GreenhouseMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    protected RecipeLogic createRecipeLogic(Object... args) {
        return new GreenhouseRecipeLogic(this);
    }

    private int readSkyLight() {
        Level level = getLevel();
        if (level == null) return skyLight;
        int light = 15;
        // The tempered-glass casing itself reports full light obstruction. Sample directly
        // above the roof, then inspect that position and every block above it for a cover.
        BlockPos center = getPos().relative(getFrontFacing().getOpposite(), 2).above(4);
        int skyDarken = level.dimension() == Level.OVERWORLD ? level.getSkyDarken() : 0;
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos sample = center.offset(x, 0, z);
                // Inspect the space above the roof directly. A solid cover blocks daylight
                // immediately, even before the light engine updates brightness.
                boolean openSky = true;
                int top = level.getHeight(Heightmap.Types.WORLD_SURFACE, sample.getX(), sample.getZ());
                BlockPos.MutableBlockPos above = new BlockPos.MutableBlockPos();
                for (int y = sample.getY(); y < top; y++) {
                    above.set(sample.getX(), y, sample.getZ());
                    if (level.getBlockState(above).getLightBlock(level, above) >= 15) {
                        openSky = false;
                        break;
                    }
                }
                int cellLight = openSky ? Math.max(0, 15 - 2 * skyDarken) : 0;
                light = Math.min(light, cellLight);
            }
        }
        skyLight = light;
        return light;
    }

    public int getCurrentIllumination() {
        return readSkyLight();
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        return readSkyLight() > 0 && super.beforeWorking(recipe);
    }

    @Override
    public boolean onWorking() {
        if (getOffsetTimer() % 20 == 0) {
            int light = readSkyLight();
            if (getRecipeLogic() instanceof GreenhouseRecipeLogic logic) {
                if (light == 0) logic.regressToStart();
                else if (light < 13) logic.regressTenTicks();
            }
        }
        return super.onWorking();
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        textList.add(Component.translatable("gtna.machine.greenhouse.skylight", readSkyLight()));
    }

    private static final class GreenhouseRecipeLogic extends RecipeLogic {

        private GreenhouseRecipeLogic(GreenhouseMachine machine) {
            super(machine);
        }

        private void regressToStart() {
            progress = 0;
        }

        private void regressTenTicks() {
            progress = Math.max(0, progress - 10);
        }
    }
}
