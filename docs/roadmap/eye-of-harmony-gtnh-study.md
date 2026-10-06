# Eye of Harmony — estudo de fidelidade e proposta de adaptação

Data: 2026-10-04. **Estudo e proposta; nenhuma mudança de comportamento implementada.**

## Referências verificadas

A wiki indicada (`https://wiki.gtnewhorizons.com/wiki/Eye_of_Harmony`) respondeu HTTP 403,
assim como seus endpoints raw/API. Não foi possível confirmar o texto atual da página.
A análise usou o código e tooltip do GTNH, fixados no commit `a3e1e11241a814c9fa0dd0973d5699548428f689` do
GTNewHorizons/GT5-Unofficial (master consultada nesta data; não identificado como release do pack).

- [Controlador original](https://github.com/GTNewHorizons/GT5-Unofficial/blob/a3e1e11241a814c9fa0dd0973d5699548428f689/src/main/java/tectech/thing/metaTileEntity/multi/MTEEyeOfHarmony.java)
- [Documentação dentro do mod](https://github.com/GTNewHorizons/GT5-Unofficial/blob/a3e1e11241a814c9fa0dd0973d5699548428f689/src/main/resources/assets/gregtech/lang/en_US/tooltip/eye-of-harmony.md)
- [Definição das receitas](https://github.com/GTNewHorizons/GT5-Unofficial/blob/a3e1e11241a814c9fa0dd0973d5699548428f689/src/main/java/tectech/recipe/EyeOfHarmonyRecipe.java)
- [Catálogo planetário](https://github.com/GTNewHorizons/GT5-Unofficial/blob/a3e1e11241a814c9fa0dd0973d5699548428f689/src/main/java/tectech/recipe/EyeOfHarmonyRecipeStorage.java)
- [Licença declarada no repositório](https://github.com/GTNewHorizons/GT5-Unofficial/blob/a3e1e11241a814c9fa0dd0973d5699548428f689/LICENSE.txt): LGPLv3. Verificar separadamente a origem
  dos assets que forem reutilizados; este estudo não copia código ou assets ao projeto.

## Conclusão

É tecnicamente viável aproximar o GTNA das mecânicas do GTNH em Minecraft 1.20.1/GTCEu 7.5.3.
Será uma reconstrução das mecânicas sobre a API moderna, e não instalação das classes de 1.7.10.
O comportamento atual é uma simplificação substancial, apesar da estrutura visual semelhante.

## O que já existe no GTNA

- Grade 33×33×33, com controller na última aisle, linha 16, coluna 16. Contagens verificadas:
  896 casings A, 534 D, 168 E, 138 F, 48 G, 36 posições B e um controller.
  As contagens correspondem às do original após renomear símbolos. Não foi demonstrada igualdade
  geométrica completa após transformação de orientação; esta deverá ser validada em GameTest.
- Renderer com estrela e três objetos orbitais fixos; não acompanha seleção planetária.
- Rede wireless Nexus, buffers persistidos de H₂/He e formatador numérico corrigido no G-0142.
- Três receitas fixas: `stellar_atmosphere`, `stellar_metallogenesis`,
  `stellar_superheavy_synthesis`; usam Gravi Star/fuel rods e UU-Matter.
- Exigência fixa de 1.024.000.000 de cada gás, recolhidos em lotes de 100.000.000 por segundo.
  Circuitos 1–4 selecionam duração de 120/60/30/15 segundos, independente da duração-base da receita.
  Custo inicial começa em 5.277.655.810.867.200 EU e multiplica por 8 entre circuitos.
- Não há seletor planetário, tiers de campos, sorteio de sucesso, pity, penalidade de excesso,
  Astral Arrays ou crédito wireless de EU ao terminar.
- As 168 posições E usam Dimensional Bridge Casing. No original representam Time Acceleration
  Field Generators; portanto existe também uma diferença de função dos blocos da estrutura.

## Mecânicas de referência

O original seleciona um planeta por item no controller. Usa três famílias de campo, cada uma com
nove tiers, que controlam acesso, duração, probabilidade e rendimento. Excesso de fluido penaliza
chance/rendimento; a proteção após falhas não deve ser descrita como garantia universal porque o
código ainda aplica penalidades de excesso. Falhas podem produzir SpaceTime. Astral Arrays permitem
paralelos e passam a exigir mistura de plasma estelar.

O catálogo considera minérios da dimensão e processamento em materiais; produz poeiras, plasmas e
materiais especiais. Tempo e gases variam por tier planetário. A duração-base mais baixa do catálogo
é 18.000 segundos (5 horas), antes dos descontos. O custo energético deriva da receita e o ciclo
credita energia wireless ao concluir; retorno líquido depende do planeta, campos e overclock.

O controller confere um input bus, dois input hatches e uma saída de cada tipo, rejeita entrada
ME stocking/dual crafting e não utiliza Energy Hatches. Essas restrições precisam de equivalentes
explícitos na API moderna.

Há diferenças entre algumas fórmulas do tooltip e do código, inclusive escala energética dos
paralelos. Para implementar, escolher uma release/commit e usar testes numéricos extraídos dessa
referência, sem misturar fórmulas de revisões distintas.

## Riscos encontrados na implementação atual

`EyeOfHarmonyMachine.recipeModifier` retira EU e gases imediatamente. No GTCEu 7.5.3,
`RecipeLogic.checkMatchedRecipeAvailable` chama `fullModifyRecipe` antes de `checkRecipe` e
`setupRecipe`. Assim, uma avaliação que depois falha pode cobrar recursos sem iniciar o ciclo.
Isso foi verificado por leitura de código, não reproduzido em jogo nesta pesquisa.

Além disso, o método calcula duração fixa e ignora `data.tier` das três receitas. A máquina recolhe
gás enquanto formada, sem checar se está ociosa. O matcher usa `any()` nos vazios e não fixa os
limites/posições dos hatches como o original.

A rede Nexus possui capacidade, regras entre dimensões e perda configurável. Seu `addEnergy`
retorna a quantidade bruta aceita, que pode ser menor que o solicitado. O retorno do Eye deverá
persistir o restante; descartar essa informação pode perder produção. Usar `BigInteger` nos cálculos
não elimina a necessidade de verificar o limite de `Int128` ao transferir para essa rede.

## Sequência proposta

Esta é a sequência preliminar da pesquisa. O autor depois pediu iniciar por blocos/itens/arte;
o [roadmap de implementação](eye-of-harmony-implementation-roadmap.md) é a ordem de execução
atual, com esse primeiro lote e critérios por marco.

1. **Fixar referência e compatibilidade.** Escolher versão GTNH; registrar matriz de paridade,
   blocos/receitas substituídos e crédito de origem. Manter inventário dos IDs já publicados.
   Proposta: oferecer modo legado para saves existentes e modo fiel configurável. A seleção
   do default e política de migração ainda não foi decidida pelo autor.
2. **Construir o ciclo completo com um planeta.** Começar pelo Overworld, com receitas explícitas
   e verificáveis. Estados: ocioso → carregando → pronto → executando → entregando saídas.
   Avaliação e UI nunca consomem recursos; confirmação do início debita uma vez, após validar
   todas as condições. Salvar plano, saldo debitado, progresso e resultado/estado aleatório.
   Troca de dono ou planeta não pode alterar uma operação em andamento.
3. **Reconstruir campos e estrutura.** Criar os três conjuntos de nove tiers; exigir uniformidade
   dentro de cada conjunto, permitindo tiers diferentes entre famílias. Implementar hatches e
   vazios corretos, ler os tiers na formação e verificar orientação/renderer nas quatro direções.
   Prever conversão das atuais Bridge Casings e regeneração do preview/tooltip.
4. **Completar a operação sem paralelos.** Adicionar chance, rendimento, excesso de gases,
   falha/SpaceTime, pity e overclock com limites claros. A UI deve prever custo, retorno líquido,
   gases mínimos/atuais, penalidade, chance e tempo antes de começar. Creditar EU e entregar
   itens/fluidos em lotes, mantendo pendências persistidas para saída cheia ou rede indisponível.
5. **Ampliar catálogo e progressão.** Overworld/Nether/End no núcleo. Ad Astra fornece os planetas
   presentes no pack; demais dimensões entram por perfis configuráveis, sem exigir os mods do GTNH.
   Usar identificadores de dimensão e aproveitar os Planet Data Chips existentes quando adequado.
   Primeiro catálogo explícito; depois gerar sugestões a partir de veias do GTCEu, com lista
   permitida e revisão de subprodutos. Não copiar automaticamente todos os recursos do pack.
6. **Adicionar Astral Arrays e apresentação.** Só depois de validar um ciclo completo. Implementar
   plasma estelar, paralelos, recuperação dos upgrades e cálculo agregado das saídas. Não criar
   milhões de ItemStacks nem iterar milhões de operações a cada tick. Escolher algoritmo de
   sorteio equivalente e validar distribuição. Renderer deve responder ao programa ativo e
   possuir toggle de animação. JEI/EMI/Jade precisam mostrar quantidades grandes e condições reais.

## Critérios de aceitação

- Consulta de receita, saída cheia e tentativa sem recursos não cobram EU/gases.
- Início confirmado cobra exatamente uma vez; término credita exatamente uma vez.
- Save/reload, unload e reconexão durante cada estado não duplicam nem perdem recursos/saídas.
- Interrupção/desmontagem seguem política explícita; não há refund implícito explorável.
- Fórmulas conferem com a referência nos extremos de tiers, overclock, excesso e paralelos.
- Sucesso, falha e pity são determinísticos nos testes; contagem de tentativas é persistida.
- Recipe selector, campos, hatches e orientação são cobertos por GameTests de estrutura/operação.
- Ausência de Ad Astra omite programas espaciais sem quebrar o núcleo.
- Balanceamento é medido junto de mineradores, Artificial Star e rede Nexus do pack.
- Gate completo exigido pelo projeto e QA visual/manual do autor antes de publicação.

## Escopo deste checkpoint

Somente pesquisa/documentação; nenhuma mecânica nova, receita, config ou asset foi implementado.
Não há estimativa de prazo: o primeiro marco é um ciclo planetário completo sem paralelos,
validado com persistência e rede wireless, antes de expandir o catálogo.

## Atualização após iniciar EOH-03 — 04/10/2026

A auditoria completa da revisão fixada encontrou as receitas dos campos/casings/Astral Array
em `gregtech/loaders/postload/recipes/BECRecipes.java`, na progressão BEC com nanites e condensados.
O controlador continua na ResearchStationAssemblyLine. Não pressupor que a cadeia clássica
está presente nessa revisão. O autor adiou fabricação para priorizar estrutura e funcionalidade.
A estrutura existente foi conferida célula por célula e já coincide após conversão de eixos;
[resultado](eye-of-harmony-structure-audit.json). Sete materiais centrais foram registrados sem
receitas automáticas; a operação planetária e suas regras matemáticas ainda serão implementadas.
