package com.raishxn.gtna.client.hud;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Client-side mirror of the local player's wireless steam network, fed by
 * {@code SWirelessSteamStats} once per second.
 *
 * <p>
 * The balance history is kept here (not synced) as a ring buffer, so the HUD graph only needs the
 * latest numbers from the server. Everything runs on the client main thread (packets use
 * {@code consumerMainThread} and rendering happens on the same thread), so no synchronization is
 * needed.
 */
@OnlyIn(Dist.CLIENT)
public final class WirelessSteamHudState {

    /** Hard cap for the graph, independent of the config (600 s at one sample per second). */
    public static final int MAX_HISTORY = 600;

    private static final long[] BALANCE_HISTORY = new long[MAX_HISTORY];
    private static int historyHead;
    private static int historySize;

    private static boolean hasData;
    private static long balance;
    private static long addedPerSecond;
    private static long consumedPerSecond;
    private static int inputHatches;
    private static int outputHatches;

    private WirelessSteamHudState() {}

    /** Called on the client main thread when a snapshot packet arrives. */
    public static void update(long balance, long addedPerSecond, long consumedPerSecond,
                              int inputHatches, int outputHatches) {
        WirelessSteamHudState.balance = balance;
        WirelessSteamHudState.addedPerSecond = addedPerSecond;
        WirelessSteamHudState.consumedPerSecond = consumedPerSecond;
        WirelessSteamHudState.inputHatches = inputHatches;
        WirelessSteamHudState.outputHatches = outputHatches;
        WirelessSteamHudState.hasData = true;

        BALANCE_HISTORY[historyHead] = balance;
        historyHead = (historyHead + 1) % MAX_HISTORY;
        if (historySize < MAX_HISTORY) {
            historySize++;
        }
    }

    /** Clears everything on disconnect so a later world never shows the previous world's numbers. */
    public static void reset() {
        hasData = false;
        balance = 0L;
        addedPerSecond = 0L;
        consumedPerSecond = 0L;
        inputHatches = 0;
        outputHatches = 0;
        historyHead = 0;
        historySize = 0;
    }

    public static boolean hasData() {
        return hasData;
    }

    /** Whether there is anything worth showing: a connected hatch, a balance or recent flow. */
    public static boolean hasNetwork() {
        return inputHatches + outputHatches > 0 || balance > 0L || addedPerSecond > 0L || consumedPerSecond > 0L;
    }

    public static long getBalance() {
        return balance;
    }

    public static long getAddedPerSecond() {
        return addedPerSecond;
    }

    public static long getConsumedPerSecond() {
        return consumedPerSecond;
    }

    public static int getInputHatches() {
        return inputHatches;
    }

    public static int getOutputHatches() {
        return outputHatches;
    }

    /**
     * The most recent balance samples in chronological order, at most {@code maxSamples} of them.
     * An empty array means there is not enough history to draw anything yet.
     */
    public static long[] history(int maxSamples) {
        int count = Math.min(Math.min(maxSamples, historySize), MAX_HISTORY);
        long[] samples = new long[count];
        for (int i = 0; i < count; i++) {
            int index = (historyHead - count + i + MAX_HISTORY * 2) % MAX_HISTORY;
            samples[i] = BALANCE_HISTORY[index];
        }
        return samples;
    }
}
