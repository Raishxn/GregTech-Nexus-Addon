# Universal Factory: balanceamento configurável

O arquivo `config/gtna/balance/universal_factory.json` é criado no primeiro início do jogo.
Os mundos e arquivos antigos continuam em `LEGACY` por padrão. Para usar o perfil inicial do
GTIA, copie os campos de `docs/config-examples/gtia/universal_factory.json` para esse arquivo
antes de iniciar o jogo. Não há alteração automática de configurações de mundos existentes.

- `LEGACY`: mantém threads por tensão, paralelo, aquecimento e lote anteriores.
- `SHARED_BUDGET`: capacidade por tier; a UI escolhe de 1 a 256 threads e cada thread recebe
  `floor(capacidade / threads)` paralelos. O executor não inicia operações que excedam a
  capacidade após considerar receitas já em andamento.
- `UNLIMITED`: disponível somente com `allowUnlimited: true`. Usa o teto técnico de operações,
  ainda limitado a 256 threads e pelos recursos reais de energia, entrada e saída. Sem a permissão,
  a máquina usa `SHARED_BUDGET`.

`allowedRecipeTypes` vazio libera os 32 tipos registrados. Se preenchido, use IDs completos,
como `gtceu:assembler`; a lista restringe novas receitas, inclusive as oferecidas pelo ME Pattern
Buffer. `specialHatchesEnabled` altera o pattern na inicialização e exige reiniciar o jogo.
Warmup e batch desligados tornam seus multiplicadores efetivos iguais a 1; valores persistidos em
mundos antigos são preservados para o caso de retorno ao modo legado.

Limite conhecido desta etapa: mudar o número de threads ou a configuração enquanto uma receita
roda não altera essa receita. O executor passa a considerar a ocupação existente e espera liberar
capacidade antes de iniciar outras. Testes manuais ainda precisam confirmar a UI, o pattern dos
hatches e o desempenho em mundo do GTIA.
