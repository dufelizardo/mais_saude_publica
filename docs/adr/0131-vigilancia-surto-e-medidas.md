# 0131 — Vigilância: surto e medidas de controle (V2b)

## Status

Aceita e implementada (segunda parte da fatia V2 da [ADR-0110](./0110-vigilancia-em-saude.md); a primeira, contatos,
local e mapa, está na [ADR-0130](./0130-vigilancia-contatos-local-e-mapa.md)).

## Contexto

- **A V2a deu aos casos local, microárea, mapa e curva.** Mas não havia como dizer que vários casos são **um evento**:
  o surto de sarampo numa escola ou o de dengue nas microáreas 04 a 06.
- **O MODELO-VIGILANCIA (§3.4) diz que surto não é "vários casos":** é uma entidade própria, com agravo, território,
  período, situação, casos relacionados, investigação e medidas de controle.
- **Medida de controle não é só de surto.** O bloqueio vacinal de um único caso de sarampo, ou o isolamento de um caso
  de meningite, é rotina e não abre surto. A V1 só tinha um texto livre "medidas" na investigação.
- **A ADR-0110 deixou fora a detecção automática de surto por regra estatística.** Quem abre o surto é quem
  investiga, e o mapa e a curva ajudam a ver.

## Decisão

1. **Surto** (`Surto`, `tb_surto`):
   - **agravo** e **unidade responsável** (o escopo de acesso), que não mudam depois de aberto;
   - **nome**, **tipo** (comunitário ou institucional) e, no institucional, o **estabelecimento**: escola, creche,
     serviço de saúde, ILPI, unidade prisional, alojamento ou abrigo, empresa, outro; com o nome (obrigatório, 400) e o
     endereço;
   - **microáreas afetadas** (`tb_surto_microarea`), **definição de caso** em texto (o critério do surto, que não é o
     da ficha), **início** e **fim** (nunca no futuro);
   - **situação:** suspeito → em investigação → confirmado ou descartado → encerrado. Entre as quatro primeiras, o
     surto vai e volta (reabrir a investigação). **Encerrar** pede o surto confirmado ou descartado (422), a
     **conclusão** (400) e **nenhuma medida em aberto**, planejada ou em andamento (422); o fim, se vazio, vira a data
     do encerramento. Encerrado, não muda (422).
2. **Casos do surto:** a notificação ganha o surto (`surto_id`) e quando entrou nele.
   - Só casos **do mesmo agravo** (422), visíveis a quem gere.
   - **Um surto por caso** (409). Retirar o caso não apaga a notificação.
   - Os casos vêm na abertura, escolhidos da lista, ou da microárea no mapa ("Abrir surto" com os casos dela, só para
     quem investiga).
   - O surto conta casos, confirmados (pela classificação atual) e óbitos pelo agravo, e tem a **curva própria** por
     semana epidemiológica, do início do surto ao fim (ou hoje).
3. **Medida de controle** (`MedidaControle`, `tb_medida_controle`), **do surto ou do caso**, um dos dois (CHECK no
   banco):
   - **tipo:** bloqueio vacinal, isolamento, quimioprofilaxia, controle vetorial, busca ativa, educação em saúde,
     interdição ou recolhimento, outra;
   - **descrição**, **responsável** (pela matrícula; sem ela, quem registra), **prazo**;
   - **situação:** planejada, em andamento, concluída (pede o **resultado**, 400) ou cancelada. Concluída ou cancelada,
     não muda (422);
   - **vencida:** aberta com o prazo passado. Conta nos indicadores, como os contatos vencidos da V2a.
4. **Linha do tempo:**
   - **do surto:** vem da **trilha de auditoria** (ADR-0070). Cada ação no surto grava o texto do que mudou ("Surto
     aberto: …", "Situação: Suspeito → Em investigação.", "Medida: bloqueio vacinal (planejada)…", "Casos incluídos: 2.",
     "Caso retirado: …"), e a linha do tempo lê os eventos com o registro do surto. Não há tabela de eventos própria: a
     auditoria já guarda quem, quando e o quê, por 20 anos (ADR-0082);
   - **do caso:** montada dos registros: notificação, início da investigação, classificações, entrada no surto,
     medidas (criada e concluída ou cancelada) e encerramento.
5. **Permissões** (sem permissão nova):
   - **abrir e gerir surto e medidas:** `VIGILANCIA.INVESTIGAR` na unidade responsável (do surto) ou na da notificação
     (medida do caso);
   - **consultar:** `VIGILANCIA.NOTIFICAR` ou `INVESTIGAR` no escopo da unidade;
   - **agravo sensível:** o surto só aparece e só abre para quem investiga na unidade responsável (403 no detalhe).
6. **Mapa:** o filtro `surtoId` mostra só os casos do surto, no período dele, e as **microáreas declaradas em
   destaque** (`doSurto`), mesmo sem caso. Cada microárea traz as notificações dela (`notificacoesIds`), só para quem
   investiga.
7. **API:**
   - `GET /vigilancia/surtos` (com os indicadores: abertos, confirmados, medidas vencidas) e `GET /vigilancia/surto/{id}`;
   - `POST /vigilancia/surto` (201), `PUT /vigilancia/surto/{id}` e `POST /vigilancia/surto/{id}/situacao`;
   - `POST /vigilancia/surto/{id}/casos` e `DELETE /vigilancia/surto/{id}/casos/{notificacaoId}`;
   - `POST /vigilancia/surto/{id}/medida` e `POST /vigilancia/notificacao/{id}/medida` (201), `PATCH /vigilancia/medida/{id}`;
   - a notificação ganha o surto (`surtoId`, `surtoNome`), as medidas e a linha do tempo; `GET /vigilancia/mapa`, o
     `surtoId`.
8. **Tela Vigilância epidemiológica:**
   - **aba Surtos:** lista com a situação, os casos e a medida vencida; "Abrir surto" (agravo, unidade, nome, tipo,
     estabelecimento, definição de caso, início e os casos do agravo fora de surto); a gaveta do surto com o ciclo
     (investigar, confirmar, descartar, reabrir, encerrar com a conclusão), os casos (abrir, retirar, incluir), as
     medidas, a curva e a linha do tempo; "Ver no mapa";
   - **aba Mapa:** filtro por surto, a microárea do surto em destaque e "Abrir surto" pela microárea;
   - **na gaveta da notificação:** o surto do caso, as medidas de controle e a linha do tempo;
   - **nos indicadores:** surtos abertos e medidas com prazo vencido;
   - a curva por semana epidemiológica virou componente próprio, usado no mapa e no surto.
9. **Migração V24:** `tb_surto`, `tb_surto_microarea`, `tb_medida_controle` e as colunas do surto em `tb_notificacao`.

## Consequências

- **A V2 está completa:** contato, local, mapa, surto, medida e linha do tempo.
- **O surto liga Vigilância e Território:** as microáreas do surto e o mapa dele. E deixa pronto o gancho da
  Emergências (E1), que vai ligar um surto a um evento emergencial.
- **A linha do tempo do surto depende da auditoria.** Uma ação que falhe ao gravar o evento (a gravação não derruba a
  ação, ADR-0070) não aparece na linha do tempo, mas o estado do surto continua certo.
- **Fora do escopo:**
  - detecção automática de surto (ADR-0110);
  - ligação com o evento emergencial (E1, ADR-0113);
  - envio ao SINAN (V5);
  - o texto livre "medidas" da investigação da V1 continua, como anotação; as medidas estruturadas são as desta ADR.

## Testes

- **JUnit:**
  - `VigilanciaSurtoControllerTest`:
    - surto institucional sem estabelecimento (400), abertura com dois casos, caso em outro surto (409), de outro agravo
      (422), inclusão e retirada;
    - medida vencida nos indicadores; encerrar suspeito (422), sem conclusão (400), com medida aberta (422); concluir a
      medida sem resultado (400), com ele, e a medida concluída que não muda (422); encerramento com o fim no dia;
      surto encerrado que não muda (422);
    - detalhe com casos, curva e a linha do tempo da auditoria; a notificação com o surto e a linha do tempo;
    - medida do caso sem descrição (400) e com ela; o mapa do surto com a microárea declarada em destaque.
  - `VigilanciaAutorizacaoControllerTest`: quem só notifica não abre surto (403), mas consulta; o gestor sem permissão
    recebe 403; o surto de agravo sensível some da lista e dá 403 no detalhe para quem só notifica.
  - `MigracoesDoEsquemaTest`: os CHECKs das enumerações novas batem com o Java.
- **Robot:**
  - **de API:** `VigilanciaSurto.robot` (CT-001 a CT-011): abertura, institucional sem estabelecimento, caso em outro
    surto, agravo diferente, retirar caso, ciclo e encerramento com a linha do tempo, concluir sem resultado, medida do
    caso, lista, mapa do surto e surto inexistente.
  - **de interface:** `UI_vigilancia.robot` CT-007 a CT-009: abrir o surto escolhendo os casos, o ciclo do surto até
    o encerramento e a medida do caso.
