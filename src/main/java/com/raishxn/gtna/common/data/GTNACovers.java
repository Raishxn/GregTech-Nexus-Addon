package com.raishxn.gtna.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.client.renderer.cover.IOCoverRenderer;
import com.gregtechceu.gtceu.common.cover.ConveyorCover;
import com.gregtechceu.gtceu.common.cover.FluidRegulatorCover;
import com.gregtechceu.gtceu.common.cover.PumpCover;
import com.gregtechceu.gtceu.common.cover.RobotArmCover;
import com.gregtechceu.gtceu.common.data.GTCovers;
import com.raishxn.gtna.GTNACORE; // Ajuste para sua classe Main
import net.minecraft.resources.ResourceLocation;

public class GTNACovers {

    public static final CoverDefinition HYDRAULIC_PUMP = GTCovers.register(
            GTNACORE.id("hydraulic_pump"),
            (def, coverable, side) -> new PumpCover(def, coverable, side, GTValues.VC_LP_STEAM),
            () -> () -> IOCoverRenderer.PUMP_LIKE_COVER_RENDERER);

    public static final CoverDefinition HYDRAULIC_CONVEYOR = GTCovers.register(
            GTNACORE.id("hydraulic_conveyor"),
            (def, coverable, side) -> new ConveyorCover(def, coverable, side, GTValues.VC_LP_STEAM) {
                @Override
                public int getTransferRate() {
                    return 1024;
                }
            },
            () -> () -> new IOCoverRenderer(
                    GTCEu.id("block/cover/conveyor"),
                    null,
                    GTCEu.id("block/cover/conveyor_emissive"),
                    GTCEu.id("block/cover/conveyor_inverted_emissive")));

    public static final CoverDefinition HYDRAULIC_REGULATOR = GTCovers.register(
            GTNACORE.id("hydraulic_regulator"),
            (def, coverable, side) -> new FluidRegulatorCover(def, coverable, side, GTValues.VC_LP_STEAM),
            () -> () -> IOCoverRenderer.PUMP_LIKE_COVER_RENDERER);

    public static final CoverDefinition HYDRAULIC_ARM = GTCovers.register(
            GTNACORE.id("hydraulic_arm"),
            (def, coverable, side) -> new RobotArmCover(def, coverable, side, GTValues.VC_LP_STEAM) {
                @Override
                public int getTransferRate() {
                    return 64;
                }
            },
            () -> () -> new IOCoverRenderer(
                    GTCEu.id("block/cover/arm"),
                    null,
                    GTCEu.id("block/cover/arm_emissive"),
                    GTCEu.id("block/cover/arm_inverted_emissive")));

    public static void init() {
    }
}