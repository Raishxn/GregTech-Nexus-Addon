# :zap: Accelerate Hatch
> *`Time is the most valuable currency. This hatch saves both.`*

## What it does
The **Accelerate Hatch** reduces the duration of ALL recipes in a multiblock. Higher tier = greater reduction.

## Available Tiers
| Tier | Min Duration (%) | Formula |
|------|:-:|---------|
| LV | 48% of original | `50 - 2x(1-1) = 48%` |
| MV | 46% | `50 - 2x(2-1) = 46%` |
| HV | 44% | `50 - 2x(3-1) = 44%` |
| EV | 42% | `50 - 2x(4-1) = 42%` |
| IV | 40% | `50 - 2x(5-1) = 40%` |
| LuV | 38% | `50 - 2x(6-1) = 38%` |
| ZPM | 36% | `50 - 2x(7-1) = 36%` |
| UV | 34% | `50 - 2x(8-1) = 34%` |
| UHV | 32% | `50 - 2x(9-1) = 32%` |
| UEV | 30% | `50 - 2x(10-1) = 30%` |
| UIV | 28% | `50 - 2x(11-1) = 28%` |
| UXV | 26% | `50 - 2x(12-1) = 26%` |
| OpV | 24% | `50 - 2x(13-1) = 24%` |

## Detailed Mechanics
### Base Formula
```
Reduced_Duration = Base_Duration x (Percentage / 100)
```
### Tier Penalty
If the Accelerate Hatch tier is **lower** than the machine tier, efficiency decreases:
```
Penalized_Percentage = Min_Percentage + (TierDiff x 20)
Maximum: 100% (no effect)
```

!!! example
    **HV Accelerate Hatch** on **EV** machine:
    - Base: 44%, TierDiff: 1
    - With penalty: 44% + (1 x 20) = 64%
    - 100 tick recipe -> 64 ticks

!!! warning
    Using an Accelerate Hatch with tier much lower than the machine will have almost no effect.

## Compatibility
Works on any multiblock using `WorkableElectricMultipleRecipesMachine`.
