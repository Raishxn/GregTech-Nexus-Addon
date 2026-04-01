# 🔌 KubeJS Integration

> **Status**: <span class="status-badge status-planned">🔮 Planejado v0.2.0</span>

## Overview

O GTNA será compatível com KubeJS, permitindo que devs de modpack criem multiblocks usando as hatches personalizadas do GTNA sem escrever Java.

## Classes Expostas

| Classe | Uso |
|--------|-----|
| `WorkableElectricMultipleRecipesMachine` | Base para multiblocks com threads |
| `AccelerateHatchPartMachine` | Hatch que reduz duração |
| `OverclockHatchPartMachine` | Hatch que multiplica velocidade |
| `ThreadPartMachine` | Hatch para receitas simultâneas diferentes |
| `AdvancedParallelHatchPartMachine` | Paralelos massivos (1K→262K) |

## Exemplo Rápido

```javascript
// kubejs/startup_scripts/my_machine.js
GTCEuStartupEvents.registry('gtceu:machine', event => {
    
    event.create('my_custom_machine', 'gtna:multiple_recipes')
        .recipeType('gtceu:assembler')
        .rotationState(RotationState.NON_Y_AXIS)
        .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
        .pattern(definition => FactoryBlockPattern.start()
            .aisle('CCC', 'CCC', 'CCC')
            .aisle('CCC', 'C#C', 'CCC')
            .aisle('CCC', 'C~C', 'CCC')
            .where('~', Predicates.controller(Predicates.blocks(definition.get())))
            .where('C', Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get())
                .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                // === HATCHES GTNA ===
                .or(Predicates.abilities(GTNAPartAbility.THREAD_HATCH).setMaxGlobalLimited(1))
                .or(Predicates.abilities(GTNAPartAbility.ACCELERATE_HATCH).setMaxGlobalLimited(1))
                .or(Predicates.abilities(GTNAPartAbility.OVERCLOCK_HATCH).setMaxGlobalLimited(1))
                .or(Predicates.abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1))
            )
            .where('#', Predicates.air())
            .build()
        )
        .tooltips(
            Component.literal('§6My Custom Thread Machine'),
            Component.literal('§7Supports all GTNA hatches!')
        )
})
```

## Tabelas de Referência

### Thread Hatch (ZPM → MAX)

| Tier | Threads | Fórmula |
|------|---------|---------|
| ZPM | +1 | `2^(7-6) - 1` |
| UV | +3 | `2^(8-6) - 1` |
| UHV | +7 | `2^(9-6) - 1` |
| UEV | +15 | `2^(10-6) - 1` |
| UIV | +31 | `2^(11-6) - 1` |
| UXV | +63 | `2^(12-6) - 1` |
| OpV | +127 | `2^(13-6) - 1` |
| MAX | +255 | `2^(14-6) - 1` |

### Accelerate Hatch (LV → MAX)

| Tier | Min Duration % |
|------|---------------|
| LV | 48% |
| MV | 46% |
| HV | 44% |
| EV | 42% |
| IV | 40% |
| LuV | 38% |
| ZPM | 36% |
| UV | 34% |

### Overclock Hatch (UV → MAX)

| Tier | Multiplier | Speed |
|------|-----------|-------|
| UV | ×0.55 | 1.82x |
| UHV | ×0.333 | 3x |
| UEV | ×0.25 | 4x |
| UIV | ×0.20 | 5x |
| UXV | ×0.167 | 6x |
| OpV | ×0.143 | 7x |
| MAX | ×0.125 | 8x |

### Advanced Parallel (UHV → OpV)

| Tier | Paralelos |
|------|-----------|
| UHV | 1,024 |
| UEV | 4,096 |
| UIV | 16,384 |
| UXV | 65,536 |
| OpV | 262,144 |

## PRD Completo

Para o PRD técnico completo da integração KubeJS, veja: [PRD #4 — KubeJS](../../prd/prd_04_kubejs_compat.md)
