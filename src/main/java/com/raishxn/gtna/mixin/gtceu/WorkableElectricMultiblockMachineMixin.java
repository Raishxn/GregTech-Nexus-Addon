package com.raishxn.gtna.mixin.gtceu;

import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.api.machine.multiblock.GTNAModuleDisplay;
import com.raishxn.gtna.common.machine.multiblock.electric.IsaMillMachine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Set;

/**
 * Shows "Formed modules: n / total" on every electric multiblock that has modules (sub-patterns),
 * including GTCEu machines such as the Electric Blast Furnace.
 */
@Mixin(WorkableElectricMultiblockMachine.class)
public abstract class WorkableElectricMultiblockMachineMixin {

    private static final Set<String> GTNA_GTO_ELECTRIC = Set.of(
            "fishing_ground", "evaporation_plant", "greenhouse", "component_assembler",
            "component_assembly_line", "large_greenhouse", "cold_ice_freezer", "chemical_plant",
            "mega_alloy_blast_smelter", "isa_mill", "industrial_flotation_cell", "vacuum_drying_furnace");

    @Inject(method = "addDisplayText", at = @At("TAIL"), remap = false)
    private void gtna$addModuleCount(List<Component> textList, CallbackInfo ci) {
        WorkableElectricMultiblockMachine machine = (WorkableElectricMultiblockMachine) (Object) this;
        GTNAModuleDisplay.append(textList, machine);
        var id = machine.getDefinition().getId();
        if (!GTNACORE.MOD_ID.equals(id.getNamespace()) || !GTNA_GTO_ELECTRIC.contains(id.getPath()) ||
                !machine.isFormed())
            return;

        int index = Math.min(2, textList.size());
        int parallel = "cold_ice_freezer".equals(id.getPath()) ? 64 :
                machine instanceof IsaMillMachine ? 2 :
                        machine.getParallelHatch().map(hatch -> hatch.getCurrentParallel()).orElse(1);
        if (parallel > 1) {
            textList.add(index++, Component.translatable("gtna.ui.parallel_max",
                    Component.literal(Integer.toString(parallel)).withStyle(ChatFormatting.LIGHT_PURPLE))
                    .withStyle(ChatFormatting.GRAY));
        }
        textList.add(index++, Component.translatable("gtna.ui.voiding_mode",
                Component.translatable(machine.getVoidingMode().getSerializedName()).withStyle(ChatFormatting.GRAY))
                .withStyle(ChatFormatting.WHITE));
        if (machine instanceof IsaMillMachine mill && mill.isGrindBallMissing()) {
            textList.add(index++, Component.translatable("gtna.jade.need_grind_ball")
                    .withStyle(ChatFormatting.RED));
        }
        if (machine.getRecipeLogic().isIdle() && machine.getRecipeLogic().getLastRecipe() == null) {
            textList.add(index, Component.translatable("gtna.ui.no_recipe_found").withStyle(ChatFormatting.GRAY));
        }
    }
}
