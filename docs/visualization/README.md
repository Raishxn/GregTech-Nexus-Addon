# Visualizacao do Integrated Ore Processing (estruturas + modulos)

Pacote **offline** de pre-visualizacao dos dois multiblocos de ore processing do GTNA e de uma
proposta de modulos auxiliares. Nada aqui e registrado no mod: as estruturas 3D sao lidas dos
patterns reais de `GTNAMachines.java` e os modulos sao uma proposta de design.

## Arquivos

| Arquivo | O que e |
| --- | --- |
| `gtna-ore-processing-preview.html` | Viewer interativo autossuficiente (canvas 2D, sem dependencias, texturas embutidas em base64). Abra direto no navegador. |
| `gallery/*.png` | Imagens 1920x1080 prontas para a galeria do Modrinth / CurseForge. |
| `../../tools/gtna_ore_preview/build_preview.py` | Extrator dos patterns + compositor de texturas + gerador do HTML e das imagens. |
| `../../tools/gtna_ore_preview/viewer_template.html` | Template do viewer (o builder injeta os dados e as texturas). |

### Viewer interativo

- **Maquina**: alterna entre Integrated (EV) e Advanced (endgame).
- **Vista**: 3/4 SO, 3/4 SE, frente, lateral, topo; arraste para girar, roda do mouse para zoom.
- **Camada Y / Explodir**: corta a estrutura por altura e separa os blocos, para inspecionar o
  miolo (chamine, coluna central, hall de vidro).
- **Modulos**: liga/desliga cada modulo proposto; os blocos do modulo ficam com contorno na cor
  do modulo.
- **Abas**: Estrutura (dimensoes, tooltip, checklist com a contagem real de blocos), Modulos
  (bonus, status de implementacao e o snippet KubeJS sugerido), Circuitos (cadeias 1..7, fluidos
  e requisitos) e Notas (como a geometria foi interpretada).
- **Hover**: nome do bloco, o modulo a que pertence e se e um hatch proposto.

Parametros uteis na URL: `?machine=advanced&tab=modules`, `?tab=circuits`, e (para captura)
`?shot=hero|circuits|modules&modules=0|1&yaw=..&pitch=..&layer=..&parts=0`.

### Imagens de galeria

| Imagem | Uso sugerido |
| --- | --- |
| `gallery/01-integrated-overview.png` | imagem de capa (Integrated) |
| `gallery/02-integrated-modules.png` | modulos na ala leste (annex + rack) |
| `gallery/03-integrated-modules-west.png` | modulos na ala oeste/traseira (coluna + tank) |
| `gallery/04-advanced-overview.png` | capa alternativa (Advanced) |
| `gallery/05-advanced-modules.png` | modulos endgame |
| `gallery/06-circuits.png` | infografico dos 7 circuitos |
| `gallery/07-modules-board.png` | infografico dos 8 modulos propostos |

Todas saem em 1920x1080 (16:9), PNG, ~0,7-1,1 MB.

## Como regenerar

```bash
python3 tools/gtna_ore_preview/build_preview.py --check    # so valida o parsing dos patterns
python3 tools/gtna_ore_preview/build_preview.py --dump     # fatias ASCII da geometria (debug)
python3 tools/gtna_ore_preview/build_preview.py --html     # escreve o viewer
python3 tools/gtna_ore_preview/build_preview.py --html --gallery   # + screenshots (chromium headless)
python3 tools/gtna_ore_preview/build_preview.py --html --gallery --only 02-integrated-modules
```

O builder usa o checkout do GTCEu em `/home/raishxn/MineProjects/GTCEu-7.5.3` para as texturas
(`GTCEU_CANDIDATES` no topo do script). As texturas do GTNA saem de `src/main/resources`.

## Convencao da geometria (para quem for ler o codigo)

`FactoryBlockPattern.start()` usa `charDir=LEFT, stringDir=UP, aisleDir=FRONT`:

- o **char** cresce para a esquerda do controller;
- a **row** cresce para cima (a row 0 e o fundo);
- a **aisle** cresce para a frente do controller.

Relativo ao controller: `X = -(char - charController)`, `Y = row - rowController`,
`Z = -(aisle - aisleController)`. No Integrated (6x12x11 no pattern) o controller fica em
`aisle 4 / row 2 / char 8`; no Advanced (32x12x15) em `aisle 31 / row 2 / char 1`.

Os modulos propostos sao ancorados no controller exatamente como o exemplo KubeJS de sub-pattern
(`examples/kubejs/server_scripts/gtna_integrated_ore_module.js`): o snippet de cada um ja esta no
formato `GTNAServerEvents.subPatterns(...)` e pode ser colado em `kubejs/server_scripts`.

## Limitacoes

- Sem CTM: os casings usam a textura base, nao as variantes conectadas do jogo.
- O sombreamento e aproximado (sem AO por vertice) e os hatches/buses sao as posicoes
  **propostas** de IO (a base aceita esses hatches nas celulas de casing do pattern).
- As imagens sao mockups geometricos fieis ao pattern, nao screenshots do jogo. Texturas do
  GTCEu (LGPLv3) e do proprio GTNA; a atribuicao do GTCEu continua valendo em
  `THIRD_PARTY_NOTICES.md`.
