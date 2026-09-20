package com.raishxn.gtna.api.machine.feature;

/**
 * Policy deciding whether a pattern buffer may change a controller's machine mode.
 *
 * <p>
 * Chosen policy: the buffer only drives the mode while the machine is <b>idle</b>. An idle machine
 * has, by definition, not found a recipe in its current mode (otherwise it would be WORKING) and is
 * not mid-recipe in WAITING — so "the current mode wins whenever it has work" falls out for free,
 * and a switch can never interrupt production nor fight a recipe in progress.
 *
 * <p>
 * The explicit manual control stays in the buffer itself: the per-slot preferred mode and the
 * buffer-level filter. There is deliberately no second override flag on the machine's mode tab,
 * because that would create two competing override layers the player cannot reason about.
 *
 * <p>
 * Keeping this rule pure is what makes it unit-testable without a Minecraft bootstrap; resolving a
 * mode id to an index is the caller's job (via {@link ModeIdMatcher}).
 */
public final class BufferModeSwitchPolicy {

    /** Returned when the machine must keep its current mode. */
    public static final int KEEP_CURRENT = -1;

    private BufferModeSwitchPolicy() {}

    /**
     * @param logicIdle    whether the controller's recipe logic is currently {@code IDLE}
     * @param pendingIndex index of the recipe type the buffer asks for, or {@code < 0} when the
     *                     buffer has no request or the machine does not offer that type
     * @param currentIndex the machine's current {@code activeRecipeType}
     * @return the index to switch to, or {@link #KEEP_CURRENT}
     */
    public static int selectTargetIndex(boolean logicIdle, int pendingIndex, int currentIndex) {
        if (!logicIdle || pendingIndex < 0 || pendingIndex == currentIndex) {
            return KEEP_CURRENT;
        }
        return pendingIndex;
    }
}
