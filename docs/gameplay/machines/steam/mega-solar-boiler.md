# ☀️ Mega Solar Boiler

> *"Pode produzir mais vapor do que jogos não jogados na sua biblioteca."*

## Stats Rápidos

| Propriedade | Valor |
|------------|-------|
| **Tipo** | Multibloco a Vapor (Expansível) |
| **Tamanho** | Dinâmico: 3w×2d (mín) até 33w×34d (máx) |
| **Receitas** | Geração automática (sem receita manual) |
| **Paralelos** | N/A — produção contínua |
| **Velocidade** | Ciclo de 20 ticks (1 segundo) |
| **Eficiência** | Requer acesso ao céu (sem blocos acima) |
| **Consumo** | Água proporcional ao vapor gerado |
| **Produção** | 500 mB/t (10,000 L/s) por bloco iluminado |

## Benefícios

- ✅ **Vapor infinito** — enquanto houver sol e água, produz vapor
- ✅ **Expandível** — quanto maior a estrutura, mais vapor
- ✅ **Sem eletricidade** — funciona 100% a vapor
- ✅ **Auto-scaling** — a produção escala linearmente com blocos ativos
- ⚠️ **Requer dia** — não funciona à noite ou durante chuva

## Mecânica Detalhada

### Produção por Bloco

Cada **Solar Boiling Cell** que tem **visão direta do céu** produz:

```
Produção por bloco = megaSolarSteamPerBlock (config: 500 mB/tick)
500 mB/t × 20 ticks = 10,000 mB/s = 10 L/s de Steam
```

O consumo de água é calculado automaticamente baseado na config `steamPerWater` do GregTech base.

### Blocos Ativos (Sunlit)

O sistema verifica cada Solar Boiling Cell individualmente:
- Se `level.canSeeSky(blockPos.above())` → **ativo** (produz vapor)
- Se bloqueado → **inativo** (não conta)

### Dimensões

| Parâmetro | Mín | Máx | Fórmula |
|-----------|-----|-----|---------|
| Largura (W) | 3 | 33 | `lDist + rDist + 3` (MAX_LR_DIST=16 por lado) |
| Profundidade (D) | 2 | 34 | `bDist + 2` (MAX_B_DIST=32) |
| Altura (H) | 1 | 1 | Sempre 1 camada + casing |
| Solar Cells | 1 | 992 | Interior preenchido por Solar Cells |

## Estrutura

=== "Layout"
    A estrutura é uma **plataforma aberta**:
    
    - **Borda**: Hyper Pressure Breel Casing
    - **Interior**: Solar Boiling Cell (deve ver o céu)
    - **Controller**: Na borda frontal, centro
    - **Hatches**: Na borda (substituem Hyper Pressure Breel Casing)
    
    ```
    Vista de cima (exemplo 7×6):
    AAAAAAA
    ABBBBBA
    ABBBBBA
    ABBBBBA
    ABBBBBA
    AAA~AAA   ← Controller
    
    A = Hyper Pressure Breel Casing (ou Hatches)
    B = Solar Boiling Cell
    ~ = Controller
    ```

=== "Materiais Necessários"
    | Material | Fórmula | Exemplo 7×6 | Exemplo 15×20 | Exemplo 33×34 |
    |----------|---------|-------------|---------------|---------------|
    | **Hyper Pressure Breel Casing** | `2×(W+D) - 4 + hatches` | ~22 | ~66 | ~130 |
    | **Solar Boiling Cell** | `(W-2) × (D-2)` | 20 | 234 | 992 |
    | **Fluid Input Hatch** | 1 (mín) | 1 | 1 | 1 |
    | **Fluid Output Hatch** | 1 (mín) | 1 | 2+ | 4+ |

## 📊 Calculadora de Tamanho

!!! tip "Planeje seu Mega Solar Boiler"

Use esta tabela para planejar sua estrutura. A produção é baseada na config padrão (500 mB/t por bloco).

| Tamanho (W×D) | Solar Cells | Produção/tick | Produção/s | Produção/min | Materiais (Casing+Cells) |
|:---:|:---:|:---:|:---:|:---:|:---:|
| 3×3 | 1 | 500 mB | 10 L | 600 L | ~8 + 1 |
| 5×5 | 9 | 4,500 mB | 90 L | 5,400 L | ~16 + 9 |
| 7×6 | 20 | 10,000 mB | 200 L | 12,000 L | ~22 + 20 |
| 11×12 | 90 | 45,000 mB | 900 L | 54,000 L | ~42 + 90 |
| 15×20 | 234 | 117,000 mB | 2,340 L | 140,400 L | ~66 + 234 |
| 21×25 | 437 | 218,500 mB | 4,370 L | 262,200 L | ~88 + 437 |
| 27×30 | 700 | 350,000 mB | 7,000 L | 420,000 L | ~110 + 700 |
| **33×34** | **992** | **496,000 mB** | **9,920 L** | **595,200 L** | **~130 + 992** |

!!! warning "Custo-Benefício"
    Cada Solar Boiling Cell requer **Hyper Pressure Breel Casing** e **Borosilicate Glass** no crafting. Considere se o investimento em materiais vale a produção extra antes de fazer a estrutura máxima.

## Exemplo: Construindo um 33×34 (Máximo)

### Recursos Necessários (Aproximado)

| Material | Quantidade | Dificuldade |
|----------|-----------|-------------|
| Hyper Pressure Breel Casing | ~130 | Requer Breel (2×Bronze + 1×Steel) |
| Solar Boiling Cell | 992 | Crafting específico |
| Fluid Input Hatch | 1 | Qualquer tier |
| Fluid Output Hatch | 4+ | Recomendável múltiplas |
| Água | ~500,000 mB/tick | Fonte infinita recomendada |

### Produção do Máximo

```
992 blocos × 500 mB/tick = 496,000 mB/tick
= 9,920 L/s de Steam
= 595,200 L/min de Steam

Consumo de água ≈ 496,000 / steamPerWater mB/tick
```

## Dicas

!!! tip "Posicionamento"
    Construa a plataforma no local mais alto possível para garantir que nenhum bloco bloqueie a visão do céu.

!!! tip "Wireless Steam"
    Combine com **Wireless Steam Output Hatch** para distribuir o vapor sem tubos. Um MSB de 33×34 pode alimentar uma fábrica inteira!

!!! warning "Comportamento Diurno"
    A produção para **completamente** à noite e durante chuva. Considere ter um buffer de vapor ou boilers de backup.
