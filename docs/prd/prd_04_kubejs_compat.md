# 🔌 PRD #4 — KubeJS Compatibility Layer
## GregTech Nexus Addon — KubeJS Integration Requirements
### Versão 1.0 | Bilíngue PT-BR / EN

---

## 1. Visão Geral / Overview

**PT-BR**: Este PRD define como expor as classes e mecânicas customizadas do GTNA para o KubeJS, permitindo que desenvolvedores de modpacks e jogadores avançados criem seus próprios multiblocos usando as hatches especiais do GTNA (Thread, Accelerate, Overclock, Advanced Parallel) e a classe `WorkableElectricMultipleRecipesMachine` sem precisar escrever código Java.

**EN**: This PRD defines how to expose GTNA's custom classes and mechanics to KubeJS, enabling modpack developers and advanced players to create their own multiblocks using GTNA's special hatches (Thread, Accelerate, Overclock, Advanced Parallel) and the `WorkableElectricMultipleRecipesMachine` class without writing Java code.

---

## 2. Objetivos / Goals

| Objetivo | Descrição |
|----------|-----------|
| **Acessibilidade** | Qualquer dev de modpack deve conseguir criar um multiblock com hatches GTNA em <30 linhas de JS |
| **Segurança** | Os scripts KubeJS não podem quebrar a lógica interna dos hatches |
| **Compatibilidade** | Funcionar com GT Modern KubeJS bindings existentes |
| **Documentação** | Cada API exposta deve ter exemplos copiáveis na wiki |
| **Extensibilidade** | Devs devem poder criar novas combinações de hatches não previstas |

---

## 3. Dependências / Dependencies

| Mod | Versão | Papel |
|-----|--------|-------|
| **KubeJS** | 6.x (1.20.1) | Scripting engine |
| **KubeJS GTCEu** | Latest | GT Modern KubeJS bindings |
| **GregTech CEu Modern** | 7.4.x | Base mod |
| **GTNA** | 0.2.0+ | Our addon |

> [!IMPORTANT]
> A integração depende do **KubeJS GTCEu** já existente. O GTNA adiciona uma camada EM CIMA dele, não substitui.

---

## 4. Arquitetura Técnica / Technical Architecture

### 4.1 Classes Expostas ao KubeJS

```
GTNA Classes para KubeJS:
├── WorkableElectricMultipleRecipesMachine  ← Multiblock base com threads
├── AccelerateHatchPartMachine              ← Reduz duração
├── AdvancedParallelHatchPartMachine        ← Paralelos customizáveis
├── OverclockHatchPartMachine               ← Multiplicador de OC
├── ThreadPartMachine                       ← Receitas simultâneas diferentes
├── GTNAPartAbility                         ← THREAD_HATCH, ACCELERATE_HATCH, OVERCLOCK_HATCH
└── GTNAMultipleRecipesLogic                ← Lógica multi-receita (trait)
```

### 4.2 Fluxo de Integração

```mermaid
graph LR
    A["KubeJS Script<br/>(startup_scripts/)"] --> B["GTNAKubeJSPlugin<br/>(registra bindings)"]
    B --> C["GTCEu KubeJS API<br/>(multiblock builder)"]
    C --> D["GTNA Machine Factory<br/>(cria máquina com traits)"]
    D --> E["Multiblock ingame<br/>com hatches GTNA"]
```

### 4.3 Implementação Java Necessária

#### 4.3.1 KubeJS Plugin (Novo arquivo)

```java
// com.raishxn.gtna.integration.kubejs.GTNAKubeJSPlugin.java
package com.raishxn.gtna.integration.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptType;

public class GTNAKubeJSPlugin extends KubeJSPlugin {

    @Override
    public void registerBindings(BindingsEvent event) {
        // Expor constantes e builders
        event.add("GTNAMachineType", GTNAMachineTypes.class);
        event.add("GTNAPartAbility", GTNAPartAbilityWrapper.class);
        event.add("GTNAHatchConfig", GTNAHatchConfigBuilder.class);
    }

    @Override
    public void registerEvents(BindingsEvent event) {
        // Eventos customizados GTNA
        event.add("gtna.multiblock.formed", GTNAMultiblockFormedEvent.class);
        event.add("gtna.thread.started", GTNAThreadStartedEvent.class);
    }
}
```

#### 4.3.2 Wrapper de Part Abilities (Novo arquivo)

```java
// com.raishxn.gtna.integration.kubejs.GTNAPartAbilityWrapper.java
package com.raishxn.gtna.integration.kubejs;

import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.raishxn.gtna.api.machine.multiblock.GTNAPartAbility;

/**
 * Wrapper para expor as PartAbilities do GTNA para KubeJS.
 * Usado como: GTNAPartAbility.THREAD_HATCH no script.
 */
public class GTNAPartAbilityWrapper {
    public static final PartAbility THREAD_HATCH = GTNAPartAbility.THREAD_HATCH;
    public static final PartAbility ACCELERATE_HATCH = GTNAPartAbility.ACCELERATE_HATCH;
    public static final PartAbility OVERCLOCK_HATCH = GTNAPartAbility.OVERCLOCK_HATCH;
    // PARALLEL_HATCH já existe no GT base
}
```

#### 4.3.3 Machine Type Builder (Novo arquivo)

```java
// com.raishxn.gtna.integration.kubejs.GTNAMachineTypes.java
package com.raishxn.gtna.integration.kubejs;

/**
 * Factory methods para criar máquinas GTNA via KubeJS.
 */
public class GTNAMachineTypes {
    
    /**
     * Cria um multiblock que suporta todas as hatches GTNA.
     * @param id Identificador do multiblock (ex: "my_multiblock")
     * @return Builder configurável via KubeJS
     */
    public static GTNAMultiblockBuilder multipleRecipesMachine(String id) {
        return new GTNAMultiblockBuilder(id, WorkableElectricMultipleRecipesMachine::new);
    }
}
```

---

## 5. API KubeJS — Exemplos Completos / Full KubeJS Examples

### 5.1 Exemplo Básico: Multiblock com Thread + Accelerate

```javascript
// kubejs/startup_scripts/my_multiblock.js

GTCEuStartupEvents.registry('gtceu:machine', event => {
    
    event.create('my_awesome_machine', 'gtna:multiple_recipes')
        // Tipo de receita que aceita
        .recipeType('gtceu:assembler')
        
        // Rotação permitida
        .rotationState(RotationState.NON_Y_AXIS)
        
        // Aparência no JEI
        .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
        
        // Padrão da estrutura
        .pattern(definition => FactoryBlockPattern.start()
            .aisle('CCCCC', 'CCCCC', 'CCCCC')
            .aisle('CCCCC', 'C###C', 'CCCCC')
            .aisle('CCCCC', 'C###C', 'CCCCC')
            .aisle('CCCCC', 'C###C', 'CCCCC')
            .aisle('CCCCC', 'CC~CC', 'CCCCC')
            .where('~', Predicates.controller(Predicates.blocks(definition.get())))
            .where('C', Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get())
                // Hatches padrão GT
                .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                // === HATCHES GTNA ===
                .or(Predicates.abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1))
                .or(Predicates.abilities(GTNAPartAbility.THREAD_HATCH).setMaxGlobalLimited(1))
                .or(Predicates.abilities(GTNAPartAbility.ACCELERATE_HATCH).setMaxGlobalLimited(1))
                .or(Predicates.abilities(GTNAPartAbility.OVERCLOCK_HATCH).setMaxGlobalLimited(1))
                .or(Predicates.abilities(PartAbility.MUFFLER).setMaxGlobalLimited(1))
            )
            .where('#', Predicates.air())
            .build()
        )
        
        // Modelo visual
        .workableCasingModel(
            'gtceu:block/casings/solid/machine_casing_solid_steel',
            'gtceu:block/multiblock/implosion_compressor'
        )
        
        // Tooltips
        .tooltips(
            Component.literal('§6My Custom Multi-Thread Machine'),
            Component.literal('§7Supports GTNA Thread, Accelerate & Overclock Hatches'),
            Component.literal('§bUses WorkableElectricMultipleRecipesMachine')
        )
})
```

### 5.2 Exemplo Avançado: Configuração de Eficiência Custom

```javascript
// kubejs/startup_scripts/advanced_machine.js

GTCEuStartupEvents.registry('gtceu:machine', event => {
    
    event.create('industrial_processor', 'gtna:multiple_recipes')
        .recipeTypes(['gtceu:assembler', 'gtceu:circuit_assembler'])
        .rotationState(RotationState.NON_Y_AXIS)
        .appearanceBlock(GTBlocks.CASING_ALUMINIUM_FROSTPROOF)
        
        // Configurações GTNA avançadas
        .gtnaConfig(config => {
            config.maxThreadMultiplier(2)    // Dobra threads do Thread Hatch
            config.overclockBonus(0.1)       // 10% bônus adicional de OC
            config.accelerateCapEnabled(true) // Limite no Accelerate
            config.accelerateMinDuration(5)  // Duração mínima 5 ticks (0.25s)
        })
        
        .pattern(definition => FactoryBlockPattern.start()
            .aisle('AAAAAAA', 'AAAAAAA', 'AAAAAAA')
            .aisle('AAAAAAA', 'A#####A', 'AAAAAAA')
            .aisle('AAAAAAA', 'A#####A', 'AAAAAAA')
            .aisle('AAAAAAA', 'A#####A', 'AAAAAAA')
            .aisle('AAAAAAA', 'A#####A', 'AAAAAAA')
            .aisle('AAAAAAA', 'A#####A', 'AAAAAAA')
            .aisle('AAAAAAA', 'AAA~AAA', 'AAAAAAA')
            .where('~', Predicates.controller(Predicates.blocks(definition.get())))
            .where('A', Predicates.blocks(GTBlocks.CASING_ALUMINIUM_FROSTPROOF.get())
                .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                .or(Predicates.abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1))
                .or(Predicates.abilities(GTNAPartAbility.THREAD_HATCH).setMaxGlobalLimited(1))
                .or(Predicates.abilities(GTNAPartAbility.ACCELERATE_HATCH).setMaxGlobalLimited(2))
                .or(Predicates.abilities(GTNAPartAbility.OVERCLOCK_HATCH).setMaxGlobalLimited(1))
                .or(Predicates.abilities(PartAbility.MUFFLER).setMaxGlobalLimited(1))
            )
            .where('#', Predicates.air())
            .build()
        )
        
        .workableCasingModel(
            'gtceu:block/casings/solid/machine_casing_frost_proof',
            'gtceu:block/multiblock/vacuum_freezer'
        )
        
        .tooltips(
            Component.literal('§6Industrial Multi-Processor'),
            Component.literal('§7Accepts Assembler + Circuit Assembler recipes'),
            Component.literal('§bSupports up to 2 Accelerate Hatches!'),
            Component.literal('§eThread multiplier: 2x')
        )
})
```

### 5.3 Exemplo: Criar Receitas para Máquina GTNA Custom

```javascript
// kubejs/server_scripts/my_recipes.js

ServerEvents.recipes(event => {
    // Receitas para assembler que funcionam na máquina custom
    event.recipes.gtceu.assembler('my_modpack:custom_component')
        .itemInputs('4x gtceu:steel_plate', '2x gtceu:copper_wire_single')
        .itemOutputs('my_modpack:custom_component')
        .duration(200)   // 10 seconds
        .EUt(30)         // LV tier
    
    // Receitas usam Thread Hatch automaticamente quando a máquina
    // é WorkableElectricMultipleRecipesMachine
})
```

---

## 6. Referência da API / API Reference

### 6.1 GTNAPartAbility Constants

| Constante | Tipo | Uso em Pattern | Efeito |
|-----------|------|----------------|--------|
| `GTNAPartAbility.THREAD_HATCH` | PartAbility | `.abilities(...).setMaxGlobalLimited(1)` | Permite receitas diferentes simultâneas |
| `GTNAPartAbility.ACCELERATE_HATCH` | PartAbility | `.abilities(...).setMaxGlobalLimited(N)` | Reduz duração das receitas por % |
| `GTNAPartAbility.OVERCLOCK_HATCH` | PartAbility | `.abilities(...).setMaxGlobalLimited(1)` | Multiplica velocidade de overclock |
| `PartAbility.PARALLEL_HATCH` | PartAbility (GT) | `.abilities(...).setMaxGlobalLimited(1)` | Advanced Parallel do GTNA |

### 6.2 Hatch Tier Effects

#### Thread Hatch (ZPM → MAX)

| Tier | Threads Adicionais | Fórmula |
|------|--------------------|---------|
| ZPM (7) | +1 | `2^(7-6) - 1 = 1` |
| UV (8) | +3 | `2^(8-6) - 1 = 3` |
| UHV (9) | +7 | `2^(9-6) - 1 = 7` |
| UEV (10) | +15 | `2^(10-6) - 1 = 15` |
| UIV (11) | +31 | `2^(11-6) - 1 = 31` |
| UXV (12) | +63 | `2^(12-6) - 1 = 63` |
| OpV (13) | +127 | `2^(13-6) - 1 = 127` |
| MAX (14) | +255 | `2^(14-6) - 1 = 255` |

#### Accelerate Hatch (LV → MAX)

| Tier | Min Duration % | Fórmula |
|------|---------------|---------|
| LV (1) | 48% | `50 - 2×(1-1) = 48%` |
| MV (2) | 46% | `50 - 2×(2-1) = 46%` |
| HV (3) | 44% | `50 - 2×(3-1) = 44%` |
| EV (4) | 42% | `50 - 2×(4-1) = 42%` |
| ... | ... | ... |
| MAX (14) | 24% | `50 - 2×(14-1) = 24%` |

> [!WARNING]
> Se a tier do Accelerate Hatch é **menor** que a tier da máquina, aplica-se penalidade de +20% por tier de diferença.

#### Overclock Hatch (UV → MAX)

| Tier | Duration Multiplier | Efeito |
|------|-------------------|--------|
| UV (8) | 0.55 (55%) | Remove 45% da duração |
| UHV (9) | 0.333 (33.3%) | Remove 66.7% |
| UEV (10) | 0.25 (25%) | Remove 75% |
| UIV (11) | 0.20 (20%) | Remove 80% |
| UXV (12) | 0.167 (16.7%) | Remove 83.3% |
| OpV (13) | 0.143 (14.3%) | Remove 85.7% |
| MAX (14) | 0.125 (12.5%) | Remove 87.5% |

#### Advanced Parallel Hatch (UHV → OpV)

| Tier | Parallel Count |
|------|---------------|
| UHV (9) | 1,024 |
| UEV (10) | 4,096 |
| UIV (11) | 16,384 |
| UXV (12) | 65,536 |
| OpV (13) | 262,144 |

### 6.3 Interaction Between Hatches

```
Final Duration = Base_Duration 
  × Electric_OC_Factor (GT standard)
  × AccelerateHatch_Factor (calcDurationPercentage / 100)
  × OverclockHatch_Factor (getOverclockMultiplier)

Example:
  Base = 200 ticks
  Electric OC (EV machine, LV recipe) = 200 / 4 = 50 ticks
  Accelerate Hatch (HV tier, EV machine) = 50 × 0.44 = 22 ticks
  Overclock Hatch (UV) = 22 × 0.55 = 12.1 → 12 ticks

Threads work INDEPENDENTLY:
  With Thread Hatch UV (+3 threads):
  Thread 1: Recipe A (12 ticks)
  Thread 2: Recipe B (12 ticks)  ← different recipe!
  Thread 3: Recipe C (12 ticks)
  Thread 4: Recipe D (12 ticks)
  All 4 run simultaneously, each with its own parallels.
```

---

## 7. Arquivos Necessários / Required Files

### 7.1 Novos Arquivos Java

| Arquivo | Pacote | Descrição |
|---------|--------|-----------|
| `GTNAKubeJSPlugin.java` | `integration.kubejs` | Plugin principal KubeJS |
| `GTNAPartAbilityWrapper.java` | `integration.kubejs` | Wrapper de PartAbilities |
| `GTNAMachineTypes.java` | `integration.kubejs` | Factory de máquinas |
| `GTNAMultiblockBuilder.java` | `integration.kubejs` | Builder fluent para multiblocks |
| `GTNAHatchConfigBuilder.java` | `integration.kubejs` | Configuração de hatches |

### 7.2 Dependência Gradle Necessária

```groovy
// build.gradle — adicionar:
modCompileOnly("dev.latvian.mods:kubejs-forge:${kubejs_version}")
```

### 7.3 Registro Condicional

```java
// A integração KubeJS só é ativada se KubeJS estiver presente
if (ModList.get().isLoaded("kubejs")) {
    GTNAKubeJSPlugin.init();
}
```

---

## 8. Testes / Testing

### 8.1 Script de Teste Mínimo

```javascript
// kubejs/startup_scripts/test_gtna.js
// Este script deve funcionar sem erros se a integração está correta

console.log('GTNA KubeJS Integration Test')
console.log('Thread Hatch ability:', GTNAPartAbility.THREAD_HATCH)
console.log('Accelerate Hatch ability:', GTNAPartAbility.ACCELERATE_HATCH)
console.log('Overclock Hatch ability:', GTNAPartAbility.OVERCLOCK_HATCH)

GTCEuStartupEvents.registry('gtceu:machine', event => {
    event.create('gtna_test_machine', 'gtna:multiple_recipes')
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
                .or(Predicates.abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1))
                .or(Predicates.abilities(GTNAPartAbility.THREAD_HATCH).setMaxGlobalLimited(1))
                .or(Predicates.abilities(GTNAPartAbility.ACCELERATE_HATCH).setMaxGlobalLimited(1))
                .or(Predicates.abilities(GTNAPartAbility.OVERCLOCK_HATCH).setMaxGlobalLimited(1))
            )
            .where('#', Predicates.air())
            .build()
        )
        .workableCasingModel(
            'gtceu:block/casings/solid/machine_casing_solid_steel',
            'gtceu:block/multiblock/implosion_compressor'
        )
        .tooltips(Component.literal('§aGTNA Integration Test Machine'))
    
    console.log('GTNA test machine registered successfully!')
})
```

### 8.2 Checklist de Validação

```
[  ] Script carrega sem erros (console log OK)
[  ] GTNAPartAbility constantes acessíveis no KubeJS
[  ] Multiblock formado corretamente in-game
[  ] Thread Hatch funciona (múltiplas receitas simultâneas)
[  ] Accelerate Hatch reduz duração
[  ] Overclock Hatch aplica multiplicador
[  ] Advanced Parallel Hatch aplica paralelos
[  ] Combinação de hatches funciona (stacking)
[  ] Tooltip mostra informações de hatches ativos
[  ] Display text mostra threads por receita
[  ] Save/Load funciona (mundo não corrompe)
```

---

## 9. Documentação para Wiki / Wiki Documentation

Esta seção será adicionada à wiki em:
- `docs/development/api/kubejs.md` — Guia completo
- `docs/gameplay/tips/kubejs-examples.md` — Exemplos para jogadores

---

*PRD KubeJS Compatibility — GTNA v0.2.0*
