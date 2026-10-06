# Void Miner: cadeia de essências e dois modos

Referência verificada: instância Prism `GregTech-Leisure-1.4.5.1`,
`minecraft/kubejs/server_scripts/gtceu.js` (6255–6729), `startup_scripts/item.js` (75–96),
e GTLCore branch `gtl-1431-skyblock`, commit `18c7814`.

## Funcionamento no GTNA

1. Um bloco de osso + 1.000 mB de Biomass no Chemical Bath HV produzem uma Essence.
   É a adaptação do bootstrap do GTL, que usa o Block Conversion Room para transformar
   Bone Block em Essence Block e depois o Macerator. Não adicionamos esse outro multibloco.
2. Mixer MV: 16 sementes (`forge:seeds`), uma Essence, 1.000 mB de Distilled Water e
   1.000 mB de Carbon Dioxide → 16 Essence Seeds, 400 ticks, 120 EU/t (receita GTL).
3. World Data Scanner LV–IV: dois tanques independentes de 64.000 mB. Os scans de
   Overworld/Nether/End consomem 1/2/4 Data Sticks, 64 dusts da pedra, PCB Coolant
   100/200/400 mB e 64.000 mB do ar correspondente. Circuito 1, 4.000 ticks,
   LV/MV/HV. **A máquina deve estar na dimensão escaneada**.
4. Para Lua, Marte, Vênus, Mercúrio e Glácio: scanner EV no planeta, um Data Stick,
   pedra planetária reutilizável, circuito 1, 1.000 mB PCB Coolant e 64.000 mB Air →
   Planet Data Chip, 4.000 ticks. O Air é um reagente genérico; não simula atmosferas
   planetárias. O Assembler deixou de fabricar esses chips.
5. Incubator HV: Essence Seed + amostras de minério + 10.000 mB de Biomass e de Milk,
   1.200 ticks (60 s, configurável) → 64 essências. Os dados não são consumidos: 16/32/64 dados dos mundos
   base, ou um chip planetário. Existem 40 essências de veias do GTL e cinco planetárias.
6. Electric Void Miner: selecionar **Preciso** ou **Aleatório** na aba de modos do GTCEu.
   Preciso consome uma essência e Drilling Fluid; os chips e a pedra ficam na cadeia de
   produção, não são mais seletores diretos da mineração. Aleatório não usa essência.
   EV permanece serial. Em IV+, o Parallel Hatch determina a quantidade, limitada por energia, insumos, saída e teto configurável de 1.024. Aceita um Accelerate Hatch.

A Incubator conserva a estrutura GTL 5×5×5: Plascrete, Sponge, Cleanroom Glass e Filter
Casing; pelo menos 40 Plascrete, manutenção e hatches de receita. O controller fica na
última aisle, na base. O scanner e a incubadora possuem crafting; o crafting EV do minerador
permanece o anterior.

## Adaptações explícitas para EV

Este port mantém a máquina EV e os custos configuráveis do GTNA; **não é uma cópia dos
custos e tiers do pack GTL**. Os scripts da referência usam:

| Operação GTL 1.4.5.1 | Fluido | Energia | Duração |
| --- | --- | --- | --- |
| Preciso Overworld/Nether | 1.000.000 mB | LuV | 200 ticks |
| Preciso End | 1.000.000 mB | ZPM | 200 ticks |
| Aleatório | 10.000.000 mB | ZPM | 1.200 ticks |

No GTNA, as 40 veias conservam os gates de energia do GTL: LuV para Overworld/Nether,
ZPM para End. O fluido usa `defaultDrillingFluidPerOperation` (1.000 mB), e a duração usa
`baseDuration` (200 ticks / 10 s). As quantidades de saída foram divididas por dez; minérios
saem como Raw Ore onde disponível. Esses ajustes de custo/rendimento são adaptações ao
perfil do GTNA, e não números originais do GTL.

Há três programas terrestres iniciais em EV (Ferro/Cobre, Estanho/Chumbo e Ouro/Prata),
com os custos e rendimentos do G-0131, agora consumindo respectivamente essência de
Iron Vein, Cassiterite Vein e Magnetite Vein Overworld. Circuito 1 seleciona os
programas básicos EV; circuito 2 seleciona as 40 veias completas LuV/ZPM. Os cinco
programas planetários permanecem em EV e usam a essência sem circuito adicional.

O modo aleatório tem duas receitas, distinguidas por circuito 1 (EV) e 2 (ZPM): a terrestre EV só inclui Iron/Copper/Tin/Lead/Gold/Silver,
com uma unidade por recurso, 25% de chance + 0,2 ponto percentual por tier de bônus.
O pool completo do GTL exige ZPM, com uma unidade por recurso distinto e 2% de chance
+ 0,2 ponto percentual de bônus (200/20 do GTL). Ambas usam 10.000 mB e 600 ticks (30 s),
configuráveis. O pool avançado não antecipa Naquadah e outros recursos avançados em EV.
`baseEUt` (2.048 EU/t) controla o aleatório terrestre e o fallback; o pool completo usa
ZPM. Packs podem substituir receitas, tiers e rendimentos via datapack/KubeJS.

Os cinco programas planetários conservam os custos, quantidades e tempos do G-0131,
mas consomem a essência planetária. A cultura exige os respectivos Raw Ores como amostra,
portanto a primeira extração ainda precisa acontecer no planeta ou por outra rota do pack.
A configuração `programRequired` aceita essências consumíveis; seletores antigos continuam
aceitos para compatibilidade com receitas de packs. As receitas nativas antigas por circuito
foram substituídas por três programas terrestres com essência e circuito 1; o fallback continua opcional.

No GTCEu padrão, Zircon/Celestine/Trinium Compound do pack são representados por
Zirconium/Strontium/Trinium. Elementos sem Raw Ore usam Dust. O port habilita Dust para
Rubidium, Strontium, Tellurium e Zirconium, que no GTCEu eram apenas declarações de elemento.
Isso não adiciona minérios ao worldgen. Amostras aceitam `forge:ores/<nome>` e o Raw Ore
correspondente (ou Dust quando não há Raw Ore).

Os registros das receitas e máquinas usam namespace `gtna`, sem depender do GTL/GTO ou
KubeJS. Ad Astra é opcional; sem ele, a cadeia dos três mundos base continua disponível.
As cinco receitas planetárias só são geradas quando as pedras do Ad Astra existem.

## Texturas e atribuição

Texturas de Essence/Essence Seed, 40 ícones de veias e overlay do scanner vêm do checkout
GTOCore, `src/main/resources/assets/gtocore/textures`, sob CC BY-NC-SA 4.0 conforme
permissão documentada em THIRD_PARTY_NOTICES.md. As variantes `_ow`/`_end` reaproveitam
os ícones correspondentes do GTO. Não copiamos assets de licença indefinida do pack Prism.
Tooltips de origem creditam GTL pelas mecânicas e GTLCore pela estrutura.

## Validação manual

- Conferir a aba de modo, tooltips e ícones no cliente.
- Escanear os cinco planetas em suas dimensões, conferir recusa fora delas.
- Montar a incubadora 5×5×5 e conferir a orientação/preview.
- Revisar rendimento/custo das veias LuV/ZPM e dos programas EV no perfil do pack.
- Exercitar saída cheia, múltiplos output buses e Parallel Control Hatch IV+.

## Correção de seleção após QA na Prism

O GTCEu indexa receitas pelas entradas e não distingue variantes apenas pelo EU/t ou duração. O ensaio conjunto do GTIA revelou descarte de programas básicos/completos com a mesma essência, além da colisão entre os dois aleatórios. Os circuitos acima tornam as entradas distintas sem retirar essências, modos ou gates de energia. `GTNAVoidMinerLookupGameTests` monta um catálogo isolado com todas as receitas reais e procura cada ID usando suas entradas; assim, um registro presente no RecipeManager mas ausente do índice passa a falhar no gate.


## QA do autor em 2026-09-30

Tempos acima adotados por escolha explícita do autor. As receitas iniciais e planetárias
agora também usam `baseDuration`; culturas usam `incubationDuration`. Configs já existentes
conservam valores próprios: o perfil GTIA e o ambiente local foram atualizados com backup.
O Jade resume tamanho do pool e chances no modo aleatório; ele não garante todos os
minérios por ciclo. Preciso mantém suas saídas determinísticas.
