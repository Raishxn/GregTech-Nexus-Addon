# :thread: Thread Hatch
> *`One thread per recipe. The future of manufacturing is parallel.`*

## What it does
The **Thread Hatch** allows a multiblock to process **different recipes at the same time**. Unlike Parallel Hatch (which repeats the SAME recipe), Thread Hatch runs completely different recipes simultaneously.

## Available Tiers
| Tier | Extra Threads | Total (with base 1) | Formula |
|------|:---:|:---:|---------|
| ZPM | +1 | 2 | `2^(7-6) - 1` |
| UV | +3 | 4 | `2^(8-6) - 1` |
| UHV | +7 | 8 | `2^(9-6) - 1` |
| UEV | +15 | 16 | `2^(10-6) - 1` |
| UIV | +31 | 32 | `2^(11-6) - 1` |
| UXV | +63 | 64 | `2^(12-6) - 1` |
| OpV | +127 | 128 | `2^(13-6) - 1` |
| MAX | +255 | 256 | `2^(14-6) - 1` |

## How it works
1. The multiblock searches for **all possible recipes** with available items
2. Each **thread** is assigned a different recipe
3. If a Parallel Hatch is installed, parallels are divided among threads
4. Each thread processes independently with its own timer

## Practical Example
Assembler with UV Thread Hatch (+3 threads) and 64-parallel Parallel Hatch:
```
Thread 1: 16x Steel Plate -> Motor (16 parallels)
Thread 2: 16x Copper Wire -> Cable (16 parallels)
Thread 3: 16x Iron Gear -> Pump (16 parallels)
Thread 4: 16x Bronze Rod -> Piston (16 parallels)
```
All 4 recipes run AT THE SAME TIME!

## Compatibility
Works on any multiblock using `WorkableElectricMultipleRecipesMachine`:
- Duration Tester (GTNA)
- Industrial Slaughterhouse (GTNA)
- Custom multiblocks via KubeJS (future)

!!! tip
    Combine Thread Hatches with Accelerate Hatches for insane speed on diverse recipes!
