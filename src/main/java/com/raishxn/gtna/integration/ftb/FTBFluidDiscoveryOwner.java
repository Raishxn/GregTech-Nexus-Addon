package com.raishxn.gtna.integration.ftb;

import com.gregtechceu.gtceu.common.machine.owner.FTBOwner;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;

import java.util.UUID;

/** Loaded only when FTB Teams is installed. No client or mandatory mod dependency. */
public final class FTBFluidDiscoveryOwner {

    private FTBFluidDiscoveryOwner() {}

    public static UUID effectiveTeam(MachineOwner owner) {
        if (!(owner instanceof FTBOwner ftb)) return null;
        var team = ftb.getTeam();
        // FTBOwner.getUUID uses the personal ID; Team.getTeamId resolves its party.
        return team == null ? null : team.getTeamId();
    }
}
