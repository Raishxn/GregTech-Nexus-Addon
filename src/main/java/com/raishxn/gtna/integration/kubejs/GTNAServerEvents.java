package com.raishxn.gtna.integration.kubejs;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

/**
 * KubeJS events exposed by GTNA.
 *
 * <pre>{@code
 * GTNAServerEvents.subPatterns(event => {
 *     event.add('gtna:liquefaction_furnace', definition => FactoryBlockPattern.start()
 *         .aisle('AAA', 'A~A', 'AAA')
 *         .aisle('AAA', 'AAA', 'AAA')
 *         .where('~', Predicates.controller(Predicates.blocks(definition.get())))
 *         .where('A', Predicates.blocks('gtceu:heatproof_machine_casing')
 *             .or(Predicates.abilities('gtceu:parallel_hatch')))
 *         .build());
 * });
 * }</pre>
 */
public interface GTNAServerEvents {

    EventGroup GROUP = EventGroup.of("GTNAServerEvents");

    /** Register extension structures ("modules") for any multiblock. */
    EventHandler SUB_PATTERNS = GROUP.server("subPatterns", () -> SubPatternEventJS.class);
}
