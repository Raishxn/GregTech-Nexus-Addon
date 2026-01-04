package com.raishxn.gtna.common.data.material;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty;
import com.gregtechceu.gtceu.api.fluids.FluidBuilder;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.api.item.tool.GTNAToolType;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet.*;
import static com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty.GasTier.HIGHER;
import static com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty.GasTier.LOW;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.raishxn.gtna.api.data.info.GTNAMaterialFlags.*;
import static com.raishxn.gtna.common.data.GTNAMaterials.*;

public class MaterialBuilder {

    public static void init() {
            Stronze = new Material.Builder(GTNACORE.id("stronze"))
                    .ingot().fluid().dust()
                    .color(0x968030).iconSet(METALLIC)
                    .components(Bronze, 1, Steel, 2)
                    .blastTemp(1123, BlastProperty.GasTier.LOW)
                    .flags(GENERATE_PLATE, GENERATE_ROD, GENERATE_LONG_ROD, GENERATE_BOLT_SCREW,
                            GENERATE_FRAME, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING,
                            GENERATE_ROUND, GENERATE_SPRING, GENERATE_SPRING_SMALL,
                            GENERATE_FOIL, GENERATE_FINE_WIRE, GENERATE_ROTOR, GENERATE_DENSE)
                    .fluidPipeProperties(1123,1000,true,true,true,true)
                    .appendFlags(EXT2_METAL)
                    .buildAndRegister().setFormula("(SnCu3)(Fe50C)2");

            Breel = new Material.Builder(GTNACORE.id("breel"))
                    .dust().ingot().fluid()
                    .color(0x506040).iconSet(MaterialIconSet.SHINY)
                    .components(Bronze, 2, Steel, 1)
                    .blastTemp(1123, BlastProperty.GasTier.LOW)
                    .flags(GENERATE_PLATE, GENERATE_ROD, GENERATE_LONG_ROD, GENERATE_BOLT_SCREW,
                            GENERATE_FRAME, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING,
                            GENERATE_ROUND, GENERATE_SPRING, GENERATE_SPRING_SMALL,
                            GENERATE_FOIL, GENERATE_FINE_WIRE, GENERATE_ROTOR, GENERATE_DENSE)
                    .fluidPipeProperties(1123,1000,true,true,true,true)
                    .buildAndRegister().setFormula("(Fe50C)(SnCu3)2");


            ClayCompound = new Material.Builder(GTNACORE.id("clay_compound"))
                    .dust().ingot()
                    .color(0xAA8866).iconSet(MaterialIconSet.DULL)
                    .components(Flint, 1, Clay, 1, Stone, 1)

                    .flags(GENERATE_PLATE, GENERATE_ROD, GENERATE_LONG_ROD, GENERATE_BOLT_SCREW,
                            GENERATE_FRAME, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING,
                            GENERATE_ROUND)
                    .fluidPipeProperties(167,1000,false,false,true,false)

                    .buildAndRegister().setFormula("?(NA2LiAl2Si2O7(H2O)2)(SiO2)");

            Echoite = new Material.Builder(GTNACORE.id("echoite"))
                    .ingot().fluid().plasma().dust()
                    .blastTemp(1730,LOW)
                    .color(0x26734d)
                    .iconSet(METALLIC)
                    .flags(GENERATE_PLATE, GENERATE_ROD, GENERATE_LONG_ROD, GENERATE_BOLT_SCREW,
                            GENERATE_FRAME, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING,
                            GENERATE_ROUND, GENERATE_ROTOR,GENERATE_SINGULARITY,GENERATE_DOUBLE_INGOT,GENERATE_TRIPLE_INGOT,GENERATE_QUADRUPLE_INGOT,GENERATE_QUINTUPLE_INGOT
                    ,GENERATE_DOUBLE_PLATE,GENERATE_TRIPLE_PLATE,GENERATE_QUADRUPLE_PLATE,GENERATE_QUINTUPLE_PLATE,GENERATE_DENSE,GENERATE_SUPERDENSE)

                    .cableProperties(GTValues.V[GTValues.MV], 32, 0, true)
                    .toolStats(ToolProperty.Builder.of(8.0F, 100.0F, 64, 6, GTNAToolType.VAJRA).magnetic()
                            .unbreakable().build())
                    .fluidPipeProperties(2000000,100000,true,true,true,true)
                    .buildAndRegister().setFormula("Ec");

            // --- LINGOTE ESPECIAL (Compressed Steam) ---

            CompressedSteam = new Material.Builder(GTNACORE.id("compressed_steam"))
                    .ingot()
                    .color(0xCCCCCC).iconSet(MaterialIconSet.SHINY)
                    .flags(NO_SMELTING, GENERATE_PLATE, GENERATE_ROD, GENERATE_FRAME, GENERATE_GEAR,
                            GENERATE_DENSE, GENERATE_SUPERDENSE, GENERATE_DOUBLE_PLATE,
                            GENERATE_TRIPLE_PLATE, GENERATE_QUADRUPLE_PLATE, GENERATE_QUINTUPLE_PLATE,GENERATE_ROTOR)
                    .fluidPipeProperties(500, 500, true, true, true, false)
                    .buildAndRegister().setFormula("H2O");

            // --- FLUIDOS (Vapores) ---

            DenseSupercriticalSteam = new Material.Builder(GTNACORE.id("dense_supercritical_steam"))
                    .gas(295000).fluid()
                    .color(0xA0A0A0)
                    .iconSet(SHINY)
                    .buildAndRegister();

            SuperHeatedSteam = new Material.Builder(GTNACORE.id("super_heated_steam"))
                    .gas(600000).fluid()
                    .color(0xC0C0C0)
                    .iconSet(BRIGHT)
                    .flags(DISABLE_DECOMPOSITION)
                    .buildAndRegister();

            InsanelySupercriticalSteam = new Material.Builder(GTNACORE .id("insanely_supercritical_steam"))
                    .gas(1000000).fluid()
                    .color(0xFFFFFF)
                    .iconSet(RADIOACTIVE)
                    .flags(DISABLE_DECOMPOSITION)
                    .buildAndRegister();

        }
    }


