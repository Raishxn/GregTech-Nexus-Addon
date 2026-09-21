# ⚡ Accelerate Hatch

> *"O tempo é a moeda mais valiosa. Esta hatch economiza ambos."*

## O que é

A **Accelerate Hatch** reduz a duração de TODAS as receitas em um multiblocko. Quanto mais avançado o tier, maior a redução.

## Tiers Disponíveis

| Tier | Redução Mín. (%) | Fórmula |
|------|-------------------|---------|
| <span class="tier-badge tier-lv">LV</span> | 50% da duração original | `50 - 2×(1-1) = 50%` |
| <span class="tier-badge tier-mv">MV</span> | 48% | `50 - 2×(2-1) = 48%` |
| <span class="tier-badge tier-hv">HV</span> | 46% | `50 - 2×(3-1) = 46%` |
| <span class="tier-badge tier-ev">EV</span> | 44% | `50 - 2×(4-1) = 44%` |
| <span class="tier-badge tier-iv">IV</span> | 42% | `50 - 2×(5-1) = 42%` |
| <span class="tier-badge tier-luv">LuV</span> | 40% | `50 - 2×(6-1) = 40%` |
| <span class="tier-badge tier-zpm">ZPM</span> | 38% | `50 - 2×(7-1) = 38%` |
| <span class="tier-badge tier-uv">UV</span> | 36% | `50 - 2×(8-1) = 36%` |
| UHV | 34% | `50 - 2×(9-1) = 34%` |
| UEV | 32% | `50 - 2×(10-1) = 32%` |
| UIV | 30% | `50 - 2×(11-1) = 30%` |
| UXV | 28% | `50 - 2×(12-1) = 28%` |
| OpV | 26% | `50 - 2×(13-1) = 26%` |
| MAX | 24% | `50 - 2×(14-1) = 24%` |

> Equivalente à fórmula do GTOCore (`52 - 2×tier`): o valor é a porcentagem **mínima** da duração
> (sem penalidade); o mínimo absoluto é 1% e o máximo 100% (config `accelerateHatch`).

## Mecânica Detalhada

### Fórmula Base
```
Duração_Reduzida = Duração_Base × (Porcentagem / 100)
```

### Penalidade de Tier
A penalidade segue a **receita**, não a máquina (igual ao GTOCore): uma hatch de tier alto numa
máquina de tier alto **não** é punida por processar receitas de tier baixo. Só há penalidade quando o
tier da **receita** é maior que o tier da hatch:
```
Porcentagem_com_Penalidade = Porcentagem_Base + max(0, Tier_Receita - Tier_Hatch) × 20
Máximo: 100% (sem efeito)
```

!!! example "Exemplo"
    **Accelerate Hatch HV** processando uma receita **EV**:
    
    - Base: 46%
    - TierDiff: EV(4) - HV(3) = 1
    - Com penalidade: 46% + (1 × 20) = 66%
    - Receita de 100 ticks → 66 ticks

!!! warning "Cuidado"
    Usar uma Accelerate Hatch com tier muito abaixo do tier das receitas quase não dará efeito. Recomendamos hatches do mesmo tier ou superior.

## Compatibilidade

Funciona em **qualquer multiblocko elétrico** que aceite a ability `ACCELERATE_HATCH` (todos os
multiblocos elétricos do GTCEu/GTNA a recebem via `autoAbilities`), tanto pela lógica multi-receita
quanto pelo mixin global de `RecipeLogic`. O efeito é aplicado **uma única vez** por receita.

!!! note "Paridade com o GTOCore"
    A penalidade usa o tier **pré-overclock** da receita (`recipeTier - hatchTier`), exatamente como o
    GTOCore. O tier da máquina não entra na conta.
