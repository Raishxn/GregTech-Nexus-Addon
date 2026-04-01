---
name: gregtech-modding
description: GregTech CEu Modern 1.20.1 modding skill - covers multiblock creation, material registration, recipe building, and GT API patterns for addons
---

# GregTech CEu Modern Modding Skill

## Overview
This skill provides comprehensive knowledge for developing GregTech CEu Modern 1.20.1 addons. It covers the GT API, multiblock creation, material system, recipe system, and best practices.

## Environment
- **Minecraft**: 1.20.1
- **Mod Loader**: Forge 47.x
- **Base Mod**: GregTech CEu Modern 7.4.x
- **Java**: 17
- **Build System**: Gradle with NeoForge ModDev Legacy

## Key Packages Reference

### GTCEu API Structure
```
com.gregtechceu.gtceu.
├── api/
│   ├── GTValues              ← Voltage tiers (ULV=0, LV=1, ..., MAX=14)
│   ├── addon/                ← IGTAddon interface for addons
│   ├── capability/recipe/    ← IO enum (IN, OUT, BOTH)
│   ├── data/
│   │   ├── chemical/         ← Element, Material, Material.Builder
│   │   ├── RotationState     ← ALL, NON_Y_AXIS, NONE
│   │   └── tag/TagPrefix     ← ingot, dust, plate, etc.
│   ├── machine/
│   │   ├── IMachineBlockEntity
│   │   ├── MachineDefinition
│   │   ├── MultiblockMachineDefinition
│   │   └── multiblock/PartAbility ← STEAM, IMPORT_ITEMS, EXPORT_ITEMS, etc.
│   ├── pattern/
│   │   ├── FactoryBlockPattern ← Builder for multiblock patterns
│   │   └── Predicates         ← controller(), blocks(), abilities(), any(), air()
│   └── recipe/
│       ├── GTRecipe           ← Recipe data class
│       └── GTRecipeType       ← Recipe category
├── common/
│   ├── data/
│   │   ├── GTBlocks           ← All base GT blocks
│   │   ├── GTMaterials        ← All base GT materials
│   │   ├── GTRecipeTypes      ← All base recipe types
│   │   └── GTElements         ← Element registration
│   └── machine/multiblock/    ← Base multiblock implementations
└── data/recipe/               ← Recipe providers
```

## Creating a New Multiblock — Step by Step

### Step 1: Create the Machine Class

```java
package com.raishxn.gtna.common.machine.multiblock.steam;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.logic.OCParams;
import com.gregtechceu.gtceu.api.recipe.logic.OCResult;
import com.gregtechceu.gtceu.api.recipe.modifier.GTRecipeModifiers;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
// For steam multiblocks, extend the GTNA base:
import com.raishxn.gtna.common.machine.multiMachineBase.SteamMultiMachineBase;

public class NewSteamMultiblock extends SteamMultiMachineBase {
    
    private static final int MAX_PARALLEL = 32;
    private static final double SPEED_MULTIPLIER = 2.0;
    
    public NewSteamMultiblock(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }
    
    /**
     * Recipe modifier — controls speed, parallel, and cost
     * MUST be static for registration
     */
    public static GTRecipe recipeModifier(MetaMachine machine, GTRecipe recipe,
                                           OCParams params, OCResult result) {
        if (!(machine instanceof NewSteamMultiblock nsm)) return recipe;
        
        // Apply parallel processing
        var parallelResult = GTRecipeModifiers.fastParallel(
            machine, recipe, MAX_PARALLEL, false
        );
        recipe = parallelResult.getFirst();
        if (recipe == null) return null;
        
        // Apply speed modifier
        recipe = recipe.copy();
        recipe.duration = Math.max(1, (int)(recipe.duration / SPEED_MULTIPLIER));
        
        return recipe;
    }
}
```

### Step 2: Register the Multiblock Definition

```java
// In your machines registration class (e.g., GTNAMachinesSteam.java)
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.raishxn.gtna.api.registry.GTNARegistry.REGISTRATE;

public static final MultiblockMachineDefinition MY_MULTIBLOCK = REGISTRATE
    .multiblock("my_multiblock_id", NewSteamMultiblock::new)
    .rotationState(RotationState.NON_Y_AXIS)
    .recipeType(GTRecipeTypes.SOME_RECIPE_TYPE)      // Which recipes it accepts
    .recipeModifier(NewSteamMultiblock::recipeModifier)
    .appearanceBlock(GTBlocks.CASING_BRONZE_BRICKS)   // JEI appearance
    .pattern(definition -> FactoryBlockPattern.start()
        .aisle("AAA", "AAA", "AAA")                   // Top layer
        .aisle("AAA", "A#A", "AAA")                   // Middle (hollow)
        .aisle("AAA", "A~A", "AAA")                   // Bottom (~ = controller)
        .where('~', controller(blocks(definition.get())))
        .where('A', blocks(GTBlocks.CASING_BRONZE_BRICKS.get())
            .or(abilities(PartAbility.STEAM_IMPORT_ITEMS).setPreviewCount(1))
            .or(abilities(PartAbility.STEAM_EXPORT_ITEMS).setPreviewCount(1))
            .or(abilities(PartAbility.STEAM).setExactLimit(1)))
        .where('#', air())
        .build())
    .workableCasingModel(
        GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
        GTNACORE.id("block/overlay/machine/my_overlay"))
    .tooltips(
        Component.translatable("gtna.tooltip.my_multiblock.desc"),
        Component.translatable("gtna.tooltip.my_multiblock.speed"),
        Component.translatable("gtna.tooltip.my_multiblock.parallel"))
    .register();
```

### Step 3: Register Recipes

```java
// In data/recipe/handler/SteamEraRecipes.java
public static void register(Consumer<FinishedRecipe> provider) {
    // Recipe for the multiblock processing
    GTNARecipeType.MY_RECIPE_TYPE.recipeBuilder("recipe_name")
        .inputItems(TagPrefix.dust, GTMaterials.Iron, 1)
        .outputItems(TagPrefix.ingot, GTMaterials.Steel, 1)
        .duration(200)                    // ticks (20 ticks = 1 second)
        .EUt(30)                          // EU per tick (for steam: converted to Steam L/t)
        .save(provider);
}
```

## Creating a New Material

```java
public static Material MyMaterial = new Material.Builder(GTNACORE.id("my_material"))
    .ingot()                              // Has ingot form
    .fluid()                              // Has fluid form
    .dust()                               // Has dust form
    .color(0xRRGGBB)                      // Material color
    .iconSet(MaterialIconSet.METALLIC)    // Visual style
    .components(Iron, 3, Copper, 1)       // Chemical composition
    .blastTemp(1200, BlastProperty.GasTier.LOW)  // EBF temperature
    .flags(                               // What item forms to generate
        GENERATE_PLATE,
        GENERATE_ROD,
        GENERATE_GEAR,
        GENERATE_FRAME,
        GENERATE_BOLT_SCREW
    )
    .fluidPipeProperties(1200, 1000, true, true, true, false)  // Can be pipes
    .buildAndRegister()
    .setFormula("Fe3Cu");                 // Display formula
```

### Material Icon Sets
- `METALLIC` — Standard metal look
- `SHINY` — Bright, reflective
- `DULL` — Matte, dark
- `BRIGHT` — Glowing
- `RADIOACTIVE` — Hazardous glow
- `DIAMOND` — Crystal-like
- `ROUGH` — Raw, unprocessed

### Material Flags Reference
```
GENERATE_PLATE, GENERATE_ROD, GENERATE_LONG_ROD, GENERATE_BOLT_SCREW,
GENERATE_FRAME, GENERATE_GEAR, GENERATE_SMALL_GEAR, GENERATE_RING,
GENERATE_ROUND, GENERATE_SPRING, GENERATE_SPRING_SMALL,
GENERATE_FOIL, GENERATE_FINE_WIRE, GENERATE_ROTOR, GENERATE_DENSE,
GENERATE_DOUBLE_PLATE, GENERATE_TRIPLE_PLATE, GENERATE_QUADRUPLE_PLATE,
GENERATE_QUINTUPLE_PLATE, GENERATE_DOUBLE_INGOT, GENERATE_TRIPLE_INGOT
```

## Creating a New Element

```java
// protons, neutrons, halfLifeSeconds (-1=stable), decayTo, name, symbol, isIsotope
Element MY_ELEMENT = GTElements.createAndRegister(
    571,      // proton count
    580,      // neutron count
    -1,       // half-life (-1 = stable)
    null,     // decay element
    "nexium", // internal name
    "Nx",     // chemical symbol
    false     // is isotope
);
```

## GT Voltage Tiers Reference

| Tier | Name | Voltage (EU/t) | Constant |
|------|------|----------------|----------|
| 0 | ULV | 8 | GTValues.ULV |
| 1 | LV | 32 | GTValues.LV |
| 2 | MV | 128 | GTValues.MV |
| 3 | HV | 512 | GTValues.HV |
| 4 | EV | 2,048 | GTValues.EV |
| 5 | IV | 8,192 | GTValues.IV |
| 6 | LuV | 32,768 | GTValues.LuV |
| 7 | ZPM | 131,072 | GTValues.ZPM |
| 8 | UV | 524,288 | GTValues.UV |
| 9 | UHV | 2,097,152 | GTValues.UHV |
| 10 | UEV | 8,388,608 | GTValues.UEV |
| 11 | UIV | 33,554,432 | GTValues.UIV |
| 12 | UXV | 134,217,728 | GTValues.UXV |
| 13 | OpV | 536,870,912 | GTValues.OpV |
| 14 | MAX | 2,147,483,647 | GTValues.MAX |

## Common Patterns & Gotchas

### Pattern Rules
1. `~` or `S` = Controller position (MUST be exactly 1)
2. `' '` (space) = `any()` — any block or air
3. `'#'` = `air()` — must be air
4. Abilities on casings: use `.or()` chaining
5. `setExactLimit(1)` for hatches that must appear exactly once
6. `setPreviewCount(1)` for JEI preview

### Steam Machines
- Use `PartAbility.STEAM` for steam input
- Use `PartAbility.STEAM_IMPORT_ITEMS` / `STEAM_EXPORT_ITEMS` for item I/O
- Steam hatches DO NOT use EU — they consume Steam fluid directly

### Common Mistakes
1. **Forgetting `.register()`** — Machine won't exist
2. **Wrong IO direction** — Use `IO.IN` for inputs, `IO.OUT` for outputs
3. **Missing textures** — Game crashes silently, check logs
4. **Pattern char reuse** — Each char must map to unique predicate
5. **Mixin order** — GT mixins must load before GTNA mixins

## Useful Code Snippets

### Get Steam from Network
```java
// In a machine that uses wireless steam:
SteamNetworkData network = SteamNetworkData.get(level);
long available = network.getStoredSteam();
boolean consumed = network.consumeSteam(amountNeeded);
```

### Custom Tooltip with Dynamic Values
```java
.tooltips(
    Component.translatable("gtna.tooltip.key", 
        Component.literal(String.valueOf(parallelCount))
            .withStyle(ChatFormatting.AQUA))
        .withStyle(ChatFormatting.GRAY)
)
```

### Check which tier a machine is
```java
int tier = machine.self().getDefinition().getTier();
String tierName = GTValues.VN[tier]; // "LV", "MV", etc.
long voltage = GTValues.V[tier];     // 32, 128, etc.
```
