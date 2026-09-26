# Auditoria dos módulos GTO no GTNA — 2026-09-26

Fonte comparada: `GTOCore-Main` commit `dc4824d`, padrões de `MultiBlockA`, `MultiBlockD` e
`MachineRegisterUtils`, contra `GTNAModules` e os controladores GTNA.

| Multibloco | Bônus do módulo | Posições que aceitam os hatches | Verificação |
| --- | --- | --- | --- |
| Electric Blast Furnace | 1 Energy Hatch adicional; 1 Accelerate Hatch | Invar Heatproof `A` | GameTest de formação, limite e rejeição no corpo principal |
| Liquefaction Furnace | 1 Parallel Hatch; 1 Accelerate Hatch | Invar Heatproof `A` na torre inox | Padrão e registro comparados com GTO; formação da base testada |
| Evaporation Plant | 1 Parallel Hatch; 1 Accelerate Hatch | Stainless Evaporation `F` e `G`, mais Titanium Stable `A` exposto | GameTest cobre os encaixes `F` e `G/A` sem force scan; IV Parallel = 4 |
| Cold Ice Freezer | Atomization/Condensation; até 6 Energy Hatches; 1 Accelerate Hatch | Cold Ice Casing `B` da torre | GameTest do desbloqueio, limites e rejeição no corpo principal |
| Rocket Large Turbine | 2× velocidade/saída, +20% eficiência, 2× desgaste do rotor; até 3 Energy Output Hatches | Titanium Turbine Casing `D` do módulo | GameTest de formação e bônus; limite de hatches conferido no padrão |
| Supercritical Steam Turbine | Mesmo bônus de turbina; até 3 Energy Output Hatches | Supercritical Turbine Casing `D` do módulo | GameTest de formação e bônus; limite de hatches conferido no padrão |
| Component Assembler | 1º módulo: cap até UV e Accelerate Hatch; 2º: Parallel Hatch e Laser Hatch | Aço `A` da primeira extensão; aço `D` da segunda | GameTests de ambas extensões e hatches |

O Parallel Hatch IV do GCYM fornece quatro paralelos pela fórmula da própria hatch (`4^(tier-EV)`).
Na Evaporation Plant, `F` e `G` têm a mesma textura, enquanto a casca de titânio `A` é a parte
exposta. O GTO original habilita os hatches somente em `F`; o teste in-game do autor mostrou que
isso não os tornava instaláveis de forma confiável. O GTNA aceita os dois hatches em `F`, `G` e `A`,
mantendo **um** de cada habilidade globalmente e rejeitando-os no corpo principal.

Pendente de confirmação visual do autor: prévia dos hatches da Evaporation Plant, UI da Component
Assembly Line, texturas dos oito casings novos e tooltip Chemical Plant/Supercritical Steam Turbine.
