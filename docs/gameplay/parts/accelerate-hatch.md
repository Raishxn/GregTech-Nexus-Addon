# ⚡ Accelerate Hatch

> *"O tempo é a moeda mais valiosa. Esta hatch economiza ambos."*

## O que é

A **Accelerate Hatch** reduz a duração de TODAS as receitas em um multiblocko. Quanto mais avançado o tier, maior a redução.

## Tiers Disponíveis

| Tier | Redução Mín. (%) | Fórmula |
|------|-------------------|---------|
| <span class="tier-badge tier-lv">LV</span> | 48% da duração original | `50 - 2×(1-1) = 48%` |
| <span class="tier-badge tier-mv">MV</span> | 46% | `50 - 2×(2-1) = 46%` |
| <span class="tier-badge tier-hv">HV</span> | 44% | `50 - 2×(3-1) = 44%` |
| <span class="tier-badge tier-ev">EV</span> | 42% | `50 - 2×(4-1) = 42%` |
| <span class="tier-badge tier-iv">IV</span> | 40% | `50 - 2×(5-1) = 40%` |
| <span class="tier-badge tier-luv">LuV</span> | 38% | `50 - 2×(6-1) = 38%` |
| <span class="tier-badge tier-zpm">ZPM</span> | 36% | `50 - 2×(7-1) = 36%` |
| <span class="tier-badge tier-uv">UV</span> | 34% | `50 - 2×(8-1) = 34%` |
| UHV | 32% | `50 - 2×(9-1) = 32%` |
| UEV | 30% | `50 - 2×(10-1) = 30%` |
| UIV | 28% | `50 - 2×(11-1) = 28%` |
| UXV | 26% | `50 - 2×(12-1) = 26%` |
| OpV | 24% | `50 - 2×(13-1) = 24%` |

## Mecânica Detalhada

### Fórmula Base
```
Duração_Reduzida = Duração_Base × (Porcentagem / 100)
```

### Penalidade de Tier
Se o tier da Accelerate Hatch é **menor** que o tier da máquina, a eficiência diminui:
```
Porcentagem_com_Penalidade = Min_Porcentagem + (TierDiff × 20)
Máximo: 100% (sem efeito)
```

!!! example "Exemplo"
    **Accelerate Hatch HV** em máquina **EV**:
    
    - Base: 44%
    - TierDiff: EV(4) - HV(3) = 1
    - Com penalidade: 44% + (1 × 20) = 64%
    - Receita de 100 ticks → 64 ticks

!!! warning "Cuidado"
    Usar uma Accelerate Hatch com tier muito abaixo da máquina quase não dará efeito. Recomendamos usar hatches do mesmo tier ou superior.

## Compatibilidade

Funciona em qualquer multiblocko que use `WorkableElectricMultipleRecipesMachine`.
