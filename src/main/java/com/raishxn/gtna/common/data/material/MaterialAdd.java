package com.raishxn.gtna.common.data.material;

import com.gregtechceu.gtceu.api.data.chemical.material.properties.*;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

public class MaterialAdd {

    public static void init() {
        Bronze.addFlags(GENERATE_SPRING_SMALL, GENERATE_SPRING);
        Beryllium.addFlags(GENERATE_ROD, GENERATE_FRAME);
    }
}
