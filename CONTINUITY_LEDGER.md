# GTNA — Continuity Ledger

Memória operacional do desenvolvimento do GregTech Nexus Addon (GTNA). Existe para que uma
**sessão nova, sem contexto de conversa, consiga continuar o trabalho** sem redescobrir o que já
foi feito nem repetir os erros já pagos.

## Como usar numa sessão nova

1. Leia **Estado atual** e **Pendências abertas** abaixo.
2. Rode o **gate de validação** (seção Comandos) para confirmar que o ponto de partida está verde
   antes de mexer em qualquer coisa.
3. Para o detalhe de um tema, os documentos de referência são:
   - `docs/roadmap/technical-audit-pattern-buffer.md` — auditoria técnica, fases, plano de testes.
   - `docs/roadmap/pattern-buffer-fidelity-gap.md` — tabela de fidelidade GTNA ↔ GTLCore/GTOCore.
   - `docs/roadmap/tooltip-standard.md` — convenção de tooltips (GTOCore-adaptada) e pendências.
4. Ao concluir qualquer etapa, **acrescente um checkpoint** aqui (ID `G-####` + data + o que mudou
   + validação + commits + pendências que abriu).

## Regras do ledger

- Registrar **fatos verificados**, não intenções. Se algo não foi testado, dizer que não foi.
- Não marcar como concluído sem validação proporcional ao risco (build/teste/gametest/datagen).
- Toda pendência conhecida fica visível, mesmo que pequena.
- Não apagar checkpoints antigos; eles preservam contexto histórico. O topo é o estado atual.

> **Visualizacao offline do ore processing (G-0122):** `docs/visualization/` ganhou um viewer HTML
> autossuficiente (canvas 2D, texturas reais embutidas) e 7 imagens 1920x1080 prontas para a
> galeria do Modrinth/CurseForge, geradas de forma deterministica a partir dos patterns reais dos
> dois ore processors. Inclui uma **proposta** de 8 modulos auxiliares com bonus (nada registrado
> no mod). Nenhum arquivo Java/build mudou; o gate Gradle nao foi rodado por isso.

## Estado atual

> **Forge of Gods — etapa 4 (G-0192):** DTPF do GTNH (estrutura 33×24×33, desconto de catalisador, calor de bobina),
> bobina Eternal, Dimensional Bridge Casing, Special Laser Lens; Six-Phased Copper, DTR, Celestial Tungsten, Astral
> Titanium, Chromatic Glass, Advanced Nitinol. Gate PASS 212.

> **Forge of Gods — etapa 3 (G-0191):** Advanced Fusion Reactor do GTO portado (estrutura base + 4 extensões de
> tier até UEV + extensão cross-recipe), 34 blocos GTO com textura e receita, controlador com receita; fusões de
> Hypogen, Rhugnor e Metastable Oganesson. Gate PASS 211. Montagem das extensões no cliente pendente.

> **Forge of Gods — etapa 2 (G-0190):** plasmas por fusão (11) e cadeia dos catalisadores DT (DTCC→DTSC + Excited)
> no Mixer/Laser Engraver; Mixer passou a 6 fluidos e Laser Engraver a 1 fluido. Gate PASS 209.

> **Cadeia do Forge of Gods (G-0189):** 47 materiais GTNH registrados (`GodforgeChainMaterials`) e mapa da cadeia
> em `docs/roadmap/forge-of-gods-material-chain.md`; receitas vêm por etapas. Matrix modules e Nexus Hypercore
> Casing removidos; Personal Space Portal com receita EV. Gate PASS 208.

> **Peças restritas por tier e controladores (G-0188):** infinite input / output boost só agem em receitas do
> próprio tier (antes do overclock) e têm receita cara LV–MAX; Chemical Plant, Component Assembly Line,
> Thermal Power Pump e ME Pattern Buffer Proxy ganharam receita. Gate PASS 208.

> **Receitas wireless e endgame (G-0187):** todas as hatches wireless (LV–MAX, 1A–1.048.576A) têm receita;
> Nexus Capacitor UEV–MAX e hatches accelerate/overclock/thread/parallel até MAX também. Blocos sem receita:
> 353 → 88 (70 são itens restritos por config). Gate PASS 207.

> **EOH nos planetas (G-0186):** programas para Overworld, Nether, End, Lua, Marte, Vênus, Mercúrio e Glacio
> com parâmetros GTNH por tier; catálogo lido dos veios de cada dimensão; 5 Planet Blocks novos com receita;
> evento KubeJS `GTNAStartupEvents.eyeOfHarmony`. Gate PASS 207. Falta a arte de Glacio (autor) e QA no cliente.

> **Veios nos planetas (G-0185):** com Ad Astra, Lua/Marte/Vênus/Mercúrio/Glacio têm veios GTCEu reais
> (33 portados do GTO; 7/12/8/6/10 por planeta) em pedras próprias de cada planeta. Gate PASS 205.
> Só chunks novos recebem os veios. QA no mundo pendente.

> **ME Export Buffer e relatório de cobertura (G-0184):** parte LuV `gtna:me_export_buffer` (bus +
> hatch de saída → ME, buffer long sem travar receita) e GameTest que grava
> `run/gtna-recipe-coverage.txt` com o que não tem receita. Gate PASS 204 GameTests; QA no cliente pendente.

> **EOH / receitas de teste e texturas (G-0183):** 32 receitas adaptadas de construção EOH
> carregam na Assembly Line; os materiais sólidos especiais têm modelos GTCEu e sprites Modernity
> ou GTO. Gate PASS 202 GameTests. QA visual e fechamento da cadeia Survival do Godforge pendentes.

> **Personal Space / port do DimensionConfig, editor e céu (G-0175):** configurações completas do
> original (visual, worldgen, preset `|B|G|S|C`, nomes 1.7.10), gerador lendo a configuração viva
> (mudança liberada por `/pspace allow-worldgen-change` só afeta chunks novos), modo S, void em y=128,
> portal/relink/item do original, `/pspace` completo, editor GUI portado com widgets.png original,
> tempo/clima próprios por dimensão, cor do céu/estrelas/nuvens. Gate PASS 195 GameTests. QA no
> cliente pendente (GUI, céu, reinício).

> **Personal Space / portal provisório (G-0174):** bloco e item próprios conectam
> as dimensões; primeiro uso cria mundo, retorno é colocado na plataforma e o
> item preserva o ID ao quebrar. Agachar alterna entre quatro presets antes da
> criação. Editor GTNH completo, visuais, alteração de worldgen e QA no cliente
> ainda faltam. Gate final `spotlessCheck compileJava runUnitTests runGameTestServer
> runData --offline` PASS, com 193 GameTests e verificação do drop real do portal.

> **Personal Space / núcleo inicial (G-0173):** geração de camadas planas e ruas
> do original, dimensão dinâmica por ID, configurações persistidas e comandos de
> operador implementados localmente. 192 GameTests passaram, inclusive criação de
> dimensão e chunk de rua. Ainda faltam portal, editor, alteração autorizada do
> worldgen, visuais, reinício real e QA de cliente. Infiniverse virou dependência
> obrigatória; não instalar jar/publicar antes do QA do autor.

> **EOH / auditoria de progressão (G-0172):** dependências de fabricação verificadas
> contra revisão GTNH congelada, inventário material e comparação GTCEu/GTNA. Documento
> propõe rota BEC integral ou adaptação Crude independente, nomes/substituições e planetas.
> Não implementa receitas ou materiais; fechamento recursivo dos mods externos pendente
> se escolhido port BEC. Gate PASS 30 classes / 190 GameTests.

> **Personal Space / PDim (G-0171):** autor pediu port fiel ao GTNH, com destaque
> para mundo plano editável e ruas. Referência PersonalSpace 1.7.10 no commit
> `a292401e0a067e37e7e02abee8bd48b58a6e1571` auditada; contrato e diferenças
> técnicas para Forge 1.20.1 em `docs/roadmap/personal-space-port.md`.
> Nenhuma parte da dimensão foi implementada ou validada no cliente ainda.

> **Wireless / acabamento e Matrix ilimitada (G-0170):** nomes EN/PT emitidos pelo
> addon e traduzidos nas conexões; casing/overlay animados originais GTLAdditions,
> origem/licença/hashes documentados. Matrix com 750 capacitores internos todos MAX
> tem armazenamento BigInteger sem teto e limite de rede ∞; saldo real depositado
> permanece finito. Formação real, downgrade, invalidação e reload testados.
> Gate PASS 30 classes / 186 GameTests; QA visual permanece manual.

> **Port wireless GTLAdditions (G-0169):** dois terminais de rede Nexus registrados,
> entrada/saída energy+laser, vínculo persistente por Data Stick/Nexus Linker e receitas
> adaptadas. Sem buffer/segunda rede; mantém capacity, dimensão, matrix e perdas.
> Um terminal aceita a receita Infinity real com modifier do Star. Somatórias/vistas
> long protegidas inclusive com múltiplos parts. Gate PASS 30 classes / 179 GameTests;
> formação física de 109 slices e QA visual continuam manuais.

> **EOH / página única e rebalanceamento (G-0167):** EMI registra uma receita com
> catálogo completo indexado, preview único até 81 itens/18 fluidos e warning de
> omitidos. Circuito usa débito base × (k+1)²; tempo mantém 2^-k e piso de um tick.
> Artificial Star gera 16× por combustível e permite até 16 dynamos, suficientes
> para Infinity base com 16 MAX 1.048.576A. Gate final/QA no checkpoint abaixo.

> **EOH / operação normal e hatches preservados (G-0166):** Replace conserva os
> IMultiParts instalados e seus dados, inclusive com No Hatch/Demolition. Aceleração
> dev de 600× removida; circuito 0–24 e tiers verificados. Controller distingue gás
> consumido no ciclo e reserva seguinte; Quantum Terminal/Flux Matrix mostram última
> retirada direta com valor exato na tooltip. Gate completo PASS: 30 classes / 173
> GameTests. Cliente novo iniciado com EMI, sem FastQA; conferência visual pendente.

> **EOH / QA de grades, unidades e preview (G-0165):** grades EMI separadas
> (até 9×9 itens + 9×2 fluidos), aviso sempre presente e quantidade após ícone.
> Plasmas de ciclos novos: 8 milhões de baldes, 8 bilhões mB, exibidos 8MB;
> energia do programa e planos já pagos preservados. Height global 1024 desfeito,
> hook opcional aumenta apenas aba EOH e restaura normal ao trocar para preview.
> Compilação/contrato EMI e 172 GameTests PASS; gate completo e QA abaixo.

> **EOH / formato vertical de referência (G-0164):** autor pediu grade grande acima
> dos dados, como a captura GTNH, substituindo as duas colunas de G-0163. EMI agora
> tem planeta central no topo, 9 colunas × até 11 linhas e dados abaixo; altura do
> rodapé medida pelas traduções. Limite local EMI aumentado de 256 para 1024 pixels
> GUI, respeitando tela/escala. Cliente vertical iniciado após gate; QA pendente.

> **EOH / painel ampliado e teste rápido (G-0163):** autor rejeitou o tamanho do EMI
> e a apresentação do controlador. EMI agora 360 pixels GUI com produtos e dados
> em colunas separadas. Controller maior, resumo fixo de estado/progresso/tempo
> restante/chance/rendimento; EU abreviado com valor exato na tooltip. Perfil dev
> `-PeohFastQA` avança ciclos novos/existentes em 600× (base: ~30 s a 20 TPS), sem
> alterar custos/produtos. Aparência ainda aguarda conferência do autor.

> **Retomada / QA (G-0162):** log do cliente corrigido mostra Artificial Star em 618 ms
> e atividade do servidor depois, sem confirmação visual do autor. Erro assíncrono
> de BlockState null durante construção permanece pendente; possível concorrência
> no estado compartilhado do pattern. Gate de retomada PASS, 171 GameTests.
> Novo cliente com EMI iniciado a pedido do autor em 05/10; QA ainda pendente.

> **Travamento do Nexus Terminal / Artificial Star (G-0161):** duas capturas do cliente
> mostraram checks GTCEu disparados pelo replay de snapshots Forge após useOn; construção
> havia terminado em 1882 ms. Refresh movido para END do tick, guard cobre notificações
> tardias e libera checks normais depois; remoção do controller continua imediata.
> Espaços ignorados pulados antes de acesso ao mundo, seleções não limitadas em cache
> por operação. Gate 30 classes / 171 GameTests PASS. Desempenho no Artificial Star
> precisa de teste no cliente reiniciado; colocação ainda síncrona. Nenhum kill do cliente.


> **Correção visual EMI (G-0160):** textos de informação pretos, aviso vermelho,
> quantidades compactas por ícone e setas internas conforme a altura real do painel.
> A versão G-0159 truncava a área visível (18/97 numa única página) embora indexasse
> o catálogo completo; essa apresentação foi rejeitada pelo autor. Reflow agora mantém
> acesso a todos os produtos. Gate 30 classes / 171 GameTests PASS; QA visual pendente.


> **EOH / portas ME, viewer e terminal (G-0159):** controlador usa Spatial Casing;
> cinco portas exclusivamente ME (entradas de buffer finito; stocking/crafting/duais
> recusados), mesmo filtro no preview/construtor. Entrega ME em lotes grandes preserva
> long e orçamento por tick. Terminal inicia centralizado; Battery mostra capacitores
> PSS registrados, sem Battery Buffers. Viewer próprio EMI opcional e JEI compartilham
> catálogo, grade adaptável até 99 produtos, Spacetime Tier por nome, prefixos EU,
> Recipe Energy Efficiency e aviso de saídas fora da página. Tooltip mantém cores ao
> paginar/quebrar linhas. Gate **30 classes / 171 GameTests**, datagen PASS. Cliente
> com EMI iniciado para QA visual do autor. Nenhuma nova HUD Jade, fabricação ou publicação.


> **Correções do QA do autor (G-0158):** três casings EOH agora usam as texturas específicas
> do BlockGTCasingsBA0 GTNH (o import inicial usava incorretamente BlockGTCasingsTT).
> Terminal abre somente controles; botão Blocos por tier → Selecionar abre categorias,
> e clicar numa categoria abre a grade. Painéis fecham com ×; vidro/iluminação excluídos,
> inclusive da aplicação de escolhas antigas. Gate final **30 classes / 171 GameTests**,
> datagen PASS. QA visual do novo fluxo e texturas permanece manual. Tudo local.


> **EOH apresentação / Nexus Terminal (G-0157):** frente dedicada com overlays importados;
> renderer acompanha o ciclo planetário, com estrela central e um Overworld em órbita.
> Manual aparece naturalmente ao passar o mouse; Z avança sem Shift. Nexus Terminal
> roxo/preto em três painéis (controles, categorias, tiers), escolhas compatíveis por ID
> com leitura do NBT antigo; Mirror e Demolition agora atuam na construção. Três aliases
> de blocos duplicados removidos com remapeamento para saves. Gate: **30 classes unitárias /
> 171 GameTests**, datagen PASS. Aparência, rede AE real e migração de save antigo ainda
> precisam de QA do autor; roteiros `nexus-terminal-advanced-manual-test.md` e
> `eye-of-harmony-visual-cleanup-manual-test.md`. Sem novas receitas, commit/push/publicação.


> **Eye of Harmony / JEI e manual (G-0156):** categoria própria JEI usa o mesmo catálogo
> Overworld da operação, grade 9 × 6 paginada com todos os produtos, quantidade exata longa
> na tooltip, gases, duração, tier e energia/chance base. Cosmos legado oculto apenas no JEI.
> Tooltip Shift/Z em EN/PT paginada pela altura da tela explica as regras implementadas;
> Astral Arrays/paralelos/outros planetas declarados pendentes. Gate: **30 unitários / 167
> GameTests**, datagen confirmado. QA visual: `docs/roadmap/eye-of-harmony-viewer-manual-test.md`.
> REI/EMI, demais planetas e invalidação do cache de operação em /reload continuam pendentes.
> Sem novas receitas de fabricação, commit, push ou publicação.

> **Eye of Harmony / Overworld integrado (G-0155):** slot de planeta, circuito 0–24, coleta
> integral de gases, débito único, ciclo/resultado persistidos, entrega parcial e crédito Nexus
> sem dupla perda. Plano calculado congelado; pausa ao desabilitar/desmontar, dono bloqueado.
> Catálogo GTCEu moderno com adaptação declarada do fluxo/processamento GTNH; demais planetas
> não operam ainda. Nova busca Cosmos desativada, ciclos antigos pagos terminam uma vez.
> Gate: **30 classes unitárias / 166 GameTests**, datagen `written: 1` (textos EN atualizados).
> QA manual de UI, relog/unload e drop/reposição: `docs/roadmap/eye-of-harmony-operation-manual-test.md`.
> Viewer JEI substituído em G-0156; renderer ainda legado; EOH-05/06/07 seguintes. Sem fabricação nova ou publicação.

> **Eye of Harmony / cálculo serial (G-0154; integrado em G-0155):** fórmulas GTNH isoladas em planos imutáveis,
> com teste determinístico de circuito 0–24, gases/excesso, tiers, EU, sucesso/falha e pity.
> Gate dessa base: 30 unitários/**158 GameTests**, datagen `written: 0`.
> A integração que faltava foi implementada em G-0155; não usar a descrição histórica dessa
> fase como estado atual do controlador. Fabricação continua adiada.

> **Eye of Harmony / estrutura (G-0153):** novos casings e 27 campos ligados à estrutura;
> tiers uniformes por família e independentes; exatamente cinco portas sem stocking/crafting/dual/energia.
> Geometria 33³ coincide célula por célula com o GTNH. Gate: 29 unitários/**158 GameTests**, datagen `written: 0`.
> Fabricação adiada pelo autor; sete materiais centrais registrados sem receitas automáticas.
> Operação planetária implementada posteriormente em G-0155; Nova busca Cosmos desativada.
> QA estrutural pendente: `docs/roadmap/eye-of-harmony-structure-manual-test.md`. Sem publicação.

> **Eye of Harmony / conteúdo físico (G-0146):** 27 campos tierados, três casings, três
> Planet Blocks e Astral Array registrados; 51 texturas/32 animações com proveniência e licenças.
> Texturas dos blocos e do item aprovadas pelo autor em 04/10/2026; demais checks manuais pendentes.
> Roteiro em `docs/roadmap/eye-of-harmony-content-manual-test.md`.
> Componentes ainda sem fabricação/integração à operação fiel; próximos marcos EOH-02/03/04.
> Mudanças locais, sem publicação.

> **Relato do jogador / GTCEu 7.5.3 (G-0142):** corrigidos o display do Eye of Harmony
> (String passada a DecimalFormat), 31 entradas de receita rejeitadas (blocos/IDs como texto),
> dois catalisadores com chance inválida e o nome do refmap. Lava Maker/Superheater agora usam
> circuitos 1/2 para evitar colisão. Gate: 28 unitários, **139/139 GameTests**, datagen `written: 0`,
> jar com refmap configurado presente. Hatches restritos descartados no modo Normal são comportamento
> intencional. Mudanças locais; QA manual e revisão de limites de entradas de receitas pendentes.

> **Roadmaps públicos (G-0138):** GTNA tem roadmap/TODO atualizado em PT/EN/ES, com 14 tarefas
> priorizadas; GTIA tem roadmap próprio no modpack, com 21 tarefas e critérios por etapa.
> Conteúdo implementado, validação humana e propostas estão separados. Arquivos locais, sem publicação.

> **QA do autor (G-0137):** minerador aceita Accelerate Hatch e paralelo IV+ acompanha o hatch
> até teto configurável de 1.024; EV serial. Ritmo escolhido: preciso 10 s / aleatório 30 s /
> cultura 60 s antes do overclock. Scanners LV–IV têm en_us; Jade aleatório resume pool/chances.
> Novo lançador dev inclui GTIACore e perfil GTIA com Radon. Guia manual de petróleo/Radon:
> `docs/roadmap/void-fluid-manual-test.md`. Gate final: **28 unitários/127 GameTests**, datagen
> `written: 0`. Mudanças locais; conferência visual pelo autor ainda necessária.

> **Extração marciana e equipes (G-0135):** perfil GTIA agora percorre depósito gerado normalmente
> em Marte → extração nativa MV → registro → produção remota T2 na Terra. Corrigido escopo FTB:
> `FTBOwner.getUUID()` era pessoal; agora usa `Team.getTeamId()` (equipe efetiva).
> Teste FTB cobre compartilhamento/cópia, recusa a terceiros e saída/reentrada da equipe.
> Gate sem FTB: **28 unitários/125 GameTests**, runData `written: 0`; perfil com FTB **125/125**.
> Jogadores são simulados nos testes; inspeção visual e duas pessoas conectadas continuam pendentes.
> Novo GTNA instalado na Prism; reabertura entrou no Test World sem novo crash. Tudo local, sem commit/push.

> **QA conjunto GTIA (G-0134):** corrigida colisão `plate_superdense` entre GTNA e
> GTIACore, reusando o tipo de ícone compartilhado em ambos. O pack entrou no Test World
> e carregou o datapack de Radon. QA revelou colisões no índice dos programas do Void Miner;
> circuito 1 agora seleciona básicos EV, circuito 2 as veias LuV/ZPM e pool aleatório ZPM.
> Gate GTNA **28 unitários/124 GameTests**, core **13 GameTests/build/QA estático**.
> Jars ativos na Prism, backups preservados; reabertura final entrou no mundo; perfil GTIA **124/124**.
> UI/montagem manual, primeira extração em Marte e equipes reais ainda pendentes. Sem commit/push.

> **Fluidos descobertos / Void Fluid Drilling Rig (G-0133):** nova máquina EV 3×7×3,
> independente do minerador de minérios, com registro de extração nativa real, dados reutilizáveis
> por fluido/origem/proprietário/equipe, produção local e upgrades remotos por etapa. Configuração
> `void_fluid_drill.json`; seis programas terrestres no default, Radon de Marte no perfil GTIA.
> Receita de bootstrap corrigida para perfuradora MV + Sensors HV, mantendo montagem EV.
> Gate: 28 unitários e **123/123 GameTests**; perfil GTIA/deposito também **123/123**;
> datagen repetido `written: 0`. T4/T5 aguardam dados de pesquisa; QA cliente/equipes reais,
> primeira extração em Marte e balanceamento/campanha do pack pendentes. Sem commit/push.

> **Void Miner: preciso/aleatório e cadeia GTL (G-0132):** a elétrica agora consome essências
> no preciso e tem recipe map aleatória separada. Portados World Data Scanners LV–IV,
> Incubator 5×5×5, Essence/Essence Seed, 40 essências GTL e cinco planetárias. Chips são
> escaneados nos planetas e reutilizados na cultura; deixaram de ser fabricados no Assembler.
> Entrada EV preservada com três programas terrestres e cinco planetários; veias completas
> mantêm energia LuV/ZPM e o pool aleatório completo exige ZPM. Custos/rendimentos adaptados
> estão em `docs/roadmap/void-miner-essence-chain.md`. Gate: 28 unitários e **114/114 GameTests**;
> datagen repetido `written: 0`. Restam QA em cliente, paralelos IV+ e balanceamento no pack.
> Sem commit/push.

> **Void Miner EV: programas planetários e custos corrigidos (G-0131):** cinco Planet Data Chips
> do GTNA têm receitas EV com amostra planetária não consumível; os programas do Ad Astra exigem chip e
> amostra não consumíveis. A validação agora requer Drilling Fluid positivo e rejeita outros
> fluidos. O teto padrão de paralelo é 2 de IV até MAX, configurável por tier. A tela informa
> programa ativo, custo efetivo de fluido e EU/t e paralelo. Gate completo: **111/111 GameTests**;
> resta QA em cliente e operação real com Parallel Control Hatch IV+. Sem commit/push.

> **Void Miner elétrico EV (G-0130):** nova máquina `gtna:electric_void_miner` (GTIA-D-159..D-162)
> com um programa por máquina, seletor não consumível, energia + Drilling Fluid por operação e
> validação por `config/gtna/balance/electric_void_miner.json` (tier mínimo, tetos de fluido e
> saída, paralelo a partir de IV, fallback opcional). Três programas terrestres por circuito,
> cinco programas planetários do Ad Astra (pedra do planeta como amostra) e controller EV com
> Titanium terrestre. Gate: 111/111 GameTests e datagen `written: 0`. Sem commit/push; aguarda
> teste manual e os programas planetários do pack.

> **Publicação do merge na main (G-0129):** o push de 26/09 (`origin/main`: HUD wireless de
> energia, família de Dehydrators LV–OpV, QA do EBF, cadeia de Boron Carbide, exemplo KubeJS,
> guarda do JEI) foi integrado ao trabalho local de 29/09 (perfis GTIA, Pattern Buffers, Nexus
> wireless loss) e publicado. A numeração paralela G-0122–G-0124 foi mantida com nota de merge.
> Gate do merge com **107/107 GameTests** e datagen repetido `written: 0`. Commit `45d68fc`.

> **Pattern Buffers: upgrade e UI após QA do autor (G-0128):** o item de upgrade não derruba mais
> padrões no chão: o swap salva o estado persistente completo (`forDrop=false`) e restaura no
> buffer novo, com o drop suprimido durante a troca. Causa raiz: o `saveToItem` só carrega campos
> `@DropSaved` (o inventário de padrões não é um deles) e o antigo probe de capacidade anexava um
> segundo storage ao block entity e sobrescrevia o NBT. O `renderState` do bloco antigo também
> deixou de ser transferido (corrige a máquina invisível após o upgrade). A página da UI passou
> de 9×6 para 9×10 (90 slots), então 72 cabem em uma página e 162 em duas. Gate: 104/104 GameTests,
> unitários e datagen `written: 0`; a rodada com Advanced 72/Ultimate 162 também passou 104/104.
> Reteste manual do autor aprovado. Sem commit/push.

> **Perda wireless do Nexus Flux Matrix configurável (G-0127):** `nexus_flux_matrix.json` ganhou
> `lossApplication` (`LEGACY` padrão, `MATRIX_INPUT_ONCE` e `NO_LOSS`), `lossPercentByTier` e
> `generatorArrayAppliesSeparateLoss`. No modo GTIA a rede aplica a perda uma única vez na entrada,
> inclusive a energia vinda do Generator Array; o resto fracionário e a dimensão da Matrix são
> salvos no mundo. Matrix e Quantum Terminal exibem bruto, creditado e perda. O gate completo
> anterior passou com 103/103 GameTests e a automação `tools/test_nexus_wireless_profiles.py`
> confirmou os três perfis (`legacy`, `matrix_input_once`, `no_loss`) com 103/103 cada.
> Pendências: QA manual no pack (UI, ausência de perda dupla pelo Array, gate entre dimensões e
> migração de save antigo). Sem commit/push.

> **ME Pattern Buffers configuráveis (G-0126):** os quatro buffers leem capacidade de
> `config/gtna/balance/pattern_buffers.json` (defaults 9/21/32/72, teto 540), com exemplo GTIA
> Advanced 72/Ultimate 162. A UI troca a página de slots de fato, Jade/tooltip mostram a
> capacidade, e a cópia para um buffer menor aproveita slots livres e informa o total colado.
> Um histórico de capacidade impede redução acidental; NBT com mais slots que o configurado é
> recusado com orientação de recuperação. Gate completo verde com **100/100 GameTests**; o
> perfil ampliado e a tentativa de redução também passaram com 100/100 cada. Restam QA visual
> em cliente e medição de desempenho/crafting sob carga no perfil GTIA. Sem commit/push.

> **Escopo GTIA corrigido (G-0125):** o autor excluiu ME Storage, Nexus ME Hypercore e Nexus
> Molecular Forge das propostas de configuração. O item 11 do backlog GTIA agora trata dos
> quatro ME Pattern Buffers: capacidade atual 9/21/32/72, UI paginada em 54 por página e
> referência GTLAdditions de 162 slots. A implementação exige regra de redução sem perda de
> padrões ou conteúdo interno. Nenhum comportamento de jogo mudou neste ajuste de escopo.

> **Auditoria de configurabilidade GTIA (G-0124):** o backlog externo do GTIA agora inclui
> sistemas adicionais verificados no código: cadeia de minério, processadoras elétricas,
> turbinas, farms e cobertura das chaves de habilitação. O item AE2/Nexus foi substituído em
> G-0125 pelos Pattern Buffers. Component Assembly Line e
> Steam especiais foram acrescentados como lacunas dos itens existentes. Nenhum comportamento
> de jogo mudou nesta auditoria; propostas e números GTIA ainda dependem de medição.

> **Universal Factory configurável (G-0123):** a primeira etapa do backlog GTIA implementa modos
> `LEGACY`, `SHARED_BUDGET` e `UNLIMITED` em `config/gtna/balance/universal_factory.json`.
> O default permanece legado; há exemplo GTIA em `docs/config-examples/gtia/`. O orçamento
> compartilhado limita threads e paralelo por thread, reserva operações de receitas em andamento,
> e pode desligar warmup/batch e filtrar recipe types. A UI mostra capacidade e divisão; hatches
> especiais só entram no pattern quando habilitados antes do início. Gate: Spotless, compilação,
> unitários e **97/97 GameTests** verdes. Datagen passou com `written: 0` na repetição, após uma
> falha intermitente de registro do renderer do Annihilate Generator. UI, hatches e perfil GTIA
> ainda exigem teste manual em jogo. Sem commit/push.

> **Push de 26/09 (outra máquina) — numeração paralela:** os blocos abaixo usam os IDs
> G-0122–G-0124 de 26/09 e convivem com os checkpoints locais de 29/09 acima; os dois históricos
> são reais e foram mantidos.

> **QA do autor e correções G-0124:** EBF sem módulo executou Aluminium Dust → Ingot em 11,05 s
> com energia HV, bobina Tritanium e muffler IV; EMI mostra 22,1 s antes do overclock normal do
> EBF. O Accelerate Hatch HV na carcaça superior do módulo quebrava a formação porque aquela
> célula `C` só aceitava casing; agora aceita um Accelerate, mantendo limite global de um.
> Paginação/hover JEI, boiler wireless, Dehydrators, Vacuum Dryer, Flux HUD, Rocket Engines e
> Evaporation Plant passaram no client do autor. A UI da base multi-receita recebeu estado,
> progresso, falha, energia, modo e saída. A textura CTM da carcaça Boron Carbide tinha caminho
> incorreto; corrigida. As duas famílias de Component Assembly Casings usam circuitos diferentes
> nos tiers LV–UV para evitar descarte de receita pelo GTCEu. A cadeia simplificada de Boron
> Carbide e Boron Carbide Ceramics foi adicionada. O exemplo KubeJS de desempenho foi deslocado
> para fora da base e do outro módulo. Gate: 100/100 GameTests, unitários, compilação, Spotless e
> datagen `written: 0`. O client aberto ainda usa o código anterior; aguarda reinício e reteste.

> **EBF sem módulo (G-0123):** a base do Electric Blast Furnace podia formar com energia e calor,
> mas a falha da checagem do módulo opcional deixava um `PatternError` no estado compartilhado.
> `WorkableMultiblockMachine.isRecipeLogicAvailable()` então retornava falso e bloqueava toda
> receita. O mixin restaura o estado válido da base após conferir os módulos. O GameTest existente
> agora remove o módulo, confirma base formada, zero módulos e lógica de receitas disponível;
> continua cobrindo o limite de Energy Hatches e o Accelerate exclusivo do módulo. Os 98/98
> GameTests, unitários, compilação e Spotless passaram. O primeiro `runData` desta sessão parou
> durante um erro intermitente de registro do renderer Annihilate; uma repetição isolada passou.
> A repetição confirmou `written: 0`. O reteste da receita no cliente, a
> navegação JEI e a medição do bônus KubeJS aguardam o teste do autor.

> **Gate final e envio para `main` (G-0122):** o Large Steam Solar Boiler aceita a saída de vapor
> wireless GTNA e a receita deposita vapor no tanque; os Dehydrators LV–OpV e os nomes dos Rocket
> Engines têm registros/traduções; a Flux Matrix ganhou HUD opcional de energia. O módulo KubeJS
> aceita bônus de velocidade e overclock perfeito nos controladores de múltiplas receitas, com
> exemplo carregado pelo cliente. O crash de índice do preview JEI recebeu guarda client-side.
> Gate final verde: **98/98 GameTests**, unitários, Spotless, compilação e datagen `written: 0`
> (`/tmp/gtna-0122-final-gate.log`). O cliente isolado carregou os scripts KubeJS e mostrou o HUD;
> o preview JEI específico e a execução de receita com módulo KubeJS formado ainda não foram
> retestados manualmente. O cliente e save originais do autor não foram substituídos.

> **Módulos sem bloqueio + cadeia de secagem (G-0121):** a thread de checagem assíncrona do GTCEu
> podia entrar em deadlock com o servidor: `gtna$setModuleCount` consultava o chunk para enviar o
> pacote visual enquanto segurava o lock do padrão. O envio agora é agendado na thread do servidor.
> O Dehydrator IV do GTO foi portado com a receita shaped original e suas texturas; a receita
> original do controlador Vacuum Drying Furnace voltou a existir. Gate completo verde com
> **97/97 GameTests** (`/tmp/gtna-dehydrator-deadlock-gate.log`); a geração escreveu 5 arquivos
> novos. Repetição do datagen: `written: 0`. A rejeição do Parallel Hatch relatada pelo autor
> ainda requer saber qual das duas torres ele estava testando. O primeiro `runClient` parou por
> traduções Jade ausentes no en_us gerado; após regenerar o cache do datagen, o cliente abriu o
> mundo `New World` sem essa falha e segue disponível para o reteste. Sem commit/push.

> **Evaporation Plant ainda em investigação (G-0120):** após o relato de que os hatches continuam
> rejeitados no módulo, o GameTest passou a montar a torre com o controlador voltado para norte,
> leste e sul; em cada orientação, substitui `F` por Parallel IV e Accelerate e depois move os
> hatches para `G/A`, deixando a atualização natural dos blocos agir sem chamar refresh. O módulo
> continua formado, coleta os dois hatches e concede quatro paralelos em **97/97 GameTests**.
> O mundo salvo tem um Evaporation Plant voltado para sul com Parallel IV na célula `F` inferior
> (`-13,-60,-121`, marcado `is_formed=true`, `currentParallel=4`) e Accelerate UXV em
> (`-10,-59,-117`, também formado). Há uma segunda Evaporation Plant em `-8,-59,-109` que está
> inválida no save. Pergunta pendente ao autor: em qual das duas tentou colocar o Parallel Hatch?
> Na segunda não havia Fluid Input Hatch entre as peças salvas; a base exige exatamente uma.
> O save atualizado às 07:37 mostra Parallel UHV em `-10,-58,-117` formado, com
> `currentParallel=1024`, ao lado do Accelerate UXV formado; a primeira torre está formada e
> funcionando. A segunda torre continua inválida. Isso comprova aceitação da peça naquela posição,
> mas ainda não identifica qual tentativa anterior falhou.
> A divergência in-game ainda requer a captura do ponto de colocação e do diagnóstico da estrutura.
> Gate completo verde;
> datagen `written: 0`. Cliente continua aberto. Sem commit/push.

> **QA do restante dos ports GTO (G-0119):** o módulo da Evaporation Plant foi testado com Parallel
> Hatch IV e Accelerate Hatch nos blocos `F` da torre: forma sem force scan e disponibiliza quatro
> paralelos. Após o autor constatar que isso ainda não atendia à instalação real, os mesmos hatches
> foram habilitados também em `G` e no titânio exposto `A`, preservando limite global de um de cada.
> Os sete multiblocos com
> módulos GTO foram auditados em `docs/fixes/2026-09-26-gto-module-audit.md`. A Rocket Large Turbine
> ganhou a receita original com Rocket Engine EV; os motores EV/IV/LuV e suas texturas foram
> portados. Chemical Plant e Supercritical Steam Turbine receberam seções de tooltip alinhadas ao
> GTO. Component Assembly Line usa os oito casings originais que faltavam, com receitas obtíveis;
> o layout `.rtui` e as barras de progresso do GTO foram copiados, e o texto de consumo mostra
> EU/t. Algumas receitas de casing substituem materiais exclusivos da fork GTO (detalhes G-0119).
> Gate completo: **95 GameTests**, unitários, Spotless, compilação e datagen. O visual do novo layout,
> dos casings e das tooltips ainda precisa ser confirmado no cliente pelo autor. Sem commit/push.

> **QA dos hatches e telas GTO (G-0118):** o autor confirmou o CTM e o funcionamento da UI/tooltip
> do Cold Ice; a hélice do Grinding Ball Hatch ainda estava escura e a Blaze escondia o limite de
> paralelos durante a receita. O renderer agora usa iluminação plena no sprite, e a Blaze mantém
> os 64 paralelos visíveis durante o trabalho. A injeção global que aceitava Accelerate/Overclock
> em qualquer `autoAbilities` foi retirada: o corpo principal do Cold Ice rejeita esses hatches,
> enquanto o módulo conserva seu encaixe explícito. Isa Mill, Flotation Cell e Vacuum Drying
> receberam linhas próprias de UI; as tooltips GTO tiveram requisitos técnicos ampliados mantendo
> lore e apenas `Source`. Gate completo verde: 94/94 GameTests e datagen. O efeito visual e a
> apresentação em jogo ainda precisam de reteste do autor. Sem commit/push.

> **Fidelidade visual/UI após novo QA do autor (G-0117):** o CTM base de Blaze/Cold Ice estava
> sem os arquivos `.png.mcmeta` de animação, embora as imagens fossem byte-idênticas ao GTO. Os
> metadados originais foram copiados e conferidos; o resultado visual ainda aguarda reteste no
> cliente. A Grinding Ball Hatch passou a desenhar os sprites originais de hélice
> `ball_hatch_idle`/`ball_hatch_spinning` em vez de girar o item. A UI da Blaze destaca o número
> paralelo em roxo, labels de voiding/heat em branco e temperatura em vermelho. Tooltips dos
> 16 multiblocos da leva usam o arranjo da Blaze, histórias originais onde existem e somente
> `Source` como atribuição. A UI das outras processadoras recebeu voiding, paralelo disponível e
> estado sem receita. Jade ganhou aviso de chunk não forçado, percentuais reais de receita e
> dureza/resistência na posição e cores do GTO. Ainda há divergência de EU/t entre o port e o
> GTO por mecânica de overclock do gtolib; não mascarar o valor no Jade.

> **QA GTO após as capturas do autor (G-0116):** a UI da Blaze foi complementada com paralelo
> máximo, modo de descarte, capacidade térmica e ausência de receita. Sua tooltip agora usa as
> quatro linhas originais de lore e os requisitos técnicos do GTOCore, sem a paráfrase anterior.
> O cache dos sub-patterns foi corrigido para cobrir a profundidade inteira (antes só monitorava
> a primeira aisle); o GameTest quebra e restaura uma célula do Cold Ice sem forçar scan e passa.
> Jade mostra dureza/resistência para blocos GTNA. Gate completo verde: 26 testes unitários,
> 94 GameTests, datagen; compilação/testes incrementais também passaram após o ajuste final.
> **Pendente do teste manual:** validar o visual de Blaze/Cold Ice casings e a UI/tooltips no cliente.
> A equivalência 1:1 das tooltips dos demais ports e do renderer original do Grinding Ball Hatch
> ainda não foi demonstrada; não tratar esses itens como concluídos.

> **Release GitHub 0.5.1 (G-0100):** a tag `v0.5.1` aponta para o commit corrigido
> `9808c5037e255522fae21139f35b7b80a6286d9d`; o release público contém o changelog
> completo e o JAR de SHA-256 `afc79d801e00cb7e1974a4f6b843ed7f89a19a86e1641c3bdab97fb4458e4585`.

> **Hotfix local após 0.5.1 (G-0097):** corrigido o crash de carregamento do
> `MetaMachineBlockMixin` no GTCEu 7.5.3 distribuído. O alvo em produção é `m_5871_`,
> enquanto no ambiente de desenvolvimento é `appendHoverText`. A injeção agora aceita ambos
> os nomes e `require = 0`. O teste na instância Prism `1.20.1` revelou outro alvo SRG,
> `CoreCraftConfirmMenuMixin`, também corrigido; o segundo lançamento chegou à interface do
> cliente sem falha de mixin. A validação manual no mundo ainda está pendente.

> **Desempenho do Terminal (G-0098/G-0099):** o Nexus Terminal causou um tick acima de 40 s ao
> construir o Hypercore na instância Prism. O watchdog capturou verificações repetidas do padrão
> durante a colocação. Um JAR corrigido foi instalado; a medição de uma nova construção no mundo
> ainda não foi registrada. O autor autorizou commit e push da correção mesmo com esse check pendente.

> **Feedback in-game do autor em 2026-09-25:** os módulos estão funcionando e a geometria do Nexus
> ME Hypercore também. Permanecem os checks visuais específicos do checklist manual que não foram
> confirmados individualmente, como tooltip, destaque de bloco e interface KubeJS.

> **PORTS ATUAIS:** leia primeiro `docs/roadmap/NEXT-SESSION-PORTS-2026-09-25.md` para decisões,
> estado verificado e próximo passo dos multiblocos até LuV. O handoff anterior
> `docs/roadmap/NEXT-SESSION-HANDOFF.md` é da sessão de 2026-09-21
> (Steam/large steam, formato de tooltip com source, blocos faltantes como o Industrial Steam
> Casing, convenção de orientação de estrutura e o `VaultPortHatch`). A sessão estourou o contexto
> várias vezes; **confira no código antes de agir** e **não confie** nas estruturas das
> `large_steam_*` antigas sem revisar contra o GTNL.

> **Ports locais em andamento (G-0101):** o Component Assembler base (LV–IV) e a Large Greenhouse
> foram acrescentados após a primeira leva de quatro. A extensão do Component Assembler, receitas
> ULV/LuV e os demais candidatos até LuV ainda estão em trabalho; não tratar a seleção como fechada.

> **Fábrica Química (G-0102):** `chemical_plant` portada (estrutura GTO 5×5×5 de PTFE inerte +
> bobinas + tubulação PTFE, Parallel Hatch, overclock perfeito e bônus de bobina de 5% por tier).
> A receita do controlador foi omitida por decisão do autor até que ele avalie portar toda a
> cadeia de recursos do GTO. O gate completo passou com 65/65 GameTests e datagen escrito.

> **Mega Alloy Blast Smelter (G-0103):** `mega_alloy_blast_smelter` portado do GTOCore (id
> exclusivo; o ABS normal já é do GTCEu). Reaproveita carcaças GCYM e `ALLOY_BLAST_RECIPES`, com
> bônus de 0,8× EU / 0,6× duração. Gate completo com 66/66 GameTests. Os candidatos restantes da
> seleção até LuV dependem de uma camada grande de carcaças/materiais/recipe types exclusivos do
> GTO; o roadmap e os bloqueios estão em `docs/roadmap/NEXT-SESSION-PORTS-2026-09-25.md`.

> **Módulo de atomização do Cold Ice Freezer (G-0112):** o sub-pattern do GTOCore
> (`MultiBlockD.java:367-388`) foi registrado como módulo GTNA no controlador existente (torre de
> Naquadah Alloy + Cold Ice Casing + Heat Vents + moldura de Naquadah) e libera o recipe type novo
> `ATOMIZATION_CONDENSATION_RECIPES` só com a extensão formada, como o `recipeTypeAvailable` do GTO.
> As receitas (`GTNAAtomizationRecipes`) portam `GTOMaterialRecipeHandler.java:425-455` para todos os
> materiais GTCEu/GTNA com dust + fluido (fluido/molten → dust ou líquido, gás inerte escalado pela
> massa e hélio líquido ≥ 5000 K). Substituição documentada: o gás de alta pressão exclusivo do GTO
> vira o gás regular na mesma quantidade. `Naquadah` recuperou `GENERATE_FRAME`. Gate completo com
> **26/26 testes unitários** (os contratos globais de QA foram criados aqui: `PortLangParityTest`,
> `PortChainClosureTest`, `MachineTooltipContractTest`, `PartAbilityCoverageTest` estendido e
> `ControllerRecipePolicyTest`) e **67/67 GameTests** na época. QA in-game pendente.

> **Extensão até UV (G-0114):** o corte do Component Assembler e da `component_assembly_line` subiu
> de LuV para UV (o máximo com receitas de lote no GTCEu base). Entram as carcaças ZPM/UV nas duas
> famílias (texturas GTO, CC BY-NC-SA), os oito lotes ZPM e os oito UV (48 → **64** receitas de
> `component_assembly`) e a produção das carcaças via Assembly Line com três substituições
> documentadas de soldas exclusivas do GTO (Pikyonium/ArtheriumTin/AbyssalAlloy). Gate completo com
> **26/26 testes unitários** e **94/94 GameTests**, `runData` com `written: 0`. QA in-game pendente.

> **QA in-game de 2026-09-26 e correções (G-0113):** o autor testou a leva inteira no cliente e
> abriu uma lista de divergências. O que foi corrigido na hora (G-0113) e o que **permanece aberto**
> — texturas dos casings Blaze/Cold Ice, Ball Hatch roxa/preta, throughput do ISA Mill (paralelismo
> de conteúdo do gtolib: GTO 96 saídas/ciclo em 2 receitas paralelas × GTNA 48), delay/re-scan dos
> módulos, divergência geral de tooltips/UIs/Jade — estão detalhados com as capturas em
> **`docs/fixes/2026-09-26-gto-fidelity-fixes.md`**. A próxima sessão deve começar por esse
> documento: a causa do ISA Mill já está identificada (`accurateContentParallel`), faltando a
> fórmula de compensação (batch/OC) e a decisão sobre o padrão de tooltip em camadas (F-07).

> **ISA Mill (G-0104):** `isa_mill` portado do GTOCore (estrutura comprimida 3×3×7, carcaças
> Inconel-625, prefixo `MILLED`, data key `grindball`, Ball Hatch e 48 receitas). O gate da esfera
> fica no `getRealRecipe` e a matemática de dano do GTO é aplicada em `beforeWorking` — o
> `fullModifyRecipe` do GTCEu 7.5.3 roda uma vez por candidata e danificar ali gastaria a esfera em
> receitas que não iniciam. A receita do controlador foi portada (Assembly Line) com os três
> materiais GTO que ela exige (Inconel-625/792 e Tantalloy-61) copiados 1:1 de `MaterialA`. Gate
> completo com 69/69 GameTests e datagen `written: 0`. QA in-game pendente.

> **Rocket Large Turbine (G-0105):** `rocket_large_turbine` portado do GTOCore (EV, `special=true`,
> base `V[EV] * 2.5 = 5120 EU/t`). O padrão base 3×3×3 e o módulo do motor de foguete usam apenas
> blocos GTCEu (titanium turbine/stable casing, gearbox de titânio, engine intake, moldura
> BlueSteel) e o recipe type novo `gtna:rocket_engine` roda a única receita de `RocketFuel` do GTO
> (10 mB → 512 EU/t por 20 ticks). A classe `RocketLargeTurbineMachine` porta o caminho não-mega da
> `TurbineMachine` do GTO (paralelo pelo rotor, eficiência na duração, gate de rotor, modo de alta
> velocidade com os valores normal do GTO e bônus do módulo 2×/20%/2×). Gate completo com **26/26
> testes unitários** e **71/71 GameTests**, `runData` repetido com `written: 0`. QA in-game pendente.
>
> **Supercritical Steam Turbine (G-0106):** `supercritical_steam_turbine` portado do GTOCore (IV,
> `special=false`, base `V[IV] * 2 = 16384 EU/t`). A máquina reusa a base não-mega
> `GTNALargeTurbineMachine` extraída do port da rocket turbine (mesma `TurbineMachine` do GTO para
> as duas), o módulo SUPERCRITICAL em carcaças GCYM (bônus 2×/20%/2×) e a carcaça nova
> `supercritical_turbine_casing` (texturas GTO, CC BY-NC-SA). A receita de combustível reusa
> `DenseSupercriticalSteam` no lugar do `SupercriticalSteam` do GTO com os números 80 mB → 8 mB /
> 30 ticks / `V[MV]`; a receita do controlador foi portada (Assembler, só GTCEu/GTNA). Gate completo
> com **26/26 testes unitários** e **73/73 GameTests**, `runData` repetido com `written: 0`. QA
> in-game pendente.
>
> **Flotação + Secagem (G-0107/G-0108):** o par **Industrial Flotation Cell** +
> **Vacuum Drying Furnace** foi portado do GTOCore com **cadeia fechada**: os 12 `*Front` que a
> flotação produz alimentam as 12 receitas de secagem, que devolvem dusts GTCEu + `RedMud` + `Water`,
> e o `RedMud` é neutralizado no Mixer. Carcaças/materiais novos: Hastelloy-N75 (casing/gearbox/pipe),
> `Flotation Cell`, `Red Steel Casing`, etilxantatos, turpentina, 12 foams, RedMud/NeutralisedRedMud,
> Hastelloy-N75 e Stellite. O `Vacuum Drying Furnace` reproduz o overclock de bobina do EBF
> (`GTNAHeatingCoilOverclock`, já que o GTCEu mantém a matemática package-private) no modo de secagem
> e o paralelo `2^(temp/900)` no modo Dehydrator. Gate completo com **26/26 testes unitários** e
> **76/76 GameTests**, `runData` repetido com `written: 0`. QA in-game pendente.
>
> **Extensão do Component Assembler (G-0109):** as duas camadas de `addSubPattern` do GTOCore
> (`MultiBlockC.java:328-397`) formam como módulos GTNA no controlador existente e elevam o teto de
> carcaça de IV para LuV; os cinco blocos novos (computer casing, control casings MK2, power
> transmission e cerâmica de nitreto de titânio) e os materiais compostos vieram do GTOCore com
> atribuição CC BY-NC-SA. Entram as oito receitas de lote LuV e a produção da carcaça LuV. Gate
> completo com **26/26 testes unitários** e **83/83 GameTests**, `runData` repetido com `written: 0`.
> QA in-game pendente.
>
> **Component Assembly Line (G-0110):** o `component_assembly_line` separado usa o `.mbs` real
> 47×15×31, a família de carcaças LV–LuV (texturas GTO) e o mesmo portão de receita; os nove blocos
> GTO exclusivos da estrutura foram substituídos por equivalentes GTNA/GTCEu documentados (tabela no
> checkpoint G-0110), o cross-recipe do gtolib não foi reproduzido e a receita do controlador foi
> omitida/registrada. Gate completo com **26/26 testes unitários** e **90 GameTests aprovados**,
> `runData` repetido com `written: 0`. QA in-game pendente.
>
> **QA retroativo GTO (G-0111):** `chemical_plant` e `mega_alloy_blast_smelter` fecharam a cobertura
> A1–A7 (negativo de formação, hatches/Parallel, sem módulo, contagem de receitas, execução real e
> política de controlador). O padrão do mega foi extraído para
> `GTNAMachines3.MEGA_ALLOY_BLAST_SMELTER_PATTERN` para o GameTest construir a mesma fonte. Gate
> completo com **26/26 testes unitários** e **93 GameTests aprovados**, `runData` com `written: 0`.
> QA in-game das duas máquinas segue pendente.

- Desenvolvimento na branch `main`; o histórico anterior a G-0026 está preservado no ledger.
- Versão `mod_version=0.5.1`. Base: Minecraft **1.20.1**, Forge **47.4.1**, GTCEu **7.5.3**,
  AE2 **15.4.10**, ModDevGradle legacyforge **2.0.91**.
- **Ports GTO em andamento (G-0089/G-0090):** Generator Array, Fishing Ground, Evaporation Plant
  e Greenhouse foram implementados com suas estruturas, controles e receitas correspondentes. O
  gate completo passou com **20/20 testes unitários e 57/57 GameTests** após incluir a Greenhouse.
  Ainda falta validação in-game do autor e a
  seleção de multiblocos até LuV permanece aberta; não tratar esta leva como concluída.
- **QA posterior (G-0094):** Wireless Steam Hatches não anunciam mais abilities universais de
  fluido. A Greenhouse verifica cobertura opaca acima do teto de vidro. O gate completo passou com
  59/59 GameTests; falta reteste in-game do autor com os novos binários.
- **Tooltips em padronização (G-0087):** auditoria das 84 tooltips feita; a linha genérica `Added by
  GregTech Nexus Addon` saiu (atribuição só com `Source:` no conteúdo portado), 5 descrições
  duplicadas corrigidas, literais hardcoded viraram chaves e o esquema GTOCore-adaptado
  (`§6Main Function:§r` + cores semânticas) foi aplicado como piloto (Integrated/Advanced Ore
  Processor e hatches de performance). Convenção em `docs/roadmap/tooltip-standard.md`; o restante
  (~70 máquinas/hatches), a atribuição da família ME e o `pt_br` ficam pendentes.
- **Gate verde em 2026-09-24 (G-0086):** `spotlessCheck` + `compileJava` + `runUnitTests`
  (**20/20**, inclui `OverclockHatchMathTest`) + `runGameTestServer` (**53/53**) + `runData`
  determinístico (`written: 0` na segunda execução). O GameTest novo
  `universalFactoryAutocraftsThroughAe2Network` monta uma rede ME real (célula criativa, drive +
  célula 1K, CPU nativa de crafting), forma a Universal Factory com o ME Advanced Pattern Buffer e
  resolve **dois** pedidos de uma árvore de três camadas e seis recipe types, um de cada vez;
  confere planejamento, despacho por slot, consumo dos insumos, retorno das saídas e conclusão. O
  **Overclock Hatch** passou a usar um **divisor inteiro de duração** (fator exato `1/divisor`,
  tooltip derivado do divisor), eliminando a divergência entre o tooltip percentual e o inteiro
  usado; coberto por `OverclockHatchMathTest` (L1) e `overclockHatchUsesIntegerDivisor` (L2). O HUD
  do Flux Matrix, os hatches wireless de alta vazão e a revisão geral de tooltips ficam para as
  próximas sessões.
- **Gate verde em 2026-09-24 (G-0085):** `spotlessCheck` + `compileJava` + `runUnitTests`
  (**19/19**) + `runGameTestServer` (**51/51**) + `runData` determinístico (`written: 0`). O GameTest
  novo percorre quatro pedidos separados de uma cadeia de seis etapas, com três ramos, duas junções
  e trocas sucessivas entre Bender, Compressor, Forge Hammer, Lathe, Cutter e Forming Press. O
  harness entrega patterns ao ME Advanced Pattern Buffer e confere modo e saída a cada etapa;
  planejamento e envio dos pedidos por uma CPU/rede AE2 real continuam fora desse teste.
- **Gate verde em 2026-09-24 (G-0084):** `spotlessCheck` + `compileJava` + `runUnitTests`
  (**19/19**) + `runGameTestServer` (**50/50**) + `runData` determinístico (`written: 0`). O
  GameTest novo reproduziu os três pedidos de laminated glass presos (6 tempered glass + 3 placas
  PVB) no ME Advanced Pattern Buffer e passou após identificar o pattern pela receita original,
  antes do multiplicador de paralelo. O autor reportou reteste in-game aparentemente perfeito com
  a rede AE2 real; a cobertura automática dessa integração ainda está pendente.
- **Gate verde em 2026-09-24 (G-0083):** `spotlessCheck` + `compileJava` + `runUnitTests`
  (**19/19**) + `runGameTestServer` (**49/49**) + `runData` determinístico (`written: 0`). O
  GameTest novo da Universal Factory reproduziu o consumo de um pattern pelo recipe type errado
  (stone pedido, gravel produzido), depois passou com a correção; cobre dois pedidos sequenciais
  e duas receitas de tipos diferentes em threads simultâneas. Resta QA no mundo com a rede AE2.
- **Gate verde em 2026-09-24 (G-0082):** `spotlessCheck` + `compileJava` + `runUnitTests`
  (**19/19**) + `runGameTestServer` (**48/48**) + `runData` determinístico (`written: 0`). O servidor
  gera relatório JUnit individual em `build/test-results/gametest/TEST-gtna.xml`; o CI valida o
  lote completo e publica XML/log. Um teste negativo de formação verifica o estado a cada tick.
- **Gate verde em 2026-09-23 (G-0081):** `spotlessCheck` + `compileJava` + `runUnitTests` (**19/19**) +
  `runGameTestServer` (**48/48**) + `runData` determinístico (`written: 0`). A checagem de estrutura
  usa a posição ancorada ao controller para padrões fixos, com gametests no Large Cutting Saw e
  Large Material Press. O botão está nas UIs fancy de controladores e na UI própria do Hypercore.
  Módulos KubeJS sincronizam chaves de tooltip localizadas aos clientes. Os ajustes visuais de HUD,
  Borosilicate Glass e Solar Boiling Cell e o tooltip do Brick Kiln aguardam QA visual do autor.
- **Gate anterior em 2026-09-23 (G-0080):** `spotlessCheck` + `compileJava` + `runUnitTests` (**19/19**) +
  `runGameTestServer` (**46/46**) + `runData` determinístico (`written: 0`). O Hypercore agora oferece uma CPU compartilhada e
  cria CPUs temporárias por pedido. O planner recebe ticks de servidor e mostra a origem no menu de
  confirmação. `plannerQa` comparou 18 cenários com o baseline RaishxCore; faltam QA visual e
  medição ponta a ponta no mundo do autor. Mudanças locais, sem commit/push.
- **Gate anterior em 2026-09-23 (G-0078):** `spotlessCheck` + `compileJava` + `runUnitTests` (**19/19**) +
  `runGameTestServer` (**46/46**) + `runData` determinístico (`written: 0`). Planner RaishxCore portado
  para AE2 1.20.1, limitado às redes com Interface do Hypercore. Integração de crafting real ainda
  requer QA in-game.
- **Gate anterior em 2026-09-23 (G-0077):** `spotlessCheck` + `compileJava` + `runUnitTests` (**18/18**) +
  `runGameTestServer` (**45/45**, `All 45 required tests passed`) + `runData` determinístico
  (`written: 0`). A execução carregou os mixins alterados e o Productive Bees de dev; os avisos/erros
  de receitas do GTCEu já conhecidos continuam no log.
- **Módulos (sub-patterns) na UI/preview/tooltip (G-0066..G-0068):** todo multibloco mostra **"Formed
  modules: n / total"** (`IGTNAModuleHost` + `WorkableElectricMultiblockMachineMixin`), cada módulo
  registrado vira uma **página extra no preview do JEI** (`MultiblockMachineDefinitionMixin`) e o
  **tooltip do item** lista o que o módulo libera (`MetaMachineBlockMixin` + `GTNASubPatterns`). O
  **Terminal Nexus** ("Module Build = N") constrói a base + os N primeiros módulos, inclusive com a
  máquina já formada. Adicionar/remover um módulo **re-forma** a máquina (não precisa quebrar o
  controller). O `liquefaction_furnace` é uma máquina **normal** (Parallel/Accelerate só com o
  módulo). A geometria do módulo do EBF (GTOCore) é validada pelo gametest `ebfModuleForms`.
  Pendente: um botão dedicado a módulos no preview.
- **Atualização de estrutura (G-0072/G-0073):** máquinas elétricas têm um botão de rechecagem na
  UI que envia a ação ao servidor e mostra o primeiro bloco incorreto com destaque no mundo
  (Shift força reconstrução); o Terminal força a atualização após construir. O liquefaction
  reavalia o módulo a cada 5 ticks. Hatches Parallel, Accelerate, Thread, Overclock e Output Boost
  são limitados a um de cada tipo no conjunto base + módulo. A origem GTOCore do módulo EBF é
  adicionada no momento em que o tooltip aparece, preservando idioma e animação.
- **Tooltip auxiliar e Hypercore (G-0074..G-0077):** EBF e liquefaction compartilham as quatro linhas
  de tooltip de módulo auxiliar; só a lista localizada de hatches muda. O Nexus ME Hypercore usa
  o layout 44×22×44 de `packet.txt`, com controller e Interface de CPU lado a lado no cilindro de
  vidro laminado, agora na linha 2 do padrão (oito blocos abaixo da posição original), e 320
  posições de cores. O padrão 41×43×41 de G-0074 pertence a outro multibloco
  ainda sem nome e está preservado em `docs/structures/unassigned_41x43x41_source.txt`; ele não é
  carregado pelo jogo. O gametest valida o padrão novo; a formação completa requer QA in-game.
- **Era Steam Elevator fechada (G-0058):** o módulo de ore processing do elevador está 100% (G-0057);
  os 8 módulos, o host 35×43×35 e a rede wireless estão no gate. Restam só itens de **QA manual
  visual** (`docs/roadmap/QA-MANUAL-CHECKLIST.md`). A logo do mod agora aparece em **todas** as UIs de
  multibloco (mixin client-only `FancyMachineUIWidgetMixin` + logos explícitas nas UIs custom).
- **Módulos do elevador (G-0052):** o IO de item/fluido agora é sempre pelos **hatches da própria
  estrutura** 1x5x2 (input/output bus e input/output hatch) — sem inventário interno. O status padrão
  (Running/Idle) aparece via `MultiblockDisplayText` e `isActive()` respeita o upkeep de steam.
- **Bee Breeding × Productive Bees (G-0051):** o módulo agora é integração real (spawn egg do PB como
  catalisador, 128 honey treats, saída = cópia da abelha) e **só existe quando o PB está carregado**
  (`ModList.isLoaded("productivebees")`). O PB 1.20.1 (`1.20.1-12.6.0`) entra como `modCompileOnly` +
  `modRuntimeOnly` de dev (o jogador não precisa instalar). Ver G-0051.
- **Rede wireless de vapor (G-0041/G-0042/G-0043):** o pull dos inputs é dividido por **fair share** entre
  os inputs com espaço (antes o primeiro hatch do tick drenava o pool inteiro — a rede sempre lia 0 e
  os outros 23 hatches nunca enchiam); `/gtna steam` mostra fluxo vitalício + estado por hatch, o
  Jade mostra o saldo da rede e um **HUD client-side** (G-0042, arrastável desde o G-0043, toggle
  `wirelessSteamHud`, default off) mostra saldo/fluxo/hatches + gráfico. Buffers de input/output
  separados (input bronze 100 B), solar boiler 20× e o host do elevador aceita 1 steam hatch por
  módulo (G-0043). Ver G-0041..G-0043 para causa raiz, testes e pendências.
- **Feature em foco:** o **ME Pattern Buffer multi-modo** (fidelidade ao GTLCore/GTOCore). A tabela
  de fidelidade está **toda verde** e as divergências conscientes estão documentadas no gap doc.
- **Testes hoje:** 26 unit tests (`main()` + asserts, padrão GTLCore) e 94 gametests (`@GameTest`),
  ambos no gate do CI; os GameTests geram relatório JUnit, verificado por
  `tools/check_gametest_report.py`.
- **Steam Cracker:** o autor confirmou em 2026-09-23 que a implementação deve continuar sendo a do
  GTNL; estrutura, comportamento e atribuição `GTNASources` atuais seguem essa origem.
- **Licenciamento (G-0019):** código do GTNA **LGPLv3**; assets do GTO em **CC BY-NC-SA 4.0**
  (o GTNA é **não-comercial**). Permissão do **GTOEPP** concedida pelo time GTO; atribuição de origem
  nos tooltips via `GTNASources`. Matriz completa em `THIRD_PARTY_NOTICES.md`.
- **Thread Hatch (G-0012/G-0018):** aceita pelas máquinas na base multi-receita. Migradas as que
  ganham com threads: **Industrial Slaughterhouse** e **Dimensionally Transcendent Dirt Forge**. Os
  demais controladores (Artificial Star/gerador, Nexus Molecular Forge e Eye of Wood/logic própria,
  Eye of Harmony/no-energy custom, ME Storage e Nexus ME Hypercore/não-processadores) **não** migram
  por decisão de escopo — não precisam aceitar Thread Hatch.
- **QA em expansão (G-0011):** a classe de bug "peça não encaixa na máquina" (predicado de estrutura
  fixado em bloco exato em vez de *ability*) ganhou um lint de contrato + gametest de integração.
  O plano de camadas está em G-0011.
- **Validação visual de G-0009:** o usuário forneceu screenshot in-game em
  `/home/raishxn/MineProjects/printUI/1.png`; a página em duas colunas cabe e não há vazamento
  visível na escala capturada. Outra escala de GUI ainda não foi testada.

## Checkpoints

### G-0156 (2026-10-04) — Eye of Harmony: catálogo JEI e manual paginado

- Referência visual: três capturas GTNH fornecidas pelo autor. Implementação usa dados do
  catálogo moderno real, não quantidades/tempo de captura de outro pack. Tempo Overworld
  base 360.000 ticks, 1B mB de H/He cada, tier mínimo 1, chance base 100%, EU base/retorno 60%.
- `EyeOfHarmonyOverworld.build(RecipeManager)` compartilhado entre servidor e JEI;
  overload ServerLevel conserva chamadas anteriores. Novo `EyeOfHarmonyDisplay` copia os
  tags dos produtos e divide a lista completa em páginas de 54 (nove colunas/seis linhas),
  sem truncar long ou descartar itens/fluidos. Páginas são partes do mesmo programa.
- Plugin/categoria JEI exclusivos do Eye: controlador como catalisador, Planet Block
  Overworld no topo, H/He como entradas, produtos indexados como saídas. Abreviação no
  ícone e quantidade exata na tooltip; fluidos desenhados cheios. Dados-base explícitos
  antes dos campos, circuito, excesso e perda Nexus. Sem transferência/fabricação fictícia.
- Categoria Cosmos antiga oculta via runtime JEI (inclui dev); recipes permanecem carregadas
  para ciclos antigos já pagos. Ausência de catálogo real é registrada sem inventar saídas.
  REI/EMI não recebem esta categoria própria ainda.
- Tooltip do controlador resumida; detalhes via Shift/Z, páginas dimensionadas pela altura
  da tela, textos EN/PT: campos/energia/circuito/gases/fórmulas/falha/pity/portas/entrega/escopo.
  Astral Arrays/paralelos/planetas futuros apresentados como não implementados. Nenhuma
  promessa de clique direto/controle de animação. Sem receitas novas de fabricação.
- Novo GameTest confere completude, ordem/identidade/quantidades contra catálogo real,
  valores acima de int e isolamento de tags de produto após mutação do catálogo original.
- Gate final PASS: `spotlessCheck compileJava runUnitTests runGameTestServer runData --offline`;
  **30 classes unitárias / 167 GameTests**. Log `/tmp/gtna-eoh-viewer-final.log`.
  Datagen anterior desta etapa atualizou EN (`written: 1`); gate final `written: 0`.
  Fonte GTCEu reconfirmada `v7.5.3-1.20.1`. Chaves requeridas EN/PT conferidas.
- Roadmap EOH-05 registra progresso parcial; roteiro visual em
  `docs/roadmap/eye-of-harmony-viewer-manual-test.md` aberto no painel. QA visual ainda
  depende do autor: ícones, tipografia/escala, navegação/indexação, Shift/Z, reconexão/reload.
  Invalidação do cache de operação após /reload sem unload segue pendente separada.
- Cliente atualizado iniciado após o gate; resources/atlases, inclusive JEI, carregados.
  Log `/tmp/gtna-eoh-viewer-client.log`; ingresso no mundo e abertura da categoria ainda manuais.
  Nenhuma aprovação visual inferida da compilação/testes. Tudo local, sem commit/push/publicação.

### G-0155 (2026-10-04) — Eye of Harmony: operação Overworld, transações e recuperação

- EOH-04/Overworld integrado localmente: slot de seletor real/reutilizável persistido, somente
  Overworld habilitado. Circuito virtual GTCEu prioritário, depois primeiro circuito físico,
  ausência=0 e clamp 0–24. H₂/He absorvidos integralmente a cada 20 ticks, apenas ocioso,
  formado e habilitado; buffers long com proteção de overflow, demais líquidos preservados.
- EyeOfHarmonyOverworld usa veios modernos Overworld de peso/densidade positivos, chances
  das entradas normalizadas e soma de 2.25 fluxos VM3 GTNH. OreProperty para multiplicador,
  smelt/subprodutos/fallbacks/separação/lavagem; diferenças legadas documentadas. Plasmas são
  interseção da whitelist GTNH com os sete disponíveis; combustível carregado define EU/litro,
  com dois fatores 3.85/truncamentos conservados. RawStarMatter 100k + WhiteDwarfMatter 1152.
  Não inventar plasmas/segunda distribuição small ores/GTPP ausentes do GTCEu. Cache reconstruído
  no load do controlador; invalidação por /reload sem unload ainda pendente de EOH-05.
- Modifier agora retorna NULL sem mutação. Nova busca Cosmos desativada. RecipeLogic permite
  concluir ciclo antigo pago uma vez, limpa lastRecipe e impede repetição automática.
- Único débito no commit server-side; só após sucesso limpa TODOS os gases (incluindo excesso).
  Duração, custo, retorno, chance/rendimento, UUID de crédito, resultado e produtos finais
  congelados. Sorteio ocorre uma vez no começo e resultado/histórico salvo; resultado nunca
  recalculado ao retomar. Pity resolvido no início (adaptação, sem mudar sequência serial seguinte).
- Estados idle/running/delivery persistidos, progresso, EU bruto restante, filas/cursores.
  CustomData versionado também em forDrop; anotação persiste slot/campos. Desabilitar ou invalidar
  pausa sem perder pagamento; reformar retoma. Data Stick não muda dono com dívida/ciclo.
  Reposicionar controlador pago conserva vínculo existente; UUID de crédito congelado.
- Entrega Nexus desconta retorno bruto aceito (não líquido), mantendo dívida parcial sem dupla
  perda. Produtos inseridos internamente nas portas OUT (não pela capacidade de entrada externa).
  Orçamento: 64 tentativas de pilha + um lote de até 1M mB por tick; round-robin impede starvation.
  Máquina só volta a idle após quitar todos os produtos e o crédito. Garantia é save/load normal;
  não foi implementada transação atômica entre arquivos SavedData/bloco contra crash do processo.
- UI EN/PT: slot, planeta, tiers, circuito, gases/mínimo/excesso, prévia após completar gases,
  chance/rendimento, tempo, custo/retorno/saldo antes da perda Nexus, progresso/pendências/bloqueio.
  Tooltips antigos de circuitos 1–4/1024 baldes/lote fixo corrigidos. Renderer/viewer Cosmos ainda
  legados para EOH-05/07; RawStarMatter como entrada Astral Array aguarda EOH-06.
- Oito GameTests adicionados: catálogo real e custo dos plasmas; bloqueios/modifier sem cobrança;
  coleta/circuito/disable; save/load completo do bloco + pausa/reformação/rebind/estado de drop;
  crédito parcial + saídas cheias + reload + perdas; falha com retorno; legado termina sem repetir;
  filas round-robin não bloqueiam produtos compatíveis. Fixture transacional tem 3 ticks/saídas
  pequenas, separada do teste de catálogo real. Nenhuma nova receita de fabricação.
- Gate final **PASS**: `spotlessCheck compileJava runUnitTests runGameTestServer runData --offline`,
  **30 classes unitárias / 166 GameTests**, datagen `written: 1` (EN). Log
  `/tmp/gtna-eoh-operation-verified.log`. GTCEu fonte confirmado `v7.5.3-1.20.1`.
- Auditoria/roteiro em `docs/roadmap/eye-of-harmony-operation-math.md` e
  `eye-of-harmony-operation-manual-test.md`; roadmap e índices PT/EN/ES atualizados,
  proveniência/licença do processamento registrada em THIRD_PARTY_NOTICES.
- QA humano ainda pendente: layout/slot/UI, relog real, unload de chunks, drop/reposição e fluxo
  externo de saídas. Nenhum cliente Forge existente detectado antes de iniciar. Primeira abertura
  (68923, `/tmp/gtna-eoh-operation-client.log`) falhou no GuideME: configuração lida antes de
  carregar, fora do Eye. Segunda abertura (95724, `/tmp/gtna-eoh-operation-client-retry.log`)
  passou pelo resource reload/atlases, iniciou o servidor integrado e Dev entrou no mundo. Não adicionado workaround
  de código para a corrida de inicialização do GuideME. Roteiro manual aberto no painel Codex.
  Tudo local, sem commit/push/publicação; fabricação segue adiada pelo autor.

### G-0154 (2026-10-04) — Eye of Harmony: cálculos seriais GTNH, primeiro bloco de EOH-04

- Implementado `EyeOfHarmonyMath`: programa Overworld com custo de plasma explícito, campos
  independentes 0–8, circuito limitado a 0–24, custo BigInteger `4^k`, crédito com penalidade
  por estabilização, duração com compressão/aceleração/circuito, excesso dos dois gases,
  chance/rendimento, SpaceTime de falha e histórico pity conforme código GTNH fixado.
- Plano imutável não acessa mundo/rede/inventários/RNG. Resolução usa roll fornecido 0–9999;
  retorno EU devido mesmo na falha. Sentinel Double.MIN_VALUE e comparação exata do pity
  conservados; pity inicial pode ser negativo em programas avançados. Discount usa a ordem
  de multiplicação por squaring do original para conservar truncamento de ticks.
- `EyeOfHarmonyMathTest` registrado no gate: números de referência, dois gases excedentes,
  limites de sorteio, alteração/repetição de chance, sentinel/pity, longo→Int128, clamps,
  recursos/tier insuficientes, entradas inválidas, overflow e repetibilidade.
- Primeiro gate encontrou uma corrida no teste estrutural da fase anterior: checkPatternAt
  direto competia com o async assembler, corrompendo contexto de match. Três chamadas dos
  testes EOH agora usam a API GTCEu `checkPatternWithLock`, recomendada por IMultiController.
  Não mudada a lógica estrutural de produção nem relaxadas asserções.
- Gate final **PASS**: `spotlessCheck compileJava runUnitTests runGameTestServer runData --offline`,
  **30 classes unitárias / 158 GameTests / datagen written: 0**. Log `/tmp/gtna-eoh-math-final.log`.
- Documentadas regras, limites e próximos passos em `docs/roadmap/eye-of-harmony-operation-math.md`;
  roadmap EOH-04 marcado em andamento e atribuição LGPL-3.0 atualizada em THIRD_PARTY_NOTICES.
  Atenção: custo dos plasmas GTNH usa fuelEU/litro, fator 3.85 com truncamento, soma por quantidade,
  depois outro fator 3.85/truncamento; não equivale a custo de fusão.
- **Limite explícito:** base de cálculo ainda não integrada ao EyeOfHarmonyMachine. Ciclo Cosmos
  continua legado, incluindo débito dentro do modifier; não anunciar esse bug como corrigido.
  Faltam catálogo de saídas/custos moderno auditado, slot, coleta, plano/débito/progresso/sorteio
  persistidos, Nexus gross credit parcial e saídas pendentes, GUI e testes de ciclo/reload.
  Nenhuma fabricação nova. Nenhum teste humano novo para cálculo isolado; QA estrutural anterior
  continua pendente. runClient não relançado nesta etapa. Tudo local, sem commit/push.

### G-0153 (2026-10-04) — Eye of Harmony: estrutura GTNH, tiers e montagem validados

- Autor mudou a prioridade: **não formular receitas do multibloco/componentes agora; focar
  funcionalidade e estrutura**. EOH-02 fabricação adiado; nenhum recipe loader/receita nova.
  Auditoria descobriu que a referência fixada usa BEC Condensate Assembler/nanites/condensados
  em BECRecipes para campos/casings/Astral Array; controlador ainda usa ResearchStationAssemblyLine.
  Não substituir essa fabricação por Assembly Line genérica. Decisão futura ainda aberta.
- **EOH-03 implementado:** a malha existente coincide nas 35.937 células com o GTNH fixado,
  usando `local[z][y][x] = original[y][32-z][x]` e mapa de símbolos. Evidência/hash em
  `docs/roadmap/eye-of-harmony-structure-audit.json`. Linha zero base/controller última aisle preservados.
- Eye agora usa 896 casings espaciais, 534 temporais, 138 campos de compressão, 168 de aceleração,
  48 de estabilização e 36 posições de fronteira. Exatamente cinco portas: um input bus,
  dois input fluid hatches, um output bus e um output fluid hatch; 31 Boundary Casings restantes.
  Uniformidade por família, tiers independentes 0–8 internos/1–9 na interface; invalidar zera
  leituras para -1. Espaços permanecem any conforme a referência, não ar obrigatório.
- Predicado/candidatos de preview rejeitam stocking ME, pattern buffers/proxies, dual e energia.
  GTCEu distingue ME buffers físicos comuns de stocking; essa adaptação está documentada.
  Saídas ME comuns possíveis. Appearance/model base do controlador usa o Boundary Casing novo;
  overlay/renderização planetária ainda não portados.
- Sete materiais compartilhados pelas progressões clássica/BEC foram registrados para operação:
  SpaceTime, RawStarMatter, White/Black Dwarf Matter, Universium, Time e Space. Quatro metais
  têm formas estruturais; autogeração de receitas desabilitada (nenhum atalho de fabricação).
  RawStarMatter é líquido normal a 295 K, não plasma-fuel; demais líquidos 0 K conforme fonte.
  Nomes EN/PT, ícones/modelos GTCEu existentes; renderers/GALAXY privado GTNH não portados.
- Seis testes novos: referência geométrica e formas/materiais, formação N/S/E/W, tiers/reset,
  rejeição de cada porta ausente/extra, casings/tiers inválidos, stocking/crafting/dual/energia,
  preview real e auto-build pelo Nexus Terminal em Creative. Regressão GUI usa conteúdo textual
  sem depender do índice da linha, pois agora há display de tiers.
- **Gate completo passou:** Spotless/compile, 29 unitários, **158/158 GameTests**, datagen
  `written: 0`. Log `/tmp/gtna-eoh-structure-verified.log`. Falha inicial era setup de teste
  usando Dual Hatch IV ausente; corrigido para selecionar um tier registrado e repetido o gate.
- **Pendente humano:** preview/montagem, aparência de hatches e leitura/diagnóstico na GUI:
  `docs/roadmap/eye-of-harmony-structure-manual-test.md`. Texturas EOH-01 já aprovadas pelo autor.
  Cliente anteriormente aberto precisa reiniciar para carregar as novas classes; não reiniciado
  automaticamente aqui. Alterações locais, sem commit/push/publicação.
- **Limites/migração:** estruturas legadas com blocos substitutos deixam de formar; reconstruir
  com os novos blocos mantendo controlador/NBT/dono/gases. Sem substituição automática/limpeza.
  A operação cosmos ainda é legada; tiers não aplicam fórmulas planetárias até EOH-04.
  O débito no recipeModifier legado permanece pendência conhecida para o ciclo transacional.

### G-0152 (2026-10-04) — Servidor dedicado com o pack completo do GTIA (109 mods)

- Correção de rumo pedida pelo autor: o GTIA se testa **inteiro**, não num subconjunto. O lockfile padrão do projeto
  GTIA (`full.lock.json`) trava os 109 jars da instância auditada, menos Re-Avaritia, GTMThings e Pretty Rain
  (decisões 8.5 e 8.6 e o crash); GTIACore e GTNA entram como builds locais.
- `server_smoke.py` com o pack completo: **"Done" em 45 s**, 106 de 109 mods (Oculus, FastQuit e Nolijium são só de cliente e
  não constroem em servidor dedicado). O GTNA carregou os **29 nós** de pesquisa sem pular nenhum, inclusive o do foguete.
- Achados do log (todos já listados em G-0151 ou sem relação com o GTNA): as 14 linhas de receitas do GTNA continuam, e
  surgiram 5 de mods opcionais ausentes (ProjectE, Patchouli, Chipped e um mod de `advanced_ae`), sem efeito em Start → LV.
- **Não verificado:** o cliente. Sem commit/push.

### G-0151 (2026-10-04) — Servidor de teste com o lockfile do GTIA: erros de receita do GTNA encontrados

- Ao subir um servidor dedicado só com os 16 mods do lockfile (`GTIA Modpack/tools/server_smoke.py`), o GTNA carregou sem
  problema (**"Done" em 22 a 42 s**, 28 nós de pesquisa), mas o GTCEu registrou **14 linhas ERROR de receitas do GTNA**. Não mexi
  nelas: estão em arquivos que a árvore de trabalho já modifica.
- **8 × `EUt can't be explicitly set to 0`** (`.EUt(0)` em `GTNAInfernalCokeRecipes` e no bloco do Cactus Wonder de
  `GTNAMachineRecipes`: `infernal_coke_from_coal`, `infernal_coke_block_from_coal_block`, `infernal_charcoal_from_logs`,
  `cactus_wonder_steam_from_charcoal|coal|coal_block`, `cactus_wonder_superheated_from_coke`,
  `cactus_wonder_supercritical_from_coke_block`). No `GTRecipeBuilder` do GTCEu 7.5.3 o erro só é logado e a receita fica sem
  energia, que é a intenção; **basta tirar o `.EUt(0)` para o log ficar limpo**.
- **6 × `Tried to set output item stack that doesn't exist`**: `outputItems(dust, material)` devolve vazio e **descarta o output** em
  `redstone_front_pro` (itérbio), `almandine_front_pro` (itérbio), `monazite_front_pro` (érbio), `grossular_front_pro` (tálio) e
  `stellar_superheavy_synthesis` (rênio 2048 e háfnio 1024). Esses materiais não têm pó no GTCEu 7.5.3 sem uma flag
  extra; **decidir se o GTNA gera os pós ou remove os outputs**. Não afeta Start a LV.
- Pendente: nenhum teste automatizado falha por isso, porque os GameTests não leem o log do GTCEu. Sugestão (não feita): um teste
  que falhe quando o log de registro trouxer ERROR do GTCEu atribuído a receitas `gtna:`/`gtceu:` do addon. Sem commit/push.

### G-0150 (2026-10-04) — Nós de HV do GTIA e `requires_mods` no motor

- **Motor (GTNA):** novo campo opcional `requires_mods` (lista de ids de mod) em cada nó. Se algum mod faltar, o
  `KnowledgeLoader` pula o nó sem erro e conta em "skipped for missing mods". Existe para o pack trazer nós de mods
  opcionais; **não faça outro nó depender dele**, pois o dependente seria descartado como pré-requisito ausente.
  Teste `research_nodes_for_missing_mods_are_skipped_not_broken`; documentado em `research-engine` (PT e EN).
- **Conteúdo (GTIACore):** 12 nós de HV (tier 4), total 29. Três bloqueiam receitas da EBF: `gtia:hv/nichrome`
  (`blast_nichrome` e `_gas`) e `gtia:hv/titanium` (`blast_titanium` e `_gas`); os demais são marcos com flag:
  hull, Vacuum Freezer, Implosion Compressor, Distillation Tower, Cracker, Large Chemical Reactor, PTFE, Cleanroom,
  Plastic Circuit Board e Rocket T1. **Rascunho meu, não aprovado pelo autor:** é fácil vetar um nó, é só apagar o
  JSON. O primeiro gate de HV segue a cadeia natural de coils (Kanthal chega a 2700 K para o Nichrome).
- `gtia:hv/rocket_t1` usa `requires_mods: ["ad_astra"]`. **O Ad Astra não existe no ambiente mínimo de
  desenvolvimento do core**, então o GameTest o pula ("Loaded 28 research nodes (29 files, 1 skipped for missing
  mods, 0 problems)") e o id `ad_astra:tier_1_rocket` **não foi verificado por automação**; vem das quests do GTO e
  do GTL. No pack o nó carrega (29).
- O jogador automático do core percorreu os 28 nós carregados sem falhas (**17/17**); nesse caminho ele achou o
  problema do Ad Astra antes de qualquer teste manual. GTNA: **152/152** (`runGameTestServer` isolado).
- `runData` do core gerou as 29 chaves de nome; `spotlessCheck build` e `qa_static` passaram dos dois lados.
- Jars instalados na Prism (backup dos anteriores em `/home/raishxn/MineProjects/GTIA-arquivo/prism-jars-before-
  hv-nodes-2026-10-04/`): GTNA `326e9af569e1198b2bdd823f86ffbd0e2579b51c2de6ab4f35ed8e432ed80b42`; GTIACore `df132c3af6df3e70ec1f7ef0a40b2baaf92a15f19a6f2bf8ff73d555ccfdb784`.
- **Pendente: teste humano** só do que a automação não vê: o desenho da árvore com 3 blocos de tier e a nova
  quantidade de nós (a tela de 29 nós pode precisar de ajuste de espaçamento) e o nó do foguete com o Ad Astra real.
  Sem commit/push.

### G-0149 (2026-10-04) — Testes automáticos da pesquisa e correção do gate por time

- **Pedido do autor:** automatizar por GameTest o que vinha sendo testado à mão, para não abrir o pack a cada
  verificação básica.
- **Bug real encontrado pela automação:** o `@Inject` em `RecipeHelper.checkConditions` passava o UUID do **dono da
  máquina** como escopo, mas o progresso é guardado pelo **time FTB efetivo**. Em time, a máquina de um membro
  continuava bloqueada mesmo com o nó liberado para o time. Solo nunca aparece (escopo = jogador), então os testes
  manuais de G-0142 a G-0148 não podiam pegar. Corrigido: o mixin converte o dono com `KnowledgeScope.of`.
  **Isto afeta jogo real em time; o jar anterior da Prism tem o bug.**
- Novos testes (todos em `runGameTestServer`, **151/151**; o de time só roda com FTB Teams carregado):
  - `research_teams_share_progress_and_machine_gates` (`FTBResearchTeamTests`): escopo único no time, desbloqueio
    por um membro vale para o outro, máquina do membro roda a receita, máquina de estranho continua bloqueada,
    sair e voltar do time revoga e restaura.
  - Comandos `/gtna research list|info|unlock|reset` executados pelo despachante real: permissão nível 2,
    pré-requisitos, `force` com a cadeia inteira, `reset` contando o que removeu.
  - `KnowledgePlaythrough` (código principal, reutilizável): um jogador roteirizado percorre a árvore carregada;
    confere que um nó não libera fora de ordem, que libera em ordem, e que cada receita gateada fica bloqueada
    antes e liberada depois. 3 testes com árvores sintéticas, inclusive uma quebrada de propósito.
  - **No GTIACore**, `shippedResearchTreePlaysThroughFromStartToEnd` roda esse jogador nos 17 nós reais
    (**17/17** com o jar de desenvolvimento do GTNA atual).
  - `ClientMixinContractTest` (unitário, ASM): os mixins de cliente não rodam em GameTest, então ele lê o bytecode
    do GTCEu e do LDLib e confere que `GTRecipeWidget.setRecipeWidget` ainda lê `GTRecipe.conditions` exatamente
    2 vezes, que o `@Redirect` declara `ordinal = 1`, que `getDisplayHeight` e `GTEmiRecipe.getId` existem e que
    os dois mixins estão na lista `client`. Verificado quebrando o ordinal de propósito: o teste falhou, e voltou a
    passar ao restaurar.
- **Continua sem automação:** a aparência da tela (K), a linha no EMI e a legibilidade; o servidor dedicado com
  rede real; e `/reload` com a tela aberta.
- Gate: `spotlessCheck compileJava runUnitTests` OK, `runData` `written: 0`, `runGameTestServer` isolado 151/151.
  Jar instalado na Prism (backup em `/home/raishxn/MineProjects/GTIA-arquivo/prism-jars-before-team-fix-2026-
  10-04/`): GTNA SHA-256 `90919fbe8f15f02454b4a8bc0fc506e803a3d24b7c1491cbfe3c45517e8c5fdb`. Sem commit/push.

### G-0148 (2026-10-04) — Nós de MV da árvore do GTIA (lado GTIACore)

- Sem mudança de Java no GTNA nesta entrada. O GTIACore ganhou 8 nós de MV (total 17); ver `GTIACore/docs/QA.md`
  e a seção 11 do plano de pesquisa no workspace GTIA. Dois bloqueiam receitas da EBF (Kanthal e boule de silício).
- O GameTest do core carregou os 17 nós pelo motor do GTNA ("Loaded 17 research nodes (17 files, 0 problems)") e
  conferiu todos os ids. Foi preciso refazer o jar de desenvolvimento do GTNA (`./gradlew jar`) para o core usar o
  motor atual. Jar do GTIACore instalado na Prism, SHA-256 `1edef43e4470417665456f43c032670d0dc4e13006e681ddf409e5ccfeaaf7df`.
- **Pendente: teste humano dos nós de MV** (cadeia, e as receitas de Kanthal e `silicon_boule` bloqueadas na EBF).
  Sem commit/push.

### G-0147 (2026-10-04) — Teste humano de G-0146 aprovado e documentação do motor

- **Teste humano aprovado pelo autor ("funcionando perfeitamente"):** a linha de pesquisa no EMI está legível, sem
  sobrepor o ícone da máquina, com a moldura do painel correta e a cor mudando ao liberar o nó. Isso confirma na
  prática o `@Redirect` de `GTRecipeWidgetMixin` e o `ModularEmiRecipeMixin`.
- Estado verificado em jogo até aqui (G-0142 a G-0146): cadeia de gatilhos por item, gate do Aluminium na EBF,
  tarefa de quest, tela da árvore (tecla K) e linha no EMI. **Ainda sem teste humano:** servidor dedicado, dois
  jogadores no mesmo time e `/reload` com a tela aberta.
- Documentação pública: `docs/gameplay/systems/research-engine` em PT, EN e ES (o ES é resumido e aponta para o
  EN), item no `nav` do `mkdocs.yml`, e um datapack de exemplo em `examples/datapack` (JSON validado).
  `mkdocs` não está instalado aqui, então o build do site **não** foi rodado.
- Sem mudança de Java nesta entrada. Sem commit/push.

### G-0146 (2026-10-04) — Eye of Harmony: conteúdo físico e primeira parada para QA humano

- **Retorno humano posterior:** autor confirmou “texturas dos blocos OK e a do item também”
  e enviou captura da galeria no cliente de desenvolvimento. Aparência das texturas aprovada;
  animações ao longo do tempo, seis faces planetárias, drops, idiomas e JEI/EMI não foram
  confirmados individualmente. Roteiro e roadmap atualizados com esse escopo. Só documentação
  alterada neste retorno; não houve nova rodada de testes nem publicação.
- Autor autorizou prosseguir até a primeira etapa que exige teste humano. EOH-01 ganhou
  **27 campos (3 famílias × 9 tiers), três casings, três Planet Blocks e um Astral Array**.
  `GTNAEyeOfHarmonyContent` registra o lote com blocos/BlockItems próprios, identidade de
  família/tier/dimensão, arrays ordenados, tooltips de função/origem e nomes EN/PT.
- Tiers internos 0–8, apresentação 1–9 e nomes Crude..Gallifreyan seguem a revisão congelada
  GT5U. Planetas iniciais referenciam minecraft:overworld/the_nether/the_end. Casings espacial,
  temporal e fronteira têm IDs novos; IDs/estrutura/operação anteriores permanecem existentes.
- Importados **51 PNGs e 32 mcmetas** sem alteração dos bytes: 39 PNGs Modernity (iguais
  ao checkout local) e 12 GT5U. Ausências Modernity usam os originais, incluindo estabilização
  0–7 e Astral Array. Manifest guarda revisão/caminho/licença/SHA por arquivo; avisos e textos
  de licença estão no projeto/jar. Planet Block: identidade/faces verificadas no GTNEIOrePlugin.
  Dois overlays estão preservados como assets, ainda sem aplicação ao renderer. Sem CTM importado.
- Datagen produz blockstates, modelos de blocos/itens, loot e tags. Corrigida a integração de
  nomes EN com o GTNALangProvider próprio, que sobrescrevia os nomes do Registrate no datagen.
  Auditoria independente verificou 33 modelos/texturas e 34 nomes EN/PT, hashes e animações.
- Dois GameTests novos verificam 27 registros únicos, família/tier, colocação, mining tags,
  drops do próprio tier, modelos/texturas/traduções, dimensões planetárias e Astral Array.
- Validação final: gate obrigatório completo passou — Spotless/compile, 28 unitários,
  **146/146 GameTests**, datagen `written: 0`; jar local auditado com as 51 texturas,
  32 animações, três licenças e nomes EN presentes. Log: `/tmp/gtna-eoh-content-verified.log`.
  Tentativa inicial do baseline encontrou classe ausente durante recompilação concorrente;
  a rodada posterior carregou os registros normalmente e os testes foram executados de fato.
- Primeira necessidade humana: aparência/animação dos 27 tiers, casings, seis faces dos planetas,
  ícones, idiomas e exposição creative/JEI no cliente. Roteiro:
  `docs/roadmap/eye-of-harmony-content-manual-test.md`. Cliente ainda não aberto neste marco.
- Roadmap atualizado: EOH-01 aguardando QA visual; fabricação/materiais (EOH-02), estrutura
  (EOH-03), operação fiel (EOH-04) e paralelos/renderer/migração continuam futuros. Tooltips
  informam que os componentes ainda não têm fabricação nem integração operacional.
  Nenhuma publicação/commit/push e nenhuma alegação de autorização individual pendente concedida.

### G-0146 (2026-10-04) — Legibilidade da linha de pesquisa no EMI

- **O teste humano de G-0145 passou:** a linha apareceu no EMI. Print (`Prints for analise/`): ficou difícil de
  ler. Três defeitos visíveis: (1) "Requires research: Aluminium Smelting" passava por baixo do ícone da máquina,
  cortando o fim do nome; (2) a linha extra empurrava "Temp" e "Coil" para fora da moldura; (3) tinha a mesma
  cor das outras linhas.
- **Texto e cor:** nova chave `gtna.research.viewer` ("Research: %s"), com código de cor dentro do texto (vermelho
  enquanto bloqueado, verde depois de liberado), porque o `GTRecipeWidget` imprime `getTooltips().getString()` e
  perde estilos de componente. O nome é limitado a 24 caracteres com reticências para não passar do ícone.
  A mensagem do controller (servidor) continua "Requires research: <nome completo>".
- **Altura do painel:** o EMI guarda o tamanho da receita ao registrá-la, antes de o cliente conhecer o grafo,
  então a altura não pode ser fixada na criação. Novo `ModularEmiRecipeMixin` (alvo por texto, pois o EMI não é
  dependência de compilação) soma 10 px por nó que bloqueia a receita ao retornar `getDisplayHeight`, só para a
  classe `GTEmiRecipe` (id obtido por reflexão de `EmiRecipe#getId`). Registrado na lista `client` do
  `gtna.mixins.json`.
- **Limites conhecidos:** o JEI continua sem a altura extra (o jogador usa EMI); o recálculo depende de o EMI
  consultar `getDisplayHeight` ao exibir (a leitura do bytecode do LDLib mostra que é um getter simples), o que
  só o cliente confirma. Se o alvo do mixin falhar, o log traz `InvalidInjectionException`.
- Validação: `spotlessCheck compileJava runUnitTests` OK; `runData` OK (`written: 0` na segunda execução);
  `runGameTestServer` isolado **146/146** (inclui testes da árvore de trabalho do autor).
- Jar instalado (backup do anterior em `/home/raishxn/MineProjects/GTIA-arquivo/prism-jars-before-emi-
  readability-2026-10-04/`): GTNA SHA-256 `5e4db4631207a227579dc184e97e1abaadf6746b0605ddf32615b54d502ecea7`.
- **Pendente: teste humano** (leitura, sobreposição, altura do painel). Sem commit/push.

### G-0145 (2026-10-04) — Roadmap detalhado do Eye of Harmony: conteúdo primeiro

- Pedido do autor: aproximar ao máximo do GTNH e começar pelos blocos, itens e texturas.
- Criado `docs/roadmap/eye-of-harmony-implementation-roadmap.md`: EOH-00..07, inventário,
  dependências, tarefas/aceitação, cadeia de fabricação, tiers/hatches/estrutura, ciclo, catálogo,
  Astral Arrays, viewer/renderer, migração e validação. EOH-01 é a primeira implementação.
- `docs/roadmap/eye-of-harmony-assets.json` mapeia 27 candidatos (3 famílias × 9 tiers),
  IDs/destinos propostos, origem, dimensões, animação e SHA-256 dos arquivos locais encontrados.
  Modernity local existe: EM_DIM 0–8 e EM_FIELD 0–8 + STABILITY_CASING 8 (**19/27**).
  STABILITY_CASING 0–7 e Astral Array não encontrados no caminho correspondente do Modernity;
  arquivos originais encontrados na árvore do GT5U. Direitos ainda devem ser conferidos por asset.
- Fonte dos ícones conferida nas classes GTNH SpacetimeCompressionFieldCasing,
  TimeAccelerationFieldCasing e StabilisationFieldCasing, na revisão congelada de G-0144.
  Recipe loader ResearchStationAssemblyLine foi localizado; mapa de fabricação completo pendente.
- Estudo anterior aponta para a nova ordem, e roadmaps públicos PT/EN/ES vinculam o plano.
- Somente documentação/inventário; **nenhum bloco/item/textura importado ou comportamento alterado**.
  Escopo desta entrega documental; implementação dos marcos permanece planejada.
- Validação: manifest com 27 IDs únicos, 19 PNGs e 19 animações JSON válidos. Gate completo
  passou: Spotless, compilação, 28 unitários, **144/144 GameTests** do checkout compartilhado,
  datagen `written: 0` (`/tmp/gtna-eoh-roadmap-gate.log`). Não valida o port ainda planejado.
  Revisão de formatação após o checkpoint passou. Sem publicação.

### G-0145 (2026-10-04) — Correção: "Requires research" não aparecia no EMI

- **Relato do autor** após G-0144: a tela da árvore funciona (tecla K), mas o EMI não mostra a linha na receita do
  Aluminium. O log do cliente confirmou que o servidor carregou os 9 nós; o problema estava na injeção.
- **Causa (duas, por leitura do código do GTCEu 7.5.3):** (1) a lista de receitas do EMI vem de
  `GTRecipeType.getRecipesInCategory`, montada em `RecipeManagerMixin` no `RecipeManager.apply` do **servidor**;
  em mundo local o EMI usa os objetos do servidor integrado, não a cópia que o cliente decodifica da rede, então
  injetar na cópia do cliente não chegava ao EMI. (2) `GTRecipe.conditions` de receitas lidas do JSON costuma ser
  `List.of()` (imutável) e o `add` do injetor falhava em silêncio.
- **Correção:** removidos `ResearchGateInjector` e o gancho `RecipesUpdatedEvent`. Em vez de mudar a receita,
  `GTRecipeWidgetMixin` ganhou um `@Redirect` na leitura de `GTRecipe.conditions` do laço que imprime as condições
  em `GTRecipeWidget.setRecipeWidget` (**ordinal 1**; o ordinal 0 alimenta o template da UI), que devolve a lista
  da receita mais uma linha por nó que a bloqueia (`ResearchGateCondition.withResearch`, consultando
  `ClientKnowledge.gatesFor(id da receita)`). Não altera receita nenhuma, então vale para objetos do servidor.
- **Verificação do alvo do mixin:** `javap -c` no `gtceu-1.20.1-7.5.3.jar` da Prism mostra exatamente 2
  `getfield GTRecipe.conditions` em `setRecipeWidget` (offsets 79 e 529). Mixins de cliente **não rodam nos
  GameTests**, então a aplicação real do redirect só se confirma no cliente. `defaultRequire = 1`: se o alvo
  falhasse, o cliente não carregaria (visível no log como `InvalidInjectionException`).
- Teste novo `research_viewer_lists_research_without_touching_the_recipe` (substitui o do injetor): a lista exibida
  ganha a linha, a receita fica intacta, o texto muda quando o nó é liberado e receitas sem gate não mudam.
- Validação: `spotlessCheck compileJava runUnitTests` OK; `runGameTestServer` isolado com FTB Quests **144/144**;
  `runData` OK (`written: 0`). **Duas execuções anteriores do `runData` falharam** (render dinâmico não registrado
  e `ClassMetadataNotFoundException` de `PlanningOrigin`, classe que existe no `build`) e a terceira passou sem
  mudança de código; trato como interferência transitória do ambiente (há outros processos Gradle/IDE ativos),
  mas **não identifiquei a causa**.
- Jar instalado na Prism (backup do anterior em `/home/raishxn/MineProjects/GTIA-arquivo/prism-jars-before-emi-
  fix-2026-10-04/`): GTNA SHA-256 `a616e687ad1d5ce77faf2082aacdbc0100e7a0d9f53b9da11d94f2dd5dea56ab`.
- **Pendente: teste humano no cliente** (a linha no EMI). Sem commit/push.

### G-0144 (2026-10-04) — Estudo do Eye of Harmony original do GTNH

- Pedido do autor: estudar fidelidade/viabilidade e propor caminho, sem iniciar o port nesta etapa.
- Estudo em `docs/roadmap/eye-of-harmony-gtnh-study.md`, com fontes fixadas no GT5-Unofficial
  `a3e1e11241a814c9fa0dd0973d5699548428f689`. Wiki respondeu 403; código, catálogo e tooltip
  oficiais foram lidos. Fonte consultada é master, não foi identificada como release de pack.
- Estrutura GTNA/original: grade 33³ e contagens de peças correspondentes; transformação
  geométrica completa não validada. As 168 posições de aceleração temporal são Bridge Casings
  no GTNA. Faltam campos com tiers, seletor planetário, chance/rendimento/excesso/pity, retorno
  wireless e Astral Arrays; atuais três receitas e custos fixos são simplificação.
- Risco verificado por leitura: modifier atual cobra EU/gases antes de check/setup do GTCEu;
  tentativa posteriormente rejeitada pode cobrar sem iniciar. Reproduzir e mover débito para
  início confirmado na implementação; não afirmar que este risco foi corrigido pelo G-0142.
- Proposta: compatibilidade/migração → ciclo completo Overworld → campos/estrutura → chance,
  retorno e saída persistida → catálogo adaptado → paralelos/visual. Modelo legado/fiel é
  proposta, não config implementada ou decisão aprovada. A rede Nexus exige tratar capacidade,
  perda e créditos parciais; não herdar suposição de rede sem limite do GTNH.
- Nenhum código de jogo/asset alterado nesta pesquisa, sem publicação. Gate completo passou:
  Spotless, compilação, 28 unitários, **142/142 GameTests** do checkout compartilhado e
  datagen `written: 0` (`/tmp/gtna-eoh-study-gate.log`). Esses testes validam o estado atual,
  não o port proposto. Revisão final de formatação após registrar o resultado também passou.

### G-0144 (2026-10-04) — Tela da árvore de pesquisa (P5) e tecla K

- Nova `KnowledgeTreeScreen` (tela vanilla desenhada com `GuiGraphics`, sem LDLib; a decisão D-209 dizia
  "LDLib", mas o resultado para o jogador é o mesmo e o controle de desenho é total). Aberta com a tecla
  **K** (rebindável em Controles, categoria "GregTech Nexus Addon"). Colunas por tier e por profundidade,
  linhas entre nó e pré-requisitos (verde = feito, amarelo = próximo, cinza = bloqueado), borda do nó por
  estado (liberado, disponível, bloqueado), arrastar para mover, tooltip com tier, estado, "Obtain: <item>",
  "Requires: <nós>" e quantas receitas o nó libera. Se existir a chave `<nome do nó>.desc`, mostra a descrição.
  Atualiza sozinha enquanto aberta, porque lê o `ClientKnowledge` que o servidor sincroniza (G-0143).
- `KnowledgeTreeLayout` (código comum, sem desenho): posiciona os nós e é coberto por 2 GameTests, inclusive
  ciclos e pré-requisitos desconhecidos. `NodeView` ganhou o campo `icon` (pacote e snapshot atualizados).
- Textos EN/PT no `GTNALangProvider` e `pt_br.json`; datagen estável (`written: 0`).
- Gate: `spotlessCheck compileJava runUnitTests runData` OK; `runGameTestServer` isolado com FTB Quests,
  **144/144**. Jar instalado na Prism (backup em `/home/raishxn/MineProjects/GTIA-arquivo/prism-jars-before-
  tree-screen-2026-10-04/`): GTNA SHA-256 `1b297b1a746040f385eddff795490fa032e4bc0b62793cbdcceafb5f7cad539b`.
- **Não verificado em jogo:** tudo da tela (nenhum teste automatizado abre a tela): abertura com K, desenho,
  arrasto, tooltip, cores, atualização ao liberar um nó, e o texto cortado em resoluções pequenas.
- Fora do escopo desta entrega: categoria própria no EMI listando nós, zoom, busca e filtro por tier.
- Sem commit/push.

### G-0143 (2026-10-04) — "Requires research" visível no EMI/JEI e sincronização com o cliente

- Pedido do autor após o teste em jogo de G-0142 (prints em `GTIA Private and Tests/Prints for analise/`): a
  receita bloqueada precisa dizer isso no recipe viewer. **O teste humano de G-0142 passou** (cadeia, gate da
  EBF e tarefa de quest); o único defeito visto foi a mensagem do controller cortada pela largura do painel.
- Novo `SKnowledgeSync` (S2C): o grafo (id, tier, pré-requisitos, item gatilho, receitas gateadas) mais os
  nós liberados do escopo do jogador. Enviado no login e a cada `/reload` (`OnDatapackSyncEvent`) e quando o
  progresso do escopo muda (`KnowledgeService.grant/reset` chamam `KnowledgeSync.syncScope`, que atinge o time
  inteiro). O cliente guarda tudo em `ClientKnowledge` (dados puros, em código comum).
- `ResearchGateCondition` (tipo `gtna:research_gate`): o `KnowledgeClientHandler` a adiciona às receitas do
  cliente (`ResearchGateInjector`), e o `GTRecipeWidget` do GTCEu já imprime as condições como linhas de texto.
  A linha diz "Requires research: <nó>" ou, se já liberado, "Research unlocked: <nó>". O injetor remove o que
  adicionou antes de reaplicar, então reload ou grafo menor não deixa cadeado velho. Avaliada num servidor, a
  condição concorda com o mixin.
- **Decisão técnica:** a condição só entra na cópia do cliente, onde o GTCEu decodifica receitas da rede em
  `ArrayList` mutável; no servidor o bloqueio continua por id no `RecipeHelperMixin`.
- Mensagem do controller sem o prefixo "Condition Fails:" (que o GTCEu já acrescentava): agora só
  "Requires research: <nó>", para caber no painel.
- Testes novos: sincronização (ida e volta do pacote), injetor (mostra, remove, não empilha) e a condição
  contra a regra do servidor. Gate: `spotlessCheck compileJava runUnitTests runData` OK (`written: 0`) e
  `runGameTestServer` isolado com FTB Quests, **142/142**.
- Jar instalado na Prism (backup do anterior em `/home/raishxn/MineProjects/GTIA-arquivo/prism-jars-before-emi-
  research-2026-10-04/`): GTNA SHA-256 `5f00ffa912e70919530faff16cfbee74fb524601ca149da42e903cae48561993`.
- **Não verificado em jogo:** se o EMI mostra a linha (depende de o EMI usar a mesma instância de receita que
  o cliente recebeu e de a ordem entre o pacote e a lista de receitas), a atualização do "unlocked" sem reabrir
  o EMI, e servidor dedicado com dois jogadores. Sem commit/push.

### G-0142 (2026-10-04) — Relato de bugs do jogador em GTNA 0.5.1 / GTCEu 7.5.3

- `EyeOfHarmonyMachine.addDisplayText`: saldo wireless e energia de partida passam `BigInteger`
  ao `FormattingUtil.formatNumbers`, preservando precisão de 128 bits. A versão anterior passava
  `String`, que o `DecimalFormat` rejeita. Regressão cobre display/UI com zero, dono ausente e
  saldo/partida maiores que `Long.MAX_VALUE`; o estado formado é isolado no teste, sem montar estrutura.
- Corrigidas **31 entradas** em `GTNAItemRecipes`, `GTNAMachineRecipes`, `GTNABlockRecipes`:
  21 blocos viraram itens explícitos; 10 IDs textuais de ExtendedAE/AE2 viraram itens do registro.
  A API local do GTCEu 7.5.3 foi conferida: `inputItems(Object)` rejeita `Block` e `String` e
  retorna o builder sem adicionar ingrediente. Os quatro crafts dependentes de ExtendedAE só
  são gerados se todos os seus ingredientes externos estiverem registrados.
- Lava Maker e Stone Superheater tinham entradas idênticas e o lookup descartava uma receita.
  Adicionados circuitos 1 e 2 respectivamente. Regressão confere as entradas recuperadas, suas
  quantidades e a presença de ambas as receitas; log final sem colisão entre esses dois controllers.
- `GTNABlockRecipes`: Basalt/Deepslate usam `notConsumable` para Blue Ice/Magma Block.
  `chancedInput(..., 0, 0)` era inválido e produzia dois rastros de `GTNAGTAddon.addRecipes` no
  ambiente local, além de omitir catalisadores. Regressão verifica presença e chance zero correta.
  Sem o `latest.log` completo do jogador, não se afirma que seus rastros têm a mesma origem.
- Refmap no config mudou de `gtna.refmap.json` para `mixins.gtna.refmap.json`, o nome produzido
  pelo build. Jar reobfuscado inspecionado: o recurso apontado existe e seu JSON é legível.
  Aviso de refmap ainda ocorre nos runs de desenvolvimento por diretórios; não equivale a jar ausente.
- Receitas `infinite_input_*`/`output_boost_*` usam condição Forge
  `gtna:restricted_items_enabled`. No default `modDifficulty=NORMAL`, retorno null e mensagem
  de descarte são esperados. Para habilitar: JOURNEY, `selfRestraint=false`,
  `restrictedItems.disableUsage=false` e `restrictedItems.disableRecipes=false`.
- Validação final: `spotlessApply spotlessCheck compileJava runUnitTests runGameTestServer runData jar
  --offline`, **BUILD SUCCESSFUL**, 28 unitários, **139/139 GameTests**, relatório XML dos dois novos
  testes sem falhas, datagen `written: 0`. Log: `/tmp/gtna-player-report-validated-gate.log`.
  Sem os erros `Input item is not one of`, `Cannot format given Object as a Number` ou rastros
  `GTNAGTAddon.addRecipes` nesse gate. O gate combinado não carrega as integrações FTB opcionais.
- Pendências: abertura visual/clique real no Eye of Harmony, fabricação no pack do jogador,
  habilitação dos hatches via config e teste do jar em produção. Há avisos de limite de entradas
  no Ultimate Pattern Buffer (10 entradas / tipo Assembler com máximo declarado 9) e Nexus
  Molecular Forge (21 / Assembly Line com máximo 16); presença dos ingredientes foi testada,
  fabricação dessas duas receitas ainda precisa de revisão. Outros avisos de materiais/receitas
  antigos seguem fora deste reparo. Tudo local; sem commit, push ou publicação.

### G-0142 (2026-10-04) — Nós do GTIA (P6) e jar de teste instalado na Prism

- O GTIACore ganhou 9 nós em `data/gtia/gtna_research/` (5 de Steam e 4 de LV, ver `GTIA Private and
  Tests/docs/PLANO_TECNICO_ARVORE_DE_PESQUISA_v01.md`). **Só um gate real:** `gtia:lv/aluminium` bloqueia
  `gtceu:electric_blast_furnace/blast_aluminium` e `..._gas`; os outros 8 só concedem flags.
- Cada nó libera pelo **item de prontidão** do tier (por exemplo, o controller da EBF e o pó de alumínio),
  nunca por um item que dependa do próprio gate.
- GameTests do core (`ResearchContentGameTests`, 3 casos): ids de item e receita existem, grafo sem ciclos e
  sem pré-requisito em tier posterior, e o GTNA carrega todos os nós. **16/16** com
  `-PgtnaDevJar=.../build/devlibs/gtna-0.5.1.jar`; log "Loaded 9 research nodes (9 files, 0 problems)".
- Jars construídos e instalados na Prism de teste (backup dos anteriores em
  `/home/raishxn/MineProjects/GTIA-arquivo/prism-jars-before-research-2026-10-04/`):
  GTNA `gtna-0.5.1.jar` SHA-256 `07546614da2855c0aa62f92233d01a1921dec7982d861a42b5bf870b47bdb933`
  (inclui as mudanças em andamento do autor na árvore); GTIACore `gtia-0.1.0.jar` SHA-256
  `2da8c9cdda04bb1c893be4ab77a9fe6dd5eb37e058076fd48a498d7fafc5f94c`.
- **Pendente: teste humano no cliente** (lista no fim da conversa): mensagem e som de liberação, ordem da
  cadeia, o bloqueio do Aluminium na EBF, a tarefa no FTB Quests e o escopo de time.

### G-0141 (2026-10-04) — Motor de pesquisa (P3 e P4): gatilho por item e tarefa FTB Quests

- **P4, gatilho `obtain_item`** (`KnowledgeTriggers`): o jogador "obteve" o item se ele está no inventário
  (itens, armadura, mão secundária) ou acabou de ser fabricado, fundido ou coletado. Varredura a cada 20
  ticks mais os eventos `ItemCrafted/ItemPickup/ItemSmelted`; repete até estabilizar, então uma cadeia cujos
  itens já estão todos no inventário libera numa passada. Só libera se os pré-requisitos estiverem
  satisfeitos. Vale para o escopo (um membro do time basta). Mensagem `gtna.research.unlocked` e som.
  Custo quando não há gatilhos de item: um teste de `isEmpty` por varredura.
- **Limite conhecido:** itens só em baús, máquinas ou na rede AE2 não contam até alguém os pegar; Curios e
  mochilas (Sophisticated Backpacks) não são varridos.
- **P3, tarefa FTB Quests `gtna:research_node`** (`ResearchNodeTask`, `GTNAQuestTypes`): `AbstractBooleanTask`
  com o campo `node`; conclui sozinha (checagem a cada 20 ticks e no login) quando o escopo tem o nó.
  Registrada no `commonSetup` só se `ftbquests` estiver carregado. `build.gradle` ganhou
  `modCompileOnly` e `modRuntimeOnly` de `ftb-quests-forge-289412:8078538` (mesma versão do pack, 2001.4.22).
- **Armadilha de teste:** `runData` no mesmo comando desliga as dependências de execução
  (`isDataRun`), inclusive FTB Teams e FTB Quests. Os GameTests do FTB só rodam de verdade com
  `./gradlew runGameTestServer` sozinho.
- Testes novos: `research_item_trigger_unlocks_chains_only_when_prerequisites_are_met` e
  `research_quest_task_completes_from_the_node_state` (corpo em `FTBResearchTaskTests`, só com FTB Quests).
- Validação: gate combinado BUILD SUCCESSFUL (137/137); `runGameTestServer` isolado com FTB Quests
  carregado, **137/137**, relatório JUnit conferido; datagen estável.
- **Não verificado em jogo:** a tarefa no livro de quests (ícone, título, conclusão automática), a mensagem
  e o som, e a detecção com um item fabricado de verdade.
- Sem commit/push; revisão do autor pendente.

### G-0140 (2026-10-04) — Motor de pesquisa (P2): nó bloqueia receita de máquina

- `RecipeHelperMixin` ganhou `gtna$checkResearchGates` no HEAD de `RecipeHelper.checkConditions`
  (`remap = false`, mesmo padrão dos injetores já existentes no arquivo). Receita cujo id consta num
  `recipe_condition` só roda se o dono da máquina (escopo = time FTB efetivo ou jogador) tiver o nó.
  Grafo vazio, receita sem gate e cliente passam sem custo (um `HashMap.get`).
- **Desvio do plano, de propósito:** o plano previa uma `RecipeCondition` anexada às receitas. Não foi feito
  porque as receitas lidas do JSON podem ter listas de condições imutáveis (`List.of()` do codec) e o campo é
  `final`; mutar exigiria reflexão e dependeria da ordem do reload. O gancho por id cobre também receitas de
  KubeJS e cópias modificadas (`copy` preserva o `id`). **Custo:** o JEI/EMI ainda não mostra o bloqueio na
  receita; isso fica para a fase da interface (P5).
- Máquina sem dono bloqueia receita com gate (sem escopo). Outras receitas da mesma máquina continuam;
  a busca do GTCEu segue para o próximo candidato quando um falha.
- Mensagem: `gtceu.recipe_logic.condition_fails: gtna.research.requires` ("Requires research: <nó>"),
  EN no `GTNALangProvider` e PT em `pt_br.json`. `KnowledgeService.firstMissingGate` concentra a regra.
- GameTest `research_gate_blocks_the_recipe_until_the_owner_unlocks_the_node`, com um Macerator LV real:
  receita livre antes do grafo, bloqueada sem o nó, bloqueada sem dono, outra receita livre, liberada após
  `unlock`, e dono diferente não herda.
- Gate `spotlessCheck compileJava runUnitTests runGameTestServer runData --offline`: BUILD SUCCESSFUL,
  **135/135 GameTests** (134 + 1) e datagen `written: 0` na segunda execução (a primeira regenerou o `en_us.json`).
- **Não verificado:** jogo real com uma receita de produção gateada, máquinas multiblock (o hook vale para
  qualquer `RecipeLogic`, mas só o Macerator foi exercitado), e o comportamento de uma receita **em andamento**
  quando o nó é resetado (`handleRecipeWorking` reavalia as condições a cada tick, então deve interromper).
- **Fora de alcance por desenho:** crafting de bancada, receitas do AE2 e padrões não passam por
  `checkConditions`; a planta como ingrediente (P8) é a rota para isso.
- Sem commit/push; revisão do autor pendente.

### G-0139 (2026-10-04) — Motor de pesquisa (P1): nós em JSON, escopo por time e comandos

- Novo pacote `com.raishxn.gtna.research` (12 classes). **Escrito do zero** a partir do plano do GTIA
  (`GTIA Private and Tests/docs/PLANO_TECNICO_ARVORE_DE_PESQUISA_v01.md`); o GTO foi só referência de
  conceito (bytecode lido apenas por assinatura, sem copiar código, nomes de classe, tags ou texturas).
  Vocabulário próprio: `KnowledgeNode/Graph/Data/Service`, gatilho `obtain_item`, sem pontos, tags ou CWU.
- Nós: `data/<ns>/gtna_research/**.json` com `tier`, `icon`, `prerequisites`, `trigger`
  (`obtain_item` | `manual`), `grants` (`recipe_condition` | `flag`) e `quest`. O id é o caminho do arquivo.
  `KnowledgeLoader` recarrega com os dados; nós inválidos são logados e descartados.
- **Campos opcionais estritos** (`StrictFields`): o `optionalFieldOf` do DFU 6 engole erro e usa o padrão,
  o que deixaria um nó com grant inválido carregar **sem o bloqueio**. Um GameTest cobre o caso.
- `KnowledgeGraph.build` remove nós com pré-requisito ausente, ciclos (inclui auto-referência), tier < 1 e
  dependentes; ordena por tier e dependência; indexa gates por receita e gatilhos por item.
- `KnowledgeData` (SavedData `gtna_knowledge`, `DATA_VERSION` 1) guarda nós por escopo e **preserva ids que
  saíram do pack**. Escopo: `KnowledgeScope` reutiliza `DepositRecorderBehavior.scope` (time FTB efetivo ou
  jogador), a mesma regra das descobertas de fluido.
- `KnowledgeService`: `unlock` (com `force` para a cadeia), `isUnlocked`, `hasFlag`, `recipeAllowed`, `reset`;
  evento `KnowledgeUnlockedEvent` no barramento Forge. Comandos `/gtna research list|info|unlock|reset`
  (nível 2 para alterar progresso). Textos EN no `GTNALangProvider` e PT em `pt_br.json`.
- Ligação: duas linhas em `CommonProxy` (`KnowledgeEvents.register`). Sem nós embutidos no GTNA; sem mudança
  de receitas, itens, máquinas ou assets.
- Testes: `GTNAKnowledgeGameTests` com 7 casos (JSON, rejeição de tipos/ids inválidos, grafo, índices,
  SavedData, serviço com prerequisitos/flags/gates/eventos/isolamento por escopo, escopo vs. fluidos).
  Testes unitários `main()` não servem aqui: o classpath de teste não carrega classes do Minecraft.
- Gate `spotlessCheck compileJava runUnitTests runGameTestServer runData --offline`: BUILD SUCCESSFUL,
  **134/134 GameTests** (127 anteriores + 7), datagen `written: 0`. Observação: `runData` sobrescreve
  `run/logs/latest.log`, então `tools/check_gametest_report.py` só vale logo após `runGameTestServer`.
- **Não verificado:** os comandos em jogo (os GameTests não executam Brigadier), a leitura de nós pelo
  recarregamento real de datapack (o log mostra "Loaded 0 research nodes", pois não há nós), e o escopo com
  time FTB real e dois jogadores.
- **Pendências do plano (P2–P8):** `ResearchNodeCondition` sobre receitas, tarefa FTB Quests, detecção do
  gatilho `obtain_item`, tela LDLib e categoria EMI, nós do GTIA no GTIACore.
- Árvore de trabalho já tinha mudanças do autor; só foram adicionados arquivos novos, duas linhas em
  `CommonProxy`, entradas no `GTNALangProvider`/`pt_br.json` e a regeneração do `en_us.json`.
  Sem commit/push; revisão do autor pendente.

### G-0138 (2026-09-30) — Roadmaps públicos e TODO de GTNA e GTIA

- Atualizado `docs/roadmap/index.md` e equivalentes EN/ES: baseline de desenvolvimento,
  sistemas implementados, 14 tarefas com IDs estáveis, prioridades Agora/Depois/Mais adiante,
  critérios de conclusão e distinção entre conteúdo em validação e propostas em estudo.
  Flux Matrix e cadeia Void deixaram de aparecer como funcionalidades futuras.
- Criado `GTIA Modpack/ROADMAP.md` no projeto irmão: base existente, 21 tarefas por etapa
  Start → Steam → LV, LV → MV → HV, espaço EV/IV e escala industrial. Quests seguem adiadas
  para autoria manual; não foi prometida distribuição reproduzível nem campanha pronta.
- READMEs GTNA, GTIA Modpack e GTIACore apontam para os roadmaps. README do core corrigido
  para Draconium próprio, dependência GTNA e 13 GameTests, conforme código/validações existentes.
- QA documental GTIA: 52 Markdown/catálogo/cópias históricas OK. IDs sequenciais, links locais
  dos roadmaps/READMEs e ausência de caminhos privados verificados. Traduções GTNA com as mesmas
  14 tarefas. Créditos usam link do repositório para funcionar também no site MkDocs.
- Gate GTNA `spotlessCheck compileJava runUnitTests runGameTestServer runData --offline`:
  BUILD SUCCESSFUL, unitários OK, **127/127 GameTests** e datagen `written: 0`.
  Log: `/tmp/gtna-g0138-roadmap-gate.log`. `git diff --check` dos documentos GTNA OK.
- Nenhuma mudança de Java, receitas, assets, configs ou quests nesta etapa. Problemas reportados
  de prospecção Radon/JEI e QA manual continuam no TODO; esta etapa não afirma tê-los resolvido.
- Roadmaps preparados localmente. Sem commit/push/publicação; revisão e testes do autor pendentes.

### G-0137 (2026-09-30) — QA do autor: hatches, tempos, Jade e cliente GTIA

- Electric Void Miner aceita um Accelerate Hatch nas posições de casing e aplica seu percentual
  pelo hook de RecipeLogic já existente, após o overclock, usando tier original da receita.
  Nenhum segundo multiplicador no recipeModifier. Novo GameTest forma a máquina com
  Accelerate + Parallel, confirma >2 operações e duração reduzida sem mudar EU/t do lote.
- O antigo default IV+ =2 era um teto do minerador, não defeito do Parallel Hatch. Default IV+
  agora permite até 1.024, respeitando ajuste do hatch, energia, insumos e saída; EV segue serial.
  Display mostra o menor limite entre config e ajuste do hatch, sem anunciar 1.024 sem hatch.
- Autor escolheu 10 s preciso, 30 s aleatório e 60 s cultivo antes do overclock. Defaults e perfil
  GTIA atualizados; starter/planetas usam baseDuration; novo incubationDuration configurável.
  Configs existentes do dev/Prism atualizadas com backup `/tmp/gtna-g0137-config-backup`.
- Jade aleatório passa a remover a lista GTCEu de resultados potenciais e mostra pool/chances;
  não altera as probabilidades. Chance base permanece 25% EV / 2% ZPM por minério/execução,
  com bônus de overclock. Conferência visual ainda necessária. Config key Jade adicionada após
  teste unitário detectar sua ausência; correção validada antes de iniciar cliente novo.
- Provider en_us agora inclui os cinco World Data Scanners LV–IV; nomes conferidos no jar.
  JEI de fluidos inclui origem da descoberta e upgrade de produção remota: a mesma receita
  marciana serve na Terra com cartão certificado, T2 e energia LuV, sem segundo programa.
- O runClient anterior não carregava GTIACore nem perfil GTIA. Novo lançador
  `tools/run_gtia_client.py --core-dev-jar <jar mapeado>` instala perfil e depósito KubeJS com
  backups persistentes. GTNA pode receber gtiaDevJar só no runtime dev, selecionando GTMThings
  1.6.0 nesse caso. Dev jar do core relaxa apenas integrações de teste, preservando GTNA obrigatório;
  build final do core restaurado com dependências obrigatórias e QA estático aprovado.
- Gate de código anterior à melhoria final de display: 28 unitários / **127/127 GameTests**,
  datagen e assemble. Perfil integrado GTIA + core: **127/127**, incluindo marcadores Marte
  e FTB, sem ausência do core no runtime. Gate final e reabertura registrados abaixo após término.
- Guia manual: `docs/roadmap/void-fluid-manual-test.md`, com extração nativa, registro,
  produção local de Oil e produção remota de Radon. Não foi dispensado certificado por criativo.
- Gate final `/tmp/gtna-g0137-gate-final-2.log`: spotlessCheck/compileJava/runUnitTests/
  runGameTestServer/runData --offline, **28 unitários/127 GameTests**, datagen `written: 0`.
  Teste de aceleração agora inicia o lote real e exige percentual exato uma vez. Uma tentativa
  anterior de datagen abortou por renderer dinâmico não registrado; processo encerrado e gate
  repetido com sucesso, sem alteração nos renderers.
- Perfil final integrado `/tmp/gtna-g0137-integrated-profile-final.log`: **127/127** e dois
  marcadores obrigatórios, com GTIACore 0.1.0 confirmado no runtime. Assemble/spotlessCheck
  finais passaram. GTNA instalado na Prism com backup, SHA-256
  `ee606ece744ef8e6487fbd16eb0c654eef469910b244f70e67f55320ebba21a3`.
  Cliente novo iniciado pelo lançador integrado; log `/tmp/gtna-g0137-integrated-client.log`.
- Sem commit/push; QA visual, montagem humana e demais pendências de G-0135 continuam abertas.

### G-0136 (2026-09-30) — Traduções da cadeia Void, viewport e dependência GTIACore

- QA do autor revelou nomes crus em inglês e grade Random Void Mining fora da janela. O provider
  manual de en_us sobrescrevia as traduções Registrate: agora inclui todas as 45 essências de veios,
  sementes, dados mundiais/planetários, Deposit Recorder/Data, filtro e seis upgrades remotos.
  Conferidos os nomes reportados no jar final; pt_br já contém essas chaves.
- Random Void Mining declara uma entrada para o circuito. Template usa viewport de seis linhas,
  scrollbar e clipping LDLib, preservando os 216 candidatos e seus IDs. Slots são filhos diretos
  do scroll para integração de tooltips no viewer. Novo GameTest verifica altura <200, clipping,
  catálogo completo e acesso à última saída por rolagem; inspeção visual pelo autor ainda pendente.
- Gate `/tmp/gtna-g0136-gate.log`: spotlessCheck, compileJava, 28 classes unitárias,
  **126/126 GameTests**, runData --offline; datagen escreveu um arquivo de idioma. Assemble
  posterior passou e confirmou os nomes no jar. Cliente dev iniciado por runClient --offline,
  log `/tmp/gtna-g0136-runclient.log`; manter aberto para teste do autor.
- Pedido explícito do autor: GTIACore exige GTNA >=0.5.1 em ambos os lados, ordering AFTER.
  GTNA continua independente de GTIACore. Core aceita jar dev mapeado via `gtnaDevJar`, com
  bibliotecas de runtime correspondentes. Teste conjunto de 13 GameTests do core passou com
  GTNA carregado e GTMThings 1.6.0; primeira tentativa detectou 1.5.4 incompatível e foi corrigida.
  Ver QA do core para manifesto empacotado/build final e instalação na Prism de teste.
- Sem commit/push. Restam QA visual pelo autor e pendências manuais de G-0135.

### G-0135 (2026-09-29) — Caminho marciano completo e escopo efetivo FTB

- O teste GTIA de Radon deixou de conceder certificado por chamada direta ao SavedData. Agora monta
  perfuradora MV em `ad_astra:mars`, consulta geração normal de bedrock GTCEu, extrai Radon no hatch,
  registra com Deposit Recorder e leva esse cartão à rig remota LuV com T2 no Overworld. Registra
  `GTIA Mars native extraction -> recorded data -> remote Radon: PASS`. O fixture limpa os blocos
  planetários e restaura o cache anterior; só usa o mundo de GameTests, sem alterar mundos da Prism.
- Auditoria GTCEu `FTBOwner` + API FTB Teams 2001.3.2 confirmou: `getPlayerTeamForPlayerID` fornece
  a equipe pessoal e `FTBOwner.getUUID()` retorna `getId()`, não a party efetiva. O novo helper
  opcional `FTBFluidDiscoveryOwner` usa `Team.getTeamId()`; outras integrações/UUID individual
  mantêm a regra anterior. Não transfere automaticamente certificados pessoais antigos para parties.
- Novo GameTest usa manager/modelos FTB reais e três identidades isoladas com jogadores simulados:
  colegas compartilham/copiam, terceiros não acessam/copiam, sair revoga a produção e voltar a restaura.
  Não é evidência de dois clientes humanos. Mapa de identidades de teste limpo em finally.
- O corpo opcional está em `FTBFluidDiscoveryTests`, sem annotation de registro. O primeiro gate
  conjunto com runData remove FTB do runtime e revelou verificação antecipada de tipos opcionais;
  mover o corpo para helper separado corrigiu o carregamento do holder sem FTB. Processo de teste
  com main abortada encerrado antes de repetir. Dependências de API são compile-only, sem classes
  FTB empacotadas ou requisito obrigatório no mods.toml.
- Gate final `/tmp/gtna-mars-team-final-gate-2.log`: spotlessCheck/compileJava/runUnitTests/
  runGameTestServer/runData/assemble --offline após spotlessApply, **28 unitários/125 GameTests**,
  datagen `written: 0`. Esse gate não carrega FTB; cenário opcional não é contado como exercitado nele.
- Perfil final `/tmp/gtna-mars-team-profile-final.log` e `build/test-results/void-fluid-gtia/profile.log`:
  **125/125**, com os dois marcadores obrigatórios (Marte e FTB). Runner exige esses marcadores
  para evitar falso positivo de cenários pulados. Config dev restaurada a seis programas e depósito
  temporário removido. Jar FTB Teams usado no dev tem SHA-256 idêntico ao da Prism 2001.3.2.
- GTNA instalado com backup: SHA-256 `5c42081f4300e91c7a7813bf97371554c7e5296859b10758e0492d50101c99c7`.
  GTIACore permanece no hash do G-0134. Reabertura entrou no Test World sem crash de
  classes opcionais ou descarte de programas void; log `mars-team-fixed-client.log` no QA da Prism.
- Restam conferência visual/montagem manual, teste com duas pessoas, percurso espacial normal,
  demanda/balanceamento e dados de pesquisa T4/T5. Nenhuma quest, commit ou push criado.


### G-0134 (2026-09-29) — Testes do cliente GTIA e correções de integração

- Protótipo de fluidos EV segue o Estado atual e `docs/roadmap/void-fluid-drilling.md`:
  registro de extração nativa real, dados por fluido/origem/proprietário, upgrades remotos,
  consumo proporcional, paralelos e pausa com saída bloqueada. O checkpoint G-0133 passou
  a documentar a rodada paralela de texturas; esta rodada preservou todas essas alterações.
- Gate pré-cliente com 28 unitários/123 GameTests, datagen e assemble passou. `runClient
  --offline` chegou ao carregamento completo de atlas sem aviso de textura/modelo ausente
  do GTNA; encerrado depois dessa verificação. Não equivale a inspeção visual das UIs.
- Instância de QA: `GregTech Infinity Ascension`; GTNA antes `.jar.disabled`, agora ativo.
  Perfil `void_fluid_drill.json` com sete programas instalado. Sem KubeJS nessa instância,
  depósito colocado como datapack em `saves/Test World/datapacks/gtia-void-fluids.zip`.
  Backups específicos em `gtna-qa-backups/` junto da instância. Quests não alteradas.
- Primeiro cliente conjunto abortou: `MaterialIconType plate_superdense already registered`.
  GTIACore `GTIAExtraForms` e GTNA `GTNATagPrefix` agora consultam `getByName` usando a chave
  normalizada `plate_superdense` e reutilizam a entrada. Mantidos IDs de formas/receitas.
  Core validado com 13 GameTests, runData, build final sem dependências opcionais e QA estático;
  wrapper bwrap exige `--no-daemon` para não reutilizar daemon fora do namespace montado.
- Cliente seguinte entrou no Test World e carregou automaticamente o datapack, mas revelou
  `failed to add recipe ... electric_void_mining/iron_vein_essence` e programa terrestre
  Tin/Lead: índice GTCEu não distingue apenas EU/t/duração. Preciso terrestre agora usa
  circuito 1 nos três básicos EV e circuito 2 nas 40 veias completas LuV/ZPM; aleatório
  usa circuito 1 no pool EV e 2 no completo ZPM. Planetários preservados sem circuito extra.
  Tooltips en/pt e documento da cadeia atualizados.
- `GTNAVoidMinerLookupGameTests.everyProductionVoidRecipeSurvivesLookupBaking` monta índice
  isolado a partir do catálogo real do RecipeManager e verifica seleção de cada ID,
  cobrindo receitas descartadas que testes de fixture/RecipeManager não detectavam.
- Gate final: `spotlessCheck compileJava runUnitTests runGameTestServer runData assemble
  --offline`, após spotlessApply: 28 unitários, **124/124 GameTests**, build verde.
  Log `/tmp/gtna-void-lookup-gate-20260929.log`; perfil GTIA repetido **124/124**.
  Reabertura final entrou no Test World sem crash de ícone ou descarte de programas
  `void_mining`/`void_fluid_drilling`; `build/test-results/prism-void-qa/results.json`.
- Jars instalados: GTNA SHA-256 `6de2957c4f595c62795ffe2c663cda5847fd54de9b0032bdb9b56cc0a00ac52c`;
  GTIACore `71719b6f2de0a18564a40dea00f83804e8cf971f96b1fefbeaa0b31617394f90`.
  Logs do primeiro crash e carregamento conjunto preservados em `build/test-results/prism-void-qa/`.
- Pendências: montagem/UI manual, operação marciana nativa, dois jogadores/equipes,
  balanceamento real e dados T4/T5. O mundo antigo também acusa mappings removidos e receitas
  de outros sistemas inválidas; não considerar a primeira fatia/campanha toda validada.
  Tudo local, sem commit/push.


### G-0133 (2026-09-30) — Redesign de Texturas: 47 Essências, 8 Itens HD Animados, 17 Itens GT-Authentic (Remote/Deposit/Data Chips) e 29 Overlays de Hatches

- `src/main/resources/assets/gtna/textures/item/essence.png` e `essence_seed.png` refeitos em pixel art `16x16` nítida (drusa/pó cristalino de vazio e cluster de 3 esporos biominerais facetados com fissura ciano).
- Todas as 40 texturas de veios em `src/main/resources/assets/gtna/textures/item/essence/` e 5 novas texturas planetárias (`moon_vein.png`, `mars_vein.png`, `venus_vein.png`, `mercury_vein.png`, `glacio_vein.png`) foram geradas seguindo o **Protótipo A**, com modelos planetários atualizados em `GTNAItems.java`.
- Os 8 itens (`neutronium_antimatter_fuel_rod`, `draconium_antimatter_fuel_rod`, `cosmic_neutronium_antimatter_fuel_rod`, `infinity_antimatter_fuel_rod`, `annihilation_constrainer`, `nexus_linker`, `tesseract_target_marker`, `command_wand`) foram atualizados com tiras animadas `.png` + `.png.mcmeta` preservando a escala `64x64` nas cápsulas de antimatéria e no constrainer (`cosmic_neutronium` com blindagem vermelha + fluido interno preto, fundo opaco removido do constrainer, e `quantum_wireless_tool.png` do `UFO-Future-1.21.1` aplicado ao `nexus_linker`).
- Os 17 itens (`fluid_remote_upgrade_terrestrial..t5`, `deposit_recorder`, `deposit_data`, `fluid_separation_filter`, `overworld_data`, `nether_data`, `end_data`, `planet_data_chip_moon..glacio`) ganharam texturas animadas `.png` + `.png.mcmeta` em `16x16` trabalhadas diretamente sobre os sprites originais do GTCEu/GTO (`data_orb`, `portable_scanner`, `data_stick`, `fluid_filter`, `planet_data_chip`), com evolução progressiva de complexidade/frames do `terrestrial` (2 frames) até o `t5` (16 frames arco-íris), e tiveram seus modelos vinculados em `GTNAItems.java`.
- Os 29 conjuntos de overlays de hatches (`thread_hatch_mk1..mk8`, `accelerate_hatch_mk1..mk14`, `overclock_hatch_mk1..mk7`) foram atualizados para animações contínuas de 16 frames sem efeito de reset (`2*pi` periódico), com bisel chanfrado e borda externa `1px` transparente.
- Validação completa executada: `./gradlew spotlessApply spotlessCheck compileJava runUnitTests runGameTestServer runData --offline`. Resta conferência visual em cliente (`runClient`). Sem commit/push.

### G-0132 (2026-09-29) — Void Miner com modos preciso/aleatório e cadeia de essências GTL

- Fonte corrigida e verificada: pack completo em
  `/home/raishxn/.local/share/PrismLauncher/instances/GregTech-Leisure-1.4.5.1/minecraft`,
  `kubejs/server_scripts/gtceu.js:6255–6729` e `startup_scripts/item.js:75–96`.
  O antigo caminho Downloads está vazio; AGENTS.md local agora aponta para a instância Prism.
  GTLCore `gtl-1431-skyblock`, commit `18c7814`, fornece o scanner e a estrutura da incubadora.
- A elétrica registra `electric_void_mining` e `random_void_mining`, com seletor nativo de
  modos do GTCEu, identificação do modo na tela e vários output buses para o pool aleatório.
  Preciso aceita essência consumível (receitas nativas) ou seletor legado de packs; aleatório
  dispensa ambos. Validações de fluido, teto de saídas e paralelo continuam aplicadas.
- `GTNAVoidVeins` registra os fatos das 40 veias GTL. World Data Scanner LV–IV tem dois tanques
  de 64.000 mB e crafting original por hull/cable/sensor/circuit. Incubator conserva pattern,
  blocos e crafting GTL (Greenhouse + Plascrete + Filter Casing + Field Generator HV).
- Essence Seed tem receita Mixer original. Bootstrap de Essence usa Bone Block + Biomass
  no Chemical Bath HV, substituindo o Block Conversion Room que não faz parte deste escopo.
  Culturas conservam 10.000 mB Biomass/Milk, 12.000 ticks, HV e 64 essências; dados reutilizáveis
  de Overworld/Nether/End são 16/32/64. Amostras aceitam ore tags ou Raw Ore/Dust correspondente;
  Ancient Debris é tratado como item direto, não como tag `ores/`.
- Dados dos três mundos vanilla e cinco planetas usam dimension gates. Chips Ad Astra exigem
  scanner EV no planeta e pedra local não consumida; os chips são reutilizados pela incubadora.
  Os programas planetários agora consomem a essência, mantendo custos/rendimentos G-0131.
  Sem Ad Astra, o registro dos cinco scans/culturas/programas planetários é omitido.
- Progressão: três programas terrestres EV consomem essência, preservando as saídas antigas.
  As 40 veias completas conservam energia LuV (Overworld/Nether) e ZPM (End), mas usam o custo
  de fluido/duração configurável GTNA e quantidades GTL divididas por dez. Aleatório EV só
  inclui Iron/Copper/Tin/Lead/Gold/Silver; pool completo é ZPM. Os dois aleatórios usam por
  padrão 10.000 mB e 4.800 ticks. As diferenças para o GTL (1M/10M mB, 200/1.200 ticks e
  saídas maiores) estão documentadas; não alegar port numérico 1:1.
- MaterialAdd habilita Dust de Rubidium/Strontium/Tellurium/Zirconium, declarados sem item
  forms no GTCEu. Zircon/Celestine/Trinium Compound usam Zirconium/Strontium/Trinium; sem
  Raw Ore, saídas usam Dust. Não foram adicionados minérios ao worldgen.
- Assets de Essence/Seed/veias/scanner vêm do GTOCore (CC BY-NC-SA 4.0, permissão existente),
  com variantes OW/End reutilizando os ícones genéricos; nenhum asset de licença indefinida
  do pack foi copiado. Atribuição central GTL e THIRD_PARTY_NOTICES atualizados.
- Validação final: `./gradlew spotlessApply spotlessCheck compileJava runUnitTests
  runGameTestServer runData --offline`, `/tmp/gtna-void-chain-final-gate.log`: **28 unitários,
  114/114 GameTests**, sem parsing error de receita GTNA. Os testes novos cobrem aleatório
  sem essência e sem gasto com output cheio; incubadora formada/produção/consumo/preservação
  dos dados e recusa de sponge ausente; catálogo completo e dimensão/capacidade dos scanners.
  O teste preciso verifica consumo de uma essência; os cinco planetas verificam a nova cadeia.
  Datagen repetido `/tmp/gtna-void-chain-data-repeat.log`: `written: 0`. `git diff --check` verde.
- Pendências: QA client de ícones/tooltips/aba de modo/preview/orientação da incubadora; scans
  reais nos cinco planetas; operação de paralelos IV+; rendimento/custo das adaptações no pack
  e migração das antigas automações por circuitos/chips. Tudo local, sem commit/push.


### G-0131 (2026-09-29) — Correção dos programas do Void Miner EV

- Adicionados cinco itens `planet_data_chip_*` ao GTNA, com nomes em inglês/português e modelo
  provisório baseado no Data Stick do GTCEu. Quando Ad Astra está presente, cada chip tem receita
  no Assembler EV usando a pedra do planeta sem consumi-la, mais Data Stick, circuito EV e
  Soldering Alloy.
  O programa planetário exige tanto a amostra quanto o chip correspondente, sem consumi-los.
- `ElectricVoidMinerMachine` só aceita receita com um custo positivo de Drilling Fluid dentro do
  teto configurado; rejeita ausência de fluido e fluido alternativo. Configuração inválida de
  fluido-base zero volta ao padrão. A UI exibe ID do programa, fluido, EU/t e paralelo efetivos,
  além das saídas já exibidas.
- `maxParallelByTier` agora tem entrada padrão para EV–MAX: 1 em EV e 2 de IV em diante. O
  exemplo GTIA explicita esses valores e continua permitindo ajuste individual por tier.
- Validação: `./gradlew spotlessApply spotlessCheck compileJava runUnitTests runGameTestServer
  runData --offline` passou após a correção final da amostra em
  `/tmp/gtna-voidminer-chip-final-gate.log`, com **111/111 GameTests** e `written: 0`;
  a primeira geração criou os modelos/traduções novos. Repetição do datagen em
  `/tmp/gtna-voidminer-fix-datagen-repeat.log` passou com `written: 0`; `git diff --check` passou.
  Sem commit/push.
- Pendências: QA manual de visual dos chips e da UI, obtenção de amostra/chip no pack, operação
  real de paralelo IV+ com consumo proporcional e compatibilidade com mundos existentes.

### G-0130 (2026-09-29) — Void Miner elétrico EV configurável

- `gtna:electric_void_miner` (GTIA-D-159..D-162): multibloco EV 3×3×3 de Titanium Stable Casing
  com núcleo de frame de Titânio; `gtna:void_miner_steam_gate_aged` permanece intocado.
- Programas são receitas de `gtna:electric_void_mining`: seletor não consumível (circuito, amostra
  ou Planet Data Chip), saídas de Raw Ore e custo de Drilling Fluid por receita. Três programas
  terrestres padrão (circuitos 1–3) e fallback de cobblestone opcional.
- Programas do Ad Astra (dependência opcional, `ModList.get().isLoaded("ad_astra")`): Lua, Marte,
  Vênus, Mercúrio e Glácio usam a pedra do planeta como amostra não consumível e espelham os
  veios do GTO (`GTOOres`): Bauxite/Ilmenite; Scheelite/Tungstate/Cooperite;
  Sulfur/Pyrite/Galena/Chromite; Garnierite/Nickel/Cobaltite; Bastnasite/Tungstate/Tantalite.
  Sem o Ad Astra nada é registrado.
- `config/gtna/balance/electric_void_miner.json` controla `enabled` (registro), `minimumTier`,
  `programRequired`, `baseDuration`/`baseEUt`/`defaultDrillingFluidPerOperation` do fallback,
  `maxDrillingFluidPerOperation`, `maxOutputStacksPerOperation`, `parallelEnabledFromTier`,
  `maxParallelByTier` e `allowFixedFallbackRecipe`. O tier operacional vem dos hatches de energia;
  o paralelo fica em 1 em EV e é limitado pelo mapa por tier. Overclock elétrico padrão.
- Controller EV em Assembler com frame/placa dupla de Titânio, componentes EV, circuitos EV e
  Soldering Alloy; sem Lua, amostra, Planet Data Chip ou minerador Steam (GTIA-D-161).
- Testes novos: `electricVoidMinerFormsAndCapsParallelAtEv`,
  `electricVoidMinerRunsProgramAndConsumesFluidOnce` (1000 mB consumidos uma única vez e 3 Raw
  Nickel), `electricVoidMinerConfigGatesPrograms` (seletor obrigatório e teto de fluido) e
  `electricVoidMinerAdAstraPrograms` (cinco programas planetários).
- Validação: `./gradlew spotlessApply spotlessCheck compileJava runUnitTests runGameTestServer
  runData --offline` passou com **111/111 GameTests** (`/tmp/gtna-voidminer-gate8.log`); repetição
  do `runData` com `written: 0` (`/tmp/gtna-voidminer-datagen-repeat2.log`). `git diff --check`
  passou. Sem commit/push.
- Pendências: teste manual do autor (formação, UI/JEI, custo real e paralelo); definição dos
  programas planetários (amostra + Planet Data Chip) no pack; GTIA-D-172 (fluidos) fora de escopo.

### G-0129 (2026-09-29) — Merge do push de 26/09 e publicação na main

- `origin/main` (60c20e8) integrado ao trabalho local (`575fc67`) e publicado em `main`
  (`45d68fc`). Entram o HUD wireless de energia (overlay, estado, pacote e config), a família
  completa de Dehydrators LV–OpV com assets gerados, a QA do módulo do EBF, a cadeia de Boron
  Carbide (materiais, Mixer/Sifter e carcaça), os circuitos distintos das duas famílias de
  carcaça, o exemplo KubeJS de múltiplas receitas e o mixin de guarda do preview JEI.
- Conflitos resolvidos em `GTNAMachines3`, `GTNAMaterials`, `MaterialBuilder`, `GTNALangProvider`,
  `GTNABlockRecipes`, `GTNAComponentRecipes`, `GTNAMachineGameTests`,
  `MultiblockControllerMachineMixin`, `gtna.mixins.json`, langs `en_us`/`en_ud`/`pt_br` e
  `CONTINUITY_LEDGER.md`. O `en_us`/`en_ud` foram regenerados pelo `runData`. A numeração
  paralela de checkpoints (G-0122–G-0124 de 26/09 vs. locais de 29/09) foi mantida com uma nota
  de merge no ledger.
- Validação da árvore do merge: `./gradlew spotlessApply spotlessCheck compileJava runUnitTests
  runGameTestServer runData --offline` passou com **107/107 GameTests**
  (`/tmp/gtna-merge-gate.log`); repetição do `runData` com `written: 0`
  (`/tmp/gtna-merge-datagen-repeat.log`).
- Publicado em `origin/main` (60c20e8..45d68fc). Sem pendências de publicação; o reteste manual
  do HUD e da família de Dehydrators fica como próximo QA opcional.

### G-0128 (2026-09-29) — Upgrade dos Pattern Buffers sem drop e página de 90 slots

- Reporte do autor em jogo: usar os itens de upgrade (21/32/72) derrubava os padrões no chão e a
  UI com 72 slots abria uma segunda página mesmo tendo espaço vertical sobrando na coluna esquerda.
- Causa do drop: `IDropSaveMachine.saveToItem` usa `saveManagedPersistentData(tag, true)`, que só
  inclui campos anotados `@DropSaved` — o `patternInventory` só tem `@Persisted`, então o upgrade
  salvava apenas as configurações; o inventário de padrões era derramado pelo `onMachineRemoved`
  da troca de bloco. Correção: o upgrader salva/restaura o estado persistente completo
  (`saveManagedPersistentData(tag, false)` / `loadManagedPersistentData`) e
  `GTNAMEPatternBufferPartMachine` ganhou um marcador que suprime o `clearInventory` durante a
  troca (consumido no primeiro `onMachineRemoved`; cancelado se o `setBlock` falhar).
- Causa secundária encontrada pelo GameTest: o antigo probe de capacidade (`createMetaMachine` no
  holder vivo) anexava o storage da segunda máquina ao mesmo block entity; no save persistente os
  campos padrão do probe sobrescreviam os do buffer real (inventário vazio). O probe foi removido;
  a checagem usa `GTNABalance.getPatternBufferSlotsOrDefault(id, atual)`.
- Invisibilidade após o upgrade (segundo relato do autor): o save persistente também transferia o
  `renderState` do block entity, que referencia a definição antiga, e o bloco novo renderizava sem
  modelo. O upgrader restaura o render state padrão da nova definição depois do load, e o GameTest
  confere `getRenderState().is(nova definição)`.
- UI: `PATTERN_ROWS` de 6 para 10 (90 slots por página). 72 slots agora cabem em uma página e 162
  em duas; `PatternBufferLayoutTest` continua validando o encaixe geométrico. Documento
  `pattern-buffer-capacity-config.md` e item 11 do backlog GTIA atualizados. O config de
  desenvolvimento foi colocado no perfil GTIA (Advanced 72/Ultimate 162) para o reteste do autor;
  `run/config/gtna/balance/pattern_buffers.json` é lido na inicialização e exige reinício (o
  exemplo em `docs/config-examples/gtia/` não é lido diretamente).
- Teste novo: `patternBufferUpgradePreservesPatternsInside` (Advanced → Ultimate: capacidade
  aplicada, padrão e configuração por slot preservados, nenhum `ItemEntity` no chão).
- Validação: `./gradlew spotlessApply spotlessCheck compileJava runUnitTests runGameTestServer
  runData --offline` passou com **104/104 GameTests** e datagen `written: 0`
  (`/tmp/gtna-g0128-gate6.log`); com Advanced 72/Ultimate 162 o `runGameTestServer` também passou
  **104/104**, incluindo a checagem de render state do upgrade. `git diff --check` passou.
  Sem commit/push.
- Reteste manual do autor (2026-09-29): **aprovado**. O upgrade preserva os padrões, as
  configurações e o modelo aparece imediatamente; a paginação com 162 slots e o restante da UI
  ficaram corretos. O histórico de capacidade registra 72/162 neste ambiente de desenvolvimento.

### G-0127 (2026-09-29) — Perda wireless configurável do Nexus Flux Matrix

- `config/gtna/balance/nexus_flux_matrix.json` passou a aceitar `lossApplication` (`LEGACY`
  default, `MATRIX_INPUT_ONCE`, `NO_LOSS`), `lossPercentByTier` (default LV 5%, MV 4%, HV 3%,
  EV 2%, IV 1%, LuV+ 0%; 0–100% com resolução de 0,01% em basis points) e
  `generatorArrayAppliesSeparateLoss` (default `true`, ignorado no modo central). O exemplo GTIA
  está em `docs/config-examples/gtia/nexus_flux_matrix.json`; semântica e migração em
  `docs/roadmap/nexus-wireless-loss-config.md`. ME Storage, Nexus ME Hypercore e Nexus Molecular
  Forge não foram alterados.
- `NexusWirelessLoss` faz a divisão exata em `BigInteger` (busca binária para respeitar o espaço
  livre, sem overflow) e devolve bruto/creditado/perdido/resto. `NexusEnergyNetwork` credita só o
  líquido, acumula o resto fracionário por dono, salva o resto e a dimensão da Matrix no NBT e
  mede bruto/creditado/perdido por tick. `MATRIX_INPUT_ONCE` recusa entradas até existir Matrix
  formada com tier conhecido e, em saves antigos sem dimensão registrada, até a estrutura
  registrá-la; EV+ preserva o gate `crossDimension` por tier.
- Generator Array e Wireless Dynamo Hatch retiram da origem apenas o bruto aceito; no modo
  `LEGACY` a perda de 5% do Array continua no caminho e entra nos totais exibidos via
  `recordLegacySourceLoss`. Matrix (`NexusFluxMatrixMachine`) e Quantum Terminal exibem bruto,
  creditado e perda.
- Testes: unitário `NexusWirelessLossTest` (espaço limitado, 20 transferências de 1 EU → exatamente
  1 EU perdido, transferência de 2^100 sem overflow); GameTests `generatorArrayForms` (seção
  wireless do Array), `wirelessDynamoCreditsMatrixAfterOneLoss`,
  `nexusWirelessFractionalLossSurvivesSave` e `nexusCrossDimensionGateUsesConfiguredTier`.
  `python3 tools/test_nexus_wireless_profiles.py` rodou `runGameTestServer` nos três perfis:
  `legacy`, `matrix_input_once` e `no_loss`, **todos PASS com 103/103 GameTests**
  (`build/test-results/nexus-wireless-profiles/*.log`, resumo em
  `/tmp/gtna-nexus-profiles-final.log`); o script restaurou o JSON de desenvolvimento ao final.
- Gate anterior completo `spotlessApply spotlessCheck compileJava runUnitTests runGameTestServer
  runData --offline`: 103/103 GameTests e datagen `written: 0` (`/tmp/gtna-nexus-final-gate.log`).
  `git diff --check` passou. Sem commit/push.
- Limites e QA manual pendente: a perda vale só na entrada da rede (não por transferência interna)
  e exige reinício após editar o JSON; o resto fracionário fica no save (abaixo de 1 EU de perda
  pendente por dono). Falta confirmar no cliente/pack: valores de bruto/creditado/perda na UI do
  Matrix e do Quantum Terminal, ausência de perda dupla com o Generator Array no perfil GTIA,
  aceitação/recusa entre dimensões por tier e migração de save antigo. O autor precisa testar e
  aprovar antes de publicar.

### G-0126 (2026-09-29) — Capacidade configurável dos ME Pattern Buffers

- `GTNABalance` carrega `pattern_buffers.json` antes do registro dos quatro hatches, com defaults
  9/21/32/72 e faixa 1–540. O exemplo GTIA propõe Advanced 72 e Ultimate 162. IDs, receitas e
  `me_craft_pattern_hatch` permanecem fora desta mudança; ME Storage, Nexus ME Hypercore e Nexus
  Molecular Forge não foram alterados.
- Inventário legado é expandido ao carregar, preservando seus índices e conteúdo. O NBT registra
  a capacidade; o histórico local conserva o maior valor já ativado. Se um mundo chegar sem esse
  histórico e contiver mais slots que o valor configurado, o carregamento recusa o buffer com
  orientação para restaurar a capacidade, evitando descarte silencioso. Levar o arquivo de
  histórico junto com o mundo é necessário ao transferir instalação.
- A UI agora cria páginas reais de 54 slots e ativa apenas a página visível. Jade e tooltip exibem
  a capacidade efetiva. O cartão de cópia cola padrões de índices ausentes em slots livres do
  destino menor e mostra quantos padrões entraram do total armazenado; os dados continuam no
  cartão. A documentação está em `docs/roadmap/pattern-buffer-capacity-config.md`.
- Validação: `./gradlew spotlessCheck compileJava runUnitTests runGameTestServer runData --offline`
  passou; **100/100 GameTests**, datagen `written: 0`. O mesmo conjunto de GameTests passou com
  Advanced 72/Ultimate 162. Após voltar os pedidos a 32/72, nova execução passou 100/100 e o
  log confirmou preservação de 72/162 pelo histórico. `git diff --check` passou. Pendências:
  conferir a troca visual de páginas e tooltip/Jade no cliente, além de medir sincronização,
  memória e crafting AE2 sob carga com 162 slots no pack. Sem commit/push.

### G-0125 (2026-09-29) — Escopo de Pattern Buffers no backlog GTIA

- Por decisão do autor, removida a proposta de configurar ME Storage, Nexus ME Hypercore e Nexus
  Molecular Forge. O item 11 do backlog externo passou a documentar ampliação de slots por
  variante dos Pattern Buffers GTNA, comparada ao Super Pattern Buffer do checkout GTLAdditions.
- Verificados defaults GTNA 9/21/32/72, geometria UI 9×6 com paginação dinâmica e a referência
  GTLAdditions 9×6×3=162. O construtor GTNA aloca também estado interno e especialização por slot;
  diminuir slots sem migração pode descartar entradas acima do novo limite. Backlog define expansão
  por ID, teto técnico, restrição de redução, UI/proxy/copy card e testes necessários. Números do
  perfil GTIA acima dos defaults continuam provisórios.
- Apenas documentação; `git diff --check` validado. Gate Gradle não rodado porque não houve
  alteração de código/runtime. Sem commit/push.

### G-0124 (2026-09-29) — Auditoria de próximas configurações GTIA

- Conferidos `GTNABalance`, `ConfigHolder`, registros e controllers do GTNA e as fontes locais
  de configuração GTNL/TST. O backlog externo
  `GTIA Private and Tests/pack/docs/BACKLOG_CONFIGURACOES_GTNA_PARA_GTIA_v01.md` ganhou itens
  7–12 com comportamento, risco, parâmetros, perfil GTIA/legado, migração, aceitação e prompt.
- Priorizados: cobertura das chaves de habilitação e cadeia de minério após os P0 já planejados;
  processadoras térmicas, turbinas, farms e AE2/Nexus depois da medição de progressão. O item
  Large Steam inclui Steam Manufacturer/Cracking; Component Assembler distingue a Assembly Line.
- Somente documentação. `git diff --check` passou; gate Gradle não rodado nesta auditoria. Sem
  commit/push. Próxima implementação ainda requer checar código vigente e medir valores no GTIA.

### G-0123 (2026-09-29) — Primeiro perfil configurável da Universal Factory

- Lido o backlog `BACKLOG_CONFIGURACOES_GTNA_PARA_GTIA_v01.md` do GTIA. Implementado o primeiro
  P0 sem alterar o perfil ativo de instalações existentes: `LEGACY` continua default e configurações
  antigas recebem campos ausentes em memória. O exemplo em `docs/config-examples/gtia/` ativa
  `SHARED_BUDGET` com entrada MV em capacidade 2, warmup/batch desligados e hatches desabilitados.
- `GTNABalance` valida modo, tabela de capacidade e teto de operações. `UNLIMITED` exige
  `allowUnlimited` e ainda usa teto técnico. A Universal Factory persiste a divisão de threads;
  o executor impede iniciar receitas que ultrapassariam a capacidade restante, inclusive após
  mudança de seleção/configuração durante receitas em andamento. Lista opcional de recipe types
  filtra a busca normal e o ME Pattern Buffer. Hatches especiais são permitidos no pattern somente
  quando a opção estiver habilitada na inicialização. Tooltips deixaram de prometer hatches no
  default, e a UI mostra os números efetivos do orçamento.
- Teste unitário novo cobre a divisão em tiers representativos, a reserva de capacidade e
  overflow. `./gradlew spotlessCheck compileJava runUnitTests runGameTestServer runData --offline`:
  Spotless, compilação, unitários e **97/97 GameTests** passaram. Na segunda execução conjunta,
  `runData` falhou porque o renderer do Annihilate Generator não estava registrado; a repetição
  isolada passou (`written: 0`). A primeira execução conjunta já havia passado integralmente
  (`written: 1`, nova tradução). Logs em `/tmp/gtna-universal-factory-gate.log`,
  `/tmp/gtna-universal-factory-gate-final.log` e `/tmp/gtna-universal-factory-datagen-retry.log`.
- Pendente: teste manual da UI, da compatibilidade dos hatches e de mundo antigo com o perfil GTIA;
  GameTests específicos dos modos, de persistência e de IO/energia; investigar a intermitência do
  renderer se voltar a ocorrer. Próximo P0 do backlog: perda wireless no Nexus Flux Matrix.

### G-0122 (2026-09-26) — Pacote de visualização do Integrated Ore Processing (viewer HTML + imagens de galeria)

- Novo pacote offline em `docs/visualization/`: viewer 3D autossuficiente
  (`gtna-ore-processing-preview.html`), 7 imagens 1920x1080 em `gallery/` e `README.md`.
- `tools/gtna_ore_preview/build_preview.py` extrai os `FactoryBlockPattern.aisle()` **reais** de
  `GTNAMachines.java` (Integrated 6x12x11 e Advanced 32x12x15), aplica a convenção
  `charDir=LEFT / stringDir=UP / aisleDir=FRONT` (row 0 = fundo, aisle cresce para a frente),
  compõe as texturas do GTCEu/GTNA com os overlays de hatch/bus e injeta tudo no template. O
  renderer é canvas 2D (painter's algorithm, tiles por data URI) — sem dependências e sem fetch,
  então o HTML abre direto de `file://`.
- Verificado por `--check`/`--dump`: todo símbolo de pattern mapeado, **zero colisão e zero bloco
  solto** nos 8 módulos propostos, contagens conferindo (Integrated **344** blocos, Advanced
  **2689**; `setMinGlobalLimited(60)` lido do pattern). Os hatches/buses propostos são pintados só
  em células de casing com abilities que já existem no pattern.
- Imagens geradas por chromium headless (`--html --gallery`): 1920x1080, 0,44-1,04 MB. A 06 traz
  os 7 circuitos e a 07 o quadro dos 8 módulos com bônus e status.
- **Módulos = proposta de design; nada registrado no mod.** 4 para o Integrated (Intake Column,
  Grinding Head Array, Wash Recovery Tank, Byproduct Sorting Rack) e 4 para o Advanced (Laser
  Array Expansion, Parallel Governor, Reagent Complex, Nexus Uplink), ancorados no controller como
  o exemplo KubeJS de G-0073. Cada card traz bônus, status (`estrutura apenas` / `precisa
  modifier` / `precisa codigo`) e o snippet KubeJS pronto para colar.
- **Ajuste pós-feedback (autor):** o canvas estava no fluxo do flex e o tamanho intrínseco dele
  realimentava o layout (Firefox/Zen e Chromium resolviam diferente) — agora o canvas é
  `position:absolute; inset:0` dentro do stage, com re-enquadramento automático (ResizeObserver +
  re-fit atrasado, só enquanto o autor não mexeu na câmera). O estilo do render também mudou:
  contorno por bloco desligado por padrão, **AO nas quinas**, gradiente de luz por face vertical,
  sombra de chão e marcadores de orientação (faixa âmbar na frente / cinza atrás, bússola
  FRENTE/TRÁS/ESQ/DIR, rim âmbar no controller e aba "Rotulos" ligada no modo interativo).
- **Nenhum arquivo Java/build foi alterado** — o gate Gradle não foi rodado nesta etapa (por
  isso não há `spotlessCheck`/gametests neste checkpoint).
- **Pendências:** (a) o autor decidir se aprova algum módulo (aí vira pattern Java/KubeJS +
  modifier + gametest); (b) confirmar in-game se cada geometria proposta encaixa no controller
  real; (c) as imagens são mockups geométricos fiéis ao pattern, **não** screenshots — vale
  incluir screenshots reais na galeria antes de publicar.

> **Push de 26/09 (outra máquina) — numeração paralela:** os checkpoints abaixo usam os IDs
> G-0122–G-0124 de 26/09 e convivem com os checkpoints locais de 29/09 acima; os dois históricos
> são reais e foram mantidos.

### G-0124 (2026-09-26) — QA em client: módulo EBF, UI, CTM, carcaças e KubeJS

- O autor confirmou EBF sem módulo processando Aluminium Dust → Ingot em 11,05 s com hatch HV,
  bobinas Tritanium e muffler IV; EMI lista 22,1 s. O EBF stock aplica `ebfOverclock` conforme
  tensão e temperatura, portanto esse tempo menor não é evidência de bônus do módulo.
- O Accelerate Hatch HV colocado na quarta camada, dois blocos à esquerda e um atrás do
  controlador, ocupa a célula superior `C` do módulo. Ela só aceitava Invar Heatproof Casing.
  O predicado agora aceita Accelerate nessa célula e o GameTest troca o hatch da base do módulo
  pelo hatch superior, confirma a formação e rejeita dois Accelerate Hatches simultâneos.
- A UI de `WorkableElectricMultipleRecipesMachine` mostrava apenas a linha personalizada de
  energia porque omitira as chamadas de estado, progresso, falha de receita, modo e saída do
  `MultiblockDisplayText`. Essas linhas foram adicionadas para o Integrated Ore Processor e os
  demais hosts dessa classe.
- A captura do Boron Carbide Radiation Resistant Casing mostrou o CTM roxo/preto: o `.mcmeta`
  apontava para `gtna:block/...`, mas o atlas está em `gtna:block/casings/...`. Caminho corrigido.
  O material GTO `BoronCarbide`/`BoronCarbideCeramics` foi adicionado com rota simplificada:
  Mixer (4 Boron + 3 Carbon → 7 B4C), Sifter (2 B4C → cerâmica), Compressor/Cutter automáticos
  para flocos e receita Assembler original da carcaça com 16 flocos. Omite os fornos não portados.
- Os casings `component_assembly_casing_*` e `component_assembly_line_casing_*` tinham insumos
  idênticos; o GTCEu descartava um resultado em seu lookup, apesar de ambos aparecerem no código.
  Circuitos 1/2 distinguem as famílias em LV–UV. GameTest confere ambas e circuitos diferentes.
- A página JEI do exemplo KubeJS de desempenho colocava a célula `P` dois blocos do controller,
  coincidindo com a extremidade da base do Integrated Ore Processor. Agora `P` fica a quatro
  blocos, fora da base e do outro exemplo de módulo.
- O autor confirmou sem falha: paginação/hover JEI do Component Assembler, Large Steam Solar
  Boiler wireless, Dehydrators LV–OpV, Vacuum Drying Furnace, HUD Flux Matrix, nomes Rocket
  Engines e Evaporation Plant. Ainda faltam reteste no client das correções acima, duração do
  Accelerate Hatch e bônus KubeJS com módulo formado.
- Gate local: `spotlessApply compileJava runUnitTests runGameTestServer runData --offline` passou
  com 100/100 GameTests e datagen `written: 0` (`/tmp/gtna-0124-final-gate.log`).

### G-0123 (2026-09-26) — EBF processa receitas sem módulo auxiliar

- A captura enviada pelo autor mostra um EBF formado e energizado, sem módulo auxiliar, sem
  iniciar receita. No GTCEu, `isRecipeLogicAvailable()` requer estrutura formada **e** estado
  sem erro. O `MultiblockControllerMachineMixin.checkPattern()` já preservava a formação da base
  quando o módulo opcional não casava, mas deixava no estado o `PatternError` da extensão.
- Após checar todos os módulos, o mixin limpa esse erro porque o padrão principal casou. Não
  altera a aceitação de hatches nem os limites: módulos só somam partes quando casam. O
  `ebfModuleForms` remove o módulo e confirma que a base segue formada, conta zero módulos e
  disponibiliza sua lógica de receitas. O mesmo teste cobre o terceiro Energy Hatch restrito
  ao módulo, rejeição do segundo Energy Hatch auxiliar, wireless no módulo e Accelerate na base.
- Verificação: `spotlessCheck`, `compileJava`, `runUnitTests`, 98/98 GameTests e `git diff --check`
  passaram. A primeira tentativa de `runData` no worktree isolado encontrou falha intermitente
  no registro do renderer Annihilate; repetição isolada passou e gerou recursos do cache inicial.
  A segunda repetição confirmou datagen `written: 0`.
- Pendências manuais: receita EBF sem módulo no client; paginação/hover da prévia JEI do Component
  Assembler; duração/overclock do exemplo de módulo KubeJS; boiler wireless, Dehydrators,
  Vacuum Drying Furnace, nomes Rocket Engine e HUD Flux Matrix. Cliente e save originais intactos.

### G-0122 (2026-09-26) — Boiler wireless, Dehydrator, HUD, JEI e módulos KubeJS

- Large Steam Solar Boiler: o padrão estático e o dinâmico aceitam
  `STEAM_EXPORT_FLUIDS` GTNA. GameTest forma com saída wireless, rejeita entrada wireless na
  posição de saída e executa receita que entrega 100 mB de vapor ao tanque remoto.
- Dehydrator: registros e receitas shaped em todos os tiers elétricos LV–OpV, com nomes
  en_us/pt_br e recursos gerados. Rocket Engine EV/IV/LuV recebeu os nomes originais em ambas
  as línguas. Os GameTests conferem as receitas dos Dehydrators.
- Flux Matrix: botão de HUD, estado/sincronização de saldo e fluxo da rede, editor de posição e
  configuração; o HUD foi observado no cliente isolado. O cliente original do autor permaneceu
  aberto, e uma cópia do save `New World` foi usada no worktree para esse teste.
- Preview JEI do Component Assembler: o relatório real mostrava acesso fora dos limites após
  troca de página. Uma guarda client-side ignora slots obsoletos. Compilação e abertura geral do
  cliente passaram; a navegação exata que causava o crash ainda não foi repetida após a correção.
- API KubeJS: `SubPatternEventJS.add` aceita velocidade e overclock perfeito; o bônus só conta
  para módulos aceitos e formados no controlador de múltiplas receitas. Aliases de Parallel
  Hatch, Overclock Hatch e Accelerate Hatch estão expostos. O exemplo
  `examples/kubejs/server_scripts/gtna_multiple_recipes_module.js` carregou sem erro e
  registrou dois módulos no Integrated Ore Processor. A aceleração de uma receita com módulo
  externo formado ainda não foi medida no cliente.
- Candidatos GTO/GTNL documentados em
  `docs/roadmap/NEXT-GTO-GTNL-CANDIDATES-2026-09-26.md`, com fontes e dependências.
- Gate: `./gradlew spotlessCheck compileJava runUnitTests runGameTestServer runData --offline`
  passou com **98/98 GameTests** e datagen `written: 0` (`/tmp/gtna-0122-final-gate.log`);
  `git diff --check` passou. O envio a `main` foi autorizado pelo autor após os testes.

### G-0121 (2026-09-26) — Dehydrator IV, receita do Vacuum Dryer e deadlock de módulos

- `GTNAMachines3.DEHYDRATOR[IV]` usa `SimpleTieredMachine` e o recipe type Dehydrator existente.
  Texturas originais do GTO copiadas sob CC BY-NC-SA; fonte declarada em `GTNASources` e
  `THIRD_PARTY_NOTICES.md`.
- Receita shaped `WCW/AMA/PRP` do GTO registrada com os componentes IV do GTCEu via
  `registerMachineRecipe`; receita Assembler do controlador Vacuum Drying Furnace restaurada com
  quatro Dehydrators IV e todos os ingredientes originais. O GameTest confirma ambas as receitas
  e o item da receita shaped. `ControllerRecipePolicyTest` não a considera mais omitida.
- Thread dump do GameTest em `/tmp/gtna-gametest-thread-dump.txt` mostrou o servidor esperando o
  lock da checagem do padrão enquanto a thread assíncrona, segurando esse lock, aguardava
  `getChunkAt` em `gtna$setModuleCount`. Agora o pacote é agendado para a thread do servidor,
  sem leitura de chunk na thread assíncrona; contagem de módulos é `volatile` e pacotes obsoletos
  são descartados.
- `./gradlew spotlessApply spotlessCheck compileJava runUnitTests runGameTestServer runData --offline`
  passou com **97/97 GameTests** (`/tmp/gtna-dehydrator-deadlock-gate.log`), datagen `written: 5`
  pelos novos recursos. Segunda execução de `runData --offline` terminou com `written: 0`
  (`/tmp/gtna-dehydrator-datagen-repeat.log`). O primeiro `runClient --offline` encontrou en_us
  gerado sem as chaves Jade; limpar somente os arquivos em `.cache` do datagen e executar
  `runData --offline` novamente restaurou as traduções. O segundo `runClient --offline` carregou
  o mundo `New World` e segue aberto para o autor. `git diff --check` passou. Sem commit/push.

### G-0120 (2026-09-26) — Evaporation Plant: orientação e atualização natural

- Teste de módulo ampliado para controladores voltados a norte, leste e sul. A célula `F` inferior
  coincide com a posição do Parallel IV no mundo de teste do autor. Cada variante verifica duas
  substituições sucessivas: hatches em `F`, depois em `G/A`, sem `GTNAStructureRefresh.refresh` após
  as trocas. O teste também remove e recoloca um tubo do módulo: ele cai e volta automaticamente,
  mantendo os hatches. O Parallel IV concede quatro paralelos e o Accelerate aparece em `getParts()`.
- **Ainda não confirmado in-game:** o autor relata rejeição dos dois hatches; é necessária a
  posição exata da tentativa e o diagnóstico da estrutura para reconciliar o relato com o teste.
  Depois informou que Accelerate passou a ser aceito, mas Parallel não. O save de 07:13 contém um
  Parallel IV já formado na torre sul; se a tentativa for nessa torre, o limite global de uma
  hatch paralela explica a rejeição de uma segunda. A outra torre está inválida no save.
  Não há Fluid Input Hatch entre os blocos com entidade da segunda base; o padrão da Evaporation
  Plant exige exatamente uma entrada de fluido.
- Leitura do save após o client voltar a abrir: o Parallel UHV em `(-10,-58,-117)` está formado com
  `currentParallel=1024`; o Accelerate UXV em `(-10,-59,-117)` e a primeira torre também estão
  formados. A segunda torre permanece inválida. Não generalizar essa observação para toda posição
  possível do módulo até o autor identificar onde a tentativa falhou.
- `./gradlew spotlessCheck compileJava runUnitTests runGameTestServer runData --offline` passou:
  **97/97 GameTests**, datagen `written: 0` (`/tmp/gtna-evap-rebuild-gate.log`). Sem commit/push.

### G-0119 (2026-09-26) — Evaporation, Rocket Engine e Component Assembly Line

- Evaporation Plant: teste novo substitui duas células `F` por Parallel IV e Accelerate após formar
  o módulo em casing puro; `GTNAStructureRefresh.refresh(plant, false)` mantém módulo e hatches sem
  force scan, e `getCurrentParallel() == 4`. O autor ainda reportou rejeição ao instalar no módulo:
  `G` tem a mesma textura de `F` e `A` é a casca exposta. O GTNA passou a aceitar Parallel/Accelerate
  em `F`, `G` e `A` com as mesmas instâncias de predicado para manter limite global de um por tipo;
  o GameTest foi ampliado para `G/A` sem force scan. É uma adaptação de usabilidade frente ao GTO,
  cujo padrão original libera apenas `F`.
- Auditoria dos módulos GTO: EBF, Liquefaction, Evaporation, Cold Ice, Rocket Large Turbine,
  Supercritical Steam Turbine e Component Assembler; bônus, limites e encaixes documentados em
  `docs/fixes/2026-09-26-gto-module-audit.md`. Liquefaction e as saídas extras das turbinas ainda
  precisam de confirmação manual específica das posições no preview.
- Rocket Large Turbine: receita original GTO `ABA/CDC/EFE` usa **Rocket Engine EV**, não Advanced
  Rocket Engine III. Motores EV/IV/LuV portados via gerador simples GTCEu e texturas GTO; recipes
  shaped seguem a cadeia original, com cabos registrados como blocos no GTCEu 7.5.3. Teste de
  receita do controlador atualizado de ausente para presente.
- Component Assembly Line: símbolos `F/H/I/R/S/W/X/Z` agora usam Molecular, Boron Carbide, Precision
  Processing, Advanced Assembly Line, Chemical Resistant Pipe, Circuit Assembly Line, Spacetime
  Assembly Line e Pressure Containment casings do GTO. Texturas, CTM/metadados e bloco molecular
  com bloom portados. Receitas originais preservadas quando seus ingredientes existem; Boron
  Carbide Ceramics vira Boron + Carbon, Pikyonium vira Ruridit, Scandium líquido vira Rhodium e o
  Spacetime Unit usa uma receita de Assembler com materiais GTCEu disponíveis. O advanced unit
  usa a textura estática; a animação ActiveBlock exclusiva do GTO não foi reproduzida.
- UI de Component Assembly: `.rtui` binário original do GTO sob namespace GTNA, com as duas barras
  de progresso referenciadas pelo layout. `GTRecipeWidgetMixin` apresenta EU/t real inclusive com
  overclock; tier rotulado `Casing Tier`. Ainda requer inspeção visual no cliente para confirmar
  compatibilidade da UI custom com GTCEu 7.5.3.
- Tooltips de Chemical Plant e Supercritical Steam Turbine reorganizadas em seções técnicas GTO,
  mantendo lore e só a linha de Source.
- Validação: `./gradlew spotlessCheck compileJava runUnitTests runGameTestServer runData --offline`
  passou com **95/95 GameTests** (ver `/tmp/gtna-final-gate.log`). Repetição após copiar o `.rtui`
  em `/tmp/gtna-final-gate2.log`, e gate final após ampliar os encaixes `A/G` e remover a descrição
  duplicada em `/tmp/gtna-evap-expanded-gate.log` — também **95/95**, datagen verde. Cliente
  reiniciado para inspeção manual. Sem commit/push.

### G-0118 (2026-09-26) — Hatches limitados ao padrão e telas GTO revisadas

- Removido `PredicatesMixin`, que anexava Accelerate/Overclock Hatch a todos os
  `autoAbilities` elétricos. Os padrões com encaixe explícito continuam aceitando esses hatches;
  os casings da base Cold Ice e Electric Blast Furnace agora os rejeitam. GameTests cobrem
  a rejeição no corpo principal e a formação com o módulo.
- A Blaze sempre mostra o limite de 64 paralelos, inclusive durante a receita. O rotor do Grinding
  Ball Hatch usa iluminação plena para corrigir a hélice escura; a aparência final depende de
  confirmação visual no cliente.
- Isa Mill mostra paralelo máximo 2 e aviso de esfera ausente. Flotation Cell e Vacuum Drying
  receberam o mesmo padrão de paralelo, voiding e estado sem receita; Vacuum Drying também mostra
  temperatura das bobinas. As tooltips técnicas e histórias dos ports GTO foram complementadas,
  mantendo somente `Source` como atribuição.
- Gate completo `spotlessCheck compileJava runUnitTests runGameTestServer runData --offline` passou;
  94/94 GameTests e datagen. `git diff --check` passou. Reteste manual de UI, tooltip e brilho da
  hélice pendente. Sem commit/push.

### G-0117 (2026-09-26) — CTM, hélice, cores e padrão das tooltips GTO

- Causa do CTM quebrado encontrada: faltavam `blaze_casing_ctm.png.mcmeta` e
  `cold_ice_casing_ctm.png.mcmeta`, necessários para dividir e animar as folhas verticais. Os
  arquivos copiados do GTO são byte-idênticos aos originais, assim como as folhas PNG.
- `BallHatchRenderer` usa os sprites originais 48×48/48×192 do GTO para o rotor parado/girando;
  a renderização dinâmica percorre quatro quadros sem renderizar o item da esfera. O aspecto
  visual no cliente ainda precisa de confirmação do autor.
- Blaze UI recebeu as cores pedidas. As demais processadoras GTO desta leva exibem voiding,
  ausência de receita e o limite de paralelo quando existe; Jade mostra os multiplicadores
  calculados da receita ativa e informa se o chunk não está forçado. O Jade mantém o EU/t real
  do GTNA, que pode diferir do GTO devido ao overclock específico do gtolib.
- `GTNAGTOTooltips` substitui os resumos das 16 máquinas de `GTNAMachines3` por linhas de lore
  originais disponíveis no GTO e seções técnicas legíveis; onde o port difere do GTO, os valores
  descrevem a implementação atual. A atribuição é só `Source`; o cabeçalho e o rodapé extras
  `GTO Core | Machine`/`GTOCore` foram removidos.
- Gate completo `spotlessCheck compileJava runUnitTests runGameTestServer runData --offline` verde
  após o último ajuste: 26 unitários, 94/94 GameTests e datagen; a checagem incremental de
  `spotlessCheck compileJava runUnitTests` também passou.
- Sem commit/push. `runClient --offline` foi relançado após o gate, chegando à tela do cliente;
  reteste visual do autor necessário para CTM, rotor, tooltips e UI.

### G-0116 (2026-09-26) — Correção de re-scan de módulos e comparação da Blaze

- O GameTest do Cold Ice Freezer passou a alterar uma célula válida da extensão sem chamar
  `GTNAStructureRefresh.refresh`: quebrar baixa o contador e recolocar o bloco restaura a extensão.
  A causa era o loop de volume usar apenas o limite da primeira aisle (`centerOffset[3/4]`), em
  vez da profundidade (`getDimensions()[0]`), perdendo o bloco do cache após a quebra.
- A UI da Blaze Blast Furnace ganhou as linhas de paralelo 64, voiding, heat capacity e
  `No Recipe found` que a captura comparativa do GTO mostrou. A tooltip da Blaze foi refeita
  com a lore literal e requisitos técnicos originais; outros ports continuam pendentes de 1:1.
- O Jade de blocos GTNA passou a exibir hardness e blast resistance. A chave de configuração
  exigida pelo Jade foi adicionada; os números inteiros aparecem sem `.0`.
- `./gradlew spotlessCheck compileJava runUnitTests runGameTestServer runData --offline` passou:
  26 testes unitários, 94/94 GameTests, datagen; `spotlessCheck compileJava runUnitTests` passou
  novamente após o ajuste de formatação numérica do Jade.
- Sem commit/push. Cliente de desenvolvimento relançado para teste manual do autor.

### G-0115 (2026-09-26) — Primeira rodada das correções de fidelidade do QA GTO

- **F-02:** o log do cliente mostrou `Dynamic render type with ID gtna:ball_hatch/ball does not exist` durante o bake do modelo. `ClientProxy` agora registra o codec do `BallHatchRenderer` antes do bake; o registro tardio em `CommonProxy` também foi alinhado. O renderer atual de item foi mantido como plano B do documento. `runClient --offline` alcançou o menu e completou o bake do atlas sem `Dynamic render type`/`Failed to load model` para o hatch; ainda conferir hatch vazio e com esfera dentro do mundo.
- **F-03:** o ISA Mill recebeu paralelo de conteúdo de até 2 receitas via `ParallelLogic.getParallelAmount`, limitado por insumos e espaço de saída. O modificador multiplica conteúdos, EU/t e `recipe.parallels`, de modo que o dano da esfera use o paralelo efetivo. Isto é uma aproximação documentada do batch/OC do gtolib; repetir o protocolo de 60 s e medir EU/t, duração, saída e desgaste antes de declarar paridade.
- **F-04:** um provider Jade do ISA Mill mostra `Need to grind ball` quando a estrutura está formada e a esfera falta, além dos multiplicadores reais de EU/t e duração em relação à receita original. As linhas genéricas de progresso, energia, paralelo, saídas e estrutura continuam providas pelo GTCEu. Ainda faltam a compensação energética/temporal exata do gtolib, o formato completo de energia e o aviso de chunk não forçado.
- **F-05:** o cache de posições do controller agora inclui o volume inteiro de cada sub-pattern, inclusive células depois da primeira falha. Isso permite revalidar quando qualquer bloco do módulo muda. Se o pacote de contagem chegar antes do block entity no cliente, ele fica pendente e é aplicado assim que a entidade carregar. A sincronização na reentrada ainda deve ser testada no cliente; falta GameTest de quebra e recolocação para todas as extensões.
- **F-06:** todos os `BlockItem` registrados pelos helpers de `GTNABlocks` mostram dureza e resistência a explosão, formatadas como no scanner do GTCEu; tradução en_us/pt_br incluída.
- **F-08:** EMI foi retirado apenas do runtime de desenvolvimento para evitar o `AbstractMethodError` com JEI e os avisos de IDs duplicados emitidos pelo EMI. JEI permanece no runtime de dev. A origem dos IDs repetidos não foi investigada separadamente.
- **F-07:** o autor escolheu seções técnicas e lore do GTO. `GTNASources` acrescenta a lista localizada de recipe types às máquinas GTO e as histórias de seis ports com correspondência verificada (Fishing Ground, Component Assembler, Blaze Blast Furnace, Cold Ice Freezer, ISA Mill e Industrial Flotation Cell), antes da atribuição `Source:`. A linha sobre o limite do Component Assembler foi adaptada para IV base/UV com extensão, refletindo o port. As demais máquinas não tinham lore correspondente confirmado no GTOCore. A padronização fina de números em cada UI ainda está aberta.
- **F-01:** texturas de duas camadas e brilho continuam pendentes de comparação visual no cliente. Os mcmeta de bloom já marcam `emissive` e `shimmer`, mas a implementação nativa do helper do gtolib não está disponível para comparação; nenhuma paridade visual foi presumida.
- **Validação:** `spotlessCheck compileJava runUnitTests runGameTestServer runData --offline` passou depois de todas as mudanças (26/26 testes unitários, banner `94 GAME TESTS COMPLETE`); a primeira geração escreveu 1 arquivo de idioma, e a segunda `runData` foi determinística (`written: 0`). Sem commit/push. Reteste in-game ainda necessário para F-01 a F-05 e revisão das tooltips F-07.


### G-0114 (2026-09-26) — Extensão até UV: Component Assembler + component_assembly_line

- **Corte LuV → UV.** O teto das duas máquinas subiu para UV, o máximo com receitas de lote no
  GTCEu base: `ComponentAssemblerMachine` usa `Math.min(GTValues.UV, ...)` com módulo formado
  (igual ao `ComponentAssemblerMachine` do GTO) e `ComponentAssemblyLineMachine` usa
  `Math.min(GTValues.UV, ...)`; as regras uniformes `componentAssemblyTierCasings()` e
  `componentAssemblyLineTierCasings()` ganharam ZPM/UV.
- Blocos novos com texturas do GTOCore `component_assembly_line_casing_zpm/uv.png`
  (CC BY-NC-SA; `THIRD_PARTY_NOTICES.md` atualizado): `COMPONENT_ASSEMBLY_CASING_ZPM/UV` (família da
  extensão) e `COMPONENT_ASSEMBLY_LINE_CASING_ZPM/UV` (família da linha), com lang en + pt_br e
  modelos/blockstates/loot tables gerados pelo datagen.
- Receitas: `GTNAComponentRecipes#assemblyLineZpmBatches`/`assemblyLineUvBatches` portam
  `ComponentRecipes.java:45-46` 1:1 (oito lotes por tier, `fluidMultiplier = 4`, L = 144,
  `component_casing_tier = 7/8`, 2400 ticks, `VA[ZPM]/VA[UV]`), incluindo o caso especial UV do
  motor (Amerício `L * 192` no lugar de `L * 24 * fluidMultiplier`). Total do recipe type:
  **48 → 64** lotes (40 base LV–IV + 8 LuV + 8 ZPM + 8 UV). Todos os materiais desses 16 lotes são
  GTCEu/GTNA (`MarM200Steel` é o port do GTNA), sem substituições; `arm_zpm`/`arm_uv` usam os
  circuitos tier-1/tier-3 corretos (LuV+EV e ZPM+IV).
- Produção das carcaças ZPM/UV: Assembly Line do GTO com estação de pesquisa no tier anterior da
  mesma família (`AssemblyLine.java:2297`/`:3613`). Únicas substituições (documentadas em
  `GTNABlockRecipes` e no `THIRD_PARTY_NOTICES.md`): Pikyonium 2016 → **Trinaquadalloy 2016**
  (liga ZPM do GTNA, já obtenível), ArtheriumTin 2304 →
  **EnrichedNaquadahTriniumEuropiumDuranide 2304** (supercondutor UV de 9900 K vs. os 9800 K do
  GTO) e AbyssalAlloy 1152 → **RutheniumTriniumAmericiumNeutronate 1152** (supercondutor UHV de
  10800 K, o mesmo blast do GTO). Indalloy140, Neutrônio e Lubrificante são 1:1; o `plateDouble`
  de Tritânio é gerado pelo GTCEu sem flag nova.
- QA (GameTests): `componentAssemblerExtensionFormsInTwoLayers` agora usa carcaças UV e confirma o
  teto UV; `componentAssemblerExtensionRaisesTheTierGate` percorre LuV→ZPM→UV (lote ZPM rejeitado
  sem a carcaça ZPM e aceito com ela; UV idem); o novo `componentAssemblerExtensionRunsTheUvBatch`
  executa o lote UV de verdade; a contagem 64 foi atualizada em
  `componentAssemblerLoadsAllBatchRecipes`/`componentAssemblerRequiresMatchingCasingTier` e nos dois
  testes da linha; `componentAssemblyLineGatesTheTier` cobre ZPM/UV. Lang e tooltips mencionam o
  teto UV.
- Validação: `./gradlew spotlessCheck compileJava runUnitTests runGameTestServer runData --offline`
  verde com **26/26 testes unitários** e **All 94 required tests passed** (`TEST-gtna.xml` com os 16
  testes de componente, incluindo o novo); `runData` repetido com `written: 0`.
  `PortLangParityTest`, `MachineTooltipContractTest` e `ControllerRecipePolicyTest` verdes.
- Pendências: QA in-game do autor (JEI dos lotes ZPM/UV, receitas das carcaças na Assembly Line,
  texturas e tooltips en/pt_br) segue aberta; sem commit/push. Se o autor preferir fidelidade total
  nas soldas, Pikyonium e ArtheriumTin podem ser portados 1:1 (todos os componentes existem no
  GTCEu/GTNA); AbyssalAlloy depende do gás `BarnardaAir`, exclusivo do GTO.

### G-0113 (2026-09-26) — Correções do QA in-game do autor

- **Cold Ice Freezer — recipe type não liberava no cliente:** o contador de módulos
  (`gtna$formedModuleCount`) existia apenas no servidor; o cliente sempre lia 0 e o seletor de modos
  filtrava fora o `atomization_condensation`. Adicionado `SModuleCountPacket` (S2C) registrado em
  `GTNANetworkHandler` e enviado no fim de `MultiblockControllerMachineMixin.checkPattern()` quando o
  valor muda. A lógica client vive em `client.ModuleCountClientHandler` (`@OnlyIn(CLIENT)`), senão o
  dedicated server quebra ao registrar o canal (o primeiro build travou o GameTest por isso).
- **Cold Ice Freezer — módulo não re-detectava:** quando um bloco do módulo é quebrado e recolocado,
  a célula que falhou não entra no `MultiblockState.cache` (o `BlockPattern` aborta antes do
  `addPosCache`), então o controller não era notificado. O mixin agora registra a posição do erro do
  sub-pattern no cache, e o módulo re-forma sem scan manual.
- **CTM/bloom dos casings:** `blaze_casing` e `cold_ice_casing` são casings de duas camadas no GTO
  (base + `_bloom` emissivo). Foram copiadas as texturas `_bloom`/`_ctm_bloom` (+ mcmeta) e os
  modelos passaram a usar `gtceu:block/cube_2_layer/all`; os cascos MK2 usam
  `cube_2_layer/bottom_top` com `side_bloom` (antes o bloom estava documentado como não portado).
- **Recipe types sem lang:** adicionadas as chaves `gtna.<tipo>` que faltavam em `en` e `pt_br`
  (`isa_mill`, `rocket_engine`, `supercritical_steam_turbine`, `flotating_beneficiation`,
  `vacuum_drying`, `dehydrator`, `atomization_condensation`, `annihilate_generator`, além das antigas
  sem pt_br).
- **Tooltip de módulo:** agora anuncia também os *recipe types* liberados
  (`gtna.machine.auxiliary_module.recipe_types`), como o `moduleTooltips(abilities, recipeTypes)` do
  GTO; o Cold Ice lista Atomization Condensation. O GameTest do módulo foi atualizado para as 5
  linhas.
- **Tooltip do ISA Mill:** destaca o Perfect Overclock (÷4 por degrau de tensão).
- **UI de threads:** `WorkableElectricMultipleRecipesMachine` só mostra o painel de threads quando
  `getMaxThreads() > 1` (Thread Hatch instalada). Corrige o "Thread 1: Unknown" na flotação/secagem.
- **Assembly line — casings:** `iridium_casing` portado do GTO (bloco + textura ctm + receita de
  Assembly Line com o `Tanmolyium` copiado 1:1 em `MaterialBuilder`), usado nas células A/K,
  `appearanceBlock`/overlay atualizados; `naquadah_alloy_casing` corrigida para a textura
  `hyper_mechanical_casing` do GTO (antes usava a de purificação de água). O GameTest da line
  constrói A/K com o irídio.
- **Ball Hatch render:** `BallHatchRenderer` (DynamicRender, client) mostra a esfera guardada
  flutuando e girando na frente da hatch — devagar em idle, rápido com a máquina trabalhando
  (`isWorking` já era `@DescSynced`). Desvio documentado: o GTO usa os sprites achatados
  `ball_hatch_idle`/`spinning`; o GTNA renderiza o item real.
- **ISA Mill — divergência de throughput em investigação:** o autor reportou GTO 34 ops vs GTNA 25
  ops com o mesmo hatch UV (contagens de stacks+itens). A receita e o overclock do port batem com o
  GTO (48 saídas, 2400 ticks, ÷4 por degrau = 9 ticks); a hipótese aberta é o paralelismo de conteúdo
  do gtolib (`accurateContentParallel`, não portado) ou janelas de tempo diferentes. Teste
  controlado (mesmo tempo de parede) marcado para a próxima sessão com o cliente aberto.
- Validação: `spotlessCheck compileJava runUnitTests runGameTestServer runData --offline` passou com
  **26/26 testes unitários** e **93/93 GameTests**; `runData` repetido com `written: 0` (a primeira
  execução escreveu 16 arquivos novos dos blocos/modelos). QA in-game pendente dos itens visuais.

### G-0112 (2026-09-26) — Módulo de atomização do Cold Ice Freezer + contratos globais de QA

- Port do sub-pattern de atomização do GTOCore (`MultiBlockD.java:367-388`) para o controlador
  `gtna:cold_ice_freezer` existente, registrado em `GTNAModules` como módulo GTNA: torre de 9×7×7
  com `NAQUADAH_ALLOY_CASING`, `GCYM HEAT_VENT`, `COLD_ICE_CASING`, moldura de Naquadah,
  `CASING_TUNGSTENSTEEL_PIPE` e `CASING_ALUMINIUM_FROSTPROOF`; as células 'B' aceitam até 6 Energy
  Hatches e uma Accelerate Hatch (`GTNAPartAbility.ACCELERATE_HATCH`; o GTCEu 7.5.3 não tem
  `PartAbility.ACCELERATE_HATCH`, desvio documentado no javadoc do módulo).
- Comportamento fiel ao `ColdIceFreezerMachine.recipeTypeAvailable`: o recipe type
  `ATOMIZATION_CONDENSATION_RECIPES` (novo em `GTNARecipeType`) só aparece em `getRecipeTypes()` /
  `getRecipeType()` quando o módulo está formado (`IGTNAModuleHost.gtna$formedModuleCount() > 0`),
  com guarda extra em `beforeWorking`.
- Receitas: `GTNAAtomizationRecipes` porta o subconjunto de `GTOMaterialRecipeHandler.java:425-455`
  para todos os materiais GTCEu/GTNA com dust + fluido: fluido→dust, molten→dust (circuito 1) e
  molten→líquido (circuito 2), com o gás inerte escalado pela massa e o gate de hélio líquido para
  blast ≥ 5000 K. `GTOUtils.getVoltageMultiplier` (gtolib nativo) foi substituído pela fórmula
  equivalente do GTCEu (`blast ≥ 2800 K ? 30 : 8 EU/t`).
  **Substituição documentada:** o GTO usa gases de alta pressão exclusivos
  (`GTOFluidStorageKey.HIGH_PRESSURE_GAS`, produzidos no Gas Compressor não portado); o GTNA usa o gás
  regular na mesma quantidade, mantendo a semântica e evitando um fluido inobtenível.
- `Naquadah` ganhou `GENERATE_FRAME` em `MaterialAdd` (o GTO usa moldura de Naquadah; o GTCEu 7.5.3
  não a gera).
- Lang en + pt_br (`tooltip.2` atualizado para descrever o módulo); `GTNASources` já tinha a entrada.
- QA: GameTest `coldIceFreezerAtomizationModuleUnlocksSecondRecipeType` cobre A1 formação base,
  A2 negativo, A3 hatches + limite de 6 energias + rejeição de Fluid Hatch em casing puro, A4 recipe
  type só com o módulo, A5 upkeep de gelo com o módulo formado e A6 contagem dinâmica do gerador vs.
  recipe manager; A7 fica no contrato unitário de política de controlador.
- Contratos globais de QA criados nesta etapa (regra adicional do autor): `PortLangParityTest`
  (161 chaves `pt_br` que faltavam traduzidas), `PortChainClosureTest`,
  `MachineTooltipContractTest`, `PartAbilityCoverageTest` estendido (GTNAMachines3 + módulos) e
  `ControllerRecipePolicyTest`. A receita do controlador do `mega_alloy_blast_smelter`
  (`classified/Vanilla.java:564`, só GTCEu/GCYM) foi portada porque o `ControllerRecipePolicyTest`
  a exigia e ela não existia.
- Validação final do lote (2026-09-26, após a máquina 1 também entrar no
  `newGtoControllersDescribeTheirFunction` junto com ISA Mill e as duas turbinas):
  `spotlessCheck compileJava runUnitTests runGameTestServer runData --offline` passou com
  **26/26 testes unitários** e **93/93 GameTests**; `runData` repetido duas vezes com
  `written: 0`. QA in-game do módulo (visual do sub-pattern, tooltip e JEI) pendente.

### G-0111 (2026-09-26) — QA retroativo: `chemical_plant` e `mega_alloy_blast_smelter`

- QA A1–A7 retroativo das duas máquinas, que em G-0102/G-0103 só tinham formação/dimensões e
  tooltips. Nenhum código de máquina mudou; o único ajuste de produção foi extrair o padrão do mega
  para `GTNAMachines3.MEGA_ALLOY_BLAST_SMELTER_PATTERN`, para o GameTest construir exatamente a
  mesma fonte que a definição registra (verificado string a string contra o
  `GCYMMachines.java:898-908` do GTOCore).
- `chemicalPlantFormsWithLargeChemicalRecipes` (estendido):
  - A2: `Blocks.GOLD_BLOCK` no lugar de uma bobina não forma; restaurar o `COIL_KANTHAL` exato
    reforma e mantém o coil tier 1.
  - A3: Parallel Hatch IV aceita numa célula 'b' (`getParallelHatch().isPresent()`); Fluid Hatch
    rejeitada na célula exclusiva de tubulação PTFE 'd'.
  - A4: sem módulo/sub-pattern GTNA registrado (`GTNASubPatterns.get(...).isEmpty()`).
  - A6: família LARGE_CHEMICAL_RECIPES não vazia + receita `blaze_casing_gtna_route` presente (a
    família é compartilhada com o GTCEu, então não há contagem estável).
  - A7: ausência intencional do controlador (comentário em `GTNAMachineRecipes.java:889` e lista
    `OMITTED` do `ControllerRecipePolicyTest`) confirmada em `CRAFTING` e `ASSEMBLER_RECIPES`.
- `chemicalPlantRunsLargeChemicalRecipe` (novo, A5): roda a rota real `blaze_casing_gtna_route`
  (1 High-Temperature Smelting Casing + 32 Tin Foil + 1440 mB Blaze + 576 mB Gallium Arsenide +
  288 mB Vanadium Gallium) com 3 fluid import hatches, bobina Kanthal (0,95×) e hatch LuV; duração
  < 900 (base 900) e 1 Blaze Casing na output bus.
- `megaAlloyBlastSmelterFormsWithParallelHatch` (novo, A1–A3): constrói o padrão registrado
  11×18×11 (HSSG 5400 K, Muffler obrigatório virado para cima, Maintenance exact 1, energia IV) e
  aceita a Parallel Hatch IV; A2 Gold Block na carcaça; A3 Fluid Hatch rejeitada na célula
  exclusiva de Heat Vent e **Item Export Bus rejeitado** porque o tipo ALLOY_BLAST tem 0 outputs de
  item (`setMaxIOSize(9, 0, 3, 1)`), então o `autoAbilities` não oferece EXPORT_ITEMS.
- `megaAlloyBlastSmelterRunsAlloyBlast` (novo, A5): roda a receita GTCEu manual de Potin
  (`gtceu:alloy_blast_smelter/potin`: 6 Copper + 2 Tin + 1 Lead + circuito 9, 1084 K, 300 ticks
  base) e entrega **1296 mB** de Potin líquido; bônus 0,8× EU / 0,6× duração + EBF overclock do
  hatch IV aplicados.
- `megaAlloyBlastSmelterMatchesGtoDefinition` (estendido): A6 família não vazia + receitas nomeadas
  (`potin`, `inconel_625`); A7 receita shaped do controlador presente em `RecipeType.CRAFTING`
  (portada do `classified/Vanilla.java:564`).
- A4 nas duas máquinas: não há módulo/extensão GTNA, coberto por asserção explícita.
- Erros diagnosticados no primeiro run do GameTest e corrigidos: ids de receita de tipo GTCEu são
  prefixados pelo path do tipo (`gtceu:alloy_blast_smelter/potin`), então os filtros usam
  `endsWith`; e `autoAbilities` só aceita EXPORT_ITEMS quando o tipo tem output de item.
- Validação: `./gradlew spotlessCheck compileJava runUnitTests runGameTestServer runData --offline`
  verde com **26/26 testes unitários** e **All 93 required tests passed**
  (`TEST-gtna.xml` com 93 testcases e 0 falhas), e `runData` repetido com `written: 0`.
- Pendências: QA in-game do autor das duas máquinas (JEI/receitas, tooltips en/pt_br,
  orientação/colocação e o visual do MEGA) segue aberta; sem commit/push.

### G-0110 (2026-09-26) — component_assembly_line

- `component_assembly_line` portado do GTOCore (`MultiBlockA.java:1927`) sobre o
  `ComponentAssemblyLineMachine` (base `WorkableElectricMultiblockMachine`): regra de carcaça única
  (a família nova `COMPONENT_ASSEMBLY_LINE_CASING_LV..LUV`, texturas do GTOCore em
  `block/casings/component_assembly_line/`, CC BY-NC-SA), portão de receita
  (`component_casing_tier <= tier da carcaça`, teto LuV; ZPM..MAX fora do escopo), Parallel Hatch e
  overclock não-perfeito — o mesmo modificador do Component Assembler base. O
  `TierCasingCrossRecipeMultiblockMachine` do gtolib é substituído e a execução cross-recipe (várias
  receitas simultâneas) não é reproduzida; documentado.
- Estrutura: `.mbs` real 47×15×31 (`pattern/gto/component_assembly_line.mbs`), com a tabela de
  substituições GTO→GTNA documentada abaixo. `Naquadria` recebeu `GENERATE_FRAME` (o GTOCore faz o
  mesmo para o anel da linha).
- Receita do controlador: omitida e registrada no `ControllerRecipePolicyTest` (o original é uma
  receita de Assembly Line com a Advanced Assembly Line/Advanced Assembly Line Unit/Mithril do GTO).
- Tabela de substituições (símbolo → bloco GTO → bloco GTNA):
  `A`/`K` IRIDIUM_CASING → `HYPER_MECHANICAL_CASING`; `F` MOLECULAR_CASING → GCYM `CASING_ATOMIC`;
  `H` BORON_CARBIDE... → `LITHIUM_OXIDE_CERAMIC_HEAT_RESISTANT_SHOCK_RESISTANT_MECHANICAL_CUBE`;
  `I` PRECISION_PROCESSING_MECHANICAL_CASING →
  `COBALT_OXIDE_CERAMIC_STRONG_THERMALLY_CONDUCTIVE_MECHANICAL_BLOCK`;
  `R` ADVANCED_ASSEMBLY_LINE_UNIT → `CASING_ASSEMBLY_CONTROL`;
  `S` CHEMICAL_CORROSION_RESISTANT_PIPE_CASING → `CASING_PTFE_INERT`;
  `W` MACHINE_CASING_CIRCUIT_ASSEMBLY_LINE → GCYM `CASING_LARGE_SCALE_ASSEMBLING`;
  `X` SPACETIME_ASSEMBLY_LINE_UNIT → `SPACETIME_COMPRESSION_FIELD_GENERATOR`;
  `Z` PRESSURE_CONTAINMENT_CASING → `HYPER_PRESSURE_BREEL_CASING`;
  `U` `light()` = GTCEu `LAMPS` → predicado das mesmas lâmpadas GTCEu. Os demais símbolos existem
  1:1 no GTCEu/GTNA (`NAQUADAH_ALLOY_CASING`, nonconducting, HERMETIC LuV, FILTER_CASING,
  CLEANROOM_GLASS, ELECTROLYTIC_CELL, CASING_ASSEMBLY_LINE, molduras Hastelloy-N/HSLA/Naquadria,
  pia de PTFE). Thread/Overclock/Accelerate Hatch do GTO não entram porque o GTNA não tem a lógica
  cross-recipe; entram Parallel, Maintenance, energia, laser e IO.
- QA: 7 GameTests novos (A1 formação pelo `.mbs` real + orientação + 63 células de tier, A2 bloco
  inválido e tier misturado, A3 hatches no casco K e rejeição em célula de tier, A4 portão LV→LuV,
  A5 execução do lote LuV com consumo, A6 as 48 receitas, A7 controlador omitido).
- Validação: gate completo `spotlessCheck compileJava runUnitTests runGameTestServer runData --offline`
  verde com **26/26 testes unitários** e **90 testes de GameTest** (84 do arquivo principal, incluindo
  os 14 novos, + 6 de outras classes; todos aprovados); `runData` repetido com `written: 0`. QA
  in-game pendente.

### G-0109 (2026-09-26) — Extensão grande do Component Assembler

- As duas camadas de `addSubPattern` do GTOCore (`MultiBlockC.java:328-397`) foram registradas como
  módulos GTNA em `GTNAModules` sob o controlador `component_assembler`: a primeira 29×6×13 (célula
  'Q' no controlador, casings de tier, Accelerate Hatch e IO) e a segunda 29×6×20 (célula 'H',
  computer casing, control casings MK2, laser/parallel). As duas podem formar ao mesmo tempo, como no
  GTOCore (`getSubFormedAmount()`); o único ponto compartilhado entre elas é o controlador.
- Regra de tier estendida: `componentAssemblyTierCasings()` agora vai até LuV
  (`COMPONENT_ASSEMBLY_CASING_LUV`, textura do `component_assembly_line_casing_luv` do GTOCore). A
  máquina lê o tier das células do padrão principal e aplica o teto do GTOCore: IV sem módulo, LuV
  com módulo (o GTOCore sobe até UV; o GTNA só tem carcaças até LuV — documentado).
- Blocos novos: `THREE_PROOF_COMPUTER_CASING`, `MACHINING_CONTROL_CASING_MK2` e
  `ENERGY_CONTROL_CASING_MK2` (sided, sem a camada de bloom emissiva), `ELECTRIC_POWER_TRANSMISSION_CASING`
  e `TITANIUM_NITRIDE_CERAMIC_IMPACT_RESISTANT_MECHANICAL_BLOCK`, todos com texturas do GTOCore
  (CC BY-NC-SA, `GTNASources`/`THIRD_PARTY_NOTICES.md`). Materiais novos 1:1:
  `CarbonFiberPolyphenyleneSulfideComposite` (moldura) e `TitaniumNitrideCeramic` (flake da receita);
  `Trinium` recebeu `GENERATE_FRAME` (o GTOCore faz o mesmo para a moldura da extensão).
- Receitas: as oito receitas de lote LuV de `ComponentRecipes#assembly_line(LuV)` (2400 ticks,
  `component_casing_tier = 6`, `fluidMultiplier = 2`), 1:1, e a produção da carcaça LuV (Assembly Line
  do GTOCore com `Indalloy140`). As receitas do ramo `ASSEMBLY_LINE_RECIPES` do mesmo método foram
  omitidas por exigirem prefixos de item exclusivos do GTO (motor enclosure, piston housing, ...).
  Caminhos GTNA documentados para os cinco blocos: os cascos de controle MK2 vêm do Precision
  Assembler (máquina excluída), então usam rotas Assembler com a mesma forma; o
  `TitaniumNitrideCeramic` ganhou uma rota Mixer (Ti + N) porque a cadeia do GTO não foi portada.
- Desvio documentado: as células de tier da extensão impõem uniformidade dentro de cada camada, mas o
  tier efetivo continua vindo do padrão base (o mixin restaura o contexto principal após cada
  sub-padrão); o GTOCore compartilha um único contexto entre base e extensão.
- QA: 7 GameTests novos (A1 formação das duas camadas + teto IV→LuV, A2 bloco inválido/tier misturado,
  A3 hatches da extensão e rejeição de hatch em casing, A4 portão de receita LuV, A5 execução do lote
  LuV com consumo, A6 contagem 48 receitas, A7 receita do controlador). Template novo `empty_48`
  (48³) para as estruturas de 29 de largura.
- Validação: gate completo `spotlessCheck compileJava runUnitTests runGameTestServer runData --offline`
  verde com **26/26 testes unitários** e **83/83 GameTests** (76 anteriores + 7 novos); `runData`
  repetido com `written: 0`. QA in-game pendente.

### G-0108 (2026-09-25) — Vacuum Drying Furnace

- Port de `vacuum_drying_furnace` do GTOCore `dc4824d` (`MultiBlockA.java:1707`, palette 1732-1744) e
  do padrão comprimido `pattern/vacuum_drying_furnace.mbs` (3 aisles × 5 rows × 3 chars, orientação
  gravada LEFT/UP/FRONT; controller em char 1, row 0, aisle 2). A casca é `RED_STEEL_CASING` (nova,
  texturas GTO com atribuição CC BY-NC-SA), com 24 células de bobina (`heatingCoils()`), Muffler
  `setExactLimit(1)` na face superior (front virado para o ar interior) e Maintenance
  `setExactLimit(1)`; energias ≤2, item in ≤2 / out ≤1, fluido in ≤2 / out ≤2.
- Comportamento: `VacuumDryingFurnaceMachine extends CoilWorkableElectricMultipleRecipesMachine`
  reproduz o `CoilCustomParallelMultiblockMachine` do GTO por tipo de receita:
  - `VACUUM_DRYING` (modo principal): serial (paralelo 1) com o `UPGRADE_EBF_OVERCLOCK` do GTO,
    reproduzido pelo novo `GTNAHeatingCoilOverclock`. O GTO usa a matemática de bobina do EBF, cujos
    `heatingCoilOC`/`getCoilEUtDiscount`/`getModifier` são package-private no GTCEu 7.5.3; o port
    reimplementa só as duas funções de matemática e reusa `OverclockingLogic.OCParams`/`OCResult`
    (records públicos): portão de `ebf_temp`, desconto de 0,95^n EU/t por 900K acima da receita e
    passos de overclock perfeitos (duração /4) enquanto houver desconto sobrando.
  - `DEHYDRATOR`: paralelo `2^floor(temperatura da bobina / 900)` (máximo com o paralelo do hatch) e
    overclock não-perfeito padrão, como o `UPGRADE_PARALLELIZABLE_OVERCLOCK` do GTO.
  A fórmula de paralelo segue o tipo **selecionado** (`getRecipeType()`), exatamente como o
  `m.getRecipeType()` do GTO; a busca multi-receita do GTNA pode iniciar receitas do outro tipo, o
  que está documentado no javadoc da classe.
- Recipe types novos em `GTNARecipeType`: `VACUUM_DRYING` (0 item in / 9 out / 1 fluido in / 2 out,
  barra `PROGRESS_BAR_ARROW`, som `COOLING`; as infos de JEI de temperatura e bobina vêm do
  `ebf_temp`, como o `BLAST_RECIPES` do GTCEu, mais o widget de bobinas válidas) e `DEHYDRATOR`
  (2/6/2/2, `PROGRESS_BAR_EXTRACT`, som `ARC`), copiando os IO do GTO
  (`GTORecipeTypes.java:664`/`:143`).
- Receitas (`GTNAFlotationDryingRecipes`, registrado no `GTNAGTAddon`): as 12 receitas de secagem do
  `classified/VacuumDrying.java` 1:1 — 4000 mB de foam → 6 pilhas de dusts GTCEu + 200 mB `RedMud` +
  2000 mB `Water`, com EUt (1920–491520), duração 2400 e `blastFurnaceTemp` (3500–9500) originais; a
  única receita do `classified/Dehydrator.java` que só usa GTCEu (`salt_dust`: 1000 mB SaltWater → 2
  Salt, EUt 30, 160 ticks); e a neutralização de Red Mud do `processing/StoneDustProcess.java`
  (RedMud 1000 + HCl 4000 → `NeutralisedRedMud` 2000, Mixer, EUt 128, 100 ticks), que fecha o
  subproduto da secagem. Cinco receitas de bloco/carcaça (Assembler) e a receita do controlador da
  flotação completam o par.
- Substituições/omissões documentadas: GTO usa a data key `temperature` para o JEI; o port lê
  `ebf_temp`, que é o que `blastFurnaceTemp` grava. Os `recoveryStacks` dinâmicos do GTO
  (`tinydustFromDustOutput`) não são portáveis pela API estática `recoveryItems` do GTCEu e ficam
  omitidos, como no ISA Mill. A receita do controlador usa quatro `DEHYDRATOR[IV]` (máquina
  single-block exclusiva do GTO) → **omitida** e registrada em `ControllerRecipePolicyTest.OMITTED`.
  Bloqueio rígido: `NeutralisedRedMud` é terminal por enquanto — a cadeia seguinte do GTO
  (StoneDustProcess: red slurry → titanyl sulfate → titanium tetrachloride / cloretos de terras
  raras) exigiria ~5 fluidos GTO adicionais e está fora do escopo do par. O `trinium_compound` (só
  na Mega Vacuum Drying Furnace, devolve `ResidualTriniiteSolution`) e as demais receitas do
  Dehydrator (dependem de fluidos GTO) não foram portados.
- Lang en (`GTNALangProvider`: nome do bloco/máquina, `tooltip` + 3 linhas) + pt_br
  (`pt_br.json`, inclui nomes de material). Atribuição em `GTNASources`
  (`vacuum_drying_furnace`) e texturas no `THIRD_PARTY_NOTICES.md`.
- QA automático em `GTNAMachineGameTests`:
  - `vacuumDryingFurnaceFormsAndDriesFoam` — A1 formação pelo `.mbs` (3×5×3, orientação gravada) e
    leitura da bobina HSSG (5400K); A2 negativo (Gold Block no lugar da bobina e Fluid Hatch numa
    célula de bobina, que é exclusiva); A3 hatches obrigatórias (Muffler e Maintenance) e
    item/fluido/energia; A4 as duas famílias de receita expostas; A5a execução real do modo
    Dehydrator (SaltWater → 2 Salt; paralelo com bobina 2^6 = 64, mas um lote de 1000 mB roda só: 160
    → 10 ticks em quatro overclocks não-perfeitos); A5b execução real do modo principal (pyrope foam
    → 128 Magnesium + 48 Silicon + 200 mB Red Mud + 2000 mB Water, com overclock EBF de um passo
    perfeito: 2400 → 600 ticks com bobina HSSG e hatch IV); A6 exatamente **12** receitas
    `vacuum_drying` e **1** `dehydrator`; A7 ausência da receita de controlador nos Assembler.
  - `flotationFoamFeedsVacuumDrying` (cadeia fechada): toda espuma produzida pela flotação tem
    receita de secagem, o `RedMud` tem consumidor (Mixer) e uma fornalha formada aceita o
    `PyropeFront` como input (`RecipeHelper.matchContents`). Complementa o
    `PortChainClosureTest` (varredura de fontes), que agora reporta os 12 `*Front` + `RedMud`.
- Validação: `spotlessCheck compileJava runUnitTests runGameTestServer runData --offline` passou com
  **26/26 classes de teste unitário**, **76/76 GameTests** (`All 76 required tests passed`) e
  `runData` repetido com `written: 0`. QA in-game (CTM das texturas, tooltip en/pt_br, JEI das duas
  categorias, rotação/colocação e comportamento sob carga das bobinas) pendente com o autor. Sem
  commit ou publicação.

### G-0107 (2026-09-25) — Industrial Flotation Cell

- Port de `industrial_flotation_cell` do GTOCore `dc4824d` (`MultiBlockA.java:1678`, palette
  1688-1703) e do padrão comprimido `pattern/industrial_flotation_cell.mbs` (9 aisles × 7 rows ×
  7 chars; orientação gravada RIGHT/BACK/UP; controller em char 3, row 0, aisle 1). Carcaças novas
  `HASTELLOY_N_75_CASING`/`_GEARBOX`/`_PIPE` e paredes `FLOTATION_CELL` (texturas GTO, CC BY-NC-SA).
- Comportamento: `IndustrialFlotationCellMachine extends WorkableElectricMultipleRecipesMachine`
  porta o `ElectricMultiblockMachine` + `parallelizablePerfectOverclock()` do GTO: o overclock
  perfeito vira `OverclockingLogic.PERFECT_OVERCLOCK`, que é exatamente o que a lógica
  multi-receitas do GTNA aplica (`ELECTRIC_OVERCLOCK.apply(getOverclockingLogic())`); uma Overclock
  Hatch do GTNA, se algum padrão um dia aceitar uma, continua tendo prioridade. O padrão aceita
  Parallel Hatch (o GTO usa), energia ≤2, fluido in ≤1 / out ≤1, item in ≤2 e Maintenance exata —
  sem Muffler, como no GTO.
- Recipe type novo `FLOTATING_BENEFICIATION` (`GTORecipeTypes.java:658`): 2 item in / 0 out / 1
  fluido in / 1 fluido out, barra `PROGRESS_BAR_BATH` e som `CHEMICAL`.
- Receitas (`GTNAFlotationDryingRecipes`, registrado no `GTNAGTAddon`): as 12 receitas do
  `classified/FlotatingBeneficiation.java` 1:1 (reagente etilxantato 32 ou 64 + 64 MILLED + turpentina
  8000–280000 mB → 1000 mB do foam, EUt 7680/30720/491520 e durações originais). A receita
  `metal_compound_particle_front` **não** foi portada: depende do `MetalCompoundParticles` da era
  espacial do GTO e o consumidor dela (`rarest_metal_mixture_dust`) exige cinco dusts de resíduos +
  `NanoScaleTungsten`, todos exclusivos do GTO; sem produtor, o `*Front` não cria beco sem saída.
  Também portadas as 5 receitas de bloco/carcaça do `classified/Assembler.java`
  (`hastelloy_n_75_casing`/`_gearbox`/`_pipe`, `flotation_cell`, `red_steel_casing`, 1:1) e a receita
  do controlador do `classified/AssemblyLine.java:1794` (Assembly Line, só GTCEu/GTNA, com a station
  research original de 32 CWU/t no ORE_WASHER[IV]) — coberta pelo `ControllerRecipePolicyTest`.
- Materiais GTNA novos (1:1 de `MaterialA`/`MaterialB`, com atribuição no ledger e nos javadocs):
  `HastelloyN75` (MaterialA:693) e `Stellite` (MaterialA:1060), necessários pelas carcaças e pela
  receita do controlador; `SodiumEthylxanthate`, `PotassiumEthylxanthate` e `Turpentine` (reagentes);
  os 12 fluidos `*Front` (MetalB:2974-3070, componentes = o minério GTCEu correspondente); `RedMud` e
  `NeutralisedRedMud` (MaterialB:4023/4028). Única substituição de material: o icon set `LIMPID` do
  GTO não existe no GTCEu, então RedMud/NeutralisedRedMud usam `FLUID`.
- Cadeia fechada: os 12 `*Front` produzidos têm as 12 receitas de secagem como consumidoras e o
  `RedMud` tem a neutralização no Mixer (ver G-0108); o `PortChainClosureTest` reporta
  `[PyropeFront, RedstoneFront, …, EnrichedNaquadahFront, RedMud]` sem becos sem saída.
- Lang en (`GTNALangProvider`: nome do bloco/máquina, `tooltip` + 3 linhas) + pt_br; atribuição em
  `GTNASources` (`industrial_flotation_cell`) e texturas no `THIRD_PARTY_NOTICES.md`.
- QA automático em `GTNAMachineGameTests`:
  - `industrialFlotationCellFormsWithParallelHatch` — A1 formação pelo `.mbs` real (9×7×7 e
    orientação gravada); A2 negativo (Gold Block no lugar de uma parede `FLOTATION_CELL` e Fluid
    Hatch na tampa superior só-carcaça, restaurando e voltando a formar); A3 hatches aceitas
    (energia LuV, Maintenance, item in, fluido in/out) e Parallel Hatch GCYM IV aceita; A5 execução
    real da receita de piropo com hatch LuV: um passo de overclock perfeito (7680 → 30720 EU/t) corta
    4800 → 1200 ticks e entrega 1000 mB de `PyropeFront`; A6 exatamente **12** receitas; A7 receita
    do controlador presente na Assembly Line.
  - `flotationFoamFeedsVacuumDrying` (ver G-0108) e `newGtoControllersDescribeTheirFunction` inclui
    as duas máquinas novas (tooltip + 1 linha de atribuição).
- Validação: `spotlessCheck compileJava runUnitTests runGameTestServer runData --offline` passou com
  **26/26 classes de teste unitário**, **76/76 GameTests** (`All 76 required tests passed`) e
  `runData` repetido com `written: 0`. QA in-game (CTM das texturas Hastelloy/Flotation Cell, tooltip
  en/pt_br, JEI da categoria `flotating_beneficiation`, rotação/colocação do controlador) pendente
  com o autor. Sem commit ou publicação.

### G-0106 (2026-09-25) — Supercritical Steam Turbine

- Port de `supercritical_steam_turbine` do GTOCore `dc4824d` (`GeneratorMultiblock.java:234-239`):
  IV, `special=false`, recipe type novo `gtna:supercritical_steam_turbine`
  (`SUPERCRITICAL_STEAM_TURBINE_FUELS` no GTO, `GTORecipeTypes.java:214`: GENERATOR, IO out,
  0/0/1/1, `CENTRIFUGE_OVERLAY`, `PROGRESS_BAR_GAS_COLLECTOR`, som `TURBINE`) e a estrutura do
  `registerLargeTurbine` (`MachineRegisterUtils.java:468-483`, sub-pattern SUPERCRITICAL). Padrão
  base 3×3×3 com a carcaça nova nas células C/H, gearbox de TungstenSteel, célula R (rotor holder
  ≥ IV **ou** Energy Output Hatch, ambos `setExactLimit(1)`); o módulo (5×4×7) usa
  `GCYMBlocks.CASING_HIGH_TEMPERATURE_SMELTING` (A), `GCYMBlocks.ELECTROLYTIC_CELL` (B), a carcaça
  nova em C/D (D aceita até 3 Energy Output Hatches) e moldura de TungstenSteel (F). Padrões em
  Java, como no GTO (sem `.mbs`).
- **Refatoração da base não-mega:** o comportamento que a máquina 3 tinha em
  `RocketLargeTurbineMachine` virou `GTNALargeTurbineMachine` (`WorkableElectricMultiblockMachine`
  + `ITurbineMachine`, caminho `mega=false` da `TurbineMachine` do GTO). `RocketLargeTurbineMachine`
  e `SupercriticalSteamTurbineMachine` são subclasses finas que fixam tier/`special`/id de lang,
  exatamente como o GTO cria as duas turbinas com o mesmo construtor. O comportamento da rocket
  turbine não mudou (base `V[EV]*2.5`, módulo 2×/20%/2×, modo de alta velocidade com os valores
  normal do GTO e dano fixo sem glass tier). As chaves de lang do modo de alta velocidade e da
  estimativa de saída agora são `<id>`-dependentes (`gtna.machine.<id>.…`).
- **Bloco novo `supercritical_turbine_casing`** em `GTNABlocks` (`createCasingBlock`, CTM ativa):
  texturas `supercritical_turbine_casing.png`, `_ctm.png` e `.png.mcmeta` copiadas do GTOCore com o
  namespace reescrito para `gtna` (CC BY-NC-SA 4.0, registrado em `GTNASources` via
  `supercritical_steam_turbine` e no `THIRD_PARTY_NOTICES.md`). Receita da carcaça portada 1:1 do
  `Assembler.java:2546` (GTCEu tungstensteel turbine casing + rod/gear/plate de MarM200Steel +
  circuito 6, `EUt(16)`/50 ticks); o `GTOMaterials.MarM200Steel` do GTO é o
  `GTNAMaterials.MarM200Steel` já portado.
- **Substituição travada GTO→GTNA no combustível:** o GTO só tem `SupercriticalSteam`
  (`MaterialB.java:3490`, 1000 K, produzido pelo Heat Exchanger); o GTNA não cria esse material e
  reusa `DenseSupercriticalSteam` — seu primeiro grau supercrítico, produzido a partir de
  `SuperHeatedSteam` no High Pressure Reactor e já usado pelo Void Miner/Cactus Wonder. O segundo
  grau (`InsanelySupercriticalSteam`) **não** é consumido para não inventar um combustível que o
  GTO não tem. A receita `FuelRecipe.java:225-230` mantém os números exatos: 80 mB → 8 mB de água
  destilada, 30 ticks, `EUt(-V[MV])`, id `supercritical_steam` (em runtime
  `gtna:supercritical_steam_turbine/supercritical_steam`).
- **Receita do controlador portada** (decisão do autor: portar quando usar só GTCEu/GTNA): o
  `Assembler.java:1527` usa `GTMachines.HULL[LuV]`, 4 circuitos LuV, 2 motores LuV,
  rod/gear/plate de `MarM200Steel` e pipes de `TungstenCarbide` — todos disponíveis no GTNA.
  Registrada em `GTNASupercriticalSteamTurbineRecipes` com `GTNAMachines3.SUPERCRITICAL_STEAM_TURBINE.asStack()`
  (coberta pelo `ControllerRecipePolicyTest` sem entrada na lista `OMITTED`).
- Lang en (`GTNALangProvider`: nome do bloco, nome da máquina, `tooltip` + 3 linhas,
  `estimated_output`, chaves do modo de alta velocidade e bônus do módulo) + pt_br (`pt_br.json`).
- QA automático em `GTNAMachineGameTests`:
  - `supercriticalSteamTurbineFormsWithRotorAndModule` — A1 formação pelo padrão real (base e
    depois o módulo, `gtna$formedModuleCount() == 1`); A2 negativo (Gold Block na carcaça e na
    casca do módulo); A3 hatches (rotor holder IV virado para fora, Energy Output Hatch
    obrigatória, muffler com face livre, manutenção e fluido) e rejeição de Fluid Hatch na célula
    do rotor e em célula só de carcaça, e de Parallel Hatch (o GTO não tem slot de paralelo na
    turbina); A6 exatamente **1** receita `supercritical_steam`; A7 receita do controlador
    presente no `ASSEMBLER_RECIPES`.
  - `supercriticalSteamTurbineBurnsSupercriticalSteamWithRotor` — A5 execução real: rotor de
    titânio (115%) em rotor holder IV com dynamo IV → 64 paralelos, 8.192 EU/t, 41 ticks
    (30 × 1,15 × 1,2), 5.120 mB por lote e 335.872 EU entregues em 41 ticks; o mesmo teste confirma
    que o holder IV mantém 100% de eficiência de holder (tier fixo do controlador).
- Bloqueios rígidos (herdados da máquina 3): gtolib sem fonte obriga a base
  `WorkableElectricMultiblockMachine` + `ParallelLogic.getParallelAmount` no lugar de
  `ElectricMultiblockMachine`/`accurateContentParallel`; `ItemPartMachine` de auto-insert de rotor
  não portado; painel de ajuste expert do modo de alta velocidade não portado; sem glass tier
  (dano fixo); turbinas mega continuam fora do escopo. Nenhum bloqueio novo nesta máquina.
- Validação: `spotlessCheck compileJava runUnitTests runGameTestServer runData --offline` passou com
  **26/26 classes de teste unitário**, **73/73 GameTests** (`All 73 required tests passed`) e
  `runData` repetido com `written: 0`. QA in-game (JEI, tooltip renderizado, orientação e modo de
  alta velocidade sob carga) pendente com o autor.

### G-0105 (2026-09-25) — Rocket Large Turbine

- Port de `rocket_large_turbine` do GTOCore `dc4824d` (`GeneratorMultiblock.java:227-232`): EV,
  `special=true`, recipe type novo `gtna:rocket_engine` (`ROCKET_ENGINE_FUELS` no GTO) e a mesma
  estrutura do `registerLargeTurbine` (`MachineRegisterUtils.java:393-486`, sub-pattern ROCKET nas
  linhas 452-467). Nenhum bloco novo: o padrão base 3×3×3 usa `CASING_TITANIUM_TURBINE`,
  `CASING_TITANIUM_GEARBOX` e a célula R (rotor holder **ou** Energy Output Hatch, ambos
  `setExactLimit(1)`); o módulo (5×4×7) usa `CASING_TITANIUM_STABLE`, `CASING_ENGINE_INTAKE`,
  `CASING_TITANIUM_TURBINE`/Energia nas células D e moldura de BlueSteel. O padrão base e o módulo
  são escritos em Java, como no GTO (não há `.mbs`; não foram copiados arquivos).
- Comportamento: `RocketLargeTurbineMachine` (`WorkableElectricMultiblockMachine`, também
  `ITurbineMachine`) porta o caminho **não-mega** da `TurbineMachine`. `getRealRecipe` reproduz a
  matemática do GTO: `EUt` do combustível, `getVoltage() = V[EV] * 2.5 * totalPower/100 * 2 (módulo)`,
  `turbineMaxVoltage = min(getOverclockVoltage(), getVoltage() * (speed/maxSpeed)^2)`, paralelo
  `turbineMaxVoltage / EUt`, saída `min(turbineMaxVoltage, paralelo * EUt)` (substituída nos
  `tickOutputs` via `EURecipeCapability.putEUContent`) e duração `receita * totalEfficiency * 1.2 /
  100` **sem** paralelizar a duração (o `recipe.copy(...)` do GTCEu escala a duração; o port usa
  `copy(modifier, false)`). O gate de rotor presente do `matchRecipeInput` está explícito no
  `getRealRecipe`, além do gate nativo do `RotorHolderPartMachine` do GTCEu. Rotação: a rotação e o
  dano base (1 + problemas de manutenção por segundo) são os do rotor holder do GTCEu; a máquina
  reporta `getTier() = EV` fixo (como o GTO), então um rotor holder EV mantém 100% de eficiência de
  holder mesmo com dynamo IV.
- Modo de alta velocidade: portado com os valores **normais** do GTO (3× saída, 10 de dano ao rotor
  por segundo acumulado e 8× de falha de manutenção), botão na UI fancy com a textura
  `high_speed_mode.png` copiada do GTOCore (CC BY-NC-SA 4.0). O painel de ajuste do modo expert
  (gtolib/`GTOGuiTextures.PARALLEL_CONFIG`, string `ars_nouveau.locked`) **não** foi portado; o GTNA
  não tem modo expert, então os valores ficam fixos. O bônus do módulo é 2× saída / +20% eficiência /
  2× multiplicador de dano (usado apenas no modo de alta velocidade pelo código do GTO).
- Substituições GTO→GTNA (bloqueios rígidos documentados):
  - gtolib `ElectricMultiblockMachine`/`RecipeHandlerUnit`/`accurateContentParallel` não têm fonte
    (gtolib é biblioteca compilada). O port estende `WorkableElectricMultiblockMachine` e usa
    `ParallelLogic.getParallelAmount` como aproximação do paralelo por conteúdo; a matemática de
    tensão/paralelo/duração é a do GTO.
  - `GTOPredicates.RotorBlockFacingOutwards` foi portado para `GTNAMachines3.rotorBlockFacingOutwards`
    com `IRotorHolderMachine` + `MetaMachineBlock.getDefinition().getTier()` + o scan de folga
    (3×3 à frente e 5×5 exclusivo). O `TraceabilityPredicate.direction` do fork do GTO não existe no
    GTCEu 7.5.3, então a checagem de direção fica dentro do predicado.
  - O `ItemPartMachine` que reinsere rotores automaticamente (peça exclusiva do GTO) não foi portado;
    os rotores continuam sendo colocados na UI do rotor holder.
  - `addFuelProperties` (jetpack `PowerlessJetpack` do GTO) e a 7ª receita (criogênio do Ad Astra) não
    foram portados. Das 6 receitas do GTO, só `rocket_engine_fuel_1` usa material GTCEu
    (`RocketFuel`, 10 mB / 20 ticks / 512 EU/t) e é a única registrada; as outras cinco usam fluidos
    exclusivos do GTO (`RocketFuelRp1`, `DenseHydrazineFuelMixture`, `RocketFuelCn3h7o3`,
    `RocketFuelH8n4c2o4`, `ExplosiveHydrazine`) — documentado no javadoc de `GTNARocketFuelRecipes`,
    no tooltip e no ledger.
  - Receita do controlador **omitida** (decisão travada: dependência exclusiva do GTO): a receita
    original em `classified/Vanilla.java:579` tem `GTOMachines.ROCKET_ENGINE_GENERATOR[EV]` no centro,
    máquina single-block que o GTNA não porta. Registrada em `ControllerRecipePolicyTest.OMITTED` com
    o motivo e documentada no javadoc do registro.
- Lang en (`GTNALangProvider`: nome, `tooltip` + 3 linhas, `estimated_output`, chaves do modo de alta
  velocidade, bônus do módulo e `gtna.multiblock.pattern.rotor_clearance`) + pt_br (`pt_br.json`).
  Atribuição em `GTNASources` (`rocket_large_turbine`) e no `THIRD_PARTY_NOTICES.md` (mecânica e a
  textura do botão).
- QA automático em `GTNAMachineGameTests`:
  - `rocketLargeTurbineFormsWithRotorAndModule` — A1 formação pelo padrão real (base e depois o
    módulo, `gtna$formedModuleCount() == 1`); A2 negativo (Gold Block em casing e na casca do módulo,
    Fluid Hatch na célula do rotor e em célula só de casing); A3 hatches (rotor holder, Energy Output
    Hatch obrigatória, muffler com face livre, manutenção e fluido), rotor holder presente e Parallel
    Hatch rejeitada (o GTO não tem slot de paralelo na turbina); A6 exatamente **1** receita
    `rocket_engine_fuel_1`; A7 ausência de receita de controlador.
  - `rocketLargeTurbineBurnsRocketFuelWithRotor` — A5 execução real: rotor de titânio (115%) em rotor
    holder EV com dynamo IV → 16 paralelos, 8.192 EU/t, 27 ticks (20 × 1,15 × 1,2), 160 mB por lote e
    221.184 EU entregues em 27 ticks; o mesmo teste confirma que o holder EV mantém 100% de eficiência
    de holder (tier fixo do controlador).
- Validação: `spotlessCheck compileJava runUnitTests runGameTestServer runData --offline` passou com
  **26/26 classes de teste unitário**, **71/71 GameTests** (`All 71 required tests passed`) e
  `runData` repetido com `written: 0` (primeira execução: `written: 6` — blockstate, model de bloco,
  model de item e as chaves de lang geradas).
- Pendências manuais: QA in-game do modelo/textura do controlador e do módulo, tooltip en/pt_br,
  JEI (categoria `rocket_engine` e a receita de RocketFuel), rotação/colocação do controlador,
  comportamento sob carga do rotor (dano/velocidade), o botão do modo de alta velocidade e a
  ausência do auto-insert de rotores do GTO. Sem commit ou publicação.

### G-0104 (2026-09-25) — ISA Mill

- Port de `isa_mill` do GTOCore `dc4824d`: estrutura comprimida 7×3×3 (7 camadas) copiada para
  `pattern/gto/isa_mill.mbs` e lida com `GTOCompressedPatternReader`; orientação gravada
  BACK/UP/RIGHT (char→+Z, aisle→+X, row→+Y com o controlador virado a NORTH). Carcaças novas
  Inconel-625 (casing/gearbox/pipe) com texturas do GTOCore (png + `_ctm` + `.mcmeta`),
  `appearanceBlock` na carcaça, `RotationState.ALL` e overclock perfeito.
- Comportamento: `IsaMillMachine` (`WorkableElectricMultiblockMachine`) roda `ISA_MILL_RECIPES`
  (2 item in / 1 out / 1 fluido in, EU in; a data key `grindball` aparece no JEI). O gate da esfera
  fica no `getRealRecipe` (sem esfera ou tier errado = receita indisponível) e a matemática de dano
  do GTO (`durability + parallels/(Unbreaking+1) + 1`, destruição ao atingir o dano máximo) é
  aplicada em `beforeWorking`. Motivo da adaptação: o `fullModifyRecipe` do GTCEu 7.5.3 é chamado
  uma vez por candidata da busca; danificar em `getRealRecipe` gastaria a esfera em receitas que
  nunca iniciam. Está no javadoc da classe e aqui.
- `BallHatchPartMachine` (IV, ability nova `GTNAPartAbility.GRIND_BALL_HATCH`, slot único filtrado,
  esferas soapstone=1/50 de durabilidade e aluminium=2/100), **sem renderer custom**: modelo hull
  com o overlay `ball_hatch` do GTOCore e o comportamento GTO (40 de dano ao tocar em
  funcionamento; a esfera não é devolvida se a hatch for removida em funcionamento). A UI é a slot
  LDLib padrão, não a tela do `ItemPartMachine` do gtolib.
- Prefixo `MILLED`: `GTNATagPrefix.MILLED` (`milled_%s`, tag `milleds/%s`, icon `milled`,
  `GTNAMaterialFlags.GENERATE_MILLED`), textura copiada e override
  `assets/gtceu/models/item/material_sets/dull/milled.json` → `gtna:item/material_sets/dull/milled`
  (fidelidade ao GTO, que também só envia o template `dull`). Flag adicionada aos 12 materiais
  GTCEu das receitas (Grossular, Almandine, Chalcopyrite, NaquadahEnriched, Platinum, Redstone,
  Monazite, Pentlandite, Nickel, Spessartine, Pyrope, Sphalerite) via `MaterialAdd`.
- Data key `GTNARecipeDataKeys.GRINDBALL = "grindball"`.
- Receitas (`GTNAIsaMillRecipes`, registrado no `GTNAGTAddon`): as 48 do `classified/IsaMill.java`
  com EUt 1920 / duração / água / quantidade / circuito idênticos
  (`TagUtils.createTGItemTag("ores/x")` → `TagPrefix.ore`; `TagPrefix.rawOre` mantido). Também
  foram portadas as receitas do casing/gearbox/pipe (Assembler), das duas esferas (Forming Press),
  da Ball Hatch (Assembler) e do controlador (Assembly Line com station research 32 CWU/t).
- Substituições/expansões documentadas: a Control Hatch do GTO não é portada; o recovery stack
  dinâmico do JEI também não (a API do GTCEu é um `Supplier` estático). Para manter a receita do
  controlador **presente e fiel**, os três materiais exclusivos do GTO que ela usa (Inconel-625,
  Inconel-792, Tantalloy-61) foram portados 1:1 de `MaterialA` (componentes, cor, icon set, blast e
  flags); as rotas de produção são as automáticas do GTCEu (alloy blast/EBF/mixer), o que o
  GameTest confirma para o Inconel-625.
- Lang en (`GTNALangProvider`, inclui `tagprefix.milled` e `gtna.recipe.grindball`) + pt_br
  (`pt_br.json`), nomes dos blocos/itens, atribuição em `GTNASources` (`isa_mill`,
  `grind_ball_hatch`) e texturas no `THIRD_PARTY_NOTICES.md`.
- QA automático em `GTNAMachineGameTests`:
  - `isaMillFormsWithBallHatch` — A1 formação pelo `.mbs` (7×3×3 e orientação gravada); A2 negativo
    (Gold Block no gearbox e Fluid Hatch no tubo); A3 hatches de item/fluido/energia/manutenção e a
    Ball Hatch aceita; A5-gate (esfera ausente/tier errado não iniciam, tier certo inicia, dano 2 e
    destruição no limite); A6 **48** receitas + alloy blast do Inconel-625; A7 receita do
    controlador na Assembly Line.
  - `isaMillGrindsOreWithGrindingBall` — execução real: 1 minério de Grossular + 100 mB de água
    destilada + esfera de soapstone → 96 MILLED Grossular, overclock perfeito (4800 → 1200 ticks) e
    2 de dano na esfera.
  - Detalhe descoberto no teste: o Muffler Hatch é obrigatório (`setExactLimit(1)` no GTO) e a face
    frontal dele precisa ficar livre, senão `IMufflerMachine.modifyRecipe` devolve null.
- Validação: `spotlessCheck compileJava runUnitTests runGameTestServer runData --offline` passou com
  **26/26 classes de teste unitário**, **69/69 GameTests** (`All 69 required tests passed`) e
  `runData` repetido com `written: 0`.
- Pendências manuais: QA in-game dos modelos/CTM Inconel, tooltip en/pt_br, JEI (categoria e a
  linha `Grinding Ball Material`), rotação/colocação do controlador e a ausência da animação
  idle/spinning da esfera. Sem commit ou publicação.

### G-0103 (2026-09-25) — Mega Alloy Blast Smelter

- Port de `mega_alloy_blast_smelter` (id exclusivo do GTOCore; o `alloy_blast_smelter` normal já
  existe no GTCEu e não foi duplicado). Estrutura GTO 11×18×11 copiada em linha, carcaça GCYM de
  alta temperatura, bobinas, `HEAT_VENT`, vidro temperado, intake extremo, fireboxes e tubulação.
- Comportamento: `MegaAlloyBlastSmelterMachine` (CoilWorkableElectricMultiblockMachine) roda
  `GCYMRecipeTypes.ALLOY_BLAST_RECIPES` com Parallel Hatch, gate de temperatura de bobina e bônus de
  0,8× EU / 0,6× duração. O `UPGRADE_GCYM_OVERCLOCKING` do gtolib é nativo/fechado; o port usa o
  overclock padrão de Alloy Blast do GTCEu e documenta a diferença.
- Substituições documentadas: a célula de "integral framework" (tier-block do GTO) virou moldura de
  Tungstênio-Aço; `autoGCYMAbilities` virou `autoAbilities`.
- Sem novos blocos, materiais ou recipe types. Atribuição em `GTNASources` e
  `THIRD_PARTY_NOTICES.md`.
- Validação: gate completo passou com **66/66 GameTests** e datagen determinístico (6 arquivos
  novos). Teste in-game pendente. Sem commit ou publicação.
- Roadmap dos candidatos restantes (com os bloqueios de recursos GTO) registrado no handoff.

### G-0102 (2026-09-25) — Fábrica Química (Chemical Plant)

- Port de `chemical_plant` a partir do GTOCore `dc4824d`: estrutura comprimida 5×5×5 copiada para
  `pattern/gto/chemical_plant.mbs` (carcaça de PTFE inerte com mínimo de 60 blocos, bobinas de
  aquecimento e tubulação de PTFE). Roda `LARGE_CHEMICAL_RECIPES`.
- Comportamento: `ChemicalPlantMachine` (CoilWorkableElectricMultiblockMachine) com Parallel
  Hatch, overclock perfeito e bônus de eficiência de bobina de `1 - tier × 0.05` em EU e duração,
  exatamente o valor que o GTO exibe no controlador. O `coilReductionOverclock(0.25)` do gtolib é
  nativo/fechado; o argumento de duração não pôde ser verificado e está documentado.
- GTO usa uma Hatch de Catalisador e o Machine Access Link (peças exclusivas do GTO) que não foram
  portados; o predicado da carcaça mantém o resto idêntico.
- Receita do controlador: **omitida por decisão do autor** (a original é Assembly Line com
  WatertightSteel e outros recursos exclusivos do GTO). Um port completo exigiria portar a cadeia
  de materiais do GTO.
- Texturas: overlay reaproveitado do Reator Químico Grande do GTCEu (mesma carcaça de PTFE inerte);
  sem asset novo. Atribuição no `GTNASources` e `THIRD_PARTY_NOTICES.md`.
- Validação: `spotlessCheck compileJava runUnitTests runGameTestServer runData --offline` passou com
  **65/65 GameTests** e datagen determinístico (15 arquivos novos gerados). Teste in-game pendente.
  Sem commit ou publicação.

### G-0101 (2026-09-25) — Component Assembler base e Large Greenhouse

- Decisão do autor: continuar todos os candidatos viáveis até LuV; criar equivalentes GTNA para
  dependências externas e rotas alternativas com os mesmos insumos quando um controlador excluído
  for necessário. Sem commit/publicação antes do teste in-game e aprovação do autor.
- Component Assembler: estrutura GTO 7×5×5, casings LV–IV de tier uniforme, casing multifuncional,
  controller e 40 receitas de componentes (oito para cada tier LV–IV). A máquina só aceita uma
  receita até o tier de casing formado. Texturas dos casings vieram do GTOCore `dc4824d`, com
  atribuição em `GTNASources` e `THIRD_PARTY_NOTICES.md`. A estrutura extra grande, receitas ULV e
  processamento LuV+ ainda não foram portados; o port atual é a base funcional.
- Large Greenhouse: estrutura GTO comprimida 9×10×9, modos Greenhouse e Tree Growth Simulator,
  receita do controlador LuV e família GTO de árvores vanilla + entradas condicionais quando os
  respectivos mods estão presentes. `minecraft:mud` substitui Rich Soil se Farmer's Delight não
  estiver instalado. Como no GTO, a máquina não exige luz solar.
- Validação: gate inicial `spotlessCheck compileJava runUnitTests runGameTestServer runData --offline`
  passou; após o Component Assembler, o gate completo passou com 61/61 GameTests e `runData`
  determinístico (`written: 0`). Após a Large Greenhouse, `spotlessApply compileJava
  runGameTestServer --offline` passou com 62/62 GameTests. Falta repetir o gate completo após a
  receita final da carcaça multifuncional e as próximas mudanças. Teste manual in-game pendente.
- Próximo: continuar os candidatos HV–LuV do handoff; revisar as dependências específicas de cada
  um antes de marcar o port completo. Nenhum commit ou publicação deste lote foi feito.

### G-0100 (2026-09-25) — GitHub Release v0.5.1 publicado

- Após o push de G-0099 (`9808c5037e255522fae21139f35b7b80a6286d9d`), o autor esclareceu
  que esperava também o GitHub Release. Publicado
  `https://github.com/Raishxn/GregTech-Nexus-Addon/releases/tag/v0.5.1` com as notas de
  `docs/curseforge-changelog-0.5.1.md` e o arquivo `gtna-0.5.1.jar`.
- A tag aponta para o commit corrigido; o release não é draft nem prerelease. O JAR do anexo foi
  baixado novamente e seu SHA-256 coincide com o build local e o JAR da instância Prism:
  `afc79d801e00cb7e1974a4f6b843ed7f89a19a86e1641c3bdab97fb4458e4585`.
- Java CI e MkDocs do commit de release passaram no GitHub. Permanecem os checks manuais do
  Hypercore, tooltip de módulos e planner AE2 anotados em G-0099. O upload no CurseForge
  continua com o autor; em 2026-09-25 a página pública ainda mostrava 0.5.0 como principal.

### G-0099 (2026-09-25) — JAR 0.5.1 corrigido autorizado para a main

- O autor autorizou commit e push na `main` e a substituição do JAR 0.5.1 de teste. O JAR
  corrigido em `build/libs/gtna-0.5.1.jar` e na instância Prism `1.20.1` tem SHA-256
  `afc79d801e00cb7e1974a4f6b843ed7f89a19a86e1641c3bdab97fb4458e4585` e declara
  `version = "0.5.1"` em `mods.toml`. O JAR 0.5.0 anterior da instância está preservado em
  `gtna-test-backup`.
- Os mixins de tooltip GTCEu e confirmação AE2 foram retestados no cliente de produção do Prism
  sem novo crash de carregamento. `spotlessCheck compileJava runUnitTests runGameTestServer
  runData --offline` passou após todas as mudanças de código: 60/60 GameTests, 0 falhas,
  datagen `written: 0`. O build do JAR também passou.
- O changelog e o texto para CurseForge foram atualizados para o JAR corrigido. A página pública
  do CurseForge mostrava 0.5.0 como arquivo principal e nenhum 0.5.1 em 2026-09-25; portanto
  não havia um arquivo 0.5.1 naquela plataforma para substituir. O upload no CurseForge fica
  com o autor conforme G-0096.
- **Pendente:** repetir a construção do Hypercore no mundo e medir o tempo; confirmar tooltip de
  módulos e fluxo do planner AE2. O log da última sessão não registrou uma nova construção.

### G-0098 (2026-09-25) — pausa longa ao construir o Hypercore pelo Nexus Terminal

- Na instância Prism `1.20.1`, o autor confirmou que a pausa começou no Shift+clique do Terminal.
  O watchdog ModernFix registrou tick de servidor acima de 40 s; a thread estava em
  `MultiblockState.onBlockStateChanged -> checkPatternWithLock -> BlockPattern.checkPatternAt`
  após colocação de bloco. A estrutura tem 44×22×44 posições. Captura posterior da thread mostrou
  o servidor ocioso, indicando pausa longa, sem deadlock persistente.
- `NexusBuildCheckGuard` e `MultiblockStateBuildMixin` adiam as verificações disparadas por blocos
  do multiblocos alvo enquanto o Terminal constrói; mudanças no controller e verificações de
  outros multiblocos continuam normais. O Terminal chama `GTNAStructureRefresh.refresh` ao final.
  O guard é local à thread, restaurado em `finally`. GameTest confere escopo e restauração.
- Adicionada medição no log do tempo total da construção para o reteste. Gate completo antes dessa
  medição passou: `spotlessCheck compileJava runUnitTests runGameTestServer runData build --offline`,
  60/60 GameTests, datagen `written: 0`. Após a medição, `spotlessCheck compileJava runUnitTests
  build --offline` passou. JAR instalado na instância Prism e cliente reaberto sem erro de mixin.
- O gate completo foi repetido após a medição: `spotlessCheck compileJava runUnitTests
  runGameTestServer runData --offline` passou, com 60/60 GameTests e datagen `written: 0`.
- **Pendente:** medir a construção no mundo do autor e confirmar formação e comportamento do
  Hypercore. Mudanças locais, sem commit/push.

### G-0097 (2026-09-25) — crash do tooltip de módulos no JAR 0.5.1

- O autor relatou `InvalidInjectionException` em `gtceu.MetaMachineBlockMixin` ao iniciar com o
  JAR 0.5.1. `javap` no `gtceu-1.20.1-7.5.3-slim.jar` distribuído confirmou que o método de
  tooltip de `MetaMachineBlock` é `m_5871_(ItemStack, BlockGetter, List, TooltipFlag)`; o JAR
  GTCEu 7.5.3 da instância Prism `1.20.1` confirmou a mesma assinatura. A fonte local e o
  ambiente de desenvolvimento usam `appendHoverText` para esse método. Sem
  refmap carregado, a injeção da 0.5.1 buscava apenas o nome de desenvolvimento e era obrigatória.
- O mixin agora declara os dois nomes, `remap = false` e `require = 0`. No primeiro lançamento
  da instância Prism `1.20.1` (GTCEu 7.5.3, KubeJS build.26, AE2 15.4.10, Forge 47.4.20),
  o crash de tooltip desapareceu, mas `CoreCraftConfirmMenuMixin` falhou procurando
  `broadcastChanges`. `javap` no AE2 distribuído confirmou `m_38946_` com a chamada `Future.get`
  usada como ponto de injeção. Esse mixin também aceita os dois nomes e usa `require = 0`.
  `ProductionMixinTargetContractTest` verifica as duas anotações compiladas.
- `spotlessCheck compileJava runUnitTests runGameTestServer runData build --offline` passou após
  a primeira correção. Após a segunda correção, `spotlessCheck compileJava runUnitTests build
  --offline` e o gate completo `spotlessCheck compileJava runUnitTests runGameTestServer runData
  --offline` também passaram: 59/59 GameTests e `runData` com `written: 0`.
  O segundo lançamento no Prism passou pelo carregamento dos mods e chegou à interface do cliente
  sem `Mixin apply failed` ou crash report novo. O log registrou falha não fatal do Narrator por
  falta de `libflite.so` no sistema.
- **Pendente:** conferir no jogo o tooltip de módulo e o fluxo do planner AE2, e obter o feedback
  do autor antes de publicar. O JAR
  0.5.0 da instância foi preservado em `gtna-test-backup`; o JAR de teste 0.5.1 está em `mods`.
  Alterações locais, sem commit/push; a versão publicada segue 0.5.1.

### G-0096 (2026-09-25) — preparação da versão 0.5.1

- O autor confirmou o teste in-game e autorizou commit e push na `main` como 0.5.1. A versão
  foi atualizada em `gradle.properties`, README e título da descrição CurseForge; o changelog
  público da versão está em `CHANGELOG.md` e o texto pronto para colar no CurseForge em
  `docs/curseforge-changelog-0.5.1.md`. A publicação do arquivo no CurseForge fica com o autor.
- `./gradlew spotlessCheck compileJava runUnitTests runGameTestServer runData build --offline`
  passou. `runData` foi determinístico (`written: 0`); nova execução do servidor seguida de
  `python3 tools/check_gametest_report.py` confirmou 59/59 GameTests e o JUnit completo.
  `build/libs/gtna-0.5.1.jar` contém `version = "0.5.1"` no `mods.toml`; SHA-256:
  `e852fead99da886144f7ea19fa73c8d49a6d5ac2b8384c13db5f6c7fe9f2b35c`.
- A branch local `main` foi conferida contra `origin/main` após `git fetch` (sem divergência).
  Permanecem os checks manuais específicos anotados em G-0092 a G-0095; o teste geral da leva
  foi confirmado pelo autor.

### G-0095 (2026-09-25) — diagnóstico da MAX Output Hatch na Evaporation Plant

- Após o autor relatar que a saída não era aceita, o `run/logs/latest.log` do cliente mostrou
  avisos de padrão e mensagens de diagnóstico às 11:54–11:55. O texto `ULV Output Hatch` é o
  bloco de exemplo exibido para a ability `EXPORT_FLUIDS`, não uma restrição de tier.
- Leitura **somente leitura** do mundo salvo `run/saves/New World` mostrou duas torres. Na primeira,
  controlador `(-13,-59,-117)`, a `gtceu:max_output_hatch` estava em `(-14,-59,-117)`, célula `Y`
  da base destinada a entrada de fluido/energia; a `gtceu:mv_input_hatch` já estava em
  `(-12,-59,-117)`. Mover a MAX Output Hatch para um casing `X` acima da base, por exemplo
  `(-14,-58,-117)`, é a correção da posição. Na segunda, controlador `(-8,-59,-109)`, a MAX Output
  Hatch em `(-7,-57,-109)` já ocupava um estágio `X` válido. Essa torre não tinha nenhuma Fluid
  Input Hatch na base; substituir um casing `Y`, por exemplo `(-9,-59,-109)`, por uma entrada de
  fluido resolve a exigência de `IMPORT_FLUIDS`. As hatches de energia encontradas ocupam células
  `Y` válidas. Não alterar o mundo salvo automaticamente.
- O GameTest da Evaporation agora confirma explicitamente que uma MAX Fluid Output Hatch é
  aceita em `X` e rejeitada em `Y`. `./gradlew spotlessCheck compileJava runUnitTests
  runGameTestServer runData --offline` passou: 59/59 GameTests, datagen `written: 0`.
  Nenhuma alteração de regra de produção foi necessária; sem commit ou publicação.

### G-0094 (2026-09-25) — Steam Hatches restritas e luz da Greenhouse

- Por pedido do autor, as Wireless Steam Input Hatches bronze/aço agora registram só `STEAM`;
  as Wireless Steam Output Hatches bronze/aço usam a ability exclusiva
  `GTNAPartAbility.STEAM_EXPORT_FLUIDS`. Saídas não podem declarar `STEAM`, pois seriam aceitas
  como fonte e invalidariam a estrutura. Nenhuma das quatro anuncia `IMPORT_FLUIDS` ou
  `EXPORT_FLUIDS`; o armazenamento interno continua filtrado para vapor. O contrato unitário e
  o GameTest conferem os quatro registros. O predicado defensivo `nonSteamFluidInputHatches`
  da Evaporation permanece, inclusive para outras peças que possam anunciar ambas as abilities.
- A Evaporation Plant mantém o desenho do GTOCore: exatamente uma entrada de fluido e uma ou duas
  entradas de energia na base `Y`; até uma saída de fluido por andar `X`. Não há exigência de tier
  ULV para a entrada de fluido; o teste usa uma hatch HV. O tooltip `en_us`/`pt_br` explicita as
  quantidades, a base e o tier livre. O GameTest agora prova que a entrada
  funciona em outro casing da base e a saída em outro casing do andar. Para diagnosticar o caso
  in-game em que não formou, ainda é necessária a posição/camada exata e saber se a peça chamada
  “ULV input hatch” era de fluido ou energia.
- A Greenhouse antes consultava o brilho acima do vidro e podia continuar mostrando sol após
  tampar o teto. A estrutura usa vidro temperado GTCEu, cujo valor de bloqueio de luz não serve
  para amostrar o interior. O centro de leitura foi alinhado com o teto de vidro; a máquina
  inspeciona a coluna acima dele, desde o primeiro bloco onde uma cobertura pode ser colocada,
  e aplica o escurecimento diurno. O GameTest cobre teto livre, cobertura de pedra, remoção da
  cobertura e crescimento de cacto após a remoção. O cenário limpa terreno gerado acima do teto
  antes de começar.
- Validação: `./gradlew spotlessCheck compileJava runUnitTests runGameTestServer runData --offline`
  passou; 59/59 GameTests e datagen `written: 1` após a tradução do tooltip (execução anterior:
  `written: 0`). Testes de integração anteriores passaram após ajustar o contrato de abilities.
  `runClient --offline` abriu a janela Forge com o código novo e ficou aberto para QA do autor.
  Sem commit nem publicação; formação e leitura no mundo real ainda precisam do autor.

### G-0093 (2026-09-25) — Nexus Flux Matrix sem modo Safe; Evaporation Plant sem hatch de vapor

- Pedido do autor após QA no cliente: remover o modo Safe da Nexus Flux Matrix. Retirada de EU
  agora só falha quando o saldo é menor que a solicitação; nenhuma porcentagem reserva energia.
  Estado/serialização `SafeMode`, alertas de limiar, opções YAML/JSON e indicadores da UI foram
  removidos. Saves antigos com `SafeMode: true` são lidos sem usar esse campo; o próximo save
  não o regrava. GameTest cobre retirada a 5%, descarga a zero e migração desse NBT legado.
- A Evaporation Plant aceitava a **Wireless Steam Input Hatch** porque o GTNA a registra com as
  habilidades `STEAM` e `IMPORT_FLUIDS`. O predicado de entrada de fluido aceitava todo bloco da
  segunda habilidade; a hatch só movimenta vapor e não abastece as receitas de evaporação.
  O predicado do controlador e do módulo auxiliar agora exclui qualquer entrada que também
  anuncie `STEAM`. GameTest verifica rejeição da hatch de vapor padrão e da sem fio e aceitação
  da Fluid Input Hatch HV. O tooltip informa entrada de fluido sem vapor.
- Validação: `spotlessCheck`, `compileJava`, `runUnitTests` e `runGameTestServer` passaram; o
  GameTest novo verifica saldo baixo e save legado, e o GameTest da Evaporation verifica as
  três hatches. O `runData` acoplado ao gate falhou uma vez no registro intermitente do renderer
  da Artificial Star e ficou com a thread KubeJS ativa; duas execuções isoladas seguintes de
  `runData --offline` passaram, a segunda com `written: 0`. Registrar esse erro de datagen para
  investigação se reaparecer. `runClient --offline` foi reiniciado com o código novo e abriu a
  janela Forge sem erro fatal; ficou aberto para QA do autor. Nenhum commit ou publicação foi feito.

### G-0092 (2026-09-25) — crash KubeJS build.26 e fidelidade dos tooltips GTO

- O `latest.log` anexado registra KubeJS `2001.6.5-build.26` e a falha fatal do mixin GTNA ao
  buscar `DataGenerator.run`. No jar SRG de Minecraft 1.20.1, o alvo real é
  `DataGenerator.m_123917_()`. A configuração tem `defaultRequire: 1` e o log mostra que o
  refmap não foi carregado. O mixin foi tornado opcional (`require = 0`), com nomes dev/SRG
  explícitos e falha de limpeza apenas registrada; a dependência de desenvolvimento agora é
  KubeJS build.26. Teste de contrato inspeciona a anotação compilada. O log também contém
  falhas de mixin de outros mods, sem evidência de que elas causaram este erro fatal do GTNA.
- Os quatro tooltips GTO usam a chave automática `.tooltip` como função principal, sem
  repetição no builder. Fishing Ground preserva quatro linhas narrativas e quatro circuitos
  da fonte; Generator Array informa os valores reais do GTNA e os três geradores suportados;
  Greenhouse separa requisitos e penalidade de luz; Evaporation Plant informa o módulo
  auxiliar já registrado. `en_us` e `pt_br` foram sincronizados. O GameTest verifica a
  quantidade de linhas, a ausência de repetição da chave automática e uma atribuição de
  origem por controller. Desvios da fonte e motivos estão no handoff dos ports.
- Validação local: `spotlessApply spotlessCheck compileJava runUnitTests runGameTestServer
  runData --offline` passou com KubeJS build.26; 21 classes de testes unitários e os GameTests
  configurados terminaram sem falhas, e `runData` finalizou os provedores e saiu normalmente.
  `runClient --offline` abriu a janela Minecraft Forge sem a falha do mixin; o processo foi
  encerrado após o teste de inicialização. Pendente: reteste do autor com jar distribuído no
  modpack completo do jogador e QA visual dos tooltips
  em inglês/português. Nenhum commit ou publicação foi feito.

### G-0091 (2026-09-25) — QA de UI e idiomas da primeira leva GTO

- Após o teste in-game do autor, adicionados tooltips descritivos a Fishing Ground e Evaporation
  Plant; adicionadas as chaves principais de tooltip dos quatro controladores e as traduções
  `gtna.fishing_ground`, `gtna.evaporation` e `gtna.greenhouse` em inglês e português.
- Generator Array agora tem um slot visível no controlador para até quatro geradores elegíveis.
  O slot tem prioridade sobre o barramento de entrada; o barramento permanece funcional em mundos
  existentes. A UI mantém o botão de saída sem fio. O GameTest verifica limite do slot, filtro
  de itens e o caminho antigo pelo barramento; um novo GameTest verifica a construção dos quatro
  tooltips funcionais.
- Gate pós-correção: `spotlessApply spotlessCheck compileJava runUnitTests runGameTestServer
  runData --offline` passou com 20/20 testes unitários e 58/58 GameTests. `runClient` foi iniciado
  para o autor conferir o resultado na interface.

### G-0090 (2026-09-25) — Greenhouse GTO

- Portada a estrutura comprimida 5×5×5 do GTOCore, o controle de luz do céu (zero impede a partida;
  abaixo de 13 regride dez ticks de progresso por segundo), a receita original do controlador e
  as 84 receitas fixas de cultivo, com suas quantidades, circuitos, água, EU/t e durações. As
  receitas de outros mods entram somente quando os itens existem. Farmer's Delight `rich_soil` é
  aceito quando instalado; lama vanilla é alternativa estável para o GTNA sem aquela dependência.
- Validação: `./gradlew spotlessApply spotlessCheck compileJava runUnitTests runGameTestServer
  runData --offline` passou com **20/20 unitários** e **57/57 GameTests**; `spotlessCheck` e
  `compileJava` foram repetidos após a limpeza final do teste, e `git diff --check` está limpo. O
  novo GameTest forma a estrutura com hatches MV em céu aberto e produz 12 cactus com o circuito 1.
  Falta QA in-game.

### G-0089 (2026-09-25) — primeira leva GTO LV–HV

- Portados Generator Array (incluindo saída pelo Nexus Flux Matrix no modo wireless e hatch normal),
  Fishing Ground (padrão GTO comprimido, iscas e loot de pesca) e Evaporation Plant (estrutura
  principal, módulo auxiliar, casings e cadeia de salmoura). Materiais que faltavam foram criados
  para manter as receitas e a progressão. O Lava Furnace foi removido do escopo e não há registro
  ou receita dele nos recursos atuais.
- Validação: `./gradlew spotlessApply spotlessCheck compileJava runUnitTests runGameTestServer runData
  --offline` passou; 20/20 unitários, 56/56 GameTests, datagen executado. `git diff --check` limpo.
  Não houve teste in-game destes três controladores.
- Pendências: conferir visual, rotação, JEI, tooltips e operação contínua no cliente; continuar os
  ports Component Assembler e os candidatos HV–LuV indicados pelo autor, preservando
  receitas/estruturas/mecânicas tão fielmente quanto o conjunto de dependências permitir.

### G-0088 (2026-09-25) — preparação da versão 0.5.0

- O autor confirmou teste in-game das mudanças locais e aprovou a publicação na `main`.
- Versão alterada para `0.5.0`; README, changelog e texto de listagem do CurseForge atualizados.
  A descrição usa a logo animada versionada em `docs/assets/logo.gif` via URL pública do GitHub.
- Validação: `spotlessCheck`, `compileJava`, `runUnitTests`, `runGameTestServer` (53/53) e
  `runData` determinístico (`written: 0`) passaram. `python3 tools/check_gametest_report.py`
  confirmou os 53 resultados. `./gradlew build` gerou `build/libs/gtna-0.5.0.jar` com
  `version = "0.5.0"` no `mods.toml`; SHA-256:
  `75b39777ec89292ab38c2a8729bf553edd2dc7261f87577157beda8514d480c7`.
- Permanecem as verificações manuais específicas listadas em **Estado atual** e em
  `docs/roadmap/QA-MANUAL-CHECKLIST.md`; o teste in-game geral foi confirmado pelo autor.

### G-0087 (2026-09-24) — auditoria e padronização inicial de tooltips

- **Auditoria:** inventariadas 84 chamadas `.tooltips(...)` (63 em `GTNAMachines`, 18 em
  `GTNAMachines2`, 3 em `GTNAEnergyHatches`), 74 entradas em `GTNASources.SOURCES` e 22 máquinas com
  a linha genérica `Added by GregTech Nexus Addon`. Foram levantados os problemas objetivos
  (descrições duplicadas, literais hardcoded, chave errada, `main_function` faltando, atribuição
  inconsistente, `pt_br` desatualizado) e os subjetivos (sem `Main Function` padronizada, cores
  inconsistentes, paredes de texto). Referência do GTOCore documentada (engine no `gtolib` selado;
  convenção replicável).
- **Decisões do autor:** atribuição só com `Source:` no conteúdo portado (sem o genérico); esquema de
  seções/cores adaptado do GTOCore; correções objetivas antes do estilo.
- **Fase 1 (bugs objetivos):** removida a linha genérica `Added by GTNA`; corrigidas 5 descrições
  duplicadas (`me_storage_access_hatch`, `me_big_storage_access_hatch`, `me_io_port_hatch`,
  `infinite_steam_input_bus`, `output_boost_steam_output_bus`); `output_boost_item_bus_*` e
  `output_boost_fluid_hatch_*` ganharam `main_function`; literais hardcoded viraram chaves
  (`nexus_me_hypercore`, `me_storage`, `duration_tester`, Artificial Star, aviso do Large Steam
  Furnace); corrigida a chave do `craft_pattern_hatch` ("Nexus Molecular Forge" → "Nexus Assembly
  Forge"); `huge_steam_bus` dividido em input/output.
- **Fase 2 (piloto de estilo):** convenção GTOCore-adaptada documentada em
  `docs/roadmap/tooltip-standard.md` e aplicada ao Integrated/Advanced Ore Processor, aos hatches
  Accelerate/Thread/Overclock/Output Boost e aos Infinite Input / Output Boost (bus/hatch/steam):
  `§6Main Function:§r` + cores semânticas (`§b` valor, `§a` bônus, `§c` aviso, `§e` ação, `§8` nota).
- **Atribuição:** a linha genérica saiu; o `Source:` continua centralizado em `GTNASources`. A
  extensão de `SOURCES` para a família ME/pattern-buffer ficou **pendente de decisão de atribuição**
  do autor (`MEStorageCoreBlock` ainda hardcoda GTO).
- **Normalização:** os `lang/en_us.json` e `lang/pt_br.json` manuais estavam com CRLF/LF misturados;
  foram normalizados para LF (`.gitattributes` usa `text=auto`), deixando `git diff --check` limpo.
- **Validação:** gate offline completo verde: `spotlessCheck`, `compileJava`, `runUnitTests`
  (**20/20**), `runGameTestServer` (**53/53**) e `runData` determinístico (`written: 0`).
  `git diff --check` limpo. Mudanças locais, sem commit/push.
- **Correções do reteste in-game (mesmo checkpoint):** o autor apontou origens erradas e menções
  redundantes. `SOURCES` ajustado: `large_steam_storage_tank` GTNL→GTO; DT dirt forge/boiler/oven
  GTO→GTL (`GregTech Leisure`, confirmado pelo autor); `eye_of_harmony` GTO→GTNH;
  `nexus_molecular_forge` GTNL→GTO; `crafting_cpu_interface` removido (conteúdo original);
  `directed_tesseract_generator` e `me_storage_access_hatch`/`me_big_storage_access_hatch`/
  `me_io_port_hatch` adicionados como GTO. A separação `──────` antes do `Source:` passou de
  `GTNASteamTooltips` (só steam) para `GTNASources` (todas as máquinas com origem). Removidas as
  menções a mod dos textos (`steam_cracking`, `mega_steam_compressor`, `large_steam_furnace`,
  `large_steam_hammer/compressor/extractor`, `large_steam_storage_tank`, DT machines, `me_storage`)
  no provider e nos manuais `en_us`/`pt_br` (17 linhas). pt_br dos três storage hatches adicionado.
  O rótulo de origem `GregTech Odyssey (GTO)` foi mantido por decisão do autor. Segunda rodada:
  `integrated_ore_processor`/`advanced_integrated_ore_processor` de `GTLCORE`→`GTL` (origem exibida
  deve ser GregTech Leisure); removidas 10 menções "GT-Not-Leisure style" que o hífen escondia da
  varredura anterior e o "GT-Not-Leisure" da estrutura do Primitive Distillation Tower (que é GTO).
- **Fase 3 (após reteste):** passada de estilo aplicada a mais ~25 máquinas (Nexus Assembly Forge,
  Eye of Harmony/Wood, Void Miner, Infernal Coke Oven, Hyper/Compact Hyper Pressure, Leap Forward,
  Industrial Slaughterhouse, Stone Superheater, Steam Manufacturer/Woodcutter, Primitive
  Distillation Tower, Universal Factory, Primitive Stone Furnace, Brick Kiln, Thermal Power Pump,
  Liquefaction Furnace, Industrial Platform Deployment Tools), com `§6Main Function:§r` e cores
  semânticas. Os manuais `pt_br` das mesmas máquinas foram recolorados. Traduzidos os 70 keys de
  `gtna.tooltip.*`/`gtna.multiblock.*` que faltavam no `pt_br` (agora 100%). Criado
  `tools/check_lang_parity.py` (paridade `712/1611`, restam `gtna.machine.*` 246, `block.gtna.*` 482,
  `item.gtna.*` 60). Removido o log `TENTANDO REGISTRAR PARALLEL HATCH`.
- **Conhecido (EMI):** o aviso `[EMI] 2 recipes loaded with the same id: gtna:<máquina>` no client de
  singleplayer é artefato da integração GTCEu×EMI (receitas runtime no pack dinâmico); o gametest
  server não acusa e as receitas funcionam.
- **Fase 4:** descoberto que o `en_us.json` manual (`src/main/resources/...`) é lido primeiro e o
  `add` deduplica, então ele **sobrescrevia** o provider em 191 chaves de tooltip/nome — a passada de
  estilo no provider não aparecia no en_us gerado. Sincronizei ao provider as 24 chaves de tooltip das
  máquinas recoloridas e porteí as cores do manual para o provider nos hatches wireless
  (energy/dynamo) e no Nexus Flux Matrix (`§6Main Function:§r`). O texto dos módulos do Steam
  Elevator e o `duration_tester` continuam sem o cabeçalho (aceitável).
- **Fase 5:** recolorida a família steam GTNL (`gtna.tooltip.large_steam_*`/`steam_*`, ~230 valores)
  com `§6Main Function:§r` nas descrições, `§8` nas estruturas e `§7` no restante.
- **Fase 6:** Reality Ripper deixou de ter literais (`item.gtna.reality_ripper_sword.tooltip.strike`
  / `.kill`); armadura e cartões já usavam chaves. Ficam os literais de HUD do `QuantumTerminalUI`.
- **Atribuição da família ME (decisão do autor):** os pattern buffers e o proxy são conteúdo
  original do GTNA (sem `Source:`); os ME Storage Access/Big/IO Port ficam GTO; os 10 cores seguem o
  GTOCore (`MEStorageCoreBlock`). Sem mudança de código.
- **Pendências:** aplicar a convenção ao restante das máquinas GTNA-nativas/famílias steam, decidir a
  atribuição da família ME, completar o `pt_br` (`gtna.machine.*`/`block.gtna.*`/`item.gtna.*`) e
  converter os tooltips de item. Ver `docs/roadmap/tooltip-standard.md`.

### G-0086 (2026-09-24) — QA de autocrafting AE2 real e semântica de divisor do Overclock Hatch

**1. Autocrafting AE2 ponta a ponta (`universalFactoryAutocraftsThroughAe2Network`).**

- O GameTest monta uma **rede ME real** no `empty_16`: célula criativa de energia, drive AE2 com uma
  célula de item 1K e uma **CPU nativa** de crafting (`CRAFTING_UNIT` + `CRAFTING_STORAGE_1K`, 0
  co-processadores para despachar um pattern por vez). A Universal Factory é formada com o **ME
  Advanced Pattern Buffer** virado para leste (a única face que o nó AE2 expõe), e a rede começa
  nessa face. O grid e a CPU se formam sozinhos via tick do servidor.
- Árvore do pedido: seis recipes sintéticas em seis recipe types, com três camadas e dois joins
  (Bender, Compressor, Forge Hammer → Lathe, Cutter → Forming Press). Os insumos-base são discos de
  música (sem receita no GTCEu) para o planner só poder satisfazer o pedido pelos seis patterns. O
  EU das receitas é de tier LuV para o hatch de teste não overclockar a receita a 1 tick.
- Cada pedido verifica: **planejamento** (`plan.patternTimes().size() == 6`), **despacho** por slot
  (contador novo `gtna$getPushedPatternCount`), **trocas de modo** (conjunto novo
  `gtna$getStartedRecipeTypes`, porque o `activeRecipeType` exibido é sobrescrito quando vários
  threads iniciam no mesmo tick), **consumo** (slots do buffer vazios ao fim) e **retorno/conclusão**
  (item final no armazenamento da rede e `craftingLogic.hasJob() == false`). São **dois** pedidos,
  um de cada vez, removendo a saída antes do segundo.
- Limite honesto: a rede é mínima (sem controlador ME, sem cabos — nós adjacentes) e o teste injeta
  os patterns pelo inventário do buffer; o cenário do autor (rede dele, cabos, terminal) continua
  sendo validação in-game. O teste cobre o que G-0084/G-0085 deixavam fora: planejamento e despacho
  por uma CPU AE2 real.
- Descoberta de API: o planner vanilla do AE2 15.4.10 usa
  `ICraftingSimulationRequester.getGridNode()` em `CraftingTreeNode.buildChildPatterns`; a
  implementação só com `getActionSource()` (lambda) fazia o plano reportar o item final como
  `missing`. O requester do teste devolve o nó do buffer.

**2. Overclock Hatch: divisor inteiro de duração.**

- Confirmado no bytecode do GTOCore (`gtolib-release.jar`): `OverclockPartMachine` tem a chave
  `gtocore.machine.overclock_hatch.divisor` = "Divisor of duration" e `getCurrentMultiplier()`
  retorna `1.0/valor`; o tooltip mostra `100/(tier-6)%`. Não é porcentagem. O port irmão GT-Shanhai
  usa `MIN_DIVISOR = 2` e `max = max(2, tier-6)`, default = max. O bytecode só tem a constante
  `Long 2l` (piso 2).
- O GTNA guardava `round(mult*100)` (ex.: 33) enquanto o tooltip exibia `33.33%`, causando a
  divergência. Agora `OverclockHatchPartMachine` guarda o **divisor** (2..`tier-6`, default
  `tier-6`), `getOverclockMultiplier()` devolve `1/divisor` exato, e o tooltip é derivado do mesmo
  divisor (`100/divisor%`), então tooltip e comportamento coincidem. A UI ganhou o rótulo
  "Divisor of duration" (`gtna.machine.overclock_hatch.divisor`).
- Config: `OverclockHatchBalance.durationMultiplierByTier` (doubles) virou `divisorByTier` (ints
  2..8); valores customizados antigos voltam ao default. A matemática pura ficou em
  `OverclockHatchMath` (piso 2, teto `tier-6`, `stepFactor`, `additionalDurationMultiplier`), usada
  pelo mixin e pela base multi-receita.
- **Hipótese aberta:** o autor relatou um controle "1 a 7" no GTOCore. A evidência (constante `2l`,
  port irmão, tooltip `100/(tier-6)`) aponta para divisor `2..(tier-6)` (OpV = 2..7; MAX = 2..8).
  Se o autor confirmar que o GTOCore aceita divisor 1, basta trocar `OverclockHatchMath.MIN_DIVISOR`
  para 1 (uma linha) e ajustar os testes.

**Validação:** gate offline completo verde: `spotlessCheck`, `compileJava`, `runUnitTests` (**20/20**,
inclui `OverclockHatchMathTest`), `runGameTestServer` (**53/53**, inclui
`universalFactoryAutocraftsThroughAe2Network` e `overclockHatchUsesIntegerDivisor`) e `runData`
determinístico (`written: 1` na primeira execução da chave nova, `written: 0` na segunda).
`tools/check_gametest_report.py` confirmou 53 casos. `git diff --check` limpo. Mudanças locais, sem
commit/push.

**Pendências:** (a) reteste in-game do autor do mesmo pedido AE2 na rede dele; (b) confirmar a faixa
do divisor do GTOCore (2..(tier-6) vs 1..(tier-6)); (c) QA visual da UI do hatch (rótulo e input
inteiro); (d) HUD do Flux Matrix, hatches wireless de alta vazão e revisão geral de tooltips ficam
para as próximas sessões.

### G-0085 (2026-09-24) — GameTest de crafting com subcamadas e trocas repetidas de recipe type

- Após o reteste in-game positivo do autor para G-0084, foi adicionado
  `universalFactoryProcessesLayeredPatternCrafts`. Ele forma a Universal Factory com ME Advanced
  Pattern Buffer, injeta seis receitas sintéticas em seis recipe types e executa quatro pedidos
  completos, um de cada vez. Cada pedido tem três ramos iniciais, uma junção de dois ramos, uma
  transformação do terceiro e uma junção final. O teste extrai cada saída real do barramento,
  verifica os insumos da etapa dependente e exige a troca correta do modo em cada uma das 24 etapas.
- Escopo preciso: a entrega dos insumos e patterns é feita pelo harness diretamente no Pattern
  Buffer. O teste ainda não monta uma rede AE2 com CPU, armazenamento e pedido final para validar o
  planejamento, o despacho e o retorno automático de todas as subcamadas. O cenário real do autor
  segue sendo a validação desse trecho do fluxo.
- Validação: gate offline completo verde (19/19 unitários, 51/51 gametests, datagen `written: 0`).
  A primeira tentativa do gate de partida foi interrompida após o datagen deixar de progredir;
  a execução completa subsequente passou. Mudanças locais, sem commit/push.

### G-0084 (2026-09-24) — pedidos paralelos de laminated glass no ME Advanced Pattern Buffer

- Repro do autor: Universal Factory inicialmente em Extractor, seis patterns de Bender, Lathe,
  Compressor, Cutter, Forming Press e Forge Hammer; quatro pedidos separados de uma receita final
  que usa as seis saídas. Alguns pedidos não concluíram, deixando 6 tempered glass e 3 placas de
  Polyvinyl Butyral no slot de laminated glass, com o modo visual parado em Forge Hammer.
- O mundo salvo confirmou `cachedRecipeId=gtceu:forming_press/laminated_glass`, buffer em All Modes
  e controller ocioso. O novo GameTest com a receita real, ME Advanced Pattern Buffer e três pedidos
  acumulados reproduziu exatamente `staged=[3 PVB plate, 6 tempered glass]`, saída zero e modo sem
  troca. A causa era a validação de identidade de G-0083: comparava o pattern de **uma** unidade
  com a cópia da receita já multiplicada para três paralelos. `matchesPatternDetails` agora usa a
  receita original registrada para reconhecer o pedido; a receita multiplicada continua sendo usada
  pelo GTCEu para consumir insumos e produzir saídas. O GameTest, com energia recarregada como no
  hatch criativo do autor, comprovou consumo e três laminated glass produzidos.
- Validação: gate offline completo verde (19/19 unitários, 50/50 gametests, datagen `written: 0`).
  O teste reproduziu falha antes da correção e passou depois. Continua pendente o reteste da cadeia
  completa no mundo salvo do autor com a CPU/rede AE2. Mudanças locais, sem commit/push.

### G-0083 (2026-09-24) — Universal Factory respeita a receita codificada no Pattern Buffer

- O QA anterior cobria a troca de modo com um buffer fixado e o espelhamento de uma receita, mas
  não dois pedidos da mesma receita nem a execução real de tipos diferentes em threads. O novo
  GameTest forma a Universal Factory com ME Pattern Buffer, começa no modo Bender, envia um pattern
  de Circuit Assembler (cobblestone → stone) duas vezes, em pedidos separados, e verifica cada
  saída. Em seguida, envia esse pattern e outro de Bender ao mesmo tempo e exige duas receitas
  ativas, com saídas exatas.
- Antes da correção, o teste falhou de forma reproduzível: a máquina consumiu o cobblestone e
  produziu **gravel**, apesar do pattern pedir stone. O slot aceitava qualquer receita cujos
  insumos combinassem, mesmo se a saída e o recipe type fossem outros. Agora um slot com pedido
  pendente só cede insumos a uma receita que combine com o pattern codificado, incluindo as saídas.
  A resolução do cache também deixou de associar recipes só por insumos; o circuito compartilhado
  é considerado ao comparar o pattern.
- Validação: gate offline completo verde (19/19 unitários, 49/49 gametests, datagen `written: 0`);
  `tools/check_gametest_report.py` confirmou os 49 casos JUnit contra o log do servidor; `git diff
  --check` verde. O GameTest injeta insumos diretamente nos slots do buffer, sem rede AE2 real:
  confirmar no mundo do autor o envio pelo autocrafting e a conclusão no terminal. Mudanças locais,
  sem commit/push.

### G-0082 (2026-09-24) — QA contínuo e resultados individuais de GameTest

- A partir da disciplina do Horizon-QA, o helper de invariantes agora verifica também o instante
  inicial. O teste negativo da Universal Factory força uma checagem de estrutura e rejeita a
  formação sem Maintenance Hatch durante toda a janela de 40 ticks, inclusive estados transitórios.
- O servidor de GameTest usa o `JUnitLikeTestReporter` nativo do Minecraft, preservando o log
  habitual, e grava um caso por teste em `build/test-results/gametest/TEST-gtna.xml`. O CI exige um
  lote completo com contagem positiva, compara a contagem de casos XML com o log e rejeita casos
  com falha/erro. XML e log são enviados como artefatos para diagnóstico.
- Validação: gate offline completo verde (19/19 unitários, 48/48 gametests, datagen `written: 0`),
  verificador do relatório passou em 48 casos e rejeitou fixtures com caso ausente e com falha.
  `docs/roadmap/qa-strategy.md` foi atualizado. Continuam pendentes os checks visuais in-game de
  G-0081 e os itens de cobertura de QA listados na estratégia. Mudanças locais, sem commit/push.

### G-0081 (2026-09-23) — diagnóstico de estrutura, módulos KubeJS e ajustes visuais

- O `BlockPattern` do GTCEu procura a primeira aisle em várias posições quando o match falha; o
  `PatternError.getPos()` pode ser a última sondagem no ar. A checagem agora percorre os predicados
  de padrões com aisles fixas a partir da célula do controller, usando a mesma transformação de
  direção/flip do GTCEu. Para padrões repetíveis, mantém o erro original do matcher. Dois novos
  gametests afirmam a posição do primeiro casing faltando no Large Cutting Saw e no Large Material
  Press, com controller voltado para norte e leste.
- O botão de rechecagem passou a ser acrescentado a todo controller com UI fancy do GTCEu,
  inclusive ME Storage; a UI própria do Nexus ME Hypercore recebeu o mesmo comando.
- Cada módulo KubeJS acrescenta automaticamente a seção `Auxiliary Module` no tooltip. O evento
  aceita uma chave de idioma opcional que descreve o benefício; o exemplo do Integrated Ore
  Processor informa o Item Import Bus adicional em inglês e português. O servidor sincroniza as
  chaves aos clientes na entrada, inclusive quando o servidor é dedicado. Sem chave específica, a
  descrição localizada genérica informa que o script define as habilidades.
- Os hatches Wireless Steam usam o ícone `LIGHT_ON` do GTCEu adotado no GTOCore. O item
  Borosilicate Glass usa um ícone azul próprio para evitar a silhueta branca no inventário, sem
  mudar a textura do bloco no mundo. O modelo do Solar Boiling Cell declara a textura lateral como
  `particle`, evitando o placeholder preto/roxo ao quebrar; o Solar Heat Collector Pipe Casing usa
  o mesmo gerador de modelo e recebeu a mesma declaração. O Brick Kiln, portado do GTOCore,
  deixou de acrescentar a linha genérica `GregTech Nexus Addon`; a origem GTO já vinha de
  `GTNASources`.
- Validação: gate offline solicitado passou antes das edições (19/19 unitários, 46/46 gametests,
  datagen `written: 0`) e após as edições (19/19 unitários, 48/48 gametests, datagen `written: 0`).
  O PNG do item, o modelo gerado e as chaves `en_us`/`pt_br` foram inspecionados. Restam testes
  visuais in-game do destaque, widgets, tooltip KubeJS, ícones e partículas. Tudo permanece local,
  sem commit/push.

### G-0080 (2026-09-23) — CPU compartilhada e velocidade do Nexus Planner

- A Interface do Hypercore expõe uma CPU com toda a capacidade de armazenamento e co-processadores
  dos cores instalados. Cada pedido aceito reserva bytes da capacidade compartilhada e cria uma CPU
  temporária; jobs simultâneos dividem os lanes de execução. O modo Transcendent preserva capacidade
  ilimitada. A lista de jobs e suas reservas são persistidas. Saves antigos com CPUs independentes
  migram conservadoramente: jobs ativos antigos reservam toda a capacidade até terminarem, porque o
  NBT antigo não guardava a capacidade individual.
- Corrigida a causa do planner que aguardava indefinidamente em grafos grandes: `Ae2PlannerBridge.tick()`
  agora roda no fim de cada tick do servidor Forge. A tela de confirmação do AE2 sincroniza a origem
  real do futuro e exibe `Nexus Planner` quando o motor próprio produziu o plano. `/gtna planner`
  mostra diagnósticos das capturas ativas no jogo.
- QA de velocidade: `./gradlew plannerQa --offline` verde, com 18 cenários comparados ao baseline do
  RaishxCore. Cadeia de 20 mil padrões: p50 de 25,240 ms com grafo em cache e 53,402 ms incluindo
  construção do grafo; p95 de 27,134 ms e 56,132 ms, respectivamente. Numa segunda rodada com o
  client aberto, p50 foi 28,998 ms e 70,291 ms; os 18 cenários também passaram. A comparação cobre
  o motor isolado; a latência de captura/AE2/GUI precisa de medição in-game pelo autor.
- Validação: `spotlessCheck`, `compileJava`, `runUnitTests` (19/19), `runGameTestServer` (46/46) e
  `runData` verdes. O primeiro `runData` escreveu a chave nova de idioma; o segundo escreveu zero.
  Uma terceira execução conjunta passou em unit/gametest, mas o datagen travou por um erro de
  registro de renderização do Annihilate Generator; `runData` isolado foi repetido e passou
  (`written: 0`). `runClient` chegou à tela principal sem erro de mixin no log e foi reiniciado
  após a última correção de nome do job. Não houve commit
  nem push, conforme pedido do autor.

### G-0079 (2026-09-23) — autoria do Hypercore e espaços livres

- Correção do autor: o Nexus ME Hypercore é um multibloco original dele. Foram removidas as
  atribuições GTO e RaishxCore do tooltip do controller. O planner continua adaptado do RaishxCore,
  que também é um mod do mesmo autor; essa relação está documentada em `THIRD_PARTY_NOTICES.md`.
- O símbolo de espaço do padrão 44×22×44 agora usa `any()` em vez de `air()`. Cabos AE2 e outros
  blocos colocados nos espaços livres, inclusive o cabo junto à Interface de CPU, não impedem a
  formação. Os 320 slots de core, casings, controller e Interface mantêm seus predicados.
- A CPU Interface não passa mais a linha `gtna.machine.crafting_cpu_interface.tooltip` explicitamente:
  o `MetaMachineBlock` do GTCEu já a acrescenta automaticamente. A duplicação reportada pelo autor
  desaparece sem remover a informação sobre a exigência de uma Interface.
- O gerador de idioma agora emite nomes para os cinco Cell Components, nove Industrial Components
  e cartões Pattern Buffer Copy/Cut, evitando chaves cruas no client. Os ícones dos dois cartões
  foram substituídos pelos originais distintos do GTLCore; a origem dos assets está em
  `THIRD_PARTY_NOTICES.md`.
- O gametest `brickKilnForms` foi estabilizado: a verificação da estrutura espera dois ticks após
  colocar todos os blocos, evitando concorrer com a checagem assíncrona do controller. Antes da
  mudança ele falhou em duas execuções do gate, e passou em outra, sem alteração no Brick Kiln.
- A pedido do autor, mudanças passam a permanecer **locais** até ele testar e aprovar explicitamente
  a publicação. O `AGENTS.md` local (ignorado pelo Git) foi atualizado com essa regra.
- Validação: gate offline completo verde: `spotlessCheck`, `compileJava`, `runUnitTests` (19/19),
  `runGameTestServer` (46/46) e `runData` determinístico (`written: 0`). Os 46 gametests passaram
  numa segunda execução consecutiva. O idioma gerado contém as 16 chaves novas. Não houve commit
  nem push. QA in-game do cabo, da formação, do tooltip e dos itens depende do teste do autor.

### G-0078 (2026-09-23) — Hypercore multi-CPU, planner e UI

- A Interface de CPU agora tem `IO.NONE`, nenhum slot interno e serve apenas como ligação AE2.
  Cada Crafting Storage Core forma uma CPU AE2 independente (até 320), com nome estável e NBT
  próprio; o NBT antigo de CPU única é migrado. A UI mostra CPUs totais/ocupadas. A capacidade
  de cada CPU acompanha o tooltip do core instalado; Transcendent Mode usa os limites máximos.
- O planner iterativo completo do RaishxCore foi portado de AE2 1.21.1 para AE2 15.4.10/Java 17:
  grafo imutável, escolha de rotas, subprodutos, captura cooperativa em fatias, cache por revisão,
  workers limitados, cancelamento, circuit breaker e fallback AE2. O hook atua somente em redes
  com Interface do Hypercore; `nexusPlannerEnabled` o desliga. Origem RaishxCore creditada via
  `GTNASources` no tooltip do Hypercore e em `THIRD_PARTY_NOTICES.md`.
- O botão de rechecagem não fecha mais a UI; formado fica com visual desativado e só Shift força
  a ação; o servidor não envia mais spam para clique normal no formado. Foi removida a linha
  redundante do tooltip do Crafting Storage Core. As receitas do controller, dos cinco Crafting
  Storage Cores e cinco ME Storage Cores seguem o GTOCore, com componentes de célula 1M–256M
  e seus assets atribuídos à GTO.
- Validação: gate offline completo verde, 19/19 unit tests (inclui casos de planner) e 46/46
  gametests (inclui CPUs independentes e Interface sem inventário), `runData` determinístico.
  A formação do Hypercore, craftings simultâneos reais, planner na rede e interação visual do
  botão ainda precisam de QA no client pelo autor.

### G-0077 (2026-09-23) — correção da direção vertical do Hypercore

- **Feedback in-game:** o autor confirmou que a mudança de `row 10` para `row 18` de G-0076
  **subiu** o controller e a Interface. A interpretação anterior da ordem vertical estava errada:
  neste padrão o índice da linha aumenta para cima no jogo.
- **Correção:** o conversor agora posiciona os blocos em `(aisle 30, row 2, column 21)` e
  `(30,2,22)`, oito blocos abaixo da posição original. Ambos substituem `k` (Laminated Glass) no
  `packet.txt`; as linhas 10 e 18 voltaram a `k`. O `.mbs` foi regenerado, com um controller e
  uma Interface.
- **Validação:** gate offline completo verde: `spotlessCheck`, `compileJava`, `runUnitTests`
  (18/18), `runGameTestServer` (45/45) e `runData` (`written: 0`). A posição visual corrigida
  aguarda confirmação no client.

### G-0076 (2026-09-23) — tentativa de descer controller e Interface do Hypercore

> **Corrigido por G-0077:** `row 18` subiu os blocos no jogo, conforme feedback do autor.

- **Pedido do autor:** deslocar controller e Interface de CPU oito blocos para baixo, mantendo-os
  adjacentes na mesma face do cilindro de vidro laminado.
- **Padrão histórico:** `tools/convert_nexus_me_hypercore.py` substituía vidro nas coordenadas
  `(aisle 30, row 18, column 21)` e `(30,18,22)`. A suposição de que as linhas eram ordenadas de
  cima para baixo estava errada. Ambas as posições eram `k` no `packet.txt`. A
  origem do padrão permanece o arquivo original do autor, com atribuição existente em
  `GTNASources`.
- **Validação:** o `.mbs` contém um controller e uma Interface nas novas posições; as antigas
  voltaram a ser vidro. Gate offline completo verde: `spotlessCheck`, `compileJava`,
  `runUnitTests` (18/18), `runGameTestServer` (45/45) e `runData` (`written: 0`). Formação visual
  ainda pendente no client.

### G-0075 (2026-09-23) — correção da estrutura do Nexus ME Hypercore

- **Correção de origem:** o autor informou que a estrutura 41×43×41 de G-0074 pertence a outro
  multibloco. O texto original foi preservado como `unassigned_41x43x41_source.txt`; seu recurso
  binário e conversor especulativo foram removidos. O Hypercore usa exclusivamente o `packet.txt`
  fornecido em seguida, copiado sem alteração para `docs/structures/nexus_me_hypercore_source.txt`.
- **Novo padrão:** 44 aisles de 22×44, convertidos de forma reproduzível por
  `tools/convert_nexus_me_hypercore.py` para `pattern/nexus_me_hypercore.mbs`. O controller substitui
  vidro laminado em `(aisle 30, row 10, column 21)` e a Interface de CPU fica ao lado em
  `(30,10,22)`, posição confirmada pelo autor em 2026-09-23.
  Os 320 blocos `m` aceitam os cinco tiers de Matrix Crafting Storage Core e o AE2 Crafting Unit;
  o limiar de Transcendent Mode é 320. A base `g` conserva um Parallel Hatch opcional. Os dois
  `grass_block` isolados (`c`) são marcadores de limite do exportador, sem exigência de bloco,
  conforme confirmação do autor em 2026-09-23.
- **Modelo e tooltip:** a aparência do controller voltou ao Nonconducting Casing; o texto agora
  descreve a geometria 44×22×44 e a capacidade de 320 cores. A tooltip auxiliar uniforme de
  G-0074 permanece.
- **Validação:** o conversor confirmou um controller, uma Interface e 320 posições de cores. Gate
  offline completo verde: `spotlessCheck`, `compileJava`, `runUnitTests` (18/18),
  `runGameTestServer` (45/45, incluindo `nexusHypercorePatternLoads`) e `runData` (`written: 0`).
  Código no commit `2b88508`.
- **Pendências:** verificar formação completa e preview no client.

### G-0074 (2026-09-23) — tooltip auxiliar uniforme e novo Nexus ME Hypercore

> **Substituído por G-0075:** o arquivo 41×43×41 era de outro multibloco; os detalhes de Hypercore
> abaixo documentam o estado do commit histórico, não a implementação atual.

- **Tooltip dos módulos:** EBF e liquefaction agora usam o mesmo cabeçalho, explicação e linha
  `Hatch types unlocked...`; apenas os hatches mudam (EBF: Accelerate + Extra Energy;
  liquefaction: Parallel + Accelerate). Os nomes são traduzíveis e foram incluídos em inglês e
  português. A origem GTOCore do módulo EBF continua via `GTNASources`, com rainbow dinâmico.
- **Integrated Ore Processor:** o exemplo KubeJS de G-0073 é uma coluna de três Clean Stainless
  Steel Casings; a posição central também aceita um Item Import Bus. Não mudou neste checkpoint.
- **Nexus ME Hypercore:** o arquivo fornecido pelo autor foi preservado em
  `docs/structures/nexus_me_hypercore_source.txt` e convertido para
  `src/main/resources/pattern/nexus_me_hypercore.mbs` pelo script reproduzível em `tools/`.
  Dimensões 41×43×41, controller na posição `(aisle 40, row 21, column 20)` substituindo o vidro
  central, Interface de CPU na posição adjacente `(40,21,19)` substituindo o grating. As 200 posições
  H e 280 posições I permitem os Matrix Crafting Modules ou o AE2 Crafting Unit no lugar dos
  computer casings; o limiar de Transcendent Mode passou de 481 para 480. O modelo do controller
  usa Advanced Computer Casing. A origem dessa nova geometria é o arquivo do autor, não um port
  externo; o tooltip descritivo foi atualizado.
- **Validação:** o conversor confirmou um controller, uma interface e 480 posições H/I. Gate offline
  completo verde: `spotlessCheck`, `compileJava`, `runUnitTests` (18/18), `runGameTestServer`
  (45/45, incluindo `nexusHypercoreNewPatternLoads`) e `runData` (`written: 0`). Código no commit
  `7331687`.
- **Pendências:** conferir no client a formação completa, o preview do JEI e o encaixe dos cores;
  manter as pendências visuais de G-0073.

### G-0073 (2026-09-23) — EBF auxiliar, diagnóstico de estrutura e módulo KubeJS

- **EBF:** o casco auxiliar aceita exatamente mais um Energy Hatch com fio (total de três, somado
  aos dois permitidos pela base) e rejeita Energy Hatch wireless. O gametest cobre dois hatches na
  base, um no módulo, a rejeição do segundo no módulo e a rejeição do wireless. O tooltip agora
  usa a redação `Auxiliary Module` e descreve `Accelerate Hatch` e `Extra Energy Hatch`, mantendo
  a origem GTOCore via `GTNASources` e o texto rainbow do GTCEu.
- **Botão de estrutura:** o clique client envia um pacote C2S, força uma rechecagem quando Shift
  está pressionado, informa no chat a posição e os blocos encontrado/esperado do primeiro erro e
  destaca essa posição no mundo por 15 segundos. A UI fecha para revelar o destaque. O botão está
  disponível nas máquinas elétricas, inclusive sem módulo registrado. Verificação visual in-game
  ainda pendente.
- **Integrated Ore Processor:** a integração KubeJS agora publica o evento de sub-patterns no
  início do servidor depois que os scripts são carregados, limpa os registros de scripts entre
  reinícios e inclui um exemplo em `examples/kubejs/server_scripts`. O ambiente de desenvolvimento
  carrega KubeJS/Rhino/Architectury e instala esse exemplo antes de `runClient`. O log do gametest
  confirmou `Integrated Ore Processor has 1 auxiliary modules`; presença visual no JEI/client
  ainda pendente. `runData` encerra o executor de scripts do KubeJS depois da geração.
- **Steam Cracker:** por confirmação explícita do autor, permanece o port do GTNL, inclusive
  estrutura, lógica, tooltip e atribuição de origem. Não houve alteração nele neste checkpoint.
- **Validação:** gate offline completo verde: `spotlessCheck`, `compileJava`, `runUnitTests` (18/18),
  `runGameTestServer` (44/44), `runData` (`written: 0`). Código: commit `02acc50`.
- **Pendências:** testar visualmente o botão e o destaque, o módulo KubeJS no client, o tooltip do
  EBF e a latência do liquefaction; comparar a geometria do EBF quando o autor enviar a lista.

### G-0072 (2026-09-23) — atualização de módulos, tooltip, documentação e referências locais

- **Liquefaction:** rechecagem periódica do padrão a cada 5 ticks enquanto formado, seguida de
  reconstrução das partes e do mapeamento de blocos quando o módulo muda. O Terminal Nexus força a
  mesma atualização após construir. A UI de multiblocos elétricos com módulos ganhou o botão
  `Update structure check` com Shift para reconstrução forçada; ícone do GTOCore atribuído em
  `THIRD_PARTY_NOTICES.md`.
- **Hatches:** o match do módulo é rejeitado se base + módulo tiverem mais de um hatch da mesma
  ability Parallel, Accelerate, Thread, Overclock ou Output Boost. O gametest do EBF exercita o
  segundo Accelerate Hatch e a restauração após removê-lo.
- **Tooltip EBF:** a linha de origem GTOCore agora é criada no hover via `GTNASources`, evitando
  congelar a tradução e as cores do arco-íris durante o registro da máquina.
- **Documentação:** README reescrito com a proposta do GTNA, funções atuais, licenças e permissões
  registradas. `AGENTS.md` local foi criado e ignorado pelo Git, com caminhos dos repositórios de
  referência e a regra de usar texturas Modernity-GTNH quando a licença permitir. GTLCore
  `AaAdoniSsS/GTLCore` (`gtl-1431-skyblock`, `18c7814`) e GTLAdditions
  `Dragonators/GTLAdditions` (`master`, `8caff5e`) atualizados via `git pull --ff-only` e listados
  no arquivo local.
- **Validação:** gate offline completo verde: `spotlessCheck`, `compileJava`, `runUnitTests` (18/18),
  `runGameTestServer` (44/44) e `runData` (`written: 0`). Houve uma falha isolada de
  `brick_kiln_forms` na primeira execução e uma falha de registro de renderer no `runData` da
  segunda; ambos passaram na execução completa final, sem alteração nesses componentes.
- **Pendências:** testar no client a latência do liquefaction, o botão e o tooltip; receber o
  print/lista de blocos do módulo EBF para conferir sua geometria real.

### G-0071 (2026-09-23) — correção de crash ao carregar mundo no client

- O `runClient` de G-0070 abriu o menu, mas caiu ao entrar no mundo:
  `GTNAMachineRecipes.registerMEStorageCoreRecipes` chamou
  `ChemicalHelper.getTag(cableGtDouble, Nickel)`, que retorna `null` para o cabo nessa versão do
  GTCEu. A shaped mantém o cabo duplo de Níquel, agora pelo item de
  `ChemicalHelper.get(cableGtDouble, Nickel)`.
- O gametest adicionado em G-0070 inicialmente comparava uma posição relativa com o cache de
  posições absolutas. A asserção agora usa `helper.absolutePos(energyPos)`.
- **Validação:** gate offline completo verde após ambas as correções: `spotlessCheck`, `compileJava`,
  `runUnitTests` (18/18), `runGameTestServer` (44/44), `runData` (`written: 0`). `runClient` foi
  relançado e o log confirmou `Dev joined the game` sem repetir o crash da receita.
- **Pendências:** comparar o print/lista do módulo EBF quando chegar; confirmar a latência do
  liquefaction no client.

### G-0070 (2026-09-23) — liquefaction original, IO do módulo EBF e cache de posições dos módulos

- **Controller do liquefaction:** removida a receita Assembler; a shaped `ABA/CDC/ABA` agora usa
  placas de Invar, cabos duplos de Níquel, blast furnaces vanilla e LV Extractor, conforme
  `GTOCore/data/recipe/classified/Vanilla.java:425`.
- **Liquefaction por prefixo:** cada material registrado percorre `TagPrefix.values()` com
  `generateRecycling()`. O item vem de `ChemicalHelper.get(prefix, material)`; apenas materiais
  com fluido entram, e o dust de material com `PropertyKey.BLAST` é excluído. Quantidade de fluido,
  duração e temperatura seguem `GTORecyclingRecipeHandler.processCrushing`. O multiplicador de
  voltagem segue o cálculo de GTCEu para material (`blastTemp >= 2800` → LV, senão ULV), usado no
  lugar do método nativo de `GTOUtils` do GTOCore.
- **Módulo EBF:** o predicado `A` usa `autoAbilities(..., false, false, true, true, true, true)` para
  aceitar somente IO de item/fluido nessa parte, sem a injeção de Overclock/Accelerate que ocorre
  com energia habilitada. Continua aceitando o Energy Hatch extra e um Accelerate Hatch. A linha
  de origem GTOCore do módulo usa `GTNASources.line` no tooltip.
- **Delay do liquefaction:** `BlockPattern.checkPatternAt` chama `MultiblockState.clean()` para cada
  subpattern e apagava o cache de posições da base. O mixin agora une os caches da base e de cada
  módulo, inclusive posições visitadas por módulos incompletos, para que alterações de blocos
  disparem a checagem imediatamente. O gametest de formação verifica que uma posição da base
  continua no cache após a checagem combinada.
- **Validação:** gate offline completo verde: `spotlessCheck`, `compileJava`, `runUnitTests` (18/18),
  `runGameTestServer` (44/44) e `runData` (`written: 0`).
- **Pendências:** o módulo EBF ainda precisa de comparação com o print/lista de blocos do autor;
  a latência do liquefaction precisa de confirmação manual in-game.

### G-0069 (2026-09-23) — handoff: pendências do feedback in-game (4) — continuar em sessão nova

Quarta rodada de feedback. O autor pediu para **continuar em uma sessão nova** (o contexto foi
compactado várias vezes). Este checkpoint é o handoff: **nada foi corrigido aqui**, só registrado.
O gate segue verde (44/44, `written: 0`).

Itens pendentes, com as referências exatas do GTOCore:

1. **Receita do controller do `liquefaction_furnace` — usar a original do GTOCore** (shaped, não
   assembler). Referência: `GTOCore-Main/.../data/recipe/classified/Vanilla.java:425`:
   ```java
   VanillaRecipeHelper.addShapedRecipe(true, GTOCore.id("liquefaction_furnace"),
       MultiBlockB.LIQUEFACTION_FURNACE.asItem(),
       "ABA", "CDC", "ABA",
       'A', new MaterialEntry(TagPrefix.plate, GTMaterials.Invar),
       'B', new MaterialEntry(TagPrefix.cableGtDouble, GTMaterials.Nickel),
       'C', new ItemStack(Blocks.BLAST_FURNACE.asItem()),
       'D', GTMachines.EXTRACTOR[GTValues.LV].asItem());
   ```
   Trocar a receita atual (ASSEMBLER em `GTNAMachineRecipes`) por essa shaped.

2. **Receitas do `liquefaction` por prefixo de material** (não só bloco): o GTOCore gera para todo
   `TagPrefix` com `generateRecycling()` — ingot, rod, dust, etc. — cada um virando o próprio fluido.
   Referência: `GTOCore-Main/.../data/recipe/generated/GTORecyclingRecipeHandler.java` (`processCrushing`):
   ```java
   if (!material.hasProperty(PropertyKey.FLUID) || material.getFluid() == null ||
           (prefix == TagPrefix.dust && material.hasProperty(PropertyKey.BLAST))) return;
   LIQUEFACTION_FURNACE_RECIPES.recipeBuilder("extract_" + itemPath)
       .outputFluids(material.getFluid((int) (amount * L / M)))   // amount = prefix.getMaterialAmount(material)
       .duration((int) Math.max(1, amount * material.getMass() / M))
       .blastFurnaceTemp(Math.max(800, (int) (material.getBlastTemperature() * 0.6)))
       .EUt(GTOUtils.getVoltageMultiplier(material))
       .inputItems(stack)
       .save();
   ```
   Ou seja: iterar `TagPrefix.values()` filtrando `generateRecycling()`, pegar
   `ChemicalHelper.get(prefix, material)`, e converter a quantidade de material para mB (`* L / M`).

3. **Nexus Terminal** — ✅ consertado (G-0068), confirmado pelo autor.

4. **Módulo do EBF ainda não forma in-game**, apesar do gametest `ebfModuleForms` (G-0068) passar com a
   geometria do GTOCore. Investigar: (a) orientação/facing com que o autor colocou o EBF; (b) os blocos
   exatos usados; (c) se a casca foi montada como o gametest. **Pedir print/lista de blocos.**

5. **EBF aceita 2 Overclock Hatch** — um no módulo e um na base. Causa: o `A` do módulo usa
   `autoAbilities(definition.getRecipeTypes())`, e o `PredicatesMixin` do GTNA injeta
   OVERCLOCK/ACCELERATE em `autoAbilities`. Corrigir usando **IO explícito** no módulo (sem
   overclock), deixando o módulo liberar só o **2º Energy Hatch + Accelerate Hatch** (como o GTOCore).

6. **Delay do módulo no `liquefaction`** — no EBF o refresh (G-0068, `onPartUnload` no mixin) ficou bom,
   mas no `liquefaction` ainda há delay. Investigar por que a diferença (o `liquefaction` é
   `CoilWorkableElectricMultiblockMachine`; o EBF é `CoilWorkableElectricMultiblockMachine` também —
   verificar se o `asyncCheckPattern` do `liquefaction` realmente dispara e se o `onPartUnload` está
   sendo chamado; talvez precise de um re-check imediato em vez de esperar o ciclo de 1 s).

**Estado do gate:** `spotlessCheck` + `compileJava` + `runUnitTests` (18/18) +
`runGameTestServer` (44/44) + `runData` determinístico (`written: 0`). Árvore limpa.

### G-0068 (2026-09-23) — feedback in-game (3): módulo do EBF validado por gametest, terminal constrói módulo em máquina formada, refresh de partes do módulo e receitas do `liquefaction`

Terceira rodada de feedback:

- **Módulo do EBF** "não funciona ainda".
- **Terminal Nexus** não constrói o módulo se o multibloco **já estiver formado** (shift+botão direito no
  controller abre a UI em vez de construir).
- **Remover o módulo** não tira os efeitos (o Parallel Hatch acoplado continua contando) até **quebrar o
  controller** — mesma coisa ao construir o módulo depois. Precisa "resetar".
- **`liquefaction_furnace`**: faltam muitas receitas e a receita do controller.

Correções:

- **Módulo do EBF validado:** novo gametest `ebfModuleForms` monta o EBF stock (3×4×3) **+** a casca
  GTOCore e afirma que `formedModuleCount == 1` — a geometria do GTOCore **casa** com a ancoragem GTNA.
  (O teste também revelou que o EBF exige os hatches de IO: energy in, item in/out e maintenance.)
- **Terminal Nexus:** `NexusTerminalBehavior.useOn` agora constrói também quando `Module Build > 0` com
  o controller **formado** (e devolve SUCCESS, então a UI do multibloco não abre).
- **Refresh de partes do módulo:** `MultiblockControllerMachineMixin` compara a contagem de módulos
  casados com a anterior; se mudou e a máquina está formada, chama `onPartUnload()` — que remove as
  partes inválidas e agenda o re-check assíncrono que reconstrói a lista de partes. Assim
  adicionar/remover um módulo **não** exige mais quebrar o controller.
- **`liquefaction_furnace` — receitas:** geradas por material (bloco → 1152 mB do próprio fluido, 200
  ticks, `temp = max(800, blastTemp * 0.6)`, `EUt` pela blast temperature), no espírito do `GlassRecipe`
  do GTOCore; e uma receita de **controller** no Assembler (invar heatproof + steel casing/pipe +
  cupronickel coils + EV pump + HV circuits).

- **Testes:** gametest novo `ebfModuleForms`. → **44/44**.
- **Validação:** `spotlessCheck` + `compileJava` + `runUnitTests` (**18/18**) +
  `runGameTestServer` (**44/44**) + `runData` determinístico (`written: 0`).
- **Pendências:** validar in-game (EBF + módulo, terminal com máquina formada, remover/adicionar módulo
  sem quebrar o controller).

### G-0067 (2026-09-23) — feedback in-game (2): módulo do EBF fiel ao GTOCore, tooltips de módulo, build de módulo no terminal e receitas do `liquefaction`

Segunda rodada de feedback do autor:

- **Terminal Nexus:** com "Module Build = 1" ele construiu a **base** do multibloco, não o módulo.
- **`liquefaction_furnace`**: "ainda não tem recipe type".
- **Módulo do EBF** não estava como no GTOCore: saiu um bloco **4×3 na frente** do controller.
- Pedido: um **mixin de tooltip** no EBF dizendo o que o módulo libera (no GTOCore: Accelerate Hatch +
  Energy Hatch extra).

Correções:

- **Módulo do EBF fiel ao GTOCore** (`GTMachineModify#ELECTRIC_BLAST_FURNACE.setSubPatternFactory`):
  substituído o bloco 3×4×3 por uma **casca de invar 5×4×5** (5 aisles × 4 linhas × 5 chars) com
  heatproof casing, frames de aço inox e steel pipe casing. As células de heatproof aceitam o IO do
  forno + um **2º Energy Hatch** (`INPUT_ENERGY`, máx. 2) + 1 **Accelerate Hatch** — exatamente o que
  o `moduleTooltips(ACCELERATE_HATCH, EXTRA_ENERGY_HATCH)` do GTOCore anuncia. As células que
  sobrepõem o forno são `any()`.
- **Tooltips de módulo:** `GTNASubPatterns.register(id, factory, Component...)` agora guarda linhas de
  tooltip; `MetaMachineBlockMixin` (novo, injeta no TAIL de `MetaMachineBlock.appendHoverText`) as
  anexa ao item da máquina. Lang: `gtna.machine.electric_blast_furnace.module` e
  `gtna.machine.liquefaction_furnace.module`.
- **Terminal Nexus — "Module Build":** `NexusBlockPattern.autoBuild` virou um seletor que chama
  `buildThisPattern` (o pattern principal) e, se `ModuleBuild > 0`, também constrói os **N primeiros
  módulos** registrados (registry + `ISubPatternMachine`), cada um via `NexusBlockPattern.fromBlockPattern`.
  Mesma ideia do advanced terminal do GTMThings/GTO.
- **`liquefaction_furnace` — receitas fixas:** o recipe type já estava registrado, mas vazio (categoria
  sem receita não aparece no JEI). Adicionadas receitas fiéis ao `GlassRecipe` do GTOCore (bloco do
  material → 1152 mB do próprio fluido, 200 ticks, `temp = max(800, blastTemp * 0.6)`, `EUt = VA[tier]`)
  para Titânio/Tungstênio/HSSS/Naquadah/Tritanium/Neutrônio, com guarda `hasFluid()`/bloco vazio.

- **Testes:** gametest novo `moduleTooltipsAreRegistered`. → **43/43**.
- **Validação:** `spotlessCheck` + `compileJava` + `runUnitTests` (**18/18**) +
  `runGameTestServer` (**43/43**) + `runData` determinístico (`written: 0`).
- **Pendências:** validar in-game a casca do módulo do EBF (a geometria é a do GTOCore com a ancoragem
  GTNA no controller); confirmar se a categoria do `liquefaction` aparece no JEI; o "Module Build" do
  terminal constrói os N primeiros módulos (se o autor quiser escolher um índice específico, ajustar).

### G-0066 (2026-09-23) — feedback in-game: orientação/hatches do `liquefaction_furnace`, preview do módulo no JEI e contagem de módulos na UI

Feedback do autor testando o client:

- **"P:0 não mostra o módulo"** (igual ao GTOCore): o preview de multibloco não expunha a
  estrutura do módulo.
- **`liquefaction_furnace`** com o **controller virado para a direita** e **sem o módulo**.
- **"Não é todo multibloco que tem Thread"**: o `liquefaction_furnace` é uma máquina **normal** —
  não aceita Parallel nem Accelerate na base; só ganha esses hatches **com o módulo instalado**
  (no GTO só `liquefaction_furnace` e `high_temperature_reaction_hub` usam o recipe type, e só o
  segundo aceita Thread Hatch).

Correções:

- **Orientação do `liquefaction_furnace`:** o pattern principal agora usa as direções do GTOCore
  (`FactoryBlockPattern.start(FRONT, UP, RIGHT)`) — o controller deixa de ficar "de lado".
- **Base normal:** `LiquefactionFurnaceMachine` voltou a ser `CoilWorkableElectricMultiblockMachine`
  (sem a base multi-receita, logo **sem threads**) e o pattern base usa **IO explícito** (energy in,
  item in, fluid out, maintenance, muffler) em vez de `autoAbilities(recipeTypes)` — assim o
  `PredicatesMixin` não injeta Parallel/Accelerate na base. Os hatches de performance só vêm do
  módulo.
- **Módulo do `liquefaction_furnace`** movido para `GTNAModules` (registry `GTNASubPatterns`), para
  aparecer no preview e ser o único caminho para Parallel/Accelerate.
- **Preview do módulo no JEI:** `MultiblockMachineDefinitionMixin` injeta em `getMatchingShapes()` e
  anexa cada sub-pattern registrado como **página extra** (o "P:1" do preview).
- **Contagem de módulos na UI:** o `MultiblockControllerMachineMixin` agora implementa
  `IGTNAModuleHost` (campo `gtna$formedModuleCount`, atualizado no `checkPattern()`), e
  `WorkableElectricMultiblockMachineMixin` adiciona **"Formed modules: n / total"** no
  `addDisplayText` (lang `gtna.machine.modules_amount`) — como o "Formed modules: 1" do GTO.

- **Testes:** `liquefactionFurnaceForms` atualizado para a nova orientação (mapa char→-Z, row→+Y,
  aisle→+X) e novo gametest `liquefactionModuleIsRegistered`. → **42/42**.
- **Validação:** `spotlessCheck` + `compileJava` + `runUnitTests` (**18/18**) +
  `runGameTestServer` (**42/42**) + `runData` determinístico (`written: 0`).
- **Pendências (features maiores, não feitas):** o **Nexus Terminal "module build"** (construir a
  estrutura do módulo, não a do multibloco — igual ao advanced terminal do GTMThings/GTO) e um
  **botão dedicado a módulos** no preview. Validar in-game a geometria do módulo do `liquefaction`
  (a torre fica à direita do controller, direções default do GTOCore).

### G-0065 (2026-09-23) — módulo do EBF (sub-pattern Java) pronto para teste in-game

O autor pediu para deixar tudo pronto para testar in-game (generator array, pump, módulo do EBF,
KubeJS).

- **Módulo do Electric Blast Furnace:** `GTNAModules` registra um sub-pattern para
  `gtceu:electric_blast_furnace` via `GTNASubPatterns` — um bloco de heatproof casing atrás do forno
  com **1 Parallel Hatch + Overclock/Accelerate/Thread hatches** (os três últimos já agem em
  multiblocos GTCEu pelo `GTRecipeLogicMixin` do GTNA). As células que sobrepõem o forno são `any()`.
  `GTNAModules.init()` chamado no `GTNAGTAddon.initializeAddon()`.
- **Teste:** gametest `ebfModuleIsRegistered` (o módulo está no registry). A **geometria** do módulo
  é para validar in-game (o teste de formação completo ficou de fora por ora).
- **`thermal_power_pump`** (G-0062) e o **KubeJS** (G-0064) já estão prontos; o **`generator_array`**
  ficou para depois (é um *storage multiblock* que lê geradores singleblock internos + modo wireless —
  port maior).
- **Validação:** `spotlessCheck` + `compileJava` + `runUnitTests` (**18/18**) +
  `runGameTestServer` (**41/41**) + `runData` determinístico (`written: 0`).
- **Pendências:** validar in-game a geometria do módulo do EBF; portar o `generator_array`.

### G-0064 (2026-09-23) — KubeJS para sub-patterns; quests do TST; `lava_furnace` pulado

Feedback do autor: pular o `lava_furnace` (o GTNA já tem um multibloco equivalente); expor os
sub-patterns ao **KubeJS** para criadores de modpack; e as quests do TST estão em
`AdityaVG13/Twist-Stuff`.

- **`lava_furnace` pulado** (decisão do autor). Atualizado no `port-roadmap-by-era.md`.
- **KubeJS para sub-patterns:**
  - `GTNASubPatterns` (registry estático, chave = id da máquina): o KubeJS registra extensões para
    máquinas **novas ou já existentes**; o mixin lê o registry **e** a interface `ISubPatternMachine`.
  - `GTNAServerEvents` + `SubPatternEventJS` (evento de servidor `GTNAServerEvents.subPatterns`):
    `event.add(machineId, definition => FactoryBlockPattern...)`. O `FactoryBlockPattern`/`Predicates`
    já são expostos pelo GTCEu; o evento foi registrado no `GTNAKubeJSPlugin.registerEvents()`.
  - Doc novo `docs/roadmap/sub-patterns.md` com o API e um exemplo.
- **Quests do TST** (`Twist-Stuff`, BetterQuesting 2.8.4/2.9): confirmam a ordem de eras
  (`Tier 0 Stone → Tier 0.5 Steam → Tier 1 LV → … → Tier 12 UMV → Endgame`) e listam os multiblocos
  próprios do TST (high-tier). Registrado em `era-mapping-gto-quests.md`.
- **Validação:** `spotlessCheck` + `compileJava` + `runUnitTests` (**18/18**) +
  `runGameTestServer` (**40/40**) + `runData` determinístico. (KubeJS é `modCompileOnly`; o caminho
  KubeJS não roda no server dedicado — validar in-game com KubeJS instalado.)
- **Pendências:** validar o evento KubeJS in-game; a ancoragem do sub-pattern (ver G-0063).

### G-0063 (2026-09-23) — mecânica de sub-pattern (módulo/extensão) + `liquefaction_furnace` (GTOCore)

Atende ao pedido do autor: **portar o `liquefaction_furnace` e criar a mecânica de módulo/extensão**
que libera novas habilidades (vai ser usada no EBF e em outros multiblocos).

- **Mecânica de sub-pattern GTNA-native** (equivalente ao `addSubPattern` do GTOCore, que é nativo
  no gtolib e não existe no GTCEu 7.5.3):
  - `ISubPatternMachine` (interface GTNA): `List<BlockPattern> gtna$getSubPatterns()`.
  - `MultiblockControllerMachineMixin`: adiciona um `checkPattern()` ao
    `MultiblockControllerMachine` que roda o pattern principal (como o default) e, se a máquina
    implementa `ISubPatternMachine`, checa cada extensão **no mesmo controller** e **funde as partes**
    (hatches/buses) no match context — assim a extensão libera habilidades (Parallel/Accelerate/etc.).
    O context do pattern principal é snapshotado/restaurado porque checar um sub-pattern o reseta.
  - Cobre **qualquer** multibloco (mixin de classe), sem depender da base.
- **`liquefaction_furnace`** (GTOCore, LGPLv3):
  - Recipe type `gtna:liquefaction_furnace` (1 item in / 1 fluid out, EU in, temperatura/coil nos
    data infos, barra EXTRACT, som ARC).
  - `LiquefactionFurnaceMachine extends CoilWorkableElectricMultipleRecipesMachine` (o `beforeWorking`
    já rejeita receitas acima da temperatura das coils) + `ISubPatternMachine`.
  - Pattern principal 5×3×5 (heatproof casing + coils + steel casing/pipe + muffler).
  - Sub-pattern (a torre de aço inox do GTOCore) definido via a mecânica nova.
- **Sem receitas fixas** (decisão A do autor): o GTOCore usa materiais que o GTNA não tem
  (cryotheum/antimatter/vidros do GTOCore); a máquina fica disponível para datapacks/modpacks.
- **Infra:** config toggle `liquefactionFurnace`, lang, atribuição `GTNASources` → `gto`, receita de
  craft do controller.
- **Teste:** gametest `liquefactionFurnaceForms` (pattern principal forma). → **40/40**.
- **Validação:** `spotlessCheck` + `compileJava` + `runUnitTests` (**18/18**) +
  `runGameTestServer` (**40/40**) + `runData` determinístico (`written: 0`).
- **Pendências:** validar no client a **geometria/ancoragem exata do sub-pattern** do GTOCore (o
  GTNA ancora o sub-pattern no controller; o GTOCore usa um offset próprio no gtolib). O mechanic
  está pronto; se a torre não casar in-game, ajustar o sub-pattern/ancoragem.

### G-0062 (2026-09-23) — `thermal_power_pump` (GTOCore) portado

Segundo alvo do mapeamento por eras (G-0061). Port do **`thermal_power_pump`** do GTOCore (LGPLv3):

- **Máquina `ThermalPowerPumpMachine`** (`noenergy`): base `WorkableElectricMultipleRecipesMachine` no
  ramo zero-energy (`IZeroEnergyMachine`), `DUMMY_RECIPES`, sem energy hatch. Replica a mecânica do
  GTOCore com um **tick GTNA-native**: `production = biomeModifier << 8` (via
  `GTUtil.getPumpBiomeModifier`), `×3/2` se chove no bioma, ciclo de 20 t; drena steam dos hatches de
  input e enche água nos de output (nada é voidado — só o que o output aceita é produzido).
- **Estrutura** decodificada de `pattern/thermal_power_pump.mbs` do GTOCore: 3 largura × 3 altura ×
  8 profundidade, com `BRASS_REINFORCED_WOODEN_CASING` (A/D, A com 1 import fluid + 1 export fluid +
  1 maintenance), `CASING_BRONZE_BRICKS` (C), `CASING_BRONZE_PIPE` (E), `BRONZE_REINFORCED_WOOD` (F,
  aproximação do `REINFORCED_WOOD_CASING` do GTOCore), frame de TreatedWood (G) e
  `CASING_BRONZE_GEARBOX` (H).
- **Infra:** config toggle `thermalPowerPump`, lang (nome, tooltips, produção/chuva), atribuição
  `GTNASources` → `gto`, receita de craft do controller.
- **Teste:** gametest `thermalPowerPumpForms` (monta e forma a estrutura decodificada, com os 3
  hatches exatos). → **39/39**.
- **Validação:** `spotlessCheck` + `compileJava` + `runUnitTests` (**18/18**) +
  `runGameTestServer` (**39/39**) + `runData` determinístico (`written: 0`).
- **Pendências:** QA manual (formação/GUI/produção no client). Próximo: **`liquefaction_furnace`**
  (máquina de bobina; tem `addSubPattern` do GTOCore e receitas que usam materiais do GTOCore —
  decidir adaptação).

### G-0061 (2026-09-23) — mapeamento de eras pelas quests do GTO (fim do "chute cego")

Dica do autor: o modpack **GregTech Odyssey** tem **FTB Quests por tier**, o que dá o mapa real de
eras sem adivinhar.

- **Fonte:** `.../instances/GregTech.Odyssey-0.6.0-dev1/minecraft/config/ftbquests/quests/chapters/*.snbt`
  — um capítulo por tier, cada quest com o item-alvo em `tasks[].item`. O GTNL tem BetterQuesting só
  com as eras steam (`Tier 0.75 Superheated`, `Tier 0.999 Supercritical`); o TST não tem dados de
  quest no repo. Logo, o GTO é a fonte de mapeamento.
- **Ordem das eras (order_index):** stoneage → steam → ulv → lv → mv → hv → ev → iv → luv → zpm → uv
  → uhv → uev → uiv → uxv → opv (confere com o roadmap).
- **Novo doc:** `docs/roadmap/era-mapping-gto-quests.md` — lista, por era, os itens
  `gtocore:`/`gtmthings:` (com marca ✅ quando o GTNA já tem), a ordem das eras e um **resumo dos
  multiblocos GTOCore por era** (confirmados no código do GTOCore).
- **Achados que corrigem o inventário:**
  - A era **ULV** do GTO é dominada por **singleblocks** do GTOCore (`ulv_assembler`, `ulv_lathe`,
    `ulv_wiremill`, `ulv_chemical_reactor`, `ulv_packer`, `ulv_fluid_solidifier`, `ulv_loom`,
    `ulv_electric_*`); de multibloco só `primitive_distillation_tower` (✅) e `digital_miner`. →
    a era ULV **fecha** (com o `brick_kiln`, G-0060); o autor tinha razão que o `chemical_plant` não é
    early.
  - **LV** tem multiblocos GTOCore genuínos: `liquefaction_furnace`, `lava_furnace`, `generator_array`,
    `tree_growth_simulator`, `thermal_power_pump`, `gas_compressor`.
  - **MV**: `reaction_furnace`, `greenhouse`, `crystallization_chamber`, `component_assembler`,
    `processing_plant`.
- **Próximo passo:** portar um multibloco **LV** do GTOCore (sugestão: `liquefaction_furnace` ou
  `lava_furnace`), seguindo o `era-mapping-gto-quests.md`.
- **Validação:** mudança só de documentação (doc novo + roadmap + ledger); gate de código não afetado.

### G-0060 (2026-09-23) — `brick_kiln` (GTOCore) portado; era ULV fechada

Fecha a lacuna ULV do inventário (G-0059) com o port do **`brick_kiln`** do GTOCore (LGPLv3):

- **Recipe type `gtna:brick_furnace`**: `setMaxIOSize(3, 1, 1, 0)`, sem EU, som `FURNACE`.
- **`BrickKilnMachine`** (`noenergy`): base `WorkableElectricMultipleRecipesMachine` no ramo
  zero-energy (`IZeroEnergyMachine`), `getMaxParallel() = 4` (GTOCore `accurateParallel(4)`), sem
  energy hatch. O `IZeroEnergyMachine` ganhou o sentinel **`gtna$recipeDuration() <= 0` = manter a
  duração da receita** (o kiln usa 150 t; o `PrimitiveStoneFurnace`/Dirt Forge continuam forçando 1).
- **Estrutura** decodificada de `pattern/brick_kiln.mbs` do GTOCore (formato nativo JNI, sem reader
  em Java): 5 largura × 4 altura × 7 profundidade, oca, com `CASING_PRIMITIVE_BRICKS` (A/C, A com IO),
  `Blocks.BRICKS` (B), `Blocks.STONE_BRICKS` (D) e o controller na última aisle.
- **Receitas** (fiéis ao GTOCore): `bricks`, `coke_bricks` e `primitive_bricks` ×2 de
  `compressed_clay`/`compressed_coke_clay`/`compressed_fireclay` ×8 + carvão, 150 t cada.
- **Infra:** config toggle `brickKiln`, lang (`gtna.brick_furnace`, nome, tooltips), atribuição
  `GTNASources` → `gto`, receita de craft do controller.
- **Teste:** novo template de gametest `empty_16` (a estrutura 7-de-fundo não cabe num quadrante
  disjunto do `empty_12`) + gametest `brickKilnForms` (monta e forma a estrutura decodificada).
- **Validação:** `spotlessCheck` + `compileJava` + `runUnitTests` (**18/18**) +
  `runGameTestServer` (**38/38**) + `runData` determinístico (`written: 0`).
- **Pendências:** QA manual visual (formação/GUI no client). **Era ULV fechada** — a próxima é o
  **port GTO/GTOCore de tier médio/alto** (mapear tiers/eras pelas quests — ver G-0061).

### G-0059 (2026-09-23) — inventário das eras ULV e LV (pós-Steam)

Com a era Steam fechada (G-0058), o roadmap manda inventariar a próxima era. Feito:

- **ULV:** quase toda coberta pelo **GTCEu base** (`primitive_blast_furnace`, `primitive_pump`,
  `charcoal_pile_igniter`, `coke_oven`, `multi_smelter`) + primitivos do GTNA. **Uma** lacuna
  genuína: **`brick_kiln`** (GTOCore, no-energy, paralelo 4, `BRICK_FURNACE_RECIPES`) — coze
  `bricks`/`coke_bricks`/`primitive_bricks` a partir de `compressed_clay`/`compressed_coke_clay`/
  `compressed_fireclay` + carvão; distinto do `primitive_stone_furnace` (que só faz `FURNACE_RECIPES`).
  Estrutura minúscula (`brick_kiln.mbs`, ~213 B).
- **LV:** **nenhuma lacuna genuína** — os multiblocos LV vêm com o GTCEu/GCYM (`large_chemical_reactor`,
  `multi_smelter`, `large_maceration_tower`, `large_*`, `alloy_blast_smelter`, ...) e o GTNA cobre os
  específicos. A continuação é o **port GTO/GTOCore de tier médio/alto** (manifest, ordem de entrega
  passo 3).
- Detalhe/evidência nas seções `## ⚡ Era ULV` e `## 🔌 Era LV` de `docs/roadmap/port-roadmap-by-era.md`.
- **Próximo passo recomendado:** portar o **`brick_kiln`** (fecha a era ULV, baixo custo: base
  no-energy + recipe type + 3 receitas + estrutura) e então seguir para o port GTO (ex.: `chemical_plant`,
  `recycler`, `mass_fabricator`).
- **Validação:** mudança só de documentação (roadmap + ledger); gate de código não afetado.

### G-0058 (2026-09-23) — fecha a era Steam Elevator; logo do mod em todas as UIs de multibloco

Feedback do autor: **o módulo do elevador está 100%** — "podemos finalizar essa etapa do steam
elevator de fato". Aproveitado para corrigir a logo.

- **Era Steam Elevator fechada:** o `SteamOreProcessorModule` (G-0052/G-0056/G-0057) refina
  raw/stone ore e consome o fluido da receita integrada por circuito; os 8 módulos, o host 35×43×35,
  a rede wireless de steam e os tooltips estão no gate. Pendências restantes são só **QA manual
  visual** (checklist).
- **Logo do mod em todas as UIs de multibloco:** antes só as UIs steam custom
  (`SteamMultiMachineBase`, `LargeSteamSolarBoiler`, `SteamElevator`, `SteamElevatorModuleMachine`)
  desenhavam a `GTNATextures.LOGO`; os multiblocos "fancy" (elétricos, no-energy e o
  `SteamManufacturer`/`VoidMinerSteamGateAged`, que trocam para `FancyMachineUIWidget`) e as duas
  UIs custom 310×270 (`NexusMEHyperCore`, `NexusFluxMatrix`) ficavam sem.
  - **Mixin client-only `FancyMachineUIWidgetMixin`** (`gtna.mixins.json` → `client`): no `RETURN` de
    `setupFancyUI(IFancyUIProvider, boolean)`, se o `mainPage` é um `MultiblockControllerMachine` do
    namespace `gtna`, adiciona a logo no canto inferior-direito do `pageContainer` (o
    `clearUI()` do próprio setup garante uma única logo por navegação). Cobre **todos** os fancy de
    uma vez, inclusive máquinas futuras.
  - Logo explícita adicionada nas duas UIs custom (`NexusMEHyperCore`, `NexusFluxMatrix`, em
    `(281,161)`).
  - A logo é uma textura 512² desenhada em 18×18 (o `ResourceTexture` escala pela UV 0..1).
- **Validação:** `spotlessCheck` + `compileJava` (0 warnings de mixin) + `runUnitTests` (**18/18**) +
  `runGameTestServer` (**37/37**, mixin é client-only e não entra no server dedicado).
- **Pendências:** QA manual visual no client (logo nas UIs fancy/custom; conferir se não sobrepõe
  conteúdo em telas cheias).

### G-0057 (2026-09-23) — fix urgente: módulo devolvia o minério cru; fluido da receita pelo material

Feedback do autor: com o módulo já rodando (G-0056), ele **pegava raw gold e devolvia raw gold**
(não refinava) e a UI mostrava `Fluid: not required` no modo 2.

- **Causa raiz do "não refina":** o `recipeOutputs`/`recipeFor` passavam `Ingredient.of(stack)` para
  `GTRecipeType.db().find(...)`. As receitas de minério usam **tag** (`forge:raw_materials/gold`), e só
  o lookup por **ItemStack** expande as tags do item em `ItemTagMapIngredient` (o nó que o DB indexa);
  `Ingredient.of(ItemStack)` vira um `ItemValue` e não gera nó de tag → lookup vazio → o `refine`
  devolvia o input. Corrigido passando o **ItemStack** (`ore.copyWithCount(1)`) — igual ao
  `SmartItemFilter` do GTCEu.
- **Causa raiz do fluido:** o DB de um recipe type **multi-input** não acha a receita buscando só o
  minério (a 1ª entrada é o circuito; a árvore é ordenada), e `ChemicalHelper.getMaterialStack(rawOre)`
  volta com `amount = 0` (o `MaterialStack.isEmpty()` é true). Solução: o fluido exigido é calculado do
  próprio `OreProperty` via `ChemicalHelper.getMaterialEntry(item)` (que devolve
  `forge:raw_materials/gold` corretamente) — mesma regra do `IntegratedOreRecipes` (circuito 1 sem
  fluido; 2/3/4 distilled water; 5/6/7 o `getWashedIn()` do minério; amount `100*crushed` ou
  `washed*crushed`).
- **Teste:** o gametest `oreProcessorModuleRefinesRawOre` agora checa `refine(rawGold, 2)` termina em
  **gold dust** (não raw gold) e que `requiredFluidFor(rawGold, 1)` é vazio e `(rawGold, 2)` é
  **distilled water**. → **37/37**.
- **Validação:** `spotlessCheck` + `compileJava` + `runUnitTests` (**18/18**) +
  `runGameTestServer` (**37/37**).
- **Pendências:** re-teste manual do módulo no client (output = dust + byproducts; fluido por circuito).

### G-0056 (2026-09-23) — feedback do client: lang do recipe type, receitas de craft e módulo sem lubricant

Feedback do autor testando o client depois do G-0055:

- **Lang do recipe type:** o JEI mostrava a chave crua `gtna.ore_processing` como título da categoria.
  O GTCEu deriva o nome da categoria de `recipeType.registryName.toLanguageKey()` (ponto, não
  underscore), então a chave certa é `gtna.ore_processing` (não `gtna.recipe_type.ore_processing`).
  Corrigido no `GTNALangProvider` e o tooltip `available_recipe_map` passou a usar a mesma chave.
- **Receitas de craft dos multiblocos:** o GTLCore não define craft para eles; o GTNA ganhou as suas
  em `GTNAMachineRecipes`:
  - `integrated_ore_processor` no **Assembler** (EV): 4× stainless clean casing, 4× frame BlueSteel,
    2× gearbox + 2× pipe de tungstensteel, 4× motor EV, 2× pump EV, 4× circuito EV, soldering 288;
  - `advanced_integrated_ore_processor` na **Assembly Line** (UHV): 8× tungstensteel robust casing,
    8× frame HSSS, 4× `RESTRAINT_DEVICE`, 8× `BOROSILICATE_GLASS_BLOCK`, 4× emitter/sensor/field
    generator UHV, 4× circuito UHV, 4× plateDouble NaquadahAlloy, soldering 1296, com station research
    (CWUt 1024).
- **Steam Ore Processing Module — sem lubricant:** o módulo ficava **Idle** com só distilled water
  porque o `isModuleWorking()` (G-0052/GTNL) exigia 1 mB de lubricant por minério além de 10 mB de
  água. Decisão do autor: **consumir só o fluido da receita integrada**, sem lubricant. Agora:
  - o módulo lê a receita `gtna:ore_processing` do minério no circuito atual e exige/consome o fluido
    dela (circuito 1 não precisa de fluido; 2/3/4 distilled water; 5/6/7 o fluido do minério, ex.
    mercúrio);
  - `isOre` foi **endurecido**: só aceita prefixos de minério (`ORES`, `rawOre`, `crushed`,
    `crushedPurified`, `crushedRefined`) — antes um lingote passava como "ore" e o módulo o
    "processava" à toa;
  - a UI mostra `Fluid: <fluido> <tem>/<precisa>` em vez de `Water/Lubricant`; tooltip atualizado.
- **Validação:** `spotlessCheck` + `compileJava` + `runUnitTests` (**18/18**) +
  `runGameTestServer` (**36/36**) + `runData` determinístico.
- **Pendências:** re-teste manual do módulo no client (fluido por circuito) e da formação/GUI dos dois
  multiblocos.

### G-0055 (2026-09-23) — Integrated / Advanced Integrated Ore Processor portados do GTLCore + receitas fiéis

- **Port completo dos dois multiblocos do GTLCore (decisão do autor no G-0054):**
  - **`gtna:integrated_ore_processor`** (`IntegratedOreProcessorMachine`, base
    `WorkableElectricMultipleRecipesMachine`): estrutura 6×12×11 idêntica à do GTLCore
    (`CASING_HSSE_STURDY` + `CASING_STAINLESS_CLEAN` (mín. 60) + `CASING_LAMINATED_GLASS` +
    `frameGt BlueSteel` + gearbox/pipe de tungstensteel + muffler ZPM), `NON_Y_AXIS`,
    `allowExtendedFacing(false)`. Aceita Parallel Hatch (autoAbilities), Maintenance, Thread/Overclock/
    Accelerate (mixin `PredicatesMixin` cobre overclock/accelerate).
  - **`gtna:advanced_integrated_ore_processor`** (`AdvancedIntegratedOreProcessorMachine`,
    `getMaxParallel() = Integer.MAX_VALUE`): estrutura 32×12×15 idêntica à do GTLCore, laser-only
    (`INPUT_LASER` + item/fluid IO) e Thread/Overclock/Accelerate. Blocos de outro mod substituídos por
    equivalentes GTNA (regra do manifesto): `kubejs:restraint_device` →
    `GTNABlocks.RESTRAINT_DEVICE`; `GTLBlocks.HSSS_REINFORCED_BOROSILICATE_GLASS` →
    `GTNABlocks.BOROSILICATE_GLASS_BLOCK`. `~` na última aisle (convenção §5).
  - As duas estruturas foram **verificadas programaticamente** contra o GTLCore (comparação exata das
    32/6 aisles) antes de compilar.
- **Receitas fiéis (substituem a geração simplificada):** novo `IntegratedOreRecipes` (datagen hook,
  sem mixin) replica o `OreRecipeHandlerMixin` do GTLCore — **um recipe por circuito 1..7** para
  **raw ore** e **stone ore**, com:
  - os **byproducts reais por estágio** (`property.getOreByProduct(i, material)`), inclusive os
    secundários do prefixo `ore` (67% no stone, 5% só no circuito 3 do raw) e os "1/9"/"1/3" chanced;
  - o **fluido de lavagem real** de cada material (`property.getWashedIn()`): **distilled water** nos
    circuitos 2/3/4, e **mercúrio / sodium persulfate / etc.** nos circuitos 5/6/7 (depende do minério);
  - durações por estágio (`IntegratedOreMath`) e EUt 30, iguais ao GTLCore;
  - `crushedAmount` = GTLCore (`integratedOreMultiplier` config, default **4** em `GTNABalance`).
  - Circuitos condicionais como no GTLCore: 2/5 exigem `crushedRefined`; 4/7 exigem `GEM`; 5/6/7
    exigem `washedIn`.
  - **Recipe type** `gtna:ore_processing` ajustado para `setMaxIOSize(2, 9, 1, 0)` (paridade
    `integrated_ore_processor` do GTLCore).
- **Verificação das receitas:** com `dev.dumpRecipes=true` (temporário) o dump gerou **818** receitas
  (`409` raw + `409` stone), circuitos 1..7 presentes, fluidos `forge:distilled_water` (500),
  `forge:sodium_persulfate` (54) e `forge:mercury` (44) — ex. `raw_5_cooperite` consome 400 mB de
  mercúrio e devolve cooperite dust ×4 + nickel byproduct. Sem `Parsing error loading recipe gtna:`,
  sem warning de max-IO para `ore_processing`.
- **Registro/infra:** toggles `integratedOreProcessor` / `advancedIntegratedOreProcessor` no
  `ConfigHolder`; lang (nome, tooltips fiéis, `gtna.recipe_type.ore_processing`, opções de config);
  atribuição `GTNASources` → `gtlcore` (decisão do autor: TST só no `eye_of_wood`); `GTNABalance`
  ganhou `machines.integratedOreMultiplier`.
- **Testes:** novo unit test `IntegratedOreMathTest` (crushedAmount/wash/duration/clamp) → **18/18**;
  novo gametest `integratedOreProcessingIsFaithful` (carrega o recipe manager e checa raw+stone,
  circuitos 1..7, distilled water, mercúrio, circuito 1 sem fluido, ≥2 outputs) → **36/36**.
- **Validação:** `spotlessCheck` + `compileJava` + `runUnitTests` (**18/18**) +
  `runGameTestServer` (**36/36**, `All 36 required tests passed`) + `runData` determinístico
  (`written: 0`).
- **Pendências:** QA manual in-game (formação das duas estruturas — a Advanced é enorme; GUI dos
  hatches de performance; JEI mostrando as variantes por fluido). Sem receita de craft definida
  ainda (o GTLCore registra os itens via registrate; o GTNA precisa decidir a receita/era das duas).

### G-0054 (2026-09-23) — Integrated Ore Processing: receitas aparecem no JEI; plano de portar os multiblocos do GTLCore

- **Estado:** as receitas do `gtna:ore_processing` **aparecem no JEI** (o input agora é o **raw ore**,
  fallback crushed). O log de startup confirma `[GTNA] registered 770 integrated ore processing recipes`
  e o EMI subiu de 61068 → 61838.
- **O que está fiel:** o **recipe type** (`gtna:ore_processing`, no modelo do `gtlcore:integrated_ore_processor`),
  os **7 circuitos** (chains 1..7 do tooltip do GTLCore) e o input raw ore + circuito + água nos modos de wash.
- **O que ainda diverge do GTLCore:** as receitas são geradas **estaticamente e simplificadas**
  (`dust` principal + stone + dustSmall). O GTLCore gera **variantes por fluido** (mercúrio / água /
  distilled water) com **byproducts reais por estágio** (`property.getOreByProducts()`), durações e
  chances próprias — via `OreRecipeHandlerMixin` (mixin no `OreRecipeHandler` do GT, que roda quando os
  maps do GT já estão prontos). Referência: `~/MineProjects/GTLCore`
  (`.../mixin/gtm/recipe/OreRecipeHandlerMixin.java`, `.../data/GTLRecipeTypes.java`
  `INTEGRATED_ORE_PROCESSOR`, `.../data/machines/MultiBlockMachineA.java`).
- **Decisão do autor (2026-09-23):** portar **100%** os multiblocos **Integrated Ore Processor** e
  **Advanced Integrated Ore Processor** do GTLCore para termos a base completa (estrutura, GUI,
  parallel/overclock hatch, receitas integradas fiéis).
- **Pendência aberta:** ~~o port dos dois multiblocos acima + trocar a geração simplificada pela fiel
  (variantes de fluido e byproducts)~~ → **resolvido no G-0055**. TST: atribuição só no `eye_of_wood`;
  `industrial_slaughterhouse` é **GTO** (corrigido em `2269542`).
- **Validação:** `spotlessCheck` + `compileJava` + `runUnitTests` (**17/17**); `runGameTestServer`
  (**35/35**).

### G-0053 (2026-09-22) — variantes I/II/III de Entity Crusher, Flight e Weather

- Feedback do autor: "como aumenta o tier do mob crush, flight, weather" → **adicionar variantes I/II/III**
  (desvio consciente do GTNL, onde esses 3 são tier 1 único).
- **Registro**: os ids `steam_elevator_{flight,weather,entity_crusher}_module` viraram
  `..._module_i/_ii/_iii` (tiers 1/2/3), com 9 `MachineDefinition`s, tooltips e receitas hidráulicas
  correspondentes. O `GTNASources` ganhou as 9 entradas (atribuição GTNL).
- **Escala por tier**:
  - Flight: alcance `64 * tier` (I/II/III = 64/128/256); upkeep já era `tier * V[5]`.
  - Weather: a carga cobre `1 h * tier` (I/II/III = 1/2/3 h) pelo mesmo custo de steam.
  - Entity Crusher: ciclo `400 >> (tier-1)` e upkeep `512 << (tier-1)` (I/II/III = 400/200/100 t e
    512/1024/2048 mB/t).
- **Lang**: as linhas de tooltip são compartilhadas por um helper novo
  (`GTNALangProvider.elevatorModuleTiers`), que registra nome + `.tooltip` + `.tooltip.n` para os 3
  ids de uma vez, evitando triplicar o texto.
- **Validação:** `spotlessCheck` + `compileJava` + `runUnitTests` (**17/17**, incl.
  `ModuleTooltipContractTest` com **20 módulos**); `runGameTestServer` (**35/35**); `runData`
  determinístico (`written: 0`, 30 arquivos escritos na 1ª passada) e 0 `Parsing error`.
- **Pendências:** o **Ore Processor com corrente de receitas GT real** (macerator→washer→thermal→
  centrifuge, escolha do autor) ainda **não** foi implementado — é a próxima tarefa. QA manual das
  variantes de tier no client (`docs/roadmap/QA-MANUAL-CHECKLIST.md`).

### G-0052 (2026-09-22) — módulos do elevador usam os hatches da própria estrutura (item/fluido) e mostram o status padrão

Feedback do autor no client:
- **Bug de UI**: os labels com `%` literal rendiam `Format error: ...` (o `LabelWidget` passa o texto
  por `I18n.get`, que chama `String.format`; um `%` solto estoura). Corrigido escapando `%%` (Entity
  Crusher e Bee Breeding) e usando `Locale.ROOT` no número.
- **IO errado**: os módulos tinham inventários/tanques **internos** (slots na GUI) em vez de usar os
  barramentos/hatches da própria estrutura 1x5x2. Agora **todo IO de item/fluido vai pelos hatches**:
  input bus, output bus, input hatch e output hatch. A base varre os parts por ability
  (`IMPORT_ITEMS`/`STEAM_IMPORT_ITEMS`, `EXPORT_ITEMS`/`STEAM_EXPORT_ITEMS`, `IMPORT_FLUIDS`,
  `EXPORT_FLUIDS`, `STEAM`) em `onStructureFormed` e expõe `countItem/consumeItem/findItem/findCircuit`,
  `canInsertItems/insertItems`, `countFluid/drainFluid/canInsertFluid/insertFluid`. Usa
  `extractItemInternal`/`insertItemInternal`/`drainInternal`/`fillInternal` porque as checagens de
  direção das capabilities bloqueariam o lado interno.
- **Oil Drill**: o óleo ia para um tanque interno; agora vai para o **fluid output hatch** (o módulo
  precisa de ≥1 hatch de saída, como o autor apontou).
- **Apiary / Bee Breeding / Greenhouse / Ore Processor / Weather / Entity Crusher**: mesmos ajustes
  (água/lubrificante no input hatch, comb/spawner/circuito no input bus, produtos no output bus).
  O circuito do Weather/Ore Processor agora é lido do **input bus** (não há mais slot interno).
- **Status "Working"**: a base agora adiciona a linha padrão do GTCEu via `MultiblockDisplayText`
  (`Running Perfectly` / `Idling`) e `isActive()` exige steam para pagar o upkeep, então o módulo
  para de parecer "sempre ativo" e passa a mostrar o status como as outras máquinas.
- **Logo**: `GTNATextures.LOGO` apontava para `gtna:logo` (textura inexistente; o log do client
  mostrava `Failed to load texture: gtna:logo`). Corrigido para `gtna:textures/logo.png`.
- **Validação:** `spotlessCheck` + `compileJava` + `runUnitTests` (**17/17**); `runGameTestServer`
  (**35/35**); `runData` determinístico (`written: 0`) e 0 `Parsing error loading recipe gtna:`.
- **Pendências:** re-teste manual no client com os hatches (a checklist ganhou a seção "IO dos
  módulos"); avaliar se `isActive()` deve olhar também os insumos (hoje olha só o upkeep de steam).

### G-0051 (2026-09-22) — Bee Breeding vira integração real com Productive Bees (módulo só existe com o mod)

- **Correção de diagnóstico:** o bloqueio anterior ("sem artefato 1.20.1") vinha de olhar só os caches
  locais (1.21.1 NeoForge). O Modrinth tem `productivebees 1.20.1-12.6.0` (forge+neoforge) e o maven
  `https://api.modrinth.com/maven` resolve; o artefato é bundlado com o ProductiveLib (jarjar), sem
  dependência dura extra.
- **build.gradle:** repositório Modrinth + `modCompileOnly` **e** `modRuntimeOnly`
  `maven.modrinth:productivebees:1.20.1-12.6.0` (o runtime é só de dev; o jogador não é obrigado a
  instalar). O `BuildDependencyContractTest` aceita o par compile+runtime do mesmo coordinate.
- **`SteamBeeBreedingModule`** agora usa a API real do PB: a "rainha" é qualquer **spawn egg** do PB
  (`cy.jdkdigital.productivebees.common.item.SpawnEgg`) no inventário de entrada e é **catalisador**
  (não consumido), fiel ao slot do controller do GTNL; o consumível é **128 honey treats** do PB (o
  "royal jelly"); a saída é **uma cópia da mesma abelha** (NBT incluso) após 12000 ticks. Upkeep
  `GTValues.V[6]` e tier 8 mantidos. Sem abelha/feed o progresso fica em 0 e o ciclo idles
  (`NO_RECIPE` no GTNL).
- **Registro condicional:** `GTNAMachines2` só chama `registerElevatorModule("steam_elevator_bee_breeding_module"...)`
  atrás de `ModList.get().isLoaded("productivebees")`. A classe referencia tipos do PB direto e só é
  carregada nesse branch, então sem o mod a classe nunca carrega (sem `NoClassDefFoundError`); a receita
  de craft e o item somem junto (`enabled()` já trata definição nula).
- **Validação:** `spotlessCheck` + `compileJava` + `runUnitTests` (**17/17**); `runGameTestServer`
  (**35/35**, log mostra `id: gtceu:steam_elevator_bee_breeding_module` registrado e PB carregado);
  `runData` determinístico (`written: 0`) e `grep -c "Parsing error loading recipe gtna:"` = 0.
- **Pendências:** QA manual do Bee Breeding no client (ver a seção nova do
  `docs/roadmap/QA-MANUAL-CHECKLIST.md`) — a lógica roda no gate, mas a UI/inventário não é exercitada
  por ele.

### G-0050 (2026-09-22) — QA plano B: gametests de lógica dos módulos do elevador

- Helpers puros extraídos dos módulos (sem montar a frágil estrutura 1×5×2):
  `SteamWeatherModule.modeForCircuit`, `SteamOreProcessorModule.{cycleTicksFor,maxParallelFor,upkeepFor}`,
  `SteamOilDrillModule.upkeepForTier`, `SteamEntityCrusherModule.{spawnerEntityType,doublingChanceFor}` e
  `SteamBeaconModule.toggleEffect`.
- Novo `GTNAModuleLogicGameTests` (6 gametests): circuito do weather + duração de 1 h; tabela de
  modos/parallel/upkeep do ore processor (incl. clamp fora da faixa); upkeep `VP` do oil drill; parsing do
  NBT do spawner + chance de dobrar com cap 34%; cap do seletor de efeitos do beacon; constantes
  documentadas (greenhouse 16.000, flight `RANGE` 64).
- **Validação:** `spotlessCheck` + `runUnitTests` (**17/17**); `runGameTestServer` (**35/35**);
  `runData` determinístico.
- **Pendências:** Bee Breeding aguardando Productive Bees 1.20.1 — **resolvido no G-0051**; demais itens de
  QA manual no `docs/roadmap/QA-MANUAL-CHECKLIST.md`.

### G-0049 (2026-09-22) — logo do addon no canto das UIs dos multiblocos (convenção GTNL)

- `GTNATextures.LOGO` (`gtna:textures/logo.png`, a logo nova do autor) + helper `logo(x, y)` de 18×18,
  desenhado no **canto inferior direito** da tela das máquinas em (151, 107):
  `SteamMultiMachineBase`, `SteamElevatorModuleMachine` e `LargeSteamSolarBoilerMachine`. No
  `SteamElevator` a logo fica em (151, 62) porque o botão "Set out" ocupa o canto inferior direito.
  Mesma convenção do GTNL (`PICTURE_GTNL_STEAM_LOGO`, 18×18 em (151,62)).
- **Validação:** `spotlessCheck` + `runUnitTests` (**17/17**); `runGameTestServer` (**29/29**);
  `runData` determinístico.
- **Pendências:** QA plano B (gametests de módulo); conferir a logo in-game (visual, cliente).

### G-0048 (2026-09-22) — Entity Crusher por loot table (sem EnderIO/MobInfo) + checklist de QA manual

- **Entity Crusher (desenho aprovado):** o módulo agora usa um **spawner vanilla com NBT** como
  **catalisador** (não consumido) e rola a **loot table do mob** (`EntityType.getDefaultLootTable`) para
  produzir os drops, sem depender de EnderIO/MobInfo. A mecânica do tooltip fica fiel: 2% de dobrar +
  0,5% por spawner idêntico (cap 34%), tempo dobrado (`CYCLE_TICKS = 400`) e potência pela metade
  (`STEAM_UPKEEP = 512`), sem overclock. UI mostra a chance atual; saída all-or-nothing (não voida).
- **QA plano C:** `docs/roadmap/QA-MANUAL-CHECKLIST.md` — checklist versionado do que não dá para
  automatizar (HUD/drag/alinhamento, Jade, tooltips renderizadas, UI das máquinas, range do voo).
  Cada item é objetivo; regressão vira checkpoint + (quando possível) teste automatizado.
- **Bee Breeding (histórico):** na época o Productive Bees disponível no ambiente era **1.21.1 NeoForge**,
  incompatível com o GTNA (1.20.1 Forge). O build **1.20.1 Forge** existe (`1.20.1-12.6.0`) e a
  integração foi feita no **G-0051** (soft-dependency + registro condicional).
- **Validação:** `spotlessCheck` + `runUnitTests` (**17/17**); `runGameTestServer` (**29/29**);
  `runData` determinístico.
- **Pendências:** QA plano B (gametests de módulo: formar o 1×5×2 e exercitar beacon/weather/ore/etc.);
  Bee Breeding — **resolvido no G-0051**.

### G-0047 (2026-09-22) — Weather por circuito + QA plano A (lint de dependência duplicada e de tooltips dos módulos)

- **Weather (novo desenho acordado):** o circuito seleciona 1 = clear, 2 = rain, 3 = thunder (0 = off);
  aplicar custa um **pagamento único grande** de steam (`WEATHER_STEAM_COST = 1.000.000 mB`) e o clima fica
  por **1 hora** (`WEATHER_TIME = 72000` ticks). Enquanto o circuito pede o mesmo clima não cobra de novo
  até expirar; a UI mostra o **tempo restante**. Substitui o botão de ciclo e o upkeep flat de 512.
- **QA plano A:**
  - `BuildDependencyContractTest`: agrupa as declarações de `build.gradle` por slug normalizado e falha se o
    **mesmo mod** aparecer em **coordenadas distintas** (teria pego o `configuration` 2.2.0/3.1.0 antes do
    crash do client). O padrão intencional `modCompileOnly + modRuntimeOnly` com a **mesma** coordenada
    (Jade, Modern UI) continua permitido.
  - `ModuleTooltipContractTest`: toda chamada `moduleLines(id, from, to)` e todo `registerElevatorModule`
    precisam ter as chaves `gtna.machine.<id>.tooltip[.N]` no `en_us.json` gerado — pega tooltip faltando ou
    fora de ordem, que em jogo renderizaria a chave crua.
- **Validação:** `spotlessCheck` + `runUnitTests` (**17/17** agora); `runGameTestServer` (**29/29**);
  `runData` determinístico.
- **Pendências:** Entity Crusher (loot table) e Bee Breeding (Productive Bees 1.20.1 inexistente) — próximos;
  QA plano B (gametests de módulo) e C (checklist manual).

### G-0046 (2026-09-22) — fidelidade GTNL dos módulos (parte 3): seletor de efeitos do Beacon na GUI e Ore Processor com os números do GTNL

- **Beacon — seletor na GUI:** máscara de efeitos `@Persisted @DescSynced` (default = os primeiros
  `tier + 2`), um botão por efeito (verde = ligado), limite de `tier + 2` e upkeep por efeitos ativos
  (`active * V[3] * max(1, level*2)`), fechando o item "config GUI" que faltava. São 10 efeitos
  disponíveis (os do GTNL que existem em 1.20.1) e o jogador escolhe até `tier + 2`.
- **Ore Processor — números do GTNL:** modo pelo circuito 0–6 com os tempos do GTNL
  (`getRecipeTickTime` = 600/300/200/400/340/640/20), parallel `8 * 2^mode` (16 no circuito 1, como o
  tooltip "up to 16 ores at a time"), upkeep `128 * 2^circuit` e, por ore, 10 mB **distilled water** +
  1 mB **lubricant**. A profundidade da cadeia (um único estágio de maceração) segue como simplificação
  documentada: o processamento de receitas do GTCEu é centrado no controller.
- **Weather / Bee Breeding:** os parâmetros (1 hora; ciclo 12000/upkeep 32768) já batem; os itens do GTNL
  (Natura/Thaumcraft; queen do Forestry) não existem no pack 1.20.1, então o equivalente com itens vanilla
  é o teto possível.
- **Entity Crusher:** bloqueado (recipe map de drops de mob / MobInfo / EnderIO powered spawner).
- **Validação:** `spotlessCheck` + `runUnitTests` (**15/15**); `runGameTestServer` (**29/29**); `runData`
  determinístico (`written: 0`).

### G-0045 (2026-09-22) — fidelidade GTNL dos módulos (parte 1): Monster Repellent nega spawn, Oil Drill upkeep VP e Beacon com os 10 efeitos 1.20.1

- **Monster Repellent (fiel):** o GTNL registra um repelente de spawn com raio `1 << (5 + tier)`; o GTNA
  removia entidades já presentes. Agora o módulo publica seu campo
  (`SteamMonsterRepellentModule.blocksSpawn`, TTL 40t) e `SteamRepellentHandler` cancela
  `MobSpawnEvent.FinalizeSpawn` para `Monster` dentro do raio — igual ao tooltip ("Only prevents spawns
  while the machine is running").
- **Oil Drill (fiel):** upkeep passa de `V[tier]` (128/512/2048) para `V*30/32` = **VP** do GTNL
  (120/480/1920). O yield já era por extração e casa com o tooltip.
- **Beacon (parcial):** a lista fixa de 6 efeitos virou os **10 efeitos do GTNL que existem em 1.20.1**
  (Warp Ward/Vis Regen são Thaumcraft; Feather Feet → Slow Falling), com `tier + 2` ativos e o upkeep
  `activeEffects * V[3] * max(1, level*2)` do GTNL. O seletor de efeitos na GUI ainda não foi portado.
- **Entity Crusher (bloqueado):** o comportamento GTNL é dirigido por um recipe map de drops de mob
  (MobInfo/kubatech + EnderIO powered spawner) que não existe no pack 1.20.1 — não dá para portar
  fielmente sem inventar um sistema de receitas de mob. Fica com o "mata monstros no raio" atual.
- **Apiary (parcial):** o ciclo passou de 200 para **6000 ticks** (os "Fixed operating time: 300 seconds"
  do tooltip, `mMaxProgresstime` do GTNL). O consumo/produção continuam sendo o análogo vanilla
  (honeycomb/água) porque Forestry/Binnie não está no pack; os "8 slots por OC" e o royal jelly não
  existem em 1.20.1.
- **Validação:** `spotlessCheck` + `runUnitTests` (**15/15**); `runGameTestServer` (**29/29**); `runData`
  determinístico (`written: 0`).
- **Pendências:** Beacon (config GUI), Ore Processor (cadeia de receitas/parallel/modos), Weather/Bee
  Breeding (itens do GTNL), Entity Crusher (bloqueado).

### G-0044 (2026-09-22) — crash de config (dep. duplicada), botão do HUD no hatch, módulos com tick próprio, range do voo aplicado e tooltips fiéis ao GTNL

- **Crash do drag do HUD:** o projeto declarava **duas** versões do `configuration`
  (`dev.toma.configuration:...:2.2.0` no classpath de compilação e `curse.maven:configuration-444699:5840405`
  = **3.1.0** no runtime). O `HudConfigValues` compilava contra `ConfigValue.set(...)` (2.2.0), que não existe
  na 3.1.0 → `NoSuchMethodError` ao soltar o drag. Removi a dependência duplicada (só a 3.1.0) e migrei para a
  API da 3.1.0 (`setValue` + `ConfigIO.saveClientValues`). Também removi o registro próprio da tela de config
  do `ClientProxy` (a 3.1.0 registra sozinha; `Configuration.getConfigScreen` nem existe mais — crash latente).
- **Toggle do HUD no hatch (paridade GTOCore WirelessEnergySubstation):** a UI dos wireless steam hatch
  (input/output) ganhou um botão: **clique esquerdo** liga/desliga o HUD; **clique direito** abre o editor de
  drag (posição persiste). O hatch (código comum) chama um bridge client-only (`WirelessSteamHudBridge`), sem
  referenciar classe de client no server. A keybind foi removida.
- **HUD alinhada:** o gráfico começava em `textY + LINE_HEIGHT - 2` e vazava ~2 px abaixo da borda; agora
  começa após as linhas, com margem simétrica.
- **Módulos do elevador (tick próprio + observabilidade):** cada módulo aplica o efeito no próprio server tick
  enquanto formado+conectado (não depende mais do tick do host), `isActive()` = formed && connected (UI/Jade
  mostram "Working") e linha explícita Working/Not Working no display.
- **Voo — range de verdade:** o módulo concedia `mayfly` e nunca revogava; o jogador mantinha voo fora do
  alcance. Agora só os jogadores que o módulo concedeu são rastreados e o `mayfly` é revogado ao sair dos 64
  blocos (equivalente à poção do GTNL expirando) e quando o módulo para.
- **Voo cancelado pela armadura (G-0043):** o `QuantumCosmicNexusArmorHandler` limpava `mayfly` de quem não usa
  o set a cada tick; o cleanup legado agora só age quando o vestígio quântico (`flyingSpeed == 0.2`) está
  presente. `checkFlightModuleSurvivesArmorCleanup` trava.
- **Tooltips fiéis ao GTNL (todos os 10 módulos):** as descrições agora são as linhas exatas do GTNL
  (`gtnl.machine.<module>.tooltip.0..N`, incluindo as flavors por tier de Beacon/Repellator/Oil Drill e as
  linhas dinâmicas de range/yield/cycle já resolvidas). Novas chaves `gtna.machine.<id>.tooltip.N` no
  `GTNALangProvider`; `registerElevatorModule` passa `moduleLines(id, from, to)`. Nomes dos blocos alinhados
  (Repellator, Steam-Powered Apiary, Steam Greenhouse Planting, Steam Ore Processing, Steam Elevator Beacon,
  Steam Oil Drill).
- **Greenhouse:** o tooltip do GTNL diz 16.000L de água por crop; o módulo drenava 1.000 — agora usa
  `WATER_PER_OPERATION` (16.000).
- **Arquivos:** `build.gradle`, `ClientProxy.java`, `HudConfigValues.java`, `WirelessSteamHudBridge.java`
  (novo), `WirelessSteamHudOverlay.java`, `ClientEventHandler.java`, os dois hatches, `SteamElevatorModuleMachine.java`,
  `SteamFlightModule.java`, `SteamGreenhouseModule.java`, `GTNAMachines2.java`, `GTNALangProvider.java` +
  `en_us.json` manual/gerado, `SteamWiringContractTest.java`.
- **Validação:** `spotlessCheck` + `runUnitTests` (**15/15**); `runGameTestServer` (**29/29**);
  `grep -c "Parsing error loading recipe gtna:"` = 0; `runData` determinístico.
- **Pendências (fidelidade de comportamento GTNL ainda não portada):** weather (consome items + 36000t),
  apiary (ciclo 6000t, slots por OC, royal jelly), bee breeding (rainha/ignoble princess), ore processor
  (10L distilled water + 1L lubricant/ore, parallel 8×2^circuito, 7 modos, 20–640t), beacon (12 efeitos
  configuráveis na GUI, níveis por OC), entity crusher (chance de dobrar output por spawner, sem range/upkeep),
  monster repellent (negar spawn em vez de remover entidades), oil drill (upkeep VP 120/480/1920 e yield por
  extração). Os tooltips já estão fiéis; falta o comportamento.

### G-0043 (2026-09-22) — review in-game do autor: HUD arrastável (HUD editor + keybind), elevador aceita 1 steam hatch por módulo, labels drain/feed, buffers input/output separados e solar boiler 20×

- **Pedido (review do autor):** (1) portar o sistema de drag/HUDScreen do GTOcore; (2) o elevador contava os
  steam hatches de todos os módulos como seus e só aceitava 1 ("Maximum: 1" no chat), deixando os outros
  módulos sem hatch; (3) o HUD rotulava o input hatch como IN e o output como OUT (perspectiva da máquina,
  confusa); (4) o input hatch de bronze deveria ter só 100 B e o de steel MAX_INT, e o solar boiler foi
  efetivamente nerfado (312 B/s é pouco para o custo massivo da estrutura).
- **Correções:**
  - **Elevador:** o pattern do host (`createSteamElevatorPattern`) deixou de limitar globalmente as abilities
    que os módulos carregam (`STEAM`, `STEAM_IMPORT/EXPORT_ITEMS`, `IMPORT/EXPORT_ITEMS`, `IMPORT/EXPORT_FLUIDS`);
    os hatches dos módulos caem nas células do casco do host e o `setMaxGlobalLimited(1)` contava cada um como
    do host. O pattern do módulo continua limitando 1 hatch por módulo. (O host não processa receitas, então
    não precisa de limite.)
  - **HUD labels:** `Hatches: N drain / M feed` (drain = input hatches, tiram da rede; feed = output hatches,
    alimentam a rede), em vez de in/out.
  - **Buffers:** `wirelessSteam.bronzeInputBuffer = 100.000` (100 B), `steelInputBuffer = MAX`, com buffers de
    saída separados (`bronzeOutputBuffer = 128.000.000`, `steelOutputBuffer = MAX`). O input pequeno impede um
    hatch de açambarcar o pool; a tooltip dos 4 hatches mostra o buffer correto por papel.
  - **Solar boiler:** `machines.solarBoilerSteamPerCell = 4.000` (20× o 200 hardcoded) → um 41×42 faz
    ~6.240.000 mB/s.
  - **HUD drag/HUD editor (GTOcore `HUDScreen`/`IMoveableHUD` parity, single-HUD):** `IMoveableHud`,
    `HudEditorScreen` (arrasta, salva X/Y no config via `HudConfigValues`/`ConfigIO.saveClientValues`, botão
    liga/desliga) e keybind `Z` (`GTNAKeyMappings`, `key.gtna.open_hud_editor`). O HUD de vapor implementa a
    interface; a posição continua em percentuais na config.
- **Testes:** gametests ajustados para o buffer de input de 100k (o round trip de 312.000 agora é drenado em
  rodadas e o cap é verificado; ciclos pequenos de 96.000 continuam exatos). `SteamWiringContractTest` ganhou
  `checkSteamElevatorHostDoesNotLimitModuleHatches`, checagens dos buffers input/output separados, do
  `solarBoilerSteamPerCell` e do wiring do editor/keybind.
- **Arquivos:** `GTNAMachines.java` (pattern do host + tooltip por papel), `ConfigHolder.java` (buffers +
  `solarBoilerSteamPerCell`), `WirelessSteamInputHatch.java`/`WirelessSteamOutputHatch.java` (buffers),
  `LargeSteamSolarBoilerMachine.java` (config), `IMoveableHud.java`/`HudEditorScreen.java`/`HudConfigValues.java`
  (novos), `WirelessSteamHudOverlay.java` (IMoveableHud), `GTNAKeyMappings.java` (novo),
  `ClientEventHandler.java` (tick do keybind), `GTNALangProvider.java` + `en_us.json` manual + `pt_br.json`
  (byte-preserving) + gerado, `GTNAMachineGameTests.java`, `SteamWiringContractTest.java`.
- **Validação:** `spotlessCheck` + `runUnitTests` (**15/15**); `runGameTestServer` (**29/29**,
  `All 29 required tests passed`); `grep -c "Parsing error loading recipe gtna:" run/logs/latest.log` = **0**;
  `runData` determinístico (`written: 0`).
- **Pendências / verificação in-game:** (a) reentrar no mundo com o elevador e confirmar que ele forma com um
  steam hatch em cada módulo; (b) abrir o editor com `Z`, arrastar o HUD e conferir que X/Y persistem no
  `config/gtna.yaml`; (c) conferir o boiler 41×42 a ~6,24M mB/s e os inputs com buffer de 100 B; (d) o HUD
  editor é single-HUD (sem o dropdown de múltiplos HUDs do GTOcore) e a posição é salva por percentual;
  (e) o host do elevador ainda coleta os tanques dos módulos no seu pool (o módulo drena o próprio primeiro,
  depois o pool do host) — refinável para excluí-los numa próxima sessão.

### G-0042 (2026-09-22) — HUD da rede wireless de vapor (paridade GTOCore `WirelessEnergyHUD`): overlay client-side com toggle na config, posição/histórico configuráveis e sync servidor→cliente

- **Pedido:** o GTOCore tem um HUD para a rede wireless de energia (`client/hud/WirelessEnergyHUD`,
  ligado por `wirelessEnergyHUDEnabled` na config do client, com posição default X/Y e segundos de
  histórico). Fazer o equivalente para a rede de vapor do GTNA, ligado/desligado por config.
- **Implementação (GTNA-native, reimplementada a partir do comportamento, sem copiar código):**
  - **Config** `ConfigHolder.Client`: `wirelessSteamHud` (default **false**, igual ao GTOcore),
    `wirelessSteamHudX` (5), `wirelessSteamHudY` (75) e `wirelessSteamHudHistorySeconds` (60; 0
    esconde o gráfico), todos com `@Range`.
  - **Servidor:** `WirelessSteamHudSync` (subscriber FORGE de `TickEvent.ServerTickEvent`) manda
    `SWirelessSteamStats` 1×/s para cada jogador online: saldo, fluxo do último segundo (os
    contadores vitalícios do `SteamNetworkData` viram deltas) e contagem de hatches in/out. As
    amostras são descartadas no logout para não reportar produção offline como se fosse do jogador.
  - **Cliente:** `WirelessSteamHudState` (espelho + ring buffer de 600 s) e
    `WirelessSteamHudOverlay` (`IGuiOverlay` registrado via `RegisterGuiOverlaysEvent`, bus MOD),
    desenhando saldo, fluxo (+/−), hatches e um sparkline do saldo. Só aparece quando há rede
    (hatch, saldo ou fluxo) e nunca com F1/F3. Reset no `ClientPlayerNetworkEvent.LoggingOut` (via
    `ClientEventHandler`).
- **Testes:** gametest `wirelessSteamHudSnapshotReportsNetworkState` (baseline 0; push →
  saldo/added; pull → consumed e saldo 0; contagens 1 in/1 out). `SteamWiringContractTest` ganhou
  `checkWirelessSteamHudWiring` (toggle default false, packet registrado, overlay registrado,
  sampler presente) — o gate não roda client, então o scan é a única trava automática do wiring.
- **Arquivos:** `ConfigHolder.java`, `WirelessSteamHudSync.java` (novo), `SWirelessSteamStats.java`
  (novo), `WirelessSteamHudState.java` (novo), `WirelessSteamHudOverlay.java` (novo),
  `ClientEventHandler.java`, `GTNANetworkHandler.java`, `GTNALangProvider.java` + `pt_br.json`
  (byte-preserving: BOM/CRLF intactos) + `en_us.json` gerado, `GTNAMachineGameTests.java`,
  `SteamWiringContractTest.java`.
- **Validação:** `spotlessCheck` + `runUnitTests` (**15/15**); `runGameTestServer` (**29/29**,
  `All 29 required tests passed`); `grep -c "Parsing error loading recipe gtna:" run/logs/latest.log`
  = **0**; `runData` determinístico (2ª execução `written: 0`).
- **Pendências / verificação in-game:** (a) ligar `wirelessSteamHud` na config do client e conferir
  saldo/fluxo/hatches + gráfico com a rede real (25 hatches); (b) o HUD **não é arrastável** (a
  posição é por config, diferente do GTOcore, que tem drag) e só desenha in-game, não sobre telas de
  container; (c) o gráfico usa o histórico local (1 amostra/s, até 600 s) — sem sync de histórico.

### G-0041 (2026-09-22) — rede wireless de vapor travada em 0 mB com 25 hatches: o primeiro input drenava o pool inteiro por tick; pull com fair share + diagnóstico por hatch

- **Reprodução do autor (25 hatches):** 1 output hatch no boiler solar (312.000 mB por ciclo) + 24
  input hatches nas máquinas grandes; `/gtna steam` lia **0 mB** em 3 leituras ao longo de ~4 min.
  O save `New World` foi inspecionado: **21 inputs parados em exatamente 20.000 mB** (o antigo buffer
  de bronze), **um** input (o primeiro na ordem de tick) com **47.756.000 mB**, output hatch vazio e
  `gtna_steam_network.dat` com o UUID do Dev e saldo 0.
- **Causa raiz medida:** o push e a chave da rede estavam certos (o vapor produzido chegava ao pool:
  ~48M mB acumulados em um hatch). O bug era o **pull all-or-nothing**: cada input pedia
  `min(space, rate, saldo)` e com rate default `Integer.MAX_VALUE` o **primeiro hatch do tick levava
  o saldo inteiro**; os 23 seguintes liam 0. Nada era voidado nem duplicado — o vapor ficava **preso
  no primeiro hatch** (idle), a rede lia 0 e todas as outras máquinas morriam de fome.
- **Correção (fair share; desvio documentado do GTNL):** o input agora divide o saldo pelo número de
  inputs **vivos e com espaço** (`getActiveInputCount`, registry com TTL 40t), com `ceil`:
  `request = min(space, rate, ceil(saldo/inputs), saldo)`. Um hatch sozinho continua levando o saldo
  inteiro (GTNL parity); com N hatches todos são servidos por tick, mantendo a ordem
  SIMULATE→cobrar→EXECUTE (nunca voida/duplica). Os hatches se **pré-registram no `onLoad`** (com
  nível do tanque) para o denominador ver o banco inteiro já no primeiro tick após restart.
- **"Working Disabled" do Jade:** o `setWorkingEnabled(false)` do port desligava o **AUTO IO** do
  GTCEu (o campo `workingEnabled` de `TieredIOPartMachine` é o toggle de auto IO, não o estado da
  rede) e o Jade lia isso como "Working Disabled" num hatch que funcionava. Agora `isWorkingEnabled()`
  espelha o master switch e o AUTO IO continua desligado via `updateTankSubscription()` no-op
  (wireless-only, sem import/export por pipes).
- **Diagnóstico `/gtna steam`:** saldo + **fluxo vitalício** (`+adicionado / -consumido`) + nº de
  inputs com espaço + uma linha por hatch com **tanque atual/capacidade**, taxa e **última operação**
  ("pushed/pulled N mB (T t ago)" / "no transfer yet"). Provider Jade novo
  `wireless_steam_network` (paridade WAILA do GTNL) mostra saldo da rede, tanque do hatch e última
  transferência; chave `config.jade.plugin_gtna.wireless_steam_network` no `GTNALangProvider`.
- **Testes:** 3 gametests novos — `wirelessSteamDistributesAcrossManyInputs` (1 output + 5 inputs:
  nenhum hatch leva mais que `ceil(312000/5)` por passada, todos puxam, 12 passadas zeram a rede com
  conservação total e o registry do comando lista os 6), `wirelessSteamFullInputDoesNotDiluteOrVoid`
  (input cheio não puxa, não dilui a share do vazio e não voida) e
  `wirelessSteamFeedsOnNaturalServerTick` (tick natural do servidor, sem `serverTick()` manual:
  output enche → input recebe em ≤10 ticks). `SteamWiringContractTest` trava o fair share e a remoção
  do `setWorkingEnabled(false)`.
- **Arquivos:** `SteamNetworkData.java` (`ConnectionInfo` com tanque/última transferência +
  `FlowStats` + `countActiveInputsWithSpace`), `SteamWirelessNetworkManager.java`
  (`reportConnection`→`ConnectionInfo`, `getActiveInputCount`, `getFlowStats`),
  `WirelessSteamInputHatch.java` (fair share, pré-registro, diagnostics, AUTO IO no-op),
  `WirelessSteamOutputHatch.java` (pré-registro, diagnostics, AUTO IO no-op), `GTNACommands.java`
  (fluxo + linha por hatch), `GTNAWirelessSteamProvider.java` (novo) + `GTNAJadePlugin.java`,
  `GTNALangProvider.java` + `pt_br.json` (byte-preserving: BOM/CRLF intactos) + `en_us.json` gerado,
  `GTNAMachineGameTests.java`, `SteamWiringContractTest.java`.
- **Validação:** `spotlessCheck` + `runUnitTests` (**15/15**); `runGameTestServer` (**28/28**,
  `All 28 required tests passed`); `grep -c "Parsing error loading recipe gtna:" run/logs/latest.log`
  = **0**; `runData` determinístico (2ª execução `written: 0`).
- **Pendências abertas:** (a) verificação in-game do autor com os 25 hatches reais — a rede deve
  parar de ler 0 assim que os buffers dos inputs encherem e os 21 inputs presos em 20.000 devem
  subir; (b) a rede continua keyed por **owner** (não por time; FTB Teams é só `modRuntimeOnly` —
  documentado desde o G-0038); (c) o hatch de saída continua movendo o tanque inteiro por tick sem
  cap default (paridade GTNL); (d) no **primeiro tick** após carregar um banco de hatches recém
  colocados ainda pode haver um hatch pegando o saldo inteiro (o `onLoad` pré-registra os carregados
  de chunk, mas um hatch colocado com a rede já cheia só se registra no 1º tick) — blip de 1 tick,
  documentado nos testes.

### G-0040 (2026-09-22) — boiler solar grande não alimentava a rede wireless: buffer/taxa do hatch de saída estrangulavam o ciclo; display/Jade reportavam 20× a produção

- **Reprodução do autor:** `large_steam_solar_boiler` 41×42, **Wireless Steam Output Hatch** no
  boiler não enchia a rede; **Wireless Steam Input Hatch** no chão não enchia; Jade mostrava
  "312 B".
- **Causa raiz #1 (buffer/taxa):** o boiler despeja o **ciclo inteiro de uma vez** no hatch de
  saída (`sunlit * 200` mB a cada 20 ticks = 312.000 mB no campo 41×42). O hatch de bronze tinha
  buffer de **20.000 mB** e cap de **10.000 mB/t**: o `RecipeLogic.onRecipeFinish` (que ignora o
  resultado do `handleRecipeIO(OUT)`) aceitava só 20.000 e **descartava 292.000**, e o hatch ainda
  limitava o push a 10.000/t. Resultado: a rede recebia ~1.000 mB/t de 15.600 mB/t reais (0,3%) —
  o autor leu isso como "não entra vapor".
- **Correção (paridade GTNL `WirelessSteamDynamoHatch`):** o hatch move o **tank inteiro** por tick.
  `bronzeBuffer` 20.000 → **128.000.000** (capacidade do dynamo de bronze do GTNL); `bronzeTransferRate`
  e `steelTransferRate` passam a default **`Integer.MAX_VALUE`** (throttle opcional, documentado no
  config/tooltip/`/gtna steam`). O input hatch enche até o espaço livre/rede. Nada de cap fixo
  minúsculo: o limite real passa a ser o buffer.
- **Causa raiz #2 (display/Jade 20×):** `lastSteamOutput = steamOut * 20` era **20× maior** que a
  produção real (o `steamOut` já é o valor do ciclo de 20 ticks = 1 s). Renomeado para
  `steamPerSecond = steamOut * 20 / TICK_INTERVAL` e o label de `L/s` para `mB/s`. O "312 B" do Jade
  é o **output da receita por craft** (provider stock `RecipeOutputProvider`): está correto para o
  ciclo, mas parecia baixo contra o display errado. Novo `GTNASolarBoilerProvider` (Jade) mostra
  "Sunlit Cells" + "Steam Production" em mB/s e uma linha de idle, alinhado ao display da máquina.
- **Observabilidade:** tooltip dinâmica dos 4 hatches (buffer via config + taxa "unlimited" ou
  valor), label de taxa na GUI do hatch, e `/gtna steam` agora imprime `| <taxa> mB/t` por hatch
  (`unlimited` quando MAX).
- **Arquivos:** `ConfigHolder.java` (defaults + comentários), `WirelessSteamOutputHatch.java`,
  `WirelessSteamInputHatch.java` (`getTransferRate`/`isTransferLimited`/`rateText`, push/pull do
  buffer inteiro), `GTNAMachines.java` (`wirelessSteamTooltip` dinâmica), `GTNACommands.java`
  (taxa por hatch), `LargeSteamSolarBoilerMachine.java` (per-second + getters),
  `GTNASolarBoilerProvider.java` (novo) + `GTNAJadePlugin.java`, `GTNALangProvider.java` +
  `pt_br.json` + `en_us.json` manual/gerado (chaves `gtna.machine.wireless_steam.transfer_rate(.unlimited)`,
  `gtna.machine.large_steam_solar_boiler.idle`, `gtna.command.steam.rate.unlimited`, formato de
  `hatch_entry`), `GTNAMachineGameTests.java` (round trip com 312.000 mB + 4 ciclos sob carga),
  `SteamWiringContractTest.java` (trava buffer/taxa e a taxa por segundo do boiler).
- **Validação:** `spotlessApply compileJava` OK; `spotlessCheck` + `runUnitTests` (**14/14**);
  `runGameTestServer` (**25/25**, `All 25 required tests passed`); `grep -c "Parsing error loading
  recipe gtna:" run/logs/latest.log` = **0**; `runData` determinístico (2ª execução `written: 0`).
- **Pendências abertas / verificação in-game:** (a) confirmar no cliente que o Jade do boiler
  mostra "Steam Production: 312000 mB/s" (dia, campo 41×42) e que o "312 B" do provider stock
  continua logo acima como output por craft; (b) `/gtna steam` deve listar o output hatch e o input
  hatch com `unlimited mB/t`; (c) opcional: dividir os buffers input (GTNL 8M) e output (128M) em
  duas chaves se o hoarding de 128M no input incomodar; (d) `machines.wirelessSteamTransferRate`
  (8192) é config legado morto — remover numa limpeza futura.

### G-0039 (2026-09-22) — tooltip duplicado dos módulos do elevador: descrição saía duas vezes (mainKey do GTCEu + `.tooltips`); descrições fiéis ao GTNL

- **Causa raiz (confirmada no fonte do GTCEu 7.5.3):** `MetaMachineBlock#appendHoverText` chama
  `definition.getTooltipBuilder().accept(...)` (que adiciona o que foi passado em `.tooltips(...)`)
  e depois insere `tooltip.add(1, Component.translatable("<namespace>.machine.<id>.tooltip"))`
  **se a chave existir**. O `registerElevatorModule` passava exatamente a mesma chave
  `gtna.machine.<id>.tooltip` em `.tooltips(...)`, então a linha saía duas vezes. O mesmo caminho
  existe em `MetaMachine#onAddFancyInformationTooltip` (índice 0).
- **Correção:** o `registerElevatorModule` deixou de listar a descrição; a linha única passa a vir
  só da inserção automática do GTCEu (índice 1, logo abaixo do nome). O `.tooltips(...)` agora
  recebe apenas as linhas de **status/upkeep** de cada módulo, terminando em
  `gtceu.part_sharing.disabled` (que continua, é intencional).
- **Tooltips fiéis ao GTNL:** descrição de "o que faz" em `gtna.machine.<id>.tooltip` (auto) +
  linhas de status via chaves compartilhadas `gtna.machine.steam_elevator_module.tooltip.{range,
  upkeep,cycle,water,yield}`. Flight: range 64/upkeep 8192; Weather: upkeep 512; Greenhouse:
  range 16/água 1000/upkeep 8192; Oil Drill I/II/III: yield 250-1000 / 1000-4000 / 3000-12000 L
  por ciclo, ciclo 1200/600/400 ticks, upkeep 128/512/2048; Entity Crusher: range 8/upkeep 512;
  Ore Processor: água 1000/ciclo 20/upkeep 128; Repellent I/II/III: range 64/128/256, upkeep
  512/1024/1536; Beacon I/II/III: range 64/128/256, upkeep 2048/8192/18432; Apiary: água
  1000/ciclo 200/upkeep 16384; Bee Breeding: ciclo 12000/upkeep 32768.
- **Arquivos:** `GTNAMachines2.java` (helpers `range/upkeep/cycle/water/yieldStat` + varargs de
  tooltips no `registerElevatorModule`), `GTNALangProvider.java` (5 chaves novas + descrições),
  `pt_br.json` (5 chaves novas + descrições; BOM + CRLF preservados byte a byte),
  `en_us.json` gerado.
- **Validação:** `spotlessApply compileJava` OK; `spotlessCheck` + `runUnitTests` (**14/14**);
  `runGameTestServer` (**25/25**, `All 25 required tests passed`); `grep -c "Parsing error loading
  recipe gtna:" run/logs/latest.log` = **0**; `runData` determinístico (2ª execução `written: 0`).
- **Pendências abertas / verificação in-game:** conferir no cliente que cada módulo mostra **uma**
  linha de descrição seguida das linhas de status e de "Multiblock Sharing Disabled"; os valores de
  yield do Oil Drill são os do cálculo GTNA (`baseYield * (1..4)` somado `tier-1` vezes), não os
  números do GTNL (o GTNL usa overclock count variável, o GTNA usa tier fixo).

### G-0038 (2026-09-22) — wireless steam input hatch "no steam": pull is now clamped to the network balance + `/gtna steam` inspection command

- **Causa raiz medida (reproduzida em gametest):** `WirelessSteamInputHatch.updateWireless` pedia
  `toPull = min(spaceNeeded, transferRate)` (bronze **10000**, steel **1000000** mB) e só então
  chamava `consumeSteamFromGlobalMap`, que é **tudo-ou-nada**. Se a rede tivesse **menos** que um
  tick da taxa (o caso comum: o hatch de saída empurra aos poucos), o consume era rejeitado
  inteiro e o hatch **nunca puxava nada** — daí o "no steam" tanto no host quanto no módulo. O
  bug do G-0037 (gate de 8192 removido) era real, mas não era o que travava o puxão.
- **Correção (paridade GTNL `tryFetchingSteam`):** o pedido agora é
  `min(spaceNeeded, transferRate, getUserSteam(network))`; como o tanque simulado aceita ≤ pedido ≤
  saldo, o consume atômico sempre passa. Nada mais é voidado nem duplicado (mantém o
  SIMULATE→cobrar→EXECUTE do G-0037). Vale para hatch no **host** e no **módulo** (ambos usam o
  `getOwnerUUID()` do colocador; a rede GTNA é keyed por owner, não por time — o comando abaixo
  mostra o saldo e os hatches para conferir).
- **Inspeção:** novo `/gtna steam` (qualquer jogador) imprime, para o próprio dono, o saldo da rede
  e os wireless hatches conectados (tipo, bronze/aço, dimensão e posição). `/gtna steam add <n>` e
  `/gtna steam set <n>` (op, nível 2) para reproduzir/verificar estados; `/gtna steam <jogador>`
  (op) para a rede de outro jogador. Os hatches agora registram-se em um mapa **transiente** (TTL 40
  ticks, nunca persistido) em `SteamNetworkData` via `SteamWirelessNetworkManager.reportConnection`;
  o comando é a fonte de verdade para "o vapor chegou na rede?".
- **Testes:** `wirelessSteamHatchIsAcceptedAsSteamSource` ganhou o **round trip real** (output hatch
  com 4321 mB → `serverTick()` → rede 4321 → input hatch `serverTick()` → tanque 4321 → rede 0, sem
  perda) — foi ele que reproduziu o bug antes da correção. `SteamWiringContractTest` agora exige
  `getUserSteam` **antes** do `fill(SIMULATE)` no input hatch, travando a regressão.
- **Arquivos:** `WirelessSteamInputHatch.java`, `WirelessSteamOutputHatch.java`,
  `SteamNetworkData.java`, `SteamWirelessNetworkManager.java`, `GTNACommands.java`,
  `GTNALangProvider.java` (12 chaves), `pt_br.json` (12 chaves, BOM/CRLF preservados),
  `GTNAMachineGameTests.java`, `SteamWiringContractTest.java`, `en_us.json` gerado.
- **Validação:** `spotlessApply compileJava` OK; `spotlessCheck` + `runUnitTests` (**14/14**);
  `runGameTestServer` (**25/25**, `All 25 required tests passed`); `grep -c "Parsing error loading
  recipe gtna:" run/logs/latest.log` = **0**; `runData` determinístico (2ª execução `written: 0`).
- **Pendências abertas / verificação in-game:** (a) montar o hatch de saída numa fonte de vapor e o
  de entrada no elevador/módulo, rodar `/gtna steam` e ver o saldo subir e os hatches listados;
  (b) a rede ainda é por **owner** (colocador), não por time — co-op com jogadores diferentes exige
  um item/comando de vínculo (FTB Teams é só `modRuntimeOnly`, não dá para referenciar no main);
  (c) balancear as taxas por hatch (bronze 10000/t, aço 1000000/t) contra os upkeeps dos módulos.

### G-0037 (2026-09-22) — review in-game: rede de vapor sem perda, elevador/módulos sem EU, hatch no módulo e overlay do elevador

- **Prioridade 1 — a rede de vapor wireless estava perdendo/entupindo vapor.** A causa medida era
  um **gate de taxa inconsistente** em `SteamWirelessNetworkManager.consumeSteamFromGlobalMap`:
  rejeitava qualquer valor acima de `machines.wirelessSteamTransferRate` (**8192**), enquanto o
  hatch calculava `toPull` pela sua própria taxa (`bronze = 10000`, `steel = 1000000`). Na prática
  o hatch **de aço nunca puxava** (toPull > 8192 sempre) e o de bronze só puxava perto de encher;
  do ponto de vista do jogador o vapor ia para a rede e "sumia". Além disso, o hatch de entrada
  **cobrava a rede e só depois enchia o tanque**, sem conferir o que o tanque aceitou (mismatch
  simulado-vs-real). Correção no estilo do GTNL (`tryFetchingSteam`):
  - `SteamNetworkData.addSteam` agora é **atômico com sinal** (aceita negativo; rejeita e não mexe
    no saldo se for abaixo de zero) e retorna `boolean`; `addSteamToGlobalSteamMap` propaga isso.
  - `consumeSteamFromGlobalMap`/`extractSteam` perderam o gate de `machines.wirelessSteamTransferRate`
    (a taxa é responsabilidade do hatch, que já limita por `wirelessSteam.*TransferRate`).
  - **Entrada:** `fill(SIMULATE)` → cobra exatamente o `accepted` → `fill(EXECUTE)`.
  - **Saída:** `drain(SIMULATE)` → adiciona exatamente o `amount` drenado → `drain(EXECUTE)`.
  - Gametest `wirelessSteamHatchIsAcceptedAsSteamSource` ganhou um bloco de **contabilidade em
    runtime** (add 1000 → 1000; consume 400 → 600; overdraft rejeitado e saldo intacto; subtract
    atômico até 0; subtract abaixo de 0 rejeitado). `SteamWiringContractTest` ganhou um lint de
    código que exige a ordem SIMULATE→cobrar→EXECUTE nos dois hatches.
- **Prioridade 2 — elevador e módulos sem buffer de EU.** Removido o `energyBuffer`/`MAX_ENERGY` do
  `SteamElevator` e o `storedEnergy`/`receiveEnergy`/`consumeEnergy`/`getEnergyStored`/
  `getEnergyCapacity` do `SteamElevatorModuleMachine`/`ISteamElevatorModule`. O elevador agora é
  **sempre ativo quando formado** (`isWorkingEnabled()`→`true`, `isActive()`→`isFormed()`,
  `isElevatorRunning()`→`isFormed()`); não consome receita/energia. Os módulos pagam um
  **upkeep em mB/t** (`getSteamUpkeep`, antes `getEnergyUsage`, mesmos números do GTNL com 1 mB =
  1 EU) drenado dos **hatches de vapor da estrutura**: primeiro os do próprio módulo, depois os do
  host, sempre checando a disponibilidade dos dois pools **antes** de drenar (nunca paga parcial e
  some com a diferença). Linhas de display "Energy: X / Y EU" e "Buffer: X / Y EU" trocadas por
  "Steam: X mB" e "Upkeep: X mB/t | Own steam: Y mB".
- **Prioridade 3 — hatch de vapor no módulo reportava "não conectado".** O pattern do módulo
  (`steam_elevator_module`) mapeava `A` para **apenas** `blocks(CASING_STEEL_SOLID)`, então um hatch
  colocado no casco do módulo não casava e a estrutura invalidava. `A` agora aceita, como no GTNL
  `SteamElevatorModuleBase#getStructureDefinition`, `PartAbility.STEAM` + `STEAM_IMPORT/EXPORT_ITEMS`
  + `IMPORT/EXPORT_ITEMS` + `IMPORT/EXPORT_FLUIDS` + `MAINTENANCE` (cada um `setMaxGlobalLimited(1)`)
  encadeado com o casing. O módulo coleta o tanque do hatch em `onStructureFormed` (filtro
  `isFluidValid(0, steam)`) e usa no upkeep.
- **Prioridade 4 — overlay do Steam Elevator.** Confirmado de novo: o GTNL renderiza o elevador com
  `gregtech:iconsets/EM_COMPUTER` (`BlockIcons.OVERLAY_FRONT_TECTECH_MULTIBLOCK`), um ícone
  **GT5U/Tectech** referenciado do resource domain do GregTech e **NÃO vendorizado** no repositório
  do GTNL (só há `SteamCarpenter`, `SteamLavaMaker`, `SteamItemVault`, `CactusWonder`,
  `MegaSteamCompressor`, `SteamManufacturer`, etc. — nenhum overlay de elevador). Como não há o que
  portar do GTNL, o ícone foi vendorizado na forma retexturizada do pack **Modernity-GTNH** (fonte
  que o projeto já usa; mesmo autor do GTNL conforme o dono) em
  `assets/gtna/textures/block/multiblock/steam_elevator/{overlay_front,overlay_front_active}.png`
  (+ `.mcmeta` animado) e o `workableCasingModel` passou de `gtceu:.../steam_grinder` para
  `gtna:.../steam_elevator`. `THIRD_PARTY_NOTICES.md` atualizado. **Re-auditoria dos demais:** as
  `large_steam_*` do GTNL não têm textura própria (usam ícones do GT++/GT5U);
  `hyper_pressure_reactor`/`compact_hyper_pressure_reactor` (GTNL `SteamFusionReactor`/
  `HighPressureSteamFusionReactor`) usam `OVERLAY_TOP_STEAM_MACERATOR` do GT5U, também não
  vendorizado — mantidos os overlays GTCEu. Nenhum outro port GTNL ficou com overlay GTCEu tendo um
  do GTNL disponível.
- **Arquivos:** `SteamWirelessNetworkManager.java`, `SteamNetworkData.java`,
  `WirelessSteamInputHatch.java`, `WirelessSteamOutputHatch.java`, `ISteamElevatorModule.java`,
  `SteamElevator.java`, `SteamElevatorModuleMachine.java`, os 10 módulos, `GTNAMachines.java`
  (overlay do elevador), `GTNAMachines2.java` (pattern do módulo), `GTNALangProvider.java`,
  `pt_br.json` (2 linhas trocadas, BOM/CRLF preservados), `SteamWiringContractTest.java`,
  `GTNAMachineGameTests.java`, `THIRD_PARTY_NOTICES.md`, as texturas do overlay e o modelo gerado
  `steam_elevator.json`.
- **Validação:** `spotlessApply compileJava` OK; `spotlessCheck` + `runUnitTests` (**14/14**);
  `runGameTestServer` (**25/25**, `All 25 required tests passed`); `grep -c "Parsing error loading
  recipe gtna:" run/logs/latest.log` = **0**; `runData` determinístico (2ª execução `written: 0`).
- **Pendências abertas / verificação in-game:** (a) montar o elevador 35x43x35 e os 12 módulos no
  `runClient` e confirmar que cada módulo paga upkeep dos hatches (próprio e do host) e que o efeito
  liga/desliga ao conectar/desconectar; (b) confirmar que colocar um hatch de vapor no casco do
  módulo agora forma o módulo e que o host **não** conta esse hatch como fonte própria (as células
  do módulo caem em `I`/`H`/`D`; se cair em `H` pode competir com o limite de 1 fonte de vapor do
  host — validar); (c) conferir visualmente o overlay animado do elevador; (d) o
  `machines.wirelessSteamTransferRate` (8192) ficou **sem uso** (a taxa por hatch é a que vale) —
  remover numa limpeza futura se o autor concordar; (e) os upkeeps em mB/t são os números do GTNL
  (1 mB = 1 EU) e podem precisar de balanceamento in-game.

### G-0036 (2026-09-22) — review in-game: Building Gadgets, orientação do módulo, casing do elevador e overlays GTNL

- **Item 1 — Building Gadgets não carregava (causa raiz):** a dependência
  `modRuntimeOnly("curse.maven:building-gadgets-298187:6850515")` aponta para o arquivo **6850515 =
  buildinggadgets2-1.3.9**, que é **NeoForge 1.21** (`META-INF/neoforge.mods.toml`, `neoforge`
  `[21.0,)`, `minecraft [1.21,1.22)`), então o Forge 1.20.1 ignorava o jar silenciosamente. Trocado
  para **6850495 = buildinggadgets2-1.0.8** (1.20.1 **Forge**; `META-INF/mods.toml` com
  `loaderVersion="[43,)"`, `modId="forge"`, `minecraft [1.19.2,)`). Verificado: o jar é baixado e o
  mod **aparece carregado** no log do `runGameTestServer` (registros `buildinggadgets2:*` e
  `run/config/buildinggadgets2-common.toml`).
- **Item 2 — orientação do módulo do Steam Elevator:** o `pattern/steam_elevator_module.mbs` (1x5x2)
  tinha sido gerado **sem** a convenção do §5 do `NEXT-SESSION-HANDOFF` (inverter linhas **e**
  aisles), então o controller ficava na **2ª linha de baixo** (aisle 0) e o corpo **à frente** dele
  (a face "para dentro"). Regenerado com a convenção: aisle 0 = corpo (`A`), aisle 1 = controller
  (`~`) na **linha 4** (2ª do topo). No frame do GTCEu isso põe o controller na 2ª linha a partir do
  topo e o corpo **atrás** dele (a face aponta **para fora**); a faixa vertical (linhas 0..4 do host)
  continua casando com as células `H`/`I` do `steam_elevator.mbs`. Binário gerado pela tabela do
  `GTNAMultiBlockFileReader` (`A`=2, `~`=61). Não foi mexido no `rotationState` (NON_Y_AXIS +
  `allowExtendedFacing(false)`/`allowFlip(false)` continuam corretos).
- **Item 3 (já feito, preservado):** o `SteamElevator.onStructureFormed` não invalida mais quando não
  há tanque de vapor ("o elevador não precisa de vapor/energia; só os módulos consomem"). O
  `SteamElevator.java` já estava modificado e entrou neste commit.
- **Item 4 — casing do elevador:** `appearanceBlock` e o casing base do `workableCasingModel` do
  `steam_elevator` passaram de `GTNABlocks.STEEL_REINFORCED_WOOD` para **`GTBlocks.CASING_STEEL_SOLID`**
  (GTNL `SteamElevator#getCasingTextureID()` = `SolidSteelMachineCasing`; o **shell `A` do pattern
  continua** steel-reinforced-wood, que é o elemento `A` do GTNL).
- **Item 5 — overlays dos ports GTNL:** portadas para `assets/gtna/textures/block/multiblock/<id>/`
  as texturas de overlay **próprias do GTNL** (`textures/blocks/iconsets/*`) que faltavam, com
  `overlay_front`/`overlay_front_active` e os `_GLOW` do GTNL mapeados para `_emissive` do GTCEu:
  `steam_lava_maker` (SteamLavaMaker), `steam_item_vault` (SteamItemVault), `steam_cactus_wonder`
  (CactusWonder) e `steam_mega_compressor` (MegaSteamCompressor). Os `.workableCasingModel` dessas
  quatro máquinas agora apontam para `GTNACORE.id("block/multiblock/<id>")`. Corrigido também o
  typo `overlay_front_activce.png` → `overlay_front_active.png` (+ `.mcmeta`) do
  `largesteamfurnace` (o overlay ativo nunca carregava). **Limite encontrado:** os `large_steam_*`
  do GTNL **não** têm textura própria no repositório do GTNL — usam ícones do **GT++**
  (`TexturesGtBlock.oMCDIndustrial*`) ou do GT5U (`Textures.BlockIcons.OVERLAY_*`), que não estão
  em `GTNL/src/main/resources`; os `steam_*` com iconset próprio já estavam portados
  (`steammanufacturer`, `steamwoodcutter`/SteamCarpenter, `steaminfernalcokeoven`) ou foram agora.
  O `steam_elevator` usa `gregtech:iconsets/EM_COMPUTER` (GT5U/Tectech), também não vendorizado.
  Atribuição GTNL atualizada em `THIRD_PARTY_NOTICES.md`.
- **Arquivos:** `build.gradle`, `GTNAMachines.java`, `pattern/steam_elevator_module.mbs`,
  `steam_elevator_module` (binário), as 4 pastas novas de overlay, o rename do
  `largesteamfurnace`, `THIRD_PARTY_NOTICES.md`, `SteamElevator.java` (item 3), os 5 modelos
  gerados em `src/generated/resources/assets/gtna/models/block/machine/`.
- **Validação:** `spotlessApply compileJava` OK; `spotlessCheck` + `runUnitTests` (**14/14**);
  `runGameTestServer` (**25/25**, `All 25 required tests passed`) e
  `grep -c "Parsing error loading recipe gtna:"` = **0**; `runData` determinístico (2ª execução
  `written: 0`).
- **Pendências abertas / verificação in-game:** (a) confirmar que os 12 módulos formam com o
  controller na 2ª linha do topo e o corpo para dentro (o jogador precisa facear o módulo para fora;
  a face do módulo é perpendicular à do host nos slots laterais); (b) conferir visualmente se
  `large_steam_*` devem manter os overlays GTCEu/GTO ou se o autor quer os ícones do GT++ (precisam
  ser fornecidos); (c) o overlay do `steam_elevator` continua `gtceu:block/multiblock/steam_grinder`
  (o ícone Tectech do GTNL não está no repositório do GTNL).

### G-0035 (2026-09-22) — Steam Elevator: módulos viram multiblocos próprios (corrige a contagem de módulos)

- **Bug reportado in-game:** o Steam Elevator contava **qualquer** bloco/peça nas células de módulo
  como módulo (ex.: colocar uma steam hatch em cima de um módulo incrementava o contador). O sistema
  era composto de **part machines** (`SteamElevatorModulePartMachine`) marcadas com a ability
  `GTNAPartAbility.STEAM_ELEVATOR_MODULE`, e o host simplesmente somava `getParts()` que
  implementassem `ISteamElevatorModule` — ou seja, um bloco solto no slot bastava.
- **Correção (desenho GTNL/GTLAdditions):** cada módulo agora é um **multiblock `1x5x2` próprio**
  (estrutura `pattern/steam_elevator_module.mbs`, decodificada com o `GTNAMultiBlockFileReader`
  existente). O host escaneia um conjunto **fixo** de 12 slots e conecta **apenas** o controller de
  módulo **formado** que estiver lá; um bloco/peça solto nunca é contado.
- **Base nova `SteamElevatorModuleMachine`** (`extends WorkableMultiblockMachine`, `implements
  ISteamElevatorModule, IDisplayUIMachine`): mantém o buffer `640000 * (1 << tier)`, o
  `receiveEnergy`/`consumeEnergy` e o `onElevatorTick` dos antigos part machines; troca
  `addedToController/removedFromController` por `connectToHost/disconnectFromHost`. RecipeLogic
  inerte (o host dirige os efeitos). `createUI` monta o painel do `IDisplayUIMachine` + o widget do
  módulo (`createModuleUIWidget`, ex-`createUIWidget`).
- **Host (`SteamElevator`):** novo `getModuleScanPositions()` com 12 offsets `{up, left, forward}`
  **relativos ao controller**, decodificados das células `I` do `steam_elevator.mbs`
  (`{0,-8,-5}..{0,8,-1}`); `RelativeDirection.offsetPos(pos, front, upwards, flipped, ...)` aplica
  facing/flip. `scanModules()` (no `onStructureFormed` e a cada 20 ticks) reconstrói o set a partir
  dos slots: só `SteamElevatorModuleMachine` com `isFormed()` entra; os demais são desconectados.
  Host e módulos usam `allowExtendedFacing(false)` + `allowFlip(false)` para o `offsetPos` ser
  exato (o `I` do padrão virou `any()`; a ability `STEAM_ELEVATOR_MODULE` foi **removida** do
  `GTNAPartAbility`).
- **Encaixe do módulo no host:** em coordenadas locais do padrão, o módulo ocupa `j=I.j-1..I.j+3`
  (5 de altura) e `i=I.i..I.i+1` (2 de profundidade), com controller em `j=1, i=0` (como o GTNL);
  um script verificou que **os 12 slots** aceitam essa caixa — as células caem em `H`/`D`
  (solid steel machine casing, que é o casing do módulo) ou em ` ` (any). O jogador deve orientar o
  módulo com o **mesmo facing** do host.
- **Lang:** 4 chaves novas (`gtna.machine.steam_elevator_module.{tier,energy,connected,disconnected}`)
  no `GTNALangProvider` (en_us gerado) + `pt_br.json` (inserção **byte-preserving**: BOM e
  CRLF/LF preservados, 4 linhas adicionadas). Tooltips/recipes existentes mantidos (os registry ids
  não mudaram).
- **Arquivos:** `SteamElevatorModuleMachine.java` (novo), `SteamElevatorModulePartMachine.java`
  (removido), `ISteamElevatorModule.java`, `SteamElevator.java`, os 10 módulos, `GTNAMachines.java`
  (padrão `I`→`any()`, allowFlip/extended), `GTNAMachines2.java` (registro `multiblock`),
  `GTNAPartAbility.java`, `GTNALangProvider.java`, `pt_br.json`, `pattern/steam_elevator_module.mbs`
  (novo) e os recursos gerados dos módulos.
- **Validação:** `spotlessApply compileJava` OK; `spotlessCheck` + `runUnitTests` (**14/14**);
  `runGameTestServer` (**25/25**, `All 25 required tests passed`); `grep -c "Parsing error loading
  recipe gtna:" run/logs/latest.log` = **0**; `runData` determinístico (2ª execução `written: 0`).
- **Pendências abertas / verificação in-game:** (a) os gates **não** montam o elevador 35x43x35 —
  validar no `runClient` que os 12 módulos formam nos slots, que um bloco solto **não** conta e que
  um módulo formado é conectado/carregado; (b) a orientação do módulo precisa coincidir com o facing
  do host (documentado; se o jogador errar, o módulo não forma e não é contado); (c) a base antiga
  era part machine — saves antigos com módulos-part podem virar blocos órfãos, sem migração.

### G-0034 (2026-09-22) — padronização das tooltips steam (Machine Type + nome rainbow + separador)

- **Contexto:** passo 5 do `NEXT-SESSION-HANDOFF.md` (§1): padronizar as tooltips das máquinas steam no
  formato do GTNL (`nome rainbow` → `Machine Type: <receita>` → stats → high pressure → separador →
  `Source:`). O handoff já registrava que o tooltip builder **não** controla a linha do nome do item.
- **Centralizado no `GTNASteamTooltips`:** agora cobre **toda** máquina GTNA com registry path
  `large_steam_*` ou `steam_*` (inclui os módulos do Steam Elevator, `steam_manufacturer/cobbler/
  woodcutter/lava_maker/item_vault/cactus_wonder/cracking/mega_compressor/elevator`). O wrapper monta a
  lista numa ordem fixa: (1) linha do nome rainbow, (2) `Machine Type` (se houver recipe type real),
  (3) linhas originais (desc/speed/efficiency/parallel/structure + `GTNA_ADD`), (4) linha high pressure
  (só as máquinas do set `HIGH_PRESSURE`, inalterado), (5) separador, e o `GTNASources` (chamado depois)
  fecha com `Source:`. Ordem final: **stats → high pressure → separador → Source**.
- **Nome rainbow — abordagem usada (fallback documentado):** `MachineDefinition` e `MetaMachineItem`
  **não** expõem hook para estilizar o nome do item (a linha do nome é renderizada pela vanilla fora do
  builder). Então a linha do nome é adicionada como **primeira linha da tooltip** com
  `Component.translatable(definition.getDescriptionId()).withStyle(TooltipHelper.RAINBOW_HSL_SLOW)`,
  mesmo padrão do GTCEu (`GTMultiMachines`/`GTMachines`). Fica **duplicada** com a linha de nome
  vanilla (não tem como estilizar a original). O estilo é aplicado **só no client**
  (`FMLEnvironment.dist.isClient()`); a linha (sem cor) continua sendo produzida no servidor, então o
  gametest dedicado `everyGtnaMachineTooltipBuilds` continua exercitando o builder.
- **`Machine Type`:** usa a **primeira recipe type não-DUMMY** (`getRecipeTypes()[0]`) e o lang key da
  categoria (`recipeType.registryName.toLanguageKey()`, ex. `gtceu.macerator`, `gtceu.cracker`,
  `gtna.hydraulic_manufacturing`), dentro de `gtna.tooltip.machine_type` = `"Machine Type: %s"`.
  Máquinas sem recipe type real (`steam_elevator`, `large_steam_storage_tank`,
  `large_steam_solar_boiler`, `steam_item_vault`, módulos) ficam **sem** essa linha.
- **Separador:** `"\u2500".repeat(30)` em `DARK_GRAY`, adicionado **apenas** quando o
  `GTNASources.hasSource(path)` é verdadeiro (novo helper), para nunca terminar em separador órfão.
- **Atribuição:** `GTNASources` ganhou `hasSource(String)` e recebeu as máquinas GTNL que faltavam:
  `large_steam_bending/extruder/sifter/wiremill`, `steam_lava_maker`, `steam_item_vault`,
  `steam_cactus_wonder`, `steam_cracking`, `steam_mega_compressor` (todas confirmadas GTNL nos
  G-0022/G-0023/G-0024/G-0031). Assim todas as `large_steam_*`/`steam_*` têm `Source:`.
- **Lang:** `gtna.tooltip.machine_type` no `GTNALangProvider` (en_us gerado) + `pt_br.json`
  (inserção **byte-preserving**: BOM, CRLF/LF preservados, 3 linhas adicionadas, nada mais mudou).
  Também adicionados os nomes de recipe type que faltavam: `gtna.lava_maker` ("Lava Maker") e
  `gtna.cactus_wonder` ("Cactus Wonder") — usados como lang key de categoria/JEI.
- **Validação:** `spotlessApply compileJava` OK; `spotlessCheck` + `runUnitTests` (**14/14**);
  `runGameTestServer` (**25/25**, `All 25 required tests passed`); `grep -c "Parsing error loading
  recipe gtna:" run/logs/latest.log` = **0**; `runData` determinístico (segunda execução
  `written: 0`). Nenhum gametest novo (o gate espera 25).
- **Pendências abertas / verificação in-game:** (a) a tooltip é **client-side** e não é vista pelos
  gates — validar no `runClient` a animação rainbow, o `Machine Type`, o separador e a ordem
  stats→high pressure→separador→Source; (b) o nome aparece **duas vezes** (nome vanilla + linha
  rainbow) porque não há hook para estilizar a linha do nome — aceito como fallback; (c) nos módulos
  do Steam Elevator o `MetaMachineBlock` insere o `mainKey` (`gtna.machine.<id>.tooltip`) no índice 1,
  então a linha rainbow fica abaixo dele (comportamento pré-existente do duplo `.tooltips(...)`).

### G-0033 (2026-09-22) — port dos módulos Steam Apiary + Bee Breeding (Productive Bees indisponível)

- **Implementado:** `SteamApiaryModule` (tier 6) e `SteamBeeBreedingModule` (tier 8), os dois módulos
  que faltavam do Steam Elevator do GTNL. Ambos são part machines (`SteamElevatorModulePartMachine` +
  `ISteamElevatorModule`) com a ability `GTNAPartAbility.STEAM_ELEVATOR_MODULE`, buffer de vapor/EU e
  upkeep por tick, exatamente como os 12 módulos do G-0032.
- **Productive Bees NÃO está no classpath de dev:** não há dependência no `build.gradle` nem no
  `gradle.properties`, e não há menção a Productive Bees/Forestry no projeto. Portanto, conforme a
  instrução, os módulos foram implementados como **aproximações GTNA-native** (sem hard dependency e
  sem referenciar API externa):
  - **Apiary:** abriga a colônia e consome 1 favo de mel (`minecraft:honeycomb`) + 1000 mB de água a
    cada 200 ticks, produzindo 2 favos + 1 garrafa de mel (`minecraft:honey_bottle`). Upkeep
    `V[4] * 8` (equivale ao `V[4] * mMaxSlots` do GTNL na colônia base de 8 abelhas).
  - **Bee Breeding:** consome 2 favos (parentais) + 8 garrafas de mel (substituto do royal jelly) a
    cada 12000 ticks (o `mMaxProgresstime` do GTNL), produzindo 1 ovo de abelha
    (`minecraft:bee_spawn_egg`, análogo nativo da princess ignoble). Upkeep `V[6]` (exato do GTNL).
  - A inserção de saída faz *dry-run* sobre uma cópia dos slots, para nunca duplicar em insert parcial.
- **Registro/receitas/lang/config/source:** ids `steam_elevator_apiary_module` e
  `steam_elevator_bee_breeding_module`; receitas `HYDRAULIC_MANUFACTURING` mapeadas dos
  `AssemblerRecipes` do GTNL (alveary/royal jelly/beeswax/pollen → favo/mel/água); lang en_us gerado +
  `pt_br.json` (inserção byte-preserving, BOM/CRLF preservados); toggles `steamApiaryModule` e
  `steamBeeBreedingModule` (além do mestre `steamElevatorModules`, que também os gateia); atribuição
  `Source: GTNL` em `GTNASources`. GUI mínima (slots + tanque de água no Apiary).
- **Validação:** `spotlessApply compileJava` OK; `spotlessCheck` + `runUnitTests` (**14/14**);
  `runGameTestServer` (**25/25**, `All 25 required tests passed`); `grep -c "Parsing error loading
  recipe gtna:"` = 0; `runData` determinístico (written: 0). Sem gametest novo (o gate espera 25).
- **Pendências abertas:** se Productive Bees virar dependência opcional no futuro, trocar a
  aproximação nativa pela API real (bee cage/hive) mantendo o check de mod carregado.

### G-0032 (2026-09-22) — port do Steam Elevator + 8 módulos do GTNL (sistema modular)

- **Implementado:** o `SteamElevator` (35x43x35) e os 8 módulos pedidos (`SteamFlightModule`,
  `SteamWeatherModule`, `SteamGreenhouseModule`, `SteamOilDrillModule`, `SteamEntityCrusherModule`,
  `SteamOreProcessorModule`, `SteamMonsterRepellentModule`, `SteamBeaconModule`). Apiary/BeeBreeding
  **não** foram tocados (outro agente).
- **Arquitetura (GTNA-native):** o GTNL tem módulos que são multiblocos e também hatches
  (`mModuleHatches`). No GTNA os módulos viraram **part machines** (`SteamElevatorModulePartMachine`
  + `ISteamElevatorModule`) que declaram a ability `GTNAPartAbility.STEAM_ELEVATOR_MODULE`; o
  controlador é uma `WorkableMultiblockMachine` com recipe logic inerte (`DUMMY_RECIPES` + `InertRecipeLogic`)
  que drena vapor dos hatches STEAM a 1 mB = 1 EU para um buffer de 256 M EU e o distribui igualmente
  entre os módulos; cada módulo paga o upkeep e aplica o efeito. 12 slots de módulo na estrutura.
- **Estrutura:** `pattern/steam_elevator.mbs` no formato do `GTNAMultiBlockFileReader` (mesmo reader
  do ME Hypercore), decodificado do `steam_elevator.mbs` "MBS1" do GTNL via o script
  `gtna_aisles` (o mesmo que gerou as `large_steam_*`). `~` em aisle 20/row 3; letras A–J mapeadas
  para `STEEL_REINFORCED_WOOD`/`STEAM_COMPACT_PIPE_CASING`/`CASING_BRONZE_BRICKS`/`CASING_STEEL_SOLID`/
  `FIREBOX_STEEL`/frame de aço/`Blocks.BRICKS`/`Blocks.STONE_BRICKS`; H = hatches (steam/item/fluido/
  maintenance), I = slots de módulo.
- **Teleporte:** `SteamElevatorTeleport` (botão na UI do controlador) sobe o jogador acima da
  estrutura e, agachado, cicla entre dimensões cujo namespace é `ad_astra` **descobertas em runtime
  pela registry** (sem importar classe do Ad Astra). Desvio: o GTNL abria a seleção celestial do
  Galacticraft; o GTNA usa o registry de dimensões (o classpath de dev tem Ad Astra, mas nada é
  hard-coded).
- **Desvios conscientes (documentados em código):** (a) os módulos não são multiblocos 1x5x2 (o
  `steam_elevator_module.mbs` do GTNL não é usado) — são parts; (b) `wirelessMode` do GTNL (rede de
  vapor wireless) não foi portado: a estrutura exige um hatch STEAM (o `WirelessSteamInputHatch`
  serve); (c) Flight concede `mayfly` (Blood Magic não existe) e revoga ao parar; (d) Weather virou
  modo limpo/chuva/trovão sem os itens Natura/Thaumcraft; (e) Greenhouse acelera plantações
  (CropsNH não existe) com água; (f) OilDrill usa os bedrock fluid veins do GTCEu (o GTNL usa o
  underground oil do GT); (g) EntityCrusher virou moedor de monstros (drop normal); (h) OreProcessor
  faz uma etapa de maceração via registry de materiais do GTCEu (a cadeia de 7 etapas do GTNL não é
  reproduzível numa part sem recipe logic de controlador); (i) Beacon tem efeitos fixos por tier em
  vez da janela de configuração (Warp Ward/Feather Feet/Vis Regen não existem no 1.20.1);
  (j) MonsterRepellent remove monstros no raio (sem hook global de spawn).
- **Registro/receitas/lang/config:** 1 controlador + 14 parts de módulo (I/II/III de Beacon,
  Repellent e OilDrill); receitas `HYDRAULIC_MANUFACTURING` (Steam Manufacturer) mapeadas dos
  `AssemblerRecipes` do GTNL para itens GTNA; `block.gtna.*`/tooltips/config no `GTNALangProvider`
  (en_us gerado) + `pt_br.json` (inserção byte-preserving ancorada numa linha existente, BOM e
  CRLF/LF preservados); toggles `steamElevator`/`steamElevatorModules`; atribuição `Source: GTNL`
  em `GTNASources`.
- **Validação:** `spotlessCheck` + `compileJava` + `runUnitTests` (14/14, incluindo o
  `PartAbilityCoverageTest` que passou a cobrir `STEAM_ELEVATOR_MODULE`) + `runGameTestServer`
  (**25/25**, `All 25 required tests passed`) + `grep -c "Parsing error loading recipe gtna:"` = 0 +
  `runData` determinístico (written: 0). **Sem gametest novo** (o gate espera 25).
- **Pendências abertas:** validar in-game a formação/orientação da estrutura 35x43x35 (só o
  carregamento do pattern foi exercitado); conferir o balanço dos upkeeps EU dos módulos; a UI dos
  módulos é mínima (labels/slots/tanques) — a janela de configuração do Beacon e a cadeia completa
  do OreProcessor ficaram simplificadas.

### G-0009 (2026-09-20) — UI do pattern buffer: painel de config **docado** (o vazamento de 106 px)

- **Sintoma (relatado in-game):** ao clicar com o botão do meio num slot, os widgets do painel de
  configuração apareciam **fora** da página — por cima da moldura e das fileiras do inventário do
  jogador. O usuário descreveu como "problema da UI"; a mecânica (modo por slot, ghost items,
  circuito) funcionava.
- **Causa medida:** página `176 x 220`, painel de config **trocado por cima** da grade de patterns
  com o mesmo tamanho, conteúdo somando **326 px** → **106 px** desenhados abaixo da borda. O
  `FancyMachineUIWidget.setupFancyUI` dimensiona a moldura por `page.getSize()` +
  `PlayerInventoryWidget`, e o LDLib **não recorta** filhos de página (`WidgetGroup.drawInBackground`
  só checa `isVisible()`, nunca a caixa). Não era um bug de "tamanho errado", era um bug de
  **layout sem fonte de verdade**.
- **Correção (`7822893`):** página única `352 x 248` em duas colunas — grade de patterns à esquerda,
  painel de config **docado** à direita (`GuiTextures.BACKGROUND_INVERSE`). O bloco que estourava e
  **não é decisão por slot** virou o side tab fancy **Buffer Tools**
  (`PatternBufferToolsConfigurator`): limpeza de cache e ferramentas de circuito dos patterns
  (`embed`/`remove`/`skip_existing`). As duas linhas de diagnóstico encurtaram para
  `Recipe: %s` / `Mode: %s` com id *pretty-printed* e truncado (`formatModeLabel` + `compactDisplay`)
  porque o id cru estourava os 164 px da coluna.
- **Geometria centralizada:** `PatternBufferLayout` (constantes + `describeViolation()` que percorre
  o plano vertical e reporta sobreposição/estouro) e o 9º unit test `PatternBufferLayoutTest`, que
  chama o walker e ainda afirma soma das colunas, largura interna do painel, ghost rows casando com
  a grade (`9 * 18`) e que a GUI inteira (`248 + 2*4 + 86 = 342`) cabe nos **360 px lógicos** de
  1080p em GUI scale 3.
- **Validação:** `spotlessCheck` + `compileJava` + `runUnitTests` (9/9) + `runData` (só as chaves
  novas/alteradas no en_us gerado) + `runGameTestServer` (5/5, 0 `invalid dist`); screenshot
  in-game fornecido pelo usuário confirma a geometria na escala capturada.
- **Divergência consciente (anti-plágio):** o layout é nosso. Ghost rows de item/fluido + catalyst
  existem porque o GTNA guarda a especialização em `slotConfigs` (não no NBT do pattern item) — um
  clone 1:1 do GTLAdditions seria impossível **e** violação de licença (GTLAdditions é **GPL-3.0**,
  o GTNA é **LGPLv3**).
- **Pendência aberta:** conferir uma segunda escala de GUI no client (`./gradlew runClient`) se o
  usuário encontrar corte; o corte de emergência é `hasPlayerInventory()` → `false` em
  `GTNAMEPatternBufferPartMachine` (economiza 86 px, mas perde o inventário na GUI).

### G-0030 (2026-09-21) — Review in-game: blocos exatos nas estruturas + receitas alinhadas ao GTNL

- **Blocos (feedback do autor):** o casing industrial não estava onde o GTNL usa, faltavam frames e o
  item vault não usava os casings próprios. Corrigido:
  - Predicados separados `industrialCasing()` (Industrial/Advanced Industrial Steam Casing) e
    `machineCasing()` (bronze bricks / steel solid); `casing()` virou a união (não usada nos patterns).
  - As 21 `large_steam_*` + as 6 novas usam as letras exatas do GTNL: industrial nos slots certos,
    machine casing nos slots certos, gearbox/pipe/firebox/frame onde é pra ser.
  - `steam_item_vault`: HyperPressureBreelCasing + VibrationSafeCasing + steel frame + glass.
  - `steam_lava_maker`: StronzeWrappedCasing + glass + lava.
- **Receitas:** reescritas a partir das originais do GTNL (`CraftingTableRecipes.java` +
  `AssemblerRecipes.java`), mapeando GT++/GTNH → GTCEu/GTNA:
  - `Hull_Bronze` → `BRONZE_HULL`; `ReinforcedGlass` → `CASING_TEMPERED_GLASS`;
    `Machine_Bronze_*` → `STEAM_*.first()`; `Controller_Steam*Multi` → `STEAM_GRINDER` /
    `STEAM_HAMMER` / `STEAM_COMPRESSOR` / singleblock LV; `PrecisionSteamMechanism` →
    `PRECISION_STEAM_COMPONENT`; `Hydraulic*` / `Stronze` / `Breel` → itens GTNA.
  - Lava maker virou receita `HYDRAULIC_MANUFACTURING` (paridade com o SteamManufacturer do GTNL).
- **Pendência (confirmar com o autor):** itens GTNL sem equivalente exato usam substitutos GTNA —
  `Super_Chest_LV`, `CompressedSteamTurbine`, `Cover_Screen`, `plateQuintuple` (GTCEu só tem
  `plateDouble`).
- **Validação:** `spotlessCheck` + `runUnitTests` (14/14) + `runGameTestServer` (25/25) + `runData`
  (written: 0); zero `Parsing error loading recipe gtna:` no log.

### G-0029 (2026-09-21) — 3 receitas GTNA com ingrediente vazio (parse error) corrigidas

- **Achado ao rodar o gametest e grepar `Parsing error loading recipe gtna:` no log** (o gate **não**
  falha por isso — as receitas ficam simplesmente sem craft):
  - `large_steam_bath`: usava `TagPrefix.foil, GTMaterials.Steel` (**Steel não tem
    `GENERATE_FOIL`**) e `TagPrefix.rotor, GTMaterials.Aluminium` (**Aluminium não tem
    `GENERATE_ROTOR`**) → ingrediente vazio. Corrigido para placa de aço + rotor de aço.
  - `thread_hatch_zpm` / `thread_hatch_uv`: usavam `TagPrefix.cableGtQuadruple` de
    `UraniumRhodiumDinaquadide` / `EnrichedNaquadahTriniumEuropiumDuranide`, prefixo que **não é
    gerado** para esses materiais (o GTCEu usa `wireGtDouble`) → corrigido para `wireGtDouble`.
- **Lição:** o `runGameTestServer` sai 0 mesmo com receitas quebradas; vale **grepar o log** por
  `Parsing error loading recipe gtna:` ao fechar o gate.
- **Validação:** `spotlessCheck` + `runUnitTests` (14/14) + `runGameTestServer` (25/25) + `runData`
  (written: 0); **zero** erros de parse de receita GTNA no log.

### G-0028 (2026-09-21) — Re-port das 21 estruturas `large_steam_*` a partir dos `.mbs` do GTNL

- **Forma exata:** as 21 `large_steam_*` agora usam a forma do GTNL decodificada dos `.mbs`
  (`StructureFileCodec`/MBS1) com a convenção do §5 (inverter linhas e aisles). Verificação
  programática: 15 derivadas do GTNL + 6 já portadas, **todas OK** contra o `.mbs`.
- **Predicados cientes de tier** (todos gravam o **menor** tier no match context, semântica do
  `checkMachineTier` do GTNL): `casing()` (bricks/solid + industrial bronze/steel),
  `gearboxCasing()`, `pipeCasing()`, `fireboxCasing()`, `frameCasing()` (`frameGt` bronze/steel).
  Uma estrutura toda em aço → high pressure; qualquer peça de bronze → normal.
- **Mapeamento** (§6): industrial/machine casing → `casing()`; gear → `gearboxCasing()`; pipe →
  `pipeCasing()`; firebox → `fireboxCasing()`; frame (`sBlockFrames` / `metaBlockColumn` 4/5) →
  `frameCasing()`; material block → `iron_block`; glass → `Blocks.GLASS`; `SteamAssemblyCasing` →
  `STEAM_ASSEMBLY_BLOCK`.
- **Substituídas (10 divergentes):** alloy_smelter, centrifuge, thermal_centrifuge, circuit_assembler,
  crusher, forming_press, furnace, mixer, ore_washer, chemical_bath (id GTNA `large_steam_bath`).
  **Atualizadas (5 de forma correta, predicados antigos):** compressor, cutting, extractor, hammer,
  lathe.
- **Tooltips:** `GTNASteamTooltips` agora lista as 21 — todas recebem a linha de high pressure.
- **Achado (pré-existente):** a receita `gtna:large_steam_bath` falha no parse
  (`Item array cannot be empty`) — a máquina fica sem craft. Não é deste re-port; investigar depois
  (provável `ChemicalHelper.get` de prefixo/material inexistente).
- **Pendência:** validação **in-game** das estruturas re-portadas — os gametests só montam o
  alloy smelter; as demais foram validadas por comparação com o `.mbs`, não por formação real.
- **Validação:** `spotlessCheck` + `runUnitTests` (14/14) + `runGameTestServer` (25/25) + `runData`
  (written: 0).

### G-0027 (2026-09-21) — Blocos: Industrial / Advanced Industrial Steam Casing (texturas do Modernity)

- **Criados** `industrial_steam_casing` e `advanced_industrial_steam_casing` — porta dos
  `GTNLCasings.IndustrialSteamCasing` / `AdvancedIndustrialSteamCasing` (`metaCasing02` 1/2), o
  **shell** dos large steam multiblocks. Texturas do pack **Modernity-GTNH** (`MetaCasing02/1.png`,
  `2.png`, 16×16; mesmo autor do GTNL, conforme o autor do projeto). Sem CTM (o `_ctm` do Modernity é
  16×16 e não casa com o layout LDLib).
- **Receitas (paridade GTNL):** crafting `AAA/ACA/AAA` (8 placas de latão/ferro + `frameGt` de
  bronze/aço) e SteamManufacturer (`HYDRAULIC_MANUFACTURING`): 6 placas + frame + circuito(1),
  2 s @ 16 EU/t.
- **Tier:** o predicado `SteamMultiMachineBase.casing()` agora reconhece os dois casings como tier 1
  (bronze) e 2 (aço), então as estruturas re-portadas com eles entram em high pressure sem código
  novo. O `steamCasing()` das 6 máquinas novas aceita os quatro casings.
- **Cobertura:** gametest `steamCasingTiers` (25º) trava a tabela de tiers; o
  `steelCasingEnablesHighPressure` agora usa o casing avançado.
- **Decisão de escopo:** os frames/columns do GTNL (`metaBlockColumn` 4/5 = Bronze/SteelMachineFrame)
  mapeiam para `frameGt(Bronze/Steel)` no re-port (§6 do handoff), então **não** viram blocos novos.
- **Atribuição:** linha do Modernity no `THIRD_PARTY_NOTICES.md` atualizada.
- **Validação:** `spotlessCheck` + `runUnitTests` (14/14) + `runGameTestServer` (25/25) + `runData`
  (written: 0).

### G-0031 (2026-09-22) — port fiel de 3 multiblocks steam do GTNL (Cactus Wonder / Steam Cracking / Mega Steam Compressor)

- **Implementado:** três máquinas do GTNL portadas seguindo as convenções do repo
  (`registerMachine`, pattern helpers próprios, config toggle, lang en_us + pt_br byte-preserving):
  - `steam_cactus_wonder` 9x11x9 (`SteamCactusWonder`);
  - `steam_cracking` 7x4x4 (`SteamCracking`, do GTNL `large_steam_cracking`);
  - `steam_mega_compressor` 35x33x35 (`MegaSteamCompressor`).
  As três estruturas foram decodificadas dos `.mbs` "MBS1" **próprios** de cada máquina (não reusa
  nenhum shape existente); o `.mb` de mesmo nome já presente em `assets/gtna/multiblock/` confere
  com o dump do GTNL.
- **SteamCactusWonder — divergência consciente (documentada em código):** o GTNL usa um recipe map
  *fake* (`CactusWonderFakeRecipes`) só para JEI + um acumulador de combustível no `onPostTick` que
  devolve o valor como vapor. O GTNA **não tem** os itens de carvão/coque de cacto do GT++ (nem o
  `InfernalCokeRecipes` do GTNL foi portado com eles), então o port promove o mapa fake a um recipe
  type real (`GTNARecipeType.CACTUS_WONDER_RECIPES`) e mapeia para os combustíveis de carbono mais
  próximos: `CHARCOAL`/`COAL`/`COAL_BLOCK` → Steam, gema de `Coke` → SuperHeatedSteam, bloco de
  `Coke` → DenseSupercriticalSteam, 20 t cada (mesma cadência do fake). A máquina é uma
  `WorkableMultiblockMachine` (não `SteamMultiMachineBase`): ela **gera** vapor, e a base de vapor
  do GTNA chama `onStructureInvalid()` se não achar fonte `IO.IN` de steam — o GTNL também não
  exige hatch de steam nessa máquina.
- **SteamCracking:** usa `GTCEu.CRACKING_RECIPES` (mesmas receitas do `SteamCrackerRecipes` do
  GTNL). O paralelo bronze/steel do GTNL (8/16) sai de `isHighPressure()`; o bônus de ×2 velocidade
  do high pressure cobre o `getDurationModifier()/tierMachine` do GTNL. Predicados de casing
  tiered (`machineCasing`/`fireboxCasing`) preservam o tier.
- **MegaSteamCompressor:** 256 paralelos (`ModifierFunction` estático) + duração ×0.5 (convenção das
  demais large steam do GTNA); a receita do Steam Manufacturer usa 64 `LARGE_STEAM_COMPRESSOR` +
  4 hydraulic pumps (2400 t @ 1600 EU/t), espelhando o GTNL.
- **Receitas de crafting:** `SteamCracking` (Stronze pipeHuge + hydraulic pump + precision mechanism
  + bronze hull) e `SteamCactusWonder` (cactus + bronze plated bricks + hydraulic regulator),
  mapeadas dos `CraftingTableRecipes` do GTNL.
- **Config:** toggles `steamCactusWonder`/`steamCracking`/`megaSteamCompressor` (`@Configurable` +
  case no switch + lang `config.gtna.option.*`), sem quebrar o `ConfigLangKeysTest`.
- **Validação:** `spotlessCheck` + `compileJava` + `runUnitTests` (14/14) + `runGameTestServer`
  (25/25, `All 25 required tests passed`) + `grep -c "Parsing error loading recipe gtna:"` = 0 +
  `runData` (written: 0).

### G-0026 (2026-09-21) — High pressure mode (steam): tier dos casings → ×2 velocidade e ×2 steam

- **Implementado (GTNL parity):** o `SteamMultiMachineBase` agora lê o **tier do casing** da estrutura
  formada (`bronze = 1`, `steel = 2`) do match context e expõe `isHighPressure()`. Com high pressure:
  **duração ×0.5** e **steam ×2** (via `getEffectiveConversionRate()`, que dobra a taxa nominal de
  mB-por-EU — respeitando overrides como os 0.75 da distillation tower), skin de GUI em aço e linha
  "High pressure mode active" no display.
- **Predicado de casing com tier:** novo `SteamMultiMachineBase.casing()` aceita
  `CASING_BRONZE_BRICKS` **ou** `CASING_STEEL_SOLID` e grava o **menor** tier encontrado no match
  context (semântica do `ofBlocksTiered` + `checkMachineTier` do GTNL: bronze vence se presente).
  Ligado no helper `steamCasing()` (6 máquinas novas) e no `large_steam_alloy_smelter`.
- **Onde o bônus de duração é aplicado:** dentro de `AdjustableSteamParallelMachine.createThreadedRecipe`
  (não só no `getRealRecipe`), porque o `GTNAMultipleRecipesLogic` chama `createThreadedRecipe`
  direto — sem isso o `FixedThreadSteamParallelMachine` ficaria de fora. O `getRealRecipe` da base
  cobre as máquinas que usam o modifier da definição (`LargeSteam*`, `SteamManufacturer`).
- **Tooltip:** `GTNASteamTooltips.applyAll()` anexa a linha compartilhada
  `gtna.tooltip.steam.high_pressure` às 7 máquinas ligadas, **antes** do `Source:` do `GTNASources`
  (ordem stats → high pressure → Source). Lang en_us + pt_br (byte-preserving) + `runData`.
- **Decisão de escopo (autor):** mecanismo + 7 máquinas agora; as demais `large_steam_*` ganham o
  predicado com tier no **passo 3** (re-portar as estruturas), evitando retrabalho.
- **Cobertura:** gametest `steelCasingEnablesHighPressure` (24º) monta o alloy smelter com casing de
  **aço** e prova `isFormed()` + `isHighPressure()` + `getEffectiveConversionRate() == 2.0`; o teste
  do wireless hatch ganhou a asserção oposta (bronze → sem high pressure, taxa 1.0).
- **Validação:** `spotlessCheck` + `runUnitTests` (14/14) + `runGameTestServer` (24/24) + `runData`
  (written: 0).

### G-0025 (2026-09-21) — correção: estruturas próprias para as 6 máquinas steam novas

- **Erro corrigido:** o port anterior reusou o pattern da `large_steam_cutting`
  (`createLargeSteamBronzePattern`) para **todas** as 6 máquinas novas — ficaram com a **mesma
  estrutura**. Agora cada uma tem a **sua própria estrutura**, decodificada do **GTNL** (formato
  `.mbs` "MBS1" via `StructureFileCodec`) e mapeada para blocos bronze do GTNA:
  - `large_steam_bending` 5x4x5, `large_steam_extruder` 5x8x5, `large_steam_sifter` 5x7x5,
    `large_steam_wiremill` 6x5x5, `steam_item_vault` 7x11x7, `steam_lava_maker` 3x5x3.
  - Novo helper `steamCasing()` (bronze bricks + abilities) usado **uma vez** por pattern; as demais
    letras usam blocos bronze específicos (gearbox/pipe/frame/glass/magma).
  - Gametest `newSteamMachinesHaveDistinctStructures` (23º) trava a regressão.
- **Aprendizado (importante):** o **GTNL é 1.7.10 (GTNH)** e guarda as estruturas em **`.mbs`**
  (binário "MBS1" = magic + tabela de strings + índices), **não** em `.mb`. Para portar: decodificar
  com `StructureFileCodec.readBinary` e mapear `grid[y][z].charAt(x)` para
  `FactoryBlockPattern.aisle(z)` (linhas = y). Os `.mb` do GTNA são cópias legadas e **não** são
  lidos em runtime.
- **Validação:** `spotlessCheck` + `runUnitTests` (14/14) + `runGameTestServer` (23/23).

### G-0024 (2026-09-21) — Steam: `SteamItemVault` (fecha o núcleo da era Steam)

- **Implementado:** `steam_item_vault` — porta GTNA-native do `SteamItemVault` do GTNL (GPL-3.0,
  reimplementado). Storage de itens de alta capacidade: **256 slots × 64.000 itens**
  (`VaultItemStackHandler extends CustomItemStackHandler`, com `getSlotLimit`/`getStackLimit`
  sobrescritos), exposto aos buses da estrutura via `NotifiableItemStackHandler` (IO.BOTH). A UI
  mostra tipos/itens armazenados. Toggle de config, lang en_us + pt_br, receita de craft (bronze +
  hydraulic pump + precision steam component + baú), gametest `steamItemVaultHoldsLargeStacks` (22º).
- **Era Steam fechada:** só ficam `SteamElevator` (precisa do **Ad Astra**) e `SteamGate*`
  (compatibilidade com o mod **Stargate**).
- **Validação:** `spotlessCheck` + `runUnitTests` (14/14) + `runGameTestServer` (22/22) + `runData`.

### G-0023 (2026-09-21) — Steam: `SteamLavaMaker` (recipe type próprio)

- **Implementado:** `steam_lava_maker` — porta GTNA-native do `SteamLavaMaker` do GTNL (GPL-3.0,
  reimplementado). Novo recipe type `LAVA_MAKER_RECIPES` (`GTNARecipeType`), receita pedra → 1000 mB
  de lava (16 EU/t, 20 t), máquina `SteamLavaMakerMachine` na base steam com paralelo ajustável (16),
  toggle de config, lang en_us + pt_br, receita de craft (bronze + hydraulic pump + precision steam
  component + magma block), gametest `steamLavaMakerHasStoneToLavaRecipe` (21º).
- **Decisões de escopo:** `PrimitiveBrickKiln` e `SteamRockBreaker`/`SteamCarpenter` são redundantes
  com o `PrimitiveStoneFurnace`/`steam_cobbler`/`steam_woodcutter` → pulados por ora.
- **Validação:** `spotlessCheck` + `runUnitTests` (14/14) + `runGameTestServer` (21/21) + `runData`.
- **Próximo (era Steam):** SteamCactusWonder, SteamTurbine, SteamCracking, SteamAssembler,
  SteamItemVault, SteamGate*, SteamElevator (Ad Astra), SteamFusionReactor*.

### G-0022 (2026-09-21) — Roadmap por eras + Steam: família `large_steam_*` completa

- **Contexto:** o autor decidiu portar **por eras** (Steam → ULV → …), fechando multiblocos/mecânicas
  de cada era. Fontes atualizadas/clonadas: GTNL `52345d2`, TST `ceaa462`, GTOCore-Main `dc4824d`,
  GTO-GregTech-Modern. Auditoria em `docs/roadmap/port-audit-2026-09-21.md` e roadmap em
  `docs/roadmap/port-roadmap-by-era.md`.
- **Implementado (4 máquinas):** na base `AdjustableSteamParallelMachine` (16 paralelos, ×0.5 duração),
  com pattern bronze compartilhado (`createLargeSteamBronzePattern`):
  `large_steam_bending` (BENDER_RECIPES), `large_steam_extruder` (EXTRUDER_RECIPES),
  `large_steam_wiremill` (WIREMILL_RECIPES), `large_steam_sifter` (SIFTER_RECIPES).
  - Config toggles (`largeSteamBending/Extruder/Wiremill/Sifter`), lang en_us + pt_br, receitas de
    craft (bronze + hydraulic pump + precision steam component + singleblock LV).
  - Gametest `largeSteamFormingFamilyIsSteamBase` (20º).
- **Decisões do autor:** Bricked Blast Furnace = Leap Forward (pular); Furnace Array = já temos o
  Primitive Stone Furnace (pular); **Steam Elevator** vai precisar do **Ad Astra**; Apiary → Forestry
  não será usado, avaliar **Productive Bees**.
- **Validação:** `spotlessCheck` + `runUnitTests` (14/14) + `runGameTestServer` (20/20) + `runData`.
- **Próximo (era Steam):** SteamAssembler (bronze/steel), SteamTurbine, geração de recursos
  (RockBreaker/LavaMaker/Carpenter/CactusWonder), SteamItemVault, primitivos (PrimitiveBrickKiln),
  SteamCracking, grandes (MegaSolarBoiler/MegaSteamCompressor), modulares (SteamElevator + módulos),
  especiais (SteamGate*/SteamFusionReactor*).

### G-0021 (2026-09-21) — runtime tests do pattern buffer: ME lossless e auto-switch staged

- **ME lossless (deferred output):** o `drainPendingNetworkOutput` foi refatorado para aceitar uma
  inserção injetável (`NetworkInsert`) — a versão de produção passa `StorageHelper.poweredInsert`, a
  de teste passa um lambda. Hooks: `gtna$bufferPendingOutput`, `gtna$pendingOutputAmount`,
  `gtna$pendingOutputIsEmpty`, `gtna$drainPendingOutput`. Gametest
  `pendingNetworkOutputRetriesUntilItFits` (18º): rede saturada (a sobra fica na fila), insert parcial
  (só a parte aceita sai), insert completo (a fila esvazia). **Não precisou de grid AE2 real.**
- **Auto-switch por conteúdo staged:** hook `gtna$stageSlotItem`. Gametest
  `stagedContentDrivesBufferModeRequest` (19º): slot vazio não pede modo; slot com conteúdo staged
  pede o `preferredModeId`; o pin do buffer (`selectedModeId`) vence o staged.
- **Achado de licença (G-0019):** o **GTCEu base já tem** `BlockPattern.autoBuild(Player,
  MultiblockState)` e um item Terminal (LGPL-3.0). O `NexusBlockPattern` do GTNA pode ser rebaseado
  nesse código (LGPL) em vez do `AdvancedBlockPattern` do GTMThings (ARR), o que **removeria** essa
  pendência de permissão. Avaliar.
- **Validação:** `spotlessCheck` + `runUnitTests` (14/14) + `runGameTestServer` (19/19).
- **Builder de estrutura (tentado):** o GTCEu **tem** `BlockPattern.autoBuild(Player, MultiblockState)`
  e, com um `FakePlayer` **criativo**, ele constrói a estrutura sem itens. Testado na Slaughterhouse
  (7×10×7): o autoBuild **coloca os blocos**, mas o `checkPatternAt` seguinte **reprova** numa célula
  `A` (provável limite de ability — `setMaxGlobalLimited`/`setExactLimit` — que o autoBuild não
  respeita ao escolher os candidatos). Revertido para manter o gate verde; próximo passo é investigar
  o autoBuild + limites, ou montar a estrutura manualmente.
- **Ainda pendente da recomendação:** (2) builder de estrutura funcional; (3) refactors da Fase 3 e a
  Nexus Flux Matrix.

### G-0020 (2026-09-21) — higiene e QA: CHANGELOG 0.4.0, i18n da UI, pt_br e 2 lints novos

- **CHANGELOG:** entrada **0.4.0** (estava parado em 0.3.2-dev), cobrindo o port do Universal
  Factory/Stone Furnace, Thread Hatch, paridade dos hatches, migração da Slaughterhouse/Dirt Forge,
  remoção do boiler duplicado, atribuição de origem, correções (Output Boost M², crashes do buffer,
  servidor dedicado, Jade, 9 configs) e a camada de QA.
- **Fase 3 — i18n das strings de UI:** `WorkableElectricMultipleRecipesMachine` e
  `GTNAMultipleRecipesLogic` deixaram de usar `Component.literal` hardcoded; novas chaves
  `gtna.multiblock.*` (max_eut, parallels, overclock/accelerate/output_boost hatch, active_threads,
  idle, thread_line, output_line, unknown). Regenerado com `runData`.
- **pt_br:** 32 chaves novas (source, multiblock UI, Universal Factory, Primitive Stone Furnace),
  inseridas byte-preserving (BOM preservado).
- **QA (camada 0) — 2 lints novos:**
  - `PartAbilityCoverageTest` — toda ability GTNA declarada por uma peça é aceita por ao menos uma
    máquina (classe do Thread Hatch órfão). Hoje: 5 abilities, todas aceitas.
  - `RegistrationContractTest` — ids de registro e config keys únicos (59 ids, 33 config keys).
- **QA (smoke) — gametest `everyGtnaMachineTooltipBuilds` (17º):** roda o tooltip builder de toda
  máquina GTNA e falha se algum lançar (classe "crash de tooltip").
- **Ainda não feito (e por quê):**
  - **ME lossless runtime** e **auto-switch por conteúdo staged**: exigem um **grid AE2** montado no
    gametest; a lógica de drain depende de `getMainNode().getGrid()` → `IGrid`/`MEStorage`, difícil de
    falsificar sem AE2 real. Fica para quando houver um harness AE2.
  - **Gametests de estrutura** da Slaughterhouse (7×10×10) e DirtForge (~24³): montar à mão é
    inviável; precisa de um **builder data-driven** (camada 2 do QA).
  - **Fase 3 refactors** (`AnnihilateGeneratorA/B`, split do `GTNAMachines`, fundir
    `getRecipeModifier`): refactors grandes, deixados para uma sessão dedicada (risco de regressão).
  - **Nexus Flux Matrix**: feature nova (fora do escopo de "terminar").
  - **Testes manuais** (autocrafting AE2 nativo, armadura Quantum) e **retest do EMI**: exigem client.
- **Validação:** `spotlessCheck` + `runUnitTests` (14/14) + `runGameTestServer` (17/17) + `runData`.

### G-0019 (2026-09-21) — Licenciamento: permissão do GTO/GTOEPP, atribuição nos tooltips e `THIRD_PARTY_NOTICES.md`

- **Contexto:** o GTNA porta conteúdo de mods/modpacks legados (GTO, GTNL, GTNH, TST, GTL). Foi feita
  uma auditoria de licenças por **hash md5** contra as fontes locais/repositórios.
- **Achados (verificados):**
  - **GTO/GTOCore:** código **LGPLv3**; **texturas originais CC BY-NC-SA 4.0** (confirmado pelo time
    GTO). 148 texturas do GTNA são idênticas a fontes (113 GTOCore, 27 GTLCore, 6 GTO-Modern,
    2 GTLAdditions — estes 2 também existem em GTOCore/GTLCore LGPL).
  - **GTOEPP** (`Gto-Extended-Platform-Presets`): **ARR**; **permissão CONCEDIDA** pelo time GTO
    (Alcox), mantendo a atribuição. Fonte: `platforms/epp/sy_1/*`.
  - **GTNL** (`ABKQPO/GT-Not-Leisure`): **GPL-3.0** (o GitHub API reporta LGPL-3.0, mas o
    `LICENSE.txt` e o README dizem GPLv3). 151 `.mb` + família `large_steam_*`. **Permissão CONCEDIDA**
    pelo autor (ABKQPO): usar com marcação de fonte; as estruturas que o GTNL pegou do GTO devem ser
    creditadas ao GTO (cuja permissão já foi concedida).
  - **TST** (`Nxer/Twist-Space-Technology-Mod`): **GPL-3.0** (ore condenser). Permissão **pendente**.
  - **GTMThings** (`liansishen/GTMThings`): **sem licença (ARR)** — `AdvancedBlockPattern` →
    `NexusBlockPattern`. Permissão **pendente**.
  - **cmme-additions → Modernity-GTNH**: ARR → **CC BY-NC-SA 4.0** (113 texturas de plate/ingot).
    Permissão **pendente**.
  - **GTLCore:** `gradle.properties` diz LGPLv3.0, sem arquivo `LICENSE` — atribuição.
- **Decisão do autor:** aceitar o GTNA como **não-comercial** (código LGPLv3 + assets do GTO em
  CC BY-NC-SA 4.0).
- **Implementado:**
  - `GTNASources` — mapa central `máquina → fonte` + `applyAll()`, que anexa a linha
    `gtna.tooltip.source` ("Source: <addon>") a cada máquina GTNA registrada, via
    `MachineDefinition.setTooltipBuilder` + `GTRegistries.MACHINES`. Chamado no
    `CommonProxy.registerMachines`.
  - Chaves de lang `gtna.tooltip.source` + `gtna.source.*` (gto/gtnl/gtnh/tst/gtl/gtlcore/gtlsupb/
    gtoepp/gtmthings) no `GTNALangProvider`; regenerado com `runData`.
  - `THIRD_PARTY_NOTICES.md` com a matriz (fonte → licença → uso → status) + aviso não-comercial.
  - README: tabela de créditos corrigida (GTO/GTNL com as licenças certas) + aviso não-comercial.
  - Gametest `portedMachinesCreditTheirSource` (16º) trava o wiring (a linha de origem aparece no
    tooltip do `annihilate_generator`).
- **Mapa de origem completo (confirmado pelo autor):** `eye_of_wood` e `industrial_slaughterhouse` →
  **TST**; `large_steam_solar_boiler` → **GTO**; `nexus_molecular_forge`, `hyper_pressure_reactor`,
  `compact_hyper_pressure_reactor`, `steam_cobbler`, `steam_woodcutter`, `stone_superheater` →
  **GTNL**. `nexus_me_hypercore` é **GTNA-original** (sem atribuição).
- **Validação:** `spotlessCheck` + `runUnitTests` (12/12) + `runGameTestServer` (16/16) + `runData`.
- **Pendências:** pedir permissão a GTNL, TST, GTMThings (e cmme/Modernity quando for usar as
  texturas); completar o mapa de origem das máquinas acima; avaliar **recriar** as texturas do GTO se
  um dia quiser um mod comercial.

### G-0018 (2026-09-21) — Fase 2 do manifest: Industrial Slaughterhouse na base multi-receita (1ª migração)

- **Objetivo (manifest, regra 8 / ordem de entrega passo 2):** todo controlador de receita deve estar
  na base multi-receita do GTNA e aceitar a Thread Hatch. Primeira migração feita.
- **`IndustrialSlaughterhouse`** passou de `extends WorkableElectricMultiblockMachine implements
  IDisplayUIMachine, IFancyUIMachine` para **`extends WorkableElectricMultipleRecipesMachine`** (a base
  já implementa as duas interfaces e já traz `getOverclockingLogic()` público). Ajustes:
  - `MANAGED_FIELD_HOLDER` passou a encadear em `WorkableElectricMultipleRecipesMachine`;
  - removido o `getRecipeType()` fixo (a base usa `recipeTypes[activeRecipeType]`, que tem 1 tipo);
  - removido o `getOverclockingLogic()` privado (a base coleta as `OverclockHatchPartMachine` no
    `onStructureFormed` e calcula o fator); o `recipeModifier` estático agora usa o herdado;
  - `addDisplayText` ganhou a linha **Active Threads** (`getRecipeLogic().getActiveRecipeCount()` /
    `getMaxThreads()`);
  - o pattern da Slaughterhouse agora aceita **`THREAD_HATCH`**.
- **Por que o `afterWorking` continua funcionando:** o `GTNAMultipleRecipesLogic.completeRecipe()`
  chama `machine.afterWorking()` e depois `handleRecipeIO(OUT)`, então a geração de drops por circuito
  e a saída continuam iguais. Como o circuito seleciona **uma** receita por vez, o efeito prático é ~1
  thread; o ganho real é a máquina entrar na base correta (Thread/Parallel/Accelerate hats coerentes).
- **Cobertura:** gametest `industrialSlaughterhouseUsesMultipleRecipesBase` (14º) trava a migração:
  colocando o controller, o block entity tem que ser `WorkableElectricMultipleRecipesMachine`.
- **2ª migração — `DimensionallyTranscendentDirtForgeMachine`** (15º gametest
  `dirtForgeUsesMultipleRecipesBase`): passou a `extends WorkableElectricMultipleRecipesMachine
  implements IZeroEnergyMachine` (é **no-energy**). O ramo zero-energy da lógica replica o que o
  modifier estático fazia (paralelo + duration 1); `getMaxParallel()` devolve 524288 e o pattern agora
  aceita `THREAD_HATCH`. O modifier estático foi mantido só para preview/EMI.
- **Escopo fechado (decisão do autor):** os demais controladores **não** precisam migrar — eles **não**
  precisam aceitar Thread Hatch. Casos:
  - **Artificial Star** — gerador (o modifier escala a geração de EU, não é overclock de processador);
  - **Nexus Molecular Forge / Eye of Wood** — usam `GTNABatchRecipeLogic` (logic própria);
  - **Eye of Harmony** — no-energy com lógica própria;
  - **ME Storage / Nexus ME Hypercore** — não são processadores de receita.
  Chegou-se a implementar um hook opt-in (`IRecipeModifierProvider`) para destravar geradores, mas ele
  foi **revertido** por não ter consumidor real (o Artificial Star voltou a `WorkableElectricMultiblockMachine`).
- **Fase 2 = concluída no que importa:** apenas as máquinas que ganham com threads/auto-switch foram
  migradas (Slaughterhouse e Dirt Forge). As demais ficam como estão por decisão de escopo.
- **Validação:** `spotlessCheck` + `runUnitTests` (12/12) + `runGameTestServer` (15/15).
- **Pendências abertas:** (a) **teste in-game** dos drops/`afterWorking` da Slaughterhouse e do
  paralelo da DirtForge; (b) gametests de **estrutura** (Slaughterhouse 7x10x10 e DirtForge) para
  formar de fato.

### G-0017 (2026-09-21) — remoção do boiler solar duplicado, perf do buffer e triagem do EMI

- **Boiler solar duplicado removido:** havia dois multiblocos que geram steam solar com a mesma
  `SOLAR_BOILING_CELL` — `mega_pressure_solar_boiler` (`MegaSolarBoilerMachine`) e
  `large_steam_solar_boiler` (`LargeSteamSolarBoilerMachine`). Mantido o **`large_steam_solar_boiler`**
  porque é o **id do GTO** (tabela do `multiblock-port-manifest.md`); removido o mega. Ajustes:
  removida a definição em `GTNAMachines`, a classe `MegaSolarBoilerMachine`, a receita própria e o
  uso no recipe do Hyper Pressure Reactor (agora usa o large), o toggle em `ConfigHolder`
  (`megaPressureSolarBoiler`), o campo/`MegaSolarBalance`/getters em `GTNABalance`, as chaves de lang
  (provider + en_us + pt_br, incluindo `config.gtna.option.megaSolarSteamPerBlock`), e os docs
  `mega-solar-boiler.*` viraram `large-steam-solar-boiler.*` (reescritos para o large) com o
  `mkdocs.yml` e as referências cruzadas atualizadas. `runData` removeu 3 arquivos stale.
- **Perf do pattern buffer:** `pushPattern` chamava `resolveAndCacheSlotRecipe` **a cada push** do
  AE2 (varredura de milhares de receitas). Agora só resolve quando o slot ainda **não tem
  `cachedRecipeId`** (o primeiro push).
- **Triagem do EMI ("2 recipes loaded with the same id" para todas as máquinas GTNA):** **não** é
  duplicata de datapack — as receitas do GTNA são registradas em runtime pelo `GTDynamicDataPack`,
  cujo `GTDynamicPackContents` guarda por caminho num mapa que **sobrescreve** (dedup). O mesmo log
  traz o `AbstractMethodError` do bridge JEMI (EMI 1.1.13 × JEI 15.20), então é artefato do EMI/JEMI.
  Ação sugerida: reproduzir com EMI desabilitado/atualizado; se persistir, abrir issue no EMI.
- **Validação:** `spotlessCheck` + `runUnitTests` (12/12) + `runGameTestServer` (13/13) + `runData`.

### G-0016 (2026-09-21) — correções do port, crash do buffer, paralelo zero-energy e estratégia de QA

- **Receita do casing da Factory:** trocada pela original do modpack
  (`kubejs/server_scripts/gtceu.js`): `BCB/DAD/BCB` → 2×, com `solid_machine_casing` + placas duplas
  de alumínio + `mv_electric_motor`/`mv_electric_piston`.
- **Primitive Stone Furnace — threads/paralelo:** virou base multi-receita zero-energia; `getMaxParallel`
  e `getAdditionalThread` efetivamente infinitos; `onWorking()` = true (sem checagem elétrica).
- **BUG do autocrafting (só 1 craft) — causa raiz:** o `GTNAOptimizedCraftingCpuLogic` extrai N crafts
  escalados e desconta `parallel`, mas o `ParallelPatternDetails.pushInputsToExternalInventory`
  **delegava** ao `AEProcessingPattern`, que para patterns "sparse" (com slots vazios, i.e. quase
  todos) empurra os próprios `sparseInputs` **sem escalar** → 1 craft. Corrigido empurrando o
  `inputHolder` já extraído/escalado. Confirmado por log (`pushPattern slotItems=1` → devia ser N).
- **Crash do servidor (NPE):** `PatternSlotResolver.consumeVirtualItemList(left=null)` quando a receita
  não tem item inputs; adicionada a guarda `left == null || left.isEmpty()`. Era o
  `Ticking grid on end of server tick` que derrubava o mundo.
- **Zero-energy parallel sempre 1:** o log provou `visibleItems=6442450941` mas
  `ParallelLogic.getMaxByInput=0` (o GTCEu não enxerga essas receitas de fornalha vanilla-convertidas).
  O caminho zero-energy agora calcula o orçamento de paralelo **direto dos insumos visíveis**
  (`computeZeroEnergyParallel`), limitado pelo output via `limitByOutputMerging`.
- **UI — seletor de machine mode:** `ScrollableMachineModeFancyConfigurator` (5 linhas + rolagem) na
  Universal Factory, via override de `attachSideTabs`.
- **Estratégia de QA:** novo doc **`docs/roadmap/qa-strategy.md`** com as camadas (L0 lint de contrato,
  L1 unit, L2 gametest, L3 CI, L4 manual/observabilidade), a tabela de classes de bug × camada que
  pega, as práticas adotadas (teste negativo, fonte única, doc como contrato, observabilidade) e a
  avaliação do **Horizon-QA**: adotar as **ideias** (asserção por tick com janela, teste negativo,
  relatório JUnit, autoria in-game), **não** o framework.
- **QA implementado a partir do doc:** (a) helper `GTNAGameTestUtils.assertEveryTickUntilTimeout`
  (invariante por tick + `succeed` no fim da janela, ideia Horizon-QA); (b) 2 gametests novos — o
  **negativo** `universalFactoryDoesNotFormWithoutMaintenance` e o **invariante**
  `universalFactoryStaysFormedWithThreads`; (c) CI agora roda `runData` e **falha se `git diff` em
  `src/generated` não estiver vazio** (determinismo de datagen).
- **Validação:** `spotlessCheck` + `runUnitTests` (12/12) + `runGameTestServer` (13/13); o client foi
  validado pelo usuário (autocrafting dos 10 rods corrigido).

### G-0015 (2026-09-21) — port GTNA-native de 2 multiblocos do GTLsupb (Universal Factory, Primitive Stone Furnace)

- **Origem:** o usuário pediu para importar 2 multiblocos do **GTLsupb 2.6.4** (jar em
  `~/.local/share/PrismLauncher/instances/GTL八周目6月2日/minecraft/mods`). Licença do mod:
  **LGPLv3** (compatível com o GTNA). Decompilado com **CFR** (o fernflower local falha em
  Kotlin/Java 17); fonte em `/tmp/opencode/gtlsupb_src` (temporário).
- **Por que não foi cópia 1:1:** o `UniversalFactoryMachine` estende
  `org.gtlcore...WorkableElectricMultipleRecipesMachine` e usa `ILockRecipe`/`IRecipeStatus`/
  `RecipeResult`/`RecipeRunnerHelper`/`IParallelLogic`/`MultipleRecipesLogic` do **GTLCore**; a
  `ZeroEnergyMultiTypeLogic` também. O GTNA não depende do GTLCore, então o port foi feito na
  **nossa** engine (`WorkableElectricMultipleRecipesMachine` + `GTNAMultipleRecipesLogic`, que já
  fazem cross-recipe parallel + threads).
- **Universal Factory** (`gtna:universal_factory`):
  - Casing novo **`gtna:universal_factory_casing`** (bloco + textura placeholder gerada por PIL;
    trocar por uma textura dedicada quando houver).
  - Classe `UniversalFactoryMachine extends WorkableElectricMultipleRecipesMachine`: campos
    persistidos `batchMultiplier`/`runningSecs`/`autoBatch`; `getWarmupMultiplier()` exponencial,
    `getOverloadUnlocked()`, `getDynamicThreads()` = `baseThreads × 2^tier`, `getMaxParallel()` =
    `baseParallel × 2^tier × batch × warmup`, e `getAdditionalThread()` alimentando os threads da
    nossa lógica. UI com `[-]/[+]/(AUTO)` no display.
  - Registro com **32 recipe types** (dos 34 originais, caíram `DEHYDRATOR_RECIPES` e
    `LIGHTNING_PROCESSOR_RECIPES`, que são do GTLCore) e o padrão 3×3×3 (casing + `steel_frame` no
    centro + maintenance obrigatório).
  - Config nova em `config/gtna/balance/universal_factory.json` (`baseParallel`, `baseThreads`,
    `maxWarmup`, `warmupTau`, `overloadTime`, `maxBatchMultiplier`).
  - Gametest `universalFactoryFormsAndExposesRecipeTypes` (10º): 32 tipos + estrutura forma.
- **Primitive Stone Furnace** (`gtna:primitive_stone_furnace`):
  - `PrimitiveStoneFurnaceMachine extends WorkableMultiblockMachine` (sem energia):
    `fullModifyRecipe` **remove o EU** de todos os content maps e fixa `duration = 1`, então
    FURNACE_RECIPES roda sem energy hatch (GTLsupb `consumeEnergy = false`).
  - Padrão 3×3×3 de `minecraft:stone` (buraco de ar no meio, como o original).
  - Gametest `primitiveStoneFurnaceSmeltsWithoutEnergy` (11º): forma **sem energia** e smelta uma
    receita injetada até o bus de saída.
- **Validação:** `spotlessCheck` + `compileJava` + `runUnitTests` (**12/12**) +
  `runGameTestServer` (**11/11**) + `runData`.
- **Pendências:** (a) textura dedicada do casing (hoje é placeholder); (b) traduções `pt_br.json` das
  chaves novas; (c) conferir in-game o layout dos displays e o comportamento do batch/AUTO; (d) os 2
  recipe types do GTLCore ficaram de fora por decisão consciente.

### G-0014 (2026-09-21) — paridade GTO nos hatches: sem punir, quantidade configurável, tooltips e teste de runtime

- **Pedido:** (1) igualar o GTO e não punir; (2) quantidade configurável; (3) tooltips no padrão GTO;
  (4) teste de runtime do Output Boost. Ordem escolhida: 1 → 2 → 3 → 4 (comportamento antes de
  texto/teste).
- **(1) Semântica igual ao GTO (tier da receita, não da máquina):** a penalidade da Accelerate agora
  usa o tier **pré-overclock da receita** (`RecipeHelper.getPreOCRecipeEuTier`, que desconta
  `ocLevel` e paralelos) em vez do tier da máquina. A matemática virou a função pura
  `AccelerateHatchMath.compute(...)` + unit test `AccelerateHatchMathTest` (12º) com os casos do GTO
  (mesmo tier, receita baixa, receita acima, teto 100 e piso). Campo de config renomeado
  `penaltyPerTierBelowMachine` → `penaltyPerTierBelowRecipe` (JSON `balance/hatches.json`; quem tinha
  valor customizado volta ao default 20). O mixin (`GTRecipeLogicMixin`) e a lógica multi-receita
  foram ajustados; o display usa `getNominalDurationMultiplier()` (sem penalidade).
- **(2) Quantidade configurável (paridade `WorkableAmountConfigurationPartMachine`):** nova base
  `ConfigurableAmountPartMachine` (int `@Persisted @DescSynced`, `IntInputWidget`, min..max, default =
  min). Aplicada em:
  - **Accelerate**: porcentagem de duração (base do tier .. 100), default = melhor valor.
  - **Overclock**: porcentagem por passo de overclock (`round(config×100)` .. 100), default = valor
    do tier. *Nota:* agora é inteiro, então 0.3333 vira 33% (arredondamento minúsculo).
  - **Thread**: contagem de threads (0 .. máximo do tier), default = máximo; a peça ganhou
    `MANAGED_FIELD_HOLDER` próprio e UI com input.
  O default preserva o comportamento anterior; o jogador só pode afrouxar (até 100% = sem efeito).
- **(3) Tooltips no padrão GTO:** reescritos no `GTNALangProvider` (en_us regenerado por `runData`) e
  com novas chaves — `accelerate_hatch.compat`, `thread_hatch.range`, `thread_hatch.requires`,
  `overclock_hatch.note`. Corrigido o texto que ainda dizia "machine tier" (agora explica que a
  penalidade é por **receita** e que receita de tier baixo numa máquina de tier alto **não** é punida),
  além de documentar a quantidade ajustável na UI e o caso do Overclock UV não dar ganho.
- **(4) Teste de runtime do Output Boost:** gametest `outputBoostAppliesOnceOnMultipleRecipesMachine`
  (9º) monta o `duration_tester` com um Output Boost Hatch LV, injeta uma receita (nether star →
  stone) e **completa** a receita, afirmando que o bus de saída tem exatamente `M` stone, não `M²`.
  **Teste negativo provado:** reintroduzindo a aplicação manual, o teste falha ("the injected
  assembler recipe never started", porque o match passa a exigir `M²` de espaço). Revertido.
- **Validação:** `spotlessCheck` + `compileJava` + `runUnitTests` (**12/12**) +
  `runGameTestServer` (**9/9**).
- **Pendências:** (a) conferir in-game o visual das novas UIs `IntInputWidget` (client); (b) traduzir
  as chaves novas de tooltip no `pt_br.json` (hoje caem no en_US); (c) a UI das hatches é
  server-autoritativa (padrão `IntInputWidget`), validar que o valor persiste ao quebrar/colocar.

### G-0013 (2026-09-21) — auditoria dos hatches vs GTOCore/GTO + bug do Output Boost duplo

- **Pedido:** comparar Thread/Overclock/Accelerate Hatch com os repositórios locais do GTOCore e do
  GTO, procurando bugs.
- **Limite da fonte:** o **gtolib** do GTOCore é um **jar protegido** (`gtolib-release.jar`): as
  classes `com.gtolib.api.machine.impl.part.ThreadPartMachine`/`OverclockPartMachine` são `native` e
  o construtor lança `UnsatisfiedLinkError` — a lógica real está nos `*.prod.bin`/`.dev.bin`, não
  decompilável. Usei como referência: `GTOCore AccelerateHatchPartMachine` (fonte completa),
  os tooltips/registros do `GTOMachines`, e o `ThreadPartMachine.kt` do **GTLAdditions** (irmão).
- **Fórmulas conferem com o GTO:**
  - Overclock: nosso `durationMultiplierByTier` = `100/(tier-6)%` → UV 0.5, UHV 1/3, …, MAX 1/8 —
    **igual** ao tooltip do GTO (`100D/(tier-6)`).
  - Accelerate: nosso base = `50 - 2*(tier-1)` = `52 - 2*tier` — **igual** ao `super(holder, tier,
    52 - 2L*tier, 100)` do GTOCore; penalidade +20/tier igual.
  - Thread: nosso extra = `(1<<(tier-6))-1`; o GTO dá total = `1<<(tier-LuV)` → mesmo total.
  - Aplicação: o mixin do Overclock emula corretamente o GTOCore (`(f/0.5)^ocLevel` sobre o
    overclock **não-perfeito**, que é o que as máquinas do GTCEu 7.5.3 usam; confirmei que
    `PERFECT_OVERCLOCK` não é usado por máquina nenhuma no source oficial).
- **BUG REAL encontrado e corrigido — Output Boost aplicado em dobro (M → M²):** o
  `GTNAMultipleRecipesLogic.tryStartRecipe` aplicava `getOutputBoostMultiplier()` via
  `ModifierFunction.outputModifier(...)`, e o `RecipeHelperMixin` (`adjustRecipeForMatching` no
  match, `applyOutputBoosts` na execução) aplicava de novo — a base multi-receita produzia `M²` e o
  match simulado exigia `M²` de espaço. Fix: removida a aplicação local; **fonte única** =
  `GTNASpecialPartUtil`. Novo `OutputBoostContractTest` (11º unit test) faz lint de fonte e falha se
  alguém reintroduzir `.outputModifier(` no logic.
- **Doc errada (corrigida em PT/EN/ES):**
  - `accelerate-hatch.*`: tabela estava **deslocada em 2** e a coluna de fórmula era aritmeticamente
    inválida (`50 - 2×(1-1) = 48%`); corrigida para os valores do código/GTO (LV 50% … MAX 24%),
    exemplo HV/EV 44%→46% (64→66 ticks), e a compatibilidade (funciona em **qualquer** multibloco
    elétrico, não só a base multi-receita).
  - `overclock-hatch.*`: UV era `×0.55`/`-45%`; o correto é `×0.50`/`-50%` (igual ao GTO). Exemplo
    ajustado (Accelerate EV 44%, Overclock UV ×0.50) e nota de que **UV não dá ganho** (igual ao
    overclock padrão); o ganho começa no UHV.
- **Divergências conscientes (não alteradas):** (a) a penalidade da Accelerate usa o tier da
  **máquina**, enquanto o GTOCore usa o tier da **receita** — mais punitivo; documentado nas docs;
  (b) não temos a quantidade configurável por hatch do GTO (`WorkableAmountConfigurationPartMachine`);
  (c) o Thread Hatch nosso é fixo por tier, o do GTO é configurável.
- **Anti-duplicação verificada:** Overclock é auto-tratado pela base multi-receita/slaughterhouse e o
  mixin os **pula**; Accelerate é aplicado só pela lógica multi-receita (o mixin não roda porque o
  `GTNAMultipleRecipesLogic` não chama `setupRecipe`). Só o Output Boost duplicava.
- **Coil:** confirmado pelo autor que `CoilWorkableElectricMultipleRecipesMachine` é base dos
  multiblocos com coil futuros — **não deletar**.
- **Validação:** `spotlessCheck` + `compileJava` + `runUnitTests` (**11/11**) + `runGameTestServer`
  (**8/8**). O bug do Output Boost **não tem teste de runtime** (exigiria completar uma receita com
  hatch de boost e contar o output no bus) — fica como pendência.

### G-0012 (2026-09-21) — auditoria proativa: Thread Hatch órfã (foundation-only) + doc mentindo

- **Origem:** depois do G-0011, o usuário perguntou se o QA precisava abranger mais. Fiz um scan
  proativo da classe "conteúdo registrado/craftável que nenhuma máquina de gameplay aceita".
- **Achado (confirmado em código):**
  - `ThreadPartMachine` (`GTNAMachines2.java:379`) declara **só** `GTNAPartAbility.THREAD_HATCH`.
  - `PredicatesMixin` injeta no `autoAbilities` do GTCEu **apenas** `OVERCLOCK_HATCH` e
    `ACCELERATE_HATCH` — `THREAD_HATCH` ficou de fora.
  - O **único** pattern do mod que aceita `THREAD_HATCH` é o `duration_tester` (máquina de teste).
  - A `industrial_slaughterhouse` **não** aceita (pattern não lista a ability) e **não** usa a base
    multi-receita (`extends WorkableElectricMultiblockMachine`, não `IThreadModifierMachine`) —
    apesar de a doc `thread-hatch.*.md` listá-la como suportada.
  - `CoilWorkableElectricMultipleRecipesMachine` é **classe morta** (nunca instanciada/registrada).
- **Intenção (achada no `docs/roadmap/multiblock-port-manifest.md`):** regra 8 — "every
  recipe-processing controller is implemented on a GTNA multiple-recipes base and accepts the Thread
  Hatch"; ordem de entrega **passo 2** — "existing GTNA controller migration and regression tests".
  Ou seja: não é bug de pattern, é **fase de migração não executada**.
- **Por que não migrei agora:** migrar a slaughterhouse para `GTNAMultipleRecipesLogic` troca o
  pipeline de receita (o `afterWorking()` dela, que gera os drops via `handleRecipeIO`, e o
  `recipeModifier` estático de registro). `GTNAMultipleRecipesLogic` *chama* `afterWorking` e
  `handleRecipeIO(OUT)`, então é factível — mas é a fase 2 do manifest e exige validação própria.
- **Correção aplicada (escopo seguro):**
  - `thread-hatch.md/.en.md/.es.md`: removida a claim falsa da Industrial Slaughterhouse; agora
    dizem o que é verdade (base `WorkableElectricMultipleRecipesMachine`; hoje Duration Tester +
    KubeJS; demais controladores 🚧 fase 2 do manifest; GTCEu base e steam não suportam).
  - Gametest `threadHatchWiresIntoMultipleRecipesMachine` (8º): monta o duration_tester com uma
    Thread Hatch ZPM e afirma que o pattern aceita, que `addedToController` entrega a peça
    (`getThreadPartMachine() != null`) e que `GTNAMultipleRecipesLogic.getMaxThreads()` sobe para
    `1 + getThreadCount()`. Trava a fundação até a migração.
- **Validação:** `spotlessCheck` + `compileJava` + `runUnitTests` (10/10) +
  `runGameTestServer` (8/8).
- **Pendências abertas:** (a) **fase 2 do manifest** — migrar os controladores reais para a base
  multi-receita e passar a aceitar a Thread Hatch (máquina a máquina, com gametest); (b) evitar que a
  doc volte a prometer suporte que o código não tem.
- **Nota:** `CoilWorkableElectricMultipleRecipesMachine` **não é classe morta** — é a base reservada
  para os multiblocos com coil futuros (confirmado pelo autor em 2026-09-21). Não deletar.

### G-0011 (2026-09-21) — wireless steam hatch rejeitado + primeira camada de QA de wiring

- **Sintoma (relatado):** o **Wireless Steam Input Hatch** não é aceito nas máquinas steam
  multibloco do GTNA — "talvez porque não funciona como um steam input bus".
- **Causa raiz (confirmada em código + source do GTCEu 7.5.3):** o hatch **é** um hatch de vapor de
  fluido correto (`PartAbility.STEAM` + `IMPORT_FLUIDS`, tanque `IO.IN` filtrado em
  `GTMaterials.Steam`). O defeito estava no **predicado da estrutura**: 20 patterns fixavam o slot de
  vapor no **bloco exato** do hatch do GTCEu
  (`.or(blocks(GTMachines.STEAM_HATCH.getBlock()).setExactLimit(1))`), que nunca casa com o bloco do
  hatch wireless. As máquinas steam do próprio GTCEu usam o idioma por ability
  (`or(abilities(PartAbility.STEAM).setExactLimit(1))` — `GTMultiMachines.java:615/639`,
  steam grinder/oven). O `PRIMITIVE_DISTILLATION_TOWER` já listava os blocos wireless na mão
  (linhas 1047-1049), o que confirmava a intenção e a inconsistência.
- **Bug secundário que apareceria na sequência:** o `WirelessSteamOutputHatch` também declarava
  `PartAbility.STEAM`; com o slot por ability, um hatch de **saída** (tanque `IO.OUT`) poderia ocupar
  o slot de energia, `steamEnergy` ficaria `null` e a máquina se **desformaria** sozinha.
- **Correção:** 20 predicados → `abilities(PartAbility.STEAM).setExactLimit(1)`; o caso especial da
  torre de destilação virou `abilities(PartAbility.STEAM)`; e `PartAbility.STEAM` removido das duas
  registrations do output hatch (agora só `EXPORT_FLUIDS`).
- **Guards novos (a resposta à pergunta de QA):**
  - Unit `SteamWiringContractTest` (10º unit test): faz scan de `common/data` e **falha** se algum
    pattern voltar a fixar `blocks(GTMachines.STEAM_HATCH`; afirma que o input hatch declara
    `STEAM + IMPORT_FLUIDS` e que o output hatch **não** declara `STEAM`; e que ao menos uma máquina
    usa o predicado por ability.
  - Gametest `wirelessSteamHatchIsAcceptedAsSteamSource` (7º gametest): monta o
    `large_steam_alloy_smelter` (3x4x3) por código com o hatch wireless no slot de vapor, e afirma o
    retorno de `checkPatternAt` **e** que a máquina continua formada com o handler de energia de
    vapor exposto (`getCapabilitiesFlat(IO.IN, EURecipeCapability.CAP)` não vazio).
- **Teste negativo (disciplina):** reintroduzindo o pin exato no alloy smelter, o unit test falha
  apontando o arquivo:linha e o gametest falha **exatamente** na célula do hatch wireless
  (`relative=1,0,0`, bloco `gtna:wireless_steam_input_hatch`, erro "Expected components ...
  gtceu:steam_input_hatch"). Revertido; gate verde depois.
- **Validação:** `spotlessCheck` + `compileJava` + `runUnitTests` (10/10) +
  `runGameTestServer` (7/7). Sem `runData` (nada de lang/registro gerado mudou).
- **Pendências abertas:** replicar o gametest para as demais máquinas steam (hoje só o alloy smelter
  é exercised); validar o hatch wireless in-game (rede wireless de verdade, owner, transfer rate);
  conferir que boilers seguem aceitando o output hatch sem a ability STEAM.

### G-0010 (2026-09-20) — triagem de duas reviews chinesas

- **BUG-EXT-001 — “AE2 autocrafting trava depois de instalar GTNA”**
  - Tradução: ao instalar o mod, a autocrafting do AE2 fica completamente travada/inutilizável;
    não se sabe se é conflito ou bug próprio.
  - Achado de código: `CraftingCPUClusterMixin` substituía no construtor o executor de **todo**
    `CraftingCPUCluster` por `GTNAOptimizedCraftingCpuLogic`. Isso também atingia CPUs AE2
    normais e era um vetor direto para regressão fora do Nexus.
  - Mitigação aplicada na árvore de trabalho: CPUs nativas mantêm `CraftingCpuLogic` do AE2;
    o executor otimizado só é instalado quando `gtna$setMachine` anexa
    `GTNACraftingCPUInterfacePartMachine` ao cluster virtual do Nexus.
  - Validação: compilação, 9/9 unit tests e 6/6 GameTests verdes. Ainda falta reprodução manual
    com uma CPU AE2 normal e uma CPU virtual Nexus para confirmar o caso reportado em jogo.

- **BUG-EXT-002 — “armadura Quantum oculta e velocidade de voo não volta”**
  - A ocultação no inventário criativo é comportamento configurado: por padrão o mod está em
    `NORMAL`, e itens restritos só ficam habilitados em `JOURNEY` sem auto-restrição; com
    `hideFromJei=true` (nome legado da opção), o grupo `quantumCosmicNexusArmor` é ocultado da
    creative tab. É uma falha de descoberta/documentação, não um bug confirmado de registro do item.
  - A persistência de velocidade era plausível: o handler impunha `0.2F`, resetava para `0.05F`
    sem guardar o valor anterior e deixava o efeito `MOVEMENT_SPEED` dos boots por até 300 ticks.
  - Correção aplicada: estado anterior de `mayfly`, `flying`, velocidade, altura de passo e efeito
    de movimento é capturado por jogador e restaurado ao remover a armadura; há limpeza compatível
    com mundos onde a versão antiga deixou exatamente a velocidade quântica salva.
  - Validação: compilação, 9/9 unit tests e 6/6 GameTests verdes. A remoção/equipamento da armadura
    ainda precisa de teste interativo no client/server.

### G-0008 (2026-09-20) — 9 opções de config sem tradução + guard automatizado

- **Achado (auditoria preventiva depois do G-0007):** dos **97** campos `@Configurable` do
  `ConfigHolder`, **9** não tinham `config.gtna.option.<campo>` no en_us gerado —
  `primitiveDistillationTower`, `largeSteamLathe`, `largeSteamCutting`, `largeSteamFormingPress`,
  `meStorage`, `mePatternBufferProxy`, `meStorageAccessHatch`, `meBigStorageAccessHatch`,
  `meIOPortHatch`. A lib de config (`dev.toma.configuration`) monta o rótulo como
  `config.%s.option.%s` e **não asserta** — então era cosmético (chave crua na tela), não crash.
- **Correção (`eeaeba7`):** rótulos no `en_us.json` manual (a fonte que o
  `addManualTranslations()` injeta no gerado) + `pt_br.json`.
- **Guard novo:** `ConfigLangKeysTest` (8º unit test) faz parsing do `ConfigHolder` procurando campos
  `@Configurable` e afirma que cada um tem chave no en_us gerado, além de afirmar as duas chaves dos
  providers do Jade. **Verificado por teste negativo:** removendo uma chave, o teste falha. É a rede
  de segurança contra a classe inteira de bug do G-0007.
- **Validação:** `runUnitTests` (8/8), `spotlessCheck`, e regeneração completa sem diffs além das 9
  chaves.

### G-0007 (2026-09-20) — crash do client: tradução de config do Jade faltando

- **Sintoma (relatado ao rodar o client):**
  `java.lang.AssertionError: Missing config translation: config.jade.plugin_gtna.me_pattern_buffer`.
- **Causa:** o Jade cria **uma entrada de config por data provider registrado** e **afirma** que a
  tradução existe, derivando a chave do UID do provider:
  `config.jade.plugin_<namespace>.<path do uid>`. O `GTNAPatternBufferProvider` (UID
  `gtna:me_pattern_buffer`) entrou no commit `90b1102` **sem** a chave — bug pré-existente; o outro
  provider (`multiple_recipes`) tinha a chave, então a asserção parava nele.
- **Correção (`0032ddd`):** chave registrada no `GTNALangProvider` (en_us, regenerado por `runData`)
  + `pt_br.json`. Conferido que `GTNAJadePlugin` registra exatamente esses dois UIDs e nenhum outro.
- **Alcance:** em dev o `AssertionError` derruba o client; em produção o Jade mostraria a chave crua
  na tela de config.
- **Nota de processo:** isso é **client-only**, então nem `runUnitTests` nem `runGameTestServer`
  (servidor dedicado, sem config client do Jade) pegam. **Todo provider do Jade novo exige a chave.**

### G-0006 (2026-09-20) — gametests de estrutura endurecidos contra uma flakiness do matcher

- **Sintoma:** o teste `runningSecondRecipeTypeMirrorsControllerMode` falhava de forma intermitente
  (~1 a cada 6 runs) com `Maximum: 1` na célula do maintenance hatch.
- **Diagnóstico (com dump):** o erro **não** era sobra de estado. Instrumentei o teste para despejar
  todos os blocos não-ar da área no momento da falha: a área tinha **exatamente os 25 blocos** que o
  teste colocou, com **um único** maintenance hatch. Ou seja, o próprio matcher do GTCEu reporta o
  erro de limite para uma estrutura correta nessa definição. `MultiblockState.clean()` **reseta** os
  contadores, então não é contagem acumulada entre checagens — a causa raiz ficou em aberto.
- **Mitigação (`d22f6a8`):** o build da estrutura virou `buildDurationTester(...)` e o match é
  tentado **até 3 vezes** (apagando e reconstruindo entre tentativas). Isso não esconde regressão de
  feature: uma regressão falha as asserções de receita/modo, não o match, e um erro de estrutura
  consistente falharia nas 3 tentativas.
- **Outras mudanças:** template `empty_12` (12³) no lugar do `empty_5x5` (morto), com os dois testes
  de estrutura em **quadrantes disjuntas** (origem `2,2,2` e `8,2,8`) e wipe por teste; e as
  mensagens de falha agora despejam a área + a célula que falhou relativa ao controller.
- **Validação:** 8 execuções consecutivas verdes (5/5 cada), além de `spotlessCheck` + `runUnitTests`.

### G-0005 (2026-09-20) — troca de modo automática em multiblocos do mod **base**

- **Problema:** o espelho de modo existia só nas máquinas do GTNA, porque ele mora em
  `GTNAMultipleRecipesLogic`. Máquinas multi-modo do GTCEu (`large_cutter` = cutter+lathe,
  `multi_smelter` = furnace+alloy_smelter, o conjunto GCYM, ...) usam a `RecipeLogic` de estoque,
  que tem um bloqueio a mais: `searchRecipe()` procura **só** `recipeTypes[activeRecipeType]`, então
  um pattern do outro tipo nunca é encontrado — ovo-e-galinha.
- **Solução (`733521e`)**: inject no **HEAD de `RecipeLogic.searchRecipe()`** (antes do corpo da
  busca) que, se a máquina oferece >1 tipo, pergunta aos pattern buffers do controller qual modo
  está pendente e aplica a fórmula de modo da GTM.
  - API nova: `IPatternBufferModeProvider.gtna$getPendingModeId()` — devolve o modo do **filtro do
    buffer** se ele estiver pinado, senão o `preferredModeId`/`derivedModeId` de um slot que
    **realmente tem insumo staged** (pattern que não pode rodar não puxa a máquina).
  - Regra pura `BufferModeSwitchPolicy.selectTargetIndex()`: troca só com a logic **IDLE**, só para
    um tipo que a máquina oferece, e nunca como no-op. Idle-only é o que dá a segurança: máquina
    ociosa não achou receita no modo atual, então nada é interrompido.
  - `ConfigHolder.machines.bufferDrivenMachineMode`, **default ON** (o opt-in real é colocar o
    buffer na máquina; a config é escape hatch).
  - Guards: pula `GTNAMultipleRecipesLogic` (já espelha) e ignora logic que não é de controller
    (`LargeCombustionEngineMachine` também chama `searchRecipe`).
- **Cobertura:** 5º gametest `patternBufferDrivesBaseMachineMode` monta o `multi_smelter` do GTCEu
  por código, forma, pina o buffer em `alloy_smelter` e afirma a troca 0 → 1 via
  `findAndHandleRecipe()`; limpar o pin deve deixar o modo quieto. Mais
  `BufferModeSwitchPolicyTest` para a regra.
- **Lacuna honesta:** o caminho de **conteúdo staged** do `gtna$getPendingModeId` (inputs empurrados
  pelo AE2 para o slot) não tem teste — o gametest cobre o caminho do **pin do buffer**.

### G-0004 (2026-09-20) — harness de gametest + espelho de modo end-to-end

- **Harness criado do zero**: run `gameTestServer` em `legacyForge.runs`, template
  `src/main/resources/data/gtna/structures/empty_5x5.nbt` (NBT gzip gerado à mão, 5x5x5 vazio),
  classe `com.raishxn.gtna.gametest.GTNAMachineGameTests`, e CI com guarda do banner
  `GAME TESTS COMPLETE` + criação do `run/eula.txt`. Commits `fb05806`, `dd5e637`.
- **Bug real encontrado antes de qualquer teste rodar** (`998c8f8`): o mod **não carregava em
  servidor dedicado** — `Attempted to load class net/minecraft/client/Minecraft for invalid dist
  DEDICATED_SERVER`. Duas causas, ambas pré-existentes: (1) `GTNACORE` registrava
  `ConfigScreenHandler.ConfigScreenFactory` (client-only) no construtor comum; (2) `NexusTerminalBehavior`
  lia `Minecraft.getInstance().player` direto. Corrigido movendo o registro para `ClientProxy` e
  criando `ClientPlayerLookup` (client-only) acessado via `DistExecutor.unsafeCallWhenOn`.
- **4º gametest, o end-to-end do espelho** (`96829e5`, `2d8f5a8`):
  `runningSecondRecipeTypeMirrorsControllerMode` monta o `duration_tester` formado, injeta uma
  receita trivial no `CIRCUIT_ASSEMBLER_RECIPES`, alimenta energia e itens, e afirma que o
  `activeRecipeType` vai de 0 (assembler) para 1 (circuit assembler).
- **Validação:** `All 4 required tests passed`; `runData` sem alterações inesperadas.

### G-0003 (2026-09-20) — Fase C: seletor de modo no buffer (paridade GTOCore)

- **`21265ae`**: `selectedModeId` no part machine (`@Persisted` + `@DescSynced`, vazio = todos),
  filtro aplicado em `gtna$slotAcceptsRecipe` **antes** do filtro por slot (um ponto cobre slot
  handlers, fast-path de receita cacheada e matcher de slots), `verifySelectedMode()` (só no
  servidor) e `PatternBufferModeConfigurator` como side tab fancy no buffer.
- **`6af82cb`**: docs (Fase C + estado real dos gametests).
- Lang keys no `GTNALangProvider` (en_us regenerado por `runData`) e no `pt_br.json`.
- **Validação:** gate verde + coberto pelo gametest `bufferModeFilterGatesSlotAcceptance`.

### G-0002 (2026-09-20) — espelho de modo alcançável + output ME lossless

- **`7e25155`** — a Fase 1 estava **inerte**: o wire-up existia, mas (a) **nenhuma máquina Java
  declarava mais de um recipe type** (scan dos 40 registros: zero `.recipeTypes(...)`, zero
  `.recipeType()` repetido), e `gtna$resolvePatternBufferMode` devolve `null` com `length <= 1`, e
  (b) `gtna$getPreferredModeForRecipe` era **código morto** (declarado e implementado, sem call
  site). Corrigido: `duration_tester` passou a declarar `ASSEMBLER_RECIPES` +
  `CIRCUIT_ASSEMBLER_RECIPES`, o provider foi ligado em `tryStartRecipe` (com fallback no tipo
  exato para slots AUTO e pins legados) e a decisão virou a função pura
  `PatternBufferModeSelection.select` + `PatternBufferModeSelectionTest`.
- **`0958e20`** — output ME lossless (paridade com o `Ticker` do GTLCore, versão **híbrida**):
  `pendingNetworkOutput` persistido guarda só a sobra da inserção inline; `NetworkOutputTicker`
  (`IGridTickable`, 5..80 ticks, `SLEEP`/`SLOWER`/`URGENT`, lotes de 64 ops, 5 falhas seguidas) e
  `alertDevice` na transição vazio→não-vazio. O `simulate` continua reportando a sobra (anti-jam).
- **Validação:** gate verde (6 unit tests). O espelho tem gametest; **o drain ticker NÃO tem teste
  de runtime** (ver pendências).

### G-0001 (2026-09-19) — Fase 3: split do pattern buffer

- **`d2f9497`, `3b28fd1`, `74c8a1b`** (doc em `3012820`): `GTNAMEPatternBufferPartMachine` caiu de
  **2759 → 1192 linhas**, dividida em `PatternSlotResolver` (busca/matching por slot),
  `PatternBufferModeRegistry` (descoberta de modos + labels) e `PatternBufferUI` (páginas, config
  panel, widgets, seleção/preview). A máquina mantém estado persistente e ações de domínio; um
  back-reference `@Nullable patternBufferUI` roteia refreshes de preview (`refreshUiPreview()`).
- **Validação:** gate verde em cada extração.

### G-0000 (2026-09-19) — base herdada (sessões anteriores)

- Fase 0/1 (`d8e1494`), Fase 2 — três suítes unitárias (`eba02db`), correção do `Int128` (`5a7129a`)
  e a linha de fidelidade do pattern buffer (`6f46134` … `90b1102`: hidden terminal, copy/paste,
  catalisadores, proxy, cache por slot, closed-circuit, Jade). Documentado no audit doc; aqui fica
  só como contexto — **não** foi validado por gametest na época (o harness não existia).

## Pendências abertas (priorizadas)

1. **Reproduzir o caso de autocrafting AE2 (G-0010 / BUG-EXT-001)** — testar uma CPU AE2 nativa
   com padrões comuns e, separadamente, a CPU virtual do Nexus; observar se a CPU nativa continua
   usando o executor original e guardar versões/modlist/log se o travamento persistir.
2. **Teste interativo da armadura Quantum (G-0010 / BUG-EXT-002)** — equipar/remover o set completo
   e os boots, inclusive após trocar survival/creative, afirmando velocidade de voo, `mayfly`,
   efeitos e altura de passo; conferir também o item em `NORMAL` e `JOURNEY`.
3. **Teste de runtime do output ME lossless / drain ticker** — único bloco grande sem cobertura de
   runtime. Exige montar um **grid AE2** no gametest (pattern buffer + controller AE) e simular
   rede cheia / sem energia de AE, afirmando que a sobra fica em `pendingNetworkOutput` e entra
   depois. Todo o resto (harness, notas de campo) já está pronto.
4. **Cobrir o caminho de conteúdo staged do auto-switch** — o gametest do `multi_smelter` exercita o
   **pin do buffer** (`selectedModeId`); falta exercitar o `gtna$getPendingModeId` quando são os
   **inputs empurrados pelo AE2** que definem o modo do slot (exige criar/pushar um pattern de
   processamento no gametest).
5. **Fase 3 restante** (do audit doc):
   - Split de `AnnihilateGeneratorA/B` → aisles em `common/data/multiblock/`;
   - Split de `GTNAMachines` por domínio;
   - Internacionalizar as strings hardcoded de UI (`WorkableElectricMultipleRecipesMachine`,
     `GTNAMultipleRecipesLogic`);
   - Fundir `getRecipeModifier` (preview/EMI) com o caminho de execução, se fizer sentido.
6. **Higiene de testes:**
   - Hoje os gametests ficam em `src/main/java/.../gametest/` e portanto **vão no jar** (inertes em
     jogo normal). O UFO Future usa sourceset/mod de teste separado — é o refinamento natural.
   - Migração opcional dos unit tests `main()`-based para JUnit 5 (como o UFO Future).
   - A lista `testClasses` em `build.gradle` é **manual**: todo teste novo precisa ser registrado
     ali, senão nunca roda.
7. **`CHANGELOG.md` parado em `0.3.2-dev`** enquanto o mod é `0.4.0`. O fix de servidor dedicado
   (`998c8f8`), o auto-switch em máquinas do mod base (`733521e`) e o layout da UI (`7822893`)
   merecem entrada — falta decidir versão/data.
8. **Ampliar o QA (G-0011)** — a camada de lint de registro/wiring existe agora, mas é em grande
   parte manual:
   - **Camada 0 (feita parcialmente):** `SteamWiringContractTest` é o molde. Próximos lints:
     (a) toda `MachineDefinition` com `PartAbility.STEAM` também declara `IMPORT_FLUIDS` se é fonte
     (e não declara STEAM se é output); (b) toda peça com ability é aceita por ao menos uma máquina;
     (c) roda `runData` e falha se `git diff` não estiver vazio (recurso gerado/lang stale).
   - **Camada 2 (gametest matriz):** hoje cada teste de estrutura é montado à mão. Vale um helper
     data-driven `(machine, parte) → forma?` para varrer as duplas documentadas, começando pelas
     máquinas steam restantes.
   - **Camada 4 (processo):** toda review/bug externo vira teste de regressão (automatizado quando
     der, item de checklist manual quando não) — o G-0010 só fez isso no caso do AE2; o G-0011
     fechou o ciclo no hatch wireless.
   - **Docs como contrato:** `docs/gameplay/parts/wireless-steam-hatches.md` prometia "qualquer
     multiblocko" e não era verdade até o G-0011, e `thread-hatch.*.md` prometia suporte da
     Industrial Slaughterhouse que não existe (G-0012). Avaliar um lint que confira claims de doc
     contra o registro (blocos citados existem) e uma nota de "validado por" nos docs de peça.
9. **Fase 2 do manifest — migrar controladores para a base multi-receita (G-0012).** Regra 8 do
   `multiblock-port-manifest.md`: todo controlador de receita deveria estar em
   `WorkableElectricMultipleRecipesMachine` e aceitar a Thread Hatch. Hoje só o `duration_tester`.
   Migrar máquina a máquina (começando pela `industrial_slaughterhouse`, que a doc já anunciava),
   validando que `afterWorking()`/drops e o `recipeModifier` continuam corretos, com gametest por
   máquina. (`CoilWorkableElectricMultipleRecipesMachine` **fica**: é a base dos multiblocos com coil
   futuros.)
10. **Teste de runtime do Output Boost (G-0013).** O bug do M² foi corrigido por lint de fonte, mas
    não há teste que complete uma receita com hatch de boost e afirme o número de itens no bus de
    saída (`M`, não `M²`). Candidato a gametest no `duration_tester` (já tem input/output bus e
    energia no harness do `runningSecondRecipeTypeMirrorsControllerMode`).

## Notas de campo (custaram iteração — não redescobrir)

**Gametest / multibloco**

- **`runGameTestServer` sai com código 0 mesmo quando o mod falha ao carregar.** O CI precisa da
  guarda `grep -q "GAME TESTS COMPLETE" run/logs/latest.log`. Foi assim que o crash de servidor
  dedicado passou batido.
- O `run/` é gitignored: **o servidor de teste exige `run/eula.txt`** (`eula=true`). O CI cria.
- **Geometria de padrão:** `FactoryBlockPattern.start()` = `(charDir=LEFT, stringDir=UP,
  aisleDir=FRONT)`. Com o controller virado para NORTH, o índice de **char → -X**, o de **string →
  +Y** e o de **aisle → -Z**, com a célula do controller como **origem**. No `duration_tester` isso
  põe o buraco de ar `#` **+1 em Z** do controller, e a casca em `-1..+1` em X/Y mas `0..+2` em Z.
- **`setExactLimit(1)` é mínimo E máximo.** O `duration_tester` exige **maintenance hatch**
  obrigatório (além do energy hatch que o `autoAbilities` pede com `setMinGlobalLimited(1)`).
- **`EnergyContainerList` (o que o controller expõe) não implementa `addEnergy`** — o default da
  interface é no-op. Para dar energia, use o `NotifiableEnergyContainer` do próprio
  `EnergyHatchPartMachine` (campo público `energyContainer`).
- **`onStructureFormed()` marca `isFormed() = true` incondicionalmente.** Afirme o retorno de
  `checkPatternAt(state, false)` e imprima `state.error.getErrorInfo()`, senão um match falho parece
  uma estrutura formada.
- A energia é exigida **até na passada simulada**: `NotifiableEnergyContainer.handleRecipeInner`
  consulta `getEnergyStored()` mesmo com `simulate = true`.
- Receitas podem ser injetadas em runtime via `type.getAdditionHandler().beginStaging()/addStaging()/
  completeStaging()` — evita depender do datapack e ainda exercita o tipo real.
- **Flakiness conhecida do matcher:** o `duration_tester` ocasionalmente falha o `checkPatternAt`
  com `Maximum: 1` **mesmo com a estrutura correta** (confirmado por dump da área: 1 maintenance
  hatch, 25 blocos exatos). Não é sobra de estado nem contagem acumulada (`clean()` reseta). A
  mitigação é o retry com rebuild no teste; se um dia isso reaparecer em outro teste, **não perca
  tempo caçando blocos fantasmas** — duplique o retry e siga.
- **Isolamento entre testes de estrutura:** o template é todo ar, então o framework não "limpa"
  nada ao reposicionar; e os testes podem se atropelar. Use **quadrantes disjuntas** dentro de um
  template maior (`empty_12`, origens `2,2,2` e `8,2,8`) + wipe da própria área antes de construir.
- **Diagnóstico que vale ouro:** em falha de `checkPatternAt`, inclua no `helper.fail` o
  `state.error.getErrorInfo()`, a célula do erro **relativa ao controller** (`state.error.getPos()`)
  e um dump de todos os blocos não-ar da área. Foi isso que provou que o erro era do matcher e não
  do teste.

**UI fancy (LDLib / GTCEu)**

- **A página não recorta os filhos.** `FancyMachineUIWidget.setupFancyUI` dimensiona a moldura a
  partir de `Math.max(86, page.getSize().height + border*2)` **mais** a altura do
  `PlayerInventoryWidget` (86) quando `hasPlayerInventory()`; o `WidgetGroup.drawInBackground` só
  checa `isVisible()`, nunca a caixa. Resultado: widget posicionado além da borda da página **é
  desenhado** — por cima da moldura e do inventário. Foi o bug do G-0009.
- **Consequência prática:** todo layout de UI precisa de uma **fonte de verdade da geometria** e de
  um teste que a valide (padrão `PatternBufferLayout` + `PatternBufferLayoutTest`). `setSize` da
  página é derivado, nunca ajustado "no olho".
- **Orçamento vertical:** numa tela 1080p em GUI scale 3 sobram **360 px lógicos**. Página + moldura
  (`2*4`) + inventário (`86`) tem que caber nisso. `hasPlayerInventory()` é consultado **uma vez**, na
  construção do `FancyMachineUIWidget` (que roda no servidor) — não dá para decidir por tela.
- **Labels são 9 px de altura** (`fontRenderer.lineHeight`) e **não têm largura máxima** — texto
  longo simplesmente vaza para a direita. Para valores de tamanho variável, use
  `compactDisplay`/`formatModeLabel` (encurta o id e deixa legível) em vez de mostrar o registry id
  cru. Não existe tooltip dinâmico em `Widget` (só estático na construção), então diagnóstico que
  muda com a seleção tem que ser **label com supplier**, não tooltip.
- `WidgetGroup.isActive()` do **pai** bloqueia o despacho de clique para os filhos
  (`mouseClicked` só chama filho com `isVisible() && isActive()`), mas `isVisible()` do filho é o que
  controla o desenho — dá para deixar um grupo inteiro inerte/oculto sem reconstruir a UI.

**Dist (cliente vs servidor)**

- **Nunca referencie `net.minecraft.client.*` de uma classe comum**, nem dentro de lambda: o
  `RuntimeDistCleaner` rejeita o carregamento no servidor dedicado e o mod inteiro falha ao carregar.
  Lambdas sintéticas contam (o descritor do método referencia o tipo client). Use uma classe
  `@OnlyIn(Dist.CLIENT)` acessada via `DistExecutor`.
- Ao mexer em registro/estrutura/entrada do mod, **rode `runGameTestServer`**: ele é o único gate
  que carrega o mod num servidor dedicado de verdade.

**Wiring de peças e máquinas (G-0011)**

- Predicado de slot de peça se escreve por **ability**, não por bloco exato:
  `or(abilities(PartAbility.X))` e não `or(blocks(outroMod.MINHA_PECA.getBlock()))`. O idioma por
  bloco exato recusa silenciosamente qualquer peça que declare a ability (foi o caso do hatch
  wireless). O GTCEu usa `abilities(PartAbility.STEAM)` nas próprias máquinas steam — é a
  referência.
- **Ability errada em output quebra na formação, não no pattern:** um hatch de saída que declarasse
  `PartAbility.STEAM` ocuparia o slot de energia por ability, o `SteamMultiMachineBase` não acharia
  fonte `IO.IN` e chamaria `onStructureInvalid()` — a máquina "desforma sozinha" logo após formar.
  Desde G-0094, output wireless steam = só `GTNAPartAbility.STEAM_EXPORT_FLUIDS` (antes era
  `EXPORT_FLUIDS`); não registrar `STEAM` nem uma ability universal de fluidos.
- **`checkPatternAt` + dump da área** continua sendo o melhor diagnóstico: o teste negativo do
  G-0011 mostrou o bloco `gtna:wireless_steam_input_hatch` na célula `relative=1,0,0` e o erro
  "Expected components ... gtceu:steam_input_hatch", que aponta direto para o predicado culpado.
- **Geometria de pattern por código:** `offset = (1 - char, string - s0, 2 - aisle)` com o
  controller em `(char 1, string s0, aisle 2)`; confirmado em `large_steam_alloy_smelter` (s0 = 1)
  e `multi_smelter` (s0 = 0).

**Build / assets**

- `src/generated/resources` é **rastreado** (só `.cache` é ignorado). Mudou o `GTNALangProvider`?
  Rode `./gradlew runData` e **commite** o `en_us.json` regenerado.
- `src/main/resources/assets/gtna/lang/pt_br.json` tem **BOM e line endings mistos**. Ferramentas que
  reescrevem o arquivo normalizam tudo e geram diff de centenas de linhas — insira chaves preservando
  os bytes (edição binária), como foi feito.
- `spotlessApply` **remove imports não usados** e reordena; rode-o antes de compilar.
- **Opção de config nova precisa de lang**: `dev.toma.configuration` resolve o rótulo por
  `config.gtna.option.<nomeDoCampo>`. Adicione no `GTNALangProvider` (en_us, regenerado por
  `runData`) **e** no `pt_br.json`, senão a tela de config mostra a chave crua.
- **Provider do Jade novo exige lang**: o Jade afirma a existência de
  `config.jade.plugin_<namespace>.<path do UID>` e **derruba o client em dev** se faltar. Mantenha em
  sincronia com os `registerBlockDataProvider`/`registerBlockComponent` do `GTNAJadePlugin`.
- **Ponto cego de validação:** os dois gates automatizados rodam sem client (unit tests são lógica
  pura; o gametest server é servidor dedicado). Nada que seja de client — config do Jade, renderer,
  texturas, tooltips de client — é pego por eles. Mudanças nessa área precisam de `./gradlew runClient`
  manual. Parcialmente mitigado pelo `ConfigLangKeysTest` (ver G-0008).
- **Datagen pode "pular" o provider:** o `en_us.json` manual é lido pelo
  `GTNALangProvider.addManualTranslations()`, mas **não é input rastreado** do datagen — editar só
  ele pode resultar em `runData` escrevendo `written: 0` e o gerado ficar velho. Se o gerado não
  pegar sua edição, **apague `src/generated/resources/.cache`** e rode `runData` de novo (foi o que
  fez as 9 chaves de config entrarem).

**Feature de modo do pattern buffer**

- O **modo global do controller é espelho de display**; o roteamento real é **por slot**
  (`gtna$slotAcceptsRecipe`). O `activeRecipeType` é global e **não** serve de estado de roteamento
  com N threads de tipos diferentes — com threads concorrentes o tab vai alternar.
- **Máquinas do mod base (GTCEu/GCYM)** têm o bloqueio extra: `RecipeLogic.searchRecipe()` procura
  **só** o tipo ativo, então o modo precisa estar certo **antes** da busca — é por isso que o
  auto-switch vive no HEAD de `searchRecipe()` (mixin `gtna$switchModeForPendingBufferContent`) e
  não depois de achar a receita, como nas nossas máquinas.
- Há **muitas** máquinas multi-tipo no GTCEu/GCYM (não só a cutter): `multi_smelter`
  (furnace+alloy_smelter, **3x3x3** — o alvo barato para gametest de máquina base), o conjunto GCYM
  de 2 a 4 tipos, etc.
- Um pin não-vazio em `preferredModeId` **só** aceita receitas cujo tipo casa (é a invariante que
  `gtna$slotAcceptsRecipe` garante); por isso o espelho pode usar o tipo exato da receita como
  fallback sem perder fidelidade.
- **Perda de output (contexto):** o `RecipeRunner` do GTCEu **voida** a sobra de output quando o
  controller é `IVoidable` (`canVoidRecipeOutputs`) e trata `PASS_NO_CONTENTS` como sucesso; além
  disso, no match simulado ele **pula** a checagem de espaço para capabilities voidáveis. Era por
  isso que "não tinha problema" até a rede encher.

## Comandos

```bash
# Gate completo (unit tests + formatação)
./gradlew spotlessCheck runUnitTests

# Compilar
./gradlew compileJava

# Datagen (obrigatório se mexer em GTNALangProvider / assets / registro)
./gradlew runData

# Gametests (sobe um servidor dedicado de verdade e roda os @GameTest)
mkdir -p run && echo "eula=true" > run/eula.txt   # run/ é gitignored
./gradlew runGameTestServer
grep -q "GAME TESTS COMPLETE" run/logs/latest.log && echo OK || echo "NAO RODOU"
```

## Referências

- Auditoria / roadmap técnico: `docs/roadmap/technical-audit-pattern-buffer.md`
- Fidelidade vs GTLCore/GTOCore: `docs/roadmap/pattern-buffer-fidelity-gap.md`
- Fontes de referência locais: `~/MineProjects/GTCEu-7.5.3`, `~/MineProjects/GTLCore`,
  `~/MineProjects/GTOCore-Main`, `~/MineProjects/GTLAdditions`
- Projeto irmão com o mesmo harness (e sourceset de teste separado):
  `~/MineProjects/UFO-Future-1.21.1` (ver `CONTINUITY_LEDGER.md` de lá)

### G-0157 (2026-10-04) — EOH apresentação e Nexus Terminal avançado

- Autor pediu cinco ajustes: frente, renderer, terminal no fluxo Advanced Terminal GTO,
  manual natural/Z e retirada dos três blocos antigos duplicados. Fabricação segue adiada.
- Frente usa cópias das texturas inactive/active já importadas e atribuídas; manifest
  `eye-of-harmony-assets.json` registra os caminhos. Estado do modelo acompanha o ciclo
  pago sem alterar o RecipeLogic legado. Renderer corrigido sobre modelos existentes:
  centro a 16 blocos atrás do controlador, escala/orbita independentes, um Overworld,
  animação congelada em pausa/entrega e caixa de render centrada. Não é um port completo
  dos efeitos GTNH; revisão artística permanece manual.
- Manual EOH sem gate Shift; Z avança uma página por pressão. Textos EN/PT ajustados.
- Terminal 500×232, três painéis simultâneos e tema roxo/preto. Números com -1/+1, roda
  e digitação; modos visíveis; categorias roláveis e grade de escolhas/limpeza. Catálogo
  original preserva índices antigos; novas escolhas em `NexusSelectedBlocks` usam ID
  validado contra categoria e candidatos reais da célula. Famílias EOH independentes.
  Cache de categorias evita percorrer o registro a cada frame. PT-BR dos controles ampliado.
- Referência inspecionada: jar GTO `gtocore-forge-1.20.1-26.9.5.jar`,
  `com.gtolib.gtm.AdvancedTerminalBehavior` (native/uipro incompatível com GTCEu 7.5.3),
  catálogo BlockMap do GTOCore local `dc4824d`. Implementação própria LDLib, sem importar
  a classe nativa. Notas de atribuição em THIRD_PARTY_NOTICES.
- Mirror agora aplica o flip ao construtor; Demolition limpa somente células AIR estritas,
  respeitando interação/quebra cancelada e preservando controlador, ANY e bedrock.
  Replace preserva blocos já compatíveis. Orientação/módulos existentes mantidos.
- Retirados registros e receitas legados de Bridge, Stability e Compression sem tier,
  e receita legada do controlador dependente deles. MissingMappingsEvent remapeia bloco
  e item para Spatial, Stabilisation[0] e Compression[0]; os aliases sem graduação usam
  conservadoramente o primeiro tier. Datagen removeu 12 arquivos antigos nesta etapa.
- Quatro GameTests novos: NBT/compatibilidade/limites; substituição dos 138 campos de
  compressão do EOH preservando controlador e célula ignorada; Mirror/Demolition em
  pattern assimétrico delimitado; ausência de registro canônico legado e destinos vivos.
  Uma falha da fixture foi corrigida usando helper.absolutePos para consultar células
  locais; não se afrouxou a exigência de tier uniforme. Relatório registra stack completo
  de falha para diagnósticos futuros.
- Gate PASS: `spotlessCheck compileJava runUnitTests runGameTestServer runData --offline`,
  **30 classes unitárias / 171 GameTests**. Log persistente
  `build/gtna-terminal-validation.log`, XML `build/test-results/gametest/TEST-gtna.xml`.
- QA pendente: interface real/escala/servidor, renderer nas quatro direções e pausa/relog,
  saves antigos reais, Survival/retorno de blocos e trocas de portas com conteúdo/NBT,
  extração de rede AE vinculada com alcance/permissões, módulos e orientação. Roteiros
  específicos em docs/roadmap. Nenhuma aprovação visual inferida dos testes.
- Trabalho local, sem commit/push/publicação.
- Conferência final de formatação/compilação e datagen após limpeza de código e texto:
  `build/gtna-terminal-final-resources.log`, PASS (`written: 1`, texto EN de demolição).
  `runClient --offline` iniciado para QA do autor, log persistente
  `build/gtna-eoh-terminal-client.log`; abertura no mundo e aprovação visual continuam manuais.

### G-0158 (2026-10-04) — casings EOH corretos e seletor sob demanda

- Autor apresentou capturas do preview GTNA, do EOH GTNH e do Advanced Terminal e
  esclareceu o fluxo desejado: controles → botão Tiered Block → categorias → escolhas.
- Erro do import inicial confirmado no código da revisão GTNH
  `a3e1e11241a814c9fa0dd0973d5699548428f689`: MTEEyeOfHarmony usa
  BlockGTCasingsBA0 metadata 11/10 e Boundary. BlockGTCasingsBA0 liga 10 ao
  EM_INNER_SPACETIME_REINFORCED_EOH_CASING, 11 ao EM_OUTER_SPACETIME_REINFORCED_EOH_CASING
  e 12 ao EM_POWER_INFINITE. Substituídos temporal/spatial/boundary por esses PNGs
  upstream sem modificações; mcmeta incorretos removidos (originais não animados).
  Nenhum candidato correspondente no Modernity local inspecionado. Manifesto atualizado
  com hashes/origem e auditoria de vínculo; THIRD_PARTY_NOTICES registra correção LGPL-3.0.
  IDs, geometria e regras de operação não mudaram.
- Terminal mantém roxo/preto; somente painel principal visível inicialmente. Linha
  Tiered Block/Blocos por tier com Selecionar e × para limpar todas as escolhas.
  Botão abre lista de categorias; categoria abre grade. × fecha grade ou lista;
  fechar lista fecha também a grade. Fundo externo transparente. Área total 500×248.
- Glass/Light retirados do seletor e ignorados ao aplicar escolhas antigas. NBT restante
  permanece compatível. Teste de persistência/compatibilidade ampliado para conferir que
  vidro antigo não restringe candidatos nativos; sem testes artificiais de layout.
- Gate final PASS: spotlessCheck compileJava runUnitTests runGameTestServer runData
  --offline, **30 classes unitárias / 171 GameTests**; log
  `build/gtna-terminal-popup-final.log`. Datagen inicial escreveu 1 arquivo (EN novas
  chaves); rodada final written: 0. Hashes dos três PNGs conferidos contra manifesto.
- Roteiros de terminal e visual EOH atualizados. Abertura/fechamento dos painéis, escala,
  persistência em UI real e aparência dos três casings no preview/mundo dependem do autor.
  Mantidas pendências de Survival/portas com NBT, AE real e save antigo de G-0157.
- Sem novas receitas de fabricação, commit/push/publicação.
- Cliente atualizado iniciado com runClient --offline para QA do autor; log
  `build/gtna-terminal-popup-client.log`. Abertura no mundo e aprovação visual não inferidas.

### G-0159 (2026-10-04) — Steam: Breel, Clay Compound e vidro dos Large steam como no GTNL

- Pedido do autor (via GTIA): as versões Large a vapor portadas do GTNL devem ser fabricáveis na era Steam,
  como no GTNL. Comparado com `/home/raishxn/MineProjects/GTNL` (`GTNLMaterials`, `CraftingTableRecipes`,
  `MixerRecipes`, `PrimitiveBrickKilnRecipes`):
  - **Breel** sem `blastTemp` (o GTNL não tem temperatura de alto-forno): lingote na fornalha; nova receita
    à mão `breel_dust_manual` (2 pó de Bronze + 1 pó de Aço → 3 pó de Breel). O mixer continua.
  - **Clay Compound** no Brick Kiln (`brick_furnace/clay_compound`: 8 pó de argila + 8 de pedra + 8 de sílex
    → 16 lingotes), receita do Primitive Brick Kiln do GTNL.
  - **Brick Kiln** ganhou receita de controlador (a do GTOCore: hastes longas, parafusos e placa dupla de
    Wrought Iron; o Primitive Blast Furnace Hatch do GTO, inexistente aqui, trocado pelo Coke Oven Hatch).
  - **Large Steam Mixer** e **Large Steam Extractor**: Tempered Glass trocado por vidro comum (o GTNL usa
    Reinforced Glass, que é de início de jogo; decisão do autor do GTIA, opção a).
- Verificação: `spotlessCheck compileJava runUnitTests runGameTestServer runData --offline` PASS duas vezes
  (171/171 GameTests; datagen written: 0), logs `build/gtna-steam-breel-gate.log` e `gate2.log`. No pack GTIA
  completo, a checagem de alcançabilidade confirma que Large Steam Furnace, Mixer, Compressor, Hammer, Extractor,
  Alloy Smelter e o Brick Kiln são alcançáveis na era Steam.
- O jar instalado na Prism do GTIA (`de27883e…`) inclui também o trabalho local não commitado de G-0157/G-0158.
- Sem commit/push. Falta teste do autor no cliente.

### G-0159 (2026-10-04) — restrição ME, apresentação EMI/JEI e terminal

- Autor pediu Spatial Casing no controlador, ME exclusivo para entradas/saídas, terminal
  inicialmente centralizado e Battery contendo só capacitores. Após esclarecer o pedido
  de HUD, corrigiu o alvo para o painel de receita e tooltips; não se adicionou provider Jade.
- Controller appearance e textura all do modelo agora Spatial, mantendo overlays de frente.
  Modelo gerado conferido (`texture_overrides.all = gtna:block/eye_of_harmony/spatial`).
- Matching/preview/autobuild aceitam somente MEInputBus/MEInputHatch de buffer finito e
  MEOutputBus/MEOutputHatch. IMEStockingPart, crafting, duais, energia e portas normais
  não formam. Cinco limites exatos permanecem. Esta é política explícita do autor,
  distinta da aceitação anterior de entradas físicas finitas; geometria GTNH mantida.
  Estruturas existentes com portas normais precisam trocar essas portas para reformar.
- Entrega para ME usa batches até Integer.MAX_VALUE, uma inserção por produto por visita,
  máximo 64 inserções/tick e um batch de fluido/tick. Evita milhares de ticks entregando
  pacotes de 64 itens em uma fila ME de long. Filas nativas GTCEu persistem e aceitam
  produtos offline, depois exportam à rede. Ciclo só encerra após EU/produtos aceitos.
- Fixture estrutural agora cinco portas ME. Teste substitui cada uma por equivalente
  normal para provar rejeição. Operação confere gases nos slots ME reais, circuito,
  fila offline/serialização, relog sem dupla entrega/crédito, fim legado e bulk com
  100×3.000.000.000 itens acima de int, orçamento/cursor e conservação total.
- Terminal inicial x162 dentro da área 500 centraliza painel 176; abrir categorias
  move controles a x6, fechar lista centraliza. Battery usa PSS_BATTERIES, como
  GTOCore BlockMap: casings de capacitor vazio e capacitores carregados, sem máquinas
  Battery Buffer. Wireless Capacitors continua a família Nexus independente.
  Teste confirma catálogo e tipo BatteryBlock, não busca genérica por nome.
- Integração própria EMI (opcional API Forge 1.1.13, publicada via Modrinth), receitas
  por página com long, catalisador planeta, workstation controlador, sem transferência
  ou receita-tree fictícia. Cosmos legado oculto do viewer; ciclos pagos preservados.
  API/dependência compile-only; `-PeohEmiQA` adiciona runtime apenas ao dev.
  Fonte API oficial inspecionada, não UI/Jade GTL importada.
- JEI e EMI compartilham EyeOfHarmonyRecipePresentation e catálogo. Páginas de 9 colunas,
  4–11 linhas conforme altura inicial; até 99 produtos. Páginas e quantidades continuam
  completas e indexadas. EMI respeita a altura recebida e avisa se reduzir área visível.
  Texto Spacetime Tier usa nome GTNH; EU Input/Output abreviados k/M/G/T/P/E; Recipe
  Energy Efficiency deriva returnEU/startupEU (Overworld 60%). Aviso vermelho quando
  não exibe catálogo completo naquela página. Valores são base antes de campos/circuito.
- AmountOverlay JEI removido do z200, evitando letras por cima da tooltip. Manual EOH
  colore títulos azul, números vermelho e fórmulas verde e conserva estilo em cada
  codepoint após font.split. Z/natural hover permanecem. Textos EN/PT e roteiros revisados.
- Gate final PASS: spotlessCheck compileJava runUnitTests runGameTestServer runData --offline,
  **30 classes / 171 GameTests**, log `build/gtna-eoh-me-final.log`. Datagen atualizado
  nesta etapa; último gate written: 0. Teste display confere capacidades 36/54/99,
  identidade/ordem/quantidade sem truncar long. Dois ajustes de fixture foram necessários:
  slots ME não respondem a setFluidInTank; Battery deve testar família PSS, não Nexus.
- Cliente `runClient --offline -PeohEmiQA` iniciado, log `build/gtna-eoh-emi-client.log`.
  QA visual de EMI/JEI, cores/navegação, escalas, centralização e rede ME real é do autor.
  Preview/ports e matemática de operação cobertos por testes; partida do cliente não
  valida interação no mundo. REI e reload do catálogo operacional sem unload seguem pendentes.
- Sem receitas de fabricação, commit/push/publicação.
- QA de inicialização EMI: log confirma plugin gtna inicializado e recarregado (8 ms),
  catálogo EMI baked/reloaded, sem aviso de catálogo EOH indisponível. O cliente entrou
  no mundo durante a conferência. O ambiente com JEI+EMI também registrou avisos de
  duplicação de IDs de previews de outros multiblocos e exceções JEMI de crafting;
  ficaram registrados como pendência de compatibilidade geral, sem atribuí-los ao novo
  programa EOH nem afirmar que são inofensivos. Não se validou o desenho via automação.

### G-0160 (2026-10-04) — corrigir contraste e paginação real do EMI

- Autor rejeitou o painel G-0159: letras brancas sobre fundo cinza, quantidade grande
  sobreposta e só 18 de 97 produtos acessíveis. Texto compartilhado JEI/EMI agora preto;
  aviso permanece vermelho. Não se considerou a inicialização anterior aprovação visual.
- EMI usa altura efetivamente fornecida pelo WidgetHolder, com setas para páginas internas.
  Slots dinâmicos mudam ícone, interação e tooltip juntos; catálogo indexado mantém long.
  Quantidade compacta desenhada dentro do ícone, tooltip conserva quantidade exata.
  API oficial EMI SlotWidget/WidgetHolder e setas inspecionadas no sources jar 1.1.13.
- EyeOfHarmonyDisplay.reflow testado para capacidades 9/18/36/99, conservando todos os
  produtos, ordem, identidade e quantidades. Mesmo programa operacional, sem fabricação.
- Gate PASS: spotlessCheck compileJava runUnitTests runGameTestServer runData --offline,
  30 classes / 171 GameTests, build/gtna-eoh-emi-fix-gate.log, datagen written: 0.
- Autor confirmou que erro de hatch distante sumiu ao inserir os dois ME Input Hatches.
  GTCEu verifica mínimos globais depois do loop e SinglePredicateError conserva a posição
  final do scanner; não é posição obrigatória de hatch. Geometria não alterada.
- Cliente aberto não recebe as classes novas; QA de contraste/setas/escala requer reinício.
  Compatibilidade geral JEMI registrada em G-0159 permanece pendente. Sem commit/push.

### G-0161 (2026-10-04) — tempestade de validações após construção com o Terminal

- Autor congelou cliente ao construir Artificial Star. Log do processo dev 173212 mostra
  Nexus Terminal built gtna:annihilate_generator in 1882 ms às 21:16:09; às 21:19:08
  cliente tentou parar. Duas capturas (Thread.print) às 21:19–21:21 mostram Server thread
  em BlockPattern.checkPatternAt via MultiblockState.onBlockStateChanged,
  Level.markAndNotifyBlock e ForgeHooks.onPlaceItemIntoWorld. Segunda captura mostra também
  consultas/carga de chunks nesse mesmo caminho. Arquivos build/gtna-terminal-freeze-threads*.txt.
- Causa verificada: guard antigo terminava no retorno do autoBuild, e refresh imediato
  registrava mapping antes de Forge terminar replay das colocações capturadas. Cada
  notificação então podia revalidar a estrutura 109 aisles, muito maior que o EOH.
- Referência GTMThings do runtime (arquivo Curse 6843500, mods.toml versão **1.5.4**, LGPLv3):
  AdvancedTerminalBehavior e AdvancedBlockPattern inspecionados com Vineflower local,
  build/gtmthings-terminal-reference. Fluxo também síncrono, sem fila por tick, e não
  força o refresh GTNA antecipado antes de retornar de useOn. Não se copiou código novo.
- NexusBuildCheckGuard mantém alvos pendentes no thread do servidor até END do tick.
  Notificações tardias do alvo agrupadas; refresh final uma vez por estado. Estados
  de outros controllers não são bloqueados; remoção do controller não é suprimida;
  alvo removido/unloaded é ignorado no flush e pending é liberado. Cleanup no server stop.
- TerminalBehavior remove refresh imediato. Builder pula ANY antes de transformar posição/
  atualizar world state e reutiliza seleção de candidatos não limitados por identidade
  de predicado, só naquela operação; limites de layer/global seguem calculados por célula.
- GameTest de replace EOH agora forma tier 0, muda campos para 8, simula 100 notificações
  após retorno, confirma tier antigo até flush, forma tier 8 após flush e quebra um casing
  para verificar invalidação normal. Teste de escopo confere alvo/terceiros/controller
  e liberação mesmo se não houver controller. Primeiro gate falhou no contrato antigo
  que exigia liberar checks imediatamente; atualizado para fim do tick, como exigido
  pelo replay. Teste funcional de 100 notificações já havia passado nessa rodada.
- Gate final PASS: spotlessCheck compileJava runUnitTests runGameTestServer runData --offline,
  **30 classes / 171 GameTests**, build/gtna-terminal-freeze-final.log, datagen written: 0.
- Roteiro de terminal atualizado. Ainda precisa medir Artificial Star no cliente novo;
  cliente congelado usa classes anteriores. Não se encerrou o processo à força e não
  se alterou o save. Colocação permanece síncrona e pode pausar em estruturas gigantes;
  fila com orçamento por tick é melhoria futura, não foi implementada nesta correção.
  Survival/AE/proteções em jogo real permanecem pendentes. Sem commit/push/publicação.

- QA G-0161: autor pediu reiniciar o cliente travado. Processo dev antigo 173212
  encerrado com SIGTERM (sem precisar SIGKILL); runClient --offline -PeohEmiQA iniciado
  novamente, log build/gtna-terminal-fixed-client.log. Recursos/atlas carregados às
  21:30:05 America/Sao_Paulo; teste no mundo e desempenho dependem do autor.

### G-0162 (2026-10-05) — retomada e novo cliente para QA Terminal/EMI

- AGENTS.md, Estado atual, G-0159/G-0160/G-0161 e convenção de orientação relidos;
  alterações locais de outras etapas preservadas. Nenhuma alteração Java nesta retomada.
- Log `build/gtna-terminal-fixed-client.log` do cliente anterior confirma construção de
  `gtna:annihilate_generator` em **618 ms** às 21:30:43 de 04/10, salvamento às 21:30:53
  e avanço do jogador às 21:36:11. Evidência de atividade posterior à construção;
  não substitui confirmação do autor sobre responsividade/formação.
- Mesmo log contém erro no GTCEu Multiblock Async Thread-0 durante a construção:
  `MultiblockState.getBlockState()` retornou null. Inspeção GTCEu 7.5.3 confirma
  `checkPatternWithTryLock` no worker; NexusBlockPattern limpa/atualiza o mesmo estado
  sem adquirir patternLock. Possível corrida concorrente fica pendente de correção/teste,
  sem afirmar que o erro é inofensivo ou que essa causa já foi reproduzida.
- Gate de retomada PASS: spotlessCheck compileJava runUnitTests runGameTestServer runData
  --offline, **171 GameTests**, datagen written: 0;
  log `build/gtna-session-continuation-gate.log`.
- Autor pediu “inicie novamente para eu ver”. Nenhum runClient do projeto estava ativo;
  instância GregTech Odyssey no Prism preservada. Iniciado runClient --offline -PeohEmiQA,
  log `build/gtna-terminal-emi-qa-2026-10-05.log`. Inicialização e QA visual registrados
  separadamente; aprovação EMI, formação/desempenho Artificial Star, Survival/AE/proteções
  e avisos JEMI continuam pendentes. Sem fabricação EOH, commit, push ou publicação.
- Novo cliente carregou recursos/atlas às 02:59:23 America/Sao_Paulo de 05/10
  (log usa 05:59:23 UTC); sem afirmar aprovação visual ou teste no mundo.

### G-0163 (2026-10-05) — ampliar EMI/controlador e acelerar QA planetário

- Quatro capturas do autor demonstram painel EMI estreito, só 7 produtos na última
  página interna e controller com números longos ocultando progresso/estado.
- EMI width 180 → 360 pixels GUI; grade 9 colunas à esquerda, dados à direita.
  Reserva vertical de informações sai da grade: altura solicitada 60 + rows×18,
  capacidade calculada pela altura efetivamente recebida. Dados com quebra de linha,
  setas internas e indexação completa mantidas. Cabeçalho agora distingue página
  de quantidade de produtos (antes “Products 11/11” significava página 11/11).
- Controller: widget 190×125 → 302×199, resumo fixo com estado, porcentagem,
  tempo restante hh:mm:ss, chance/rendimento e selo QA; detalhes em área separada
  com rolagem, slot planetário identificado. EU abreviado com BigDecimal, sem
  converter Int128 para long; tooltip preserva inteiro exato. EN/PT atualizados.
- `-PeohFastQA` configura somente runClient com gtna.eoh.qaTicksPerTick=600.
  Tick planetário avança até 600 ticks simulados, limitado ao tempo restante;
  ciclos já pagos aceleram sem novo débito/roll. Pausa, estrutura, ME e liquidação
  permanecem nas rotinas existentes. Sem o parâmetro, normal 1 tick/tick.
  Programa/viewer conservam base 360.000 ticks; selo e tempo do controller mostram
  a velocidade real de teste (~30 s para ciclo base a 20 TPS). Sem fabricação EOH.
- GameTest novo cobre ciclo já iniciado, pausa, limite final, produtos congelados,
  débito único e crédito sem duplicação. Regressão Int128 verifica agora tooltip
  exata em vez de exigir inteiro completo no texto compacto.
- Primeiro gate detectou contrato antigo de texto exato e expectativa fixa de perda
  de 5% no teste novo; ajustado para tooltip e política de perda Nexus configurada.
  Os 172 GameTests da rodada final passaram; resultado completo abaixo.
- Cliente anterior encerrou normalmente (log antigo BUILD SUCCESSFUL); Odyssey Prism
  preservado. Roteiro de viewer documenta perfil acelerado. QA visual e desempenho
  no mundo continuam manuais; corrida assíncrona G-0162 e avisos JEMI não foram
  corrigidos nesta etapa. Tudo local, sem commit/push/publicação.
- Gate final PASS: spotlessCheck compileJava runUnitTests runGameTestServer runData
  --offline, **30 classes / 172 GameTests**, datagen written: 1;
  log `build/gtna-eoh-ui-expand-final.log`. Cliente com EMI e FastQA iniciado;
  log `build/gtna-eoh-ui-fastqa-client.log`. Inicialização não é aprovação visual.
- Cliente novo carregou recursos/atlas; clientRunVmArgs.txt confirma
  gtna.eoh.qaTicksPerTick=600. Conferência visual e teste no mundo ainda pendentes.

### G-0164 (2026-10-05) — EMI vertical conforme referência GTNH

- Autor enviou referência de painel alto com planeta, grade 9×11 e informações
  abaixo; corrigida interpretação de G-0163 que colocava dados ao lado dos produtos.
- EMI width 198 pixels GUI; planeta central, entradas de gás nas laterais superiores,
  indicador de página sobre a grade; 9 colunas com capacidade até 99 produtos.
  Dados abaixo, pretos, aviso vermelho só quando catálogo repartido. Ordem e long
  preservados, setas internas mantidas. Não foram copiados tempo/quantidades GTNH.
- Reserva de altura calculada a partir das linhas traduzidas com font.split (182px),
  incluindo espaço do aviso e botões; linhas de produtos usam a altura real do holder.
  Observação sobre valores base foi para tooltip dos produtos para liberar rodapé.
- API EMI 1.1.13 sources local confirma RecipeScreen limita altura por configuração
  maximumRecipeScreenHeight=256 e pela tela/margem. `run/config/emi.css` local agora
  maximum-recipe-screen-height=1024, sem mudar opções do Odyssey ou escala de GUI.
  Essa preferência local não impõe configuração global do EMI aos futuros usuários.
- Roteiro manual atualizado. Controlador e aceleração dev de 600× de G-0163 preservados;
  sem receitas EOH, mudanças de geometria, commit/push/publicação.
- Gate inicial PASS, 30 classes / 172 GameTests, datagen written: 0,
  build/gtna-eoh-vertical-gate.log. Último ajuste mede a reserva do rodapé pela fonte;
  validação final em andamento, resultado registrado abaixo após conclusão.
- Cliente anterior permanece ativo e não foi encerrado sem autorização; imagem nova
  ainda não foi verificada no jogo. Corrida assíncrona do Terminal e avisos JEMI seguem
  pendentes conforme G-0162/G-0163.
- Gate final PASS após cálculo de altura pela tradução: spotlessCheck compileJava
  runUnitTests runGameTestServer runData --offline, **30 classes / 172 GameTests**,
  datagen written: 0; build/gtna-eoh-vertical-final.log.
- Antes do lançamento final não havia mais processo de cliente GTNA ativo (nenhum
  modFolders=gtna após o término do gate); não foi necessário encerrar processo.
  Novo runClient --offline -PeohEmiQA -PeohFastQA iniciado, log
  build/gtna-eoh-vertical-client.log. QA visual ainda depende do autor.

### G-0165 (2026-10-05) — separar fluidos/itens, corrigir baldes e isolar altura EMI

- Autor pediu 9×9 itens + 9×2 fluidos, warning de produtos fora da página, quantidade
  acima do ícone, plasmas em 8 milhões de baldes e preview em altura normal. Rejeitou
  efeito global da alteração de maximum-recipe-screen-height em G-0164.
- GTLCore existe, branch gtl-1431-skyblock, commit 18c7814, LGPLv3.0 em gradle.properties.
  COSMOS_SIMULATION declara 120 itens/18 fluidos; preview usa widget independente.
  Inspecionado como referência; nenhum código/arte copiado. THIRD_PARTY_NOTICES atualizado.
- EyeOfHarmonyDisplay.separatedPages/separated paginam itens e fluidos em regiões
  independentes, sem repetir fluidos para cada página de itens. Capacidade máxima
  81/18; em telas menores as duas famílias continuam separadas. Todos os produtos
  permanecem indexados, em ordem por família, com long e snapshots imutáveis.
- Slot EMI desenha somente RENDER_ICON na fase do ícone e quantidade na fase overlay,
  após flush da geometria, com pose restaurada. Quantidades compactas em baldes B/kB/MB;
  tooltip mantém valor exato em baldes e mB. Entradas gasosas também usam essa unidade.
  JEI usa rótulo compacto em baldes para suas saídas fluidas. QA visual ainda pendente.
- Aviso vermelho sempre: catálogo parcial mantém contagem página/total; catálogo
  completo explica quantidades base alteradas por campos/excesso. Não se inventou
  sorteio/produtos ocultos não implementados para imitar a frase GTNH.
- Correção real solicitada para plasmas: 8.000.000 mB → 8.000.000.000 mB por plasma,
  isto é 8.000.000 baldes. Raw Star Matter/White Dwarf conservam quantidades anteriores.
  Cálculo de energia do programa não rebalanceado nesta correção de saída; pendências
  de ciclos pagos continuam congeladas e não recebem quantidades retroativas.
- run/config/emi.css retorna maximum height a 256. EyeOfHarmonyEmiScreenMixin opcional
  @Pseudo, alvo RecipeScreen.setPage após atribuir tab e antes de construir widgets,
  amplia só gtna:eye_of_harmony; troca para qualquer categoria/preview restaura altura
  configurada, recalcula centro e rebake. Não modifica EmiConfig global. Tela/escala
  seguem como limite físico. EMI 1.1.13 fonte oficial local inspecionada.
- ClientMixinContractTest verifica métodos e ordem de atribuição do tab/hook; EMI só
  no testRuntimeOnly para leitura de bytecode, mantendo runtime do mod opcional.
  GameTests conferem plasma long de 8 bilhões mB, conservação por família para grades
  1/4/9 de itens e 1/2 de fluidos, e reflow sem perda/duplicação.
- Compilação PASS; runUnitTests (30 classes) e 172 GameTests PASS na rodada atual.
  Resultado completo do gate abaixo. Roteiro manual atualizado. Cliente antigo ativo
  preservado; reinício e conferência do autor pendentes. Sem fabricação EOH,
  commit/push/publicação. Corrida Terminal/JEMI de etapas anteriores continuam pendentes.
- Gate final PASS: spotlessCheck compileJava runUnitTests runGameTestServer runData
  --offline, **30 classes / 172 GameTests**, datagen written: 1;
  build/gtna-eoh-separated-gate.log. Solicitada autorização para reiniciar apenas
  o cliente dev GTNA ativo, pois não recebe classes novas sem reinício; nenhuma
  autorização presumida. QA visual e execução do hook no cliente seguem pendentes.

### G-0166 (2026-10-05) — Replace seguro, tempo normal, gases e débito direto EOH

- Replace no Nexus Terminal protege máquinas IMultiPart já instaladas antes da
  demolição/substituição, preservando BlockEntity/NBT/inventário. Blocos estruturais
  continuam substituíveis. Hatches incompatíveis não são removidos automaticamente.
  GameTest conserva cinco hatches/buses, identidade e marcador persistente ao trocar
  campos para tier 8, com No Hatch ligado e desligado.
- Removidos eohFastQA/qaTicksPerTick e badge 600×. Operação avança um tick por tick;
  duração restante usa 20 TPS. Testes cobrem circuitos reais 0–24 no ME bus e fórmula
  de tiers 0–8: circuito/aceleração dividem tempo por 2 por nível; compressão aplica
  fator 0,97 por tier; estabilização não altera duração. Plano pago mantém duração
  congelada até terminar, mesmo ao trocar circuito.
- EOH registra e sincroniza Hydrogen/Helium consumidos no início do ciclo, em baldes;
  controller separa consumo ativo de reservas para o próximo ciclo. Excesso ativo usa
  snapshot consumido. Reload conserva snapshot; ciclos antigos sem esse dado mostram
  desconhecido, sem inventar consumo. Reserva zero após início é consumo imediato.
- NexusEnergyNetwork.consumeDirectEnergy registra última retirada bem-sucedida
  (fonte GlobalPos, máquina, Int128, tick) e persiste no NBT da rede. EOH usa essa rota
  para o único débito inicial. Quantum Terminal e Nexus Flux Matrix compartilham linha
  de última retirada e tooltip exata/localização; não simulam extração contínua EU/t.
  Testes cobrem débito, saldo, reload, precisão acima de long e falha sem trocar histórico.
  Ciclos anteriores não recebem histórico retroativo; registro guarda só último evento.
- Roteiros manuais de EOH/viewer e Terminal atualizados. Primeiro gate falhou apenas
  na asserção de tooltip localizada antes de datagen; teste passou a inspecionar também
  argumentos semânticos de TranslatableContents. Gate final PASS: spotlessCheck
  compileJava runUnitTests runGameTestServer runData --offline, 30 classes / 173
  GameTests, datagen written: 1; build/gtna-eoh-normal-terminal-final.log.
- Autor optou por fechar cliente manualmente. Ausência de cliente GTNA confirmada
  antes de iniciar runClient --offline -PeohEmiQA; nenhum processo foi encerrado.
  Log build/gtna-eoh-normal-separated-client.log; args não contêm qaTicksPerTick,
  configuração EMI maximum-recipe-screen-height=256. QA visual de G-0165, ME real,
  retirada nos dois painéis e testes manuais de proteção/sobrevivência pendentes.
- Sem commit/push/publicação e sem receitas de fabricação EOH. Corrida assíncrona
  GTCEu/avisos JEMI anteriores continuam pendentes conforme checkpoints anteriores.
- Cliente carregou recursos/atlas e áudio às 06:58:47; nenhum InvalidMixinException,
  InjectionError ou falha de aplicação do mixin encontrado no log de inicialização.
  Abertura efetiva da aba EMI e aparência ainda exigem conferência no jogo.

### G-0160 (2026-10-05) — Cactus Wonder com os valores do GTNL

- O port lia o valor de vitrine das receitas falsas do GTNL (64000 mB) como saída real e trocava o combustível
  (Cactus Charcoal/Coke do GT++) por carvão comum, com vapor superaquecido/supercrítico a partir de coque.
  No GTNL o vapor por item vem de `SteamCactusWonder.TOTAL_VALUE`: 8000 (cactus charcoal), 90000 (bloco),
  16000 (cactus coke), 180000 (bloco de coke); superaquecido e supercrítico só a partir de blocos
  duplamente comprimidos, que não existem aqui.
- Agora: carvão e carvão vegetal 8000 mB, seus blocos 90000 mB, coque 16000 mB, bloco de coque 180000 mB, tudo
  vapor comum, 20 ticks por item. `.EUt(0)` removido (gerava "EUt can't be explicitly set to 0" no log). Tooltip
  de combustível atualizado.
- Portão PASS: spotlessCheck compileJava runUnitTests runGameTestServer runData --offline, 173/173 GameTests
  (datagen written: 1, a chave nova do tooltip). Jar na Prism do GTIA `9d2c26e8…`. Sem commit.

### G-0167 (2026-10-05) — EMI único e economia EOH/Artificial Star

- Autor pediu uma página EMI com warning em vez de duas e escolheu explicitamente
  rebalancear EOH/Artificial Star para viabilizar circuito 24.
- EMI singlePage conserva o catálogo completo para getOutputs/indexação. singlePreview
  limita independentemente itens/fluidos aos slots disponíveis, sempre 1/1, sem setas
  internas. Warning conta visíveis/total; saídas ocultas no desenho continuam produzidas.
  JEI e snapshots de produção não truncados. GameTest verifica capacidade das regiões,
  identidades/longs e conservação de indexação ao recortar o desenho.
- Débito inicial trocado de GTNH 4^k para GTNA (k+1)^2 (0=1×, 4=25×, 8=81×,
  12=169×, 24=625×). startupDebit compartilhado entre plano e cotação controller;
  manuais/tooltips EN/PT atualizados. Circuito não aumenta produtos/crédito/gás.
  Tempo mantém redução por dois e piso de um tick: após atingir piso, circuito maior
  não oferece mais velocidade. Ciclos pagos preservam seus snapshots antigos.
- Quatro combustíveis Artificial Star recebem 16× EU/t mantendo duração base 200,
  ingredientes/chances. Infinity 36.028.797.018.963.968 EU/t, energia por barra
  7.205.759.403.792.793.600 EU. Quatro barras a 95% cobrem débito Overworld circuito 24;
  não significa sustentar ciclos de um tick continuamente.
- Verificação adicional encontrou gargalo de saída: um Wireless Dynamo MAX 1.048.576A
  transporta apenas 2.251.799.813.685.248 EU/t. Pattern agora aceita até 16 dynamos,
  preview default conserva um. Tooltip indica configuração Infinity; hatches/tensões
  novos não criados. GameTest monta 16 hatches existentes e verifica capacidade conjunta
  via EnergyContainerList e buffers sem overflow, além de preços/geração reais carregados.
- Testes verificam custos/duração 0–24, cotação real no ME bus, clamp 32→24 e precisão
  acima de long. Primeiro gate encontrou dois cenários antigos (clamp com 4^24 e
  circuito 6 acima de long); atualizados para custo quadrático/circuito 24. Rodada
  seguinte PASS 30 classes / 174 GameTests; ampliada verificação de transporte depois.
- Roteiros e THIRD_PARTY_NOTICES identificam adaptações GTNA. Sem cópia upstream nova,
  fabricação EOH, commit/push/publicação. QA visual e formação no mundo com 16 dynamos
  ainda manuais. Corrida assíncrona GTCEu/JEMI anteriores continuam pendentes.
- Gate final após verificação de transporte PASS: spotlessCheck compileJava runUnitTests
  runGameTestServer runData --offline; 30 classes / 174 GameTests, datagen written: 0,
  build/gtna-eoh-single-star-transfer-final.log. git diff --check PASS.
- Ausência de cliente GTNA ativo confirmada antes de iniciar runClient --offline
  -PeohEmiQA, sem encerrar processos; build/gtna-eoh-single-star-client.log.
  Abertura/QA visual e testes manuais do autor não presumidos como aprovação.
- Cliente carregou atlas/recursos; log de inicialização sem InvalidMixinException,
  InjectionError ou falha de aplicação do mixin. Conferência visual não realizada.

### G-0168 (2026-10-05) — auditoria dos terminais wireless GTLAdditions

- Autor indicou checkout GTLAdditions como candidato a port. Caminho existe, master,
  commit 8caff5e93a5e65914d10dd176d48d66e7ec8c329; GTCEu declarado 1.4.4 versus 7.5.3
  do GTNA. Inspecionados registro, base/handler dos dois parts, NetworkEnergyContainer,
  guide, receitas e mixin GTMThings do backend/telemetria.
- São terminais diretos para conta UUID, abilities energy/laser em entrada e saída;
  interface comum Long.MAX_VALUE por operação, handler especializado BigInteger para
  máquinas compatíveis. Saldo BigInteger não torna ilimitada cada chamada GTCEu comum.
  Saída nominal suficiente para Star Infinity buffado, podendo substituir 16 dynamos.
  Overclock/busca/transferência reais ainda precisam ser testados no port.
- Recomenda-se adaptar dois parts à rede Nexus existente: manter capacity/matrix,
  perdas, dimensão, vínculo e monitoramento. EOH já opera direto em Int128 e não deve
  ganhar hatch obrigatório/débito duplicado. Escopo solicitado ao autor via pergunta
  assíncrona: dois parts ou também máquinas/lógica wireless do GTLAdditions.
- Fonte tem getOutputAmperage retornando tensão e somas Long sem proteção; não portar
  essas inconsistências. Saída Nexus precisa simular corretamente recusa/capacidade
  parcial; UUID vinculado sozinho não prova que energia será aceita.
- LICENSE da origem GPL-3.0; metadata declara LGPLv3.0, divergência registrada sem
  presumir autorização/licença mais permissiva. Nenhum código/asset copiado.
- Auditoria salva em docs/roadmap/gtladditions-wireless-network-port-audit.md.
  Só documentação alterada nesta etapa, diff check PASS; gate Java não repetido.
  Implementação, QA e publicação não realizados; cliente existente preservado.

### G-0169 (2026-10-05) — port dos Network Terminals GTLAdditions para Nexus

- Autor autorizou "vamos portar"; aplicado escopo mínimo dos dois parts indicado na
  auditoria G-0168, sem máquinas/lógica wireless adicionais nem conta GTMThings nova.
- IDs wireless_energy_network_input_terminal / wireless_energy_network_output_terminal,
  GTNAEnergyHatches.NETWORK_INPUT_TERMINAL/NETWORK_OUTPUT_TERMINAL. Tier nominal MAX,
  abilities energy e laser correspondentes, não compartilháveis, sem cabos/buffer local.
- NexusNetworkTerminalPartMachine mantém UUID persistido, vínculo ao colocar, Data
  Stick direito/esquerdo para bind/unbind e Nexus Linker existente (shift desvincula).
  Heartbeat/conexão/transferências reportados ao mesmo NexusEnergyNetwork dos painéis.
- NexusNetworkEnergyContainer adapta EnergyStack GTCEu 7.5.3 (origem usava Long na
  API antiga); BigInteger para multiplicação/soma e rejeição sem mutação acima de long.
  Teto por operação Long.MAX_VALUE; saldo real Int128 e vista long saturada. Orçamento
  nominal de tensão/capacidade compartilhado entre parts, reservando hatches ordinários,
  evita overflow no EnergyContainerList; não multiplica saldo com terminais adicionais.
- quoteInsertion/availableForTransfer na rede respeitam mesma matrix/dimensão/capacidade/
  perda fracionária do commit. Matching sem débito/crédito/telemetria. Receita de saída
  exige aceitação integral; full/offline não descarta energia. API changeEnergy permite
  inserção parcial retornando somente gross realmente aceito. Perda Nexus aplicada uma vez.
- Modelos usam overlays GTNA/GTO e hull GTCEu já existentes; nenhum asset GTLAdditions
  importado. Source GTLAdditions em GTNASources/tooltips; EN/PT e dica Artificial Star
  indicam alternativa de um terminal de saída aos 16 dynamos.
- Assembly Line adaptada usa 16 wireless hatches UHV 4096A correspondentes, sensores
  UV/field generators UV (16 cada), 64 Gravi Stars/placas duplas Neutronium, 46080mB
  Soldering Alloy; duração 2400, EU/t MAX, pesquisa do precursor 512 CWU/t. Não usa
  componentes/KubeJS ausentes. Ambos IDs de receita carregados verificados.
- Arquivos adaptados de comportamento NexusNetworkTerminalPartMachine e
  NexusNetworkEnergyContainer retêm GPL-3.0-only; licença upstream incluída em
  META-INF/licenses/gtladditions/GPL-3.0.txt. LICENSE GPL versus metadata LGPL da
  origem documentados em THIRD_PARTY_NOTICES; nenhuma permissão especial presumida.
- Cinco GameTests novos: simulação sem alterar NBT, input/output/custo exato, perdas,
  valores >long, rejeição de soma overflow, full/partial/offline/dimensão/disable,
  vínculo reload, capacidades sem cabos, receita Infinity real + modifier Star por
  um part, múltiplos terminais input/output com hatch comum e receitas registradas.
  Teste Star isola modifier/capability; não afirma formação física inteira automática.
- Gate final PASS: spotlessCheck compileJava runUnitTests runGameTestServer runData
  --offline, 30 classes / 179 GameTests, datagen written: 0;
  build/gtna-network-terminal-complete.log. Primeira geração dos novos modelos/lang
  escreveu 11 arquivos em build/gtna-network-terminal-data-final.log. diff check/JSON PASS.
- Roteiro manual na auditoria atualizado: formação Star 109 slices com terminal,
  vínculo/linker, pausa por rede cheia, relog e painéis continuam para QA do autor.
  EOH não ganha hatch obrigatório nem muda débito/tempo/produtos. Sem commit/push/
  publicação; corrida assíncrona GTCEu/JEMI anteriores permanecem pendentes.
- Ausência de cliente GTNA ativo confirmada antes de lançar cliente atualizado;
  nenhum processo encerrado. QA visual/ aprovação do autor não presumidos.
- Cliente atualizado runClient --offline -PeohEmiQA carregou recursos/atlas;
  build/gtna-network-terminal-client.log, sem falha de mixin ou aviso de modelo/textura
  dos novos terminais encontrado na inicialização. Conferência visual permanece manual.


## G-0170 — 2026-10-05 — Lang/visuais GTLAdditions e Matrix máxima ilimitada

- Pedido do autor: corrigir lang, utilizar assets upstream e liberar armazenamento/
  transfer rate quando a Flux Matrix atingir 750 capacitores máximos.
- Nomes dos dois terminais adicionados explicitamente ao GTNALangProvider; langValue
  do registro não os emitia no datagen do addon. EN gerado, PT existente e conexões
  novas passam chave tradutível em vez de texto inglês fixo. Linhas capacity/transfer/
  energy/unlimited e tooltip de condição ilimitada têm EN/PT.
- Quatro arquivos upstream casing/overlay/mcmeta importados sem alteração; hashes
  confirmados contra manifesto docs/roadmap/gtladditions-wireless-assets.json.
  Modelo upstream emissivo adaptado para GTCEu 7.5.3, cube base e face afastada.
  Registro preserva casing original quando formado. GPL/commit/origem documentados
  em THIRD_PARTY_NOTICES, licença upstream já distribuída. Sem permissão presumida.
- Matrix conta somente posições no cache do pattern validado. Modo ilimitado exige
  750 capacitores internos todos MAX; capacitor externo não substitui um ausente.
  Capacity/transfer exibem ∞ em Matrix/Quantum Terminal, sem barra percentual enganosa.
- Saldo persistido migrado de Int128 para BigInteger (mesma chave Amount decimal),
  aceita depósitos além de 128 bits sem criar energia. getExactEnergy alimenta os
  painéis/tooltip do EOH; vistas compatíveis Int128 e contadores saturam sem overflow.
  Hatches/receitas mantêm limites GTCEu long por operação; ∞ remove teto da rede.
- Downgrade/749/invalidação remove modo ilimitado e conserva saldo. Se saldo exceder
  capacidade finita nova, depósitos são recusados até haver espaço. Flag NBT valida
  matrix formed/count/tier antes de restaurar.
- Dois novos GameTests: formação física 32 camadas com 750 MAX, tier menor, 749+
  capacitor externo, reparo/invalidação; simulação sem mutação, depósito >Int128,
  consumo exato, reload, downgrade, contadores sem wrap e apresentação ∞.
- Gate completo PASS: spotlessCheck compileJava runUnitTests runGameTestServer runData
  --offline; 30 classes / 186 GameTests, datagen written: 0. Log
  build/gtna-matrix-unlimited-gate.log. Primeira geração alterou 3/removeu 2 modelos
  provisórios em build/gtna-matrix-unlimited-data.log. JSON/lang/hashes/diff check PASS.
- Sem cliente GTNA ativo detectado antes de iniciar runClient --offline -PeohEmiQA;
  nenhum processo encerrado. Log build/gtna-matrix-unlimited-client.log.
- Sem commit/push/publicação; QA visual de emissão/animação/nomes e formação inteira
  do Artificial Star continuam para o autor. Não altera tempo/circuitos EOH.
- Cliente carregou recursos e atlas; nenhum aviso de modelo/textura dos terminais
  encontrado na inicialização. Conferência visual de emissão/animação segue manual.

### G-0161 (2026-10-05) — Pesquisa v02, P1: pontos por área, custo e compra

- Desenho aprovado no GTIA (GTIA-D-224, `PLANO_PESQUISA_v02_PROPOSTA.md`). Motor reutilizável; nada muda para
  packs que não definem custo.
- `KnowledgeNode`: campos `cost` (área → pontos), `kind` (`trunk`/`branch`/`leaf`) e `eureka` (item e desconto);
  construtor antigo mantido (nó grátis de tronco). Nó com custo é **comprado**: o gatilho `obtain_item` vira
  requisito (o item precisa ter sido segurado uma vez) e não libera mais o nó sozinho.
- `KnowledgeData` versão 2: por escopo, nós, pontos por área, requisitos cumpridos e eurekas; lê a versão 1 sem
  perda; saldo nunca negativo; gasto atômico.
- `KnowledgeService`: `purchase` (ordem: desconhecido, já liberado, pré-requisitos, requisito, pontos), `cost` com
  desconto de eureka (arredondado para cima), `addPoints`, `markRequirement`, `markEureka`.
- Sincronização: `NodeView` e `SKnowledgeSync` levam custo, tipo, eureka, pontos, requisitos e eurekas (construtores
  antigos mantidos). Comandos `/gtna research buy <nó>`, `points`, `points <jogador> add <área> <qtd>`.
- `KnowledgePlaythrough` compra nós com custo e acusa nó com custo que libera só por segurar o item.
- 5 GameTests novos (JSON, compra, item não libera nó comprado, dados v1→v2, pacote). Portão: spotless, compilação,
  testes unitários e **184/184 GameTests** passaram; `runData` caiu por `ClassNotFoundException` porque um
  `runClient --offline -PeohEmiQA` (de outra sessão/do autor) recompilava o mesmo projeto ao mesmo tempo. Repetir
  `runData` (chaves de idioma novas) quando o cliente fechar. Jar não instalado na Prism. Sem commit.
- (Atualização G-0161) `runData` repetido depois que o cliente fechou: BUILD SUCCESSFUL, chaves de idioma geradas.

### G-0162 (2026-10-05) — Pesquisa v02, P2: fontes de pontos com retorno decrescente

- `ResearchSource` (`data/<ns>/gtna_research_sources/*.json`): evento (`obtain`, `craft`, `smelt`, `machine_recipe`,
  `multiblock_formed`), alvo (item, tipo de receita, máquina), área, `first` (paga na primeira vez) e `milestones`
  (pagam quando o total acumulado cruza a contagem). Validação na carga: cada evento exige o alvo que usa; fonte que
  não paga nada é rejeitada.
- `ResearchSources`: índice por evento, carregador (reload listener), `record` que avança o contador do escopo e paga
  só o que cruzou; mensagem na action bar "+N <área> research".
- Ganchos: varredura de inventário (obtain), `ItemCraftedEvent`/`ItemSmeltedEvent` (craft/smelt, contam a pilha),
  `RecipeLogic.onRecipeFinish` (machine_recipe, 1 por receita) e `MultiblockControllerMachine.onStructureFormed`
  (multiblock_formed), creditando sempre o **dono** da máquina (escopo do time); máquina sem dono não paga.
  Ambos saem na hora quando nenhum pack define fontes.
- `KnowledgeData` guarda os contadores por fonte e escopo (persistidos).
- 3 GameTests novos (validação do JSON, primeira vez e marcos, receitas e multiblocos por dono). Portão PASS:
  spotlessCheck compileJava runUnitTests runGameTestServer runData --offline, **189/189 GameTests**, datagen
  written: 1. Jar na Prism do GTIA `39f5a0d8…`. Sem commit.

### G-0163 (2026-10-05) — Pesquisa v02, P3 (motor) e P5: análise, Eureka, compra pela tela

- `ResearchSource` ganhou o evento `analyze` (exige `item` e `recipe_type`): máquina do time termina receita do tipo
  que consome o item. `ResearchSources.analyze` paga as fontes e, na primeira vez, marca o Eureka de todo nó cujo
  `eureka.item` é o item, avisando o time no chat (`gtna.research.eureka`). Gancho em
  `ResearchMachineHooks.onRecipeFinished` (só para tipos que têm fonte `analyze`).
- Pacote C→S `CKnowledgePurchase` (id de pacote novo no fim da lista): o servidor refaz todas as checagens via
  `KnowledgeService.purchase` e responde na action bar. Mensagem extraída para `KnowledgeCommands.purchaseMessage`
  (usada também por `/gtna research buy`).
- `KnowledgeTreeScreen` reescrita no visual GTCEu: `GuiTextures.BACKGROUND` no painel, `DISPLAY` na área da árvore
  (arrastar, rolar; Shift+rolar = horizontal) e no painel lateral; molduras por tipo em
  `<ns>:textures/gui/research/frame_<kind>.png` (fallback `GuiTextures.SLOT`); ícones de área
  `<ns>:textures/gui/research/area_<área>.png` com saldo na barra de título; painel lateral com custo (saldo atual,
  verde/vermelho), requisito, Eureka (achado ou dica), pré-requisitos, receitas, texto `.desc` e botão Pesquisar
  (`GuiTextures.BUTTON`), ativo só quando o cliente acha que a compra passa.
- Chaves novas: `gtna.research.eureka`, `kind.*`, `screen.select|click|cost|free|requirement|eureka.found|eureka.hint|research`.
- 1 GameTest novo (análise paga uma vez só na máquina certa, acha só o Eureka certo, custo cai pela metade,
  pacote ida e volta, compra com o custo reduzido). Portão PASS: spotlessCheck compileJava runUnitTests
  runGameTestServer runData --offline, **190/190 GameTests**. Jar na Prism do GTIA `baa04c97…` (backup em
  `GTIA-arquivo/jar-backups/2026-10-05-p3-analysis/`). Visual da tela ainda não testado no cliente. Sem commit.

### G-0164 (2026-10-05) — GTIA abandona a pesquisa; P6 desfeita

- O autor do GTIA abandonou a árvore de pesquisa (GTIA-D-226) após auditoria: espelhava o livro, a escolha era ilusória
  e os efeitos eram pequenos. O GTIA não define mais nós nem fontes; o motor do GTNA fica como está (P1–P3, P5), inerte
  sem conteúdo de pack. P4 (consumidores de flag) e P6 não serão feitas.
- Desfeito o que esta sessão tinha acrescentado para a P6: modo `budget` do `KnowledgePlaythrough` e o GameTest dele
  (os dois arquivos voltaram ao estado do HEAD). Antes de desfazer, o portão tinha passado com 191/191 GameTests.
- Sem commit.

## G-0171 — 2026-10-06 — Auditoria inicial Personal Space / PDim

- Pedido do autor: portar a Personal Dim do GTNH com fidelidade 1:1, sobretudo a
  personalização e o mundo plano com ruas.
- Verificado o código do PersonalSpace `a292401e0a067e37e7e02abee8bd48b58a6e1571`
  (Minecraft 1.7.10; licença LGPL-3.0). Os três presets upstream padrão não incluem
  ruas: o gerador de `ROAD` está nas opções estendidas de limites/espaços.
- `docs/roadmap/personal-space-port.md` registra comportamento, algoritmo das ruas,
  preservação de portais/destinos e gates de fidelidade. O projeto GTNA é Forge 1.20.1;
  registro dinâmico/persistente de dimensões ainda requer prova técnica.
- Alterações nesta etapa: documentação somente. Nenhum código, asset, receita ou
  dimensão Personal Space adicionado; nenhum cliente/manual QA executado. Gate
  `spotlessCheck compileJava runUnitTests runGameTestServer runData --offline` PASS,
  190/190 GameTests, datagen `written: 0`; log
  `build/gtna-personal-space-audit-gate.log`. Sem commit/push.


## G-0172 — 2026-10-06 — Auditoria de fabricação/progressão EOH

- Pedido do autor: auditar conteúdo a portar, método e sugestões de substituições/nomes.
- Novo docs/roadmap/eye-of-harmony-progression-port-audit.md: estado atual, dependências
  do controller/BEC/campos/casings/Astral Array, nove tiers, materiais, funções de máquinas,
  propostas de nomes e substituições, mineração espacial versus veias físicas, ordem de
  entregas e critérios de aquisição/validação. Links nos índices PT/EN/ES.
- Referência GTNH a3e1e11241a814c9fa0dd0973d5699548428f689 conferida por fontes raw
  e loaders. BECMetaMaterialRecipes, CondensateType, NaniteTier, BECAssembler/Generator,
  BECFactoryNetwork, Godforge/NanoForge também lidos para delimitar dependências.
- Novo eye-of-harmony-progression-inventory.json: extração de símbolos diretos e loader
  de metamateriais, presença declarada em GTCEu/GTNA, tiers de nanites, materiais de campo,
  hashes das fontes. Presença não certifica obtenção/forma. Sem fechamento recursivo
  completo dos mods upstream; isso fica explícito, não é afirmada paridade da cadeia.
- Fontes locais GTCEu/GTLCore/GTOCore/GTNL conferidas nos commits registrados na auditoria.
  GTL é candidato moderno, não prova de cadeia fiel. Fuel rods Infinity/Cosmic não
  equivalem a metais registrados. Programas Ad Astra Void Miner não equivalem a worldgen.
- Recomendação proposta, não aprovada/implementada: cadeia pré-EOH para Crude sem produtos
  exclusivos da própria máquina; síntese estelar e upgrades depois. Manter IDs e identidade
  de SpaceTime/anãs/Universium; não colocar esses produtos sintéticos em veias naturais.
- Gate obrigatório PASS: spotlessCheck compileJava runUnitTests runGameTestServer runData
  --offline, 30 classes / 190 GameTests, written: 0;
  build/gtna-eoh-progression-audit-gate.log. Confirma estado do checkout; nenhuma cadeia
  de fabricação nova existe para validar. JSON e links locais conferidos.
- Somente documentos alterados neste trabalho; sem commit/push/publicação ou reinício
  do cliente. Nomes, custos, rota BEC/adaptada e máquinas novas permanecem propostas.

## G-0173 — 2026-10-06 — Personal Space: terreno e criação dinâmica inicial

- A partir de PersonalSpace GTNH `a292401e0a067e37e7e02abee8bd48b58a6e1571`,
  `PersonalSpaceTerrain` reproduz lotes, ruas, listras, cruzamentos e centro em
  coordenadas positivas/negativas. `PersonalSpaceChunkGenerator` gera camadas,
  superfície e plataforma inicial; não implementa ainda todas as opções originais.
- `PersonalSpaceDirectory` guarda IDs desde 180 e opções no `SavedData` do Overworld.
  `PersonalSpaceWorlds` cria/carrega `gtna:personal_space/pdim_<id>` via Infiniverse
  1.0.0.5 (MIT; dependência obrigatória Forge 1.20.1). Fonte e licença em
  `THIRD_PARTY_NOTICES.md`; a arquitetura de chunk generator moderna foi conferida
  no PersonalSpace Unofficial `dab12d35325aef2c65b79e9a79ef0025f1274fa4`.
- Comandos de operador provisórios `/gtna personalspace create void|flat|roads`,
  `list`, `tp <id>` e `return` para testar a base sem portal; `mining` acrescentado
  após o gate abaixo. Nenhuma receita, portal ou GUI foi adicionada.
- Gate PASS: `spotlessCheck compileJava runUnitTests runGameTestServer runData --offline`,
  192/192 GameTests, datagen `written: 1` (textos de comando);
  `build/gtna-personalspace-worlds-gate.log`. Um GameTest criou dimensão real,
  leu um chunk de rua e fez roundtrip do catálogo. Reinício real, teleporte no
  cliente e worldgen editável permanecem sem validação. Sem commit/push/publicação.

## G-0174 — 2026-10-06 — Personal Space: portal inicial e vínculo no item

- `gtna:personal_space_portal` registrado como bloco, item e block entity. Primeiro
  uso no Overworld cria uma dimensão; agachar antes da criação alterna entre Void,
  Flat, Mining e Roads. Uso seguinte entra no mundo já vinculado. Um portal de
  retorno é criado na plataforma e atualizado para o local atual do portal origem.
- Drop do bloco usa o `BlockEntityTag` para guardar ID e destino de retorno. Item
  mostra ID e `Source: PersonalSpace (GTNH)`. Aparência provisória usa textura
  vanilla Crying Obsidian, sem asset upstream copiado. Sem receita, como original.
- Posição de retorno ocupada causa recusa de entrada em vez de teleporte sem saída.
  Ainda faltam editor completo, ajustes visuais, worldgen editável após comando,
  restrições de blocos, comandos GTNH completos e teste no cliente/reinício real.
- Gate completo do estado atual: `spotlessCheck`, `compileJava`, `runUnitTests`,
  `runGameTestServer` (193/193) e `runData` PASS em
  `build/gtna-personalspace-final-validation.log`. O teste cobre criação de dimensão,
  rua, NBT e drop real do portal. `runData` escreveu 1 arquivo. Ainda é necessário
  testar portal, retorno e persistência no cliente após reinício. Sem commit/push/publicação.

## G-0175 — 2026-10-06 — Personal Space: DimensionConfig, editor GUI, portal e céu

- `PersonalSpaceSettings` virou port de `DimensionConfig`: cor do céu, estrelas, ciclo SUN/MOON/CYCLE,
  nuvens, clima, sky type, bioma, árvores, vegetação, trava de worldgen, camadas, limites, ruas, S e
  centro; string completa `camadas|B,..|G,..|S,..|C,..` com aplicação atômica; nomes/metas 1.7.10
  (`minecraft:wool:14`, `grass`, `double_stone_slab`...) convertidos por `PersonalSpaceBlocks`.
- `PersonalSpaceConfig` (`config/gtna/personal_space.json`): presets (3 originais + ruas GTNA),
  allow-lists com sintaxe original de meta, `#tags`, biomas, firstDimensionId, block event checks.
- Gerador reescrito seguindo `PersonalChunkProvider`: void com chão 128, plataforma de laje dupla,
  modo S (todas as superfícies expostas), sem bloco A não gera rua, sem mobs naturais, árvores/vegetação.
  Lê a configuração viva: alteração liberada só afeta chunks novos (GameTest confirma).
- Tipo de dimensão `gtna:personal_space` (y 0..255, cama não funciona). Infiniverse grava as dimensões
  no level.dat: codec tolerante e setup em `LevelEvent.Load`; pré-carga no ServerStarted.
- Tempo e clima próprios por dimensão (`PersonalSpaceLevelData` via accessor mixins), persistidos;
  SUN/MOON fixam meio-dia/meia-noite. Cliente: efeitos `gtna:personal_space` (nuvens 256/sem nuvens,
  névoa) e mixin em `ClientLevel` (cor do céu, estrelas, brilho do sol) como o original.
- Portal: forma 0.75, obsidiana/portal/mesa de encantamento + livro animado (BER), facing, alvo
  (8, chão+1, 8), teleporte para frente do alvo, relink com proteção, item com DIMn: x,y,z, brilho,
  não despawna/indestrutível, proximidade 2 blocos, picareta obrigatória. Editor GUI completo
  (páginas 1/2, camadas, presets, dropdowns com busca, preview) validado no servidor (spawn protection,
  BreakEvent, camadas e blocos de decoração permitidos, bioma).
- `/pspace ls|where|tpx|give-portal|allow-worldgen-change|reload-config`; comandos `/gtna personalspace`
  provisórios removidos. widgets.png copiado (LGPL-3.0, hash em THIRD_PARTY_NOTICES).
- Gate `spotlessCheck compileJava runUnitTests runGameTestServer runData --offline` PASS, 195/195
  GameTests, `written: 0`; `build/gtna-personalspace-editor-gate.log`. Lacunas: céus GalaxySpace/Botania
  não portados; GUI, céu, clima e reinício real não testados no cliente. Sem commit/push.

## G-0176 — 2026-10-06 — Personal Space: correções do QA do autor

- Travada de minutos ao criar/teleportar e "mundo vazio": loop infinito em
  `PersonalSpacePortalItem.createEntity` (o Forge substituía de novo o ItemEntity comum). Agora retorna
  subclasse. Thread dump confirmou a causa; o gerador também passou a calcular por coluna.
- Preset 4 segue o `personalspace.cfg` do GTNH (concreto branco y=53 e y=63, lotes 2x2, ruas 1),
  com equivalentes vanilla para blocos etfuturum/chisel/ExtraUtilities.
- Allow-lists padrão ampliadas (blocos de construção vanilla, `xtonesreworked:*`, `xtones:*`,
  `factory_blocks:*`); regra `modid:*` aceita no config.
- Plataforma de chegada reduzida de 9x9 para 5x5 a pedido do autor (diferença intencional do original).
- Autor testou no cliente: criação, teleporte e preset 4 OK. Gate PASS 195/195,
  `build/gtna-personalspace-platform-gate.log`. Sem commit/push.

## G-0177 — 2026-10-06 — EOH: custo inicial volta a 4^k (GTNH)

- Pedido do autor: com a rede wireless absorvendo toda a geração do Artificial Star, restaurar o
  multiplicador original do GTNH. `EyeOfHarmonyMath.startupDebit` = startupEU × 4^circuito
  (circuito 24 = 4^24 ≈ 2,8×10^14×), substituindo (k+1)^2. Combustíveis do Artificial Star mantêm 16×
  EU/t (escolha do autor). Manual EN/PT, teste unitário e GameTests atualizados; removida a asserção
  de que quatro barras Infinity pagam um ciclo de circuito 24 (deixa de ser verdade).
- Gate PASS 195/195, `build/gtna-eoh-4k-gate.log`, datagen written: 1. Sem commit/push.

## G-0178 — 2026-10-06 — Forge of Gods: auditoria e fases 1–3 (núcleo, estrutura, controlador)

- Auditoria em `docs/roadmap/forge-of-gods-port-audit.md` (GT5-Unofficial a3e1e112; GTO como base visual).
- Fase 1: `api/machine/feature/godforge` — 31 upgrades (pré-requisitos, custos, splits, posições da árvore),
  armazenamento de custos, `GodforgeData`, `GodforgeMath` (combustível, calor, paralelos, velocidade,
  desconto, voltagem, milestones, inversão, shards) com a mesma ordem de operações do GTNH.
  `GodforgeMathTest` (unitário) confere fórmulas e regras da árvore.
- Fase 2: estrutura convertida das strings do GTNH por `tools/convert_godforge_structure.py` em três `.mbs`
  alinhados (principal com anel 1; anéis 2 e 3); contagens batem com o tooltip do GTNH. 8 casings + lente
  com texturas GTOCore (CC BY-NC-SA). Materiais DTR, MHDCSM, Magmatter, QGP e Graviton Shard registrados.
- Fase 3 (base): `ForgeOfGodsMachine` — combustível de partida (bloco de Neutronium, fallback do GTNH sem
  Avaritia), bateria, drenagem de DTR/Raw Star Matter/MHDCSM, anéis 2/3 checados com CD/END, compra/
  reembolso de upgrades, ejeção de shards, coleta de módulos nas vagas J, NBT com chaves do GTNH.
- GameTests: estrutura do preview forma (7561 blocos), estrela acende com o combustível e apaga sem fluido.
  Receita do controlador omitida e documentada (depende da fase de progressão).
- Gate PASS 197/197, `build/gtna-godforge-phase2-gate.log`. Pendentes: módulos, render, GUI, receitas.
  Sem commit.

## G-0179 — 2026-10-06 — Forge of Gods: fase 4 (módulos)

- `GodforgeModuleMachine` (estrutura 7x7x13 do GTNH, OC do GT5, débito wireless em `beforeWorking`):
  Smelting (BLAST + FURNACE), Molten (BLAST com saídas fundidas), Plasma (tabela de tier/multistep).
- `GodforgeExoticModuleMachine`: sorteio ponderado de 7 entradas, plasmas exatos, saída QGP ou Magmatter
  (Time/Space), modo trocado com chave de fenda e liberado pelo upgrade Effortless Existence.
- Receitas de plasma do Godforge em `GTNAGodforgeRecipes`; GameTests de forma, estrela, anéis, smelting e plasma.
- Gate completo: 199/199 GameTests. Pendente: GameTest do módulo exótico, render da estrela (fase 5), GUI (fase 6).

## G-0180 — 2026-10-06 — Forge of Gods: fases 5 e 6 (render, anéis, GUI, sons, idiomas)

- Render (GTNH `RenderForgeOfGods`): estrela 122 blocos atrás do controlador com as três camadas
  (`star` GLSL portado para core shader 1.20.1, cor YIQ + gamma, rotação por camada), anéis girando em
  torno do eixo do feixe (malha dos blocos reais em `VertexBuffer`) e feixe suave + intenso
  (`gorgeBeam` com geometria na CPU, meia-casca voltada à câmera). Ciclo de cores dos presets e cores
  customizadas (`GodforgeStarColor`, formato `StarColorV1` do GTNH).
- Anéis "entram no controlador": com a estrela acesa os blocos dos anéis viram ar e o padrão checado é
  o eixo + anéis vazios (`god_forge_active`, `*_ring_N_air`); ao apagar, os anéis são recolocados
  (`GodforgeRings`). GameTest confere 6044 blocos removidos, forma vazia e forma cheia depois.
- GUI (GTNH `MTEForgeOfGodsGui` + painéis) como abas do Fancy UI: tela principal, árvore de upgrades
  (31 nós, conectores rotacionados, detalhe com corpo/lore/custos, construir/reembolsar, segredo),
  marcos (barras normal/invertida, próximos limites, fragmentos), combustível, bateria, cosméticos
  (lista de cores, giro, tamanho, animações, editor e importação), estatísticas com prévia de fator,
  inserção manual de custos extras (16 slots), info geral e agradecimentos. Módulos: tela de status e
  estatísticas, aba de tensão/paralelo (TBF), botões Magmatéria e "reiniciar receita" no exótico.
- Idiomas: 340 textos EN extraídos do GTNH (`tools/extract_godforge_lang.py` → `GodforgeLang`) e
  tradução PT-BR (`tools/merge_godforge_lang_pt_br.py`). Som de loop do GTNH tocando no controlador.
- Gate: GameTests 200/200 (EOH `normal_speed_circuit` falhou uma vez por corrida no check assíncrono
  do GTCEu e passou na nova execução). Fase 7 (receitas/custos extras) aguarda decisão do autor:
  `docs/roadmap/forge-of-gods-recipes-proposal.md`.

## G-0181 — 2026-10-06 — Forge of Gods: fase 7 (receitas e custos extras, provisório)

- `GTNAGodforgeProgression`: assembly lines com pesquisa do controlador, 4 casings, lente, modulador,
  conduto e 4 módulos (estrutura/quantidades/tiers do GTNH) e custos extras dos 7 upgrades do GTNH.
  Materiais ausentes usam substitutos documentados em `docs/roadmap/forge-of-gods-recipes-proposal.md`
  (escolha provisória feita sem o autor presente — revisar: opção 1/3 trocam só os substitutos).
- GameTest novo confere receitas e custos; teste da estrutura agora carrega os chunks que ela ocupa.
- Gate: spotless, unit, runData e 201/201 GameTests.
## G-0182 — 2026-10-07 — Forge of Gods: decisão sobre materiais

- Autor decidiu portar a cadeia completa do GTNH (máquinas e intermediários) em sessão futura; plano em
  `docs/roadmap/forge-of-gods-recipes-proposal.md`. Substitutos seguem ativos; agora o autor testa o
  multibloco em `runClient`.

## G-0183 — 2026-10-07 — Receitas de construção EOH e sprites dos materiais

- Rota provisória do EOH em `GTNAEyeOfHarmonyProgression`: 32 receitas de Assembly Line com pesquisa
  para três casings, 27 campos (tiers 0–8), controlador e Planet Block Overworld. O primeiro tier usa
  componentes GTCEu anteriores ao EOH; os tiers seguintes consomem quatro campos do tier anterior.
  É uma adaptação GTNA, não um port das receitas BEC do GTNH. GameTest novo confirma as receitas
  carregadas, inclusive início/fim das três famílias.
- O conjunto de PNGs de materiais foi trocado pelas versões Modernity-GTNH para SpaceTime,
  White Dwarf Matter, Universium, MHDCSM, Magmatter e Graviton Shard; conjuntos Infinity e Eternity
  ficam preparados, mas os materiais ainda não são registrados. O conjunto de Black Dwarf Matter
  veio de GTOCore. Modelos GTCEu de itens e blocos e camadas secundárias transparentes foram
  gerados; um verificador local encontrou zero referências a texturas ausentes. Origem/licença em
  `THIRD_PARTY_NOTICES.md`.
- Gate `./gradlew spotlessCheck compileJava runUnitTests runGameTestServer runData --offline` PASS:
  202/202 GameTests, datagen `written: 0`; log `build/gtna-eoh-godforge-recipes-textures-gate.log`.
  Sem commit/push. QA visual no cliente ainda pendente. A progressão completa de Survival do
  Godforge continua pendente: SpaceTime/White/Black Dwarf Matter/Universium têm autogen de receitas
  desativado, e DTR não tem fonte de aquisição; as receitas provisórias do Forge podem ser
  conferidas com ingredientes fornecidos para teste, mas ainda não fecham a cadeia inteira.

- 2026-10-07 (Forge of Gods): `/gtna godforge shards <n>` (OP 2; ajusta gravitonShardsSpent, porque o disponível é recalculado a cada tick). Módulos Smelting/Molten/Plasma aceitam 1 Thread Hatch via GTNAMultipleRecipesLogic, com o modificador GTNH aplicado em cada thread. Um hatch acima do tier liberado forma, mas não adiciona threads (base UHV, GPCI→UIV, CD→OpV, END→MAX). O Exotic continua com uma thread só. O controller usa RotationState.ALL (para cima/para baixo). Gate verde: 202/202. Sem commit.

## G-0184 — 2026-10-07 — ME Export Buffer e relatório de cobertura de receitas

- `GTNAMEExportBufferPartMachine` (`gtna:me_export_buffer`, LuV, toggle `meExportBuffer`): EXPORT_ITEMS +
  EXPORT_FLUIDS numa parte; saídas de receita vão a `KeyStorage` com contagem long (fluido não limitado a
  int como no ME Output Hatch do GTCEu) e são enviadas à rede ME no intervalo AE2; ao remover, devolve o
  buffer à rede. Não aceita extração; screwdriver não troca IO. Receita de Assembler LuV: hull LuV, ME
  Output Bus, ME Output Hatch, 256k Crafting Storage, 2 esteiras e 2 bombas LuV, 2 circuitos LuV,
  576 mB de solda. Escrito sobre GTCEu (LGPL), sem código do GTMThings (licença pendente).
- `GTNARecipeCoverageGameTests` grava `gtna-recipe-coverage.txt` no diretório do servidor de teste:
  1ª execução — 353 blocos/máquinas, 96 itens (maioria baldes), 79 itens de material e 10 fluidos sem
  receita. Lacunas reais: hatches wireless energy/dynamo (quase todos os tiers), hatches
  accelerate/output boost/infinite input (todos) e overclock/parallel/thread acima de UV,
  chemical_plant, component_assembly_line, thermal_power_pump, matrix_module_i–iv, nexus_capacitor
  UEV+, nexus_hypercore_casing, planet blocks Nether/End, hypogen_coil, graviton flow modulators,
  restraint_device, singularity_reinforced_stellar_shielding_casing, personal_space_portal,
  me_pattern_buffer_proxy; formas sólidas de SpaceTime/WDM/BDM/Universium/MHDCSM/Magmatter/Graviton.
- Gate `spotlessCheck compileJava runUnitTests runGameTestServer runData --offline` PASS, 204/204.
  Sem commit. QA no cliente do buffer (GUI, rede ME, quebrar com buffer cheio) pendente.

## G-0185 — 2026-10-07 — Veios de minério nos planetas do Ad Astra

- Decisão do autor: veios reais nos planetas (base para o Void Miner e as simulações do EOH); Fusion
  Mk4/Mk5 do GTNH a ser substituído pelo Advanced Fusion Reactor do GTO com módulos (em avaliação).
- `common/data/worldgen/GTNAPlanetOres`: só ativo com Ad Astra. Prefixos de minério `moon_stone`,
  `mars_stone`, `venus_stone`, `mercury_stone`, `glacio_stone` (blocos `gtceu:<planeta>_stone_<mat>_ore`),
  cada um limitado aos materiais dos veios daquele planeta (~140 blocos no total). Camada de worldgen
  `gtna_planet_stone` (tag `gtna:planet_ore_replaceables`, entradas opcionais) e 33 veios `gtna:planet/*`
  traduzidos do GTOCore `GTOOres` para a API GTCEu 7.5.3. Excluídos os veios só-GTO (Celestine, Desh,
  Calorite, Ostrum, Zircon); Desh/Ostrum/Calorite continuam vindo do worldgen do Ad Astra.
- Borax recebe OreProperty (só com Ad Astra), porque é o minério principal do veio de borax de Marte e
  Vênus e não é minério no GTCEu 7.5.3; isso também cria blocos de minério de Borax nas pedras comuns,
  sem veio novo no Overworld. Veio de Scheelite do End (GTIA-D-164) intocado.
- Lang EN gerado e PT manual (`tagprefix.<planeta>_stone`). GameTest `planetVeinsHaveOreBlocksOnTheirStones`:
  cada planeta tem veio, cada material de veio tem bloco na pedra do planeta, pedras na tag e
  resolução pedra → prefixo. Contagem: Lua 7, Marte 12, Vênus 8, Mercúrio 6, Glacio 10.
- Gate `spotlessCheck compileJava runUnitTests runGameTestServer runData --offline` PASS, 205/205.
  Sem commit. Pendente: QA no mundo (gerar chunks novos em cada planeta, conferir textura, drops, ore
  processing, indicadores), balanceamento conforme GTIA-D-150/158/164 e catálogo EOH lendo esses veios.

## G-0186 — 2026-10-07 — Simulações planetárias do Eye of Harmony e KubeJS

- `EyeOfHarmonyPrograms`: programas por item seletor → dimensão + tier de foguete GTNH (Overworld/Nether/End 0,
  Lua 1, Marte 2, Vênus e Mercúrio 4; Glacio 5, escolha GTNA por não existir no GTNH). `EyeOfHarmonyOverworld`
  virou `EyeOfHarmonyCatalog` e lê os veios GTCEu de qualquer dimensão; `Program.planet(tier, plasma)` aplica
  18000 s × 1,4^tier, gás 10^9 × (tier+1), chance 1 − 0,05·tier, retorno 0,6 + tier/10, tier de receita
  max(tier,1)−1 (compressão mínima e SpaceTime em falha), plasmas e Raw Star Matter × (casing+1) e fluido
  especial WDM/BDM/Universium do GTNH. Overworld idêntico ao anterior.
- Controlador: aceita qualquer item com programa; bloqueia com `compression` se o campo for menor que o exigido;
  gás exigido por programa; orçamento com slot vazio continua usando o Overworld. Planeta sincronizado para o
  renderer, que mostra a esfera do planeta do slot (`obj/planet_<nome>`).
- Planet Blocks Lua/Marte/Vênus/Mercúrio/Glacio; texturas Modernity Mo/Ma/Ve/Me copiadas; Glacio aguarda a
  arte do autor (`eye_of_harmony/planet/glacio_{top,bottom,front,back,left,right}.png`, 16×16), até lá aparece
  sem textura. Receitas de Assembly Line para Nether, End e os 5 planetas (pesquisa do planeta anterior,
  64 blocos da superfície, Planet Data Chip não consumido, custo crescente por tier). EMI/JEI: uma página por
  programa.
- KubeJS: `GTNAStartupEvents.eyeOfHarmony(event => event.add(item, dimensão, tier[, pó]) / remove(item))`,
  postado no common setup; exemplo em `examples/kubejs/startup_scripts/gtna_eye_of_harmony_programs.js`.
- GameTests novos: parâmetros GTNH de todos os programas, Lua com Ilmenite, Marte com Tungstate, registro e
  remoção de programa custom. Gate `spotlessCheck compileJava runUnitTests runGameTestServer runData --offline`
  PASS 207/207. Sem commit. QA no cliente pendente (EMI por planeta, render, operação real em Marte+).

## G-0187 — 2026-10-07 — Receitas das hatches wireless, Nexus Capacitors e hatches de módulo

- Textura de Glacio entregue pelo autor (6 faces 16×16); arte do autor, não alterada.
- Bug corrigido: as receitas de laser 256A/1024A/4096A geravam as hatches wireless de 64A/256A/1024A
  (índice de amperagem deslocado). `registerWirelessHatchRecipes` reescrito: LV–MAX, entrada e saída; cada
  amperagem com par GTCEu (energy 1A/4A/16A, substation 64A, laser 256A/1024A/4096A) sai no Assembler a partir
  dele; 16384A+ (e lacunas de tier sem hatch GTCEu) vêm de 4 hatches wireless da amperagem anterior, na
  Assembly Line com pesquisa a partir de 16384A. Solda Indalloy 140 acima de LuV; MAX usa componentes OpV.
- Nexus Capacitor UEV–MAX (Assembly Line, cadeia a partir do UHV); UXV pede White Dwarf Matter, OpV Black
  Dwarf Matter, MAX Black Dwarf Matter + Raw Star Matter (saídas do EOH).
- Accelerate/overclock UHV–MAX, thread UEV–MAX e parallel UHV–OpV: Assembly Line consumindo o tier anterior.
- Cobertura (`run/gtna-recipe-coverage.txt`): 88 blocos sem receita, dos quais 70 são output boost e
  infinite input (receitas restritas por `RestrictedItemsEnabled`, intencional). Restam: FOG (graviton flow
  modulators, singularity casing, hypogen coil, restraint device — aguardam a cadeia GTNH), matrix modules,
  nexus hypercore casing, ME pattern buffer proxy, chemical plant, component assembly line, thermal power
  pump, personal space portal, infinite steam input bus, output boost steam bus, duration tester (dev).
- Gate PASS 207/207. Sem commit. Balanceamento das quantidades ainda não jogado.

## G-0188 — 2026-10-07 — Trava de tier das peças restritas e receitas de controladores

- Decisão do autor: output boost e infinite input continuam atrás da condição de itens restritos (Journey),
  com receita cara para o tier, e só funcionam em receitas do tier da peça.
- `GTNASpecialPartUtil.matchesTier`: peça com tier (ITieredMachine) só age se o tier dela for igual ao tier
  da receita antes do overclock e dos paralelos (`RecipeHelper.getPreOCRecipeEuTier`; receitas ULV contam
  como LV). Peças a vapor (sem tier) continuam agindo sempre. Tooltip `gtna.machine.restricted_part.tier_lock`.
  GameTest `restrictedPartsOnlyActOnTheirTier`.
- Receitas restritas novas LV–MAX (Assembler): peça base do tier, 4+4 componentes do tier, 4 field generators,
  8 circuitos do tier + 2 do tier seguinte, solda 576·tier mB, 1200+400·tier ticks. Ainda condicionais.
- Chemical Plant: receita GTO 1:1 (o motivo antigo de omissão estava errado: Watertight Steel existe no GCYM).
  Component Assembly Line: receita GTO adaptada (Assembly Line GTCEu ×16 no lugar do Advanced Assembly Line,
  Advanced Assembly Line Unit do GTNA, Tritanium no lugar de Mithril), pesquisa do Component Assembler; o
  GameTest que exigia a omissão foi trocado por um que exige a receita. Thermal Power Pump: shaped do GTO com
  Bronze Reinforced Wood no lugar do Reinforced Wood Casing. ME Pattern Buffer Proxy do GTNA: receita GTCEu
  pesquisada a partir do buffer GTNA.
- Sem receita, por decisão pendente do autor: matrix_module_i–iv e nexus_hypercore_casing (nenhuma estrutura
  usa), personal_space_portal (upstream sem receita). FOG aguarda a cadeia GTNH. duration_tester é dev.
- Gate PASS 208/208. Sem commit.

## G-0189 — 2026-10-07 — Remoções, portal e materiais da cadeia do Forge of Gods

- Removidos (pedido do autor): `matrix_module_i`–`iv` e `nexus_hypercore_casing` (nenhuma estrutura usava;
  texturas `wireless_energy_unit/*` mantidas porque os Nexus Capacitors usam). Mundos com esses blocos os perdem.
- Personal Space Portal: Assembler EV (4 frames de Titanium, 8 Crying Obsidian, 2 field generators, 2 emitters,
  2 sensors EV, 8 placas de Ender Eye, 4 circuitos EV, solda). Upstream não tem receita; tier escolhido pela
  ideia de dimensão-fábrica do GT:IA (D-238 J).
- Forge of Gods: rastreio da cadeia GTNH (master atual do GT5-Unofficial) documentado em
  `docs/roadmap/forge-of-gods-material-chain.md` (multiblocos, materiais, intermediários, itens, ordem).
  `GodforgeChainMaterials`: Creon, Mellion, Transcendent Metal, Six-Phased Copper, Tengam (raw/purified/attuned),
  Infinity, Eternity, Ichorium, Shijima, Churitsu, Superconductor Base UIV/UMV + fios supercondutores UIV/UXV,
  Hypogen, Rhugnor, Celestial Tungsten, Astral Titanium, Chromatic Glass, Nitinol 60, Advanced Nitinol, Quantum,
  Dragonblood, Metastable Oganesson, Shirabon, Atomic Separation Catalyst, Dilithium, Orundum, Magneto Resonatic,
  DTCC/DTPC/DTRC/DTEC/DTSC e Excited, Phonon Medium/Crystal Solution, Mutated Living Solder, Primordial Matter,
  Stabilised Baryonic Matter, Heavy Radox. Nomes, cores e fórmulas do GTNH; sem temperatura de EBF e sem fundição
  de pó (nenhum atalho até a receita própria). Icon sets Infinity/Eternity ligados às texturas já importadas;
  os demais usam SHINY/METALLIC/DIAMOND por enquanto (texturas próprias pendentes). Nome PT dos materiais pendente.
- Gate PASS 208/208. Sem commit. Os substitutos em `GTNAGodforgeProgression` continuam até as etapas 2–8.

## G-0190 — 2026-10-07 — Forge of Gods, etapa 2: plasmas e catalisadores DT

- `GTNAGodforgeChainRecipes` (novo, registrado antes de `GTNAGodforgeProgression`):
  - Fusão GTNH: Calcium, Sulfur, Zinc, Niobium, Silver, Bismuth, Radon, Thorium (Thorium com início de 1 bi EU,
    como o GTO, no lugar de 6 bi). GTO: Titanium, Boron (o GTNH não tem fusão para eles). Lead: o GTNH usa
    Telúrio, que não tem forma no GTCEu; trocado por Ouro + Lítio (79+3 prótons), 1 bi EU.
  - Mixer GTNH 1:1 (5 fluidos, circuitos 9–13, 50.000 ticks, ZPM→UIV): DTCC, DTPC, DTRC, DTEC, DTSC. O DTSC pede
    plasma de Naquadria (módulo de plasma do Forge) e Raw Star Matter (EOH): continua pós-Forge, como no GTNH.
  - Laser Engraver em cleanroom: os cinco Excited, 50.000 ticks cada.
- `GTNARecipeType.init`: `MIXER_RECIPES.setMaxIOSize(6,1,6,1)` e `LASER_ENGRAVER_RECIPES.setMaxIOSize(2,1,1,1)`,
  como o GTO; mixers e gravadores singleblock ganham os tanques extras.
- GameTest `stageTwoCatalystChainLoads`: as 21 receitas carregam e nenhum fluido ficou vazio.
- Gate PASS 209/209. Sem commit. QA no cliente: UI do Mixer/Laser Engraver com os tanques novos.

## G-0191 — 2026-10-07 — Forge of Gods, etapa 3: Advanced Fusion Reactor (GTO)

- `gtna:advanced_fusion_reactor` ("Advanced Fusion Reactor MK-I"), `AdvancedFusionReactorMachine` sobre
  `WorkableElectricMultipleRecipesMachine`: roda `FUSION_RECIPES` com Parallel/Accelerate (base) e
  Thread/Overclock (extensão cross-recipe). Tier = LuV + extensões formadas em ordem (até UEV). Buffer interno
  `entradas × 2^(tier−LuV) × 10⁷` EU (16 entradas em UEV = 2,56 bi), preenchido pelas hatches; regra de calor do
  GTCEu (`eu_to_start` precisa caber no buffer; calor esfria 10.000/tick parado). Aceita 16 laser + 16 energia
  (inclusive wireless); Control Hatch do GTO não portado. Sem renderer de anel (GTO tem `AdvancedFusionReactorRenderer`).
- Extensões via `GTNASubPatterns` (kuangbiao2..5 + crossrecipe). `IGTNAModuleHost.gtna$formedModuleMask` novo
  (mixin registra quais sub-patterns formaram), usado para contar extensões consecutivas.
- 34 blocos GTO portados (`GTNABlocks`, texturas GTO), helpers novos `createColumnCasingBlock` e
  `createHermeticCasingBlock`. Materiais: Platinum-Manganese-Antimony Heusler Alloy e Odyssey Nano Superalloy
  (frames; EBF pela composição). Amprosium do GTO = Neutronium.
- Receitas (`GTNAAdvancedFusionRecipes`): controlador por Assembly Line (Precision Assembler do GTO não existe;
  circuitos HUI → circuitos do tier); 34 blocos — fiéis quando todos os insumos existem, adaptados no resto.
- Fusões novas: Metastable Oganesson (Enriched Naquadah + Radônio no lugar de Oganesson, 1 bi EU), Rhugnor
  (Infinity + Quantum, 2 bi; e QGP + Quantum), Hypogen (Dragonblood + Rhugnor, 1,2 bi). Infinity, Quantum e
  Dragonblood ainda sem rota (etapa 7).
- GameTests: `advancedFusionAndChainRecipesHaveNoEmptyIngredient` (≥50 receitas, nenhum insumo vazio) e
  `advancedFusionBaseFormsAtLuV` (estrutura base forma pelo preview; tier LuV; buffer UEV 2,56 bi).
- Gate PASS 211/211. Sem commit. QA no cliente: montar base e extensões, conferir tier/buffer na UI, rodar uma
  fusão de 1 bi EU, texturas dos 34 blocos e nomes PT.

## G-0192 — 2026-10-07 — Forge of Gods, etapa 4: Dimensionally Transcendent Plasma Forge

- `gtna:dimensionally_transcendent_plasma_forge` (`PlasmaForgeMachine` sobre `CoilWorkableElectricMultiblockMachine`):
  estrutura convertida de `MTEPlasmaForge` (`tools/convert_dtpf_structure.py` → `pattern/dtpf_gtnh.mbs`; 2121
  Transcendent, 1273 Injection com mínimo 1250, 2112 bobinas, 120 Bridge). Até 2 energy hatches ou 1 laser.
  Modificadores: `catalystDiscount` (tempo de receita acumulado; 8 h → −50% nos Excited DTxC; parada drena 100/tick)
  + `ebfOverclock` do GTCEu. Convergence (Transdimensional Alignment Matrix) não portada. `dtpf.mbs` antigo segue sem uso.
- Tipo `gtna:plasma_forge` (9/2 itens, 6/2 fluidos, `ebf_temp`). Bobina Eternal 13.500 K (tier 9); Dimensional Bridge
  Casing com id novo `plasma_forge_bridge_casing` (o id `dimensional_bridge_casing` é remapeamento legado do EOH).
  Item `laser_lens_special`.
- Receitas: controlador, Bridge Casing, bobinas Hypogen e Eternal (Assembly Line com pesquisa); lente pela Assembly
  Line (UV, sem DTPF) e pelo DTPF (Excited DTRC + Duranium → lente + DTR); DTR (Excited DTCC + Neutronium, bootstrap
  GTNA); Six-Phased Copper (receita GT5U, Singularity do Avaritia → 8 Gravi Stars); Laser Engraver com a lente para
  Celestial Tungsten, Astral Titanium, Chromatic Glass e Advanced Nitinol; Nitinol 60 no Alloy Blast Smelter; EBF
  pó → lingote dos quatro metais GT++. Substituições: Mutated Living Solder → Indalloy 140 (sem rota ainda),
  Oganesson → Radon, Exothermic Hearth → Mega Blast Furnace.
- GameTest `plasmaForgeFormsAndDiscountsCatalysts`. Gate PASS 212/212. Sem commit. QA cliente: montar o DTPF,
  conferir desconto na UI e as texturas novas.


## G-0193 — 2026-10-07 — Forge of Gods, etapa 5: Transcendent Plasma Mixer

- `gtna:transcendent_plasma_mixer` (`TranscendentPlasmaMixerMachine`): estrutura do `MTETranscendentPlasmaMixer`;
  energia só da rede wireless do dono, custo total pago no início (`multiplicador × tpm_eut × duração` por paralelo);
  paralelos pelo Parallel Hatch, limitados por insumos e saldo; sem overclock. Receitas sem EU (dados `tpm_eut`,
  `tpm_duration`, `eu_multiplier`).
- Receitas: controlador (Assembly Line UIV com pesquisa do DTPF; energy tunnels → energy hatches UIV, ProtoHalkonite
  → Six-Phased Copper); Excited DTCC..DTSC; Primordial Matter; plasma de Creon (Fermium → Americium); Harmonic
  Compound (EBF 14.000 K) e separação no Vacuum Freezer → Mellion + Creon; Mellion no Mixer (sem circuito, limite de 6
  itens; Fiery Steel → Blaze, Firestone → Redstone); Orundum (Tiberium → Naquadria); Atomic Separation Catalyst.
- Item `harmonic_compound` usa o modelo do Quantum Eye do GTCEu (sem textura no Modernity) — autor decide a arte.
- GameTests `excitedDtscCostMatchesGtnh`, `plasmaMixerForms`. Gate PASS 214/214. Sem commit.
- Aviso pré-existente (G-0191): `metastable_oganesson` conflita com a fusão GTCEu enriched naquadah + radon.
- QA cliente: montar o TPM, rodar com saldo wireless, conferir custo na receita (JEI/EMI) e nomes PT.

## G-0194 — 2026-10-07 — Forge of Gods, etapa 6: Phonon, Tengam, supercondutores, Shirabon

- Receitas em `GTNAGodforgeChainRecipes.phonon`: Raw Tengam (Mixer de terras raras — sem minério ainda) →
  Purified (Electromagnetic Separator) → lingote (EBF 10.800 K) → Attuned (Polarizer); Dilithium (Autoclave Li +
  diamante + hélio); Magneto Resonatic (Mixer + Autoclave); bases supercondutoras UIV/UMV (Mixer, composições GTNA
  guiadas pelas fórmulas GTNH) → EBF 12.700 / 13.500 K → Vacuum Freezer com hélio líquido → Superconductor UIV/UMV;
  Phononic Seed Crystal (Autoclave; água grau 8 → DTR); Phonon Crystal Solution (DTPF 13.500 K, GTNH 17.000 K limitado
  pela bobina Eternal); Phonon Medium (Alloy Blast Smelter); Shirabon (DTPF, receita adaptada).
- Item `phononic_seed_crystal` usa o modelo da Gravi Star do GTCEu — autor decide a arte.
- Dimensionally Shifted Superfluid não existe como material; fica para a etapa 7/8.
- Datagen: o lang en_us gerado podia ficar sem as entradas do `GTNALangProvider` quando o HashCache pulava o provider
  e o Registrate sobrescrevia o arquivo; resolvido apagando a entrada do cache (`.cache/c622…`). Se o
  `ConfigLangKeysTest` acusar 115 chaves faltando, é isso.
- GameTest `phononChainIsComplete`. Gate PASS 215/215. Sem commit.

## G-0195 — 2026-10-07 — Forge of Gods, etapas 7 e 8 (parcial): rotas exóticas e troca de substitutos

- `GTNAGodforgeChainRecipes.exoticRoutes` (rotas GTNA; sem Avaritia/Thaumcraft/Draconic/Chemical Plant GT++):
  Infinity (DTPF: Neutronium + Excited DTRC + Gravi Stars), Churitsu (Mixer) → Quantum + Shijima (Centrífuga, EBF),
  Dragonblood (Mixer com Dragon's Breath), Ichorium (Alloy Blast Smelter), Mutated Living Solder (Mixer sobre
  Indalloy 140 + Infinity), Eternity (DTPF até o Nano Forge ser portado).
- `GTNAGodforgeProgression`: substitutos trocados pelos materiais reais — Transcendent Metal, Creon, Mellion,
  Six-Phased Copper, Hypogen, Metastable Oganesson, Eternity, Superconductor UIV e Mutated Living Solder.
  Controladores do DTPF/TPM seguem com Indalloy 140 (bootstrap: MLS depende do DTPF).
- Pendente: Nano Forge (nanites; Eternity sai do DTPF), itens próprios dos custos (doc §4), Dimensionally
  Shifted Superfluid, Convergence do DTPF.
- Gate PASS 215/215. Sem commit.

## G-0196 — 2026-10-07 — Nano Forge (tiers 1-3), texturas originais, conflito de fusão

- Autor (2026-10-07): usar as texturas originais; conflito de receita fica a meu critério; título PNG apagado de
  propósito; vai testar no fim.
- Texturas originais: Phononic Seed Crystal (Modernity `gt.metaitem.03/761`), Harmonic Compound (GT5U `762`).
- Metastable Oganesson agora funde Enriched Naquadah + **Xenon** (Radon colidia com a fusão GTCEu de plasma de
  Naquadria).
- `gtna:nano_forge` (`NanoForgeMachine`): peças main/tier2/tier3 do `MTENanoForge` convertidas por
  `tools/convert_nano_forge_structure.py` (`pattern/nano_forge_{main,tier2,tier3}.mbs`); tiers 2/3 são sub-padrões
  (`GTNASubPatterns`). Tier = menor entre extensões formadas e nanite num input bus (Carbon 1, Neutronium 2,
  Transcendent Metal 3; não consumido — GTCEu não tem slot no controlador). Receita abaixo do tier → OC perfeito.
  Tier 4 (MagMatter/render) não portado.
- Blocos/materiais: Radiant Naquadah Alloy Casing (Modernity, CTM), Stellar Alloy (frames; ABS, composição GTNA).
- 11 nanites (texturas GT5U/Modernity: genérica tingida + custom Eternity/Universium/Glowstone/White Dwarf).
- Receitas: as 11 do `NaniteChain` (tiers 1-3, substituições documentadas na Javadoc), controlador e Carbon Nanites
  na Assembly Line com pesquisa de scanner.
- GameTests `naniteRecipesAreRegistered`, `nanoForgeBaseFormsAtTierOne`. Gate PASS 217/217. Sem commit.
- QA cliente: montar base + extensões tier 2/3 (o preview do JEI mostra só a base), conferir tier na UI.

## G-0197 — 2026-10-07 — Forge of Gods, etapa 8: componentes e progressão fiel ao GTNH

- `GTNAGodforgeComponents` (texturas GT5U/Modernity): Stable Boson Containment Unit, Superconductor Composite,
  Tengam Electromagnet, Raw Tesseract, Thermal Superconductor, Relativistic Heat Capacitor, Neutronium Heat Capacitor,
  1080k Sp Coolant Cell, Graviton Anomaly. Blocos: Cosmic Fabric Manipulator, Force Field Glass (CTM), Field
  Restriction Coil T3.
- Receitas (`GTNAGodforgeChainRecipes.components`): receita final GTNH de cada um, intermediários ausentes
  substituídos (lista na Javadoc). **Transcendent Metal agora tem rota** (GTNH): Raw Tesseract → Macerador (8 pó) →
  EBF 11.701 K com Tungsten fundido (+ Celestial Tungsten).
- `GTNAGodforgeProgression` reescrito a partir de `addGodforgeRecipes` e `Godforge.java` (custos): itens e fluidos
  do GTNH (Excited DTEC, Phonon Medium, nanites de prata, módulos com TPM/Advanced Fusion/Fluid Heater…).
  Substitutos restantes: ZPM4/ZPM6 → Ultimate Battery, Eternal Singularity → Gravi Stars, energy tunnel → laser
  hatch 4096A UIV, energy distributor → power transformer UXV, Exothermic Hearth → Mega Blast Furnace, Plasma
  Generator UV ×64 → Large Plasma Turbine ×4, TFFT field → Quantum Tank UHV; Transdimensional Alignment Matrix e
  nanites de MagMatter fora dos custos (não portados).
- Gate PASS 217/217 (cache do lang limpo de novo antes do runData). Sem commit.

## G-0198 — 2026-10-07 — Baterias ZPM3-ZPM6 e Dimensionally Shifted Superfluid

- Itens Really/Extremely/Insanely/Mega Ultimate Battery (GTNH ZPM3-ZPM6, ícone de carga cheia do Modernity) com as
  receitas de Assembly Line do GTNH (substituições na Javadoc de `batteries`). Progressão: ZPM4 → Extremely,
  ZPM6 → Mega (ZPM2 continua a Ultimate Battery do GTCEu).
- Material `dimensionally_shifted_superfluid` + 4 receitas do DTPF (tiers 2-5 do GTNH; Stable Baryonic Matter → Raw
  Star Matter, Radox → Naquadria, água grau 8 → água destilada ×10). Field Restriction Coil T3 agora usa o DSS.
- Fora: Transdimensional Alignment Matrix / Convergence (dependem do Bose-Einstein Condensate do GTNH), Eternal
  Singularity (sem textura original), Nano Forge tier 4.
- Gate PASS 217/217. Sem commit.

## G-0199 — 2026-10-07 — Auditoria de craftabilidade (alcançabilidade)

- `GTNARecipeCoverageGameTests` agora também calcula ponto fixo de alcançabilidade (tudo fora do namespace gtna conta
  como obtível), lista o conteúdo gtna inalcançável, a primeira entrada faltante de cada receita bloqueada, exporta
  `run/gtna-blocked-graph.json` e lista tipos de receita sem máquina (0).
- Raízes encontradas e corrigidas: plasmas GT++ fora da tabela do Plasma Module (Celestial Tungsten, Astral Titanium,
  Advanced Nitinol, Rhugnor, Dragon Metal, Chromatic Glass, Ichorium, Hypogen, Six-Phased Copper — agora na tabela,
  tiers do GTNH); Space/Time (centrífuga GT5U de SpaceTime com Tesseract + lente); cadeia de flotação sem rota
  (CS2, etilatos, xantatos e Turpentine do GTO, etilato de potássio por K + etanol); Singularity Shielding Casing e
  Medial/Central Graviton Flow Modulator (no GTNH 2.9 saem do BEC Condensate Assembler — receita provisória de
  Assembly Line até portar o BEC); Restraint Device (GTO); CF-PPS (banho químico GTO); solidificação de WDM/BDM/
  Universium/SpaceTime; rod/bolt/screw de SpaceTime (Eternal coil).
- Resultado: com as saídas de máquina do EOH (Raw Star Matter, WDM, BDM, Universium, SpaceTime) tudo do GTNA é
  alcançável; EOH → Forge of Gods → 4 módulos sem ciclo; só MagMatter/MHDCSM/QGP dependem do próprio Forge.
- Flaky conhecido: `eye_of_harmony_normal_speed_circuit_preview...` falhou 1× por corrida no check assíncrono de
  padrão do GTCEu (HashMap); passou na repetição.
- Gate PASS 217/217. Sem commit.

## G-0200 — 2026-10-07 — Roadmap de multiblocos e mecânicas

- Direção do autor: o GTNA porta todo multibloco viável **e** mecânicas, com KubeJS; receitas e linhas ficam com o
  modpack; máquinas "atalho" entram (caras no GT:IA); o Research Center do GTO é do research padrão do GTCEu e entra.
- `docs/roadmap/auditoria-multiblocos.md` (vereditos, blocos faltando) e `docs/roadmap/ROADMAP-multiblocos.md`
  (princípios, checklist de port, pendências P1–P6, fases A–G, APIs transversais, ordem sugerida).
- **Próxima sessão:** começar pelo ROADMAP §2 (QA/commit/datagen/flaky) e §5.

## G-0201 — 2026-10-07 — Correções do QA do autor + release 0.5.2

- Lang: Registrate e `GTNALangProvider` gravavam o mesmo `en_us.json` (o último apagava o outro: 71 itens sem nome
  ou 115 chaves de config). Agora há um único escritor: `GTNALangProvider.feed` injeta no provider de lang do
  Registrate. Removido o hack de cache.
- Componentes sem receita no GTCEu: motor/piston/pump/conveyor/robot arm/emitter/sensor/field generator UHV→OpV,
  casings UEV→MAX, energy hatches UEV→MAX e UU Matter (`GTNAHighTierComponentRecipes`, ids `gtna:*/high_tier/*`,
  receitas padrão para o pack trocar). Pendente: **circuitos acima de UHV não existem** (tags UEV+ vazias).
- Iconsets: Hypogen e Chromatic Glass com sets próprios (GT5U), Dragonblood no set Hypogen, prefixos superdense/
  triple/quadruple/quintuple portados, BDM completado (texturas faltantes geradas escurecidas do set metallic).
- Nanites de Glowstone: faltava o `.mcmeta` do overlay animado. Baterias: ZPM3 com textura própria do GT5U; ZPM4–6
  (mesmo ícone no GTNH) tingidas por tier.
- Tooltips no padrão: DTPF, TPM e Nano Forge no formato GTNH (Machine Type + linhas originais + separadores),
  Advanced Fusion no formato GTO; os quatro com linha de fonte.
- Nano Forge: o bloco "A" dos tiers 2/3 é a Assembly Line Casing (corrigido).
- Personal Space: aparece na aba de blocos; receita LV com obsidian normal.
- Eternal coil: a receita do GTNH também não usa Eternity (SpaceTime + Hypogen); mantida.
- EMI do EOH: o código só trocou ids/catalisador por planeta; aguardando o autor dizer o que piorou.
- Nova dependência obrigatória desde a 0.5.1: Infiniverse 1.0.0.5 (Personal Space). Opcionais: Ad Astra, SGJourney.
- Gate PASS 217/217. Versão 0.5.2.

## G-0202 — 2026-10-07 — Circuitos Optical e Exotic

- Autor: portar circuitos "básicos"; os melhores ficam para o GT:IA. `GTNACircuits`: famílias Optical (UV/UHV/UEV/UIV)
  e Exotic (UHV/UEV/UIV/UXV) do GTNH, texturas Modernity animadas, tags de tier do GTCEu. Receitas padrão:
  processor/assembly/computer no Circuit Assembler (cleanroom), mainframe na Assembly Line com pesquisa.
- Com isso os componentes UEV–UXV ficam craftáveis. OpV/MAX continuam sem circuito (Cosmic/Transcendent ficam para o
  GT:IA).
- EMI do EOH: o código do layout é idêntico ao do commit 18368a0/3e600a7; aguardando print do autor.
- Gate PASS 217/217.

## G-0203 — 2026-10-07 — QA do EOH no JEI, fluidos e ciclos (release 0.5.2 substituída)

- JEI do EOH no layout do EMI (gases + planeta, grade de itens, grade de fluidos separada, info), uma página por
  simulação; `JeiRecipeGuiHeightMixin` sobe o limite `RecipeGuiHeight` do JEI para a moldura externa caber.
  Fluidos de bilhões de mB desenhados cheios. Autor aprovou no cliente.
- Texturas de fluido originais (Modernity; GT5U para Hypogen/Chromatic/Rhugnor); BDM/Pt-Mn-Sb/Shijima com cor própria.
- Receitas do EOH com fluidos (base GTNH); ciclos de bootstrap quebrados (Space/Time/SpaceTime só saem do EOH):
  EOH, carcaças, campos, planeta Overworld, Tesserato, base SC UIV e célula Sp usam metais pré-EOH.
- Dimension markers do GTCEu para os planetas do Ad Astra (via `GTCEuAPI.RegisterEvent`).
- Relatório de cobertura lista receitas ≥ UHV sem fluido. Gate PASS 217/217.
