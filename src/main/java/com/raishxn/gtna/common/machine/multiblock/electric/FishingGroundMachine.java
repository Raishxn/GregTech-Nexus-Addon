package com.raishxn.gtna.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.capability.IParallelHatch;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.IItemHandler;

import com.raishxn.gtna.common.data.GTNARecipeType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** GTOCore Fishing Ground with its four fixed bait recipes and vanilla fishing loot circuit modes. */
public class FishingGroundMachine extends WorkableElectricMultiblockMachine {

    private static final SoundEvent[] FISHING_SOUNDS = {
            SoundEvents.FISHING_BOBBER_RETRIEVE, SoundEvents.FISHING_BOBBER_SPLASH,
            SoundEvents.FISHING_BOBBER_THROW, SoundEvents.FISH_SWIM
    };
    private FishingHook fishingHook;
    private int soundCooldown;

    public FishingGroundMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    public static ModifierFunction parallelModifier(MetaMachine machine, GTRecipe recipe) {
        return isLootRecipe(recipe) ?
                ModifierFunction.IDENTITY : GTRecipeModifiers.hatchParallel(machine, recipe);
    }

    private static boolean isLootRecipe(GTRecipe recipe) {
        return recipe.id != null && recipe.id.getPath().contains("/fishing_loot_");
    }

    @Override
    public boolean onWorking() {
        if (soundCooldown > 0) soundCooldown--;
        else if (fishingHook != null && getLevel() instanceof ServerLevel level) {
            level.playSound(null, getPos(), FISHING_SOUNDS[level.random.nextInt(FISHING_SOUNDS.length)],
                    SoundSource.BLOCKS);
            soundCooldown = 10 + level.random.nextInt(100);
        }
        return super.onWorking();
    }

    @Override
    protected @NotNull RecipeLogic createRecipeLogic(Object... args) {
        return new RecipeLogic(this) {

            @Override
            public @NotNull Iterator<GTRecipe> searchRecipe() {
                GTRecipe loot = createLootRecipe();
                return loot == null ? super.searchRecipe() : List.of(loot).iterator();
            }
        };
    }

    @Override
    public GTRecipe fullModifyRecipe(GTRecipe recipe) {
        if (isLootRecipe(recipe)) {
            GTRecipe fresh = createLootRecipe();
            if (fresh == null) return null;
            recipe = fresh;
        }
        return super.fullModifyRecipe(recipe);
    }

    private GTRecipe createLootRecipe() {
        if (!isFormed() || !(getLevel() instanceof ServerLevel level)) return null;
        int circuit = getCircuitMode();
        if (circuit < 1 || circuit > 4) return null;

        LootTable table = level.getServer().getLootData().getLootTable(switch (circuit) {
            case 2 -> BuiltInLootTables.FISHING_FISH;
            case 3 -> BuiltInLootTables.FISHING_JUNK;
            case 4 -> BuiltInLootTables.FISHING_TREASURE;
            default -> BuiltInLootTables.FISHING;
        });
        if (fishingHook == null) fishingHook = new OpenWaterFishingHook(level);
        LootParams context = new LootParams.Builder(level)
                .withOptionalParameter(LootContextParams.THIS_ENTITY, fishingHook)
                .withParameter(LootContextParams.TOOL, new ItemStack(Items.FISHING_ROD))
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(getPos()))
                .create(LootContextParamSets.FISHING);

        int parallel = Math.min(1024, Math.max(1,
                getParallelHatch().map(IParallelHatch::getCurrentParallel).orElse(1)));
        List<ItemStack> catches = new ArrayList<>();
        int tagged = 0;
        for (int draw = 0; draw < parallel; draw++) {
            for (ItemStack stack : table.getRandomItems(context)) {
                if (stack.hasTag() && tagged++ >= 100) continue;
                boolean merged = false;
                for (ItemStack existing : catches) {
                    if (ItemStack.isSameItemSameTags(existing, stack)) {
                        existing.grow(stack.getCount());
                        merged = true;
                        break;
                    }
                }
                if (!merged) catches.add(stack.copy());
            }
        }
        var builder = GTNARecipeType.FISHING_GROUND_RECIPES.recipeBuilder("fishing_loot_" + circuit)
                .circuitMeta(circuit)
                .EUt(480L * parallel)
                .duration(20);
        for (ItemStack catchStack : catches) builder.outputItems(catchStack);
        return builder.buildRawRecipe();
    }

    public int getCircuitMode() {
        var inputs = getCapabilitiesFlat().get(IO.IN);
        if (inputs == null) return 0;
        var handlers = inputs.get(ItemRecipeCapability.CAP);
        if (handlers == null) return 0;
        for (Object candidate : handlers) {
            if (!(candidate instanceof IItemHandler handler)) continue;
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack stack = handler.getStackInSlot(slot);
                if (IntCircuitBehaviour.isIntegratedCircuit(stack)) {
                    return IntCircuitBehaviour.getCircuitConfiguration(stack);
                }
            }
        }
        return 0;
    }

    private static class OpenWaterFishingHook extends FishingHook {

        OpenWaterFishingHook(ServerLevel level) {
            super(EntityType.FISHING_BOBBER, level);
        }

        @Override
        public boolean isOpenWaterFishing() {
            return true;
        }
    }
}
