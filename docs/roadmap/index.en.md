# Public roadmap — GregTech Nexus Addon

Updated **2026-09-30**. Development baseline: **Minecraft 1.20.1 / GTCEu 7.5.3 / GTNA 0.5.1**.

GTNA expands steam industry, electric factories and GregTech automation with multiblocks,
wireless networks, modules and AE2 integration. Configuration lets modpacks adapt these
systems to their own progression.

## Reading the roadmap

- **Implemented:** present in current development; may not be in a published release yet.
- **In validation:** covered by tests, with in-game confirmation or balancing still needed.
- **Planned:** a next task; checked only after implementation and verification.
- **Under consideration:** scope remains undecided; not a promised delivery.

Priorities may change with testing. No release dates are committed. This page covers
major systems rather than listing every machine.

## Implemented

- [x] Wireless steam and energy networks, monitoring and Nexus Flux Matrix.
- [x] Steam and Large Steam machines, hydraulic components and Steam Elevator modules.
- [x] Nexus Structure Terminal previews and construction for registered multiblocks/modules.
- [x] Auxiliary modules on compatible electric machines and an ore processing chain.
- [x] Acceleration, overclock, parallel and concurrent-recipe hatches on compatible machines.
- [x] ME Pattern Buffers with recipe modes, configurable capacity and upgrades that preserve data.
- [x] Universal Factory with configurable rules for sharing resources between recipes.
- [x] Electric Void Miner precise/random modes, essences, World Data Scanners and Incubator.
- [x] Void Fluid Drilling Rig: actual extraction unlocks reusable discovery data and remote upgrades.
- [x] JEI/Jade integration, English/Portuguese translations and automated tests for major systems.

## Now — stabilize the content being tested

- [ ] **GTNA-01 · Void mining:** confirm both modes, revised times, Accelerate Hatch and more than two parallels in game; check consumption and full outputs.
- [ ] **GTNA-02 · Fluid discovery:** verify human prospecting, extraction, oil recording and Mars → Radon → remote production in the GTIA profile; clarify the instructions.
- [ ] **GTNA-03 · Interfaces:** check scanner names, Jade chance summaries, scrolling large JEI output pools and different GUI scales.
- [ ] **GTNA-04 · Teams:** test discovery sharing and access limits with two real players, including leaving and rejoining a team.
- [ ] **GTNA-05 · Data safety:** retest Pattern Buffer upgrades, full/unpowered ME networks, world reloads and capacity migration without loss or duplication.
- [ ] **GTNA-06 · Wireless energy:** verify loss is applied once, cross-dimension transfers and recovery of existing worlds across configurable profiles.

**Completion:** record the outcomes and fix item loss, duplication, crashes and incorrect
costs. Automated tests complement client and multiplayer-server validation.

## Next — integration, balancing and documentation

- [ ] **GTNA-07 · Real factories:** measure AE2 autocrafting, concurrent recipes and performance under load; check native CPUs alongside Nexus systems.
- [ ] **GTNA-08 · Multiblocks/modules:** expand checks for orientation, formation, previews and hatch support on existing machines.
- [ ] **GTNA-09 · Pack configuration:** complete cost, yield, parallel and enable/disable controls where verified needs arise; document defaults and examples.
- [ ] **GTNA-10 · Progression content:** review Steam, electric and component recipes, costs and yields, prioritizing practical machines.
- [ ] **GTNA-11 · Guides/translations:** update construction/use instructions and keep languages and tooltips consistent with actual behavior and limits.

**Completion:** documented machines and settings should reproduce observed factory
behavior, with understandable costs and limitations.

## Later — expansion and release preparation

- [ ] **GTNA-12 · New ports:** select machines for progression needs; verify structure, recipes, behavior, art and licensing before implementation.
- [ ] **GTNA-13 · Technical quality:** reduce duplicated registrations and expand regression coverage where player reports reveal problems.
- [ ] **GTNA-14 · Release preparation:** review changes, save/config migration, compatibility, credits, changelog and installation instructions before publication.

Complex new machine families remain **under consideration** until scope and prerequisites
are defined. This roadmap does not automatically approve every reference port.

## Relationship with GregTech Infinity Ascension

GTNA is a core part of GTIA and remains usable by other packs. GTIACore requires GTNA;
GTNA does not require GTIACore. Pack-specific recipes, planetary deposits and gates belong
to the GTIA profile and can differ from the addon's defaults.

## Help test

Report version, machine, relevant settings, reproduction steps and expected/observed behavior.
An [oil and Radon testing guide](void-fluid-manual-test.md) is available in Portuguese.
See [credits and licenses](https://github.com/Raishxn/GregTech-Nexus-Addon/blob/main/THIRD_PARTY_NOTICES.md) for code and asset origins.

## Eye of Harmony

- [ ] [GTNH-faithful Eye of Harmony](eye-of-harmony-implementation-roadmap.md). In progress: physical content, structure and Overworld operation implemented locally; client QA pending. Crafting deferred; other planets, viewer and parallels follow. Detailed plan in Portuguese.

- [Crafting, materials and planets audit](eye-of-harmony-progression-port-audit.md): verified dependencies, BEC route, adapted progression and naming proposals. Document in Portuguese.
