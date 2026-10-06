# Eye of Harmony — teste humano da estrutura GTNH

Marco EOH-03. Receitas novas do multibloco/componentes adiadas pelo autor. A validação usa Creative.
É preciso reiniciar o cliente de desenvolvimento após recompilar; o cliente já aberto antes deste
marco ainda usa a versão anterior das classes. Usar um mundo de teste e bastante espaço livre.

## Montagem inicial

1. Colocar o controlador Eye of Harmony elevado pelo menos 17 blocos acima do solo. A estrutura
   ocupa 33 × 33 × 33, com controlador no centro vertical da face frontal.
2. Abrir o preview e conferir os três casings novos e campos Rudimentares (tier 1/9).
3. Em Creative, usar o Nexus Structure Terminal para montar a máquina com configurações padrão,
   sem modo “sem hatches”. O preview/montagem deve usar exatamente um bus de entrada de itens,
   dois hatches de entrada de fluidos, um bus de saída de itens e um hatch de saída de fluidos.
4. Conferir que a estrutura forma e a interface mostra os tiers 1/9 das três famílias.
   Testar o acesso à GUI e a aparência dos hatches sobre o Boundary Casing.
5. Repetir com controlador virado para outra direção; frente deve ficar externa e acessível.

## Tiers e diagnóstico

- Substituir todos os 138 campos de compressão por um tier maior da mesma família. Ao formar,
  a interface deve mostrar esse tier sem mudar aceleração/estabilização.
- Trocar somente um campo de qualquer família por outro tier: a máquina deve deixar de formar.
  Restaurar o tier uniforme deve permitir reformação. Conferir a mensagem de diagnóstico.
- Retirar um dos cinco hatches/buses obrigatórios: a estrutura não deve formar.
- Tentar um ME Stocking Input Bus/Hatch, Pattern Buffer, hatch dual ou hatch de energia numa
  posição de fronteira: a estrutura deve rejeitá-lo.

## Limites deste marco

A operação planetária fiel será implementada no EOH-04. Os tiers agora são lidos e exibidos,
mas não mudam ainda as fórmulas das receitas cosmos legadas. Não testar custo/retorno energético
ou balanceamento usando esta etapa como referência de fidelidade.

Estruturas antigas com Bridge/High Power/Transcendent/Injection Casings precisam dos novos
blocos nas posições correspondentes. O controlador e seus dados anteriores são preservados;
não há substituição automática de blocos nem publicação desta versão.

Relatar: direção usada, montagem pelo terminal ou manual, tiers escolhidos, resultado da
formação e qualquer problema no preview/GUI. Captura do diagnóstico ajuda a localizar a célula.
