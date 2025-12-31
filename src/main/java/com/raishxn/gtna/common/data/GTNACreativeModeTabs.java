package com.raishxn.gtna.common.data;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.*;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.raishxn.gtna.GTNACORE;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.gregtechceu.gtceu.api.item.PipeBlockItem;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.NotNull;

import static com.raishxn.gtna.api.registry.GTNARegistry.REGISTRATE;

@SuppressWarnings("unused")
public class GTNACreativeModeTabs {

    // 1. Aba de Items Materiais
    public static RegistryEntry<CreativeModeTab> MATERIAL_ITEMS = REGISTRATE.defaultCreativeTab("gtna_material_items",
                    builder -> builder.displayItems(new RegistrateDisplayItemsGenerator("material_items", REGISTRATE))
                            .title(REGISTRATE.addLang("itemGroup", GTNACORE.id("creative_tab.material_items"), "GTNA Material Items"))
                            .icon(() -> ChemicalHelper.get(TagPrefix.ingot, GTNAMaterials.Echoite))
                            .build())
            .register();

    // 2. Aba de Blocos
    public static RegistryEntry<CreativeModeTab> MATERIAL_BLOCKS = REGISTRATE.defaultCreativeTab("gtna_material_blocks",
                    builder -> builder.displayItems(new RegistrateDisplayItemsGenerator("blocks", REGISTRATE))
                            .title(REGISTRATE.addLang("itemGroup", GTNACORE.id("creative_tab.material_blocks"), "GTNA Blocks"))
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

    // 6. Aba de Máquinas
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
                case "blocks" -> isBlockItem(item);
                case "fluids" -> isFluidItem(item);
                case "pipes" -> isPipeItem(item);
                case "items" -> isMiscItem(item);
                case "machines" -> item instanceof MetaMachineItem;
                default -> false;
            };
        }

        private TagPrefix getTagPrefix(Item item) {
            if (item instanceof TagPrefixItem tagPrefixItem) return tagPrefixItem.tagPrefix;
            if (item instanceof MaterialBlockItem materialBlockItem) return materialBlockItem.tagPrefix;
            // Removido PipeBlockItem daqui pois ele não tem o campo tagPrefix acessível
            return null;
        }

        private boolean isPipeItem(Item item) {
            // CORREÇÃO: Verificação direta da classe. Se for PipeBlockItem, é 100% um tubo ou cabo.
            if (item instanceof PipeBlockItem) return true;

            TagPrefix prefix = getTagPrefix(item);
            return prefix != null && (prefix.name().contains("pipe") || prefix.name().contains("wire") || prefix.name().contains("cable"));
        }

        private boolean isFluidItem(Item item) {
            if (item instanceof BucketItem) return true;
            TagPrefix prefix = getTagPrefix(item);
            return prefix != null && (prefix.name().contains("cell") || prefix.name().contains("bucket"));
        }

        private boolean isBlockItem(Item item) {
            if (item instanceof MetaMachineItem) return false;

            // CORREÇÃO: Exclui PipeBlockItem explicitamente para ele não cair aqui como bloco genérico
            if (item instanceof PipeBlockItem) return false;

            if (isPipeItem(item)) return false;

            if (item instanceof BlockItem && getTagPrefix(item) == null) return true;
            TagPrefix prefix = getTagPrefix(item);
            return prefix != null && (prefix.name().contains("block") || prefix.name().contains("ore") || prefix.name().contains("frame") || prefix.name().contains("planks") || prefix.name().contains("log"));
        }

        private boolean isMaterialItem(Item item) {
            if (item instanceof MetaMachineItem || isBlockItem(item) || isPipeItem(item) || isFluidItem(item)) return false;
            return getTagPrefix(item) != null;
        }

        private boolean isMiscItem(Item item) {
            if (item instanceof MetaMachineItem) return false;
            // PipeBlockItem é BlockItem, então precisamos garantir que ele não entre aqui também,
            // mas como ele tem lógica própria acima, o filtro "isBlockItem" e "isPipeItem" já cuidam disso nas outras abas.
            if (item instanceof PipeBlockItem) return false;

            return getTagPrefix(item) == null && !(item instanceof BlockItem) && !(item instanceof BucketItem);
        }
    }
}