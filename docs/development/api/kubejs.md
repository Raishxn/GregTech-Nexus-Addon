# 🔌 KubeJS Compatibility

Starting with version `0.2.0`, GregTech Nexus Addon (GTNA) provides full compatibility with **KubeJS** out of the box via soft-dependency.
Modpack developers can create advanced multiblocks utilizing our custom Hatches (Thread, Accelerate, Overclock, Advanced Parallel) directly from KubeJS!

## The `multiple_recipes` Machine Type

You can define a new specialized multiblock machine using our factory type `gtna:multiple_recipes`.
Any multiblock instantiated this way automatically uses `WorkableElectricMultipleRecipesMachine` under the hood.

---

## Practical Example: 4-Tier Threaded Processor

Here's a working example inside a KubeJS script that creates an Assembly Machine capable of having multiple recipes running simultaneously.

**File:** `kubejs/startup_scripts/gtna_processor.js`

```javascript
GTCEuStartupEvents.registry('gtceu:machine', event => {
    
    event.create('my_awesome_machine', 'gtna:multiple_recipes')
        // Which recipes the machine will look for. E.g. assembler recipes.
        .recipeType('gtceu:assembler') 
        .rotationState(RotationState.NON_Y_AXIS)
        .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
        
        // Let's create a 5x5 hollow structure.
        .pattern(definition => FactoryBlockPattern.start()
            .aisle('CCCCC', 'CCCCC', 'CCCCC')
            .aisle('CCCCC', 'C###C', 'CCCCC')
            .aisle('CCCCC', 'C###C', 'CCCCC')
            .aisle('CCCCC', 'C###C', 'CCCCC')
            .aisle('CCCCC', 'CC~CC', 'CCCCC')
            .where('~', Predicates.controller(Predicates.blocks(definition.get())))
            .where('C', Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get())
                // Standard GT Hatches
                .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                // === GTNA ADVANCED HATCH OPPORTUNITIES ===
                .or(Predicates.abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1))
                .or(Predicates.abilities(GTNAPartAbility.THREAD_HATCH).setMaxGlobalLimited(1))
                .or(Predicates.abilities(GTNAPartAbility.ACCELERATE_HATCH).setMaxGlobalLimited(1))
                .or(Predicates.abilities(GTNAPartAbility.OVERCLOCK_HATCH).setMaxGlobalLimited(1))
                .or(Predicates.abilities(PartAbility.MUFFLER).setMaxGlobalLimited(1))
            )
            .where('#', Predicates.air())
            .build()
        )
        
        // Define Custom Multipliers (Optional Config)
        .gtnaConfig(config => {
            config.maxThreadMultiplier(2)    // Double thread amount calculation!
            config.overclockBonus(0.1)       // Extra 10% OC scale
        })
        
        .workableCasingModel(
            'gtceu:block/casings/solid/machine_casing_solid_steel',
            'gtceu:block/multiblock/implosion_compressor'
        )
        
        .tooltips(
            Component.literal('§6My Custom Multi-Thread Machine'),
            Component.literal('§7Supports GTNA Thread, Accelerate & Overclock Hatches'),
            Component.literal('§bUses WorkableElectricMultipleRecipesMachine')
        )
})
```

### Explanation of Custom Abilities
- `PartAbility.PARALLEL_HATCH`: Uses GTCEu's standard parallel system or GTNA's advanced large-scale parallels.
- `GTNAPartAbility.THREAD_HATCH`: Allows executing **completely different recipes** simultaneously. (e.g. Assembling Recipe A and Recipe B at the same time).
- `GTNAPartAbility.ACCELERATE_HATCH`: Strips raw duration (ticks) from recipes without costing extra energy-per-tick.
- `GTNAPartAbility.OVERCLOCK_HATCH`: Provides immense Overclock multiples on top of standard GT electrical tiers.

### Registering Recipes

Whenever you add normal GTCEu recipes for the type passed into `.recipeType()`, it will immediately benefit from all the hatches without needing special syntax inside `server_scripts/recipes.js`. 
