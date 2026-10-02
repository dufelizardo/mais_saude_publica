# 0091 — Agenda do profissional

## Status

Aceita e implementada no backend (parte 1 de 2). A tela, a partir do protótipo `Agenda.html`, vem na parte 2.
Desenha o `HORARIO` previsto no DER (seção 12) e resolve a pendência de conflito de horário da
[ADR-0042](./0042-agendamento-independente-do-atendimento.md).

## Contexto

- O agendamento tinha paciente, profissional, data e status, sem **unidade**, sem duração e sem regra:
  qualquer horário era aceito, inclusive dois pacientes no mesmo instante.
- O DER previa `HORARIO` (profissional, dia da semana, início, fim, intervalo de 15 minutos), também sem
  unidade. Mas o mesmo profissional atende em mais de uma unidade, e a agenda é sempre de um lugar.
- O protótipo `Agenda.html` mostra:
  - agenda recorrente com vagas por bloco ("4 vagas / 15 min");
  - encaixe;
  - falta que libera a vaga;
  - bloqueios (almoço, unidade fechada, indisponível);
  - indicadores de ocupação, encaixes e faltas.
- A Regulação (ADR-0089) agendava na executante com a data que o regulador digitava, sem saber se havia
  vaga.

## Decisão

1. **Bloco de agenda** (`TB_BLOCO_AGENDA`): a agenda recorrente de um profissional numa unidade.
   - Campos: dia da semana, início, fim, duração da vaga (5 a 240 minutos), tipo (consulta,
     procedimento, retorno) e vigência.
   - **Mudar um bloco é encerrá-lo e criar outro**, e o histórico fica, como os outros dados temporais do
     projeto.
   - O profissional não fica em dois lugares ao mesmo tempo: bloco que cruza horário e vigência com outro
     dele, **em qualquer unidade**, é recusado (409).
2. **Bloqueio** (`TB_BLOQUEIO_AGENDA`): um período sem vagas, de um profissional (numa unidade ou em
   todas) ou da unidade inteira, com motivo: folga, reunião, capacitação, unidade fechada ou outro.
   - Pode ser removido (é planejamento, não registro clínico), e a remoção vai para a auditoria.
3. **Férias e afastamentos aprovados ou em andamento no RH fecham a agenda** naqueles dias, sem duplicar o
   dado: são lidos na hora de montar a agenda. O almoço é o intervalo entre blocos; não precisa de bloqueio.
4. **Vagas:** o bloco fatiado na duração, menos o que está bloqueado, o que cai em afastamento e o que já
   tem marcação. `GET /agenda/vagas` traz as livres a partir de agora.
5. **Agendamento:** ganha **unidade**, **encaixe** e o status **FALTOU**.
   - **Com unidade**, a marcação segue a agenda: precisa cair numa vaga livre, ou ser encaixe. Em nenhum
     caso entra em bloqueio ou com o profissional afastado (422).
   - **Sem unidade**, segue como antes, para não quebrar quem já marca assim.
   - `POST /agendamento/{uuid}/falta` registra a falta, só em agendado ou confirmado.
6. **Agenda do período** (`GET /agenda?profissionalMatricula&unidadeId&de&ate`, até 62 dias):
   - **dia a dia:** vagas livres, marcações, encaixes, bloqueios e afastamento;
   - **resumo:** vagas ofertadas e ocupadas, ocupação (%), marcações, encaixes e faltas.
   - Entram as marcações da unidade e as antigas sem unidade, que ocupam o mesmo profissional.
7. **Regulação:**
   - o agendamento criado pela regulação entra na agenda da unidade executante;
   - a falta registrada lá passa a ser `FALTOU`, e não mais `CANCELADO`.
   - Escolher a vaga livre na autorização é da tela (parte 2).
8. **Acesso:**
   - montar a agenda e criar blocos ou bloqueios exige `AGENDAMENTO.GERENCIAR` na unidade;
   - ver a agenda e as vagas aceita também `ATENDIMENTO.GERENCIAR` e `REGULACAO.REGULAR`, no escopo da
     unidade.
9. **Migração V3** (ADR-0090): colunas novas no agendamento, a restrição de status com `FALTOU`, as duas
   tabelas e os índices da leitura da agenda.

## Consequências

- A rede passa a ter vaga de verdade: ocupação medida, encaixe explícito e falta contada.
- O teste de migração (`MigracoesDoEsquemaTest`) passou a conhecer os enums do JDK gravados como texto (o
  dia da semana).
- **Ficam para depois**, porque dependem de domínios que não existem: atividades coletivas (grupo,
  vacinação aberta, visita domiciliar), programas (HiperDia, pré-natal), especialidade e equipe.
- Feriados não são calculados: a unidade fechada é um bloqueio.

## Testes

- **JUnit** (`AgendaControllerTest`):
  - blocos com as vagas, fim antes do início e bloco cruzado em outra unidade;
  - encerramento do bloco;
  - marcação na vaga, vaga ocupada, fora da agenda e encaixe, com o resumo;
  - falta;
  - bloqueio e remoção;
  - férias do RH fechando a agenda;
  - marcação sem unidade como antes.
- O teste da Regulação confere a unidade do agendamento e a falta como `FALTOU`.
- **Robot de API:** blocos, encerramento, agenda, bloqueio e falta de agendamento (12 casos).
