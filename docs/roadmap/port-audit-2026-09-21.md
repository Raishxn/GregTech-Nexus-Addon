# Auditoria de port — GTNL / TST / GTO (2026-09-21)

Levantamento do que vale portar para o GTNA, com foco em **criadores de modpack** e **jogadores
normais**. As fontes foram baixadas/atualizadas em `~/MineProjects`.

## Fontes

| Fonte | Commit | Base | Multiblocos | Licença | Como portar |
|---|---|---|---|---|---|
| **GTNL** (`ABKQPO/GT-Not-Leisure`) | `52345d2` (2026-09-20) | **1.7.10 / GTNH** | **221** | **GPL-3.0** | **Reimplementar** (GTNA-native). Estruturas concedidas com atribuição (ABKQPO). |
| **TST** (`Nxer/Twist-Space-Technology-Mod`) | `ceaa462` (2026-09-21) | **1.7.10 / GTNH** | ~25 | **GPL-3.0** | **Reimplementar**. |
| **GTOCore** (`GregTech-Odyssey/GTOCore-Main`) | `dc4824d` | **1.20.1 / GTCEu** | **213** | **LGPLv3** | **Portável** (código + assets) com atribuição. |
| **GTO-GregTech-Modern** | atualizado | 1.20.1 | (fork do GTCEu) | LGPL-3.0 | Base. |

> **Correção importante:** GTNL e TST são **1.7.10 (GTNH)** — o port é **reimplementação de mecânica**,
> nunca cópia. O **único** fonte portável direto é o **GTOCore (1.20.1, LGPLv3)**, que é focado em
> endgame.

> **Achado:** o `KerrNewmanHomogenizerRenderer` do GTOCore tem o comentário *"从GTNL特效修改而来，
> 协议: LGPLv3"* (adaptado do GTNL, licenciado **LGPLv3**). Ou seja, **GTO reliencia efeitos do GTNL
> sob LGPLv3** — é uma fonte segura para renderers que no GTNL seriam GPL.

## O que o GTNA já tem

Steam (`large_steam_*`, wireless, huge buses), hatches (Thread/Accelerate/Overclock/Parallel/Output
Boost), `universal_factory`, `primitive_stone_furnace`, `primitive_distillation_tower`,
`industrial_slaughterhouse`, `eye_of_harmony`, `eye_of_wood`, `nexus_molecular_forge`,
`nexus_me_hypercore`, `me_storage`, `dimensionally_transcendent_*`, `hyper_pressure_reactor`,
`infernal_coke_oven`, `leap_forward_one_blast_furnace`, `void_miner_steam_gate_aged`,
`steam_cobbler/manufacturer/woodcutter`, `stone_superheater`, pattern buffer, crafting CPU interface.

## Candidatos — GTNL (mesma versão base; melhor compatibilidade)

### Com render custom (destaque visual)
| Multibloco | Renderer | O que é |
|---|---|---|
| **EternalGregTechWorkshop** | `EternalGregTechWorkshopRenderer` | **modular**: módulos + upgrades (o "GregTech Workshop") |
| **KerrNewmanHomogenizer** | `KerrNewmanHomogenizerRenderer` | homogeneização de alto tier |
| **RealArtificialStar** | `RealArtificialStarRenderer` | gerador (estrela artificial) |
| **AtomicEnergyExcitationPlant** | `AtomicEnergyExcitationPlantRenderer` | planta de energia atômica |
| **HighPerformanceComputationArray** | `HighPerformanceComputationArrayRenderer` | computação de alto desempenho |
| **NanoPhagocytosisPlant** | `NanoPhagocytosisPlantRenderer` | nanofagia |
| **KuangBiaoOneGiantNuclearFusionReactor** | `...Renderer` | fusão gigante |
| **AdvancedHyperNaquadahReactor** | `...Renderer` | reator de naquadah |
| **RocketAssembler** | `RocketAssemblerRenderer` | montagem de foguetes |
| **EyeOfHarmonyInjector** | `EyeOfHarmonyInjectorRenderer` | injetor do Eye of Harmony |

### Mecânicas úteis (sem render custom)
- **Automação/QoL:** `ProcessingArray`, `PCBFactory`, `ResearchCenter`, `QuantumComputer`,
  `DataCenter`, `SupercomputingCenter`, `AssemblerMatrix`, `GrandAssemblyLine`.
- **Energia/geração:** `PhotovoltaicPowerStation`, `SiphonTurbine`, `WhiteNightGenerator`,
  `NuclearReactor`, `NaquadahReactor`.
- **Escala:** `SteamElevator`, `SuperSpaceElevator`, `SpaceAssembler`.
- **Módulos a vapor** (`SteamGreenhouseModule`, `SteamApiaryModule`, `SteamBeeBreedingModule`,
  `SteamFlightModule`, `SteamWeatherModule`, `SteamMonsterRepellentModule`, ...) — multiblocos
  **modulares** que reaproveitam a base steam do GTNA.

## Candidatos — GTO / GTOCore (LGPLv3 — código portável)

- **GodForge** (`GodforgeRenderer`) — forja divina.
- **CosmicCelestialSpireOfConvergence** (`...Renderer`).
- **AdvancedFusionReactor** (`AdvancedFusionReactorRenderer`).
- **DigitalMiner** (`DigitalMinerRenderer`).
- **CelestialCondenser** (`CelestialCondenserRenderer`).
- **DimensionallyTranscendent*** (alguns já temos).
- Máquinas de **mana** (`ManaHeater`, `ManaPipe`) — só se o modpack tiver mana.

## Candidatos — TST (1.7.10 → reimplementar mecânica)

- `CrystallineInfinitier`, `ElvenWorkshop`, `LightningSpire`, `HolySeparator`,
  `IndustrialMagicMatrix`, `IntensifyChemicalDistorter`.
- `MagneticMixer`, `MagneticDrivePressureFormer`, `MagneticDomainConstructor`.
- `MegaBrickedBlastFurnace`, `MoleculeDeconstructor`, `Silksong`, `StellarMaterialSiphon`,
  `SpaceScaler`, `MiracleTop`, `MegaEggGenerator`, `PhysicalFormSwitcher`.
- `LagrangeDysonSpaceStation` + **sistema de estação espacial modular**.
- Hatches: `BufferedEnergyHatch`, `InfiniteWirelessDynamoHatch`, `WirelessData` in/out,
  `AEStorageCellInputBus/Hatch`, `CircuitImprintHatch`, `BloodOrbHatch`.

## Mecânicas transversais que valem a pena

1. **Multiblocos modulares** (EGTW do GTNL, steam modules, estação espacial do TST) — o jogador
   monta a máquina por módulos; é o que mais diferencia para criadores de modpack.
2. **Renders/animações custom** — GTNL tem muitos; GTO também (alguns derivados do GTNL sob LGPLv3).
3. **Steam wireless + módulos a vapor** — a base já existe no GTNA.
4. **AE2 pattern buffer** — já implementado.
5. **Reatores nucleares** (TST/GTNL) e **escala espacial** (elevadores/estações).

## Recomendação (ordem sugerida)

1. **EternalGregTechWorkshop** (modular, render custom) — mecânica única e muito "modpack-friendly".
2. **KerrNewmanHomogenizer** — render **LGPLv3** disponível no GTOCore.
3. **Módulos a vapor** (greenhouse/apiary/...) — alto reaproveitamento da base atual.
4. **ProcessingArray / PCBFactory / ResearchCenter** — QoL de automação que jogadores pedem.
5. **TST** (mágicas/estelares) — só mecânica, reimplementada.
