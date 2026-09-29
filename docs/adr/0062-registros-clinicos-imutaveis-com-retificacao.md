# 0062 — Registros clínicos imutáveis, com retificação

## Status

Aceita e implementada (backend). A tela Atendimentos, que usa este modelo, é a próxima fatia.

## Contexto

Triagem e evolução de enfermagem (ADRs 0047 e 0048), consulta (ADR-0043) e procedimento
(ADR-0044) aceitavam `PATCH` livre: qualquer campo podia ser sobrescrito, sem guardar a versão
anterior, sem motivo e sem registrar quem mudou nem quando. O próprio protótipo da tela Atendimentos
avisava "Edição sem histórico" e pedia um "estou ciente" antes de salvar.

Para registro clínico isso não serve. A orientação do COFEN para anotação de enfermagem — e a prática
de prontuário em geral — é que o que foi registrado não se altera: uma correção é uma nova anotação
que se refere à anterior, preservando o texto original, o autor e a hora. É o mesmo princípio que a
ADR-0057 aplicou ao estoque da Farmácia.

Na mesma revisão apareceram mais três pontos do fluxo de atendimento:

1. A lista de atendimentos do protótipo mostra CPF e idade do paciente, o risco da última triagem e
   quantos registros cada atendimento tem — hoje isso exigiria uma chamada por atendimento.
2. "Iniciar atendimento a partir do agendamento" e "marcar o agendamento como realizado" eram duas
   chamadas separadas, que podiam falhar pela metade.
3. O `PATCH` do atendimento trocava o paciente mesmo com triagem, consulta e evolução registradas —
   o que levaria esses registros para o prontuário de outra pessoa.

## Decisão

1. **Triagem, evolução de enfermagem, consulta e procedimento não são editados.** O `PATCH` dessas
   quatro rotas sai.
2. **Correção é retificação** (`POST /api/v1/{recurso}/{id}/retificacao`): grava uma **nova versão**
   com os campos corrigidos e o `motivoRetificacao` (obrigatório), ligada à anterior por
   `retificacaoDe`. A versão anterior continua existindo e no prontuário, marcada como `retificado`
   e apontando para quem a corrigiu (`retificadoPorUuid`). Regras:
   - só a versão vigente pode ser retificada (422 se já foi) — a cadeia é linear;
   - a retificação fica no mesmo atendimento (ou, no procedimento, na mesma consulta) do registro
     original (400 se mudar);
   - a versão é travada durante a retificação, para duas retificações simultâneas não criarem ramos.
3. **Todo registro clínico novo guarda a hora do servidor (`registradoEm`) e o CPF do usuário
   autenticado (`registradoPorCpf`)** quando o login está ligado (ADR-0055). O `dataHora` informado
   continua sendo a hora do ato clínico; `registradoEm` é quando foi digitado.
4. **O desfecho do procedimento é um evento, não uma edição** (`POST /api/v1/procedimento/{id}/status`):
   um procedimento `AGENDADO` passa uma única vez para `REALIZADO` (com a data em que foi feito) ou
   `CANCELADO` (com justificativa), registrando o profissional e a hora. A data que estava prevista
   fica guardada em `dataPrevista`. Depois disso, correção é retificação.
5. **Procedimentos de uma consulta retificada** aparecem, no prontuário, sob a versão vigente da
   consulta; um procedimento novo só pode ser registrado na versão vigente (422).
6. **Prontuário** traz todas as versões, cada uma com `retificado`/`retificadoPorUuid`/`retificacaoDeUuid`;
   quem exibe decide se mostra o histórico. A tela de Pacientes mostra só as vigentes nos sinais vitais
   e no histórico.
7. **Atendimento:**
   - `GET /atendimento/` e `GET /atendimento/{id}` trazem `pacienteCpf`, `pacienteDataNascimento`,
     `classificacaoRiscoAtual` (triagem vigente mais recente) e os totais de triagens, consultas,
     procedimentos e evoluções vigentes, calculados em consultas agrupadas (sem uma chamada por
     atendimento);
   - criar um atendimento a partir de um agendamento marca o agendamento como `REALIZADO` na mesma
     transação; agendamento de outro paciente → 400; `CANCELADO` ou já `REALIZADO` → 422;
   - o paciente não pode ser trocado depois que o atendimento tem registro clínico (422).

## Trade-offs considerados

**Nova versão ligada à anterior (escolhida)** × **tabela de histórico (auditoria de alterações)** ×
**manter o PATCH e registrar log**
- ✅ A versão anterior continua sendo um registro completo e consultável, com o mesmo formato; o
  prontuário mostra exatamente o que foi escrito em cada momento.
- ✅ Não depende de infraestrutura de auditoria transversal (ADR-0054), que ainda não existe.
- ❌ Listagens e contagens precisam distinguir o vigente do retificado. Resolvido com
  `retificado` nas respostas e consultas de contagem que ignoram versões corrigidas.
- Uma tabela de histórico ou um log mantêm a "edição" como operação normal e escondem a correção num
  lugar à parte — o oposto do que a norma pede.

**Desfecho do procedimento como evento (escolhida)** × **tratar mudança de status como retificação**
- ✅ Um procedimento agendado que é realizado não foi "registrado errado": é o fluxo normal. Tratá-lo
  como retificação encheria o prontuário de versões e de motivos artificiais.

**Qualquer profissional pode retificar (escolhida por ora)** × **só o autor**
- A norma recomenda que a correção seja feita por quem registrou. Sem o modelo de papéis e escopo
  (ADR-0054), o sistema não consegue garantir isso com segurança; a retificação registra quem
  corrigiu, e a restrição entra junto com o RBAC.

## Consequências

**Positivas:** o prontuário deixa de poder ser reescrito sem rastro; toda correção tem motivo, autor
e hora; o fluxo do protótipo Atendimentos (lista com resumo, iniciar a partir do agendamento) fica
atendido pela API.

**Negativas / pendências:**
- **Contrato alterado:** o `PATCH` de triagem, evolução, consulta e procedimento deixa de existir.
  As telas antigas de Consultas e Procedimentos passaram a "Retificar" (com motivo); elas serão
  substituídas pela tela Atendimentos.
- Registros anteriores a esta ADR não têm `registradoEm`/`registradoPorCpf`.
- Restringir a retificação ao autor depende do RBAC (ADR-0054).
- Atendimento e agendamento continuam editáveis por `PATCH`: são registros administrativos, não
  clínicos. Revisitar se algum campo deles passar a ter valor clínico.

## Referências

- [ADR-0043](./0043-consulta-registrada-durante-o-atendimento.md), [ADR-0044](./0044-procedimento-realizado-durante-a-consulta.md),
  [ADR-0047](./0047-triagem-primeira-entidade-da-enfermagem.md), [ADR-0048](./0048-evolucao-de-enfermagem-segunda-entidade-da-enfermagem.md)
  — as quatro entidades que deixam de ser editáveis.
- [ADR-0045](./0045-prontuario-agregacao-de-leitura.md) — prontuário, que passa a trazer as versões.
- [ADR-0057](./0057-livro-de-movimentacao-do-estoque-da-farmacia.md) — mesmo princípio aplicado ao estoque.
- [ADR-0054](./0054-modelo-de-identidade-autorizacao-e-auditoria.md) — RBAC e escopo, dos quais depende restringir a retificação ao autor.
- [ADR-0055](./0055-primeira-implementacao-de-login-usuario-jwt-e-toggle-por-ambiente.md) — usuário autenticado, origem do `registradoPorCpf`.
