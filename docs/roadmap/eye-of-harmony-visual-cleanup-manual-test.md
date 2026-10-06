# Eye of Harmony — frente, renderer, tooltip e blocos antigos

- O corpo/appearance do controlador usa Reinforced Spatial Structure Casing. Confira a frente do controlador com a textura GTNH/Modernity já importada, nos estados
  ocioso, executando, pausado e aguardando entrega. Não deve usar a frente do Fluid Drilling Rig.
- Forme o Eye e execute Overworld. Confira estrela central e um planeta Overworld em órbita;
  não devem aparecer Nether e End num programa Overworld. Confira centro nas quatro direções.
- Desabilite durante a operação: animação deve congelar. Reformação/relog deve conservar o ciclo.
  Confira de longe, atravessando a área da estrela, e com outros multiblocos por perto.
- Passe o mouse no item EOH: detalhes aparecem sem Shift. Pressione/solte Z para avançar.
  Confira legibilidade com diferentes alturas/escalas e idioma PT-BR/inglês.
- Procure os antigos `Dimensional Bridge Casing`, `Dimensional Stability Casing` e
  `Spacetime Compression Field Generator` sem tier: não devem existir como blocos separados.
  Os 27 campos tierados e casings atuais permanecem.
- Saves antigos: Bridge é remapeado para Spatial Casing; Stability para Stabilisation tier 1;
  Compression sem tier para Compression tier 1. Os dois aliases sem tier não possuem uma
  graduação GTNH confiável, portanto a migração usa o primeiro tier, sem conceder upgrade.
  Blocos/itens em mundo/inventário devem migrar sem virar ar. Conferência real de save antigo pendente.
- Receitas antigas dos três aliases e de fabricação do controlador EOH foram retiradas.
  Não foram adicionadas receitas de aquisição dos novos componentes.

Renderer corrigido em cima dos modelos já existentes do port; não se afirma equivalência
completa aos efeitos/renderização GTNH. A revisão artística ainda depende da conferência do autor.

## Correção dos casings (G-0158)

Conferir também no preview JEI e nos blocos colocados: Boundary claro
(`EM_POWER_INFINITE`), Spatial externo azul (`EM_OUTER_SPACETIME_REINFORCED_EOH_CASING`)
e Temporal interno azul (`EM_INNER_SPACETIME_REINFORCED_EOH_CASING`). O import inicial
usava incorretamente imagens de outros blocos TecTech. A referência correta é
`BlockGTCasingsBA0`, metadatas 12/11/10, na revisão GTNH fixada no manifesto.
Os IDs e posições estruturais permanecem; somente as imagens foram corrigidas.

## Portas ME (G-0159)

Por decisão do autor, as cinco portas são exclusivamente ME: 1 bus de entrada,
2 hatches de entrada de fluidos, 1 bus de saída e 1 hatch de saída de fluidos.
Entradas ME finitas são aceitas; stocking/crafting/duais/energia/normais recusados.
Substituir uma porta ME por sua correspondente normal deve invalidar a estrutura.
Preview e Nexus Terminal devem oferecer/construir somente as portas ME aceitas.
Saídas usam as filas ME persistentes, inclusive quando a rede está desconectada;
conectar a rede deve entregar os produtos uma vez. Entradas precisam ser configuradas
com H/He e circuito no bus ME. Validar também pausa/relog durante a entrega.
