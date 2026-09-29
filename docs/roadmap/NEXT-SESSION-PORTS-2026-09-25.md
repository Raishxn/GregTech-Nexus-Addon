# Handoff — ports de multiblocos até LuV (2026-09-25)

## Comece aqui

Leia `CONTINUITY_LEDGER.md` (G-0089 e G-0090), `docs/roadmap/multiblock-port-manifest.md`
e este arquivo. O handoff `NEXT-SESSION-HANDOFF.md` cobre uma fase anterior de steam;
suas regras de orientação e atribuição ainda são úteis, mas seu estado de progresso está antigo.

O checkout contém **mudanças locais sem commit**. Não descarte o worktree. Os quatro ports abaixo
passaram `spotlessCheck`, `compileJava`, `runUnitTests` (20/20) e `runGameTestServer` (58/58)
em 2026-09-25. `runData` foi repetido após as correções de UI e idioma. O autor encontrou
tooltips ausentes e um slot ausente no Generator Array; as correções estão neste worktree,
aguardando nova verificação in-game. Antes de uma nova
alteração, rode o gate descrito no ledger/`AGENTS.md` e inspecione `git status`.

| Implementado | Estrutura / mecânica | Cobertura automática |
|---|---|---|
| `generator_array` | 3×3×3 GTO; slot no controlador para quatro geradores (bus ainda aceito); hatch normal ou Nexus Flux Matrix no modo wireless | formação, slot, combustível e geração |
| `fishing_ground` | MBS GTO 13×4×13; iscas e loot vanilla por circuito | formação, isca e loot |
| `evaporation_plant` | coluna GTO, módulo auxiliar, salmoura e cadeia de bromo/iodo | formação e as duas evaporações |
| `greenhouse` | MBS GTO 5×5×5; luz do céu; 84 receitas fixas de cultivo | formação e receita de 12 cactus |

`src/main/java/com/raishxn/gtna/common/data/GTNAMachines3.java` registra esses controladores.
As classes de comportamento ficam em `common/machine/multiblock/`; as receitas, em
`data/recipe/GTNAMachineRecipes.java` e `GTNAGreenhouseRecipes.java`. Os MBS copiados do GTO
ficam em `src/main/resources/pattern/gto/` e o leitor em
`common/data/multiblock/GTOCompressedPatternReader.java`. O GameTest está em
`src/main/java/com/raishxn/gtna/gametest/GTNAMachineGameTests.java`.

## Decisões já tomadas pelo autor

- Portar **todos os candidatos viáveis até LuV**, visando ao menos dez multiblocos, com
  estrutura, comportamento, materiais e receitas tão fiéis quanto possível. Criar materiais
  necessários para preservar a progressão.
- Manter `greenhouse` e `component_assembler` na seleção. Excluir `lava_furnace`,
  `tree_growth_simulator`, `reaction_furnace`, `crystallization_chamber`,
  `polymerization_reactor` e `processing_plant`. `large_gas_turbine` já existe no GTCEu;
  não registrar duplicata.
- Candidatos citados: `fishing_ground`, `evaporation_plant`, módulo para Alloy Blast,
  `large_greenhouse`, `water_purification_plant`, `clarifier_purification_unit`,
  `steam_mega_turbine`, `rocket_large_turbine`, `ozonation_purification_unit`,
  `chemical_plant`, `blaze_blast_furnace`, `cold_ice_freezer`,
  `supercritical_steam_turbine`, `supercritical_mega_steam_turbine`, `isa_mill`,
  `industrial_flotation_cell`, `vacuum_drying_furnace`, `precision_assembler`,
  `advanced_fusion_reactor_mk1` e seus módulos. Portar somente o que estiver na
  progressão até LuV; registrar dependências e desvios concretos.
- Para o `generator_array`, wireless significa integração com **Nexus Flux Matrix**;
  o modo normal usa hatch de energia. Não pedir nova confirmação dessas decisões.

## Próxima ação

**Atualização G-0101:** Component Assembler base (LV–IV) e Large Greenhouse já estão implementados
no worktree, com GameTests de formação e 62/62 GameTests aprovados. A extensão grande e receitas
ULV/LuV do Component Assembler ainda faltam; a seleção HV–LuV segue aberta. O autor autorizou
equivalentes locais para dependências externas e receitas alternativas com os mesmos insumos para
controladores excluídos. Nenhum commit/publicação desta leva foi autorizado.

**Nota histórica:** `component_assembler` ainda não foi portado; é o próximo controlador MV mantido pelo autor.
No GTOCore, sua estrutura está em `common/data/machines/MultiBlockC.java:299`, a classe de
comportamento em `common/machine/multiblock/electric/assembly/ComponentAssemblerMachine.java`
e as receitas de componentes em `data/recipe/misc/ComponentRecipes.java`. Ele depende de casings
de tier e do `multi_functional_casing`; portar estrutura, regras de tier, receitas e produção
dos casings antes de considerá-lo pronto. A extensão grande é um módulo separado.

Depois, priorizar os candidatos HV–LuV pelo conjunto de dependências **real** encontrado nas
fontes, sem trocar máquinas ou omitir receitas em silêncio. `blaze_blast_furnace` tem um impasse
de progressão: o casing original é feito no `reaction_furnace`, que o autor excluiu; planejar uma
rota alternativa usando os mesmos insumos e registrar claramente a diferença. O conjunto
`water_purification_plant`/clarifier/ozonation compartilha uma lógica de unidades; estudar a
cadeia inteira antes de registrar peças isoladas. O `large_greenhouse` usa receitas de Greenhouse
**e** Tree Growth Simulator, portanto precisa da segunda família de receitas mesmo sem portar
um controlador Tree Growth duplicado.

Fontes locais: `/home/raishxn/MineProjects/GTOCore-Main` (commit `dc4824d`) e
`/home/raishxn/MineProjects/GTCEu-7.5.3`. Outras fontes estão listadas em `AGENTS.md`.
O autor solicitou `runClient` para os testes desta leva. Manter as mudanças locais até
o autor testar e autorizar publicação.

## QA de compatibilidade e tooltips — G-0092

- O `latest.log` do jogador com GTNA 0.5.0 e KubeJS build.26 aponta a falha fatal no
  `DataGeneratorMixin`. O jar SRG de Minecraft 1.20.1 expõe `DataGenerator.m_123917_()`; a
  injeção publicada usava apenas `run`, sem refmap carregado, e herdava `defaultRequire: 1`.
  O mixin agora aceita ambos os nomes sem remapeamento, usa `require = 0` e apenas registra
  falhas de reflexão. O ambiente de desenvolvimento usa KubeJS build.26. O contrato compilado
  é verificado por `DataGeneratorMixinContractTest`; confirmar também em um cliente distribuído.
- `MetaMachine.onAddFancyInformationTooltip` insere automaticamente a chave
  `gtna.machine.<id>.tooltip` no começo. Nos quatro ports, ela agora contém a função principal,
  e as chamadas `.tooltips(...)` começam em `.tooltip.0`, sem repetir essa descrição.
- Generator Array: o GTO usa multiplicador configurável por dificuldade, limite configurável
  (4 no Normal/Expert) e perda sem fio configurável (5% no Normal). O GTNA usa 1,3×, quatro
  geradores e 5% fixos, pois ainda não porta o sistema de dificuldades do GTO. O tooltip expõe
  esses valores, o slot do controlador ou barramento legado, os hatches de fluido e o destino
  Nexus Flux Matrix. Só três tipos de gerador GTCEu são elegíveis; semi-fluid, rocket engine e
  naquadah reactor do GTO ainda não têm os respectivos ports/receitas no GTNA.
- Fishing Ground: preservadas as quatro linhas narrativas do `FishingFarmTooltips` e, em ordem,
  os quatro circuitos do `fishingFarmTooltips`. O texto explicita isca e água, requisitos da
  implementação GTNA. Os modos de loot não usam o Parallel Hatch; as receitas fixas usam.
- Evaporation Plant: o GTO não registra tooltip narrativo/funcional próprio no controlador;
  o texto GTNA explica torre, limite de repetição, hatches e o módulo auxiliar de titânio já
  registrado em `GTNAModules`, que aceita as hatches Paralela e de Aceleração.
- Greenhouse: preservadas as duas condições originais de luz solar em `GreenhouseTooltips`.
  O GTNA informa a alternativa de lama ao solo rico do Farmer's Delight quando o mod não está
  presente. A tradução `pt_br` acompanha todas as novas linhas; atribuição `Source: GTO` vem
  somente de `GTNASources`.
- Gate final com KubeJS build.26: `spotlessCheck`, `compileJava`, `runUnitTests`,
  `runGameTestServer` e `runData --offline` passaram. `runData` terminou os provedores e saiu
  normalmente. `runClient --offline` abriu a janela Forge sem a falha do mixin e foi encerrado
  após a checagem de inicialização. Ainda falta reteste com o jar distribuído no modpack completo
  do jogador e QA visual das duas traduções. Não houve commit nem publicação.

## QA seguinte — G-0093

- Nexus Flux Matrix: modo Safe removido por decisão do autor. Não há corte de saída por carga
  percentual; o saldo pode cair até zero. Campos antigos `SafeMode` do mundo e `safeMode` do
  balance JSON são ignorados; novos saves não gravam `SafeMode`. Reiniciar o cliente para testar.
- Evaporation Plant: a Wireless Steam Input Hatch do GTNA tinha também a habilidade
  `IMPORT_FLUIDS`, por isso passava no predicado genérico. O controlador e a torre auxiliar
  agora aceitam entradas de fluido que **não** anunciam `STEAM`. O teste cobre as hatches de
  vapor padrão/sem fio e a Fluid Input Hatch HV. A hatch de vapor padrão só registra `STEAM` no
  GTCEu, portanto a causa de sua aparição em outro ponto da estrutura exigiria a posição exata
  ou uma captura do autor.
- Gate: `spotlessCheck`, `compileJava`, `runUnitTests` e `runGameTestServer` passaram. O
  `runData` do gate teve uma falha intermitente no registro do renderizador da Artificial Star;
  duas repetições isoladas passaram (segunda sem arquivos alterados). Se o erro reaparecer,
  investigar o registro do renderer. O cliente foi reiniciado com G-0093 e ficou aberto para QA.

## QA de Steam Hatches e Greenhouse — G-0094

- Por solicitação do autor, a limitação das Wireless Steam Hatches passou do predicado da
  Evaporation Plant para o registro das quatro peças: inputs bronze/aço anunciam só `STEAM`,
  outputs bronze/aço anunciam só `GTNAPartAbility.STEAM_EXPORT_FLUIDS`. Nenhuma é hatch universal
  de fluidos. O output não pode anunciar `STEAM`, pois isso o faria ocupar a posição de entrada
  de vapor e desformar máquinas steam. O predicado defensivo da Evaporation continua presente.
- O padrão da Evaporation é fiel ao GTO: exatamente uma entrada de fluido e uma ou duas entradas
  de energia na base `Y`, até uma saída por camada `X`. Não exige tier ULV. O GameTest aceitou
  Fluid Input HV em duas posições da base e Fluid Output HV em duas posições do corpo. O tooltip
  `en_us`/`pt_br` agora explicita essas posições, quantidades e tier livre. Ainda
  falta a camada/posição exata do caso relatado pelo autor e distinguir a hatch ULV de energia
  da de fluido.
- Greenhouse: a amostra de luz foi alinhada à área 3×3 sobre o vidro temperado. A máquina
  verifica blocos opacos desde o nível logo acima do teto, portanto uma cobertura impede sol
  mesmo quando o valor de skylight ainda está em cache. O GameTest provou luz inicial, ausência
  com cobertura, recuperação após removê-la e produção de 12 cactos. O nível do GameTest tinha
  pedra gerada acima da estrutura; o teste agora limpa a camada antes de verificar a luz.
- Gate completo `spotlessCheck compileJava runUnitTests runGameTestServer runData --offline`
  passou com 59/59 GameTests e datagen `written: 1` após a tradução (`written: 0` antes dela).
  `runClient --offline` abriu a janela Forge com a versão atual e ficou aberto para QA. Ainda
  falta testar formação e leitura no mundo do autor; manter tudo local, sem commit ou publicação.

## QA da MAX Output Hatch — G-0095

- O log do cliente registrou diagnóstico de estrutura na Evaporation às 11:54–11:55. A prévia
  escreve `ULV Output Hatch` como representante de `EXPORT_FLUIDS`; hatches MAX também são
  válidas quando colocadas em estágios `X`.
- No mundo salvo, a torre de `(-13,-59,-117)` tem MAX Output Hatch em `(-14,-59,-117)`, que é
  célula `Y` da base: movê-la para `(-14,-58,-117)` (casing `X` do primeiro estágio). A Fluid
  Input Hatch MV em `(-12,-59,-117)` já atende à entrada. A torre de `(-8,-59,-109)` tem MAX
  Output Hatch em `(-7,-57,-109)`, célula `X` válida; falta nela uma Fluid Input Hatch na base,
  que pode ocupar `(-9,-59,-109)` no lugar do casing. Coordenadas vieram de leitura do save,
  sem modificá-lo.
- GameTest novo confirma MAX Output em `X` e sua rejeição em `Y`. Gate completo passou com
  59/59 GameTests e `runData` sem mudanças (`written: 0`). O usuário precisa mover/colocar as
  peças no mundo e confirmar a formação; não houve mudança na regra de produção.

## Rodada de 2026-09-25 (autor ausente) — Chemical Plant e Mega Alloy Blast Smelter

Seguindo a decisão do autor de portar todas as cadeias viáveis até LuV, com gate após cada máquina,
sem commit e com receitas de controlador omitidas quando a original depende de recursos exclusivos
do GTO:

- **`chemical_plant` (G-0102):** portada e verde no gate (65/65). Receita do controlador omitida e
  anotada. Ver `CONTINUITY_LEDGER.md`.
- **`mega_alloy_blast_smelter`:** portado do GTOCore (id exclusivo; o `alloy_blast_smelter` normal já
  é do GTCEu). Estrutura 11×18×11 copiada, roda `GCYMRecipeTypes.ALLOY_BLAST_RECIPES` já existente,
  com Parallel Hatch e bônus documentado de 0,8× EU / 0,6× duração. A célula de "integral framework"
  do GTO virou moldura de Tungstênio-Aço e as habilidades GCYM viraram `autoAbilities`. Gate
  verificado nesta rodada.
- **`isa_mill` (G-0104):** portado do GTOCore com o padrão comprimido, prefixo `MILLED`, data key
  `grindball`, Ball Hatch funcional (sem renderer animado) e as 48 receitas de moagem. O gate da
  esfera fica no `getRealRecipe` e o dano é aplicado em `beforeWorking` (o `fullModifyRecipe` do
  GTCEu roda por candidata). Receita do controlador presente: Assembly Line com Inconel-625/792 e
  Tantalloy-61 portados 1:1 de `MaterialA`. Gate: 69/69 GameTests e `runData written: 0`. Ver
  `CONTINUITY_LEDGER.md` (G-0104).

### Bloqueios dos candidatos restantes (pesquisa concluída)

O material dos subagentes confirma que cada máquina restante depende de uma camada grande de recursos
exclusivos do GTO (carcaças, materiais/fluidos, recipe types) e/ou de classes base do gtolib
(compiladas, sem fonte). Port fiel exige portar essa camada primeiro. Resumo:

- **Extensão grande do Component Assembler** e **módulo de atomização do Cold Ice Freezer:** são
  `addSubPattern` no mesmo controlador. Exigem carcaças GTO que faltam
  (`THREE_PROOF_COMPUTER_CASING`, `MACHINING_CONTROL_CASING_MK2`, `ENERGY_CONTROL_CASING_MK2`,
  `ELECTRIC_POWER_TRANSMISSION_CASING`) e o recipe type `ATOMIZATION_CONDENSATION_RECIPES`; o
  `NAQUADAH_ALLOY_CASING` já existe no GTNA. Precisam também de `TierCasingMultiblockMachine`
  (gtolib) e itens do Ad Astra no atomizador.
- **Turbinas** (`steam_mega_turbine`, `rocket_large_turbine`, `supercritical_steam_turbine`,
  `supercritical_mega_steam_turbine`): carcaças GTO (`SUPERCRITICAL_TURBINE_CASING`,
  `CHEMICAL_CORROSION_RESISTANT_PIPE_CASING`, `HSSS_BOROSILICATE_GLASS`, `IRIDIUM_GEARBOX`), recipe
  types GTO (`ROCKET_ENGINE_FUELS`, `SUPERCRITICAL_STEAM_TURBINE_FUELS`), material `SupercriticalSteam`
  e a classe `TurbineMachine` do gtolib (modo alta velocidade, mega com 4 rotores). O GTCEu já tem
  rotores e `RotorHolderPartMachine`, mas não mega/rocket/supercritical.
  **Atualização G-0105:** a `rocket_large_turbine` foi concluída sem blocos GTO (o `registerLargeTurbine`
  do GTO usa carcaças GTCEu) e sem gtolib: `WorkableElectricMultiblockMachine` + `TurbineMachine`
  adaptada, recipe type novo `gtna:rocket_engine` e só a receita de `RocketFuel` do GTCEu. Falta a
  `supercritical_steam_turbine` (ainda exige `SUPERCRITICAL_TURBINE_CASING` e o recipe type
  `SUPERCRITICAL_STEAM_TURBINE_FUELS`).
  **Atualização G-0106:** a `supercritical_steam_turbine` também foi concluída: a carcaça
  `SUPERCRITICAL_TURBINE_CASING` foi portada como bloco GTNA (texturas GTO, CC BY-NC-SA), o recipe
  type `gtna:supercritical_steam_turbine` foi criado e a receita de combustível reusa
  `DenseSupercriticalSteam` no lugar do `SupercriticalSteam` do GTO. As duas turbinas não-mega
  compartilham a base `GTNALargeTurbineMachine`; só as versões **mega** (4 rotores + glass tier)
  continuam fora do escopo. Ver `CONTINUITY_LEDGER.md` (G-0106).
- **Processamento:**
  - `isa_mill`: carcaças Inconel-625 (casing/gearbox/pipe), `GRIND_BALL_HATCH` + `BallHatchPartMachine`
    + dois itens de esfera + renderer, e `ISA_MILL_RECIPES`.
  - `industrial_flotation_cell`: carcaças Hastelloy-N75 (casing/gearbox/pipe), `FLOTATION_CELL` e
    `FLOTATING_BENEFICIATION_RECIPES`. **Concluído em G-0107** (12 receitas, controlador em Assembly
    Line, overclock perfeito + Parallel Hatch).
  - `vacuum_drying_furnace`: `RED_STEEL_CASING` (textura existe no GTOCore), `VACUUM_DRYING_RECIPES`
    e `DEHYDRATOR_RECIPES`; **concluído em G-0108** com as 12 receitas de secagem 1:1 e a receita
    `salt_dust` do Dehydrator (as demais receitas do Dehydrator continuam omitidas por dependerem de
    fluidos exclusivos do GTO). A cadeia flotação → secagem → RedMud está fechada.
  - `precision_assembler`: a carcaça principal já existe no GTNA; faltam
    `PRECISION_ASSEMBLER_RECIPES`, os predicados de tier de vidro/carcaça e receitas (muito GTO).
- **Cadeia de purificação de água:** 8 unidades + o controlador. Exige ~12 carcaças GTO, ~15
  materiais/fluidos GTO, os recipe types GTO e as classes gtolib
  `WaterPurificationUnitMachine`/`IIWirelessInteractor`/`NoEnergyCustomParallelMultiblockMachine`, além
  da réplica do `.mbs` do GTOCore lida pelo `MultiBlockFileReader` (proprietário).
- **Advanced Fusion Reactor MK1 e módulos:** endgame; mesma situação de recursos GTO.

Recomendação para a próxima fase: portar primeiro a **camada compartilhada** (carcaças GTO reusáveis
com texturas do GTOCore + recipe types GTO equivalentes + materiais/fluidos), porque isso destrava
várias máquinas de uma vez; só então portar as máquinas. Manter tudo local até teste in-game.
