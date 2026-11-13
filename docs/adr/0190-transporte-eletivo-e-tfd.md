# 0190 — Transporte: transporte sanitário eletivo e TFD

## Status

Aceita e implementada. Fatia **T2** do domínio 16 ([ADR-0178](./0178-transporte-sanitario.md); detalhe em
[`transporte/MODELO-TRANSPORTE.md`](../transporte/MODELO-TRANSPORTE.md)). A T3 (veículos nas Emergências) segue só com aprovação, junto
da ES3 e antes da E4.

## Contexto

- **A T1 fez a frota e o SAMU:** os veículos com a situação em histórico, o plantão das viaturas e a viatura empenhada pela regulação de
  urgência. Faltava o transporte que não é urgência, que é a maior parte do uso da frota municipal.
- **O paciente precisa chegar à consulta, ao exame e às sessões** (hemodiálise três vezes por semana, quimioterapia, radioterapia,
  fisioterapia), e voltar para casa na alta.
- **O tratamento fora do domicílio (TFD)** leva o paciente a outro município quando a rede local não tem a oferta (Portaria SAS
  55/1999). O pedido traz o laudo médico e passa por uma comissão que autoriza ou nega.
- **O que já existe no sistema:**
  - o agendamento ([ADR-0042](./0042-agendamento-independente-do-atendimento.md));
  - a solicitação regulada com a executante e a data ([ADR-0089](./0089-regulacao-agendamento-desfecho-e-vinculo.md));
  - a internação com a previsão de alta ([ADR-0098](./0098-leitos-e-internacao-backend.md)).

## Decisão

1. **Solicitação de transporte** (`TB_SOLICITACAO_TRANSPORTE`, permissão nova `TRANSPORTE.SOLICITAR`):
   - **Quem pede:** a unidade de origem do paciente.
   - **Motivo:** consulta, exame, hemodiálise, terapia seriada, alta hospitalar, TFD ou outro.
   - **Vínculo opcional, um só (400):**
     - **Agendamento:** a data, a hora e a unidade de destino vêm dele. Precisa estar agendado ou confirmado (422).
     - **Solicitação regulada:** a executante e a data (do agendamento dela ou a prevista) vêm dela. Precisa estar autorizada ou agendada e
       ter executante e data (422).
     - **Internação:** só para a alta hospitalar (400). Precisa ter alta registrada ou previsão de alta (422). O embarque é o hospital e o
       destino, o endereço do paciente.
     - Vínculo de outro paciente: 422.
   - **Dados:**
     - o compromisso: no passado, 422; sem ele, 400;
     - o endereço de embarque: o do cadastro do paciente quando não se informa outro;
     - o destino: a unidade ou um texto, com o município;
     - o trecho: ida, volta ou ida e volta;
     - maca, cadeira de rodas e oxigênio;
     - o acompanhante: obrigatório para menor de 18 anos na data e para quem vai de maca (400). Ocupa um lugar.
   - **Duplicada:** o mesmo paciente com o mesmo horário, fora de cancelada e negada: 409.
   - **Situações:** solicitada (na fila), aguardando autorização (TFD), autorizada, programada, realizada, faltou, negada e cancelada.
   - **Cancelar:**
     - com motivo, pela unidade que pediu;
     - a que já está numa viagem sai da viagem antes (422).
2. **Recorrência** (hemodiálise e terapia seriada; outro motivo, 400):
   - informa os dias da semana e a data final, até 90 dias da primeira sessão (400);
   - gera uma solicitação por data, ligadas pela mesma recorrência;
   - **cancelar uma sessão:** pode levar junto as próximas que ainda não estão numa viagem.
3. **TFD** (Portaria SAS 55/1999):
   - **Laudo:** CID, justificativa e a matrícula de quem pede (400).
   - **Destino:** outro município, que é obrigatório (400). O mesmo município da unidade de origem dá 422; a comparação ignora maiúsculas
     e acentos.
   - **Comissão:** o pedido fica aguardando a decisão dela.
     - Permissão nova `TFD.AUTORIZAR`, para o papel novo **Comissão de TFD** e para o **Gestor**.
     - Autorizar põe o pedido na fila das viagens; negar pede motivo; fora de aguardando, 422.
   - **Quem vê o laudo:** só quem pede ou autoriza. A frota vê a logística.
4. **Viagem** (`TB_VIAGEM_TRANSPORTE` e `TB_EMBARQUE_VIAGEM`, `TRANSPORTE.GERENCIAR` na unidade do veículo):
   - **Composição:** veículo, condutor, saída e retorno previstos (até 72 horas, 400), destino (padrão: os destinos dos pacientes) e as
     solicitações.
   - **Veículo:** só o eletivo serve (ambulância de transporte, van, micro-ônibus, carro). Viatura do SAMU, inativo ou em manutenção: 422.
   - **Solicitações:**
     - na fila (o TFD só autorizado, 422); já noutra viagem: 409;
     - o compromisso cai entre a saída e o retorno (422).
   - **Capacidade:**
     - pacientes e acompanhantes até os lugares do veículo (422);
     - maca exige veículo com maca e cadeira de rodas, o acesso para cadeira (422).
   - **Conflito:** o veículo ou o condutor noutra viagem programada ou em andamento que cruza a janela: 409.
   - **Programada:** os embarques podem ser trocados; quem sai volta para a fila.
   - **Saída:** com a quilometragem do painel, que não volta (422). O veículo fica em serviço pelo mesmo mecanismo da T1.
   - **Embarque:** quem embarcou e quem faltou, com motivo; a falta fica na solicitação. Pode ser corrigido até a chegada.
   - **Chegada:**
     - pede todos os embarques registrados (422) e a quilometragem a partir da de saída (422);
     - atualiza o veículo, que volta a disponível, e as solicitações embarcadas ficam realizadas.
   - **Cancelar:** só antes da saída, com motivo; as solicitações voltam para a fila.
5. **Quilometragem:** por veículo, as viagens concluídas com a chegada no período (padrão: os últimos 30 dias; até um ano), com o número
   de viagens, de pacientes embarcados e os quilômetros.
6. **Telas:**
   - **Frota e transporte**, com abas novas:
     - **Solicitações:** a fila com filtros, a autorização do TFD e a gaveta de pedido, com a recorrência;
     - **Viagens:** a agenda de sete dias, com as gavetas de programar (a fila com os avisos de janela, maca e cadeira, e os lugares
       ocupados), embarques, saída, embarque e chegada;
     - **Quilometragem:** o relatório por veículo.

     A recepção e a comissão de TFD, que não gerenciam a frota, veem só a aba Solicitações.
   - **"Pedir transporte":** na marcação da Agenda e na internação do Leitos, abrindo a gaveta já ligada ao agendamento ou à internação.
   - **Prontuário:** os transportes do paciente.
7. **Papéis:**

   | Quem | O que recebe |
   |---|---|
   | Recepção, Coordenação de enfermagem e Assistente social (a alta e o TFD passam pelo serviço social) | `TRANSPORTE.SOLICITAR` |
   | Comissão de TFD (papel novo) | `TFD.AUTORIZAR`, `PACIENTE.CONSULTAR` e `TRANSPORTE.CONSULTAR` |
   | Gestor | `TFD.AUTORIZAR` |

### API

- `GET, POST /api/v1/transporte/solicitacao`, com os filtros `unidadeId`, `pacienteId`, `situacao`, `motivo`, `de` e `ate`;
  `GET /solicitacao/{uuid}`.
- `POST /solicitacao/recorrente`.
- `POST /solicitacao/{uuid}/cancelamento`, `/autorizacao` e `/negacao`.
- `GET, POST /api/v1/transporte/viagem`, com `data`, `dias` (até 14), `veiculoId` e `unidadeId`; `GET /viagem/{uuid}`.
- `PUT /viagem/{uuid}/embarques`.
- `POST /viagem/{uuid}/saida`, `/embarque`, `/chegada` e `/cancelamento`.
- `GET /api/v1/transporte/quilometragem`, com `de`, `ate` e `unidadeId`.

### Migração

`V75__transporte_eletivo_e_tfd.sql`.

## Alternativas descartadas

- **Solicitação dentro do agendamento:** a alta, o TFD e as sessões não nascem de um agendamento; o vínculo opcional cobre os que nascem.
- **Recorrência como uma regra única que gera as viagens:** a falta, o cancelamento e o embarque são por sessão; uma solicitação por data
  deixa isso simples e auditável.
- **Roteirização automática:** quem programa conhece as estradas e os horários; o sistema avisa da janela, dos lugares e dos recursos.

## Consequências

- **STATUS-DOS-DOMINIOS:** a T2 sai das pendências do domínio 16; segue a T3.
- **Implantação:** conceder `TRANSPORTE.SOLICITAR` às recepções e o papel **Comissão de TFD** quando a autorização for ligada; cadastrar os
  lugares e os recursos (maca, cadeira) dos veículos eletivos.
- **Fora do escopo:**
  - a ajuda de custo e as diárias do TFD, do paciente e do acompanhante (financeiro, domínio 17);
  - a passagem aérea ou rodoviária;
  - a roteirização, o GPS, o combustível e os custos.

## Testes

- **JUnit:**
  - `TransporteEletivoControllerTest`:
    - a solicitação: acompanhante do menor e da maca, destino, data no passado, dois vínculos, duplicada, endereço do cadastro, o agendamento
      de outro paciente e o que preenche a data e o destino;
    - o TFD: sem laudo, mesmo município, aguardando, autorizar duas vezes, negar com motivo;
    - a hemodiálise recorrente: motivo, limite de 90 dias, as sessões e o cancelamento das próximas;
    - a viagem do programar à chegada: viatura do SAMU, TFD sem autorização, maca, lugares, janela, conflitos de veículo, de condutor e da
      solicitação, troca de embarques, saída, falta, chegada com a quilometragem, cancelamento e a quilometragem por veículo.
  - `TransporteEletivoAutorizacaoControllerTest`, com a segurança ligada:
    - a recepção pede na sua unidade e não noutra, e não autoriza;
    - a comissão de TFD autoriza e lê o laudo;
    - o coordenador de frota não pede, vê a fila sem o laudo e programa a viagem;
    - a recepção não vê a agenda de viagens.
- **Robot:**
  - `TransporteEletivo.robot` (API, CT-001 a CT-003), com a regressão de `Transporte.robot`;
  - os casos CT-002 e CT-003 de `UI_frota.robot`.
