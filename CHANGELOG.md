# Changelog

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
