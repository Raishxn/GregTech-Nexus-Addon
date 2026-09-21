# :fire: Overclock Hatch
> *`No basta con ser rapido. Hay que ser absurdamente rapido.`*

## Que es
El **Overclock Hatch** multiplica la velocidad de procesamiento aplicando una reduccion directa en la duracion de la receta. El hatch mas poderoso para acelerar recetas, disponible desde tier UV.

## Tiers Disponibles
| Tier | Multiplicador | Reduccion de Tiempo | Velocidad Efectiva |
|------|:---:|:---:|:---:|
| UV | x0.50 | -50% | 2x mas rapido |
| UHV | x0.333 | -66.7% | 3x mas rapido |
| UEV | x0.25 | -75% | 4x mas rapido |
| UIV | x0.20 | -80% | 5x mas rapido |
| UXV | x0.167 | -83.3% | 6x mas rapido |
| OpV | x0.143 | -85.7% | 7x mas rapido |
| MAX | x0.125 | -87.5% | 8x mas rapido |

> Valores iguales al GTOCore (`100 / (tier - 6) %`). En UV el multiplicador es `0.50`, que es **igual
> al overclock electrico estandar** - la Overclock Hatch UV no da ganancia; la ganancia empieza en UHV.

## Como Funciona
La Overclock Hatch **mejora el propio overclock**: en vez de que la duracion caiga a x0.50 por paso de
overclock, cae al multiplicador de la hatch. Entra en el overclock del GT (en la base multi-receta) o
se aplica como correccion post-overclock via mixin (demas multibloques electricos), y el Accelerate
Hatch se aplica encima. Todo multiplicativo:
```
Duracion_Final = Duracion_Base x Multiplicador_Overclock^pasos x Factor_Accelerate
```

## Ejemplo
Receta de 200 ticks en maquina EV:
1. GT Electric Overclock (LV->EV): 200 / 4 = **50 ticks**
2. Accelerate Hatch EV (44%): 50 x 0.44 = **22 ticks**
3. Overclock Hatch UV (x0.50): 22 x 0.50 = **11 ticks**

**Resultado: 200 ticks -> 11 ticks (~18x mas rapido!)**

!!! tip "Combinacion Poderosa"
    Overclock + Accelerate + Thread = recetas diferentes, todas ultrarapidas, con paralelos masivos.
