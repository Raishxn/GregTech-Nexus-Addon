# Void Fluid Drilling Rig — discovered-fluid prototype

GTIA-D-177–D-187, 2026-09-29. Native GTNA implementation inspired by GTL's 3×7×3
Void Fluid Drilling Rig. This is separate from ore mining and does not change bedrock reserves.

## Player flow

1. Use the native GTCEu Fluid Drilling Rig on a natural deposit and complete a cycle that actually
   delivers positive fluid output. Scanning a world, finding a vein or synthesizing a fluid is insufficient.
2. Right-click its controller with the Deposit Recorder and a Data Stick in your inventory.
   One stick becomes reusable Deposit Data for that fluid and origin.
3. Put the data in the new machine's item input bus; supply power, Drilling Fluid and any special reagent.
4. To copy the data, hold it in the other hand and use the recorder in the air with another Data Stick.
   Copying requires the existing discovery; it does not require returning to the deposit.
5. Initially produce in the source dimension. For another dimension, insert that origin's remote upgrade
   in the same item bus and supply its minimum energy tier.

Certificates live in the overworld SavedData `gtna_fluid_discoveries`, scoped to GTCEu's owner/team. With FTB Teams, resolve the personal team's `getTeamId()`
to the effective party ID; `FTBOwner.getUUID()` alone returns the personal ID.
If the ownership integration has no team UUID, use the individual player UUID, never the shared EMPTY UUID.
The machine must have an owner. Team membership changes can require recording/copying data for the new
scope; old discoveries are retained rather than reassigned automatically. A newly placed drill, or one
reloaded before it was recorded, must complete a new extraction cycle to obtain its transient receipt.
Already issued discovery data persists across restart and deposit depletion. It is specific to its world,
fluid, origin and owner/team; a sample or an unissued data card does not grant access.

## Machine and costs

Pattern matches the GTL 3×7×3 drilling tower, bottom rows first, controller in the last aisle.
Titanium casing, Steel frames, three fixed internal casings; eight base positions for hatches.
Requires one item input, one or two fluid inputs, one fluid output, maintenance and one or two energy
inputs; one optional parallel hatch. Two single-fluid inputs suffice for gas/Radon. Output voiding is
disabled. A full output prevents starting; if filled during a cycle, the paid cycle waits and delivers
once space is available. Missing data or remote upgrade pauses progress without discarding the cycle.

Controller recipe: Fluid Drilling Rig **MV**, four EV pumps, two **HV sensors**, one EV Field Generator,
four EV circuits, eight Titanium Plates, 576 mB Soldering Alloy; Assembler EV, 600 ticks, 1920 EU/t.
This corrects two bootstrap assumptions in the original proposal: stock GTCEu's EV Fluid Drilling Rig
requires LuV components, and its EV Sensor requires Quantum Eye. Both ingredient tiers are configurable;
other packs can select the original components. The prototype preserves terrestrial EV entry.

Recorder: HV Sensor + HV circuit + Data Stick + two Titanium Plates; HV Assembler, 200 ticks.
Separation filters: four Steel Rings + four Carbon Dust + eight Fine Gold Wires → 32;
HV Assembler, 200 ticks. There is no ordinary crafting recipe for discovered data.

Default cycles: 400 ticks, 1920 EU/t, 100 mB Drilling Fluid. Oil 2000, Heavy Oil 1500,
Light Oil 2500, Natural Gas 1500 (+100 Nitrogen), Salt Water 4000, Raw Oil 2000 mB.
Stock Raw Oil's actual registry ID is `gtceu:oil_medium`, verified in `UnknownCompositionMaterials`.
The GTIA example adds Mars Radon: 50 mB +100 Nitrogen +1 filter, otherwise the same costs.

Parallel caps EV 1 / IV 2 / LuV 4 / ZPM 8 / UV 16, then double up to the technical cap 1024.
Use the common parallel-hatch ability: GCYM's early hatches and GTNA's later advanced hatches are accepted.
Parallel reserves energy first; normal GTCEu overclock follows. Outputs, reagents and EU/t scale linearly;
one data card and one remote upgrade suffice. UI reports origin, cycle, EU/t, parallel, fluid costs and rate.

## Pack configuration

Startup file: `config/gtna/balance/void_fluid_drill.json`. Restart to apply changes; recipes are registered
from the profile. `enabled=false` disables production, recording and crafting, preserving registered block
and item IDs in existing worlds. Invalid configuration fails startup with its path, without silently
replacing the profile. Missing optional fluids/research items omit the affected recipe and log the cause.

Each program supplies `id`, `fluid`, `dimension`, `remoteUpgrade`, `minimumTier`, `duration`,
`euPerTick`, `drillingFluid`, `nitrogen`, `filters`, `output`. Origins/program IDs must be unique;
Drilling Fluid and output must be positive, output bounded by the configured per-operation limit
(up to 1000000 mB). With the 1024-parallel technical ceiling, this keeps scaled Forge fluid amounts
within the signed integer range.
Upgrade entries supply `tier` and reusable `researchItems` (registered item IDs).
Empty research lists intentionally disable an upgrade's craft.

Defaults: terrestrial/IV with Overworld Data; T1/IV with lunar chip; T2/LuV with Mars chip;
T3/LuV with Venus and Mercury chips. T4/LuV and T5/ZPM are registered but have no crafting recipe
until the pack supplies Ceres and Io/Ganymede research IDs. Those planets and their fluid catalog have
not been selected/implemented in the base Ad Astra integration. Do not invent unlock data for them.

Upgrade crafting: Data Orb, two Sensors, two Field Generators and four circuits at its tier,
576 mB Soldering Alloy, 600 ticks; research items non-consumable. A remote upgrade never grants discovery,
changes the program's minimum tier or unlocks the preceding upgrades.

The example `docs/config-examples/gtia/void_fluid_drill.json` adds only the approved Mars Radon.
The sibling `void-fluid-datapack/` contains a pack-owned Mars bedrock deposit; copy its data into KubeJS's
`data/` folder or install as a world datapack. Its provisional deposit parameters follow GTO's Mars
reference (weight 20, yield 50–80, depleted yield 40), distinct from the void production rate. Those
natural-deposit numbers need in-game measurement; their inclusion does not establish final balance.
The example does not remove other Radon recipes or prove the complete rocket/Quantum Eye progression.

## Validation and manual checks

GameTests exercise native extraction and recorder interaction, copying, ownership/origin certificates
and NBT persistence, EV output/cost/data preservation, full-output pause before and during a cycle,
IV parallel cost and output, remote upgrade/tier gates and gas reagents. The latest full gate passed
28 unit test classes and 125/125 GameTests, including resource generation with FTB Teams absent.
The profile runner (`tools/test_void_fluid_gtia_profile.py`) also passed 125/125 with FTB Teams loaded.
It now requires explicit success markers for both stronger scenarios:

- Actual Mars ServerLevel, normal GTCEu bedrock deposit generation (no injected vein), formed native
  MV drill outputting Radon, recorder-created certificate, then T2 remote production in the Overworld.
- Real FTB manager/personal/party model objects with isolated test identities: same-party sharing and
  copying, outsider rejection without stick loss, departure revoking production, rejoin restoring it.
  These identities use simulated players, not two connected human clients.

The optional FTB test body is isolated from the registered GameTest holder so discovery can load
without FTB Teams. Compilation uses optional API dependencies; the jar bundles no FTB classes and
adds no mandatory FTB dependency. Datagen repeated with `written: 0`.

Remaining client QA: tower preview/orientation, item models/tooltips, controls, team sharing with two real
players, planetary travel/extraction/transport under normal play and measured petrochemical/Radon demand.
Keep changes local; publication still requires author testing and explicit approval.
