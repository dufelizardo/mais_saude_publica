# 0214 — Compras: ordem de fornecimento, recebimento e painel (CP3)

## Status

Aceita e implementada. Terceira e última fatia do domínio 14 (o desenho está na [ADR-0212](./0212-compras-fornecedores-e-solicitacoes.md); a
CP2, na [ADR-0213](./0213-compras-contratacao-atas-e-contratos.md)). Com ela, o domínio 14 fica completo.

## Contexto

- **A CP2 deixa a ata ou o contrato com saldo por item,** mas nada consome esse saldo, e o que chega não entra em estoque.
- **A Lei 14.133/2021, art. 140,** separa o recebimento em duas fases:
  - **provisório:** a entrega física, com a nota fiscal;
  - **definitivo:** a verificação de que o objeto atende ao contrato.

  O recebimento é feito pelo fiscal designado (art. 117).
- **O Estoque guarda o fornecedor em texto livre** (ADR-0177). O cadastro de fornecedores existe desde a CP1.
- **O fiscal costuma ser da unidade** (um enfermeiro, um almoxarife, um gerente), sem permissão de Compras nem de movimentar o Estoque.

## Decisão

### Ordem de fornecimento

1. **Emissão.** O setor de compras (`COMPRAS.GERENCIAR`) emite a ordem contra a ata ou o contrato **vigente** (422 fora da vigência).
   - **Dados:** a unidade de entrega, o número do empenho em texto e o prazo de entrega. Desde a
     [ADR-0218](./0218-financeiro-dotacao-e-empenho.md), o empenho do Financeiro pode ser escolhido e a ordem consome o saldo dele. O prazo vai de 1 a
     365 dias e é de 30 sem informar.
   - **Itens:** são do instrumento (422) e não se repetem (400).
   - **Saldo:** cada quantidade fica dentro do saldo (422), que é **reservado na emissão**. Duas ordens não usam o mesmo saldo.
   - **Número:** `AAAA/000001`.
2. **Situações:** emitida → parcialmente recebida → recebida, ou cancelada.
   - O cancelamento é da ordem aberta e sem recebimento provisório pendente (422).
   - O que não foi aceito volta ao saldo.

### Recebimento (art. 140)

3. **Quem recebe:** o **fiscal designado** ou quem compra; os outros recebem 403. O gestor do contrato acompanha, mas não recebe.
4. **Provisório,** na ordem aberta (422):
   - a nota fiscal com a data até hoje (400);
   - as quantidades entregues: itens da ordem (422), sem repetir (400), até o que falta entregar (422);
   - o **lote e a validade**, obrigatórios no medicamento e no material que controla lote, e proibidos nos outros (400). Lote vencido
     não entra (422).

   Uma ordem aceita várias entregas.
5. **Definitivo,** só de recebimento provisório (422):
   - o aceito de cada item entregue, todos e só eles (400), até o entregue (400);
   - o recusado pede o motivo (400) e vira **ocorrência de item fora da especificação**. A quantidade recusada volta a faltar na ordem.

   Quando tudo é aceito, a ordem fica recebida; antes disso, parcialmente recebida.
6. **Entrada do aceito na unidade da ordem:**
   - **material:** no **Estoque**, com a origem nota fiscal, o número da nota, o lote e a validade e o custo pelo preço do contrato. O
     **fornecedor fica ligado pelo cadastro** (`fornecedor_id` na movimentação), sem deixar o texto livre de lado;
   - **medicamento:** como **lote na Farmácia**, com o número e a validade informados (ADR-0060);
   - **serviço:** fica atestado, sem estoque.

   A entrada passa por métodos internos do Estoque e da Farmácia, que **não conferem** `ESTOQUE.MOVIMENTAR` nem
   `FARMACIA.GERENCIAR_ESTOQUE`. A conferência foi feita antes, ao verificar que quem recebe é o fiscal do contrato. A auditoria
   registra a unidade.

### Ocorrências

7. **Tipos:**
   - **atraso:** **nasce sozinho**, uma vez por ordem, quando a ordem aberta passa do prazo. Fica registrado na próxima leitura das
     ocorrências ou do painel, sem rotina agendada (ADR-0209) e sem registro à mão (400);
   - **entrega parcial** e **outra:** registradas pelo fiscal ou por quem compra;
   - **fora da especificação:** nasce da recusa no definitivo.
8. **Tratamento,** só de ocorrência aberta (422), por quem compra:
   - **encerrar** com a resposta do fornecedor;
   - **converter em sanção.** A sanção segue as regras do art. 156 da CP1 e fica ligada à ocorrência.

### Painel e atalhos

9. **Painel de Compras** (`COMPRAS.GERENCIAR`). O período padrão são os últimos 12 meses, e mais de um ano dá 400. Mostra:
   - as solicitações em andamento por situação;
   - o valor contratado no período, pela data da homologação;
   - as ordens em aberto e em atraso, com a lista das atrasadas;
   - os contratos vencendo em 90 dias e os itens com saldo abaixo de 20%;
   - as ocorrências abertas;
   - o **desempenho por fornecedor** nas ordens do período: entregas, percentual no prazo, prazo médio da emissão à entrega,
     quantidade recusada e ocorrências.
10. **Sala de Situação:** um alerta para cada ordem atrasada há mais de 10 dias (até 3), com o link para o painel.
11. **"Meus contratos" no menu.** O desenho previa um cartão em "Meu dia", mas essa tela não existe. No lugar, entra um item de menu
    que aparece só para quem é gestor ou fiscal de algum contrato e não vê Compras pela permissão, com o número de ordens à espera de
    entrega. Ele leva à tela de Compras na aba Contratos; a rota só exige login. Isso resolve a pendência deixada pela ADR-0213.
12. **Tela:**
    - a gaveta do contrato ganha as ordens e a emissão;
    - a gaveta da ordem tem os itens (pedido, aceito, em recebimento, falta), o recebimento provisório, o definitivo item a item, as
      ocorrências e o cancelamento;
    - a nova aba **Painel** traz os números, as atrasadas, o desempenho e o tratamento das ocorrências.
13. **Banco:** a `V95__compras_ordens_e_recebimento.sql` cria `tb_ordem_fornecimento`, `tb_item_ordem_fornecimento`,
    `tb_recebimento_compra`, `tb_item_recebimento_compra` e `tb_ocorrencia_fornecedor`. Também acrescenta `fornecedor_id` em
    `tb_movimentacao_estoque`.

## Alternativas descartadas

- **Baixar o saldo só no recebimento:** duas ordens poderiam prometer o mesmo saldo ao fornecedor. A reserva na emissão é o que a ordem
  representa.
- **Dar `ESTOQUE.MOVIMENTAR` ao fiscal:** ele ganharia saída, perda e ajuste em todo o estoque da unidade. A entrada pelo recebimento é
  um efeito do atesto, não uma movimentação livre.
- **Rotina diária para o atraso:** o atraso é derivado da data. Registrar a ocorrência na leitura evita rotina e trava entre réplicas
  (ADR-0209).
- **Recebimento em uma fase só:** a lei separa a entrega da conformidade. Na prática, o almoxarifado confere a nota na porta, e o fiscal
  atesta depois.

## Consequências

- **O domínio 14 fica completo:** da demanda (DFD) ao estoque, com o fornecedor cadastrado do começo ao fim.
- **O custo do Estoque passa a vir do contrato** nas entradas por recebimento. O custo médio e a contabilidade são do domínio 17.
- **Fora daqui:** a liquidação e o pagamento (domínio 17); a nota fiscal eletrônica (XML), o PNCP e o Compras.gov (domínio 20).

## Testes

- **JUnit `EntregasControllerTest`:**
  - a ordem acima do saldo e com item de outro instrumento (422), o saldo reservado e o número de ordens abertas no contrato;
  - o provisório: nota futura e lote ausente ou indevido (400), quantidade acima do pendente (422);
  - o definitivo: item faltando, aceito acima do entregue e recusa sem motivo (400), definitivo repetido (422);
  - a entrada no Estoque (fornecedor ligado, nota, lote, custo) e na Farmácia (lote);
  - o cancelamento que devolve o saldo;
  - o atraso que nasce uma vez, a ocorrência à mão, o encerramento, a sanção e o painel com o período inválido (400).
- **JUnit `EntregasAutorizacaoControllerTest`:**
  - o fiscal com papel de recepção recebe, faz o definitivo (com entrada no Estoque) e registra ocorrência;
  - o fiscal não emite ordem nem vê o painel;
  - o gestor do contrato acompanha e não recebe;
  - outro profissional recebe 403; sem login, 401.
- **Robot de API:** `test/compras/Entregas.robot`, CT-001 a CT-004.
- **Robot de tela:** `UI_compras.robot`, CT-007 (ordem e recebimento) e CT-008 (painel).
