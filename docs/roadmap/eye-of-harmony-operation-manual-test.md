# Eye of Harmony — QA da operação Overworld

Usar cliente reiniciado com as classes atuais. Fabricação continua adiada; componentes obtidos
em Creative. Montagem conforme [QA estrutural](eye-of-harmony-structure-manual-test.md).

## Interface e seleção

1. Monte os novos casings/campos e exatamente cinco portas. Abra o controlador: deve aparecer
   o slot de planeta abaixo do display rolável; conferir textos/valores sem sobreposição ou erro.
2. Insira Overworld Planet Block. O item permanece reutilizável, inclusive após uma operação.
   Nether/End devem ser aceitos no slot mas impedir início. Sem seletor, o motivo pede Overworld.
3. Vincule sua rede com Data Stick. A rede Nexus precisa estar acessível, com matriz/capacidade
   e saldo conforme sua política normal. A interface mostra o custo inteiro, inclusive acima de long.
4. Circuito ausente/0 é válido; testar 1, 4 e 24 (também 32 no slot virtual, limitado a 24).
   Tempo cai por 2 e custo cresce por 4 a cada nível. Circuito não é consumido.
5. Gases abaixo dos mínimos impedem início: 1.000.000.000 mB de H₂ e de He (um milhão de baldes
   **de cada**). Os hatches são drenados integralmente para o buffer, sem lote fixo. Desabilitado
   deve parar a coleta. Saldos de gás aparecem na interface; excesso reduz chance e rendimento.
6. Abrir/fechar a GUI ou consultar receitas não deve reduzir saldo Nexus/gases. Uma transferência
   de gás do hatch ao buffer é visível e não equivale ao consumo de início.

## Ciclo e recuperação

Para ciclo curto sem overclock energético, usar as três famílias no **tier 9** (interno 8),
circuito 0: perto de **55 segundos**, chance 66%, rendimento 60%, custo-base sem multiplicador
de circuito. O valor energético completo depende dos plasmas e aparece na prévia. Esse QA ainda
exige uma rede suficientemente abastecida; os testes automáticos usam valores menores para
verificar transações sem construir uma economia inteira.

1. Abasteça gases exatamente nos mínimos e saldo suficiente. Ao começar: um único débito,
   ambos os buffers inteiros zerados e progresso iniciado. Excesso também é consumido.
2. Durante execução, mudar planeta/circuito não deve recalcular duração ou cobrar outra vez.
   Trocar dono com Data Stick deve recusar, inclusive enquanto saídas estiverem pendentes.
3. Desabilite: progresso pausa. Habilite: retoma do mesmo ponto. Remova um campo para invalidar
   e restaure: mesma pausa/retomada. Tiers novos podem formar, mas não alteram o ciclo já pago.
4. Saia e volte ao mundo durante o ciclo; depois afaste-se para descarregar chunks e retorne.
   Confirmar progresso recuperado, nenhum novo débito e mesmo resultado/produtos planejados.
5. Ao terminar, sucesso entrega os recursos do catálogo com rendimento; falha entrega SpaceTime
   conforme chance e ainda devolve EU. Não esperar que falha zere o retorno energético.
6. Bloqueie saídas e/ou encha a rede perto do término. Deve aguardar mantendo os restantes.
   Libere gradualmente: entrega parcial, sem descarte nem outra operação enquanto houver dívida.
   Saldos Nexus recebidos podem ser menores que o retorno bruto devido à perda configurada.
7. Faça outro relog enquanto houver saídas/crédito pendentes. Nada já entregue deve ser repetido.
8. Em mundo de teste, quebre e reposicione o controlador pago; seu item deve conservar o estado
   de recuperação. Recompor a estrutura deve permitir retomar. Não duplicar o item em Creative
   durante esse teste de conservação de recursos.

Renderer ainda usa a cena orbital antiga; a aparência planetária dinâmica não faz parte desta etapa.
JEI/Cosmos ainda mostra receitas legadas; elas não iniciam novas operações no controlador.
Se algo falhar, registrar tiers, circuito, seletor, estado, gases, saldo e trecho do latest.log.
