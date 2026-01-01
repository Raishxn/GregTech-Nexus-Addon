package com.raishxn.gtna.common.machine.multiblock.steam;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.config.ConfigHolder;
// Importação correta do FluidStack do Forge
import net.minecraftforge.fluids.FluidStack;
import com.raishxn.gtna.common.data.GTNABlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.gregtechceu.gtceu.api.machine.multiblock.PartAbility.EXPORT_FLUIDS;
import static com.gregtechceu.gtceu.api.machine.multiblock.PartAbility.IMPORT_FLUIDS;

public class MegaSolarBoilerMachine extends WorkableMultiblockMachine implements IDisplayUIMachine {

    private static final int MAX_LR_DIST = 16;
    private static final int MAX_B_DIST = 32;
    private static final int TICK_INTERVAL = 20;

    private int lDist, rDist, bDist, sunlit;
    private long lastSteamOutput;
    private boolean formed;

    public MegaSolarBoilerMachine(IMachineBlockEntity holder) {
        super(holder);
    }
    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            subscribeServerTick(this::updateSolarLogic);
        }
    }

    private void updateStructureDimensions() {
        Level world = getLevel();
        if (world == null) return;

        Direction front = getFrontFacing();
        Direction back = front.getOpposite();
        Direction left = front.getCounterClockWise();
        Direction right = left.getOpposite();

        this.bDist = calculateDistance(world, getPos(), back, MAX_B_DIST);
        this.lDist = calculateDistance(world, getPos().relative(back), left, MAX_LR_DIST);
        this.rDist = calculateDistance(world, getPos().relative(back), right, MAX_LR_DIST);

        this.formed = bDist >= 3 && lDist >= 1 && rDist >= 1;
    }

    private int calculateDistance(Level world, BlockPos start, Direction dir, int max) {
        int dist = 0;
        BlockPos.MutableBlockPos pos = start.mutable();
        for (int i = 1; i <= max; i++) {
            pos.move(dir);
            if (world.getBlockState(pos).is(GTNABlocks.SOLAR_BOILING_CELL.get())) dist = i;
            else break;
        }
        return dist;
    }

    @NotNull
    @Override
    public BlockPattern getPattern() {
        if (getLevel() != null) updateStructureDimensions();

        int safeL = formed ? lDist : 1;
        int safeR = formed ? rDist : 1;
        int safeB = formed ? bDist : 3;

        int totalWidth = safeL + safeR + 3;
        String boundary = "A".repeat(totalWidth);
        String middle = "A" + "B".repeat(totalWidth - 2) + "A";
        String controllerRow = "A".repeat(safeL + 1) + "~" + "A".repeat(safeR + 1);

        return FactoryBlockPattern.start(RelativeDirection.LEFT, RelativeDirection.UP, RelativeDirection.FRONT)
                .aisle(boundary)
                .aisle(middle).setRepeatable(safeB)
                .aisle(controllerRow)
                .where('~', Predicates.controller(Predicates.blocks(getDefinition().get())))
                .where('A', Predicates.blocks(GTNABlocks.HYPER_PRESSURE_BREEL_CASING.get())
                        .or(Predicates.abilities(IMPORT_FLUIDS).setPreviewCount(1))
                        .or(Predicates.abilities(EXPORT_FLUIDS).setPreviewCount(1)))
                .where('B', Predicates.blocks(GTNABlocks.SOLAR_BOILING_CELL.get()))
                .build();
    }
    @Override
    protected RecipeLogic createRecipeLogic(Object... args) {
        return new RecipeLogic(this);
    }
    private void updateSolarLogic() {
        if (formed && isWorkingEnabled() && !this.recipeLogic.isWorking()) {
            if (getOffsetTimer() % 20 == 0) {
                if (isDaytime()) {
                    sunlit = calculateSunlitArea();
                    if (sunlit > 0) {
                        GTRecipe recipe = createSolarRecipe();
                        this.recipeLogic.setupRecipe(recipe);
                    }
                } else {
                    sunlit = 0;
                    lastSteamOutput = 0;
                }
            }
        }
    }
    private boolean isDaytime() {
        return getLevel() != null && getLevel().isDay() && !getLevel().isRaining();
    }
    private int calculateSunlitArea() {
        int count = 0;
        Level level = getLevel();
        if (level == null) return 0;
        BlockPos pos = getPos();
        Direction back = getFrontFacing().getOpposite();
        Direction left = getFrontFacing().getCounterClockWise();
        Direction right = left.getOpposite();
        for (int b = 1; b <= bDist; b++) {
            BlockPos rowPos = pos.relative(back, b);
            if (level.canSeeSky(rowPos.above())) count++;
            for (int l = 1; l <= lDist; l++) if (level.canSeeSky(rowPos.relative(left, l).above())) count++;
            for (int r = 1; r <= rDist; r++) if (level.canSeeSky(rowPos.relative(right, r).above())) count++;
        }
        return count;
    }
    private GTRecipe createSolarRecipe() {
        long steamOutLong = (long) sunlit * 500;
        int steamOut = (int) steamOutLong;
        int waterIn = (int) Math.ceil((double) steamOut / ConfigHolder.INSTANCE.machines.largeBoilers.steamPerWater);
        lastSteamOutput = steamOutLong * 20;
        return GTRecipeBuilder.of(GTCEu.id("mega_solar_gen"), getRecipeType())
                .inputFluids(new FluidStack(Fluids.WATER, waterIn))
                .outputFluids(GTMaterials.Steam.getFluid(steamOut))
                .duration(TICK_INTERVAL)
                .buildRawRecipe();
    }

    @Override
    public void addDisplayText(@NotNull List<Component> textList) {
        IDisplayUIMachine.super.addDisplayText(textList);
        if (isFormed()) {
            textList.add(Component.translatable("gtna.machine.mega_solar.size", (lDist + rDist + 3), (bDist + 2)));
            textList.add(Component.translatable("gtna.machine.mega_solar.sunlit", sunlit));
            textList.add(Component.translatable("gtna.machine.mega_solar.production", lastSteamOutput));
        }
    }
}