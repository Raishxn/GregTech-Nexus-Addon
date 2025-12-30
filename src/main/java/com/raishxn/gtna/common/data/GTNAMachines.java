package com.raishxn.gtna.common.data;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.common.machine.multiblock.part.steam.WirelessSteamHatch;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import static com.raishxn.gtna.api.registry.GTNARegistry.REGISTRATE;

public class GTNAMachines {

    static {
        REGISTRATE.creativeModeTab(() -> GTNACreativeModeTabs.GTNA_CORE_ITEMS);
    }
    // --- Versão NORMAL (Bronze, 20k Cap) ---
    public static final MachineDefinition WIRELESS_STEAM_INPUT_NORMAL = REGISTRATE
            .machine("wireless_steam_input_normal", holder -> new WirelessSteamHatch(holder, 0, IO.IN))
            .tier(0) // Tier Bronze
            .rotationState(RotationState.ALL)
            // CORREÇÃO: Usa IMPORT_FLUIDS (plural) e STEAM
            .abilities(PartAbility.STEAM, PartAbility.IMPORT_FLUIDS)
            // Renderização: Usa o modelo base de vapor com seu overlay por cima
            .overlaySteamHullModel(new ResourceLocation(GTNACORE.MOD_ID, "block/overlay/machine/overlay_steam_wireless_in"))
            // Força a aparência de Bronze (false)
            .modelProperty(GTMachineModelProperties.IS_STEEL_MACHINE, false)
            .langValue("Normal Wireless Steam Input Hatch")
            .tooltips(
                    Component.literal("§7Conecta a uma Rede de Vapor Wireless"),
                    Component.literal("§7Capacidade: §e20,000 L/t"),
                    Component.literal("§eShift + Clique Direito com Chave de Fenda para vincular")
            )
            .register();

    public static final MachineDefinition WIRELESS_STEAM_OUTPUT_NORMAL = REGISTRATE
            .machine("wireless_steam_output_normal", holder -> new WirelessSteamHatch(holder, 0, IO.OUT))
            .tier(0)
            .rotationState(RotationState.ALL)
            // CORREÇÃO: Usa EXPORT_FLUIDS (plural) e STEAM
            .abilities(PartAbility.STEAM, PartAbility.EXPORT_FLUIDS)
            .overlaySteamHullModel(new ResourceLocation(GTNACORE.MOD_ID, "block/overlay/machine/overlay_steam_wireless_out"))
            .modelProperty(GTMachineModelProperties.IS_STEEL_MACHINE, false)
            .langValue("Normal Wireless Steam Dynamo Hatch")
            .tooltips(
                    Component.literal("§7Envia vapor para a Rede Wireless"),
                    Component.literal("§7Capacidade: §e20,000 L/t"),
                    Component.literal("§eShift + Clique Direito com Chave de Fenda para vincular")
            )
            .register();

    // --- Versão STEEL (Aço, Infinita Cap) ---
    public static final MachineDefinition WIRELESS_STEAM_INPUT_STEEL = REGISTRATE
            .machine("wireless_steam_input_steel", holder -> new WirelessSteamHatch(holder, 1, IO.IN))
            .tier(1) // Tier Steel
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.STEAM, PartAbility.IMPORT_FLUIDS)
            .overlaySteamHullModel(new ResourceLocation(GTNACORE.MOD_ID, "block/overlay/machine/overlay_steam_wireless_in"))
            // Força a aparência de Aço (true)
            .modelProperty(GTMachineModelProperties.IS_STEEL_MACHINE, true)
            .langValue("Steel Wireless Steam Input Hatch")
            .tooltips(
                    Component.literal("§7Conecta a uma Rede de Vapor Wireless"),
                    Component.literal("§7Capacidade: §bInfinita"),
                    Component.literal("§eShift + Clique Direito com Chave de Fenda para vincular")
            )
            .register();

    public static final MachineDefinition WIRELESS_STEAM_OUTPUT_STEEL = REGISTRATE
            .machine("wireless_steam_output_steel", holder -> new WirelessSteamHatch(holder, 1, IO.OUT))
            .tier(1)
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.STEAM, PartAbility.EXPORT_FLUIDS)
            .overlaySteamHullModel(new ResourceLocation(GTNACORE.MOD_ID, "block/overlay/machine/overlay_steam_wireless_out"))
            .modelProperty(GTMachineModelProperties.IS_STEEL_MACHINE, true)
            .langValue("Steel Wireless Steam Dynamo Hatch")
            .tooltips(
                    Component.literal("§7Envia vapor para a Rede Wireless"),
                    Component.literal("§7Capacidade: §bInfinita"),
                    Component.literal("§eShift + Clique Direito com Chave de Fenda para vincular")
            )
            .register();

    public static void init() {
    }
}