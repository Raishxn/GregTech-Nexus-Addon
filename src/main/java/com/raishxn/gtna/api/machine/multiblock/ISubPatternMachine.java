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
 * The merge is applied by {@code IMultiControllerMixin} at the end of
 * {@code IMultiController#checkPattern()}, so any machine implementing this interface gets it
 * regardless of its base class. A sub-pattern that does not match is simply absent; the machine
 * still forms with the main structure.
 */
public interface ISubPatternMachine {

    /** The additional structures attached to this machine (may be empty). */
    List<BlockPattern> gtna$getSubPatterns();
}
