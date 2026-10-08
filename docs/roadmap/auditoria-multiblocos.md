# Auditoria de multiblocos: o que portar para o GT:IA (2026-10-07)

> **Critério revisado pelo autor (2026-10-07, vale sobre as tabelas abaixo):** o GTNA é uma **biblioteca de
> multiblocos**. Para cada máquina viável, porta-se estrutura, mecânica, texturas originais e suporte a KubeJS, e
> registra-se o tipo de receita. As receitas e linhas de processamento ficam para o modpack (GT:IA) e outros
> criadores; não precisam ser 1:1 com o GTNH.
> Com isso, "atalho" e "depende de conteúdo" **deixam de ser motivo para não portar**: o pack decide as receitas.
> Continuam fora só:
> 1. **duplicatas exatas**, isto é, a mesma máquina em dois addons (porta-se a melhor fonte);
> 2. máquinas que dependem de um **sistema ausente** (pesquisa, Thaumcraft/Botania);
> 3. as que ficam **inviáveis** pelo custo (avaliar caso a caso).
> A ordem de prioridade passa a ser **custo de port**: fonte GTO/GTL antes de GTNH/1.7.10, e menos blocos faltando
> primeiro.

## Máquinas acrescentadas por último (GTNH)

| Máquina | Classe | O que faz | Esforço |
|---|---|---|---|
| Semi-Stable Antimatter Stabilization Sequencer | GoodGenerator `AntimatterForge` (756 linhas) | Produz antimatéria por mecânica própria (protomatéria + catalisadores, sem tipo de receita comum) | A |
| Pseudostable Black Hole Containment Field | GT5U `MTEBlackHoleCompressor` (854 linhas) | Compressor + Neutronium Compressor; buraco negro com semente e estabilidade que decai | M–A |

Fontes analisadas:
- GTNH 2.9.0-RC-1, instância do Prism: jars do GTNL (sciencenotleisure), TST, 123Technology (OTHTechnology), Steam Reborn (gtsr), GT Not Good, MessTech, ThinkTech, EasyTech e Futa.
- Fontes do GT5-Unofficial.
- Fontes do GTOCore e do GTLCore (1.20/GTCEu).

A comparação é contra o que o GT:IA já tem: GTCEu 7.5.3 + GCYM, GTNA (84 multiblocos) e GTIACore (lime kiln, Solvay, Bayer, Hall-Héroult, contato, amônia, célula de diafragma, wood retort).

**Critérios (direção do GT:IA):**
- Megafábrica, cadeias longas, química com referência real.
- "Mega" e multiblocos sem limite são bem-vindos.
- Máquina que **pula etapas** de uma cadeia conta como exploit.

## Achados principais

1. **Quase toda máquina do GTNL é backport de uma máquina do GTO/GTL.** O GTO tem a versão 1.20/GTCEu do Kerr-Newman, Cracker Hub, Crystal Builder, PCB Factory, Nano Phagocytosis, Gemini, Dyson, Space Elevator, Hyper Reactor, Heavy Rolling, Holy Separator e outros. O GTNA já lê os padrões `.mbs` do GTO. Portar do GTO custa uma fração do port da versão 1.7.10 (StructureLib + reimplementar a lógica).
2. **O custo está nos blocos, não na estrutura.** Cada máquina do GTO usa de 5 a 27 casings do GTO. O GTNA já tem 187 blocos, então a tabela mostra quantos ainda faltam.
3. **O BEC (Bose-Einstein Condensate) é requisito de progressão.** No GTNH 2.9 ele produz a Singularity Shielding Casing, os Graviton Flow Modulators T2/T3 e a Transdimensional Alignment Matrix. Hoje esses itens usam uma receita provisória de Assembly Line (G-0199).

## Veredito por máquina da sua lista

Legenda: **esforço** B baixo / M médio / A alto. "Faltam" = blocos do GTO que o GTNA ainda não tem.

### 1. Necessário

| Máquina | Fonte | Por quê | Esforço |
|---|---|---|---|
| Multiblocos Bose-Einstein (Condensate Assembler + geradores) | GTNH | Shielding Casing, Graviton T2/T3, TAM/Convergence do DTPF, hoje provisórios | A |

### 2. Portar, valor real (função nova ou fantasia de megafábrica)

| Máquina | Melhor fonte | O que faz / por quê | Faltam | Esforço |
|---|---|---|---|---|
| Desulfurizer | GTO | Dessulfurização de óleos/gases; química real ligando linhas | 5 | M |
| Cracker Hub | GTO | Mega cracker + pirólise; **substitui** o Mega Oil Cracker do GTNH e o Advanced do TST | 2 | B |
| Mega Distillation Tower | GTL (`mega_distillery`) | Destilação em escala de megafábrica | ~0–2 | B |
| Supercomputing Center | GTO | CWU de endgame; várias receitas do GTNA pedem 512–1024 CWU/t de pesquisa | 0 (classe própria) | M |
| Crystal Builder | GTO | Autoclave + cristalização; precisa do tipo de receita de cristalização (conteúdo) | 3 | M |
| PCB Factory | GTO | Linha nova de PCB; química/fábrica forte, muito conteúdo de receita | 0 (classe própria) | M–A |
| Hyper Thermal Convector | TST | Trocador de calor mega; útil se o vapor supercrítico for ganhar escala | — (1.7.10) | M |
| Space Elevator + Ore Extraction Module | GTO | Sistema de módulos de endgame (mineração/montagem) | 6 | A |
| Dyson Sphere Launch + Receiver | GTO | Energia de endgame com logística de lançamento; muito visual | 10 + 4 | A |
| Kerr-Newman Homogenizer | GTO | Mixer de endgame; o ganho é principalmente **a aparência** | 15 + classe | A |

### 3. Barato, redundante na função, mas cabe como "Mega" (escala)

São máquinas do GTO com arquivo de padrão e poucos ou nenhum bloco faltando. Valem pela fábrica, não por função nova.

| Máquina | Equivalente hoje | Faltam |
|---|---|---|
| Large Incubator | Incubator do GTNA | 0 |
| Mega Vacuum Drying | Vacuum Drying do GTNA (o Giant Vacuum Drying do TST é a mesma ideia) | 0 |
| Mega Brewer | Large Brewer do GCYM | 0 |
| Engraving Laser Plant | Large Engraver do GCYM | 1 |
| Large Pyrolyse Oven | Pyrolyse Oven do GTCEu | 0 |
| High Energy Laser Lathe | **não existe torno grande** no GCYM | 3 |
| Holy Separator (cortador/torno) | Large Cutter do GCYM | 3 |
| Neutronium Wire Cutting | Large Wiremill do GCYM | 3 |
| Superconducting Magnetic Presser | Large Material Press do GCYM | 2 |
| Giant Electrochemical Workstation | Large Electrolyzer do GCYM | 3 |
| Extreme Compressor | Compressor do GTCEu / Steam Mega Compressor do GTNA | 4 |
| Gemini Containment System (empacotar/desempacotar) | Large Packer do GCYM | 4 |
| Infinity Fluid Drilling Rig | Void Fluid Drilling Rig do GTNA | 0 |
| Heavy Rolling / Reaction Furnace | — (precisam dos tipos de receita `rolling`/`reaction_furnace` do GTO, ou seja, conteúdo) | 3 / 0 |

### 4. Não portar (redundante, atalho ou conflito com a direção)

| Máquina | Motivo |
|---|---|
| Industrial Arc Furnace (GTNH) | O Large Arc Smelter do GCYM cobre |
| Endothermic Fridge (GTNH) | É um vacuum freezer com bônus de velocidade; o Mega Vacuum Freezer do GCYM cobre |
| Mega Oil Cracker (GTNH) / Advanced Mega Oil Cracker (TST) | O Cracker Hub (GTO) cobre com menos esforço |
| Ammonia Plant (Steam Reborn) | O GTIACore já tem o conversor de amônia (Haber) |
| Wood Chemical Plant (GTNL) | O GTIACore tem o wood retort; duplicaria a linha |
| ISA Processing Integrated Factory (123) | **Atalho declarado** ("jogou fora os processos bagunçados") |
| Platinum Group Processing Hub (GTNL) | Platina em uma etapa = atalho sobre a cadeia real |
| Advanced Rare Earth Centrifuge (GTNL/GTL) | Terras raras em uma etapa = atalho |
| Tangshan Steel Mill (123) | "Solução única" para todas as ligas de aço = atalho; só como EBF cosmético com paralelos |
| Hyperdimensional Biebie Plasma Forge (123) | Variante meme; o DTPF já existe |
| DTPF MK-II (TST) | Sobrepõe o DTPF já portado (−75% vs −50% de catalisador) |
| Integrated Assembly Facility (GTNL) / Integrated Assembly Matrix (TST) | Corta a logística da Assembly Line; vai contra "linhas longas" |
| Nano Phagocytosis Plant | Isa Mill + macerador já existem; faltam 6 blocos |
| Vortex Matter Centrifuge, Field Forge Press, Horizontal Compressor, Mega Mixer, Nano Assembler Mk-50 (GTNL) | Só existem no GTNL; máquinas de paralelo para tipos que o GCYM/GTNA já cobrem |
| Precise High-Energy Photonic Quantum Master (TST) | Engraver grande + um modo "photon" que só existe com as receitas do TST |
| Magnetic Drive Pressure Former (TST) | Os 4 modos já são cobertos pelo GCYM; só faria sentido como "GTO Rules" |
| Starcore Miner (TST) | Os void miners do GTNA cobrem |
| Astral Computing Array (TST) | O Supercomputing Center (GTO) faz o papel com menos esforço |
| Research Center (GTNL/GTO) | **Corrigido:** serve ao research padrão do GTCEu, que o GT:IA usa; entra no port (ver ROADMAP) |
| Data Center (GTNL/GTO) | O Data Bank do GTCEu cobre |
| Eye of Harmony Injector (GTNL) | Conveniência de entrada (1.342 linhas); o AE2 já alimenta o EOH |
| EGTW e seus módulos (GTNL) | Um segundo "Forge of Gods" (2.376 linhas); sobrepõe o FoG já portado |
| Hyper / Advanced Hyper Naquadah Reactor | Só com a cadeia de combustíveis hyper do GTO (conteúdo novo); senão, não |
| Fast Neutron Breeder | Só se o GT:IA quiser um sistema de fissão |
| Nanites Circuit Assembly Factory | Só se for criada uma linha de circuitos com nanites |

## Ordem sugerida

1. BEC (resolve os itens provisórios).
2. Baratos de alto valor: Cracker Hub → Mega Distillation (GTL) → Desulfurizer → Supercomputing Center.
3. Conteúdo de química: Crystal Builder, PCB Factory.
4. Bloco "Mega" da seção 3, em lotes (primeiro os que faltam 0–1 blocos).
5. Endgame/visual: Space Elevator, Dyson, Kerr-Newman, Nano Forge tier 4.

## Segunda lista (GTO e GTLAdditions)

"Faltam" = blocos do GTO que o GTNA ainda não tem. Muitas destas máquinas usam um **tipo de receita próprio do GTO**: portar a estrutura é barato, mas a máquina só serve quando houver a linha de receitas dela (conteúdo).

### GTO

| Máquina | O que faz | Faltam | Veredito |
|---|---|---|---|
| Hyperdimensional Plasma Fusion Core | DTPF + Stellar Forge, usa a mesma classe de máquina do DTPF | 3 | **Portar.** É o "DTPF MK2" barato: aproveita o DTPF já portado |
| Dimensional Focus Engraving Array | Gravação de endgame (tipo de receita próprio) + render | 0 | **Portar** (visual, barato); precisa das receitas |
| Giant Flotation Tank | Flotação em escala; o GTNA já tem a célula de flotação e as receitas | 2 | **Portar** (Mega barato e coerente) |
| Fuel Cell Generator | Célula a combustível (absorção/transferência/liberação), química real | 3 + classe | **Portar**; combina com o capítulo de energia estilo GTO |
| Phase Change Cube | Extractor + solidifier em escala | 1 | Mega barato |
| Compound Extreme Cooling Unit | Vacuum freezer + atomização + condensador de plasma | 0 + classe | Médio; atomização e condensador são tipos novos |
| Giant Sintering Array | Sinterização (metalurgia/cerâmica real) | 3 | Bom se criarmos a linha de sinterização |
| Integrated Vapor Deposition System | CVD + PVD (fabricação de chips, real) | 5 | Bom junto com uma linha de wafers |
| Lightning Rod | Gerador de arco (energia) | 2 | Médio; variedade de geração |
| Planetary Gas Collector | Coleta de gases de planetas | 13 | Combina com os planetas do Ad Astra, mas é caro |
| Space Probe Surface Reception | Coleta de materiais do espaço | 4 + classe | Mesmo tema; médio |
| Plasma Centrifuge | Centrífuga de plasma de endgame | 10 | Só com as receitas dela; caro |
| Stellar Forge | Forja de materiais de endgame | 7 + classe | Sobrepõe DTPF/Forge of Gods; depois |
| Star Ultimate Material Forge | Forja de materiais finais | 1 | Barato, mas depende do conteúdo final do GTO |
| Nyarlathotep's Tentacle | Assembly Line + Circuit AL + Suprachronal em escala | 10 | Endgame visual; caro |
| Road of Heaven | Super space elevator (padrão de 144 KB, hospedeiro de módulos) | 16 | Topo do endgame; comece pelo `space_elevator` normal |
| Particle Stream Matrix Filling Machine | Canner em escala | 3 | Mega; baixo valor |
| Transliminal Oasis | Estufa + crescimento de árvores em escala | 7 | Redundante (Greenhouse/Large Greenhouse do GTNA) |
| Neutron Forging Anvil | Forge hammer + prensagem isostática | 10 | Redundante e caro |
| Core Drilling Plant (`planet_core_drilling`) | Minerador de núcleo (mesma classe do Starcore Miner) | 9 | Não: os void miners cobrem |
| Petrochemical Plant | Refinaria integrada do GTO | 9 | **Cuidado:** refino em uma máquina tende a pular a cadeia; só se as receitas mantiverem as etapas |
| Fuel Refining Complex | Combustíveis de alto tier do GTO | 10 | Só com essa linha de combustíveis |
| Biochemical Extraction | Extração biológica | 1 + classe | Só com uma linha bio |
| Hand of Arachne | Tear (tecidos do GTO) | 2 | Só com a linha de tecidos |
| Bio Oscillation Generator | — | — | Não achei no GTO (talvez tenha outro nome); preciso do nome exato ou de um print |

### GTLAdditions

O estilo do GTLAdditions é paralelo infinito, várias receitas ao mesmo tempo e energia absurda. Isso briga com "linhas longas" e com o equilíbrio.

| Máquina | O que faz de verdade | Veredito |
|---|---|---|
| Apocalyptic Torsion Quantum Matrix | **QFT** (Quantum Force Transformer) com 100% de chance, 1/10 do insumo, 1024 threads + "distort" + neutron compressor. **Não é o DTPF MK2** | Não: o QFT pula etapas, e esta versão ainda mais |
| Heart of the Universe | Gerador "Genesis Engine": cada plano gera 4096× um Creative Laser | Não: quebra qualquer balanço |
| Fuxi Bagua Heaven Forging Furnace | Stellar ignition, chaotic alchemy, molecular deconstruction, ultimate material forge | Só se trouxermos o conteúdo final do GTL |
| Primordial Evolution Nexus | Tipo de receita próprio (evolution of primordial) | Mesmo caso: conteúdo do GTL |
| Biosphere III (+ módulos Garden of Hermes) | Hospedeiro de módulos com laser | Caro; só pelo visual |
| Inferno Cleft Smelting Vault | Pirólise + cracking em escala | Duplica o Cracker Hub (GTO): escolha um |
| Space Scaling Instrument | Packer em escala com bobina especial | Redundante |
| Skeleton Shift Rift Engine | Decay hastener | Nicho; não temos a linha |
| Planetary Ionisation Convergence Tower | Mecânica própria (tipo de receita "dummy") | Não li a classe; avalio se você quiser |
| "Space hub" (Space Elevator MKII / Mega Space Elevator / Light Hunter Space Station) | Hubs de módulos com paralelo máximo (`Int.MAX`) e várias receitas | Não; prefira o Space Elevator do GTO e, no topo, o Road of Heaven |

### Ordem sugerida (consolidada com a primeira lista)

1. BEC.
2. Baratos de alto valor: Cracker Hub, Mega Distillation, Hyperdimensional Plasma Fusion Core (o "DTPF MK2"), Giant Flotation Tank, Desulfurizer, Supercomputing Center.
3. Química e energia reais: Fuel Cell Generator, Crystal Builder, PCB Factory, Giant Sintering, Integrated Vapor Deposition (estes três pedem linhas de receita).
4. Megas baratos (0–1 bloco faltando): Dimensional Focus Engraving Array, Phase Change Cube, Large Incubator, Mega Vacuum Drying, Mega Brewer, Engraving Laser Plant.
5. Endgame visual: Space Elevator → Road of Heaven, Dyson, Kerr-Newman, Nyarlathotep, Nano Forge tier 4.

O GTO tem 301 multiblocos e o GTL 158. Esta lista cobriu só os que você citou; o restante do GTO/GTL ainda não foi auditado.
