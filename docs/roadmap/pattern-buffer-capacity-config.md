# Configuração dos ME Pattern Buffers

Os quatro buffers usam `config/gtna/balance/pattern_buffers.json`. A seção `slots` aceita
valores de 1 a 540 por variante. Os padrões de fábrica são Mini 9, normal 21, Advanced 32
e Ultimate 72. A configuração é lida na inicialização; reinicie o jogo/servidor após editar.
O exemplo GTIA em `docs/config-examples/gtia/pattern_buffers.json` usa 72 slots no Advanced
e 162 no Ultimate. O ME Craft Pattern Hatch não faz parte desta configuração.

A UI usa páginas de 90 slots (9 colunas × 10 linhas), portanto 72 slots cabem em uma página
e 162 aparecem em duas. Capacidade, tooltip e inventário são determinados pelo mesmo valor na
inicialização. O proxy usa os handlers do buffer vinculado e acompanha sua capacidade.
O item de upgrade (21/32/72) move o buffer para a variante maior preservando padrões,
configurações por slot e inventários internos, sem derrubar nada no chão.

O arquivo `config/gtna/balance/pattern_buffer_capacity_history.json` registra a maior
capacidade já ativada por variante. O mod preserva esse valor se o JSON principal for reduzido,
para evitar remover slots de máquinas já salvas. Mantenha o arquivo de histórico junto com
o mundo ao transferir uma instalação. Se ele faltar e uma máquina salva possuir mais slots que
a configuração atual, o carregamento recusa essa máquina com uma mensagem orientando a
restaurar a capacidade anterior. Para reduzir a capacidade de fato, retire todos os buffers
da variante do mundo, esvazie seus conteúdos e só então remova o histórico correspondente.
