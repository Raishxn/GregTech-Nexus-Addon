# :satellite: Red de Vapor Inalambrico
## Como Funciona
Cada jugador tiene su propia red inalambrica de vapor. Usa `SteamNetworkData` (SavedData) para almacenar vapor globalmente.

### Componentes
- **Wireless Steam Output Hatch** - Conecta al boiler, envia vapor a la red
- **Wireless Steam Input Hatch** - Conecta a la maquina, recibe vapor de la red

### Caracteristicas
- **Sin limite de distancia** (funciona entre dimensiones!)
- Cada jugador tiene red separada (basada en UUID)
- Variantes Steel tienen capacidad maxima (Integer.MAX_VALUE)

### Consejos
- Cambia a Steel Wireless Hatches lo antes posible
- Un Mega Solar Boiler con Wireless Output puede alimentar toda tu fabrica
