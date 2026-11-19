# 0219 — Financeiro: liquidação, pagamento, diárias do TFD e execução (F2)

## Status

Aceita e implementada. Segunda fatia do domínio 17; o desenho está na [ADR-0218](./0218-financeiro-dotacao-e-empenho.md).

## Contexto

- **A F1 deixou a dotação e o empenho,** e a ordem de fornecimento consumindo o saldo do empenho. Faltava o segundo e o terceiro
  estágio da despesa: a liquidação (Lei 4.320/1964, art. 63) e o pagamento (art. 64).
- **O atesto já existe na plataforma em três lugares:**
  - o recebimento definitivo pelo fiscal (ADR-0214);
  - a ordem de serviço de manutenção concluída por contrato (ADR-0216);
  - a autorização do TFD pela comissão (ADR-0190), que dá direito à diária ou à ajuda de custo (Portaria SAS/MS 55/1999).
- **A Lei 14.133/2021, art. 141,** exige pagar na ordem cronológica das exigibilidades, por fonte. A quebra da ordem precisa de
  justificativa.
- **O empenho de um exercício que não foi pago vira resto a pagar no seguinte:** processado, se já foi liquidado; não processado, se não
  foi.

## Decisão

1. **Fila a liquidar:** cada atesto ainda sem liquidação aparece com o valor, o credor, o contrato e o empenho da ordem, quando há um.
   - **Recebimento definitivo:** o aceito vezes o preço do contrato.
   - **Ordem de serviço por contrato concluída:** o custo da ordem (mão de obra e peças).
   - **Diária do TFD concedida:** os dias vezes o valor da diária.
2. **Liquidação,** por quem gerencia o Financeiro:
   - o atesto existe (404) e ainda não foi liquidado (409; os índices únicos no banco garantem);
   - o **empenho** é o da ordem de fornecimento, quando ela tem um; senão, é do mesmo credor e não aponta outra ata ou contrato (422);
   - o **bruto** vai até o valor do atesto e até o **saldo a liquidar** do empenho (o valor atual menos o já liquidado) (422);
   - as **retenções** (ISS, IRRF, INSS e outras) não passam do bruto (400); o **líquido** é o bruto menos as retenções;
   - a **nota** vem do recebimento, quando não é informada; o **vencimento** é de 30 dias, quando não é informado;
   - a **liquidação avulsa** (conta de consumo, serviço fora de Compras) pede a justificativa e a matrícula de quem atestou (400).
3. **Pagamento:**
   - só da liquidação não paga (422);
   - a data fica entre a liquidação e hoje (400); a ordem bancária vem do SIAFIC;
   - **ordem cronológica:** se houver outra liquidação da mesma fonte, não paga, com vencimento anterior e já vencida, o pagamento pede
     a justificativa (422), que fica registrada.
4. **Diárias e ajuda de custo do TFD:**
   - a solicitação é de TFD (422) e está autorizada, programada ou realizada (422);
   - o beneficiário é o paciente ou o acompanhante, que precisa estar na solicitação (422);
   - uma diária por beneficiário (409);
   - o valor é os dias (de 1 a 30) vezes a diária da tabela do município;
   - o **credor é a pessoa física**: escolhida no cadastro de fornecedores (422 se for CNPJ) ou indicada pelo CPF. Pelo CPF, o credor é
     encontrado ou cadastrado com o nome do beneficiário, para quem autoriza o TFD não precisar do acesso a Compras;
   - a liquidação usa um empenho do próprio credor.
5. **Restos a pagar:** calculados dos empenhos de exercícios anteriores, sem rotina na virada do ano:
   - **processados:** o liquidado não pago;
   - **não processados:** o valor atual não liquidado.

   O cancelamento continua pela anulação do empenho (F1).
6. **Execução do exercício:**
   - por dotação e por fonte: autorizado, empenhado, liquidado e pago;
   - a fila a liquidar (quantidade e valor);
   - as liquidações vencidas e não pagas;
   - o prazo médio do atesto ao pagamento.
7. **Acesso:**

   | Permissão | O que libera |
   |-----------|--------------|
   | `FINANCEIRO.CONSULTAR` | A fila, as liquidações, os restos a pagar, a execução e as diárias |
   | `FINANCEIRO.GERENCIAR` | Liquidar, pagar e conceder a diária |
   | `TFD.AUTORIZAR` | Conceder e ver as diárias do TFD |
8. **Tela:**
   - no Financeiro, as abas **A liquidar** (com a liquidação avulsa), **Liquidações** (com o pagamento), **Restos a pagar** e
     **Execução**;
   - na Frota, a solicitação de TFD autorizada ganha **Diárias**.
9. **Banco:** a `V100__financeiro_liquidacao_e_pagamento.sql` cria `tb_liquidacao` e `tb_diaria_tfd`.

## Alternativas descartadas

- **Liquidação parcial do mesmo atesto em várias vezes:** o recebimento definitivo já é a entrega aceita com a nota. Um atesto, uma
  liquidação, o que deixa o índice único no banco simples. O valor menor que o atesto continua possível.
- **Rotina na virada do exercício para inscrever os restos a pagar:** os restos derivam dos empenhos e das liquidações. Calcular na
  leitura evita a rotina e a trava entre réplicas (ADR-0209).
- **Diária paga por um empenho global de "beneficiários do TFD":** o empenho identifica o credor (Lei 4.320/1964, art. 61). Um empenho
  sem credor certo não se sustenta na prestação de contas.

## Consequências

- **A despesa da saúde fica completa na plataforma:** da dotação ao empenho, ao atesto, à liquidação e ao pagamento, cada passo com o
  documento de origem.
- **A F3 parte das liquidações** para apurar o custo dos contratos por unidade.
- **Pendências:**
  - a importação das ordens bancárias e dos empenhos do SIAFIC e o SIOPS (domínio 20);
  - a tabela de valores das diárias como cadastro (hoje o valor é informado na concessão).

## Testes

- **JUnit `LiquidacaoControllerTest`:**
  - o recebimento na fila com o valor e o empenho da ordem;
  - a liquidação: outro empenho (422), acima do atesto (422), retenções acima do bruto (400), o líquido, a nota herdada, o vencimento
    e a repetida (409);
  - a avulsa sem justificativa (400);
  - o pagamento: data futura (400), fora da ordem cronológica (422), pago e pago de novo (422);
  - a execução da dotação; o saldo a liquidar do empenho (422);
  - a ordem cronológica com a justificativa registrada e as vencidas na execução;
  - a diária: TFD não autorizado, acompanhante ausente e credor pessoa jurídica (422), o valor total, a repetida (409), a fila, a
    liquidação com o empenho de outro credor (422) e do próprio, e o credor cadastrado pelo CPF (400 se inválido).
- **JUnit `LiquidacaoAutorizacaoControllerTest`:**
  - só o Financeiro liquida (o gestor e o Compras, 403);
  - o gestor vê a fila e a execução;
  - outro profissional não vê nem concede a diária (403); sem login, 401.
- **Robot de API:** `test/financeiro/Liquidacao.robot`, CT-001 a CT-004.
- **Robot de tela:** `UI_financeiro.robot`, CT-004 (liquidar e pagar) e CT-005 (diárias do TFD na Frota).
