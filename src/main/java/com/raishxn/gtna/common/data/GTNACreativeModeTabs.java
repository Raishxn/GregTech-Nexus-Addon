package com.raishxn.gtna.common.data;

import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.raishxn.gtna.GTNACORE;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.core.registries.Registries; // Importante para o getAll
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import static com.raishxn.gtna.api.registry.GTNARegistry.REGISTRATE;

public class GTNACreativeModeTabs {

    // Aba Principal (Core): Contém tudo que NÃO é máquina
    public static RegistryEntry<CreativeModeTab> GTNA_CORE_ITEMS = REGISTRATE.defaultCreativeTab("gtna_core_items",
                    builder -> builder.displayItems((itemDisplayParameters, output) -> {
                                // Itera sobre todos os itens registrados pelo seu Mod
                                REGISTRATE.getAll(Registries.ITEM).forEach(entry -> {
                                    Item item = entry.get();
                                    // Se NÃO for máquina, adiciona aqui
                                    if (!(item instanceof MetaMachineItem)) {
                                        output.accept(new ItemStack(item));
                                    }
                                });
                            })
                            .title(REGISTRATE.addLang("itemGroup", GTNACORE.id("creative_tab.core"), "GTNA Core Items"))
                            .icon(GTItems.BATTERY_ZPM_NAQUADRIA::asStack)
                            .build())
            .register();

    // Aba de Máquinas: Contém apenas MetaMachineItems
    public static RegistryEntry<CreativeModeTab> GTNA_MACHINES = REGISTRATE.defaultCreativeTab("gtna_machines",
                    builder -> builder.displayItems((itemDisplayParameters, output) -> {
                                // Itera sobre todos os itens registrados pelo seu Mod
                                REGISTRATE.getAll(Registries.ITEM).forEach(entry -> {
                                    Item item = entry.get();
                                    // Se FOR máquina, adiciona aqui
                                    if (item instanceof MetaMachineItem) {
                                        output.accept(new ItemStack(item));
                                    }
                                });
                            })
                            .title(REGISTRATE.addLang("itemGroup", GTNACORE.id("creative_tab.machines"), "GTNA Machines"))
                            .icon(GTNAMachines.WIRELESS_STEAM_INPUT_HATCH::asStack)
                            .build())
            .register();

    public static void init() {
    }
}