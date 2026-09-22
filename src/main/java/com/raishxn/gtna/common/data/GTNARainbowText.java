package com.raishxn.gtna.common.data;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.utils.GradientUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

/**
 * GTNL-style animated per-letter rainbow text.
 *
 * <p>
 * GTCEu's {@code TooltipHelper.RAINBOW_HSL_SLOW} colours a whole component with a single hue that
 * changes over time; GTNL instead walks a moving rainbow gradient across the individual letters
 * (each character a slightly different colour, animated). This helper reproduces the per-letter
 * effect using GTCEu's {@link GradientUtil} and {@code GTValues.CLIENT_TIME}.
 *
 * <p>
 * Client-only in practice: {@code GTValues.CLIENT_TIME} only advances on the client, so callers
 * should guard with {@code FMLEnvironment.dist.isClient()} to avoid a static colour on the server.
 */
public final class GTNARainbowText {

    /** Degrees of hue between adjacent letters. */
    private static final float LETTER_STEP = 25f;
    /** Degrees of hue change per client tick. */
    private static final float SPEED = 2.5f;
    private static final float SATURATION = 95f;
    private static final float BRIGHTNESS = 60f;

    private GTNARainbowText() {}

    public static Component of(String text) {
        MutableComponent result = Component.empty();
        float base = (GTValues.CLIENT_TIME & ((1 << 20) - 1)) * SPEED;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ' ') {
                result.append(Component.literal(" "));
                continue;
            }
            int rgb = GradientUtil.toRGB((base + i * LETTER_STEP) % 360f, SATURATION, BRIGHTNESS);
            result.append(Component.literal(String.valueOf(c)).withStyle(Style.EMPTY.withColor(rgb)));
        }
        return result;
    }

    public static Component of(Component text) {
        return of(text.getString());
    }
}
