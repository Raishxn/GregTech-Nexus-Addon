# Handoff — Rede wireless de steam (próxima sessão)

> Sessão dedicada. A rede wireless de steam **ainda está com bugs sérios**. Há muitas peças
> (25 hatches na reprodução do autor) e o saldo da rede fica **0 mB** mesmo com um output hatch
> cheio. Trate como **investigação + robustez**, não só patch.

## Estado atual (o que já foi feito)

Commits relevantes (na `main` local, **sem push**):
- `103572d` — primeira correção: `SteamNetworkData.addSteam` atômico/assinado, simulate→cobra→execute.
- `1e4d89a` — correção do "input hatch mostrando 0": clampa o pull a `min(space, rate, saldo)`; adiciona `/gtna steam` + `SteamNetworkData` registry runtime.
- `822be81` — solar boiler engasgava (buffer/rate) e display 20× errado; hatches passam a mover o **tanque inteiro**; buffers/rates `wirelessSteam` atualizados; Jade do boiler.
- `0643216` — `config.jade.plugin_gtna.solar_boiler_provider` (crash de client) + `JadeLangKeysTest`.

## Reprodução atual (autor, in-game)

- `large_steam_solar_boiler` **41×42** → **312.000 mB por ciclo (1 s)** = 15.600 mB/t.
- **Output hatch** local: mostra `Fluid Amount: 312000` e `Transfer Rate: Unlimited`.
- **Input hatch** local: mostra `Fluid Amount: 20000`, tooltip `20,000/128,000,000 mB`, Jade
  `Working Disabled`.
- `/gtna steam` → **`Wireless steam network (Dev): 0 mB`** e **`Connected wireless steam hatches: 25`**,
  listando vários `Input bronze @ ... | unlimited mB/t`.

### Sintomas a explicar (bugs)
1. **A rede fica em 0 mB** apesar do output hatch cheio (312.000). → o output **não está entrando**
   na rede, ou entra e é consumido/limpo no mesmo tick, ou a chave de rede não bate.
2. O **output hatch não aparece** na lista do `/gtna steam` (só `Input bronze`) — pode não estar
   reportando conexão, ou o filtro da lista está errado.
3. O **input hatch** mostra `Working Disabled` e não enche da rede.
4. **25 hatches** conectados — validar se isso é o esperado (25 inputs?) e se o "team/owner key"
   está certo (a rede é por **UUID de quem colocou**, não por team — GTNL usa team?).

### Evidência direta do `/gtna steam` (autor, 2026-09-22)

```
[GTNA] Wireless steam network (Dev): 0 mB
[GTNA] Connected wireless steam hatches: 25
  - Input bronze @ minecraft:overworld -28, -59, -63 | unlimited mB/t
  ... (24 inputs) ...
  - Output bronze @ minecraft:overworld -52, -60, -65 | unlimited mB/t
```
**Repetido idêntico** em 3 leituras ao longo de ~4 min: a rede **nunca sai de 0**.

Ponto importante: **o output hatch ESTÁ registrado/conectado** (aparece na lista) e o tanque dele
mostra 312.000 mB, mas a rede continua 0. Isso **descarta** "output não conectado" e aponta para:
- o push do output não está sendo chamado (ou falha silenciosamente), **ou**
- o push acontece e algo **zera a rede** no mesmo tick (ex.: reset por tick, ou consumo dos 24 inputs
  que drenam tudo imediatamente — o que explicaria a rede sempre em 0 e os inputs não encherem),
- ou o saldo é mantido **por hatch/por dimensão** e o `getUserSteam` do comando lê outra chave.

Os hatches de input também deveriam estar **consumindo** o que entra; se 24 inputs concorrem, pode
haver corrida/round-robin que esvazia a rede a cada tick. Investigar a **ordem de tick** entre
output/inputs e se o saldo é global ou por hatch.


## Arquivos-chave (GTNA)

- `src/main/java/com/raishxn/gtna/common/machine/multiblock/part/steam/WirelessSteamInputHatch.java`
- `.../WirelessSteamOutputHatch.java`
- `.../network/SteamWirelessNetworkManager.java` e `SteamNetworkData.java` (procurar a pasta exata —
  `git show 1e4d89a --stat` lista os caminhos)
- `src/main/java/com/raishxn/gtna/common/command/GTNACommands.java` (ou equivalente; `/gtna steam`)
- `src/main/java/com/raishxn/gtna/common/machine/multiblock/steam/LargeSteamSolarBoilerMachine.java`
- `src/main/java/com/raishxn/gtna/common/machine/multiblock/module/steamElevator/SteamElevator.java`
  (upkeep dos módulos puxa steam dos hatches da estrutura)
- `src/main/java/com/raishxn/gtna/integration/jade/GTNAJadePlugin.java` + `provider/GTNASolarBoilerProvider.java`
- Config: `wirelessSteam` em `ConfigHolder.java`/`GTNABalance.java` (`bronzeBuffer`, `steelBuffer`,
  `bronzeTransferRate`, `steelTransferRate`).
- `run/config/gtna.yaml` (config local afetado pelos testes).

## Referência (GTNL, 1.7.10/GTNH, GPL-3.0 — reimplementar)

- `~/MineProjects/GTNL/src/main/java/com/science/gtnl/utils/world/steam/SteamWirelessNetworkManager.java`
- `~/MineProjects/GTNL/src/main/java/com/science/gtnl/common/machine/hatch/WirelessSteamEnergyHatch.java`
  (e `WirelessSteamInputHatch`/`OutputHatch` se existirem)
- `~/MineProjects/GTNL/src/main/java/com/science/gtnl/common/machine/multiMachineBase/SteamMultiMachineBase.java`
  (como o steam flui entre hatches/estrutura)

**Alvo:** comportamento equivalente ao GTNL — a rede **move o volume inteiro por tick**, nunca
voida nem duplica, e hatches de input/output da mesma equipe/dono compartilham o pool.

## Como investigar

1. **Ler o caminho inteiro** output→rede→input, tick a tick:
   - `WirelessSteamOutputHatch` (o que drena do tanque e quanto chama `addSteam`),
   - `SteamWirelessNetworkManager` (`addSteam`/`getUserSteam`/`consumeSteamFromGlobalMap`),
   - `SteamNetworkData` (saldo, chave owner/team, reset por tick?),
   - `WirelessSteamInputHatch` (condição de "working"/`isWorkingEnabled`, quando puxa).
2. **Reproduzir com gametest** (melhor caminho): o harness em
   `src/main/java/com/raishxn/gtna/gametest/GTNAMachineGameTests.java` já tem um round-trip de
   wireless steam — estendê-lo para: output cheio → **rede > 0** → input puxa → rede volta a 0,
   em ticks consecutivos; e para **vários hatches** (25). Um teste que hoje passe não basta:
   o bug aparece em jogo com o boiler real.
3. **`/gtna steam`** para inspecionar o saldo/hatches em tempo real; provavelmente é preciso
   **melhorar o diagnóstico** (mostrar se um hatch está "pulling/pushing", o último erro, o tick).
4. Verificar se o **`isWorkingEnabled`/`Working Disabled`** do input hatch é a causa de ele não
   puxar (parece que está desabilitado no Jade).
5. Conferir se o **registry runtime** de conexões (`reportConnection`, TTL 40t) está sendo chamado
   pelo **output** também (o output não aparece na lista).
6. Checar se `getUserSteam` está sendo chamado com a **mesma chave** dos dois lados
   (owner UUID do placer vs team).

## Gate / comandos

```bash
./gradlew spotlessCheck runUnitTests            # 15/15
./gradlew runGameTestServer                     # espera "All 25 required tests passed"
grep -c "Parsing error loading recipe gtna:" run/logs/latest.log   # 0
./gradlew runData                               # determinístico (written: 0 na 2ª)
./gradlew runClient                             # teste manual
```
- O gate **não** pega bugs de rede em jogo → adicionar gametest forte é parte da tarefa.
- Cuidado com o **Jade**: provider novo exige `config.jade.plugin_gtna.<uid>` (há `JadeLangKeysTest`).
- `pt_br.json` é byte-preserving (BOM + CRLF/LF).

---

## Prompt pronto para colar na próxima sessão

> Contexto: no repositório `GregTech-Nexus-Addon` (Minecraft 1.20.1, Forge, GTCEu 7.5.3) a **rede
> wireless de steam** está com bugs sérios. Já houve 3 tentativas de correção (commits `103572d`,
> `1e4d89a`, `822be81`) mas a rede **ainda fica em 0 mB** e o input hatch não enche.
>
> Reprodução do autor: um `large_steam_solar_boiler` 41×42 gerando **312.000 mB/s**; um
> `Wireless Steam Output Hatch` (mostra `Fluid Amount: 312000`) e vários `Wireless Steam Input Hatch`
> (um mostra `20.000/128.000.000 mB` e Jade `Working Disabled`). O comando `/gtna steam` mostra
> **`Wireless steam network (Dev): 0 mB`** e **25 hatches** conectados (só entradas).
>
> Tarefa:
> 1. Ler `docs/roadmap/NEXT-SESSION-WIRELESS-STEAM.md` (handoff com arquivos, referência GTNL e
>    sintomas) e os commits citados.
> 2. Rastrear o fluxo output→rede→input e **encontrar por que a rede fica em 0** e por que o input
>    não enche; comparar com o `SteamWirelessNetworkManager` do GTNL
>    (`~/MineProjects/GTNL`) e deixar **robusto e sem void/duplicação**, movendo o volume inteiro.
> 3. Reimplementar/consertar conforme necessário; melhorar o `/gtna steam` para diagnóstico
>    (estado por hatch, saldo, última operação).
> 4. Adicionar **gametests fortes** (round-trip sob carga, múltiplos hatches) — o gate atual não
>    cobre isso.
> 5. Rodar o gate completo (`spotlessCheck runUnitTests`, `runGameTestServer` = 25/25,
>    `grep -c "Parsing error loading recipe gtna:"` = 0, `runData` determinístico) e **commitar**
>    (sem push). Registrar checkpoint no `CONTINUITY_LEDGER.md`.
>
> Regras: blocos/estruturas do GTNL são GPL-3.0 → reimplementar; lang byte-preserving; não quebrar
> os testes existentes; cuidado com a armadilha do Jade (provider novo exige
> `config.jade.plugin_gtna.<uid>`).
