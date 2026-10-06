# Eye of Harmony — roadmap de implementação fiel ao GTNH

Atualizado em 04/10/2026. Base: Minecraft 1.20.1 / GTCEu 7.5.3 / GTNA 0.5.1.
**EOH-01 implementado localmente; texturas aprovadas pelo autor. EOH-03 e EOH-04/Overworld passaram a validação automática; QA de estrutura e interface/operação no cliente pendente. Fabricação adiada; catálogo de outros planetas, viewer e renderer nas próximas etapas.**

## Objetivo e referência

Reconstruir a identidade, estrutura, progressão e operação do Eye of Harmony original,
começando pelo conteúdo físico: blocos, itens, texturas e materiais. A lógica vem depois,
com uma primeira operação completa antes de ampliar o catálogo e os paralelos.

Referência congelada: GTNewHorizons/GT5-Unofficial,
`a3e1e11241a814c9fa0dd0973d5699548428f689`. É a revisão de master estudada,
não uma release do modpack identificada. Atualização da referência exige revisar diferenças.
O [estudo](eye-of-harmony-gtnh-study.md) documenta as fontes e as lacunas atuais.

Prioridade de fidelidade: funções dos blocos → estrutura/hatches → regras matemáticas →
receitas/progressão → apresentação. Adaptações inevitáveis de dimensões, materiais e APIs
ficam explícitas em uma matriz de equivalência. O padrão fiel não recebe descontos arbitrários;
ajustes do GTIA pertencem ao perfil do pack.

## Inventário inicial verificado

| Grupo | GTNA atual | Entrega planejada |
|---|---|---|
| Compressão | Um bloco fixo | Nove variantes, tier interno 0–8 |
| Aceleração temporal | Bridge Casing em 168 posições | Nove geradores próprios |
| Estabilização | Um bloco fixo | Nove variantes |
| Boundary Casing | High Power Casing como substituto | Casing correspondente ao original |
| Casings externos | Transcendent e Injection já registrados | Verificar nome, função, modelo e origem |
| Controller | ID e renderer existentes | Preservar ID; conferir overlay inativo/ativo |
| Seleção planetária | Sem slot de planeta | Planet Blocks/seletores com slot persistido |
| Astral Array Fabricator | Ausente | Item próprio, acumulação e retirada posteriores |
| Materiais especiais | Equivalências não fechadas | SpaceTime, mistura estelar, matérias de anãs e Universium |

As 27 texturas dos campos estão mapeadas em [eye-of-harmony-assets.json](eye-of-harmony-assets.json),
com caminho original, candidato Modernity, tier, destino proposto, dimensões e SHA-256 local.
No Modernity `2026-09-07`, existem EM_DIM_0–8, EM_FIELD_0–8 e STABILITY_CASING_8:
**19 de 27** candidatos encontrados. STABILITY_CASING_0–7 existem na árvore original do GTNH,
mas não no caminho correspondente do Modernity local. `itemAstralArray.png` também não está
nesse caminho do Modernity; existe no repositório original. Não criar tiers visualmente iguais
por falta de imagem nem tratar a licença do pack como autorização automática por asset.

## Marcos e dependências

| Marco | Entrega revisável | Depende de | Estado |
|---|---|---|---|
| EOH-00 | Referência, inventário e plano | — | Inventário inicial concluído; auditoria restante aberta |
| EOH-01 | Blocos, itens e texturas visíveis | EOH-00 | Texturas aprovadas; demais checks manuais pendentes |
| EOH-02 | Materiais e receitas de fabricação | EOH-01 | Fabricação adiada pelo autor; sete materiais centrais registrados |
| EOH-03 | Estrutura fiel e leitura dos campos | EOH-01 | Validação automática passou; inspeção no cliente pendente |
| EOH-04 | Uma operação planetária completa | EOH-03 + materiais centrais | Overworld integrado; validação automática passou; QA no cliente pendente |
| EOH-05 | Catálogo de dimensões e viewer | EOH-04 | Planejado |
| EOH-06 | Astral Arrays e paralelos | EOH-04/05 | Planejado |
| EOH-07 | Visual, migração e validação final | Todos | Planejado |

### EOH-00 — fechar contratos antes de importar

- [x] Conferir três famílias, nove variantes por família, texturas e contagens locais.
- [x] Fixar a revisão do controlador/catálogo de referência.
- [x] Completar o mapa dos casings externos, Boundary Casing, overlay, Astral Array e seletores do lote inicial.
- [ ] Mapear os recipes loaders de cada componente e suas dependências materiais/itens.
- [x] Conferir nomes de tiers, índices 0–8 internos e apresentação 1–9; evitar deslocamento de um tier.
- [x] Verificar origem/licença de cada imagem e animação importada neste lote. Modernity é preferido quando disponível
  e utilizável; registrar exceções e não representar autorização pendente como concedida.
- [ ] Inventariar outros usos dos IDs antigos antes de defini-los como aliases ou legado.

Saída: manifest completo com cada entrada marcada como reaproveitar/importar/adaptar,
com origem, destino, direitos verificados e dependências. Nenhum asset sem origem identificada.

### EOH-01 — primeiro lote: blocos, itens e arte

- [x] Registrar as três famílias de campos com nove tiers e arrays ordenados em GTNAEyeOfHarmonyContent,
  inicializados a partir de GTNABlocks/GTNAItems.
  IDs registrados: `<family>_field_generator_tier_0` até `_tier_8`, conforme manifest;
  são registros próprios, separados dos IDs legados.
- [x] Cada bloco tem seu BlockItem, nome, tier legível, tooltip de função e origem GTNH.
  Registrar as 27 variantes no creative; conferir visualmente a exposição no creative/JEI.
- [x] Registrar Boundary Casing e casings espacial/temporal próprios, mantendo os existentes.
- [x] Importar somente PNGs/animações aprovados pela auditoria, preservar frames e interpolação.
  Arquivos CTM de 1.7.10 precisam de adaptação ao renderer moderno; não copiar configs cegamente.
- [x] Criar modelos, blockstates, loot, tags, ícones e traduções en_us/pt_br.
- [x] Registrar Astral Array Fabricator e os primeiros seletores físicos de dimensão.
  Overworld, Nether e End formam o lote inicial; planetas externos vêm em EOH-05.
- [x] Manter os IDs antigos existentes; não substituir silenciosamente blocos em mundos salvos.
- [x] Atualizar GTNASources e THIRD_PARTY_NOTICES com o conteúdo efetivamente incorporado.

Aceitação: 27 campos distintos + casing/item novos resolvem suas texturas; nenhum bloco roxo/preto,
loot correto e tier reconhecível. Conferir PNGs e modelos automaticamente e a aparência/animação
no cliente. Esta etapa ainda não conecta os novos blocos à operação da máquina.

Entrega física: **33 blocos e um item**, com 51 PNGs e 32 animações preservadas.
39 imagens vêm do Modernity e 12 da referência GT5U; os dois overlays estão importados,
mas sua aplicação ao controlador pertence a EOH-07. Receitas e integração estrutural continuam
em EOH-02/03. A exposição visual no creative/JEI e a aparência das animações ainda exigem o cliente.

- [x] Autor confirma as texturas dos blocos e do item no cliente (04/10/2026).
- [ ] Concluir os demais checks do [roteiro visual](eye-of-harmony-content-manual-test.md).

### EOH-02 — materiais, componentes e progressão

**Decisão do autor (04/10/2026): adiar receitas do multibloco e dos componentes; focar estrutura
 e funcionalidade.** A fabricação não condiciona os próximos testes em Creative. Não implementar
receitas substitutas para obter o Eye durante esse foco.

A referência fixada contém a progressão nova via **BEC Condensate Assembler**, em
`gregtech/loaders/postload/recipes/BECRecipes.java`: 27 campos, três casings e Astral Array,
com nanites por slot e condensados entangled. O controlador permanece no
`tectech/loader/recipe/ResearchStationAssemblyLine.java`. Isso amplia o inventário futuro de
fabricação: BEC, rede de condensados, nanites, metamateriais e dependências Godforge/GT++/GoodGenerator.
Entangled condensate não deve virar fluido fabricável num mixer genérico: a referência o reserva
para a rede BEC (`CondensateType.java`). A decisão de fabricar fica adiada, sem mudar a revisão fixada.

Sete identidades compartilhadas pelas progressões estão registradas: SpaceTime, RawStarMatter,
White/Black Dwarf Matter, Universium, Time e Space. Os quatro metais têm formas estruturais;
autogeração de receitas está desabilitada para não inserir processamento endgame barato.
RawStarMatter usa líquido normal (295 K), apesar do nome conter plasma; Time/Space e os quatro
metais usam líquido a 0 K, como as definições da referência. A arte de materiais usa os modelos
GTCEu existentes nesta etapa, não os renderers especiais GTNH. Nenhuma fabricação foi acrescentada.

- [x] Auditar GTCEu 7.5.3/GTNA para as sete identidades centrais; nenhuma equivalente encontrada.
- [ ] Auditar os demais materiais da progressão BEC quando a fabricação for retomada.
- [x] Mapear e registrar SpaceTime, Condensed Raw Stellar Plasma Mixture, White/Black Dwarf Matter,
  Universium e fluidos Time/Space, com formas/fluidos necessários à operação futura.
- [ ] Portar fabricação de campos 0–8, casings, controller, Astral Array e seletores.
  Fonte candidata já localizada: `tectech/loader/recipe/ResearchStationAssemblyLine.java`;
  localizar os demais loaders antes de afirmar paridade da cadeia.
- [ ] Registrar, por receita, máquina, energia, duração, entradas, quantidades, pesquisa e saída
  originais; documentar cada material/item sem equivalente disponível.
- [ ] Manter dependências da progressão em uma lista explícita; não trocar item endgame por
  circuito genérico sem registrar a adaptação.
- [ ] Conferir número de slots/entradas e colisões no lookup; receita visível precisa ser fabricável.

Aceitação: cadeia acessível por receitas reais, sem ingrediente vazio/omitido, nenhum recipe
serializer null inesperado e sem crescimento circular que impeça construir o primeiro Eye.

### EOH-03 — estrutura e tiers

- [x] Comparar célula por célula com a estrutura de referência, incluindo orientação.
  Preservar convenção local: linha zero na base, controller na última aisle, face externa.
- [x] Validar 33³: 896 casings externos, 534 injection, 138 compressão, 168 aceleração,
  48 estabilização e 36 células de fronteira (31 casings + cinco hatches no arranjo original).
- [x] Ler tier uniforme em cada família, permitindo valores diferentes entre as três famílias.
- [x] Exigir um input bus, dois input hatches, um output bus e um output hatch;
  sem stocking ME, pattern buffers/proxies, hatches duais ou Energy Hatches.
- [x] Conferir o tratamento original das células vazias antes de impor air/any.
- [x] Atualizar preview, assembly no terminal, diagnóstico e tooltips.

Aceitação: formação N/S/E/W, rejeição de tiers misturados dentro da mesma família, hatches
inválidos e peças faltantes. Preview e estrutura montada precisam coincidir.

Geometria conferida célula por célula: [auditoria](eye-of-harmony-structure-audit.json),
35.937 células. Conversão: `local[z][y][x] = original[y][32-z][x]` e remapeamento de símbolos.
A malha existente já coincide; somente os predicados/registros de bloco foram substituídos.
Tier uniforme por família, independente das outras famílias, mostrado em 1–9 na interface.
Espaços continuam ignorados (any), como os símbolos não mapeados da StructureLib original.

Adaptação de hatches: GTCEu distingue ME Input Bus/Hatch com buffer físico de **ME Stocking**.
O bloqueio recai no `IMEStockingPart`; buffers de crafting/proxies e Dual Hatches também são
rejeitados. Saídas ME comuns continuam possíveis. Candidatos do preview recebem o mesmo filtro.
Essa equivalência é específica do modelo moderno, não uma permissão aos stocking buses do GTNH.

**Mundos existentes:** estruturas feitas com os blocos substitutos anteriores deixarão de formar.
Não há troca automática de blocos nem apagamento de dono/gases/NBT; reconstruir com os novos
casings e campos, mantendo o controlador. A migração de operação permanece em EOH-07.
A simulação cosmos legada ainda está no controlador; tiers estruturais não aplicam as fórmulas
planetárias até EOH-04. Fabricação nova adiada; validar a estrutura em Creative.

- [ ] Autor confere preview/montagem e os três tiers na interface usando o
  [roteiro estrutural](eye-of-harmony-structure-manual-test.md).

### EOH-04 — ciclo fiel com Overworld, sem paralelos

Base de cálculo implementada em `EyeOfHarmonyMath` e conectada ao controlador com o catálogo
Overworld moderno. A cobrança foi removida do modifier; transações pertencem ao ciclo persistido.
Validação automática passou; QA de interface/relog/unload no cliente ainda pendente. Auditoria em
[`eye-of-harmony-operation-math.md`](eye-of-harmony-operation-math.md).

- [x] Slot de planeta persistido; circuito aceita a faixa da referência, inclusive zero.
- [x] Operação própria com plano imutável: selecionar → carregar → validar → debitar →
  executar → resolver resultado → entregar. Modifier/preview/consulta nunca cobram recursos.
- [x] Reproduzir duração, entrada/retorno EU, gases, excesso, chance, rendimento e pity,
  usando o código fixado quando tooltip e código divergirem; registrar a divergência.
- [x] Absorver gases e limpar buffers conforme os estados/regras do original, com quantidades
  de long e aritmética energética exata. Não herdar o lote fixo atual de 100 milhões.
- [x] Confirmar início uma vez, persistir débito, plano, progresso, sorteio e saídas pendentes.
  Resultado não pode mudar por reload nem pelo jogador trocar planeta/circuito durante o ciclo.
- [x] Finalizar EU uma vez; tratar capacidade, limites, dimensão e política de perda Nexus.
  Crédito parcial mantém restante salvo. Mesma garantia para itens/fluidos em saída cheia.
- [x] Definir explicitamente interrupção, desmontagem e troca de dono, com comportamento de
  referência e adaptações de recuperação documentados.
- [x] Mostrar planeta, tiers, recursos mínimos/atuais, excesso, chance, rendimento, custo,
  retorno, saldo líquido esperado, tempo e motivo de bloqueio na interface.

Aceitação: uma receita completa com casos de sucesso/falha/sem recursos/saída cheia;
nenhum débito em busca falhada; save/reload/unload não duplica nem perde saldos/produtos.
Testes usam valores determinísticos e relógio controlado para operações longas.

Catálogo: distribuição GTCEu moderna adaptada ao total de 2.25 fluxos VM3 do GTNH;
não anunciar equivalência literal das listas de minérios entre versões.
Renderer/viewer continuam nas fases seguintes. [Roteiro manual](eye-of-harmony-operation-manual-test.md).

### EOH-05 — catálogo planetário e integração de receitas

**Progresso local:** Overworld executável (G-0155) e viewer próprio JEI com catálogo real,
páginas completas e tooltip paginada implementados em G-0156 (abertura natural/Z revisados em G-0157). QA visual pendente em
`eye-of-harmony-viewer-manual-test.md`. Nether/End, demais dimensões, REI/EMI e
invalidação do cache de operação após `/reload` ainda pendentes.

- [ ] Overworld/Nether/End; depois Lua, Marte, Vênus, Mercúrio e Glacio quando Ad Astra estiver presente.
  Cada dimensão moderna recebe correspondência documentada; Glacio não vira planeta GTNH por suposição.
- [ ] Seletores inspirados nos Planet Blocks, com dimensão registrada; Planet Data Chips existentes
  podem ser ingredientes de aquisição, preservando a identidade e o slot da máquina.
- [ ] Definir materiais/produtos, processamento de minérios/subprodutos, plasmas, custos e tier
  por programa. Evitar conceder automaticamente todos os materiais registrados.
- [ ] Catálogo explícito primeiro; geração baseada em veias com filtro e auditoria depois.
- [ ] Exibir receita real no JEI/EMI: quantidades grandes, requisitos e chances dinâmicas.
  Integrações espaciais ausentes omitem os programas correspondentes sem quebrar o núcleo.
- [ ] Separar receitas atuais de cosmos simulation: planejar transição ou conservar como
  programas legados identificados; não apagar conteúdo já publicado sem migração.

Aceitação: cada programa fabricável, demonstrado no viewer e executado em GameTest;
nenhuma receita espacial carregada com item ausente; perfil GTIA auditável separadamente.

### EOH-06 — Astral Arrays

- [ ] Inserção/acumulação/retirada dos fabricadores, limites e persistência iguais à referência.
- [ ] Regime de plasma estelar, paralelo, sorteios, escalas energéticas e ausência de pity
  em paralelos; não aplicar o Parallel Hatch genérico como substituto.
- [ ] Distribuição estatística validada sem gerar milhões de objetos/iterações por tick.
- [ ] Contagens, overflow e crédito/entrega parcial cobertos nos extremos.

Aceitação: de zero upgrades ao teto, quantidade de sucessos/produtos e EU conferem;
retirada não duplica upgrades e não muda um ciclo já iniciado; desempenho medido.

### EOH-07 — apresentação, migração e fechamento

- [ ] Renderer reage ao planeta/programa e aos tiers; incluir toggle de animação.
- [ ] Overlay original ativo/inativo adaptado, sincronização cliente, som e diagnóstico Jade.
- [ ] Plano versionado para saves: nunca apagar NBT nem reinterpretar tier/gases/EU antigos
  silenciosamente. Opção legado/fiel continua proposta até política ficar documentada.
- [ ] Evitar trocar Bridge Casings globalmente: outros multiblocos podem usá-los.
- [ ] Checklist manual de estrutura, tooltips, animação, viewer, fabricação, consumo/retorno,
  rede cheia, reload e operação com outros jogadores.
- [ ] Rodar gate completo; registrar os resultados por marco no ledger.
- [ ] Autor testa e aprova publicação antes de commit/push main.

## Ritmo de entrega

Primeira implementação: EOH-00 restante + EOH-01, com galeria dos 27 campos e inventário completo.
Depois receitas e estrutura; o primeiro marco funcional é EOH-04. Cada marco tem checkpoint,
validação automatizada proporcional e pendências manuais visíveis. Não marcar importação ou
fidelidade como concluída apenas porque o conteúdo aparece no creative.

Gate do projeto: `./gradlew spotlessCheck compileJava runUnitTests runGameTestServer runData --offline`.
Quando o autor pedir teste no jogo: `./gradlew runClient --offline`.

## Validação de EOH-03 — 04/10/2026

Gate obrigatório passou: Spotless/compile, 29 unitários, **158/158 GameTests**, datagen
`written: 0`. Seis GameTests novos cobrem o hash da geometria/material identities, formação
N/S/E/W, tiers independentes/reset, cada porta obrigatória, tipos proibidos, preview e montagem
pelo Nexus Structure Terminal em Creative. Log local: `/tmp/gtna-eoh-structure-verified.log`.
Uma falha inicial era do setup do teste (DUAL_IMPORT_HATCH IV não registrado no GTCEu 7.5.3);
o teste agora seleciona um tier dual existente. Nenhuma receita nova de fabricação.
