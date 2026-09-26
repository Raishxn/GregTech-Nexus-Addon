# Sub-patterns (módulos / extensões) de multiblocos

A partir do **G-0063**, um multiblock do GTNA (ou de qualquer mod, via KubeJS) pode ser **estendido**
por estruturas adicionais — "sub-patterns" / módulos — presas ao **mesmo controller**. As partes
(hatches/buses) detectadas por cada extensão são **fundidas** no controller, então o módulo **libera
novas habilidades**: Parallel/Accelerate hatches, IO extra, etc. É a versão GTNA-native do
`addSubPattern` do GTOCore.

## Como funciona (interno)

- `ISubPatternMachine` (interface GTNA): uma máquina Java declara suas extensões em
  `List<BlockPattern> gtna$getSubPatterns()`.
- `GTNASubPatterns` (registry estático, chave = id da máquina): usado pelo KubeJS para adicionar
  extensões a máquinas novas **ou já registradas**.
- `MultiblockControllerMachineMixin`: adiciona um `checkPattern()` ao `MultiblockControllerMachine`.
  Roda o pattern principal (como o default) e, se casar, checa **cada extensão no mesmo controller**,
  fundindo as partes encontradas. O match context do pattern principal é snapshotado/restaurado
  porque checar um sub-pattern o reseta. Funciona para **qualquer** multibloco (mixin de classe).

## API KubeJS (para criadores de modpack)

Evento de **servidor** `GTNAServerEvents.subPatterns`. O factory recebe o
`MultiblockMachineDefinition` e devolve um `BlockPattern` montado com o `FactoryBlockPattern` e o
`Predicates` (já expostos pelo GTCEu ao KubeJS). O evento é disparado após o carregamento dos
scripts de servidor. O exemplo executável está em
[`examples/kubejs/server_scripts/gtna_integrated_ore_module.js`](../../examples/kubejs/server_scripts/gtna_integrated_ore_module.js);
`runClient` o instala automaticamente no diretório local `run/kubejs/server_scripts`.

```js
// kubejs/server_scripts/gtna_integrated_ore_module.js
GTNAServerEvents.subPatterns(event => {
  // Coluna auxiliar de três blocos ao lado do Integrated Ore Processor.
  event.add('gtna:integrated_ore_processor', definition => FactoryBlockPattern.start()
    .aisle('   A', 'C  A', '   A')
    .where('C', Predicates.controller(Predicates.blocks(definition.get())))
    .where('A', Predicates.blocks(GTBlocks.CASING_STAINLESS_CLEAN.get())
      .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(1)))
    .where(' ', Predicates.any())
    .build())
})
```

Notas:
- O sub-pattern é **opcional**: se não casar, a máquina continua formando com o pattern principal.
- A âncora é o **controller** (o sub-pattern é checado na posição/facing do controller).
- As abilities disponíveis incluem as do GTCEu (`PartAbility.IMPORT_ITEMS`,
  `PartAbility.PARALLEL_HATCH`, ...) e as do GTNA (`GTNAPartAbility.ACCELERATE_HATCH`,
  `GTNAPartAbility.THREAD_HATCH`, ...).
- Máquinas Java usam a interface `ISubPatternMachine` (ver `LiquefactionFurnaceMachine` como
  exemplo); o registry é o caminho para KubeJS/datapacks.

### Módulos de desempenho em `gtna:multiple_recipes`

O overload `event.add(id, factory, descriptionKey, speedBonus, perfectOverclock)` configura bônus
que só entram em vigor quando **aquele módulo** forma. `speedBonus=2.0` reduz a duração à metade;
valores maiores são aceitos (teto combinado de 1024×). `perfectOverclock=true` aplica o overclock
perfeito quando não há Overclock Hatch substituindo a curva. Os bônus são usados pela classe GTNA
`WorkableElectricMultipleRecipesMachine` e por `GTNAMultipleRecipesLogic` na execução real.

O binding `GTNAPartAbility` publica `PARALLEL_HATCH` e `PARALLEL_CONTROL_HATCH` (sinônimos para a
ability de Parallel Hatch do GTCEu), `OVERCLOCK_HATCH` e `ACCELERATE_HATCH`. Use cada um em
`Predicates.abilities(...).setMaxGlobalLimited(1)` no slot desejado. O Parallel Hatch define a
quantidade de receitas paralelas; o Accelerate Hatch aplica seu bônus de duração por tier; o
Overclock Hatch fornece sua curva de overclock. Veja o script completo em
[`examples/kubejs/server_scripts/gtna_multiple_recipes_module.js`](../../examples/kubejs/server_scripts/gtna_multiple_recipes_module.js).

## Na UI e no preview (G-0066)

- **Contagem na UI:** todo multibloco expõe `IGTNAModuleHost` (implementado pelo
  `MultiblockControllerMachineMixin`): o `checkPattern()` grava quantos módulos casaram e o
  `WorkableElectricMultiblockMachineMixin` mostra **"Formed modules: n / total"** no `addDisplayText`
  (lang `gtna.machine.modules_amount`). Quem tem UI própria pode chamar
  `GTNAModuleDisplay.append(textList, this)`.
- **Preview no JEI:** `MultiblockMachineDefinitionMixin` anexa cada sub-pattern **registrado** como
  uma **página extra** do preview de multibloco (`getMatchingShapes()`), ao lado das páginas do
  pattern principal. Só o registry (`GTNASubPatterns`) alimenta o preview — módulos declarados só via
  `ISubPatternMachine` (por máquina) não aparecem.
- **Tooltip do item:** `GTNASubPatterns.register(id, factory, Component...)` guarda linhas de tooltip e
  o `MetaMachineBlockMixin` as anexa ao item da máquina (ex.: o EBF anuncia Accelerate Hatch e um
  Energy Hatch adicional, como o `moduleTooltips` do GTOCore).
- **Terminal Nexus:** a opção **"Module Build = N"** faz o auto-build construir a base **e** os N
  primeiros módulos registrados (registry + `ISubPatternMachine`), cada um via
  `NexusBlockPattern.fromBlockPattern` — como o advanced terminal do GTMThings/GTO.

## Regra de design: a base não ganha threads/parallel por acidente

Um módulo que libera Parallel/Accelerate/Thread só faz sentido se a **base** não aceitar esses hatches.
Cuidados:

- Não use `autoAbilities(recipeTypes)` no pattern base se não quiser que o `PredicatesMixin` injete
  Overclock/Accelerate automaticamente. Prefira **IO explícito**
  (`INPUT_ENERGY`/`IMPORT_ITEMS`/`EXPORT_FLUIDS`/`MAINTENANCE`/`MUFFLER`).
- Não estenda a base multi-receita (`WorkableElectricMultipleRecipesMachine`) se a máquina **não**
  deve ter threads. Ex.: o `liquefaction_furnace` é `CoilWorkableElectricMultiblockMachine` (normal).
- Os hatches de performance entram **só pelo módulo** (ex.: o módulo do `liquefaction_furnace` adiciona
  1 Parallel Hatch + 1 Accelerate Hatch).

## Pendência

A ancoragem GTNA é no controller; o GTOCore usa um offset próprio no gtolib. Ao portar um sub-pattern
do GTOCore, validar in-game se a geometria casa; se não, ajustar o offset/geometria.

Ainda **não** implementado (pedido do autor, G-0066): um **botão dedicado a módulos** no preview.
