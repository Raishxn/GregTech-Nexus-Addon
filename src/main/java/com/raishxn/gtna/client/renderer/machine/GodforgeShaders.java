package com.raishxn.gtna.client.renderer.machine;

import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterShadersEvent;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.raishxn.gtna.GTNACORE;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/** Core shaders of the Forge of Gods star and beam (GTNH {@code star} and {@code gorgeBeam}). */
@OnlyIn(Dist.CLIENT)
public final class GodforgeShaders {

    @Nullable
    private static ShaderInstance star;
    @Nullable
    private static ShaderInstance beam;

    private GodforgeShaders() {}

    public static void register(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(), GTNACORE.id("godforge_star"),
                DefaultVertexFormat.POSITION_TEX), shader -> star = shader);
        event.registerShader(new ShaderInstance(event.getResourceProvider(), GTNACORE.id("godforge_beam"),
                DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL), shader -> beam = shader);
    }

    @Nullable
    public static ShaderInstance star() {
        return star;
    }

    @Nullable
    public static ShaderInstance beam() {
        return beam;
    }
}
