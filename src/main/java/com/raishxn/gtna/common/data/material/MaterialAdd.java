package com.raishxn.gtna.common.data.material;

import com.gregtechceu.gtceu.api.data.chemical.material.properties.*;

import com.raishxn.gtna.api.data.info.GTNAMaterialFlags;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

public class MaterialAdd {

    public static void init() {
        Bronze.addFlags(GENERATE_SPRING_SMALL, GENERATE_SPRING);
        Beryllium.addFlags(GENERATE_ROD, GENERATE_FRAME);
        // GTOCore's Cold Ice Freezer auxiliary tower is framed in Naquadah; GTCEu 7.5.3 does not
        // generate that frame, so the flag is restored here (same as Beryllium above).
        Naquadah.addFlags(GENERATE_FRAME);
        // GTOCore's Component Assembler extension is framed in Trinium; GTOCore adds the frame flag
        // to the GTCEu material and GTCEu 7.5.3 does not generate it.
        Trinium.addFlags(GENERATE_FRAME);
        // Same situation for the component_assembly_line's Naquadria frame ring.
        Naquadria.addFlags(GENERATE_FRAME);
        // Multi-plate items used by the ported GTNL recipes (triple/quadruple/quintuple plates).
        Steel.addFlags(GTNAMaterialFlags.GENERATE_TRIPLE_PLATE, GTNAMaterialFlags.GENERATE_QUADRUPLE_PLATE,
                GTNAMaterialFlags.GENERATE_QUINTUPLE_PLATE);
        // GTOCore ISA Mill MILLED products: the exact material list from classified/IsaMill.java.
        Grossular.addFlags(GTNAMaterialFlags.GENERATE_MILLED);
        Almandine.addFlags(GTNAMaterialFlags.GENERATE_MILLED);
        Chalcopyrite.addFlags(GTNAMaterialFlags.GENERATE_MILLED);
        NaquadahEnriched.addFlags(GTNAMaterialFlags.GENERATE_MILLED);
        Platinum.addFlags(GTNAMaterialFlags.GENERATE_MILLED);
        Redstone.addFlags(GTNAMaterialFlags.GENERATE_MILLED);
        Monazite.addFlags(GTNAMaterialFlags.GENERATE_MILLED);
        Pentlandite.addFlags(GTNAMaterialFlags.GENERATE_MILLED);
        Nickel.addFlags(GTNAMaterialFlags.GENERATE_MILLED);
        Spessartine.addFlags(GTNAMaterialFlags.GENERATE_MILLED);
        Pyrope.addFlags(GTNAMaterialFlags.GENERATE_MILLED);
        Sphalerite.addFlags(GTNAMaterialFlags.GENERATE_MILLED);
    }
}
