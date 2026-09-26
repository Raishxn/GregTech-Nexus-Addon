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
    /** GTOCore alloy additions required by the ISA Mill casings and controller recipe. */
    public static Material Inconel625;
    public static Material Inconel792;
    public static Material Tantalloy61;
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
    public static Material Tanmolyium;
    /** GTOCore alloy additions required by the Industrial Flotation Cell casings and controller. */
    public static Material HastelloyN75;
    public static Material Stellite;
    /** GTOCore flotation reagents (Industrial Flotation Cell inputs). */
    public static Material SodiumEthylxanthate;
    public static Material PotassiumEthylxanthate;
    public static Material Turpentine;
    /**
     * GTOCore flotation products: the {@code *Front} ore foams produced by the Industrial Flotation
     * Cell and dried back into GTCEu dusts by the Vacuum Drying Furnace. Every one of them has a
     * drying consumer (see {@code GTNAFlotationDryingRecipes}).
     */
    public static Material PyropeFront;
    public static Material RedstoneFront;
    public static Material ChalcopyriteFront;
    public static Material MonaziteFront;
    public static Material EnrichedNaquadahFront;
    public static Material GrossularFront;
    public static Material NickelFront;
    public static Material AlmandineFront;
    public static Material PlatinumFront;
    public static Material PentlanditeFront;
    public static Material SpessartineFront;
    public static Material SphaleriteFront;
    /** GTOCore drying by-products; {@code RedMud} is neutralised in the Chemical Reactor. */
    public static Material RedMud;
    public static Material NeutralisedRedMud;
    /** GTOCore composite frame of the Component Assembler extension (GTO MaterialComposite:179). */
    public static Material CarbonFiberPolyphenyleneSulfideComposite;
    /** GTOCore ceramic of the extension's titanium-nitride mechanical block (GTO MaterialB:4980). */
    public static Material TitaniumNitrideCeramic;

    public static void init() {
        MaterialBuilder.init();
        MaterialAdd.init();
    }
}
