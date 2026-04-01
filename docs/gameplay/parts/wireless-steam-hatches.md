# 📡 Wireless Steam Hatches

## O que são

As **Wireless Steam Hatches** permitem transmitir vapor sem fio entre boilers e multiblocos. Sem tubos, sem espaguete, sem lag de fluido.

## Variantes

### Wireless Steam Input Hatch (Recebe Vapor)

| Variante | Tier | Capacidade |
|----------|------|-----------|
| **Bronze** | ULV | 20,000 mB |
| **Steel** | LV | 2,147,483,647 mB (MAX) |

Coloque em qualquer multiblocko que precisa de vapor. Ela puxa automaticamente da rede wireless.

### Wireless Steam Output Hatch (Envia Vapor)

| Variante | Tier | Capacidade |
|----------|------|-----------|
| **Bronze** | ULV | 20,000 mB |
| **Steel** | LV | 2,147,483,647 mB (MAX) |

Coloque no seu boiler ou qualquer fonte de vapor. Ela envia o vapor para a rede wireless.

## Como a Rede Funciona

1. Cada **jogador** tem sua própria rede wireless (baseada em UUID)
2. O vapor é armazenado em um pool global (SavedData do mundo)
3. Output Hatches **adicionam** vapor ao pool
4. Input Hatches **consomem** vapor do pool
5. Sem limite de distância — funciona entre dimensões!

!!! tip "Upgrade para Steel"
    As variantes Steel têm capacidade de armazenamento **Integer.MAX_VALUE**. Faça o upgrade o mais rápido possível!

## Huge Steam Buses
