# 0094 — Laboratório assistencial: tela

## Status

Aceita e implementada (parte 2 de 3). Usa a API da [ADR-0093](./0093-laboratorio-backend.md). Os resultados no
prontuário e no atendimento, o laudo imprimível e a recoleta pela tela vêm na ADR-0095.

## Contexto

- A ADR-0093 criou o backend: catálogo, pedido por itens, coleta em amostras, resultado, liberação,
  retificação, rejeição de amostra e cancelamento, com listas de trabalho por etapa.
- Não há protótipo de laboratório. A tela segue o padrão das telas da Assistência (Farmácia e Regulação):
  indicadores, abas, tabelas no `card` e gavetas `dw-*`.
- O atendimento já tinha o "Encaminhar" para a Regulação. Faltava o caminho equivalente para pedir
  exames.

## Decisão

1. **Tela Laboratório** em `/assistencia/laboratorio`, no grupo Assistência, depois da Regulação.
   - Aparece com `EXAME.SOLICITAR` ou qualquer `LABORATORIO.*`.
   - Carrega sob demanda.
2. **Indicadores:** para coletar, em análise, para liberar e urgentes (em qualquer etapa).
3. **Abas pela permissão de cada etapa**, para que cada papel abra no seu trabalho:
   - **Para coletar** (`LABORATORIO.COLETAR`): um pedido por linha, com os exames, o preparo e a
     prioridade. O botão **Coletar** abre a gaveta de coleta.
   - **Em análise** (`LABORATORIO.ANALISAR`): um exame por linha, com o código da amostra. Tem os botões
     **Resultado** e **Rejeitar amostra**.
   - **Para liberar** (`LABORATORIO.LIBERAR`): um exame por linha. O botão **Revisar** abre a gaveta de
     liberação.
   - **Pedidos**, para todos que veem a tela: andamento sem valores, com busca e filtro de situação. O
     botão **Ver** abre o detalhe.
   - **Catálogo de exames**, também para todos: material, referência, prazo e situação. **+ Exame** e
     **Editar** só aparecem com `LABORATORIO.GERENCIAR`.
   - A tela abre na primeira aba permitida, ou na pedida em `?aba=`.
4. **Gavetas:**
   - **Pedido de exames:**
     - paciente com busca;
     - unidade e matrícula de quem pede;
     - exames do catálogo em uso, marcados numa lista com filtro;
     - indicação clínica, CID-10 opcional e prioridade.
   - **Coleta:**
     - o preparo em destaque;
     - os exames do pedido, todos marcados (o que ficar desmarcado continua aguardando);
     - onde foi coletado, que vem com a unidade solicitante;
     - o laboratório que analisa (por padrão, a própria unidade da coleta);
     - a matrícula de quem coletou.
   - **Resultado:**
     - mostra a referência e a unidade do catálogo;
     - campo numérico ou texto, conforme o exame;
     - avisa quando já existe um resultado, que fica guardado.
   - **Revisar e liberar:**
     - valor, referência e a **interpretação calculada** (dentro, acima ou abaixo);
     - quem analisou e quando;
     - **Liberar resultado** ou **Registrar de novo** (para quem também analisa).
   - **Rejeitar amostra:** motivo, observação e matrícula. Avisa que os exames voltam para a coleta.
   - **Detalhe do pedido** (larga):
     - dados do pedido e indicação clínica;
     - exames com situação, resultado liberado com a interpretação, retificação e quem liberou;
     - amostras com laboratório e rejeição;
     - histórico de eventos.
     - Ações por exame: **Retificar** (liberado, com `LABORATORIO.LIBERAR`) e **Cancelar** (não liberado,
       com `EXAME.SOLICITAR` ou `LABORATORIO.ANALISAR`). O backend continua decidindo.
   - **Retificar:** valor correto, motivo e matrícula.
   - **Cancelar exame:** motivo e matrícula.
   - **Exame do catálogo:**
     - nome, material e tipo de resultado;
     - unidade e faixa (numérico) ou referência em texto;
     - preparo, prazo e "Em uso".
5. **Pedir exames no atendimento:** o detalhe do atendimento ganha o botão **Pedir exames** (com
   `EXAME.SOLICITAR`). Ele abre `/assistencia/laboratorio?acao=pedido` com o paciente, a unidade e o
   atendimento já preenchidos, e o pedido fica ligado ao atendimento.
6. A matrícula das gavetas vem preenchida com a de quem está logado, quando é profissional.

## Consequências

- O fluxo inteiro do laboratório pode ser feito pela tela, da coleta à liberação.
- Valores só aparecem no detalhe, que é leitura auditada (ADR-0093). As listas não mostram resultado.
- Até a ADR-0095, o resultado liberado não aparece no prontuário nem no atendimento. A recoleta depois
  da rejeição acontece pela própria aba **Para coletar**, porque o exame volta para ela.

## Testes

- **Robot de interface** (`test/ui/assistencia/laboratorio/UI_laboratorio.robot`, 8 casos):
  - pedido pela gaveta;
  - pedido vazio recusado;
  - coleta pela lista;
  - resultado acima da referência e liberação;
  - rejeição de amostra, com o exame voltando a aguardar coleta;
  - detalhe do pedido;
  - cadastro no catálogo;
  - link do atendimento com o pedido preenchido.
