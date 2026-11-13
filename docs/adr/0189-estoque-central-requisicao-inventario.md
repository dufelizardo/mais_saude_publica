# 0189 — Estoque: almoxarifado central, requisição, transferência e inventário

## Status

Aceita e implementada. Fatia **ES2** do domínio 13 ([ADR-0177](./0177-estoque-e-almoxarifado.md); detalhe em
[`estoque/MODELO-ESTOQUE.md`](../estoque/MODELO-ESTOQUE.md)). A ES3 (emergências) segue só com aprovação.

## Contexto

- **A ES1 deu a cada unidade o próprio estoque** (lotes, livro, mínimo e alertas), mas não havia quem abastecesse a rede: nem a central,
  nem o pedido das unidades, nem a transferência conferida, nem o inventário periódico com fechamento.
- **A ADR-0177 deixou para a ES2 decidir como a central é marcada:** a capacidade administrativa `ESTOQUE` pertence ao perfil
  administrativo ([ADR-0031](./0031-perfil-administrativo-por-tipo-de-unidade.md)), não à unidade.
- **A Farmácia já tem a transferência em duas etapas** ([ADR-0059](./0059-transferencia-de-estoque-entre-unidades.md) e
  [ADR-0061](./0061-transferencia-em-duas-etapas-envio-e-recebimento.md)), o modelo a seguir.

## Decisão

1. **Central de abastecimento:** capacidade nova do modelo operacional `CENTRAL_ABASTECIMENTO`, sem perfil — o mesmo mecanismo da central
   do 192 (`REGULACAO_MEDICA_URGENCIA`, [ADR-0176](./0176-regulacao-de-urgencia.md)). A gestão inclui a capacidade na unidade pelo modelo
   operacional; pode haver mais de uma central, e a unidade escolhe a quem pede.
2. **Requisição** (`TB_REQUISICAO_ESTOQUE` e `TB_ITEM_REQUISICAO_ESTOQUE`, permissão nova `ESTOQUE.REQUISITAR`, dada ao Almoxarife e à
   Coordenação de enfermagem):
   - **Rascunho:** a unidade escolhe a central, a prioridade (normal ou urgente) e os materiais com as quantidades. Sem itens ou com material
     repetido, 400; pedir a quem não é central ou à própria unidade, 422. O rascunho muda até ser enviado.
   - **Situações:** rascunho, enviada, atendida (total ou em parte), recebida, negada e cancelada.
   - **Para justificar e atender:** o detalhe mostra, por item, o saldo e o consumo dos últimos 30 dias da unidade e o saldo da central.
   - **Cancelar:** a unidade cancela o rascunho ou a enviada, com motivo. A central não vê o rascunho.
3. **Atendimento** (permissão nova `ESTOQUE.ATENDER`, dada ao Almoxarife, no escopo da central):
   - de zero até o pedido em cada item (400 acima), sem passar do saldo da central dentro da validade (422); tudo zero, 422 (negue);
   - sai pelos lotes que vencem primeiro, como o consumo da ES1, e vira uma transferência em trânsito;
   - **negar:** a requisição enviada, com motivo.
4. **Transferência em duas etapas** (`TB_TRANSFERENCIA_ESTOQUE` e `TB_ITEM_TRANSFERENCIA_ESTOQUE`):
   - **envio:** a saída da origem no livro (tipos novos `TRANSFERENCIA_SAIDA`, `TRANSFERENCIA_ENTRADA` e `TRANSFERENCIA_ESTORNO`), com a
     transferência no lançamento (`TB_MOVIMENTACAO_ESTOQUE.TRANSFERENCIA_ID`);
   - **recebimento** (`ESTOQUE.MOVIMENTAR` no destino): todos os itens conferidos (400 faltando), até o enviado; o que chegou entra no
     mesmo lote e validade no destino; a diferença pede o motivo (falta, avaria, outro; 400 sem ele). **Falta e outro** voltam ao lote de
     origem como estorno; **avaria** entra e sai como perda no destino. A requisição fica recebida.
   - **Avulsa:** de uma unidade a qualquer outra, por lote (dentro da validade e com saldo), sem requisição — por exemplo, remanejar o que
     está para vencer.
5. **Inventário com fechamento** (`TB_INVENTARIO_ESTOQUE` e `TB_ITEM_INVENTARIO_ESTOQUE`, `ESTOQUE.MOVIMENTAR`):
   - **abertura:** congela os lotes com saldo e o saldo do sistema; um aberto por unidade (409); sem lote com saldo, 422;
   - **bloqueio:** com o inventário aberto, a movimentação da unidade para (422) — entrada, saída, perda, ajuste e transferência. O consumo
     lançado pelo sistema (insumos de exame, [ADR-0183](./0183-laudo-pdf-assinado-e-insumos.md)) segue, porque o atendimento não para;
   - **contagem:** por lote, corrigível enquanto aberto;
   - **fechamento:** com todos contados (422), lança o ajuste de inventário de cada diferença no livro, com a referência do inventário;
   - **cancelamento:** com motivo; nada é ajustado.
6. **Tela Estoque** (Administrativo): abas **Requisições** (a unidade pede e acompanha; a central atende e nega), **Transferências** (o
   destino recebe e confere; a unidade transfere a outra) e **Inventário** (abre, conta, fecha ou cancela).

## Alternativas descartadas

- **Marcar a central por um campo novo na unidade:** duplicaria o que o modelo operacional já faz para capacidades da rede.
- **Movimentação do livro sem bloqueio durante o inventário:** a contagem de um lote que mudou no meio não confere com nada.
- **Requisição gerada pelo mínimo (ponto de reposição):** fica para depois; a unidade vê o alerta de mínimo e pede.

## Consequências

- **STATUS-DOS-DOMINIOS:** a ES2 sai das pendências do domínio 13; segue a ES3.
- **Implantação:** incluir a capacidade Central de abastecimento no almoxarifado central e conceder `ESTOQUE.REQUISITAR` e
  `ESTOQUE.ATENDER` (o Almoxarife já tem as duas) quando a autorização for ligada.
- **Fora do escopo:** ponto de reposição automático; rota e veículo da entrega (Transporte); fornecedor e compra (domínio 14); código de
  barras.

## Testes

- **JUnit:**
  - `CentralEstoqueControllerTest`:
    - requisição do rascunho ao recebimento: central inválida, atendimento acima do pedido e acima do saldo, atendimento em parte pelos
      lotes que vencem primeiro, recebimento sem conferir tudo e sem motivo, falta voltando à origem e avaria como perda no destino;
    - negar e cancelar com motivo; transferência avulsa com saldo insuficiente e para a própria unidade;
    - inventário: duplicado, sem lote, bloqueio da saída, fechamento sem contar tudo, fechamento com ajustes e cancelamento;
  - `CentralEstoqueAutorizacaoControllerTest`, com a segurança ligada: o almoxarife da UBS pede e recebe; o da central atende mas não pede
    pela UBS e não vê o rascunho; o de outra unidade recebe 403.
- **Robot:** `EstoqueCentral.robot` (API, CT-001 a CT-003) e os casos CT-002 e CT-003 de `UI_estoque.robot`.
