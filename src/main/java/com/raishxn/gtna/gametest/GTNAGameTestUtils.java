package com.raishxn.gtna.gametest;

import net.minecraft.gametest.framework.GameTestHelper;

/**
 * Small helpers for the GameTest harness.
 *
 * <p>
 * Inspired by GTNH's Horizon-QA discipline ({@code onEachTick} + {@code succeedAtTimeout}): re-check
 * an invariant every tick over the whole test window so a <b>transient</b> violation (a machine that
 * forms and unforms, a state that flickers) fails on the exact tick it happens.
 */
public final class GTNAGameTestUtils {

    private GTNAGameTestUtils() {}

    /**
     * Runs {@code assertion} every tick and only marks the test passed near the end of the window, so
     * any violation in between fails the test on that tick. The owning {@code @GameTest} must use a
     * {@code timeoutTicks} greater than {@code timeoutTicks} passed here.
     */
    public static void assertEveryTickUntilTimeout(GameTestHelper helper, int timeoutTicks, String name,
                                                   Runnable assertion) {
        helper.onEachTick(() -> {
            try {
                assertion.run();
            } catch (Throwable failure) {
                helper.fail(name + ": " + failure.getMessage());
            }
        });
        helper.runAtTickTime(Math.max(1, timeoutTicks - 1), helper::succeed);
    }
}
