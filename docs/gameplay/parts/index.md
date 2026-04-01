# 🔩 Hatches & Partes / Parts

O GTNA adiciona diversas hatches especializadas que expandem as capacidades dos multiblocos.

## Tabela Rápida

| Hatch | Tiers | Efeito | Tipo |
|-------|-------|--------|------|
| [Wireless Steam Input](wireless-steam-hatches.md) | Bronze, Steel | Recebe vapor sem fio | Vapor |
| [Wireless Steam Output](wireless-steam-hatches.md) | Bronze, Steel | Envia vapor sem fio | Vapor |
| [Huge Steam Input Bus](huge-steam-bus.md) | ULV | Bus com mais slots | Itens |
| [Huge Steam Output Bus](huge-steam-bus.md) | ULV | Bus com mais slots | Itens |
| [Thread Hatch](thread-hatch.md) | ZPM → MAX | Receitas diferentes simultâneas | Processamento |
| [Accelerate Hatch](accelerate-hatch.md) | LV → MAX | Reduz duração das receitas | Velocidade |
| [Overclock Hatch](overclock-hatch.md) | UV → MAX | Multiplicador de overclock | Velocidade |
| [Advanced Parallel](advanced-parallel-hatch.md) | UHV → OpV | Paralelos massivos (1K→262K) | Escalabilidade |

---

## Como Funcionam Juntas

Quando múltiplas hatches são instaladas no mesmo multiblocko, seus efeitos se **combinam**:

```
Duração Final = Duração Base
  × Fator GT Overclock (reducão automática pelo GT)
  × Fator Accelerate Hatch (redução baseada em tier)
  × Fator Overclock Hatch (multiplicador de velocidade)

Threads ativos = 1 + Thread Hatch (receitas diferentes em paralelo)
Paralelos por Thread = Advanced Parallel / Threads
```

!!! example "Exemplo Combinado"
    **Máquina EV** com receita LV (200 ticks base):
    
    - GT Overclock: 200 → 50 ticks (2 níveis de OC)
    - Accelerate Hatch HV: 50 × 0.44 = 22 ticks
    - Overclock Hatch UV: 22 × 0.55 = 12 ticks
    - Thread Hatch UV (+3): 4 receitas diferentes simultâneas
    - Advanced Parallel UHV (1024): 256 paralelos por thread
    
    **Resultado**: 4 receitas × 256 paralelos = 1024 operações em 12 ticks!
