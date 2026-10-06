# Teste manual de descoberta e produção de fluidos

Ambiente: cliente de desenvolvimento com GTNA + GTIACore e perfil GTIA. O perfil contém
seis programas terrestres e Radon de Marte. O lançador `tools/run_gtia_client.py` instala
as configurações e o depósito de Marte via KubeJS, com backup dos arquivos anteriores.
O GTIACore sozinho não instala esse perfil no GTNA.

## Petróleo no Overworld

1. Monte uma **Fluid Drilling Rig comum do GTCEu**, usando o preview/JEI da estrutura,
   em uma área cujo depósito indique **Oil**. A perfuradora MV basta. Dê energia,
   faça manutenção e deixe espaço no hatch de saída. Essa perfuradora faz a descoberta;
   a Electric Void Miner de minérios e a Void Fluid Drilling Rig não substituem essa etapa.
2. Espere um ciclo terminar e confirme petróleo no hatch. Tenha **Deposit Recorder**
   na mão e pelo menos **um Data Stick** no inventário. Clique com o botão direito
   no controller da perfuradora. Ela precisa pertencer a você/equipe. O Data Stick
   é consumido e você recebe **Deposit Data**, com origem Overworld e fluido Oil.
3. Monte a **Void Fluid Drilling Rig do GTNA** no Overworld, com energia EV,
   manutenção feita, barramento de entrada de itens, entrada de fluidos e saída de fluidos.
   Coloque o Deposit Data obtido no barramento e Drilling Fluid na entrada.
   Para esta prova, use **um paralelo**. Não precisa de upgrade remoto, filtro ou nitrogênio
   para Oil local. O cartão é reutilizado; cada ciclo consome 100 mB de Drilling Fluid
   e produz 2.000 mB de Oil em 20 segundos antes do overclock, com 1.920 EU/t.
4. Confirme que o cartão permanece e que o fluido sai. Retire o cartão: a próxima produção
   deve parar. Um cartão vazio retirado do criativo/JEI não é uma descoberta certificada.
   Devolva o cartão e confira a retomada. Saída cheia deve pausar a máquina.

Heavy Oil e Light Oil possuem cartões próprios: um cartão de Oil não desbloqueia todos
os tipos de petróleo. Se a perfuradora estiver extraindo outro fluido, procure o programa
desse fluido no JEI e use seus custos, em vez de esperar 2.000 mB de Oil.

## Radon de Marte na Terra

1. No JEI, abra **Void Fluid Drilling** (ou as receitas da Void Fluid Drilling Rig) e
   procure Radon. O programa mostra origem `ad_astra:mars` e produção remota com **T2**.
   Há um único programa; não existe uma segunda receita separada chamada “Radon na Terra”.
2. Em Marte, extraia Radon com a **Fluid Drilling Rig comum** e registre com Deposit Recorder
   + Data Stick. A entrada do JEI serve como referência: não substitui o certificado real.
3. Na Terra, use a **Void Fluid Drilling Rig** com energia **LuV**, o cartão certificado
   e **Remote Fluid Upgrade T2** no barramento. Forneça, por operação, **100 mB Drilling Fluid,
   100 mB Nitrogen e um Fluid Separation Filter**. Use uma entrada com dois tanques ou
   dois hatches para os dois fluidos.
4. A saída deve ser **50 mB de Radon** por ciclo (20 s antes do overclock). Retirar T2
   impede a produção na Terra. Em Marte, a origem permite produção local sem esse upgrade.

T2 possui receita de montagem que usa pesquisa de Marte; o teste criativo pode fornecer
os componentes, mas a descoberta continua sendo feita pela extração real.
