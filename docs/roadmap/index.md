# Roadmap público — GregTech Nexus Addon

Atualizado em **30/09/2026**. Base de desenvolvimento: **Minecraft 1.20.1 / GTCEu 7.5.3 / GTNA 0.5.1**.

GTNA amplia a indústria a vapor, as fábricas elétricas e a automação do GregTech com
multiblocos, redes wireless, módulos e integração AE2. Também oferece configurações
para que modpacks adaptem esse conteúdo à sua própria progressão.

## Como acompanhar

- **Implementado:** existe no desenvolvimento atual; pode ainda não estar em uma versão publicada.
- **Em validação:** já funciona em testes, mas precisa de confirmação no jogo ou de balanceamento.
- **Planejado:** próxima tarefa; a caixa só será marcada após implementação e verificação.
- **Em estudo:** depende de definição de escopo e não é uma entrega prometida.

As prioridades podem mudar conforme os testes. Não há datas de lançamento comprometidas.
O roadmap descreve os principais sistemas; não é um inventário de todas as máquinas.

## Implementado

- [x] Redes wireless de vapor e energia, com monitoramento e Nexus Flux Matrix.
- [x] Máquinas Steam e Large Steam, componentes hidráulicos e Steam Elevator com módulos.
- [x] Nexus Structure Terminal para preview e montagem de multiblocos e módulos registrados.
- [x] Módulos auxiliares em máquinas elétricas compatíveis e cadeia de processamento de minério.
- [x] Hatches de aceleração, overclock, paralelo e receitas simultâneas em máquinas compatíveis.
- [x] ME Pattern Buffers com modos de receita, capacidades configuráveis e upgrades que preservam dados.
- [x] Universal Factory com regras configuráveis para dividir os recursos entre receitas.
- [x] Electric Void Miner com modos preciso/aleatório, essências, World Data Scanners e Incubator.
- [x] Void Fluid Drilling Rig com descoberta por extração real, cartões reutilizáveis e upgrades remotos.
- [x] Integrações JEI/Jade, traduções em inglês/português e testes automatizados dos sistemas principais.

## Agora — estabilizar o conteúdo em teste

- [ ] **GTNA-01 · Mineração Void:** confirmar no cliente os dois modos, os novos tempos, Accelerate Hatch e paralelos acima de dois; conferir consumo e saída cheia.
- [ ] **GTNA-02 · Descoberta de fluidos:** verificar com uma pessoa a prospecção, extração, registro de petróleo e o caminho Marte → Radon → produção remota no perfil GTIA; tornar as instruções claras.
- [ ] **GTNA-03 · Interfaces:** conferir nomes dos scanners, chances no Jade, rolagem dos grandes catálogos no JEI e escalas diferentes da interface.
- [ ] **GTNA-04 · Equipes:** testar compartilhamento e bloqueios de dados com dois jogadores reais, incluindo saída e retorno à equipe.
- [ ] **GTNA-05 · Segurança dos dados:** retestar Pattern Buffer upgrades, redes ME cheias/sem energia, recarregamento do mundo e migração de capacidades sem perda ou duplicação.
- [ ] **GTNA-06 · Energia wireless:** confirmar perda única, transferências entre dimensões e recuperação de mundos existentes nos perfis configuráveis.

**Para encerrar esta etapa:** os casos acima devem ter resultados registrados; problemas de
perda de itens, duplicação, travamento ou custos incorretos precisam estar corrigidos.
Testes automatizados complementam a validação no cliente e em servidor multiplayer.

## Depois — integração, balanceamento e documentação

- [ ] **GTNA-07 · Fábricas em uso real:** medir autocrafting AE2, múltiplas receitas e desempenho sob carga; conferir CPUs nativas e sistemas Nexus juntos.
- [ ] **GTNA-08 · Multiblocos e módulos:** ampliar a conferência de orientação, formação, preview e hatches nas máquinas já adicionadas.
- [ ] **GTNA-09 · Configuração para packs:** completar os controles de custo, rendimento, paralelo e habilitação conforme as necessidades verificadas; documentar defaults e exemplos.
- [ ] **GTNA-10 · Conteúdo de progressão:** revisar receitas, custos e rendimento das cadeias Steam, elétricas e de componentes, priorizando máquinas com uso prático.
- [ ] **GTNA-11 · Guias e traduções:** atualizar instruções de montagem/uso e manter idiomas e tooltips coerentes com as máquinas e seus limites.

**Para encerrar esta etapa:** máquinas e configurações documentadas devem reproduzir o
comportamento observado em uma fábrica real, com custos e limitações compreensíveis.

## Mais adiante — expansão e preparação de versão

- [ ] **GTNA-12 · Novos ports:** selecionar novas máquinas por necessidade de progressão; verificar estrutura, receitas, comportamento, arte e licença antes de implementar.
- [ ] **GTNA-13 · Qualidade técnica:** reduzir duplicação nos registros e ampliar testes de regressão nas áreas que os relatos de jogadores apontarem.
- [ ] **GTNA-14 · Preparar uma versão:** revisar alterações, migração de saves/configs, compatibilidade, créditos, changelog e instruções de instalação antes de publicar.

Novas famílias complexas de máquinas permanecem **em estudo** até terem escopo e
pré-requisitos definidos. Este roadmap não aprova automaticamente todos os ports de referência.

## Relação com o GregTech Infinity Ascension

GTNA é um dos mods-base do GTIA, mas continua sendo um addon utilizável por outros packs.
O GTIACore exige GTNA; o GTNA não exige GTIACore. Receitas, depósitos planetários e gates
específicos do pack pertencem ao perfil GTIA e podem diferir dos defaults do addon.

## Ajudar com os testes

Relate versão, máquina, configuração relevante, passos para reproduzir e resultado esperado/observado.
Para fluidos, há um [guia de teste de petróleo e Radon](void-fluid-manual-test.md).
Para origem de código e arte, consulte os [créditos e licenças](https://github.com/Raishxn/GregTech-Nexus-Addon/blob/main/THIRD_PARTY_NOTICES.md).

## Eye of Harmony

- [ ] [Eye of Harmony fiel ao GTNH](eye-of-harmony-implementation-roadmap.md). Em andamento: conteúdo físico, estrutura e operação Overworld implementados localmente; QA no cliente pendente. Fabricação adiada; demais planetas, viewer e paralelos por etapas.

- [Auditoria de fabricação, materiais e planetas](eye-of-harmony-progression-port-audit.md): dependências verificadas, rota BEC, proposta adaptada e sugestões de nomes.
