---
name: ae2-integration
description: Applied Energistics 2 integration patterns for GregTech addons - covers ME network interaction, pattern providers, storage buses, and advanced autocraft setups
---

# Applied Energistics 2 Integration Skill

## Overview
This skill covers integrating with AE2 (Applied Energistics 2) in the context of GregTech CEu Modern 1.20.1 addons. Focus on ME network interaction, pattern providers, and autocraft routing.

## Dependencies
- **Applied Energistics 2**: 1.20.1 (Forge)
- **ExtendedAE**: Provides extra AE2 features (Extended Pattern Provider, Circuit Cutter, etc.)
- **GuideME**: AE2 documentation framework

## Key AE2 Packages

```
appeng.
├── api/
│   ├── networking/           ← ME Network API
│   │   ├── IGrid
│   │   ├── IGridNode
│   │   └── crafting/         ← Autocraft API
│   ├── storage/              ← Storage API
│   │   ├── IStorageChannel
│   │   └── data/IAEStack
│   ├── parts/                ← Parts/Cables API
│   └── config/               ← Config channels, priorities
├── helpers/                  ← Utility classes
└── me/cluster/              ← ME Controller cluster
```

## Integration Patterns

### Pattern 1: ME Storage Bus on GT Multiblock

When you want AE2 to read items from a GT machine:

```java
// The GT machine needs to expose IItemHandler via Forge capabilities
// This is usually automatic via GT's item import/export bus system
// 
// Key: Use ME Storage Bus pointed at the GT Output Bus
// Priority: Set to 0 (default) for normal routing
// Priority: Set to -1 for subproduct consumption (see tip below)
```

### Pattern 2: Pattern Provider → GT Machine

For autocraft routing to GT multiblocks:

```java
// Setup:
// 1. ME Dual Interface facing the GT Input Bus
// 2. Set blocking mode on the Dual Interface
// 3. Use Screwdriver on GT machine: "Input from output side allowed"
//
// For mixed item+fluid recipes:
// 1. Place two ME Dual Interfaces facing each other
// 2. One on main network, one on sub-network
// 3. Sub-network interface → GT Input Bus/Hatch
// 4. Use Advanced Blocking Card in the receiving interface
```

### Pattern 3: Priority -1 Trick (Subproduct Consumption)

```java
// When a GT machine produces a subproduct (e.g., HCl from chemical reaction):
// 1. Route subproduct to ME network via Fluid Storage Bus
// 2. Set that Storage Bus priority to -1
// 3. AE2 extraction logic inverts for negative priority
// 4. System consumes subproduct BEFORE touching main stock
//
// This saves resources automatically!
```

### Pattern 4: Pattern Buffer ↔ AE2 Integration

```java
// GTNA's Pattern Buffer can interact with AE2 in two ways:
//
// Mode A: Passive (Storage Bus reads Pattern Buffer)
//   - AE2 sees the Pattern Buffer's configured slots as available crafting patterns
//   - Storage Bus on the Pattern Buffer's face
//
// Mode B: Active (Pattern Buffer pulls from ME Network)  
//   - Pattern Buffer has built-in ME connectivity
//   - It reads patterns from the ME network and auto-configures slots
//   - Requires Nexium Wire connection to ME Controller
```

## ExtendedAE Features Reference

```
ExtendedAE adds:
├── Extended Pattern Provider    ← More pattern slots
├── Extended Interface           ← More item slots  
├── Wireless Connector           ← Cable-free P2P alternative
├── Circuit Cutter               ← For GT circuit autocraft
├── Pattern Modifier             ← Edit patterns in-place
└── Tag Storage Bus              ← OreDict-aware storage
```

## AE2 Autocraft with GT: Best Practices

1. **Always use Blocking Mode** on interfaces feeding GT machines
2. **Use Advanced Blocking Cards** for mixed item+fluid recipes
3. **Avoid Smart Cables** in large bases — use Network Visualization Tool instead
4. **P2P Fluid Tunnels** have unlimited throughput — abuse them
5. **Stocking Input Bus** (GT LuV+) is superior to regular ME buses

## Common AE2 + GT Mistakes

| Mistake | Fix |
|---------|-----|
| Autocraft stuck — items won't enter machine | Screwdriver → Enable "Input from output side" |
| Wrong items going to wrong machine | Use Item Advanced Blocking Card |
| Network lag with many machines | Use sub-networks, avoid Smart Cables |
| Fluid craft fails | Use Dual Interface pairs for mixed recipes |
| AE2 extracts my catalysts | Lock catalyst slot with ghost items |

## Code: Checking AE2 Availability

```java
// Check if AE2 is loaded before using AE2 features
import net.minecraftforge.fml.ModList;

public static boolean isAE2Loaded() {
    return ModList.get().isLoaded("ae2");
}

public static boolean isExtendedAELoaded() {
    return ModList.get().isLoaded("ae2wtlib") || ModList.get().isLoaded("expatternprovider");
}
```
