# Third-Party Notices

GregTech Nexus Addon (GTNA) ports mechanics, multiblocks and features from legacy GregTech
mods and modpacks to GregTech CEu Modern.

- **GTNA's own code** is licensed under **LGPLv3** (see [`LICENSE`](LICENSE)).
- **Ported content and assets remain under their original licenses**, listed below. The LGPLv3
  license of GTNA's code does **not** relicense or supersede any third-party material.

> **Non-commercial notice:** GTNA includes assets licensed under **CC BY-NC-SA 4.0**
> (non-commercial). As a result, the combined distribution may not be used for commercial
> purposes while those assets are included.

If you are a rightsholder and want an entry corrected or removed, please open an issue.

## Summary

| Project | Source | License | Used content | Status |
|---|---|---|---|---|
| GregTech Odyssey (GTO) | [GregTech-Odyssey/GTOCore](https://github.com/GregTech-Odyssey/GTOCore) | Code: LGPLv3 · Original assets: **CC BY-NC-SA 4.0** | Textures (casings, hatches, storage cores, wireless energy units, overlays), machine mechanics (reimplemented) | Assets stay CC BY-NC-SA 4.0 — permitted with attribution; tooltip credit requested by the GTO team |
| GTO Extended Platform Presets (GTOEPP) | [GregTech-Odyssey/Gto-Extended-Platform-Presets](https://github.com/GregTech-Odyssey/Gto-Extended-Platform-Presets) | All Rights Reserved | Platform presets (`platforms/epp/sy_1/*`) | **Permission granted** by the GTO team (keep attribution intact) |
| GT: Not Leisure (GTNL) | [ABKQPO/GT-Not-Leisure](https://github.com/ABKQPO/GT-Not-Leisure) | **GPL-3.0** | Multiblock structure files (`.mb`), large steam multiblock family | **Permission granted** (credit the source). Structures that GTNL itself took from GTO are credited to GTO |
| Twist Space Technology (TST) | [Nxer/Twist-Space-Technology-Mod](https://github.com/Nxer/Twist-Space-Technology-Mod) | **GPL-3.0** | Overworld-only ore condenser | **Pending** — permission requested |
| GTMThings | [liansishen/GTMThings](https://github.com/liansishen/GTMThings) | None declared (All Rights Reserved) | `AdvancedBlockPattern` (basis for `NexusBlockPattern`) | **Pending** — permission requested |
| cmme-additions → Modernity-GTNH | [CristalGaming/cmme-additions](https://github.com/CristalGaming/cmme-additions) → [ModernityGTNH/Modernity-GTNH](https://github.com/ModernityGTNH/Modernity-GTNH) | ARR → **CC BY-NC-SA 4.0** | Plate/ingot textures (triple/quadruple/quintuple, etc.) and the **Industrial / Advanced Industrial Steam Casing** textures (`MetaCasing02/1`, `2`) | **Pending** — permission requested; same author as GTNL per the project owner |
| GTLCore | [nutant233/GTLCore](https://github.com/nutant233/GTLCore) | Declared LGPLv3.0 (`gradle.properties`; no `LICENSE` file found) | Textures, pattern-buffer parity code | Attribution (license to confirm) |
| GTLsupb | GTLsupb (LGPLv3) | LGPLv3 | Universal Factory, Primitive Stone Furnace | Attribution only |
| GregTech CEu Modern | [GregTechCEu/GregTech-Modern](https://github.com/GregTechCEu/GregTech-Modern) | LGPL-3.0 | Base API / framework | Attribution only |

## Notes

### GregTech Odyssey (GTO)

- The GTO team confirmed that **original code** (`src/main/java` of GTOCore) is **LGPLv3**, while
  **original textures/assets are CC BY-NC-SA 4.0** and must stay under that license.
- GTO also asked that ported content **credit the original addon in tooltips** (as GTNH and GTO do
  between themselves). GTNA implements this through `GTNASources`.
- GTOCore itself includes textures from other mods; see
  [GTO's `THIRD_PARTY_LICENSES.md`](https://github.com/GregTech-Odyssey/GregTech-Odyssey/blob/main/THIRD_PARTY_LICENSES.md).

### GT: Not Leisure (GTNL)

- The GTNL author granted permission to use GTNL structures with source attribution.
- Structures that GTNL itself ported from GTO are credited to **GTO**, whose assets are used under
  **CC BY-NC-SA 4.0** (permission confirmed by the GTO team).

### Attribution in tooltips

GTNA appends a `Source: <addon>` line to the tooltips of ported machines/items. The mapping lives in
`src/main/java/com/raishxn/gtna/common/data/GTNASources.java`.
