# 🔥 Large Steam Furnace

> *"Não tente assar cookies dentro. Eles vão vaporizar instantaneamente."*

## Stats Rápidos

| Propriedade | Valor |
|------------|-------|
| **Tipo** | Multiblocko a Vapor |
| **Tamanho** | 9×8×9 (WxHxD) |
| **Receitas** | Furnace + Blast Furnace |
| **Paralelos** | 128 |
| **Velocidade** | 900% mais rápido (9x) |
| **Eficiência** | 50% do consumo normal de vapor |
| **Consumo** | Steam variável (50% do normal) |

## Benefícios

- ✅ **9x mais rápido** que o Steam Furnace singleblock
- ✅ **50% menos vapor** — eficiência energética absurda
- ✅ **128 paralelos** — processe stacks inteiros de uma vez
- ✅ **Aceita Blast Furnace** — faz receitas de EBF sem eletricidade (≤1800K)
- ✅ **Wall sharing** — pode compartilhar paredes com outros multiblocks

## Estrutura

=== "Layout"
    A estrutura é um bloco oco de 9×8×9:
    
    - **Exterior**: Bronze Plated Bricks (com Stone Bricks na decoração)
    - **Interior Superior**: Steel Solid Casing (parcial)
    - **Pipes**: Bronze Pipe Casing (camada interna)
    - **Frames**: Bronze Frames (vigas estruturais nos cantos)
    - **Centro**: Oco (ar)

    ```
    Vista frontal (camada do controller):
    FFFFFFFFF    F = Bronze Plated Bricks
    DAAAAAAAD    D = Bronze Frame
    DAAAAAAAD    A = Bronze Plated Bricks (ou Hatches)
    DAAASAAAD    S = Controller (~)
    DAAAAAAAD
    DAAAAAAAD
    DAAAAAAAD
    FFFFFFFFF
    ```

=== "Materiais Necessários"
    | Material | Quantidade (~) | Como Obter |
    |----------|:---:|------------|
    | Bronze Plated Bricks | ~200 | Crafting padrão GT |
    | Steel Solid Casing | ~60 | Steel Plates + Wrench |
    | Bronze Pipe Casing | ~30 | Bronze Pipes |
    | Bronze Frame | ~20 | Bronze Rods + Wrench |
    | Stone Bricks | ~30 | 4x Stone |
    | Steam Hatch | ×1 | — |
    | Steam Item Input | ×1+ | — |
    | Steam Item Output | ×1+ | — |

=== "Predicates"
    ```
    S = Controller
    A = Bronze Plated Bricks (ou Steam I/O + Steam Hatch)
    B = Bronze Pipe Casing
    C = Bronze Gearbox Casing
    D = Bronze Frame
    E = Bronze Plated Bricks (topo)
    F = Bronze Plated Bricks ('F')
    G = Stone Bricks
    H = Steel Solid Casing
    ```

## Dicas

!!! tip "Receitas de Blast Furnace"
    O Large Steam Furnace pode processar receitas do **Electric Blast Furnace** que tenham temperatura ≤ 1800K. Isso permite produzir materiais avançados sem eletricidade!

!!! tip "Eficiência de Vapor"
    Com 50% de eficiência, este é o multiblocko mais econômico em vapor. Priorize construí-lo antes de investir em outros.

!!! warning "Tamanho"
    A estrutura é grande (9×8×9). Certifique-se de ter espaço suficiente e materiais antes de começar.
