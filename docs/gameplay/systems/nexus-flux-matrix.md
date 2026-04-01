# ⚡ Nexus Flux Matrix — Wireless Energy System

> **Status**: <span class="status-badge status-planned">🔮 Em Desenvolvimento — v0.2.0</span>

## O que é

O **Nexus Flux Matrix** é um multiblocko de armazenamento massivo de energia EU com distribuição wireless. Funciona como um "banco central" de energia onde geradores depositam e máquinas sacam sem cabos.

## Stats Rápidos

| Propriedade | Valor |
|------------|-------|
| **Tipo** | Multiblocko Elétrico (expansível) |
| **Tamanho** | 3×7×7 (mín) até 31×7×7 (máx) |
| **Capacitors Internos** | Até 750 blocos |
| **Escalabilidade** | Quadrática — `Capacity × Count / 2` |
| **Eficiência** | 85% (LV) → 100% (MAX) |
| **Transfer Limit (MAX)** | 500 ZEU/t |
| **Cross-Dimension** | Sim (ZPM+) |
| **Safe Mode** | Auto em <10%, reativa em 25% |

## Componentes

### Nexus Capacitor Blocks

Preenche o interior do multibloco. Cada bloco adiciona capacidade:

| Tier | Capacidade/Bloco | Nome |
|------|:---:|------|
| LV | 160K EU | Basic |
| MV | 1.5M EU | Advanced |
| HV | 10M EU | Elite |
| EV | 50M EU | Master |
| IV | 250M EU | Ultimate |
| LuV | 1.5G EU | Superior |
| ZPM | 15G EU | Quantum |
| UV | 150G EU | Stellar |
| UHV | 3T EU | Cosmic |
| UEV | 50T EU | Infinite |
| UIV | 900T EU | Ultra |
| UXV | 15P EU | Extreme |
| OpV | 250P EU | Omniscient |
| MAX | 5E EU | Omni |

### Wireless Energy Hatch

Alimenta multiblocos sacando da rede wireless. 11 variantes de amperagem (1A→1048576A).

### Wireless Dynamo Hatch

Recebe de geradores e deposita na rede wireless.

### Wireless Covers (Singleblocks)

- **Receiver Cover**: Alimenta singleblocks (1A, 4A, 16A, 64A)
- **Transmitter Cover**: Extrai de geradores singleblock

### Nexus Linker (Item)

Vincula componentes à rede via Shift+Click no Controller → Click no Hatch.

### Quantum Network Terminal (GUI)

Monitor portátil com energia atual, taxa de I/O, lista de conexões e tempo restante.

## Sistema de Segurança

| Nível | Ação |
|:---:|------|
| ≤75% | ⚠️ Aviso no chat |
| ≤50% | ⚠️ Aviso no chat |
| ≤25% | ⚠️ Aviso urgente |
| ≤10% | ⛔ **Safe Mode**: corta output, continua aceitando input |
| ≥25% | 🔋 Safe Mode desativado, output restaurado |

## PRD Completo

Para detalhes técnicos de implementação, veja: [PRD #5 — Nexus Flux Matrix](../../prd/prd_05_nexus_flux_matrix.md)
