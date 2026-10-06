# :books: Motor de Investigación

Un árbol de investigación basado en datos que otros sistemas pueden consultar. El modpack describe los **nodos** en JSON; el equipo **desbloquea** cada nodo al obtener un objeto; el nodo puede entonces **bloquear recetas de máquina** o activar **flags** que lee otro código. GTNA incluye solo el motor. Los nodos pertenecen al modpack.

## Para jugadores

- Pulsa **K** (reasignable) para abrir el árbol. Los nodos se agrupan por tier y las líneas unen cada nodo con sus requisitos. Verde es desbloqueado, amarillo es el siguiente que puedes desbloquear y gris es bloqueado. Pasa el ratón sobre un nodo para ver qué pide y qué desbloquea.
- Un nodo se desbloquea solo cuando se cumplen los requisitos y tienes el objeto que pide (fabricar, recoger o salir de una máquina, todo cuenta). Basta con que un compañero lo tenga.
- Una receta bloqueada muestra en EMI una línea roja **Investigación: <nodo>** (verde al desbloquearse), y la máquina que no puede ejecutarla avisa *Requiere investigación: <nodo>*.

El progreso pertenece al **equipo FTB** efectivo del jugador, o al propio jugador si no tiene equipo, la misma regla que los descubrimientos de fluidos.

## Para autores de packs

Un archivo JSON por nodo en `data/<namespace>/gtna_research/`. El id del nodo es la ruta del archivo. Campos: `tier`, `icon`, `prerequisites`, `trigger` (`obtain_item` o `manual`), `grants` (`recipe_condition` y `flag`). Un nodo con requisito desconocido, ciclo o tier menor que 1 se descarta y se registra en el log, junto con sus dependientes. Un campo presente pero inválido descarta el nodo entero.

Elige objetos de disparo que el jugador alcance **sin** el bloqueo del propio nodo. Los nombres vienen de claves de idioma `gtna.research.node.<namespace>.<ruta con puntos>`. Las recetas de mesa de crafteo, AE2 y patrones no están cubiertas.

Hay un datapack de ejemplo en `examples/datapack` y la referencia completa en la versión en inglés de esta página.

## Comandos

`/gtna research list [jugador]`, `/gtna research info <nodo>`, `/gtna research unlock <jugador> <nodo> [force]` y `/gtna research reset <jugador>` (los dos últimos requieren op).
