# 0088 — Regulação do acesso: tela

## Status

Aceita e implementada (parte 2 de 3). Usa a API da [ADR-0087](./0087-regulacao-do-acesso-backend.md). O
fechamento do ciclo (agendamento na executante, vínculo e contrarreferência) fica para a ADR-0089.

## Contexto

- A ADR-0087 criou a Central de Regulação do Acesso no backend: catálogo, solicitação, fila e eventos.
- Faltava a tela. Sem ela, o médico não tinha como encaminhar, e o regulador não tinha como trabalhar a
  fila.
- O encaminhamento nasce durante o atendimento, e é ali que o médico precisa do atalho.

## Decisão

1. **Tela Regulação** em `/assistencia/regulacao`, no grupo Assistência do menu.
   - Aparece com `REGULACAO.CONSULTAR`, `SOLICITAR` ou `REGULAR`.
   - Carrega sob demanda (`loadComponent`).
2. **Cabeçalho e indicadores:** na fila (com a espera média), vermelho ou amarelo aguardando,
   devolvidas e autorizadas.
3. **Abas**, no padrão da Farmácia (`page-tabs` com contagem e navegação por setas):
   - **Fila:** só para quem regula, e é a aba inicial dessa pessoa.
     - Mostra posição, prioridade nas cores da classificação de risco, paciente, procedimento,
       solicitante e espera.
     - Filtra por procedimento e prioridade.
     - Cada linha tem o botão "Analisar".
   - **Solicitações:** o andamento de cada encaminhamento, sem o dado clínico.
     - Mostra a situação em badge e a posição na fila ou a vaga marcada.
     - Tem busca e filtro de situação.
     - Ações: "Ver" ou "Complementar" (quando devolvida) e "Cancelar".
   - **Procedimentos regulados:** o catálogo, com "+ Procedimento" e "Editar" para quem regula.
4. **Gavetas:**
   - **Nova solicitação:**
     - campos: paciente (busca por nome ou CPF), procedimento em uso, unidade solicitante, CID-10,
       matrícula, prioridade proposta nas quatro cores e justificativa;
     - a matrícula já vem com a de quem está logado (ADR-0065).
   - **Detalhe (gaveta larga):**
     - status, prioridade, posição, dados do pedido, CID, justificativa e o histórico em linha do
       tempo;
     - a leitura é auditada pela API;
     - **para quem regula e a solicitação está na fila**, traz a decisão: autorizar com vaga (unidade
       executante, data e hora, orientação ao paciente), devolver (o que falta), negar (motivo) ou
       reclassificar (nova prioridade e motivo);
     - **para quem solicita e a solicitação foi devolvida**, traz o complemento, que devolve à fila na
       posição original.
   - **Cancelar**, com motivo.
   - **Procedimento:** nome, tipo e em uso.
5. **"Encaminhar" no atendimento:**
   - Fica na gaveta do atendimento, para quem tem `REGULACAO.SOLICITAR`.
   - Abre a Regulação com a nova solicitação já preenchida com o paciente e a unidade do atendimento:
     `?acao=nova&pacienteId=&unidadeId=`.
6. **As validações da tela repetem as da API** (CID-10, justificativa de 10 caracteres ou mais, vaga
   que não está no passado, prioridade diferente da atual). A mensagem aparece no campo, sem ida à API.
7. **Correção de estilo encontrada aqui:** `.cell-main` usado direto na `<td>` tinha `display: block` e
   desalinhava a célula. Isso também acontecia nas tabelas do perfil (ADRs 0084 a 0086). Agora
   `td.cell-main` continua célula de tabela.

## Consequências

- O médico encaminha a partir do atendimento ou da tela, e acompanha a fila e a vaga.
- O regulador trabalha a fila do seu escopo com todas as decisões numa gaveta só.
- A recepção vê o andamento e a posição na fila sem ver o dado clínico (sem o botão "Ver").
- Até a ADR-0089, a vaga autorizada aparece na solicitação, mas não vira agendamento na unidade
  executante.

## Testes

- **Robot de interface** (`test/ui/assistencia/regulacao/UI_regulacao.robot`):
  - nova solicitação pela gaveta;
  - autorização com vaga pela fila;
  - devolução pelo regulador e complemento pelo solicitante;
  - CID inválido recusado na gaveta;
  - cadastro de procedimento no catálogo;
  - link do "Encaminhar" abre a gaveta com paciente e unidade.
- O teste de menu (`test/ui/shell`) continua verde com o item novo.
