package com.raishxn.gtna.common.data.material;

import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;

/**
 * GTNH custom texture sets ({@code TextureSet.SET_SPACETIME}, {@code SET_UNIVERSIUM}, {@code SET_WHITE_DWARF_MATTER},
 * {@code SET_MHDCSM}, {@code SET_MAGMATTER}, {@code SET_GRAVITON_SHARD}; GT5-Unofficial a3e1e112). Textures are
 * ported by {@code tools/port_godforge_icon_sets.py}; materials using them stay untinted (white).
 */
public final class GodforgeIconSets {

    public static final MaterialIconSet SPACETIME = new MaterialIconSet("spacetime", MaterialIconSet.SHINY);
    public static final MaterialIconSet UNIVERSIUM = new MaterialIconSet("universium", MaterialIconSet.METALLIC);
    public static final MaterialIconSet WHITE_DWARF_MATTER = new MaterialIconSet("white_dwarf_matter",
            MaterialIconSet.SHINY);
    public static final MaterialIconSet BLACK_DWARF_MATTER = new MaterialIconSet("black_dwarf_matter",
            MaterialIconSet.METALLIC);
    public static final MaterialIconSet MHDCSM = new MaterialIconSet("mhdcsm", MaterialIconSet.SHINY);
    public static final MaterialIconSet MAGMATTER = new MaterialIconSet("magmatter", MaterialIconSet.SHINY);
    public static final MaterialIconSet GRAVITON_SHARD = new MaterialIconSet("graviton_shard",
            MaterialIconSet.DIAMOND);

    /** GTNH {@code SET_INFINITY}/{@code SET_ETERNITY}; textures were imported with G-0183. */
    public static final MaterialIconSet INFINITY = new MaterialIconSet("infinity", MaterialIconSet.SHINY);
    public static final MaterialIconSet ETERNITY = new MaterialIconSet("eternity", MaterialIconSet.SHINY);

    /** GT++ {@code SET_HYPOGEN} (Hypogen, Dragonblood tinted) and {@code SET_CHROMATIC_GLASS}. */
    public static final MaterialIconSet HYPOGEN = new MaterialIconSet("hypogen", MaterialIconSet.SHINY);
    public static final MaterialIconSet CHROMATIC_GLASS = new MaterialIconSet("chromatic_glass",
            MaterialIconSet.SHINY);

    private GodforgeIconSets() {}
}
