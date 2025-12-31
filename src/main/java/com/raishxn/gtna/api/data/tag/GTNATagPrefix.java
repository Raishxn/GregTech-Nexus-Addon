package com.raishxn.gtna.api.data.tag;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.raishxn.gtna.api.data.info.GTNAMaterialFlags;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.Conditions.hasDustProperty;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.Conditions.hasIngotProperty;

@SuppressWarnings("unused")
public class GTNATagPrefix {

    public static final TagPrefix doubleIngot = new TagPrefix("doubleIngot")
            .idPattern("double_%s_ingot")
            .defaultTagPath("double_ingots/%s")
            .unformattedTagPath("double_ingots")
            .langValue("Double %s Ingot")
            .materialAmount(GTValues.M * 2)
            .maxStackSize(32)
            .materialIconType(MaterialIconType.ingotDouble)
            .unificationEnabled(true)
            .enableRecycling()
            .generateItem(true)
            .generationCondition(hasIngotProperty.and(mat -> mat.hasFlag(GTNAMaterialFlags.GENERATE_DOUBLE_INGOT)));

    public static final TagPrefix tripleIngot = new TagPrefix("tripleIngot")
            .idPattern("triple_%s_ingot")
            .defaultTagPath("triple_ingots/%s")
            .unformattedTagPath("triple_ingots")
            .langValue("Triple %s Ingot")
            .materialAmount(GTValues.M * 3)
            .maxStackSize(32)
            .materialIconType(MaterialIconType.ingotTriple)
            .unificationEnabled(true)
            .enableRecycling()
            .generateItem(true)
            .generationCondition(hasIngotProperty.and(mat -> mat.hasFlag(GTNAMaterialFlags.GENERATE_TRIPLE_INGOT)));

    public static final TagPrefix quadrupleIngot = new TagPrefix("quadrupleIngot")
            .idPattern("quadruple_%s_ingot")
            .defaultTagPath("quadruple_ingots/%s")
            .unformattedTagPath("quadruple_ingots")
            .langValue("Quadruple %s Ingot")
            .materialAmount(GTValues.M * 4)
            .maxStackSize(32)
            .materialIconType(MaterialIconType.ingotQuadruple)
            .unificationEnabled(true)
            .enableRecycling()
            .generateItem(true)
            .generationCondition(hasIngotProperty.and(mat -> mat.hasFlag(GTNAMaterialFlags.GENERATE_QUADRUPLE_INGOT)));

    public static final TagPrefix quintupleIngot = new TagPrefix("quintupleIngot")
            .idPattern("quintuple_%s_ingot")
            .defaultTagPath("quintuple_ingots/%s")
            .unformattedTagPath("quintuple_ingots")
            .langValue("Quintuple %s Ingot")
            .materialAmount(GTValues.M * 5)
            .maxStackSize(32)
            .materialIconType(MaterialIconType.ingotQuintuple)
            .unificationEnabled(true)
            .enableRecycling()
            .generateItem(true)
            .generationCondition(hasIngotProperty.and(mat -> mat.hasFlag(GTNAMaterialFlags.GENERATE_QUINTUPLE_INGOT)));

    public static final TagPrefix triplePlate = new TagPrefix("triplePlate")
            .idPattern("triple_%s_plate")
            .defaultTagPath("triple_plates/%s")
            .unformattedTagPath("triple_plates")
            .langValue("Triple %s Plate")
            .materialAmount(GTValues.M * 3)
            .maxStackSize(32)
            .materialIconType(MaterialIconType.plateTriple)
            .unificationEnabled(true)
            .enableRecycling()
            .generateItem(true)
            .generationCondition(hasDustProperty.and(mat -> mat.hasFlag(GTNAMaterialFlags.GENERATE_TRIPLE_PLATE)));

    public static final TagPrefix quadruplePlate = new TagPrefix("quadruplePlate")
            .idPattern("quadruple_%s_plate")
            .defaultTagPath("quadruple_plates/%s")
            .unformattedTagPath("quadruple_plates")
            .langValue("Quadruple %s Plate")
            .materialAmount(GTValues.M * 4)
            .maxStackSize(32)
            .materialIconType(MaterialIconType.plateQuadruple)
            .unificationEnabled(true)
            .enableRecycling()
            .generateItem(true)
            .generationCondition(hasDustProperty.and(mat -> mat.hasFlag(GTNAMaterialFlags.GENERATE_QUADRUPLE_PLATE)));

    public static final TagPrefix quintuplePlate = new TagPrefix("quintuplePlate")
            .idPattern("quintuple_%s_plate")
            .defaultTagPath("quintuple_plates/%s")
            .unformattedTagPath("quintuple_plates")
            .langValue("Quintuple %s Plate")
            .materialAmount(GTValues.M * 5)
            .maxStackSize(32)
            .materialIconType(MaterialIconType.plateQuintuple)
            .unificationEnabled(true)
            .enableRecycling()
            .generateItem(true)
            .generationCondition(hasDustProperty.and(mat -> mat.hasFlag(GTNAMaterialFlags.GENERATE_QUINTUPLE_PLATE)));

    public static final TagPrefix superdensePlate = new TagPrefix("superdensePlate")
            .idPattern("superdense_%s_plate")
            .defaultTagPath("superdense_plates/%s")
            .unformattedTagPath("superdense_plates")
            .langValue("Superdense %s Plate")
            .materialAmount(GTValues.M * 64)
            .maxStackSize(16)
            .materialIconType(new MaterialIconType("plateSuperdense"))
            .unificationEnabled(true)
            .enableRecycling()
            .generateItem(true)
            .generationCondition(hasDustProperty.and(mat -> mat.hasFlag(GTNAMaterialFlags.GENERATE_SUPERDENSE)));

    public static final TagPrefix singularity = new TagPrefix("singularity")
            .idPattern("%s_singularity")
            .defaultTagPath("singularities/%s")
            .unformattedTagPath("singularities")
            .langValue("%s Singularity")
            .materialIconType(new MaterialIconType("singularity"))
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(mat -> mat.hasFlag(GTNAMaterialFlags.GENERATE_SINGULARITY));

    public static void register() {
    }
}