# Changelog

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
