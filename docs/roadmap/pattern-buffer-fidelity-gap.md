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

### 2.2 Ausentes ou divergentes no GTNA ✅ (todas portadas)

| Feature GTLCore | Estado GTNA | Prioridade | Esforço |
|---|---|---|---|
| **`cacheRecipe[]` toggle por slot** (liga/desliga cache por slot) | ✅ **portado** — `cacheRecipe` por slot no `slotConfigs` (persistido), toggle no painel de config, tooltip "Recipe cached" no slot (igual à referência) | Média | Baixo |
| **`keepByProduct`** (manter subprodutos no slot em vez de devolver à rede) | ✅ **portado** — toggle no painel de configuradores; OFF (default GTLCore) compara só o output primário no matching | Média | Médio |
| **Circuito embarcado** (`embeddedCircuitConfig` + `embedCircuitToPatterns` + `removeAllPatternCircuits` + `skipExistingCircuitPatterns`) | ✅ **portado** — `GTNAPatternCircuitHelper` (extract/with/without circuit em `AEProcessingPattern` via `IntCircuitBehaviour.isIntegratedCircuit`, sem o campo de fork `GTItems.INTEGRATED_CIRCUIT`) + UI: input de circuito, toggle skip-existing, ações "Embed"/"Remove" em massa | Média | Médio |
| **Catalyst inventories** (`catalystItems[]`/`catalystFluids[]` + `reCalculateCatalyst*Map` + `testCatalyst*`) — insumos catalisadores não-consumidos | ✅ **portado** — lógica (`catalystItems`/`catalystFluids` + persistência + `testCatalyst*` no match) **e UI** (linhas de ghost slots item/fluido no painel de config por slot + lang keys, seguindo o padrão das linhas existentes em vez do docked manager do GTLCore) | Alta | Alto |
| **Proxy buffer** (`MEPatternBufferProxyPartMachine`) | ✅ **portado** — `GTNAMEPatternBufferProxyPartMachine` (UV) vinculado por data stick: buffer guarda a pos no shift-right-click, proxy aplica no right-click e delega `getRecipeHandlers()` aos handlers de slot do buffer; invalidação de cache notifica os proxies; registrado com config toggle (adaptação: o GTLCore roteia por ME handler traits dedicados, que não existem na base GTM oficial) | Baixa | Alto |
| **Copy/Paste de config** (`copyFromTag`/`pasteFromTag` via data stick) | ✅ **portado** — API versionada (`copyBufferToTag`/`pasteBufferFromTag`) + itens **Pattern Buffer Copy Card** e **Cut Card** (sneak-copy / cut, right-click paste; ocupados nunca sobrescritos; "cut" limpa o buffer de origem) | Média | Médio |
| **Jade provider** (`MEPatternBufferProvider`/`...ProxyProvider`) | ✅ **portado** — `GTNAPatternBufferProvider`: conteúdo mesclado dos slots (itens + fluidos, formato NBT idêntico ao GTM oficial) + contagem de proxies vinculados; registrado no `GTNAJadePlugin` | Baixa | Baixo |
| **`isHiddenTerminal`** toggle | ✅ **portado** (`hiddenInTerminal` + override `isVisibleInTerminal` + toggle no painel) | Baixa | Baixo |
| **Output ME diferido + drain pump** (`tickingRequest`/`TickRateModulation`) | ✅ **portado (híbrido)** — `pendingNetworkOutput` (persistido; formato NBT do `AEUtils.createListTag`: chave + `real`) guarda **só a sobra** da inserção inline; `NetworkOutputTicker` (`IGridTickable`, `TickingRequest(5, 80, false, true)`, `SLEEP`/`SLOWER`/`URGENT`) drena com `poweredInsert` em lotes de até 64 ops, desistindo após 5 falhas seguidas; `alertDevice` acorda o pump na transição vazio → não-vazio (o `Ticker` do GTLCore não faz isso e pode ficar dormindo com pendência). O `refund()` dos `InternalSlot` manda a sobra para o mesmo buffer em vez de deixá-la presa no slot | Alta | Médio |

### 2.3 Divergências de design (GTNA ≠ referência, por escolha ou por base)
- **Layout da UI do buffer (divergência visual deliberada, G-0009 / commit `7822893`):** o GTNA usa
  uma página de duas colunas (`352x248`) — grade de patterns à esquerda, painel de configuração do
  slot **docado** à direita — mais o side tab **Buffer Tools** para as ações de manutenção
  (cache de receitas, embed/remove de circuito). A referência (GTLAdditions/GTOCore) usa um docked
  manager próprio. **Manter a divergência**, por dois motivos: (1) o GTNA guarda a especialização em
  `slotConfigs`, não no NBT do pattern item, então as ghost rows de item/fluido **e** as de catalyst
  são obrigatórias — um clone 1:1 não é possível; (2) **licença** — o GTLAdditions é **GPL-3.0** e o
  GTNA é **LGPLv3**: copiar código de lá seria violação, não só plágio. Nenhuma linha foi copiada.
- **NBT no pattern item:** GTLCore/GTOCore gravam receita/estado no item; o GTNA **removeu
  isso de propósito** (fonte única de verdade = `slotConfigs`) para eliminar estado-fantasma
  quando um pattern migra de buffer. **Manter a divergência** — é uma melhoria, não um gap.
- **Matching de modo:** GTNA usa `ModeIdMatcher` estrito (sem o fuzzy `contains("saw")`).
  **Manter a divergência** — o fuzzy da referência era fonte de regressão.
- **Output ME híbrido (inline + sobra bufferizada) em vez de puramente diferido.** O GTLCore
  **nunca** insere na hora: todo output vai para o `buffer` e o `Ticker` drena em até 80 ticks. O
  GTNA tenta a inserção inline (latência zero no caminho comum) e só manda a **sobra** para o
  `pendingNetworkOutput`. Motivo: escala — no 1:1 puro toda craft concluída acorda o ticker daquele
  buffer (pressão no `TickManagerService` do AE2, que tem orçamento de ticks por tick de jogo) e o
  teto de 64 ops/tick passa a valer para *todo* output; no híbrido o pump só acorda quando a rede
  está de fato saturada. **Manter a divergência** — mesma garantia de não perder nada, com menos
  latência e menos pressão no tick manager.
- **Sem backpressure de output: a sobra é bufferizada em vez de voidada.** Evidência de que hoje há
  perda real: `RecipeRunner.handleContents()` (linhas 232-236) **voida** a sobra de output quando o
  controller é `IVoidable` (`canVoidRecipeOutputs(cap)`) e `PASS_NO_CONTENTS` conta como sucesso —
  então o `onRecipeFinish()` que ignora o retorno de `handleRecipeIO(IO.OUT)` não é o único caminho
  de descarte (e, para controllers voidáveis, o próprio match simulado pula a checagem de espaço,
  linha 82). Com o buffer nada é voidado; em troca, a máquina deixa de "esperar" com a rede cheia —
  acumula e entrega depois, como o GTLCore. **Divergência consciente.**

---

## 3. Recomendação de rota para o "1:1"

1. **Não trocar a base** (GTM oficial) — o roteamento multi-tipo já está em paridade funcional.
2. **Portar as features de conveniência da seção 2.2** — ✅ **todas portadas**. Ordem usada:
   catalyst inventories → `cacheRecipe[]`/`keepByProduct`/circuito embarcado → copy/paste →
   proxy/Jade/`isHiddenTerminal` → output ME diferido + drain pump.
3. Cada port deve vir **com um caso de teste** no `src/test` (padrão GTLCore) quando a lógica
   for pura, ou validação `runData` quando tocar registro/recurso.

!!! warning "Bloqueador independente"
    Antes de expandir features, **corrigir `Int128.multiply` e `Int128.divideNew`** (achado #20
    da auditoria) — bugs reais de aritmética que comprometem a matemática de energia do
    Nexus Flux Matrix e qualquer cálculo de 128 bits futuro.
