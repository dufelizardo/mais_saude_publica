# 0089 — Regulação do acesso: agendamento, desfecho e vínculo

## Status

Aceita e implementada (parte 3 de 3). Fecha o ciclo da Central de Regulação do Acesso das ADRs
[0087](./0087-regulacao-do-acesso-backend.md) e [0088](./0088-regulacao-do-acesso-tela.md), e resolve a
pendência da [ADR-0076](./0076-prontuario-por-vinculo-assistencial.md) sobre vínculo pela regulação.

## Contexto

- Até aqui, a autorização registrava a unidade e a data da vaga, mas nada acontecia na unidade
  executante: nenhum agendamento e nenhum registro de que o paciente foi atendido ou faltou.
- A unidade de origem não recebia a **contrarreferência**, o retorno de quem atendeu para quem continua
  o cuidado.
- Com o prontuário por vínculo ligado, o profissional da unidade executante não tinha vínculo com o
  paciente encaminhado e precisaria de acesso justificado.
- O `Agendamento` existente tem paciente, profissional e data, mas não tem unidade.

## Decisão

1. **A autorização vira agendamento na executante.**
   - Se o regulador já sabe quem vai atender, informa a matrícula na autorização, e ela já cria o
     agendamento. A solicitação fica Agendada.
   - Se não sabe, a solicitação fica Autorizada, e a recepção da executante (`AGENDAMENTO.GERENCIAR`
     lá) ou a regulação agenda depois: `POST {uuid}/agendamento`, com o profissional e a data. Sem data,
     vale a da vaga.
   - O agendamento é do tipo Consulta para consulta especializada e Procedimento para exame e
     procedimento. A observação aponta a solicitação.
2. **Desfecho na executante:**
   - **Atendido** (`POST {uuid}/realizacao`):
     - exige `CONSULTA.REGISTRAR` ou `PROCEDIMENTO.REGISTRAR` na executante;
     - registra a **contrarreferência**: o que foi feito, o achado e a conduta, com 10 caracteres ou
       mais;
     - o agendamento fecha como Realizado.
     - A contrarreferência fica na solicitação, com leitura auditada. O evento diz só que houve
       retorno, sem repetir o texto clínico.
   - **Faltou** (`POST {uuid}/falta`):
     - exige `AGENDAMENTO.GERENCIAR` ou `ATENDIMENTO.GERENCIAR` na executante;
     - o agendamento fecha como Cancelado, com a falta na observação, porque o agendamento não tem
       status de falta;
     - para atender depois, é preciso uma nova solicitação.
   - Cancelar uma solicitação agendada também cancela o agendamento.
3. **Quarta condição de vínculo assistencial** (ADR-0076):
   - vale para a unidade **executante** e para a **solicitante**, quando estão no escopo de quem lê;
   - vale enquanto o encaminhamento está em curso (solicitada, devolvida, autorizada ou agendada) ou
     foi realizado dentro da mesma janela do atendimento concluído (30 dias, por padrão);
   - a base aparece na auditoria: "Regulação: unidade executante X" ou "Regulação: unidade
     solicitante Y".
4. **Tela:**
   - Na autorização, há o campo opcional "Quem vai atender".
   - Na aba Solicitações:
     - "Agendar" aparece nas autorizadas;
     - "Atendido" e "Faltou" aparecem nas agendadas;
     - os filtros ganham Agendadas, Realizadas e Faltou.
   - O detalhe mostra com quem e quando está agendado, e a contrarreferência.

## Consequências

- A Central de Regulação do Acesso tem o ciclo completo: solicitação, regulação, agendamento, desfecho
  e retorno para a origem.
- O profissional da executante abre o prontuário do paciente encaminhado sem acesso justificado.
- Falta, na rede, registrar quantas vagas cada unidade oferece por procedimento (cotas e PPI). A vaga
  continua sendo informada pelo regulador.

## Testes

- **JUnit:**
  - autorizar já agendando, e cancelar cancela o agendamento;
  - agendar depois e registrar a realização com contrarreferência, com as recusas de status e de texto
    curto;
  - falta fecha o agendamento;
  - com autorização e vínculo ligados, a executante ganha vínculo ao ser autorizada, e não antes.
- **Robot de API:** agendamento, realização e falta (200 e 422).
- **Robot de interface:**
  - autorizar já agendando;
  - agendar pela linha;
  - registrar atendimento com contrarreferência e vê-la no detalhe;
  - registrar a falta.
