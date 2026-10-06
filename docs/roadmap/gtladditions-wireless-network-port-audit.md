# GTLAdditions — auditoria dos terminais wireless para GTNA

Fonte local verificada em 2026-10-05: `/home/raishxn/MineProjects/GTLAdditions`,
branch `master`, commit `8caff5e93a5e65914d10dd176d48d66e7ec8c329`.
Metadata: GTLAdditions 3.2.8Custom-fix2, Minecraft 1.20.1, Forge 47.3.7,
GTCEu 1.4.4, LDLib 1.0.33.b. GTNA usa GTCEu 7.5.3; port não é cópia binária.
Nenhum código ou asset da origem foi copiado nesta auditoria.

## Comportamento confirmado

- `common/machine/GTLAddMachines.kt`: dois parts, entrada e saída. Entrada registra
  INPUT_ENERGY/INPUT_LASER; saída OUTPUT_ENERGY/OUTPUT_LASER. Tier nominal MAX.
- `WirelessEnergyNetworkTerminalPartMachineBase.kt`: UUID persistido, vínculo ao
  jogador que coloca; Data Stick vincula por clique direito/desvincula por esquerdo.
  Part não compartilhável, sem GUI própria, conexão via trait de energia.
- `common/machine/trait/NetworkEnergyContainer.kt`: sem buffer físico intermediário;
  receitas leem/escrevem a conta UUID via GTMThings. Tensão nominal Long.MAX_VALUE,
  intenção de amperagem 1. Transações comuns continuam Long por chamada; saldo da
  conta é BigInteger. Não é energia infinita em multiblocos comuns.
- `WirelessEnergyNetworkTerminalPartMachine.kt`: adicionalmente fornece um handler
  BigInteger a controllers que implementam IWirelessElectricMultiblockMachine.
  Portar o terminal comum não exige portar todas as máquinas/lógica wireless do GTL.
- Guide documenta uso especial de saída infinita em Heart of the Universe. Esse
  comportamento depende da máquina e não faz parte do port mínimo dos dois parts.
- `WirelessEnergyManagerMixin.java` substitui rota GTMThings BigInteger e registra
  máquina/UUID/última transferência em cache fraco de cinco segundos. Backend da
  origem inclui contas por time; nossa rede Nexus já tem saldo Int128, persistência,
  perdas, política dimensional, capacidade de matrix e registro de transferências.

## Aplicação ao Artificial Star / EOH

Um terminal de saída nominal Long.MAX_VALUE alcança 9.223.372.036.854.775.807 EU
por operação, acima da receita Infinity atual (36.028.797.018.963.968 EU/t).
Pode substituir a montagem de 16 dynamos no Star, desde que a integração GTCEu de
busca/modificador não force overclock que exceda o teto por operação. Testar a
receita real, overclock e a transferência completa, não apenas comparar números.

EOH já debita diretamente uma conta Nexus e aceita custo acima de Long.MAX_VALUE
via Int128. O novo part não deve forçar hatch energético no EOH nem duplicar seu
débito. Deve servir ao Star e outros multiblocos convencionais que aceitam as
abilities correspondentes. Manter Quantum Terminal/Flux Matrix como observadores
da mesma rede; nenhuma migração automática de saldo GTMThings ou rede duplicada.

## Adaptações necessárias

1. Implementar em Java sobre GTCEu 7.5.3, mantendo UUID/NBT, sides e capabilities
   compatíveis; usar rede Nexus e vínculo existente, sem dependência obrigatória GTL.
2. Entrada: simulação nunca altera saldo; retirada real deve falhar sem consumo se
   rede offline/dimensão bloqueada/energia insuficiente. Long.MAX é teto da operação,
   saldo real permanece Int128 e não pode ser truncado para negativo.
3. Saída: aplicar perda uma única vez, respeitar capacidade e contabilizar somente
   energia bruta realmente aceita. Rede cheia/offline não pode descartar combustível
   ou indicar aceitação fictícia. A simulação precisa corresponder ao commit.
4. Monitorar conexão e tráfego nos painéis existentes, sem tratar débito inicial EOH
   como taxa contínua. Revisar reconexão, unload, desvincular e reload do part.
5. Corrigir inconsistências da origem: getOutputAmperage retorna outputVoltage em
   vez do campo outputAmperage; isso seria overflow ao agregar containers. Não usar
   soma Long sem checar overflow de múltiplos conteúdos. Não declarar saída aceita
   só porque UUID existe quando nossa rede pode recusar crédito.
6. Receitas originais exigem Suprachronic Assembly Line, materiais e itens KubeJS
   ausentes; adaptar progressão com ingredientes GTNA existentes. Reusar assets
   próprios ou avaliar origem/licença antes de copiar casing/overlay GTLAdditions.

## Licença / atribuição

Existe divergência na origem: `LICENSE` contém GPL-3.0, mas `gradle.properties`
declara `mod_license=LGPLv3.0`. Não afirmar que uma autorização específica foi
concedida nem transferir a declaração LGPL do metadata para arquivos GPL. Registrar
origem e termos efetivos se houver código/assets derivados; nenhum foi copiado aqui.

## Validação prevista para implementação

Part vinculado/desvinculado; entrada/saída; simulação sem mutação; rede cheia;
capacidade parcial; perdas e resto; dimensão/matrix inválida; BigInteger/Int128
acima de long com chamada long saturada corretamente; energia agrupada sem overflow;
Star Infinity real pelo novo part; reload e painéis. Gate local padrão completo.
Escopo do port solicitado ao autor: dois parts mínimos ou também máquinas/lógica
wireless adicionais. Auditoria não é implementação nem aprovação visual/publicação.

## Implementação G-0169

Autor autorizou o port dos dois terminais em 2026-10-05. Implementação Java adaptada
nas classes NexusNetworkTerminalPartMachine/NexusNetworkEnergyContainer, sobre a
rede Nexus existente, sem importar máquinas/lógica wireless especiais do GTL.
Tipos GTCEu 7.5.3 usam EnergyStack (voltage/amperage), em vez do Long da origem.
Os dois arquivos adaptados retêm GPL-3.0-only e licença distribuída; metadata LGPL
contraditório não foi usado para reclassificá-los. Assets próprios já existentes.

Registro: GTNAEnergyHatches.NETWORK_INPUT_TERMINAL/NETWORK_OUTPUT_TERMINAL;
IDs `gtna:wireless_energy_network_input_terminal` e `gtna:wireless_energy_network_output_terminal`.
Abilities correspondentes energy/laser. Conta UUID persistida, Data Stick direito
vincula/esquerdo desvincula, Nexus Linker usa NetworkID; shift+Linker desvincula.
Não compartilháveis, sem cabos, sem segunda conta/buffer. Matrix existente exigida
conforme política Nexus. Output full/recusado não dá sucesso à receita.

Receitas de Assembly Line adaptadas: 16 hatches wireless UHV 4096A correspondentes,
16 sensores UV, 16 field generators UV, 64 Gravi Stars, 64 placas duplas Neutronium,
46.080 mB Soldering Alloy; 2400 ticks em MAX, pesquisa do hatch precursor (512 CWU/t).
Não dependem de materiais/itens KubeJS/GTL ausentes.

Long.MAX_VALUE é teto comum por chamada. NominalVoltage/capacity dividem orçamento
long pelos terminais associados, reservando capacidades dos hatches comuns, evitando
overflow de EnergyContainerList. Saldo Nexus continua Int128; vista long saturada e
limitada ao orçamento do part. Mais terminais não multiplicam a energia armazenada.

## Conferência manual

1. Colocar terminal e conferir source tooltip/nomes/texturas no EMI/Jade.
2. Data Stick vincular/desvincular; copiar vínculo da Flux Matrix com Nexus Linker,
   aplicar no terminal e desvincular com shift. Reabrir mundo para conferir UUID.
3. Substituir saída do Star por um Network Output Terminal em posição S. Terminal
   fornece OUTPUT_ENERGY/OUTPUT_LASER; preservar buses e abastecer Infinity. Formar
   estrutura inteira de 109 slices e conferir fluxo para a mesma conta da matrix.
4. Encher rede: receita não pode iniciar/continuar debitando combustível enquanto
   saída não couber. Liberar espaço e verificar retomada sem crédito duplicado.
5. Instalar entrada em multibloco consumidor compatível e verificar débito, pausa
   sem energia, matrix inválida, dimensão bloqueada e vários parts sem overflow.
6. Quantum Terminal/Flux Matrix: conexão de terminal e geração/consumo registrados;
   EOH mantém débito direto inicial e não requer novo hatch energético.

Testes automáticos isolam capacidades/transações/modificador real do Star; não
substituem teste da formação física inteira e aparência. Nada publicado.


## Correções G-0170

Os nomes dos dois blocos agora são emitidos explicitamente pelo GTNALangProvider
(en_us/en_ud), além do pt_br existente. As conexões novas reportam a chave do nome
para tradução no cliente, em vez de congelar o nome em inglês.

Casing e overlay animados são os assets originais do GTLAdditions, incluindo mcmeta.
O modelo emissivo foi adaptado ao GTCEu 7.5.3 com cube de base e pequeno afastamento
da face para evitar z-fighting. Origem, hashes e adaptações estão em
`gtladditions-wireless-assets.json`; licença GPL e conflito de metadata documentados
em THIRD_PARTY_NOTICES. Esta correção substitui os modelos provisórios de G-0169.

Matrix máxima significa 750 capacitores, todos MAX, dentro da estrutura realmente
validada. Capacitores externos não contam. Nesse estado armazenamento e limite de
transferência da rede aparecem como ∞. O saldo depositado usa BigInteger, sem teto
Int128; persistência mantém a mesma chave decimal Amount, compatível com saves
anteriores. A Matrix não gera energia por estar ilimitada. Hatches/receitas ainda
respeitam suas APIs GTCEu long por operação. Vistas compatíveis e contadores de
tráfego Int128 saturam em MAX, sem alterar ou truncar o saldo real.

Ao retirar um capacitor, baixar seu tier ou invalidar a Matrix, o modo ilimitado é
removido. Saldo já depositado é conservado; uma capacidade finita menor que o saldo
impede novos depósitos até haver espaço. Painéis e tooltip do EOH exibem saldo exato.

Conferência manual adicional: visualizar animação/emissão e nomes em EN/PT; formar
750 MAX, verificar ∞ nos dois painéis; alterar um capacitor e conferir volta ao modo
finito; relogar e conferir saldo. A formação real e reload são cobertos também pelos
GameTests específicos, sem afirmar aprovação visual do autor.
