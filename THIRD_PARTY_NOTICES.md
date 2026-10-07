# Third-Party Notices

GregTech Nexus Addon (GTNA) ports mechanics, multiblocks and features from legacy GregTech
mods and modpacks to GregTech CEu Modern.

- **GTNA's own code** is licensed under **LGPLv3** (see [`LICENSE`](LICENSE)).
- **Ported content and assets remain under their original licenses**, listed below. The LGPLv3
  license of GTNA's code does **not** relicense or supersede any third-party material.

> **Non-commercial notice:** GTNA includes assets licensed under **CC BY-NC-SA 4.0**
> (non-commercial). As a result, the combined distribution may not be used for commercial
> purposes while those assets are included.

If you are a rightsholder and want an entry corrected or removed, please open an issue.

## Summary

| Project | Source | License | Used content | Status |
|---|---|---|---|---|
| GregTech Odyssey (GTO) | [GregTech-Odyssey/GTOCore](https://github.com/GregTech-Odyssey/GTOCore) | Code: LGPLv3 · Original assets: **CC BY-NC-SA 4.0** | Textures (casings, hatches, storage cores, 1M–256M cell components, wireless energy units, overlays, `gui/overlay/structure_check.png`; Borosilicate Glass inventory icon adapted from the GTO texture), machine mechanics and recipes (reimplemented) | Assets stay CC BY-NC-SA 4.0 — permitted with attribution; tooltip credit requested by the GTO team |
| GTO Extended Platform Presets (GTOEPP) | [GregTech-Odyssey/Gto-Extended-Platform-Presets](https://github.com/GregTech-Odyssey/Gto-Extended-Platform-Presets) | All Rights Reserved | Platform presets (`platforms/epp/sy_1/*`) | **Permission granted** by the GTO team (keep attribution intact) |
| GT: Not Leisure (GTNL) | [ABKQPO/GT-Not-Leisure](https://github.com/ABKQPO/GT-Not-Leisure) | **GPL-3.0** | Multiblock structure files (`.mb`), large steam multiblock family, per-machine controller overlay textures (`textures/blocks/iconsets/*`) | **Permission granted** (credit the source). Structures that GTNL itself took from GTO are credited to GTO |
| Twist Space Technology (TST) | [Nxer/Twist-Space-Technology-Mod](https://github.com/Nxer/Twist-Space-Technology-Mod) | **GPL-3.0** | `eye_of_wood` structure (overworld ore condenser) | **Permission granted** — free to use openly; the TST author asks that the part using a TST structure states **in game** that it is referenced from TST (the `gtna.source.tst` "Source: TST" tooltip line covers this) |
| GTMThings | [liansishen/GTMThings](https://github.com/liansishen/GTMThings) | None declared (All Rights Reserved) | `AdvancedBlockPattern` (basis for `NexusBlockPattern`) | **Pending** — permission requested |
| cmme-additions → Modernity-GTNH | [CristalGaming/cmme-additions](https://github.com/CristalGaming/cmme-additions) → [ModernityGTNH/Modernity-GTNH](https://github.com/ModernityGTNH/Modernity-GTNH) | ARR → **CC BY-NC-SA 4.0** | Plate/ingot textures (triple/quadruple/quintuple, etc.), the **Industrial / Advanced Industrial Steam Casing** textures (`MetaCasing02/1`, `2`) and the **`EM_COMPUTER`** elevator overlay (`gregtech/textures/blocks/iconsets/EM_COMPUTER{,_ACTIVE}`) | **Pending** — permission requested; same author as GTNL per the project owner |
| GTLCore | [nutant233/GTLCore](https://github.com/nutant233/GTLCore) | Declared LGPLv3.0 (`gradle.properties`; no `LICENSE` file found) | Textures (including Pattern Buffer Copy/Cut Card icons), pattern-buffer parity code, **Integrated / Advanced Integrated Ore Processor** (structure + faithful recipe generation) | Attribution (license to confirm) |
| GTLsupb | GTLsupb (LGPLv3) | LGPLv3 | Universal Factory, Primitive Stone Furnace | Attribution only |
| GregTech CEu Modern | [GregTechCEu/GregTech-Modern](https://github.com/GregTechCEu/GregTech-Modern) | LGPL-3.0 | Base API / framework | Attribution only |
| GT5-Unofficial (GTNH TecTech) | [GTNewHorizons/GT5-Unofficial](https://github.com/GTNewHorizons/GT5-Unofficial/tree/a3e1e11241a814c9fa0dd0973d5699548428f689) | LGPL-3.0 | Forge of Gods: rules (`GodforgeMath`, upgrades, data, star colors), structure strings converted to `pattern/god_forge*.mbs`; star/beam GLSL (`shaders/core/godforge_*`), star and beam textures (`textures/render/godforge`), GUI textures (`textures/gui/godforge`), loop sound (`sounds/godforge`), GUI layout and en_US strings (`data/lang/GodforgeLang`, generated) | Licensed reuse with attribution; no individual permission claimed |
| PersonalSpace (GTNH) | [GTNewHorizons/PersonalSpace](https://github.com/GTNewHorizons/PersonalSpace/tree/a292401e0a067e37e7e02abee8bd48b58a6e1571) | LGPL-3.0 | Personal dimension port for 1.20.1: `DimensionConfig` settings and preset strings, chunk provider (layers, lots, streets, boundaries, center marker, surface layers), portal/teleport/relink logic, `/pspace` commands, the editor GUI and its widget toolkit, and the GUI sheet `assets/gtna/textures/gui/personalspace/widgets.png` (copied unchanged from `assets/personalspace/textures/widgets.png`, SHA-256 `68db48c6826c3cb46df9d83eff1bc7d56ec73cb679281f21d609a352ab70a97d`) | Licensed reuse with attribution under LGPL-3.0; no individual permission claimed |
| PersonalSpace Unofficial | [Crazerium/PersonalSpace-Unofficial](https://github.com/Crazerium/PersonalSpace-Unofficial/tree/dab12d35325aef2c65b79e9a79ef0025f1274fa4) | LGPL-3.0 | Forge 1.20.1 chunk generator API structure used as implementation reference; its ownership and simplified road rules are not adopted | Licensed reuse with attribution; no individual permission claimed |
| Infiniverse | [Commoble/infiniverse](https://github.com/Commoble/infiniverse) | MIT | Forge 1.20.1 runtime dimension API dependency for Personal Space | Separate required mod; no source or assets copied |

## Notes

### Same-author project

The iterative AE2 crafting planner in GTNA was adapted from **RaishxCore**, another mod by the
GTNA author, from AE2 1.21.1 to AE2 1.20.1. RaishxCore declares LGPL-3.0-or-later. The Nexus ME
Hypercore multiblock itself is original to GTNA and has no source attribution in its tooltip.

### GregTech Odyssey (GTO)

- The GTO team confirmed that **original code** (`src/main/java` of GTOCore) is **LGPLv3**, while
  **original textures/assets are CC BY-NC-SA 4.0** and must stay under that license.
- GTO also asked that ported content **credit the original addon in tooltips** (as GTNH and GTO do
  between themselves). GTNA implements this through `GTNASources`.
- The Fishing Ground's compressed `pattern/gto/fishing_ground.mbs` and the Aluminium Bronze /
  Stainless Evaporation casing textures come from GTOCore. The Generator Array, Fishing Ground and
  Evaporation Plant behavior, structures and recipes are adapted from GTOCore source at commit
  `dc4824d`.
- The Greenhouse's compressed `pattern/gto/greenhouse.mbs`, light behavior, controller recipe and
  crop recipes are adapted from the same GTOCore commit. Its nine Rich Soil positions also accept
  vanilla Mud, allowing the structure to work without Farmer's Delight installed.
- The Component Assembler's base structure, tier matching, and LV–IV component batch recipes are
  adapted from GTOCore at commit `dc4824d`. Its LV–IV casing and Multi Functional Casing textures
  (including the connected texture sheet) are from GTOCore and retain CC BY-NC-SA 4.0 attribution.
- The Component Assembler's large extension (both `addSubPattern` layers of
  `MultiBlockC.java:328-397`), the LuV–UV batch family, the extension casing production and the
  extension's tier cap are adapted from GTOCore at commit `dc4824d`. The `THREE_PROOF_COMPUTER_CASING`,
  `MACHINING_CONTROL_CASING_MK2`, `ENERGY_CONTROL_CASING_MK2`, `ELECTRIC_POWER_TRANSMISSION_CASING`
  and `TITANIUM_NITRIDE_CERAMIC_IMPACT_RESISTANT_MECHANICAL_BLOCK` textures (with their connected
  texture sheets and `.png.mcmeta`; the two MK2 control casings are sided) and the LuV–UV casing
  textures are from GTOCore and retain CC BY-NC-SA 4.0 attribution. The `CarbonFiberPolyphenyleneSulfideComposite`
  and `TitaniumNitrideCeramic` material definitions are copied 1:1 (minus GTO-only flags). GTOCore's
  MK2 control-casing recipes are Precision Assembler recipes of an excluded machine, so GTNA adds
  documented alternative Assembler routes.
- The `component_assembly_line`'s compressed `pattern/gto/component_assembly_line.mbs`, uniform tier
  rule and LuV–UV batch gating are adapted from GTOCore at commit `dc4824d`. The
  `component_assembly_line_casing_lv..uv` and `iridium_casing` textures (with their connected sheets
  and `.png.mcmeta`) are from GTOCore and retain CC BY-NC-SA 4.0 attribution; the UHV–MAX casings are
  out of scope. GTOCore's nine GTO-only structure casings and its cross-recipe execution are
  substituted by documented GTNA/GTCEu equivalents (see the ledger G-0110 table); the iridium casing
  is ported from GTO with its Assembly Line recipe (GTO's Tanmolyium plate copied 1:1), and the
  controller recipe is omitted because it needs the GTO-only Advanced Assembly Line chain. The ZPM/UV
  casing recipes substitute GTO's Pikyonium/ArtheriumTin/AbyssalAlloy solders with obtainable
  GTNA/GTCEu equivalents (ledger G-0114).
- The Blaze and Cold Ice two-layer casings, including the emissive `_bloom` overlays and their
  connected sheets and animation metadata, and the MK2 control casings' `side_bloom` overlays are from GTOCore and retain
  CC BY-NC-SA 4.0 attribution. `naquadah_alloy_casing` reuses the GTO `hyper_mechanical_casing` sheet
  (the earlier GTNA copy used the water-purification sheet by mistake).
- The Grinding Ball Hatch's `ball_hatch_idle` and `ball_hatch_spinning` rotor sprites and animation
  metadata are copied from GTOCore under CC BY-NC-SA 4.0; the renderer is reimplemented for GTCEu 7.5.3.
- The Large Greenhouse's compressed structure, dual greenhouse/tree recipe modes, controller recipe,
  and tree growth family are adapted from GTOCore at commit `dc4824d`.
- The Blaze Blast Furnace's compressed structure, molten Blaze upkeep and controller recipe are
  adapted from GTOCore at commit `dc4824d`. Its Blaze Casing texture and connected texture sheet
  retain the original CC BY-NC-SA 4.0 asset license. GTNA crafts that casing in the Large Chemical
  Reactor with the same inputs because the original Reaction Furnace is excluded from this port.
- The Cold Ice Freezer's base structure, liquid Ice upkeep, casing/controller recipes and Cold Ice
  Casing texture (including its connected texture sheet) are adapted from GTOCore at commit
  `dc4824d`. The textures retain CC BY-NC-SA 4.0 attribution.
- The Chemical Plant's compressed `pattern/gto/chemical_plant.mbs`, structure, coil efficiency
  behavior and controller recipe are adapted from GTOCore at commit `dc4824d`. GTO applies its
  coil bonus through the closed-source gtolib `coilReductionOverclock`; GTNA reproduces the
  behavior the controller displays (5% EU and duration reduction per coil tier plus a perfect
  overclock). The controller reuses GTCEu's Large Chemical Reactor front overlay, which shares the
  same inert-PTFE casing.
- The Mega Alloy Blast Smelter's structure, 0.8× EU / 0.6× duration bonus and Parallel Hatch are
  adapted from GTOCore at commit `dc4824d`. It reuses GTCEu's GCYM casings and `ALLOY_BLAST_RECIPES`.
  GTO's tiered integral-framework cell and GCYM ability predicate are substituted by a TungstenSteel
  frame and the standard auto abilities because GTNA does not port that tier-block system.
- The ISA Mill's compressed `pattern/gto/isa_mill.mbs`, perfect overclock, grinding-ball gate and
  durability formula, the 48 wet-grinding recipes, the Inconel-625 casing/gearbox/pipe recipes, the
  two grinding-ball Forming Press recipes, the Grinding Ball Hatch and the Assembly Line controller
  recipe are adapted from GTOCore at commit `dc4824d`. Its Inconel-625 casing, gearbox, pipe and
  `ball_hatch` overlay textures, the two grinding-ball item textures and the `milled` material item
  texture/model are from GTOCore and retain CC BY-NC-SA 4.0 attribution. The Inconel-625, Inconel-792
  and Tantalloy-61 material definitions are copied from GTO's `MaterialA` so those recipes stay
  faithful; GTCEu's automatic alloy-blast/EBF/mixer generation produces them.
- GTOCore itself includes textures from other mods; see
  [GTO's `THIRD_PARTY_LICENSES.md`](https://github.com/GregTech-Odyssey/GregTech-Odyssey/blob/main/THIRD_PARTY_LICENSES.md).
- The Rocket Large Turbine's structure and non-mega `TurbineMachine` behavior (base
  `V[EV] * 2.5` output, rotor speed/voltage math, high-speed mode and the rocket engine module
  bonus) are adapted from GTOCore at commit `dc4824d`. It reuses GTCEu's titanium casings, gearbox,
  rotor holder, rotors and `RocketFuel`, so no GTO casing is copied. Its high-speed-mode GUI toggle
  texture (`gui/overlay/high_speed_mode.png`) is from GTOCore and retains CC BY-NC-SA 4.0
  attribution.
- The EV, IV and LuV Rocket Engine generator textures and their shaped crafting recipes are adapted
  from GTOCore at commit `dc4824d`. GTNA uses GTCEu's `SimpleGeneratorMachine` implementation and
  the already ported Rocket Fuel recipe; the cable ingredients refer to GTCEu's cable blocks.
  The generator textures retain CC BY-NC-SA 4.0
  attribution.
- The Component Assembly Line's Molecular, Boron Carbide, Precision Processing, Advanced Assembly
  Line, Chemical Corrosion Resistant Pipe, Circuit Assembly Line, Spacetime Assembly Line and
  Pressure Containment casing textures, connected sheets and animation metadata are from GTOCore
  at commit `dc4824d` and retain CC BY-NC-SA 4.0 attribution. Their structure positions follow
  GTOCore. Crafting inputs that require GTO-only materials or production machines are adapted to
  GTNA/GTCEu materials in `GTNABlockRecipes`. The Component Assembly recipe layout
  (`ui/recipe_type/component_assembly.rtui`) is copied from GTOCore under GTNA's namespace; its
  two Component Assembly Line progress bar textures are copied under `assets/gtceu` because the
  original binary layout references those resource paths.
- The Supercritical Steam Turbine's structure and non-mega `TurbineMachine` behavior (base
  `V[IV] * 2` output, rotor speed/voltage math, high-speed mode and the supercritical module bonus),
  its controller and casing Assembler recipes and its fuel recipe are adapted from GTOCore at commit
  `dc4824d`. GTNA substitutes its own `DenseSupercriticalSteam` for GTO's `SupercriticalSteam` in
  the fuel recipe (same 80 mB → 8 mB distilled water / 30 ticks / `V[MV]` numbers). Its
  `supercritical_turbine_casing` texture, connected texture sheet and `.png.mcmeta` are from
  GTOCore and retain CC BY-NC-SA 4.0 attribution.
- The Industrial Flotation Cell and Vacuum Drying Furnace pair (structure, machine behavior, recipe
  types and recipes) is adapted from GTOCore at commit `dc4824d`
  (`MultiBlockA.java:1678`/`:1707`, `pattern/industrial_flotation_cell.mbs`,
  `pattern/vacuum_drying_furnace.mbs`, `classified/FlotatingBeneficiation.java`,
  `classified/VacuumDrying.java`, `classified/Dehydrator.java`, the casing recipes of
  `classified/Assembler.java`, the flotation controller of `classified/AssemblyLine.java` and the
  red-mud neutralisation of `processing/StoneDustProcess.java`). The `hastelloy_n_75_casing`,
  `hastelloy_n_75_gearbox`, `hastelloy_n_75_pipe`, `flotation_cell` and `red_steel_casing`
  textures (with their connected texture sheets and `.png.mcmeta`) are from GTOCore and retain
  CC BY-NC-SA 4.0 attribution. The Hastelloy-N75 and Stellite material definitions are copied 1:1
  from GTO's `MaterialA` so the casing and controller recipes stay faithful; GTCEu's automatic
  alloy-blast/EBF/mixer generation produces them.
- The IV Dehydrator machine and the Vacuum Drying Furnace's original controller recipe are adapted
  from GTOCore `GTOMachines.java`, `MachineRecipe.java` and `classified/Assembler.java` at commit
  `dc4824d`. The six Dehydrator overlay textures are copied from GTOCore and retain CC BY-NC-SA
  4.0 attribution.

### GT: Not Leisure (GTNL)

- The GTNL author granted permission to use GTNL structures with source attribution.
- The same permission covers GTNL's own per-machine controller overlay textures, ported under
  `assets/gtna/textures/block/multiblock/*` (from GTNL `textures/blocks/iconsets/*`).
- Structures that GTNL itself ported from GTO are credited to **GTO**, whose assets are used under
  **CC BY-NC-SA 4.0** (permission confirmed by the GTO team).
- **Steam Elevator overlay:** GTNL renders the elevator front with `gregtech:iconsets/EM_COMPUTER`
  (`BlockIcons.OVERLAY_FRONT_TECTECH_MULTIBLOCK`), a **GT5U/Tectech** icon that GTNL references from
  GregTech's resource domain but does **not** vendor in its own repository. Since the GTNL repo has
  no elevator overlay to port, GTNA uses the same icon as retextured by the Modernity-GTNH pack
  (already a source of this project) under `assets/gtna/textures/block/multiblock/steam_elevator/`.

### Attribution in tooltips

GTNA appends a `Source: <addon>` line to the tooltips of ported machines/items. The mapping lives in
`src/main/java/com/raishxn/gtna/common/data/GTNASources.java`.

### Void Miner essence chain (2026-09-29)

The 40 vein compositions and seed/incubation mechanics were verified against the author's
local Prism instance `GregTech-Leisure-1.4.5.1`, `kubejs/server_scripts/gtceu.js:6255–6729`.
The GTNA implementation is native Java, with explicitly adapted EV costs, Raw Ore outputs,
stock-material substitutions and five base Ad Astra planet programs. The 5×5×5 Incubator
structure and scanner recipe map/tank capacity follow GTLCore `gtl-1431-skyblock`, commit
`18c7814`; machine tooltips credit GregTech Leisure through `GTNASources`.

Essence/Essence Seed icons, vein icons in `assets/gtna/textures/item/essence/`, and the
World Data Scanner overlay come from the local GTOCore checkout under
`src/main/resources/assets/gtocore/textures/{item,block/machines/world_data_scanner}`.
They retain **CC BY-NC-SA 4.0**, the original asset license confirmed by the GTO team above.
World-specific vein variants reuse GTO's corresponding generic icon. Planet essence items
reuse the GTO Essence icon. No assets from the pack's unconfirmed asset license were copied.

### Discovered-fluid drilling (2026-09-29)

The Void Fluid Drilling Rig tower follows GTLCore's 3×7×3 drilling pattern
(`MultiBlockMachineAStructureA.java`, local `gtl-1431-skyblock` reference). GTNA uses
Titanium/Steel materials and new native discovery certificates, configurable recipes and
remote-production gates. GTL's terrestrial fluid proportions informed the prototype, with
explicitly reduced production rates and added reagents. Machine tooltips credit GTL.
No GTL/GTO textures were copied for this system: models reference existing GTCEu casing,
drilling-rig, scanner, Data Stick, Data Orb and fluid-filter resources.
The optional GTIA Mars deposit example uses the numeric reference in GTOCore's
`GTOBedrockFluids.java`; it is pack-owned data, not default GTNA world generation.

## Eye of Harmony content foundation (2026-10-04)

The 27 tiered field blocks, three structural casings, three Planet Blocks and Astral Array
Fabricator are new physical components. Existing machine behavior and published casing IDs
are preserved in this foundation stage. No external source code was copied verbatim.

- **GTNH / GT5-Unofficial**, revision `a3e1e11241a814c9fa0dd0973d5699548428f689`:
  field identity, tier names, casing roles and Astral Array reference. Original field textures
  missing from Modernity, plus the original Astral Array sprite/animation and other fallbacks,
  retain the repository's LGPLv3 license. Source: [GT5U](https://github.com/GTNewHorizons/GT5-Unofficial/tree/a3e1e11241a814c9fa0dd0973d5699548428f689).
  `EyeOfHarmonyMath.java` adapts the serial duration, startup/return energy, gas overflow,
  chance, yield, failure fluid and pity rules from `MTEEyeOfHarmony.java`,
  `EyeOfHarmonyRecipe.java` and `EyeOfHarmonyRecipeStorage.java`. This adaptation retains
  LGPL-3.0 attribution; it isolates pure calculations, uses immutable plans and an explicit
  supplied roll, and leaves the modern ore/plasma catalog and world transactions separate.
  `EyeOfHarmonyOverworld.java` also adapts `processDimension`, `processHelper`, dust/plasma
  eligibility and plasma-energy calculation from `EyeOfHarmonyRecipe.java`. Modern GTCEu
  ore definitions, OreProperty values and loaded plasma fuel recipes replace the legacy wrappers;
  differences and the retained 2.25 normal-vein flow are documented in the operation audit.
  The controller implements GTNA-specific persisted transactions and bounded pending delivery.
- **Modernity-GTNH**: corresponding field, casing, controller overlay and Planet Block textures
  are used where available. Their PNG/animation bytes are unchanged; resource paths were relocated
  into the GTNA namespace. These assets retain **CC BY-NC-SA 4.0**, as declared by the source
  repository. This is licensed reuse, not a claim of an individually granted permission.
- **GTNH NEI Ore Plugin**: Planet Block dimension identity and six-face naming convention are
  referenced from revision `2ccf8c43d8d25b10239f5bcc48e5be9ac4a0ab65`. Its license notice declares
  GTNH modifications LGPLv3-or-later and retains the original MIT notice (JJN, 2016).
  Source: [GTNEIOrePlugin](https://github.com/GTNewHorizons/GTNEIOrePlugin/tree/2ccf8c43d8d25b10239f5bcc48e5be9ac4a0ab65).

Exact source paths, repository revisions, licenses, modifications and SHA-256 hashes for all
51 imported textures and their animation files are recorded in
[`docs/roadmap/eye-of-harmony-assets.json`](docs/roadmap/eye-of-harmony-assets.json).
The Modernity candidates matched the author's local `Modernity-GTNH-2026-09-07` files byte for byte.
Repository license texts are bundled in `META-INF/licenses/eye_of_harmony/` inside the jar.
Attribution appears on the component tooltips through `GTNASources`.

The new overlays are staged for the later controller-rendering milestone and are not yet bound
to the existing controller. CTM atlases/configurations were not imported; block animations keep
original frame timing. These notes do not change the permission status of unrelated Modernity
assets listed elsewhere in this file.

Modernity source revision: [1b52340349b92a11676eefb149eaec3b934eafcf](https://github.com/ModernityGTNH/Modernity-GTNH/tree/1b52340349b92a11676eefb149eaec3b934eafcf); [license](https://github.com/ModernityGTNH/Modernity-GTNH/blob/1b52340349b92a11676eefb149eaec3b934eafcf/LICENSE.txt).

### Eye of Harmony structural stage (2026-10-04)

The existing 33³ geometry was checked against every cell of the same pinned GT5U controller;
`docs/roadmap/eye-of-harmony-structure-audit.json` records its source hash, symbol mapping and
coordinate conversion. The new GTNA matcher requires uniform field tiers per family and the
reference's five ports. GTCEu's finite ME buffers are distinguished from its stocking parts;
stocking, crafting buffers/proxies, dual and energy hatches are excluded from the structure.

Core material identities and liquid temperatures follow `gregtech/loaders/materials/MaterialsInit.java`
and `gregtech/common/GTProxy.java` in that revision. Seven materials are registered with explicit
future processing: SpaceTime, RawStarMatter, White/Black Dwarf Matter, Universium, Time and Space.
GTCEu's existing material icons/models are used in this stage; no additional external artwork
was imported. GTNH's special material renderers and private-font galaxy glyph are not ported.
New fabrication recipes are deferred at the author's request. The controller still runs its
previous cosmos simulation until the planetary-operation stage; structural tier reading alone
does not establish operational parity.

### Eye of Harmony presentation and Nexus Terminal (2026-10-04)

The controller now binds the previously attributed inactive/active Eye of Harmony images as
front overlays; the copies and their origin are recorded in `eye-of-harmony-assets.json`.
The renderer reuses the existing attributed GTNA/GTO model assets with corrected transforms
and planetary-cycle state; no additional external artwork was imported in this stage.

Nexus Terminal's three-panel settings/category/tier workflow was studied from GTO's installed
`gtocore-forge-1.20.1-26.9.5.jar` (`com.gtolib.gtm.AdvancedTerminalBehavior`) and GTOCore's
`common/block/BlockMap.java` at revision `dc4824d`. Its native uipro implementation is not
imported: GTNA implements the interface using GTCEu 7.5.3/LDLib widgets and its own persisted
settings and pattern builder. The purple/black theme is retained.

### Eye of Harmony casing binding correction (2026-10-04)

The initial import mistakenly associated the casings with unrelated `BlockGTCasingsTT`
textures. The corrected bindings follow `tectech/thing/casing/BlockGTCasingsBA0.java` in
the same pinned GT5-Unofficial revision: metadata 10 (Temporal) uses
`EM_INNER_SPACETIME_REINFORCED_EOH_CASING`, metadata 11 (Spatial) uses
`EM_OUTER_SPACETIME_REINFORCED_EOH_CASING`, and metadata 12 (Boundary) uses
`EM_POWER_INFINITE`. The three unmodified upstream PNGs replace the initial files;
there are no matching candidates in the inspected local Modernity resource pack.
LGPL-3.0 source paths and new file hashes are recorded in the assets manifest.

### Eye of Harmony native EMI integration (2026-10-04)

The new recipe viewer uses EMI's published Forge API 1.1.13 for Minecraft 1.20.1
(`maven.modrinth:emi:1.1.13+1.20.1+forge`), as an optional compile dependency, not bundled
inside GTNA. Widget/API contracts were checked against its official source artifact.
The catalog, presentation and pagination are GTNA implementations; no external UI art
was imported. JEI and EMI share the same planetary catalog and information renderer.
The Terminal Battery selector follows GTOCore BlockMap's PSS_BATTERIES catalog,
using GTCEu 7.5.3 registered capacitor blocks instead of a name-based block search.

### Eye of Harmony viewer QA references (2026-10-05)

GTLCore `gtl-1431-skyblock`, local commit `18c7814`, was inspected for its separate
Cosmos Simulation item/fluid capacities and independent multiblock preview widget.
No GTLCore source or artwork was copied for these viewer changes. The optional
EOH-only EMI screen sizing hook was written for EMI 1.1.13 using its published source
contracts. The author requested plasma outputs of eight million buckets; this is a
GTNA quantity adaptation, not a claim of unchanged GTNH output units or balance.

### GTNA energy rebalance (G-0167)

At the author's request, Eye of Harmony startup scaling now uses `(circuit+1)^2`
instead of the GTNH `4^circuit` formula. Artificial Star's four fuel recipes generate
16 times their previously ported EU/t. These are GTNA balance adaptations; upstream
source attribution remains intact. No upstream code or assets were newly copied.

### GTLAdditions network terminals (G-0169)

Behavior adapted from local GTLAdditions master commit
`8caff5e93a5e65914d10dd176d48d66e7ec8c329` (GTLAdditions 3.2.8Custom-fix2):
`common/machine/multiblock/part/WirelessEnergyNetworkTerminalPartMachineBase.kt`,
`WirelessEnergyNetworkTerminalPartMachine.kt`, `common/machine/trait/NetworkEnergyContainer.kt`
and registration in `common/machine/GTLAddMachines.kt`. Adapted Java implementation:
`NexusNetworkTerminalPartMachine.java` and `NexusNetworkEnergyContainer.java`, under
GPL-3.0-only; bundled upstream license `META-INF/licenses/gtladditions/GPL-3.0.txt`.
The upstream LICENSE is GPL-3.0 while gradle.properties declares LGPLv3.0: these
adapted files retain the explicit LICENSE terms, without claiming special permission.
GTLCore/GTMThings-specific code, machine families and assets were not imported.
Models use existing GTNA/GTO overlays and GTCEu voltage casing assets with existing
attribution. Recipes use GTNA/GTCEu ingredients instead of unavailable GTL/KubeJS items.
GTNA-specific changes include Nexus matrix capacity/dimension/loss, simulation quotes,
BigInteger-safe sums, correct one-amp metadata and aggregate nominal voltage budgeting.
Source attribution is included in GTNASources and in-game tooltips.

### GTLAdditions terminal visual assets (G-0170)

At the author's request, terminal casing and animated overlay now use the GTLAdditions
assets at commit `8caff5e93a5e65914d10dd176d48d66e7ec8c329`: `wireless_terminate_casing.png`
and `wireless_energy_network_terminal.png`, their animation metadata, and the terminal
front-overlay model. PNGs/animation metadata are copied unchanged; the model namespace
is changed to gtna and a base cube/overlay offset is added for GTCEu 7.5.3.
Source/destination hashes and adaptations: `docs/roadmap/gtladditions-wireless-assets.json`.
Retain upstream LICENSE GPL-3.0 terms (bundled in META-INF/licenses/gtladditions);
metadata LGPL discrepancy remains documented. No special permission is claimed.

### Eye of Harmony / Forge of Gods material sprites (2026-10-07)

- The item and material-block PNGs under `assets/gtceu/textures/{item,block}/material_sets/`
  for SpaceTime, Universium, White Dwarf Matter, MHDCSM, Magmatter, Graviton Shard,
  Infinity and Eternity were copied from the author's local Modernity-GTNH pack, whose
  source revision is `1b52340349b92a11676eefb149eaec3b934eafcf` and whose art is
  **CC BY-NC-SA 4.0**. The filenames were mapped from GTNH material icon names to GTCEu
  names. Animation metadata was preserved. Corresponding GTCEu models and transparent
  secondary layers were generated locally. Infinity and Eternity are prepared texture
  sets; no GTNA material registration is claimed for them.
- Black Dwarf Matter item sprites and its block sprite came from GTOCore revision
  `dc4824d1608ffad3bb0e2d53a2a068a739da3e84`, from its
  `black_dwarf_mtter` set. These are **CC BY-NC-SA 4.0** GTO assets, renamed to
  `black_dwarf_matter` for GTNA. GTO has no corresponding frame or dense-plate sprite;
  those forms use the GTCEu parent set.
- The new EOH construction recipes are a GTNA adaptation using GTCEu components.
  They do not reproduce GTNH's BEC material, nanite or condensate requirements.
