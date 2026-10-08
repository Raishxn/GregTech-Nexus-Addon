# Forge of Gods — cadeia de materiais GTNH a portar

Fonte: GT5-Unofficial `master` (2026-10-07; `ResearchStationAssemblyLine.addGodforgeRecipes`,
`Godforge.java` custos extras, `MaterialsInit`, GT++ `MaterialsElements`/`MaterialsAlloy`/`MaterialMisc`,
GoodGenerator `GGMaterial`). Rastreio feito pela saída de cada receita (`itemOutputs`/`fluidOutputs`).

Decisão do autor (2026-10-07): **registrar todos os materiais agora** (G-0189, `GodforgeChainMaterials`) e
portar/ajustar as receitas depois. Os materiais não têm temperatura de EBF nem fundição de pó, para não abrir
atalho antes da receita própria.

Tiers GTNH → GTCEu: UMV → UXV, UXV → OpV.

## 1. Multiblocos que a cadeia exige

| Multibloco GTNH | Produz na cadeia | Situação no GTNA |
|---|---|---|
| Dimensionally Transcendent Plasma Forge (DTPF) | Six-Phased Copper, DTR, Laser Lens Special, Shirabon | **portar** |
| Transcendent Plasma Mixer | Plasma de Creon, Excited DTEC/DTSC, Primordial Matter | **portar** |
| Nano Forge | Nanites (Eternity, Six-Phased Copper, Transcendent Metal) | **portar** |
| Quantum Force Transformer | Mutated Living Solder (rota alternativa), atalhos químicos | **portar** |
| Fusão acima de UV (Mk4/Mk5) | Hypogen (1,2 bi EU), Rhugnor (2 bi EU), Metastable Oganesson | **Advanced Fusion do GTO** (decisão do autor) |
| Exothermic Hearth | ingrediente dos módulos | **portar** (ou substituir) |
| Chemical Plant GT++ | Mutated Living Solder | adaptar no Chemical Plant do GTNA |
| Alloy Blast Smelter | Phonon Medium, Nitinol 60 | já existe (Mega ABS) |
| Laser Engraver | Celestial Tungsten, Astral Titanium, Chromatic Glass, Advanced Nitinol, Excited DTEC | GTCEu |
| EBF com bobina ≥ 14.000 K | Creon/Mellion (Harmonic Compound), Transcendent Metal, Phonon Crystal Solution | GTCEu + bobina alta |
| Polarizer, Electromagnetic Separator, Vacuum Freezer, Mixer, Autoclave | Tengam, intermediários | GTCEu |

## 2. Materiais usados diretamente pelo Forge

| Material | Como o GTNH produz | Registrado |
|---|---|---|
| Creon | Transcendent Plasma Mixer (Celestial Tungsten + Ca + DTR + Th); Vacuum Freezer de Harmonic Compound | sim |
| Mellion | Mixer UMV (DTR, Fiery Steel, Firestone, Rb, Tritanium, Atomic Separation Catalyst, Orundum) | sim |
| Transcendent Metal | EBF 11.701 K com Tungsten; pó vem de macerar Tesseract | sim |
| Six-Phased Copper | DTPF 12.600 K | sim |
| Mutated Living Solder | Chemical Plant GT++ (Biocell, Gravistar, catalisadores de Infinity) ou QFT | sim |
| Excited DTEC | Laser Engraver de DTEC; Transcendent Plasma Mixer | sim |
| Phonon Medium | Alloy Blast Smelter (Magneto Resonatic, Phonon Crystal Solution, Pr, Superconductor Base UIV, Metastable Oganesson) | sim |
| Hypogen | Fusão 1,2 bi EU | sim |
| Rhugnor | Fusão 2 bi EU (Infinity + Quantum) | sim |
| Chromatic Glass | Laser Engraver (vidro + Laser Lens Special) | sim |
| Astral Titanium | Laser Engraver (Titanium + Laser Lens Special) | sim |
| Celestial Tungsten | Laser Engraver (Tungsten + Laser Lens Special) | sim |
| Advanced Nitinol | Laser Engraver (Nitinol 60 + Laser Lens Special) | sim |
| Attuned Tengam | Polarizer de Purified Tengam ← Electromagnetic Separator de Raw Tengam | sim (Raw/Purified/Attuned) |
| Superconductor UIV / UMV (+ Base) | EBF 12.700 / 13.600 K + Vacuum Freezer | sim (fio supercondutor UIV e UXV) |
| Infinity | Avaritia no GTNH; **sem Avaritia no GT:IA**, rota nova a definir | sim |
| Eternity | Nano Forge tier 3 (nanites) | sim |
| Ichorium, Dragonblood (Dragon Metal) | Thaumcraft/Draconic no GTNH; rota nova a definir | sim |
| Magmatter, SpaceTime, Universium, WDM/BDM, MHDCSM, DTR, QGP, Raw Star Matter | EOH / Forge | já existiam |

## 3. Intermediários (segundo nível)

| Intermediário | Origem GTNH | Registrado / plano |
|---|---|---|
| DTCC → DTPC → DTRC → DTEC → DTSC | Mixer em cadeia (ZPM → UIV), cada um consome o anterior | sim, os 5 + 5 Excited |
| Harmonic Compound | EBF 14.000 K de Creon + Mellion (item) | item na fase de receitas |
| Phonon Crystal Solution | EBF 17.000 K (Dilithium, Mellion, Phononic Seed Crystal, Six-Phased Copper) | sim |
| Phononic Seed Crystal | Autoclave UMV (Mellion, Transcendent Metal, água pura grau 8) | item na fase de receitas |
| Orundum | Forming Press (Silicon + Tiberium) | sim (gema + fluido); Tiberium a decidir |
| Atomic Separation Catalyst | EBF 5.000 K de Orundum + Vacuum Freezer | sim |
| Metastable Oganesson | Fusão (Oganesson + Naquadah) ou centrífuga de combustível de naquadah | sim |
| Shirabon | DTPF 13.500 K | sim |
| Quantum (liga) | Centrífuga de Churitsu ← Mixer (Californium, Quantum, Ruridit, Trinium-Naquadah…) | sim (Quantum, Churitsu, Shijima) |
| Nitinol 60 | Alloy Blast Smelter Ni + Ti | sim |
| Magneto Resonatic, Dilithium | BartWorks / GT (gemas) | sim |
| Laser Lens Special | DTPF 10.800 K; Cyclotron/Beam Crafter de Duranium | item na fase de receitas |
| Primordial Matter, Stabilised Baryonic Matter, Heavy Radox | Plasma Mixer / cadeias próprias | sim (fluidos) |
| Fiery Steel, Firestone, Tiberium, água pura grau 8 | Twilight Forest / Railcraft / BartWorks / purificação | **substituir** na fase de receitas |
| Runite, Force, Titansteel, Arcanite, Tartarite | rotas alternativas via Thaumcraft | **não portar** (rota do Laser Engraver basta) |

## 4. Itens e blocos dos custos

ZPM2/ZPM4/ZPM6, Relativistic Heat Capacitor, Neutronium Heat Capacitor, Stable Boson Containment Unit,
Superconductor Composite, Thermal Superconductor, Energy Tunnel UIV, Battery Gem 4, Force Field Glass,
Cosmic Fabric Manipulator, Electromagnet Tengam, Dimensional Bridge Casing, Eternal Singularity,
Transdimensional Alignment Matrix, UHT Resistant Mesh, Space Coolant Cell, Fusion Internal Casing 2,
Phononic Seed Crystal, Harmonic Compound, Laser Lens Special, Hypogen Coil (bloco já existe no GTNA, sem
receita). Hoje a maioria tem substituto em `GTNAGodforgeProgression`; serão itens próprios na fase de receitas.

## 5. Ordem proposta

1. **Materiais** (feito, G-0189).
2. **Feito (G-0190):** cadeia DT (DTCC→DTSC) no Mixer + Laser Engraver (Excited), com 11 plasmas por fusão.
3. **Feito (G-0191):** Advanced Fusion do GTO → Hypogen, Rhugnor, Metastable Oganesson.
4. **Feito (G-0192):** DTPF → Six-Phased Copper, DTR, Laser Lens Special; Celestial Tungsten, Astral Titanium,
   Chromatic Glass, Advanced Nitinol. Shirabon e Dimensionally Shifted Superfluid ficam para a etapa 6.
5. **Feito (G-0193):** Transcendent Plasma Mixer (só rede wireless, custo pago no início) → Excited DTCC..DTSC,
   Primordial Matter, plasma de Creon; Mellion, Orundum, Atomic Separation Catalyst e Harmonic Compound
   (textura emprestada do Quantum Eye até o autor decidir).
6. **Feito (G-0194):** Phonon (Seed Crystal, Crystal Solution, Medium), Tengam, Dilithium, Magneto Resonatic,
   supercondutores UIV/UMV e Shirabon.
7. **Parcial (G-0195):** rotas próprias para Infinity, Quantum, Dragonblood, Ichorium, Mutated Living Solder e
   Eternity (no DTPF). **G-0196:** Nano Forge tiers 1-3 + 11 nanites (tier 4 MagMatter não portado).
8. **Feito (G-0197):** componentes GTNH portados e progressão/custos reescritos do GTNH. Restam: ZPM4/ZPM6,
   Eternal Singularity, Transdimensional Alignment Matrix (precisa do BEC), Nano Forge tier 4. (G-0198: ZPM3-6 e DSS feitos.)

## 6. Etapa 4 (DTPF) — levantamento (implementado em G-0192)

Fonte: GT5-Unofficial master, `MTEPlasmaForge` e `PlasmaForgeRecipes`.

- **Estrutura:** 33×24×33, controlador no meio da 3ª camada. Letras: `N` Dimensionally Transcendent Casing,
  `b` Dimensional Injection Casing (onde ficam hatches; mínimo 1.250), `s` Dimensional Bridge Casing, `C` bobinas.
  O GTNA já tem `pattern/dtpf.mbs` e as casings `dimensionally_transcendent_casing` /
  `dimension_injection_casing` (usadas pelas máquinas "dimensionally transcendent" do GTL); conferir o mapeamento
  de letras e se existe a Dimensional Bridge Casing antes de reaproveitar.
- **Energia:** até 2 energy hatches do mesmo tier, ou 1 laser. Overclock tipo EBF (calor da bobina, sem limite de
  tier); a receita exige `COIL_HEAT` ≤ calor da máquina.
- **Desconto de catalisador:** cada tick rodando soma tempo; em 8 h contínuas o consumo dos Excited DTxC cai até
  50%; parado, o tempo cai 100× mais rápido. Convergence (Transdimensional Alignment Matrix no controlador) dá
  overclock perfeito pagando catalisador extra — item ainda não portado.
- **Bobinas:** Hypogen já existe (12.600 K). Receitas pedem 10.800–13.500 K; falta uma bobina acima (Eternal,
  ~13.500 K) com textura do Modernity (`MACHINE_COIL_ETERNAL`).
- **Receitas a portar:** Laser Lens Special (Duranium + Excited DTRC → lente + DTR; insumos GT++/NH trocados por
  Quantum Star, circuito UHV e lente comum), Six-Phased Copper (Celestial Tungsten, Astral Titanium, Hypogen,
  Chromatic Glass, Rhugnor, Mellion; a Singularity do Avaritia vira Gravi Stars), DTR a partir dos Excited, e o
  Dimensionally Shifted Superfluid quando houver Radox/água pura (fase posterior).
- **Com a lente (Laser Engraver, lente não consumida):** Celestial Tungsten ← Tungsten, Astral Titanium ← Titanium,
  Chromatic Glass ← vidro, Advanced Nitinol ← Nitinol 60 (Nitinol 60 no Alloy Blast Smelter: Ni + Ti).
- **Itens novos:** `laser_lens_special` e, depois, a Transdimensional Alignment Matrix.
