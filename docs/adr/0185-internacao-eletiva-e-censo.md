# 0185 — Regulação e Leitos: internação eletiva com reserva de leito e censo diário

## Status

Aceita e implementada. Fatia **RL2** do plano da [ADR-0184](./0184-regulacao-de-leitos.md) para fechar os domínios 11 (Regulação) e
12 (Leitos e Internação):

| Fatia | O quê |
|---|---|
| RL1 | Central de regulação de leitos entre unidades, reserva de leito com prazo e faixa etária do leito |
| **RL2 (esta)** | Internação eletiva agendada com reserva de leito e censo hospitalar diário formal |
| RL3 | Laudo para solicitação de AIH e autorização interna, com o número da AIH |
| RL4 | Prescrição hospitalar diária (medicamentos, dieta e cuidados) e checagem pela enfermagem |
| RL5 | Ficha clínica do atendimento pré-hospitalar (APH) na ocorrência de urgência |

Ficam no domínio 20, sem impedir a fatia: o envio do censo a sistemas externos e a agenda do centro cirúrgico integrada a outro sistema.

## Contexto

- **Cirurgia eletiva sem leito garantido.** A internação só existia como admissão na hora. O hospital não tinha onde agendar a cirurgia
  programada nem como segurar o leito para o dia.
- **Censo calculado, não fechado.** Os indicadores de leitos mostravam a ocupação do momento. O censo hospitalar diário, que o NIR e a
  enfermagem fecham todo dia e que alimenta a taxa de ocupação, o paciente-dia e a média de permanência, não existia como registro.
- **A reserva da RL1** já tinha o leito RESERVADO com prazo, mas só para o pedido da central.

## Decisão

1. **Agendamento da eletiva** (`TB_INTERNACAO_AGENDADA`, `INTERNACAO.GERENCIAR` na unidade):
   - **Conteúdo:** paciente, unidade, data prevista, tipo de leito, médico responsável, CID, procedimento ou cirurgia, previsão de
     permanência e, opcionalmente, a solicitação da Regulação que a originou.
   - **Regras:** data no passado é 422; paciente inativo é 422; uma eletiva em aberto (AGENDADA ou RESERVADA) por paciente (409).
   - **Situações:** AGENDADA, RESERVADA, ADMITIDA, FALTA e CANCELADA, com os eventos guardados (`TB_EVENTO_INTERNACAO_AGENDADA`).
2. **Reserva do leito:**
   - **Janela:** abre **dois dias antes** da data prevista e vai até ela; fora disso, 422.
   - **Leito:** livre, da unidade da eletiva, do tipo pedido, com o sexo da enfermaria e a faixa etária do paciente (a faixa pode ser
     justificada, como na RL1).
   - **Prazo:** a reserva vale até 23:59:59 do dia previsto. O leito fica RESERVADO com o identificador do agendamento
     (`TB_LEITO.RESERVA_AGENDAMENTO_ID`), ao lado do identificador do pedido da RL1.
   - **Vencimento:** visto na consulta e gravado na próxima ação, sem rotina agendada (mesma escolha da RL1). A eletiva volta para
     AGENDADA e aparece como **aguardando desfecho**, para registrar admissão ou falta.
3. **Admissão:** gera a internação pelo mesmo caminho da admissão comum, com o médico do agendamento. Com reserva, usa o leito reservado;
   sem reserva, pede um leito livre compatível (400 se faltar).
4. **Falta, remarcação e cancelamento:** pedem motivo (400). A falta só a partir do dia previsto (422). A remarcação pede nova data, não
   no passado. As três liberam o leito reservado.
5. **Censo diário** (`TB_CENSO_DIARIO` e `TB_CENSO_DIARIO_PACIENTE`):
   - **Dia:** só dia terminado (hoje ou futuro é 422), no fuso do servidor.
   - **Números:**
     - leitos existentes, bloqueados e operacionais (os ativos da unidade, contados como estão na hora do fechamento);
     - internados às 0h e às 24h, admissões;
     - altas por tipo (melhorado, a pedido, transferência), evasões e óbitos;
     - transferências internas (trocas de leito no dia);
     - **paciente-dia** = internados às 24h mais os que entraram e saíram no mesmo dia;
     - **taxa de ocupação** = paciente-dia ÷ leitos operacionais × 100;
     - **média de permanência** dos que tiveram alta no dia, em dias.
   - **Censo nominal:** quem está internado às 24h, com leito, setor e dias de internação, guardado no fechamento.
   - **Prévia:** sem censo fechado, a consulta devolve o cálculo do dia, sem identificador.
   - **Fechamento** (`CENSO.FECHAR`, nova): grava a versão 1. Já fechado é 409 (índice único parcial por unidade e dia, entre os não
     reabertos).
   - **Reabertura** (`CENSO.FECHAR`): pede motivo (400). A versão reaberta fica guardada com o motivo, e o novo fechamento grava a versão
     seguinte.
   - **Pendências:** os dias terminados dos últimos 30 sem censo fechado, por unidade com leito no escopo.
6. **Permissão nova `CENSO.FECHAR`** (dado de saúde), dada ao Gestor de leitos (NIR) e à Coordenação de enfermagem. Consultar o censo
   pede `CENSO.FECHAR` ou as permissões de ver leitos.
7. **Tela Leitos:**
   - botão **Agendar internação eletiva** no cabeçalho;
   - aba **Eletivas**: agenda, reservar leito, admitir, remarcar, falta e cancelar, com o histórico;
   - aba **Censo diário**: unidade e dia (padrão: ontem), números, censo nominal, fechar e reabrir com motivo, aviso dos censos pendentes.

## Alternativas descartadas

- **Guardar o histórico do cadastro de leitos para o censo:** os leitos existentes e bloqueados são os da hora do fechamento. Fechar no
  dia seguinte, que é a rotina, dá o número certo; o histórico de cadastro do leito seria uma mudança maior, sem pedido.
- **Reserva desde o agendamento:** seguraria leito por semanas. A janela de dois dias segue a rotina dos NIRs.
- **Rotina agendada para fechar o censo sozinho:** o fechamento é um ato de quem responde pelo número. A lista de pendências cobre o
  esquecimento.
- **Editar o censo fechado:** quebraria a rastreabilidade. Reabrir com motivo e fechar outra versão guarda as duas.

## Consequências

- **STATUS-DOS-DOMINIOS:** "Reserva de leito para cirurgia eletiva" e "Censo diário formal" saem das pendências dos domínios 11 e 12.
- **Coordenação de enfermagem e NIR:** conceder `CENSO.FECHAR` nos ambientes, quando a autorização for ligada, é tarefa de implantação.
- **Ficam para as próximas fatias:** RL3 a RL5, acima.

## Testes

- **JUnit:**
  - `EletivaCensoControllerTest`:
    - agendamento, reserva na janela e admissão como eletiva;
    - reserva só na janela de dois dias e admissão sem reserva pedindo leito;
    - falta, remarcação e cancelamento liberando a reserva;
    - reserva vencida voltando o leito e deixando a eletiva aguardando desfecho;
    - censo do dia com paciente-dia e taxa de ocupação conferidos, fechamento, reabertura e nova versão;
  - `EletivaCensoAutorizacaoControllerTest`, com a segurança ligada: o NIR do hospital agenda e fecha o censo; o médico agenda mas não
    fecha; o NIR de outro hospital recebe 403.
- **Robot:** `POST_eletiva_censo.robot` (API, CT-001 a CT-006) e os casos CT-013 a CT-015 de `UI_leitos.robot`.
