# Correções de fidelidade GTNA ↔ GTOCore — feedback in-game de 2026-09-26

Documento de handoff para a próxima sessão. Contém **todos** os achados do teste in-game do autor
no cliente de desenvolvimento, com as capturas de tela em `img/`, análise técnica, causa provável,
correção proposta e critério de aceite. Também cobre a divergência de **qualidade de informação e
design** das tooltips/UIs do GTOCore em relação às do GTNA.

> **Atualização G-0117:** a causa do CTM de Blaze/Cold Ice foi identificada: faltavam os
> `.png.mcmeta` das folhas base `_ctm`, então o atlas não separava os quadros animados. Os dois
> arquivos foram copiados 1:1 do GTO. A Grinding Ball Hatch usa os sprites originais de hélice
> idle/spinning; a UI da Blaze recebeu as cores da captura; as tooltips dos 16 multiblocos desta
> leva foram refeitas no padrão aprovado, com somente `Source`. O cliente ainda precisa confirmar
> visualmente CTM, hélice e interfaces após essa alteração. O gate completo passou (26 unitários,
> 94 GameTests, datagen); veja `CONTINUITY_LEDGER.md` G-0117 para o estado detalhado.

- Branch: `main` (local, **sem commit/push**; HEAD `3c1fee8`).
- Estado do gate na última execução: `spotlessCheck compileJava runUnitTests runGameTestServer runData
  --offline` verde — **26/26 testes unitários**, **94/94 GameTests**, `runData written: 0`.
- Correções já aplicadas nesta sessão: ver `CONTINUITY_LEDGER.md` **G-0113** (sync do contador de
  módulos, cache de re-detecção, bloom dos casings, lang dos recipe types, tooltip do módulo,
  tooltip do ISA Mill, UI de threads, iridium casing na assembly line, render da esfera) e
  **G-0114** (extensão do Component Assembler/assembly line até UV).
- O que **continua quebrado** ou divergente está listado abaixo; nada foi marcado como resolvido sem
  verificação in-game.

---

## Índice

- [F-01 — Texturas dos casings Blaze e Cold Ice divergentes](#f-01--texturas-dos-casings-blaze-e-cold-ice-divergentes)
- [F-02 — Grinding Ball Hatch com textura roxa/preta (missing texture)](#f-02--grinding-ball-hatch-com-textura-roxapreta-missing-texture)
- [F-03 — ISA Mill: throughput divergente (paralelismo de conteúdo do gtolib)](#f-03--isa-mill-throughput-divergente-paralelismo-de-conteúdo-do-gtolib)
- [F-04 — ISA Mill/UI: informação pobre vs. Jade do GTO](#f-04--isa-millui-informação-pobre-vs-jade-do-gto)
- [F-05 — Módulos (Cold Ice e todos): delay e necessidade de forçar re-scan](#f-05--módulos-cold-ice-e-todos-delay-e-necessidade-de-forçar-re-scan)
- [F-06 — Tooltip de blocos sem Hardness/Blast Resistance](#f-06--tooltip-de-blocos-sem-hardnessblast-resistance)
- [F-07 — Divergência geral de tooltips/UIs/Jade (GTO × GTNA)](#f-07--divergência-geral-de-tooltipsuísjade-gto--gtna)
- [F-08 — Ruído do EMI/JEI no ambiente de dev](#f-08--ruído-do-emijei-no-ambiente-de-dev)
- [Protocolo do teste controlado do ISA Mill](#protocolo-do-teste-controlado-do-isa-mill)
- [Mapa de arquivos por issue](#mapa-de-arquivos-por-issue)

---

## F-01 — Texturas dos casings Blaze e Cold Ice divergentes

**Capturas:** `img/01_cold_ice_casing_gtna.png` (GTNA) e `img/02_cold_ice_casing_gto.png` (GTO).
Também relatado para o **Blaze Casing** ("ctm bugado" no G-0113).

**Observado (GTNA):** o casing aparece mais escuro/apagado, o padrão de ondas fica visível mas sem o
brilho e sem os detalhes de canto que o GTO mostra; o tooltip do bloco traz só o nome e a atribuição
`GregTech: Nexus Addon`.

**Observado (GTO):** mesmo padrão, porém mais claro/contrastado, com os cantos metálicos e o brilho
do bloom; o tooltip mostra `Hardness: 5 Blast Resistance: 6` e a atribuição `GTOCore`.

**Fatos verificados no código:**
- As texturas base **são idênticas** ao GTOCore (md5 conferido): `cold_ice_casing.png`,
  `cold_ice_casing_ctm.png` e `cold_ice_casing.png.mcmeta`. O bloom (`cold_ice_casing_bloom.png`,
  `cold_ice_casing_ctm_bloom.png` + mcmeta) foi copiado no G-0113 e o modelo passou a
  `gtceu:block/cube_2_layer/all` com `bot_all`/`top_all`.
- No GTOCore, `COLD_ICE_CASING` e `BLAZE_CASING` são `createTwoLayerCasingBlock` (base + bloom
  emissivo); o GTO **não** portou o helper, então o GTNA reimplementou com o parent de duas camadas.
- RenderType do GTNA: `cutoutMipped` nas duas camadas (`GTNABlocks.createTwoLayerCasingBlock`).

**Hipóteses para a diferença restante (verificar no cliente):**
1. O bloom do GTO é **emissivo** (`"emissive": true` + `"shimmer": {"bloom": true}` nos mcmeta) e o
   GTNA não tem o *shader*/lightmap de bloom do GTO; sem ele, o topo do modelo aparece como uma
   segunda camada opaca escura em vez de brilho. Comparar o render com
   `RenderType::translucent`/`solid` e decidir entre (a) aceitar a ausência do shimmer documentando,
   ou (b) usar o `emissive` do GTCEu (há modelos `emissive` no GTCEu para coils/lâmpadas).
2. O modelo `cube_2_layer/all` aplica a camada de bloom em **todas** as faces; o GTO pode usar CTM
   por face com UVs diferentes. Conferir no cliente com o preview do JEI e em parede/coluna.
3. A textura `_ctm` é aplicada pelo LDLib via mcmeta da textura **base**; confirmar que a camada de
   bloom também recebe CTM (o mcmeta do bloom aponta para `_ctm_bloom`, que tem `animation`+`shimmer`).

**Correção proposta:** reproduzir o renderer do GTOCore o mais perto possível —
- manter `cube_2_layer/all/bottom_top`;
- avaliar render type do bloom (emissivo/translucent) e registrar a decisão;
- adicionar teste visual/manual no checklist.

**Aceite:** o casing colocado no mundo e no JEI deve ficar visualmente igual ao GTO (base + brilho +
cantos), sem camada escura; tooltip com Hardness/Blast (ver F-06).

---

## F-02 — Grinding Ball Hatch com textura roxa/preta (missing texture)

**Captura:** `img/03_grinding_ball_hatch_gtna_textura_quebrada.png`.

**Observado:** a hatch colocada mostra, no centro da face frontal, um quadrado xadrez roxo/preto
(padrão de *missing texture* do Minecraft), sobre um casco verde. O tooltip do item é normal
(`Grinding Ball Hatch`, `GregTech: Nexus Addon`).

**Fatos verificados:**
- `ball_hatch.png` (16×16, RGBA) é **byte-idêntico** ao do GTOCore e está em
  `src/main/resources/assets/gtna/textures/block/overlay/machine/ball_hatch.png` — e foi copiado
  para `build/resources/main` (03:38).
- O modelo gerado (`src/generated/resources/assets/gtna/models/block/machine/grind_ball_hatch.json`)
  referencia `gtna:block/overlay/machine/ball_hatch` e registra o dynamic render
  `gtna:ball_hatch/ball`.
- A textura é um **mask** escuro (tons de cinza 22–62, com alpha), feita para ser tingida pelo
  `colorOverlayTieredHullMachineModel`; não é o magenta/preto.
- O `BallHatchRenderer` novo desenha o **item** da esfera em rotação; o quadrado roxo/preto parece
  ser um **modelo de item não resolvido** na renderização ou o overlay do template de hatch sem o
  *tint*. O casco verde na captura também não corresponde ao hull IV padrão do GTCEu, o que reforça
  a suspeita de modelo/tint errados no hatch.

**Correção proposta (mais fiel ao GTO):** portar o renderer do GTO de verdade, com quads de sprite
na face frontal:
- copiar de novo `ball_hatch_idle.png` e `ball_hatch_spinning.png` (+ `.mcmeta` do spinning) de
  `gtocore/textures/block/overlay/machine/`;
- implementar `BallHatchRenderer` como renderer de face (quads baked) usando
  `BALL_HATCH_OVERLAY`/`IDLE`/`SPINNING` exatamente como
  `GTOCore/client/renderer/machine/BallHatchRenderer.java`, em vez do item em rotação;
- garantir que o overlay estático `ball_hatch` continua aplicado pelo template;
- testar no cliente com hatch vazio (deve mostrar só o overlay estático) e com esfera (idle/spinning).

**Plano B (se o renderer baked não encaixar no GTCEu 7.5.3):** manter o item em rotação, mas corrigir
o modelo/tint do hatch e validar que o item render resolve (`getModelManager().getModel`). O
sintoma roxo/preto precisa ser eliminado em qualquer um dos planos.

**Aceite:** nenhuma textura roxa/preta; hatch com esfera mostra a esfera girando (ou o sprite
spinning) e sem esfera mostra só o overlay; UI/UI do item sem alteração.

---

## F-03 — ISA Mill: throughput divergente (paralelismo de conteúdo do gtolib)

**Capturas:** `img/04_isa_mill_gto_jade_completo.png` (GTO) e
`img/05_isa_mill_gtna_tooltip.png` (GTNA).

**Teste do autor (esfera soapstone até quebrar, mesmo hatch UV, insumo raw platinum + distilled
water, circuito 1):**
- GTO: **1632 saídas** (25 stacks + 32 itens) — 17 ciclos × 96 Milled Platinum.
- GTNA: **1200 saídas** (18 stacks + 48 itens) — 25 ciclos × 48 Milled Platinum.

**Prova na captura do GTO:** o Jade mostra `Using 491,520 EU/t (0.94A UV)`,
`Total Energy Cost Multiplier: 25,600%`, `Total Time Cost Multiplier: 0.77%`,
`Performing 2 Recipes in Parallel (Batch/OC Compensation x2)` e `Recipe Outputs: 96X [Milled
Platinum]`. Ou seja, o GTO roda **2 receitas em paralelo** por ciclo (96 saídas), enquanto o GTNA
roda 1 (48 saídas).

**Causa:** o port não reproduz o **paralelismo de conteúdo do gtolib**
(`ElectricMultiblockMachine` + `accurateContentParallel` + `getRealRecipe(RecipeHandlerUnit)`), que
o próprio inventário de fidelidade já listava como bloqueio. O `IsaMillMachine` do GTO estende
`ElectricMultiblockMachine` e o gtolib aplica o paralelo com compensação de batch/OC; o GTNA portou
a classe sobre `WorkableElectricMultiblockMachine` com `GTRecipeModifiers.PERFECT_OVERCLOCK`
apenas.

**Matemática que fecha com as contagens:** a fórmula de dano do GTO é
`dano = durabilidade + parallels/(Unbreaking+1) + 1`.
- GTO com `parallels = 2`: 3 de dano por ciclo → 50/3 ≈ 16–17 ciclos × 96 = **1536–1632**.
- GTNA com `parallels = 1`: 2 de dano por ciclo → 25 ciclos × 48 = **1200**.
Isso explica exatamente os dois números e confirma que a receita/overclock do port estão corretos.

**Correção proposta:** implementar no GTNA um paralelismo de conteúdo para as máquinas elétricas
(ou ao menos para o ISA Mill e as demais processadoras GTO), com a compensação do GTO:
- descobrir a fórmula de `accurateContentParallel`/`Batch/OC Compensation` (tentar
  `gtolib-release.jar`; os números do Jade — EU 25.600% e tempo 0,77% para 2 paralelos — são um
  ponto de partida: 1920 × 256 = 491.520 EU/t e 2400 × 0,0077 ≈ 18,5 ticks);
- se a fórmula não for recuperável, implementar uma aproximação documentada (paralelo limitado pelo
  EU disponível × multiplicador de conteúdo, com o dano da esfera usando o paralelo real);
- expor a decisão no tooltip/Jade (ver F-04).

**Protocolo de reteste:** ver a seção [Protocolo do teste controlado do ISA Mill](#protocolo-do-teste-controlado-do-isa-mill).

**Aceite:** com o mesmo hatch/insumo e a mesma janela de tempo, o GTNA produz dentro da mesma ordem
do GTO; a esfera desgasta na mesma proporção por lote; o Jade mostra o paralelo.

---

## F-04 — ISA Mill/UI: informação pobre vs. Jade do GTO

**Capturas:**
`img/04_isa_mill_gto_jade_completo.png` (GTO) × `img/05_isa_mill_gtna_tooltip.png` (GTNA) e
`img/06_isa_mill_gtna_ui_sem_grindball.png` (UI GTNA).

**O que o GTO mostra (Jade) e o GTNA não:**
| Informação | GTO | GTNA |
|---|---|---|
| Barra de progresso | `2 / 2 s` (tempo) | `8 / 9 t` (ticks) |
| Chunk não forçado | "The chunk the machine is in is not forced loaded" | ausente |
| Energia | `491,520 EU/t (0.94A UV)` | `0,77 A @ UV (404,374 EU/t)` (formato/ordem diferentes) |
| Multiplicador de custo total | `25,600%` | ausente |
| Multiplicador de tempo total | `0.77%` | ausente |
| Paralelo | `Performing 2 Recipes in Parallel (Batch/OC Compensation x2)` | ausente |
| Saídas | `96X [Milled Platinum]` | `48x [Milled Platinum]` |
| Estrutura/manutenção | `Structure Formed` / `Maintenance Fine` | iguais |
| Atribuição | `GTOCore` | `GregTech: Nexus Addon` |

**Observação:** a UI fancy do GTNA **já mostra** `Need to grind ball` quando falta a esfera
(captura 06), então esse item específico está coberto na UI; falta no **Jade**.

**Correção proposta:**
1. Criar um provider **Jade genérico para multiblocos GTNA** (progresso em tempo, EU/t com
   tensão/amperagem no formato do GTO, paralelo, multiplicadores de custo/tempo quando houver
   compensação, saídas, estrutura/manutenção, aviso de chunk não forçado). O GTNA já tem providers
   Jade pontuais (`integration/jade/provider`), então o registro segue o mesmo padrão.
2. Padronizar o texto de progresso para **segundos** quando o tempo for ≥ 1 s (como o GTO), mantendo
   ticks para receitas rápidas.
3. Mostrar o motivo do idle (ex.: `Need to grind ball`) também no Jade, com a chave já existente na
   UI.

**Aceite:** com o Jade instalado, a máquina formada mostra as mesmas linhas relevantes do GTO
(energia, progresso, paralelo/compensação, saídas, estrutura/manutenção); com a esfera faltando,
`Need to grind ball` aparece no Jade.

---

## F-05 — Módulos (Cold Ice e todos): delay e necessidade de forçar re-scan

**Captura:** `img/07_cold_ice_freezer_gtna_ui_forma_0.png` — `Formed modules: 0` com a UI ainda
mostrando a base, e o autor relata que **todos** os módulos têm delay e exigem "forçar scan".

**Estado após o G-0113:**
- O contador de módulos agora é sincronizado por `SModuleCountPacket` (S2C) quando muda.
- O mixin registra a **posição do erro** do sub-pattern no cache do controller, então recolocar o
  bloco que faltava deve notificar o controller sem scan manual.
- O autor testou **antes/depois?** O relato é posterior ao cliente que já continha essas mudanças
  (o cliente rodou o build com G-0113 + G-0114), então o sintoma **persiste** nesse cenário.

**Hipóteses a investigar na próxima sessão:**
1. **Delay do ciclo assíncrono:** o GTCEu revalida estruturas quebradas a cada 4 ticks
   (`asyncCheckPattern`). Para o módulo, a máquina continua formada (padrão base ok), então a
   revalidação depende de `MultiblockState.onBlockStateChanged` no chunk mapeado. Se o chunk do
   bloco quebrado não estiver no `chunkPosMapping`, nada acontece até o scan manual. O G-0113
   adiciona só a posição do **primeiro** erro; se o usuário quebrar um bloco **válido** do módulo
   (não o que estava com erro), a posição nova pode não estar no cache.
2. **Sincronização de parts:** ao reformar o módulo, as parts (energy hatch etc.) mudam; o cliente
   recebe `partPositions` (DescSynced), mas o `gtna$formedModuleCount` do cliente só é atualizado
   pelo pacote. Se o pacote for enviado antes de o cliente carregar o BE (ou o BE for recriado), o
   valor se perde.
3. **UI:** `Formed modules: 0` na captura indica que, naquele momento, o servidor também estava com
   0 (ou o cliente não recebeu o pacote). Capturar `latest.log` do servidor no momento do teste
   ajuda a distinguir.

**Correções propostas:**
- adicionar **todas** as posições dos sub-patterns ao cache do controller **sempre** que o padrão
  principal forma (não só a primeira que falhou), para que qualquer bloco do módulo dispare a
  revalidação;
- reenviar o `SModuleCountPacket` quando o cliente carregar o BE (ex.: em `onLoad` do lado do
  cliente, pedir o valor por um C2S `CModuleCountRequest` ou reenviar o pacote periodicamente no
  `onStructureFormed`/mudança de parts);
- considerar forçar `checkPatternWithLock` no `onBlockStateChanged` para posições do módulo (já é o
  que o GTCEu faz quando a posição está mapeada);
- adicionar um GameTest que quebra/recoloca um bloco do módulo de cada máquina com extensão
  (Cold Ice, EBF, Evaporation, turbinas, Component Assembler) e afirma que `formedModuleCount`
  volta a 1 **sem** `GTNAStructureRefresh.refresh(..., true)` manual.

**Aceite:** quebrar/recolocar qualquer bloco de módulo re-forma o(s) módulo(s) em ≤ 1 s e a UI do
cliente atualiza sozinha, sem botão de scan.

---

## F-06 — Tooltip de blocos sem Hardness/Blast Resistance

**Capturas:** `01_cold_ice_casing_gtna.png` (só nome + autor) × `02_cold_ice_casing_gto.png`
(`Hardness: 5 Blast Resistance: 6` + autor).

**Causa:** os blocos do GTNA usam `Block` puro (`GTNABlocks.createCasingBlock`), enquanto no GTO os
blocos passam pelo handler de tooltip do GTCEu/GTO que adiciona dureza e resistência a explosão.

**Correção proposta:** adicionar as duas linhas ao tooltip dos blocos GTNA (subclasse de bloco com
`appendHoverText`, ou o tooltip behavior equivalente do GTCEu), usando `getExplosionResistance()` e
`defaultDestroyTime()`. Vale para todos os casings/blocos novos (`cold_ice_casing`, `blaze_casing`,
`inconel_625_*`, `hastelloy_n_75_*`, `flotation_cell`, `red_steel_casing`,
`supercritical_turbine_casing`, os blocos do assembler, `iridium_casing`, etc.).

**Aceite:** todo bloco GTNA mostra `Hardness`/`Blast Resistance` como os equivalentes do GTO.

---

## F-07 — Divergência geral de tooltips/UIs/Jade (GTO × GTNA)

O autor destacou que as tooltips/UI do GTO são muito mais informativas. Comparação estrutural do
que o GTO coloca no item do controlador (ex.: Cold Ice Freezer, Rocket Turbine, Component Assembly
Line) versus o padrão atual do GTNA:

| Seção do GTO | Exemplo (Cold Ice Freezer) | GTNA hoje |
|---|---|---|
| História/lore | 4–5 linhas de texto (`GTOMachineStories`) | ausente |
| `Running Requirements` | `Requires 2^(tier-2)×10 mB/s of Liquid Ice` + nota de consumo | parcial (tooltip de 3 linhas) |
| `Auxiliary Module` | lista hatches **e** recipe types liberados | hatches + recipe types (G-0113) |
| `Time Cost Multiple` / `Parallel Number` / `Recipe Types` | valores numéricos explícitos | ausente |
| Atribuição | `GTOCore` (anexada pelo GTO) | `GTNA` já anexa (`GTNASources`) |
| UI fancy | modo de máquina, progresso, energia, módulo, paralelo | modo de máquina, energia, paralelo (parcial) |
| Jade | progresso/energia/multiplicadores/paralelo/saídas/estrutura | só em providers pontuais |

**Proposta (a ser decidida pelo autor):**
1. **Padrão de tooltip em camadas** (mantendo o esquema GTOCore-adaptado já documentado em
   `docs/roadmap/tooltip-standard.md`):
   - linha 1: `§6Main Function:§r` (obrigatória);
   - linhas seguintes: `Running Requirements`, `Auxiliary Module`, `Time Cost`/`Parallel`,
     `Recipe Types` — **sem limite rígido de 3**, com o teste
     `newGtoControllersDescribeTheirFunction` passando a ter contagem por máquina (mapa) em vez de
     assumir 3 para todas;
   - `Source:` continua centralizado em `GTNASources`.
2. **UI fancy:** expor os mesmos números que o GTO expõe no tooltip/UI (custo de tempo, paralelo
   efetivo, bônus do módulo, motivo do idle) — parte já existe, mas falta padronização por máquina.
3. **Jade:** uma camada genérica para multiblocos GTNA (F-04) e o aviso de chunk não forçado.
4. **Lore:** opcional; o autor decide se porta as histórias do GTO (há arquivos de texto no
   GTOCore: `GTOMachineStories`/`GTOMachineTooltips`) ou mantém a atribuição só no `Source:`.

**Aceite:** o autor define o padrão; a próxima sessão aplica o padrão a todas as máquinas GTO do
GTNA e atualiza o teste de contrato de tooltips de acordo.

---

## F-08 — Ruído do EMI/JEI no ambiente de dev

Não é regressão desta leva, mas polui o log e pode mascarar problemas reais:
- `java.lang.AbstractMethodError: dev/emi/emi/jemi/impl/JemiRecipeLayoutBuilder.addSlot ... is
  abstract` em massa — incompatibilidade EMI 1.1.13 × JEI 15.20.0.115 no dev.
- `[EMI] 2 recipes loaded with the same id: gtna:<id>` para ~40 controladores antigos (não aparece
  para as máquinas novas). Investigar se o GTNA registra a receita do controlador duas vezes
  (ex.: `GTNAMachineRecipes` + algo do Registrate/KubeJS).
- Sugestão: rodar o cliente de QA sem o EMI (`modRuntimeOnly` de dev) ou fixar a versão compatível,
  para o log de QA ficar limpo.

---

## Protocolo do teste controlado do ISA Mill

Para decidir se o que falta é só o paralelismo de conteúdo (F-03):
1. Abrir o cliente com o mod compilado.
2. Colocar **duas** ISA Mills idênticas (mesmo hatch UV, mesma manutenção, mesmo muffler) — uma com
   o port GTNA e, se possível, uma build de referência não é necessária: use a captura do GTO como
   referência.
3. Insira **raw platinum** (16) + **distilled water** (50) e **uma esfera soapstone nova** em cada;
   use circuito 1.
4. Deixe rodar **exatamente 60 s** (ou até a esfera quebrar, o que vier primeiro) e conte as saídas.
5. Registrar: número de ciclos, dano da esfera, saída total, EU/t médio e o texto do Jade.
6. Repetir com circuito 10 (esfera alumínio) para comparar com a captura do GTO.

**Referência GTO para o mesmo teste:** 96 saídas/ciclo, 2 recipes in parallel, 491.520 EU/t,
`Total Energy Cost Multiplier: 25,600%`, `Total Time Cost Multiplier: 0.77%`.

---

## Mapa de arquivos por issue

| Issue | Arquivos/âncoras |
|---|---|
| F-01 | `GTNABlocks.createTwoLayerCasingBlock`, `createSidedCasingBlock`, texturas `assets/gtna/textures/block/casings/{cold_ice,blaze}_casing*` |
| F-02 | `client/renderer/machine/BallHatchRenderer.java`, `common/machine/multiblock/part/BallHatchPartMachine.java`, `GTNAMachines2.registerGrindBallHatch`, `assets/gtna/textures/block/overlay/machine/ball_hatch.png`, modelos gerados do hatch |
| F-03 | `common/machine/multiblock/electric/IsaMillMachine.java`, `GTNAMachines3.ISA_MILL`, `GTNAIsaMillRecipes`, `GTRecipeLogicMixin` (muffler/overclock), `GTNAMultipleRecipesLogic`, `ParallelLogic`; gtolib `accurateContentParallel` |
| F-04 | `integration/jade/provider/*`, `WorkableElectricMultipleRecipesMachine.addDisplayText`, `GTNALangProvider`, `GTNAMachineGameTests.newGtoControllersDescribeTheirFunction` |
| F-05 | `mixin/gtceu/MultiblockControllerMachineMixin.java`, `network/packet/SModuleCountPacket.java`, `client/ModuleCountClientHandler.java`, `api/machine/multiblock/GTNAStructureRefresh.java` |
| F-06 | `GTNABlocks` (todos os `createCasingBlock`/`createSidedCasingBlock`), tooltip handler de blocos |
| F-07 | `docs/roadmap/tooltip-standard.md`, `GTNALangProvider`, `GTNASources`, `newGtoControllersDescribeTheirFunction` |
| F-08 | `build.gradle` (runtime de dev), registro duplicado de receitas de controlador |

## Referências das capturas

| Arquivo | Conteúdo |
|---|---|
| `img/01_cold_ice_casing_gtna.png` | Cold Ice Casing no GTNA (textura/modelo atuais) |
| `img/02_cold_ice_casing_gto.png` | Cold Ice Casing no GTO (referência, com Hardness/Blast) |
| `img/03_grinding_ball_hatch_gtna_textura_quebrada.png` | Ball Hatch com o quadrado roxo/preto |
| `img/04_isa_mill_gto_jade_completo.png` | Jade do ISA Mill no GTO (progresso, EU/t, multiplicadores, paralelo, 96 saídas) |
| `img/05_isa_mill_gtna_tooltip.png` | Tooltip/janela do ISA Mill no GTNA (8/9 t, 48 saídas) |
| `img/06_isa_mill_gtna_ui_sem_grindball.png` | UI do ISA Mill no GTNA sem esfera ("Need to grind ball") |
| `img/07_cold_ice_freezer_gtna_ui_forma_0.png` | UI do Cold Ice Freezer com "Formed modules: 0" |
| `img/08_vacuum_drying_furnace_gtna_ui.png` | UI da Vacuum Drying Furnace |
| `img/09_blaze_blast_furnace_gtna_ui.png` | UI do Blaze Blast Furnace |
