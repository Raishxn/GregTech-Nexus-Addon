# Padrão de tooltips do GTNA

!!! info "Propósito"
    Convenção única para tooltips de controllers, máquinas, hatches, blocos e itens do GTNA,
    adaptada do estilo do GTOCore ao GTCEu 7.5.3. O estado corrente e as pendências ficam no
    `CONTINUITY_LEDGER.md`; aqui fica o *método*.

## Por que

A auditoria de 2026-09-24 (G-0087) encontrou 84 chamadas `.tooltips(...)` sem convenção comum:
algumas máquinas mostravam `Source:` **e** `Added by GregTech Nexus Addon`, outras nenhum; algumas
começavam com um stat sem cor, outras com `Main Function:`; paredes de texto sem hierarquia; e
textos hardcoded não traduzíveis. O GTOCore tem uma casa clara (`ComponentListSupplier` +
`ComponentSlang`), mas a engine dele está no `gtolib` selado — então o GTNA replica a *convenção*,
não a engine.

## Convenção GTNA (adaptada do GTOCore)

### Ordem das linhas

1. **Nome** (vanilla, fora do builder).
2. **Main Function** — `§6Main Function:§r §7<uma linha>`.
3. **Seções** — `§6<Seção>:§r` (dourado), seguidas de linhas indentadas.
4. **Stats** — `§7<Rótulo>:§r §b<valor>` (rótulo cinza, valor aqua) ou `§a<valor>` para bônus.
5. **Módulos** — `§6Auxiliary Module:§r §b<nome>`.
6. **Separador** (só em famílias steam) + **`§8Source: <addon>`**.
7. **Compartilhamento** — `gtceu.part_sharing.disabled`/`enabled` por último.

### Cores semânticas

| Uso | Cor | Código |
|---|---|---|
| Cabeçalho de seção / rótulo | dourado | `§6` |
| Conteúdo neutro | cinza | `§7` |
| Valor / número | aqua | `§b` |
| Input / ação | amarelo | `§e` |
| Bônus / positivo | verde | `§a` |
| Aviso / penalidade | vermelho | `§c` |
| Nota / rodapé | cinza escuro | `§8` |

### Atribuição (decisão do autor, G-0087)

- **Conteúdo portado:** uma única linha `Source: <addon>` (via `GTNASources`), **sem** a linha
  genérica "Added by GregTech Nexus Addon" (removida).
- **Conteúdo original do GTNA:** sem linha de atribuição (o item já é do GTNA).
- A origem continua centralizada em `GTNASources.SOURCES` (fonte única).

### Regras

1. Nada de texto hardcoded: todo tooltip usa chave de lang (en_us + pt_br quando possível).
2. A chave auto `<namespace>.machine.<path>.tooltip` (inserida pelo `MetaMachineBlock` no índice 1)
   não pode ser repetida explicitamente — isso imprime a descrição duas vezes.
3. Chave com argumento (`%s`) nunca pode ter o mesmo nome da chave auto (o auto renderiza `%s` cru).
4. Uma máquina/hatch tem **uma** linha de atribuição.
5. pt_br pode faltar (cai no en_us), mas a chave tem de existir no en_us.
6. **Override do manual:** `src/main/resources/assets/gtna/lang/en_us.json` é lido primeiro pelo
   provider e `add` **deduplica**, então qualquer chave presente lá **sobrescreve** o valor do
   provider (`GTNALangProvider`). Ao mudar um tooltip, confira se a chave está nesse manual — se
   estiver, edite o manual (ou sincronize-o ao provider), senão a edição no provider não aparece.
   `tools/check_lang_parity.py` ajuda a ver a união gerada.

## O que já foi migrado (G-0087)

- Removida a linha genérica `Added by GregTech Nexus Addon` (22 máquinas + hatches steam).
- Corrigidas 5 descrições duplicadas (`me_storage_access_hatch`, `me_big_storage_access_hatch`,
  `me_io_port_hatch`, `infinite_steam_input_bus`, `output_boost_steam_output_bus`).
- `output_boost_item_bus_*` e `output_boost_fluid_hatch_*` ganharam `main_function`.
- Hardcoded → lang: `nexus_me_hypercore`, `me_storage`, `duration_tester`, Artificial Star,
  aviso do Large Steam Furnace.
- Chave errada corrigida (`craft_pattern_hatch` apontava "Nexus Molecular Forge" → "Nexus Assembly
  Forge"); `huge_steam_bus` dividido em input/output.
- Atribuição: `Source:` centralizado em `GTNASources`, com a separação `──────` para **todas** as
  máquinas com origem (antes só steam). Origens corrigidas conforme reteste do autor
  (`large_steam_storage_tank`→GTO, DT dirt forge/boiler/oven→GTL, `eye_of_harmony`→GTNH,
  `nexus_molecular_forge`→GTO, `crafting_cpu_interface` removido, `directed_tesseract_generator` e
  os três ME storage hatches→GTO, ore processors `GTLCORE`→`GTL`). Menções a mod removidas dos
  textos (provider + manuais en_us/pt_br), inclusive as "GT-Not-Leisure" com hífen.

### Convenção aplicada (estilo)

- Integrated/Advanced Ore Processor, Accelerate/Thread/Overclock/Output Boost Hatches, Infinite
  Input / Output Boost (bus/hatch/steam).
- Nexus Assembly Forge, Eye of Harmony, Eye of Wood, Void Miner, Infernal Coke Oven, Hyper Pressure
  e Compact Hyper Pressure, Leap Forward, Industrial Slaughterhouse, Stone Superheater, Steam
  Manufacturer, Steam Woodcutter, Primitive Distillation Tower, Universal Factory, Primitive Stone
  Furnace, Brick Kiln, Thermal Power Pump, Liquefaction Furnace, Industrial Platform Deployment Tools.
- Família steam GTNL (`gtna.tooltip.large_steam_*` / `steam_*`): recolorida mecanicamente (115+115
  chaves) com `§6Main Function:§r` na descrição, `§8` nas estruturas e `§7` no restante.

## Status do pt_br

`tools/check_lang_parity.py` reporta a paridade (fallback é automático para en_us):
`python3 tools/check_lang_parity.py` (informativo) ou `--strict` (falha se faltar).

- `gtna.tooltip.*` e `gtna.multiblock.*`: **100% traduzidos**.
- `gtna.machine.*`: ~246 chaves faltando (UI/HUD e estados, além de nomes); as máquinas migradas
  nesta sessão já estão em pt_br.
- `block.gtna.*` (~482) e `item.gtna.*` (~60): nomes próprios, em inglês por fallback.

## Pendências da migração

- [ ] Aplicar a convenção ao restante das máquinas GTNA-nativas e famílias steam GTNL que ainda
      estão sem cabeçalho dourado/seções.
- [x] Atribuição da família ME (decisão do autor, 2026-09-24): os **pattern buffers e o proxy são
      conteúdo original do GTNA** (sem `Source:`); os ME Storage Access / Big Storage / IO Port
      Hatches são **GTO** (feito); os 10 storage/crafting cores seguem o GTOCore
      (`MEStorageCoreBlock`).
- [ ] Rebuild completo do `pt_br.json` (faltam `gtna.machine.*`, `block.gtna.*`, `item.gtna.*`).
- [ ] Tooltips de item: **Reality Ripper** convertido para chaves (feito); restam os literais de HUD
      do `QuantumTerminalUI` (muitas linhas dinâmicas) e revisar armadura/cartões (já usam chaves).
- [ ] **Conhecido (EMI):** `[EMI] 2 recipes loaded with the same id: gtna:<máquina>` no client de
      singleplayer. O gametest server não acusa e as receitas funcionam; é um artefato da integração
      GTCEu×EMI (receitas runtime no pack dinâmico), não uma receita duplicada no RecipeManager.
