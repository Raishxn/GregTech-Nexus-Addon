package com.raishxn.gtna.common.data.material;

import com.gregtechceu.gtceu.api.data.chemical.material.properties.*;

import com.raishxn.gtna.api.data.info.GTNAMaterialFlags;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

public class MaterialAdd {

    public static void init() {
        Bronze.addFlags(GENERATE_SPRING_SMALL, GENERATE_SPRING);
        Beryllium.addFlags(GENERATE_ROD, GENERATE_FRAME);
        // Multi-plate items used by the ported GTNL recipes (triple/quadruple/quintuple plates).
        Steel.addFlags(GTNAMaterialFlags.GENERATE_TRIPLE_PLATE, GTNAMaterialFlags.GENERATE_QUADRUPLE_PLATE,
                GTNAMaterialFlags.GENERATE_QUINTUPLE_PLATE);
    }
}
