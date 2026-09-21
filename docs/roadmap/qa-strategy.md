# 🧪 Estratégia de QA do GTNA

!!! info "Propósito"
    Este documento descreve **como testamos** o GTNA, quais classes de bug cada camada pega (com
    exemplos reais que já custaram iteração) e o que adotamos — por ideia, não por dependência — de
    frameworks de QA da comunidade (GTNH/Horizon-QA). O estado corrente e as pendências ficam no
    `CONTINUITY_LEDGER.md`; aqui fica o *método*.

!!! warning "Este documento é a estratégia, não o estado"
    Contagem de testes, gates verdes e pendências ficam no `CONTINUITY_LEDGER.md`.

---

## 1. Camadas

O princípio é: **cada classe de bug tem um gate que a pega**, e o teste é proporcional ao risco.

| Camada | O que roda | O que pega | O que NÃO pega |
|---|---|---|---|
| **L0 — Lint estático/contrato** | `runUnitTests` com testes que fazem *source scan* | Registro/wiring: predicado de máquina × ability de peça, chave de lang, fonte única de um efeito | Comportamento em runtime |
| **L1 — Unit puro** | `runUnitTests` (main + asserts, padrão GTLCore) | Lógica pura: matemática, matching, políticas, geometria de UI | Qualquer coisa que precise bootar Minecraft |
| **L2 — GameTest de integração** | `runGameTestServer` (servidor dedicado) | Estrutura/registro/API do GTCEu, receita ponta-a-ponta, AE2/CPU, pattern buffer | Client (render, UI, config do Jade), e só cobre cenários escritos |
| **L3 — CI** | `.github/workflows/gradle.yml` | Regressão de build/formatação/testes + banner de gametest | Determinismo de datagen (pendente) |
| **L4 — Manual + observabilidade** | `runClient` + logs de diagnóstico | Render/UI/config de client, e **bugs de integração que precisam de log** | Automatizável por definição |

Regra de ouro do gate: **`runGameTestServer` sai com código 0 mesmo quando o mod falha ao carregar** —
o CI tem que exigir o banner `GAME TESTS COMPLETE`.

---

## 2. Classes de bug que já apareceram (e o que as pega)

| Bug real | Classe | Camada que pega | Exemplo |
|---|---|---|---|
| Wireless Steam Hatch não aceito nas máquinas steam | Predicado por **bloco exato** em vez de **ability** | L0 (`SteamWiringContractTest`) + L2 (gametest de aceitação) | G-0011 |
| Thread Hatch sem máquina de gameplay que aceite | Conteúdo registrado sem consumidor | L0 (lint) + docs-alinhamento | G-0012 |
| Output Boost aplicado **em dobro** (M→M²) | Duas fontes para o mesmo efeito | L0 (`OutputBoostContractTest`) + L2 (gametest completo com teste negativo) | G-0013/G-0014 |
| Tabelas de doc/penalidade desatualizadas | **Doc como contrato** que mente | Revisão humana + doc-alinhamento | G-0013/G-0014 |
| Autocrafting do pattern buffer **só 1 craft** | Integração AE2 (CPU otimizado × `pushInputsToExternalInventory` do pattern) | L4 (log) → depois L1/L2 | sessão 09-21 |
| Servidor cai (NPE) ao empurrar pattern sem item inputs | `null` não tratado num caminho raro | L4 (crash log) → unit testável | sessão 09-21 |
| Zero-energy parallel sempre 1 | **API do framework não faz o esperado** (`ParallelLogic.getMaxByInput`=0) | L4 (log de diagnóstico) | sessão 09-21 |
| UI do pattern buffer vazando 106 px | Geometria sem fonte de verdade | L1 (`PatternBufferLayoutTest`) | G-0009 |
| Jade/config sem tradução (crash no client) | Config do client não coberta por gates server-only | L0 (`ConfigLangKeysTest`) | G-0007/G-0008 |

**Lição central:** os bugs de *wiring* e de *integração* são os que mais escapam. L0 (contratos de
fonte) e L2 (integração real) são os que mais pagam; L4 (log de diagnóstico) é o que **localiza** o
bug quando os outros não alcançam.

---

## 3. Práticas que adotamos

1. **Teste negativo sempre que possível.** Provar que o teste falha com o bug reintroduzido (feito no
   Output Boost e no Steam Wiring). Um teste que nunca falhou não é um teste.
2. **Fonte única de verdade por efeito.** Se dois lugares aplicam o mesmo bônus, um teste de contrato
   (source scan) trava a duplicação.
3. **Contrato de wiring em lint.** "Toda peça com ability X é aceita por ao menos uma máquina", "nenhum
   pattern fixa bloco exato de peça", "toda opção de config tem lang".
4. **Observabilidade barata.** Logs `[GTNA][...]` que dizem o que o código viu (ex.: `visibleItems`,
   `byInput`, `pushPattern slotItems`) transformam um bug de integração em algo diagnosticável sem
   debugger.
5. **Doc como contrato.** Se a doc promete algo, ou o código cumpre, ou a doc é corrigida no mesmo
   commit (G-0011/G-0012/G-0013).
6. **Bug/review externo vira teste de regressão.** Automatizado quando dá; item de checklist manual
   quando é client-only.

---

## 4. Horizon-QA (GTNH) — o que aproveitar e o que não

`GTNewHorizons/Horizon-QA` é um framework **MIT** de teste ponta-a-ponta que **reimplementa a API de
GameTest no Minecraft 1.7.10** (que não tem GameTest nativo), acoplado ao **RetroFuturaGradle** e a
conceitos do GT5-Unofficial (`helper.gtnh()`, EU, manutenção, time-warp).

### Não adotar (o framework)

- O motivo de existir dele é justamente o que o **Forge 1.20.1 já tem nativo**.
- O toolchain (RFG, `--mcJvmArgs`, wand/export próprio) não existe no nosso ModDevGradle.
- Dependência desnecessária e divergência de API.

### Adotar (as ideias) ✅

1. **Asserção por tick com janela (`onEachTick` + `succeedAtTimeout`).** Em vez de checar uma
   invariante uma vez, re-checar a cada tick por N ticks. Pega **falhas transitórias** (ex.: uma
   máquina que forma e desforma, um estado que pisca). Dá para implementar como um helper nosso sobre
   o `GameTestHelper` do Forge.
2. **Teste negativo como categoria explícita.** Ex.: "não forma sem a coil", "não aceita a peça
   errada", "não duplica o boost". Já usamos pontualmente; formalizar.
3. **Relatório JUnit no CI.** Horizon-QA gera XML compatível com JUnit. Nosso `runGameTestServer`
   deve ter relatório equivalente (`build/.../test-results`); vale **converter/parsear** para o CI
   publicar os testes no job.
4. **Autoria de estrutura in-game.** Eles exportam a estrutura com um wand. Nosso caminho barato
   equivalente é **structure block** (exportar `.nbt` de dentro do jogo) em vez de gerar NBT à mão,
   que foi o que fizemos (`empty_12.nbt`).
5. **Feedback visual de falha.** Opcional: destacar a célula que falhou no `patternError` (já
   imprimimos a área); um overlay in-game seria o próximo nível.

### Resumo

> Adota-se a **disciplina** (invariante por tick, teste negativo, relatório no CI, autoria in-game),
> **não** o framework — o Forge 1.20.1 já entrega a base de GameTest.

---

## 5. Backlog de QA (curto)

- [x] Helper `GTNAGameTestUtils.assertEveryTickUntilTimeout(...)` no harness de gametest (ideia
      Horizon-QA) + 2 gametests usando-o (negativo e invariante). Ver G-0016.
- [x] Rodar `runData` no CI e falhar se `git diff` em `src/generated` não estiver vazio (determinismo).
      Ver `.github/workflows/gradle.yml`.
- [ ] Publicar relatório JUnit do `runGameTestServer` no CI.
- [ ] Gametest matriz para as demais máquinas steam (hoje só o alloy smelter).
- [ ] Lint "ability declarada × máquina que aceita × doc que promete" generalizado.
- [ ] Teste de runtime do Output Boost (feito) → replicar padrão para outros bônus.

---

## Referências

- Estado/pendências: `CONTINUITY_LEDGER.md`
- Auditoria técnica: `docs/roadmap/technical-audit-pattern-buffer.md`
- Horizon-QA: <https://github.com/GTNewHorizons/Horizon-QA>
