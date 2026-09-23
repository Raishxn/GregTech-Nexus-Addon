// Development example: a three-block auxiliary column beside the Integrated Ore Processor.
// Copy this file to kubejs/server_scripts with KubeJS installed. The middle block may be an
// Item Import Bus; the other two are Clean Stainless Steel Casings.
GTNAServerEvents.subPatterns(event => {
  event.add('gtna:integrated_ore_processor', definition => FactoryBlockPattern.start()
    .aisle('   A', 'C  A', '   A')
    .where('C', Predicates.controller(Predicates.blocks(definition.get())))
    .where('A', Predicates.blocks(GTBlocks.CASING_STAINLESS_CLEAN.get())
      .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(1)))
    .where(' ', Predicates.any())
    .build())
  console.info('[GTNA] Registered Integrated Ore Processor auxiliary module example')
})
