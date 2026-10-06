package com.raishxn.gtna.common.world.personalspace;

import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

/** Calls into client-only code from common blocks without loading client classes on a server. */
public final class PersonalSpaceClientHooks {

    private PersonalSpaceClientHooks() {}

    public static void openPortalGui(BlockPos pos) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.raishxn.gtna.client.personalspace.PersonalSpaceClient.openPortalGui(pos));
    }

    public static void closePortalGui(PersonalSpacePortalEntity portal) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.raishxn.gtna.client.personalspace.PersonalSpaceClient.closePortalGui(portal));
    }
}
