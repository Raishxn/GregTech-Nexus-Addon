# GregTech Nexus Addon 0.5.1

This update adds four GregTech Odyssey multiblocks and fixes the KubeJS startup crash reported with GTNA 0.5.0. Changes below are relative to 0.5.0.

## Corrected 0.5.1 JAR

- Fixed two additional startup crashes affecting GTCEu module tooltips and the AE2 crafting confirmation screen in production modpacks.
- Reduced repeated structure checks while the Nexus Terminal builds the Nexus ME Hypercore. In-world build time still needs confirmation with this corrected JAR.
- Verified startup with GTCEu 7.5.3, AE2 15.4.10 and KubeJS build.26 in the Prism `1.20.1` modpack. The automated suite now passes all 60 GameTests.

## New machines

- **Generator Array:** runs supported GTCEu generators together, with normal energy output or wireless transfer to the Nexus Flux Matrix.
- **Fishing Ground:** processes bait and water with circuit selected catches.
- **Evaporation Plant:** processes water and brine in a stainless steel tower. A titanium auxiliary tower supports Parallel and Accelerate hatches.
- **Greenhouse:** grows crops using daylight, with the original light based slowdown behavior.

The machines include their structures, recipes, assets and English/Portuguese tooltips. Ported content credits its source in game.

## Improvements and fixes

- Fixed the GTNA DataGenerator mixin crash seen with `kubejs-forge-2001.6.5-build.26`. The injection now handles the Minecraft 1.20.1 runtime method name and cannot crash startup merely because its target is absent.
- Removed Nexus Flux Matrix Safe Mode. The network can now spend its remaining energy down to zero. Old SafeMode world data is ignored.
- Wireless Steam Hatches now work only as steam parts; electric multiblocks no longer accept them as universal fluid hatches.
- Fixed the Greenhouse reporting sunlight when its glass roof was covered by opaque blocks.
- Clarified Evaporation Plant hatch positions: put one fluid input and one or two energy inputs in the base, and fluid outputs in the tower stages. Any fluid hatch tier is allowed.
- Revised the four machines' tooltips for accurate mechanics, values, source attribution and translations.

## Validation

- Passed formatting, compilation, unit tests, data generation and all 60 required GameTests. The tests include a MAX fluid output hatch placed in a valid Evaporation Plant stage.
- Requires Minecraft 1.20.1, Forge and GregTech CEu Modern 7.5.3 or newer. Check the file dependencies for the full mod list.
