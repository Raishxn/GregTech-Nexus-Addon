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
