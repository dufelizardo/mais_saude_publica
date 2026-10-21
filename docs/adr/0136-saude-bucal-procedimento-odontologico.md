# 0136 — Saúde Bucal: catálogo de procedimentos e procedimento odontológico (SB3a)

## Status

Aceita e implementada (primeira parte da fatia SB3 da [ADR-0111](./0111-saude-bucal.md); usa o odontograma da
[ADR-0135](./0135-saude-bucal-odontograma.md)). O plano de tratamento fica para a SB3b.

## Contexto

- **O `Procedimento` só existia dentro de uma consulta** (ADR-0044), com o tipo em texto livre.
- **O técnico em saúde bucal não registra consulta** (não tem `CONSULTA.REGISTRAR`). Com o procedimento preso à
  consulta, ele nunca registraria a aplicação de flúor ou o selante que a Lei 11.889/2008 permite.
- **O MODELO-SAUDE-BUCAL (seção 7) pede um catálogo geral de procedimentos,** e não um catálogo odontológico. O
  procedimento odontológico é o procedimento normal mais um complemento (dente e faces), e o complemento gera os
  registros do odontograma.
- **A SB2 deixou o procedimento como origem do registro do odontograma para esta fatia,** para responder "qual
  procedimento mudou a condição?".
- **A SB3 inteira (catálogo, complemento e plano de tratamento) não cabe num PR.** Fica dividida como a V2:
  - **SB3a**, esta ADR: catálogo, procedimento no atendimento, complemento e odontograma;
  - **SB3b**: plano de tratamento.

## Decisão

1. **Catálogo geral de procedimentos** (`ProcedimentoCatalogo`, `tb_procedimento_catalogo`). Serve a todas as
   especialidades.
   - **Campos:**
     - código (único, 409; não muda, 422) e nome;
     - especialidade, opcional; sem ela, o item é de **uso geral**;
     - **SIGTAP** opcional, com 10 dígitos (400), para a integração futura;
     - ativo. O item sai de uso desativado, sem apagar.
   - **Só no item odontológico** (de especialidade odontológica; fora dela, 422):
     - o **alcance**: nenhum, o dente inteiro ou o dente e as faces;
     - a **condição resultante**, que o procedimento realizado grava no odontograma. Ela precisa caber no alcance (422):
       a de face pede face, a do dente pede o dente, e o item sem alcance não grava;
     - **exige dentista**: o item marcado só é registrado por quem tem `ODONTOLOGIA.REGISTRAR`.
   - **Manutenção** com `ORGANIZACAO.GERENCIAR`, como os catálogos de especialidades e condições; leitura por qualquer
     logado, com filtro opcional pela especialidade (os itens dela mais os gerais).
   - **Carga inicial editável:**
     - **gerais:** curativo, glicemia capilar, aferição de pressão, nebulização, retirada de pontos e eletrocardiograma;
     - **odontológicos:** restaurações em resina, amálgama e ionômero (gravam Restaurado, por face, só dentista);
       selante (grava Selante, por face); exodontia (grava Ausente, dente inteiro, só dentista); pulpotomia e drenagem
       de abscesso (dente, só dentista); flúor, raspagem supragengival, profilaxia e orientação de higiene (sem dente);
     - **o SIGTAP fica vazio na carga.** Quem cadastra informa o código; não há chute.
   - **A unificação** com os catálogos da Regulação (`ProcedimentoRegulado`) e do Laboratório (`ExameLaboratorial`)
     continua pendente, como a ADR-0111 registrou.
2. **O procedimento fica no atendimento.**
   - `tb_procedimento` ganha o `atendimento_id`, obrigatório. A migração preenche os registros antigos com o
     atendimento da consulta, e a `consulta_id` passa a ser opcional.
   - **Registro:** com a consulta (o atendimento é o dela; outro atendimento informado dá 400) ou direto no atendimento.
     Sem nenhum dos dois, 400.
   - **As checagens de acesso** (unidade, leitura, filtro da listagem) passam a olhar o atendimento do procedimento.
   - **A retificação mantém** a mesma consulta, ou nenhuma, e o mesmo atendimento (400).
   - **O procedimento aponta para o item do catálogo,** opcional. Com o item, o tipo é o nome dele. Sem o item, o tipo
     em texto livre é obrigatório (400): ficam assim os registros antigos e o "outro".
3. **Complemento odontológico** (`ComplementoOdontologico`, `tb_complemento_odontologico` e as faces em
   `tb_complemento_odontologico_face`): o dente (FDI) e as faces do procedimento.
   - **Segue o alcance do item:**
     - **sem alcance:** não leva dente nem face (422). Sem item do catálogo, também não (422);
     - **dente inteiro:** pede o dente (400) e não aceita face (422);
     - **dente e faces:** pede o dente e pelo menos uma face (400).
   - **Mesmas regras do odontograma:** dente fora da FDI (400); oclusal só nos posteriores e incisal só nos anteriores
     (422).
   - **O item odontológico só entra em atendimento de especialidade odontológica** (422).
4. **O procedimento realizado grava o odontograma.**
   - **Quando:** ao ser registrado como REALIZADO, ou no desfecho que leva o agendado a REALIZADO, se o item tiver
     condição resultante.
   - **O que grava:** um registro por face, ou um do dente inteiro, na data da realização. A origem é o procedimento
     (`tb_registro_odontograma.procedimento_id`), e o histórico do dente mostra o nome dele.
   - **Agendado ou cancelado:** não grava.
   - **Retificação:** anula os registros da versão anterior, com o motivo "Procedimento retificado: …", e a nova versão
     grava os seus.
   - **O registro que veio de um procedimento não se anula pelo odontograma** (422): corrige-se retificando o
     procedimento.
5. **Acesso:** registrar continua com `PROCEDIMENTO.REGISTRAR`, que o técnico em saúde bucal e o dentista têm. O item
   que exige dentista pede também `ODONTOLOGIA.REGISTRAR` (403 para o técnico), no registro, na retificação e no
   desfecho. Sem permissão nova.
6. **Prontuário:** o atendimento ganha a lista `procedimentos`, com os feitos direto nele, as versões marcadas e o
   complemento. O procedimento da consulta continua dentro da consulta. Os dois trazem o atendimento, o item do
   catálogo, o SIGTAP, o dente e as faces.
7. **API:**
   - `GET /procedimentos-catalogo?especialidadeId=`, `POST /procedimento-catalogo` (201) e
     `PUT /procedimento-catalogo/{id}`;
   - `POST /procedimento/` aceita `atendimentoId`, `procedimentoCatalogoId`, `dente` e `faces`, com `consultaId` e
     `tipo` agora opcionais. O mesmo vale para a retificação.
8. **Tela:**
   - **no atendimento de odontologia,** a seção **"Procedimentos odontológicos"**, entre a avaliação e o odontograma,
     com "+ Procedimento odontológico";
   - **na gaveta do procedimento** (da consulta e do atendimento):
     - a escolha do catálogo (os itens da especialidade e os gerais; "(só dentista)" no nome) ou "Outro", em texto
       livre;
     - o dente e as faces, conforme o alcance do item;
     - o aviso do que o item grava no odontograma;
   - **depois de salvar,** o odontograma se recarrega e mostra a mudança;
   - **na lista,** o procedimento mostra o dente e as faces, e o SIGTAP quando houver;
   - **o catálogo de procedimentos** fica na tela Setores, ao lado do de especialidades.
9. **As telas grandes passam a ser carregadas sob demanda** (`loadComponent`): Atendimentos, Setores, Pacientes,
   Farmácia, Profissionais e Folha de pagamento.
   - Com a seção nova, o bundle inicial chegou a 1,01 MB e passou do limite de erro de 1 MB, quebrando o build.
   - Essas eram as últimas rotas carregadas de uma vez; as outras já usavam `loadComponent` (ADRs 0068, 0073, 0074 e
     0086).
   - **O bundle inicial caiu para 557 kB.** Continua acima do aviso de 500 kB, mas com folga até o erro; o que ainda
     pesa é o AppShell, o login e a página inicial.
10. **Migração V29:** o catálogo com a carga inicial; `atendimento_id` e `procedimento_catalogo_id` em
   `tb_procedimento`; o complemento e as faces; `procedimento_id` em `tb_registro_odontograma`.

## Consequências

- **O técnico em saúde bucal registra o que a lei permite** sem precisar de consulta, e o sistema barra o que é só do
  dentista.
- **O odontograma responde qual procedimento mudou a condição,** e a correção do procedimento corrige o odontograma.
- **O catálogo geral serve às outras especialidades** e prepara a produção (SIGTAP), sem ser um catálogo odontológico.
- **Fora do escopo:**
  - o plano de tratamento, com os itens previstos e o tratamento concluído (SB3b);
  - a unificação dos catálogos de Regulação, Laboratório e o geral;
  - a regra de qual item do catálogo cada categoria profissional pode fazer além da exigência de dentista.

## Testes

- **JUnit:**
  - **`ProcedimentoOdontologicoControllerTest`:**
    - **o catálogo:**
      - a resina grava Restaurado; a exodontia exige dentista e o flúor não;
      - o filtro pela especialidade traz os itens gerais;
      - recusas: código repetido (409), SIGTAP inválido (400), item geral com dente (422), condição de face num item do
        dente inteiro (422) e condição num item sem alcance (422);
      - aceito: o item com SIGTAP; depois, a troca de código (422), a desativação e o item inativo recusado no registro
        (422);
    - **o complemento:**
      - pedidos com 400: sem consulta nem atendimento, sem item nem tipo, resina sem face e dente fora da FDI;
      - pedidos com 422: incisal no 36, exodontia com face, flúor com dente, texto livre com dente e flúor fora da
        odontologia;
      - o curativo geral no atendimento comum e o flúor no odontológico entram no prontuário do atendimento;
    - **o odontograma:**
      - a resina no 36 (distal e oclusal) grava Restaurado com o nome do procedimento;
      - o registro não se anula pelo odontograma (422);
      - a exodontia agendada não grava, e o desfecho realizado grava Ausente no 48;
      - a retificação para o 37 anula os registros do 36, com o motivo, e grava os do 37;
      - o prontuário marca a versão retificada.
  - **`SaudeBucalAutorizacaoControllerTest`:**
    - o técnico registra o flúor e o selante, mas não a exodontia (403); o enfermeiro também não (403); o dentista
      registra;
    - o catálogo é lido pelo técnico e não é mantido pelo dentista (403).
  - **`ProcedimentoControllerTest`:** o procedimento da consulta continua igual.
- **Robot:**
  - **de API:** `ProcedimentoOdontologico.robot` (CT-001 a CT-010):
    - catálogo; item com SIGTAP; item geral com dente; código repetido;
    - a restauração gravando o odontograma; sem face; face que o dente não tem; fora do atendimento odontológico;
    - a retificação que anula e regrava;
    - sem consulta nem atendimento.
  - **de interface:** `UI_saude_bucal.robot`:
    - CT-004: a restauração em resina na oclusal do 36, pelo catálogo, grava Restaurado no odontograma e aparece no
      histórico do dente;
    - CT-005: o catálogo de procedimentos na tela Setores.
