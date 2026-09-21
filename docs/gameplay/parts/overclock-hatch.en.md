# :fire: Overclock Hatch
> *`It's not enough to be fast. You need to be absurdly fast.`*

## What it does
The **Overclock Hatch** multiplies processing speed by applying a direct duration reduction. The most powerful hatch for accelerating recipes, available from UV tier.

## Available Tiers
| Tier | Multiplier | Time Reduction | Effective Speed |
|------|:---:|:---:|:---:|
| UV | x0.50 | -50% | 2x faster |
| UHV | x0.333 | -66.7% | 3x faster |
| UEV | x0.25 | -75% | 4x faster |
| UIV | x0.20 | -80% | 5x faster |
| UXV | x0.167 | -83.3% | 6x faster |
| OpV | x0.143 | -85.7% | 7x faster |
| MAX | x0.125 | -87.5% | 8x faster |

> Values match GTOCore (`100 / (tier - 6) %`). At UV the multiplier is `0.50`, which is **the same as
> the standard electric overclock** - the UV Overclock Hatch gives no gain; the gain starts at UHV.

## How it Works
The Overclock Hatch **improves the overclock itself**: instead of the duration dropping to x0.50 per
overclock step, it drops to the hatch's multiplier. It feeds the GT overclock on the multiple-recipes
base, or is applied as a post-overclock correction by the mixin on other electric multiblocks, and
the Accelerate Hatch stacks on top. All multiplicative:
```
Final_Duration = Base_Duration x Overclock_Multiplier^steps x Accelerate_Factor
```

## Example
200 tick recipe on EV machine:
1. GT Electric Overclock (LV->EV): 200 / 4 = **50 ticks**
2. EV Accelerate Hatch (44%): 50 x 0.44 = **22 ticks**
3. UV Overclock Hatch (x0.50): 22 x 0.50 = **11 ticks**

**Result: 200 ticks -> 11 ticks (~18x faster!)**

!!! tip "Powerful Combination"
    Overclock + Accelerate + Thread = different recipes, all ultrafast, with massive parallels.
