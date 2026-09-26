// Example for GTNA's Integrated Ore Processor, which uses the same
// WorkableElectricMultipleRecipesMachine base as the gtna:multiple_recipes KubeJS builder.
// This module occupies the three blocks to the controller's right. It accepts one performance
// hatch, runs recipes twice as fast, and enables perfect overclock only while formed.
GTNAServerEvents.subPatterns(event => {
  event.add('gtna:integrated_ore_processor', definition => FactoryBlockPattern.start()
    .aisle('C P')
    .where('C', Predicates.controller(Predicates.blocks(definition.get())))
    .where('P', Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get())
      .or(Predicates.abilities(GTNAPartAbility.PARALLEL_CONTROL_HATCH).setMaxGlobalLimited(1))
      .or(Predicates.abilities(GTNAPartAbility.OVERCLOCK_HATCH).setMaxGlobalLimited(1))
      .or(Predicates.abilities(GTNAPartAbility.ACCELERATE_HATCH).setMaxGlobalLimited(1)))
    .where(' ', Predicates.any())
    .build(), 'gtna.machine.auxiliary_module.kubejs.performance', 2.0, true)
})
