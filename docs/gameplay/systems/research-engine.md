# :books: Motor de Pesquisa

Uma árvore de pesquisa guiada por dados que outros sistemas podem consultar. O modpack descreve os **nós** em JSON; o time **libera** cada nó ao obter um item; o nó pode então **bloquear receitas de máquina** ou ligar **flags** lidas por outros códigos. O GTNA traz só o motor. Os nós pertencem ao modpack.

## Para jogadores

- Aperte **K** (rebindável) para abrir a árvore de pesquisa. Os nós ficam agrupados por tier e linhas ligam cada nó aos pré-requisitos. Verde é liberado, amarelo é o próximo que você pode liberar e cinza é bloqueado. Passe o mouse num nó para ver o que ele pede e o que libera.
- Um nó libera sozinho quando os pré-requisitos estão cumpridos e você tem o item pedido (fabricar, pegar ou sair de uma máquina, tudo conta). Basta um colega do time ter o item.
- Uma receita bloqueada mostra no EMI uma linha vermelha **Pesquisa: <nó>** (verde depois de liberada), e a máquina que não pode rodá-la avisa *Requer pesquisa: <nó>*.

O progresso pertence ao **time FTB** efetivo do jogador, ou ao próprio jogador se não houver time, a mesma regra das descobertas de fluidos.

## Para autores de pack

Coloque um arquivo JSON por nó em `data/<namespace>/gtna_research/`. O id do nó é o caminho do arquivo: `data/gtia/gtna_research/lv/aluminium.json` é `gtia:lv/aluminium`.

```json
{
  "tier": 2,
  "icon": "gtceu:aluminium_ingot",
  "prerequisites": ["gtia:lv/blast_furnace"],
  "trigger": { "type": "obtain_item", "item": "gtceu:aluminium_dust" },
  "grants": [
    { "type": "recipe_condition", "recipes": ["gtceu:electric_blast_furnace/blast_aluminium"] },
    { "type": "flag", "id": "gtia:aluminium" }
  ]
}
```

| Campo | Significado |
|---|---|
| `tier` | Grupo de colunas na árvore, 1 ou mais. Padrão 1. |
| `icon` | Item mostrado na árvore. Padrão: o item do gatilho. |
| `prerequisites` | Ids de nós que precisam estar liberados antes. |
| `trigger` | `obtain_item` (libera quando o time tem o item) ou `manual` (só comandos e quests). Padrão `manual`. |
| `requires_mods` | Lista opcional de ids de mods. Se algum faltar, o nó é ignorado sem erro, para o pack poder trazer nós de mods opcionais. Não faça outros nós dependerem dele. |
| `grants` | `recipe_condition` bloqueia os ids de receita listados até o nó ser liberado. `flag` liga um interruptor lido por `KnowledgeService.hasFlag`. |

Regras do carregador: um nó com pré-requisito desconhecido, ciclo de pré-requisitos ou tier abaixo de 1 é **descartado e registrado no log**, junto com tudo que depende dele. Um campo presente mas inválido derruba o nó inteiro, em vez de usar um padrão em silêncio.

Escolha itens de gatilho que o jogador alcança **sem** o gate do próprio nó, ou o nó nunca libera. Um bom gatilho é a máquina ou o ingrediente que mostra que o jogador está pronto.

Os nomes vêm de chaves de idioma: `gtna.research.node.<namespace>.<caminho com pontos>` (no exemplo, `gtna.research.node.gtia.lv.aluminium`), com uma chave `.desc` opcional para o tooltip. Sem chave, aparece o id.

Uma receita com gate só roda para um dono cujo time tenha o nó. Receitas de bancada, do AE2 e padrões não são cobertas.

## Comandos

| Comando | Quem | Efeito |
|---|---|---|
| `/gtna research list [jogador]` | qualquer um (os outros exigem op) | Nós liberados e bloqueados. |
| `/gtna research info <nó>` | qualquer um | Tier e pré-requisitos. |
| `/gtna research unlock <jogador> <nó> [force]` | op | Libera um nó; `force` libera toda a cadeia de pré-requisitos. |
| `/gtna research reset <jogador>` | op | Limpa o progresso do time do jogador. |

## Para desenvolvedores

- `KnowledgeService`: `isUnlocked`, `unlock`, `hasFlag`, `recipeAllowed`, `reset`. Lado do servidor.
- `KnowledgeUnlockedEvent` é publicado no barramento do Forge quando um escopo libera um nó pela primeira vez.
- FTB Quests: a tarefa **Research node** (`gtna:research_node`) conclui quando o nó é liberado. Só é registrada se o FTB Quests estiver instalado.
- O servidor envia o grafo e os nós liberados do jogador ao cliente no login, após `/reload` e quando o progresso muda.

Há um datapack de exemplo em `examples/datapack`.
