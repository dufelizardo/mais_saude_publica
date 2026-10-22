# 0134 — Saúde Bucal: especialidade no atendimento e avaliação odontológica (SB1)

## Status

Aceita e implementada (fatia SB1 da [ADR-0111](./0111-saude-bucal.md); detalhe em
[`saude-bucal/MODELO-SAUDE-BUCAL.md`](../saude-bucal/MODELO-SAUDE-BUCAL.md)).

## Contexto

- **O `Atendimento` só tinha o tipo** (consulta, urgência, internação). Não dava para dizer que era um atendimento de
  odontologia, e os registros da Saúde Bucal precisam se ligar a ele (ADR-0111, decisão 3).
- **A Agenda tinha o filtro "Especialidade" desabilitado** desde a ADR-0092, à espera do catálogo.
- **A eSB já existe** (ADR-0103), com o cirurgião-dentista, o técnico e o auxiliar em saúde bucal como funções da
  equipe, mas não havia papel de acesso para eles nem permissão odontológica.
- **Registro clínico não se edita** (ADR-0062): a avaliação segue a retificação, como consulta, triagem e evolução.

## Decisão

1. **Catálogo de especialidades** (`Especialidade`, `tb_especialidade`), cadastrável, para todas as especialidades:
   - **código** (único, 409; não muda, 422), **nome**, **ativa** e a marca **odontológica**, que habilita os registros
     da Saúde Bucal no atendimento (uma "Odontopediatria" cadastrada pelo município também habilita);
   - **carga inicial editável:** clínica geral, medicina de família e comunidade, enfermagem, odontologia, pediatria,
     ginecologia e obstetrícia, psicologia e serviço social;
   - **manutenção:** `ORGANIZACAO.GERENCIAR`, a mesma de unidades e setores, porque é estrutura da rede (e não acesso);
     **leitura:** qualquer usuário logado;
   - a especialidade sai de uso **desativada**, sem apagar: os atendimentos antigos continuam apontando para ela, e a
     inativa não entra em atendimento, bloco ou marcação novos (422).
2. **A especialidade no atendimento e na Agenda:**
   - o **atendimento** ganha a especialidade (opcional, para não quebrar o que existe); sem ela, a **da marcação** de
     onde ele nasce;
   - o **bloco da agenda** pode ser de uma especialidade (a odontologia de terça de manhã);
   - a **marcação** na vaga **herda a especialidade do bloco** (o encaixe não, por não ter vaga), ou recebe a informada;
   - cada **item da agenda** (vaga e marcação) traz a especialidade, e a tela da Agenda ganha o **filtro por
     especialidade**; bloqueios e afastamentos continuam visíveis.
3. **Avaliação odontológica** (`AvaliacaoOdontologica`, `tb_avaliacao_odontologica`), ligada a um **atendimento de
   especialidade odontológica** (422 em outro):
   - queixa principal, anamnese, exame clínico e achados, diagnóstico e condições bucais;
   - **risco em saúde bucal** (baixo, médio, alto) e **necessidade de tratamento** (sem necessidade, eletiva,
     prioritária, urgência), obrigatórios (400);
   - plano inicial e observações em texto (o plano estruturado é da SB3);
   - o **cirurgião-dentista** pela matrícula; a data não fica no futuro (400);
   - **retificação** (ADR-0062): nova versão ligada à anterior, no mesmo atendimento (400), com motivo (400), só da
     vigente (422), e de outro profissional só com supervisão (403);
   - entra no **prontuário**, no atendimento, com as versões retificadas marcadas, e a leitura é auditada.
4. **Acesso** (Lei 5.081/1966 e Lei 11.889/2008):
   - **permissão nova `ODONTOLOGIA.REGISTRAR`:** registrar e retificar a avaliação;
   - **papel novo "Cirurgião-dentista":** avaliação e diagnóstico, atendimento, agenda, procedimentos, encaminhamento à
     Regulação, programas e notificação;
   - **papel novo "Técnico em saúde bucal":** procedimentos permitidos, sob supervisão; **sem** avaliação nem
     diagnóstico (403);
   - `ODONTOLOGIA.PLANEJAR` fica para o plano de tratamento (SB3).
5. **API:**
   - `GET /especialidades`, `POST /especialidade` (201) e `PUT /especialidade/{id}`;
   - `POST /avaliacao-odontologica` (201), `POST /avaliacao-odontologica/{id}/retificacao` (201) e
     `GET /avaliacao-odontologica/{id}`;
   - o atendimento, o bloco e a marcação ganham `especialidadeId` (e o nome na saída); o item da agenda, a especialidade;
     o prontuário, as avaliações odontológicas de cada atendimento.
6. **Telas:**
   - **Atendimentos:** a especialidade no formulário do atendimento; no atendimento de odontologia, a seção "Avaliação
     odontológica" com o registro e a retificação;
   - **Agenda:** o filtro por especialidade e a especialidade do bloco;
   - **Setores:** o catálogo de especialidades (incluir, desativar e reativar).
7. **Migração V27:** `tb_especialidade` com a carga inicial, `tb_avaliacao_odontologica` e a coluna
   `especialidade_id` em `tb_atendimento`, `tb_bloco_agenda` e `tb_agendamento`.

## Consequências

- **O atendimento sabe a especialidade,** o que serve a todas elas (o filtro da Agenda, os indicadores por
  especialidade e, na Regulação, as especialidades odontológicas da SB4).
- **A Saúde Bucal ganha o primeiro registro clínico próprio,** sem cópia paralela de paciente, prontuário, atendimento
  ou agenda (ADR-0111, decisão 1).
- **O odontograma (SB2) e o plano de tratamento (SB3) se ligam ao mesmo atendimento de odontologia.**
- **Fora do escopo:**
  - o odontograma (SB2);
  - o catálogo de procedimentos e o plano de tratamento (SB3);
  - o encaminhamento ao CEO e a prótese (SB4);
  - as ações coletivas de saúde bucal (SB5).

## Testes

- **JUnit:**
  - `SaudeBucalControllerTest`:
    - o catálogo com a odontologia odontológica; código repetido (409), fora do padrão (400) e trocado (422); a
      especialidade desativada não entra no atendimento novo (422);
    - a avaliação em atendimento sem odontologia (422), sem risco (400) e registrada; a retificação sem motivo (400),
      de outro atendimento (400), registrada e repetida na versão antiga (422); a antiga marcada como retificada; as duas
      versões no prontuário;
    - o bloco de odontologia, as vagas com a especialidade, a marcação que herda e o atendimento que herda da marcação.
  - `SaudeBucalAutorizacaoControllerTest`: o técnico em saúde bucal e o enfermeiro não registram a avaliação (403), o
    cirurgião-dentista registra; o catálogo é lido por qualquer logado (sem login, 401) e mantido por quem cuida da
    organização (403 para o dentista).
  - `AgendaControllerTest`, `AgendamentoControllerTest` e `AtendimentoControllerTest` seguem como antes.
  - `MigracoesDoEsquemaTest`: os CHECKs das enumerações novas batem com o Java.
- **Robot:**
  - **de API:** `SaudeBucal.robot` (CT-001 a CT-009): catálogo, código repetido, avaliação, atendimento sem
    odontologia, sem risco, retificação e repetida, retificação sem motivo, avaliação no prontuário e inexistente.
  - **de interface:** `UI_saude_bucal.robot` CT-001 e CT-002: a avaliação odontológica pela tela (risco obrigatório,
    registro e retificação) e o catálogo de especialidades na tela de Setores.
