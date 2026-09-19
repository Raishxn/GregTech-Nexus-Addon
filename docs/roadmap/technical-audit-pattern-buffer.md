# 🔍 Auditoria Técnica — Estrutura do GTNA + Pattern Buffer Multi-Modo

!!! info "Metadados"
    **Data:** 2026-09 · **Escopo:** Estrutura completa do mod + feature de pattern buffer multi-modo
    **Projetos de referência:** GTOCore-Main, GTO-GregTech-Modern (fork GTM do GTO), GTLCore e **source oficial GTCEu 7.5.3** (`/home/raishxn/MineProjects/GTCEu-7.5.3`)
    **Verificações:** 203 arquivos Java, build/CI/workflows, mixins, datagen, git status, bytecode/source do GTM 7.5.3 (`javap` + leitura direta do source)
    **Manifest correlato:** [`docs/roadmap/multiblock-port-manifest.md`](multiblock-port-manifest.md)

---

## 📊 Resumo Executivo

| Área | Status | Nota |
|---|---|---|
| Estrutura de pacotes | 🟡 Boa com ressalvas | 8/10 |
| Higiene de repositório/build | 🔴 Problemas reais | 5/10 |
| Arquitetura do código | 🟡 Monolitos + dead code | 6/10 |
| Pattern Buffer multi-modo | 🟠 Mecânica ok, fundação errada | 5/10 |
| Testes | 🔴 **Zero** (0 testes, 0 gametests) | 0/10 |
| Docs/CI | 🟢 Acima da média da comunidade | 9/10 |

**Pontos fortes** (muitos addons da comunidade não têm): docs MkDocs com CI + i18n, manifest de port mecânico com IDs registráveis, spotless configurado, datagen separado em `src/generated`, estrutura de pacotes `api/common/integration/mixin` correta, e o handler por-slot do pattern buffer (`GTNAPatternBufferRecipeHandler`) — equivalente conceitual correto do `InternalSlotRecipeHandler` do GTOCore.

---

## 🔴 Achados Críticos

### 1. Diretório `net/` compilando no build

`net/minecraftforge/common/crafting/**` — 6 classes Forge **decompiladas** na raiz do projeto, sendo **compiladas** (o `sourceSets.main.java` pega a raiz em setups LegacyForge/ModDevGradle). Risco de conflito com as classes reais do Forge.

**Ação:** deletar `net/` e `site/` (output do mkdocs não deve ser versionado — o workflow de docs já faz `gh-deploy`); adicionar `site/` e `net/` ao `.gitignore`.

### 2. Dead code: `gtna$applyPatternBufferMode` nunca é chamado

Confirmado por busca global: `IPatternBufferModeHost.gtna$applyPatternBufferMode` (implementado em `WorkableElectricMultipleRecipesMachine.java:254` e `SteamMultiMachineBase.java:120`) **não tem nenhum call site**. O multibloco **nunca** troca `activeRecipeType` pela receita do pattern — metade da feature simplesmente não roda. É a causa raiz do "nunca ficou perfeito igual GTOCore".

**Ação:** wire-up na Fase 1 (ver Deep-Dive abaixo).

### 3. Descrição Lorem Ipsum no `mods.toml`

`src/main/resources/META-INF/mods.toml:27` — "Example Description... Lorem Ipsum". Primeira coisa que o jogador vê no painel de mods.

### 4. Zero testes

- `src/test` não existe. GTLCore (referência) tem `src/test/java/org/gtlcore/gtlcore/...` com testes JUnit.
- Verificado no jar/source oficial do GTM 7.5.3: **não há infraestrutura de GameTest publicada pela GTM** — nenhum addon da comunidade tem gametests de máquina. O padrão real é **JUnit puro para lógica** + teste manual/runtime.
- O `build.gradle` já tem `forge.enabledGameTestNamespaces` configurado (herdado do template) — abordagem híbrida viável (JUnit agora, gametest depois).

### 5. Dependências duras de soft-deps

`mods.toml` (~linhas 44-54): `ad_astra` e `sgjourney` com `mandatory = true` — qualquer usuário sem Ad Astra/Stargate Journey não carrega o mod inteiro, mesmo sem usar as máquinas espaciais.

**Ação:** `mandatory = false` + guards `ModList.isLoaded(...)` nos pontos de uso (máquinas espaciais).

---

## 🟠 Achados Altos

### 6. `gtna$matchesModeId` com hardcode de regressão oculta

`IPatternBufferModeHost.java:42-43`:

```java
return ("saw".equals(requested) || "cutting_saw".equals(requested)) &&
        (path.contains("cutter") || path.contains("saw"));
```

Um pattern de **cutter** aceita modo **"saw"** e vice-versa — matching falso que mascara bugs de roteamento. O GTOCore faz matching por **igualdade exata de `GTRecipeType`** (objeto), nunca por string fuzzy.

**Ação:** extrair para `ModeIdMatcher` pura (Fase 1), com match exato + sufixo controlado.

### 7. NBT mutado no item do pattern

`GTNAMEPatternBufferPartMachine` grava `gtnaPatternRecipeId`/`gtnaPatternModeId` **no ItemStack do pattern** (`saveSlotData`, ~linha 860). Problemas:

- Estado mora no item: mover o pattern para outro buffer carrega estado "fantasma";
- O slot já tem fonte de verdade própria (`slotConfigs[i]` com `cachedRecipeId`/`preferredModeId`) — **duas fontes de verdade**, e a do item é a redundante.

**Ação:** remover os tags do item; manter tudo em `GTNAPatternBufferSlotConfig`.

### 8. `activeRecipeType` é global — incompatível com multi-threads

O próprio código já registra isso (comentário em `gtna$slotAcceptsRecipe`, ~linha 427): *"The controller's active recipe type is global, so it cannot be used as the routing state when several GTNA recipe threads run at once."* O routing **por slot** está correto e é o que faz a mecânica funcionar; o "modo global" só pode existir como **espelho de UI** (ver Deep-Dive).

### 9. Monolitos

| Arquivo | Linhas | Problema |
|---|---|---|
| `common/data/AnnihilateGeneratorA.java` | **6.333** | ~95% é aisle-pattern de estrutura |
| `common/data/AnnihilateGeneratorB.java` | **5.994** | Variante de A → duplicação massiva |
| `common/data/GTNAMachines.java` | 2.915 | Registro de tudo num arquivo |
| `GTNAMEPatternBufferPartMachine.java` | 2.310 | Mistura inventário+sync+UI+resolvers+modos |
| `data/recipe/GTNAMachineRecipes.java` | 1.271 | Aceitável (arquivo de receitas) |

**Ação (Fase 3):** aisles de estrutura → `common/data/multiblock/` (já é o padrão de EyeOfHarmony/EyeOfWood — padronizar); GTNAMachines → dividir por domínio (GTOCore usa lotes `machines/MultiBlockA..H.java`); PatternBuffer → extrair `PatternBufferUI`, `PatternSlotResolver`, `ModeRegistry`.

### 10. `EXAMPLE_REGISTRATE` órfão

`GTNACORE.java:26` cria **um segundo** `GTRegistrate` (nome de template) que ninguém usa — o real é `GTNARegistry.REGISTRATE`. Dois registrates no mesmo modid = risco de registros órfãos.

**Ação:** deletar (Fase 0).

---

## 🟡 Achados Médios

11. **Comentários que mentem** em `GTNAMultipleRecipesLogic.java`: linhas 65, 369, 405-406 — comentários de "sumário" ("(... mantidos iguais)", "(Código original...)") seguidos do código real logo abaixo. Armadilha de manutenção.
12. **Magic numbers**: `searchLimit = 30` (GTNAMultipleRecipesLogic:131), `Math.max(4, remaining/...)` (217), `maxLength = 20` (446), `PATTERNS_PER_PAGE = 54`. Extrair para constantes nomeadas.
13. **`ThreadMultiplierStrategy`** usa `HashMap<MultiblockMachineDefinition, Integer>` estático — estado global paralelo ao registrate. Torne-o imutável pós-registro (hoje `register()` aceita escrita a qualquer momento).
14. **Strings hardcoded na UI**: `"Parallels: "`, `"Active Threads: "`, `"Idle - Waiting for inputs..."`, `" -> "`, `" x"` (em `WorkableElectricMultipleRecipesMachine` e `GTNAMultipleRecipesLogic`) — não passam pelo lang. Usar `Component.translatable` (o datagen de lang `GTNALangProvider` + `pt_br.json` já existem).
15. **`isRecipeAlreadyActive`** bloqueia por `recipe.id` quando paralelo > 1 — impede rodar a *mesma* receita em threads distintas. Documentar/parametrizar a decisão.
16. **CI não roda testes nem spotless**: `gradle.yml` só roda `./gradlew build`. Adicionar `./gradlew build spotlessCheck test`.
17. **Árvore de trabalho suja**: 8 arquivos modificados + `CoilWorkableElectricMultipleRecipesMachine.java` (novo, 70 linhas, herda `GTValues` sem usar) sem commit. Commitar antes de qualquer refactor.
18. **Nome de pacote** `common/machine/multiMachineBase` — camelCase em pacote; convenção Java é minúsculas (`multiblock/base`).
19. **`saveCustomPersistedData`/`loadCustomPersistedData`** em `GTNAMultipleRecipesLogic` não re-notifica handlers pós-load (o GTOCore sempre re-notifica). Menor.
20. **`Int128` reimplementado** — 687 linhas de aritmética custom = campo de bugs silenciosos. Se for para energia além de `long`, mantenha, mas **teste unitário é mandatório** (`Int128Test` na Fase 2).

---

## 🔬 Deep-Dive: Pattern Buffer Multi-Modo — Por que o GTOCore "funciona" e o GTNA não

### Como o GTOCore realmente funciona (código verificado)

Em `GTOCore-Main/.../MEPatternBufferPartMachine.java` + `MultiMachineModeFancyConfigurator.java` + o fork GTM deles:

1. **O modo mora no BUFFER, não no controller.** O buffer tem `List<GTRecipeType> recipeTypes` (tipos que o controller suporta) e `GTRecipeType recipeType` (filtro selecionado, `null` = "todos") — seletor `MultiMachineModeFancyConfigurator` na UI do buffer.
2. Cada pattern é decodificado num `InternalSlot` que **cacheia o GTRecipe resolvido** junto com o tipo da receita.
3. O `InternalSlotRecipeHandler` expõe cada slot como `RecipeHandlerUnit` — a busca de receita do controller **não consulta a recipe map por tipo**; recebe receitas prontas dos slots.
4. O fork GTM deles mudou o cerne: `fullModifyRecipe` valida com `GTRecipeType.available(definition.recipeType, getAvailableRecipeTypes())` — o controller aceita **qualquer tipo disponível**; o "modo ativo" virou **estado de UI**.
5. `setRecipeType` do buffer só faz `machine.getRecipeLogic().markLastRecipeDirty()` — troca de modo = invalidar cache de busca, não "trocar o motor".

### O que o GTNA faz hoje (e por que travou)

- ✅ Ponto 3 já implementado (`GTNAPatternBufferRecipeHandler` com RHLs por slot) e routing por slot (`gtna$slotAcceptsRecipe`) — **a mecânica de rodar múltiplos tipos já funciona**;
- ❌ O "modo do controller" (`setActiveRecipeType`) ficou órfão: `gtna$applyPatternBufferMode` existe mas ninguém chama; `resolveDerivedMode` é computado e **só vira tooltip na UI** do buffer;
- ❌ `GTNAMultipleRecipesLogic` tem **dois pipelines** (`tryStartRecipe` manual duplicando `getRecipeModifier`, além do `searchRecipe` cego por todos os tipos no fallback);
- ❌ Matching por string fuzzy com hardcode saw/cutter (achado #6).

### Por que "igual GTOCore" não era possível no GTM oficial

No GTM 7.5.3 oficial, `IRecipeLogicMachine` **não tem** `getAvailableRecipeTypes()` nem handler-units — `RecipeLogic` busca **só o tipo ativo**:

```java
// RecipeLogic.java:334 (source oficial GTCEu 7.5.3)
public @NotNull Iterator<GTRecipe> searchRecipe() {
    return machine.getRecipeType().searchRecipe(machine, r -> true);
}
// WorkableMultiblockMachine.java:304
public GTRecipeType getRecipeType() { return recipeTypes[activeRecipeType]; }
```

Com N threads simultâneas de tipos distintos (cutter + lathe + bender ao mesmo tempo), um único `activeRecipeType` global é **matematicamente insuficiente**. O GTOCore contornou isso **modificando o GTM**; o GTNA não pode. A solução correta é a que o código já fazia sem perceber: **modo por slot/thread, com o modo global como espelho de UI**.

### 🎯 Arquitetura-alvo (curadoria)

```
┌────────────────────────────────────────────────────────────────┐
│ GTNAMultipleRecipesLogic (único ponto de início de thread)     │
│  startRecipeInternal(GTRecipe base, slotHint)                  │
│    1. dry-run match (matchContents SIMULATE)                   │
│    2. host.gtna$applyPatternBufferMode(modeId, recipe) ← espelho│
│    3. modificadores (paralelo/OC/hatches) via UM pipeline      │
│    4. IO real + activeRecipes.add                              │
│    5. provider.gtna$onRecipeStarted(recipe)                    │
└────────────────────────────────────────────────────────────────┘
```

Mudanças concretas da **Fase 1**:

**1. Wire-up do espelho de modo** — em `startRecipeInternal`, após o dry-run bem-sucedido:

```java
if (machine instanceof IPatternBufferModeHost host) {
    host.gtna$applyPatternBufferMode(recipe.getType().registryName.toString(), recipe);
}
```

E a implementação correta do host — **fórmula exata da própria GTM** (copiada de `MachineModeFancyConfigurator.setActiveRecipeTypeAndUpdateTickSubs`, source oficial):

```java
@Override
public boolean gtna$applyPatternBufferMode(String modeId, GTRecipe recipe) {
    for (int i = 0; i < getRecipeTypes().length; i++) {
        if (ModeIdMatcher.matches(modeId, getRecipeTypes()[i])) {
            int newType = i;
            boolean needUpdate = !keepSubscribing() && getActiveRecipeType() != newType;
            setActiveRecipeType(newType);   // @Persisted: NBT + sync de rede automáticos
            if (needUpdate) getRecipeLogic().updateTickSubscription();
            return true;
        }
    }
    return false;
}
```

**2. `ModeIdMatcher` pura e testável** (substitui o fuzzy+hardcode):

```java
public final class ModeIdMatcher {
    /** Match exato: namespace:path, ou sufixo exato do path ("cutter" matcha gtna:big_cutter). */
    public static boolean matches(String requested, @Nullable GTRecipeType type) {
        if (requested == null || type == null || type.registryName == null) return false;
        String req = requested.trim().toLowerCase(Locale.ROOT).replace('_', ' ');
        ResourceLocation rl = type.registryName;
        String full = rl.toString().toLowerCase(Locale.ROOT).replace('_', ' ');
        String path = rl.getPath().toLowerCase(Locale.ROOT).replace('_', ' ');
        return req.equals(full) || req.equals(path) || path.endsWith("/" + req) || path.endsWith(" " + req);
    }
}
```

Nada de `contains("saw")`. Dois tipos cujo path termina com o mesmo sufixo colidem — nesses casos o modo do slot deve ser o **id completo**.

**3. Fonte única de verdade** — deletar `PATTERN_RECIPE_ID_TAG`/`PATTERN_MODE_ID_TAG` do ItemStack; `slotConfigs[i]` manda (com `cachedRecipeId` + `preferredModeId` + `derivedModeId`). Elimina a classe inteira de "limpeza de modo stale" (`resolveAndCacheSlotRecipe`, linhas 926-937).

**4. Unificar modificadores** — `tryStartRecipe` duplica `getRecipeModifier`/ParallelLogic/OC; manter **um** caminho: `ModifierFunction` do host aplicado na receita base. Ordem oficial de referência (`RecipeLogic.setupRecipe`, source oficial): retry de `lastRecipe` → `beforeWorking(recipe)` → `handleRecipeIO(IN)` → status/progress/duration.

**5. AUTO nunca "trava"** — `preferredModeId` em branco = AUTO (aceita tudo); `derivedModeId` é só display.

---

## 🧪 Plano de Testes (padrão real da comunidade GTM)

### Nível 1 — JUnit 5 (`src/test/java`) — copy do padrão GTLCore

```gradle
// build.gradle
dependencies {
    testImplementation platform('org.junit:junit-bom:5.10.2')
    testImplementation 'org.junit.jupiter:junit-jupiter'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}
test { useJUnitPlatform() }
```

Alvos imediatos (todos **sem Minecraft bootstrap**, lógica pura ou extraível):

- `ModeIdMatcherTest` — nova classe (exato, sufixo, colisões, null/blank);
- `Int128Test` — 687 linhas de aritmética custom;
- `GTRecipe2IntBiMultiMapTest`, `CacheStateTest`, `NumberUtilsTest`, `StructureSlicerTest`;
- `IPatternBufferModeHostTest` após extrair o matching para `ModeIdMatcher`.

### Nível 2 — GameTests Forge (`@GameTest`) — Fase 2+

O build **já** configura `forge.enabledGameTestNamespaces`. Estruturas de teste não precisam de `.mb`: registre NBTs de estrutura pequenos em `src/test/resources/data/gtna/gametest/structures/...` via vanilla structure block. Esqueleto:

```java
public class GTNAGametest {
    @GameTest(template = "gtna:pattern_buffer_mode_switch")
    public static void patternBufferSwitchesControllerMode(GameTestHelper helper) {
        // 1. formar multibloco pequeno com 2 recipe types;
        // 2. ME Pattern Buffer + pattern de receita do tipo B;
        // 3. suprimento via import hatch cheat + helper.runAfterDelay;
        // 4. assert: controller.getActiveRecipeType() == idx(B)
        helper.succeed();
    }
}
```

⚠️ **Caveat honesto:** gametests de máquina exigem AE2+GTCEu no ambiente de teste e a GTM oficial não dá helpers prontos. Comece pelos **JUnits** (retorno garantido); gametest como Fase 2, priorizando 1 multibloco simples de steam antes do pattern buffer.

### Nível 3 — CI

```yaml
- name: Build + Quality
  run: ./gradlew build spotlessCheck test
```

---

## ✅ Checklist Priorizado de Execução

### Fase 0 — Higiene (~30 min, risco zero)

- [ ] `rm -rf net/ site/` + `.gitignore` (`site/`, `net/`, `build/`)
- [ ] Deletar `EXAMPLE_REGISTRATE` de `GTNACORE.java`
- [ ] Corrigir descrição do `mods.toml` + `ad_astra`/`sgjourney` → `mandatory = false` (com guards `ModList.isLoaded`)
- [ ] Commit do WIP pendente (8 arquivos + `CoilWorkableElectricMultipleRecipesMachine`)

### Fase 1 — Feature pattern buffer multi-modo (o objetivo)

- [ ] Criar `ModeIdMatcher` (sem hardcode saw/cutter)
- [ ] Wire-up: `gtna$applyPatternBufferMode` chamado em `startRecipeInternal` + `updateTickSubscription` (fórmula GTM)
- [ ] Remover NBT do pattern item; consolidar em `slotConfigs`
- [ ] Unificar pipeline de modificadores (ordem oficial: match → modify → beforeWorking → IO IN)
- [ ] Remover dead code restante (ou completar o que falta)

### Fase 2 — Testes

- [ ] JUnit 5 + primeiros 6 testes unitários
- [ ] CI com `spotlessCheck test`
- [ ] (Opcional) 1 gametest de steam simples

### Fase 3 — Refactor estrutural

- [ ] Split AnnihilateGeneratorA/B → aisles em `common/data/multiblock/`
- [ ] Split `GTNAMEPatternBufferPartMachine` (UI/resolver/mode)
- [ ] Split GTNAMachines por domínio
- [ ] Internacionalizar strings de UI

---

## 📎 Verificações no Source Oficial GTCEu 7.5.3 (append de 2ª passada)

Esta seção registra as confirmações feitas com o **source oficial** (`/home/raishxn/MineProjects/GTCEu-7.5.3`), que fecharam as incertezas da primeira passada (que usava bytecode do jar slim):

1. **Busca de receita usa só o tipo ativo** — `RecipeLogic.searchRecipe()` (linha 334) chama `machine.getRecipeType().searchRecipe(...)`; `getRecipeType()` retorna `recipeTypes[activeRecipeType]` (linha 304). Não existe concatenação de todos os tipos no GTM oficial (isso é invenção do fork do GTO). Buscar por tipo com `searchRecipe` por índice é o caminho correto.

2. **O GTM oficial JÁ tem a UI de troca de modo** — `IFancyUIMachine.java:104` anexa `new MachineModeFancyConfigurator(rLMachine)` a **todo** `IRecipeLogicMachine`. O tab "Machine Mode" já existe na UI do multibloco hoje; o que faltava era só o **automatismo** (atualizar `activeRecipeType` quando o pattern entrega receita de outro tipo).

3. **Fórmula exata do "set mode"** — `MachineModeFancyConfigurator.setActiveRecipeTypeAndUpdateTickSubs` (linhas 66-72):

```java
boolean needUpdateTickSubs = !machine.keepSubscribing() && activeRecipeType != machine.getActiveRecipeType();
machine.setActiveRecipeType(activeRecipeType);
if (needUpdateTickSubs) {
    machine.getRecipeLogic().updateTickSubscription();
}
```

   E o campo em si é `@Getter @Setter @Persisted private int activeRecipeType;` (WorkableMultiblockMachine:63-66) — **persistência em NBT + sync de rede automáticos** via ldlib. Não precisa de packet manual nem `requestSync`.

4. **⚠️ Armadilha confirmada: `setRecipeType(GTRecipeType)` não troca modo** — `WorkableMultiblockMachine.java:312-319` faz `recipeTypes[activeRecipeType] = newType;` (**substitui o tipo no array**, anotado `@ApiStatus.Internal @VisibleForTesting`). Chamar achando que troca de modo **corrompe a máquina em runtime**. Nunca usar; sempre `setActiveRecipeType(int)`.

5. **Ordem de `setupRecipe` oficial** — retry de `lastRecipe` primeiro → `beforeWorking(recipe)` → `handleRecipeIO(IN)` → status/progress/duration (RecipeLogic.java:393-411). Há também `lastFailedMatches` (cache de falhas para UI).

### Impacto no plano

| Item da Fase 1 | Status com o source oficial |
|---|---|
| `ModeIdMatcher` sem hardcode | ✅ segue como planejado |
| Wire-up do modo automático | ✅ fórmula **exata** da GTM (setter + updateTickSubs) |
| Remover NBT do pattern item | ✅ segue como planejado |
| Unificar pipeline de modificadores | ✅ ordem oficial de referência |
| `searchRecipe` por tipo no logic | ✅ confirmado que não existe API melhor no GTM oficial |

---

## 💡 Conclusão — Resposta ao objetivo original

> *"criar a funcionalidade do meu pattern buffer mudar o modo de multiblocos dependendo da receita... nunca consegui fazer de forma perfeita igual o GTOCore/GTOlib"*

O objetivo não era alcançável por **replicação direta**: o GTOCore executa essa mecânica num GTM **modificado por eles** (controller aceita todos os recipe types simultaneamente via handler-units). No GTM oficial, o "modo ativo" é um inteiro global — impossível representar N threads de tipos distintos.

O GTOCore "mente" na UI: o que muda de modo de verdade lá é o **buffer** (por buffer/slot), e o controller aceita qualquer tipo. O código do GTNA já chegou 80% dessa conclusão sozinho (routing por slot) — faltava: (1) wire do espelho de modo pro display, (2) matar o dead code, (3) fonte única de verdade (`slotConfigs`, não NBT do item), (4) matching sem hardcode.

**Regra de ouro extraída:** *mecânica de roteamento por slot + `activeRecipeType` como espelho de UI atualizado via `setActiveRecipeType(i)` + `updateTickSubscription()` — exatamente o que o GTOCore faz, com a fundação correta do GTM 7.5.3 oficial.*
