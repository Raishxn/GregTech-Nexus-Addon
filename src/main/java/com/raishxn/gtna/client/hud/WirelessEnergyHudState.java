package com.raishxn.gtna.client.hud;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class WirelessEnergyHudState {

    private static String balance = "0";
    private static String capacity = "0";
    private static String input = "0";
    private static String output = "0";
    private static int connections;
    private static boolean hasData;

    private WirelessEnergyHudState() {}

    public static void update(String balance, String capacity, String input, String output, int connections) {
        WirelessEnergyHudState.balance = balance;
        WirelessEnergyHudState.capacity = capacity;
        WirelessEnergyHudState.input = input;
        WirelessEnergyHudState.output = output;
        WirelessEnergyHudState.connections = connections;
        hasData = true;
    }

    public static void reset() {
        update("0", "0", "0", "0", 0);
        hasData = false;
    }

    public static boolean hasNetwork() {
        return hasData && (connections > 0 || !"0".equals(balance));
    }

    public static String balance() {
        return balance;
    }

    public static String capacity() {
        return capacity;
    }

    public static String input() {
        return input;
    }

    public static String output() {
        return output;
    }

    public static int connections() {
        return connections;
    }
}
