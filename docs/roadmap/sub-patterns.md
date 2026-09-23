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
`Predicates` (já expostos pelo GTCEu ao KubeJS).

```js
// kubejs/server_scripts/gtna_sub_patterns.js
GTNAServerEvents.subPatterns(event => {
  // Adiciona uma torre de módulo ao Integrated Ore Processor (máquina já registrada).
  event.add('gtna:integrated_ore_processor', definition => FactoryBlockPattern.start()
    .aisle('AAA', 'A~A', 'AAA')
    .aisle('AAA', 'AAA', 'AAA')
    .aisle('AAA', 'AAA', 'AAA')
    .where('~', Predicates.controller(Predicates.blocks(definition.get())))
    .where('A', Predicates.blocks('gtceu:stainless_steel_casing')
      .or(Predicates.abilities('gtceu:parallel_hatch').setMaxGlobalLimited(1))
      .or(Predicates.abilities('gtna:accelerate_hatch').setMaxGlobalLimited(1)))
    .build())
})
```

Notas:
- O sub-pattern é **opcional**: se não casar, a máquina continua formando com o pattern principal.
- A âncora é o **controller** (o sub-pattern é checado na posição/facing do controller).
- As abilidades disponíveis incluem as do GTCEu (`gtceu:parallel_hatch`, `gtceu:maintenance`, ...) e
  as do GTNA (`gtna:accelerate_hatch`, `gtna:overclock_hatch`, `gtna:thread_hatch`, ...).
- Máquinas Java usam a interface `ISubPatternMachine` (ver `LiquefactionFurnaceMachine` como
  exemplo); o registry é o caminho para KubeJS/datapacks.

## Pendência

A ancoragem GTNA é no controller; o GTOCore usa um offset próprio no gtolib. Ao portar um sub-pattern
do GTOCore, validar in-game se a geometria casa; se não, ajustar o offset/geometria.
