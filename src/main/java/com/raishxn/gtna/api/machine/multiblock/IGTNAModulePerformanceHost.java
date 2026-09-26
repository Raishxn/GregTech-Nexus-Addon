package com.raishxn.gtna.api.machine.multiblock;

/** Effective bonuses from formed KubeJS auxiliary modules. */
public interface IGTNAModulePerformanceHost {

    double gtna$getModuleSpeedBonus();

    boolean gtna$hasModulePerfectOverclock();

    void gtna$setModulePerformance(double speedBonus, boolean perfectOverclock);
}
