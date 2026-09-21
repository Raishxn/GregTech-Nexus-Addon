# 🧵 Thread Hatch

> *"Uma thread para cada receita. O futuro da manufatura é paralelo."*

## O que é

A **Thread Hatch** permite que um multiblocko processe **receitas diferentes ao mesmo tempo**. Diferente do Parallel Hatch (que repete a MESMA receita), o Thread Hatch roda receitas completamente distintas simultaneamente.

## Tiers Disponíveis

| Tier | Threads Adicionais | Total (com base 1) | Fórmula |
|------|--------------------|--------------------|---------|
| <span class="tier-badge tier-zpm">ZPM</span> | +1 | 2 | `2^(7-6) - 1` |
| <span class="tier-badge tier-uv">UV</span> | +3 | 4 | `2^(8-6) - 1` |
| UHV | +7 | 8 | `2^(9-6) - 1` |
| UEV | +15 | 16 | `2^(10-6) - 1` |
| UIV | +31 | 32 | `2^(11-6) - 1` |
| UXV | +63 | 64 | `2^(12-6) - 1` |
| OpV | +127 | 128 | `2^(13-6) - 1` |
| MAX | +255 | 256 | `2^(14-6) - 1` |

## Como funciona

1. O multiblocko busca **todas as receitas possíveis** com os itens disponíveis
2. Cada **thread** é atribuída a uma receita diferente
3. Se há Parallel Hatch instalado, os paralelos são divididos entre threads
4. Cada thread processa independentemente, com seu próprio timer

## Exemplo Prático

Imagine um Assembler com Thread Hatch UV (+3 threads) e Parallel Hatch com 64 paralelos:

```
Thread 1: 16x Steel Plate → Motor (16 paralelos)
Thread 2: 16x Copper Wire → Cable (16 paralelos)
Thread 3: 16x Iron Gear → Pump (16 paralelos)
Thread 4: 16x Bronze Rod → Piston (16 paralelos)
```

Todas as 4 receitas rodam AO MESMO TEMPO!

## Compatibilidade

A Thread Hatch é reconhecida pela ability `THREAD_HATCH`, que só existe em multiblocos baseados em
`WorkableElectricMultipleRecipesMachine`:

- ✅ **Duration Tester** (máquina de referência do GTNA)
- ✅ Multiblocos customizados via KubeJS
- 🚧 Demais controladores do GTNA — a migração para a base multi-receita é a **fase 2** do
  [manifest de port](../../roadmap/multiblock-port-manifest.md) (regra 8); enquanto ela não acontece,
  as máquinas existentes **não** aceitam a Thread Hatch
- ❌ Multiblocos do GTCEu base (não suportado)
- ❌ Máquinas steam: usam threads fixas (`FixedThreadSteamParallelMachine`), sem slot para a hatch

!!! tip "Dica"
    Use Thread Hatches combinados com Accelerate Hatches para velocidade insana em receitas diversas!
