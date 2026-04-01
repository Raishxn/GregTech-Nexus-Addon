# :satellite: Wireless Steam Hatches
## Que son
Los **Wireless Steam Hatches** permiten transmitir vapor sin cables entre boilers y multibloques. Sin tubos, sin espagueti, sin lag de fluidos.

## Variantes
### Wireless Steam Input Hatch (Recibe Vapor)
| Variante | Tier | Capacidad |
|----------|------|-----------|
| **Bronze** | ULV | 20,000 mB |
| **Steel** | LV | 2,147,483,647 mB (MAX) |

Coloca en cualquier multibloque que necesite vapor. Extrae automaticamente de la red inalambrica.

### Wireless Steam Output Hatch (Envia Vapor)
| Variante | Tier | Capacidad |
|----------|------|-----------|
| **Bronze** | ULV | 20,000 mB |
| **Steel** | LV | 2,147,483,647 mB (MAX) |

Coloca en tu boiler o fuente de vapor. Envia vapor a la red inalambrica.

## Como Funciona la Red
1. Cada **jugador** tiene su propia red inalambrica (basada en UUID)
2. El vapor se almacena en un pool global (SavedData del mundo)
3. Los Output Hatches **agregan** vapor al pool
4. Los Input Hatches **consumen** vapor del pool
5. **Sin limite de distancia** - funciona entre dimensiones!

!!! tip "Mejora a Steel"
    Las variantes Steel tienen capacidad **Integer.MAX_VALUE**. Mejora lo antes posible!
