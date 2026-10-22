# 0132 — Programas: ações programáticas (P4)

## Status

Aceita e implementada (fatia P4 da [ADR-0109](./0109-programas-acoes-e-linhas-de-cuidado.md); depende da P2,
[ADR-0119](./0119-programas-acompanhamento.md)).

## Contexto

- **A P2 acompanha o paciente no programa** e aponta faltosos e atrasados, mas não diz **o que fazer** com eles: a
  consulta programada do hipertenso, a aferição, a busca ativa de quem sumiu, o grupo de gestantes.
- **O MODELO-PROGRAMAS separa ação programática de programa:** é a atividade concreta prevista, realizada pelos
  serviços, ligada ao registro assistencial que a cumpriu.
- **A Agenda não sabia de programa.** O protótipo e as pendências da Agenda (ADR-0092) pedem o programa na marcação
  (HiperDia, pré-natal) e as atividades coletivas.
- **O Território já diz quem é o ACS de cada microárea** (ADRs 0125 e 0128). É quem faz a busca ativa.

## Decisão

1. **Ação programática** (`AcaoProgramatica`, `tb_acao_programatica`):
   - **programa** (ativo; a ligação com campanha entra na P3);
   - **tipo:** consulta programada, aferição, exame, vacina, visita domiciliar, busca ativa, grupo ou atividade coletiva,
     orientação, outra;
   - **individual:** pelo **acompanhamento vigente** do paciente no programa (422 se encerrado ou de outro programa); a
     unidade e a equipe vêm dele. Consulta programada, visita domiciliar e busca ativa **só são individuais** (400);
   - **coletiva:** pela **unidade** (obrigatória, 400), com equipe, local e **participantes**, que precisam ser
     acompanhados no programa (422; `tb_acao_programatica_participante`);
   - **responsável** (com lotação vigente na unidade, 422), **data prevista**, descrição;
   - **situação:** pendente → realizada, não realizada ou cancelada (estas duas pedem o motivo, 400). Só a pendente muda
     (422). **Vencida** é a pendente com a data passada.
2. **Realização:** a data (até hoje, 400), o **atendimento que a cumpriu** (do mesmo paciente, 422; a coletiva não se
   liga a atendimento, 400), a observação e, na coletiva, quem participou.
   - **Busca ativa pede o resultado** (400): encontrado e agendado, encontrado e recusou, não encontrado, mudou de
     endereço. O resultado é só da busca ativa (400).
   - **"Mudou de endereço" sugere** encerrar o acompanhamento (transferência), sem encerrar sozinho.
3. **Busca ativa a partir da P2:** "Busca ativa" nos faltosos e atrasados da lista de acompanhados
   (`POST /acompanhamento/{id}/busca-ativa`).
   - **Responsável sugerido:** o ACS da microárea onde o paciente está adscrito, se lotado na unidade do acompanhamento.
   - **Uma pendente por acompanhamento** (409).
4. **Programa na marcação da Agenda:**
   - o agendamento ganha o **programa** (opcional); o paciente precisa ser acompanhado nele (422);
   - a consulta programada pendente tem **"Agendar"**: a Agenda abre com o paciente, o programa e a ação, e a marcação
     fica **ligada à ação** (`acaoProgramaticaId`; do mesmo paciente e programa, 422);
   - quando o atendimento dessa marcação é **concluído**, a ação aparece como **"pronta para confirmar"**, com o
     atendimento sugerido. A rede aponta; o profissional confirma.
5. **Permissões** (sem permissão nova): `PROGRAMA.ACOMPANHAR` na unidade da ação, como o acompanhamento. **Programa
   sensível:** a ação só aparece para quem acompanha na unidade (a mesma regra da P2). As ações do paciente seguem as
   regras do prontuário (vínculo assistencial e leitura auditada).
6. **API:**
   - `GET /acoes-programaticas` (filtros: programa, unidade, equipe, responsável, período e situação; padrão, a semana;
     as vencidas de antes entram; indicadores: pendentes, vencidas, realizadas no mês, prontas para confirmar);
   - `POST /acao-programatica` (201), `POST /acompanhamento/{id}/busca-ativa` (201);
   - `POST /acao-programatica/{id}/realizacao` e `POST /acao-programatica/{id}/situacao`;
   - `GET /paciente/{id}/acoes-programaticas`;
   - o agendamento ganha `programaId` e `acaoProgramaticaId` na entrada e o programa na saída.
7. **Telas:**
   - **Programas de Saúde:** abas "Programas" e "Ações programáticas" (filtros, período de 1 ou 4 semanas ou o mês,
     indicadores, prever, confirmar, não realizada, cancelar, agendar); no detalhe do programa, "Busca ativa" nos
     faltosos e atrasados e as ações do programa;
   - **Paciente, aba Programas:** as ações do paciente;
   - **Agenda:** o programa na marcação e a marcação vinda de "Agendar".
8. **Migração V25:** `tb_acao_programatica`, `tb_acao_programatica_participante` e `tb_agendamento.programa_id`.

## Consequências

- **O "atrasado" da P2 vira trabalho:** a busca ativa com o ACS e o resultado, e a consulta programada agendada e
  confirmada.
- **A Agenda passa a saber de programa,** o que serve aos indicadores da P6 (consultas de HiperDia, pré-natal).
- **A ação coletiva da saúde bucal (SB5)** reaproveita esta entidade, com o detalhe odontológico.
- **Fora do escopo:**
  - gerar as ações sozinhas por protocolo (a consulta trimestral criada automaticamente), que fica para as linhas de
    cuidado (P5);
  - ação de campanha (P3) e metas (P6);
  - a confirmação automática pela conclusão do atendimento (o profissional confirma).

## Testes

- **JUnit:**
  - `AcaoProgramaticaControllerTest`:
    - individual sem acompanhamento (400), responsável sem lotação (422), vencida na lista; realização no futuro (400),
      resultado fora da busca (400), realizada e repetida (422); não realizada sem motivo (400) e com situação errada
      (400); as ações do paciente;
    - busca ativa com a data de hoje, repetida (409), sem resultado (400) e "mudou de endereço" com a sugestão;
      coletiva com busca ativa (400), sem unidade (400), participante fora do programa (422), realizada com os presentes;
    - marcação no programa sem acompanhamento (422), ligada à ação; atendimento da marcação concluído deixa a ação
      pronta para confirmar com o atendimento sugerido; atendimento de outro paciente (422) e confirmação com o sugerido.
  - `AcompanhamentoAutorizacaoControllerTest`: gestor e outra unidade não preveem ação nem busca ativa (403); a ação de
    programa sensível só aparece para quem acompanha na unidade; o gestor não lê as ações (403).
  - `MigracoesDoEsquemaTest`: os CHECKs das enumerações novas batem com o Java.
- **Robot:**
  - **de API:** `AcaoProgramatica.robot` (CT-001 a CT-011): individual, consulta sem acompanhamento, coletiva,
    participante fora do programa, realização, não realizada, busca ativa e repetida, busca sem resultado e "mudou de
    endereço", lista, ações do paciente e ação inexistente.
  - **de interface:** `UI_programas.robot` CT-011 a CT-013: busca ativa do faltoso confirmada com o resultado, prever a
    aferição no detalhe do programa e a ação não realizada pela aba Ações programáticas.
