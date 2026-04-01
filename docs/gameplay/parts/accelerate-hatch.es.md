# :zap: Accelerate Hatch
> *`El tiempo es la moneda mas valiosa. Este hatch ahorra ambos.`*

## Que es
El **Accelerate Hatch** reduce la duracion de TODAS las recetas en un multibloque. Cuanto mas avanzado el tier, mayor la reduccion.

## Tiers Disponibles
| Tier | Duracion Min (%) | Formula |
|------|:-:|---------|
| LV | 48% del original | `50 - 2x(1-1) = 48%` |
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

## Mecanica Detallada
### Formula Base
```
Duracion_Reducida = Duracion_Base x (Porcentaje / 100)
```
### Penalizacion de Tier
Si el tier del Accelerate Hatch es **menor** que el tier de la maquina, la eficiencia disminuye:
```
Porcentaje_con_Penalizacion = Min_Porcentaje + (TierDiff x 20)
Maximo: 100% (sin efecto)
```

!!! example "Ejemplo"
    **Accelerate Hatch HV** en maquina **EV**:
    - Base: 44%, TierDiff: 1
    - Con penalizacion: 44% + (1 x 20) = 64%
    - Receta de 100 ticks -> 64 ticks

!!! warning
    Usar un Accelerate Hatch con tier muy inferior a la maquina casi no tendra efecto.

## Compatibilidad
Funciona en cualquier multibloque que use `WorkableElectricMultipleRecipesMachine`.
