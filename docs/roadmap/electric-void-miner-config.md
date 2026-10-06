# Void Miner elétrico EV — configuração

> Atualização: agora há modos Preciso/Aleatório e uma cadeia de essências. O funcionamento
> atual está em [void-miner-essence-chain.md](void-miner-essence-chain.md). Os exemplos
> antigos por seletor abaixo continuam válidos para packs, mas os programas nativos de
> mineração agora consomem essências; chips planetários são produzidos pelo scanner.


O `gtna:electric_void_miner` é a máquina nova prevista pelas decisões GTIA-D-159 a GTIA-D-162:
entrada em EV, um programa por máquina, energia + Drilling Fluid por operação e paralelismo
apenas a partir de IV. O `gtna:void_miner_steam_gate_aged` continua independente e intacto.

## Arquivo

`config/gtna/balance/electric_void_miner.json` (exemplo GTIA em
`docs/config-examples/gtia/electric_void_miner.json`). A configuração é lida na inicialização;
reinicie o jogo/servidor após editar. O campo `enabled: false` **não registra** o bloco — use
apenas antes de distribuir o mundo, já que blocos colocados deixariam de existir.

| Campo | Padrão | Efeito |
| --- | --- | --- |
| `enabled` | `true` | Registra (ou não) o controller. |
| `minimumTier` | `EV` | Menor tier que pode operar; abaixo disso `isProgramAllowed` recusa. |
| `programRequired` | `true` | No preciso, exige essência consumível ou seletor legado não consumível. Aleatório e fallback por id são isentos. |
| `baseDuration` | `2400` | Duração das 40 receitas de veia e do fallback (ticks). |
| `baseEUt` | `2048` | EU/t do aleatório terrestre EV e do fallback. |
| `defaultDrillingFluidPerOperation` | `1000` | Drilling Fluid (mB) das receitas de veia e do fallback. |
| `maxDrillingFluidPerOperation` | `100000` | Teto de fluido por operação de qualquer programa. |
| `maxOutputStacksPerOperation` | `16` | Teto de saídas do modo preciso. |
| `maxRandomOutputStacksPerOperation` | `216` | Teto de saídas potenciais do modo aleatório. |
| `randomDrillingFluidPerOperation` | `10000` | Fluido da receita aleatória nativa (mB). |
| `randomDuration` | `4800` | Duração da receita aleatória nativa (ticks). |
| `parallelEnabledFromTier` | `IV` | Tier a partir do qual o Parallel Control Hatch conta. |
| `maxParallelByTier` | EV 1; IV e tiers superiores 2 | Teto configurável por tier. |
| `allowFixedFallbackRecipe` | `false` | Registra `gtna:electric_void_mining_fallback` (sem seletor) para packs que removem os programas. |

## Programas

Programas são receitas de `gtna:electric_void_mining`:

```java
GTNARecipeType.ELECTRIC_VOID_MINING_RECIPES.recipeBuilder("meu_programa")
        .notConsumable(MEU_ITEM_DE_PROGRAMA)   // amostra / Planet Data Chip; não é consumido
        .inputFluids(GTMaterials.DrillingFluid.getFluid(2000))
        .outputItems(/* Raw Ores */)
        .duration(1200)
        .EUt(GTValues.VA[GTValues.EV])
        .save(provider);
```

O GTNA embarca 40 programas de veias do GTL em LuV/ZPM e três programas terrestres EV, cada um consumindo sua essência. O modo
aleatório tem recipe type separado `gtna:random_void_mining` e dispensa seletor/essência;
ainda exige Drilling Fluid positivo e respeita os tetos de fluido/saída e paralelo.
Os dados planetários são reutilizados na Incubator; os cinco programas do Ad Astra consomem
as essências planetárias. Veja a cadeia e as adaptações numéricas no documento vinculado acima.

## Paralelismo e overclock

Em EV o teto é 1 (sem paralelo). A partir de `parallelEnabledFromTier`, o Parallel Control Hatch
conta, limitado por `maxParallelByTier` e pelos insumos/energia disponíveis. O padrão é 2 em IV
e em todos os tiers superiores, podendo ser ajustado separadamente por tier. O overclock elétrico
é o padrão do GTCEu (não perfeito), aplicado depois da validação. A tela da máquina informa o
programa ativo, o custo efetivo em Drilling Fluid e EU/t, o paralelo e as saídas; JEI/EMI mostra
os ingredientes do programa, incluindo chip e amostra.

## Referências

- GTIA-D-159 (máquina nova separada da Steam), GTIA-D-160 (energia + Drilling Fluid), GTIA-D-161
  (controller EV com Titanium terrestre, sem Lua/amostra/dado), GTIA-D-162 (um programa, overclock
  elétrico, paralelo a partir de IV).
- GTIA-D-172: não adicionar saída de fluidos descobertos ao minerador por inferência; a máquina de
  automação de fluidos ainda não foi escolhida.
