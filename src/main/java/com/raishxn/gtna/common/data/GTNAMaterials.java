package com.raishxn.gtna.common.data;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.raishxn.gtna.common.data.material.MaterialAdd;
import com.raishxn.gtna.common.data.material.MaterialBuilder;

import static com.raishxn.gtna.api.registry.GTNARegistry.REGISTRATE;

public class GTNAMaterials {

    public static Material Stronze;
    public static Material Breel;
    public static Material ClayCompound;
    public static Material Echoite;
    public static Material DenseSupercriticalSteam;
    public static Material SuperHeatedSteam;
    public static Material InsanelySupercriticalSteam;
    public static Material CompressedSteam;

    public static void init(){
        MaterialBuilder.init();
        MaterialAdd.init();
    }
}