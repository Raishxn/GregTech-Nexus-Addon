# :bar_chart: Advanced Parallel Hatch
> *`When 256 parallels are not enough...`*

## What it does
GTNA's **Advanced Parallel Hatches** extend GregTech base's parallel system to massive numbers, starting at 1,024 and reaching 262,144 parallels.

## Available Tiers
| Tier | Parallels | Comparison |
|------|:---------:|------------|
| UHV | 1,024 | 4x more than GT base UV (256) |
| UEV | 4,096 | 16x more than UV |
| UIV | 16,384 | 64x more than UV |
| UXV | 65,536 | 256x more than UV |
| OpV | 262,144 | 1024x more than UV |

## Interaction with Threads
When combined with Thread Hatches, parallels are **distributed among threads**:
```
Parallels per Thread = Total_Parallels / Number_of_Threads
```

!!! example
    Advanced Parallel UEV (4,096) + UV Thread Hatch (+3 threads):
    - Total threads: 4
    - Parallels per thread: 4,096 / 4 = 1,024
    - Each thread processes 1,024 copies of a different recipe

## Important Notes
- Only **1** Parallel Hatch per multiblock (`.setMaxGlobalLimited(1)`)
- Works with any multiblock that accepts `PartAbility.PARALLEL_HATCH`
- Part sharing **disabled** to prevent exploits
