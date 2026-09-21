# 🔥 Overclock Hatch

> *"Não basta ser rápido. É preciso ser absurdamente rápido."*

## O que é

A **Overclock Hatch** multiplica a velocidade de processamento aplicando uma redução direta na duração da receita. É o hatch mais poderoso para acelerar receitas, disponível a partir do tier UV.

## Tiers Disponíveis

| Tier | Multiplicador | Redução de Tempo | Velocidade Efetiva |
|------|--------------|-------------------|--------------------|
| <span class="tier-badge tier-uv">UV</span> | ×0.50 | -50% | 2x mais rápido |
| UHV | ×0.333 | -66.7% | 3x mais rápido |
| UEV | ×0.25 | -75% | 4x mais rápido |
| UIV | ×0.20 | -80% | 5x mais rápido |
| UXV | ×0.167 | -83.3% | 6x mais rápido |
| OpV | ×0.143 | -85.7% | 7x mais rápido |
| MAX | ×0.125 | -87.5% | 8x mais rápido |

> Valores iguais ao GTOCore (`100 / (tier - 6) %`). No UV o multiplicador é `0.50`, que é **igual ao
> overclock elétrico padrão** — ou seja, a Overclock Hatch UV não traz ganho; o ganho começa no UHV.

## Como Funciona

A Overclock Hatch **melhora o próprio overclock**: em vez de a duração cair para ×0.50 por passo de
overclock, ela cai para o multiplicador da hatch. O efeito entra no overclock do GT (na base
multi-receita) ou como correção pós-overclock via mixin (demais multiblocos elétricos), e a
Accelerate Hatch é aplicada por cima. Tudo multiplicativo:

```
Duração Final = Duração_Base × Overclock_Multiplier^passos × Accelerate_Factor
```

## Exemplo

Receita de 200 ticks em máquina EV:

1. GT Electric Overclock (LV→EV): 200 ÷ 4 = **50 ticks**
2. Accelerate Hatch EV (44%): 50 × 0.44 = **22 ticks**
3. Overclock Hatch UV (×0.50): 22 × 0.50 = **11 ticks**

**Resultado: 200 ticks → 11 ticks (≈18x mais rápido!)**

!!! tip "Combinação Poderosa"
    Overclock + Accelerate + Thread = receitas diferentes, todas ultrarrápidas, com paralelos massivos.
