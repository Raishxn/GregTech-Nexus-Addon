# :books: Research Engine

A data-driven research tree that other systems can read. A modpack describes **nodes** in JSON; a team **unlocks** them by obtaining an item; a node can then **gate machine recipes** or switch **flags** that other code reads. GTNA ships the engine only. The nodes belong to the modpack.

## For players

- Press **K** (rebindable) to open the research tree. Nodes are grouped by tier; lines connect a node to its prerequisites. Green is unlocked, yellow is the next one you can unlock, grey is locked. Hover a node for what it needs and what it unlocks.
- A node unlocks by itself once its prerequisites are met and you hold the item it asks for (crafting, picking up or machine output all count). One teammate holding it is enough for the whole team.
- A gated recipe shows a red **Research: <node>** line in EMI (green once unlocked), and a machine that cannot run it says *Requires research: <node>*.

Progress belongs to the player's effective **FTB Team**, or to the player when there is none, the same rule as fluid discoveries.

## For pack authors

Put one JSON file per node in `data/<namespace>/gtna_research/`. The node id is the file path: `data/gtia/gtna_research/lv/aluminium.json` is `gtia:lv/aluminium`.

```json
{
  "tier": 2,
  "icon": "gtceu:aluminium_ingot",
  "prerequisites": ["gtia:lv/blast_furnace"],
  "trigger": { "type": "obtain_item", "item": "gtceu:aluminium_dust" },
  "grants": [
    { "type": "recipe_condition", "recipes": ["gtceu:electric_blast_furnace/blast_aluminium"] },
    { "type": "flag", "id": "gtia:aluminium" }
  ],
  "quest": "optional text, not used by the engine"
}
```

| Field | Meaning |
|---|---|
| `tier` | Column group in the tree, 1 or higher. Default 1. |
| `icon` | Item shown in the tree. Defaults to the trigger item. |
| `prerequisites` | Node ids that must be unlocked first. |
| `trigger` | `obtain_item` (unlocks when the team holds the item) or `manual` (commands and quests only). Default `manual`. |
| `requires_mods` | Optional list of mod ids. If any is missing the node is skipped silently, so a pack can ship nodes for optional mods. Do not make other nodes depend on it. |
| `grants` | `recipe_condition` blocks the listed recipe ids until the node is unlocked. `flag` sets a switch read by `KnowledgeService.hasFlag`. |

Rules the loader enforces: a node with an unknown prerequisite, a prerequisite cycle or a tier below 1 is **dropped and logged**, together with everything that depends on it. A field that is present but invalid fails the whole node, instead of silently using a default.

Choose trigger items the player can reach **without** the node's own gate, or the node can never unlock. A good trigger is the machine or ingredient that shows the player is ready.

Names come from lang keys: `gtna.research.node.<namespace>.<path with dots>` (for the example, `gtna.research.node.gtia.lv.aluminium`), with an optional `.desc` key for the tooltip. Without a key, the id is shown.

A recipe with a gate runs only for an owner whose team holds the node. Crafting-table, AE2 and pattern recipes are not covered.

## Commands

| Command | Who | Effect |
|---|---|---|
| `/gtna research list [player]` | anyone (others need op) | Unlocked and locked nodes. |
| `/gtna research info <node>` | anyone | Tier and prerequisites. |
| `/gtna research unlock <player> <node> [force]` | op | Unlock a node; `force` unlocks its whole prerequisite chain. |
| `/gtna research reset <player>` | op | Clear the player's team progress. |

## For developers

- `KnowledgeService`: `isUnlocked`, `unlock`, `hasFlag`, `recipeAllowed`, `reset`. Server side.
- `KnowledgeUnlockedEvent` is posted on the Forge bus when a scope newly unlocks a node.
- FTB Quests: the **Research node** task type (`gtna:research_node`) completes when the node is unlocked. It is registered only when FTB Quests is installed.
- The server sends the graph and the player's unlocked nodes to the client on login, after `/reload` and when progress changes.

An example datapack is in `examples/datapack`.
