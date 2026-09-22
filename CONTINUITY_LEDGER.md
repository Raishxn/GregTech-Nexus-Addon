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
  `runGameTestServer` (**25/25**, `All 25 required tests passed`) + `runData` determinístico. A
  execução carregou os mixins alterados; os avisos/erros de receitas do GTCEu já conhecidos
  continuam no log.
- **Feature em foco:** o **ME Pattern Buffer multi-modo** (fidelidade ao GTLCore/GTOCore). A tabela
  de fidelidade está **toda verde** e as divergências conscientes estão documentadas no gap doc.
- **Testes hoje:** 14 unit tests (`main()` + asserts, padrão GTLCore) e 25 gametests (`@GameTest`),
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

### G-0040 (2026-09-22) — boiler solar grande não alimentava a rede wireless: buffer/taxa do hatch de saída estrangulavam o ciclo; display/Jade reportavam 20× a produção

- **Reprodução do autor:** `large_steam_solar_boiler` 41×42, **Wireless Steam Output Hatch** no
  boiler não enchia a rede; **Wireless Steam Input Hatch** no chão não enchia; Jade mostrava
  "312 B".
- **Causa raiz #1 (buffer/taxa):** o boiler despeja o **ciclo inteiro de uma vez** no hatch de
  saída (`sunlit * 200` mB a cada 20 ticks = 312.000 mB no campo 41×42). O hatch de bronze tinha
  buffer de **20.000 mB** e cap de **10.000 mB/t**: o `RecipeLogic.onRecipeFinish` (que ignora o
  resultado do `handleRecipeIO(OUT)`) aceitava só 20.000 e **descartava 292.000**, e o hatch ainda
  limitava o push a 10.000/t. Resultado: a rede recebia ~1.000 mB/t de 15.600 mB/t reais (0,3%) —
  o autor leu isso como "não entra vapor".
- **Correção (paridade GTNL `WirelessSteamDynamoHatch`):** o hatch move o **tank inteiro** por tick.
  `bronzeBuffer` 20.000 → **128.000.000** (capacidade do dynamo de bronze do GTNL); `bronzeTransferRate`
  e `steelTransferRate` passam a default **`Integer.MAX_VALUE`** (throttle opcional, documentado no
  config/tooltip/`/gtna steam`). O input hatch enche até o espaço livre/rede. Nada de cap fixo
  minúsculo: o limite real passa a ser o buffer.
- **Causa raiz #2 (display/Jade 20×):** `lastSteamOutput = steamOut * 20` era **20× maior** que a
  produção real (o `steamOut` já é o valor do ciclo de 20 ticks = 1 s). Renomeado para
  `steamPerSecond = steamOut * 20 / TICK_INTERVAL` e o label de `L/s` para `mB/s`. O "312 B" do Jade
  é o **output da receita por craft** (provider stock `RecipeOutputProvider`): está correto para o
  ciclo, mas parecia baixo contra o display errado. Novo `GTNASolarBoilerProvider` (Jade) mostra
  "Sunlit Cells" + "Steam Production" em mB/s e uma linha de idle, alinhado ao display da máquina.
- **Observabilidade:** tooltip dinâmica dos 4 hatches (buffer via config + taxa "unlimited" ou
  valor), label de taxa na GUI do hatch, e `/gtna steam` agora imprime `| <taxa> mB/t` por hatch
  (`unlimited` quando MAX).
- **Arquivos:** `ConfigHolder.java` (defaults + comentários), `WirelessSteamOutputHatch.java`,
  `WirelessSteamInputHatch.java` (`getTransferRate`/`isTransferLimited`/`rateText`, push/pull do
  buffer inteiro), `GTNAMachines.java` (`wirelessSteamTooltip` dinâmica), `GTNACommands.java`
  (taxa por hatch), `LargeSteamSolarBoilerMachine.java` (per-second + getters),
  `GTNASolarBoilerProvider.java` (novo) + `GTNAJadePlugin.java`, `GTNALangProvider.java` +
  `pt_br.json` + `en_us.json` manual/gerado (chaves `gtna.machine.wireless_steam.transfer_rate(.unlimited)`,
  `gtna.machine.large_steam_solar_boiler.idle`, `gtna.command.steam.rate.unlimited`, formato de
  `hatch_entry`), `GTNAMachineGameTests.java` (round trip com 312.000 mB + 4 ciclos sob carga),
  `SteamWiringContractTest.java` (trava buffer/taxa e a taxa por segundo do boiler).
- **Validação:** `spotlessApply compileJava` OK; `spotlessCheck` + `runUnitTests` (**14/14**);
  `runGameTestServer` (**25/25**, `All 25 required tests passed`); `grep -c "Parsing error loading
  recipe gtna:" run/logs/latest.log` = **0**; `runData` determinístico (2ª execução `written: 0`).
- **Pendências abertas / verificação in-game:** (a) confirmar no cliente que o Jade do boiler
  mostra "Steam Production: 312000 mB/s" (dia, campo 41×42) e que o "312 B" do provider stock
  continua logo acima como output por craft; (b) `/gtna steam` deve listar o output hatch e o input
  hatch com `unlimited mB/t`; (c) opcional: dividir os buffers input (GTNL 8M) e output (128M) em
  duas chaves se o hoarding de 128M no input incomodar; (d) `machines.wirelessSteamTransferRate`
  (8192) é config legado morto — remover numa limpeza futura.

### G-0039 (2026-09-22) — tooltip duplicado dos módulos do elevador: descrição saía duas vezes (mainKey do GTCEu + `.tooltips`); descrições fiéis ao GTNL

- **Causa raiz (confirmada no fonte do GTCEu 7.5.3):** `MetaMachineBlock#appendHoverText` chama
  `definition.getTooltipBuilder().accept(...)` (que adiciona o que foi passado em `.tooltips(...)`)
  e depois insere `tooltip.add(1, Component.translatable("<namespace>.machine.<id>.tooltip"))`
  **se a chave existir**. O `registerElevatorModule` passava exatamente a mesma chave
  `gtna.machine.<id>.tooltip` em `.tooltips(...)`, então a linha saía duas vezes. O mesmo caminho
  existe em `MetaMachine#onAddFancyInformationTooltip` (índice 0).
- **Correção:** o `registerElevatorModule` deixou de listar a descrição; a linha única passa a vir
  só da inserção automática do GTCEu (índice 1, logo abaixo do nome). O `.tooltips(...)` agora
  recebe apenas as linhas de **status/upkeep** de cada módulo, terminando em
  `gtceu.part_sharing.disabled` (que continua, é intencional).
- **Tooltips fiéis ao GTNL:** descrição de "o que faz" em `gtna.machine.<id>.tooltip` (auto) +
  linhas de status via chaves compartilhadas `gtna.machine.steam_elevator_module.tooltip.{range,
  upkeep,cycle,water,yield}`. Flight: range 64/upkeep 8192; Weather: upkeep 512; Greenhouse:
  range 16/água 1000/upkeep 8192; Oil Drill I/II/III: yield 250-1000 / 1000-4000 / 3000-12000 L
  por ciclo, ciclo 1200/600/400 ticks, upkeep 128/512/2048; Entity Crusher: range 8/upkeep 512;
  Ore Processor: água 1000/ciclo 20/upkeep 128; Repellent I/II/III: range 64/128/256, upkeep
  512/1024/1536; Beacon I/II/III: range 64/128/256, upkeep 2048/8192/18432; Apiary: água
  1000/ciclo 200/upkeep 16384; Bee Breeding: ciclo 12000/upkeep 32768.
- **Arquivos:** `GTNAMachines2.java` (helpers `range/upkeep/cycle/water/yieldStat` + varargs de
  tooltips no `registerElevatorModule`), `GTNALangProvider.java` (5 chaves novas + descrições),
  `pt_br.json` (5 chaves novas + descrições; BOM + CRLF preservados byte a byte),
  `en_us.json` gerado.
- **Validação:** `spotlessApply compileJava` OK; `spotlessCheck` + `runUnitTests` (**14/14**);
  `runGameTestServer` (**25/25**, `All 25 required tests passed`); `grep -c "Parsing error loading
  recipe gtna:" run/logs/latest.log` = **0**; `runData` determinístico (2ª execução `written: 0`).
- **Pendências abertas / verificação in-game:** conferir no cliente que cada módulo mostra **uma**
  linha de descrição seguida das linhas de status e de "Multiblock Sharing Disabled"; os valores de
  yield do Oil Drill são os do cálculo GTNA (`baseYield * (1..4)` somado `tier-1` vezes), não os
  números do GTNL (o GTNL usa overclock count variável, o GTNA usa tier fixo).

### G-0038 (2026-09-22) — wireless steam input hatch "no steam": pull is now clamped to the network balance + `/gtna steam` inspection command

- **Causa raiz medida (reproduzida em gametest):** `WirelessSteamInputHatch.updateWireless` pedia
  `toPull = min(spaceNeeded, transferRate)` (bronze **10000**, steel **1000000** mB) e só então
  chamava `consumeSteamFromGlobalMap`, que é **tudo-ou-nada**. Se a rede tivesse **menos** que um
  tick da taxa (o caso comum: o hatch de saída empurra aos poucos), o consume era rejeitado
  inteiro e o hatch **nunca puxava nada** — daí o "no steam" tanto no host quanto no módulo. O
  bug do G-0037 (gate de 8192 removido) era real, mas não era o que travava o puxão.
- **Correção (paridade GTNL `tryFetchingSteam`):** o pedido agora é
  `min(spaceNeeded, transferRate, getUserSteam(network))`; como o tanque simulado aceita ≤ pedido ≤
  saldo, o consume atômico sempre passa. Nada mais é voidado nem duplicado (mantém o
  SIMULATE→cobrar→EXECUTE do G-0037). Vale para hatch no **host** e no **módulo** (ambos usam o
  `getOwnerUUID()` do colocador; a rede GTNA é keyed por owner, não por time — o comando abaixo
  mostra o saldo e os hatches para conferir).
- **Inspeção:** novo `/gtna steam` (qualquer jogador) imprime, para o próprio dono, o saldo da rede
  e os wireless hatches conectados (tipo, bronze/aço, dimensão e posição). `/gtna steam add <n>` e
  `/gtna steam set <n>` (op, nível 2) para reproduzir/verificar estados; `/gtna steam <jogador>`
  (op) para a rede de outro jogador. Os hatches agora registram-se em um mapa **transiente** (TTL 40
  ticks, nunca persistido) em `SteamNetworkData` via `SteamWirelessNetworkManager.reportConnection`;
  o comando é a fonte de verdade para "o vapor chegou na rede?".
- **Testes:** `wirelessSteamHatchIsAcceptedAsSteamSource` ganhou o **round trip real** (output hatch
  com 4321 mB → `serverTick()` → rede 4321 → input hatch `serverTick()` → tanque 4321 → rede 0, sem
  perda) — foi ele que reproduziu o bug antes da correção. `SteamWiringContractTest` agora exige
  `getUserSteam` **antes** do `fill(SIMULATE)` no input hatch, travando a regressão.
- **Arquivos:** `WirelessSteamInputHatch.java`, `WirelessSteamOutputHatch.java`,
  `SteamNetworkData.java`, `SteamWirelessNetworkManager.java`, `GTNACommands.java`,
  `GTNALangProvider.java` (12 chaves), `pt_br.json` (12 chaves, BOM/CRLF preservados),
  `GTNAMachineGameTests.java`, `SteamWiringContractTest.java`, `en_us.json` gerado.
- **Validação:** `spotlessApply compileJava` OK; `spotlessCheck` + `runUnitTests` (**14/14**);
  `runGameTestServer` (**25/25**, `All 25 required tests passed`); `grep -c "Parsing error loading
  recipe gtna:" run/logs/latest.log` = **0**; `runData` determinístico (2ª execução `written: 0`).
- **Pendências abertas / verificação in-game:** (a) montar o hatch de saída numa fonte de vapor e o
  de entrada no elevador/módulo, rodar `/gtna steam` e ver o saldo subir e os hatches listados;
  (b) a rede ainda é por **owner** (colocador), não por time — co-op com jogadores diferentes exige
  um item/comando de vínculo (FTB Teams é só `modRuntimeOnly`, não dá para referenciar no main);
  (c) balancear as taxas por hatch (bronze 10000/t, aço 1000000/t) contra os upkeeps dos módulos.

### G-0037 (2026-09-22) — review in-game: rede de vapor sem perda, elevador/módulos sem EU, hatch no módulo e overlay do elevador

- **Prioridade 1 — a rede de vapor wireless estava perdendo/entupindo vapor.** A causa medida era
  um **gate de taxa inconsistente** em `SteamWirelessNetworkManager.consumeSteamFromGlobalMap`:
  rejeitava qualquer valor acima de `machines.wirelessSteamTransferRate` (**8192**), enquanto o
  hatch calculava `toPull` pela sua própria taxa (`bronze = 10000`, `steel = 1000000`). Na prática
  o hatch **de aço nunca puxava** (toPull > 8192 sempre) e o de bronze só puxava perto de encher;
  do ponto de vista do jogador o vapor ia para a rede e "sumia". Além disso, o hatch de entrada
  **cobrava a rede e só depois enchia o tanque**, sem conferir o que o tanque aceitou (mismatch
  simulado-vs-real). Correção no estilo do GTNL (`tryFetchingSteam`):
  - `SteamNetworkData.addSteam` agora é **atômico com sinal** (aceita negativo; rejeita e não mexe
    no saldo se for abaixo de zero) e retorna `boolean`; `addSteamToGlobalSteamMap` propaga isso.
  - `consumeSteamFromGlobalMap`/`extractSteam` perderam o gate de `machines.wirelessSteamTransferRate`
    (a taxa é responsabilidade do hatch, que já limita por `wirelessSteam.*TransferRate`).
  - **Entrada:** `fill(SIMULATE)` → cobra exatamente o `accepted` → `fill(EXECUTE)`.
  - **Saída:** `drain(SIMULATE)` → adiciona exatamente o `amount` drenado → `drain(EXECUTE)`.
  - Gametest `wirelessSteamHatchIsAcceptedAsSteamSource` ganhou um bloco de **contabilidade em
    runtime** (add 1000 → 1000; consume 400 → 600; overdraft rejeitado e saldo intacto; subtract
    atômico até 0; subtract abaixo de 0 rejeitado). `SteamWiringContractTest` ganhou um lint de
    código que exige a ordem SIMULATE→cobrar→EXECUTE nos dois hatches.
- **Prioridade 2 — elevador e módulos sem buffer de EU.** Removido o `energyBuffer`/`MAX_ENERGY` do
  `SteamElevator` e o `storedEnergy`/`receiveEnergy`/`consumeEnergy`/`getEnergyStored`/
  `getEnergyCapacity` do `SteamElevatorModuleMachine`/`ISteamElevatorModule`. O elevador agora é
  **sempre ativo quando formado** (`isWorkingEnabled()`→`true`, `isActive()`→`isFormed()`,
  `isElevatorRunning()`→`isFormed()`); não consome receita/energia. Os módulos pagam um
  **upkeep em mB/t** (`getSteamUpkeep`, antes `getEnergyUsage`, mesmos números do GTNL com 1 mB =
  1 EU) drenado dos **hatches de vapor da estrutura**: primeiro os do próprio módulo, depois os do
  host, sempre checando a disponibilidade dos dois pools **antes** de drenar (nunca paga parcial e
  some com a diferença). Linhas de display "Energy: X / Y EU" e "Buffer: X / Y EU" trocadas por
  "Steam: X mB" e "Upkeep: X mB/t | Own steam: Y mB".
- **Prioridade 3 — hatch de vapor no módulo reportava "não conectado".** O pattern do módulo
  (`steam_elevator_module`) mapeava `A` para **apenas** `blocks(CASING_STEEL_SOLID)`, então um hatch
  colocado no casco do módulo não casava e a estrutura invalidava. `A` agora aceita, como no GTNL
  `SteamElevatorModuleBase#getStructureDefinition`, `PartAbility.STEAM` + `STEAM_IMPORT/EXPORT_ITEMS`
  + `IMPORT/EXPORT_ITEMS` + `IMPORT/EXPORT_FLUIDS` + `MAINTENANCE` (cada um `setMaxGlobalLimited(1)`)
  encadeado com o casing. O módulo coleta o tanque do hatch em `onStructureFormed` (filtro
  `isFluidValid(0, steam)`) e usa no upkeep.
- **Prioridade 4 — overlay do Steam Elevator.** Confirmado de novo: o GTNL renderiza o elevador com
  `gregtech:iconsets/EM_COMPUTER` (`BlockIcons.OVERLAY_FRONT_TECTECH_MULTIBLOCK`), um ícone
  **GT5U/Tectech** referenciado do resource domain do GregTech e **NÃO vendorizado** no repositório
  do GTNL (só há `SteamCarpenter`, `SteamLavaMaker`, `SteamItemVault`, `CactusWonder`,
  `MegaSteamCompressor`, `SteamManufacturer`, etc. — nenhum overlay de elevador). Como não há o que
  portar do GTNL, o ícone foi vendorizado na forma retexturizada do pack **Modernity-GTNH** (fonte
  que o projeto já usa; mesmo autor do GTNL conforme o dono) em
  `assets/gtna/textures/block/multiblock/steam_elevator/{overlay_front,overlay_front_active}.png`
  (+ `.mcmeta` animado) e o `workableCasingModel` passou de `gtceu:.../steam_grinder` para
  `gtna:.../steam_elevator`. `THIRD_PARTY_NOTICES.md` atualizado. **Re-auditoria dos demais:** as
  `large_steam_*` do GTNL não têm textura própria (usam ícones do GT++/GT5U);
  `hyper_pressure_reactor`/`compact_hyper_pressure_reactor` (GTNL `SteamFusionReactor`/
  `HighPressureSteamFusionReactor`) usam `OVERLAY_TOP_STEAM_MACERATOR` do GT5U, também não
  vendorizado — mantidos os overlays GTCEu. Nenhum outro port GTNL ficou com overlay GTCEu tendo um
  do GTNL disponível.
- **Arquivos:** `SteamWirelessNetworkManager.java`, `SteamNetworkData.java`,
  `WirelessSteamInputHatch.java`, `WirelessSteamOutputHatch.java`, `ISteamElevatorModule.java`,
  `SteamElevator.java`, `SteamElevatorModuleMachine.java`, os 10 módulos, `GTNAMachines.java`
  (overlay do elevador), `GTNAMachines2.java` (pattern do módulo), `GTNALangProvider.java`,
  `pt_br.json` (2 linhas trocadas, BOM/CRLF preservados), `SteamWiringContractTest.java`,
  `GTNAMachineGameTests.java`, `THIRD_PARTY_NOTICES.md`, as texturas do overlay e o modelo gerado
  `steam_elevator.json`.
- **Validação:** `spotlessApply compileJava` OK; `spotlessCheck` + `runUnitTests` (**14/14**);
  `runGameTestServer` (**25/25**, `All 25 required tests passed`); `grep -c "Parsing error loading
  recipe gtna:" run/logs/latest.log` = **0**; `runData` determinístico (2ª execução `written: 0`).
- **Pendências abertas / verificação in-game:** (a) montar o elevador 35x43x35 e os 12 módulos no
  `runClient` e confirmar que cada módulo paga upkeep dos hatches (próprio e do host) e que o efeito
  liga/desliga ao conectar/desconectar; (b) confirmar que colocar um hatch de vapor no casco do
  módulo agora forma o módulo e que o host **não** conta esse hatch como fonte própria (as células
  do módulo caem em `I`/`H`/`D`; se cair em `H` pode competir com o limite de 1 fonte de vapor do
  host — validar); (c) conferir visualmente o overlay animado do elevador; (d) o
  `machines.wirelessSteamTransferRate` (8192) ficou **sem uso** (a taxa por hatch é a que vale) —
  remover numa limpeza futura se o autor concordar; (e) os upkeeps em mB/t são os números do GTNL
  (1 mB = 1 EU) e podem precisar de balanceamento in-game.

### G-0036 (2026-09-22) — review in-game: Building Gadgets, orientação do módulo, casing do elevador e overlays GTNL

- **Item 1 — Building Gadgets não carregava (causa raiz):** a dependência
  `modRuntimeOnly("curse.maven:building-gadgets-298187:6850515")` aponta para o arquivo **6850515 =
  buildinggadgets2-1.3.9**, que é **NeoForge 1.21** (`META-INF/neoforge.mods.toml`, `neoforge`
  `[21.0,)`, `minecraft [1.21,1.22)`), então o Forge 1.20.1 ignorava o jar silenciosamente. Trocado
  para **6850495 = buildinggadgets2-1.0.8** (1.20.1 **Forge**; `META-INF/mods.toml` com
  `loaderVersion="[43,)"`, `modId="forge"`, `minecraft [1.19.2,)`). Verificado: o jar é baixado e o
  mod **aparece carregado** no log do `runGameTestServer` (registros `buildinggadgets2:*` e
  `run/config/buildinggadgets2-common.toml`).
- **Item 2 — orientação do módulo do Steam Elevator:** o `pattern/steam_elevator_module.mbs` (1x5x2)
  tinha sido gerado **sem** a convenção do §5 do `NEXT-SESSION-HANDOFF` (inverter linhas **e**
  aisles), então o controller ficava na **2ª linha de baixo** (aisle 0) e o corpo **à frente** dele
  (a face "para dentro"). Regenerado com a convenção: aisle 0 = corpo (`A`), aisle 1 = controller
  (`~`) na **linha 4** (2ª do topo). No frame do GTCEu isso põe o controller na 2ª linha a partir do
  topo e o corpo **atrás** dele (a face aponta **para fora**); a faixa vertical (linhas 0..4 do host)
  continua casando com as células `H`/`I` do `steam_elevator.mbs`. Binário gerado pela tabela do
  `GTNAMultiBlockFileReader` (`A`=2, `~`=61). Não foi mexido no `rotationState` (NON_Y_AXIS +
  `allowExtendedFacing(false)`/`allowFlip(false)` continuam corretos).
- **Item 3 (já feito, preservado):** o `SteamElevator.onStructureFormed` não invalida mais quando não
  há tanque de vapor ("o elevador não precisa de vapor/energia; só os módulos consomem"). O
  `SteamElevator.java` já estava modificado e entrou neste commit.
- **Item 4 — casing do elevador:** `appearanceBlock` e o casing base do `workableCasingModel` do
  `steam_elevator` passaram de `GTNABlocks.STEEL_REINFORCED_WOOD` para **`GTBlocks.CASING_STEEL_SOLID`**
  (GTNL `SteamElevator#getCasingTextureID()` = `SolidSteelMachineCasing`; o **shell `A` do pattern
  continua** steel-reinforced-wood, que é o elemento `A` do GTNL).
- **Item 5 — overlays dos ports GTNL:** portadas para `assets/gtna/textures/block/multiblock/<id>/`
  as texturas de overlay **próprias do GTNL** (`textures/blocks/iconsets/*`) que faltavam, com
  `overlay_front`/`overlay_front_active` e os `_GLOW` do GTNL mapeados para `_emissive` do GTCEu:
  `steam_lava_maker` (SteamLavaMaker), `steam_item_vault` (SteamItemVault), `steam_cactus_wonder`
  (CactusWonder) e `steam_mega_compressor` (MegaSteamCompressor). Os `.workableCasingModel` dessas
  quatro máquinas agora apontam para `GTNACORE.id("block/multiblock/<id>")`. Corrigido também o
  typo `overlay_front_activce.png` → `overlay_front_active.png` (+ `.mcmeta`) do
  `largesteamfurnace` (o overlay ativo nunca carregava). **Limite encontrado:** os `large_steam_*`
  do GTNL **não** têm textura própria no repositório do GTNL — usam ícones do **GT++**
  (`TexturesGtBlock.oMCDIndustrial*`) ou do GT5U (`Textures.BlockIcons.OVERLAY_*`), que não estão
  em `GTNL/src/main/resources`; os `steam_*` com iconset próprio já estavam portados
  (`steammanufacturer`, `steamwoodcutter`/SteamCarpenter, `steaminfernalcokeoven`) ou foram agora.
  O `steam_elevator` usa `gregtech:iconsets/EM_COMPUTER` (GT5U/Tectech), também não vendorizado.
  Atribuição GTNL atualizada em `THIRD_PARTY_NOTICES.md`.
- **Arquivos:** `build.gradle`, `GTNAMachines.java`, `pattern/steam_elevator_module.mbs`,
  `steam_elevator_module` (binário), as 4 pastas novas de overlay, o rename do
  `largesteamfurnace`, `THIRD_PARTY_NOTICES.md`, `SteamElevator.java` (item 3), os 5 modelos
  gerados em `src/generated/resources/assets/gtna/models/block/machine/`.
- **Validação:** `spotlessApply compileJava` OK; `spotlessCheck` + `runUnitTests` (**14/14**);
  `runGameTestServer` (**25/25**, `All 25 required tests passed`) e
  `grep -c "Parsing error loading recipe gtna:"` = **0**; `runData` determinístico (2ª execução
  `written: 0`).
- **Pendências abertas / verificação in-game:** (a) confirmar que os 12 módulos formam com o
  controller na 2ª linha do topo e o corpo para dentro (o jogador precisa facear o módulo para fora;
  a face do módulo é perpendicular à do host nos slots laterais); (b) conferir visualmente se
  `large_steam_*` devem manter os overlays GTCEu/GTO ou se o autor quer os ícones do GT++ (precisam
  ser fornecidos); (c) o overlay do `steam_elevator` continua `gtceu:block/multiblock/steam_grinder`
  (o ícone Tectech do GTNL não está no repositório do GTNL).

### G-0035 (2026-09-22) — Steam Elevator: módulos viram multiblocos próprios (corrige a contagem de módulos)

- **Bug reportado in-game:** o Steam Elevator contava **qualquer** bloco/peça nas células de módulo
  como módulo (ex.: colocar uma steam hatch em cima de um módulo incrementava o contador). O sistema
  era composto de **part machines** (`SteamElevatorModulePartMachine`) marcadas com a ability
  `GTNAPartAbility.STEAM_ELEVATOR_MODULE`, e o host simplesmente somava `getParts()` que
  implementassem `ISteamElevatorModule` — ou seja, um bloco solto no slot bastava.
- **Correção (desenho GTNL/GTLAdditions):** cada módulo agora é um **multiblock `1x5x2` próprio**
  (estrutura `pattern/steam_elevator_module.mbs`, decodificada com o `GTNAMultiBlockFileReader`
  existente). O host escaneia um conjunto **fixo** de 12 slots e conecta **apenas** o controller de
  módulo **formado** que estiver lá; um bloco/peça solto nunca é contado.
- **Base nova `SteamElevatorModuleMachine`** (`extends WorkableMultiblockMachine`, `implements
  ISteamElevatorModule, IDisplayUIMachine`): mantém o buffer `640000 * (1 << tier)`, o
  `receiveEnergy`/`consumeEnergy` e o `onElevatorTick` dos antigos part machines; troca
  `addedToController/removedFromController` por `connectToHost/disconnectFromHost`. RecipeLogic
  inerte (o host dirige os efeitos). `createUI` monta o painel do `IDisplayUIMachine` + o widget do
  módulo (`createModuleUIWidget`, ex-`createUIWidget`).
- **Host (`SteamElevator`):** novo `getModuleScanPositions()` com 12 offsets `{up, left, forward}`
  **relativos ao controller**, decodificados das células `I` do `steam_elevator.mbs`
  (`{0,-8,-5}..{0,8,-1}`); `RelativeDirection.offsetPos(pos, front, upwards, flipped, ...)` aplica
  facing/flip. `scanModules()` (no `onStructureFormed` e a cada 20 ticks) reconstrói o set a partir
  dos slots: só `SteamElevatorModuleMachine` com `isFormed()` entra; os demais são desconectados.
  Host e módulos usam `allowExtendedFacing(false)` + `allowFlip(false)` para o `offsetPos` ser
  exato (o `I` do padrão virou `any()`; a ability `STEAM_ELEVATOR_MODULE` foi **removida** do
  `GTNAPartAbility`).
- **Encaixe do módulo no host:** em coordenadas locais do padrão, o módulo ocupa `j=I.j-1..I.j+3`
  (5 de altura) e `i=I.i..I.i+1` (2 de profundidade), com controller em `j=1, i=0` (como o GTNL);
  um script verificou que **os 12 slots** aceitam essa caixa — as células caem em `H`/`D`
  (solid steel machine casing, que é o casing do módulo) ou em ` ` (any). O jogador deve orientar o
  módulo com o **mesmo facing** do host.
- **Lang:** 4 chaves novas (`gtna.machine.steam_elevator_module.{tier,energy,connected,disconnected}`)
  no `GTNALangProvider` (en_us gerado) + `pt_br.json` (inserção **byte-preserving**: BOM e
  CRLF/LF preservados, 4 linhas adicionadas). Tooltips/recipes existentes mantidos (os registry ids
  não mudaram).
- **Arquivos:** `SteamElevatorModuleMachine.java` (novo), `SteamElevatorModulePartMachine.java`
  (removido), `ISteamElevatorModule.java`, `SteamElevator.java`, os 10 módulos, `GTNAMachines.java`
  (padrão `I`→`any()`, allowFlip/extended), `GTNAMachines2.java` (registro `multiblock`),
  `GTNAPartAbility.java`, `GTNALangProvider.java`, `pt_br.json`, `pattern/steam_elevator_module.mbs`
  (novo) e os recursos gerados dos módulos.
- **Validação:** `spotlessApply compileJava` OK; `spotlessCheck` + `runUnitTests` (**14/14**);
  `runGameTestServer` (**25/25**, `All 25 required tests passed`); `grep -c "Parsing error loading
  recipe gtna:" run/logs/latest.log` = **0**; `runData` determinístico (2ª execução `written: 0`).
- **Pendências abertas / verificação in-game:** (a) os gates **não** montam o elevador 35x43x35 —
  validar no `runClient` que os 12 módulos formam nos slots, que um bloco solto **não** conta e que
  um módulo formado é conectado/carregado; (b) a orientação do módulo precisa coincidir com o facing
  do host (documentado; se o jogador errar, o módulo não forma e não é contado); (c) a base antiga
  era part machine — saves antigos com módulos-part podem virar blocos órfãos, sem migração.

### G-0034 (2026-09-22) — padronização das tooltips steam (Machine Type + nome rainbow + separador)

- **Contexto:** passo 5 do `NEXT-SESSION-HANDOFF.md` (§1): padronizar as tooltips das máquinas steam no
  formato do GTNL (`nome rainbow` → `Machine Type: <receita>` → stats → high pressure → separador →
  `Source:`). O handoff já registrava que o tooltip builder **não** controla a linha do nome do item.
- **Centralizado no `GTNASteamTooltips`:** agora cobre **toda** máquina GTNA com registry path
  `large_steam_*` ou `steam_*` (inclui os módulos do Steam Elevator, `steam_manufacturer/cobbler/
  woodcutter/lava_maker/item_vault/cactus_wonder/cracking/mega_compressor/elevator`). O wrapper monta a
  lista numa ordem fixa: (1) linha do nome rainbow, (2) `Machine Type` (se houver recipe type real),
  (3) linhas originais (desc/speed/efficiency/parallel/structure + `GTNA_ADD`), (4) linha high pressure
  (só as máquinas do set `HIGH_PRESSURE`, inalterado), (5) separador, e o `GTNASources` (chamado depois)
  fecha com `Source:`. Ordem final: **stats → high pressure → separador → Source**.
- **Nome rainbow — abordagem usada (fallback documentado):** `MachineDefinition` e `MetaMachineItem`
  **não** expõem hook para estilizar o nome do item (a linha do nome é renderizada pela vanilla fora do
  builder). Então a linha do nome é adicionada como **primeira linha da tooltip** com
  `Component.translatable(definition.getDescriptionId()).withStyle(TooltipHelper.RAINBOW_HSL_SLOW)`,
  mesmo padrão do GTCEu (`GTMultiMachines`/`GTMachines`). Fica **duplicada** com a linha de nome
  vanilla (não tem como estilizar a original). O estilo é aplicado **só no client**
  (`FMLEnvironment.dist.isClient()`); a linha (sem cor) continua sendo produzida no servidor, então o
  gametest dedicado `everyGtnaMachineTooltipBuilds` continua exercitando o builder.
- **`Machine Type`:** usa a **primeira recipe type não-DUMMY** (`getRecipeTypes()[0]`) e o lang key da
  categoria (`recipeType.registryName.toLanguageKey()`, ex. `gtceu.macerator`, `gtceu.cracker`,
  `gtna.hydraulic_manufacturing`), dentro de `gtna.tooltip.machine_type` = `"Machine Type: %s"`.
  Máquinas sem recipe type real (`steam_elevator`, `large_steam_storage_tank`,
  `large_steam_solar_boiler`, `steam_item_vault`, módulos) ficam **sem** essa linha.
- **Separador:** `"\u2500".repeat(30)` em `DARK_GRAY`, adicionado **apenas** quando o
  `GTNASources.hasSource(path)` é verdadeiro (novo helper), para nunca terminar em separador órfão.
- **Atribuição:** `GTNASources` ganhou `hasSource(String)` e recebeu as máquinas GTNL que faltavam:
  `large_steam_bending/extruder/sifter/wiremill`, `steam_lava_maker`, `steam_item_vault`,
  `steam_cactus_wonder`, `steam_cracking`, `steam_mega_compressor` (todas confirmadas GTNL nos
  G-0022/G-0023/G-0024/G-0031). Assim todas as `large_steam_*`/`steam_*` têm `Source:`.
- **Lang:** `gtna.tooltip.machine_type` no `GTNALangProvider` (en_us gerado) + `pt_br.json`
  (inserção **byte-preserving**: BOM, CRLF/LF preservados, 3 linhas adicionadas, nada mais mudou).
  Também adicionados os nomes de recipe type que faltavam: `gtna.lava_maker` ("Lava Maker") e
  `gtna.cactus_wonder` ("Cactus Wonder") — usados como lang key de categoria/JEI.
- **Validação:** `spotlessApply compileJava` OK; `spotlessCheck` + `runUnitTests` (**14/14**);
  `runGameTestServer` (**25/25**, `All 25 required tests passed`); `grep -c "Parsing error loading
  recipe gtna:" run/logs/latest.log` = **0**; `runData` determinístico (segunda execução
  `written: 0`). Nenhum gametest novo (o gate espera 25).
- **Pendências abertas / verificação in-game:** (a) a tooltip é **client-side** e não é vista pelos
  gates — validar no `runClient` a animação rainbow, o `Machine Type`, o separador e a ordem
  stats→high pressure→separador→Source; (b) o nome aparece **duas vezes** (nome vanilla + linha
  rainbow) porque não há hook para estilizar a linha do nome — aceito como fallback; (c) nos módulos
  do Steam Elevator o `MetaMachineBlock` insere o `mainKey` (`gtna.machine.<id>.tooltip`) no índice 1,
  então a linha rainbow fica abaixo dele (comportamento pré-existente do duplo `.tooltips(...)`).

### G-0033 (2026-09-22) — port dos módulos Steam Apiary + Bee Breeding (Productive Bees indisponível)

- **Implementado:** `SteamApiaryModule` (tier 6) e `SteamBeeBreedingModule` (tier 8), os dois módulos
  que faltavam do Steam Elevator do GTNL. Ambos são part machines (`SteamElevatorModulePartMachine` +
  `ISteamElevatorModule`) com a ability `GTNAPartAbility.STEAM_ELEVATOR_MODULE`, buffer de vapor/EU e
  upkeep por tick, exatamente como os 12 módulos do G-0032.
- **Productive Bees NÃO está no classpath de dev:** não há dependência no `build.gradle` nem no
  `gradle.properties`, e não há menção a Productive Bees/Forestry no projeto. Portanto, conforme a
  instrução, os módulos foram implementados como **aproximações GTNA-native** (sem hard dependency e
  sem referenciar API externa):
  - **Apiary:** abriga a colônia e consome 1 favo de mel (`minecraft:honeycomb`) + 1000 mB de água a
    cada 200 ticks, produzindo 2 favos + 1 garrafa de mel (`minecraft:honey_bottle`). Upkeep
    `V[4] * 8` (equivale ao `V[4] * mMaxSlots` do GTNL na colônia base de 8 abelhas).
  - **Bee Breeding:** consome 2 favos (parentais) + 8 garrafas de mel (substituto do royal jelly) a
    cada 12000 ticks (o `mMaxProgresstime` do GTNL), produzindo 1 ovo de abelha
    (`minecraft:bee_spawn_egg`, análogo nativo da princess ignoble). Upkeep `V[6]` (exato do GTNL).
  - A inserção de saída faz *dry-run* sobre uma cópia dos slots, para nunca duplicar em insert parcial.
- **Registro/receitas/lang/config/source:** ids `steam_elevator_apiary_module` e
  `steam_elevator_bee_breeding_module`; receitas `HYDRAULIC_MANUFACTURING` mapeadas dos
  `AssemblerRecipes` do GTNL (alveary/royal jelly/beeswax/pollen → favo/mel/água); lang en_us gerado +
  `pt_br.json` (inserção byte-preserving, BOM/CRLF preservados); toggles `steamApiaryModule` e
  `steamBeeBreedingModule` (além do mestre `steamElevatorModules`, que também os gateia); atribuição
  `Source: GTNL` em `GTNASources`. GUI mínima (slots + tanque de água no Apiary).
- **Validação:** `spotlessApply compileJava` OK; `spotlessCheck` + `runUnitTests` (**14/14**);
  `runGameTestServer` (**25/25**, `All 25 required tests passed`); `grep -c "Parsing error loading
  recipe gtna:"` = 0; `runData` determinístico (written: 0). Sem gametest novo (o gate espera 25).
- **Pendências abertas:** se Productive Bees virar dependência opcional no futuro, trocar a
  aproximação nativa pela API real (bee cage/hive) mantendo o check de mod carregado.

### G-0032 (2026-09-22) — port do Steam Elevator + 8 módulos do GTNL (sistema modular)

- **Implementado:** o `SteamElevator` (35x43x35) e os 8 módulos pedidos (`SteamFlightModule`,
  `SteamWeatherModule`, `SteamGreenhouseModule`, `SteamOilDrillModule`, `SteamEntityCrusherModule`,
  `SteamOreProcessorModule`, `SteamMonsterRepellentModule`, `SteamBeaconModule`). Apiary/BeeBreeding
  **não** foram tocados (outro agente).
- **Arquitetura (GTNA-native):** o GTNL tem módulos que são multiblocos e também hatches
  (`mModuleHatches`). No GTNA os módulos viraram **part machines** (`SteamElevatorModulePartMachine`
  + `ISteamElevatorModule`) que declaram a ability `GTNAPartAbility.STEAM_ELEVATOR_MODULE`; o
  controlador é uma `WorkableMultiblockMachine` com recipe logic inerte (`DUMMY_RECIPES` + `InertRecipeLogic`)
  que drena vapor dos hatches STEAM a 1 mB = 1 EU para um buffer de 256 M EU e o distribui igualmente
  entre os módulos; cada módulo paga o upkeep e aplica o efeito. 12 slots de módulo na estrutura.
- **Estrutura:** `pattern/steam_elevator.mbs` no formato do `GTNAMultiBlockFileReader` (mesmo reader
  do ME Hypercore), decodificado do `steam_elevator.mbs` "MBS1" do GTNL via o script
  `gtna_aisles` (o mesmo que gerou as `large_steam_*`). `~` em aisle 20/row 3; letras A–J mapeadas
  para `STEEL_REINFORCED_WOOD`/`STEAM_COMPACT_PIPE_CASING`/`CASING_BRONZE_BRICKS`/`CASING_STEEL_SOLID`/
  `FIREBOX_STEEL`/frame de aço/`Blocks.BRICKS`/`Blocks.STONE_BRICKS`; H = hatches (steam/item/fluido/
  maintenance), I = slots de módulo.
- **Teleporte:** `SteamElevatorTeleport` (botão na UI do controlador) sobe o jogador acima da
  estrutura e, agachado, cicla entre dimensões cujo namespace é `ad_astra` **descobertas em runtime
  pela registry** (sem importar classe do Ad Astra). Desvio: o GTNL abria a seleção celestial do
  Galacticraft; o GTNA usa o registry de dimensões (o classpath de dev tem Ad Astra, mas nada é
  hard-coded).
- **Desvios conscientes (documentados em código):** (a) os módulos não são multiblocos 1x5x2 (o
  `steam_elevator_module.mbs` do GTNL não é usado) — são parts; (b) `wirelessMode` do GTNL (rede de
  vapor wireless) não foi portado: a estrutura exige um hatch STEAM (o `WirelessSteamInputHatch`
  serve); (c) Flight concede `mayfly` (Blood Magic não existe) e revoga ao parar; (d) Weather virou
  modo limpo/chuva/trovão sem os itens Natura/Thaumcraft; (e) Greenhouse acelera plantações
  (CropsNH não existe) com água; (f) OilDrill usa os bedrock fluid veins do GTCEu (o GTNL usa o
  underground oil do GT); (g) EntityCrusher virou moedor de monstros (drop normal); (h) OreProcessor
  faz uma etapa de maceração via registry de materiais do GTCEu (a cadeia de 7 etapas do GTNL não é
  reproduzível numa part sem recipe logic de controlador); (i) Beacon tem efeitos fixos por tier em
  vez da janela de configuração (Warp Ward/Feather Feet/Vis Regen não existem no 1.20.1);
  (j) MonsterRepellent remove monstros no raio (sem hook global de spawn).
- **Registro/receitas/lang/config:** 1 controlador + 14 parts de módulo (I/II/III de Beacon,
  Repellent e OilDrill); receitas `HYDRAULIC_MANUFACTURING` (Steam Manufacturer) mapeadas dos
  `AssemblerRecipes` do GTNL para itens GTNA; `block.gtna.*`/tooltips/config no `GTNALangProvider`
  (en_us gerado) + `pt_br.json` (inserção byte-preserving ancorada numa linha existente, BOM e
  CRLF/LF preservados); toggles `steamElevator`/`steamElevatorModules`; atribuição `Source: GTNL`
  em `GTNASources`.
- **Validação:** `spotlessCheck` + `compileJava` + `runUnitTests` (14/14, incluindo o
  `PartAbilityCoverageTest` que passou a cobrir `STEAM_ELEVATOR_MODULE`) + `runGameTestServer`
  (**25/25**, `All 25 required tests passed`) + `grep -c "Parsing error loading recipe gtna:"` = 0 +
  `runData` determinístico (written: 0). **Sem gametest novo** (o gate espera 25).
- **Pendências abertas:** validar in-game a formação/orientação da estrutura 35x43x35 (só o
  carregamento do pattern foi exercitado); conferir o balanço dos upkeeps EU dos módulos; a UI dos
  módulos é mínima (labels/slots/tanques) — a janela de configuração do Beacon e a cadeia completa
  do OreProcessor ficaram simplificadas.

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

### G-0030 (2026-09-21) — Review in-game: blocos exatos nas estruturas + receitas alinhadas ao GTNL

- **Blocos (feedback do autor):** o casing industrial não estava onde o GTNL usa, faltavam frames e o
  item vault não usava os casings próprios. Corrigido:
  - Predicados separados `industrialCasing()` (Industrial/Advanced Industrial Steam Casing) e
    `machineCasing()` (bronze bricks / steel solid); `casing()` virou a união (não usada nos patterns).
  - As 21 `large_steam_*` + as 6 novas usam as letras exatas do GTNL: industrial nos slots certos,
    machine casing nos slots certos, gearbox/pipe/firebox/frame onde é pra ser.
  - `steam_item_vault`: HyperPressureBreelCasing + VibrationSafeCasing + steel frame + glass.
  - `steam_lava_maker`: StronzeWrappedCasing + glass + lava.
- **Receitas:** reescritas a partir das originais do GTNL (`CraftingTableRecipes.java` +
  `AssemblerRecipes.java`), mapeando GT++/GTNH → GTCEu/GTNA:
  - `Hull_Bronze` → `BRONZE_HULL`; `ReinforcedGlass` → `CASING_TEMPERED_GLASS`;
    `Machine_Bronze_*` → `STEAM_*.first()`; `Controller_Steam*Multi` → `STEAM_GRINDER` /
    `STEAM_HAMMER` / `STEAM_COMPRESSOR` / singleblock LV; `PrecisionSteamMechanism` →
    `PRECISION_STEAM_COMPONENT`; `Hydraulic*` / `Stronze` / `Breel` → itens GTNA.
  - Lava maker virou receita `HYDRAULIC_MANUFACTURING` (paridade com o SteamManufacturer do GTNL).
- **Pendência (confirmar com o autor):** itens GTNL sem equivalente exato usam substitutos GTNA —
  `Super_Chest_LV`, `CompressedSteamTurbine`, `Cover_Screen`, `plateQuintuple` (GTCEu só tem
  `plateDouble`).
- **Validação:** `spotlessCheck` + `runUnitTests` (14/14) + `runGameTestServer` (25/25) + `runData`
  (written: 0); zero `Parsing error loading recipe gtna:` no log.

### G-0029 (2026-09-21) — 3 receitas GTNA com ingrediente vazio (parse error) corrigidas

- **Achado ao rodar o gametest e grepar `Parsing error loading recipe gtna:` no log** (o gate **não**
  falha por isso — as receitas ficam simplesmente sem craft):
  - `large_steam_bath`: usava `TagPrefix.foil, GTMaterials.Steel` (**Steel não tem
    `GENERATE_FOIL`**) e `TagPrefix.rotor, GTMaterials.Aluminium` (**Aluminium não tem
    `GENERATE_ROTOR`**) → ingrediente vazio. Corrigido para placa de aço + rotor de aço.
  - `thread_hatch_zpm` / `thread_hatch_uv`: usavam `TagPrefix.cableGtQuadruple` de
    `UraniumRhodiumDinaquadide` / `EnrichedNaquadahTriniumEuropiumDuranide`, prefixo que **não é
    gerado** para esses materiais (o GTCEu usa `wireGtDouble`) → corrigido para `wireGtDouble`.
- **Lição:** o `runGameTestServer` sai 0 mesmo com receitas quebradas; vale **grepar o log** por
  `Parsing error loading recipe gtna:` ao fechar o gate.
- **Validação:** `spotlessCheck` + `runUnitTests` (14/14) + `runGameTestServer` (25/25) + `runData`
  (written: 0); **zero** erros de parse de receita GTNA no log.

### G-0028 (2026-09-21) — Re-port das 21 estruturas `large_steam_*` a partir dos `.mbs` do GTNL

- **Forma exata:** as 21 `large_steam_*` agora usam a forma do GTNL decodificada dos `.mbs`
  (`StructureFileCodec`/MBS1) com a convenção do §5 (inverter linhas e aisles). Verificação
  programática: 15 derivadas do GTNL + 6 já portadas, **todas OK** contra o `.mbs`.
- **Predicados cientes de tier** (todos gravam o **menor** tier no match context, semântica do
  `checkMachineTier` do GTNL): `casing()` (bricks/solid + industrial bronze/steel),
  `gearboxCasing()`, `pipeCasing()`, `fireboxCasing()`, `frameCasing()` (`frameGt` bronze/steel).
  Uma estrutura toda em aço → high pressure; qualquer peça de bronze → normal.
- **Mapeamento** (§6): industrial/machine casing → `casing()`; gear → `gearboxCasing()`; pipe →
  `pipeCasing()`; firebox → `fireboxCasing()`; frame (`sBlockFrames` / `metaBlockColumn` 4/5) →
  `frameCasing()`; material block → `iron_block`; glass → `Blocks.GLASS`; `SteamAssemblyCasing` →
  `STEAM_ASSEMBLY_BLOCK`.
- **Substituídas (10 divergentes):** alloy_smelter, centrifuge, thermal_centrifuge, circuit_assembler,
  crusher, forming_press, furnace, mixer, ore_washer, chemical_bath (id GTNA `large_steam_bath`).
  **Atualizadas (5 de forma correta, predicados antigos):** compressor, cutting, extractor, hammer,
  lathe.
- **Tooltips:** `GTNASteamTooltips` agora lista as 21 — todas recebem a linha de high pressure.
- **Achado (pré-existente):** a receita `gtna:large_steam_bath` falha no parse
  (`Item array cannot be empty`) — a máquina fica sem craft. Não é deste re-port; investigar depois
  (provável `ChemicalHelper.get` de prefixo/material inexistente).
- **Pendência:** validação **in-game** das estruturas re-portadas — os gametests só montam o
  alloy smelter; as demais foram validadas por comparação com o `.mbs`, não por formação real.
- **Validação:** `spotlessCheck` + `runUnitTests` (14/14) + `runGameTestServer` (25/25) + `runData`
  (written: 0).

### G-0027 (2026-09-21) — Blocos: Industrial / Advanced Industrial Steam Casing (texturas do Modernity)

- **Criados** `industrial_steam_casing` e `advanced_industrial_steam_casing` — porta dos
  `GTNLCasings.IndustrialSteamCasing` / `AdvancedIndustrialSteamCasing` (`metaCasing02` 1/2), o
  **shell** dos large steam multiblocks. Texturas do pack **Modernity-GTNH** (`MetaCasing02/1.png`,
  `2.png`, 16×16; mesmo autor do GTNL, conforme o autor do projeto). Sem CTM (o `_ctm` do Modernity é
  16×16 e não casa com o layout LDLib).
- **Receitas (paridade GTNL):** crafting `AAA/ACA/AAA` (8 placas de latão/ferro + `frameGt` de
  bronze/aço) e SteamManufacturer (`HYDRAULIC_MANUFACTURING`): 6 placas + frame + circuito(1),
  2 s @ 16 EU/t.
- **Tier:** o predicado `SteamMultiMachineBase.casing()` agora reconhece os dois casings como tier 1
  (bronze) e 2 (aço), então as estruturas re-portadas com eles entram em high pressure sem código
  novo. O `steamCasing()` das 6 máquinas novas aceita os quatro casings.
- **Cobertura:** gametest `steamCasingTiers` (25º) trava a tabela de tiers; o
  `steelCasingEnablesHighPressure` agora usa o casing avançado.
- **Decisão de escopo:** os frames/columns do GTNL (`metaBlockColumn` 4/5 = Bronze/SteelMachineFrame)
  mapeiam para `frameGt(Bronze/Steel)` no re-port (§6 do handoff), então **não** viram blocos novos.
- **Atribuição:** linha do Modernity no `THIRD_PARTY_NOTICES.md` atualizada.
- **Validação:** `spotlessCheck` + `runUnitTests` (14/14) + `runGameTestServer` (25/25) + `runData`
  (written: 0).

### G-0031 (2026-09-22) — port fiel de 3 multiblocks steam do GTNL (Cactus Wonder / Steam Cracking / Mega Steam Compressor)

- **Implementado:** três máquinas do GTNL portadas seguindo as convenções do repo
  (`registerMachine`, pattern helpers próprios, config toggle, lang en_us + pt_br byte-preserving):
  - `steam_cactus_wonder` 9x11x9 (`SteamCactusWonder`);
  - `steam_cracking` 7x4x4 (`SteamCracking`, do GTNL `large_steam_cracking`);
  - `steam_mega_compressor` 35x33x35 (`MegaSteamCompressor`).
  As três estruturas foram decodificadas dos `.mbs` "MBS1" **próprios** de cada máquina (não reusa
  nenhum shape existente); o `.mb` de mesmo nome já presente em `assets/gtna/multiblock/` confere
  com o dump do GTNL.
- **SteamCactusWonder — divergência consciente (documentada em código):** o GTNL usa um recipe map
  *fake* (`CactusWonderFakeRecipes`) só para JEI + um acumulador de combustível no `onPostTick` que
  devolve o valor como vapor. O GTNA **não tem** os itens de carvão/coque de cacto do GT++ (nem o
  `InfernalCokeRecipes` do GTNL foi portado com eles), então o port promove o mapa fake a um recipe
  type real (`GTNARecipeType.CACTUS_WONDER_RECIPES`) e mapeia para os combustíveis de carbono mais
  próximos: `CHARCOAL`/`COAL`/`COAL_BLOCK` → Steam, gema de `Coke` → SuperHeatedSteam, bloco de
  `Coke` → DenseSupercriticalSteam, 20 t cada (mesma cadência do fake). A máquina é uma
  `WorkableMultiblockMachine` (não `SteamMultiMachineBase`): ela **gera** vapor, e a base de vapor
  do GTNA chama `onStructureInvalid()` se não achar fonte `IO.IN` de steam — o GTNL também não
  exige hatch de steam nessa máquina.
- **SteamCracking:** usa `GTCEu.CRACKING_RECIPES` (mesmas receitas do `SteamCrackerRecipes` do
  GTNL). O paralelo bronze/steel do GTNL (8/16) sai de `isHighPressure()`; o bônus de ×2 velocidade
  do high pressure cobre o `getDurationModifier()/tierMachine` do GTNL. Predicados de casing
  tiered (`machineCasing`/`fireboxCasing`) preservam o tier.
- **MegaSteamCompressor:** 256 paralelos (`ModifierFunction` estático) + duração ×0.5 (convenção das
  demais large steam do GTNA); a receita do Steam Manufacturer usa 64 `LARGE_STEAM_COMPRESSOR` +
  4 hydraulic pumps (2400 t @ 1600 EU/t), espelhando o GTNL.
- **Receitas de crafting:** `SteamCracking` (Stronze pipeHuge + hydraulic pump + precision mechanism
  + bronze hull) e `SteamCactusWonder` (cactus + bronze plated bricks + hydraulic regulator),
  mapeadas dos `CraftingTableRecipes` do GTNL.
- **Config:** toggles `steamCactusWonder`/`steamCracking`/`megaSteamCompressor` (`@Configurable` +
  case no switch + lang `config.gtna.option.*`), sem quebrar o `ConfigLangKeysTest`.
- **Validação:** `spotlessCheck` + `compileJava` + `runUnitTests` (14/14) + `runGameTestServer`
  (25/25, `All 25 required tests passed`) + `grep -c "Parsing error loading recipe gtna:"` = 0 +
  `runData` (written: 0).

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
