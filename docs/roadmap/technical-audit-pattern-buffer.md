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

- `src/test` não existia (agora existe, ver Fase 2). **Correção factual (2ª passada):** o GTLCore **não usa JUnit** — seus testes em `src/test/java/org/gtlcore/gtlcore/...` são classes com `public static void main` + helper `require()` (asserts manuais, ex: `WirelessTerminalGridResolverTest`), sem `useJUnitPlatform` no build. Esse padrão é **mais simples** e foi o adotado (ver Fase 2).
- Verificado no jar/source oficial do GTM 7.5.3: **não há infraestrutura de GameTest publicada pela GTM** — nenhum addon da comunidade tem gametests de máquina. O padrão real é **teste puro para lógica** + teste manual/runtime.
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
20. **`Int128` reimplementado** — 687 linhas de aritmética custom. ✅ **CORRIGIDO E VALIDADO (Fase 2, `Int128Test`):**
    - A suspeita de "campo de bugs silenciosos" era real: `multiply(Int128)` errava **~7%
      dos casos** (7298/100000) por perda de carry entre os limbs de 32 bits; `divideNew(long)`
      e `divide(long)` erravam para dividendos grandes/negativos (mesma família de bug limb).
    - **Correção:** os três métodos agora roteiam pelos caminhos já verificados —
      `multiply*` por `toBigInteger`/`fromBigInteger` (roundtrip exato, 0/100k erros);
      `divide(long)`/`divideNew(long)` pela `divide(Int128, rem)` bit-a-bit.
    - **Validação:** 0 erros em 100.000 casos aleatórios cada (add, subtract, multiply,
      shiftLeft, divide) contra oráculo `BigInteger` + probes de regressão no teste.
    - Quem chama `Int128`: a matemática de energia do **Nexus Flux Matrix** — agora confiável.
    Teste: `src/test/java/com/raishxn/gtna/Int128Test.java`.

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

### Nível 1 — Testes puros (`src/test/java`) — padrão **real** do GTLCore ✅ **IMPLEMENTADO**

**Correção da 1ª passada:** o GTLCore não usa JUnit. O padrão adotado (igual ao deles) é
`main()` + asserts, sem framework — mais leve e sem dor de classpath no ModDevGradle.
Implementado:

```gradle
// build.gradle: sourceSet 'test' (src/test/java) + task `runUnitTests` (JavaExec por classe)
// `check` agora depende de `runUnitTests`.
```

Testes criados e **passando** (`./gradlew runUnitTests` → BUILD SUCCESSFUL):

- `Int128Test` — add/subtract/shiftLeft/negate validados contra `BigInteger` (0 erros em 100k
  casos cada); `multiply` e `divideNew` documentados como **bugs reais** (ver achado #20).
- `ModeIdMatcherTest` — trava as regras de matching (exato + sufixo) e as garantias
  anti-hardcode (saw↛cutter, etc.).

Próximos alvos (todos **sem Minecraft bootstrap**, lógica pura ou extraível): `NumberUtilsTest`,
`StructureSlicerTest`, `GTRecipe2IntBiMultiMapTest`.

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

### Fase 0 — Higiene ✅ **CONCLUÍDA** (commit `d8e1494`)

- [x] `git rm -r net/ site/` + `.gitignore` (`site/`, `net/`) — `build/` já estava ignorado
- [x] Deletado `EXAMPLE_REGISTRATE` de `GTNACORE.java` (confirmado: zero usos)
- [x] Descrição do `mods.toml` substituída; `ad_astra`/`sgjourney` → `mandatory = false`.
      **Nota:** guards `ModList.isLoaded` foram **desnecessários** — nenhuma classe GTNA
      importa `ad_astra`/`sgjourney`/`botarium`/`resourceful*` ainda (essas deps foram
      adicionadas no WIP para o conteúdo espacial futuro). O mod carrega standalone.
- [x] Commit do WIP pendente (snapshot `af94676`, 17 arquivos)
- [x] **Bônus:** `spotlessApply` executado no repositório inteiro (spotless estava
      configurado mas nunca enforced — o CI não o rodava; ver achado #16)

### Fase 1 — Feature pattern buffer multi-modo ✅ **CONCLUÍDA** (commit `d8e1494`)

- [x] `ModeIdMatcher` criado (`api/machine/feature/ModeIdMatcher.java`) — matching estrito
      (exato + sufixo de path), **sem** `contains("saw")` e sem equivalência saw↔cutter.
      Validado por 17 casos de lógica (incl. garantias anti-hardcode).
- [x] Wire-up: `gtna$applyPatternBufferMode` agora é chamado em `tryStartRecipe`
      (após o dry-run `matchContents`, antes de `beforeWorking`). As duas implementações
      (`WorkableElectricMultipleRecipesMachine`, `SteamMultiMachineBase`) usam a **fórmula
      exata da GTM** (setter + `updateTickSubscription()` condicional; `@Persisted` cuida
      de NBT/sync). Dead code eliminado — agora há 1 call site real.
- [x] NBT do pattern item removido: `persistPatternRecipeMetadata` deletado;
      `loadPatternRecipeMetadata` virou migração que **remove** tags legadas de itens
      antigos em vez de carregá-las. `slotConfigs` é a única fonte de verdade.
- [x] Pipeline de modificadores: confirmado **um único caminho de execução**
      (`tryStartRecipe`: parallel → overclock → hatches → match → mode-mirror →
      beforeWorking → IO IN). O `getRecipeModifier` separado serve só ao preview/EMI,
      não é um segundo pipeline de execução — fusão completa fica para a Fase 3.

### Fase 2 — Testes ✅ **CONCLUÍDA** (gametest opcional fica para a Fase 2+)

- [x] `src/test` criado + task `runUnitTests` (padrão GTLCore: `main()` + asserts, sem JUnit —
      correção da 1ª passada que sugeria JUnit). `check` agora depende de `runUnitTests`.
- [x] `Int128Test` — **revelou 2 bugs reais** em `multiply`/`divideNew` (achado #20 🔴).
- [x] `ModeIdMatcherTest` — trava as regras anti-hardcode.
- [x] **🔴 Corrigir `Int128.multiply` e `Int128.divideNew`** — corrigido (commit `5a7129a`) e
      validado com **0 erros em 100.000 casos** vs oráculo `BigInteger`
      (add/subtract/multiply/shiftLeft/divide), com probes de regressão travadas no `Int128Test`.
- [x] `NumberUtilsTest` (formatação K/M/G, tabela `pow95`, tiers de voltagem fake com round-trip,
      `saturatedAdd`/`saturatedMultiply`), `StructureSlicerTest` (semântica exata de
      slice/insert dos aisles, clamps e `q` 1-based) e `GTRecipe2IntBiMultiMapTest`
      (espelho byte-a-byte da estrutura, pois `GTRecipe` exige bootstrap; chave stand-in com
      equals/hashCode por id igual ao `GTRecipe`, + fuzz de 5.000 passos contra modelo de
      referência verificando a invariante dos dois mapas a cada passo).
- [x] CI: `spotlessCheck` + `runUnitTests` adicionados ao `gradle.yml` (commit `4bf0f0c`).
- [ ] (Opcional) 1 gametest de steam simples — Fase 2+, requer AE2+GTCEu no ambiente de teste.

### Fase 3 — Refactor estrutural 🟡 **INICIADA**

- [ ] Split AnnihilateGeneratorA/B → aisles em `common/data/multiblock/`
- [ ] Split `GTNAMEPatternBufferPartMachine` (UI/resolver/mode) — 🟡 **resolver extraído**
      (2759 → 1923 linhas): `PatternSlotResolver` (mesmo pacote) agora carrega o núcleo de
      busca/matching de receita por slot (resolução com preferência de cache, migração de tags
      legados, matching slot/pattern com catalyst + keepByProduct, e os helpers puramente
      estáticos de copy/consume/collect/compare). A máquina mantém o estado persistente e a
      orquestração de UI. Restam: extração da UI (config panel + widgets internos) e do cluster
      de modos (`ModeOption`/cycle/refresh).
- [ ] Split GTNAMachines por domínio
- [ ] Internacionalizar strings de UI
- [ ] Fundir `getRecipeModifier` (preview) com o caminho de execução, se fizer sentido

---

## 📎 Verificações no Source Oficial GTCEu 7.5.3 (append de 2ª passada)

Esta seção registra as confirmações feitas com o **source oficial** (`/home/raishxn/MineProjects/GTCEu-7.5.3`), que fecharam as incertezas da primeira passada (que usava bytecode do jar slim):

1. **Busca de receita usa só o tipo ativo** — `RecipeLogic.searchRecipe()` (linha 334) chama `machine.getRecipeType().searchRecipe(...)`; `getRecipeType()` retorna `recipeTypes[activeRecipeType]` (linha 304). Não existe concatenação de todos os tipos no GTM oficial (isso é invenção do fork do GTO). Buscar por tipo com `searchRecipe` por índice é o caminho correto.

2. **O GTM oficial JÁ tem a UI de troca de modo** — `IFancyUIMachine.attachSideTabs` (~linha 103) anexa `new MachineModeFancyConfigurator(rLMachine)` a todo `IRecipeLogicMachine` **com `getRecipeTypes().length > 1`** (refinamento da 2ª passada: máquinas de tipo único não recebem o tab). O tab "Machine Mode" já existe na UI dos multiblocos multi-tipo hoje; o que faltava era só o **automatismo** (atualizar `activeRecipeType` quando o pattern entrega receita de outro tipo) — implementado na Fase 1.

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
