package com.raishxn.gtna.data;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.api.data.tag.GTNATagPrefix;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;
import org.apache.commons.lang3.text.WordUtils;

import java.util.HashSet;
import java.util.Set;

public class GTNALangProvider extends LanguageProvider {

    private final Set<String> addedKeys = new HashSet<>();

    private static final TagPrefix[] GTNA_PREFIXES = {
            GTNATagPrefix.doubleIngot,
            GTNATagPrefix.tripleIngot,
            GTNATagPrefix.quadrupleIngot,
            GTNATagPrefix.quintupleIngot,
            GTNATagPrefix.triplePlate,
            GTNATagPrefix.quadruplePlate,
            GTNATagPrefix.quintuplePlate,
            GTNATagPrefix.superdensePlate,
            GTNATagPrefix.singularity
    };

    public GTNALangProvider(PackOutput output) {
        super(output, GTNACORE.MOD_ID, "en_us");
    }
    @Override
    public void add(String key, String value) {
        if (addedKeys.contains(key)) {
            return;
        }
        addedKeys.add(key);
        super.add(key, value);
    }
    @Override
    protected void addTranslations() {
        addStaticTranslations();
        addTagPrefixCategories();
        for (Material material : GTCEuAPI.materialManager.getRegisteredMaterials()) {
            if (material.getModid().equals(GTNACORE.MOD_ID)) {
                String langKey = material.getUnlocalizedName();
                String matName = formatMaterialName(material.getName());

                add(langKey, matName);

                for (TagPrefix prefix : GTNA_PREFIXES) {
                    if (prefix.doGenerateItem(material)) {
                        String idNoMod = String.format(prefix.idPattern(), material.getName());
                        String itemKey = "item." + GTNACORE.MOD_ID + "." + idNoMod;
                        String itemValue = String.format(prefix.langValue(), matName);

                        add(itemKey, itemValue);
                    }
                }
            }
        }
    }

    private String formatMaterialName(String name) {
        return WordUtils.capitalize(name.replace('_', ' '));
    }

    private void addTagPrefixCategories() {
        add(GTNATagPrefix.doubleIngot.getUnlocalizedName(), "Double Ingot");
        add(GTNATagPrefix.tripleIngot.getUnlocalizedName(), "Triple Ingot");
        add(GTNATagPrefix.quadrupleIngot.getUnlocalizedName(), "Quadruple Ingot");
        add(GTNATagPrefix.quintupleIngot.getUnlocalizedName(), "Quintuple Ingot");
        add(GTNATagPrefix.triplePlate.getUnlocalizedName(), "Triple Plate");
        add(GTNATagPrefix.quadruplePlate.getUnlocalizedName(), "Quadruple Plate");
        add(GTNATagPrefix.quintuplePlate.getUnlocalizedName(), "Quintuple Plate");
        add(GTNATagPrefix.superdensePlate.getUnlocalizedName(), "Superdense Plate");
        add(GTNATagPrefix.singularity.getUnlocalizedName(), "Singularity");
    }

    private void addStaticTranslations() {
        add("block.gtna.wireless_steam_input_hatch", "Wireless Steam Input Hatch");
        add("block.gtna.wireless_steam_output_hatch", "Wireless Steam Output Hatch");
        add("item.gtna.structure_detect", "Structure Writer");
        add("item.gtna.debug_structure_writer", "Debug Structure Writer");
        add("item.gtna.vajra", "Vajra");

        add("item.gtna.structure_detect.tooltip.0", "§aRight click§7 block to select Multiblock Controller.");
        add("item.gtna.structure_detect.tooltip.1", "§aShift Right click§7 to change mode.");
        add("item.gtna.structure_detect.tooltip.2", "§aMode: §f%s");
        add("item.gtna.structure_detect.error.0", "Required at %s:\n");
        add("item.gtna.structure_detect.error.1", "Required at %s:");
        add("item.gtna.structure_detect.error.2", "At %s %s");
        add("item.gtna.structure_detect.error.3", "(Mirrored Mode)");
        add("item.gtna.structure_detect.error.4", "(Normal Mode)");
        add("structure_detect.tooltip.0", "Right-click multiblock main block");
        add("structure_detect.tooltip.1", "Shift right-click to switch detection mode");

        add("itemGroup.gtna.creative_tab", "GregTech: Nexus Addon");
        add("itemGroup.gtna.creative_tab.machines", "GregTech: Nexus Addon Machines");
        add("itemGroup.gtna.creative_tab.items", "GregTech: Nexus Addon Items");
        add("itemGroup.gtna.creative_tab.material_blocks", "GregTech: Nexus Addon Material Blocks");
        add("itemGroup.gtna.creative_tab.material_fluids", "GregTech: Nexus Addon Material Fluids");
        add("itemGroup.gtna.creative_tab.material_items", "GregTech: Nexus Addon Material Items");
        add("itemGroup.gtna.creative_tab.material_pipes", "GregTech: Nexus Addon Material Pipes & Wires");

        add("structure_writer.export_order", "Export Order: C:%s  S:%s  A:%s");
        add("structure_writer.structural_scale", "Structure Scale: X:%s  Y:%s  Z:%s");
        add("message.gtna.detection_mode_mirrored", "Current detection mode: (Mirrored mode)");
        add("message.gtna.detection_mode_normal", "Current detection mode: (Normal mode)");
        add("message.gtnacore.structure_formed", "Structure formed");
        add("block.gtna.large_steam_crusher", "Large Steam Crusher");
        add("item.gtna.precision_steam_component", "Precision Steam Component");
        add("gtna.tooltip.large_steam_crusher.speed", "Speed: 900% faster than singleblock");
        add("gtna.tooltip.large_steam_crusher.steam", "Steam Consumption: 80% of original");
        add("gtna.tooltip.large_steam_crusher.parallel","Process up to 128 items at once");
        add("gtna.registry.add", "Added by GregTech Nexus Addon");
    }
}