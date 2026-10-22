# 0135 — Saúde Bucal: odontograma com histórico (SB2)

## Status

Aceita e implementada (fatia SB2 da [ADR-0111](./0111-saude-bucal.md); usa a avaliação odontológica da
[ADR-0134](./0134-saude-bucal-base.md)).

## Contexto

- **A SB1 registra a avaliação odontológica em texto,** mas não diz dente por dente o que foi encontrado.
- **A ADR-0111 (decisão 5) define o odontograma por registros, e não por estado mutável:** a situação dos dentes muda
  com o tempo, e o sistema precisa responder "como estava o 36 antes do tratamento?" e "o que mudou a condição?".
- **O MODELO-SAUDE-BUCAL (seção 6)** fixa a notação FDI (ISO 3950), as cinco faces e as condições como catálogo.

## Decisão

1. **Catálogo de condições** (`CondicaoOdontologica`, `tb_condicao_odontologica`), cadastrável, sem enum:
   - código (único, 409; não muda, 422), nome, **cor** (#RRGGBB, 400) e **sigla** do desenho, e o **alcance**: da face,
     do dente inteiro ou dos dois;
   - **carga inicial editável:** hígido, ausente, extração indicada, prótese, implante e raiz residual (do dente); cárie,
     restaurado, restauração insatisfatória e selante (da face); fratura (dos dois);
   - manutenção com `ORGANIZACAO.GERENCIAR`, como o catálogo de especialidades (ADR-0134); leitura por qualquer logado.
2. **Registro do odontograma** (`RegistroOdontograma`, `tb_registro_odontograma`):
   - paciente, **dente FDI**, **face** (nula: o dente inteiro), condição, **data** e a **origem**;
   - **FDI:** permanentes nos quadrantes 1 a 4 (dentes 1 a 8) e decíduos nos quadrantes 5 a 8 (dentes 1 a 5); fora
     disso, 400;
   - **faces:** mesial, distal, oclusal, incisal, vestibular e lingual ou palatina. A **oclusal só nos posteriores** e a
     **incisal só nos anteriores** (dentes 1 a 3 de cada quadrante); o contrário dá 422;
   - **a condição cabe no alcance:** a da face pede a face, a do dente inteiro não aceita face (422); a inativa não
     entra (422);
   - **origem:** a **avaliação odontológica vigente** do atendimento, que recebe várias marcações de uma vez; a
     avaliação retificada não recebe (422). A data, sem informar, é a da avaliação, e não fica no futuro (400). O
     procedimento como origem entra na SB3;
   - **registro clínico não se edita:** a marcação errada é **anulada** com motivo (400 sem ele; já anulada, 422; de
     outro profissional, só com supervisão). A anulada continua no histórico e sai da projeção.
3. **Projeção por data:** o odontograma de uma data são os registros não anulados até ela, em ordem.
   - **O registro do dente inteiro vale por cima das faces registradas antes dele** (o dente com extração indicada não
     mostra mais a cárie da oclusal); faces registradas depois dele voltam a aparecer.
   - O **histórico do dente** traz todos os registros, inclusive os anulados, com a origem e quem registrou.
4. **Acesso:** marcar e anular com `ODONTOLOGIA.REGISTRAR` (o cirurgião-dentista); ler com as regras do prontuário
   (`PRONTUARIO.CONSULTAR` ou `ODONTOLOGIA.REGISTRAR`, vínculo assistencial e leitura auditada). Sem permissão nova.
5. **API:**
   - `GET /condicoes-odontologicas`, `POST /condicao-odontologica` (201) e `PUT /condicao-odontologica/{id}`;
   - `POST /avaliacao-odontologica/{id}/odontograma` (201; responde o odontograma de hoje);
   - `POST /registro-odontograma/{id}/anulacao`;
   - `GET /paciente/{id}/odontograma?data=` e `GET /paciente/{id}/odontograma/dente/{dente}`.
6. **Tela:** no atendimento de odontologia, a seção **"Odontograma"**:
   - o desenho dos dentes (dentição permanente ou decídua) com as cinco faces, a vestibular do lado de fora da arcada e
     a mesial voltada para a linha média, pintadas pela cor da condição; a condição do dente inteiro contorna o dente e
     mostra a sigla;
   - clicar na face ou no número do dente mostra as condições que cabem e o histórico do dente, com a anulação;
   - as marcações ficam pendentes até "Salvar marcações" na avaliação vigente; sem avaliação, só leitura;
   - o **seletor de data** mostra como estava; marcar, só no odontograma de hoje.
7. **Migração V28:** `tb_condicao_odontologica` com a carga inicial e `tb_registro_odontograma`.

## Consequências

- **O dentista marca dente por dente,** e o histórico responde como estava antes e quem registrou.
- **O plano de tratamento (SB3) se apoia no odontograma:** o procedimento feito no dente gera o registro que muda a
  condição (a restauração da oclusal do 36).
- **Fora do escopo:**
  - o procedimento como origem do registro (SB3);
  - o periograma (bolsas e sangramento);
  - o índice CPO-D, que fica para o levantamento epidemiológico da saúde bucal coletiva (SB5).

## Testes

- **JUnit:**
  - `OdontogramaControllerTest`:
    - o catálogo com a cárie da face; código repetido (409), cor inválida (400), código trocado (422); cor e sigla em
      maiúsculas;
    - dente 19 e 56 fora da FDI (400); cárie sem face e ausente na face (422); oclusal no 11 e incisal no 36 (422); data
      no futuro (400); as marcações aceitas no 11, 36, 48 e 85;
    - a projeção: a cárie há 10 dias, a restauração há 3, e hoje a extração indicada por cima das faces; a anulação sem
      motivo (400), com ele e repetida (422); o histórico com as três, a anulada marcada; o dente 99 (400); a avaliação
      retificada não recebe marcação (422).
  - `SaudeBucalAutorizacaoControllerTest`: o técnico em saúde bucal não marca o odontograma (403); o dentista marca.
  - `MigracoesDoEsquemaTest`: os CHECKs das enumerações novas batem com o Java.
- **Robot:**
  - **de API:** `Odontograma.robot` (CT-001 a CT-009): catálogo, marcação, dente fora da FDI, face que o dente não tem,
    alcance da condição, projeção por data, anulação e repetida, anulação sem motivo e avaliação inexistente.
  - **de interface:** `UI_saude_bucal.robot` CT-003: marcar a cárie na oclusal do 36 e o 48 ausente, salvar e ver no
    histórico do dente.
