# ☀️ Large Steam Solar Boiler

> *"Mientras haya sol y agua, hay vapor."*

## Stats Rapidos

| Propiedad | Valor |
|------------|-------|
| **Tipo** | Multibloque de vapor (se expande en horizontal) |
| **Tamano** | Dinamico: 5x1x5 (min) - se expande en ancho/profundidad |
| **Tamano maximo** | 63 celdas por lado, 125 hacia atras |
| **Recetas** | Generacion automatica (sin receta manual) |
| **Ciclo** | 20 ticks (1 segundo) |
| **Produccion** | 200 mB por **Solar Boiling Cell iluminada** por ciclo |
| **Consumo** | Agua proporcional al vapor generado (`steamPerWater` del GTCEu) |
| **Requisitos** | Dia, sin lluvia y vision directa del cielo sobre cada celda |

## Estructura

- **Borde**: Steel Hull (o los hatches de fluido)
- **Interior**: Solar Boiling Cell (cada una debe ver el cielo)
- **Controller**: en el borde
- **Hatches**: Fluid Input (agua) y Fluid Output (vapor) reemplazan Steel Hull en el borde

## Mecanica

- Solo produce de **dia** y **sin lluvia**.
- Cada celda con `level.canSeeSky(pos.above())` cuenta como **iluminada**.
- Cada ciclo de 20 ticks produce `celdas iluminadas x 200 mB` de vapor y consume el agua
  correspondiente.

## Consejos

!!! tip "Wireless"
    Combinalo con la **Wireless Steam Output Hatch** para distribuir vapor sin tuberias.

!!! tip "Posicion"
    Construyelo lo mas alto posible: cualquier bloque sobre una celda la apaga.

!!! warning "Solo de dia"
    La produccion se detiene de noche o con lluvia. Ten un buffer de vapor o boilers de respaldo.
