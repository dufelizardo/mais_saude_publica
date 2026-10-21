# 0130 — Vigilância: contatos, local do caso e mapa (V2a)

## Status

Aceita e implementada (primeira parte da fatia V2 da [ADR-0110](./0110-vigilancia-em-saude.md); a V2b, surto e
medidas de controle, está na [ADR-0131](./0131-vigilancia-surto-e-medidas.md)).

## Contexto

- **A V1 ([ADR-0124](./0124-vigilancia-epidemiologica-fundacao.md)) notifica, investiga, classifica e encerra o
  caso.** Mas o caso fica isolado: não se sabe quem foi exposto a ele nem onde ele aconteceu. O "local provável" é só
  um texto.
- **O Território está completo** (ADRs 0125 a 0129): áreas e microáreas desenhadas, imóvel com coordenada, família no
  domicílio e o mapa compartilhado (F2). Com isso a vigilância já pode situar o caso sem pedir nada de novo.
- **O MODELO-VIGILANCIA (§3.3) prevê o contato:** pessoa exposta a um caso, que **pode ainda não ter cadastro** e vira
  paciente quando é atendida.
- **A V2 inteira é grande demais para uma fatia.** Contatos e mapa não dependem do surto; o surto agrupa casos que já
  precisam estar no mapa. Por isso a V2 se divide: **V2a (contatos, local e mapa)** e **V2b (surto e medidas)**.

## Decisão

1. **Contato epidemiológico** (`ContatoEpidemiologico`, `tb_contato_epidemiologico`), ligado à notificação do caso
   índice:
   - **paciente cadastrado ou só os dados da pessoa** (nome, telefone, endereço); sem paciente, o nome é obrigatório;
   - **tipo de exposição:** domiciliar, sexual, trabalho, escola ou creche, serviço de saúde, comunitário, outro;
   - **data da exposição** (não pode ser futura) e **fim do monitoramento**, calculado: data da exposição (ou da
     notificação, sem ela) mais o **período de monitoramento do agravo**;
   - **situação:** em acompanhamento, sintomático, liberado, perda de seguimento e **virou caso**;
   - o caso não pode ser contato de si mesmo (422).
2. **Período de monitoramento de contatos no agravo** (`periodoMonitoramentoContatoDias`, 1 a 365, opcional). Vem
   preenchido para os agravos em que o rastreio de contatos é rotina: **sarampo 21 dias, coqueluche 14, meningite 10**.
   Sem período, o contato não tem prazo.
3. **O contato que adoece vira caso só por notificação.**
   - A notificação recebe `contatoId`. O contato precisa existir (404), não pode já ter gerado notificação (409) e, se
     tem paciente, tem de ser o mesmo da notificação (422).
   - Ao notificar, o contato ganha o paciente, a notificação gerada e a situação **virou caso**. Assim a cadeia de
     transmissão fica registrada: caso índice → contato → caso secundário.
   - **"Virou caso" não se marca à mão** (422) e não se desfaz (422).
4. **Local do caso** (colunas novas em `tb_notificacao`: latitude, longitude, microárea e origem do local):
   - **pelo domicílio:** sem ponto informado, a notificação toma o domicílio atual da família do paciente no cadastro
     territorial (ADR-0127): a coordenada do imóvel e a microárea dele. Origem `DOMICILIO`;
   - **marcado:** quem investiga marca o local provável de infecção no mapa (`POST notificacao/{id}/local`), ou o ponto
     vem na própria notificação. A microárea é a que contém o ponto, com preferência pelos territórios de finalidade
     vigilância. Origem `MARCADO`;
   - **o ponto marcado prevalece sobre o domicílio**, e o domicílio não sobrescreve um ponto já marcado;
   - o texto "local provável" da V1 continua, como referência descritiva.
5. **Mapa dos casos** (`GET /vigilancia/mapa`, filtros agravo, unidade, período e classificação; padrão: as últimas 12
   semanas, pela data dos primeiros sintomas ou, sem ela, pela da notificação):
   - **casos por microárea**, com a geometria, para o mapa coroplético;
   - **pontos dos casos**, só para quem investiga (`VIGILANCIA.INVESTIGAR`), e **nunca dos agravos sensíveis**: o ponto
     de um caso de violência identifica a casa da vítima;
   - **curva por semana epidemiológica** no período;
   - **quantos ficaram sem local**, para não esconder o que o mapa não mostra.
6. **Permissões** (as da V1, sem permissão nova):
   - **contatos e local:** `VIGILANCIA.INVESTIGAR` na unidade da notificação e acesso ao conteúdo dela (agravo sensível
     só para a vigilância e quem notificou);
   - **mapa:** `VIGILANCIA.NOTIFICAR` ou `INVESTIGAR`. Quem só notifica vê as contagens e a curva, sem pontos; os casos
     de agravo sensível só entram para quem investiga e pode ver o conteúdo.
7. **API:**
   - `POST /vigilancia/notificacao/{id}/contato` (201) e `PATCH /vigilancia/contato/{id}`;
   - `POST /vigilancia/notificacao/{id}/local`;
   - `GET /vigilancia/mapa`;
   - a notificação ganha `latitude`, `longitude` e `contatoId` na entrada e, na saída, o local (`origemLocal`,
     `microareaCodigo`, `areaCodigo`) e os contatos; a lista ganha `contatosVencidos`.
8. **Tela Vigilância epidemiológica:**
   - **aba Mapa:** filtros, microáreas coloridas por faixa de casos (com legenda), pinos dos casos (o clique abre a
     notificação), lista de casos por microárea, curva em barras e a contagem sem local;
   - **na gaveta da notificação:** "Local do caso" (de onde veio, a microárea, o minimapa e "Marcar local no mapa") e
     "Contatos" (lista com a situação e o fim do monitoramento, registro, mudança de situação e "Notificar", que abre a
     notificação do contato já preenchida e ligada ao caso índice);
   - **no agravo:** o campo "Monitorar contatos por (dias)";
   - **nos indicadores:** os contatos com monitoramento vencido.
9. **Migração V23:** colunas do local em `tb_notificacao`, o período em `tb_agravo` (com os três agravos preenchidos)
   e a tabela `tb_contato_epidemiologico`.

## Consequências

- **O caso deixa de ser isolado:** quem foi exposto, até quando acompanhar e qual contato virou caso.
- **O mapa reaproveita o Território** sem cadastro novo: a maioria dos casos ganha local pelo domicílio, e o resto se
  marca.
- **A V2b tem onde se apoiar:** o surto agrupa casos que já têm local, microárea e curva.
- **Fora do escopo:**
  - surto e medidas de controle (V2b, ADR-0131);
  - escopo territorial na vigilância (o ACS ver só os casos da sua microárea), que fica com o interruptor da ADR-0129;
  - alertas automáticos de concentração de casos;
  - a linha do tempo do caso, que entra com o surto.

## Testes

- **JUnit:**
  - `VigilanciaContatosControllerTest`:
    - contatos: sem nome (400), o próprio caso (422), o fim do monitoramento do sarampo e o vencido, `contatosVencidos`;
    - "virou caso" à mão (422), a notificação do contato (201), a segunda (409), a ligação ao caso índice e o "virou
      caso" que não se desfaz (422);
    - local pelo domicílio e marcado no mapa, com a microárea; ponto incompleto (400);
    - o mapa: contagem por microárea, pontos e curva; o caso de violência conta, mas sem ponto.
  - `VigilanciaAutorizacaoControllerTest`: quem só notifica vê o mapa sem pontos; a vigilância com pontos; o gestor
    sem permissão recebe 403; contato só para quem investiga.
  - `MigracoesDoEsquemaTest`: os CHECKs das enumerações novas batem com o Java.
- **Robot:**
  - **de API:** `VigilanciaContatos.robot` (CT-001 a CT-009): contato, sem nome, o próprio caso, virar caso e repetir,
    contato inexistente, local pelo domicílio e marcado, ponto incompleto e o mapa.
  - **de interface:** `UI_vigilancia.robot` CT-004 a CT-006: registrar contato e mudar a situação, notificar o contato
    e marcar o local com o mapa da vigilância.
