# 🐄 Industrial Slaughterhouse

> *"Desde passivos inocentes até dragões ancestrais — tudo vira loot."*

## Stats Rápidos

| Propriedade | Valor |
|------------|-------|
| **Tipo** | Multiblocko Elétrico |
| **Tamanho** | Definido na estrutura (ver JEI) |
| **Receitas** | Slaughterhouse (geração procedural de loot) |
| **Paralelos** | Baseado em tier — `max(1, (tier-2) × 8)` loops |
| **Velocidade** | Standard GT Overclock (NON_PERFECT) |
| **Consumo** | Variável por tier (EU/t) |
| **Hatches GTNA** | ✅ Thread, ✅ Accelerate, ✅ Overclock, ✅ Parallel |

## Benefícios

- ✅ **4 modos** de operação via Programmed Circuit
- ✅ **Drops reais** — usa as Loot Tables do Minecraft
- ✅ **Multiplicador exponencial** — quanto maior o tier, mais drops
- ✅ **Suporta TODAS as hatches GTNA** — Thread, Accelerate, Overclock, Parallel
- ✅ **GUI informativa** — mostra modo atual, multiplicador, e últimos drops

## Modos de Operação

Selecione o modo inserindo um **Programmed Circuit** na Input Bus:

<div class="mode-card mode-passive" markdown>

### 🐔 Modo Passivo (Circuit 0 ou 1)
**Tier Mínimo**: <span class="tier-badge tier-lv">LV</span> (32 EU/t)

| Mob | Drop Possíveis |
|-----|---------------|
| Chicken | Raw Chicken, Feather |
| Cow | Raw Beef, Leather |
| Pig | Raw Porkchop |
| Sheep | Raw Mutton, Wool |
| Rabbit | Raw Rabbit, Rabbit Hide, Rabbit's Foot |
| Horse | Leather |
| Goat | — |

**Multiplicador**: `2^(tier - LV)` — Ex: EV = 2³ = 8x drops

</div>

<div class="mode-card mode-hostile" markdown>

### 💀 Modo Hostil (Circuit 2)
**Tier Mínimo**: <span class="tier-badge tier-mv">MV</span> (128 EU/t)

| Mob | Drops Possíveis |
|-----|---------------|
| Zombie | Rotten Flesh, Iron Ingot, Carrot, Potato |
| Skeleton | Bone, Arrow |
| Creeper | Gunpowder |
| Spider | String, Spider Eye |
| Enderman | Ender Pearl |
| Witch | Glass Bottle, Glowstone Dust, Redstone, etc. |
| Blaze | Blaze Rod |

**Multiplicador**: `2^(tier - MV)` — Ex: IV = 2³ = 8x drops

</div>

<div class="mode-card mode-boss" markdown>

### 👑 Modo Bosses (Circuit 3)
**Tier Mínimo**: <span class="tier-badge tier-zpm">ZPM</span> (524,288 EU/t)

| Mob | Drops Possíveis |
|-----|---------------|
| Warden | Sculk Catalyst |
| Wither | Nether Star |
| Elder Guardian | Prismarine Shard/Crystal, Wet Sponge |

**Multiplicador**: `3^(tier - ZPM)` — Ex: UV = 3¹ = 3x drops

</div>

<div class="mode-card mode-dragon" markdown>

### 🐉 Modo Dragon (Circuit 4)
**Tier Mínimo**: UHV (2,097,152 EU/t)

| Drop | Quantidade Base |
|------|----------------|
| Dragon Egg | 1 |
| Dragon Breath | 4 |
| Dragon Head | 1 |

**Multiplicador**: `5^(tier - UHV)` — Ex: UEV = 5¹ = 5x drops

</div>

## Tabela de Multiplicadores por Tier e Modo

| Tier | Passive (base=2) | Hostile (base=2) | Bosses (base=3) | Dragon (base=5) |
|------|:-:|:-:|:-:|:-:|
| <span class="tier-badge tier-lv">LV</span> | 1x | ❌ | ❌ | ❌ |
| <span class="tier-badge tier-mv">MV</span> | 2x | 1x | ❌ | ❌ |
| <span class="tier-badge tier-hv">HV</span> | 4x | 2x | ❌ | ❌ |
| <span class="tier-badge tier-ev">EV</span> | 8x | 4x | ❌ | ❌ |
| <span class="tier-badge tier-iv">IV</span> | 16x | 8x | ❌ | ❌ |
| <span class="tier-badge tier-luv">LuV</span> | 32x | 16x | ❌ | ❌ |
| <span class="tier-badge tier-zpm">ZPM</span> | 64x | 32x | 1x | ❌ |
| <span class="tier-badge tier-uv">UV</span> | 128x | 64x | 3x | ❌ |
| UHV | 256x | 128x | 9x | 1x |
| UEV | 512x | 256x | 27x | 5x |
| UIV | 1024x | 512x | 81x | 25x |

## Loops por Tier

Além do multiplicador, o número de **loops** (quantidade de mobs processados por operação) também escala:

```
Loops = max(1, (tier - 2) × 8)
```

| Tier | Loops |
|------|-------|
| LV (1) | 1 |
| MV (2) | 1 |
| HV (3) | 8 |
| EV (4) | 16 |
| IV (5) | 24 |
| LuV (6) | 32 |
| ZPM (7) | 40 |
| UV (8) | 48 |

## Dicas de Uso

!!! tip "Output Buses"
    Use **múltiplas Output Buses** para evitar overflow de itens. A GUI mostra quantas buses estão conectadas.

!!! tip "Combinação com Hatches GTNA"
    Adicione uma **Thread Hatch** para processar modos diferentes simultaneamente! Com UV Thread Hatch, rode Passive + Hostile + Bosses em paralelo.

!!! warning "Custo Energético"
    O modo **Dragon** requer no mínimo UHV. Certifique-se de ter energia rificiente antes de ativar.

!!! info "Loot Tables"
    Os drops são gerados pelas **Loot Tables reais do Minecraft**. Mods que modificam loot tables de mobs também afetam esta máquina.
