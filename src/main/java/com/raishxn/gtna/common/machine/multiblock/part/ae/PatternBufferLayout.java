package com.raishxn.gtna.common.machine.multiblock.part.ae;

/**
 * Geometry of the pattern buffer page, in one place.
 *
 * <p>
 * The page is a single {@code 352 x 248} group split into two columns: the pattern grid on the left
 * and the per-slot configuration panel docked on the right. Docking is the fix for the bug that
 * motivated this class: the fancy UI sizes its frame from {@code page.getSize()} and LDLib does not
 * clip a page's children, so while the configuration panel was swapped on top of the pattern grid
 * at the same size its ~326 px of widgets were drawn ~106 px past the bottom edge and bled over the
 * frame — and over the player inventory slot rows below it.
 *
 * <p>
 * The plan is plain constants rather than locals inside the widget builder so
 * {@code PatternBufferLayoutTest} can assert that it still fits the page and that no two rows
 * overlap. Moving a row means keeping that test green, which is the point.
 */
final class PatternBufferLayout {

    private PatternBufferLayout() {}

    // ------------------------------------------------------------------------------------------
    // Page
    // ------------------------------------------------------------------------------------------

    static final int PAGE_WIDTH = 352;
    static final int PAGE_HEIGHT = 248;
    /** Breathing room kept under the lowest widget of each column. */
    static final int PAGE_BOTTOM_MARGIN = 4;

    // ------------------------------------------------------------------------------------------
    // Left column: pattern grid
    // ------------------------------------------------------------------------------------------

    static final int PATTERN_COLUMN_X = 0;
    static final int PATTERN_COLUMN_WIDTH = 176;
    static final int PATTERN_HEADER_Y = 4;
    static final int RENAME_FIELD_X = 96;
    static final int RENAME_FIELD_Y = 4;
    static final int RENAME_FIELD_WIDTH = 74;
    static final int RENAME_FIELD_HEIGHT = 10;
    static final int PATTERN_GRID_X = 8;
    static final int PATTERN_GRID_Y = 22;
    static final int PATTERN_COLUMNS = 9;
    /** Rows a full page shows; a partially filled page only draws what it has. */
    static final int PATTERN_ROWS = 6;
    static final int PATTERN_CELL = 18;
    static final int PATTERNS_PER_PAGE = PATTERN_COLUMNS * PATTERN_ROWS;
    /** Bottom edge of a page that is completely full of patterns. */
    static final int PATTERN_GRID_MAX_BOTTOM = PATTERN_GRID_Y + PATTERN_ROWS * PATTERN_CELL;

    static final int NAV_BUTTON_WIDTH = 28;
    static final int NAV_BUTTON_HEIGHT = 13;
    static final int NAV_LEFT_X = 5;
    static final int NAV_RIGHT_X = PATTERN_COLUMN_WIDTH - NAV_LEFT_X - NAV_BUTTON_WIDTH;
    /** Page navigation is anchored to the bottom of the page, under the grid. */
    static final int NAV_ROW_Y = PAGE_HEIGHT - 6 - NAV_BUTTON_HEIGHT;
    static final int PAGE_HINT_Y = NAV_ROW_Y - 12;

    // ------------------------------------------------------------------------------------------
    // Right column: docked per-slot configuration panel
    // ------------------------------------------------------------------------------------------

    static final int CONFIG_COLUMN_X = PATTERN_COLUMN_WIDTH;
    static final int CONFIG_COLUMN_WIDTH = PAGE_WIDTH - CONFIG_COLUMN_X;
    /** Inset wide enough to clear the 4 px frame of {@code GuiTextures.BACKGROUND_INVERSE}. */
    static final int CONFIG_INNER_X = 6;
    static final int CONFIG_INNER_WIDTH = CONFIG_COLUMN_WIDTH - 2 * CONFIG_INNER_X;
    static final int GHOST_SLOT = 18;
    static final int GHOST_ROW_WIDTH = PATTERN_COLUMNS * GHOST_SLOT;
    static final int GHOST_ROW_HEIGHT = 18;
    static final int LABEL_HEIGHT = 9;
    static final int BUTTON_HEIGHT = 14;
    static final int SMALL_BUTTON_HEIGHT = 13;

    static final int HEADER_Y = 4;
    static final int HEADER_HEIGHT = SMALL_BUTTON_HEIGHT;
    static final int CACHED_LABEL_Y = 20;
    static final int DERIVED_LABEL_Y = 31;
    static final int ITEM_LABEL_Y = 45;
    static final int ITEM_ROW_Y = 54;
    static final int FLUID_LABEL_Y = 76;
    static final int FLUID_ROW_Y = 85;
    static final int CATALYST_ITEM_LABEL_Y = 107;
    static final int CATALYST_ITEM_ROW_Y = 116;
    static final int CATALYST_FLUID_LABEL_Y = 138;
    static final int CATALYST_FLUID_ROW_Y = 147;
    static final int CIRCUIT_LABEL_Y = 169;
    static final int CIRCUIT_ROW_Y = 178;
    static final int CIRCUIT_ROW_HEIGHT = 18;
    static final int MODE_LABEL_Y = 200;
    static final int MODE_BUTTON_Y = 209;
    static final int CACHE_TOGGLE_Y = 227;

    /** Bottom edge of the configuration panel's content. */
    static final int CONFIG_CONTENT_BOTTOM = CACHE_TOGGLE_Y + BUTTON_HEIGHT;

    // ------------------------------------------------------------------------------------------
    // Buffer Tools page (its own side tab, so it sizes itself)
    // ------------------------------------------------------------------------------------------

    static final int TOOLS_PAGE_WIDTH = 176;
    static final int TOOLS_PAGE_HEIGHT = 104;
    static final int TOOLS_INNER_X = 6;
    static final int TOOLS_INNER_WIDTH = 164;
    static final int TOOLS_CACHE_LABEL_Y = 4;
    static final int TOOLS_CLEAR_PATTERN_BUTTON_Y = 16;
    static final int TOOLS_CLEAR_MACHINE_BUTTON_Y = 33;
    static final int TOOLS_CIRCUIT_LABEL_Y = 52;
    static final int TOOLS_CIRCUIT_INPUT_Y = 64;
    static final int TOOLS_SKIP_TOGGLE_X = 60;
    static final int TOOLS_SKIP_TOGGLE_WIDTH = 110;
    static final int TOOLS_ACTION_BUTTON_Y = 82;
    static final int TOOLS_ACTION_BUTTON_WIDTH = 80;
    static final int TOOLS_REMOVE_BUTTON_X = 90;
    static final int TOOLS_CONTENT_BOTTOM = TOOLS_ACTION_BUTTON_Y + SMALL_BUTTON_HEIGHT;
    static final int TOOLS_WIDE_BUTTON_WIDTH = TOOLS_INNER_WIDTH;

    /**
     * The vertical plan the validation below walks. Kept next to the constants it mirrors; if a
     * constant moves without its entry here, {@link #describeViolation()} notices the mismatch.
     */
    private static final String[] CONFIG_ROW_NAMES = {
            "header", "cached recipe label", "derived mode label",
            "ghost item label", "ghost item row",
            "ghost fluid label", "ghost fluid row",
            "catalyst item label", "catalyst item row",
            "catalyst fluid label", "catalyst fluid row",
            "circuit label", "circuit row",
            "mode label", "mode button",
            "recipe cache toggle" };

    private static final int[] CONFIG_ROW_Y = {
            HEADER_Y, CACHED_LABEL_Y, DERIVED_LABEL_Y,
            ITEM_LABEL_Y, ITEM_ROW_Y,
            FLUID_LABEL_Y, FLUID_ROW_Y,
            CATALYST_ITEM_LABEL_Y, CATALYST_ITEM_ROW_Y,
            CATALYST_FLUID_LABEL_Y, CATALYST_FLUID_ROW_Y,
            CIRCUIT_LABEL_Y, CIRCUIT_ROW_Y,
            MODE_LABEL_Y, MODE_BUTTON_Y,
            CACHE_TOGGLE_Y };

    private static final int[] CONFIG_ROW_HEIGHT = {
            HEADER_HEIGHT, LABEL_HEIGHT, LABEL_HEIGHT,
            LABEL_HEIGHT, GHOST_ROW_HEIGHT,
            LABEL_HEIGHT, GHOST_ROW_HEIGHT,
            LABEL_HEIGHT, GHOST_ROW_HEIGHT,
            LABEL_HEIGHT, GHOST_ROW_HEIGHT,
            LABEL_HEIGHT, CIRCUIT_ROW_HEIGHT,
            LABEL_HEIGHT, BUTTON_HEIGHT,
            BUTTON_HEIGHT };

    /**
     * @return an empty string when the plan fits the page, otherwise the first problem found, in
     *         prose. The unit test turns this into an assertion; production code never calls it.
     */
    static String describeViolation() {
        if (PATTERN_COLUMN_WIDTH + CONFIG_COLUMN_WIDTH != PAGE_WIDTH) {
            return "columns do not add up to the page width";
        }
        if (PATTERN_GRID_X + GHOST_ROW_WIDTH + PAGE_BOTTOM_MARGIN > PATTERN_COLUMN_WIDTH) {
            return "the pattern grid is wider than its column";
        }
        if (RENAME_FIELD_X + RENAME_FIELD_WIDTH + PAGE_BOTTOM_MARGIN > PATTERN_COLUMN_WIDTH) {
            return "the rename field runs past the right edge of the pattern column";
        }
        if (NAV_LEFT_X + NAV_BUTTON_WIDTH > NAV_RIGHT_X) {
            return "the page navigation buttons overlap";
        }
        if (NAV_RIGHT_X + NAV_BUTTON_WIDTH + PAGE_BOTTOM_MARGIN > PATTERN_COLUMN_WIDTH) {
            return "the page navigation buttons run past the right edge of the pattern column";
        }
        if (CONFIG_INNER_X + GHOST_ROW_WIDTH > CONFIG_INNER_X + CONFIG_INNER_WIDTH) {
            return "the config ghost row is wider than the config panel's inner width";
        }
        if (CONFIG_INNER_X + GHOST_ROW_WIDTH + CONFIG_INNER_X > CONFIG_COLUMN_WIDTH) {
            return "the config ghost row is wider than its column";
        }
        if (PATTERN_GRID_MAX_BOTTOM + PAGE_BOTTOM_MARGIN > PAGE_HEIGHT) {
            return "a full pattern page runs past the bottom of the page";
        }
        if (PATTERN_GRID_MAX_BOTTOM > PAGE_HINT_Y) {
            return "a full pattern page overlaps the left column footer";
        }
        if (NAV_ROW_Y + NAV_BUTTON_HEIGHT + PAGE_BOTTOM_MARGIN > PAGE_HEIGHT) {
            return "the page navigation row runs past the bottom of the page";
        }
        if (PAGE_HINT_Y + LABEL_HEIGHT + PAGE_BOTTOM_MARGIN > PAGE_HEIGHT) {
            return "the left column footer runs past the bottom of the page";
        }
        if (PAGE_HINT_Y + LABEL_HEIGHT > NAV_ROW_Y) {
            return "the page hint overlaps the navigation row";
        }
        int previousBottom = 0;
        int planBottom = 0;
        for (int i = 0; i < CONFIG_ROW_Y.length; i++) {
            String name = CONFIG_ROW_NAMES[i];
            int y = CONFIG_ROW_Y[i];
            int height = CONFIG_ROW_HEIGHT[i];
            if (y < previousBottom) {
                return "config row '" + name + "' starts at " + y + " but the row above ends at " + previousBottom;
            }
            previousBottom = y + height;
            planBottom = Math.max(planBottom, previousBottom);
        }
        if (planBottom != CONFIG_CONTENT_BOTTOM) {
            return "CONFIG_CONTENT_BOTTOM is " + CONFIG_CONTENT_BOTTOM + " but the plan ends at " + planBottom;
        }
        if (planBottom + PAGE_BOTTOM_MARGIN > PAGE_HEIGHT) {
            return "the config panel content runs past the bottom of the page";
        }
        if (TOOLS_INNER_X + TOOLS_WIDE_BUTTON_WIDTH + TOOLS_INNER_X != TOOLS_PAGE_WIDTH) {
            return "the tools inner width and its margins do not fill the tools page";
        }
        if (TOOLS_SKIP_TOGGLE_X + TOOLS_SKIP_TOGGLE_WIDTH + TOOLS_INNER_X > TOOLS_PAGE_WIDTH) {
            return "the tools skip-existing toggle runs past the tools page";
        }
        if (TOOLS_REMOVE_BUTTON_X + TOOLS_ACTION_BUTTON_WIDTH != TOOLS_INNER_X + TOOLS_INNER_WIDTH) {
            return "the tools action buttons do not fill the inner width";
        }
        if (TOOLS_CONTENT_BOTTOM + PAGE_BOTTOM_MARGIN > TOOLS_PAGE_HEIGHT) {
            return "the tools page content runs past its own bottom edge";
        }
        return "";
    }
}
