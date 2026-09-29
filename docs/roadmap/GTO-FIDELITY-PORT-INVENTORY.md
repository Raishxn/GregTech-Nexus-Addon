# Inventário para um port fiel do GTO (além do que já existe no GTNA)

Escopo: tudo que falta para portar, com fidelidade máxima, os multiblocos restantes da seleção até
LuV. Fontes: `/home/raishxn/MineProjects/GTOCore-Main` (commit `dc4824d`), `GTCEu-7.5.3`,
`GTNA`. Alguns itens vêm de leitura estática; a lista pode variar em poucos itens.

## 0. Resumo do tamanho real

| Camada | Quantidade aproximada |
|---|---|
| Classes-base do gtolib (jar protegido, sem fonte) | ~15 |
| Predicados / data keys / abilities do GTO | ~20 |
| Part machines próprios do GTO | ~8 |
| Recipe types exclusivos do GTO | ~12 |
| Materiais/fluidos/items/prefixos exclusivos | ~40 |
| Blocos e carcaças exclusivos | ~45 (inclui 14 tiers de casing de assembly line) |
| Padrões `.mbs` a copiar | ~15 (o GTNA já lê o formato) |
| Dependências de mods externos | 4 (Ad Astra, Deeper and Darker, Ars Nouveau, GTMThings) |

Conclusão: um port fiel é, na prática, portar **uma fatia grande do ecossistema do GTOCore**, não só
algumas máquinas. A boa notícia é que o leitor de `.mbs` já existe no GTNA e as texturas do GTOCore
são CC BY-NC-SA (copiáveis com atribuição).

## 1. Infraestrutura base (o bloqueio estrutural)

O GTOCore usa classes do `gtolib` (jar protegido/nativo, sem código-fonte no checkout). Para um port
fiel, o GTNA precisa reimplementar equivalentes:

- `ElectricMultiblockMachine` (base de quase todo o GTO)
- `CoilCustomParallelMultiblockMachine`
- `CustomParallelMultiblockMachine`
- `NoEnergyCustomParallelMultiblockMachine`
- `TierCasingMultiblockMachine`
- `TierCasingCrossRecipeMultiblockMachine`
- `CrossRecipeMultiblockMachine`
- `CoilCrossRecipeMultiblockMachine`
- `CoilMultiblockMachine`
- `GCYMMultiblockMachine`
- `MultiBlockFileReader` (formato `.mbs`) — **o GTNA já tem** `GTOCompressedPatternReader`
- `CoilTrait`, `EnergyContainerTrait`
- `IdleReason`, `MachineUtils`
- `ICustomRecipeLogicHolder`, `IIWirelessInteractor`
- `NotifiableCatalystHandler`
- `GTOCleanroomType`
- `GTORecipeModifiers` (nativo: `coilReductionOverclock`, `UPGRADE_GCYM_OVERCLOCKING`,
  `UPGRADE_EBF_OVERCLOCK`, `UPGRADE_PARALLELIZABLE_OVERCLOCK`, `ln`, `parallel`, etc.)
- `GTOTagPrefix`
- anotações do datasynclib (`@SaveToDisk`, `@SyncToClient`)

`GTOPredicates`, `GTORecipeDataKeys`, `GTOPartAbility` e `TurbineMachine` estão em código-fonte
aberto no GTOCore e podem ser reimplementados/adaptados diretamente.

## 2. Predicados, data keys e abilities

**GTOPredicates:** `tierBlock` (+ mapas `CALMAP`, `GLASSMAP`, `INTEGRALFRAMEWORKMAP`), `glass()`,
`machineCasing()`, `integralFramework()`, `autoGCYMAbilities`, `autoAccelerateAbilities`,
`autoLaserAbilities`, `autoThreadLaserAbilities`, `recordPosition`, `absBlocks`, `light`,
`frame(GTO material)`.

**GTORecipeDataKeys:** `TIER`, `GRINDBALL`, `GLASS_TIER`, `MACHINE_CASING_TIER`,
`COMPONENT_ASSEMBLY_CASING_TIER`, `INTEGRAL_FRAMEWORK_TIER`.

**GTOPartAbility:** `CATALYST_HATCH`, `PARALLEL_HATCH` (o do GTO, diferente do GTCEu),
`ACCELERATE_HATCH`, `LENS_INDICATOR_HATCH`, `DEGASSING_CONTROL_HATCH`, `OPTICAL_DATA_RECEPTION`.

**Part machines do GTO:** `CatalystHatchPartMachine`, `BallHatchPartMachine` (+ `BallHatchRenderer`
e itens de esfera), `SensorPartMachine`, `IndicatorHatchPartMachine`, `RotorHatch`,
`ParallelHatchPartMachine` (gtolib), `MachineAccessLink`, `PH_SENSOR`, `HEAT_SENSOR`, `LENS_HOUSING`.

## 3. Recipe types exclusivos do GTO

- `ISA_MILL_RECIPES`
- `FLOTATING_BENEFICIATION_RECIPES`
- `VACUUM_DRYING_RECIPES`
- `DEHYDRATOR_RECIPES`
- `PRECISION_ASSEMBLER_RECIPES`
- `ATOMIZATION_CONDENSATION_RECIPES`
- `PETROCHEMICAL_PLANT_RECIPES`
- `WATER_PURIFICATION_PLANT_RECIPES`
- `ROCKET_ENGINE_FUELS`
- `SUPERCRITICAL_STEAM_TURBINE_FUELS`
- `SUPRACHRONAL_ASSEMBLY_LINE_RECIPES`
- demais recipe types GTO usados por módulos (ex.: `component_assembly` já existe no GTNA)

## 4. Materiais, fluidos, items e prefixos exclusivos

- **Purificação de água:** `FilteredSater`, `OzoneWater`, `FlocculentWater`, `PHNeutralWater`,
  `ExtremeTemperatureWater`, `ElectricEquilibriumWater`, `DegassedWater`, `BaryonicPerfectionWater`,
  `Ozone`, `PolyAluminiumChloride`, `FlocculationWasteSolution`, `QuarkGluon`,
  `StableBaryonicMatter` + 6 catalisadores de quark e `SCRAP`.
- **Flotação:** `SodiumEthylxanthate`, `PotassiumEthylxanthate`, `Turpentine` e os fluidos
  `*Front` (`PyropeFront`, `RedstoneFront`, `ChalcopyriteFront`, `MetalCompoundParticleFront`,
  `GrossularFront`, `SphaleriteFront`, `NickelFront`, ...).
- **Secagem a vácuo:** `RedMud`, `MetalCompoundParticleFront`, `ParamagneticResidues`,
  `FerromagneticResidues`, `HeavyDiamagneticResidues`, `HeavyFerromagneticResidues`,
  `HeavyParamagneticResidues`, `NanoScaleTungsten`.
- **Turbinas/fusão:** `SupercriticalSteam`, `Amprosium`, `Mithril`, combustíveis de fusão GTO.
- **Component Assembler / Precision Assembler:** `CarbonFiberPolyphenyleneSulfideComposite`,
  componentes ULV, `PM_CHIP`, `UIV_VOLTAGE_COIL`, `Infuscolium`, `MutatedLivingSolder`, etc.
- **GTOTagPrefix:** `MILLED`, `NANITES`, entre outros.
- Os materiais que o GTNA já tem (`HastelloyN`, `Indalloy140`, `Echoite`, `Stronze`, `Breel`,
  `DenseSupercriticalSteam`, `InsanelySupercriticalSteam`, `CompressedSteam`, ...) não precisam ser
  refeitos.

## 5. Blocos e carcaças exclusivos

**Purificação de água (18):** `STERILE_WATER_PLANT_CASING`, `REINFORCED_STERILE_WATER_PLANT_CASING`,
`CHEMICAL_GRADE_GLASS`, `HIGH_PRESSURE_RESISTANT_CASING`, `OZONE_CASING`, `FLOCCULATION_CASING`,
`INERT_NEUTRALIZATION_WATER_PLANT_CASING`, `STABILIZED_NAQUADAH_WATER_PLANT_CASING`, `SPEEDING_PIPE`,
`PLASMA_HEATER_CASING`, `IMPROVED_SUPERCONDUCTOR_COIL`, `HIGH_ENERGY_ULTRAVIOLET_EMITTER_CASING`,
`ELECTRON_PERMEABLE_AMPROSIUM_COATED_GLASS`, `NAQUADAH_REINFORCED_PLANT_CASING`,
`OMNI_PURPOSE_INFINITY_FUSED_GLASS`, `NON_PHOTONIC_MATTER_EXCLUSION_GLASS`, `QUARK_PIPE`,
`QUARK_EXCLUSION_CASING`.

**Turbinas (4):** `SUPERCRITICAL_TURBINE_CASING`, `CHEMICAL_CORROSION_RESISTANT_PIPE_CASING`,
`HSSS_BOROSILICATE_GLASS`, `IRIDIUM_GEARBOX`.

**ISA Mill (3):** `INCONEL_625_CASING`, `INCONEL_625_GEARBOX`, `INCONEL_625_PIPE`.

**Flotation Cell (4):** `HASTELLOY_N_75_CASING`, `HASTELLOY_N_75_GEARBOX`, `HASTELLOY_N_75_PIPE`,
`FLOTATION_CELL`.

**Vacuum Drying (1):** `RED_STEEL_CASING`.

**Extensão do Component Assembler (5 + 14 tiers):** `THREE_PROOF_COMPUTER_CASING`,
`MACHINING_CONTROL_CASING_MK2`, `ENERGY_CONTROL_CASING_MK2`, `ELECTRIC_POWER_TRANSMISSION_CASING`,
`TITANIUM_NITRIDE_CERAMIC_IMPACT_RESISTANT_MECHANICAL_BLOCK` e
`COMPONENT_ASSEMBLY_LINE_CASING_LV..MAX` (14 tiers) para a `component_assembly_line`.

**Advanced Fusion Reactor MK1 / Kuangbiao (`luv_kuangbiao_one_giant_nuclear_fusion_reactor`):
`MOLECULAR_CASING`, `ADVANCED_FUSION_COIL`, `LASER_CASING`, `URUIUM_COIL_BLOCK`, `HYPER_CORE`,
`SPACETIME_ASSEMBLY_LINE_CASING`, `LASER_COOLING_CASING`, `QUANTUM_GLASS`, `SPS_CASING`,
`SPACETIME_ASSEMBLY_LINE_UNIT`, `CONTAINMENT_FIELD_GENERATOR`, `AMPROSIUM_ACTIVE_CASING`,
`MOLECULAR_COIL`, `FUSION_CASING_MK4`, `FUSION_CASING_MK5` (+ renderer próprio).

**Já existem no GTNA (não portar de novo):** `HIGH_STRENGTH_CONCRETE`,
`OXIDATION_RESISTANT_HASTELLOY_N_MECHANICAL_CASING`, `PROCESS_MACHINE_CASING`, `NAQUADAH_ALLOY_CASING`,
`HYPER_MECHANICAL_CASING`, `HOLLOW_CASING`, `DIMENSION_INJECTION_CASING`,
`DIMENSIONALLY_TRANSCENDENT_CASING`, `DIMENSIONAL_BRIDGE_CASING`,
`DEGENERATE_RHENIUM_CONSTRAINED_CASING`, `RESTRAINT_DEVICE`, `EXTREME_DENSITY_CASING`,
`GRAVITON_FIELD_CONSTRAINT_CASING`, `MULTI_FUNCTIONAL_CASING`, `COMPONENT_ASSEMBLY_CASING_LV..IV`.

## 6. Padrões `.mbs`

O GTNA já lê o formato comprimido do GTOCore (`GTOCompressedPatternReader`). Basta copiar o `.mbs`
de cada máquina para `src/main/resources/pattern/gto/` e mapear os símbolos com predicados GTNA.
Máquinas com padrão 100% inline no GTO (ex.: `mega_alloy_blast_smelter`, `greenhouse`) não precisam
de `.mbs`.

## 7. Dependências de mods externos

- **Ad Astra** — itens no módulo de atomização do Cold Ice Freezer.
- **Deeper and Darker** — itens nas receitas do Precision Assembler.
- **Ars Nouveau** + **GTMThings** — usados pela classe `TurbineMachine` do GTO.

## 8. Bloqueio por máquina (o que cada uma exige)

| Máquina | Blocos GTO que faltam | Recipe type GTO | Outros |
|---|---|---|---|
| Extensão Component Assembler | 5 + 14 tiers | — (usa `component_assembly`) | `TierCasingMultiblockMachine`, `CALMAP` |
| Atomização Cold Ice Freezer | 0 (usa `NAQUADAH_ALLOY_CASING`) | `ATOMIZATION_CONDENSATION_RECIPES` | itens Ad Astra, `ACCELERATE_HATCH` |
| Water purification (9 máquinas) | 18 | `WATER_PURIFICATION_PLANT_RECIPES` | gtolib `IIWirelessInteractor`, `NoEnergyCustomParallelMultiblockMachine`, ~15 materiais |
| Turbinas (4) | 4 | `ROCKET_ENGINE_FUELS`, `SUPERCRITICAL_STEAM_TURBINE_FUELS` | `TurbineMachine`, `RotorHatch`, `SupercriticalSteam` |
| ISA Mill | 3 | `ISA_MILL_RECIPES` | `GRIND_BALL_HATCH` + `BallHatchPartMachine` + renderer + 2 itens |
| Industrial Flotation Cell | 4 | `FLOTATING_BENEFICIATION_RECIPES` | ~7 materiais GTO — ✅ portado em G-0107 |
| Vacuum Drying Furnace | 1 | `VACUUM_DRYING_RECIPES`, `DEHYDRATOR_RECIPES` | vários fluidos GTO — ✅ portado em G-0108 |
| Precision Assembler | 0 (carcaça já existe) | `PRECISION_ASSEMBLER_RECIPES` | `GLASS_TIER`/`MACHINE_CASING_TIER`, receitas GTO |
| Advanced Fusion Reactor MK1 | ~14 | `FUSION`-like GTO | `CrossRecipeMultiblockMachine`, renderer próprio, calor custom |

## 9. Ordem recomendada para o port fiel

1. **Fase 0 — infraestrutura:** reimplementar as bases gtolib usadas, `GTOPredicates`/data keys,
   abilities e part machines. Sem isso, nenhuma máquina fica fiel.
2. **Fase 1 — camada compartilhada:** recipe types GTO + materiais/fluidos/items + prefixos e as
   ~45 carcaças (copiando texturas do GTOCore com atribuição CC BY-NC-SA). Isso destrava várias
   máquinas de uma vez.
3. **Fase 2 — máquinas**, na ordem de dependências: processamento (ISA, flotação, secagem,
   precision) → turbinas → atomização/extensão do Component Assembler → purificação de água →
   fusão avançada + módulos.
4. Manter tudo local; testar in-game por máquina; gate completo a cada etapa.

## 10. Triagem: o que vale trazer e o que não vale

Critérios do autor: evitar o sistema de **glass tier** e as cadeias de alto tier (que exigiriam
mais ~10 multiblocos), evitar dependências novas (Ars Nouveau, Deeper and Darker, Farmer's Delight)
e reaproveitar a infraestrutura que o GTNA já tem (Ad Astra e GTMThings já são dependências).

### Vale trazer (fase 2 enxuta)

| Máquina | Precisa portar | Reaproveita | Prós | Contras |
|---|---|---|---|---|
| **Módulo de atomização do Cold Ice Freezer** | `ATOMIZATION_CONDENSATION_RECIPES`, part/ability de aceleração, um subconjunto das receitas | Carcaças existentes (`NAQUADAH_ALLOY_CASING`, `HEAT_VENT`), Ad Astra já é dep | Completa uma máquina que o GTNA já tem; zero carcaça nova | O gerador de receitas toca materiais GTO; portar subconjunto |
| **ISA Mill** | 3 carcaças Inconel-625 (textura do GTO), `ISA_MILL_RECIPES`, prefixo `MILLED` (ou substituto), `GRINDBALL` key, `BallHatchPartMachine` (+2 itens; renderer é cosmético) | — | Receitas com **0 materiais GTO** (só GTCEu + `MILLED`); estrutura 7×3×3 cabe no teste | Parte custom (esferas) + conjunto de itens `MILLED` |
| **Rocket Large Turbine** | `ROCKET_ENGINE_FUELS`, base de turbina (pode reusar `LargeTurbineMachine` do GTCEu), 1 receita de combustível GTCEu | Todas as carcaças são GTCEu/GCYM; rotores e rotor holder GTCEu; Ad Astra já é dep | Zero bloco novo; distinto das turbinas existentes | Fidelidade do comportamento de turbina; demais combustíveis são materiais GTO |

### Possível, mas com custo alto (avaliar depois)

| Máquina | Custo | Por que não agora |
|---|---|---|
| **Industrial Flotation Cell** | 4 blocos novos + `FLOTATING_BENEFICIATION_RECIPES` + ~7 materiais GTO — ✅ feito em G-0107 | Resolvido: os `*Front` têm consumidores na secagem e o `RedMud` no Mixer (cadeia fechada) |
| **Vacuum Drying Furnace** | 1 carcaça (`RED_STEEL_CASING`) + 2 recipe types + fluidos GTO — ✅ feito em G-0108 | Resolvido: receitas 1:1 com dusts GTCEu + `RedMud`/`Water`; `NeutralisedRedMud` é terminal até a cadeia StoneDust ser portada |
| **Steam Mega / Supercritical turbines** | Carcaças GTO + recipe types + lógica de mega turbina (4 rotores) + tier de vidro para dano | Mega turbina é lógica nova; supercritical exige carcaças/materiais GTO |

### Não vale trazer nesta fase

| Máquina | Motivo |
|---|---|
| **Precision Assembler** | Depende de glass tier; receitas pesadas em GTO + Deeper and Darker |
| **Extensão do Component Assembler / component_assembly_line** | Sistema de casing por tier (14 tiers) + receitas ULV/LuV |
| **Cadeia de purificação de água (9 máquinas)** | 18 carcaças + ~15 materiais + sistema wireless de unidades do gtolib; puxa ~10 multiblocos |
| **Advanced Fusion Reactor MK1 (Kuangbiao)** | ~14 blocos endgame + `CrossRecipeMultiblockMachine` + renderer próprio + fusão |

### Predicados/keys/abilities/partes — mínimo necessário para a fase enxuta

- `ISA_MILL_RECIPES`, `ROCKET_ENGINE_FUELS`, `ATOMIZATION_CONDENSATION_RECIPES` (3 recipe types).
- `GTORecipeDataKeys.GRINDBALL` (1 data key).
- `GTOTagPrefix.MILLED` (ou substituto GTCEu) — conjunto de itens + flag de material.
- `BallHatchPartMachine` simplificado + 2 itens de esfera.
- Ability de aceleração (o GTCEu já tem `ACCELERATE_HATCH`).
- **Nada de** `tierBlock`/`glass()`/`machineCasing()`/`CALMAP`/`GLASSMAP`, `TierCasing*`,
  `CrossRecipe*`, catalisador de quark, sistema wireless de unidades.

### GTMThings

No `TurbineMachine` o GTMThings é usado **só para formatação** (`FormatUtil.voltageAmperage`/
`voltageName`), não para rede wireless. A rede wireless do GTOCore vem do GTMThings, mas o GTNA já
tem a sua própria (Nexus Flux Matrix). Como GTMThings **já é dependência do GTNA**, pode ficar; se
quisermos remover, basta um formatador GTNA equivalente.

### Mods externos — resolução fiel sem dependência nova

- **Ars Nouveau:** substituir a string de tooltip `ars_nouveau.locked` por texto GTNA.
- **Deeper and Darker:** só aparece no Precision Assembler (que fica de fora); nenhuma outra máquina.
- **Farmer's Delight:** já tratado como opcional no GTNA (Rich Soil → Mud).
