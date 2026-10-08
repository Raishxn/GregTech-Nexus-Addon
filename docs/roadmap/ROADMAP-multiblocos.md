# Roadmap do GTNA: multiblocos, mecânicas e suporte (2026-10-07)

Documento de referência para as próximas sessões. Complementa:
- `auditoria-multiblocos.md`: veredito e contagem de blocos faltando por máquina;
- `forge-of-gods-material-chain.md`: cadeia do Forge of Gods;
- `CONTINUITY_LEDGER.md`: checkpoints, o último é o G-0199.

## 0. Princípios (decisões do autor)

- **O que o GTNA é:** um mod de multiblocos **e** mecânicas (wireless, módulos, EOH, Forge of Gods, hatches de paralelo/thread…). Tudo tem API para KubeJS, para outros criadores montarem receitas e linhas.
- **Receitas:** o GTNA traz um *default* funcional. O balanço e as linhas são do modpack (GT:IA) e não precisam ser 1:1 com o GTNH.
- **Máquinas "atalho" também são portadas.** No GT:IA elas são craftáveis, mas extremamente caras, na proporção da facilidade que dão.
- **O que fica de fora:**
  - duplicatas exatas, ou seja, a mesma máquina vinda de duas fontes (porta-se a melhor);
  - máquinas inviáveis pelo custo, avaliadas caso a caso.
- **Research:** o Research Center / Analysis Center do GTO serve ao **research padrão do GTCEu** (station research, que o GT:IA usa). Não é a árvore de pesquisa removida do GT:IA (D-226): **entra no port**.
- **Fonte:** GTO/GTL (1.20, GTCEu) primeiro. O GTNA já lê os padrões `.mbs` do GTO (`GTOCompressedPatternReader`, `pattern/gto/`) e `.mbs` do GTNA (`GTNAMultiBlockFileReader`). Máquinas do GTNH 1.7.10 / TST / GTNL / 123Tech exigem converter o StructureLib por script (exemplo: `tools/convert_dtpf_structure.py`, `tools/convert_nano_forge_structure.py`).
- **Texturas:** originais (GTO para blocos do GTO; Modernity-GTNH ou GT5U para blocos do GTNH). Nunca chamar a arte do autor de placeholder.
- **Commits:** só depois que o autor testar e aprovar; sem atribuição ao Claude.

## 1. Checklist padrão de port de cada multibloco

1. Ler a classe original: estrutura, letras/blocos, hatches, mecânicas e tipos de receita.
2. Estrutura:
   - GTO: copiar o `.mbs` para `pattern/gto/` e mapear as letras.
   - 1.7.10: converter por script para `.mbs` e registrar a transformação usada (fatias/linhas invertidas).
3. Blocos que faltam:
   - portar com a textura original (CTM via `ldlib.connection` no `.mcmeta`);
   - usar `createCasingBlock` / `createTwoLayerCasingBlock` / `createGlassCasingBlock`.
4. Classe da máquina:
   - reaproveitar uma classe do GTCEu/GTNA sempre que der (`WorkableElectricMultiblockMachine`, `CoilWorkable…`, máquinas de módulo ou de paralelo do GTNA);
   - mecânica especial vai numa classe própria, com estado `@Persisted`.
5. Tipo de receita em `GTNARecipeType`:
   - tamanho de IO e chaves de dados (`addDataInfo`);
   - pode ficar vazio; o pack preenche.
6. KubeJS:
   - o tipo de receita é exposto automaticamente pelo GTCEu;
   - mecânica especial precisa de evento ou binding em `GTNAStartupEvents` (modelo: `eyeOfHarmony` do EOH);
   - módulos usam `GTNASubPatterns` (aceita registro via KubeJS).
7. Receita do controlador + receitas default (o pack pode trocar).
8. Lang EN (`GTNALangProvider`) e PT (`pt_br.json`); tooltips.
9. GameTest:
   - a estrutura forma pelo preview;
   - a mecânica especial tem teste de cálculo.
10. Gate:
   - comandos: `./gradlew runData` e depois `spotlessCheck compileJava runUnitTests runGameTestServer`;
   - `gtna-recipe-coverage.txt` / `gtna-blocked-graph.json` sem becos sem saída novos;
   - depois `THIRD_PARTY_NOTICES`, ledger e este roadmap.

## 2. Pendências do que já foi feito (fazer antes)

| # | Item | Detalhe |
|---|---|---|
| P1 | **QA do autor no cliente** | TPM (wireless), Nano Forge tiers 1–3 (módulos + nanite no bus), DTPF, Advanced Fusion, progressão do Forge of Gods, texturas novas (animadas/CTM), nomes em PT |
| P2 | **Commit** | Tudo de G-0184 a G-0199 está sem commit, aguardando aprovação do autor |
| P3 | **Cache do datagen** | Registrate e `GTNALangProvider` escrevem o mesmo `en_us.json`; o HashCache pula o provider do GTNA e o `ConfigLangKeysTest` falha (115 chaves). Workaround manual: apagar a entrada `Languages: en_us` em `src/generated/resources/.cache`. Correção permanente proposta no `build.gradle` (`doFirst` no `runData`), ainda não aplicada (o autor interrompeu) |
| P4 | Teste instável | `eye_of_harmony_normal_speed_circuit_preview…`: corrida no check assíncrono de padrão do GTCEu (HashMap). Investigar ou tornar o teste síncrono |
| P5 | Receitas provisórias | Singularity Shielding Casing e Medial/Central Graviton Flow Modulator usam Assembly Line até portar o BEC (§3.1) |
| P6 | Itens sem arte original | Eternal Singularity (sem textura no Modernity; continua Gravi Stars). Possível item futuro |

## 3. Roadmap de multiblocos

Colunas: **Fonte** · **Faltam** (blocos do GTO que o GTNA não tem; "1.7" = exige conversão) · **Mecânica** (o que vai além de "rodar receita").

### 3.1 Fase A: necessário e mecânicas do endgame existente

| Máquina | Fonte | Faltam | Mecânica / observação |
|---|---|---|---|
| **Multiblocos BEC** (Condensate Assembler + geradores/contenção) | GTNH (GT5U `BECRecipes`, tectech) | 1.7 | Sistema de condensados (famílias de condensados "entangled") e tiers de nanite por receita. Destrava Shielding Casing, Graviton T2/T3, a **Transdimensional Alignment Matrix** e a **Convergence** do DTPF |
| Convergence do DTPF | GTNH `MTEPlasmaForge` | — | Overclock perfeito pagando catalisador extra; precisa da TAM (sai do BEC) |
| Nano Forge **tier 4** | GTNH `MTENanoForge` | 1.7 | Estrutura diferente (base tier 4 + render), 4 casings `NANO_FORGE_CASING_1..4` (Modernity), frames de MagMatter; paralelos drenando MagMatter, duração ×0,9999^paralelo, consumo de nanite; MagMatter Nanites |
| Hyperdimensional Plasma Fusion Core ("DTPF MK2") | GTO | 3 | Reaproveita a classe do DTPF; DTPF + Stellar Forge |

### 3.2 Fase B: barato e de alto valor (GTO/GTL, poucos blocos faltando)

| Máquina | Fonte | Faltam | Observação |
|---|---|---|---|
| Cracker Hub | GTO | 2 | Cracking + pirólise (cobre o Mega Oil Cracker do GTNH/TST e o Inferno Cleft do GTLAdditions) |
| Mega Distillation | GTL `mega_distillery` | ~0–2 | |
| Desulfurizer | GTO | 5 | Padrão inline (19 aisles) |
| Supercomputing Center | GTO | 0 | Classe própria; CWU de endgame (cobre o Astral Computing Array do TST) |
| Research / Analysis Center | GTO `analysis_and_research_center` | 0 | Classe própria; research padrão do GTCEu |
| Data Center | GTO | 1 | `DataBankMachine` |
| Giant Flotation Tank | GTO | 2 | Mesmo tipo de receita que a célula de flotação do GTNA já usa |
| Dimensional Focus Engraving Array | GTO | 0 | Tem renderer próprio |
| Phase Change Cube | GTO | 1 | Extractor + solidifier |
| Large Incubator | GTO | 0 | `IncubatorMachine` (o GTNA já tem o Incubator) |
| Mega Vacuum Drying | GTO | 0 | (cobre o Giant Vacuum Drying do TST) |
| Mega Brewer | GTO | 0 | |
| Engraving Laser Plant | GTO | 1 | |
| Large Pyrolyse Oven | GTO/GTL | 0 | |
| Infinity Fluid Drilling Rig | GTO | 0 | |
| Star Ultimate Material Forge | GTO | 1 | |
| Reaction Furnace | GTO | 0 | Inline |
| PCB Factory | GTO | 0 | `PCBFactoryMachine` (classe própria) |
| Tiers da ISA Mill / Isa Factory | GTO `isa_mill` + 123Tech `OTEMegaIsaFactory` | 0 / 1.7 | O GTNA já tem Isa Mill; a fábrica integrada é "atalho" (cara no pack) |

### 3.3 Fase C: médio (GTO, 2–5 blocos faltando ou classe própria)

| Máquina | Fonte | Faltam |
|---|---|---|
| Crystal Builder | GTO | 3 |
| Holy Separator | GTO/GTL (também no TST) | 3 |
| High Energy Laser Lathe | GTO | 3 |
| Neutronium Wire Cutting | GTO | 3 |
| Superconducting Magnetic Presser | GTO | 2 |
| Giant Electrochemical Workstation | GTO | 3 |
| Heavy Rolling | GTO | 3 |
| Hand of Arachne (tear) | GTO | 2 |
| Lightning Rod (gerador de arco) | GTO | 2 |
| Fuel Cell Generator | GTO | 3 + classe |
| Giant Sintering Array | GTO | 3 |
| Particle Stream Matrix Filling | GTO | 3 |
| Extreme Compressor | GTO | 4 |
| Gemini Containment System | GTO | 4 |
| Space Probe Surface Reception | GTO | 4 + classe |
| Integrated Vapor Deposition System | GTO | 5 |
| Integrated Assembler / Integrated Assembly Facility | GTO (+ versões GTNL/TST) | 1 |
| Compound Extreme Cooling Unit | GTO | 0 + classe (tipos de atomização e condensador de plasma) |
| Hyper Naquadah Reactor / Advanced | GTO `hyper_reactor` / `advanced_hyper_reactor` | 1 / 4 |
| Nanites Circuit Assembly Factory | GTO | 2 |
| Rare Earth Centrifugal / Advanced | GTO/GTL | ~10 (GTL próprio) |
| Platinum Group Processing Hub | GTNL | 1.7 ("atalho") |

### 3.4 Fase D: caro / sistemas grandes (GTO)

| Máquina | Fonte | Faltam | Mecânica |
|---|---|---|---|
| Space Elevator + módulos (Ore Extraction, drilling, assembly) | GTO | 6 | Hospedeiro de módulos |
| Road of Heaven | GTO | 16 | Super elevador (padrão de 144 KB) |
| Dyson Sphere Launch Silo + Receiving Station | GTO | 10 + 4 | Logística de lançamento + geração |
| Kerr-Newman Homogenizer | GTO | 15 + classe | Mixer de endgame (visual) |
| Nyarlathotep's Tentacle | GTO | 10 | Mega Assembly Line / Circuit AL / Suprachronal |
| Nano Phagocytosis Plant | GTO | 6 + classe | |
| Plasma Centrifuge | GTO | 10 | |
| Stellar Forge | GTO | 7 + classe | |
| Planetary Gas Collector | GTO | 13 | Combina com os planetas do Ad Astra |
| Core Drilling Plant (`planet_core_drilling`) | GTO | 9 | Mesma ideia do Starcore Miner do TST (portar esta) |
| Petrochemical Plant | GTO | 9 | |
| Fuel Refining Complex | GTO | 10 | |
| Neutron Forging Anvil | GTO | 10 | |
| Transliminal Oasis | GTO | 7 | |
| Biochemical Extraction | GTO | 1 + classe | |
| Fast Neutron Breeder Reactor | GTO | 3 + classe | Fissão |

### 3.5 Fase E: GTNH 1.7.10 sem equivalente no GTO/GTL (conversão por script)

| Máquina | Fonte | Mecânica |
|---|---|---|
| Semi-Stable Antimatter Stabilization Sequencer | GoodGenerator `AntimatterForge` | Produção própria de antimatéria (sem tipo de receita comum) |
| Pseudostable Black Hole Containment Field | GT5U `MTEBlackHoleCompressor` | Buraco negro com semente e estabilidade que decai; compressor + neutronium compressor |
| Industrial Arc Furnace | GT++ | (baixa prioridade: o GCYM cobre) |
| Endothermic Fridge | GT5U | Bônus de velocidade por resfriamento + refrigerantes (Infinity/SpaceTime/Eternity) |
| Mega Distillation Tower / Mega Oil Cracker | BartWorks | Duplicatas de Mega Distillation (GTL) / Cracker Hub (GTO): só se quiser o visual |

### 3.6 Fase F: TST, GTNL e 123Tech sem versão no GTO/GTL

| Máquina | Fonte | Mecânica |
|---|---|---|
| Hyper Thermal Convector | TST | Trocador de calor mega (2 modos) |
| Magnetic Drive Pressure Former | TST | 4 modos (extrusora/dobradeira/prensa/martelo) |
| Precise High-Energy Photonic Quantum Master | TST | Engraver + modo fóton |
| DTPF MK-II (`MM_…PlasmaForgePrototypeMK2`) | TST (modular) | −75% de catalisador após 24 h; **escolher entre este e o Hyperdimensional Plasma Fusion Core** |
| Eye of Harmony Injector | GTNL | Injeta fluidos no EOH com limites configuráveis na GUI |
| EGTW (Eternal GregTech Workshop) + módulos | GTNL (2.376 linhas) | Um segundo "Forge" de módulos |
| Vortex Matter Centrifuge, Field Forge Press, Horizontal Compressor, Mega Mixer, Nano Assembler Mk-50 | GTNL | Máquinas de paralelo |
| Tangshan Steel Mill | 123Tech | "Solução única" para todas as ligas de aço, modo wireless |
| Hyperdimensional Biebie Plasma Forge | 123Tech | Variante do DTPF |
| Ammonia Plant | Steam Reborn (gtsr) | Amônia na era do vapor |

### 3.7 Fase G: GTLAdditions

| Máquina | Mecânica | Observação |
|---|---|---|
| Apocalyptic Torsion Quantum Matrix | QFT com 100% de chance, 1/10 do insumo probabilístico, 1024 threads + distort + neutron compressor | **Não é o DTPF MK2.** Exige portar o QFT (tipo de receita) |
| Heart of the Universe | Genesis Engine: 4096× um Creative Laser por plano | O balanço fica com o pack (config) |
| Fuxi Bagua Heaven Forging Furnace | 4 tipos de endgame do GTL | |
| Primordial Evolution Nexus | Tipo de receita próprio | |
| Biosphere III + módulos Garden of Hermes | Hospedeiro de módulos com laser | |
| Skeleton Shift Rift Engine | Decay hastener | |
| Space Scaling Instrument | Packer com bobina especial | |
| Planetary Ionisation Convergence Tower | Mecânica própria (classe ainda não lida) | Ler antes de começar |
| Inferno Cleft Smelting Vault | Pirólise + cracking | Duplica o Cracker Hub (escolher a fonte) |
| Space Elevator MKII / Mega Space Elevator / Light Hunter Space Station | Hubs de módulos com paralelo máximo | Comparar com o Space Elevator / Road of Heaven do GTO |

### 3.8 Ainda não auditado

O GTO tem 301 multiblocos e o GTL 158; só os citados pelo autor foram avaliados. Os demais addons 1.7.10 (GT Not Good, MessTech, ThinkTech, EasyTech, Futa) foram listados por classe em G-0199/auditoria, mas não avaliados:
- GT Not Good: máquinas de abelha/cultivo/favo, transmutação, void miner;
- ThinkTech: cadinhos, Czochralski, gases nobres;
- EasyTech: mineradores entrelaçados.

## 4. Mecânicas e APIs transversais

| Item | Estado | Próximo passo |
|---|---|---|
| KubeJS: EOH (`GTNAStartupEvents.eyeOfHarmony`) | feito | Mesmo padrão para o Forge of Gods (tabela de plasmas, custos, Exotic Module), Nano Forge (nanite/tier), TPM (custo wireless via data) |
| KubeJS: módulos (`GTNASubPatterns.registerKubeJS`) | feito | Documentar em `examples/` |
| Chaves de dado de receita (`tpm_eut`, `nano_forge_tier`, `ebf_temp`, `fog_plasma_tier`…) | espalhadas | Documentar em `examples/` para criadores de pack |
| Relatório de alcançabilidade (GameTest) | feito (G-0199) | Virar gate: falhar quando conteúdo novo do gtna ficar inalcançável (lista de permissão para saídas de máquina) |
| Blocos do GTO | 187 no GTNA | Lote compartilhado: `AMPROSIUM_*`, `HYPER_CORE`, `ENHANCE_HYPER_MECHANICAL_CASING`, `IRIDIUM_PIPE_CASING`, `HIGH_PRESSURE_RESISTANT_CASING`, `LAFIUM_MECHANICAL_CASING`, `QUANTUM_GLASS`, `PIKYONIUM_MACHINE_CASING` e `DIMENSIONAL_BRIDGE_CASING` aparecem em muitas máquinas das fases C/D. **Portar esse lote primeiro barateia dezenas de máquinas.** |

## 5. Ordem sugerida para as próximas sessões

1. Fechar P1–P4 (QA, commit, correção do datagen, teste instável).
2. **Lote de blocos compartilhados do GTO** (§4); isso barateia todas as fases.
3. Fase A: BEC → TAM/Convergence → Nano Forge tier 4; Hyperdimensional Plasma Fusion Core.
4. Fase B, em lotes de 3–5 máquinas por checkpoint.
5. Fase C, depois D.
6. Fases E–G conforme a prioridade do autor (máquinas com mecânica própria exigem API de KubeJS).
