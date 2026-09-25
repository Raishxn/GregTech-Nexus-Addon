# :clipboard: Roadmap & Checklist

Track the development progress of GregTech Nexus Addon in real time.

---

## :white_check_mark: v0.1.5 - Current Version (Released)

### Completed Features
- [x] **Wireless Steam Network** - Wireless input/output hatches (Bronze + Steel)
- [x] **Large Steam Furnace** - 9x speed, 128 parallels, 50% efficiency
- [x] **Large Steam Crusher** - Bulk steam crusher
- [x] **Large Steam Alloy Smelter** - 64 parallels, 43% faster
- [x] **Large Steam Solar Boiler** - Infinite steam via Sun (10,000 L/s per cell)
- [x] **Industrial Slaughterhouse** - Industrial mob farm (electric)
- [x] **Thread / Accelerate / Overclock Hatches** - Custom hatches
- [x] **Advanced Parallel Hatch** - UHV to OpV, 1K to 262K parallels
- [x] **Materials**: Stronze, Breel, Echoite, Clay Compound, Compressed Steam
- [x] **Fluids**: Dense Supercritical, SuperHeated, Insanely Supercritical Steam
- [x] **Hydraulic System** - 10 components (Motor, Piston, Pump, Arm, etc.)

---

## :arrows_counterclockwise: v0.2.0 - In Development

### :zap: Nexus Flux Matrix - Wireless Energy System (**PRIORITY**)
- [ ] **Nexus Flux Matrix** - Central wireless storage multiblock (3x7x7 to 31x7x7)
- [ ] **Nexus Capacitor Blocks** (14 tiers, LV to MAX)
- [ ] **Wireless Energy/Dynamo Hatches** - 11 amperages, all tiers
- [ ] **Nexus Linker** - Network binding item
- [ ] **Quantum Network Terminal** - Full monitoring GUI

### New Multiblocks (Planned)
- [ ] Forge of the Iron Crown
- [ ] Steam Pressure Crystallizer
- [ ] Pneumatic Ore Washer
- [ ] Steam Distillation Column
- [ ] Hydraulic Press Complex

### KubeJS Integration
- [ ] KubeJS plugin registered
- [ ] GTNAPartAbility exposed to scripts
- [ ] WorkableElectricMultipleRecipesMachine via KubeJS

### Stability Follow-up from External Reviews (2026-09-20)
- [x] **BUG-EXT-001 - AE2 executor isolation**: native AE2 CPUs keep the original
      `CraftingCpuLogic`; only the Nexus virtual CPU receives the optimized executor.
      Protected by `nativeCraftingCpuKeepsAe2Executor` GameTest.
- [ ] **BUG-EXT-001 - manual reproduction**: test autocrafting with a normal AE2 CPU and a
      Nexus virtual CPU on a real network.
- [x] **BUG-EXT-002 - Quantum armor state restoration**: flying speed, `mayfly`, flying state,
      step height, and movement effect are restored when the armor is removed.
- [ ] **BUG-EXT-002 - interactive test**: equip/remove the set and boots, including survival/
      creative transitions, and check visibility in `NORMAL` and `JOURNEY`.
- [ ] **Visibility/documentation**: clarify in the configuration that the armor is restricted and
      hidden from the creative tab by default in `NORMAL`.
- [ ] Details and validation evidence: [Continuity Ledger](../../CONTINUITY_LEDGER.md).

### Wiring Fix + First QA Layer (2026-09-21)
- [x] **Wireless Steam Input Hatch accepted by steam machines**: the patterns pinned the steam slot
      to the exact GTCEu hatch block; they now use the ability (`abilities(PartAbility.STEAM)`),
      matching GTCEu's own steam multiblocks. The output hatch no longer declares `STEAM`.
- [x] **New guards**: `SteamWiringContractTest` (source lint, 10th unit test) + GameTest
      `wirelessSteamHatchIsAcceptedAsSteamSource` (7th gametest), both proven by negative tests.
- [ ] **Layered QA**: replicate the gametest for the remaining steam machines; more registry/wiring
      lints; CI `runData` determinism check. See G-0011 in the
      [Continuity Ledger](../../CONTINUITY_LEDGER.md).
- [x] **Thread Hatch (G-0012)**: docs aligned with reality (only the
      `WorkableElectricMultipleRecipesMachine` base; today the Duration Tester + KubeJS) and GameTest
      `threadHatchWiresIntoMultipleRecipesMachine` locking the foundation.
- [ ] **Manifest phase 2 - controller migration** onto the multiple-recipes base, so the Thread Hatch
      becomes usable on a gameplay machine (rule 8 of the
      [port manifest](multiblock-port-manifest.md)).

### GTOCore Parity in the Hatches (2026-09-21)

- [x] **No punishment on Accelerate**: the penalty now follows the recipe's **pre-overclock** tier
      (GTOCore parity), not the machine tier; pure function + unit test.
- [x] **Configurable amount** on the Accelerate, Overclock and Thread hatches
      (`ConfigurableAmountPartMachine` base, `IntInputWidget` UI), like GTOCore's
      `WorkableAmountConfigurationPartMachine`.
- [x] **GTO-style tooltips** explaining the effect, the penalty rule and the adjustable amount.
- [x] **Output Boost runtime test** (gametest completes a recipe and asserts `M`, not `M^2`), proven
      by a negative test.
- [ ] Check the new UIs in-game and translate the new keys in `pt_br.json`.
- [ ] **Manifest phase 2** is still pending so the Thread Hatch is usable on a gameplay machine.

### GTLsupb Multiblock Ports (2026-09-21)

- [x] **Universal Factory** (`gtna:universal_factory`): 32 recipe types, cross-recipe parallel +
      threads (GTNA engine), warmup/overload/batch with UI, new casing. Test bed for the
      multiple-recipes logic.
- [x] **Primitive Stone Furnace** (`gtna:primitive_stone_furnace`): a **no-energy** multiblock
      furnace (FURNACE_RECIPES), 3x3x3 stone pattern.
- [ ] Dedicated casing texture, `pt_br.json` translations and an in-game check of the displays.
- [ ] Details, license (LGPLv3) and evidence: [Continuity Ledger](../../CONTINUITY_LEDGER.md) G-0015.
