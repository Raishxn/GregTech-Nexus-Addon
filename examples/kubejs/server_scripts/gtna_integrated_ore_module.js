// Development example: a three-block auxiliary column beside the Integrated Ore Processor.
// Copy this file to kubejs/server_scripts with KubeJS installed. The middle block may be an
// Item Import Bus; the other two are Clean Stainless Steel Casings.
// The third argument is a translation key for the controller's Auxiliary Module tooltip.
GTNAServerEvents.subPatterns(event => {
  event.add('gtna:integrated_ore_processor', definition => FactoryBlockPattern.start()
    .aisle('   A', 'C  A', '   A')
    .where('C', Predicates.controller(Predicates.blocks(definition.get())))
    .where('A', Predicates.blocks(GTBlocks.CASING_STAINLESS_CLEAN.get())
      .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(1)))
    .where(' ', Predicates.any())
    .build(), 'gtna.machine.auxiliary_module.kubejs.item_import')
  console.info('[GTNA] Registered Integrated Ore Processor auxiliary module example')
})
