# :fire: Overclock Hatch
> *`No basta con ser rapido. Hay que ser absurdamente rapido.`*

## Que es
El **Overclock Hatch** multiplica la velocidad de procesamiento aplicando una reduccion directa en la duracion de la receta. El hatch mas poderoso para acelerar recetas, disponible desde tier UV.

## Tiers Disponibles
| Tier | Multiplicador | Reduccion de Tiempo | Velocidad Efectiva |
|------|:---:|:---:|:---:|
| UV | x0.55 | -45% | 1.82x mas rapido |
| UHV | x0.333 | -66.7% | 3x mas rapido |
| UEV | x0.25 | -75% | 4x mas rapido |
| UIV | x0.20 | -80% | 5x mas rapido |
| UXV | x0.167 | -83.3% | 6x mas rapido |
| OpV | x0.143 | -85.7% | 7x mas rapido |
| MAX | x0.125 | -87.5% | 8x mas rapido |

## Como Funciona
El multiplicador se aplica **despues** del GT Electric Overclock y **despues** del Accelerate Hatch:
```
Duracion_Final = Duracion_post_GT_OC x Factor_Accelerate x Multiplicador_Overclock
```

## Ejemplo
Receta de 200 ticks en maquina EV:
1. GT Electric Overclock (LV->EV): 200 / 4 = **50 ticks**
2. Accelerate Hatch EV (42%): 50 x 0.42 = **21 ticks**
3. Overclock Hatch UV (55%): 21 x 0.55 = **11 ticks**

**Resultado: 200 ticks -> 11 ticks (18x mas rapido!)**

!!! tip "Combinacion Poderosa"
    Overclock + Accelerate + Thread = recetas diferentes, todas ultrarapidas, con paralelos masivos.
