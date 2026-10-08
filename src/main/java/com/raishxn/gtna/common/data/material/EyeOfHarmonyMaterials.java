package com.raishxn.gtna.common.data.material;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;

import com.raishxn.gtna.GTNACORE;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.raishxn.gtna.common.data.GTNAMaterials.*;

/** Core GTNH EOH identities, shared by the classic and BEC progressions. */
public final class EyeOfHarmonyMaterials {

    private EyeOfHarmonyMaterials() {}

    public static void init() {
        // GTCEu 7.5.3 has none of these identities. Acquisition and working recipes are
        // intentionally explicit: stock low-tier material autogen would bypass their progression.
        // GTNH texture sets (untinted) and fluid textures, as in MaterialsInit.
        SpaceTime = metal("space_time", "SpaceTime", 0xffffff, GodforgeIconSets.SPACETIME, true).setFormula("Φ");
        WhiteDwarfMatter = metal("white_dwarf_matter", "White Dwarf Matter", 0xffffff,
                GodforgeIconSets.WHITE_DWARF_MATTER, true).setFormula("∅");
        BlackDwarfMatter = metal("black_dwarf_matter", "Black Dwarf Matter", 0xffffff,
                GodforgeIconSets.BLACK_DWARF_MATTER, false).setFormula(">>∅<<");
        Universium = metal("universium", "Universium", 0xffffff, GodforgeIconSets.UNIVERSIUM, true)
                .setFormula("Σ§kX");
        // RawStarMatter is a normal liquid in GTNH, despite "plasma" in its display name.
        RawStarMatter = new Material.Builder(GTNACORE.id("raw_star_matter"))
                .langValue("Condensed Raw Stellar Plasma Mixture")
                .liquid(new com.gregtechceu.gtceu.api.fluids.FluidBuilder().temperature(295).customStill())
                .color(0x6401ff).flags(DISABLE_DECOMPOSITION, DISABLE_MATERIAL_RECIPES).buildAndRegister();
        Time = new Material.Builder(GTNACORE.id("temporal_fluid"))
                .langValue("Tachyon Rich Temporal Fluid").liquid(0)
                .color(0x6401ff).flags(DISABLE_DECOMPOSITION, DISABLE_MATERIAL_RECIPES).buildAndRegister();
        Space = new Material.Builder(GTNACORE.id("spatial_fluid"))
                .langValue("Spatially Enlarged Fluid").liquid(0)
                .color(0x6401ff).flags(DISABLE_DECOMPOSITION, DISABLE_MATERIAL_RECIPES).buildAndRegister();
    }

    private static Material metal(String id, String name, int color, MaterialIconSet iconSet,
                                  boolean customFluid) {
        // Only the structural forms needed by EOH and its dependencies; no ore/worldgen/tools.
        var fluid = new com.gregtechceu.gtceu.api.fluids.FluidBuilder().temperature(0);
        if (customFluid) fluid.customStill();
        // GTNH Black Dwarf Matter is black (ARGB ff000000); it has no fluid texture, so colour the liquid only.
        if ("black_dwarf_matter".equals(id)) fluid.color(0x16141e);
        return new Material.Builder(GTNACORE.id(id)).langValue(name).ingot().liquid(fluid)
                .color(color).iconSet(iconSet)
                .flags(GENERATE_PLATE, GENERATE_DENSE, GENERATE_FRAME, GENERATE_ROD, GENERATE_LONG_ROD,
                        GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_BOLT_SCREW,
                        DISABLE_DECOMPOSITION, DISABLE_MATERIAL_RECIPES)
                .buildAndRegister();
    }
}
