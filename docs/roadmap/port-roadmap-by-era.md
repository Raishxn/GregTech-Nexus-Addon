# Roadmap de port por eras

Abordagem: **fechar era por era** — completar multiblocos e mecânicas de uma era antes de subir de
tier, para o jogador/criador de modpack ter uma progressão coerente.

Ordem: **Steam → ULV → LV → MV → HV → EV → IV → LuV → ZPM → UV → UHV+**

Legenda: ✅ já no GTNA · 🎯 candidato · 🔁 reimplementar (fonte GPL) · ⛔ fora de escopo

Fontes: **GTNL** (1.20.1, GPL-3.0 — estruturas liberadas com atribuição), **GTOCore** (1.20.1,
LGPLv3 — código/render portável), **TST** (1.7.10, GPL-3.0 — só mecânica).

---

## 🌫️ Era STEAM (Bronze/Steel)

### Já no GTNA ✅
- `large_steam_*`: alloy_smelter, bath, centrifuge, circuit_assembler, compressor, crusher, cutting,
  extractor, forming_press, furnace, hammer, lathe, mixer, ore_washer, solar_boiler, storage_tank,
  thermal_centrifuge.
- `steam_cobbler`, `steam_manufacturer`, `steam_woodcutter`, `stone_superheater`.
- `primitive_distillation_tower`, `infernal_coke_oven`, `leap_forward_one_blast_furnace`,
  `hyper_pressure_reactor`, `compact_hyper_pressure_reactor`, `void_miner_steam_gate_aged`.
- Rede steam wireless (input/output bronze+steel), huge steam buses, infinite steam input bus,
  output-boost steam output bus.

### Lacunas 🎯 (fonte: GTNL, salvo indicado)

**1. Completar a família `large_steam_*` (rápido, consistente):**
- `LargeSteamBending`, `LargeSteamExtruder`, `LargeSteamWiremill`, `LargeSteamSifter`.

**2. Máquinas steam básicas (single/multi):**
- `SteamAssemblerBronze`, `SteamAssemblerSteel` — montagem a vapor.
- `SteamTurbine` — geração de energia a vapor.
- `SteamRockBreaker` — quebra-pedra.
- `SteamLavaMaker` — geração de lava.
- `SteamCarpenter` — madeira.
- `SteamCactusWonder` — cactos.
- `SteamExtractinator` — extração.
- `SteamCracking` — craqueamento a vapor.
- `SteamItemVault` — armazenamento.

**3. Primitivos / early game:**
- `PrimitiveBrickKiln`, `BrickedBlastFurnace`, `FurnaceArray`.

**4. Grandes a vapor:**
- `MegaSolarBoiler` (o GTNA removeu o seu boiler duplicado; o do GTNL é a referência).
- `MegaSteamCompressor`.

**5. Modulares a vapor (destaque modpack-friendly):**
- `SteamElevator` + módulos: `SteamGreenhouseModule`, `SteamApiaryModule`,
  `SteamBeeBreedingModule`, `SteamFlightModule`, `SteamWeatherModule`,
  `SteamMonsterRepellentModule`, `SteamOilDrillModule`, `SteamOreProcessorModule`,
  `SteamEntityCrusherModule`, `SteamBeaconModule`.

**6. Especiais:**
- `SteamGate` / `SteamGateAssembler` (relacionados ao void miner steam gate).
- `SteamFusionReactor`, `HighPressureSteamFusionReactor`.

### Status (2026-09-21)

**Feito ✅**
- `large_steam_bending`, `large_steam_extruder`, `large_steam_wiremill`, `large_steam_sifter` (G-0022).
- `steam_lava_maker` (G-0023).

**Pulado (redundante / fora de escopo) ⛔**
- `BrickedBlastFurnace` = `leap_forward_one_blast_furnace`; `FurnaceArray` = `primitive_stone_furnace`.
- `PrimitiveBrickKiln` (o `PrimitiveStoneFurnace` já faz receitas de fornalha).
- `SteamRockBreaker` = `steam_cobbler`; `SteamCarpenter` = `steam_woodcutter`.
- `SteamCracking` = GTCEu `CRACKER`.
- `SteamAssembler` = singleblock (não é multiblock).
- `SteamTurbine` (decisão do autor: não precisa).
- `MegaSteamCompressor` / `SteamFusionReactor*` (versões maiores de equivalentes).
- `SteamGate` / `SteamGateAssembler` (compatibilidade com o mod **Stargate** — avaliar depois).

**Pendente (genuinamente novo) 🎯**
- `SteamItemVault` (armazenamento de itens; AE2 no GTNL).
- `SteamElevator` + módulos (modular; **precisa do Ad Astra**).

> **Conclusão:** o **núcleo da era Steam está fechado** no GTNA. O que resta é novo-mas-nicho
> (`SteamItemVault`), dependente de mod (`SteamElevator`/Ad Astra, `SteamGate`/Stargate) ou redundante.

### Prioridade sugerida (Steam) — histórico
1. **Completar `large_steam_*`** (bending/extruder/wiremill/sifter) — baixo risco, alto polimento.
2. **`SteamAssembler` (bronze/steel)** + **`SteamTurbine`** — automação e energia iniciais.
3. **`FurnaceArray`** — QoL de fundição.
4. **Geração de recursos:** `SteamRockBreaker`, `SteamLavaMaker`, `SteamCarpenter`,
   `SteamCactusWonder`.
5. **`SteamItemVault`** — armazenamento.
6. **Primitivos:** `PrimitiveBrickKiln`, `BrickedBlastFurnace`.
7. **`SteamCracking`** — petroquímica inicial.
8. **Grandes:** `MegaSolarBoiler`, `MegaSteamCompressor`.
9. **Modulares:** `SteamElevator` + módulos (feature grande; fazer por último na era).
10. **Especiais:** `SteamGate*`, `SteamFusionReactor*`.

---

## ⚡ Era ULV

> **Inventariada em 2026-09-23 (G-0059).** Conclusão: a era ULV é **quase toda coberta** pelo GTCEu
> base e pelos primitivos já portados. Resta **um** candidato genuíno.

### Já disponível ✅
- **GTCEu base:** `primitive_blast_furnace`, `primitive_pump`, `charcoal_pile_igniter`, `coke_oven`,
  `multi_smelter` (o núcleo primitivo do próprio GTCEu).
- **GTNA (primitivo/steam):** `primitive_stone_furnace`, `primitive_distillation_tower`,
  `leap_forward_one_blast_furnace`, `infernal_coke_oven`, `hyper_pressure_reactor`,
  `compact_hyper_pressure_reactor`, `steam_cobbler`, `stone_superheater`, `steam_lava_maker`.

### Lacuna genuína 🎯
- ~~**`brick_kiln`** (GTOCore `MultiBlockH.BRICK_KILN`, LGPLv3)~~ → **portado no G-0060** ✅.
  Era ULV **fechada**; seguir para o port GTO/GTOCore de tier médio/alto.

### Redundante / fora de escopo ⛔
- GTNL `BrickedBlastFurnace` = `leap_forward_one_blast_furnace`; `FurnaceArray` =
  `primitive_stone_furnace`; `PrimitiveBrickKiln` ≈ `primitive_stone_furnace`.
- TST `TST_SteamBasicGenerator` (gerador a vapor; o GTNA cobre com boiler), `TST_LargeSolarBoiler` =
  `large_steam_solar_boiler`, `MegaBrickedBlastFurnace` (redundante).
- GTOCore `large_coke_oven` → era industrial (ver manifesto), não ULV.

### Prioridade sugerida (ULV)
1. **`brick_kiln`** — fecha a era com baixo custo (base no-energy + recipe type + 3 receitas +
   estrutura minúscula). Depois, seguir para o LV / port GTO.

## 🔌 Era LV

> **Inventariada em 2026-09-23 (G-0059).** Conclusão: os multiblocos LV **já vêm com o GTCEu/GCYM**;
> o GTNA cobre os específicos. Não há lacuna LV genuína — o que falta é o **port GTO/GTOCore de tier
> médio/alto** (ver `multiblock-port-manifest.md`).

### Já disponível ✅
- **GTCEu/GCYM (LV..):** `large_chemical_reactor`, `multi_smelter`, `large_maceration_tower`,
  `large_chemical_bath`, `large_centrifuge`, `large_electrolyzer`, `large_mixer`, `large_packer`,
  `large_assembler`, `large_circuit_assembler`, `large_arc_smelter`, `large_engraving_laser`,
  `large_sifting_funnel`, `alloy_blast_smelter`, `large_autoclave`, `large_material_press`,
  `large_brewer`, `large_cutter`, `large_distillery`, `large_extractor`, `large_extruder`,
  `large_solidifier`, `large_wiremill`, `large_bender`, `large_rolling`, `large_forming` (lista
  completa no `multiblock-port-manifest.md`, seção "Not ported as new controllers").
- **GTNA:** `universal_factory`, `industrial_slaughterhouse`, `integrated_ore_processor`,
  `advanced_integrated_ore_processor`.

### Lacuna 🎯 (confirmada pelas quests do GTO — ver `era-mapping-gto-quests.md`)
- Multiblocos GTOCore de **LV**: `liquefaction_furnace`, `lava_furnace`, `generator_array`,
  `tree_growth_simulator`, `thermal_power_pump`, `gas_compressor`.
- Próximo passo: portar um desses (sugestão: **`liquefaction_furnace`** ou **`lava_furnace`**, os mais
  "early" do bloco), e então subir para o MV (`reaction_furnace`, `greenhouse`,
  `crystallization_chamber`, `component_assembler`, `processing_plant`).

## Eras seguintes (MV → UHV+)

**Mapeadas** em `era-mapping-gto-quests.md` (quests do modpack GTO, um capítulo por tier). Use esse
doc para saber **em que era** cada conteúdo entra; a lista de multiblocos a portar continua no
`multiblock-port-manifest.md`. Resumo dos multiblocos GTOCore por era está no topo do doc.

---

## Processo por era (repetir)

1. **Inventariar** os multiblocos/mecânicas da era nas fontes (GTNL/GTOCore/TST).
2. **Marcar** o que o GTNA já tem.
3. **Priorizar** por valor (automação, energia, geração de recursos, modularidade, render).
4. **Portar** um a um: mecânica GTNA-native na base certa, com **atribuição de origem no tooltip**
   (`GTNASources`) e gametest.
5. **Validar** com o gate (`spotlessCheck` + unit + gametest + `runData`) e registrar no ledger.
