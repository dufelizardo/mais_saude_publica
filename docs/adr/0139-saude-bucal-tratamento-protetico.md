# 0139 — Saúde Bucal: tratamento protético (SB4b)

## Status

Aceita e implementada (segunda parte da fatia SB4 da [ADR-0111](./0111-saude-bucal.md); usa o odontograma da
[ADR-0135](./0135-saude-bucal-odontograma.md), o plano da [ADR-0137](./0137-saude-bucal-plano-de-tratamento.md) e o
Modelo Operacional da [ADR-0115](./0115-modelo-operacional.md)). Fecha a SB4.

## Contexto

- **A prótese não é um procedimento só:** tem um ciclo de avaliação, indicação, planejamento, moldagem, produção no
  laboratório, prova, entrega e acompanhamento (MODELO-SAUDE-BUCAL, seção 10).
- **O laboratório regional de prótese dentária (LRPD)** pode ser da rede ou contratado.
- **A SB4a deixou o CEO como unidade com capacidades,** e a prótese dentária no catálogo da Regulação. Falta registrar
  o tratamento em si.

## Decisão

1. **Tratamento protético** (`TratamentoProtetico`, `tb_tratamento_protetico`):
   - **onde e com quem:**
     - abre num atendimento de especialidade odontológica (422 fora dele): na UBS ou no CEO, depois da
       contrarreferência;
     - o dentista responsável;
     - fica ligado ao plano de tratamento aberto do paciente, se houver;
   - **tipo:**
     - a **total** e a **parcial removível** pedem a arcada (400) e não levam dente (422);
     - a **unitária** (coroa) pede o dente FDI, com as regras da SB2 (400), e a arcada vem dele;
   - **laboratório:**
     - uma unidade com a **capacidade nova "Laboratório regional de prótese dentária (LRPD)"** do Modelo Operacional
       (422 sem ela), ou o **laboratório contratado**, pelo nome;
     - não os dois (400);
     - pode ser informado na abertura ou no envio ao laboratório;
   - **situação:** em andamento, concluído ou cancelado. O cancelamento pede motivo (400).
2. **Etapas** (`EtapaTratamentoProtetico`, `tb_etapa_tratamento_protetico`): cada uma com data (não no futuro, 400),
   profissional e observação. O histórico responde quando foi moldado e quando voltou do laboratório.
   - **A ordem do ciclo** (422 fora dela):
     - o planejamento é registrado na abertura;
     - a moldagem pode se repetir;
     - a prova e o laboratório vêm depois da moldagem e podem se alternar e se repetir: é comum voltar ao
       laboratório depois da prova;
     - o envio ao laboratório pede o laboratório definido;
     - a entrega pede ter passado pelo laboratório;
     - depois da entrega, só o acompanhamento.
   - **O tratamento diz as próximas etapas permitidas,** e a tela só mostra essas.
   - **A entrega grava a data de entrega.** Na unitária, grava também a condição "Prótese" no dente, no odontograma,
     com o tratamento como origem (`tb_registro_odontograma.tratamento_protetico_id`).
   - **Concluir** só depois da entrega (422).
3. **O plano de tratamento não espera a prótese:** como o item encaminhado, a prótese em andamento não impede concluir
   o tratamento da APS. Ela é um ciclo à parte, muitas vezes no CEO, e o tratamento protético guarda a ligação com o
   plano.
4. **Acesso:** `ODONTOLOGIA.PLANEJAR` (o dentista) para abrir, registrar etapa, concluir e cancelar; o técnico em saúde
   bucal não (403). Ler segue o prontuário: vínculo e leitura auditada.
5. **API:**
   - `POST /tratamento-protetico` (201) e `POST /tratamento-protetico/{id}/etapa` (201);
   - `POST /tratamento-protetico/{id}/conclusao` e `POST /tratamento-protetico/{id}/cancelamento`;
   - `GET /paciente/{id}/tratamentos-proteticos`.
6. **Tela:** no atendimento de odontologia, a seção **"Prótese"**, entre o plano e o odontograma:
   - **abrir:** a prótese (tipo), a arcada ou o dente e a matrícula;
   - **cada prótese mostra:**
     - a situação, a etapa atual, o laboratório e a data de entrega;
     - a linha do tempo das etapas;
     - os botões das etapas permitidas, com o laboratório (da rede ou contratado) e a observação;
     - "Concluir prótese" depois da entrega e o cancelamento com motivo;
   - **no histórico do dente,** o registro aparece como "Prótese entregue".
7. **Migração V32:** a capacidade LRPD, as duas tabelas e a origem prótese no odontograma.

## Consequências

- **O ciclo da prótese tem registro,** com datas, laboratório e a entrega no odontograma.
- **A SB4 fecha:** encaminhamento pelo CEO (SB4a) e prótese (SB4b). Da Saúde Bucal, falta a SB5 (coletiva).
- **Fora do escopo:**
  - o detalhe do laboratório: etapas de produção, custos e o contrato;
  - a contagem de próteses entregues para o indicador (P6 e BI);
  - a prótese total e a parcial no odontograma, que continuam marcadas na avaliação, dente a dente.

## Testes

- **JUnit:**
  - **`TratamentoProteticoControllerTest`:**
    - **abrir:**
      - recusas: total sem arcada (400), total com dente (422), unitária sem dente (400) ou fora da FDI (400),
        laboratório sem a capacidade LRPD (422), os dois laboratórios (400), fora da odontologia (422);
      - a total com o LRPD, só com a moldagem como próxima;
      - a unitária no 46 na arcada inferior, com o contratado;
      - a lista do paciente;
    - **etapas e conclusão:**
      - recusas: prova, entrega e planejamento fora da ordem (422); data no futuro (400); laboratório sem
        laboratório (422) ou sem a capacidade (422);
      - laboratório com o LRPD; prova e volta ao laboratório;
      - concluir antes da entrega (422);
      - a entrega com a data, e o acompanhamento como única próxima; prova depois da entrega (422);
      - a unitária como "Prótese" no 46, com a origem;
      - a conclusão;
    - **cancelamento:**
      - a parcial ligada ao plano aberto;
      - o cancelamento sem motivo (400) e com ele;
      - etapa no cancelado (422).
  - **`SaudeBucalAutorizacaoControllerTest`:** o técnico não abre a prótese (403); o dentista abre.
  - **`MigracoesDoEsquemaTest`:** os CHECKs das enumerações novas batem com o Java.
- **Robot:**
  - **de API:** `TratamentoProtetico.robot` (CT-001 a CT-006): abrir; sem arcada; fora da ordem; a entrega da
    unitária no odontograma; concluir antes da entrega; cancelar sem motivo.
  - **de interface:** `UI_saude_bucal.robot` CT-008: a coroa do 46, da abertura à conclusão, com o laboratório
    contratado.
