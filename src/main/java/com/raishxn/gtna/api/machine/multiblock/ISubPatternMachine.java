package com.raishxn.gtna.api.machine.multiblock;

import com.gregtechceu.gtceu.api.pattern.BlockPattern;

import java.util.List;

/**
 * A GTNA multiblock that can be <b>extended</b> by additional structures ("sub-patterns" / modules)
 * attached to the same controller, in the spirit of GTOCore's {@code MultiblockDefinition}
 * sub-patterns.
 *
 * <p>
 * The controller's main pattern is checked first; if it matches, every sub-pattern is checked
 * independently at the same controller. The parts (hatches, buses, ...) detected by the sub-patterns
 * are merged into the controller's part set, so an extension structure can unlock new abilities —
 * e.g. a tower that adds Parallel or Accelerate hatches to the machine.
 *
 * <p>
 * The merge is applied by {@code MultiblockControllerMachineMixin} at the end of
 * {@code IMultiController#checkPattern()}, so any machine implementing this interface gets it
 * regardless of its base class. A sub-pattern that does not match is simply absent; the machine
 * still forms with the main structure.
 *
 * <p>
 * Machines that do not ship their own sub-patterns do not need to implement this interface; they can
 * still receive modules registered in {@link GTNASubPatterns} (Java or KubeJS). Every multiblock
 * exposes the number of formed modules through {@link IGTNAModuleHost}.
 */
public interface ISubPatternMachine extends IGTNAModuleHost {

    /** The additional structures attached to this machine (may be empty). */
    default List<BlockPattern> gtna$getSubPatterns() {
        return List.of();
    }
}
