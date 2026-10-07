package com.raishxn.gtna.api.machine.feature.godforge;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;

/**
 * The 31 Forge of Gods upgrades, with the prerequisites, shard costs, split rule and tree layout of GTNH
 * {@code ForgeOfGodsUpgrade} (GT5-Unofficial a3e1e112). Extra item costs are attached by the recipe loader.
 */
public enum GodforgeUpgrade {

    START,
    IGCC,
    STEM,
    CFCE,
    GISS,
    FDIM,
    SA,
    GPCI,
    REC,
    GEM,
    CTCDD,
    QGPIU,
    SEFCP,
    TCT,
    GGEBE,
    TPTP,
    DOP,
    CNTI,
    EPEC,
    IMKG,
    NDPE,
    POS,
    DOR,
    NGMS,
    SEDS,
    PA,
    CD,
    TSE,
    TBF,
    EE,
    END;

    /** Background color of a node in the upgrade tree. */
    public enum Color {
        BLUE,
        RED,
        PURPLE,
        ORANGE,
        GREEN
    }

    /** Milestone symbol drawn on a node. */
    public enum Symbol {
        CHARGE,
        CONVERSION,
        CATALYST,
        COMPOSITION
    }

    /** One extra item cost: item registry ID and amount (at most 12 per upgrade). */
    public record ExtraCost(String item, int amount) {}

    public static final GodforgeUpgrade[] VALUES = values();
    /** At most one of these per built ring may be active. */
    public static final Set<GodforgeUpgrade> SPLIT_UPGRADES;

    private GodforgeUpgrade[] prerequisites = new GodforgeUpgrade[0];
    private GodforgeUpgrade[] dependents = new GodforgeUpgrade[0];
    private boolean requireAllPrerequisites;
    private int shardCost;
    private Color color = Color.BLUE;
    private Symbol symbol = Symbol.COMPOSITION;
    private boolean largePanel;
    private int treeX, treeY;
    private final List<ExtraCost> extraCost = new ArrayList<>();

    static {
        // spotless:off
        START.set(0, false, Color.BLUE, Symbol.COMPOSITION, 126, 56).largePanel = true;
        IGCC.set(1, false, Color.BLUE, Symbol.CONVERSION, 126, 116, START);
        STEM.set(1, false, Color.BLUE, Symbol.CATALYST, 96, 176, IGCC);
        CFCE.set(1, false, Color.BLUE, Symbol.CATALYST, 156, 176, IGCC);
        GISS.set(1, false, Color.BLUE, Symbol.CHARGE, 66, 236, STEM);
        FDIM.set(1, false, Color.BLUE, Symbol.COMPOSITION, 126, 236, STEM, CFCE);
        SA.set(1, false, Color.BLUE, Symbol.CONVERSION, 186, 236, CFCE);
        GPCI.set(2, false, Color.BLUE, Symbol.COMPOSITION, 126, 296, FDIM);
        REC.set(2, true, Color.RED, Symbol.CHARGE, 56, 356, GISS, GPCI);
        GEM.set(2, false, Color.BLUE, Symbol.CATALYST, 126, 356, GPCI);
        CTCDD.set(2, true, Color.RED, Symbol.CONVERSION, 196, 356, GPCI, SA);
        QGPIU.set(2, false, Color.BLUE, Symbol.CATALYST, 126, 416, REC, CTCDD);
        SEFCP.set(3, false, Color.PURPLE, Symbol.CATALYST, 66, 476, QGPIU);
        TCT.set(3, false, Color.ORANGE, Symbol.CONVERSION, 126, 476, QGPIU);
        GGEBE.set(3, false, Color.GREEN, Symbol.CHARGE, 186, 476, QGPIU);
        TPTP.set(4, false, Color.GREEN, Symbol.CONVERSION, 246, 496, GGEBE);
        DOP.set(4, false, Color.PURPLE, Symbol.CONVERSION, 6, 556, CNTI);
        CNTI.set(3, false, Color.PURPLE, Symbol.CHARGE, 66, 536, SEFCP);
        EPEC.set(3, false, Color.ORANGE, Symbol.CONVERSION, 126, 536, TCT);
        IMKG.set(3, false, Color.GREEN, Symbol.CHARGE, 186, 536, GGEBE);
        NDPE.set(3, false, Color.PURPLE, Symbol.CHARGE, 66, 596, CNTI);
        POS.set(3, false, Color.ORANGE, Symbol.CONVERSION, 126, 596, EPEC);
        DOR.set(3, false, Color.GREEN, Symbol.CONVERSION, 186, 596, IMKG);
        NGMS.set(4, false, Color.BLUE, Symbol.CHARGE, 126, 656, NDPE, POS, DOR);
        SEDS.set(5, false, Color.BLUE, Symbol.CONVERSION, 126, 718, NGMS);
        PA.set(6, false, Color.BLUE, Symbol.CONVERSION, 36, 758, SEDS);
        CD.set(7, false, Color.BLUE, Symbol.COMPOSITION, 36, 848, PA);
        TSE.set(8, false, Color.BLUE, Symbol.CATALYST, 126, 888, CD);
        TBF.set(9, false, Color.BLUE, Symbol.CHARGE, 216, 848, TSE);
        EE.set(10, false, Color.BLUE, Symbol.CONVERSION, 216, 758, TBF);
        END.set(12, false, Color.BLUE, Symbol.COMPOSITION, 126, 798, EE).largePanel = true;
        // spotless:on
        SPLIT_UPGRADES = Set.of(SEFCP, TCT, GGEBE);

        EnumMap<GodforgeUpgrade, List<GodforgeUpgrade>> dependents = new EnumMap<>(GodforgeUpgrade.class);
        for (GodforgeUpgrade upgrade : VALUES) {
            for (GodforgeUpgrade prerequisite : upgrade.prerequisites) {
                dependents.computeIfAbsent(prerequisite, k -> new ArrayList<>()).add(upgrade);
            }
        }
        dependents.forEach((upgrade, list) -> upgrade.dependents = list.toArray(new GodforgeUpgrade[0]));
    }

    private GodforgeUpgrade set(int cost, boolean requireAll, Color color, Symbol symbol, int x, int y,
                                GodforgeUpgrade... prerequisites) {
        this.shardCost = cost;
        this.requireAllPrerequisites = requireAll;
        this.color = color;
        this.symbol = symbol;
        this.treeX = x;
        this.treeY = y;
        this.prerequisites = prerequisites;
        return this;
    }

    public void addExtraCost(ExtraCost... costs) {
        if (extraCost.size() + costs.length > 12) {
            throw new IllegalArgumentException("Too many inputs for Godforge upgrade cost, cannot be more than 12!");
        }
        extraCost.addAll(List.of(costs));
    }

    public void clearExtraCost() {
        extraCost.clear();
    }

    public GodforgeUpgrade[] prerequisites() {
        return prerequisites;
    }

    public GodforgeUpgrade[] dependents() {
        return dependents;
    }

    public boolean requiresAllPrerequisites() {
        return requireAllPrerequisites;
    }

    public int shardCost() {
        return shardCost;
    }

    public boolean hasExtraCost() {
        return !extraCost.isEmpty();
    }

    public List<ExtraCost> extraCost() {
        return List.copyOf(extraCost);
    }

    public Color color() {
        return color;
    }

    public Symbol symbol() {
        return symbol;
    }

    public boolean largePanel() {
        return largePanel;
    }

    public int treeX() {
        return treeX;
    }

    public int treeY() {
        return treeY;
    }

    /** Translation keys follow the GTNH {@code fog.upgrade.*.<ordinal>} (renamed to gtna.godforge.upgrade) scheme. */
    public String nameKey() {
        return "gtna.godforge.upgrade.tt." + ordinal();
    }

    public String shortNameKey() {
        return "gtna.godforge.upgrade.tt.short." + ordinal();
    }

    public String bodyKey() {
        return "gtna.godforge.upgrade.text." + ordinal();
    }

    public String loreKey() {
        return "gtna.godforge.upgrade.lore." + ordinal();
    }
}
