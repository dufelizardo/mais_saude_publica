# 0177 — Estoque e Almoxarifado

## Status

Aceita. Desenho do domínio 13 e fatia **ES1** implementada; ES2 e ES3 só com aprovação. O detalhe fica em
[`estoque/MODELO-ESTOQUE.md`](../estoque/MODELO-ESTOQUE.md).

## Contexto

- **Medicamento e vacina já têm estoque:** a Farmácia, com lote, livro de movimentação, perda, ajuste e transferência em duas etapas
  ([ADR-0050](./0050-lote-segunda-entidade-da-farmacia.md), [ADR-0057](./0057-livro-de-movimentacao-do-estoque-da-farmacia.md) a
  [ADR-0061](./0061-transferencia-em-duas-etapas-envio-e-recebimento.md)), e a Sala de Vacina
  ([ADR-0148](./0148-imunizacao-sala-de-vacina.md)). Os dois têm regra própria (dispensação, prescrição, cadeia de frio) e ficam onde estão.
- **Faltava o material geral:** luva, seringa, gaze, EPI, limpeza, expediente, odontológico, laboratório, rouparia. Só existia o rótulo
  `ESTOQUE` no catálogo de capacidades administrativas ([ADR-0032](./0032-catalogo-de-capacidades-administrativas.md)).
- **As Emergências esperam o Estoque:** a E3 registra insumo como necessidade, sem execução
  ([ADR-0158](./0158-emergencias-coordenacao.md)).

## Decisão

1. **Domínio separado, com o padrão da Farmácia:** lote, livro imutável, saldo que nunca fica negativo. Quem conhece a Farmácia reconhece a
   tela e as regras.
2. **Fatias:**
   - **ES1 (esta):** catálogo de materiais, estoque por unidade em lotes, livro de movimentação (entrada, saída por consumo, perda, ajuste),
     estoque mínimo e alertas de mínimo e validade;
   - **ES2:** requisição das unidades ao almoxarifado central, transferência em duas etapas com divergência e inventário com fechamento;
   - **ES3:** a ligação com as Emergências (E4): a necessidade de insumo da E3 atendida pelo estoque, com reserva e consumo extraordinário.
3. **Catálogo** (`TB_MATERIAL_ESTOQUE`), da rede inteira:
   - código e nome únicos (409); código CATMAT opcional;
   - categoria e unidade de medida;
   - **controla lote e validade** ou não (papel A4 não controla). O controle não muda depois da primeira entrada (422).
4. **Estoque por unidade** (`TB_LOTE_MATERIAL`): qualquer unidade guarda material. Com controle de lote, um lote por número; sem, um saldo
   único por material e unidade. O custo unitário da última entrada dá o valor do estoque.
5. **Livro** (`TB_MOVIMENTACAO_ESTOQUE`), só inclusão, com a variação e o saldo depois:
   - **entrada:** nota fiscal (com o número, 400), doação, saldo inicial ou outra; o fornecedor em texto até Compras (#14). Material com lote
     pede lote e validade (400); sem, não aceita (400). Lote vencido não entra (422); o mesmo lote com outra validade, 422;
   - **saída por consumo:** para um setor da unidade (de outra unidade, 422). Sem lote escolhido, sai dos que vencem primeiro (FEFO), em
     quantos lotes precisar, um lançamento por lote. Lote vencido não sai por consumo (422), só como perda. Saldo insuficiente: 422;
   - **perda:** vencimento, avaria, extravio ou outro; mais que o lote, 422;
   - **ajuste de inventário:** a contagem, com justificativa; igual ao saldo, 422.
6. **Mínimo** (`TB_ESTOQUE_MINIMO`), por material e unidade: o ponto de reposição.
7. **Alertas** no saldo: abaixo do mínimo, vencendo em até 90 dias e vencidos com saldo.
8. **Permissões e papéis:**
   - `ESTOQUE.CONSULTAR`, `ESTOQUE.MOVIMENTAR` e `ESTOQUE.GERENCIAR` (catálogo e mínimo), todas de operação;
   - papel novo **Almoxarife** (consultar e movimentar); o **Gestor** consulta e gerencia; a **Coordenação de enfermagem** consulta.
9. **O almoxarifado central fica para a ES2.** O desenho previa marcá-lo pela capacidade administrativa `ESTOQUE`, mas ela pertence ao perfil
   administrativo ([ADR-0031](./0031-perfil-administrativo-por-tipo-de-unidade.md)), não à unidade, e a ES1 não precisa da central: cada unidade tem o próprio
   estoque. A ES2 decide como a central é marcada.

### API

- `GET, POST /api/v1/estoque/material`, `PUT /material/{uuid}`, `PUT /minimo`;
- `POST /entrada`, `/saida`, `/perda`, `/ajuste`;
- `GET /saldo?unidadeId=` (com `categoria`) e `GET /movimentacao` (`unidadeId`, `materialId`, `tipo`, `dias`: 30, até 365).

### Tela

**Estoque e almoxarifado,** em Administrativo: a unidade, o resumo (materiais, abaixo do mínimo, vencendo, valor), e as abas Saldo (por
material, com os lotes e as ações), Movimentação (o livro), Alertas e Catálogo. Gavetas de material, entrada, saída, perda, ajuste e mínimo.

### Migração

`V62__estoque_e_almoxarifado.sql`.

## Consequências

- **Valor do estoque** já existe para o Financeiro (#17) usar depois; custo médio e contabilidade ficam lá.
- **Ficam fora:** compras, contratos e fornecedores cadastrados (#14); patrimônio (#15); código de barras; medicamento e vacina.

## Testes

- **JUnit:**
  - `EstoqueControllerTest`: catálogo e entrada (lote, validade, nota fiscal, lote vencido, mesmo lote); saída pelos que vencem primeiro, com
    setor, lote vencido e saldo insuficiente; perda e ajuste; saldo com mínimo, alertas e valor;
  - `EstoqueAutorizacaoControllerTest`, com a segurança ligada: sem login, o almoxarife fora da unidade e sem catálogo, a coordenação de
    enfermagem que consulta e não movimenta, o ACS sem acesso e o gestor do município.
- **Robot:** `Estoque.robot` (API, cinco casos) e `UI_estoque.robot`.
