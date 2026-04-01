# :bar_chart: Advanced Parallel Hatch
> *`Cuando 256 paralelos no son suficientes...`*

## Que es
Los **Advanced Parallel Hatches** de GTNA extienden el sistema de paralelos del GregTech base a numeros masivos, empezando en 1,024 y llegando a 262,144 paralelos.

## Tiers Disponibles
| Tier | Paralelos | Comparacion |
|------|:---------:|-------------|
| UHV | 1,024 | 4x mas que GT base UV (256) |
| UEV | 4,096 | 16x mas que UV |
| UIV | 16,384 | 64x mas que UV |
| UXV | 65,536 | 256x mas que UV |
| OpV | 262,144 | 1024x mas que UV |

## Interaccion con Threads
Cuando se combinan con Thread Hatches, los paralelos se **distribuyen entre los threads**:
```
Paralelos por Thread = Total_Paralelos / Numero_de_Threads
```

!!! example "Ejemplo"
    Advanced Parallel UEV (4,096) + Thread Hatch UV (+3 threads):
    - Total threads: 4
    - Paralelos por thread: 4,096 / 4 = 1,024
    - Cada thread procesa 1,024 copias de una receta diferente

## Notas Importantes
- Solo **1** Parallel Hatch por multibloque (`.setMaxGlobalLimited(1)`)
- Funciona con cualquier multibloque que acepte `PartAbility.PARALLEL_HATCH`
- Part sharing **deshabilitado** para evitar exploits
