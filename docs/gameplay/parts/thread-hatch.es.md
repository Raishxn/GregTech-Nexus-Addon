# :thread: Thread Hatch
> *`Un hilo por receta. El futuro de la manufactura es paralelo.`*

## Que es
El **Thread Hatch** permite que un multibloque procese **recetas diferentes al mismo tiempo**. A diferencia del Parallel Hatch (que repite la MISMA receta), el Thread Hatch ejecuta recetas completamente distintas simultaneamente.

## Tiers Disponibles
| Tier | Threads Extra | Total (con base 1) | Formula |
|------|:---:|:---:|---------|
| ZPM | +1 | 2 | `2^(7-6) - 1` |
| UV | +3 | 4 | `2^(8-6) - 1` |
| UHV | +7 | 8 | `2^(9-6) - 1` |
| UEV | +15 | 16 | `2^(10-6) - 1` |
| UIV | +31 | 32 | `2^(11-6) - 1` |
| UXV | +63 | 64 | `2^(12-6) - 1` |
| OpV | +127 | 128 | `2^(13-6) - 1` |
| MAX | +255 | 256 | `2^(14-6) - 1` |

## Como funciona
1. El multibloque busca **todas las recetas posibles** con los items disponibles
2. Cada **thread** se asigna a una receta diferente
3. Si hay un Parallel Hatch instalado, los paralelos se dividen entre threads
4. Cada thread procesa independientemente con su propio temporizador

## Ejemplo Practico
Assembler con Thread Hatch UV (+3 threads) y Parallel Hatch con 64 paralelos:
```
Thread 1: 16x Steel Plate -> Motor (16 paralelos)
Thread 2: 16x Copper Wire -> Cable (16 paralelos)
Thread 3: 16x Iron Gear -> Pump (16 paralelos)
Thread 4: 16x Bronze Rod -> Piston (16 paralelos)
```
Las 4 recetas corren AL MISMO TIEMPO!

## Compatibilidad
Funciona en cualquier multibloque que use `WorkableElectricMultipleRecipesMachine`:
- Duration Tester (GTNA)
- Industrial Slaughterhouse (GTNA)
- Multibloques personalizados via KubeJS (futuro)

!!! tip
    Combina Thread Hatches con Accelerate Hatches para velocidad insana en recetas diversas!
