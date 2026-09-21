# ☀️ Large Steam Solar Boiler

> *"Enquanto houver sol e água, há vapor."*

## Stats Rápidos

| Propriedade | Valor |
|------------|-------|
| **Tipo** | Multibloco a Vapor (expansível na horizontal) |
| **Tamanho** | Dinâmico: 5×1×5 (mín) — expande para largura/profundidade |
| **Tamanho máximo** | 63 células para cada lado, 125 para trás |
| **Receitas** | Geração automática (sem receita manual) |
| **Ciclo** | 20 ticks (1 segundo) |
| **Produção** | 200 mB por **Solar Boiling Cell iluminada** por ciclo |
| **Consumo** | Água proporcional ao vapor gerado (`steamPerWater` do GTCEu) |
| **Requisitos** | Dia, sem chuva, e visão direta do céu acima de cada célula |

## Estrutura

- **Borda**: Steel Hull (ou os hatches de fluido)
- **Interior**: Solar Boiling Cell (cada uma precisa ver o céu)
- **Controller**: na borda
- **Hatches**: Fluid Input (água) e Fluid Output (vapor) substituem Steel Hull na borda

```
Vista de cima (exemplo 7×5):
AAAAAAA
ABBBBBA
ABB~BBA   ← Controller (na borda)
ABBBBBA
AAAAAAA

A = Steel Hull (ou hatch)   B = Solar Boiling Cell   ~ = Controller
```

## Mecânica

- O sistema só produz durante o **dia** e **sem chuva**.
- Cada célula com `level.canSeeSky(pos.above())` conta como **iluminada**.
- A cada ciclo de 20 ticks o boiler gera `células iluminadas × 200 mB` de vapor e consome a água
  correspondente (divisão por `steamPerWater`).

## Dicas

!!! tip "Espaço wireless"
    Combine com a **Wireless Steam Output Hatch** para distribuir o vapor sem tubos.

!!! tip "Posicionamento"
    Construa no ponto mais alto possível: qualquer bloco sobre uma célula a desliga.

!!! warning "Só de dia"
    À noite ou na chuva a produção para. Tenha um buffer de vapor ou boilers de backup.

## Diferença para os boilers a combustível

O Large Steam Solar Boiler **não consome combustível** — só água. Em troca, depende do clima/horário e
de uma área grande com acesso ao céu.
