# Hoja de ruta pública — GregTech Nexus Addon

Actualizada el **30/09/2026**. Base de desarrollo: **Minecraft 1.20.1 / GTCEu 7.5.3 / GTNA 0.5.1**.

GTNA amplía la industria de vapor, las fábricas eléctricas y la automatización de GregTech
con multibloques, redes inalámbricas, módulos e integración AE2. La configuración permite
adaptar estos sistemas a la progresión de cada modpack.

## Cómo seguir el progreso

- **Implementado:** existe en el desarrollo actual; puede no estar en una versión publicada.
- **En validación:** tiene pruebas, pero falta confirmación en juego o balanceo.
- **Planeado:** siguiente tarea; se marca tras implementarla y verificarla.
- **En estudio:** alcance pendiente de definición; no es una entrega prometida.

Las prioridades pueden cambiar según las pruebas. No hay fechas de lanzamiento comprometidas.
Esta página cubre los sistemas principales, sin enumerar todas las máquinas.

## Implementado

- [x] Redes inalámbricas de vapor y energía, monitorización y Nexus Flux Matrix.
- [x] Máquinas Steam y Large Steam, componentes hidráulicos y módulos del Steam Elevator.
- [x] Nexus Structure Terminal para previsualizar y construir multibloques/módulos registrados.
- [x] Módulos auxiliares en máquinas eléctricas compatibles y cadena de procesamiento de minerales.
- [x] Hatches de aceleración, overclock, paralelos y recetas simultáneas en máquinas compatibles.
- [x] ME Pattern Buffers con modos, capacidad configurable y mejoras que conservan los datos.
- [x] Universal Factory con reglas configurables para compartir recursos entre recetas.
- [x] Electric Void Miner preciso/aleatorio, esencias, World Data Scanners e Incubator.
- [x] Void Fluid Drilling Rig: extracción real, datos reutilizables de descubrimiento y mejoras remotas.
- [x] Integración JEI/Jade, traducciones al inglés/portugués y pruebas automatizadas de los sistemas principales.

## Ahora — estabilizar el contenido en pruebas

- [ ] **GTNA-01 · Minería Void:** comprobar ambos modos, tiempos nuevos, Accelerate Hatch y más de dos paralelos en juego; revisar consumo y salida llena.
- [ ] **GTNA-02 · Descubrimiento de fluidos:** comprobar prospección, extracción, registro de petróleo y Marte → Radon → producción remota con el perfil GTIA; aclarar las instrucciones.
- [ ] **GTNA-03 · Interfaces:** revisar nombres de scanners, probabilidades en Jade, desplazamiento de catálogos grandes en JEI y distintas escalas de interfaz.
- [ ] **GTNA-04 · Equipos:** probar datos compartidos y límites de acceso con dos jugadores reales, incluyendo salida y regreso al equipo.
- [ ] **GTNA-05 · Seguridad de datos:** volver a probar mejoras de Pattern Buffers, redes ME llenas/sin energía, recarga del mundo y migración de capacidad sin pérdida ni duplicación.
- [ ] **GTNA-06 · Energía inalámbrica:** comprobar pérdida única, transferencias entre dimensiones y recuperación de mundos existentes con los perfiles configurables.

**Para completar:** registrar resultados y corregir pérdida de objetos, duplicación,
cuelgues y costes incorrectos. Las pruebas automáticas complementan las pruebas en cliente
y servidor multijugador.

## Después — integración, balanceo y documentación

- [ ] **GTNA-07 · Fábricas reales:** medir autocrafting AE2, recetas simultáneas y rendimiento bajo carga; comprobar CPUs nativas junto a sistemas Nexus.
- [ ] **GTNA-08 · Multibloques/módulos:** ampliar comprobaciones de orientación, formación, previsualización y hatches en máquinas existentes.
- [ ] **GTNA-09 · Configuración de packs:** completar controles de coste, rendimiento, paralelo y habilitación según necesidades verificadas; documentar valores por defecto y ejemplos.
- [ ] **GTNA-10 · Progresión:** revisar recetas, costes y rendimientos Steam, eléctricos y de componentes, priorizando máquinas de uso práctico.
- [ ] **GTNA-11 · Guías/traducciones:** actualizar instrucciones de construcción/uso y mantener idiomas y tooltips coherentes con el comportamiento real.

**Para completar:** las máquinas y configuraciones documentadas deben reproducir el
comportamiento observado en una fábrica real, con costes y límites comprensibles.

## Más adelante — expansión y preparación de versión

- [ ] **GTNA-12 · Nuevos ports:** seleccionar máquinas por necesidades de progresión; verificar estructura, recetas, comportamiento, arte y licencia antes de implementar.
- [ ] **GTNA-13 · Calidad técnica:** reducir duplicación en registros y ampliar pruebas de regresión donde los informes de jugadores revelen problemas.
- [ ] **GTNA-14 · Preparar una versión:** revisar cambios, migración de mundos/configuración, compatibilidad, créditos, changelog e instalación antes de publicar.

Las nuevas familias complejas de máquinas siguen **en estudio** hasta definir alcance y
requisitos. Esta hoja de ruta no aprueba automáticamente todos los ports de referencia.

## Relación con GregTech Infinity Ascension

GTNA es uno de los mods-base de GTIA y sigue siendo utilizable por otros packs. GTIACore
requiere GTNA; GTNA no requiere GTIACore. Las recetas, depósitos planetarios y gates propios
del pack pertenecen al perfil GTIA y pueden diferir de los valores por defecto del addon.

## Ayudar con las pruebas

Indica versión, máquina, configuración relevante, pasos para reproducir y resultado esperado/observado.
Hay una [guía de pruebas de petróleo y Radon](void-fluid-manual-test.md) en portugués.
Consulta los [créditos y licencias](https://github.com/Raishxn/GregTech-Nexus-Addon/blob/main/THIRD_PARTY_NOTICES.md) para el origen del código y arte.

## Eye of Harmony

- [ ] [Eye of Harmony fiel a GTNH](eye-of-harmony-implementation-roadmap.md). En desarrollo: contenido físico, estructura y operación Overworld implementados localmente; QA del cliente pendiente. Fabricación aplazada; otros planetas, viewer y paralelos después. Plan detallado en portugués.

- [Auditoría de fabricación, materiales y planetas](eye-of-harmony-progression-port-audit.md): dependencias verificadas, ruta BEC, adaptación y nombres propuestos. Documento en portugués.
