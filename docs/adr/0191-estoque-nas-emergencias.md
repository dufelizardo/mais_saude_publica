# 0191 — Estoque: o estoque nas emergências (reserva, requisição urgente e consumo extraordinário)

## Status

Aceita e implementada. Fatia **ES3** do domínio 13 ([ADR-0177](./0177-estoque-e-almoxarifado.md); detalhe em
[`estoque/MODELO-ESTOQUE.md`](../estoque/MODELO-ESTOQUE.md)), feita junto com a T3 ([ADR-0192](./0192-transporte-nas-emergencias.md)) e
antes da E4 das Emergências ([ADR-0113](./0113-emergencias-e-desastres.md)). Com ela, o domínio 13 fica completo.

## Contexto

- **A E3 das Emergências registra o insumo como necessidade:** a mobilização de insumo tinha só um texto e uma quantidade, à espera do
  Estoque ([ADR-0158](./0158-emergencias-coordenacao.md)). O medicamento já seguia uma transferência da Farmácia.
- **A ES2 deu ao estoque a central, a requisição e a transferência em duas etapas** ([ADR-0189](./0189-estoque-central-requisicao-inventario.md)).
- **O princípio da ADR-0113:** Emergências registra; o dono executa. A coordenação não mexe no saldo de ninguém.
- **Na resposta, o material separado para a emergência precisa ficar separado.** Sem reserva, o consumo do dia a dia leva os kits
  antes que cheguem aos abrigos.

## Decisão

1. **Mobilização de insumo pelo estoque.** A mobilização de insumo ganha o material do catálogo (opcional; só para o insumo, 422 nos
   demais), com a quantidade obrigatória (400). Há dois caminhos:
   - **Pela central:**
     - quando a origem tem a capacidade Central de abastecimento, a coordenação gera a **requisição urgente** da unidade de destino à
       central, já enviada e ligada à emergência (`tb_requisicao_estoque.emergencia_id`);
     - a central a vê no alto da fila, marcada "Emergência", e atende pelo fluxo da ES2;
     - o solicitante pode ficar vazio: quem pede é a coordenação ("Coordenação da emergência").
   - **Pela transferência avulsa:** a mobilização se liga a uma transferência do estoque para a mesma unidade de destino (outra unidade,
     422). Pelo mesmo endpoint que liga o medicamento à Farmácia.
   - **A situação acompanha o estoque** e não muda à mão (422):

     | No estoque | A mobilização |
     |---|---|
     | Requisição enviada | Solicitada |
     | Atendida (em trânsito) ou transferência em trânsito | Em andamento |
     | Recebida | Concluída |
     | Negada ou cancelada | Cancelada, com o motivo do estoque |

   - **Cancelar:** a coordenação cancela a mobilização ainda não atendida e a requisição vai junto. Depois de atendida, 422.
   - **Sem material** (fora do catálogo), segue como antes: a necessidade em texto.
2. **Reserva para a emergência** (`TB_RESERVA_ESTOQUE`):
   - **Quem reserva:** o estoque da unidade, com `ESTOQUE.MOVIMENTAR`. A coordenação vê, mas não reserva.
   - **Quando:** com a emergência ativa (422).
   - **Quanto:** até o livre, que é o saldo dentro da validade menos as reservas ativas (422). Material inativo, 422.
   - **Situações:** ativa, liberada (à mão, com motivo, ou no fim da emergência) e consumida (usada inteira).
   - **O saldo** passa a mostrar o reservado e o livre de cada material.
3. **O reservado fica de fora das saídas comuns** (422):
   - o consumo comum;
   - a perda de lote válido (a de lote vencido continua livre: a reserva só conta o que está na validade);
   - a transferência avulsa;
   - o atendimento de requisição que não é da emergência.

   **Ficam livres:** o ajuste e o inventário, que corrigem a contagem, e o consumo lançado pelo sistema (insumos do laboratório,
   [ADR-0183](./0183-laudo-pdf-assinado-e-insumos.md)), porque já aconteceu.
4. **O que usa a reserva:**
   - o atendimento da requisição urgente da emergência na central;
   - o consumo extraordinário: a saída por consumo pode indicar a emergência ativa ou em desmobilização (encerrada, 422). Fica marcada
     (`tb_movimentacao_estoque.emergencia_id`).

   O que sai abate das reservas daquela emergência, as mais antigas primeiro.
5. **Fim da emergência:** iniciar a desmobilização ou encerrar libera o que resta das reservas. As mobilizações ligadas ao estoque seguem o
   estoque, como as ligadas à Farmácia.
6. **O que a emergência mostra:** a seção **Estoque**, com as reservas e o consumo por unidade e material, em quantidade e em valor (pelo
   custo do lote). O **relatório pós-evento** ganha os insumos consumidos e o valor deles.
7. **Telas:**
   - **Emergência:** na gaveta de mobilizar, o material do catálogo para o insumo; na linha, a requisição ou a transferência e a
     situação; "Ligar transferência" também para o insumo.
   - **Estoque:**
     - o saldo com o reservado e o livre;
     - "Reservar para emergência" e a lista das reservas da unidade, com "Liberar";
     - na saída, o "Consumo da emergência";
     - nas requisições, a marca "Emergência".

### API

- `GET /api/v1/estoque/reserva`, com `unidadeId`, `emergenciaId` e `situacao`. Para quem tem `ESTOQUE.CONSULTAR`, `ESTOQUE.MOVIMENTAR`,
  `ESTOQUE.GERENCIAR` ou `EMERGENCIA.COORDENAR`.
- `POST /api/v1/estoque/reserva` e `POST /reserva/{uuid}/liberacao`, com `ESTOQUE.MOVIMENTAR` na unidade.
- `GET /api/v1/estoque/emergencias`: as ativas e em desmobilização, para o estoque, que não lê as emergências.
- `POST /api/v1/estoque/saida` aceita `emergenciaId`.
- `POST /api/v1/emergencia/{id}/mobilizacao` aceita `materialId`; `PUT /mobilizacao/{mob}/transferencia` liga também o insumo.
- A emergência ganha `estoque`; a mobilização, o material, a requisição e a transferência do estoque.

### Migração

`V76__estoque_e_transporte_nas_emergencias.sql`, junto com a T3: `tb_reserva_estoque`; `material_id`, `requisicao_estoque_id` e
`transferencia_estoque_id` em `tb_mobilizacao`; `emergencia_id` em `tb_requisicao_estoque` (e o solicitante sem `NOT NULL`) e em
`tb_movimentacao_estoque`.

## Alternativas descartadas

- **A coordenação reserva o estoque:** quem responde pelo saldo é o almoxarifado; a coordenação pede e vê.
- **Reserva por lote:** o consumo sai pelos que vencem primeiro; reservar por material e unidade não prende um lote que vence antes.
- **Um tipo novo de saída "emergência":** a saída continua sendo consumo; a marca da emergência basta para somar e não muda os relatórios
  que já existem.

## Consequências

- **STATUS-DOS-DOMINIOS:** o domínio 13 fica completo; o compras e o fornecedor seguem no domínio 14.
- **Implantação:** sem permissão nova. O Almoxarife reserva; a coordenação (Gestor, Coordenador de enfermagem) pede e vê.
- **Fora do escopo:** a entrega com veículo (rota do almoxarifado); a doação recebida na emergência entra como entrada de doação, como
  hoje.

## Testes

- **JUnit `EstoqueEmergenciaControllerTest`:**
  - a reserva acima do livre (422);
  - a mobilização pela central vira a requisição urgente, no alto da fila e sem mudança à mão (422);
  - a requisição comum barrada pelo reservado (422) e a da emergência que usa a reserva, com a mobilização andando e concluindo;
  - o cancelamento que leva a requisição;
  - a saída comum e a perda de lote válido barradas (422) e o consumo da emergência que abate a reserva;
  - a transferência ligada (outro destino, 422);
  - a desmobilização que libera, o relatório com o consumo, o consumo em desmobilização e a reserva fora da ativa (422).
- **JUnit `EstoqueFrotaEmergenciaAutorizacaoControllerTest`,** com a segurança ligada: a coordenação não reserva e vê as reservas; o
  almoxarife reserva na sua unidade e não noutra.
- **Robot:**
  - `EstoqueEmergencia.robot` (API, CT-001 e CT-002);
  - CT-004 de `UI_estoque.robot` e CT-013 de `UI_emergencias.robot`.
