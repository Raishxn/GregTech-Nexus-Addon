# 🔥 Overclock Hatch

> *"Não basta ser rápido. É preciso ser absurdamente rápido."*

## O que é

A **Overclock Hatch** multiplica a velocidade de processamento aplicando uma redução direta na duração da receita. É o hatch mais poderoso para acelerar receitas, disponível a partir do tier UV.

## Tiers Disponíveis

| Tier | Multiplicador | Redução de Tempo | Velocidade Efetiva |
|------|--------------|-------------------|--------------------|
| <span class="tier-badge tier-uv">UV</span> | ×0.55 | -45% | 1.82x mais rápido |
| UHV | ×0.333 | -66.7% | 3x mais rápido |
| UEV | ×0.25 | -75% | 4x mais rápido |
| UIV | ×0.20 | -80% | 5x mais rápido |
| UXV | ×0.167 | -83.3% | 6x mais rápido |
| OpV | ×0.143 | -85.7% | 7x mais rápido |
| MAX | ×0.125 | -87.5% | 8x mais rápido |

## Como Funciona

O multiplicador é aplicado **depois** do GT Electric Overclock e **depois** do Accelerate Hatch:

```
Duração Final = Duração_pós_GT_OC × Accelerate_Factor × Overclock_Multiplier
```

## Exemplo

Receita de 200 ticks em máquina EV:

1. GT Electric Overclock (LV→EV): 200 ÷ 4 = **50 ticks**
2. Accelerate Hatch EV (42%): 50 × 0.42 = **21 ticks**
3. Overclock Hatch UV (55%): 21 × 0.55 = **11 ticks**

**Resultado: 200 ticks → 11 ticks (18x mais rápido!)**

!!! tip "Combinação Poderosa"
    Overclock + Accelerate + Thread = receitas diferentes, todas ultrarrápidas, com paralelos massivos.
