package com.raishxn.gtna.common.data;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.*;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.raishxn.gtna.GTNACORE;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static com.raishxn.gtna.api.registry.GTNARegistry.REGISTRATE;

@SuppressWarnings("unused")
public class GTNACreativeModeTabs {

    // 1. Aba de Items Materiais (Lingotes, Pó, etc.)
    public static RegistryEntry<CreativeModeTab> MATERIAL_ITEMS = REGISTRATE.defaultCreativeTab("gtna_material_items",
                    builder -> builder.displayItems(new RegistrateDisplayItemsGenerator("material_items", REGISTRATE))
                            .title(REGISTRATE.addLang("itemGroup", GTNACORE.id("creative_tab.material_items"), "GTNA Material Items"))
                            .icon(() -> ChemicalHelper.get(TagPrefix.ingot, GTNAMaterials.Echoite))
                            .build())
            .register();

    // 2. Aba de Blocos de Materiais (Minérios, Blocos de Metal, etc.)
    public static RegistryEntry<CreativeModeTab> MATERIAL_BLOCKS = REGISTRATE.defaultCreativeTab("gtna_material_blocks",
                    builder -> builder.displayItems(new RegistrateDisplayItemsGenerator("material_blocks", REGISTRATE))
                            .title(REGISTRATE.addLang("itemGroup", GTNACORE.id("creative_tab.material_blocks"), "GTNA Material Blocks"))
                            .icon(() -> ChemicalHelper.get(TagPrefix.block, GTNAMaterials.Echoite))
                            .build())
            .register();

    // 3. Aba de Fluidos
    public static RegistryEntry<CreativeModeTab> MATERIAL_FLUIDS = REGISTRATE.defaultCreativeTab("gtna_material_fluids",
                    builder -> builder.displayItems(new RegistrateDisplayItemsGenerator("fluids", REGISTRATE))
                            .title(REGISTRATE.addLang("itemGroup", GTNACORE.id("creative_tab.material_fluids"), "GTNA Fluids"))
                            .icon(GTItems.FLUID_CELL_LARGE_TUNGSTEN_STEEL::asStack)
                            .build())
            .register();

    // 4. Aba de Tubos e Cabos
    public static RegistryEntry<CreativeModeTab> MATERIAL_PIPES = REGISTRATE.defaultCreativeTab("gtna_material_pipes",
                    builder -> builder.displayItems(new RegistrateDisplayItemsGenerator("pipes", REGISTRATE))
                            .title(REGISTRATE.addLang("itemGroup", GTNACORE.id("creative_tab.material_pipes"), "GTNA Pipes & Wires"))
                            .icon(() -> ChemicalHelper.get(TagPrefix.pipeNormalFluid, GTNAMaterials.Echoite))
                            .build())
            .register();

    // 5. Aba de Itens Diversos
    public static RegistryEntry<CreativeModeTab> ITEMS = REGISTRATE.defaultCreativeTab("gtna_items",
                    builder -> builder.displayItems(new RegistrateDisplayItemsGenerator("items", REGISTRATE))
                            .title(REGISTRATE.addLang("itemGroup", GTNACORE.id("creative_tab.items"), "GTNA Items"))
                            .icon(GTItems.BATTERY_ZPM_NAQUADRIA::asStack)
                            .build())
            .register();

    // 6. Aba de Blocos Gerais (Seus blocos customizados do GTNABlocks)
    public static RegistryEntry<CreativeModeTab> BLOCKS = REGISTRATE.defaultCreativeTab("gtna_blocks",
                    builder -> builder.displayItems(new RegistrateDisplayItemsGenerator("custom_blocks", REGISTRATE))
                            .title(REGISTRATE.addLang("itemGroup", GTNACORE.id("creative_tab.blocks"), "GTNA Blocks"))
                            .icon(GTNABlocks.HYPER_PRESSURE_BREEL_CASING::asStack)
                            .build())
            .register();

    // 7. Aba de Máquinas
    public static RegistryEntry<CreativeModeTab> MACHINES = REGISTRATE.defaultCreativeTab("gtna_machines",
                    builder -> builder.displayItems(new RegistrateDisplayItemsGenerator("machines", REGISTRATE))
                            .title(REGISTRATE.addLang("itemGroup", GTNACORE.id("creative_tab.machines"), "GTNA Machines"))
                            .icon(GTNAMachines.WIRELESS_STEAM_INPUT_HATCH::asStack)
                            .build())
            .register();

    public static void init() {
    }

    public static class RegistrateDisplayItemsGenerator implements CreativeModeTab.DisplayItemsGenerator {
        public final String tabType;
        public final GTRegistrate registrate;

        // Lista estática dos blocos exclusivos do GTNABlocks
        private static final Set<Item> CUSTOM_BLOCKS = Set.of(
                GTNABlocks.BREEL_PIPE_CASING.get().asItem(),
                GTNABlocks.HYPER_PRESSURE_BREEL_CASING.get().asItem(),
                GTNABlocks.STEAM_COMPACT_PIPE_CASING.get().asItem(),
                GTNABlocks.VIBRATION_SAFE_CASING.get().asItem(),
                GTNABlocks.BRONZE_REINFORCED_WOOD.get().asItem(),
                GTNABlocks.SOLAR_BOILING_CELL.get().asItem()
        );

        public RegistrateDisplayItemsGenerator(String tabType, GTRegistrate registrate) {
            this.tabType = tabType;
            this.registrate = registrate;
        }

        @Override
        public void accept(@NotNull CreativeModeTab.ItemDisplayParameters itemDisplayParameters,
                           @NotNull CreativeModeTab.Output output) {

            registrate.getAll(Registries.ITEM).forEach(entry -> {
                Item item = entry.get();

                if (shouldInclude(item)) {
                    if (item instanceof IComponentItem componentItem) {
                        NonNullList<ItemStack> list = NonNullList.create();
                        componentItem.fillItemCategory(null, list);
                        list.forEach(output::accept);
                    } else if (item instanceof IGTTool tool) {
                        NonNullList<ItemStack> list = NonNullList.create();
                        tool.definition$fillItemCategory(null, list);
                        list.forEach(output::accept);
                    } else {
                        output.accept(item);
                    }
                }
            });
        }

        private boolean shouldInclude(Item item) {
            return switch (tabType) {
                case "material_items" -> isMaterialItem(item);
                case "material_blocks" -> isMaterialBlockItem(item); // Nome alterado para clareza
                case "fluids" -> isFluidItem(item);
                case "pipes" -> isPipeItem(item);
                case "items" -> isMiscItem(item);
                case "machines" -> item instanceof MetaMachineItem;
                case "custom_blocks" -> isCustomBlockItem(item); // Nova categoria
                default -> false;
            };
        }

        private TagPrefix getTagPrefix(Item item) {
            if (item instanceof TagPrefixItem tagPrefixItem) return tagPrefixItem.tagPrefix;
            if (item instanceof MaterialBlockItem materialBlockItem) return materialBlockItem.tagPrefix;
            return null;
        }

        private boolean isPipeItem(Item item) {
            if (item instanceof PipeBlockItem) return true;
            TagPrefix prefix = getTagPrefix(item);
            return prefix != null && (prefix.name().contains("pipe") || prefix.name().contains("wire") || prefix.name().contains("cable"));
        }

        private boolean isFluidItem(Item item) {
            if (item instanceof BucketItem) return true;
            TagPrefix prefix = getTagPrefix(item);
            return prefix != null && (prefix.name().contains("cell") || prefix.name().contains("bucket"));
        }

        // Verifica se é um bloco gerado automaticamente (Minério, Bloco de Material)
        private boolean isMaterialBlockItem(Item item) {
            // Se estiver na lista customizada, retorna FALSO para não aparecer na aba de materiais
            if (isCustomBlockItem(item)) return false;

            if (item instanceof MetaMachineItem) return false;
            if (item instanceof PipeBlockItem) return false;
            if (isPipeItem(item)) return false;

            // Se for um bloco genérico sem TagPrefix, pode cair aqui, mas queremos evitar isso se for um bloco custom
            if (item instanceof BlockItem && getTagPrefix(item) == null) return true;

            TagPrefix prefix = getTagPrefix(item);
            return prefix != null && (prefix.name().contains("block") || prefix.name().contains("ore") || prefix.name().contains("frame") || prefix.name().contains("planks") || prefix.name().contains("log"));
        }

        // Verifica se é um dos seus blocos customizados
        private boolean isCustomBlockItem(Item item) {
            return CUSTOM_BLOCKS.contains(item);
        }

        private boolean isMaterialItem(Item item) {
            // Exclui blocos customizados, máquinas, tubos, etc.
            if (item instanceof MetaMachineItem || isMaterialBlockItem(item) || isCustomBlockItem(item) || isPipeItem(item) || isFluidItem(item)) return false;
            return getTagPrefix(item) != null;
        }

        private boolean isMiscItem(Item item) {
            if (item instanceof MetaMachineItem) return false;
            if (item instanceof PipeBlockItem) return false;
            // Exclui se for um bloco customizado
            if (isCustomBlockItem(item)) return false;

            return getTagPrefix(item) == null && !(item instanceof BlockItem) && !(item instanceof BucketItem);
        }
    }
}