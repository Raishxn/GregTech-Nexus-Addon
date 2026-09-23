<div align="center">
  <img src="./docs/assets/logo.gif" width="225" height="225" alt="GregTech Nexus Addon logo">
  <h1>GregTech Nexus Addon</h1>
  <p><strong>The Nexus of Steam & Steel</strong></p>
  <p>
    <a href="https://github.com/raishxn/GregTech-Nexus-Addon/releases">Releases</a> ·
    <a href="./THIRD_PARTY_NOTICES.md">Credits and permissions</a> ·
    <a href="https://discord.gg/d3qHufwRxb">Discord</a>
  </p>
</div>

## What GTNA is building

GregTech Nexus Addon expands **GregTech CEu Modern for Minecraft 1.20.1** into a connected
progression from steam and hydraulics through industrial processing and large endgame machines. It
brings selected multiblocks and mechanics from GregTech projects to the modern game while adapting
their structure, recipes, interfaces and automation to GTCEu 7.5.3 and AE2.

The aim is a coherent mod, rather than a collection of copied machines: a wireless steam network
serves early factories; hydraulic components give steam a broader production role; modular
multiblocks and the Nexus Structure Terminal make large builds manageable; and ME Pattern Buffers
connect those machines to autocrafting. Source credits appear in game for ported content.

For future ports of GT: Not Leisure (GTNL) multiblocks, GTNA uses GTNL as the reference for
structure and behavior. Where a corresponding Modernity-GTNH texture is available and its use is
permitted by its license, the port uses that modern visual style. Asset origin and licensing are
recorded in [Third-Party Notices](THIRD_PARTY_NOTICES.md).

## Current features

- **Wireless steam:** input and output hatches, network balance and flow monitoring, plus an
  optional client HUD.
- **Steam and hydraulic industry:** large steam machines, hydraulic manufacturing and specialized
  multiblocks such as the Steam Elevator and its modules.
- **Modular electric machines:** auxiliary structures can add hatches to machines such as the
  Electric Blast Furnace and Liquefaction Furnace.
- **Nexus Structure Terminal:** previews and builds multiblocks and their registered modules.
- **AE2 integration:** ME Pattern Buffers with multiple recipe modes and larger processing systems.

The [continuity ledger](CONTINUITY_LEDGER.md) records implemented behavior, validation and known
in-game checks. Features under development may change before a release.

## Version and dependencies

Development currently targets **Minecraft 1.20.1**, **Forge 47.4.1**, **GTCEu 7.5.3** and
**GTNA 0.4.0**. See [gradle.properties](gradle.properties) and [build.gradle](build.gradle) for the
exact dependency versions used by this checkout. Download builds from
[GitHub Releases](https://github.com/raishxn/GregTech-Nexus-Addon/releases).

## Origins, permissions and licenses

GTNA's own code is licensed under **LGPLv3**. Ported code and art keep their original licenses;
GTNA's license does not replace them. The GTO assets used by GTNA include **CC BY-NC-SA 4.0**
material, so the combined distribution is **non-commercial** while those assets are included.

| Source | What GTNA uses | Basis |
|---|---|---|
| GregTech Odyssey / GTOCore | Multiblock mechanics, recipes and selected assets | LGPLv3 code; CC BY-NC-SA 4.0 original assets, with attribution requested by the GTO team |
| GTO Extended Platform Presets | Platform presets | Explicit permission from the GTO team |
| GT: Not Leisure | Multiblock structures and selected controller overlays | Explicit permission with source credit |
| Twist Space Technology | Eye of Wood structure | Explicit permission; in-game source credit requested |
| GTLCore and GTLsupb | Selected machines and pattern-buffer behavior | Original licenses and source attribution as detailed in the notices |
| Modernity-GTNH | Selected modern textures | CC BY-NC-SA 4.0; a separate permission request is recorded as pending |

See [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) for exact paths, license details and pending
permission requests. GTNA credits ported machines through `GTNASources` tooltips.
