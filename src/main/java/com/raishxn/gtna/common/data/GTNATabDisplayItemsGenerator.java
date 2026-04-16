package com.raishxn.gtna.common.data;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.MaterialBlockItem;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.item.PipeBlockItem;
import com.gregtechceu.gtceu.api.item.TagPrefixItem;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.config.ConfigHolder;
import com.raishxn.gtna.config.GTNABalance;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class GTNATabDisplayItemsGenerator implements CreativeModeTab.DisplayItemsGenerator {

    private static final Set<Item> CUSTOM_BLOCKS = Set.of(
            GTNABlocks.BREEL_PIPE_CASING.get().asItem(),
            GTNABlocks.HYPER_PRESSURE_BREEL_CASING.get().asItem(),
            GTNABlocks.STEAM_COMPACT_PIPE_CASING.get().asItem(),
            GTNABlocks.VIBRATION_SAFE_CASING.get().asItem(),
            GTNABlocks.BRONZE_REINFORCED_WOOD.get().asItem(),
            GTNABlocks.SOLAR_BOILING_CELL.get().asItem(),
            GTNABlocks.STRONZE_WRAPPED_CASING.get().asItem(),
            GTNABlocks.HYDRAULIC_ASSEMBLER_CASING.get().asItem(),
            GTNABlocks.BREEL_PLATED_CASING.get().asItem(),
            GTNABlocks.BOROSILICATE_GLASS_BLOCK.get().asItem(),
            GTNABlocks.STEEL_REINFORCED_WOOD.get().asItem(),
            GTNABlocks.IRON_REINFORCED_WOOD.get().asItem());

    private final String tabType;
    private final GTRegistrate registrate;

    public GTNATabDisplayItemsGenerator(String tabType, GTRegistrate registrate) {
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
        String restrictedGroup = getRestrictedGroup(item);
        if (restrictedGroup != null && (!ConfigHolder.isRestrictedGroupAllowed(restrictedGroup) ||
                (ConfigHolder.shouldHideRestrictedItemsFromJei() &&
                        GTNABalance.isRestrictedGroupHiddenFromJei(restrictedGroup)))) {
            return false;
        }
        return switch (tabType) {
            case "material_items" -> isMaterialItem(item);
            case "material_blocks" -> isMaterialBlockItem(item);
            case "fluids" -> isFluidItem(item);
            case "pipes" -> isPipeItem(item);
            case "items" -> isMiscItem(item);
            case "machines" -> item instanceof MetaMachineItem && !isWirelessItem(item);
            case "wireless" -> isWirelessItem(item);
            case "custom_blocks" -> isCustomBlockItem(item);
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
        return prefix != null && (prefix.name().contains("pipe") || prefix.name().contains("wire") ||
                prefix.name().contains("cable"));
    }

    private boolean isFluidItem(Item item) {
        if (item instanceof BucketItem) return true;
        TagPrefix prefix = getTagPrefix(item);
        return prefix != null && (prefix.name().contains("cell") || prefix.name().contains("bucket"));
    }

    private boolean isMaterialBlockItem(Item item) {
        if (isCustomBlockItem(item) || isWirelessItem(item) || item instanceof MetaMachineItem ||
                item instanceof PipeBlockItem || isPipeItem(item)) {
            return false;
        }

        if (item instanceof BlockItem && getTagPrefix(item) == null) {
            return true;
        }

        TagPrefix prefix = getTagPrefix(item);
        return prefix != null && (prefix.name().contains("block") || prefix.name().contains("ore") ||
                prefix.name().contains("frame") || prefix.name().contains("planks") ||
                prefix.name().contains("log"));
    }

    private boolean isCustomBlockItem(Item item) {
        return CUSTOM_BLOCKS.contains(item);
    }

    private boolean isMaterialItem(Item item) {
        if (item instanceof MetaMachineItem || isMaterialBlockItem(item) || isCustomBlockItem(item) ||
                isPipeItem(item) || isFluidItem(item)) {
            return false;
        }
        return getTagPrefix(item) != null;
    }

    private boolean isMiscItem(Item item) {
        if (item instanceof MetaMachineItem || item instanceof PipeBlockItem || isCustomBlockItem(item)) {
            return false;
        }

        return getTagPrefix(item) == null && !(item instanceof BlockItem) && !(item instanceof BucketItem);
    }

    private boolean isWirelessItem(Item item) {
        ResourceLocation rl = ForgeRegistries.ITEMS.getKey(item);
        if (rl != null && rl.getNamespace().equals(GTNACORE.MOD_ID)) {
            String path = rl.getPath();
            return path.startsWith("wireless_energy_hatch_") || path.startsWith("wireless_dynamo_hatch_") ||
                    path.startsWith("nexus_capacitor_");
        }
        return false;
    }

    private String getRestrictedGroup(Item item) {
        ResourceLocation rl = ForgeRegistries.ITEMS.getKey(item);
        if (rl == null || !GTNACORE.MOD_ID.equals(rl.getNamespace())) {
            return null;
        }

        String path = rl.getPath();
        if (path.equals("infinite_steam_singleblock_cover") || path.equals("infinite_electric_singleblock_cover")) {
            return "infinityCovers";
        }
        if (path.equals("infinite_steam_input_bus") || path.startsWith("infinite_input_bus_") ||
                path.startsWith("infinite_input_hatch_")) {
            return "infiniteInputParts";
        }
        if (path.equals("output_boost_steam_output_bus") || path.startsWith("output_boost_hatch_") ||
                path.startsWith("output_boost_item_bus_") || path.startsWith("output_boost_fluid_hatch_")) {
            return "outputBoostParts";
        }
        if (path.startsWith("quantum_cosmic_nexus_")) {
            return "quantumCosmicNexusArmor";
        }
        if (path.equals("reality_ripper_sword")) {
            return "realityRipper";
        }
        return null;
    }
}
