# 🔋 PRD #5 — Nexus Flux Matrix (Wireless Energy System)
## GregTech Nexus Addon — Sistema de Energia Wireless
### Versão 1.0 | Bilíngue PT-BR / EN

---

## 1. Conceito Central / Core Concept

**Nome da Estrutura**: **Nexus Flux Matrix**  
**Função**: Armazenamento massivo de energia EU e servidor central de distribuição wireless. Funciona como um "banco central" onde geradores depositam e máquinas sacam energia, sem cabos.

**Referências de Implementação**:
- [GTMThings](https://github.com/liansishen/gtmthings) — `WirelessEnergyHatchPartMachine.java`, `WirelessEnergyContainer.java`
- [PhoenixCore](https://github.com/P-H-O-E-N-I-X-PackForge/PhoenixCore)
- [GTOCore](https://github.com/GregTech-Odyssey/GTOCore)

---

## 2. Mecânica do Multibloco / Multiblock Mechanics

### 2.1 Dimensões

| Parâmetro | Mín | Máx | Detalhes |
|-----------|-----|-----|----------|
| **Largura (W)** | 3 | 31 | Eixo X |
| **Altura (H)** | 7 | 7 | Fixo |
| **Profundidade (D)** | 7 | 7 | Fixo |
| **Capacitors Internos** | 1 | 750 | Bloco por bloco no interior |
| **Formato** | Oco | — | Casco de casing, interior preenchido com capacitors |

### 2.2 Nexus Capacitor Blocks (Blocos Internos)

Blocos que definem capacidade e tier da rede. Valores por unidade:

| Tier | Nome | Capacidade Unitária (EU) | Notação |
|------|------|:---:|---------|
| LV | Nexus Capacitor (Basic) | 160,000 | 160K |
| MV | Nexus Capacitor (Advanced) | 1,500,000 | 1.5M |
| HV | Nexus Capacitor (Elite) | 10,000,000 | 10M |
| EV | Nexus Capacitor (Master) | 50,000,000 | 50M |
| IV | Nexus Capacitor (Ultimate) | 250,000,000 | 250M |
| LuV | Nexus Capacitor (Superior) | 1,500,000,000 | 1.5G |
| ZPM | Nexus Capacitor (Quantum) | 15,000,000,000 | 15G |
| UV | Nexus Capacitor (Stellar) | 150,000,000,000 | 150G |
| UHV | Nexus Capacitor (Cosmic) | 3,000,000,000,000 | 3T |
| UEV | Nexus Capacitor (Infinite) | 50,000,000,000,000 | 50T |
| UIV | Nexus Capacitor (Ultra) | 900,000,000,000,000 | 900T |
| UXV | Nexus Capacitor (Extreme) | 15,000,000,000,000,000 | 15P |
| OpV | Nexus Capacitor (Omniscient) | 250,000,000,000,000,000 | 250P |
| MAX | Nexus Capacitor (Omni) | 5,000,000,000,000,000,000 | 5E |

### 2.3 Cálculo de Capacidade Total (`Int128`)

A capacidade escala **quadraticamente** para incentivar estruturas cheias:

```
Total Capacity = Σ(Unit Capacity) × Block Count / 2
```

**Exemplo**: 750 blocos MAX:
```
= (5,000,000,000,000,000,000 × 750) × 750 / 2
= 1,406,250,000,000,000,000,000,000 EU
≈ 1.4 Yotta EU
```

> ⚠️ Requer a classe `Int128` do GTNA para evitar overflow (valores excedem `long` MAX de 9.2 quintilhões).

### 2.4 Tier Médio

O **Tier Médio** da rede é calculado pela média dos tiers de todos os capacitors instalados (arredondado para baixo). Este valor define:
- Eficiência (perda de energia)
- Limite de transferência
- Cross-dimension support (requer ZPM+)

---

## 3. Balanceamento e Limitações / Balancing

### 3.1 Eficiência (Perda de Energia)

A perda ocorre na **extração** (Wireless Energy Hatch sacando da rede):

| Tier Médio | Perda | Eficiência |
|:---:|:---:|:---:|
| LV | 15% | 85% |
| MV | ~13.9% | 86.1% |
| HV | ~12.8% | 87.2% |
| EV | ~11.7% | 88.3% |
| IV | ~10.6% | 89.4% |
| LuV | ~9.4% | 90.6% |
| ZPM | ~8.3% | 91.7% |
| UV | ~7.2% | 92.8% |
| UHV | ~6.1% | 93.9% |
| UEV | ~5% | 95% |
| UIV | ~3.9% | 96.1% |
| UXV | ~2.8% | 97.2% |
| OpV | ~1.7% | 98.3% |
| MAX | **0%** | **100%** |

**Fórmula**: `loss% = 15 × (1 - tierIndex / maxTierIndex)`

**Incentivo**: Misturar blocos de tier baixo com tier alto PIORA a média → jogador é incentivado a trocar TODOS para o mesmo tier alto.

### 3.2 Limite de Transferência (Throughput)

Limite máximo de EU/t que a estrutura pode processar (input + output somados):

| Tier Médio | Transfer Limit (EU/t) |
|:---:|:---:|
| LV | ~2K |
| MV | ~8K |
| HV | ~32K |
| EV | ~128K |
| IV | ~512K |
| LuV | ~2M |
| ZPM | ~8M |
| UV | ~32M |
| UHV | ~128M |
| UEV | ~512M |
| UIV | ~2G |
| UXV | ~8G |
| OpV | ~32G |
| MAX | **500 Z EU/t** (5×10²³) |

**Curva**: Exponencial com base 4 + scaling factor no topo.

---

## 4. Componentes de Rede / Network Components

Todos vinculados via item **Nexus Linker** (Shift+Click no Controller → Click no componente).

### 4.1 Para Multiblocos

#### Wireless Energy Hatch (Output → Alimenta Multiblocos)

Consome da rede wireless → injeta no energy container do multiblocko.

| Amperagem | Disponível |
|:---------:|:---:|
| 1A | ✅ |
| 4A | ✅ |
| 16A | ✅ |
| 64A | ✅ |
| 256A | ✅ |
| 1,024A | ✅ |
| 4,096A | ✅ |
| 16,384A | ✅ |
| 65,536A | ✅ |
| 262,144A | ✅ |
| 1,048,576A | ✅ |

Cada hatch existe para **todos os tiers** (LV→MAX).

**Tooltip**: Mostra coordenadas, dimensão, dono e amperagem da rede vinculada.

**Implementação** (baseada no GTMThings):
```java
// Ciclo de tick:
// 1. Calcula espaço disponível no energy container local
// 2. Saca da rede wireless (removeEnergy)
// 3. Injeta no container local (setEnergyStored)
```

#### Wireless Dynamo Hatch (Input → Recebe de Multiblocos)

Recebe do energy container do multiblocko → deposita na rede wireless.

Mesmas amperagens e tiers do Wireless Energy Hatch.

### 4.2 Para Singleblocks (Covers)

#### Wireless Receiver Cover
- Alimenta máquinas singleblock puxando da rede
- **Amperagens**: 1A, 4A, 16A, 64A
- **Info no tooltip**: (Nome da Máquina) - (EU/t) [Coords]

#### Wireless Transmitter Cover
- Extrai de geradores singleblock e envia à rede
- **Amperagens**: 1A, 4A, 16A, 64A

---

## 5. Itens e Interface / Items & UI

### 5.1 Nexus Linker (Item)

**Função**: Vincula componentes à rede.
1. **Shift+Right Click** no Controller da Matrix → copia Network ID
2. **Right Click** no Hatch/Cover → vincula à rede (cola Network ID)
3. **Left Click** com Data Stick → desvincula

### 5.2 Quantum Network Terminal (GUI)

Item de mão que abre uma GUI com informações completas da rede:

```
╔══════════════════════════════════╗
║  [Nome: Editável]  Status: ✅ ON ║
║  Team: [FTB Teams]  Tier: UV     ║
║  Cross-Dim: ✅ (ZPM+)            ║
╠══════════════════════════════════╣
║  Energy: ███████████░░ 78%       ║
║  12.5T / 16T EU                  ║
║  Avg Input:  +2.5M EU/t  🟢     ║
║  Avg Output: -1.8M EU/t  🔴     ║
║  Time to Empty: 14:32:07         ║
╠══════════════════════════════════╣
║  📋 Connections (12):            ║
║  IN  16A ZPM Dynamo   +230K [0] ║
║  IN  64A UV  Dynamo   +2.1M [0] ║
║  OUT 4A  LuV W.Hatch  -180K [0] ║
║  OUT 1A  IV  W.Hatch  -32K  [-1]║
║  CVR Macerator (IV)   -128  [0] ║
╚══════════════════════════════════╝
```

---

## 6. Sistema de Segurança / Safety System

Executado via `serverTick` com flags anti-spam:

### 6.1 Alertas de Bateria

| Nível | Mensagem | Ação |
|:---:|----------|------|
| ≤75% | `⚠️ Energy Level: 75%` | Aviso no chat |
| ≤50% | `⚠️ Energy Level: 50%` | Aviso no chat |
| ≤25% | `⚠️ Energy Level: 25% (Approaching Safe Mode)` | Aviso urgente |
| ≤10% | `⛔ CRITICAL: Energy < 10%. Entering Safe Mode.` | **SAFE MODE ATIVADO** |

### 6.2 Safe Mode

- **Gatilho**: Energia abaixo de **10%**
- **Ação**: Corta **todo** output de energia (máquinas param)
- **Continua aceitando** input (geradores ainda carregam)
- **Retorno**: Sistema volta ao normal quando atingir **25%** de carga
- **Lógica**:
```java
if (safeModeActive) {
    if (percent >= 25) {
        safeModeActive = false;
        sendAlert("🔋 Power restored. Safe Mode deactivated.");
    }
    return; // Block all output
} else if (percent <= 10) {
    safeModeActive = true;
    sendAlert("⛔ CRITICAL: Safe Mode activated.");
}
```

---

## 7. Arquivos de Implementação / Implementation Files

### 7.1 Novos Arquivos Java

| Arquivo | Pacote | Descrição |
|---------|--------|-----------|
| `NexusFluxMatrixMachine.java` | `machine.multiblock.energy` | Controller principal |
| `NexusCapacitorBlock.java` | `common.block` | Blocos capacitores (14 tiers) |
| `NexusEnergyNetwork.java` | `api.energy` | Lógica da rede wireless (SavedData) |
| `WirelessEnergyHatchPartMachine.java` | `machine.multiblock.part` | Hatch energy wireless |
| `WirelessDynamoHatchPartMachine.java` | `machine.multiblock.part` | Dynamo hatch wireless |
| `NexusLinkerItem.java` | `common.item` | Item de vinculação |
| `QuantumNetworkTerminal.java` | `common.item` | GUI terminal de rede |
| `WirelessReceiverCover.java` | `common.cover` | Cover para singleblocks |
| `WirelessTransmitterCover.java` | `common.cover` | Cover para singleblocks |

### 7.2 Modificações

| Arquivo | Mudança |
|---------|---------|
| `GTNAMachines.java` | Registrar Nexus Flux Matrix + hatches |
| `GTNABlocks.java` | Registrar Nexus Capacitor Blocks |
| `GTNAItems.java` | Registrar Linker + Terminal |
| `GTNACovers.java` | Registrar Covers wireless |
| `Int128.java` | Verificar métodos necessários (multiply, divide, compareTo) |

### 7.3 Integração com GTMThings Pattern

Baseado no código do GTMThings, o `WirelessEnergyHatchPartMachine`:
1. Estende `TieredIOPartMachine`
2. Implementa `IWirelessEnergyContainerHolder`
3. Usa `NotifiableEnergyContainer` como bridge local
4. No `serverTick`:
   - **IO.IN (Dynamo)**: saca do container local → `container.addEnergy()`
   - **IO.OUT (Energy)**: puxa da rede `container.removeEnergy()` → injeta no local
5. Bind via Data Stick (Right Click = bind, Left Click = unbind)

---

## 8. Config

```yaml
# Nexus Flux Matrix Config
nexusFluxMatrix:
  maxCapacitorBlocks: 750
  baseLossPercent: 15.0
  maxTransferTierMAX: 500000000000000000000000  # 500 ZEU/t
  safeModeThreshold: 10
  safeModeRecovery: 25
  crossDimensionMinTier: 7  # ZPM
  alertCooldownTicks: 1200  # 1 minuto entre alertas iguais
```

---

## 9. Fases de Implementação

1. **Fase 1**: `NexusEnergyNetwork` (SavedData) + `NexusCapacitorBlock` (14 tiers)
2. **Fase 2**: `NexusFluxMatrixMachine` (controller com pattern dinâmico)
3. **Fase 3**: `WirelessEnergyHatch` + `WirelessDynamoHatch` (baseado em GTMThings)
4. **Fase 4**: `NexusLinkerItem` + bind/unbind logic
5. **Fase 5**: Safe Mode + alertas + config
6. **Fase 6**: Covers (Receiver/Transmitter)
7. **Fase 7**: Quantum Network Terminal (GUI)
8. **Fase 8**: Integração FTB Teams (opcional)

---

*PRD Nexus Flux Matrix — GTNA v0.2.0*
