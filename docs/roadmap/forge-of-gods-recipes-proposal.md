# Forge of Gods — fase 7 (receitas e custos extras): proposta para decisão do autor

Referência: GT5-Unofficial `a3e1e112`, `tectech/loader/recipe/ResearchStationAssemblyLine.java`
(`addGodforgeRecipes`) e `tectech/loader/recipe/Godforge.java` (custos extras dos upgrades).

A auditoria já registrava que esta fase depende de uma escolha do autor: portar fielmente os materiais
do GTNH ou usar substitutos. Ela não foi implementada sem essa escolha. Toda a lógica, a estrutura, os
módulos, o render, a GUI e o mecanismo de custos extras (janela de inserção manual, `payCost`) já
funcionam. Falta apenas definir **quais itens** compõem cada custo e cada receita.

## O que o GTNH exige

| Receita (Research Station → Assembly Line, UMV) | Entradas principais do GTNH |
|---|---|
| Controlador (pesquisa: Stellar Energy Siphon Casing) | 4 Siphon Casings, 2 ZPM4, 64 Dimensional Bridge casings, 32 Eternal Singularity, Mellion/Six-Phased Copper/Creon/Metastable Oganesson (placas densas), Stable Boson Containment, fio SC UIV, sensores UIV, circuitos UIV, Energy Tunnel UIV; fluidos: Mutated Living Solder, Excited DTEC, plasma de Tório, Transcendent Metal |
| Magnetic Confinement Casing ×8 | frames de Transcendent Metal, Magneto Resonatic, Tengam Attuned, Creon, parafusos de Hypogen/Six-Phased Copper, SC Composite, emissores UIV, eletroímã Tengam |
| Boundless Structure Casing | frames de Mellion/Six-Phased Copper/Transcendent Metal/Astral Titanium, Stable Boson, geradores de campo UEV, gerador de gravidade artificial (AmunRa) |
| Guidance Casing | Boundless Casing, ZPM2, Cosmic Fabric Manipulator, campos UEV, emissores UIV, Creon/Mellion |
| Stellar Energy Siphon Casing | Boundless Casing, 128 bobinas Hypogen, fio SC UIV, capacitores de calor de neutrônio, Energy Tunnel UIV, 64 geradores de plasma UV |
| Gravitational Lens | vidros quânticos e de campo de força, lentes NH, Chronomatic Glass, hastes Creon/Mellion/Six-Phased Copper |
| Graviton Modulator T1 ×2 | Confinement Casings, Field Restriction Coil T3, Creon, Mellion, baterias de gema T4, emissores UIV, nanites de prata |
| Phonon Transmission Conduit | frame de Transcendent Metal, Creon, campo TFFT, Tesseracts, capacitores relativísticos, Phonon Medium |
| Módulos (Smelting/Molten/Plasma/Exotic) | 4 Shielding Casings, 64 Exothermic Hearth / Mega ABS / aquecedor UIV / misturador transcendente, ZPM4, fio SC UIV, braços robóticos/esteiras/bombas UIV, Creon/Mellion/Six-Phased Copper |

Custos extras dos upgrades (só com Eternal Singularity no pack): START, FDIM, GPCI, QGPIU, CD, EE e
END (cada um com 6 a 12 pilhas de componentes UIV–UXV, Eternal Singularity, casings do próprio Forge,
frames de MagMatter/Eternity/Universium etc.).

## O que o GTNA já tem

- **Disponível:** componentes GTCEu de UEV a UXV (braços robóticos, esteiras, bombas, emissores,
  sensores, geradores de campo), SpaceTime, Universium, White/Black Dwarf Matter, Magmatter, DTR,
  Raw Star Matter, MHDCSM, Graviton Shard, Tesseract, bobina Hypogen (bloco) e as próprias casings do Forge.
- **Ausente:** Mellion, Creon, Six-Phased Copper, Transcendent Metal, Hypogen (material), Excited DTEC,
  Mutated Living Solder, Phonon Medium, Magneto Resonatic, Tengam, Astral Titanium, Chronomatic Glass,
  Rhugnor, Metastable Oganesson, Eternity, Infinity (material GTCEu), ZPM4/ZPM6 e Eternal Singularity.

## Opções

1. **Port fiel dos materiais.** Adicionar os cerca de 15 materiais ausentes, com propriedades e cadeias
   do GTNH, e então portar as receitas 1:1. É a opção mais fiel, mas também a mais cara, porque cada
   material traz a sua própria cadeia de produção.
2. **Substitutos documentados.** Manter a estrutura das receitas do GTNH (mesmos tipos de item,
   quantidades, tier UMV e pesquisa) e trocar cada material ausente pelo equivalente de tier mais
   próximo do GTNA/GTCEu. A tabela de substituição fica registrada em `THIRD_PARTY_NOTICES` e no ledger.
3. **Híbrido.** Portar só os materiais que definem a identidade do Forge (Transcendent Metal, Creon,
   Mellion, Six-Phased Copper, Hypogen, Mutated Living Solder) e substituir o resto.

Recomendação: **opção 3**. Esses seis materiais aparecem em quase todas as receitas do Forge, e o resto
aparece uma vez cada.

## Próximo passo

Quando a opção for escolhida, a implementação é direta. As receitas entram em `GTNAGodforgeRecipes`
(assembly line com pesquisa, quando o GTNA tiver a Research Station correspondente). Os custos extras
são registrados com `GodforgeUpgrade.addExtraCost(new ExtraCost(itemId, quantidade))`, que a GUI e a
janela de inserção manual já usam.

## Implementado (provisório, opção 2 — substitutos)

Para concluir o port sem materiais impossíveis de obter, as receitas entraram com a estrutura do GTNH
(mesmos itens-tipo, quantidades, durações e tiers; UMV/UXV do GTNH → UXV do GTCEu) e substitutos em
`GTNAGodforgeProgression`. Basta trocar os métodos de substituição no topo da classe se a opção 1 ou 3
for escolhida.

| GTNH | Substituto no GTNA |
|---|---|
| Transcendent Metal | SpaceTime |
| Creon | White Dwarf Matter |
| Mellion | Black Dwarf Matter |
| Six-Phased Copper | Neutronium |
| Hypogen (material) | Tritanium |
| Metastable Oganesson | Darmstadtium |
| Eternity | Universium |
| SC UIV | Ruthenium Trinium Americium Neutronate |
| Mutated Living Solder | Indalloy 140 fundido |
| Excited DTEC | Resíduo Dimensionalmente Transcendente |
| Phonon Medium | Raw Star Matter |
| Eternal Singularity | Gravi-Star |
| ZPM2 / ZPM4 / ZPM6 | Energy Module / Energy Cluster / Ultimate Battery |
| Stable Boson Containment, Energy Tunnel | Field Generator UIV |
| Exothermic Hearth / Mega ABS | EBF do GTCEu / Mega Alloy Blast Smelter do GTNA |
| Fusion Computer UV3, Transcendent Plasma Mixer | Fusion Reactor UV, módulo Plasma |

Pesquisa: GTNH pede 48M CWU a 8192 CWU/t; o GTCEu usa 256 CWU/t com o mesmo tempo de pesquisa.
Custos extras dos upgrades START, FDIM, GPCI, QGPIU, CD, EE e END seguem a lista do GTNH com os
mesmos substitutos.

## Decisão do autor (2026-10-07)

Portar a **cadeia completa do GTNH** numa sessão futura: Transcendent Plasma Mixer, Dimensionally
Transcendent Plasma Forge e todos os intermediários (Orundum, Fiery Steel, Firestone, Atomic Separation
Catalyst, Fermium, Celestial Tungsten, Astral Titanium, Hypogen, Chronomatic Glass, Rhugnor, Harmonic
Compound, DTEC/DTRC e excitados, Phonon Medium, Eternity e a cadeia de nanites), com as receitas
originais. Depois, trocar os substitutos em `GTNAGodforgeProgression` pelos materiais reais. Até lá os
substitutos continuam ativos para o Forge poder ser testado.

Rastreio inicial: Mellion (Mixer UMV), Creon (Transcendent Plasma Mixer; Vacuum Freezer a partir de
Harmonic Compound), Six-Phased Copper (DTPF 12600K), Transcendent Metal (EBF + Vacuum Freezer; origem
do pó a rastrear), Excited DTEC (Laser Engraver a partir de DTEC). Phonon Medium (GT++) e Eternity
(nanites) ainda a rastrear.
