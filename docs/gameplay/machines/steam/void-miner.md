# ⛏️ Void Miner (Steam Gate Aged)

> *"Extrai riquezas do vazio absoluto — alimentado por vapor supercrítico."*

## Stats Rápidos

| Propriedade | Valor |
|------------|-------|
| **Tipo** | Multiblocko a Vapor (Steam Multi) |
| **Tamanho** | Definido na estrutura (ver JEI) |
| **Receitas** | Conforme `VOID_MINER_RECIPES` (configurable) |
| **Paralelos** | Baseado em recipe modifier |
| **Velocidade** | Variável por tier de vapor (1x a 5x) |
| **Eficiência** | Custo de energia escala com tier de vapor |
| **Consumo** | Vapor (regular ou supercrítico) |

## Benefícios

- ✅ **Mineração sem ir à mina** — extrai recursos do vazio
- ✅ **3 tiers de boost** via fluidos supercríticos
- ✅ **Escalável** — output e velocidade aumentam com melhor fluido
- ✅ **Configurável** — todos os multiplicadores são configuráveis via YAML

## Tiers de Fluido

O Void Miner aceita 3 tipos de fluido especial que **amplificam** a produção:

### Tier 0: Vapor Normal
Funcionamento base sem bônus.

| Propriedade | Valor |
|------------|-------|
| Output Mult | 1x |
| Velocidade | 1x |
| Consumo EU | 1x |

### Tier 1: Dense Supercritical Steam

<span style="display:inline-block; width:16px; height:16px; background:#A0A0A0; border-radius:3px; vertical-align:middle;"></span> **Temperatura**: 295,000 K

| Propriedade | Valor Padrão | Config Key |
|------------|-------------|------------|
| Output Mult | **2x** | `voidMinerDenseOutputMult` |
| Velocidade | **2x** mais rápido | `voidMinerDenseSpeedMult` |
| Consumo EU | **1.5x** | `voidMinerDenseEnergyMult` |

### Tier 2: SuperHeated Steam

<span style="display:inline-block; width:16px; height:16px; background:#C0C0C0; border-radius:3px; vertical-align:middle;"></span> **Temperatura**: 600,000 K

| Propriedade | Valor Padrão | Config Key |
|------------|-------------|------------|
| Output Mult | **3x** | `voidMinerSuperHeatedOutputMult` |
| Velocidade | **3x** mais rápido | `voidMinerSuperHeatedSpeedMult` |
| Consumo EU | **2x** | `voidMinerSuperHeatedEnergyMult` |

### Tier 3: Insanely Supercritical Steam

<span style="display:inline-block; width:16px; height:16px; background:#FFFFFF; border:1px solid #888; border-radius:3px; vertical-align:middle;"></span> **Temperatura**: 1,000,000 K

| Propriedade | Valor Padrão | Config Key |
|------------|-------------|------------|
| Output Mult | **5x** | `voidMinerInsanelyOutputMult` |
| Velocidade | **5x** mais rápido | `voidMinerInsanelySpeedMult` |
| Consumo EU | **4x** | `voidMinerInsanelyEnergyMult` |

## Tabela Comparativa

| Fluido | Output | Speed | Energy Cost | R.O.I. |
|--------|:------:|:-----:|:-----------:|:------:|
| Steam Normal | 1x | 1x | 1x | Base |
| Dense Supercritical | **2x** | **2x** | 1.5x | ⭐⭐ |
| SuperHeated | **3x** | **3x** | 2x | ⭐⭐⭐ |
| Insanely Supercritical | **5x** | **5x** | 4x | ⭐⭐⭐⭐ |

!!! tip "Melhor Custo-Benefício"
    O **SuperHeated Steam** oferece o melhor custo-benefício: 3x output e 3x velocidade por apenas 2x custo de energia. O Insanely dá 5x output mas custa 4x energia.

## Mecânica Interna

O recipe modifier funciona assim:

1. Detecta o fluido de **maior tier** em qualquer Input Hatch
2. Aplica o multiplicador correspondente em output, duração e EU/t
3. Se nenhum fluido especial for encontrado, usa o modo base (1x)

```
Prioridade de detecção:
Insanely Supercritical > SuperHeated > Dense Supercritical > Base
```

## GUI

A GUI mostra em tempo real:
- **Status** de trabalho (working/idle)
- **Barra de progresso** da receita atual
- **Tier de vapor detectado** com multiplicadores ativos
- **Últimos outputs** produzidos

## Configuração

Todos os valores podem ser alterados no arquivo de config `gtna.yaml`:

```yaml
# Void Miner Steam Gate Aged
voidMinerDenseOutputMult: 2
voidMinerDenseSpeedMult: 2.0
voidMinerDenseEnergyMult: 1.5

voidMinerSuperHeatedOutputMult: 3
voidMinerSuperHeatedSpeedMult: 3.0
voidMinerSuperHeatedEnergyMult: 2.0

voidMinerInsanelyOutputMult: 5
voidMinerInsanelySpeedMult: 5.0
voidMinerInsanelyEnergyMult: 4.0
```

## Dicas

!!! tip "Obtendo Fluidos Supercríticos"
    Os fluidos supercríticos são produzidos pelo sistema de receitas do GTNA. Verifique o JEI para as rotas de produção.

!!! warning "Fluido como Boost"
    O fluido supercrítico deve estar **já presente** na Fluid Input Hatch quando a máquina inicia. Não é consumido durante a operação — ele atua como um **catalisador/boost permanente**.
