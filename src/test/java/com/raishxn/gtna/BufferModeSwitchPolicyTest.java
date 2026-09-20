package com.raishxn.gtna;

import com.raishxn.gtna.api.machine.feature.BufferModeSwitchPolicy;

/**
 * Locks the rule that lets a pattern buffer drive the machine mode of GTCEu multiblocks: only while
 * the machine is idle, only for a type the machine actually offers, and never as a no-op switch.
 *
 * <p>
 * Exercises the production class directly (plain ints in, int out), so no mirrored copy has to be
 * kept in sync. GTLCore-style: {@code main()} + asserts, no JUnit.
 */
public final class BufferModeSwitchPolicyTest {

    private BufferModeSwitchPolicyTest() {}

    public static void main(String[] args) {
        switchesWhenIdle();
        neverInterruptsWork();
        ignoresUselessRequests();
        System.out.println("[BufferModeSwitchPolicyTest] all cases passed");
    }

    private static void switchesWhenIdle() {
        check(1, BufferModeSwitchPolicy.selectTargetIndex(true, 1, 0), "idle machine follows the buffer");
        check(2, BufferModeSwitchPolicy.selectTargetIndex(true, 2, 0), "any offered index can be the target");
        check(0, BufferModeSwitchPolicy.selectTargetIndex(true, 0, 2), "switching back is allowed too");
    }

    /** The whole point of an idle-only policy: an occupied machine is never touched. */
    private static void neverInterruptsWork() {
        check(BufferModeSwitchPolicy.KEEP_CURRENT, BufferModeSwitchPolicy.selectTargetIndex(false, 1, 0),
                "working/waiting machine keeps its mode");
        check(BufferModeSwitchPolicy.KEEP_CURRENT, BufferModeSwitchPolicy.selectTargetIndex(false, 0, 1),
                "a busy machine is not even switched back");
    }

    private static void ignoresUselessRequests() {
        check(BufferModeSwitchPolicy.KEEP_CURRENT, BufferModeSwitchPolicy.selectTargetIndex(true, -1, 0),
                "no pending request (or a type the machine lacks) changes nothing");
        check(BufferModeSwitchPolicy.KEEP_CURRENT, BufferModeSwitchPolicy.selectTargetIndex(true, 1, 1),
                "an already-active mode is not re-applied");
    }

    private static void check(int expected, int actual, String name) {
        if (expected != actual) {
            throw new AssertionError(name + ": expected " + expected + " but got " + actual);
        }
    }
}
