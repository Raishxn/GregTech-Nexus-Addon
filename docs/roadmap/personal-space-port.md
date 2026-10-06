# Personal Space (PDim) — alvo de port fiel ao GTNH

Estado em 2026-10-06 (G-0175): **DimensionConfig, editor GUI, portal, `/pspace`, tempo/clima e céu
portados**; falta QA no cliente. Ver checkpoint G-0175 no ledger. O texto abaixo descreve etapas anteriores.

Estado anterior: **port inicial implementado localmente**. Há gerador de camadas/ruas,
catálogo persistente, criação dinâmica, portal com item vinculado e comandos de teste.
**Ainda não há editor completo, visuais de céu/clima, alteração de geração em chunks
futuros nem receita.**
Referência verificada: [GTNewHorizons/PersonalSpace](https://github.com/GTNewHorizons/PersonalSpace),
commit `a292401e0a067e37e7e02abee8bd48b58a6e1571`, Minecraft 1.7.10, `LICENSE` LGPL-3.0.
GTNA usa Forge 1.20.1. A equivalência pretendida é de regras e experiência, não de classes antigas.

## Contrato de fidelidade a verificar no cliente

| Área | Comportamento original confirmado | Referência |
| --- | --- | --- |
| Criação | Um portal novo no Overworld cria uma dimensão nova ao salvar suas opções. O portal ativo guarda o destino; o portal de retorno é vinculado. Não há receita upstream: cabe ao pack decidir. | `PortalTileEntity.java`, `README.MD` |
| Persistência | Configuração de cada dimensão é salva; portal quebrado mantém seu vínculo no item. Reiniciar o servidor não pode trocar IDs/destinos. | `DimensionConfig.java`, `PortalTileEntity.java`, `PortalItem.java` |
| Presets básicos | Vazio; plano baixo (`bedrock;dirt*3;grass`); plano alto/de mineração (`bedrock*4;stone*58;dirt;grass`). A lista de presets e os blocos permitidos são configuráveis pelo servidor. | `DimensionConfig.java`, `Config.java` |
| Editor | Camadas, bioma, céu/cor, estrelas, nuvens, clima, ciclo solar, vegetação/árvores, limites, ruas, marcador central. Opções visuais podem mudar depois da criação; geração fica travada após uso e o administrador pode liberar **uma** alteração. | `GuiEditWorld.java`, `DimensionConfig.java`, `PortalTileEntity.java`, `PersonalSpaceCommand.java` |
| Comandos | `give-portal`, `tpx`, `ls`, `where`, `allow-worldgen-change`; permissões e checagens devem ser refeitas no servidor. | `PersonalSpaceCommand.java` |
| Geração | Camadas planas por altura; plataforma de 9×9 no chunk (0,0); bioma selecionado; decoração e árvores opcionais. | `PersonalChunkProvider.java` |
| Importação antiga | O original migra saves UtilityWorlds. Exige estudo separado de formato e compatibilidade; **não** presumir que saves 1.7.10 abrem em 1.20.1. | `README.MD`, `DimensionConfig.java` |

## Ruas e terrenos — regras exatas do gerador original

- Lotes repetem a cada `intervalX + gapWidth` e `intervalZ + gapWidth` **chunks**. Intervalos X/Z aceitam 0–20; largura da rua 0–5 chunks. Para coordenadas negativas, usa módulo não negativo.
- Os chunks de espaço entre lotes recebem `ROAD` ou `SOLID` na camada superficial. `ROAD` usa bloco A como piso, B nas duas bordas e C em linha central de dois blocos de largura, com traços de 4 blocos a cada 8 e deslocamento de 2. No cruzamento X/Z, B marca apenas os quatro cantos externos; o restante usa A.
- Os limites do lote usam dois blocos alternados em listras. Podem ser aplicados só na superfície superior ou em todas as superfícies expostas, conforme opção `S`.
- Marcador central é opcional, com direção `SE/SW/NE/NW`. O algoritmo usa posição relativa aos intervalos X/Z; comparar coordenadas negativas, quatro direções e chunks de fronteira em GameTests.
- O preset copiável inclui todas as partes: `camadas|B,blocoA,blocoB,intervalX,intervalZ|G,largura,tipo,blocoA,blocoB,blocoC|S,0/1|C,1,direção,bloco`. `C` é opcional; strings antigas podem omitir `S`. O editor tem campos próprios para estas opções.
- Os três presets *padrão* do repositório não ligam ruas. Um template plano **com ruas** deve ser oferecido explicitamente no GTNA como preset adicional, preservando a edição manual; não atribuir esse default ao original.

## Adaptação técnica necessária no 1.20.1

1. Provar criação, registro, sincronização cliente/servidor, recarga e remoção segura de múltiplos `ServerLevel` em Forge 1.20.1. O `DimensionManager` numérico usado pelo original não existe da mesma forma; destinos precisarão de chaves estáveis e persistidas. Criação e chunk de rua já passaram em GameTest; **reinício real e teleporte de cliente ainda não**.
2. Implementar gerador por dimensão que produza as camadas e ruas sem reescrever chunks existentes. Mudanças de geração autorizadas afetam apenas chunks novos, como no original; mostrar isso claramente no editor.
3. Persistir configuração e vínculo do portal com validação de proprietário/permissão, limites de quantidade configuráveis e lista de blocos/biomas autorizados no servidor. A GUI apenas apresenta o estado, o servidor valida cada atualização.
4. Portar portal, item, teleporte de ida/volta, editor, renderização de céu/clima e comandos. Verificar morte, logout, dimension travel, restart e multiplayer dedicado.
5. Decidir receita/progressão com o autor quando o restante já for concreto; upstream deliberadamente não possui receita. Não publicar nem instalar jar no pack antes do QA e aprovação do autor.

## Validação antes de declarar o port 1:1

- Comparar chunks amostrados com o algoritmo de `PersonalChunkProvider` (lotes, ruas, cruzamentos, limites, centro, coordenadas positivas e negativas, vazio e dois superflats).
- Testar bloqueio e liberação única de worldgen, alteração visual posterior, presets e rejeição de blocos não autorizados.
- Testar dois portais independentes, portal minerado/recolocado, link de retorno, persistência após reinício, permissões e carga em servidor dedicado com dois clientes.
- Rodar `./gradlew spotlessCheck compileJava runUnitTests runGameTestServer runData --offline` após cada etapa de código; depois `runClient --offline` quando o autor pedir teste no jogo.

## Implementado no GTNA até aqui

- `PersonalSpaceTerrain` espelha coordenadas de lotes, ruas, listras, cruzamentos e centro; teste unitário inclui chunks negativos.
- `PersonalSpaceChunkGenerator` produz camadas planas, ruas e plataforma inicial com blocos atuais configurados por ID. `PersonalSpaceDirectory` guarda IDs numéricos a partir de 180 e opções no `SavedData` do Overworld; as chaves modernas são `gtna:personal_space/pdim_<id>`.
- `PersonalSpaceWorlds` usa Infiniverse 1.0.0.5 (dependência obrigatória) para criar/carregar `ServerLevel`. Comandos de operador para teste: `/gtna personalspace create void|flat|mining|roads`, `list`, `tp <id>` e `return`.
- Portal `gtna:personal_space_portal`: agachado alterna temporariamente entre quatro presets antes da criação; uso normal cria/entra no mundo e mantém portal de retorno. O item guarda o ID ao quebrar e recolocar. Sem receita, como upstream; aparência provisória usa textura vanilla de Crying Obsidian. O editor GTNH e suas opções por campo ainda faltam.
- Gate passou com 193 GameTests; um cria uma dimensão real e confere o primeiro chunk de rua. Roundtrip de `SavedData` e NBT do item do portal também foram testados. Não foi feito reinício de servidor nem QA de cliente.

O gerador ainda fixa as opções de worldgen no momento da criação. Antes de oferecer edição
após desbloqueio, é necessário permitir que **só chunks futuros** leiam a nova configuração;
trocar blocos já construídos não reproduziria o original.

O próximo passo é portar o editor do portal com estado validado pelo servidor. A referência
serializa camadas, bordas, lacunas e centro no formato
`camadas|B,blocoA,blocoB,intervaloX,intervaloZ|G,largura,preset,blocoA,blocoB,blocoC|S,0/1|C,ativo,direção,bloco`.
O campo `S` aplica a decoração a todas as superfícies expostas; a implementação atual só trata
a camada superior. O importador precisa aceitar nomes e metadados legados de forma explícita,
mostrar incompatibilidades de blocos da versão 1.7.10 e não gravar uma edição parcial quando
algum campo for inválido. A edição após geração também exige um gerador que consulte a versão
atual das opções por chunk novo e mantenha os chunks existentes intactos.

### QA manual quando o autor pedir teste em jogo

1. Com Infiniverse 1.0.0.5 presente, obter `gtna:personal_space_portal` no criativo ou com `/give`.
2. Colocar no Overworld, agachar e clicar para escolher `ROADS`, clicar normalmente. Conferir estrada, interseção, plataforma e portal de retorno; voltar e entrar de novo.
3. Quebrar o portal de origem em survival, pegar e recolocar: o item deve mostrar o ID e continuar levando ao **mesmo** mundo. Conferir que o portal de retorno foi relincado ao novo local.
4. Sair/reiniciar o servidor e repetir ida/volta; testar dois portais distintos e dois jogadores. O GameTest cobre criação/roundtrip de dados, mas **não** esse reinício real nem a interface do cliente.
5. Conferir os quatro presets e o comportamento quando a posição fixa do portal de retorno está ocupada. A interface atual é provisória: agachar apenas alterna presets antes da primeira criação.

Este port não afirma permissão individual: PersonalSpace e PersonalSpace Unofficial declaram
LGPL-3.0; Infiniverse declara MIT. Atribuição pública está em `THIRD_PARTY_NOTICES.md`.
