package com.raishxn.gtna.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.client.renderer.cover.IOCoverRenderer;
import com.gregtechceu.gtceu.client.renderer.cover.SimpleCoverRenderer;
import com.gregtechceu.gtceu.common.cover.ConveyorCover;
import com.gregtechceu.gtceu.common.cover.FluidRegulatorCover;
import com.gregtechceu.gtceu.common.cover.PumpCover;
import com.gregtechceu.gtceu.common.cover.RobotArmCover;
import com.gregtechceu.gtceu.common.data.GTCovers;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.common.cover.InfiniteElectricSingleblockCover;
import com.raishxn.gtna.common.cover.InfiniteSteamSingleblockCover;
import com.raishxn.gtna.common.cover.WirelessEUReceiverCover;
import com.raishxn.gtna.common.cover.WirelessEUTransmitterCover;

import java.util.Locale;

public class GTNACovers {

    public static final CoverDefinition HYDRAULIC_PUMP = GTCovers.register(
            GTNACORE.id("hydraulic_pump"),
            (def, coverable, side) -> new PumpCover(def, coverable, side, GTValues.VC_LP_STEAM),
            () -> () -> IOCoverRenderer.PUMP_LIKE_COVER_RENDERER);

    public static final CoverDefinition HYDRAULIC_CONVEYOR = GTCovers.register(
            GTNACORE.id("hydraulic_conveyor"),
            (def, coverable, side) -> new ConveyorCover(def, coverable, side, GTValues.VC_LP_STEAM) {
                @Override public int getTransferRate() { return 1024; }
            },
            () -> () -> new IOCoverRenderer(
                    GTCEu.id("block/cover/conveyor"), null,
                    GTCEu.id("block/cover/conveyor_emissive"),
                    GTCEu.id("block/cover/conveyor_inverted_emissive")));

    public static final CoverDefinition HYDRAULIC_REGULATOR = GTCovers.register(
            GTNACORE.id("hydraulic_regulator"),
            (def, coverable, side) -> new FluidRegulatorCover(def, coverable, side, GTValues.VC_LP_STEAM),
            () -> () -> IOCoverRenderer.PUMP_LIKE_COVER_RENDERER);

    public static final CoverDefinition HYDRAULIC_ARM = GTCovers.register(
            GTNACORE.id("hydraulic_arm"),
            (def, coverable, side) -> new RobotArmCover(def, coverable, side, GTValues.VC_LP_STEAM) {
                @Override public int getTransferRate() { return 64; }
            },
            () -> () -> new IOCoverRenderer(
                    GTCEu.id("block/cover/arm"), null,
                    GTCEu.id("block/cover/arm_emissive"),
                    GTCEu.id("block/cover/arm_inverted_emissive")));

    public static final CoverDefinition INFINITE_STEAM_SINGLEBLOCK_COVER = GTCovers.register(
            GTNACORE.id("infinite_steam_singleblock_cover"),
            InfiniteSteamSingleblockCover::new,
            () -> () -> IOCoverRenderer.PUMP_LIKE_COVER_RENDERER);

    public static final CoverDefinition INFINITE_ELECTRIC_SINGLEBLOCK_COVER = GTCovers.register(
            GTNACORE.id("infinite_electric_singleblock_cover"),
            InfiniteElectricSingleblockCover::new,
            () -> () -> new IOCoverRenderer(
                    GTCEu.id("block/cover/arm"), null,
                    GTCEu.id("block/cover/arm_emissive"),
                    GTCEu.id("block/cover/arm_inverted_emissive")));

    // ── Wireless EU cover arrays [tier][ampIndex] ─────────────────────────────
    // ampIndex: 0=1A  1=4A  2=16A  3=64A
    public static final CoverDefinition[][] WIRELESS_EU_RECEIVER_COVERS =
            new CoverDefinition[GTValues.MAX + 1][4];
    public static final CoverDefinition[][] WIRELESS_EU_TRANSMITTER_COVERS =
            new CoverDefinition[GTValues.MAX + 1][4];

    private static final int[]    AMP_VALUES  = { 1,  4,    16,    64    };
    private static final String[] AMP_TAGS    = { "1a", "4a", "16a", "64a" };
    private static final String[] AMP_OVERLAY = { "",   "_4a", "_16a", "_64a" };

    private static void registerWirelessEUCovers() {
        int[] tiers = GTValues.tiersBetween(GTValues.LV,
                GTCEuAPI.isHighTier() ? GTValues.MAX : GTValues.UV);

        for (int tier : tiers) {
            String tierLower = GTValues.VN[tier].toLowerCase(Locale.ROOT);
            for (int ai = 0; ai < AMP_VALUES.length; ai++) {
                final int amp = AMP_VALUES[ai];
                final int idx = ai;
                final String tag  = AMP_TAGS[ai];
                final String over = AMP_OVERLAY[ai];
                final int    t    = tier;

                String rcvId = "wireless_eu_receiver_"    + tag + "_" + tierLower;
                String txId  = "wireless_eu_transmitter_" + tag + "_" + tierLower;

                CoverDefinition rcv = GTCovers.register(
                        GTNACORE.id(rcvId),
                        (def, cov, side) -> new WirelessEUReceiverCover(def, cov, side, t, amp),
                        () -> () -> new SimpleCoverRenderer(
                                GTNACORE.id("block/cover/overlay_wireless_eu_receiver" + over)));
                WIRELESS_EU_RECEIVER_COVERS[tier][idx] = rcv;

                CoverDefinition tx = GTCovers.register(
                        GTNACORE.id(txId),
                        (def, cov, side) -> new WirelessEUTransmitterCover(def, cov, side, t, amp),
                        () -> () -> new SimpleCoverRenderer(
                                GTNACORE.id("block/cover/overlay_wireless_eu_transmitter" + over)));
                WIRELESS_EU_TRANSMITTER_COVERS[tier][idx] = tx;
            }
        }
    }

    public static void init() {
        registerWirelessEUCovers();
    }
}
