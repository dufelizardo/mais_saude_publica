# 0178 — Transporte Sanitário

## Status

Aceita. Desenho do domínio 16 e fatia **T1** implementada; T2 e T3 só com aprovação. O detalhe fica em
[`transporte/MODELO-TRANSPORTE.md`](../transporte/MODELO-TRANSPORTE.md).

## Contexto

- **A regulação de urgência registrava o recurso em texto** ("USA-01"), à espera do Transporte
  ([ADR-0176](./0176-regulacao-de-urgencia.md)).
- **O modelo operacional já tem a unidade móvel:** o perfil Atenção móvel e a base
  ([ADR-0166](./0166-modelo-operacional-ponto-operacional.md)).
- **As Emergências contam ambulâncias como número** (E6a, [ADR-0160](./0160-emergencias-evento-de-massa-e-estruturas-temporarias.md)) e
  registram veículo só como necessidade (E3, [ADR-0158](./0158-emergencias-coordenacao.md)).

## Decisão

1. **Fatias:**
   - **T1 (esta):** frota com a situação em histórico, plantão das viaturas com a tripulação mínima e a viatura empenhada pelo SAMU;
   - **T2:** transporte sanitário eletivo: a solicitação para consulta, exame, hemodiálise, alta e tratamento fora do domicílio (TFD), a agenda
     de viagens com os embarques e o registro de quilometragem;
   - **T3:** veículos nas Emergências (E4).
2. **Veículo** (`TB_VEICULO`):
   - placa no padrão antigo (ABC1234) ou Mercosul (ABC1D23), guardada sem hífen e em maiúsculas (400); placa e prefixo únicos (409);
   - tipo: USA, USB, motolância, aeromédico (do SAMU), ambulância de transporte, van sanitária, micro-ônibus, carro administrativo;
   - modelo, ano, lugares, maca, acesso para cadeira de rodas e quilometragem, que não volta (422);
   - **lotação:** a viatura do SAMU só fica em unidade com o perfil Atenção móvel (422); os outros tipos, em qualquer unidade.
3. **Situação com histórico** (`TB_SITUACAO_VEICULO`): disponível, em serviço, em manutenção ou inativo, com início e motivo.
   - À mão: manutenção, volta a disponível e inativação. "Em serviço" não se marca à mão (400).
   - Veículo em serviço não muda de situação (422); a mesma situação, 422.
4. **Plantão da viatura** (`TB_PLANTAO_VIATURA`, `TB_TRIPULANTE_PLANTAO`): veículo, início, fim (até 24 horas, 400) e a tripulação.
   - **Tripulação mínima** (Portarias 2.048/2002 e 1.010/2012), senão 422:
     - USB: condutor e técnico de enfermagem (ou enfermeiro);
     - USA: condutor, enfermeiro e médico; aeromédico: enfermeiro e médico;
     - motolância: técnico de enfermagem ou enfermeiro;
     - os demais: condutor.
   - O mesmo profissional duas vezes no plantão: 400. A viatura ou um tripulante em outro plantão no período: 409. Veículo inativo: 422.
   - Encerrar: o vigente termina agora; o futuro fica cancelado.
   - **Fica para depois:** ligar o plantão à escala geral ([ADR-0105](./0105-escalas-backend.md)).
5. **A viatura no SAMU:**
   - a regulação escolhe a viatura no envio de recurso ou na transferência (noutra decisão, 400). Ela precisa estar disponível, com plantão
     vigente e ser do tipo pedido (422); sem tipo pedido, o tipo da viatura vira o recurso, e o prefixo, a identificação;
   - viatura já empenhada noutra ocorrência aberta: 409;
   - o registro do envio põe a viatura em serviço; encerrar, receber no destino ou cancelar a devolve a disponível (se alguém a pôs em
     manutenção, fica como está);
   - o painel da urgência traz as viaturas disponíveis com plantão e sem ocorrência aberta;
   - o recurso em texto continua aceito, para a central que ainda não cadastrou a frota.
6. **Permissões e papéis:** `TRANSPORTE.CONSULTAR` e `TRANSPORTE.GERENCIAR` (operação); papel novo **Coordenador de frota**; o **Gestor**
   consulta e gerencia; o **atendente (TARM)** e o **médico regulador de urgência** consultam.

### API

- `GET, POST /api/v1/transporte/veiculo`, `GET, PUT /veiculo/{uuid}`, `POST /veiculo/{uuid}/situacao`;
- `GET, POST /api/v1/transporte/plantao` (`unidadeId`, `dias`: 7, até 14), `POST /plantao/{uuid}/encerramento`;
- `POST /api/v1/urgencia/ocorrencia/{uuid}/regulacao` aceita `veiculoId`; `GET /urgencia/painel` traz `viaturas`.

### Tela

- **Frota e transporte,** em Administrativo: o resumo (disponíveis, em serviço, em manutenção, com plantão agora), a aba Frota com a ficha
  do veículo (dados, tripulação do plantão, histórico de situações) e a aba Plantões. Gavetas de veículo, situação e plantão.
- **Regulação de urgência:** a viatura da frota na gaveta "Regular" e o quadro de viaturas disponíveis.

### Migração

`V63__transporte_frota_e_plantao.sql`, com a coluna `veiculo_id` na ocorrência de urgência.

## Consequências

- **Ficam fora:** combustível, custos, multas, GPS e rastreamento; o veículo como bem patrimonial (Patrimônio, #15).

## Testes

- **JUnit:**
  - `TransporteControllerTest`: placa e prefixo, lotação da viatura do SAMU, situação com histórico e quilometragem; plantão (duração,
    composição mínima, tripulante repetido, conflitos de viatura e de tripulante, encerramento); a viatura no SAMU (sem plantão, tipo
    diferente, orientação, empenho, conflito, em serviço no envio e livre no encerramento);
  - `TransporteAutorizacaoControllerTest`, com a segurança ligada: sem login, o coordenador de frota fora da unidade, o atendente que
    consulta e não cadastra, a recepção sem acesso.
- **Robot:** `Transporte.robot` (API, quatro casos) e `UI_frota.robot`; regressão de `Urgencia.robot` e `UI_urgencia.robot`.
