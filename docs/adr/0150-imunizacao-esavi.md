# 0150 — Imunização: ESAVI e erro de imunização (I4)

## Status

Aceita e implementada. Quarta fatia do domínio 24, desenhado na [ADR-0147](./0147-imunizacao.md). Usa a notificação, a
investigação e o encerramento da Vigilância ([ADR-0124](./0124-vigilancia-epidemiologica-fundacao.md)).

## Contexto

- **A ADR-0147 deixou a I4 assim:** o evento supostamente atribuível à vacinação ou imunização (ESAVI), notificado pela
  Vigilância e ligado à dose.
- **A Vigilância já tem o fluxo inteiro:**
  - o agravo como catálogo e a ficha como dado, com versão;
  - a notificação, a investigação, a classificação e o encerramento com prazo;
  - as sugestões de notificação a partir da rede.

  Faltava o que é próprio da vacina: a dose, a gravidade, a causalidade e o que fazer com o esquema.
- **O PNI trata o ESAVI assim:**
  - o grave (internação, risco de morte, sequela, óbito) é comunicado em até 24 horas;
  - a investigação termina com a **causalidade** da OMS: A1 reação ligada ao produto, A2 desvio de qualidade, A3 erro
    de imunização, A4 ansiedade pela imunização, B indeterminado, C coincidente, D inclassificável;
  - a conduta sobre o esquema é manter, continuar com precaução no CRIE (centro de referência para imunobiológicos
    especiais) ou contraindicar as doses seguintes;
  - o **erro de imunização** (vacina, idade, intervalo, via, dose, validade ou armazenamento fora do recomendado) é
    notificado mesmo sem evento.

## Decisão

1. **Dois agravos no catálogo, sem tabela própria:**
   - **ESAVI:** compulsório, imediato, prazo de encerramento de 60 dias, critérios clínico e laboratorial, CID T88.1.
     A ficha pede as manifestações (reação local, febre, convulsão, anafilaxia, episódio hipotônico-hiporresponsivo,
     choro persistente, Guillain-Barré, encefalite ou encefalopatia, outra), as horas até o início e a internação;
   - **Erro de imunização:** compulsório, semanal, prazo de 30 dias, critério clínico. A ficha pede o tipo de erro e se
     houve evento;
   - as duas fichas são dado, com versão, como as outras.
2. **A notificação aponta as doses** (`TB_NOTIFICACAO_DOSE`), uma ou várias, porque a criança costuma tomar várias no
   mesmo dia:
   - **origem nova:** `VACINACAO`;
   - **o ESAVI e o erro de imunização pedem a dose** (400); os outros agravos não aceitam dose (400);
   - **a dose** precisa ser do paciente, nem anulada nem retificada (422);
   - **os primeiros sintomas** não vêm antes da dose mais antiga (400).
3. **Gravidade:** todo ESAVI é grave ou não grave (400 sem ela). O erro de imunização e os outros agravos não têm
   gravidade (400). O grave aparece como "Grave · comunicação imediata".
4. **O encerramento do ESAVI:**
   - **pede a causalidade e a conduta** (400 sem elas). Nos outros agravos, as duas são 400;
   - **a causalidade dá a classificação final:** as associações consistentes (A1 a A4) confirmam, a coincidente (C)
     descarta, e a indeterminada (B) e a inclassificável (D) ficam inconclusivas. O ESAVI não precisa da classificação
     manual antes de encerrar, e o boletim da Vigilância conta confirmados e descartados do mesmo jeito;
   - **o erro de imunização** segue o encerramento comum, com a classificação.
5. **Contraindicação vacinal** (`TB_CONTRAINDICACAO_VACINAL`):
   - **nasce da conduta "contraindicar"** no encerramento do ESAVI: uma por paciente e imunobiológico das doses, com a
     notificação de origem, a data e o motivo;
   - **na caderneta:** o alerta "contraindicada por ESAVI";
   - **no registro da dose aplicada:** uma nova dose dessa vacina só entra com a justificativa, que é a decisão do CRIE
     (422 sem ela). A dose transcrita da caderneta não é barrada, porque já aconteceu.
6. **Sugestão de erro de imunização:** as doses aplicadas nos últimos 30 dias fora do calendário (abaixo da idade
   mínima, acima da máxima ou antes do intervalo mínimo), que só entraram com justificativa, viram sugestão de
   notificação na Vigilância. A dose já ligada a uma notificação sai da lista.
7. **Permissões** (sem permissão nova):
   - **notificar:** `VIGILANCIA.NOTIFICAR` na unidade, que o técnico de enfermagem da sala de vacina já tem;
   - **investigar e encerrar:** `VIGILANCIA.INVESTIGAR`, da vigilância epidemiológica;
   - **a caderneta**, com a contraindicação e as doses notificadas, segue as regras do prontuário.

### API

Sem rota nova; mudam as da Vigilância e da Imunização:
- `POST vigilancia/notificacao` ganha `doseIds` e `gravidade`;
- `POST vigilancia/notificacao/{id}/encerramento` ganha `causalidade` e `conduta`;
- a notificação devolve `gravidadeEsavi`, `causalidadeEsavi`, `condutaEsavi` e as `doses` (vacina, dose, data, lote e
  unidade);
- a caderneta (`GET paciente/{id}/vacinacao`) devolve `contraindicacoes` e `dosesNotificadas`;
- `GET vigilancia/sugestoes` traz a sugestão de origem `VACINACAO`;
- `GET indicadores-rede/vacinacao` ganha `esaviNotificados` e `esaviGraves` no período.

### Tela

- **Caderneta do paciente:**
  - cada dose ganha "Notificar ESAVI", que abre a notificação da Vigilância já com o paciente, a unidade da dose, o
    agravo e a dose;
  - a dose já notificada leva a marca "ESAVI notificado";
  - no alto, o alerta das vacinas contraindicadas por ESAVI.
- **Vigilância:**
  - **na notificação** de ESAVI ou erro de imunização: o aviso da dose ligada e, no ESAVI, a gravidade. Sem dose, a
    tela orienta a notificar pela caderneta;
  - **no detalhe:** a gravidade, as doses, a causalidade e a conduta;
  - **no encerramento do ESAVI:** a causalidade e a conduta;
  - a sugestão de erro de imunização abre a notificação já com a dose.
- **Sala de Situação, bloco Vacinação:** o número de ESAVI do período e os graves.

### Migração

`V39__imunizacao_esavi.sql`:
- os agravos `ESAVI` e `ERRO_IMUNIZACAO`, com critérios, CID e fichas;
- a origem `VACINACAO` no CHECK de `tb_notificacao`;
- as colunas `gravidade_esavi`, `causalidade_esavi` e `conduta_esavi`, com os CHECKs;
- as tabelas `tb_notificacao_dose` e `tb_contraindicacao_vacinal`.

## Consequências

- **O ESAVI não tem fluxo paralelo:** prazo, investigação, linha do tempo, boletim e sugestões são os da Vigilância.
- **A contraindicação não tem fim por ora.** Ela vale até alguém registrar a dose com a justificativa. Revogar a
  contraindicação (por reavaliação no CRIE) fica para depois, com a tela do CRIE.
- **Ficam para depois:**
  - o alerta por lote (ESAVI graves no mesmo lote, ou causalidade A2), com o bloqueio do lote na rede de frio;
  - o envio do ESAVI ao Ministério (e-SUS Notifica / SI-PNI), com a I5 e o domínio 20.

## Testes

- **JUnit:**
  - `EsaviControllerTest`:
    - as recusas: sem dose, sem gravidade, dose de outro paciente, sintomas antes da dose, dose em outro agravo;
    - o ESAVI com duas doses, a origem e a gravidade;
    - as doses notificadas na caderneta;
    - o encerramento sem causalidade e sem conduta (400), e com elas, que dá a classificação final;
    - a contraindicação na caderneta e no registro da dose, com e sem justificativa;
    - o ESAVI na Sala de Situação;
    - o erro de imunização: a sugestão pela dose fora do calendário, a gravidade recusada, a saída da sugestão depois de
      notificar e o encerramento comum, sem causalidade;
  - `EsaviAutorizacaoControllerTest`, com segurança ligada: o técnico notifica na própria unidade, não em outra, e não
    encerra; a recepção não notifica; a vigilância encerra.
- **Robot de API:** `test/imunizacao/Esavi.robot`, CT-001 a CT-008.
- **Robot de tela:** `test/ui/imunizacao/UI_sala_de_vacina.robot`, com a notificação pela caderneta e o encerramento
  com contraindicação.
