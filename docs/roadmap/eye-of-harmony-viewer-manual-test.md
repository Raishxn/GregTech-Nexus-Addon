# Eye of Harmony — EMI/JEI e tooltip

Referência visual: capturas GTNH fornecidas pelo autor em 04/10/2026. O viewer moderno
mostra o catálogo Overworld executado pelo controlador, não os números de uma captura de outro pack.
Não foram adicionadas receitas de fabricação.

## Conferência no cliente

1. Entre em um mundo e procure Eye of Harmony no EMI/JEI. Abra seus usos (`U`) e a categoria
   **Eye of Harmony**. O controlador é catalisador; o Planet Block Overworld aparece no topo.
2. Confira a grade de nove colunas, até 99 produtos por página. No JEI, a grade usa 4–11 linhas conforme a altura inicial. No EMI, painel vertical de 198 pixels de GUI mostra planeta/gases no topo, grades separadas de itens (9 × até 9) e fluidos (9 × até 2) abaixo e dados após a grade; a grade se adapta à altura real do painel; as setas inferiores avançam/voltam entre os produtos. Navegue até a última página e retorne:
   são partes do mesmo programa. Poeira de pedra e os fluidos finais devem estar presentes.
3. Passe o mouse nos produtos. A quantidade exata deve aparecer na tooltip; o pequeno texto
   no ícone é abreviado (`k`, `M`, `G`); fluidos mostram baldes (`B`, `kB`, `MB`). As quantidades são bases anteriores ao rendimento dos campos.
   Abra a receita (`R`) de uma poeira presente para conferir sua indexação entre os produtos.
4. Confira tempo base de **360.000 ticks**, hidrogênio/hélio de **1.000.000.000 mB cada**,
   **Spacetime Tier: Crude**, entrada/saída de EU com prefixos k/M/G/T/P/E, chance base
   de 100% e **Recipe Energy Efficiency: 60%**. A eficiência é a razão de retorno/entrada
   do programa; não está fixada em 60% para futuros programas.
   Os textos de informação devem ser pretos sobre o fundo cinza; somente o aviso é vermelho.
   Esses valores não são a previsão de uma máquina com campos/circuito particulares.
5. O viewer antigo Cosmos Simulation deve estar oculto no EMI/JEI. Os dados legados permanecem
   carregados para terminar ciclos antigos já pagos, conforme o teste de operação.
6. Na tooltip do controlador, pressione/solte **Z** para avançar.
   Confira campos, energia, circuito, excesso, falha, pity, portas e recursos ainda ausentes.
   A página adapta sua quantidade de linhas à altura da tela. Confira PT-BR e inglês,
   títulos azuis, números/penalidades vermelhos e fórmulas verdes.
7. Repita com outra escala de GUI e após sair/reentrar no mundo. Confira ícones de fluidos,
   sobreposição de quantidades e legibilidade das linhas de EU. Faça também `/reload` e
   confira se o viewer reconstrói o programa. A invalidação do cache do controlador sem unload
   continua uma pendência separada; não considerar esse teste aprovação da operação após reload.

## Limites desta etapa

- Somente Overworld; Nether/End e planetas externos ainda aguardam implementação.
- Integração própria JEI e EMI (EMI 1.1.13 Forge API, opcional); REI continua pendente.
  Dev com EMI: `./gradlew runClient --offline -PeohEmiQA`.
- Sem Astral Arrays/paralelos, fabricação, inserção por clique no controlador ou controles novos de animação.
- EMI tem uma única página por programa. Todos os produtos continuam indexados; a grade
  exibe até 81 itens + 18 fluidos, ou menos conforme espaço disponível. Aviso vermelho
  informa a contagem visível/total; saídas omitidas do desenho continuam na simulação.
- QA visual depende da conferência no cliente; os testes de servidor cobrem completude das
  páginas, identidade e quantidades longas, sem validar o desenho na tela.

## Tempo normal e teste de circuitos (G-0166)

- A aceleração de QA 600× foi removida a pedido do autor, inclusive selo, propriedade
  e flag Gradle. Usar `./gradlew runClient --offline -PeohEmiQA` para viewer.
- Circuitos 0–24: o débito total inicial é base × (circuito+1)² e cada nível divide
  o tempo por dois, com mínimo de 1 tick. Não aumenta produtos/retorno de energia. Base:
  circuito 0 = 360.000 ticks (5 h); 4 = 22.500 ticks (18 min 45 s);
  8 = 1.406 ticks (~70,3 s); 12 = 87 ticks (~4,35 s).
- Compressão: cada tier adicional aplica desconto multiplicativo de 3% no tempo.
  Aceleração: cada tier adicional divide o tempo por dois. Estabilização altera
  chance/rendimento/retorno de EU, sem reduzir o tempo.
- O plano de um ciclo pago fica congelado. Trocas de circuito ou campos durante
  um ciclo só afetam o próximo. UI mostra tempo restante/progresso do ciclo pago.

## Formato vertical solicitado em G-0164

- A referência GTNH orienta o formato, sem copiar seus números de outro pack.
  Planeta central no topo, grade completa na largura, dados abaixo em preto e aviso vermelho.
- O EMI limita a janela por `ui.maximum-recipe-screen-height` (padrão 256).
  O aumento global para 1024 foi desfeito em G-0165. Somente a categoria EOH ganha
  altura extra por um hook opcional; preview e outras categorias respeitam a configuração normal. Escalas maiores reduzem a quantidade de slots visíveis, mantendo uma única página.
- A observação de valores base antes de campos/circuito/excesso/perda Nexus está na tooltip
  dos produtos para não cortar o rodapé. Quantidades exatas permanecem na mesma tooltip.

## Correções de QA G-0165

- Conferir grade máxima de 81 itens + 18 fluidos com intervalo entre as regiões;
  telas menores preservam as duas famílias. Desde G-0167 há uma única página, sem
  setas internas; nenhuma saída da simulação é descartada por não caber no desenho.
- Aviso vermelho sempre visível: quando parcial, indica quantos produtos daquela página
  pertencem ao catálogo completo; quando completo, explica que são quantidades base
  alteradas por campos/excesso. Não promete produtos ocultos que a implementação não gera.
- Quantidade desenhada depois do ícone. Conferir minérios claros/escuros, fluidos,
  tooltip para detectar qualquer sobreposição residual.
- Plasmas: **8MB = 8.000.000 baldes = 8.000.000.000 mB**. Correção real do catálogo
  solicitada pelo autor; energia do programa mantém o cálculo anterior. Raw Star Matter
  e White Dwarf Matter conservam suas quantidades anteriores; todos são exibidos em baldes.
  Tooltips incluem ambos os valores exatos. Hidrogênio/hélio: 1.000.000 B de entrada cada.
- Ciclos novos usam a quantidade corrigida. Ciclos já pagos preservam o plano/produtos
  congelados; não alterar uma entrega em andamento para conceder quantidades extras.
- Alternar EOH → Multiblock Info → outra receita → EOH na mesma janela; só EOH deve
  ficar alto. Repetir depois de redimensionar janela e mudar escala/idioma. O hook
  respeita limites físicos da tela; escalas altas exibem menos produtos, com contagem correta no aviso.

## Gases e energia na UI (G-0166)

- Gases dos ME Input Hatches são absorvidos e consumidos integralmente no início.
  UI distingue buffer para o próximo ciclo dos baldes consumidos no ciclo atual.
  Ambos os gases consumidos e o excesso do ciclo persistem após relog; ciclos antigos
  sem snapshot mostram a informação como indisponível, sem inventar quantidade.
- Quantum Network Terminal e Nexus Flux Matrix exibem a última retirada direta com
  máquina, EU e tooltip de valor exato/posição/dimensão/tick. Débito EOH é inicial,
  único, sem hatch de energia; essa transação não deve ser apresentada como consumo
  contínuo em EU/t. A linha persiste depois que o pico instantâneo acaba e após relog.

## QA do rebalanceamento G-0167

- EMI deve indicar Page 1 of 1, sem setas internas; grade 9×9 itens + 9×2 fluidos
  quando a tela permite. Warning informa saídas visíveis/total. Testar busca EMI por
  uma saída além dos primeiros 81 itens: programa ainda está indexado para ela.
- Circuitos: custos relativos 0=1×, 1=4×, 4=25×, 8=81×, 12=169×, 24=625×.
  Tempo conserva redução por dois e piso de um tick; produtos e crédito não aumentam.
  UI/tooltip manual devem indicar fórmula quadrática. Ciclos pagos antes do ajuste
  conservam o débito/crédito/produtos congelados, inclusive após relog.
- Artificial Star: quatro combustíveis geram 16× EU/t, duração base 200 ticks,
  consumo/chance do Annihilation Constrainer preservados. Usar dynamos com capacidade
  suficiente para a receita; overclock segue limitado pelos dynamos instalados.
- Infinity base: 36.028.797.018.963.968 EU/t; 7.205.759.403.792.793.600 EU por barra.
  Quatro barras a 95% cobrem circuito 24 do Overworld. Reservar capacidade/saldo na
  rede acima do débito antes de iniciar; isso não assegura ciclos contínuos de 1 tick.

Artificial Star aceita agora até 16 dynamos de energia (preview padrão continua com
um). Infinity na velocidade base requer capacidade conjunta de 36.028.797.018.963.968
EU/t: 16 Wireless Dynamos MAX de 1.048.576A alcançam esse valor usando hatches já
registrados. Com saída menor, receitas de geração superiores não cabem; usar combustível
inferior até ampliar a saída. Não foram criados hatches/tensões novos.
