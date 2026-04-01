# :fire: Overclock Hatch
> *`It's not enough to be fast. You need to be absurdly fast.`*

## What it does
The **Overclock Hatch** multiplies processing speed by applying a direct duration reduction. The most powerful hatch for accelerating recipes, available from UV tier.

## Available Tiers
| Tier | Multiplier | Time Reduction | Effective Speed |
|------|:---:|:---:|:---:|
| UV | x0.55 | -45% | 1.82x faster |
| UHV | x0.333 | -66.7% | 3x faster |
| UEV | x0.25 | -75% | 4x faster |
| UIV | x0.20 | -80% | 5x faster |
| UXV | x0.167 | -83.3% | 6x faster |
| OpV | x0.143 | -85.7% | 7x faster |
| MAX | x0.125 | -87.5% | 8x faster |

## How it Works
The multiplier is applied **after** GT Electric Overclock and **after** Accelerate Hatch:
```
Final_Duration = Post_GT_OC_Duration x Accelerate_Factor x Overclock_Multiplier
```

## Example
200 tick recipe on EV machine:
1. GT Electric Overclock (LV->EV): 200 / 4 = **50 ticks**
2. EV Accelerate Hatch (42%): 50 x 0.42 = **21 ticks**
3. UV Overclock Hatch (55%): 21 x 0.55 = **11 ticks**

**Result: 200 ticks -> 11 ticks (18x faster!)**

!!! tip "Powerful Combination"
    Overclock + Accelerate + Thread = different recipes, all ultrafast, with massive parallels.
