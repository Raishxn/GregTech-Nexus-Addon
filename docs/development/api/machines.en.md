# 🏭 API: Creating Multiblocks with GTNA Hatches

This guide shows how to create a multiblock that uses GTNA's custom hatches (Thread, Accelerate, Overclock, Advanced Parallel) and the `WorkableElectricMultipleRecipesMachine` class.

## Prerequisites

- Familiarity with GTCEu Modern's multiblock system
- For **Java**: access to GTNA classpath as a dependency
- For **KubeJS**: GTNA + KubeJS GTCEu installed (future v0.2.0)

---

## Example: Multiblock with All GTNA Hatches

=== "Java"

    ```java
    package com.example.myaddon.data;

    import com.gregtechceu.gtceu.api.data.RotationState;
    import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
    import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
    import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
    import com.gregtechceu.gtceu.api.pattern.Predicates;
    import com.gregtechceu.gtceu.common.data.GTBlocks;
    import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
    // GTNA Imports
    import com.raishxn.gtna.api.machine.multiblock.GTNAPartAbility;
    import com.raishxn.gtna.common.machine.multiblock.electric
        .WorkableElectricMultipleRecipesMachine;
    import net.minecraft.network.chat.Component;

    import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
    import static com.gregtechceu.gtceu.api.machine.multiblock.PartAbility.*;

    public class MyAddonMachines {

        public static final MultiblockMachineDefinition MY_CUSTOM_MACHINE =
            REGISTRATE
                .multiblock("my_threaded_assembler",
                    WorkableElectricMultipleRecipesMachine::new)
                .rotationState(RotationState.NON_Y_AXIS)
                .recipeType(GTRecipeTypes.ASSEMBLER_RECIPES)
                .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
                .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("CCCCC", "CCCCC", "CCCCC")
                    .aisle("CCCCC", "C###C", "CCCCC")
                    .aisle("CCCCC", "C###C", "CCCCC")
                    .aisle("CCCCC", "C###C", "CCCCC")
                    .aisle("CCCCC", "CC~CC", "CCCCC")
                    .where('~', controller(blocks(definition.get())))
                    .where('C', blocks(GTBlocks.CASING_STEEL_SOLID.get())
                        // Standard GregTech Hatches
                        .or(autoAbilities(definition.getRecipeTypes()))
                        .or(abilities(MAINTENANCE).setExactLimit(1))
                        .or(abilities(MUFFLER).setMaxGlobalLimited(1))
                        // === GTNA HATCHES ===
                        .or(abilities(PARALLEL_HATCH)
                            .setMaxGlobalLimited(1))
                        .or(abilities(GTNAPartAbility.THREAD_HATCH)
                            .setMaxGlobalLimited(1))
                        .or(abilities(GTNAPartAbility.ACCELERATE_HATCH)
                            .setMaxGlobalLimited(1))
                        .or(abilities(GTNAPartAbility.OVERCLOCK_HATCH)
                            .setMaxGlobalLimited(1))
                    )
                    .where('#', air())
                    .build())
                .workableCasingModel(
                    GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                    GTCEu.id("block/multiblock/implosion_compressor"))
                .tooltips(
                    Component.literal("§6My Custom Thread Machine"),
                    Component.literal("§7Supports GTNA Thread, Accelerate & Overclock Hatches"),
                    Component.literal("§bUp to 256 different recipes simultaneously!")
                )
                .register();
    }
    ```

    > **Key class**: `WorkableElectricMultipleRecipesMachine` — this
    > is the class that implements multi-recipe simultaneous logic
    > (Threads). Without it, Thread Hatches won't work.

=== "KubeJS"

    ```javascript
    // kubejs/startup_scripts/my_threaded_machine.js
    // Requires: GTNA v0.2.0+ with KubeJS integration

    GTCEuStartupEvents.registry('gtceu:machine', event => {
        
        event.create('my_threaded_assembler', 'gtna:multiple_recipes')
            // Recipe type
            .recipeType('gtceu:assembler')
            
            .rotationState(RotationState.NON_Y_AXIS)
            .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
            
            // Structure
            .pattern(definition => FactoryBlockPattern.start()
                .aisle('CCCCC', 'CCCCC', 'CCCCC')
                .aisle('CCCCC', 'C###C', 'CCCCC')
                .aisle('CCCCC', 'C###C', 'CCCCC')
                .aisle('CCCCC', 'C###C', 'CCCCC')
                .aisle('CCCCC', 'CC~CC', 'CCCCC')
                .where('~', Predicates.controller(
                    Predicates.blocks(definition.get())))
                .where('C', Predicates.blocks(
                    GTBlocks.CASING_STEEL_SOLID.get())
                    // Standard hatches
                    .or(Predicates.autoAbilities(
                        definition.getRecipeTypes()))
                    .or(Predicates.abilities(
                        PartAbility.MAINTENANCE).setExactLimit(1))
                    .or(Predicates.abilities(
                        PartAbility.MUFFLER).setMaxGlobalLimited(1))
                    // === GTNA HATCHES ===
                    .or(Predicates.abilities(
                        PartAbility.PARALLEL_HATCH)
                        .setMaxGlobalLimited(1))
                    .or(Predicates.abilities(
                        GTNAPartAbility.THREAD_HATCH)
                        .setMaxGlobalLimited(1))
                    .or(Predicates.abilities(
                        GTNAPartAbility.ACCELERATE_HATCH)
                        .setMaxGlobalLimited(1))
                    .or(Predicates.abilities(
                        GTNAPartAbility.OVERCLOCK_HATCH)
                        .setMaxGlobalLimited(1))
                )
                .where('#', Predicates.air())
                .build()
            )
            
            // Model
            .workableCasingModel(
                'gtceu:block/casings/solid/machine_casing_solid_steel',
                'gtceu:block/multiblock/implosion_compressor'
            )
            
            // Tooltips
            .tooltips(
                Component.literal('§6My Custom Thread Machine'),
                Component.literal('§7Supports GTNA Thread, Accelerate & Overclock Hatches'),
                Component.literal('§bUp to 256 different recipes simultaneously!')
            )
    })
    ```

    > **Machine type**: Use `'gtna:multiple_recipes'` instead of the standard GT
    > type to get Thread Hatch support. Requires GTNA v0.2.0+.

---

## Difference between Java and KubeJS

| Aspect | Java | KubeJS |
|--------|------|--------|
| **When to use** | Standalone addons, complex logic | Modpacks, quick tweaks |
| **Machine type** | `WorkableElectricMultipleRecipesMachine::new` | `'gtna:multiple_recipes'` |
| **Part Abilities** | `GTNAPartAbility.THREAD_HATCH` | `GTNAPartAbility.THREAD_HATCH` |
| **Compile-time** | ✅ Type checking | ❌ Runtime only |
| **Hot reload** | ❌ Requires restart | ✅ `/kubejs reload` |
| **Recipes** | `GTRecipeBuilder` | `event.recipes.gtceu...` |

---

## GTNA Part Abilities Reference

```java
// Java
import com.raishxn.gtna.api.machine.multiblock.GTNAPartAbility;

GTNAPartAbility.THREAD_HATCH      // Different simultaneous recipes
GTNAPartAbility.ACCELERATE_HATCH  // Reduces duration
GTNAPartAbility.OVERCLOCK_HATCH   // Multiplies speed
PartAbility.PARALLEL_HATCH        // Advanced Parallel (GT base)
```

```javascript
// KubeJS
GTNAPartAbility.THREAD_HATCH
GTNAPartAbility.ACCELERATE_HATCH
GTNAPartAbility.OVERCLOCK_HATCH
PartAbility.PARALLEL_HATCH
```

---

## See Also

- [Thread Hatch — Gameplay](../../gameplay/parts/thread-hatch.md)
- [Accelerate Hatch — Gameplay](../../gameplay/parts/accelerate-hatch.md)
- [Overclock Hatch — Gameplay](../../gameplay/parts/overclock-hatch.md)
- [Advanced Parallel — Gameplay](../../gameplay/parts/advanced-parallel-hatch.md)
- [PRD #4 — KubeJS Compatibility](../../prd/prd_04_kubejs_compat.md)
