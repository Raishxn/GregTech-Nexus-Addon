package com.raishxn.gtna.common.data;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import com.raishxn.gtna.common.data.material.MaterialAdd;
import com.raishxn.gtna.common.data.material.MaterialBuilder;

public class GTNAMaterials {

    public static Material Stronze;
    public static Material Breel;
    public static Material HastelloyN;
    public static Material AluminiumBronze;
    public static Material EglinSteel;
    public static Material RawBrine;
    public static Material HotBrine;
    public static Material HotChlorinatedBrominatedBrine;
    public static Material HotDebrominatedBrine;
    public static Material HotAlkalineDebrominatedBrine;
    public static Material DebrominatedBrine;
    public static Material BrominatedChlorineVapor;
    public static Material AcidicBromineSolution;
    public static Material ConcentratedBromineSolution;
    public static Material AcidicBromineExhaust;
    public static Material HydrogenIodide;
    public static Material DarkSteel;
    public static Material EndSteel;
    public static Material Indalloy140;
    public static Material Trinaquadalloy;
    public static Material MarM200Steel;
    public static Material FallKing;
    public static Material Acrylonitrile;
    public static Material Abs;
    public static Material Polystyrene;
    public static Material CobaltOxide;
    public static Material LithiumOxide;
    public static Material ZirconiumOxide;
    public static Material ZirconiaCeramic;
    public static Material ClayCompound;
    public static Material Echoite;
    public static Material DenseSupercriticalSteam;
    public static Material SuperHeatedSteam;
    public static Material InsanelySupercriticalSteam;
    public static Material CompressedSteam;

    public static void init() {
        MaterialBuilder.init();
        MaterialAdd.init();
    }
}
