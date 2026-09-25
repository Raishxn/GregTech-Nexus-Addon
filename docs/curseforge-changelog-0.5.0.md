# GregTech Nexus Addon 0.5.0

This update builds out the steam era, adds modular multiblocks and improves AE2 autocrafting. Changes below are relative to 0.4.0.

## New content

- Added more large steam machines and rebuilt 21 `large_steam_*` structures from their GT: Not Leisure references. High pressure casings now enable the intended faster mode.
- Added the Steam Elevator and its modules for flight, weather, greenhouse, oil drilling, entity processing, ore processing, mob repelling, beacon effects, apiary and bee breeding. Bee breeding integrates with Productive Bees when it is installed.
- Added Industrial and Advanced Industrial Steam Casings, Steam Lava Maker, Steam Item Vault, Steam Cactus Wonder, Steam Cracking and Mega Steam Compressor.
- Added the Integrated and Advanced Integrated Ore Processors, with circuit-selected ore processing recipes and their real stage byproducts and washing fluids.
- Added Brick Kiln, Thermal Power Pump and Liquefaction Furnace.
- Added auxiliary multiblock structures, starting with the Electric Blast Furnace and Liquefaction Furnace. Modules appear in structure previews and can unlock additional hatch abilities. KubeJS scripts can register their own modules.
- Added the Nexus Structure Terminal's module building and structure refresh tools, including a button that highlights the first incorrect block on supported fixed structures.
- Added a configurable wireless steam HUD and per-hatch network diagnostics through `/gtna steam`.
- Expanded the Nexus ME Hypercore with shared AE2 crafting capacity, concurrent jobs and the Nexus Planner integration.
- Added the animated GTNA logo to the project assets and the logo to multiblock UIs.

## Improvements

- ME Pattern Buffers can select and automatically switch recipe modes for supported machines. AE2 crafting across multiple recipe types and layered patterns has been strengthened.
- Steam Elevator modules use their own structure hatches for item and fluid input/output and show Running/Idle state in the machine model and Jade.
- Updated source attribution, machine tooltips and English/Portuguese text. Ported content now uses a consistent `Source:` line; many descriptions have clearer values, warnings and main functions.
- Overclock Hatch settings now use an exact integer duration divisor, so the tooltip matches the applied speed. **Existing custom Overclock Hatch balance values return to defaults** because the configuration format changed.

## Bug fixes

- Fixed wireless steam networks appearing empty when the first input hatch drained the entire pool every tick. Inputs now share available steam fairly.
- Fixed steam loss in the wireless network, output hatch transfer limits that starved the Large Steam Solar Boiler, and incorrect solar production shown in the UI/Jade.
- Fixed Steam Elevator module detection, orientation, upkeep and hatch placement issues; modules can now refresh when built onto an already formed machine.
- Fixed ore processing recipes missing from JEI and the Steam Ore Processing Module returning raw ore or using the wrong washing fluid.
- Fixed ME Pattern Buffer crafting requests using the wrong recipe when different patterns shared inputs, and parallel requests stalling with ingredients left in the buffer.
- Fixed the Nexus ME Hypercore structure's controller position/direction and allowed AE2 cables in its free spaces. Also fixed duplicated CPU Interface tooltip text and missing item names.
- Fixed structure diagnostics pointing at the wrong block after a failed pattern search.
- Fixed duplicate or untranslated tooltips, missing Jade/config translations, missing item textures and particles, and several client startup/config crashes.

## Validation

- Built the 0.5.0 release JAR and passed 20 unit tests and 53 GameTests, including an AE2 network autocrafting scenario.
- Minecraft 1.20.1, Forge and GregTech CEu Modern 7.5.3 or newer are required. See the project page for feature-specific integrations and credits.
