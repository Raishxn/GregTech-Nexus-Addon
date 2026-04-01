# 🏭 API: Criando Multiblocks com Hatches GTNA

Este guia mostra como criar um multiblock que usa as hatches personalizadas do GTNA (Thread, Accelerate, Overclock, Advanced Parallel) e a classe `WorkableElectricMultipleRecipesMachine`.

## Pré-requisitos

- Familiaridade com o sistema de multiblocks do GTCEu Modern
- Para **Java**: acesso ao classpath do GTNA como dependência
- Para **KubeJS**: GTNA + KubeJS GTCEu instalados (futuro v0.2.0)

---

## Exemplo: Multiblock com Todas as Hatches GTNA

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
    // Importações GTNA
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
                        // Hatches padrão do GregTech
                        .or(autoAbilities(definition.getRecipeTypes()))
                        .or(abilities(MAINTENANCE).setExactLimit(1))
                        .or(abilities(MUFFLER).setMaxGlobalLimited(1))
                        // === HATCHES GTNA ===
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

    > **Classe-chave**: `WorkableElectricMultipleRecipesMachine` — esta
    > é a classe que implementa a lógica de múltiplas receitas simultâneas
    > (Threads). Sem ela, Thread Hatches não funcionam.

=== "KubeJS"

    ```javascript
    // kubejs/startup_scripts/my_threaded_machine.js
    // Requer: GTNA v0.2.0+ com KubeJS integration

    GTCEuStartupEvents.registry('gtceu:machine', event => {
        
        event.create('my_threaded_assembler', 'gtna:multiple_recipes')
            // Tipo de receita
            .recipeType('gtceu:assembler')
            
            .rotationState(RotationState.NON_Y_AXIS)
            .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
            
            // Estrutura
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
                    // Hatches padrão
                    .or(Predicates.autoAbilities(
                        definition.getRecipeTypes()))
                    .or(Predicates.abilities(
                        PartAbility.MAINTENANCE).setExactLimit(1))
                    .or(Predicates.abilities(
                        PartAbility.MUFFLER).setMaxGlobalLimited(1))
                    // === HATCHES GTNA ===
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
            
            // Modelo
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

    > **Tipo de máquina**: Use `'gtna:multiple_recipes'` em vez do tipo GT
    > padrão para obter suporte a Thread Hatches. Requer GTNA v0.2.0+.

---

## Diferença entre Java e KubeJS

| Aspecto | Java | KubeJS |
|---------|------|--------|
| **Quando usar** | Addons standalone, lógica complexa | Modpacks, tweaks rápidos |
| **Tipo de máquina** | `WorkableElectricMultipleRecipesMachine::new` | `'gtna:multiple_recipes'` |
| **Part Abilities** | `GTNAPartAbility.THREAD_HATCH` | `GTNAPartAbility.THREAD_HATCH` |
| **Compile-time** | ✅ Verificação de tipos | ❌ Runtime only |
| **Hot reload** | ❌ Requer restart | ✅ `/kubejs reload` |
| **Receitas** | `GTRecipeBuilder` | `event.recipes.gtceu...` |

---

## Referência de Part Abilities GTNA

```java
// Java
import com.raishxn.gtna.api.machine.multiblock.GTNAPartAbility;

GTNAPartAbility.THREAD_HATCH      // Receitas diferentes simultâneas
GTNAPartAbility.ACCELERATE_HATCH  // Reduz duração
GTNAPartAbility.OVERCLOCK_HATCH   // Multiplica velocidade
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

## Ver Também

- [Thread Hatch — Gameplay](../../gameplay/parts/thread-hatch.md)
- [Accelerate Hatch — Gameplay](../../gameplay/parts/accelerate-hatch.md)
- [Overclock Hatch — Gameplay](../../gameplay/parts/overclock-hatch.md)
- [Advanced Parallel — Gameplay](../../gameplay/parts/advanced-parallel-hatch.md)
- [PRD #4 — KubeJS Compatibility](../../prd/prd_04_kubejs_compat.md)
