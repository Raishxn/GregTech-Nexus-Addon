package com.raishxn.gtna.data;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.raishxn.gtna.GTNACORE;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;
import org.apache.commons.lang3.text.WordUtils;

public class GTNALangProvider extends LanguageProvider {

    public GTNALangProvider(PackOutput output) {
        super(output, GTNACORE.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        addStaticTranslations();
        for (Material material : GTCEuAPI.materialManager.getRegisteredMaterials()) {
            if (material.getModid().equals(GTNACORE.MOD_ID)) {
                String langKey = material.getUnlocalizedName();
                String name = formatMaterialName(material.getName());

                add(langKey, name);
            }
        }
    }

    private String formatMaterialName(String name) {
        return WordUtils.capitalize(name.replace('_', ' '));
    }
    private void addStaticTranslations() {
        // Blocos
        add("block.gtna.wireless_steam_input_hatch", "Wireless Steam Input Hatch");
        add("block.gtna.wireless_steam_input_hatch_steel", "Wireless Steam Input Hatch Steel");
        add("block.gtna.wireless_steam_output_hatch", "Wireless Steam Output Hatch");
        add("block.gtna.wireless_steam_output_hatch_steel", "Wireless Steam Output Hatch Steel");

        // Items e Tools
        add("item.gtceu.tool.vajra", "Vajra");
        add("item.gtna.debug_structure_writer", "Debug Structure Writer");
        add("item.gtna.hydraulic_arm", "Hydraulic Arm");
        add("item.gtna.hydraulic_conveyor", "Hydraulic Conveyor");
        add("item.gtna.hydraulic_motor", "Hydraulic Motor");
        add("item.gtna.hydraulic_piston", "Hydraulic Piston");
        add("item.gtna.hydraulic_pump", "Hydraulic Pump");
        add("item.gtna.hydraulic_regulator", "Hydraulic Regulator");
        add("item.gtna.hydraulic_steam_jet_spewer", "Hydraulic Steam Jet Spewer");
        add("item.gtna.hydraulic_steam_receiver", "Hydraulic Steam Receiver");
        add("item.gtna.hydraulic_vapor_generator", "Hydraulic Vapor Generator");

        // Structure Detector
        add("item.gtna.structure_detect", "Structure Detector");
        add("item.gtna.structure_detect.error.0", "Required at %s:\n");
        add("item.gtna.structure_detect.error.1", "Required at %s:");
        add("item.gtna.structure_detect.error.2", "At %s %s");
        add("item.gtna.structure_detect.error.3", "(Mirrored Mode)");
        add("item.gtna.structure_detect.error.4", "(Normal Mode)");
        add("structure_detect.tooltip.0", "Right-click multiblock main block");
        add("structure_detect.tooltip.1", "Shift right-click to switch detection mode");

        // Tabs
        add("itemGroup.gtna.creative_tab", "GregTech: Nexus Addon");
        add("itemGroup.gtna.creative_tab.machines", "GregTech: Nexus Addon Machines");
        add("itemGroup.gtna.creative_tab.core", "GregTech: Nexus Addon Items");

        // Messages & Misc
        add("structure_writer.export_order", "Export Order: C:%s  S:%s  A:%s");
        add("structure_writer.structural_scale", "Structure Scale: X:%s  Y:%s  Z:%s");
        add("message.gtna.detection_mode_mirrored", "Current detection mode: (Mirrored mode)");
        add("message.gtna.detection_mode_normal", "Current detection mode: (Normal mode)");
        add("message.gtnacore.structure_formed", "Structure formed");

        // Machine Tooltips
        add("gtna.machine.wireless_steam_hatch.tooltip", "Receives steam wirelessly from your global network.");
        add("gtna.machine.wireless_steam_output_hatch.tooltip", "Sends steam wirelessly to your global network (Boilers Only).");
    }
}