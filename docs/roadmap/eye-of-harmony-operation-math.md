# Eye of Harmony — base de cálculo serial

Referência: GT5-Unofficial `a3e1e11241a814c9fa0dd0973d5699548428f689`.
Implementação: `api/machine/feature/eyeofharmony/EyeOfHarmonyMath.java`.
Esta etapa não cria receitas de fabricação. Os cálculos estão conectados ao controlador para
o Overworld. Ciclos Cosmos já pagos podem terminar uma vez; novas receitas Cosmos não iniciam.
EOH-04 tem validação automática; conferência da interface e operação no cliente pendente.

## Regras verificadas no código

Tiers internos `c/a/s` variam de 0 a 8. Circuito `k` é limitado a 0–24; ausência equivale a zero.
`r` é a compressão mínima do programa. `P` representa a soma das penalidades dos dois gases.

| Grandeza | Regra serial |
| --- | --- |
| Duração | `max(int(T × 2^-a × 0.97^(c-r) × 2^-k), 1)` ticks |
| Débito | `startupEU × (k+1)²`, calculado como BigInteger (rebalanceamento GTNA G-0167) |
| Crédito bruto | `long(returnEU × (1 - (8-s) × 0.05))` |
| Excesso de cada gás | `1 - exp(-(30 × (stored/required - 1))²)` |
| Chance antes de pity/excesso | `baseChance - a × 0.0925 + s × 0.05` |
| Chance final | aplicar pity; subtrair `P`; limitar a 0–1 |
| Rendimento | limitar `1 - s × 0.05 - P` a 0–1 |
| Produtos de sucesso | `long(baseAmount × yield)` |
| SpaceTime em falha | `long(chance × 14400 × 2^(rocketTier+1))` |

O crédito ocorre em sucesso **e falha**. Ele não recebe o multiplicador de circuito e não depende
do rendimento. O Nexus pode aplicar sua política de perda na transferência; essa perda não faz
parte da fórmula GTNH e deve ser mostrada separadamente na integração.

Ao iniciar uma operação serial, o original esvazia **todo** o buffer interno de hidrogênio e hélio,
inclusive o excedente. O plano guarda as quantidades totais; calcular ou consultar esse plano
não altera inventários, gases, rede energética, histórico ou gerador aleatório.

O sorteio serial usa um inteiro de 0 a 9999 e sucesso quando `roll < int(10000 × chance)`.
O resultado recebe um sorteio explícito: a integração deve sortear uma vez e persistir o resultado.

### Pity: conservar o comportamento do código

- Histórico inicial: chance anterior e pity iguais a zero.
- Sucesso grava `Double.MIN_VALUE`; o próximo início substitui esse sentinel pela chance-base
  calculada com os campos, antes de clamp/excesso. Em programas avançados esse valor pode ser negativo.
- A garantia exige igualdade exata entre a chance-base nova e a chance efetiva anterior,
  além de pity pelo menos 1. Mesmo com garantia, o excesso ainda reduz a chance.
- Falha com chance efetiva igual à anterior soma `(1-chance) × chance` ao pity;
  caso contrário, pity passa a ser a chance efetiva nova.

Não substituir isso por uma garantia genérica após N falhas: mudaria a referência.

## Programa Overworld

- 18.000 segundos / 360.000 ticks; rocket tier 0, compressão mínima 0.
- 1.000.000.000 mB de cada gás e chance-base 100%.
- Custo-base = custo dos plasmas + `360000 × (524288 + 100000000000)` EU.
- Retorno-base = 60% do custo-base, com conversão double → long igual à referência.
- Sem custo de plasmas, a parcela conhecida é 36.000.188.743.680.000 EU.
  Esse valor **não é o custo final** de uma operação com plasmas.
- Em campos 0/0/0, o retorno sofre mais 40% de redução; circuito zero é válido.
- Custo máximo com circuito 24 excede long e permanece dentro de Int128 para o programa Overworld.

O custo de plasmas é um parâmetro obrigatório: ele depende das saídas reais e do valor energético
do plasma como combustível. `plasmaEnergyMap` primeiro converte `fuelEUPerLiter × 3.85` em long;
`plasmaCostCalculator` soma esse valor vezes a quantidade de cada plasma e depois aplica **3.85
novamente**, convertendo para long. Preservar os dois fatores e os dois truncamentos conforme
o código fixado; não substituir pelo custo da receita de fusão.
Não há programa executável de produção com um catálogo fictício ou custo zero implícito.

## Catálogo moderno implementado

`EyeOfHarmonyOverworld` lê `GTRegistries.ORE_VEINS`: somente veios que declaram Overworld,
com peso e densidade positivos; suas entradas materiais usam `getValidMaterialsChances()`.
O peso de cada veio é distribuído pelas chances positivas de suas entradas e depois normalizado.
Materiais sem OreProperty não entram nessa distribuição. Biomas não restringem o catálogo de
um planeta inteiro. Blocos sem material GT reconhecido são ignorados.

O GTNH distribui duas entradas completas e duas de 1/8 por veio, usando o fluxo VM3
de `18000 × 384`. O catálogo moderno tem entradas arbitrárias; conserva as proporções modernas
e a soma de **2.25 fluxos**. É uma adaptação declarada: não uma reprodução da lista de minérios
do GTNH, nem simulação física de volumes/alturas de cada veio. GTCEu não tem aqui os wrappers
separados de small ores, Werkstoff e GT++; não é criada uma segunda distribuição fictícia.

Processamento usa `OreProperty` para multiplicador, smelt result, três subprodutos/fallbacks,
separação eletromagnética e lavagem. Multiplicadores GTNH 2, `0.1+1/9`, `1/9`, `0.1`, `0.7`
e `0.4/4+0.2/9` foram conservados, inclusive a contribuição de lavagem repetida quando não há
subprodutos. Há diferenças explícitas: lavagem moderna declarada usa 70%; o marcador legado
de mercúrio 99% não existe nessa API e não é inferido. Separação usa os materiais modernos
de `getSeparatedInto()`. MaterialInto/mMacerateInto legados não têm equivalência completa;
o fallback é o próprio material, e a saída é seu direct smelt result quando declarado.

Quantidades são acumuladas por material, ordenadas por nome e arredondadas para baixo.
Somente pós realmente registrados entram nas saídas de itens; Stone Dust adicional é três
vezes sua soma. Plasmas são a interseção de materiais processados com a whitelist GTNH e
os plasmas disponíveis no GTCEu: He, Fe, N, Ni, Am, O e Sn. Cada elegível recebe 8 milhões mB;
Argon moderno não é adicionado por não estar na whitelist GTNH. Não se inventam os outros
plasmas GTNH ausentes da API moderna. RawStarMatter 100.000 mB e WhiteDwarfMatter 1.152 mB
completam as saídas; todos sofrem rendimento em sucesso.

O valor EU/litro vem da receita **carregada** de `PLASMA_GENERATOR_FUELS`:
`outputEUt × duration / inputAmount`. Sem combustível para um plasma elegível, a operação
é bloqueada antes de cobrar. Os dois fatores 3.85/truncamentos GTNH permanecem.
O catálogo é reconstruído após carregar o controlador. Alterações por `/reload` sem recarregar
o controlador ainda não invalidam esse cache; integração de reload/viewer fica para EOH-05.

## Transação e recuperação implementadas

- Slot de planeta persistido e reutilizável; somente Overworld inicia. Nether/End registrados
  continuam seletores futuros. Circuito virtual GTCEu tem prioridade, seguido do primeiro circuito
  no input bus; sem circuito, zero. Circuito acima de 24 é limitado a 24.
- A cada 20 ticks, formado/habilitado/ocioso, absorve todo H₂/He disponível nos dois hatches
  para buffers long, limitando a soma sem overflow. Demais líquidos não são drenados nesta fase.
- Consultar modifier retorna NULL e nunca cobra. Não há nova busca Cosmos. O custo é debitado
  uma única vez pelo caminho de início; só após sucesso do débito, buffers são zerados.
- Duração, custo, retorno, chance, rendimento, dono, resultado e **quantidades finais de saída**
  ficam congelados. Resultado é sorteado uma vez ao iniciar e salvo; não depende de RNG ao retomar.
  Alterar seletor/circuito/casings depois não modifica a operação paga.
- O histórico pity é resolvido/salvo junto do sorteio no início, em vez de no término GTNH.
  Como não pode iniciar outra operação antes de terminar toda a entrega, a próxima chance
  conserva a mesma sequência serial. A operação paga é recuperável mesmo com desmontagem.
- Desabilitar ou invalidar a estrutura pausa; reformar/habilitar retoma. Isso adapta o abort
  destrutivo original para conservar o ciclo pago. Troca de dono via Data Stick é bloqueada
  até concluir o ciclo e todas as entregas pendentes. Dono de débito/crédito permanece congelado.
- EU de retorno é entregue pelo Nexus em quantidades parciais; seu retorno é **bruto aceito**,
  descontado do restante salvo. Perda da rede não é debitada novamente no restante.
- Produtos também permanecem pendentes. Máximo de 64 tentativas de pilha e um lote de líquido
  de até 1 milhão mB por tick. Cursores persistidos circulam entre entradas para que produtos
  bloqueados não impeçam outros compatíveis. Saída cheia não perde recursos nem inicia outro ciclo.
- Estado customizado versionado é salvo tanto no bloco quanto no item do controlador ao quebrar.
  As anotações persistem os campos e o slot; `EyeOfHarmonyOperation` conserva recuperação também
  em saves de drop. Não foi introduzida atomicidade contra uma falha do processo entre a gravação
  dos dois arquivos de mundo; a garantia testada é save/load normal.
- Receita Cosmos já ativa usa o caminho GTCEu de término uma vez, limpa o lastRecipe e não
  repete automaticamente. A política de saída dessa receita antiga continua a do GTCEu.

UI mostra seletor, tiers, circuito, gases/minimum/excesso, prévia após completar mínimos,
chance/rendimento, duração, débito, retorno bruto/saldo antes da perda Nexus e estado de bloqueio.
Operação paga mostra progresso e crédito bruto restante. Viewer Cosmos/JEI e o renderer orbital
ainda são legados; substituições pertencem a EOH-05/07.

Oito GameTests verificam catálogo real, modifier/recursos sem débito, coleta/circuito/disable,
save/load completo do bloco, pausa/reformação e owner lock, saída cheia/crédito parcial/reload,
falha com retorno, término de legado sem repetir e cursores sem starvation.
Fixture de ciclo usa programa de 3 ticks e produtos pequenos para testar transações, enquanto
um teste independente verifica o catálogo real. A conferência visual, relog do cliente e
unload real de chunks permanecem no [roteiro manual](eye-of-harmony-operation-manual-test.md).

## Rebalanceamento GTNA G-0167

Débito exponencial GTNH `4^k` substituído por `(k+1)²` a pedido do autor para
viabilizar circuito 24. Tempo mantém redução por dois e piso de um tick. Produtos,
gases e crédito não aumentam com circuito. Planos já pagos conservam seus snapshots.
Artificial Star gera 16× EU/t por combustível, conservando duração base de 200 ticks,
consumo e chances; overclock ainda depende da capacidade real dos dynamos.
Infinity fornece 7.205.759.403.792.793.600 EU por barra antes de overclock/perdas;
EOH Overworld circuito 24 custa aproximadamente 22,6 quintilhões EU (625× base).
Quatro barras Infinity cobrem esse débito com eficiência Nexus de 95%; é energia
acumulada, não promessa de sustentar um ciclo de um tick continuamente.

Artificial Star aceita agora até 16 dynamos de energia (preview padrão continua com
um). Infinity na velocidade base requer capacidade conjunta de 36.028.797.018.963.968
EU/t: 16 Wireless Dynamos MAX de 1.048.576A alcançam esse valor usando hatches já
registrados. Com saída menor, receitas de geração superiores não cabem; usar combustível
inferior até ampliar a saída. Não foram criados hatches/tensões novos.
