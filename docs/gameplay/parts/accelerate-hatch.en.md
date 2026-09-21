# :zap: Accelerate Hatch
> *`Time is the most valuable currency. This hatch saves both.`*

## What it does
The **Accelerate Hatch** reduces the duration of ALL recipes in a multiblock. Higher tier = greater reduction.

## Available Tiers
| Tier | Min Duration (%) | Formula |
|------|:-:|---------|
| LV | 50% of original | `50 - 2x(1-1) = 50%` |
| MV | 48% | `50 - 2x(2-1) = 48%` |
| HV | 46% | `50 - 2x(3-1) = 46%` |
| EV | 44% | `50 - 2x(4-1) = 44%` |
| IV | 42% | `50 - 2x(5-1) = 42%` |
| LuV | 40% | `50 - 2x(6-1) = 40%` |
| ZPM | 38% | `50 - 2x(7-1) = 38%` |
| UV | 36% | `50 - 2x(8-1) = 36%` |
| UHV | 34% | `50 - 2x(9-1) = 34%` |
| UEV | 32% | `50 - 2x(10-1) = 32%` |
| UIV | 30% | `50 - 2x(11-1) = 30%` |
| UXV | 28% | `50 - 2x(12-1) = 28%` |
| OpV | 26% | `50 - 2x(13-1) = 26%` |
| MAX | 24% | `50 - 2x(14-1) = 24%` |

> Equivalent to GTOCore's formula (`52 - 2xtier`): the value is the **minimum** duration percentage
> (no penalty); the absolute floor is 1% and the ceiling 100% (config `accelerateHatch`).

## Detailed Mechanics
### Base Formula
```
Reduced_Duration = Base_Duration x (Percentage / 100)
```
### Tier Penalty
The penalty follows the **recipe**, not the machine (same as GTOCore): a high-tier hatch in a
high-tier machine is **not** punished for running low-tier recipes. It only applies when the
**recipe** tier is above the hatch tier:
```
Penalized_Percentage = Base_Percentage + max(0, Recipe_Tier - Hatch_Tier) x 20
Maximum: 100% (no effect)
```

!!! example
    **HV Accelerate Hatch** running an **EV** recipe:
    - Base: 46%, TierDiff: 1
    - With penalty: 46% + (1 x 20) = 66%
    - 100 tick recipe -> 66 ticks

!!! warning
    Using an Accelerate Hatch with a tier much lower than the recipes will have almost no effect.

## Compatibility
Works on **any electric multiblock** that accepts the `ACCELERATE_HATCH` ability (all GTCEu/GTNA
electric multiblocks get it via `autoAbilities`), through both the multiple-recipes logic and the
global `RecipeLogic` mixin. The effect is applied **exactly once** per recipe.

!!! note "GTOCore parity"
    The penalty uses the recipe's **pre-overclock** tier (`recipeTier - hatchTier`), exactly like
    GTOCore. The machine tier is not part of the calculation.
