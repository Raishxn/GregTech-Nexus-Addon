package com.raishxn.gtna.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import com.raishxn.gtna.GTNACORE;

public class NexusWingsModel extends EntityModel<AbstractClientPlayer> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation(GTNACORE.MOD_ID, "nexus_wings"), "main");

    private final ModelPart core;
    private final ModelPart leftWing;
    private final ModelPart rightWing;

    public NexusWingsModel(ModelPart root) {
        this.core = root.getChild("core");
        this.leftWing = root.getChild("left_wing");
        this.rightWing = root.getChild("right_wing");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("core",
                CubeListBuilder.create()
                        .texOffs(26, 8)
                        .addBox(-6.0F, -10.0F, 1.5F, 12.0F, 20.0F, 2.0F),
                PartPose.offset(0.0F, 12.0F, 0.0F));

        root.addOrReplaceChild("left_wing",
                CubeListBuilder.create()
                        .texOffs(0, 4)
                        .addBox(0.0F, -6.0F, 0.0F, 24.0F, 12.0F, 0.0F),
                PartPose.offset(3.0F, 7.0F, 2.5F));

        root.addOrReplaceChild("right_wing",
                CubeListBuilder.create()
                        .texOffs(0, 4)
                        .mirror()
                        .addBox(-24.0F, -6.0F, 0.0F, 24.0F, 12.0F, 0.0F),
                PartPose.offset(-3.0F, 7.0F, 2.5F));

        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        boolean flying = player.getAbilities().flying || player.isFallFlying();
        boolean crouching = player.isCrouching();
        float flap = Mth.cos(ageInTicks * (flying ? 0.55F : 0.18F)) * (flying ? 0.12F : 0.03F);

        this.core.xRot = crouching ? 0.20F : 0.0F;
        this.core.y = crouching ? 13.5F : 12.0F;
        this.core.z = crouching ? 4.0F : 0.0F;

        float wingXRot = flying ? -0.20F : (crouching ? 0.15F : 0.02F);
        float wingYRot = flying ? 0.10F : (crouching ? 0.08F : 0.04F);
        float wingZRot = flying ? -0.95F : -0.45F;

        this.leftWing.xRot = wingXRot;
        this.leftWing.yRot = wingYRot;
        this.leftWing.zRot = wingZRot + flap;

        this.rightWing.xRot = wingXRot;
        this.rightWing.yRot = -wingYRot;
        this.rightWing.zRot = -this.leftWing.zRot;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        core.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        leftWing.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        rightWing.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
