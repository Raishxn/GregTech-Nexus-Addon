package com.raishxn.gtna.mixin.gtceu;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.raishxn.gtna.api.data.info.GTNAMaterialFlags;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TagPrefix.class, remap = false)
public class GTNAPlateDoubleMixin {

    @Shadow @Final @Mutable
    public static TagPrefix plateDouble;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void gtna$overridePlateDouble(CallbackInfo ci) {
        plateDouble = new TagPrefix("doublePlate")
                .idPattern("double_%s_plate")
                .defaultTagPath("double_plates/%s")
                .unformattedTagPath("double_plates")
                .langValue("Double %s Plate")
                .materialAmount(GTValues.M * 2)
                .maxStackSize(32)
                .materialIconType(MaterialIconType.plateDouble)
                .unificationEnabled(true)
                .enableRecycling()
                .generateItem(true)
                .generationCondition(mat ->
                        (mat.hasProperty(PropertyKey.INGOT) && mat.hasFlag(MaterialFlags.GENERATE_PLATE)  && !mat.hasFlag(MaterialFlags.NO_SMASHING)
                        ) || mat.hasFlag(GTNAMaterialFlags.GENERATE_DOUBLE_PLATE)
                );
    }
}