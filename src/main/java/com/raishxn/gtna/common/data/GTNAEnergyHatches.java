package com.raishxn.gtna.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.network.chat.Component;

import com.raishxn.gtna.common.machine.multiblock.energy.NexusFluxMatrixMachine;
import com.raishxn.gtna.common.machine.multiblock.part.energy.WirelessDynamoHatchPartMachine;
import com.raishxn.gtna.common.machine.multiblock.part.energy.WirelessEnergyHatchPartMachine;

import java.util.Locale;

import static com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.IS_FORMED;
import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;
import static com.gregtechceu.gtceu.api.pattern.Predicates.controller;
import static com.raishxn.gtna.api.registry.GTNARegistry.REGISTRATE;

public class GTNAEnergyHatches {

    public static final MachineDefinition[][] WIRELESS_ENERGY_HATCHES = new MachineDefinition[GTValues.MAX + 1][11];
    public static final MachineDefinition[][] WIRELESS_DYNAMO_HATCHES = new MachineDefinition[GTValues.MAX + 1][11];

    public static final MultiblockMachineDefinition NEXUS_FLUX_MATRIX = REGISTRATE
            .multiblock("nexus_flux_matrix", NexusFluxMatrixMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .appearanceBlock(() -> com.gregtechceu.gtceu.common.data.GTBlocks.CASING_STEEL_SOLID.get())
            .pattern(definition -> FactoryBlockPattern
                    .start(com.gregtechceu.gtceu.api.pattern.util.RelativeDirection.RIGHT,
                            com.gregtechceu.gtceu.api.pattern.util.RelativeDirection.FRONT,
                            com.gregtechceu.gtceu.api.pattern.util.RelativeDirection.UP)
                    .aisle("AAAAAAA", "AAAAAAA", "AAAAAAA", "AAAAAAA", "AAAAAAA", "AAAAAAA", "AAABAAA")
                    .aisle("ACCCCCA", "CDDDDDC", "CDDDDDC", "CDDDDDC", "CDDDDDC", "CDDDDDC", "ACCCCCA")
                    .setRepeatable(2, 30)
                    .aisle("AAAAAAA", "AAAAAAA", "AAAAAAA", "AAAAAAA", "AAAAAAA", "AAAAAAA", "AAAAAAA")
                    .where('A', blocks(com.gregtechceu.gtceu.common.data.GTBlocks.CASING_STEEL_SOLID.get())
                            .or(Predicates
                                    .abilities(com.gregtechceu.gtceu.api.machine.multiblock.PartAbility.OUTPUT_ENERGY)
                                    .setPreviewCount(1))
                            .or(Predicates
                                    .abilities(com.gregtechceu.gtceu.api.machine.multiblock.PartAbility.INPUT_ENERGY)
                                    .setPreviewCount(1)))
                    .where('B', controller(blocks(definition.get())))
                    .where('C', blocks(GTNABlocks.BOROSILICATE_GLASS_BLOCK.get()))
                    .where('D', Predicates.air().or(blocks(
                            GTNABlocks.NEXUS_CAPACITOR_LV.get(),
                            GTNABlocks.NEXUS_CAPACITOR_MV.get(),
                            GTNABlocks.NEXUS_CAPACITOR_HV.get(),
                            GTNABlocks.NEXUS_CAPACITOR_EV.get(),
                            GTNABlocks.NEXUS_CAPACITOR_IV.get(),
                            GTNABlocks.NEXUS_CAPACITOR_LUV.get(),
                            GTNABlocks.NEXUS_CAPACITOR_ZPM.get(),
                            GTNABlocks.NEXUS_CAPACITOR_UV.get(),
                            GTNABlocks.NEXUS_CAPACITOR_UHV.get(),
                            GTNABlocks.NEXUS_CAPACITOR_UEV.get(),
                            GTNABlocks.NEXUS_CAPACITOR_UIV.get(),
                            GTNABlocks.NEXUS_CAPACITOR_UXV.get(),
                            GTNABlocks.NEXUS_CAPACITOR_OPV.get(),
                            GTNABlocks.NEXUS_CAPACITOR_MAX.get())))
                    .build())
            .workableCasingModel(
                    GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                    GTCEu.id("block/multiblock/implosion_compressor"))
            .tooltips(
                    Component.translatable("gtna.machine.nexus_flux_matrix.tooltip_1"),
                    Component.translatable("gtna.machine.nexus_flux_matrix.tooltip_2"))
            .register();

    public static void init() {
        for (int tier = GTValues.LV; tier <= GTValues.MAX; tier++) {
            for (int ampExp = 0; ampExp <= 10; ampExp++) {
                int amps = (int) Math.pow(4, ampExp);
                registerWirelessHatches(tier, amps, ampExp);
            }
        }
    }

    private static void registerWirelessHatches(int tier, int amps, int ampExp) {
        String tierName = GTValues.VN[tier].toLowerCase(Locale.ROOT);

        // E.g. wireless_energy_hatch_1A_lv
        String outName = "wireless_energy_hatch_" + amps + "a_" + tierName;
        String inName = "wireless_dynamo_hatch_" + amps + "a_" + tierName;

        String upperTierName = GTValues.VN[tier];
        long capacity = GTValues.V[tier] * 64L * amps;
        String capacityStr = formatCapacity(capacity);

        WIRELESS_ENERGY_HATCHES[tier][ampExp] = REGISTRATE
                .machine(outName, holder -> new WirelessEnergyHatchPartMachine(holder, tier, amps))
                .langValue(GTValues.VNF[tier] + " " + amps + "A Wireless Energy Hatch")
                .tier(tier)
                .rotationState(RotationState.ALL)
                .abilities(com.gregtechceu.gtceu.api.machine.multiblock.PartAbility.INPUT_ENERGY)
                .modelProperty(IS_FORMED, false)
                .model((ctx, prov, builder) -> {
                    var model = prov.models()
                            .withExistingParent(ctx.getName(), GTCEu.id("block/machine/template/part/hatch_machine"))
                            .texture("overlay",
                                    new net.minecraft.resources.ResourceLocation("gtna",
                                            "block/overlay/machine/overlay_steam_wireless_out"))
                            .texture("side", GTCEu.id("block/casings/voltage/" + tierName + "/side"))
                            .texture("top", GTCEu.id("block/casings/voltage/" + tierName + "/top"))
                            .texture("bottom", GTCEu.id("block/casings/voltage/" + tierName + "/bottom"))
                            .texture("particle", GTCEu.id("block/casings/voltage/" + tierName + "/side"));
                    builder.partialState().setModel(model);
                })
                .tooltips(
                        Component.translatable("gtna.machine.wireless_energy_hatch.tooltip"),
                        Component.translatable("gtna.machine.wireless_energy_hatch.tier_info", upperTierName,
                                String.valueOf(amps)),
                        Component.translatable("gtna.machine.wireless_hatch.capacity", capacityStr),
                        Component.translatable("gtna.machine.wireless_hatch.auto_bind"))
                .register();

        WIRELESS_DYNAMO_HATCHES[tier][ampExp] = REGISTRATE
                .machine(inName, holder -> new WirelessDynamoHatchPartMachine(holder, tier, amps))
                .langValue(GTValues.VNF[tier] + " " + amps + "A Wireless Dynamo Hatch")
                .tier(tier)
                .rotationState(RotationState.ALL)
                .abilities(com.gregtechceu.gtceu.api.machine.multiblock.PartAbility.OUTPUT_ENERGY)
                .modelProperty(IS_FORMED, false)
                .model((ctx, prov, builder) -> {
                    var model = prov.models()
                            .withExistingParent(ctx.getName(), GTCEu.id("block/machine/template/part/hatch_machine"))
                            .texture("overlay",
                                    new net.minecraft.resources.ResourceLocation("gtna",
                                            "block/overlay/machine/overlay_steam_wireless_in"))
                            .texture("side", GTCEu.id("block/casings/voltage/" + tierName + "/side"))
                            .texture("top", GTCEu.id("block/casings/voltage/" + tierName + "/top"))
                            .texture("bottom", GTCEu.id("block/casings/voltage/" + tierName + "/bottom"))
                            .texture("particle", GTCEu.id("block/casings/voltage/" + tierName + "/side"));
                    builder.partialState().setModel(model);
                })
                .tooltips(
                        Component.translatable("gtna.machine.wireless_dynamo_hatch.tooltip"),
                        Component.translatable("gtna.machine.wireless_dynamo_hatch.tier_info", upperTierName,
                                String.valueOf(amps)),
                        Component.translatable("gtna.machine.wireless_hatch.capacity", capacityStr),
                        Component.translatable("gtna.machine.wireless_hatch.auto_bind"))
                .register();
    }

    private static String formatCapacity(long capacity) {
        if (capacity >= 1_000_000_000L) {
            return String.format("%.1fG", capacity / 1_000_000_000.0);
        } else if (capacity >= 1_000_000L) {
            return String.format("%.1fM", capacity / 1_000_000.0);
        } else if (capacity >= 1_000L) {
            return String.format("%.1fK", capacity / 1_000.0);
        }
        return String.valueOf(capacity);
    }
}
