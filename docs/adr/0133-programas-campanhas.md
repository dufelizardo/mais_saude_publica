# 0133 — Programas: campanhas (P3)

## Status

Aceita e implementada (fatia P3 da [ADR-0109](./0109-programas-acoes-e-linhas-de-cuidado.md); usa as ações
programáticas da [ADR-0132](./0132-programas-acoes-programaticas.md)).

## Contexto

- **A ADR-0109 separa campanha de programa:** a campanha tem início e fim, público, meta e locais; pode estar ligada a
  um programa (a vacinação ao de Imunização, o Outubro Rosa ao de Saúde da Mulher), mas não é um programa.
- **O usuário pediu que campanhas fossem cadastráveis** pelo município, sem mudança de código.
- **A landing page já tinha a seção "Campanhas em destaque",** mas com conteúdo fixo de exemplo.
- **A ação programática da P4 deixou a ligação com campanha para esta fatia.**

## Decisão

1. **Campanha** (`Campanha`, `tb_campanha`):
   - **nome**, **descrição** (aparece na consulta pública), **tipo** (vacinação, rastreamento ou prevenção,
     conscientização, mutirão, outra) e **programa** opcional;
   - **período:** início e fim obrigatórios, fim depois do início (400);
   - **situação pelas datas** (planejada, em andamento, encerrada), sem guardar; **cancelada** é marcada à mão, com o
     motivo (400 sem ele);
   - **público-alvo como dado,** como no programa: faixa etária, sexo, gestante e condições (CID-10 ou CIAP-2), com as
     mesmas validações (400), e um **público em palavras** opcional que substitui o texto montado;
   - **meta:** em texto ("90% de cobertura") e, opcional, a **quantidade de pessoas**;
   - **publicada:** só a publicada aparece na consulta pública. A cancelada sai dela e não se publica de novo (422);
     depois de cancelada, a campanha não muda (422).
2. **Local da campanha** (`LocalCampanha`, `tb_local_campanha`):
   - **unidade da rede** (a mesma uma vez só, 409) ou **local temporário** (escola, praça, unidade móvel) com **nome**
     (400), endereço e ponto no mapa opcionais, sob a **unidade responsável**;
   - **funcionamento** em texto e um período próprio opcional, dentro do da campanha (422);
   - retirar o local com ações registradas não pode (409).
3. **Ações da campanha:** a ação programática passa a ser **do programa ou da campanha, um dos dois** (400; CHECK no
   banco).
   - A da campanha é **coletiva**, num **local dela** (400 sem ele; 422 se for de outra), dentro do período (422) e com
     a campanha planejada ou em andamento (422). Consulta programada, visita e busca ativa não entram (400).
   - **Novo tipo de ação: mobilização.**
   - **Quantidade atendida:** a realização da ação coletiva registra quantas pessoas foram atendidas (não negativa,
     400; não vale para a individual, 400). A soma nas ações realizadas é o **andamento da meta**.
   - **Cancelar a campanha cancela as ações pendentes dela,** com o motivo.
4. **Permissões** (sem permissão nova):
   - **cadastrar, publicar e cancelar:** `PROGRAMA.GERENCIAR`;
   - **incluir e retirar local:** `PROGRAMA.GERENCIAR` na unidade do local;
   - **ações da campanha:** `PROGRAMA.ACOMPANHAR` na unidade do local;
   - **consultar campanhas:** `PROGRAMA.GERENCIAR` ou `ACOMPANHAR` (não é dado de paciente).
5. **Consulta pública** (`GET /publico/campanha`, sem login): as publicadas, em andamento e as próximas, com tipo,
   descrição, público, período, meta e andamento, e os locais (nome, endereço, funcionamento e ponto). Nenhum dado de
   paciente.
6. **API:**
   - `GET /campanhas` (filtros: situação e programa), `GET /campanha/{id}`;
   - `POST /campanha` (201), `PUT /campanha/{id}`, `POST /campanha/{id}/publicacao` e `POST /campanha/{id}/cancelamento`;
   - `POST /campanha/{id}/local` (201) e `DELETE /campanha-local/{id}`;
   - a ação programática ganha `campanhaId`, `localCampanhaId` e `quantidadeAtendida`, e `GET /acoes-programaticas`, o
     filtro `campanhaId`.
7. **Telas:**
   - **Programas de Saúde, aba "Campanhas":** lista com o período, o público, a meta (atendido de meta) e a situação;
     cadastro; gaveta da campanha com a barra da meta, publicar e retirar, cancelar com o motivo, os locais (incluir e
     retirar) e as ações da campanha (o componente da P4, com o local e as pessoas atendidas);
   - **Landing page:** a seção "Campanhas" com as campanhas reais (a primeira em destaque, "Ver todas" além das cinco
     primeiras), no lugar do conteúdo de exemplo. As notícias ficam para a Comunicação (C1).
8. **Migração V26:** `tb_campanha`, `tb_campanha_condicao`, `tb_local_campanha` e, em `tb_acao_programatica`, o
   programa opcional, a campanha, o local, a quantidade e o tipo mobilização.

## Consequências

- **O município cadastra e publica as campanhas,** e o cidadão vê na landing onde e para quem são.
- **A meta tem andamento** pelas ações registradas, sem esperar a Imunização.
- **A Comunicação (C2) e a Imunização (#24) se ligam à campanha:** a divulgação será uma `CampanhaComunicacao` dela, e
  as doses aplicadas virão do registro de vacinação.
- **Fora do escopo:**
  - a divulgação (C2);
  - a dose aplicada e a cobertura vacinal real (#24);
  - as metas apuradas por indicador (P6).

## Testes

- **JUnit:**
  - `CampanhaControllerTest`:
    - fim antes do início, sexo ignorado e condição inválida (400); a situação pelas datas e o público montado ou em
      palavras; a lista por situação;
    - só a publicada na consulta pública, com os locais; a unidade da rede repetida (409), o temporário sem nome (400) e
      fora do período (422);
    - ação da campanha: consulta programada (400), sem local (400), fora do período (422), sem programa nem campanha
      (400); a realização com quantidade negativa (400) e o andamento da meta, também na consulta pública; o local com
      ação não sai (409);
    - cancelamento sem motivo (400), com ele: fora da consulta pública, ações pendentes canceladas, publicar (422) e
      nova ação (422).
  - `ProgramaAutorizacaoControllerTest`: consulta pública sem login; sem login, 401; quem acompanha consulta mas não
    cadastra nem publica (403); quem gerencia cadastra e inclui o local.
  - `AcaoProgramaticaControllerTest`: as ações de programa seguem como na P4.
  - `MigracoesDoEsquemaTest`: os CHECKs das enumerações novas batem com o Java.
- **Robot:**
  - **de API:** `Campanha.robot` (CT-001 a CT-011): cadastro, fim antes do início, publicação, local repetido, local
    temporário sem nome, ação e andamento, ação fora do período, local com ação, cancelamento, lista e campanha
    inexistente.
  - **de interface:** `UI_programas.robot` CT-014 a CT-016: cadastrar a campanha com o local e publicar; a campanha na
    landing page; a ação da campanha confirmada com as pessoas atendidas e a meta.
