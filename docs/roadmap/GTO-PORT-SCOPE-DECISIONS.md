# Decisões de escopo — ports GTO até o Component Assembler assembly line (2026-09-25)

Decisões do autor para a próxima sessão em loop. Complementa
`GTO-FIDELITY-PORT-INVENTORY.md` e `NEXT-SESSION-PORTS-2026-09-25.md`.

## Escopo INCLUÍDO (ordem de execução)

1. **Módulo de atomização do Cold Ice Freezer** (sub-pattern no controlador existente) —
   ✅ **CONCLUÍDO (G-0112, 2026-09-26)**: sub-pattern do GTO registrado como módulo GTNA (Naquadah
   Alloy + Heat Vents + Cold Ice Casing + moldura de Naquadah), recipe type
   `ATOMIZATION_CONDENSATION_RECIPES` liberado só com a extensão formada e receitas geradas para
   todos os materiais GTCEu/GTNA com dust + fluido. Substituições documentadas: gás de alta pressão
   do GTO → gás regular na mesma quantidade; `GTOUtils.getVoltageMultiplier` → fórmula equivalente
   do GTCEu. Gate com 26/26 unit e 67/67 GameTests na época; QA in-game pendente.
2. **ISA Mill** — ✅ **CONCLUÍDO (G-0104, 2026-09-25)**: estrutura 7×3×3, carcaças Inconel-625,
   prefixo `MILLED`, data key `grindball`, Ball Hatch funcional (sem renderer) e as 48 receitas.
   A receita do controlador foi portada como Assembly Line; os três materiais GTO que ela exige
   (Inconel-625/792, Tantalloy-61) foram portados 1:1. Gate: 69/69 GameTests, `runData written: 0`.
3. **Rocket Large Turbine** — ✅ **CONCLUÍDO (G-0105, 2026-09-25)**: `rocket_large_turbine`
   (EV, `special=true`, base `V[EV] × 2,5 = 5120 EU/t`) com o caminho não-mega da `TurbineMachine`
   do GTO, o recipe type `gtna:rocket_engine`, o módulo do motor de foguete (2× saída / +20%
   eficiência / 2× dano) e a única receita de `RocketFuel` do GTCEu. Sem blocos novos (tudo GTCEu).
   Bloqueio rígido documentado: gtolib sem fonte → base `WorkableElectricMultiblockMachine` do GTCEu
   + `ParallelLogic.getParallelAmount`; o `ItemPartMachine` de auto-insert de rotor e a receita do
   controlador (item central exclusivo do GTO) não foram portados. Gate: 26/26 unit, 71/71
   GameTests, `runData written: 0`.
4. **Supercritical Steam Turbine** — ✅ **CONCLUÍDO (G-0106, 2026-09-25)**: `supercritical_steam_turbine`
   (IV, `special=false`, base `V[IV] × 2 = 16384 EU/t`) sobre a base não-mega
   `GTNALargeTurbineMachine` extraída do port da rocket turbine, o recipe type
   `gtna:supercritical_steam_turbine`, o módulo SUPERCRITICAL (2× saída / +20% eficiência / 2×
   dano) e a carcaça nova `supercritical_turbine_casing` (texturas GTO, CC BY-NC-SA). A receita de
   combustível reusa `DenseSupercriticalSteam` no lugar do `SupercriticalSteam` do GTO com os
   mesmos números (80 mB → 8 mB / 30 ticks / `V[MV]`); a receita do controlador foi portada
   (Assembler, só GTCEu/GTNA). Gate: 26/26 unit, 73/73 GameTests, `runData written: 0`.
5. **Industrial Flotation Cell** — ✅ **CONCLUÍDO (G-0107, 2026-09-25)**: `industrial_flotation_cell`
   (estrutura 9×7×7 do `.mbs` do GTO, carcaças Hastelloy-N75 casing/gearbox/pipe e paredes
   `FLOTATION_CELL`, Parallel Hatch GCYM e overclock perfeito) com as 12 receitas de
   `flotating_beneficiation` 1:1 e a receita do controlador portada (Assembly Line, só GTCEu/GTNA).
   Materiais novos 1:1: Hastelloy-N75, Stellite, etilxantatos de sódio/potássio, turpentina e os 12
   fluidos `*Front`. Gate: 26/26 unit, 76/76 GameTests, `runData written: 0`.
6. **Vacuum Drying Furnace** — ✅ **CONCLUÍDO (G-0108, 2026-09-25)**: `vacuum_drying_furnace`
   (estrutura 3×5×3 do `.mbs`, `RED_STEEL_CASING`, 24 bobinas, Muffler/Maintenance obrigatórias) com
   as 12 receitas de secagem 1:1, a receita `salt_dust` do Dehydrator e a neutralização de `RedMud`
   no Mixer. Overclock de bobina do EBF reproduzido por `GTNAHeatingCoilOverclock` e paralelo
   `2^(temp/900)` no modo Dehydrator. A receita do controlador foi inicialmente **omitida** (quatro
   `DEHYDRATOR[IV]` exclusivos do GTO); o G-0121 portou o Dehydrator IV e restaurou a receita.
   Gate original: 26/26
   unit, 76/76 GameTests, `runData written: 0`. Bloqueio rígido: `NeutralisedRedMud` é terminal até a
   cadeia StoneDust do GTO ser portada.
7. **Extensão grande do Component Assembler** (sub-patterns) — ✅ **CONCLUÍDO (G-0109, 2026-09-26)**:
   as duas camadas de `addSubPattern` do GTOCore (29×6×13 e 29×6×20) registradas como módulos GTNA,
   teto de carcaça IV→LuV com o módulo formado, os cinco blocos novos + `CarbonFiberPolyphenyleneSulfideComposite`
   (e `TitaniumNitrideCeramic`) com texturas GTO (CC BY-NC-SA), as oito receitas de lote LuV e a carcaça
   LuV. Gate: 26/26 unit, 83/83 GameTests, `runData written: 0`. Diferenças documentadas: o teto do GTO
   é UV, os cascos MK2 usam rotas Assembler (Precision Assembler excluído) e a extensão não compartilha
   o contexto de tier com a base.
8. **`component_assembly_line`** (controlador separado) — ✅ **CONCLUÍDO (G-0110, 2026-09-26)**:
   `.mbs` real 47×15×31, família de carcaças `COMPONENT_ASSEMBLY_LINE_CASING_LV..LUV` (texturas GTO,
   CC BY-NC-SA), regra de tier uniforme, portão de receita até LuV, Parallel Hatch e as mesmas 48
   receitas de componentes. Blocos GTO exclusivos substituídos por equivalentes GTNA/GTCEu
   documentados (tabela G-0110); cross-recipe do gtolib não reproduzido; receita do controlador
   omitida e registrada. Gate: 26/26 unit, 90 GameTests aprovados, `runData written: 0`.
   ➕ **G-0114 (2026-09-26):** os dois tetos subiram de LuV para **UV** — carcaças ZPM/UV nas duas
   famílias, regras de tier até UV, lotes ZPM/UV portados 1:1 (`ComponentRecipes.java:45-46`), produção
   das carcaças ZPM/UV com substituições documentadas e GameTests até 64 batches.

## Escopo EXCLUÍDO (e por quê)

- **Precision Assembler** — depende do glass tier e de receitas GTO-pesadas; sem elas a máquina
  tem pouca função.
- **Turbinas mega** (`steam_mega_turbine`, `supercritical_mega_steam_turbine`,
  `rocket_mega_turbine`) — exigem lógica de 4 rotores e glass tier.
- **Cadeia de purificação de água** (9 máquinas) — 18 carcaças + ~15 materiais + sistema wireless de
  unidades; puxa ~10 multiblocos.
- **Advanced Fusion Reactor MK1** (Kuangbiao) — perde renderer e lógica de calor; endgame.

Essas ficam fora desta leva e continuam pendentes/documentadas.

## Decisões travadas

- **Turbinas:** só as **não-mega** (`rocket_large_turbine`, `supercritical_steam_turbine`).
  Comportamento: portar a **`TurbineMachine` do GTO fielmente**, **sem glass tier** (dano fixo).
  Vapor supercrítico: **reusar o do GTNA** (`DenseSupercriticalSteam`/`InsanelySupercriticalSteam`).
- **Component Assembler:** portar **a extensão (sub-patterns)** e também o
  **`component_assembly_line` separado**. Tiers: **até UV** — o corte subiu de LuV para UV em
  **G-0114 (2026-09-26)**, que é o máximo com receitas no GTCEu base. UHV e acima ficam
  documentados fora do escopo.
- **ISA Mill:** **criar o prefixo `MILLED` no GTNA** (item + flag de material + datagen).
  Ball Hatch **sem renderer custom** (funcional).
- **Flotação + Secagem:** **cadeia fechada** — `*Front` da flotação alimenta a secagem, que devolve
  dusts GTCEu + `RedMud`.
- **Receita de controlador:** **omitir e anotar só quando depender de recursos exclusivos do GTO**;
  quando usar só GTCEu/GTNA, portar fielmente.
- **Se travar:** **documentar o bloqueio e seguir** para a próxima máquina do loop.

## Mods externos

- **Ad Astra**, **GTMThings**: o GTNA **já depende**. Nada a fazer.
  - Em turbinas, o GTMThings só formata números (`FormatUtil`) — não é rede wireless.
- **Ars Nouveau**: não usar; trocar a string `ars_nouveau.locked` por texto GTNA.
- **Deeper and Darker**: não usar (só existia no Precision Assembler, que está fora).
- **Farmer's Delight**: já tratado como opcional (`Rich Soil` → `mud`).
- **Não adicionar nenhuma dependência nova.**

## Glass tier

**Não implementar** o sistema de glass tier (`GLASS_TIER`/`GTOPredicates.glass()`/`GLASSMAP`) nem o
`tierBlock`/`CALMAP` genérico. Quando alguma máquina do escopo citar tier, usar apenas o tier da
carcaça de máquina (GTCEu `MACHINE_CASING_*`) ou um valor fixo documentado.

## Política de validação e publicação

- Rodar o gate completo após **cada** máquina:
  `./gradlew spotlessCheck compileJava runUnitTests runGameTestServer runData --offline`.
- Registrar checkpoint no `CONTINUITY_LEDGER.md` (G-0104 em diante) com validação e pendências.
- **Manter tudo local**: sem commit e sem push até o autor testar in-game e autorizar.
- Texturas novas: copiar do GTOCore com atribuição **CC BY-NC-SA** em `GTNASources` e
  `THIRD_PARTY_NOTICES.md`. Modernity-GTNH só quando houver asset correspondente.
- Todo port precisa de `@GameTest` (formação + um comportamento) e entradas de lang `en` + `pt_br`.

## QA automático obrigatório (para minimizar o teste in-game)

O servidor de GameTest é dedicated server: NÃO renderiza tooltip e NÃO resolve lang. Por isso, além
dos GameTests, são obrigatórios contratos por varredura de fonte (estilo GTLCore: `main()` + asserts,
em `src/test/java/com/raishxn/gtna/`).

### Por máquina (GameTest)
1. Formação: monta a estrutura pelo padrão real e confirma que forma.
2. Negativo: troca 1 bloco por inválido (ou remove um hatch obrigatório) e confirma que NÃO forma.
3. Hatches: coloca item/fluido/energia/manutenção nas posições permitidas e confirma formação;
   para máquinas com Parallel Hatch, confirma que a hatch é aceita; se houver posição só de casing,
   confirma que a hatch de fluido é rejeitada ali.
4. Módulo/extensão: confirma que o recipe type extra só fica disponível com a extensão formada
   (Cold Ice atomização, Component Assembler extensão).
5. Execução/upkeep: roda uma receita de verdade, ou valida `beforeWorking`/consumo como Blaze/Cold Ice.
6. Receitas: confirma a contagem do recipe type (`getAllRecipesFor(...).size() == N`).
7. Controlador: confirma receita presente (se portada) ou ausente + documentada (se omitida).

### Contratos globais (novos testes unitários de varredura)
1. `PortLangParityTest`: toda chave `block.gtna.<id>` e `gtna.machine.<id>.tooltip*` referenciada no
   `GTNALangProvider` também existe em `src/main/resources/assets/gtna/lang/pt_br.json`. Falha se
   faltar (pega lang quebrada sem abrir o cliente).
2. `PortChainClosureTest`: todo fluido `*Front` e `RedMud` produzido tem receita consumidora.
3. Estender `ModuleTooltipContractTest` (ou criar `MachineTooltipContractTest`): toda máquina de
   `GTNAMachines3` tem `gtna.machine.<id>.tooltip` + >= 3 `.tooltip.N`.
4. Estender `PartAbilityCoverageTest` para `GTNAMachines3` e para as novas partes (Ball Hatch,
   atomização): toda parte nova é aceita por pelo menos um padrão.
5. `ControllerRecipePolicyTest`: cada máquina nova ou tem receita de controlador nos recursos
   gerados, ou está numa lista explícita de "omitida/documentada".
6. Datagen determinístico: `runData` duas vezes; a segunda precisa terminar com `written: 0`.

### Fica para o teste in-game do autor (curto)
- Aparência do modelo e do tooltip renderizado (cor, quebra de linha, em `en` e `pt_br`).
- JEI: a máquina aparece nas categorias certas e as receitas novas são visíveis.
- Rotação/colocação do controlador e orientação real do padrão no mundo.
- Turbinas sob carga (dano/velocidade) e hatches nas posições que o jogador costuma usar.
