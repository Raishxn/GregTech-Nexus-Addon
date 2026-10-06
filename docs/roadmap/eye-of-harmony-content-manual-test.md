# Eye of Harmony — inspeção visual do primeiro lote

Estado: texturas dos blocos e do item aprovadas pelo autor em 04/10/2026. Demais checks manuais ainda não confirmados. Este teste fecha a parte
visual de EOH-01; não representa aprovação da operação fiel nem autorização de publicação.

## Preparação

Usar o cliente de desenvolvimento desta checkout e um mundo de teste em Creative. Quando o autor
pedir para abrir o jogo, usar `./gradlew runClient --offline`. O launcher Prism já instalado
não recebe este lote automaticamente. Para a primeira inspeção, usar o resource pack padrão,
sem shader; depois repetir com o pack/shader habitual se desejado.

## Galeria dos campos

Na aba de blocos GTNA, procurar os geradores de campo de Compressão do Espaço-Tempo,
Aceleração Temporal e Estabilização. Em inglês: Spacetime Compression, Time Acceleration,
Stabilisation. Colocar três fileiras de nove blocos, uma por família, separados para observar
as faces. A ordem interna é 0–8; o tooltip apresenta 1–9.

| Tooltip | Nome de referência |
|---|---|
| 1 / 9 | Crude / Rudimentar |
| 2 / 9 | Primitive / Primitivo |
| 3 / 9 | Stable / Estável |
| 4 / 9 | Advanced / Avançado |
| 5 / 9 | Superb / Superior |
| 6 / 9 | Exotic / Exótico |
| 7 / 9 | Perfect / Perfeito |
| 8 / 9 | Tipler |
| 9 / 9 | Gallifreyan |

- [ ] As três famílias têm nove entradas; os tooltips identificam família, tier e origem.
- [ ] Ícones do inventário e blocos colocados não mostram textura roxa/preta ou faces vazias.
- [ ] Observar por pelo menos 20 segundos: animações com frames preservados, sem flashes de
  imagens inteiras empilhadas, cortes ou travamento aparente. Algumas texturas são estáticas.
- [ ] Conferir os nove tiers de cada família. A estabilização 1–8 usa a arte original GTNH,
  pois essas variantes não foram encontradas no Modernity local; avaliar a diferença de estilo.
- [ ] Quebrar um tier inicial e um final de cada família em Survival com picareta adequada:
  o drop deve devolver exatamente o próprio bloco e tier.

## Casings, planetas e item

- [ ] Colocar os casings de fronteira energética, estrutura espacial e estrutura temporal;
  conferir ícone, faces e animação. São três registros novos.
- [ ] Colocar Planet Blocks do Overworld, Nether e End, elevados para observar também a base.
  Conferir as seis faces e seus alinhamentos; tooltip indica a dimensão correspondente.
- [ ] Na aba de itens GTNA, conferir o Fabricador de Arranjo Astral (Astral Array Fabricator):
  ícone/animação, nome e tooltip.
- [ ] Alternar português e inglês; nenhum novo nome ou tooltip deve aparecer como chave bruta.
- [ ] Conferir que os novos componentes aparecem no JEI/EMI, se presente. Ainda não há receitas
  de fabricação nem uso operacional desses registros; os tooltips informam essa etapa.

## Resultado e próximos passos

Registrar quais blocos/tiers falharam, idioma e resource packs usados. Uma captura do problema
ajuda a localizar face, orientação ou frame. Não é necessário testar consumo energético ou montar
o Eye completo neste lote: a máquina existente ainda usa os registros e regras anteriores.

Depois deste resultado: corrigir eventual apresentação e continuar EOH-02 (materiais/receitas),
EOH-03 (estrutura e leitura de tiers) e EOH-04 (primeira operação planetária). Os overlays do
controlador estão preservados como assets, mas ainda não aplicados ao renderer.

## Confirmação do autor — 04/10/2026

O autor confirmou: “texturas dos blocos OK e a do item também”, com captura da galeria
no cliente de desenvolvimento. A aprovação cobre a aparência das texturas dos blocos e do
Astral Array. Não foi relatada falha visual. A captura está em
`/tmp/codex-clipboard-d3737668-4651-4b8a-9360-49709ced652e.png` (arquivo temporário).

Animações ao longo do tempo, todas as seis faces planetárias, drops em Survival, troca de
idioma e exposição no JEI/EMI não foram confirmados individualmente neste retorno.
