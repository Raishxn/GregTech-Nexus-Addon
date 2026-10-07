# Forge of Gods (Godforge) — auditoria de port

Referência: GT5-Unofficial (GTNH) `a3e1e11241a814c9fa0dd0973d5699548428f689`, mesma revisão da
auditoria do EOH; código LGPL-3.0 (TecTech). Base visual 1.20.1 existente: GTOCore
`dc4824d1608ffad3bb0e2d53a2a068a739da3e84` (código LGPL-3.0, assets CC BY-NC-SA 4.0, já usados
pelo GTNA com atribuição).

## O que é o diferencial

O Godforge não é uma máquina de receitas. É um **sistema**:

1. **Controlador** (estrutura 127×29×186, 3 anéis) que **queima combustível estelar** para manter
   uma **bateria interna**. Sem bateria, nada funciona; com bateria, a estrutura de anéis some e
   aparece a **estrela renderizada** (shader), girando, com cor e tamanho configuráveis.
2. **Módulos** separados (até 8/12/16, conforme anéis) conectados ao controlador. Eles não têm
   energia própria: puxam EU da **rede wireless** do dono e herdam do controlador calor, paralelos,
   bônus de velocidade, desconto de energia, voltagem e tier de plasma.
   - **Smelting** (EBF + fornalha, base 1024 paralelos), **Molten** (blast smelter, 512),
     **Plasma** (sólido/fluido → plasma, 512, tiers 1–5 e multi-step), **Exotic** (receitas
     **aleatórias** de QGP com 7 entradas de plasma sorteadas; modo **MagMatter** com Tempo/Espaço).
3. **Árvore de 31 upgrades** comprados com **Graviton Shards** (+ itens extras em alguns), com
   pré-requisitos, divisões de caminho (`SPLIT_UPGRADES`) e efeitos numéricos em `GodforgeMath`.
4. **Quatro milestones** (Carga/EU gasto, Conversão/receitas, Catalisador/combustível,
   Composição/módulos e anéis). Cada nível dá shards (`n(n+1)/2`); com os 4 em 7 ocorre a
   **Inversão** (progresso infinito, barras invertidas).
5. **Anéis 2 e 3** liberados pelos upgrades CD e END; END troca o combustível de entrada por
   Graviton Shards e permite ejetá-los.
6. **Cosméticos**: cor da estrela (presets + editor RGB/gama + import/export), tamanho, rotação,
   animações desligáveis (chave de fenda).

## Inventário do upstream

| Parte | Arquivos | Linhas |
| --- | --- | --- |
| Controlador | `MTEForgeOfGods` | 1 048 |
| Estrutura | `ForgeOfGodsStructureString` + `ForgeOfGodsRingsStructureString` | 10 540 |
| Módulos | `MTEBaseModule`, `Smelting`, `Molten`, `Plasma`, `Exotic` | 1 670 |
| Regras | `GodforgeMath`, `ForgeOfGodsData`, `ForgeOfGodsUpgrade`, `UpgradeStorage` | 1 756 |
| Cor da estrela | `color/*` | 606 |
| Render | `RenderForgeOfGods`, `TileEntityForgeOfGods`, `BlockForgeOfGods`, 3 shaders, 4 texturas | ~970 |
| GUI (MUI2) | 37 arquivos em `gui/modularui/multiblock/godforge` | ~6 000 |
| Receitas | `loader/recipe/Godforge.java` (plasma/exótico/custos de upgrade), Research Station (controlador, casings, módulos) | 927 + ~300 |
| Blocos | 8 casings + vidro (`BlockGodforgeCasings`, `BlockGodforgeGlass`) | ~270 |

## Fórmulas que precisam ser idênticas

- Consumo: resíduo `f·300·1.15^f`, plasma estelar `f·2·1.08^f`, MagMatter `f/25`; ×0,8 com STEM;
  partida `f·25·1.2^f` em Stellar Fuel; drenagem a cada 5 s (×2 carregando bateria).
- Calor: `12601 + log_{1.5}(f)·1000` (base 1,12/1,18 com SEFCP); OC cap 15 000 / 30 000 (CNTI) /
  acima com expoente 0,8–0,85 (NDPE).
- Paralelo base 1024/512/512/64 × CTCDD(2) × SA `1+ef/15` (TCT ×2/×3) × EPEC calor × POS upgrades;
  Exotic usa raiz quadrada com PA. Fator efetivo `f ≤ 43 ? f : 43+(f−43)^0,4`.
- Velocidade: IGCC `heat^-0,01`, DOR `/parallel^0,02|0,012`; desconto REC/IMKG; voltagem
  2·10⁹ (+ef·10⁸ GISS, ×4^anéis NGMS); OC tempo 2 / GGEBE.
- Milestones: constantes 10¹⁵ EU (log 9), 10⁷ receitas (log 4), 10⁴ combustível (log 3).

Essas funções são puras e serão portadas com testes unitários comparando valores do upstream.

## Mapeamento para o GTNA / GTCEu 1.20.1

| Upstream | GTNA |
| --- | --- |
| `TTMultiblockBase` + hatches | `WorkableMultiblockMachine` GTCEu; o controlador não tem receita própria |
| Rede wireless GTNH (`addEUToGlobalEnergyMap`) | `WirelessEnergyManager` / `NexusEnergyNetwork` já usado pelo EOH |
| Módulo ligado ao controlador | mesmo padrão dos módulos auxiliares GTNA (`IGTNAModuleHost`), por posição na estrutura |
| `OverclockCalculator` com heat OC e fator de tempo | `ModifierFunction` própria (não usar OC padrão do GTCEu) |
| Receitas EBF/fornalha/blast smelter | `GTRecipeTypes.BLAST_RECIPES`, fornalha vanilla via lookup, ABS do GTCEu |
| Mapas `godforgePlasma`/`ExoticMatter` | novos `GTRecipeType` GTNA gerados a partir dos materiais com plasma |
| Estrutura em strings | portar strings 1:1 para `.mbs`/pattern; GTO já tem `god_forge.mbs` e os 3 anéis (`StructurePattern`) — conferir contra o upstream antes de usar |
| Blocos/texturas dos casings | GTO já tem os 8 casings com texturas animadas/CTM (CC BY-NC-SA) |
| Estrela GLSL (`star`, `gorgeBeam`, `fadebypass`) + `StarLayer0-2`, `spaceLayer` | portar para shaders core 1.20.1 (`assets/gtna/shaders/core/*.json`) via `RegisterShadersEvent`; o GTO usa um modelo OBJ simples, que **não** é o visual do GTNH |
| Remoção/reconstrução dos anéis quando a estrela acende | o GTO remove só no cliente; o upstream troca blocos no servidor — portar o comportamento do servidor |
| GUI MUI2 (árvore de upgrades, milestones, cores, estatísticas) | LDLib `FancyMachineUIWidget` + telas próprias; maior bloco de trabalho |

## Materiais e itens faltando

Presentes em algum projeto 1.20 conhecido (GTO/GTL) mas não no GTNA: TranscendentMetal,
ExcitedDTEC, MagMatter, Hypogen, CosmicNeutronium. Sem equivalente encontrado: Creon, Mellion,
SixPhasedCopper, DTR (Dimensionally Transcendent Residue), MHDCSM, Graviton Shard, Phonon Medium,
Tengam Attuned, SuperconductorUIV, Time/Space. `RawStarMatter` e `Infinity` já existem no GTNA.
Os combustíveis (Stellar Fuel, DTR, Raw Star Matter, MHDCSM) e o Graviton Shard são **necessários
para a lógica**; os demais só para receitas de fabricação.

## Plano em fases

1. **Núcleo de regras** — `ForgeOfGodsData`, `ForgeOfGodsUpgrade` (31 nós, custos, pré-requisitos,
   splits), `GodforgeMath`, milestones/inversão, NBT; testes unitários de paridade numérica.
2. **Blocos e estrutura** — 8 casings + vidro (texturas GTO), controlador, estrutura principal e
   anéis 2/3 conferidas contra as strings do upstream; formação em GameTest.
3. **Controlador funcional** — combustível de partida, bateria, drenagem, anéis por upgrade,
   compra de upgrades, shards, ejeção, troca de blocos dos anéis ao acender.
4. **Módulos** — base + Smelting e Molten (receitas existentes), depois Plasma (novos tipos de
   receita) e Exotic (receitas aleatórias QGP/MagMatter), consumo pela rede wireless.
5. **Render** — estrela com shaders portados, feixe, cor/tamanho/rotação, anéis girando; desligar
   com chave de fenda.
6. **GUI** — painel principal, combustível, bateria, árvore de upgrades, milestones, estatísticas,
   cores da estrela; tooltips/manual EN/PT.
7. **Receitas e progressão** — materiais faltantes ou substitutos documentados, receitas do
   controlador/módulos/casings, custos dos upgrades; validação com o autor.

Cada fase termina com o gate completo e checkpoint no ledger. A fase 1 não depende de decisões de
balanceamento; a fase 7 depende de escolha do autor (port fiel dos materiais ou substitutos).

## Riscos

- Shaders: o 1.7.10 usa GLSL 1.20/3.30 com GTNHLib; no 1.20.1 o pipeline é `ShaderInstance` com
  formato de vértice fixo. Iris/Oculus pode exigir fallback sem shader.
- Estrutura gigante (127×29×186): checagem e construção precisam de orçamento por tick, como o
  upstream (1000 blocos por chamada).
- Trocar blocos dos anéis no servidor ao acender a estrela precisa ser reversível e não perder
  blocos se o chunk estiver descarregado.
- A GUI do upstream é muito grande; portar fielmente é o item mais longo.
