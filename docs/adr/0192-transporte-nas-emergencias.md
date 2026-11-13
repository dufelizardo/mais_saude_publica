# 0192 — Transporte: os veículos nas emergências (pedido, cessão e devolução)

## Status

Aceita e implementada. Fatia **T3** do domínio 16 ([ADR-0178](./0178-transporte-sanitario.md); detalhe em
[`transporte/MODELO-TRANSPORTE.md`](../transporte/MODELO-TRANSPORTE.md)), feita junto com a ES3
([ADR-0191](./0191-estoque-nas-emergencias.md)) e antes da E4 das Emergências. Com ela, o domínio 16 fica completo.

## Contexto

- **A E3 registra o veículo como necessidade** ([ADR-0158](./0158-emergencias-coordenacao.md)), e a E6a conta as ambulâncias das
  estruturas temporárias como número ([ADR-0160](./0160-emergencias-evento-de-massa-e-estruturas-temporarias.md)).
- **A T1 tem a frota com a situação em histórico** e o "em serviço" que o SAMU e a viagem eletiva usam
  ([ADR-0190](./0190-transporte-eletivo-e-tfd.md)).
- **O princípio da ADR-0113:** a coordenação pede, a frota executa.

## Decisão

1. **O pedido:** a coordenação continua pedindo pela mobilização de veículo (o recurso em texto, como "duas ambulâncias de transporte",
   e a quantidade opcional).
2. **A cessão** (`TB_CESSAO_VEICULO`, com `TRANSPORTE.GERENCIAR` na unidade do veículo):
   - **O pedido:**
     - só a mobilização de veículo (422);
     - aberta (422), da emergência ativa (422);
     - até a quantidade pedida, quando há uma (422).
   - **O veículo:**
     - já cedido a uma emergência: 409;
     - inativo, em manutenção ou em serviço (numa ocorrência do SAMU ou numa viagem): 422;
     - a viatura do SAMU pode ser cedida, por exemplo num acidente com muitas vítimas.
   - **Ao ceder:**
     - o veículo fica **em serviço** pelo mesmo mecanismo da T1, com o motivo "Cedido à emergência X";
     - a lotação não muda;
     - o pedido passa a em andamento;
     - guarda a quilometragem do momento.
   - **Avisos:** as viagens eletivas programadas do veículo nos próximos 30 dias, ou até o fim do pedido, voltam para a frota
     reprogramar.
3. **Enquanto cedido:**
   - programar viagem eletiva com o veículo dá 409;
   - a saída da viagem e o empenho pelo SAMU já pedem o veículo disponível;
   - a situação não se muda à mão.
4. **A devolução:**
   - pela frota, com a quilometragem do painel (opcional);
   - a quilometragem não volta, nem abaixo da cessão nem abaixo da atual do veículo (422);
   - o veículo volta a **disponível**;
   - já devolvido, 422.

   **Devolvem também:** concluir ou cancelar o pedido pela coordenação, iniciar a desmobilização e encerrar a emergência (sem a
   quilometragem).
5. **O que a emergência mostra:** na mobilização, os veículos cedidos com as datas e os quilômetros. Na seção **Frota**:
   - os disponíveis da rede por tipo;
   - os veículos das unidades afetadas, com a situação e a emergência a que estão cedidos.
6. **O relatório pós-evento** ganha os veículos cedidos, os dias de uso e os quilômetros.
7. **Telas:**
   - **Frota e transporte**, aba Frota, "Pedidos das emergências":
     - cada pedido com o destino, a quantidade e o que falta;
     - "Ceder veículo", com os disponíveis;
     - os cedidos com "Devolver".
   - **Emergência:** os veículos cedidos na linha da mobilização e a seção Frota.

### API

- `GET /api/v1/transporte/emergencia/pedidos`, com `TRANSPORTE.CONSULTAR` ou `TRANSPORTE.GERENCIAR`: os pedidos das emergências ativas e
  em desmobilização que ainda estão abertos ou têm veículo cedido.
- `POST /api/v1/transporte/emergencia/mobilizacao/{mobilizacaoId}/cessao`, com `veiculoId` e `observacao`; devolve a cessão e os avisos.
- `POST /api/v1/transporte/emergencia/cessao/{uuid}/devolucao`, com `quilometragem` e `observacao`.
- A emergência ganha `frota`; a mobilização de veículo, as `cessoes`.

### Migração

`V76__estoque_e_transporte_nas_emergencias.sql`, junto com a ES3: `tb_cessao_veiculo`.

## Alternativas descartadas

- **A coordenação escolhe o veículo:** quem sabe o que pode sair é a frota.
- **Mudar a lotação do veículo para a unidade de destino:** seria lotação falsa; a cessão é temporária e fica no histórico.
- **Um veículo por mobilização:** o pedido "três ambulâncias" é um só; a cessão é que é por veículo.
- **Uma situação nova "cedido":** o "em serviço" da T1 já bloqueia o SAMU, a viagem e a mudança à mão; a cessão diz a qual emergência.

## Consequências

- **STATUS-DOS-DOMINIOS:** o domínio 16 fica completo.
- **Implantação:** sem permissão nova. O Coordenador de frota cede e devolve; a coordenação da emergência pede.
- **Fora do escopo:**
  - o transporte dos pacientes deslocados, que usa a solicitação da T2 e será ligado na E4;
  - a escala do condutor na emergência, que vem pela mobilização de profissional (E3);
  - o combustível e os custos.

## Testes

- **JUnit `FrotaEmergenciaControllerTest`:**
  - o pedido na lista da frota;
  - ceder para mobilização de insumo e veículo em manutenção (422);
  - a cessão com o aviso da viagem programada, em serviço e o pedido em andamento;
  - acima da quantidade (422), já cedido (409) e a viagem nova (409);
  - a devolução com a quilometragem que não volta (422) e de novo (422);
  - concluir o pedido e a desmobilização que devolvem;
  - fora da emergência ativa (422);
  - o relatório com os veículos e os quilômetros.
- **JUnit `EstoqueFrotaEmergenciaAutorizacaoControllerTest`,** com a segurança ligada: a coordenação pede e não cede; o almoxarife não
  vê os pedidos; o coordenador de frota cede o veículo da garagem dele.
- **Robot:** `FrotaEmergencia.robot` (API, CT-001 e CT-002) e CT-004 de `UI_frota.robot`.
