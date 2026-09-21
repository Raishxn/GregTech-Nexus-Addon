# ☀️ Large Steam Solar Boiler

> *"As long as there is sun and water, there is steam."*

## Quick Stats

| Property | Value |
|------------|-------|
| **Type** | Steam multiblock (expands horizontally) |
| **Size** | Dynamic: 5x1x5 (min) - expands in width/depth |
| **Max size** | 63 cells per side, 125 to the back |
| **Recipes** | Automatic generation (no manual recipe) |
| **Cycle** | 20 ticks (1 second) |
| **Output** | 200 mB per **sunlit Solar Boiling Cell** per cycle |
| **Consumption** | Water proportional to the steam produced (GTCEu `steamPerWater`) |
| **Requirements** | Daytime, no rain, and direct sky access above every cell |

## Structure

- **Border**: Steel Hull (or the fluid hatches)
- **Interior**: Solar Boiling Cell (each must see the sky)
- **Controller**: on the border
- **Hatches**: Fluid Input (water) and Fluid Output (steam) replace Steel Hull on the border

## Mechanics

- Only produces during the **day** and with **no rain**.
- Every cell with `level.canSeeSky(pos.above())` counts as **sunlit**.
- Each 20-tick cycle it produces `sunlit cells x 200 mB` of steam and consumes the matching water.

## Tips

!!! tip "Wireless"
    Pair it with the **Wireless Steam Output Hatch** to ship steam without pipes.

!!! tip "Placement"
    Build it as high as possible: any block above a cell turns it off.

!!! warning "Day only"
    Production stops at night or in rain. Keep a steam buffer or backup boilers.
