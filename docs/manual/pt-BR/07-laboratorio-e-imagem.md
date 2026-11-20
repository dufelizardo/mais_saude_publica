# Laboratório e imagem

> **Versão:** 1.15.0 · [English](../en/07-laboratorio-e-imagem.md)

Menu: **Assistência › Laboratório e imagem**. Cobre os dois fluxos de exame:

- **Laboratório:** pedido, coleta, análise, liberação e ciência de quem pediu.
- **Imagem:** pedido, realização, laudo e ciência de quem pediu.

Os dois terminam no laudo em PDF com código de verificação.

**Quem usa e o que cada um vê:** as abas aparecem conforme o perfil.

| Aba | Quem usa |
|-----|----------|
| **Para coletar** | enfermagem, técnico de enfermagem, técnico de laboratório e responsável técnico |
| **Em análise** | técnico de laboratório e responsável técnico |
| **Para liberar** | responsável técnico do laboratório |
| **Para realizar** | técnico em radiologia |
| **Para laudar** | médico radiologista |
| **Críticos a comunicar** | responsável técnico e médico radiologista |
| **Para ciência** | quem pede exame: médico e enfermeiro |
| **Pedidos** e **Catálogo de exames** | todos que abrem a tela; o catálogo é mantido pelo responsável técnico |

**Os cartões do alto:** **Para coletar**, **Em análise**, **Para liberar** e **Urgentes** (em qualquer etapa).

## 1. Pedir exames

**Quem usa:** médico e enfermeiro. O botão **Pedir exames** de dentro do atendimento (módulo 03) abre o pedido ligado ao
atendimento de onde veio.

1. Clique em **Pedido de exames**.
2. Em **Paciente**, digite parte do nome ou do CPF e escolha a pessoa.
3. Escolha a **Unidade que pede** e informe a **Matrícula de quem pede**.
4. Em **Exames**, marque os exames. Use **Filtrar exames…** para achar no catálogo.
5. Se houver exame de imagem, escolha o **Serviço de imagem**: a unidade que realiza e lauda, que precisa ter diagnóstico por
   imagem. O exame regulado vai para a unidade que a Regulação autorizou.
6. Escreva a **Indicação clínica** (por que os exames são pedidos) e, se quiser, o **CID-10**.
7. Marque **Urgente** quando for o caso e salve.

## 2. Coletar (laboratório)

A aba **Para coletar** mostra um pedido por linha, urgente primeiro, com o **Preparo** de cada exame.

1. Clique em **Coletar**. Se houver **Recoleta**, a amostra anterior foi rejeitada: confira o motivo antes de coletar de novo.
2. Em **O que foi coletado**, marque os exames. O que ficar desmarcado continua aguardando coleta.
3. Informe **Onde foi coletado** e o **Laboratório que analisa** (ou **A própria unidade da coleta**). Esse laboratório é o que vai
   ver a amostra na lista de análise.
4. Informe a **Matrícula de quem coletou** e salve.

**A coleta gera uma amostra por material**, com o código da etiqueta.

## 3. Analisar e liberar (laboratório)

**Em análise** (técnico de laboratório): a lista é por exame, urgente primeiro.

1. Clique em **Resultado**.
2. Informe o **Resultado** (número ou texto, conforme o exame) e, se quiser, uma **Observação**.
3. Informe a **Matrícula de quem analisou** e salve. O resultado vai para **Para liberar**.

**Rejeitar amostra:** amostra com problema é rejeitada, e os exames dela ainda não liberados voltam para a coleta.

1. Escolha o **Motivo**: hemolisada, volume insuficiente, coagulada, identificação incorreta ou outro.
2. Informe a **Matrícula de quem rejeita**.

**Para liberar** (responsável técnico): os resultados registrados aguardando revisão.

1. Clique em **Revisar** e confira o resultado e quem analisou.
2. Informe a **Matrícula de quem libera** e libere (*Resultado liberado*). Se algo estiver errado antes da liberação, use
   **Registrar de novo**.

> **Depois de liberado, o resultado vai para o pedido e só muda por retificação.**

## 4. Realizar e laudar (imagem)

**Para realizar** (técnico em radiologia): os exames pedidos ao seu serviço, urgente primeiro. Confira o preparo antes de chamar o
paciente.

1. Clique em **Realizar**.
2. Informe o **Equipamento** (por exemplo, *Aparelho da sala 1*).
3. Se usou contraste, marque **Com contraste** e informe o **Contraste usado**.
4. Informe a **Matrícula de quem realizou** e salve.

**Para laudar** (médico radiologista): os exames realizados aguardando o laudo, urgente primeiro.

1. Clique em **Laudar**.
2. Preencha a **Técnica**, os **Achados** e a **Conclusão**.
3. Escolha a **Classificação**: sem alterações, com alterações ou achado crítico.
4. Se houver, cole o **Link das imagens**: o endereço do estudo no visualizador de imagens (PACS).
5. Informe a **Matrícula do radiologista** e salve.

> **O laudo assinado sai liberado.** A correção é por **adendo**, que grava um laudo novo; o anterior fica guardado e ligado a ele.
>
> **Achado crítico:** quem pediu recebe o aviso, e o exame entra em **Críticos a comunicar** até você registrar com quem falou.

## 5. Valor crítico: comunicar

A aba **Críticos a comunicar** traz os resultados liberados com valor crítico (o valor de pânico) que o médico ainda não viu.

1. Fale com quem responde pelo paciente: o solicitante, o plantonista ou a enfermagem do setor.
2. Clique em **Comunicar** e registre:
   - **Quem recebeu a informação:** nome e função, por exemplo *Dra. Ana, plantonista*;
   - o **Meio:** telefone, presencial ou mensagem no sistema;
   - **Quando** (vazio é agora);
   - a **Matrícula de quem comunicou**.

## 6. Ciência de quem pediu

A aba **Para ciência** mostra os resultados liberados dos exames que **você** pediu e ainda não viu, críticos primeiro.

1. Informe a **Sua matrícula** e clique em **Ver resultados**.
2. Abra o pedido e veja o valor. **Retificado** avisa que o valor foi corrigido.
3. Clique em **Registrar ciência** (*Ciente*).

## 7. Acompanhar o pedido, retificar e entregar o resultado

A aba **Pedidos** mostra o andamento de cada pedido, **sem os valores**: aguardando coleta, em andamento, concluídos ou
cancelados. Busque por paciente, exame ou unidade e clique em **Ver**. O detalhe registra a leitura dos resultados e mostra:

- as **Amostras** (inclusive as rejeitadas);
- os **Laudos de imagem** (técnica, achados, conclusão e **Ver as imagens**);
- a **Realização**, a **Comunicação de críticos** e as **Entregas ao paciente**;
- os **Laudos emitidos** e o **Histórico**.

**Retificar um resultado liberado** (responsável técnico):

1. No detalhe, clique em **Retificar**.
2. Informe o **Valor correto** (ou o **Resultado correto**), o **Motivo da retificação** e a **Matrícula de quem retifica**.
3. Salve. A retificação grava um resultado novo, já liberado por você. O anterior fica guardado e ligado a ele.

**Cancelar um exame do pedido:** ele sai das listas de trabalho, e os outros exames do pedido continuam.

**Laudo em PDF:**

- **Baixar laudo (PDF)** emite o laudo assinado, com um código de verificação e um QR code no rodapé.
- Baixar de novo, sem resultado novo, devolve o mesmo arquivo.
- Com resultado novo, sai uma nova versão, e a anterior fica como **Substituída**.
- A **Prévia para imprimir** abre uma página simples para imprimir.

**Registrar entrega** (quem coleta ou pede):

1. Em **Quem retirou**, escolha **O próprio paciente** ou **Outra pessoa**, com o nome, o documento e o parentesco.
2. Escolha a **Forma**: impresso e retirado na unidade, ou entregue na consulta.
3. Informe a **Unidade da entrega** e a **Matrícula de quem entrega**.

## 8. Conferir se um laudo é autêntico

Qualquer pessoa, **sem login**, confere o laudo na página **Verificar laudo**, pelo QR code ou pelo endereço do sistema.

1. Digite o **Código de verificação** do rodapé do laudo.
2. A página responde **Laudo autêntico e vigente** ou **Laudo autêntico, mas substituído**, com a data, a unidade, o paciente, os
   exames e o **Hash do PDF (SHA-256)**.
3. Para conferir o arquivo, calcule o SHA-256 do PDF recebido: ele precisa ser igual ao hash mostrado.

## 9. Catálogo de exames (responsável técnico)

1. Clique em **+ Exame**.
2. Informe o **Nome** (por exemplo, *Glicemia de jejum*) e a **Categoria**:
   - **Laboratorial:** coleta e liberação;
   - **Imagem:** realização e laudo. A **Modalidade** pode ser raio-X, ultrassonografia, tomografia, ressonância magnética,
     mamografia, densitometria óssea ou outra.

   A categoria decide o fluxo e **não muda depois de criado**.
3. Informe o **Prazo em dias** e, no laboratorial:
   - o **Material** e o **Tipo de resultado** (numérico ou texto);
   - no numérico, a unidade (por exemplo, *mg/dL*), a **Referência mínima** e **máxima** e o crítico abaixo de e acima de;
   - no texto, a referência (por exemplo, *Não reagente*).
4. Escreva o **Preparo do paciente** (por exemplo, *Jejum de 8 horas*).
5. **Faixas por sexo e idade** (opcional):
   - em **+ Faixa**, informe o sexo, a descrição (por exemplo, *Recém-nascido*), a idade a partir de e até antes de, e os
     valores;
   - vale a faixa do sexo do paciente que cobre a idade na data da coleta; senão, a sem sexo; senão, a faixa geral.
6. **Insumos** (opcional): os materiais do estoque e a quantidade que cada exame consome. Marque **Só com contraste** quando o
   insumo baixa só se a realização usar contraste.
7. Deixe **Em uso** marcado. Fora de uso, o exame não aparece no pedido, e os pedidos antigos continuam.

> A faixa de referência é copiada para cada resultado: **mudar o catálogo não muda resultado já dado.**

## 10. Mensagens comuns

| Mensagem | O que quer dizer | O que fazer |
|----------|------------------|-------------|
| *Marque pelo menos um exame.* / *Informe a indicação clínica.* | o pedido está incompleto | marque os exames e escreva a indicação |
| *Selecione o serviço de imagem.* | há exame de imagem sem o serviço que realiza | escolha a unidade de imagem |
| *Marque o que foi coletado.* | a coleta está sem exame marcado | marque o que foi coletado |
| *Informe o valor numérico.* / *Informe o resultado.* | o resultado está vazio | registre o valor |
| *Informe qual contraste foi usado.* | marcou contraste sem dizer qual | informe o contraste |
| *Descreva os achados.* / *Informe a conclusão.* | o laudo de imagem está incompleto | preencha os achados e a conclusão |
| *Informe quem recebeu a informação.* | a comunicação do crítico está sem o receptor | escreva o nome e a função |
| *Informe a sua matrícula para ver os resultados dos seus pedidos.* | a aba Para ciência precisa saber quem você é | informe a sua matrícula |
| *Informe o nome de quem retirou.* / *Informe o documento de quem retirou.* | a entrega a outra pessoa está incompleta | preencha o nome e o documento |
| *Não foi possível emitir o laudo em PDF.* | o laudo não foi emitido | tente de novo; o pedido precisa ter resultado liberado |
| *Informe ao menos um limite de referência.* | o exame numérico está sem referência | informe o mínimo, o máximo ou os dois |
| **Recoleta** no pedido | a amostra anterior foi rejeitada | confira o motivo e colete de novo |
