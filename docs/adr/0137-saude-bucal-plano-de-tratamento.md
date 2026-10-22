# 0137 — Saúde Bucal: plano de tratamento (SB3b)

## Status

Aceita e implementada (segunda parte da fatia SB3 da [ADR-0111](./0111-saude-bucal.md); usa o catálogo e o
procedimento odontológico da [ADR-0136](./0136-saude-bucal-procedimento-odontologico.md)). Fecha a SB3.

## Contexto

- **O tratamento odontológico acontece em várias consultas.** Não basta "atendimento → procedimentos": o dentista
  planeja o que vai fazer, e os procedimentos executam o plano ao longo do tempo (MODELO-SAUDE-BUCAL, seção 8).
- **O "tratamento concluído" é indicador da Atenção Primária** e precisa de um marco: o plano concluído, com a data.
- **A SB3a deixou o procedimento no atendimento,** com o item do catálogo e o complemento (dente e faces). O item do
  plano usa as mesmas peças.

## Decisão

1. **Plano de tratamento** (`PlanoTratamento`, `tb_plano_tratamento`):
   - **campos:**
     - paciente, unidade e atendimento de origem (de especialidade odontológica; fora dela, 422);
     - a avaliação odontológica de origem, opcional e do mesmo paciente (422);
     - o dentista responsável, diagnóstico e objetivo;
     - a **prioridade**, na escala da necessidade de tratamento da avaliação (SB1);
     - a situação: **aberto, concluído ou abandonado**;
   - **com a avaliação de origem,** o diagnóstico e a prioridade não informados valem os dela. Sem avaliação, a
     prioridade é obrigatória (400);
   - **um plano aberto por paciente** (409; índice único parcial no banco). Os encerrados ficam no histórico, e um
     novo pode ser aberto depois;
   - **concluir é o tratamento concluído,** com a data. Só conclui se:
     - nenhum item estiver planejado ou em andamento (422);
     - ao menos um item estiver concluído (422). Sem procedimento realizado, o caminho é abandonar;
     - itens encaminhados e cancelados não impedem: o encaminhado segue pela Regulação;
   - **abandonar** pede o motivo (400);
   - **o plano é vivo:** enquanto aberto, ganha itens. Cada mudança de situação guarda quem fez, quando e o motivo, e
     passa pela trilha de auditoria. O plano encerrado não muda mais (422).
2. **Item do plano** (`ItemPlanoTratamento`, `tb_item_plano_tratamento` e as faces em `tb_item_plano_tratamento_face`):
   - a ordem (a sessão sugerida), o **procedimento previsto** do catálogo (ativo e odontológico, 422), o dente e as
     faces com as regras do complemento da SB3a (400 e 422), e a observação;
   - a situação: **planejado, em andamento, concluído, encaminhado ou cancelado**.
3. **O procedimento executa o item.**
   - O registro do procedimento ganha o `itemPlanoId`.
   - **O procedimento precisa corresponder ao item** (422): o mesmo item do catálogo, o mesmo dente e as mesmas faces,
     do mesmo paciente, num plano aberto, com o item planejado ou em andamento.
   - **Realizado:** o item fica concluído e aponta para o procedimento. **Agendado:** fica em andamento. O procedimento
     cancelado não executa item (422).
   - **No desfecho do agendado,** o realizado conclui o item, e o cancelado o devolve a planejado.
   - **Na retificação,** o item passa a apontar para a nova versão, que precisa continuar correspondendo a ele (422), e
     a situação segue a nova versão.
   - **O item só fica concluído por um procedimento realizado.** Não existe "concluir" à mão (400).
4. **O dentista muda a situação do item:**
   - **em andamento**, do planejado, para o tratamento de várias sessões (endodontia, por exemplo);
   - **encaminhado**, com a solicitação de regulação do mesmo paciente, opcional (422 se for de outro). A SB4 traz as
     especialidades odontológicas na Regulação;
   - **cancelado**, com motivo (400);
   - **concluído e cancelado não mudam mais** (422).
5. **Acesso:**
   - **permissão nova `ODONTOLOGIA.PLANEJAR`**, no papel Cirurgião-dentista: abrir, incluir item, mudar situação,
     concluir e abandonar;
   - **executar é registrar o procedimento** (`PROCEDIMENTO.REGISTRAR`). O técnico em saúde bucal executa os itens que
     a SB3a já permite a ele (flúor, selante);
   - **ler** segue as regras do prontuário: vínculo e leitura auditada.
6. **API:**
   - `POST /plano-tratamento` (201, com itens) e `POST /plano-tratamento/{id}/item` (201);
   - `POST /item-plano-tratamento/{id}/situacao`;
   - `POST /plano-tratamento/{id}/conclusao` e `POST /plano-tratamento/{id}/abandono`;
   - `GET /paciente/{id}/planos-tratamento`;
   - `POST /procedimento/` aceita o `itemPlanoId`.
7. **Tela:** no atendimento de odontologia, a seção **"Plano de tratamento"**, entre os procedimentos e o odontograma:
   - **sem plano aberto:** a matrícula do dentista, a prioridade (por padrão, a da avaliação) e o objetivo, e "Abrir
     plano de tratamento";
   - **com o plano aberto:**
     - os itens em ordem, com o dente, as faces e a situação;
     - **"Executar"**, que abre a gaveta do procedimento já com o item do catálogo, o dente e as faces, e avisa qual
       item será executado;
     - "Em andamento", "Encaminhar" e "Cancelar", com o motivo pedido na hora;
     - a inclusão de item (catálogo, dente e faces), "Concluir tratamento" e "Abandonar plano";
   - **os planos anteriores** ficam recolhidos, com a situação e as datas;
   - depois de salvar o procedimento, o plano se recarrega e mostra o item concluído.
8. **Migração V30:** `tb_plano_tratamento` (com o índice único do plano aberto), `tb_item_plano_tratamento` e as faces.

## Consequências

- **O dentista planeja e acompanha o tratamento** ao longo das consultas, e o técnico executa o que pode.
- **O "tratamento concluído" tem marco e data,** pronto para o indicador da APS (Programas, P6, e BI).
- **A SB4 tem onde se apoiar:** o item encaminhado já guarda a solicitação de regulação.
- **Fora do escopo:**
  - a contagem do indicador (fica para P6 e BI);
  - a evolução clínica livre por sessão: o procedimento e a avaliação já registram o que foi feito;
  - o tratamento protético (SB4).

## Testes

- **JUnit:**
  - **`PlanoTratamentoControllerTest`:**
    - **abrir:**
      - recusas: fora da odontologia (422), sem prioridade nem avaliação (400), resina sem face (400) e curativo, que
        não é odontológico (422);
      - o plano com dois itens planejados, em ordem;
      - o segundo plano aberto (409);
      - o item incluído depois ganha a próxima ordem; a leitura traz os planos do paciente;
    - **executar e concluir:**
      - recusas: procedimento em outro dente ou outro procedimento (422);
      - transições: o realizado conclui o item; o agendado põe em andamento; o desfecho cancelado devolve a planejado,
        e o realizado conclui;
      - recusas depois de concluído: executar o item concluído (422) e retificar o procedimento para outro dente
        (422);
      - concluir com item pendente (422);
      - a situação do item: concluído à mão (400), cancelado sem motivo (400), em andamento e encaminhado;
      - a conclusão com a data; o item do plano encerrado não muda (422); um novo plano abre depois;
    - **sem item concluído e abandono:**
      - com o único item cancelado, a conclusão não pode (422);
      - o abandono sem motivo (400) e com ele;
      - o plano abandonado não ganha item (422).
  - **`SaudeBucalAutorizacaoControllerTest`:**
    - o técnico não abre o plano nem muda a situação do item (403);
    - o dentista abre o plano;
    - o técnico executa o item de flúor pelo procedimento, e o item fica concluído.
  - **`MigracoesDoEsquemaTest`:** os CHECKs das enumerações novas batem com o Java.
- **Robot:**
  - **de API:** `PlanoTratamento.robot` (CT-001 a CT-009):
    - abrir; segundo plano aberto; fora do atendimento odontológico;
    - executar pelo procedimento; procedimento diferente do item;
    - concluir com pendente; concluir; abandono sem motivo; cancelar item.
  - **de interface:** `UI_saude_bucal.robot` CT-006:
    - abrir o plano e incluir a restauração do 36 e o flúor;
    - executar a restauração pela gaveta, cancelar o flúor e concluir o tratamento.
