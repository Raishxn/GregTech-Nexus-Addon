# GTNA Pattern Buffer Task Manager

Status: `COMPLETE`
Branch: `codex/pattern-buffer`

## Goal

Implement a GTNA-native `ME Pattern Buffer` system inspired by GTO/GTL with:

- tiered pattern buffers with capacities `9`, `21`, `32`, and `72`
- per-pattern slot specialization for `item`, `fluid`, and `circuit`
- automatic recipe identification and slot-level recipe cache
- automatic multiblock mode switching when the matched recipe requires a different mode
- persistence of full configuration and stored patterns when the block is broken and placed again
- upgrade items to convert lower-tier pattern buffers into higher tiers without losing configuration
- milestone-based commits and pushes during implementation

## Constraints

- Prefer GTNA architecture over a direct source copy from GTO/GTL
- Keep the first iteration maintainable and testable
- Avoid proxy support until the core pattern buffer is stable
- Preserve player configuration during tier upgrades and block breaking

## Phase 0: Baseline

- [x] Create working branch for feature development
- [x] Commit and push current repository baseline before implementation
- [x] Create a versioned task manager inside the repository

## Phase 1: Architecture

- [x] Define core classes for the feature
- [x] Define serialization strategy for patterns + per-slot config
- [x] Define tier registration strategy
- [x] Define upgrade-item flow preserving NBT/config
- [x] Define multiblock mode switching contract

### Planned core classes

- `GTNAMEPatternBufferPartMachine`
- `GTNAMEPatternBufferPartMachine.InternalSlot`
- `GTNAPatternBufferSlotConfig`
- `GTNAPatternBufferRecipeHandler`
- `PatternBufferUpgradeBehavior`
- `saveToItem/loadFromItem` + custom persisted NBT strategy

## Phase 2: Machine Registration

- [x] Register `Mini Pattern Buffer` tier with `9` slots
- [x] Register tier 2 pattern buffer with `21` slots
- [x] Register tier 3 pattern buffer with `32` slots
- [x] Register final pattern buffer with `72` slots
- [x] Add models, language entries, and tooltips

### Acceptance criteria

- each tier appears in registries and creative tabs
- capacities are visible in tooltips and/or UI
- tier identity is persisted in item/block state properly

## Phase 3: Slot Data Model

- [x] Store encoded processing patterns per slot
- [x] Store specialized item config per slot
- [x] Store specialized fluid config per slot
- [x] Store specialized circuit config per slot
- [x] Store cached recipe identity per slot
- [x] Store preferred/derived multiblock mode per slot

### Acceptance criteria

- slot config survives chunk reload
- slot config survives world restart
- empty slots serialize cleanly without corrupting data

## Phase 4: UI and Slot Interaction

- [x] Build base UI for pattern inventory
- [x] Add per-slot configurator
- [x] Support middle-click on slot for specialization config
- [x] Show configured item/fluid/circuit/mode in UI
- [x] Add clear/reset controls per slot

### Acceptance criteria

- user can edit slot specialization without commands or NBT editing
- middle-click opens or toggles per-slot configuration workflow
- UI clearly distinguishes pattern data from specialization data

## Phase 5: Recipe Matching

- [x] Resolve pattern inputs against GT recipes
- [x] Merge slot specializations into the recipe matching flow
- [x] Cache matched recipe per slot
- [x] Invalidate slot cache when pattern or specialization changes
- [x] Support not-consumable specialized item/fluid/circuit semantics

### Acceptance criteria

- recipes can be identified consistently from slot data
- cache invalidates correctly after edits
- specialized circuit can drive recipe selection

## Phase 6: Multiblock Mode Switching

- [x] Detect machine mode requirements from resolved recipe or slot configuration
- [x] Add safe mode-switch hook for compatible multiblocks
- [x] Switch mode before recipe execution
- [x] Avoid switching when already in correct mode
- [x] Fail gracefully when a target machine does not support mode switching

### Acceptance criteria

- compatible multiblocks can be switched automatically
- unsupported machines continue working without crashes
- mode changes are deterministic and traceable

## Phase 7: Break/Place Persistence

- [x] Save patterns into dropped item NBT
- [x] Save all slot specialization config into dropped item NBT
- [x] Restore contents/config on placement
- [x] Preserve data across Silk Touch / normal break flow if applicable

### Acceptance criteria

- breaking and replacing the block restores the full buffer state
- no silent loss of patterns or slot specialization

## Phase 8: Tier Upgrades

- [x] Register upgrade items/behaviors for each forward tier jump
- [x] Upgrade lower-tier buffer to higher-tier buffer in-world
- [x] Preserve patterns and slot config during upgrade
- [x] Handle capacity increases safely
- [x] Block downgrade or unsupported conversion paths unless explicitly designed

### Acceptance criteria

- upgrades are lossless
- slots beyond old capacity start empty but valid
- upgraded block keeps custom data

## Phase 9: Validation

- [x] Compile project successfully
- [x] Run data generation successfully
- [x] Run automated test task successfully (`NO-SOURCE`)
- [x] Validate serialization, recipe-cache, and mode-switch code paths by build verification
- [x] Generate runtime assets/models/lang entries for new blocks and upgrade items

## Milestone Commits

- [x] Milestone A: architecture + task manager + registration scaffolding
- [x] Milestone B: base machine + serialization + tier registration
- [x] Milestone C: UI + slot specialization
- [x] Milestone D: recipe matching + auto mode switching
- [x] Milestone E: drop persistence + upgrade items + validation

## Notes

- Proxy buffers are intentionally deferred until the standalone buffer is stable
- If GTNA lacks a clean generic mode-switch API, introduce a small GTNA-side interface instead of hardcoding per-machine logic
- If middle-click is awkward in LDLib slot widgets, fallback can be `Shift + Middle Click` or a dedicated config button while preserving the intended workflow
- In-game manual smoke testing is still recommended because the repository currently has no UI/integration test harness

## Post-Implementation Fixes

- [x] Fix recursive cache invalidation crash caused by slot config callbacks
- [x] Search recipes across all supported machine modes before attempting auto mode switching
- [x] Refit the slot configurator into a compact GTL-style docked side panel to remove empty UI space
