# Handoff para a próxima sessão — Steam / large steam / tooltips

> ⚠️ **A sessão anterior estourou o contexto várias vezes.** Trate o que está aqui como o **plano
> verificado**, mas **confira no código/fontes antes de agir**. Várias coisas passaram batido.
> As decisões marcadas com 🟢 foram dadas pelo autor.

Fontes locais: `~/MineProjects/GTNL` (1.7.10, clone `52345d2`), `~/MineProjects/TST` (1.7.10),
`~/MineProjects/GTOCore-Main` (1.20.1, LGPLv3), `~/MineProjects/GTO-GregTech-Modern`.

---

## 1. Tooltips — formato de referência (GTNL) 🟢

Padrão do GTNL (`LargeSteamLathe`):
```
Large Steam Lathe
[shift]

Machine Type: Lathe
11% faster than single-block Lathe of the same tier.
Only consumes 95% do normal steam requirement.
Can process up to 8 items at once.
High pressure mode doubles processing speed and steam consumption

──────────────────────────────
Source: Science Not Leisure: 狐涂重工 GT-Odyssey
```

**Decisões do autor:**
- A linha final é **`Source:`** (não `Add Mod:`) — "não tem como adicionar mais de um mod no mesmo
  mod". Reaproveitar `gtna.tooltip.source` (`"Source: %s"`) + `gtna.source.<id>`.
- **Os nomes das máquinas devem ser "rainbow"** como no GTNL (texto animado/arco-íris). Investigar
  `com/science/gtnl/utils/text/AnimatedText.java` e `AnimatedTooltipHandler`.
- Estrutura da tooltip: nome → `[shift]` → `Machine Type: <receita>` → stats (velocidade / eficiência /
  paralelo / **high pressure**) → separador → `Source: ...`.

---

## 2. Blocos faltantes / texturas 🟢

- **`IndustrialSteamCasing`** e **`AdvancedIndustrialSteamCasing`** (`GTNLCasings` metaCasing02 1/2),
  **bronze machine frame** (`sBlockFrames`), **column** (`metaBlockColumn`), pipe casings, casings tiered.
  > ✅ **Feito no G-0027:** `industrial_steam_casing` e `advanced_industrial_steam_casing` criados com
  > texturas do Modernity + receitas (crafting e SteamManufacturer) e já reconhecidos pelo predicado
  > de tier. Os frames/columns (`metaBlockColumn` 4/5) mapeiam para `frameGt(Bronze/Steel)` (§6).
  > Pipe/gearbox/frame/glass/material block já existem no GTCEu.
- **Texturas:** importar do **Modernity GTNH** (`ModernityGTNH/Modernity-GTNH`) — as texturas base são
  da versão antiga; o Modernity é o pack para versões novas. **Modernity é CC BY-NC-SA 4.0** → manter
  atribuição e o aviso não-comercial (ver `THIRD_PARTY_NOTICES.md`).
- Observação: `grep IndustrialSteamCasing` **não achou** uso direto nas `LargeSteam*` (elas usam
  `ofBlocksTiered`). **Confirmar com o autor** onde ele entra (aparência do controller? outras máquinas?).
- As **`large_steam_*` já portadas** provavelmente **não têm os blocos certos** (ex.: `large_steam_lathe`
  deveria usar bronze machine frame + industrial steam casing) → **re-portar as estruturas**.

---

## 3. As 21 large steam machines (lista do autor) 🟢

| # | Máquina (autor) | GTNA atual | Ação |
|---|---|---|---|
| 1 | large steam lathe | `large_steam_lathe` | re-portar estrutura |
| 2 | large steam cutter | `large_steam_cutting` | re-portar |
| 3 | large steam bender | `large_steam_bending` | feito (G-0025) — conferir/re-portar |
| 4 | large steam wiremill | `large_steam_wiremill` | feito (G-0025) — conferir/re-portar |
| 5 | large steam mixer | `large_steam_mixer` | re-portar |
| 6 | **large bronze boiler** | ❌ não existe | **portar (novo)** |
| 7 | large steam sifter | `large_steam_sifter` | feito (G-0025) — conferir/re-portar |
| 8 | large steam compressor | `large_steam_compressor` | re-portar |
| 9 | large steam forging hammer | `large_steam_hammer` | re-portar |
| 10 | large steam centrifuge | `large_steam_centrifuge` | re-portar |
| 11 | large steam extruder | `large_steam_extruder` | feito (G-0025) — conferir/re-portar |
| 12 | large steam ore washer | `large_steam_ore_washer` | re-portar |
| 13 | large steam extractor | `large_steam_extractor` | re-portar |
| 14 | large steam chemical bath | `large_steam_bath` | re-portar |
| 15 | large steam thermal centrifuge | `large_steam_thermal_centrifuge` | re-portar |
| 16 | large steam alloy smelter | `large_steam_alloy_smelter` | re-portar |
| 17 | large steam furnace | `large_steam_furnace` | re-portar (🟢 **usamos sim**) |
| 18 | large steam crusher | `large_steam_crusher` | re-portar |
| 19 | large steam circuit assembler | `large_steam_circuit_assembler` | re-portar |
| 20 | large steam forming press | `large_steam_forming_press` | re-portar |
| 21 | (o autor contou 21; **confirmar** a 21ª) | — | — |

> GTNL tem 19 classes `LargeSteam*`; a lista do autor inclui **large bronze boiler** (classe
> `LargeBoiler` em `structuralReconstructionPlan/`) e conta ~21. **Confirmar** a lista final.
> 🟢 **Todas as 21 são usadas** (inclusive o `large steam furnace`).

---

## 4. High pressure mode 🟢 (TODAS têm)

**Toda `large steam` do GTNL tem "high pressure mode": dobra a velocidade de processamento E dobra o
consumo de steam.**

No GTNL: `SteamMultiMachineBase.isHighPressure()` → `tierMachine == 2` (ou seja, **o tier vem dos
casings da estrutura**: bronze = normal, **steel = high pressure**). As tooltips citam
`gtnl.machine.steam.high_pressure.tooltip` = "High pressure mode doubles processing speed and steam
consumption".

**GTNA hoje NÃO tem** high pressure mode (`grep` em `SteamMultiMachineBase`/`steam/` não achou nada).
→ Implementar: detectar o tier dos casings (bronze vs steel) e, se steel, aplicar **×2 velocidade** e
**×2 consumo de steam**; refletir na tooltip.

> ✅ **Feito no G-0026.** `SteamMultiMachineBase` lê o tier do match context
> (`SteamMultiMachineBase.casing()` aceita bronze **ou** steel e grava o menor tier), aplica duração
> ×0.5 + steam ×2 (`getEffectiveConversionRate()`), skin de aço na GUI, linha no display e tooltip
> compartilhada (`GTNASteamTooltips`). Ligado no `steamCasing()` (6 máquinas novas) e no
> `large_steam_alloy_smelter`; gametest `steelCasingEnablesHighPressure` (24/24). As **demais**
> `large_steam_*` ainda usam predicado de bronze inline → ganham o tier no **passo 3** (§11.3).

---

## 5. Convenção de orientação de estrutura (CRÍTICO)

Erro cometido: reusei **um** pattern para todas e depois deixei **de cabeça pra baixo** com o
**controller virado para dentro**.

Regras do `FactoryBlockPattern` (GTCEu, default `charDir=LEFT, stringDir=UP, aisleDir=FRONT`):
1. **`string 0` = base** da estrutura (a 1ª string é o **fundo**, não o topo).
2. O **controller (`~`) deve ficar na ÚLTIMA aisle** (aisle 0 = +Z), senão a face aponta para dentro.
3. Referência: `duration_tester` e `large_steam_cutting` (controller na última aisle).

Ao converter do GTNL: **inverter a ordem das linhas** e **a ordem das aisles**.

---

## 6. Como decodificar as estruturas do GTNL (`.mbs` "MBS1")

**GTNL é 1.7.10 (GTNH)** e guarda as estruturas em **`.mbs`** (binário), **não** em `.mb`.
Os `.mb` do GTNA são cópias legadas e **não são lidos em runtime**.

Formato `MBS1` (`StructureFileCodec.readBinary`): `"MBS1" | int stringCount | (int len + bytes)* |
int rowCount | (int colCount + int stringIndex[colCount])*`. Leitura: `grid[y][z]` = string de `x` chars.

Script (rodar de `~/MineProjects`):
```python
import struct as st
def decode(path):
    d=open(path,'rb').read(); assert d[:4]==b'MBS1'; off=4
    def ri():
        nonlocal off
        v=st.unpack_from('>i',d,off)[0]; off+=4; return v
    n=ri(); table=[]
    for _ in range(n):
        ln=ri(); table.append(d[off:off+ln].decode()); off+=ln
    rows=ri(); g=[]
    for _ in range(rows):
        cols=ri(); g.append([table[ri()] for _ in range(cols)])
    return g
g=decode("GTNL/src/main/resources/assets/sciencenotleisure/multiblock/<nome>.mbs")[::-1]  # inverter linhas
for z in range(len(g[0])-1,-1,-1):  # inverter aisles
    print("                .aisle(" + ", ".join('"'+g[y][z]+'"' for y in range(len(g))) + ")")
```
Mapeamento: quase tudo `ofBlocksTiered` (casings por tier) + `chainAllGlasses`. No GTNA: casings tiered →
`CASING_BRONZE_BRICKS`/`CASING_STEEL_SOLID`; pipe → `CASING_BRONZE_PIPE`; gear → `CASING_BRONZE_GEARBOX`;
frame → `frameGt(Bronze/Steel)`; glass → `Blocks.GLASS`. **O tier dos casings é o que define o high
pressure mode** (§4).

---

## 7. Steam Manufacturer (para usar as mesmas receitas do GTNL) 🟢

O autor pediu para **portar o `SteamManufacturer`** para que as receitas do GTNL funcionem iguais.
No GTNL: `com/science/gtnl/common/machine/multiblock/steam/SteamManufacturer.java` + receitas em
`com/science/gtnl/common/recipe/gtnl/SteamManufacturerRecipes.java`. O GTNA já tem um
`steam_manufacturer` (`SteamManufacturer`) — **comparar** e alinhar receitas/mecânica.

---

## 8. `SteamItemVault` + `VaultPortHatch` 🟢

- GTNL: `SteamItemVault`; GTNA: `steam_item_vault` (**confirmar o nome final**).
- Mecânica especial: aceita um **`VaultPortHatch`** (`common/machine/hatch/VaultPortHatch.java`), um
  hatch **AE2** (`AENetworkProxy`) que dá acesso de rede ao vault, **disponível só a partir do HV**
  (confirmar o gate exato). O GTNA hoje tem só o storage, **sem** o vault port.

---

## 9. Licenças

| Fonte | Base | Licença | Como portar |
|---|---|---|---|
| GTNL | **1.7.10/GTNH** | **GPL-3.0** | **Reimplementar** (GTNA-native). Estruturas: permissão do ABKQPO com atribuição. |
| TST | **1.7.10/GTNH** | GPL-3.0 | Reimplementar. |
| GTOCore | 1.20.1 | **LGPLv3** | Portável com atribuição. |
| GTOEPP | — | ARR | **Permissão concedida** (Alcox). |
| Modernity-GTNH | — | **CC BY-NC-SA 4.0** | Texturas com atribuição; GTNA fica não-comercial. |

Detalhe em `docs/roadmap/port-audit-2026-09-21.md` e `THIRD_PARTY_NOTICES.md`.

---

## 10. O que já foi feito (verificar!)

- **6 máquinas steam novas** (G-0022..G-0025): `large_steam_bending/extruder/wiremill/sifter`,
  `steam_lava_maker`, `steam_item_vault`.
- Estruturas próprias para as 6 (G-0025), decodificadas do GTNL — **conferir orientação in-game**.
- Tooltips de origem (`GTNASources`), `THIRD_PARTY_NOTICES.md`, lints de QA, **23 gametests / 14 unit tests**.
- ⚠️ **Suspeita:** as `large_steam_*` antigas têm estruturas **inventadas** e faltam blocos (§2);
  **não confiar** sem revisar contra o GTNL.

---

## 11. Ordem sugerida para a próxima sessão

1. **Criar os blocos faltantes** (Industrial/Advanced Industrial Steam Casing, bronze frame, column) —
   textura do **Modernity GTNH** + casing GTNA novo; atribuição no `THIRD_PARTY_NOTICES.md`.
   ✅ **Feito (G-0027)** para os dois Industrial Steam Casings; frames → `frameGt` (§6).
2. ~~**High pressure mode** na base steam (`SteamMultiMachineBase`): tier dos casings → ×2 velocidade e
   ×2 steam (§4), + tooltip.~~ ✅ **Feito (G-0026)** — mecanismo + 7 máquinas; as demais no passo 3.
3. **Re-portar as estruturas das `large_steam_*`** (as 21, incluindo o furnace) a partir dos `.mbs` do
   GTNL, com §5/§6 e o mapeamento de blocos certo — **e trocar o predicado de casing inline pelo
   `SteamMultiMachineBase.casing()`** (aceita bronze/steel) e adicionar o id em `GTNASteamTooltips`.
   ✅ **Feito (G-0028)** para as 21; falta **validar in-game** as que não têm gametest de formação.
4. **Portar o `LargeBronzeBoiler`** (novo) e alinhar o **`SteamManufacturer`** (§7).
5. **Padronizar as tooltips** no estilo GTNL, com **`Source:`** e **nomes rainbow** (§1).
6. **`VaultPortHatch`** (AE2, HV) para o `steam_item_vault` (§8).
7. Rodar o gate (`spotlessCheck` + `runUnitTests` + `runGameTestServer` + `runData`) e **validar in-game**.

---

## 12. Comandos / ambiente

- Gate: `./gradlew spotlessCheck runUnitTests` e `./gradlew runGameTestServer` (precisa `run/eula.txt`
  com `eula=true`); `./gradlew runData`.
- Client: `./gradlew runClient` (Ad Astra e Stargate **já estão** no classpath de dev).
- O `runGameTestServer` sai 0 mesmo se o mod não carregar — confira o banner `GAME TESTS COMPLETE`.
- Flakiness conhecida: `runningSecondRecipeTypeMirrorsControllerMode` (~1/7).
