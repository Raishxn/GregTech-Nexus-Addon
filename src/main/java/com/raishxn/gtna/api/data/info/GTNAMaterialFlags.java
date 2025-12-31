package com.raishxn.gtna.api.data.info;

import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlag;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_PLATE;

public class GTNAMaterialFlags {

    public static final MaterialFlag GENERATE_DOUBLE_INGOT = new MaterialFlag.Builder("generate_double_ingot")
            .requireProps(PropertyKey.INGOT)
            .build();

    public static final MaterialFlag GENERATE_TRIPLE_INGOT = new MaterialFlag.Builder("generate_triple_ingot")
            .requireProps(PropertyKey.INGOT)
            .build();

    public static final MaterialFlag GENERATE_QUADRUPLE_INGOT = new MaterialFlag.Builder("generate_quadruple_ingot")
            .requireProps(PropertyKey.INGOT)
            .build();

    public static final MaterialFlag GENERATE_QUINTUPLE_INGOT = new MaterialFlag.Builder("generate_quintuple_ingot")
            .requireProps(PropertyKey.INGOT)
            .build();

    public static final MaterialFlag GENERATE_DOUBLE_PLATE = new MaterialFlag.Builder("generate_double_plate")
            .requireFlags(GENERATE_PLATE)
            .requireProps(PropertyKey.DUST)
            .build();

    public static final MaterialFlag GENERATE_TRIPLE_PLATE = new MaterialFlag.Builder("generate_triple_plate")
            .requireFlags(GENERATE_PLATE)
            .requireProps(PropertyKey.DUST)
            .build();

    public static final MaterialFlag GENERATE_QUADRUPLE_PLATE = new MaterialFlag.Builder("generate_quadruple_plate")
            .requireFlags(GENERATE_PLATE)
            .requireProps(PropertyKey.DUST)
            .build();

    public static final MaterialFlag GENERATE_QUINTUPLE_PLATE = new MaterialFlag.Builder("generate_quintuple_plate")
            .requireFlags(GENERATE_PLATE)
            .requireProps(PropertyKey.DUST)
            .build();

    public static final MaterialFlag GENERATE_SUPERDENSE = new MaterialFlag.Builder("generate_superdense")
            .requireFlags(GENERATE_PLATE)
            .requireProps(PropertyKey.DUST)
            .build();

    public static final MaterialFlag GENERATE_SINGULARITY = new MaterialFlag.Builder("generate_singularity")
            .build();

    public static void register() {
    }
}
