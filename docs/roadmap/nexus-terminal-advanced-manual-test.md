# Nexus Terminal — interface com seletor e construção

Referência: capturas do autor e Advanced Terminal do GTO instalado em
`GregTech.Odyssey-0.6.0-dev9` (`gtocore-forge-1.20.1-26.9.5.jar`). Sua classe usa
`uipro` e métodos nativos, incompatíveis com GTCEu 7.5.3. O Nexus usa widgets
ModularUI próprios e mantém seu formato de configurações; não importa essa classe.
O catálogo `BlockMap` foi consultado no GTOCore local (`dc4824d`).

## Interface

1. Clique direito no ar com o Nexus Terminal. Confira o tema roxo/preto e apenas o painel
   de controles centralizado. Ao abrir as opções, os painéis se distribuem lado a lado;
   fechar a lista centraliza os controles novamente. Categorias e escolhas começam fechadas. Repetições/módulos e todos os
   modos ficam visíveis. Clique em Blocos por tier → Selecionar para abrir as categorias.
2. Use `-1`, `+1`, digitação e roda do mouse nos números. Repetições: 0–1000;
   módulos: 0–100 (quantidade dos primeiros subpatterns, conforme o comportamento GTNA existente).
3. Clique nos modos. Feche/reabra para conferir persistência real, incluindo no servidor.
4. Selecione uma categoria para abrir sua grade; escolha um bloco e confira a borda roxa
   e o ícone da categoria. Feche a grade com ×; feche as categorias com × e reabra pelo botão.
   Clique novamente para limpar; confira também limpar esta escolha e limpar todas.
   As escolhas novas são IDs; seleções antigas por índice continuam sendo lidas.
5. Capacitores/Battery deve conter só os blocos registrados em PSS_BATTERIES, incluindo
   os casings de capacitor vazio; sem Battery Buffers, itens soltos ou capacitores Nexus.
   Wireless Capacitors permanece a família Nexus independente. Confira categorias presentes no pack. Categorias sem blocos registrados ficam ausentes;
   não se inventam componentes GTO que não existem no GTNA/GTCEu. Vidro e iluminação
   ficam fora deste seletor e escolhas antigas dessas famílias não alteram a construção.
6. Confira inglês/PT-BR, barras de rolagem e diferentes escalas de GUI.

## Construção

- Shift + clique no controlador executa a construção. Comece em Creative, depois Survival.
- EOH: selecione separadamente compressão, aceleração e estabilização e confira tiers uniformes.
  Deixe No Hatch desligado para construir as portas. Selecione outro tier e habilite Replace:
  o controlador e posições ignoradas devem permanecer; apenas escolhas compatíveis substituem
  os candidatos da célula. Um vidro escolhido não pode virar um campo/coil.
- No Hatch: impede a colocação automática de portas. Não esperar formação quando as portas
  obrigatórias ainda não foram colocadas manualmente.
- Mirror: construa um multibloco assimétrico com suporte à geometria espelhada e confira formação.
  Espelhamento é aplicado à construção; a validação nativa determina o flip da estrutura.
- Demolition: coloque pedra numa célula que exige ar e noutra posição ignorada. A primeira
  deve ser limpa; a segunda deve permanecer. O modo não varre áreas fora do pattern.
  Não remove o controlador, bedrock ou uma posição cuja quebra foi cancelada por outro mod.
- Survival: confira consumo, devolução de blocos em Replace e saídas de Demolition.
  Compare inventário antes/depois. Trocas de portas com conteúdo/NBT precisam de QA específica.
- AE: vincule ao Wireless Access Point, habilite Use AE e confira retirada real com alcance/permissões.
- Módulos: mantenha o roteiro existente de orientação; confira construção em máquina já formada.

## Limites e pendências manuais

A janela usa largura 500/altura 248 em pixels de GUI. Escalas muito altas podem exigir
reduzir a escala de GUI; a conferência de layout no monitor do autor continua obrigatória.
Os testes de servidor verificam seleções/compatibilidade e construção; não validam a imagem
na tela nem uma rede AE montada pelo autor.

## Regressão de desempenho — Artificial Star

- Num cliente reiniciado com a correção, construa o Artificial Star usando o Terminal.
  Confira se o mundo volta a responder após a colocação e se a estrutura é validada.
  O congelamento observado ocorria no replay de snapshots do Forge após o retorno do Terminal.
- Faça uma construção/replacement de campos EOH e confirme os novos tiers depois do tick.
  Remova depois um casing obrigatório: a máquina deve invalidar normalmente.
- Repita em Survival, verificando inventário e eventos de proteção/cancelamento, e com AE.
  O guard só agrupa checks do controlador construído; não ignora remoção do controlador.
- Construção ainda é síncrona; estruturas enormes podem causar uma pausa na colocação.
  Esta correção aborda a tempestade de checks, não implementa uma fila de colocação por tick.

## Preservação de hatches no Replace (G-0166)

- Com o multibloco já formado, marque Replace e troque campos/casings por tier.
  Os hatches/buses instalados devem manter exatamente o mesmo bloco, conteúdo,
  configuração AE, facing e NBT. Repetir com No Hatch ligado e desligado.
- Replace preserva todos os IMultiPart instalados, mesmo numa posição incompatível.
  Corrigir essas posições manualmente; o modo não usa uma carcaça como substituto
  automático de uma porta existente. Também preserva partes com Demolition combinado.
