# Changelog

## [0.3.2-dev] - 2026-04-25

### Added
- **Primitive Distillation Tower**:
  - Added the primitive steam distillation tower controller with GT-Not-Leisure-inspired structure behavior.
  - Added MV-and-below recipe restriction and 75% steam consumption behavior.
  - Added tooltip text explaining the 6-fluid-output limit and steam efficiency.
- **Dimensionally Transcendent Steam Line**:
  - Added fixed two-thread processing support to the `Dimensionally Transcendent Steam Oven`, allowing two different furnace recipes to run at the same time.
  - Added clearer tooltips for the Dimensionally Transcendent steam machines, including threads, speed, parallelism, and structure notes.
- **Recipes and Progression**:
  - Added missing recipes for coordinate cards, additional pattern buffer tiers, and thread hatches through UHV.
  - Added generated models/assets for the new steam-era machines, casings, and the primitive spacetime distortion device item.
- **Eye of Wood**:
  - Added dedicated Eye of Wood structure data, renderer support, localization, and richer player-facing tooltip text based on the original Twist Space Technology behavior.

### Changed
- **Primitive Distillation Tower**:
  - Reworked the structure to match the GT-Not-Leisure primitive tower layout: 3x3 steel firebox base, five hollow steel hull layers, and a closed steel hull top layer.
  - Fixed the multiblock preview so the tower renders upright instead of lying horizontally.
  - Restricted steam handling to normal steam input or wireless steam input and removed wireless steam output acceptance from the tower structure.
- **Large Steam Multiblocks**:
  - Fixed multiple controller facings that were turned inward, including Large Steam Lathe, Cutting Machine, Extractor, Forming Press, Hammer, and Ore Washer cases.
  - Fixed inverted or swapped structure blocks in several previews/patterns, including Large Steam Hammer and Large Steam Compressor glass/frame placement.
  - Replaced incorrect Solid Machine Casing requirements with Steam Machine Casing where appropriate.
  - Added or expanded Large Steam tooltips with speed, efficiency, parallel, and structure information.
- **Eye of Wood**:
  - Changed the structure requirement from Solid Machine Casing to Steam Machine Casing.
  - Fixed the controller overlay/render path that caused the controller to show as black/pink.
  - Expanded the tooltip to explain water/lava storage, success chance, 60-second runs, ore outputs, and steam venting on failure.
- **Primitive Man's SpaceTime Distortion Device**:
  - Reduced the item model scale without changing the multiblock render.
  - Switched the item texture to the requested `22.png` source texture.
- **Steam Solar Boilers**:
  - Adjusted the Mega Pressure Solar Boiler preview orientation to match the horizontal solar-boiler style better.
- **Custom Hatches**:
  - Changed Overclock Hatch behavior to modify the machine's overclock curve per 4x EU/t instead of acting as a final duration multiplier.
  - Kept Accelerate Hatch as a final post-overclock recipe duration reduction.
  - Updated Overclock Hatch and Accelerate Hatch tooltips to explain the difference clearly for players.

### Fixed
- Fixed Primitive Distillation Tower preview/pattern registration issues that caused `Pattern formed checking failed: gtna:primitive_distillation_tower` during client startup.
- Fixed several Large Steam structures incorrectly accepting wireless steam output hatches where only normal outputs or steam inputs should be valid.
- Fixed missing or overly simple tooltip coverage for newly added steam multiblocks.
- Fixed GTCEu standard electric multiblocks applying Overclock Hatch duration reduction even when no real energy overclock was available.

### Tested
- Ran `compileJava`, `runData`, and `runClient` after the latest multiblock and hatch changes.
- Confirmed the client starts without a Primitive Distillation Tower pattern failure after the upright preview fix.
- Ran `compileJava` after correcting Overclock Hatch scaling on GTCEu standard electric multiblocks.

## [0.3.1] - 2026-04-23

### Added
- **Eye of Wood**:
  - Added the `Eye of Wood` multiblock as a wood-and-bronze early proto-singularity machine.
  - Added dynamic water/lava-fed success logic with steam venting on failed rolls.
  - Added processed ore dust output bundles and an in-machine success chance display.
  - Added crafting recipe, config toggle, and runtime localization for the controller.
- **Steam Expansion Follow-up**:
  - Added `Large Steam Circuit Assembler`, `Large Steam Mixer`, `Large Steam Centrifuge`, `Large Steam Thermal Centrifuge`, `Large Steam Bath`, `Large Steam Storage Tank`, `Large Steam Solar Boiler`, `Dimensionally Transcendent Steam Boiler`, and `Dimensionally Transcendent Steam Oven`.

### Changed
- **Steam Controller Coverage**:
  - Expanded machine recipes and language entries so the newer steam multiblocks are fully exposed in-game.

## [0.3.0] - 2026-04-16

### Added
- **Steam Logistics Expansion**:
  - Added `Wireless Steam Input Hatch` and `Wireless Steam Output Hatch` in bronze and steel variants.
  - Added `Huge Steam Input Bus` and `Huge Steam Output Bus` for higher-throughput steam multiblocks.
  - Added `Infinite Steam Input Bus` for creative and testing workflows.
  - Added `Output Boost Steam Output Bus` with tier-based output multiplication.
- **Infinity Covers**:
  - Added `Steam Infinity Cover` for feeding singleblock steam machines without a conventional boiler chain.
  - Added `Electric Infinity Cover` for singleblock electric machine testing and automation setups.
- **Nexus Machine Expansion**:
  - Added `Nexus Assembly Forge` multiblock.
  - Added pattern-aware internal batching and craft pattern hatch aggregation for the Nexus forge UI and processing flow.
  - Added `Nexus Structure Terminal` to support multiblock structure handling.
- **Large Steam Multiblocks**:
  - Added `Large Steam Hammer`.
  - Added `Large Steam Compressor`.
  - Added `Large Steam Extractor`.
  - Added `Large Steam Ore Washer`.
  - Reused reference-inspired multiblock layouts based on the addon structure files already bundled in the project.
  - Hooked controller models to GTCEu overlays to keep the new steam line visually aligned with the rest of the pack.

### Changed
- **Steam Progression**:
  - Expanded the early steam roster with stronger multiblock options aimed at reducing beginner grind while keeping steam-age identity.
  - Added adjustable parallel controls to the new `Large Steam` machines so throughput can be tuned in-machine.
- **Recipe Coverage**:
  - Added crafting and assembler coverage for the new infinity covers.
  - Added machine crafting recipes for the new `Large Steam` controller blocks using the addon's hydraulic and precision steam component progression.
- **Nexus Forge UX**:
  - Improved the `Nexus Assembly Forge` display text to surface craft-pattern hatch count and loaded pattern totals directly on the machine.
- **Config and Gameplay Controls**:
  - Expanded `gtna` config entries with grouped client, gameplay, machine, and Nexus balancing options.
  - Added restriction messaging for Journey mode and `Self Restraint`-gated items to better surface when recipes are intentionally disabled.

## [0.2.0] - 2026-04-01

### Added
- **Nexus Wireless Energy Network**: 
  - Automated player-binding for wireless hatches upon placement.
  - New shift-right-click unbind mechanic using the Nexus Linker.
  - Synchronized energy storage data between the controller UI and the Quantum Terminal.
  - Visual locator for network connections using a custom Tesselator-based rendering system (replacing Jad/Highlight dependency).

### Fixed
- Fixed data-gen failures on the Wireless Recipes by resolving Registry lookups directly from the `GTMachines` array values instead of strings.
- Added strict backpressure in `NexusEnergyNetwork` and output-hatches to prevent infinite internal buffering or voiding in filled networks. 
- Aligned energy capacity calculations to directly respect GTCEu tier formulas.
- Resolved localized plugin crashes in Jade tooltips (`en_us.json`).

### Changed
- Converted all specific hard-coded item ingredients in Wireless hatch crafting to TagPrefix representations (`circuit`, `cableGtHex`, `plate`).
