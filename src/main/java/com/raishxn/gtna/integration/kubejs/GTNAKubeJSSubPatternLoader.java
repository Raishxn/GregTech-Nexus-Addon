package com.raishxn.gtna.integration.kubejs;

import com.raishxn.gtna.GTNACORE;
import com.raishxn.gtna.api.machine.multiblock.GTNASubPatterns;
import com.raishxn.gtna.common.data.GTNAMachines;

/** Loaded only when KubeJS is present, after its server scripts have been read. */
public final class GTNAKubeJSSubPatternLoader {

    private GTNAKubeJSSubPatternLoader() {}

    public static void load() {
        GTNASubPatterns.clearKubeJS();
        GTNAServerEvents.SUB_PATTERNS.post(new SubPatternEventJS());
        if (GTNAMachines.INTEGRATED_ORE_PROCESSOR != null) {
            GTNACORE.LOGGER.info("KubeJS sub-patterns loaded; Integrated Ore Processor has {} auxiliary modules",
                    GTNASubPatterns.get(GTNAMachines.INTEGRATED_ORE_PROCESSOR).size());
        }
    }
}
