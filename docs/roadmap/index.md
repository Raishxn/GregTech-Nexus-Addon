# 📋 Roadmap & Checklist

Acompanhe o progresso de desenvolvimento do GregTech Nexus Addon em tempo real.

---

## ✅ v0.1.5 — Versão Atual (Lançada)

### Funcionalidades Completas

- [x] **Wireless Steam Network** — Hatches de input/output sem fio (Bronze + Steel)
- [x] **Huge Steam Buses** — Input/Output buses com mais slots
- [x] **Large Steam Furnace** — 9x velocidade, 128 paralelos, 50% eficiência
- [x] **Large Steam Crusher** — Triturador em massa a vapor
- [x] **Large Steam Alloy Smelter** — 64 paralelos, 43% mais rápido
- [x] **Mega Solar Boiler** — Vapor infinito via Sol (10,000 L/s por célula)
- [x] **Steam Cobbler** — Gerador de pedra, 16 paralelos
- [x] **Stone Superheater** — Derrete pedra em fluidos, 32 paralelos
- [x] **Steam Manufacturer** — Assembler hidráulico a vapor
- [x] **Steam Woodcutter** — Corte automático de madeira
- [x] **Void Miner (Steam)** — Mineração do vazio a vapor
- [x] **Infernal Coke Oven** — Forno de coque intensificado
- [x] **Hyper Pressure Reactor** — Reator de alta pressão
- [x] **Leap Forward Blast Furnace** — Alto forno avançado
- [x] **Industrial Slaughterhouse** — Matadouro industrial (elétrico)
- [x] **Thread Hatch** — ZPM→MAX, receitas simultâneas diferentes
- [x] **Accelerate Hatch** — LV→MAX, reduz duração
- [x] **Overclock Hatch** — UV→MAX, multiplicador de OC
- [x] **Advanced Parallel Hatch** — UHV→OpV, 1K→262K paralelos
- [x] **Duration Tester** — Multibloco de teste para hatches
- [x] **Material: Stronze** — Liga Bronze+Steel (1:2)
- [x] **Material: Breel** — Liga Bronze+Steel (2:1)
- [x] **Material: Clay Compound** — Material primitivo
- [x] **Material: Echoite** — Elemento customizado (Ec)
- [x] **Material: Compressed Steam** — Sólido de vapor
- [x] **Fluido: Dense Supercritical Steam**
- [x] **Fluido: Super Heated Steam**
- [x] **Fluido: Insanely Supercritical Steam**
- [x] **Sistema Hidráulico** — 10 componentes (Motor, Piston, Pump, Arm, etc.)
- [x] **Receitas Hidráulicas** — Tipo de receita customizado
- [x] **Receitas Woodcutter** — Tipo de receita customizado
- [x] **Receitas Superheater** — Tipo de receita customizado
- [x] **Receitas Infernal Coke** — Tipo de receita customizado
- [x] **Receitas High Pressure** — Tipo de receita customizado
- [x] **Receitas Slaughterhouse** — Tipo de receita customizado
- [x] **Vajra** — Ferramenta universal (Echoite)
- [x] **Structure Detector** — Ferramenta de detecção de estrutura
- [x] **Jade Integration** — HUD info para multiblocos
- [x] **Blocos Customizados** — 12 tipos de casing

---

## 🔄 v0.2.0 — Em Desenvolvimento

### ⚡ Nexus Flux Matrix — Wireless Energy System (**PRIORIDADE**)

- [ ] **Nexus Flux Matrix** — Multibloco central de armazenamento wireless (3×7×7 a 31×7×7)
- [ ] **Nexus Capacitor Blocks** (14 tiers, LV→MAX) — Blocos de armazenamento interno
- [ ] **NexusEnergyNetwork** — SavedData per-player com Int128
- [ ] **Wireless Energy Hatch** — 11 amperagens (1A→1048576A), todos os tiers
- [ ] **Wireless Dynamo Hatch** — 11 amperagens, todos os tiers
- [ ] **Wireless Receiver Cover** — Para singleblocks (1A, 4A, 16A, 64A)
- [ ] **Wireless Transmitter Cover** — Para geradores singleblock
- [ ] **Nexus Linker** — Item de vinculação (Shift+Click Controller → Click Hatch)
- [ ] **Quantum Network Terminal** — GUI de monitoramento completo
- [ ] **Safe Mode** — Proteção auto. em <10% → desliga output → reativa em 25%
- [ ] **Alertas de bateria** — Chat msgs em 75%, 50%, 25%, <10%
- [ ] **Cross-dimension** — Suporte inter-dimensional (ZPM+)
- [ ] **Escalabilidade Quadrática** — `Capacity × Count / 2` com Int128
- [ ] PRD completo: [prd_05_nexus_flux_matrix.md](../prd/prd_05_nexus_flux_matrix.md)

### Novos Multiblocos (Planejados)

- [ ] **Forge of the Iron Crown** — EBF movido a vapor (≤1800K)
- [ ] **Steam Pressure Crystallizer** — Autoclave turbinada a vapor
- [ ] **Pneumatic Ore Washer** — Ore Washer + Chemical Bath + Centrifuge
- [ ] **Steam Distillation Column** — Destilação a vapor
- [ ] **Hydraulic Press Complex** — Bender+Compressor+Hammer+Press+Extruder

### Pattern Buffer System ✅ **Implementado** (multi-modo funcional)

- [x] **ME Mini Pattern Buffer** (9 slots) — LuV
- [x] **ME Pattern Buffer** (21 slots) — ZPM
- [x] **ME Advanced Pattern Buffer** (32 slots) — UV
- [x] **ME Ultimate Pattern Buffer** (72 slots) — UHV
- [x] GUI completa com configuração por slot (item/fluido/circuito/modo) — página de duas colunas
      (`352x248`): grade de patterns à esquerda e painel de configuração do slot **docado** à
      direita, mais o side tab **Buffer Tools** (cache de receitas + ferramentas de circuito dos
      patterns). Geometria centralizada em `PatternBufferLayout` e guardada pelo unit test
      `PatternBufferLayoutTest`.
- [x] Integração AE2 (pattern push/pull via rede ME)
- [x] **Troca automática de modo do multibloco** conforme o recipe type do pattern
      (espelho de `activeRecipeType` com a fórmula oficial do GTM — ver
      [auditoria técnica](technical-audit-pattern-buffer.md)). **Coberto por gametest end-to-end**
      desde 2026-09-20: um `duration_tester` formado que roda uma receita do segundo tipo vira o
      modo sozinho (`runningSecondRecipeTypeMirrorsControllerMode`).
- [x] **Troca automática de modo também em multiblocos do GTCEu/GCYM** (Large Cutter entre cutter
      e lathe, Multi Smelter entre furnace e alloy smelter, etc.): o pattern buffer dirige o modo
      quando a máquina está **ociosa**, então nunca interrompe produção. Coberto pelo gametest
      `patternBufferDrivesBaseMachineMode`; desligável em `ConfigHolder` (`bufferDrivenMachineMode`,
      ligado por padrão).
- [x] **Filtro de modo por buffer** — seletor na UI do próprio buffer (paridade com o
      `MultiMachineModeFancyConfigurator` do GTOCore) que limita o buffer a um recipe type.
- [x] **Output ME sem perda** — sobra de output vai para um buffer persistido e é drenada por um
      `IGridTickable` com frequência adaptativa, em vez de ser voidada.
- [x] Itens de upgrade entre tiers (preservam padrões e configuração)
- [x] Fidelidade 1:1 com GTOCore/GTLCore — **tabela de gap toda verde**; as divergências restantes
      são conscientes e documentadas em [gap de fidelidade](pattern-buffer-fidelity-gap.md).

### Novos Elementos

- [ ] **Nexium (Nx)** — Elemento ponte vapor↔eletricidade
- [ ] **Steamforged (Sf)** — Metal que auto-repara
- [ ] **Crystallium (Cr★)** — Amplificador de sinais
- [ ] **Voidessence (Vd)** — Extraído do Void
- [ ] **Hazardium (Hz)** — Fonte de energia instável
- [ ] **Gravitium (Gv)** — Controle gravitacional
- [ ] **Thermium (Th★)** — Superconduta calor

### Novas Ligas

- [ ] **Pressurized Bronze** — Bronze+CompressedSteam
- [ ] **Reinforced Stronze** — Stronze+Nexium
- [ ] **Crystalline Alloy** — Crystallium+Stronze
- [ ] **Thermosteel** — Steel+Thermium
- [ ] **Voidsteel** — Steel+Voidessence
- [ ] **Nexus Compound** — Nexium+Echoite+Stronze
- [ ] **Steam-Hardened Iron** — Iron+CompressedSteam
- [ ] **Pneumatic Steel** — Steel+CompressedSteam+Copper

### KubeJS Integration

- [x] Plugin KubeJS registrado (`GTNAKubeJSPlugin`)
- [x] GTNAPartAbility exposto para scripts
- [x] WorkableElectricMultipleRecipesMachine via KubeJS (`gtna:multiple_recipes`)
- [ ] Documentação e exemplos completos
- [ ] Script de teste validado

### Melhorias Gerais

- [ ] Smart Tooltips em todos os multiblocos
- [ ] Wireless Steam Dashboard GUI
- [ ] Estrutura de pastas refatorada
- [ ] Novos fluidos (Crystal Coolant, Hydraulic Fluid, etc.)
- [ ] JEI/EMI custom pages

---

## 🔮 v0.3.0+ — Futuro

### Era Hidráulica

- [ ] **Nexus Reactor Core** — Conversor Steam→EU
- [ ] **Industrial Electrolyzer Complex**
- [ ] Novos casings e blocos da era
- [ ] Receitas de transição

### Era Supercrítica

- [ ] Multiblocks de processamento massivo
- [ ] Novos fluidos supercríticos
- [ ] Gate challenges

### Infraestrutura

- [ ] Wiki completa (MkDocs + GitHub Pages)
- [x] **Automated testing** — 6 unit tests (`main()` + asserts, padrão GTLCore) e **4 gametests**
      (`@GameTest` em servidor dedicado real), todos no gate do CI.
- [x] **CI/CD pipeline** — `gradle.yml` roda `build`, `spotlessCheck`, `runUnitTests` e
      `runGameTestServer` (com guarda do banner `GAME TESTS COMPLETE`); `docs.yml` publica o MkDocs.
- [ ] Community playtest program

!!! tip "Auditoria Técnica"
    A auditoria estrutural completa do mod (pacotes, registries, mixins, testes, CI) + o plano de execução detalhado do **pattern buffer multi-modo** estão em [`technical-audit-pattern-buffer.md`](technical-audit-pattern-buffer.md). O escopo mecânico do port GTOCore/GTLAdditions segue em [`multiblock-port-manifest.md`](multiblock-port-manifest.md).

!!! info "Retomar o trabalho numa sessão nova"
    O estado atual, as pendências priorizadas e as **notas de campo** (os erros já pagos, para não
    redescobrir) ficam no `CONTINUITY_LEDGER.md`, na raiz do repositório. Comece por lá: ele diz o
    que está verde, o que falta e quais comandos rodar antes de mexer em qualquer coisa.

---

## 📊 Estatísticas do Projeto

| Métrica | Valor |
|---------|-------|
| **Multiblocos** | 14 implementados, 5 planejados |
| **Hatches Customizadas** | 4 tipos (Thread, Accelerate, Overclock, Adv. Parallel) |
| **Materiais** | 5 ligas + 1 elemento + 3 fluidos |
| **Recipe Types** | 6 tipos customizados |
| **Itens Hidráulicos** | 10 componentes |
| **Blocos** | 12 casings customizados |

---

!!! info "Contribua!"
    Quer ajudar no desenvolvimento? Veja o [Guia de Contribuição](../development/contributing.md) ou entre no nosso [Discord](https://discord.gg/d3qHufwRxb)!
