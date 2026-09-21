# GTNA — Continuity Ledger

Memória operacional do desenvolvimento do GregTech Nexus Addon (GTNA). Existe para que uma
**sessão nova, sem contexto de conversa, consiga continuar o trabalho** sem redescobrir o que já
foi feito nem repetir os erros já pagos.

## Como usar numa sessão nova

1. Leia **Estado atual** e **Pendências abertas** abaixo.
2. Rode o **gate de validação** (seção Comandos) para confirmar que o ponto de partida está verde
   antes de mexer em qualquer coisa.
3. Para o detalhe de um tema, os documentos de referência são:
   - `docs/roadmap/technical-audit-pattern-buffer.md` — auditoria técnica, fases, plano de testes.
   - `docs/roadmap/pattern-buffer-fidelity-gap.md` — tabela de fidelidade GTNA ↔ GTLCore/GTOCore.
4. Ao concluir qualquer etapa, **acrescente um checkpoint** aqui (ID `G-####` + data + o que mudou
   + validação + commits + pendências que abriu).

## Regras do ledger

- Registrar **fatos verificados**, não intenções. Se algo não foi testado, dizer que não foi.
- Não marcar como concluído sem validação proporcional ao risco (build/teste/gametest/datagen).
- Toda pendência conhecida fica visível, mesmo que pequena.
- Não apagar checkpoints antigos; eles preservam contexto histórico. O topo é o estado atual.

## Estado atual

> ⚠️ **LEIA PRIMEIRO:** `docs/roadmap/NEXT-SESSION-HANDOFF.md` — handoff da sessão de 2026-09-21
> (Steam/large steam, formato de tooltip com source, blocos faltantes como o Industrial Steam
> Casing, convenção de orientação de estrutura e o `VaultPortHatch`). A sessão estourou o contexto
> várias vezes; **confira no código antes de agir** e **não confie** nas estruturas das
> `large_steam_*` antigas sem revisar contra o GTNL.

- **HEAD `9c0833a`** (o acúmulo G-0010..G-0025 foi commitado em `feat:` + `docs:`); árvore limpa
  antes do G-0026.
- Versão `mod_version=0.4.0`. Base: Minecraft **1.20.1**, Forge **47.4.1**, GTCEu **7.5.3**,
  AE2 **15.4.10**, ModDevGradle legacyforge **2.0.91**.
- **Gate verde em 2026-09-21:** `spotlessCheck` + `compileJava` + `runUnitTests` (**14/14**) +
  `runGameTestServer` (**24/24**, `All 24 required tests passed`) + `runData` determinístico. A
  execução carregou os mixins alterados; os avisos/erros de receitas do GTCEu já conhecidos
  continuam no log.
- **Feature em foco:** o **ME Pattern Buffer multi-modo** (fidelidade ao GTLCore/GTOCore). A tabela
  de fidelidade está **toda verde** e as divergências conscientes estão documentadas no gap doc.
- **Testes hoje:** 14 unit tests (`main()` + asserts, padrão GTLCore) e 24 gametests (`@GameTest`),
  ambos no gate do CI.
- **Licenciamento (G-0019):** código do GTNA **LGPLv3**; assets do GTO em **CC BY-NC-SA 4.0**
  (o GTNA é **não-comercial**). Permissão do **GTOEPP** concedida pelo time GTO; atribuição de origem
  nos tooltips via `GTNASources`. Matriz completa em `THIRD_PARTY_NOTICES.md`.
- **Thread Hatch (G-0012/G-0018):** aceita pelas máquinas na base multi-receita. Migradas as que
  ganham com threads: **Industrial Slaughterhouse** e **Dimensionally Transcendent Dirt Forge**. Os
  demais controladores (Artificial Star/gerador, Nexus Molecular Forge e Eye of Wood/logic própria,
  Eye of Harmony/no-energy custom, ME Storage e Nexus ME Hypercore/não-processadores) **não** migram
  por decisão de escopo — não precisam aceitar Thread Hatch.
- **QA em expansão (G-0011):** a classe de bug "peça não encaixa na máquina" (predicado de estrutura
  fixado em bloco exato em vez de *ability*) ganhou um lint de contrato + gametest de integração.
  O plano de camadas está em G-0011.
- **Validação visual de G-0009:** o usuário forneceu screenshot in-game em
  `/home/raishxn/MineProjects/printUI/1.png`; a página em duas colunas cabe e não há vazamento
  visível na escala capturada. Outra escala de GUI ainda não foi testada.

## Checkpoints

### G-0009 (2026-09-20) — UI do pattern buffer: painel de config **docado** (o vazamento de 106 px)

- **Sintoma (relatado in-game):** ao clicar com o botão do meio num slot, os widgets do painel de
  configuração apareciam **fora** da página — por cima da moldura e das fileiras do inventário do
  jogador. O usuário descreveu como "problema da UI"; a mecânica (modo por slot, ghost items,
  circuito) funcionava.
- **Causa medida:** página `176 x 220`, painel de config **trocado por cima** da grade de patterns
  com o mesmo tamanho, conteúdo somando **326 px** → **106 px** desenhados abaixo da borda. O
  `FancyMachineUIWidget.setupFancyUI` dimensiona a moldura por `page.getSize()` +
  `PlayerInventoryWidget`, e o LDLib **não recorta** filhos de página (`WidgetGroup.drawInBackground`
  só checa `isVisible()`, nunca a caixa). Não era um bug de "tamanho errado", era um bug de
  **layout sem fonte de verdade**.
- **Correção (`7822893`):** página única `352 x 248` em duas colunas — grade de patterns à esquerda,
  painel de config **docado** à direita (`GuiTextures.BACKGROUND_INVERSE`). O bloco que estourava e
  **não é decisão por slot** virou o side tab fancy **Buffer Tools**
  (`PatternBufferToolsConfigurator`): limpeza de cache e ferramentas de circuito dos patterns
  (`embed`/`remove`/`skip_existing`). As duas linhas de diagnóstico encurtaram para
  `Recipe: %s` / `Mode: %s` com id *pretty-printed* e truncado (`formatModeLabel` + `compactDisplay`)
  porque o id cru estourava os 164 px da coluna.
- **Geometria centralizada:** `PatternBufferLayout` (constantes + `describeViolation()` que percorre
  o plano vertical e reporta sobreposição/estouro) e o 9º unit test `PatternBufferLayoutTest`, que
  chama o walker e ainda afirma soma das colunas, largura interna do painel, ghost rows casando com
  a grade (`9 * 18`) e que a GUI inteira (`248 + 2*4 + 86 = 342`) cabe nos **360 px lógicos** de
  1080p em GUI scale 3.
- **Validação:** `spotlessCheck` + `compileJava` + `runUnitTests` (9/9) + `runData` (só as chaves
  novas/alteradas no en_us gerado) + `runGameTestServer` (5/5, 0 `invalid dist`); screenshot
  in-game fornecido pelo usuário confirma a geometria na escala capturada.
- **Divergência consciente (anti-plágio):** o layout é nosso. Ghost rows de item/fluido + catalyst
  existem porque o GTNA guarda a especialização em `slotConfigs` (não no NBT do pattern item) — um
  clone 1:1 do GTLAdditions seria impossível **e** violação de licença (GTLAdditions é **GPL-3.0**,
  o GTNA é **LGPLv3**).
- **Pendência aberta:** conferir uma segunda escala de GUI no client (`./gradlew runClient`) se o
  usuário encontrar corte; o corte de emergência é `hasPlayerInventory()` → `false` em
  `GTNAMEPatternBufferPartMachine` (economiza 86 px, mas perde o inventário na GUI).

### G-0026 (2026-09-21) — High pressure mode (steam): tier dos casings → ×2 velocidade e ×2 steam

- **Implementado (GTNL parity):** o `SteamMultiMachineBase` agora lê o **tier do casing** da estrutura
  formada (`bronze = 1`, `steel = 2`) do match context e expõe `isHighPressure()`. Com high pressure:
  **duração ×0.5** e **steam ×2** (via `getEffectiveConversionRate()`, que dobra a taxa nominal de
  mB-por-EU — respeitando overrides como os 0.75 da distillation tower), skin de GUI em aço e linha
  "High pressure mode active" no display.
- **Predicado de casing com tier:** novo `SteamMultiMachineBase.casing()` aceita
  `CASING_BRONZE_BRICKS` **ou** `CASING_STEEL_SOLID` e grava o **menor** tier encontrado no match
  context (semântica do `ofBlocksTiered` + `checkMachineTier` do GTNL: bronze vence se presente).
  Ligado no helper `steamCasing()` (6 máquinas novas) e no `large_steam_alloy_smelter`.
- **Onde o bônus de duração é aplicado:** dentro de `AdjustableSteamParallelMachine.createThreadedRecipe`
  (não só no `getRealRecipe`), porque o `GTNAMultipleRecipesLogic` chama `createThreadedRecipe`
  direto — sem isso o `FixedThreadSteamParallelMachine` ficaria de fora. O `getRealRecipe` da base
  cobre as máquinas que usam o modifier da definição (`LargeSteam*`, `SteamManufacturer`).
- **Tooltip:** `GTNASteamTooltips.applyAll()` anexa a linha compartilhada
  `gtna.tooltip.steam.high_pressure` às 7 máquinas ligadas, **antes** do `Source:` do `GTNASources`
  (ordem stats → high pressure → Source). Lang en_us + pt_br (byte-preserving) + `runData`.
- **Decisão de escopo (autor):** mecanismo + 7 máquinas agora; as demais `large_steam_*` ganham o
  predicado com tier no **passo 3** (re-portar as estruturas), evitando retrabalho.
- **Cobertura:** gametest `steelCasingEnablesHighPressure` (24º) monta o alloy smelter com casing de
  **aço** e prova `isFormed()` + `isHighPressure()` + `getEffectiveConversionRate() == 2.0`; o teste
  do wireless hatch ganhou a asserção oposta (bronze → sem high pressure, taxa 1.0).
- **Validação:** `spotlessCheck` + `runUnitTests` (14/14) + `runGameTestServer` (24/24) + `runData`
  (written: 0).

### G-0025 (2026-09-21) — correção: estruturas próprias para as 6 máquinas steam novas

- **Erro corrigido:** o port anterior reusou o pattern da `large_steam_cutting`
  (`createLargeSteamBronzePattern`) para **todas** as 6 máquinas novas — ficaram com a **mesma
  estrutura**. Agora cada uma tem a **sua própria estrutura**, decodificada do **GTNL** (formato
  `.mbs` "MBS1" via `StructureFileCodec`) e mapeada para blocos bronze do GTNA:
  - `large_steam_bending` 5x4x5, `large_steam_extruder` 5x8x5, `large_steam_sifter` 5x7x5,
    `large_steam_wiremill` 6x5x5, `steam_item_vault` 7x11x7, `steam_lava_maker` 3x5x3.
  - Novo helper `steamCasing()` (bronze bricks + abilities) usado **uma vez** por pattern; as demais
    letras usam blocos bronze específicos (gearbox/pipe/frame/glass/magma).
  - Gametest `newSteamMachinesHaveDistinctStructures` (23º) trava a regressão.
- **Aprendizado (importante):** o **GTNL é 1.7.10 (GTNH)** e guarda as estruturas em **`.mbs`**
  (binário "MBS1" = magic + tabela de strings + índices), **não** em `.mb`. Para portar: decodificar
  com `StructureFileCodec.readBinary` e mapear `grid[y][z].charAt(x)` para
  `FactoryBlockPattern.aisle(z)` (linhas = y). Os `.mb` do GTNA são cópias legadas e **não** são
  lidos em runtime.
- **Validação:** `spotlessCheck` + `runUnitTests` (14/14) + `runGameTestServer` (23/23).

### G-0024 (2026-09-21) — Steam: `SteamItemVault` (fecha o núcleo da era Steam)

- **Implementado:** `steam_item_vault` — porta GTNA-native do `SteamItemVault` do GTNL (GPL-3.0,
  reimplementado). Storage de itens de alta capacidade: **256 slots × 64.000 itens**
  (`VaultItemStackHandler extends CustomItemStackHandler`, com `getSlotLimit`/`getStackLimit`
  sobrescritos), exposto aos buses da estrutura via `NotifiableItemStackHandler` (IO.BOTH). A UI
  mostra tipos/itens armazenados. Toggle de config, lang en_us + pt_br, receita de craft (bronze +
  hydraulic pump + precision steam component + baú), gametest `steamItemVaultHoldsLargeStacks` (22º).
- **Era Steam fechada:** só ficam `SteamElevator` (precisa do **Ad Astra**) e `SteamGate*`
  (compatibilidade com o mod **Stargate**).
- **Validação:** `spotlessCheck` + `runUnitTests` (14/14) + `runGameTestServer` (22/22) + `runData`.

### G-0023 (2026-09-21) — Steam: `SteamLavaMaker` (recipe type próprio)

- **Implementado:** `steam_lava_maker` — porta GTNA-native do `SteamLavaMaker` do GTNL (GPL-3.0,
  reimplementado). Novo recipe type `LAVA_MAKER_RECIPES` (`GTNARecipeType`), receita pedra → 1000 mB
  de lava (16 EU/t, 20 t), máquina `SteamLavaMakerMachine` na base steam com paralelo ajustável (16),
  toggle de config, lang en_us + pt_br, receita de craft (bronze + hydraulic pump + precision steam
  component + magma block), gametest `steamLavaMakerHasStoneToLavaRecipe` (21º).
- **Decisões de escopo:** `PrimitiveBrickKiln` e `SteamRockBreaker`/`SteamCarpenter` são redundantes
  com o `PrimitiveStoneFurnace`/`steam_cobbler`/`steam_woodcutter` → pulados por ora.
- **Validação:** `spotlessCheck` + `runUnitTests` (14/14) + `runGameTestServer` (21/21) + `runData`.
- **Próximo (era Steam):** SteamCactusWonder, SteamTurbine, SteamCracking, SteamAssembler,
  SteamItemVault, SteamGate*, SteamElevator (Ad Astra), SteamFusionReactor*.

### G-0022 (2026-09-21) — Roadmap por eras + Steam: família `large_steam_*` completa

- **Contexto:** o autor decidiu portar **por eras** (Steam → ULV → …), fechando multiblocos/mecânicas
  de cada era. Fontes atualizadas/clonadas: GTNL `52345d2`, TST `ceaa462`, GTOCore-Main `dc4824d`,
  GTO-GregTech-Modern. Auditoria em `docs/roadmap/port-audit-2026-09-21.md` e roadmap em
  `docs/roadmap/port-roadmap-by-era.md`.
- **Implementado (4 máquinas):** na base `AdjustableSteamParallelMachine` (16 paralelos, ×0.5 duração),
  com pattern bronze compartilhado (`createLargeSteamBronzePattern`):
  `large_steam_bending` (BENDER_RECIPES), `large_steam_extruder` (EXTRUDER_RECIPES),
  `large_steam_wiremill` (WIREMILL_RECIPES), `large_steam_sifter` (SIFTER_RECIPES).
  - Config toggles (`largeSteamBending/Extruder/Wiremill/Sifter`), lang en_us + pt_br, receitas de
    craft (bronze + hydraulic pump + precision steam component + singleblock LV).
  - Gametest `largeSteamFormingFamilyIsSteamBase` (20º).
- **Decisões do autor:** Bricked Blast Furnace = Leap Forward (pular); Furnace Array = já temos o
  Primitive Stone Furnace (pular); **Steam Elevator** vai precisar do **Ad Astra**; Apiary → Forestry
  não será usado, avaliar **Productive Bees**.
- **Validação:** `spotlessCheck` + `runUnitTests` (14/14) + `runGameTestServer` (20/20) + `runData`.
- **Próximo (era Steam):** SteamAssembler (bronze/steel), SteamTurbine, geração de recursos
  (RockBreaker/LavaMaker/Carpenter/CactusWonder), SteamItemVault, primitivos (PrimitiveBrickKiln),
  SteamCracking, grandes (MegaSolarBoiler/MegaSteamCompressor), modulares (SteamElevator + módulos),
  especiais (SteamGate*/SteamFusionReactor*).

### G-0021 (2026-09-21) — runtime tests do pattern buffer: ME lossless e auto-switch staged

- **ME lossless (deferred output):** o `drainPendingNetworkOutput` foi refatorado para aceitar uma
  inserção injetável (`NetworkInsert`) — a versão de produção passa `StorageHelper.poweredInsert`, a
  de teste passa um lambda. Hooks: `gtna$bufferPendingOutput`, `gtna$pendingOutputAmount`,
  `gtna$pendingOutputIsEmpty`, `gtna$drainPendingOutput`. Gametest
  `pendingNetworkOutputRetriesUntilItFits` (18º): rede saturada (a sobra fica na fila), insert parcial
  (só a parte aceita sai), insert completo (a fila esvazia). **Não precisou de grid AE2 real.**
- **Auto-switch por conteúdo staged:** hook `gtna$stageSlotItem`. Gametest
  `stagedContentDrivesBufferModeRequest` (19º): slot vazio não pede modo; slot com conteúdo staged
  pede o `preferredModeId`; o pin do buffer (`selectedModeId`) vence o staged.
- **Achado de licença (G-0019):** o **GTCEu base já tem** `BlockPattern.autoBuild(Player,
  MultiblockState)` e um item Terminal (LGPL-3.0). O `NexusBlockPattern` do GTNA pode ser rebaseado
  nesse código (LGPL) em vez do `AdvancedBlockPattern` do GTMThings (ARR), o que **removeria** essa
  pendência de permissão. Avaliar.
- **Validação:** `spotlessCheck` + `runUnitTests` (14/14) + `runGameTestServer` (19/19).
- **Builder de estrutura (tentado):** o GTCEu **tem** `BlockPattern.autoBuild(Player, MultiblockState)`
  e, com um `FakePlayer` **criativo**, ele constrói a estrutura sem itens. Testado na Slaughterhouse
  (7×10×7): o autoBuild **coloca os blocos**, mas o `checkPatternAt` seguinte **reprova** numa célula
  `A` (provável limite de ability — `setMaxGlobalLimited`/`setExactLimit` — que o autoBuild não
  respeita ao escolher os candidatos). Revertido para manter o gate verde; próximo passo é investigar
  o autoBuild + limites, ou montar a estrutura manualmente.
- **Ainda pendente da recomendação:** (2) builder de estrutura funcional; (3) refactors da Fase 3 e a
  Nexus Flux Matrix.

### G-0020 (2026-09-21) — higiene e QA: CHANGELOG 0.4.0, i18n da UI, pt_br e 2 lints novos

- **CHANGELOG:** entrada **0.4.0** (estava parado em 0.3.2-dev), cobrindo o port do Universal
  Factory/Stone Furnace, Thread Hatch, paridade dos hatches, migração da Slaughterhouse/Dirt Forge,
  remoção do boiler duplicado, atribuição de origem, correções (Output Boost M², crashes do buffer,
  servidor dedicado, Jade, 9 configs) e a camada de QA.
- **Fase 3 — i18n das strings de UI:** `WorkableElectricMultipleRecipesMachine` e
  `GTNAMultipleRecipesLogic` deixaram de usar `Component.literal` hardcoded; novas chaves
  `gtna.multiblock.*` (max_eut, parallels, overclock/accelerate/output_boost hatch, active_threads,
  idle, thread_line, output_line, unknown). Regenerado com `runData`.
- **pt_br:** 32 chaves novas (source, multiblock UI, Universal Factory, Primitive Stone Furnace),
  inseridas byte-preserving (BOM preservado).
- **QA (camada 0) — 2 lints novos:**
  - `PartAbilityCoverageTest` — toda ability GTNA declarada por uma peça é aceita por ao menos uma
    máquina (classe do Thread Hatch órfão). Hoje: 5 abilities, todas aceitas.
  - `RegistrationContractTest` — ids de registro e config keys únicos (59 ids, 33 config keys).
- **QA (smoke) — gametest `everyGtnaMachineTooltipBuilds` (17º):** roda o tooltip builder de toda
  máquina GTNA e falha se algum lançar (classe "crash de tooltip").
- **Ainda não feito (e por quê):**
  - **ME lossless runtime** e **auto-switch por conteúdo staged**: exigem um **grid AE2** montado no
    gametest; a lógica de drain depende de `getMainNode().getGrid()` → `IGrid`/`MEStorage`, difícil de
    falsificar sem AE2 real. Fica para quando houver um harness AE2.
  - **Gametests de estrutura** da Slaughterhouse (7×10×10) e DirtForge (~24³): montar à mão é
    inviável; precisa de um **builder data-driven** (camada 2 do QA).
  - **Fase 3 refactors** (`AnnihilateGeneratorA/B`, split do `GTNAMachines`, fundir
    `getRecipeModifier`): refactors grandes, deixados para uma sessão dedicada (risco de regressão).
  - **Nexus Flux Matrix**: feature nova (fora do escopo de "terminar").
  - **Testes manuais** (autocrafting AE2 nativo, armadura Quantum) e **retest do EMI**: exigem client.
- **Validação:** `spotlessCheck` + `runUnitTests` (14/14) + `runGameTestServer` (17/17) + `runData`.

### G-0019 (2026-09-21) — Licenciamento: permissão do GTO/GTOEPP, atribuição nos tooltips e `THIRD_PARTY_NOTICES.md`

- **Contexto:** o GTNA porta conteúdo de mods/modpacks legados (GTO, GTNL, GTNH, TST, GTL). Foi feita
  uma auditoria de licenças por **hash md5** contra as fontes locais/repositórios.
- **Achados (verificados):**
  - **GTO/GTOCore:** código **LGPLv3**; **texturas originais CC BY-NC-SA 4.0** (confirmado pelo time
    GTO). 148 texturas do GTNA são idênticas a fontes (113 GTOCore, 27 GTLCore, 6 GTO-Modern,
    2 GTLAdditions — estes 2 também existem em GTOCore/GTLCore LGPL).
  - **GTOEPP** (`Gto-Extended-Platform-Presets`): **ARR**; **permissão CONCEDIDA** pelo time GTO
    (Alcox), mantendo a atribuição. Fonte: `platforms/epp/sy_1/*`.
  - **GTNL** (`ABKQPO/GT-Not-Leisure`): **GPL-3.0** (o GitHub API reporta LGPL-3.0, mas o
    `LICENSE.txt` e o README dizem GPLv3). 151 `.mb` + família `large_steam_*`. **Permissão CONCEDIDA**
    pelo autor (ABKQPO): usar com marcação de fonte; as estruturas que o GTNL pegou do GTO devem ser
    creditadas ao GTO (cuja permissão já foi concedida).
  - **TST** (`Nxer/Twist-Space-Technology-Mod`): **GPL-3.0** (ore condenser). Permissão **pendente**.
  - **GTMThings** (`liansishen/GTMThings`): **sem licença (ARR)** — `AdvancedBlockPattern` →
    `NexusBlockPattern`. Permissão **pendente**.
  - **cmme-additions → Modernity-GTNH**: ARR → **CC BY-NC-SA 4.0** (113 texturas de plate/ingot).
    Permissão **pendente**.
  - **GTLCore:** `gradle.properties` diz LGPLv3.0, sem arquivo `LICENSE` — atribuição.
- **Decisão do autor:** aceitar o GTNA como **não-comercial** (código LGPLv3 + assets do GTO em
  CC BY-NC-SA 4.0).
- **Implementado:**
  - `GTNASources` — mapa central `máquina → fonte` + `applyAll()`, que anexa a linha
    `gtna.tooltip.source` ("Source: <addon>") a cada máquina GTNA registrada, via
    `MachineDefinition.setTooltipBuilder` + `GTRegistries.MACHINES`. Chamado no
    `CommonProxy.registerMachines`.
  - Chaves de lang `gtna.tooltip.source` + `gtna.source.*` (gto/gtnl/gtnh/tst/gtl/gtlcore/gtlsupb/
    gtoepp/gtmthings) no `GTNALangProvider`; regenerado com `runData`.
  - `THIRD_PARTY_NOTICES.md` com a matriz (fonte → licença → uso → status) + aviso não-comercial.
  - README: tabela de créditos corrigida (GTO/GTNL com as licenças certas) + aviso não-comercial.
  - Gametest `portedMachinesCreditTheirSource` (16º) trava o wiring (a linha de origem aparece no
    tooltip do `annihilate_generator`).
- **Mapa de origem completo (confirmado pelo autor):** `eye_of_wood` e `industrial_slaughterhouse` →
  **TST**; `large_steam_solar_boiler` → **GTO**; `nexus_molecular_forge`, `hyper_pressure_reactor`,
  `compact_hyper_pressure_reactor`, `steam_cobbler`, `steam_woodcutter`, `stone_superheater` →
  **GTNL**. `nexus_me_hypercore` é **GTNA-original** (sem atribuição).
- **Validação:** `spotlessCheck` + `runUnitTests` (12/12) + `runGameTestServer` (16/16) + `runData`.
- **Pendências:** pedir permissão a GTNL, TST, GTMThings (e cmme/Modernity quando for usar as
  texturas); completar o mapa de origem das máquinas acima; avaliar **recriar** as texturas do GTO se
  um dia quiser um mod comercial.

### G-0018 (2026-09-21) — Fase 2 do manifest: Industrial Slaughterhouse na base multi-receita (1ª migração)

- **Objetivo (manifest, regra 8 / ordem de entrega passo 2):** todo controlador de receita deve estar
  na base multi-receita do GTNA e aceitar a Thread Hatch. Primeira migração feita.
- **`IndustrialSlaughterhouse`** passou de `extends WorkableElectricMultiblockMachine implements
  IDisplayUIMachine, IFancyUIMachine` para **`extends WorkableElectricMultipleRecipesMachine`** (a base
  já implementa as duas interfaces e já traz `getOverclockingLogic()` público). Ajustes:
  - `MANAGED_FIELD_HOLDER` passou a encadear em `WorkableElectricMultipleRecipesMachine`;
  - removido o `getRecipeType()` fixo (a base usa `recipeTypes[activeRecipeType]`, que tem 1 tipo);
  - removido o `getOverclockingLogic()` privado (a base coleta as `OverclockHatchPartMachine` no
    `onStructureFormed` e calcula o fator); o `recipeModifier` estático agora usa o herdado;
  - `addDisplayText` ganhou a linha **Active Threads** (`getRecipeLogic().getActiveRecipeCount()` /
    `getMaxThreads()`);
  - o pattern da Slaughterhouse agora aceita **`THREAD_HATCH`**.
- **Por que o `afterWorking` continua funcionando:** o `GTNAMultipleRecipesLogic.completeRecipe()`
  chama `machine.afterWorking()` e depois `handleRecipeIO(OUT)`, então a geração de drops por circuito
  e a saída continuam iguais. Como o circuito seleciona **uma** receita por vez, o efeito prático é ~1
  thread; o ganho real é a máquina entrar na base correta (Thread/Parallel/Accelerate hats coerentes).
- **Cobertura:** gametest `industrialSlaughterhouseUsesMultipleRecipesBase` (14º) trava a migração:
  colocando o controller, o block entity tem que ser `WorkableElectricMultipleRecipesMachine`.
- **2ª migração — `DimensionallyTranscendentDirtForgeMachine`** (15º gametest
  `dirtForgeUsesMultipleRecipesBase`): passou a `extends WorkableElectricMultipleRecipesMachine
  implements IZeroEnergyMachine` (é **no-energy**). O ramo zero-energy da lógica replica o que o
  modifier estático fazia (paralelo + duration 1); `getMaxParallel()` devolve 524288 e o pattern agora
  aceita `THREAD_HATCH`. O modifier estático foi mantido só para preview/EMI.
- **Escopo fechado (decisão do autor):** os demais controladores **não** precisam migrar — eles **não**
  precisam aceitar Thread Hatch. Casos:
  - **Artificial Star** — gerador (o modifier escala a geração de EU, não é overclock de processador);
  - **Nexus Molecular Forge / Eye of Wood** — usam `GTNABatchRecipeLogic` (logic própria);
  - **Eye of Harmony** — no-energy com lógica própria;
  - **ME Storage / Nexus ME Hypercore** — não são processadores de receita.
  Chegou-se a implementar um hook opt-in (`IRecipeModifierProvider`) para destravar geradores, mas ele
  foi **revertido** por não ter consumidor real (o Artificial Star voltou a `WorkableElectricMultiblockMachine`).
- **Fase 2 = concluída no que importa:** apenas as máquinas que ganham com threads/auto-switch foram
  migradas (Slaughterhouse e Dirt Forge). As demais ficam como estão por decisão de escopo.
- **Validação:** `spotlessCheck` + `runUnitTests` (12/12) + `runGameTestServer` (15/15).
- **Pendências abertas:** (a) **teste in-game** dos drops/`afterWorking` da Slaughterhouse e do
  paralelo da DirtForge; (b) gametests de **estrutura** (Slaughterhouse 7x10x10 e DirtForge) para
  formar de fato.

### G-0017 (2026-09-21) — remoção do boiler solar duplicado, perf do buffer e triagem do EMI

- **Boiler solar duplicado removido:** havia dois multiblocos que geram steam solar com a mesma
  `SOLAR_BOILING_CELL` — `mega_pressure_solar_boiler` (`MegaSolarBoilerMachine`) e
  `large_steam_solar_boiler` (`LargeSteamSolarBoilerMachine`). Mantido o **`large_steam_solar_boiler`**
  porque é o **id do GTO** (tabela do `multiblock-port-manifest.md`); removido o mega. Ajustes:
  removida a definição em `GTNAMachines`, a classe `MegaSolarBoilerMachine`, a receita própria e o
  uso no recipe do Hyper Pressure Reactor (agora usa o large), o toggle em `ConfigHolder`
  (`megaPressureSolarBoiler`), o campo/`MegaSolarBalance`/getters em `GTNABalance`, as chaves de lang
  (provider + en_us + pt_br, incluindo `config.gtna.option.megaSolarSteamPerBlock`), e os docs
  `mega-solar-boiler.*` viraram `large-steam-solar-boiler.*` (reescritos para o large) com o
  `mkdocs.yml` e as referências cruzadas atualizadas. `runData` removeu 3 arquivos stale.
- **Perf do pattern buffer:** `pushPattern` chamava `resolveAndCacheSlotRecipe` **a cada push** do
  AE2 (varredura de milhares de receitas). Agora só resolve quando o slot ainda **não tem
  `cachedRecipeId`** (o primeiro push).
- **Triagem do EMI ("2 recipes loaded with the same id" para todas as máquinas GTNA):** **não** é
  duplicata de datapack — as receitas do GTNA são registradas em runtime pelo `GTDynamicDataPack`,
  cujo `GTDynamicPackContents` guarda por caminho num mapa que **sobrescreve** (dedup). O mesmo log
  traz o `AbstractMethodError` do bridge JEMI (EMI 1.1.13 × JEI 15.20), então é artefato do EMI/JEMI.
  Ação sugerida: reproduzir com EMI desabilitado/atualizado; se persistir, abrir issue no EMI.
- **Validação:** `spotlessCheck` + `runUnitTests` (12/12) + `runGameTestServer` (13/13) + `runData`.

### G-0016 (2026-09-21) — correções do port, crash do buffer, paralelo zero-energy e estratégia de QA

- **Receita do casing da Factory:** trocada pela original do modpack
  (`kubejs/server_scripts/gtceu.js`): `BCB/DAD/BCB` → 2×, com `solid_machine_casing` + placas duplas
  de alumínio + `mv_electric_motor`/`mv_electric_piston`.
- **Primitive Stone Furnace — threads/paralelo:** virou base multi-receita zero-energia; `getMaxParallel`
  e `getAdditionalThread` efetivamente infinitos; `onWorking()` = true (sem checagem elétrica).
- **BUG do autocrafting (só 1 craft) — causa raiz:** o `GTNAOptimizedCraftingCpuLogic` extrai N crafts
  escalados e desconta `parallel`, mas o `ParallelPatternDetails.pushInputsToExternalInventory`
  **delegava** ao `AEProcessingPattern`, que para patterns "sparse" (com slots vazios, i.e. quase
  todos) empurra os próprios `sparseInputs` **sem escalar** → 1 craft. Corrigido empurrando o
  `inputHolder` já extraído/escalado. Confirmado por log (`pushPattern slotItems=1` → devia ser N).
- **Crash do servidor (NPE):** `PatternSlotResolver.consumeVirtualItemList(left=null)` quando a receita
  não tem item inputs; adicionada a guarda `left == null || left.isEmpty()`. Era o
  `Ticking grid on end of server tick` que derrubava o mundo.
- **Zero-energy parallel sempre 1:** o log provou `visibleItems=6442450941` mas
  `ParallelLogic.getMaxByInput=0` (o GTCEu não enxerga essas receitas de fornalha vanilla-convertidas).
  O caminho zero-energy agora calcula o orçamento de paralelo **direto dos insumos visíveis**
  (`computeZeroEnergyParallel`), limitado pelo output via `limitByOutputMerging`.
- **UI — seletor de machine mode:** `ScrollableMachineModeFancyConfigurator` (5 linhas + rolagem) na
  Universal Factory, via override de `attachSideTabs`.
- **Estratégia de QA:** novo doc **`docs/roadmap/qa-strategy.md`** com as camadas (L0 lint de contrato,
  L1 unit, L2 gametest, L3 CI, L4 manual/observabilidade), a tabela de classes de bug × camada que
  pega, as práticas adotadas (teste negativo, fonte única, doc como contrato, observabilidade) e a
  avaliação do **Horizon-QA**: adotar as **ideias** (asserção por tick com janela, teste negativo,
  relatório JUnit, autoria in-game), **não** o framework.
- **QA implementado a partir do doc:** (a) helper `GTNAGameTestUtils.assertEveryTickUntilTimeout`
  (invariante por tick + `succeed` no fim da janela, ideia Horizon-QA); (b) 2 gametests novos — o
  **negativo** `universalFactoryDoesNotFormWithoutMaintenance` e o **invariante**
  `universalFactoryStaysFormedWithThreads`; (c) CI agora roda `runData` e **falha se `git diff` em
  `src/generated` não estiver vazio** (determinismo de datagen).
- **Validação:** `spotlessCheck` + `runUnitTests` (12/12) + `runGameTestServer` (13/13); o client foi
  validado pelo usuário (autocrafting dos 10 rods corrigido).

### G-0015 (2026-09-21) — port GTNA-native de 2 multiblocos do GTLsupb (Universal Factory, Primitive Stone Furnace)

- **Origem:** o usuário pediu para importar 2 multiblocos do **GTLsupb 2.6.4** (jar em
  `~/.local/share/PrismLauncher/instances/GTL八周目6月2日/minecraft/mods`). Licença do mod:
  **LGPLv3** (compatível com o GTNA). Decompilado com **CFR** (o fernflower local falha em
  Kotlin/Java 17); fonte em `/tmp/opencode/gtlsupb_src` (temporário).
- **Por que não foi cópia 1:1:** o `UniversalFactoryMachine` estende
  `org.gtlcore...WorkableElectricMultipleRecipesMachine` e usa `ILockRecipe`/`IRecipeStatus`/
  `RecipeResult`/`RecipeRunnerHelper`/`IParallelLogic`/`MultipleRecipesLogic` do **GTLCore**; a
  `ZeroEnergyMultiTypeLogic` também. O GTNA não depende do GTLCore, então o port foi feito na
  **nossa** engine (`WorkableElectricMultipleRecipesMachine` + `GTNAMultipleRecipesLogic`, que já
  fazem cross-recipe parallel + threads).
- **Universal Factory** (`gtna:universal_factory`):
  - Casing novo **`gtna:universal_factory_casing`** (bloco + textura placeholder gerada por PIL;
    trocar por uma textura dedicada quando houver).
  - Classe `UniversalFactoryMachine extends WorkableElectricMultipleRecipesMachine`: campos
    persistidos `batchMultiplier`/`runningSecs`/`autoBatch`; `getWarmupMultiplier()` exponencial,
    `getOverloadUnlocked()`, `getDynamicThreads()` = `baseThreads × 2^tier`, `getMaxParallel()` =
    `baseParallel × 2^tier × batch × warmup`, e `getAdditionalThread()` alimentando os threads da
    nossa lógica. UI com `[-]/[+]/(AUTO)` no display.
  - Registro com **32 recipe types** (dos 34 originais, caíram `DEHYDRATOR_RECIPES` e
    `LIGHTNING_PROCESSOR_RECIPES`, que são do GTLCore) e o padrão 3×3×3 (casing + `steel_frame` no
    centro + maintenance obrigatório).
  - Config nova em `config/gtna/balance/universal_factory.json` (`baseParallel`, `baseThreads`,
    `maxWarmup`, `warmupTau`, `overloadTime`, `maxBatchMultiplier`).
  - Gametest `universalFactoryFormsAndExposesRecipeTypes` (10º): 32 tipos + estrutura forma.
- **Primitive Stone Furnace** (`gtna:primitive_stone_furnace`):
  - `PrimitiveStoneFurnaceMachine extends WorkableMultiblockMachine` (sem energia):
    `fullModifyRecipe` **remove o EU** de todos os content maps e fixa `duration = 1`, então
    FURNACE_RECIPES roda sem energy hatch (GTLsupb `consumeEnergy = false`).
  - Padrão 3×3×3 de `minecraft:stone` (buraco de ar no meio, como o original).
  - Gametest `primitiveStoneFurnaceSmeltsWithoutEnergy` (11º): forma **sem energia** e smelta uma
    receita injetada até o bus de saída.
- **Validação:** `spotlessCheck` + `compileJava` + `runUnitTests` (**12/12**) +
  `runGameTestServer` (**11/11**) + `runData`.
- **Pendências:** (a) textura dedicada do casing (hoje é placeholder); (b) traduções `pt_br.json` das
  chaves novas; (c) conferir in-game o layout dos displays e o comportamento do batch/AUTO; (d) os 2
  recipe types do GTLCore ficaram de fora por decisão consciente.

### G-0014 (2026-09-21) — paridade GTO nos hatches: sem punir, quantidade configurável, tooltips e teste de runtime

- **Pedido:** (1) igualar o GTO e não punir; (2) quantidade configurável; (3) tooltips no padrão GTO;
  (4) teste de runtime do Output Boost. Ordem escolhida: 1 → 2 → 3 → 4 (comportamento antes de
  texto/teste).
- **(1) Semântica igual ao GTO (tier da receita, não da máquina):** a penalidade da Accelerate agora
  usa o tier **pré-overclock da receita** (`RecipeHelper.getPreOCRecipeEuTier`, que desconta
  `ocLevel` e paralelos) em vez do tier da máquina. A matemática virou a função pura
  `AccelerateHatchMath.compute(...)` + unit test `AccelerateHatchMathTest` (12º) com os casos do GTO
  (mesmo tier, receita baixa, receita acima, teto 100 e piso). Campo de config renomeado
  `penaltyPerTierBelowMachine` → `penaltyPerTierBelowRecipe` (JSON `balance/hatches.json`; quem tinha
  valor customizado volta ao default 20). O mixin (`GTRecipeLogicMixin`) e a lógica multi-receita
  foram ajustados; o display usa `getNominalDurationMultiplier()` (sem penalidade).
- **(2) Quantidade configurável (paridade `WorkableAmountConfigurationPartMachine`):** nova base
  `ConfigurableAmountPartMachine` (int `@Persisted @DescSynced`, `IntInputWidget`, min..max, default =
  min). Aplicada em:
  - **Accelerate**: porcentagem de duração (base do tier .. 100), default = melhor valor.
  - **Overclock**: porcentagem por passo de overclock (`round(config×100)` .. 100), default = valor
    do tier. *Nota:* agora é inteiro, então 0.3333 vira 33% (arredondamento minúsculo).
  - **Thread**: contagem de threads (0 .. máximo do tier), default = máximo; a peça ganhou
    `MANAGED_FIELD_HOLDER` próprio e UI com input.
  O default preserva o comportamento anterior; o jogador só pode afrouxar (até 100% = sem efeito).
- **(3) Tooltips no padrão GTO:** reescritos no `GTNALangProvider` (en_us regenerado por `runData`) e
  com novas chaves — `accelerate_hatch.compat`, `thread_hatch.range`, `thread_hatch.requires`,
  `overclock_hatch.note`. Corrigido o texto que ainda dizia "machine tier" (agora explica que a
  penalidade é por **receita** e que receita de tier baixo numa máquina de tier alto **não** é punida),
  além de documentar a quantidade ajustável na UI e o caso do Overclock UV não dar ganho.
- **(4) Teste de runtime do Output Boost:** gametest `outputBoostAppliesOnceOnMultipleRecipesMachine`
  (9º) monta o `duration_tester` com um Output Boost Hatch LV, injeta uma receita (nether star →
  stone) e **completa** a receita, afirmando que o bus de saída tem exatamente `M` stone, não `M²`.
  **Teste negativo provado:** reintroduzindo a aplicação manual, o teste falha ("the injected
  assembler recipe never started", porque o match passa a exigir `M²` de espaço). Revertido.
- **Validação:** `spotlessCheck` + `compileJava` + `runUnitTests` (**12/12**) +
  `runGameTestServer` (**9/9**).
- **Pendências:** (a) conferir in-game o visual das novas UIs `IntInputWidget` (client); (b) traduzir
  as chaves novas de tooltip no `pt_br.json` (hoje caem no en_US); (c) a UI das hatches é
  server-autoritativa (padrão `IntInputWidget`), validar que o valor persiste ao quebrar/colocar.

### G-0013 (2026-09-21) — auditoria dos hatches vs GTOCore/GTO + bug do Output Boost duplo

- **Pedido:** comparar Thread/Overclock/Accelerate Hatch com os repositórios locais do GTOCore e do
  GTO, procurando bugs.
- **Limite da fonte:** o **gtolib** do GTOCore é um **jar protegido** (`gtolib-release.jar`): as
  classes `com.gtolib.api.machine.impl.part.ThreadPartMachine`/`OverclockPartMachine` são `native` e
  o construtor lança `UnsatisfiedLinkError` — a lógica real está nos `*.prod.bin`/`.dev.bin`, não
  decompilável. Usei como referência: `GTOCore AccelerateHatchPartMachine` (fonte completa),
  os tooltips/registros do `GTOMachines`, e o `ThreadPartMachine.kt` do **GTLAdditions** (irmão).
- **Fórmulas conferem com o GTO:**
  - Overclock: nosso `durationMultiplierByTier` = `100/(tier-6)%` → UV 0.5, UHV 1/3, …, MAX 1/8 —
    **igual** ao tooltip do GTO (`100D/(tier-6)`).
  - Accelerate: nosso base = `50 - 2*(tier-1)` = `52 - 2*tier` — **igual** ao `super(holder, tier,
    52 - 2L*tier, 100)` do GTOCore; penalidade +20/tier igual.
  - Thread: nosso extra = `(1<<(tier-6))-1`; o GTO dá total = `1<<(tier-LuV)` → mesmo total.
  - Aplicação: o mixin do Overclock emula corretamente o GTOCore (`(f/0.5)^ocLevel` sobre o
    overclock **não-perfeito**, que é o que as máquinas do GTCEu 7.5.3 usam; confirmei que
    `PERFECT_OVERCLOCK` não é usado por máquina nenhuma no source oficial).
- **BUG REAL encontrado e corrigido — Output Boost aplicado em dobro (M → M²):** o
  `GTNAMultipleRecipesLogic.tryStartRecipe` aplicava `getOutputBoostMultiplier()` via
  `ModifierFunction.outputModifier(...)`, e o `RecipeHelperMixin` (`adjustRecipeForMatching` no
  match, `applyOutputBoosts` na execução) aplicava de novo — a base multi-receita produzia `M²` e o
  match simulado exigia `M²` de espaço. Fix: removida a aplicação local; **fonte única** =
  `GTNASpecialPartUtil`. Novo `OutputBoostContractTest` (11º unit test) faz lint de fonte e falha se
  alguém reintroduzir `.outputModifier(` no logic.
- **Doc errada (corrigida em PT/EN/ES):**
  - `accelerate-hatch.*`: tabela estava **deslocada em 2** e a coluna de fórmula era aritmeticamente
    inválida (`50 - 2×(1-1) = 48%`); corrigida para os valores do código/GTO (LV 50% … MAX 24%),
    exemplo HV/EV 44%→46% (64→66 ticks), e a compatibilidade (funciona em **qualquer** multibloco
    elétrico, não só a base multi-receita).
  - `overclock-hatch.*`: UV era `×0.55`/`-45%`; o correto é `×0.50`/`-50%` (igual ao GTO). Exemplo
    ajustado (Accelerate EV 44%, Overclock UV ×0.50) e nota de que **UV não dá ganho** (igual ao
    overclock padrão); o ganho começa no UHV.
- **Divergências conscientes (não alteradas):** (a) a penalidade da Accelerate usa o tier da
  **máquina**, enquanto o GTOCore usa o tier da **receita** — mais punitivo; documentado nas docs;
  (b) não temos a quantidade configurável por hatch do GTO (`WorkableAmountConfigurationPartMachine`);
  (c) o Thread Hatch nosso é fixo por tier, o do GTO é configurável.
- **Anti-duplicação verificada:** Overclock é auto-tratado pela base multi-receita/slaughterhouse e o
  mixin os **pula**; Accelerate é aplicado só pela lógica multi-receita (o mixin não roda porque o
  `GTNAMultipleRecipesLogic` não chama `setupRecipe`). Só o Output Boost duplicava.
- **Coil:** confirmado pelo autor que `CoilWorkableElectricMultipleRecipesMachine` é base dos
  multiblocos com coil futuros — **não deletar**.
- **Validação:** `spotlessCheck` + `compileJava` + `runUnitTests` (**11/11**) + `runGameTestServer`
  (**8/8**). O bug do Output Boost **não tem teste de runtime** (exigiria completar uma receita com
  hatch de boost e contar o output no bus) — fica como pendência.

### G-0012 (2026-09-21) — auditoria proativa: Thread Hatch órfã (foundation-only) + doc mentindo

- **Origem:** depois do G-0011, o usuário perguntou se o QA precisava abranger mais. Fiz um scan
  proativo da classe "conteúdo registrado/craftável que nenhuma máquina de gameplay aceita".
- **Achado (confirmado em código):**
  - `ThreadPartMachine` (`GTNAMachines2.java:379`) declara **só** `GTNAPartAbility.THREAD_HATCH`.
  - `PredicatesMixin` injeta no `autoAbilities` do GTCEu **apenas** `OVERCLOCK_HATCH` e
    `ACCELERATE_HATCH` — `THREAD_HATCH` ficou de fora.
  - O **único** pattern do mod que aceita `THREAD_HATCH` é o `duration_tester` (máquina de teste).
  - A `industrial_slaughterhouse` **não** aceita (pattern não lista a ability) e **não** usa a base
    multi-receita (`extends WorkableElectricMultiblockMachine`, não `IThreadModifierMachine`) —
    apesar de a doc `thread-hatch.*.md` listá-la como suportada.
  - `CoilWorkableElectricMultipleRecipesMachine` é **classe morta** (nunca instanciada/registrada).
- **Intenção (achada no `docs/roadmap/multiblock-port-manifest.md`):** regra 8 — "every
  recipe-processing controller is implemented on a GTNA multiple-recipes base and accepts the Thread
  Hatch"; ordem de entrega **passo 2** — "existing GTNA controller migration and regression tests".
  Ou seja: não é bug de pattern, é **fase de migração não executada**.
- **Por que não migrei agora:** migrar a slaughterhouse para `GTNAMultipleRecipesLogic` troca o
  pipeline de receita (o `afterWorking()` dela, que gera os drops via `handleRecipeIO`, e o
  `recipeModifier` estático de registro). `GTNAMultipleRecipesLogic` *chama* `afterWorking` e
  `handleRecipeIO(OUT)`, então é factível — mas é a fase 2 do manifest e exige validação própria.
- **Correção aplicada (escopo seguro):**
  - `thread-hatch.md/.en.md/.es.md`: removida a claim falsa da Industrial Slaughterhouse; agora
    dizem o que é verdade (base `WorkableElectricMultipleRecipesMachine`; hoje Duration Tester +
    KubeJS; demais controladores 🚧 fase 2 do manifest; GTCEu base e steam não suportam).
  - Gametest `threadHatchWiresIntoMultipleRecipesMachine` (8º): monta o duration_tester com uma
    Thread Hatch ZPM e afirma que o pattern aceita, que `addedToController` entrega a peça
    (`getThreadPartMachine() != null`) e que `GTNAMultipleRecipesLogic.getMaxThreads()` sobe para
    `1 + getThreadCount()`. Trava a fundação até a migração.
- **Validação:** `spotlessCheck` + `compileJava` + `runUnitTests` (10/10) +
  `runGameTestServer` (8/8).
- **Pendências abertas:** (a) **fase 2 do manifest** — migrar os controladores reais para a base
  multi-receita e passar a aceitar a Thread Hatch (máquina a máquina, com gametest); (b) evitar que a
  doc volte a prometer suporte que o código não tem.
- **Nota:** `CoilWorkableElectricMultipleRecipesMachine` **não é classe morta** — é a base reservada
  para os multiblocos com coil futuros (confirmado pelo autor em 2026-09-21). Não deletar.

### G-0011 (2026-09-21) — wireless steam hatch rejeitado + primeira camada de QA de wiring

- **Sintoma (relatado):** o **Wireless Steam Input Hatch** não é aceito nas máquinas steam
  multibloco do GTNA — "talvez porque não funciona como um steam input bus".
- **Causa raiz (confirmada em código + source do GTCEu 7.5.3):** o hatch **é** um hatch de vapor de
  fluido correto (`PartAbility.STEAM` + `IMPORT_FLUIDS`, tanque `IO.IN` filtrado em
  `GTMaterials.Steam`). O defeito estava no **predicado da estrutura**: 20 patterns fixavam o slot de
  vapor no **bloco exato** do hatch do GTCEu
  (`.or(blocks(GTMachines.STEAM_HATCH.getBlock()).setExactLimit(1))`), que nunca casa com o bloco do
  hatch wireless. As máquinas steam do próprio GTCEu usam o idioma por ability
  (`or(abilities(PartAbility.STEAM).setExactLimit(1))` — `GTMultiMachines.java:615/639`,
  steam grinder/oven). O `PRIMITIVE_DISTILLATION_TOWER` já listava os blocos wireless na mão
  (linhas 1047-1049), o que confirmava a intenção e a inconsistência.
- **Bug secundário que apareceria na sequência:** o `WirelessSteamOutputHatch` também declarava
  `PartAbility.STEAM`; com o slot por ability, um hatch de **saída** (tanque `IO.OUT`) poderia ocupar
  o slot de energia, `steamEnergy` ficaria `null` e a máquina se **desformaria** sozinha.
- **Correção:** 20 predicados → `abilities(PartAbility.STEAM).setExactLimit(1)`; o caso especial da
  torre de destilação virou `abilities(PartAbility.STEAM)`; e `PartAbility.STEAM` removido das duas
  registrations do output hatch (agora só `EXPORT_FLUIDS`).
- **Guards novos (a resposta à pergunta de QA):**
  - Unit `SteamWiringContractTest` (10º unit test): faz scan de `common/data` e **falha** se algum
    pattern voltar a fixar `blocks(GTMachines.STEAM_HATCH`; afirma que o input hatch declara
    `STEAM + IMPORT_FLUIDS` e que o output hatch **não** declara `STEAM`; e que ao menos uma máquina
    usa o predicado por ability.
  - Gametest `wirelessSteamHatchIsAcceptedAsSteamSource` (7º gametest): monta o
    `large_steam_alloy_smelter` (3x4x3) por código com o hatch wireless no slot de vapor, e afirma o
    retorno de `checkPatternAt` **e** que a máquina continua formada com o handler de energia de
    vapor exposto (`getCapabilitiesFlat(IO.IN, EURecipeCapability.CAP)` não vazio).
- **Teste negativo (disciplina):** reintroduzindo o pin exato no alloy smelter, o unit test falha
  apontando o arquivo:linha e o gametest falha **exatamente** na célula do hatch wireless
  (`relative=1,0,0`, bloco `gtna:wireless_steam_input_hatch`, erro "Expected components ...
  gtceu:steam_input_hatch"). Revertido; gate verde depois.
- **Validação:** `spotlessCheck` + `compileJava` + `runUnitTests` (10/10) +
  `runGameTestServer` (7/7). Sem `runData` (nada de lang/registro gerado mudou).
- **Pendências abertas:** replicar o gametest para as demais máquinas steam (hoje só o alloy smelter
  é exercised); validar o hatch wireless in-game (rede wireless de verdade, owner, transfer rate);
  conferir que boilers seguem aceitando o output hatch sem a ability STEAM.

### G-0010 (2026-09-20) — triagem de duas reviews chinesas

- **BUG-EXT-001 — “AE2 autocrafting trava depois de instalar GTNA”**
  - Tradução: ao instalar o mod, a autocrafting do AE2 fica completamente travada/inutilizável;
    não se sabe se é conflito ou bug próprio.
  - Achado de código: `CraftingCPUClusterMixin` substituía no construtor o executor de **todo**
    `CraftingCPUCluster` por `GTNAOptimizedCraftingCpuLogic`. Isso também atingia CPUs AE2
    normais e era um vetor direto para regressão fora do Nexus.
  - Mitigação aplicada na árvore de trabalho: CPUs nativas mantêm `CraftingCpuLogic` do AE2;
    o executor otimizado só é instalado quando `gtna$setMachine` anexa
    `GTNACraftingCPUInterfacePartMachine` ao cluster virtual do Nexus.
  - Validação: compilação, 9/9 unit tests e 6/6 GameTests verdes. Ainda falta reprodução manual
    com uma CPU AE2 normal e uma CPU virtual Nexus para confirmar o caso reportado em jogo.

- **BUG-EXT-002 — “armadura Quantum oculta e velocidade de voo não volta”**
  - A ocultação no inventário criativo é comportamento configurado: por padrão o mod está em
    `NORMAL`, e itens restritos só ficam habilitados em `JOURNEY` sem auto-restrição; com
    `hideFromJei=true` (nome legado da opção), o grupo `quantumCosmicNexusArmor` é ocultado da
    creative tab. É uma falha de descoberta/documentação, não um bug confirmado de registro do item.
  - A persistência de velocidade era plausível: o handler impunha `0.2F`, resetava para `0.05F`
    sem guardar o valor anterior e deixava o efeito `MOVEMENT_SPEED` dos boots por até 300 ticks.
  - Correção aplicada: estado anterior de `mayfly`, `flying`, velocidade, altura de passo e efeito
    de movimento é capturado por jogador e restaurado ao remover a armadura; há limpeza compatível
    com mundos onde a versão antiga deixou exatamente a velocidade quântica salva.
  - Validação: compilação, 9/9 unit tests e 6/6 GameTests verdes. A remoção/equipamento da armadura
    ainda precisa de teste interativo no client/server.

### G-0008 (2026-09-20) — 9 opções de config sem tradução + guard automatizado

- **Achado (auditoria preventiva depois do G-0007):** dos **97** campos `@Configurable` do
  `ConfigHolder`, **9** não tinham `config.gtna.option.<campo>` no en_us gerado —
  `primitiveDistillationTower`, `largeSteamLathe`, `largeSteamCutting`, `largeSteamFormingPress`,
  `meStorage`, `mePatternBufferProxy`, `meStorageAccessHatch`, `meBigStorageAccessHatch`,
  `meIOPortHatch`. A lib de config (`dev.toma.configuration`) monta o rótulo como
  `config.%s.option.%s` e **não asserta** — então era cosmético (chave crua na tela), não crash.
- **Correção (`eeaeba7`):** rótulos no `en_us.json` manual (a fonte que o
  `addManualTranslations()` injeta no gerado) + `pt_br.json`.
- **Guard novo:** `ConfigLangKeysTest` (8º unit test) faz parsing do `ConfigHolder` procurando campos
  `@Configurable` e afirma que cada um tem chave no en_us gerado, além de afirmar as duas chaves dos
  providers do Jade. **Verificado por teste negativo:** removendo uma chave, o teste falha. É a rede
  de segurança contra a classe inteira de bug do G-0007.
- **Validação:** `runUnitTests` (8/8), `spotlessCheck`, e regeneração completa sem diffs além das 9
  chaves.

### G-0007 (2026-09-20) — crash do client: tradução de config do Jade faltando

- **Sintoma (relatado ao rodar o client):**
  `java.lang.AssertionError: Missing config translation: config.jade.plugin_gtna.me_pattern_buffer`.
- **Causa:** o Jade cria **uma entrada de config por data provider registrado** e **afirma** que a
  tradução existe, derivando a chave do UID do provider:
  `config.jade.plugin_<namespace>.<path do uid>`. O `GTNAPatternBufferProvider` (UID
  `gtna:me_pattern_buffer`) entrou no commit `90b1102` **sem** a chave — bug pré-existente; o outro
  provider (`multiple_recipes`) tinha a chave, então a asserção parava nele.
- **Correção (`0032ddd`):** chave registrada no `GTNALangProvider` (en_us, regenerado por `runData`)
  + `pt_br.json`. Conferido que `GTNAJadePlugin` registra exatamente esses dois UIDs e nenhum outro.
- **Alcance:** em dev o `AssertionError` derruba o client; em produção o Jade mostraria a chave crua
  na tela de config.
- **Nota de processo:** isso é **client-only**, então nem `runUnitTests` nem `runGameTestServer`
  (servidor dedicado, sem config client do Jade) pegam. **Todo provider do Jade novo exige a chave.**

### G-0006 (2026-09-20) — gametests de estrutura endurecidos contra uma flakiness do matcher

- **Sintoma:** o teste `runningSecondRecipeTypeMirrorsControllerMode` falhava de forma intermitente
  (~1 a cada 6 runs) com `Maximum: 1` na célula do maintenance hatch.
- **Diagnóstico (com dump):** o erro **não** era sobra de estado. Instrumentei o teste para despejar
  todos os blocos não-ar da área no momento da falha: a área tinha **exatamente os 25 blocos** que o
  teste colocou, com **um único** maintenance hatch. Ou seja, o próprio matcher do GTCEu reporta o
  erro de limite para uma estrutura correta nessa definição. `MultiblockState.clean()` **reseta** os
  contadores, então não é contagem acumulada entre checagens — a causa raiz ficou em aberto.
- **Mitigação (`d22f6a8`):** o build da estrutura virou `buildDurationTester(...)` e o match é
  tentado **até 3 vezes** (apagando e reconstruindo entre tentativas). Isso não esconde regressão de
  feature: uma regressão falha as asserções de receita/modo, não o match, e um erro de estrutura
  consistente falharia nas 3 tentativas.
- **Outras mudanças:** template `empty_12` (12³) no lugar do `empty_5x5` (morto), com os dois testes
  de estrutura em **quadrantes disjuntas** (origem `2,2,2` e `8,2,8`) e wipe por teste; e as
  mensagens de falha agora despejam a área + a célula que falhou relativa ao controller.
- **Validação:** 8 execuções consecutivas verdes (5/5 cada), além de `spotlessCheck` + `runUnitTests`.

### G-0005 (2026-09-20) — troca de modo automática em multiblocos do mod **base**

- **Problema:** o espelho de modo existia só nas máquinas do GTNA, porque ele mora em
  `GTNAMultipleRecipesLogic`. Máquinas multi-modo do GTCEu (`large_cutter` = cutter+lathe,
  `multi_smelter` = furnace+alloy_smelter, o conjunto GCYM, ...) usam a `RecipeLogic` de estoque,
  que tem um bloqueio a mais: `searchRecipe()` procura **só** `recipeTypes[activeRecipeType]`, então
  um pattern do outro tipo nunca é encontrado — ovo-e-galinha.
- **Solução (`733521e`)**: inject no **HEAD de `RecipeLogic.searchRecipe()`** (antes do corpo da
  busca) que, se a máquina oferece >1 tipo, pergunta aos pattern buffers do controller qual modo
  está pendente e aplica a fórmula de modo da GTM.
  - API nova: `IPatternBufferModeProvider.gtna$getPendingModeId()` — devolve o modo do **filtro do
    buffer** se ele estiver pinado, senão o `preferredModeId`/`derivedModeId` de um slot que
    **realmente tem insumo staged** (pattern que não pode rodar não puxa a máquina).
  - Regra pura `BufferModeSwitchPolicy.selectTargetIndex()`: troca só com a logic **IDLE**, só para
    um tipo que a máquina oferece, e nunca como no-op. Idle-only é o que dá a segurança: máquina
    ociosa não achou receita no modo atual, então nada é interrompido.
  - `ConfigHolder.machines.bufferDrivenMachineMode`, **default ON** (o opt-in real é colocar o
    buffer na máquina; a config é escape hatch).
  - Guards: pula `GTNAMultipleRecipesLogic` (já espelha) e ignora logic que não é de controller
    (`LargeCombustionEngineMachine` também chama `searchRecipe`).
- **Cobertura:** 5º gametest `patternBufferDrivesBaseMachineMode` monta o `multi_smelter` do GTCEu
  por código, forma, pina o buffer em `alloy_smelter` e afirma a troca 0 → 1 via
  `findAndHandleRecipe()`; limpar o pin deve deixar o modo quieto. Mais
  `BufferModeSwitchPolicyTest` para a regra.
- **Lacuna honesta:** o caminho de **conteúdo staged** do `gtna$getPendingModeId` (inputs empurrados
  pelo AE2 para o slot) não tem teste — o gametest cobre o caminho do **pin do buffer**.

### G-0004 (2026-09-20) — harness de gametest + espelho de modo end-to-end

- **Harness criado do zero**: run `gameTestServer` em `legacyForge.runs`, template
  `src/main/resources/data/gtna/structures/empty_5x5.nbt` (NBT gzip gerado à mão, 5x5x5 vazio),
  classe `com.raishxn.gtna.gametest.GTNAMachineGameTests`, e CI com guarda do banner
  `GAME TESTS COMPLETE` + criação do `run/eula.txt`. Commits `fb05806`, `dd5e637`.
- **Bug real encontrado antes de qualquer teste rodar** (`998c8f8`): o mod **não carregava em
  servidor dedicado** — `Attempted to load class net/minecraft/client/Minecraft for invalid dist
  DEDICATED_SERVER`. Duas causas, ambas pré-existentes: (1) `GTNACORE` registrava
  `ConfigScreenHandler.ConfigScreenFactory` (client-only) no construtor comum; (2) `NexusTerminalBehavior`
  lia `Minecraft.getInstance().player` direto. Corrigido movendo o registro para `ClientProxy` e
  criando `ClientPlayerLookup` (client-only) acessado via `DistExecutor.unsafeCallWhenOn`.
- **4º gametest, o end-to-end do espelho** (`96829e5`, `2d8f5a8`):
  `runningSecondRecipeTypeMirrorsControllerMode` monta o `duration_tester` formado, injeta uma
  receita trivial no `CIRCUIT_ASSEMBLER_RECIPES`, alimenta energia e itens, e afirma que o
  `activeRecipeType` vai de 0 (assembler) para 1 (circuit assembler).
- **Validação:** `All 4 required tests passed`; `runData` sem alterações inesperadas.

### G-0003 (2026-09-20) — Fase C: seletor de modo no buffer (paridade GTOCore)

- **`21265ae`**: `selectedModeId` no part machine (`@Persisted` + `@DescSynced`, vazio = todos),
  filtro aplicado em `gtna$slotAcceptsRecipe` **antes** do filtro por slot (um ponto cobre slot
  handlers, fast-path de receita cacheada e matcher de slots), `verifySelectedMode()` (só no
  servidor) e `PatternBufferModeConfigurator` como side tab fancy no buffer.
- **`6af82cb`**: docs (Fase C + estado real dos gametests).
- Lang keys no `GTNALangProvider` (en_us regenerado por `runData`) e no `pt_br.json`.
- **Validação:** gate verde + coberto pelo gametest `bufferModeFilterGatesSlotAcceptance`.

### G-0002 (2026-09-20) — espelho de modo alcançável + output ME lossless

- **`7e25155`** — a Fase 1 estava **inerte**: o wire-up existia, mas (a) **nenhuma máquina Java
  declarava mais de um recipe type** (scan dos 40 registros: zero `.recipeTypes(...)`, zero
  `.recipeType()` repetido), e `gtna$resolvePatternBufferMode` devolve `null` com `length <= 1`, e
  (b) `gtna$getPreferredModeForRecipe` era **código morto** (declarado e implementado, sem call
  site). Corrigido: `duration_tester` passou a declarar `ASSEMBLER_RECIPES` +
  `CIRCUIT_ASSEMBLER_RECIPES`, o provider foi ligado em `tryStartRecipe` (com fallback no tipo
  exato para slots AUTO e pins legados) e a decisão virou a função pura
  `PatternBufferModeSelection.select` + `PatternBufferModeSelectionTest`.
- **`0958e20`** — output ME lossless (paridade com o `Ticker` do GTLCore, versão **híbrida**):
  `pendingNetworkOutput` persistido guarda só a sobra da inserção inline; `NetworkOutputTicker`
  (`IGridTickable`, 5..80 ticks, `SLEEP`/`SLOWER`/`URGENT`, lotes de 64 ops, 5 falhas seguidas) e
  `alertDevice` na transição vazio→não-vazio. O `simulate` continua reportando a sobra (anti-jam).
- **Validação:** gate verde (6 unit tests). O espelho tem gametest; **o drain ticker NÃO tem teste
  de runtime** (ver pendências).

### G-0001 (2026-09-19) — Fase 3: split do pattern buffer

- **`d2f9497`, `3b28fd1`, `74c8a1b`** (doc em `3012820`): `GTNAMEPatternBufferPartMachine` caiu de
  **2759 → 1192 linhas**, dividida em `PatternSlotResolver` (busca/matching por slot),
  `PatternBufferModeRegistry` (descoberta de modos + labels) e `PatternBufferUI` (páginas, config
  panel, widgets, seleção/preview). A máquina mantém estado persistente e ações de domínio; um
  back-reference `@Nullable patternBufferUI` roteia refreshes de preview (`refreshUiPreview()`).
- **Validação:** gate verde em cada extração.

### G-0000 (2026-09-19) — base herdada (sessões anteriores)

- Fase 0/1 (`d8e1494`), Fase 2 — três suítes unitárias (`eba02db`), correção do `Int128` (`5a7129a`)
  e a linha de fidelidade do pattern buffer (`6f46134` … `90b1102`: hidden terminal, copy/paste,
  catalisadores, proxy, cache por slot, closed-circuit, Jade). Documentado no audit doc; aqui fica
  só como contexto — **não** foi validado por gametest na época (o harness não existia).

## Pendências abertas (priorizadas)

1. **Reproduzir o caso de autocrafting AE2 (G-0010 / BUG-EXT-001)** — testar uma CPU AE2 nativa
   com padrões comuns e, separadamente, a CPU virtual do Nexus; observar se a CPU nativa continua
   usando o executor original e guardar versões/modlist/log se o travamento persistir.
2. **Teste interativo da armadura Quantum (G-0010 / BUG-EXT-002)** — equipar/remover o set completo
   e os boots, inclusive após trocar survival/creative, afirmando velocidade de voo, `mayfly`,
   efeitos e altura de passo; conferir também o item em `NORMAL` e `JOURNEY`.
3. **Teste de runtime do output ME lossless / drain ticker** — único bloco grande sem cobertura de
   runtime. Exige montar um **grid AE2** no gametest (pattern buffer + controller AE) e simular
   rede cheia / sem energia de AE, afirmando que a sobra fica em `pendingNetworkOutput` e entra
   depois. Todo o resto (harness, notas de campo) já está pronto.
4. **Cobrir o caminho de conteúdo staged do auto-switch** — o gametest do `multi_smelter` exercita o
   **pin do buffer** (`selectedModeId`); falta exercitar o `gtna$getPendingModeId` quando são os
   **inputs empurrados pelo AE2** que definem o modo do slot (exige criar/pushar um pattern de
   processamento no gametest).
5. **Fase 3 restante** (do audit doc):
   - Split de `AnnihilateGeneratorA/B` → aisles em `common/data/multiblock/`;
   - Split de `GTNAMachines` por domínio;
   - Internacionalizar as strings hardcoded de UI (`WorkableElectricMultipleRecipesMachine`,
     `GTNAMultipleRecipesLogic`);
   - Fundir `getRecipeModifier` (preview/EMI) com o caminho de execução, se fizer sentido.
6. **Higiene de testes:**
   - Hoje os gametests ficam em `src/main/java/.../gametest/` e portanto **vão no jar** (inertes em
     jogo normal). O UFO Future usa sourceset/mod de teste separado — é o refinamento natural.
   - Migração opcional dos unit tests `main()`-based para JUnit 5 (como o UFO Future).
   - A lista `testClasses` em `build.gradle` é **manual**: todo teste novo precisa ser registrado
     ali, senão nunca roda.
7. **`CHANGELOG.md` parado em `0.3.2-dev`** enquanto o mod é `0.4.0`. O fix de servidor dedicado
   (`998c8f8`), o auto-switch em máquinas do mod base (`733521e`) e o layout da UI (`7822893`)
   merecem entrada — falta decidir versão/data.
8. **Ampliar o QA (G-0011)** — a camada de lint de registro/wiring existe agora, mas é em grande
   parte manual:
   - **Camada 0 (feita parcialmente):** `SteamWiringContractTest` é o molde. Próximos lints:
     (a) toda `MachineDefinition` com `PartAbility.STEAM` também declara `IMPORT_FLUIDS` se é fonte
     (e não declara STEAM se é output); (b) toda peça com ability é aceita por ao menos uma máquina;
     (c) roda `runData` e falha se `git diff` não estiver vazio (recurso gerado/lang stale).
   - **Camada 2 (gametest matriz):** hoje cada teste de estrutura é montado à mão. Vale um helper
     data-driven `(machine, parte) → forma?` para varrer as duplas documentadas, começando pelas
     máquinas steam restantes.
   - **Camada 4 (processo):** toda review/bug externo vira teste de regressão (automatizado quando
     der, item de checklist manual quando não) — o G-0010 só fez isso no caso do AE2; o G-0011
     fechou o ciclo no hatch wireless.
   - **Docs como contrato:** `docs/gameplay/parts/wireless-steam-hatches.md` prometia "qualquer
     multiblocko" e não era verdade até o G-0011, e `thread-hatch.*.md` prometia suporte da
     Industrial Slaughterhouse que não existe (G-0012). Avaliar um lint que confira claims de doc
     contra o registro (blocos citados existem) e uma nota de "validado por" nos docs de peça.
9. **Fase 2 do manifest — migrar controladores para a base multi-receita (G-0012).** Regra 8 do
   `multiblock-port-manifest.md`: todo controlador de receita deveria estar em
   `WorkableElectricMultipleRecipesMachine` e aceitar a Thread Hatch. Hoje só o `duration_tester`.
   Migrar máquina a máquina (começando pela `industrial_slaughterhouse`, que a doc já anunciava),
   validando que `afterWorking()`/drops e o `recipeModifier` continuam corretos, com gametest por
   máquina. (`CoilWorkableElectricMultipleRecipesMachine` **fica**: é a base dos multiblocos com coil
   futuros.)
10. **Teste de runtime do Output Boost (G-0013).** O bug do M² foi corrigido por lint de fonte, mas
    não há teste que complete uma receita com hatch de boost e afirme o número de itens no bus de
    saída (`M`, não `M²`). Candidato a gametest no `duration_tester` (já tem input/output bus e
    energia no harness do `runningSecondRecipeTypeMirrorsControllerMode`).

## Notas de campo (custaram iteração — não redescobrir)

**Gametest / multibloco**

- **`runGameTestServer` sai com código 0 mesmo quando o mod falha ao carregar.** O CI precisa da
  guarda `grep -q "GAME TESTS COMPLETE" run/logs/latest.log`. Foi assim que o crash de servidor
  dedicado passou batido.
- O `run/` é gitignored: **o servidor de teste exige `run/eula.txt`** (`eula=true`). O CI cria.
- **Geometria de padrão:** `FactoryBlockPattern.start()` = `(charDir=LEFT, stringDir=UP,
  aisleDir=FRONT)`. Com o controller virado para NORTH, o índice de **char → -X**, o de **string →
  +Y** e o de **aisle → -Z**, com a célula do controller como **origem**. No `duration_tester` isso
  põe o buraco de ar `#` **+1 em Z** do controller, e a casca em `-1..+1` em X/Y mas `0..+2` em Z.
- **`setExactLimit(1)` é mínimo E máximo.** O `duration_tester` exige **maintenance hatch**
  obrigatório (além do energy hatch que o `autoAbilities` pede com `setMinGlobalLimited(1)`).
- **`EnergyContainerList` (o que o controller expõe) não implementa `addEnergy`** — o default da
  interface é no-op. Para dar energia, use o `NotifiableEnergyContainer` do próprio
  `EnergyHatchPartMachine` (campo público `energyContainer`).
- **`onStructureFormed()` marca `isFormed() = true` incondicionalmente.** Afirme o retorno de
  `checkPatternAt(state, false)` e imprima `state.error.getErrorInfo()`, senão um match falho parece
  uma estrutura formada.
- A energia é exigida **até na passada simulada**: `NotifiableEnergyContainer.handleRecipeInner`
  consulta `getEnergyStored()` mesmo com `simulate = true`.
- Receitas podem ser injetadas em runtime via `type.getAdditionHandler().beginStaging()/addStaging()/
  completeStaging()` — evita depender do datapack e ainda exercita o tipo real.
- **Flakiness conhecida do matcher:** o `duration_tester` ocasionalmente falha o `checkPatternAt`
  com `Maximum: 1` **mesmo com a estrutura correta** (confirmado por dump da área: 1 maintenance
  hatch, 25 blocos exatos). Não é sobra de estado nem contagem acumulada (`clean()` reseta). A
  mitigação é o retry com rebuild no teste; se um dia isso reaparecer em outro teste, **não perca
  tempo caçando blocos fantasmas** — duplique o retry e siga.
- **Isolamento entre testes de estrutura:** o template é todo ar, então o framework não "limpa"
  nada ao reposicionar; e os testes podem se atropelar. Use **quadrantes disjuntas** dentro de um
  template maior (`empty_12`, origens `2,2,2` e `8,2,8`) + wipe da própria área antes de construir.
- **Diagnóstico que vale ouro:** em falha de `checkPatternAt`, inclua no `helper.fail` o
  `state.error.getErrorInfo()`, a célula do erro **relativa ao controller** (`state.error.getPos()`)
  e um dump de todos os blocos não-ar da área. Foi isso que provou que o erro era do matcher e não
  do teste.

**UI fancy (LDLib / GTCEu)**

- **A página não recorta os filhos.** `FancyMachineUIWidget.setupFancyUI` dimensiona a moldura a
  partir de `Math.max(86, page.getSize().height + border*2)` **mais** a altura do
  `PlayerInventoryWidget` (86) quando `hasPlayerInventory()`; o `WidgetGroup.drawInBackground` só
  checa `isVisible()`, nunca a caixa. Resultado: widget posicionado além da borda da página **é
  desenhado** — por cima da moldura e do inventário. Foi o bug do G-0009.
- **Consequência prática:** todo layout de UI precisa de uma **fonte de verdade da geometria** e de
  um teste que a valide (padrão `PatternBufferLayout` + `PatternBufferLayoutTest`). `setSize` da
  página é derivado, nunca ajustado "no olho".
- **Orçamento vertical:** numa tela 1080p em GUI scale 3 sobram **360 px lógicos**. Página + moldura
  (`2*4`) + inventário (`86`) tem que caber nisso. `hasPlayerInventory()` é consultado **uma vez**, na
  construção do `FancyMachineUIWidget` (que roda no servidor) — não dá para decidir por tela.
- **Labels são 9 px de altura** (`fontRenderer.lineHeight`) e **não têm largura máxima** — texto
  longo simplesmente vaza para a direita. Para valores de tamanho variável, use
  `compactDisplay`/`formatModeLabel` (encurta o id e deixa legível) em vez de mostrar o registry id
  cru. Não existe tooltip dinâmico em `Widget` (só estático na construção), então diagnóstico que
  muda com a seleção tem que ser **label com supplier**, não tooltip.
- `WidgetGroup.isActive()` do **pai** bloqueia o despacho de clique para os filhos
  (`mouseClicked` só chama filho com `isVisible() && isActive()`), mas `isVisible()` do filho é o que
  controla o desenho — dá para deixar um grupo inteiro inerte/oculto sem reconstruir a UI.

**Dist (cliente vs servidor)**

- **Nunca referencie `net.minecraft.client.*` de uma classe comum**, nem dentro de lambda: o
  `RuntimeDistCleaner` rejeita o carregamento no servidor dedicado e o mod inteiro falha ao carregar.
  Lambdas sintéticas contam (o descritor do método referencia o tipo client). Use uma classe
  `@OnlyIn(Dist.CLIENT)` acessada via `DistExecutor`.
- Ao mexer em registro/estrutura/entrada do mod, **rode `runGameTestServer`**: ele é o único gate
  que carrega o mod num servidor dedicado de verdade.

**Wiring de peças e máquinas (G-0011)**

- Predicado de slot de peça se escreve por **ability**, não por bloco exato:
  `or(abilities(PartAbility.X))` e não `or(blocks(outroMod.MINHA_PECA.getBlock()))`. O idioma por
  bloco exato recusa silenciosamente qualquer peça que declare a ability (foi o caso do hatch
  wireless). O GTCEu usa `abilities(PartAbility.STEAM)` nas próprias máquinas steam — é a
  referência.
- **Ability errada em output quebra na formação, não no pattern:** um hatch de saída que declarasse
  `PartAbility.STEAM` ocuparia o slot de energia por ability, o `SteamMultiMachineBase` não acharia
  fonte `IO.IN` e chamaria `onStructureInvalid()` — a máquina "desforma sozinha" logo após formar.
  Output = só `EXPORT_FLUIDS`.
- **`checkPatternAt` + dump da área** continua sendo o melhor diagnóstico: o teste negativo do
  G-0011 mostrou o bloco `gtna:wireless_steam_input_hatch` na célula `relative=1,0,0` e o erro
  "Expected components ... gtceu:steam_input_hatch", que aponta direto para o predicado culpado.
- **Geometria de pattern por código:** `offset = (1 - char, string - s0, 2 - aisle)` com o
  controller em `(char 1, string s0, aisle 2)`; confirmado em `large_steam_alloy_smelter` (s0 = 1)
  e `multi_smelter` (s0 = 0).

**Build / assets**

- `src/generated/resources` é **rastreado** (só `.cache` é ignorado). Mudou o `GTNALangProvider`?
  Rode `./gradlew runData` e **commite** o `en_us.json` regenerado.
- `src/main/resources/assets/gtna/lang/pt_br.json` tem **BOM e line endings mistos**. Ferramentas que
  reescrevem o arquivo normalizam tudo e geram diff de centenas de linhas — insira chaves preservando
  os bytes (edição binária), como foi feito.
- `spotlessApply` **remove imports não usados** e reordena; rode-o antes de compilar.
- **Opção de config nova precisa de lang**: `dev.toma.configuration` resolve o rótulo por
  `config.gtna.option.<nomeDoCampo>`. Adicione no `GTNALangProvider` (en_us, regenerado por
  `runData`) **e** no `pt_br.json`, senão a tela de config mostra a chave crua.
- **Provider do Jade novo exige lang**: o Jade afirma a existência de
  `config.jade.plugin_<namespace>.<path do UID>` e **derruba o client em dev** se faltar. Mantenha em
  sincronia com os `registerBlockDataProvider`/`registerBlockComponent` do `GTNAJadePlugin`.
- **Ponto cego de validação:** os dois gates automatizados rodam sem client (unit tests são lógica
  pura; o gametest server é servidor dedicado). Nada que seja de client — config do Jade, renderer,
  texturas, tooltips de client — é pego por eles. Mudanças nessa área precisam de `./gradlew runClient`
  manual. Parcialmente mitigado pelo `ConfigLangKeysTest` (ver G-0008).
- **Datagen pode "pular" o provider:** o `en_us.json` manual é lido pelo
  `GTNALangProvider.addManualTranslations()`, mas **não é input rastreado** do datagen — editar só
  ele pode resultar em `runData` escrevendo `written: 0` e o gerado ficar velho. Se o gerado não
  pegar sua edição, **apague `src/generated/resources/.cache`** e rode `runData` de novo (foi o que
  fez as 9 chaves de config entrarem).

**Feature de modo do pattern buffer**

- O **modo global do controller é espelho de display**; o roteamento real é **por slot**
  (`gtna$slotAcceptsRecipe`). O `activeRecipeType` é global e **não** serve de estado de roteamento
  com N threads de tipos diferentes — com threads concorrentes o tab vai alternar.
- **Máquinas do mod base (GTCEu/GCYM)** têm o bloqueio extra: `RecipeLogic.searchRecipe()` procura
  **só** o tipo ativo, então o modo precisa estar certo **antes** da busca — é por isso que o
  auto-switch vive no HEAD de `searchRecipe()` (mixin `gtna$switchModeForPendingBufferContent`) e
  não depois de achar a receita, como nas nossas máquinas.
- Há **muitas** máquinas multi-tipo no GTCEu/GCYM (não só a cutter): `multi_smelter`
  (furnace+alloy_smelter, **3x3x3** — o alvo barato para gametest de máquina base), o conjunto GCYM
  de 2 a 4 tipos, etc.
- Um pin não-vazio em `preferredModeId` **só** aceita receitas cujo tipo casa (é a invariante que
  `gtna$slotAcceptsRecipe` garante); por isso o espelho pode usar o tipo exato da receita como
  fallback sem perder fidelidade.
- **Perda de output (contexto):** o `RecipeRunner` do GTCEu **voida** a sobra de output quando o
  controller é `IVoidable` (`canVoidRecipeOutputs`) e trata `PASS_NO_CONTENTS` como sucesso; além
  disso, no match simulado ele **pula** a checagem de espaço para capabilities voidáveis. Era por
  isso que "não tinha problema" até a rede encher.

## Comandos

```bash
# Gate completo (unit tests + formatação)
./gradlew spotlessCheck runUnitTests

# Compilar
./gradlew compileJava

# Datagen (obrigatório se mexer em GTNALangProvider / assets / registro)
./gradlew runData

# Gametests (sobe um servidor dedicado de verdade e roda os @GameTest)
mkdir -p run && echo "eula=true" > run/eula.txt   # run/ é gitignored
./gradlew runGameTestServer
grep -q "GAME TESTS COMPLETE" run/logs/latest.log && echo OK || echo "NAO RODOU"
```

## Referências

- Auditoria / roadmap técnico: `docs/roadmap/technical-audit-pattern-buffer.md`
- Fidelidade vs GTLCore/GTOCore: `docs/roadmap/pattern-buffer-fidelity-gap.md`
- Fontes de referência locais: `~/MineProjects/GTCEu-7.5.3`, `~/MineProjects/GTLCore`,
  `~/MineProjects/GTOCore-Main`, `~/MineProjects/GTLAdditions`
- Projeto irmão com o mesmo harness (e sourceset de teste separado):
  `~/MineProjects/UFO-Future-1.21.1` (ver `CONTINUITY_LEDGER.md` de lá)
