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

    private final ModelPart leftWing;
    private final ModelPart leftTip;
    private final ModelPart rightWing;
    private final ModelPart rightTip;

    public NexusWingsModel(ModelPart root) {
        this.leftWing = root.getChild("left_wing");
        this.leftTip = leftWing.getChild("left_tip");
        this.rightWing = root.getChild("right_wing");
        this.rightTip = rightWing.getChild("right_tip");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition leftWing = root.addOrReplaceChild("left_wing",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(0.0F, -1.5F, 0.0F, 10.0F, 18.0F, 1.0F),
                PartPose.offset(2.0F, 2.0F, 2.5F));
        leftWing.addOrReplaceChild("left_tip",
                CubeListBuilder.create()
                        .texOffs(22, 0)
                        .addBox(0.0F, 0.0F, 0.0F, 12.0F, 14.0F, 1.0F),
                PartPose.offset(10.0F, 2.0F, 0.0F));

        PartDefinition rightWing = root.addOrReplaceChild("right_wing",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .mirror()
                        .addBox(-10.0F, -1.5F, 0.0F, 10.0F, 18.0F, 1.0F),
                PartPose.offset(-2.0F, 2.0F, 2.5F));
        rightWing.addOrReplaceChild("right_tip",
                CubeListBuilder.create()
                        .texOffs(22, 0)
                        .mirror()
                        .addBox(-12.0F, 0.0F, 0.0F, 12.0F, 14.0F, 1.0F),
                PartPose.offset(-10.0F, 2.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        boolean flying = player.getAbilities().flying || player.isFallFlying();
        boolean crouching = player.isCrouching();
        float flap = Mth.cos(ageInTicks * (flying ? 0.55F : 0.18F)) * (flying ? 0.12F : 0.03F);

        float wingXRot = flying ? -0.35F : (crouching ? 0.20F : 0.05F);
        float wingYRot = flying ? 0.20F : (crouching ? 0.12F : 0.08F);
        float wingZRot = flying ? -1.10F : -0.55F;

        this.leftWing.xRot = wingXRot;
        this.leftWing.yRot = wingYRot;
        this.leftWing.zRot = wingZRot + flap;
        this.leftTip.zRot = -0.30F + flap * 1.4F;

        this.rightWing.xRot = wingXRot;
        this.rightWing.yRot = -wingYRot;
        this.rightWing.zRot = -this.leftWing.zRot;
        this.rightTip.zRot = -this.leftTip.zRot;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        leftWing.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        rightWing.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
