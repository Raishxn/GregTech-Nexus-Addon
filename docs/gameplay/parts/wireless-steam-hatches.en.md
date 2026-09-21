# :satellite: Wireless Steam Hatches
## What they are
**Wireless Steam Hatches** allow transmitting steam wirelessly between boilers and multiblocks. No pipes, no spaghetti, no fluid lag.

## Variants
### Wireless Steam Input Hatch (Receives Steam)
| Variant | Tier | Capacity |
|---------|------|----------|
| **Bronze** | ULV | 20,000 mB |
| **Steel** | LV | 2,147,483,647 mB (MAX) |

Place on any multiblock that needs steam. It automatically pulls from the wireless network.

!!! note "How the steam slot recognizes it"
    The Input Hatch counts as a **steam hatch**: it declares the `STEAM` ability, so it replaces the
    stock GTCEu steam input hatch in both GTCEu's and GTNA's steam multiblocks. The Output Hatch is a
    **fluid output** hatch (`EXPORT_FLUIDS`) - place it wherever the multiblock accepts a fluid
    output hatch.

### Wireless Steam Output Hatch (Sends Steam)
| Variant | Tier | Capacity |
|---------|------|----------|
| **Bronze** | ULV | 20,000 mB |
| **Steel** | LV | 2,147,483,647 mB (MAX) |

Place on your boiler or any steam source. It sends steam to the wireless network.

## How the Network Works
1. Each **player** has their own wireless network (UUID-based)
2. Steam is stored in a global pool (World SavedData)
3. Output Hatches **add** steam to the pool
4. Input Hatches **consume** steam from the pool
5. **No distance limit** -- works across dimensions!

!!! tip "Upgrade to Steel"
    Steel variants have **Integer.MAX_VALUE** storage capacity. Upgrade ASAP!
