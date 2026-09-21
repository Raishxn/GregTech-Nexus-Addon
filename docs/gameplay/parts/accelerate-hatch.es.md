# :zap: Accelerate Hatch
> *`El tiempo es la moneda mas valiosa. Este hatch ahorra ambos.`*

## Que es
El **Accelerate Hatch** reduce la duracion de TODAS las recetas en un multibloque. Cuanto mas avanzado el tier, mayor la reduccion.

## Tiers Disponibles
| Tier | Duracion Min (%) | Formula |
|------|:-:|---------|
| LV | 50% del original | `50 - 2x(1-1) = 50%` |
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

> Equivalente a la formula del GTOCore (`52 - 2xtier`): el valor es el porcentaje **minimo** de
> duracion (sin penalizacion); el piso absoluto es 1% y el techo 100% (config `accelerateHatch`).

## Mecanica Detallada
### Formula Base
```
Duracion_Reducida = Duracion_Base x (Porcentaje / 100)
```
### Penalizacion de Tier
La penalizacion sigue la **receta**, no la maquina (igual que GTOCore): una hatch de tier alto en una
maquina de tier alto **no** se penaliza por procesar recetas de tier bajo. Solo aplica cuando el tier
de la **receta** es mayor que el de la hatch:
```
Porcentaje_con_Penalizacion = Porcentaje_Base + max(0, Tier_Receta - Tier_Hatch) x 20
Maximo: 100% (sin efecto)
```

!!! example "Ejemplo"
    **Accelerate Hatch HV** procesando una receta **EV**:
    - Base: 46%, TierDiff: 1
    - Con penalizacion: 46% + (1 x 20) = 66%
    - Receta de 100 ticks -> 66 ticks

!!! warning
    Usar un Accelerate Hatch con tier muy inferior al de las recetas casi no tendra efecto.

## Compatibilidad
Funciona en **cualquier multibloque electrico** que acepte la ability `ACCELERATE_HATCH` (todos los
multibloques electricos de GTCEu/GTNA la reciben via `autoAbilities`), tanto por la logica
multi-receta como por el mixin global de `RecipeLogic`. El efecto se aplica **una sola vez** por
receta.

!!! note "Paridad con GTOCore"
    La penalizacion usa el tier **pre-overclock** de la receta (`recipeTier - hatchTier`), igual que
    GTOCore. El tier de la maquina no entra en el calculo.
