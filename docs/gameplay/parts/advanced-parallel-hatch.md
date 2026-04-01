# 📊 Advanced Parallel Hatch

> *"Quando 256 paralelos não são suficientes..."*

## O que é

As **Advanced Parallel Hatches** do GTNA estendem o sistema de paralelos do GregTech base para números massivos, começando em 1,024 e chegando a 262,144 paralelos.

## Tiers Disponíveis

| Tier | Paralelos | Comparação |
|------|-----------|------------|
| UHV | 1,024 | 4x mais que o GT base UV (256) |
| UEV | 4,096 | 16x mais que UV |
| UIV | 16,384 | 64x mais que UV |
| UXV | 65,536 | 256x mais que UV |
| OpV | 262,144 | 1024x mais que UV |

## Interação com Threads

Quando combinados com Thread Hatches, os paralelos são **distribuídos entre as threads**:

```
Paralelos por Thread = Total_Paralelos / Número_de_Threads
```

!!! example "Exemplo"
    Advanced Parallel UEV (4,096) + Thread Hatch UV (+3 threads):
    
    - Total threads: 4
    - Paralelos por thread: 4,096 ÷ 4 = 1,024
    - Cada thread processa 1,024 cópias de uma receita diferente

## Notas Importantes

- Apenas **1** Parallel Hatch por multiblocko (`.setMaxGlobalLimited(1)`)
- Funciona com qualquer multiblocko que aceite `PartAbility.PARALLEL_HATCH`
- Part sharing **desabilitado** para evitar exploits
