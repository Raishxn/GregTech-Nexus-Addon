# GTNA — Checklist de QA manual (client/visual)

Coisas que os gates automatizados (`runUnitTests` + `runGameTestServer`) **não** cobrem:
renderização, UI, Jade, pacotes client, drag, tooltips renderizadas. Rodar `./gradlew runClient`,
entrar num mundo com as estruturas montadas e marcar cada item. Regressão encontrada → anotar num
checkpoint novo do `CONTINUITY_LEDGER.md` e, quando der, transformar em gametest/contract test.

## Wireless Steam HUD
- [ ] HUD aparece com a rede carregada: saldo, `Flow: +N / -N mB/s`, `Hatches: N drain / M feed`.
- [ ] O **gráfico fica dentro da moldura** (não vaza a borda de baixo).
- [ ] Botão no **wireless steam hatch**: clique esquerdo **liga/desliga** o HUD.
- [ ] Clique direito no botão abre o **editor**; arrastar move o HUD.
- [ ] Ao fechar/reabrir o editor (e ao reiniciar o client) a posição **persiste** (`config/gtna.yaml` X/Y).
- [ ] F1 (HUD escondido) e F3 (debug) escondem o HUD.
- [ ] O HUD só aparece quando há rede (hatch/saldo/fluxo) e some em rede vazia/sem hatches.

## Jade
- [ ] Wireless hatch: "Network: X mB", "Hatch Tank: X / Y mB", "Last push/pull".
- [ ] Solar boiler: "Sunlit Cells" + "Steam Production (mB/s)".
- [ ] Pattern buffer: provider de múltiplas receitas.
- [ ] Passar o Jade em cada provider configurado (`config.jade.plugin_gtna.*`) não crasha o client.

## Tooltips / UI de máquina
- [ ] Módulos do elevador: tooltip com as linhas fiéis ao GTNL (cabeçalho colorido + linhas na ordem).
- [ ] **Beacon**: botões por efeito (verde = ligado), limite `tier + 2`; upkeep muda com a seleção.
- [ ] **Weather**: circuito 1/2/3 muda o clima; UI mostra o **tempo restante**; cobra 1.000.000 mB por troca.
- [ ] **Ore Processor**: circuito muda modo/parallel; UI mostra modo/parallel/upkeep; consome distilled water + lubricant.
- [ ] **Entity Crusher**: spawner com NBT no slot → drops; UI mostra a chance de dobrar.
- [ ] **Flight**: dentro de 64 blocos ganha voo (duplo pulo); **sair do alcance revoga** o voo.
- [ ] **Monster Repellent**: mobs hostis não nascem dentro do raio enquanto roda.
- [ ] **Apiary / Greenhouse / Oil Drill**: consomem água/steam e produzem conforme o tooltip.
- [ ] **Bee Breeding** (só com Productive Bees): o item/módulo **não existe** sem o mod; com o mod, aceita
      um spawn egg de abelha do PB como catalisador, consome 128 honey treats e produz uma cópia da abelha.

## Bee Breeding × Productive Bees
- [ ] Sem o Productive Bees instalado: o módulo não tem receita e não aparece no JEI/EMI.
- [ ] Com o Productive Bees instalado: o módulo crafta e forma a estrutura 1x5x2.
- [ ] Spawn egg do PB no slot de entrada (catalisador, **não** é consumido).
- [ ] 128 honey treats do PB no inventário de entrada → após 10 min produz **uma** cópia da abelha.
- [ ] Sem abelha ou sem honey treats: o progresso fica em 0% e não consome nada além do upkeep de steam.
- [ ] Saída cheia: não voida itens (dry-run do `hasOutputRoom`).

## Estruturas
- [ ] Elevador forma com 1 steam hatch em **cada** módulo (sem "Maximum: 1" no chat).
- [ ] Cada módulo forma e mostra **Working** (formed && connected).
- [ ] O host do elevador conecta/desconecta módulos ao (des)montá-los.

## Fluxo de rede
- [ ] Um boiler enchendo um output hatch alimenta vários input hatches (fair share; não monopoliza).
- [ ] Input bronze satura em 100.000 mB; output cabe o ciclo do boiler.
- [ ] `/gtna steam` mostra saldo, fluxo vitalício e a última operação por hatch.
