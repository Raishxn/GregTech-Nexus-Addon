---
name: modding-references
description: External documentation references for Minecraft 1.20.1 modding — covers Forge docs, Mixin wiki (Sponge & Fabric), LDLib/LowDragMC docs, and GT addon patterns. Always consult these before implementing core systems.
---

# Modding References Skill

## Purpose
This skill centralizes all external documentation references used during GTNA development. **Always consult the relevant reference docs before implementing features** to ensure compatibility, follow best practices, and avoid reinventing the wheel.

---

## 1. Minecraft Forge Documentation (1.20.x)

**URL**: https://docs.minecraftforge.net/en/1.20.x/

### When to Use
- Registering blocks, items, entities, capabilities, events
- Understanding Forge's lifecycle (mod loading, registry events, config)
- Implementing `ICapabilityProvider`, `AttachCapabilitiesEvent`
- Handling network packets (SimpleChannel)
- Using `SavedData` for per-world/per-player persistent data  
- Data generation (tags, recipes, loot tables)
- Config system (`ForgeConfigSpec` or third-party)

### Key Sections to Consult
| Section | Use Case |
|---------|----------|
| [Getting Started](https://docs.minecraftforge.net/en/1.20.x/gettingstarted/) | Mod setup, mods.toml |
| [Blocks](https://docs.minecraftforge.net/en/1.20.x/blocks/) | Block registration, BlockStates |
| [Items](https://docs.minecraftforge.net/en/1.20.x/items/) | Item registration, capabilities |
| [Capabilities](https://docs.minecraftforge.net/en/1.20.x/datastorage/capabilities/) | `IEnergyStorage`, custom caps |
| [SavedData](https://docs.minecraftforge.net/en/1.20.x/datastorage/saveddata/) | Per-world persistent data (e.g., `NexusEnergyNetwork`) |
| [Networking](https://docs.minecraftforge.net/en/1.20.x/networking/) | Client-Server sync |
| [Events](https://docs.minecraftforge.net/en/1.20.x/events/) | `@SubscribeEvent`, lifecycle |
| [Data Gen](https://docs.minecraftforge.net/en/1.20.x/datagen/) | Recipes, tags, loot tables |

### GTNA-Specific Usage
- **Nexus Flux Matrix**: Uses `SavedData` for persistent wireless energy network storage
- **Wireless Steam**: Uses capabilities for fluid transfer across dimensions
- **Config**: Uses `dev.toma.configuration` (not ForgeConfigSpec) — check `ConfigHolder.java`

---

## 2. Mixin Wiki (SpongePowered — Official)

**URL**: https://github.com/SpongePowered/Mixin/wiki

### When to Use
- Modifying GregTech base classes without forking
- Injecting custom logic into GT recipe processing
- Overriding or extending GT multiblock behavior
- Any class that GT doesn't expose via its API

### Key Sections
| Page | Use Case |
|------|----------|
| [Introduction](https://github.com/SpongePowered/Mixin/wiki) | Concepts: `@Mixin`, `@Inject`, `@Redirect`, `@Overwrite` |
| [Injection Points](https://github.com/SpongePowered/Mixin/wiki/Injection-Points) | `HEAD`, `TAIL`, `RETURN`, `INVOKE`, `FIELD` |
| [@Inject](https://github.com/SpongePowered/Mixin/wiki/Inject) | Adding code before/after methods |
| [@Redirect](https://github.com/SpongePowered/Mixin/wiki/Redirect) | Replacing a single method call |
| [@Accessor](https://github.com/SpongePowered/Mixin/wiki/Accessor) | Access private fields/methods |
| [@Shadow](https://github.com/SpongePowered/Mixin/wiki/Shadow) | Reference existing fields in target class |
| [Mixin Targets](https://github.com/SpongePowered/Mixin/wiki/Mixins-on-Minecraft) | Targeting Minecraft/Forge classes |

### Important Rules
1. **Never `@Overwrite`** unless absolutely necessary — use `@Inject` with `@Cancellable`
2. **Mixin priority**: GTNA defaults to `priority = 1000`. Set higher to run after GT mixins
3. **Registration**: Mixins go in `src/main/resources/gtna.mixins.json`
4. **Compatibility**: Always check if GT provides an API hook before using a Mixin

---

## 3. Fabric Mixin Tutorial (Beginner-Friendly)

**URL**: https://wiki.fabricmc.net/tutorial:mixin_introduction

### When to Use
- Learning Mixin concepts from scratch (more beginner-friendly than Sponge wiki)
- Understanding CallbackInfo, CallbackInfoReturnable patterns
- Visual examples of injection points

### Key Pages
| Page | Content |
|------|---------|
| [Introduction](https://wiki.fabricmc.net/tutorial:mixin_introduction) | What are mixins, basic concepts |
| [Injects](https://wiki.fabricmc.net/tutorial:mixin_injects) | `@Inject` with `CallbackInfo` patterns |
| [Accessors](https://wiki.fabricmc.net/tutorial:mixin_accessors) | Accessing private members |
| [Hotswap](https://wiki.fabricmc.net/tutorial:mixin_hotswap) | Development workflow |
| [Examples](https://wiki.fabricmc.net/tutorial:mixin_examples) | Real-world patterns |

### Note
Even though Fabric docs, the Mixin system is **identical** on Forge. The only difference is:
- Forge uses `mixins` field in `mods.toml` or `@Mod` annotation
- Fabric uses `mixins` field in `fabric.mod.json`
- The actual `@Mixin`, `@Inject`, `@Shadow`, etc. annotations are 100% the same

---

## 4. LDLib / LowDragMC Documentation

**URL**: https://low-drag-mc.github.io/LowDragMC-Doc/ldlib/

### When to Use
- Building custom GUIs for multiblocks (LDLib widget system)
- Using `WidgetGroup`, `DraggableScrollableWidgetGroup`, `LabelWidget`
- Working with LDLib's `ManagedFieldHolder` and `@Persisted` annotations
- Understanding LDLib's `SyncData` system for client-server sync
- Custom rendering (LDLib renderer system)

### Key Sections
| Section | Use Case |
|---------|----------|
| LDLib Overview | Core library features |
| GUI System | Widget-based UI creation |
| SyncData | `@Persisted`, `ManagedFieldHolder`, field sync |
| Renderer | Custom block/item rendering |

### GTNA Usage
- **All GTNA machine GUIs** use LDLib widgets (`WidgetGroup`, `ComponentPanelWidget`, etc.)
- **Field persistence**: `@Persisted` on fields in machine classes (e.g., `lastDrops` in `IndustrialSlaughterhouse`)
- **ManagedFieldHolder**: Required in every GT machine class for data sync
- **Pattern**: Every GT machine class must declare:
  ```java
  protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
      MyMachine.class, ParentMachine.MANAGED_FIELD_HOLDER);
  
  @Override
  public ManagedFieldHolder getFieldHolder() {
      return MANAGED_FIELD_HOLDER;
  }
  ```

---

## 5. Additional References

### GregTech CEu Modern Wiki
**URL**: https://gregtechceu.github.io/GregTech-Modern/1.20.1/

- Modpack author guides
- Example multiblock definitions (Java + KubeJS)
- Material/element system docs
- Electricity and logistics system docs

### GT Addon Reference Mods
Use these as code references for patterns and implementations:

| Mod | URL | Specialty |
|-----|-----|-----------|
| **GTMThings** | https://github.com/liansishen/gtmthings | Wireless energy, ME hatches |
| **PhoenixCore** | https://github.com/P-H-O-E-N-I-X-PackForge/PhoenixCore | Advanced wireless, Int128 |
| **GTOCore** | https://github.com/GregTech-Odyssey/GTOCore | End-game machines, scaling |
| **CosmicCore** | https://github.com/Frontiers-PackForge/CosmicCore | Complex mechanics |
| **GTLCore** | https://github.com/AaAdoniSsS/GTLCore | Skyblock mechanics |
| **GTLAdditions** | https://github.com/Dragonators/GTLAdditions | Extra machines |
| **Twist Space Tech** | https://github.com/Nxer/Twist-Space-Technology | Advanced tech |
| **NHCM** | https://github.com/GTNewHorizons/NewHorizonsCoreMod | Legacy reference |

---

## Quick Reference Card

```
Forge Docs ----→ Registries, Capabilities, SavedData, Events, Networking
Mixin (Sponge) → @Inject, @Redirect, @Accessor, @Shadow (technical reference)
Mixin (Fabric) → Same annotations, better beginner tutorials & visual examples
LDLib ---------→ GUI widgets, @Persisted, ManagedFieldHolder, SyncData
GT Modern Docs → Multiblock examples, material system, KubeJS integration
GTMThings -----→ Wireless energy pattern (WirelessEnergyContainer reference)
```

**Rule**: Before implementing ANY core feature, search these docs first. If an existing pattern exists (especially in GTMThings for wireless energy), adapt it rather than building from scratch.
