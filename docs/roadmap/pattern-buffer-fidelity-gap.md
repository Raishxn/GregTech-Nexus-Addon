# 🎯 Gap de Fidelidade — Pattern Buffer GTNA vs GTOCore/GTLCore

!!! info "Propósito"
    O objetivo declarado é que o nosso ME Pattern Buffer seja **1:1 (ou o mais fiel possível)**
    com o do GTOCore/GTLCore/GTLAdditions. Este documento mede o delta real entre as
    implementações e define o que "1:1" significa **sem trocar a base GTM oficial**.

    **Cadeia de herança da referência:**
    `GTLAdditions.MESuperPatternBufferPartMachine` → (mixin/estende) `GTLCore.MEPatternBufferPartMachine`
    (994 linhas) → deriva de `GTOCore.MEPatternBufferPartMachine` (790 linhas).

    O **alvo de fidelidade correto é o GTLCore** (`org.gtlcore.gtlcore.common.machine.multiblock.part.ae.MEPatternBufferPartMachine`),
    porque ele roda sobre o GTM com `@Persisted`/`@DescSynced` do **ldlib — a mesma base que o GTNA já usa**.
    O GTOCore, por sua vez, roda sobre um **fork profundo do GTM** (`GTRecipeDefinition`,
    `RecipeHandlerUnit`, `fullModifyRecipe(unit, def)`, `IntLongMap` da fastutil) que **não existe
    no GTM 7.5.3 oficial** — portar 1:1 do GTOCore exigiria trocar a base, o que está fora de questão.

---

## 1. O que "1:1" NÃO significa (limitação estrutural confirmada)

O GTOCore/GTLAdditions fazem o controller **aceitar todos os recipe types simultaneamente**
via `RecipeHandlerUnit` por slot (`InternalSlotRecipeHandler`) — isso depende de APIs que só
existem no fork GTM deles:

| API do fork GTO | Existe no GTM 7.5.3 oficial? |
|---|---|
| `IRecipeLogicMachine.fullModifyRecipe(RecipeHandlerUnit, GTRecipeDefinition)` | ❌ não (só `fullModifyRecipe(GTRecipe)`) |
| `GTRecipeType.available(type, getAvailableRecipeTypes())` | ❌ não |
| `RecipeHandlerUnit` (unidade por slot) | ❌ não (só `RecipeHandlerList`) |
| `GTRecipeDefinition` + `DATA_CODEC` | ❌ não (oficial usa `GTRecipe`) |

**Conclusão (confirmada na auditoria):** o roteamento multi-tipo do GTOCore **não é replicável 1:1**
no GTM oficial. O GTNA já adotou a única aproximação correta para a base oficial:
**roteamento por slot** (`gtna$slotAcceptsRecipe`) + **`activeRecipeType` como espelho de UI**
(fórmula exata do `MachineModeFancyConfigurator`). Isso está implementado e é o comportamento
equivalente observável pelo jogador.

> ✅ **A mecânica "mudar o modo do multibloco conforme a receita do pattern" já está em
> paridade funcional com o que o jogador vê no GTOCore.** O que falta para 1:1 são as
> *features de conveniência* listadas abaixo — não a mecânica central.

---

## 2. Delta de features — o que falta para 1:1

Comparando `GTLCore.MEPatternBufferPartMachine` (994 linhas) com
`GTNAMEPatternBufferPartMachine` (2274 linhas):

### 2.1 Presentes no GTNA ✅
- Tiers múltiplos com capacidades distintas (9/21/32/72) + itens de upgrade lossless.
- Inventário interno por slot (`InternalSlot`) com refund ao trocar pattern.
- Cache de receita por slot + invalidação ao editar config.
- Roteamento por slot + modo preferido/derivado por slot.
- Persistência break/place (drop salva estado).
- Circuito por slot + especialização item/fluido/circuito.
- Rename custom + grupo de terminal (AE2 `PatternContainerGroup`).

### 2.2 Ausentes ou divergentes no GTNA 🔴 (candidatos a port 1:1)

| Feature GTLCore | Estado GTNA | Prioridade | Esforço |
|---|---|---|---|
| **`cacheRecipe[]` toggle por slot** (liga/desliga cache por slot) | ✅ **portado** — `cacheRecipe` por slot no `slotConfigs` (persistido), toggle no painel de config, tooltip "Recipe cached" no slot (igual à referência) | Média | Baixo |
| **`keepByProduct`** (manter subprodutos no slot em vez de devolver à rede) | ✅ **portado** — toggle no painel de configuradores; OFF (default GTLCore) compara só o output primário no matching | Média | Médio |
| **Circuito embarcado** (`embeddedCircuitConfig` + `embedCircuitToPatterns` + `removeAllPatternCircuits` + `skipExistingCircuitPatterns`) | ✅ **portado** — `GTNAPatternCircuitHelper` (extract/with/without circuit em `AEProcessingPattern` via `IntCircuitBehaviour.isIntegratedCircuit`, sem o campo de fork `GTItems.INTEGRATED_CIRCUIT`) + UI: input de circuito, toggle skip-existing, ações "Embed"/"Remove" em massa | Média | Médio |
| **Catalyst inventories** (`catalystItems[]`/`catalystFluids[]` + `reCalculateCatalyst*Map` + `testCatalyst*`) — insumos catalisadores não-consumidos | ✅ **portado** — lógica (`catalystItems`/`catalystFluids` + persistência + `testCatalyst*` no match) **e UI** (linhas de ghost slots item/fluido no painel de config por slot + lang keys, seguindo o padrão das linhas existentes em vez do docked manager do GTLCore) | Alta | Alto |
| **Proxy buffer** (`MEPatternBufferProxyPartMachine`) | ❌ (adiado por design, ver task-manager) | Baixa | Alto |
| **Copy/Paste de config** (`copyFromTag`/`pasteFromTag` via data stick) | 🟡 **API pronta** (`copyBufferToTag`/`pasteBufferFromTag` com versionamento; faltam os itens de comportamento estilo GTLCore `Copy/Cut`) | Média | Médio |
| **Jade provider** (`MEPatternBufferProvider`/`...ProxyProvider`) | ⚠️ GTNA tem Jade p/ multiblocos, falta p/ buffer | Baixa | Baixo |
| **`isHiddenTerminal`** toggle | ✅ **portado** (`hiddenInTerminal` + override `isVisibleInTerminal` + toggle no painel) | Baixa | Baixo |
| **Ticking AE2 otimizado** (`tickingRequest`/`TickRateModulation` por slot) | ⚠️ verificar necessidade | Média | Médio |

### 2.3 Divergências de design (GTNA ≠ referência, por escolha ou por base)
- **NBT no pattern item:** GTLCore/GTOCore gravam receita/estado no item; o GTNA **removeu
  isso de propósito** (fonte única de verdade = `slotConfigs`) para eliminar estado-fantasma
  quando um pattern migra de buffer. **Manter a divergência** — é uma melhoria, não um gap.
- **Matching de modo:** GTNA usa `ModeIdMatcher` estrito (sem o fuzzy `contains("saw")`).
  **Manter a divergência** — o fuzzy da referência era fonte de regressão.

---

## 3. Recomendação de rota para o "1:1"

1. **Não trocar a base** (GTM oficial) — o roteamento multi-tipo já está em paridade funcional.
2. **Portar as features de conveniência da seção 2.2** em ordem de prioridade:
   - **Alta:** catalyst inventories (a mais usada em automação pesada).
   - **Média:** `cacheRecipe[]` toggle, `keepByProduct`, circuito embarcado, copy/paste.
   - **Baixa:** proxy, Jade, `isHiddenTerminal`.
3. Cada port deve vir **com um caso de teste** no `src/test` (padrão GTLCore) quando a lógica
   for pura, ou validação `runData` quando tocar registro/recurso.

!!! warning "Bloqueador independente"
    Antes de expandir features, **corrigir `Int128.multiply` e `Int128.divideNew`** (achado #20
    da auditoria) — bugs reais de aritmética que comprometem a matemática de energia do
    Nexus Flux Matrix e qualquer cálculo de 128 bits futuro.
