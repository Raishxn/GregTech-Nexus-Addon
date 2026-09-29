package com.raishxn.gtna.common.data;

/**
 * Recipe data keys consumed by GTNA machines. GTCEu 7.5.3 has no shared {@code GTRecipeDataKeys}
 * holder, so the ported GTOCore keys live here; the string value is what ends up in the saved
 * recipe data, so it must match the original.
 */
public final class GTNARecipeDataKeys {

    /**
     * GTOCore ISA Mill grinding-ball tier: {@code 1} = Soapstone ball, {@code 2} = Aluminium ball.
     * Compared against {@code BallHatchPartMachine.GRINDBALL}.
     */
    public static final String GRINDBALL = "grindball";

    private GTNARecipeDataKeys() {}
}
