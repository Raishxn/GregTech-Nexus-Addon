# :zap: Nexus Flux Matrix - Sistema de Energia Inalambrica
> **Estado**: <span class="status-badge status-planned">En Desarrollo - v0.2.0</span>

## Resumen
El **Nexus Flux Matrix** es un multibloque de almacenamiento masivo de energia EU con distribucion inalambrica. Funciona como un `banco central` de energia donde los generadores depositan y las maquinas retiran sin cables.

## Stats Rapidos
| Propiedad | Valor |
|-----------|-------|
| **Tipo** | Multibloque Electrico (expandible) |
| **Tamano** | 3x7x7 (min) a 31x7x7 (max) |
| **Capacitores Internos** | Hasta 750 bloques |
| **Escalabilidad** | Cuadratica: Capacidad x Cantidad / 2 |
| **Eficiencia** | 85% (LV) a 100% (MAX) |
| **Transferencia Max** | 500 ZEU/t |
| **Cross-Dimension** | Si (ZPM+) |
| **Modo Seguro** | Auto en <10%, reactiva en 25% |

## Bloques Capacitores (14 tiers)
| Tier | Capacidad/Bloque | Nombre |
|------|:---:|------|
| LV | 160K EU | Basic |
| MV | 1.5M EU | Advanced |
| HV | 10M EU | Elite |
| EV | 50M EU | Master |
| IV | 250M EU | Ultimate |
| LuV | 1.5G EU | Superior |
| ZPM | 15G EU | Quantum |
| UV | 150G EU | Stellar |
| UHV | 3T EU | Cosmic |
| UEV | 50T EU | Infinite |
| UIV | 900T EU | Ultra |
| UXV | 15P EU | Extreme |
| OpV | 250P EU | Omniscient |
| MAX | 5E EU | Omni |

## Sistema de Seguridad
| Nivel | Accion |
|:---:|------|
| 75% | Aviso en chat |
| 50% | Aviso en chat |
| 25% | Aviso urgente |
| <10% | **Modo Seguro**: corta output, sigue aceptando input |
| 25%+ | Modo Seguro desactivado, output restaurado |

Ver [PRD #5](../../prd/prd_05_nexus_flux_matrix.md) para detalles tecnicos completos.
