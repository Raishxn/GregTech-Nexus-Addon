package com.raishxn.gtna.common.machine.multiblock.part.ae;

/**
 * Regression guard for the pattern buffer page geometry.
 *
 * <p>
 * Why this exists: the per-slot configuration panel used to be swapped on top of the pattern grid at
 * the same {@code 176 x 220} page size while its content added up to 326 px, so 106 px of widgets
 * were drawn outside the page — and, because LDLib does not clip a page's children, straight over
 * the player inventory below it. The panel is now docked in its own column and the page is sized from
 * the plan in {@link PatternBufferLayout}, but nothing at runtime would notice a row creeping past
 * the edge again, and neither {@code runUnitTests}' gametests nor a dedicated-server gametest can see
 * a client-side overflow. This can.
 *
 * <p>
 * GTLCore-style: {@code main()} plus asserts, no JUnit, no Minecraft on the classpath.
 */
public final class PatternBufferLayoutTest {

    /** Frame border the fancy UI adds around the page, on both sides. */
    private static final int BORDER = 4;
    /** {@code PlayerInventoryWidget} height the fancy UI adds under the page. */
    private static final int PLAYER_INVENTORY_HEIGHT = 86;
    /** Logical screen height at GUI scale 3 on a 1080p display: the smallest target we support. */
    private static final int LOGICAL_SCREEN_HEIGHT = 1080 / 3;

    private PatternBufferLayoutTest() {}

    public static void main(String[] args) {
        checkPlanFits();
        checkColumnArithmetic();
        checkGhostRowsMatchThePatternGrid();
        checkFitsTheSmallestSupportedScreen();
        System.out.println("[PatternBufferLayoutTest] all cases passed");
    }

    /** The 326-px-panel bug: content must stay inside the page it is drawn on. */
    private static void checkPlanFits() {
        String violation = PatternBufferLayout.describeViolation();
        check(violation.isEmpty(), "pattern buffer layout does not fit: " + violation);
    }

    private static void checkColumnArithmetic() {
        int columns = PatternBufferLayout.PATTERN_COLUMN_WIDTH + PatternBufferLayout.CONFIG_COLUMN_WIDTH;
        check(columns == PatternBufferLayout.PAGE_WIDTH,
                "the two columns add up to " + columns + " but the page is " + PatternBufferLayout.PAGE_WIDTH);

        int innerRight = PatternBufferLayout.CONFIG_INNER_X + PatternBufferLayout.CONFIG_INNER_WIDTH +
                PatternBufferLayout.CONFIG_INNER_X;
        check(innerRight == PatternBufferLayout.CONFIG_COLUMN_WIDTH,
                "the config inner width plus its margins is " + innerRight + " but its column is " +
                        PatternBufferLayout.CONFIG_COLUMN_WIDTH);

        check(PatternBufferLayout.CONFIG_CONTENT_BOTTOM + PatternBufferLayout.PAGE_BOTTOM_MARGIN <=
                PatternBufferLayout.PAGE_HEIGHT,
                "config content ends at " + PatternBufferLayout.CONFIG_CONTENT_BOTTOM +
                        " with no room left in a " + PatternBufferLayout.PAGE_HEIGHT + " px page");

        check(PatternBufferLayout.PATTERN_GRID_MAX_BOTTOM < PatternBufferLayout.PAGE_HINT_Y,
                "a full pattern grid ends at " + PatternBufferLayout.PATTERN_GRID_MAX_BOTTOM +
                        " and overlaps the footer at " + PatternBufferLayout.PAGE_HINT_Y);
    }

    /** The ghost rows reuse the pattern grid's nine columns, so both must agree on the cell size. */
    private static void checkGhostRowsMatchThePatternGrid() {
        check(PatternBufferLayout.GHOST_ROW_WIDTH ==
                PatternBufferLayout.PATTERN_COLUMNS * PatternBufferLayout.PATTERN_CELL,
                "the ghost rows are " + PatternBufferLayout.GHOST_ROW_WIDTH +
                        " px wide but the pattern grid is " +
                        PatternBufferLayout.PATTERN_COLUMNS * PatternBufferLayout.PATTERN_CELL + " px");
        check(PatternBufferLayout.GHOST_ROW_WIDTH <= PatternBufferLayout.CONFIG_INNER_WIDTH,
                "the ghost rows do not fit the config panel's inner width");
    }

    /**
     * The page plus the fancy frame plus the player inventory has to stay on screen. This is why the
     * cache maintenance and circuit tooling moved out of the per-slot panel into a side tab.
     */
    private static void checkFitsTheSmallestSupportedScreen() {
        int total = PatternBufferLayout.PAGE_HEIGHT + 2 * BORDER + PLAYER_INVENTORY_HEIGHT;
        check(total <= LOGICAL_SCREEN_HEIGHT,
                "the buffer GUI is " + total + " px tall, which does not fit the " + LOGICAL_SCREEN_HEIGHT +
                        " logical px of a 1080p screen at GUI scale 3");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("[PatternBufferLayoutTest] " + message);
        }
    }
}
