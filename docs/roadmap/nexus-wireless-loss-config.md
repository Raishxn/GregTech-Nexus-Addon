# Perda da rede wireless do Nexus Flux Matrix

`config/gtna/balance/nexus_flux_matrix.json` controla a política de perda. A opção
`lossApplication` aceita `LEGACY` (padrão), `MATRIX_INPUT_ONCE` e `NO_LOSS`.

- `LEGACY`: preserva o comportamento anterior. A rede credita toda energia recebida; o
  Generator Array desconta 5% antes do envio se `generatorArrayAppliesSeparateLoss` for `true`.
- `MATRIX_INPUT_ONCE`: a rede aplica `lossPercentByTier` uma vez à energia bruta recebida do
  Wireless Dynamo Hatch ou Generator Array. A perda separada do Array fica desativada nesse modo,
  mesmo que o sinalizador legado ainda esteja `true` em um arquivo antigo.
- `NO_LOSS`: a rede e o Array não descontam energia.

O exemplo GTIA em `docs/config-examples/gtia/nexus_flux_matrix.json` aplica LV 5%, MV 4%, HV 3%,
EV 2%, IV 1% e LuV+ 0%. As opções `tiers` existentes continuam definindo capacidade, limite de
transferência e acesso entre dimensões (padrão: EV+). O valor por tier aceita 0–100%, com
precisão de 0,01%. É necessário reiniciar o jogo/servidor após editar o arquivo.
Entradas e saídas em outra dimensão são recusadas nos tiers que têm `crossDimension: false`.

Na migração, saldos antigos permanecem iguais: a perda só atinge novas entradas. A rede devolve
às máquinas a quantidade **bruta aceita**, para que elas retirem essa quantidade da origem; a
quantidade creditada e a perda aparecem separadamente na tela do Matrix. Frações de perda são
acumuladas por dono da rede e salvas no mundo, evitando que transferências pequenas escapem da
taxa configurada. Entradas são recusadas enquanto não houver Matrix formado no modo
`MATRIX_INPUT_ONCE`, pois nesse estado não há tier de capacitores para escolher a taxa.
Saves antigos também podem não registrar a dimensão da Matrix: nesse modo, a rede recusa
transferências até a estrutura ser reconhecida e registrar sua dimensão. Isso evita liberar
acesso entre dimensões antes de conhecer a origem da rede.
No modo `LEGACY`, a perda separada do Generator Array também entra nos totais de energia bruta e
perda exibidos pela Matrix e pelo Quantum Terminal.

Para repetir os GameTests nos três modos, execute
`python3 tools/test_nexus_wireless_profiles.py`. O script restaura o JSON de desenvolvimento e
grava um log por perfil em `build/test-results/nexus-wireless-profiles/`.
