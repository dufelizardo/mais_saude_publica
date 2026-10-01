# 0060 — Uma remessa, um lote por unidade

## Status

Aceita e implementada.

## Contexto

A ADR-0059 (transferência entre unidades) registrou como "raro" o risco de duas transferências
simultâneas para a mesma unidade criarem dois lotes da mesma remessa no destino. Na revisão, o
usuário apontou que um risco que existe vai acontecer em algum momento, e a análise mostrou que ele
era maior do que parecia:

1. **A entrada de lote (`POST /api/v1/lote/`) não verificava duplicata.** Cadastrar a mesma remessa
   (medicamento, número do lote e validade) duas vezes na mesma unidade criava dois lotes — sem
   precisar de concorrência nenhuma.
2. **A correção de lote (`PATCH`) também podia gerar duplicata**, trocando número ou validade para os
   de outro lote da mesma unidade.
3. **A transferência** tinha a corrida registrada na ADR-0059.

Com dois lotes da mesma remessa, o estoque dela fica dividido em dois extratos, o saldo exibido por
lote engana, e um recall pelo número do lote precisa juntar os dois à mão.

## Decisão

1. **Invariante: uma remessa tem um único lote ativo por unidade.** Remessa = medicamento + número do
   lote + validade. A mesma remessa em unidades diferentes continua sendo lotes diferentes (ADR-0059).
2. **Entrada de uma remessa que já existe na unidade soma no lote existente.** `POST /api/v1/lote/`
   lança uma nova `ENTRADA` no lote existente e responde **200** ("Entrada registrada no lote já
   existente!"); quando a remessa é nova, cria o lote e responde **201**, como antes. Duas entregas
   da mesma remessa (entrega parcial, reposição) viram dois lançamentos no mesmo extrato.
3. **Correção que transformaria o lote em outra remessa já existente na unidade → 422.** A mensagem
   orienta registrar as quantidades no lote existente. Juntar dois lotes por uma correção esconderia
   a operação.
4. **Sem corrida: a unidade é travada antes de procurar a remessa.** Entrada de lote, correção e
   transferência (na unidade de destino) fazem `SELECT ... FOR UPDATE` na linha da
   `UnidadeDeSaude` antes de procurar a remessa. Uma segunda operação simultânea na mesma unidade
   espera e, ao continuar, já encontra o lote criado pela primeira. A ordem das travas é sempre
   unidade → lotes (em ordem de id), então não há ciclo entre operações.
5. **Garantia no banco: índice único parcial** em `tb_lote (medicamento_id, unidade_id, numero_lote,
   validade) WHERE lote_incorporador_id IS NULL`. É a segunda linha de defesa, para qualquer caminho
   que um dia contorne os serviços. Como o esquema é gerado pelo Hibernate (`ddl-auto=update`), que
   não expressa índice parcial, o índice é criado por `IncorporacaoLotesDuplicados` na subida da
   aplicação (`CREATE UNIQUE INDEX IF NOT EXISTS`).
6. **Duplicados que já existirem são incorporados, não apagados.** Na subida, antes de criar o
   índice, cada grupo de lotes ativos da mesma remessa na mesma unidade fica com o lote mais antigo
   (primeiro lançamento no livro). Para cada duplicado:
   - o saldo sai dele (`INCORPORACAO_SAIDA`) e entra no que fica (`INCORPORACAO_ENTRADA`), com
     justificativa que aponta um para o outro;
   - ele é marcado com `loteIncorporador` e `incorporadoEm`, fica com saldo zero, some da listagem
     de lotes e não aceita mais movimentação (422) nem correção. O extrato continua disponível pelo
     id, e dispensações antigas continuam apontando para ele.

   Os lançamentos do livro são imutáveis (ADR-0057), por isso não se "move" histórico de um lote
   para outro: a incorporação é registrada como mais dois lançamentos. Sem duplicados, a rotina não
   faz nada.

## Trade-offs considerados

**Somar no lote existente (escolhida)** × **recusar a segunda entrada (409/422)**
- ✅ Reflete a operação real: chegou mais da mesma remessa, entra no mesmo lote.
- ✅ Não obriga a tela a ter um fluxo separado de "entrada em lote existente".
- ❌ O `POST` deixa de ser só "criar": responde 200 quando soma. Documentado no Swagger e no teste.

**Travar a unidade (escolhida)** × **só o índice único, tratando a violação**
- ✅ A segunda operação simplesmente espera e soma no lote certo; o usuário não vê erro.
- ❌ Operações de estoque na mesma unidade que podem criar lote passam a ser feitas uma de cada vez.
  São operações curtas; dispensação, perda e ajuste não travam a unidade.

**Incorporar duplicados (escolhida)** × **apagar/mesclar linhas** × **deixar como está**
- ✅ Preserva os dois extratos e registra a junção no livro, com hora e motivo.
- Apagar ou reescrever linhas quebraria a imutabilidade do livro e as dispensações antigas. Deixar
  como está manteria o problema e impediria o índice único.

## Consequências

**Positivas:** o risco deixa de existir nos três caminhos (entrada, correção, transferência), com
garantia no banco; duplicados antigos são corrigidos de forma auditável.

**Negativas / pendências:**
- O índice depende da rotina de subida. Uma ferramenta de migração de esquema (Flyway/Liquibase)
  tornaria isso explícito — hoje nenhuma entidade do projeto usa, fica registrado para quando entrar.
- A transferência em duas etapas (envio e recebimento) é a próxima fatia e segue esta mesma regra no
  recebimento.

## Referências

- [ADR-0057](./0057-livro-de-movimentacao-do-estoque-da-farmacia.md) — livro imutável, que registra
  a incorporação.
- [ADR-0059](./0059-transferencia-de-estoque-entre-unidades.md) — transferência e o risco revisto aqui.
- [ADR-0050](./0050-lote-segunda-entidade-da-farmacia.md) — lote por unidade.
