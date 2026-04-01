# ❓ Perguntas Frequentes / FAQ

??? question "O GTNA é compatível com qual versão do GregTech?"
    GregTech CEu Modern **7.4.0+** para Minecraft **1.20.1** com Forge.

??? question "Posso usar o GTNA sem Applied Energistics 2?"
    O AE2 e ExtendedAE são **dependências obrigatórias**. O GTNA usa funcionalidades do AE2 para integração de autocraft e storage.

??? question "As máquinas do GTNA funcionam com KubeJS?"
    Estamos trabalhando na integração KubeJS! Em breve, devs de modpack poderão criar multiblocks usando as hatches do GTNA via scripts KubeJS. Veja o [Roadmap](../roadmap/index.md).

??? question "O que é a Wireless Steam Network?"
    É um sistema que permite transmitir Steam sem fio entre máquinas. Coloque uma **Wireless Steam Output Hatch** no boiler e uma **Wireless Steam Input Hatch** na máquina — o vapor é transmitido automaticamente sem tubos.

??? question "Qual a diferença entre Thread Hatch e Parallel Hatch?"
    - **Parallel Hatch**: Processa a **mesma receita** múltiplas vezes simultaneamente (ex: 64 lingotes de ferro ao mesmo tempo)
    - **Thread Hatch**: Processa **receitas DIFERENTES** simultaneamente (ex: lingotes de ferro E lingotes de cobre ao mesmo tempo)

??? question "O GTNA funciona em servidores?"
    Sim! O GTNA é totalmente compatível com servidores dedicados. O Wireless Steam Network é per-player (cada jogador tem sua própria rede).

??? question "Posso compartilhar paredes entre multiblocks do GTNA e do GT base?"
    Sim! O wall sharing funciona entre multiblocks do GTNA e multiblocks do GregTech base.

??? question "Como reporto bugs?"
    Abra uma issue no [GitHub](https://github.com/Raishxn/GregTech-Nexus-Addon/issues) com:
    
    1. Versão do GTNA e do GTCEu
    2. Log de crash (se aplicável)
    3. Passos para reproduzir o bug
    4. Screenshots (se relevante)

??? question "Posso contribuir com o mod?"
    Sim! Veja o [Guia de Contribuição](../development/contributing.md) para detalhes sobre como configurar o ambiente de desenvolvimento e enviar pull requests.
