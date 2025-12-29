package com.raishxn.gtna.common.data;

import com.gregtechceu.gtceu.common.data.GTCreativeModeTabs;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.raishxn.gtna.GTNACORE;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.world.item.CreativeModeTab;


import static com.raishxn.gtna.api.registry.GTNARegistry.REGISTRATE;

public class GTNACreativeModeTabs {

    public static RegistryEntry<CreativeModeTab> GTNA_CORE_ITEMS = REGISTRATE.defaultCreativeTab(GTNACORE.MOD_ID,
                    builder -> builder.displayItems(new GTCreativeModeTabs.RegistrateDisplayItemsGenerator(GTNACORE.MOD_ID, REGISTRATE))
                            .title(REGISTRATE.addLang("itemGroup", GTNACORE.id("creative_tab"), "GTNA Core Items"))
                            .icon(GTItems.BATTERY_ZPM_NAQUADRIA::asStack)
                            .build())
            .register();

    public static void init() {}
}
