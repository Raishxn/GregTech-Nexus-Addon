package com.raishxn.gtna.common.data;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;

import net.minecraft.world.item.*;

import com.raishxn.gtna.GTNACORE;
import com.tterrag.registrate.util.entry.RegistryEntry;

import static com.raishxn.gtna.api.registry.GTNARegistry.REGISTRATE;

@SuppressWarnings("unused")
public class GTNACreativeModeTabs {

    public static RegistryEntry<CreativeModeTab> MATERIAL_ITEMS = REGISTRATE.defaultCreativeTab("gtna_material_items",
            builder -> builder.displayItems(new GTNATabDisplayItemsGenerator("material_items", REGISTRATE))
                    .title(REGISTRATE.addLang("itemGroup", GTNACORE.id("creative_tab.material_items"),
                            "GTNA Material Items"))
                    .icon(() -> ChemicalHelper.get(TagPrefix.ingot, GTNAMaterials.Echoite))
                    .build())
            .register();

    public static RegistryEntry<CreativeModeTab> MATERIAL_BLOCKS = REGISTRATE.defaultCreativeTab("gtna_material_blocks",
            builder -> builder.displayItems(new GTNATabDisplayItemsGenerator("material_blocks", REGISTRATE))
                    .title(REGISTRATE.addLang("itemGroup", GTNACORE.id("creative_tab.material_blocks"),
                            "GTNA Material Blocks"))
                    .icon(() -> ChemicalHelper.get(TagPrefix.block, GTNAMaterials.Echoite))
                    .build())
            .register();

    public static RegistryEntry<CreativeModeTab> MATERIAL_FLUIDS = REGISTRATE.defaultCreativeTab("gtna_material_fluids",
            builder -> builder.displayItems(new GTNATabDisplayItemsGenerator("fluids", REGISTRATE))
                    .title(REGISTRATE.addLang("itemGroup", GTNACORE.id("creative_tab.material_fluids"), "GTNA Fluids"))
                    .icon(GTItems.FLUID_CELL_LARGE_TUNGSTEN_STEEL::asStack)
                    .build())
            .register();

    public static RegistryEntry<CreativeModeTab> MATERIAL_PIPES = REGISTRATE.defaultCreativeTab("gtna_material_pipes",
            builder -> builder.displayItems(new GTNATabDisplayItemsGenerator("pipes", REGISTRATE))
                    .title(REGISTRATE.addLang("itemGroup", GTNACORE.id("creative_tab.material_pipes"),
                            "GTNA Pipes & Wires"))
                    .icon(() -> ChemicalHelper.get(TagPrefix.pipeNormalFluid, GTNAMaterials.Echoite))
                    .build())
            .register();

    public static RegistryEntry<CreativeModeTab> ITEMS = REGISTRATE.defaultCreativeTab("gtna_items",
            builder -> builder.displayItems(new GTNATabDisplayItemsGenerator("items", REGISTRATE))
                    .title(REGISTRATE.addLang("itemGroup", GTNACORE.id("creative_tab.items"), "GTNA Items"))
                    .icon(GTItems.BATTERY_ZPM_NAQUADRIA::asStack)
                    .build())
            .register();

    public static RegistryEntry<CreativeModeTab> BLOCKS = REGISTRATE.defaultCreativeTab("gtna_blocks",
            builder -> builder.displayItems(new GTNATabDisplayItemsGenerator("custom_blocks", REGISTRATE))
                    .title(REGISTRATE.addLang("itemGroup", GTNACORE.id("creative_tab.blocks"), "GTNA Blocks"))
                    .icon(GTNABlocks.HYPER_PRESSURE_BREEL_CASING::asStack)
                    .build())
            .register();

    public static RegistryEntry<CreativeModeTab> MACHINES = REGISTRATE.defaultCreativeTab("gtna_machines",
            builder -> builder.displayItems(new GTNATabDisplayItemsGenerator("machines", REGISTRATE))
                    .title(REGISTRATE.addLang("itemGroup", GTNACORE.id("creative_tab.machines"), "GTNA Machines"))
                    .icon(() -> GTNAMachines.WIRELESS_STEAM_INPUT_HATCH != null ?
                            GTNAMachines.WIRELESS_STEAM_INPUT_HATCH.asStack() :
                            GTNAItems.NEXUS_LINKER.asStack())
                    .build())
            .register();

    public static RegistryEntry<CreativeModeTab> WIRELESS = REGISTRATE.defaultCreativeTab("gtna_wireless",
            builder -> builder.displayItems(new GTNATabDisplayItemsGenerator("wireless", REGISTRATE))
                    .title(REGISTRATE.addLang("itemGroup", GTNACORE.id("creative_tab.wireless"), "GTNA Wireless"))
                    .icon(() -> {
                        // Use LV 1A Wireless Energy Hatch as icon
                        var def = com.raishxn.gtna.common.data.GTNAEnergyHatches.WIRELESS_ENERGY_HATCHES[com.gregtechceu.gtceu.api.GTValues.LV][0];
                        return def != null ? def.asStack() : ItemStack.EMPTY;
                    })
                    .build())
            .register();

    public static void init() {}
}
