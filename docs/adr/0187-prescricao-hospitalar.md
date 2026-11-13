# 0187 — Regulação e Leitos: prescrição hospitalar, dieta e checagem pela enfermagem

## Status

Aceita e implementada. Fatia **RL4** do plano da [ADR-0184](./0184-regulacao-de-leitos.md) para fechar os domínios 11 (Regulação) e
12 (Leitos e Internação). Com ela, o domínio 12 fica completo; o SIH e o faturamento seguem no domínio 20.

| Fatia | O quê |
|---|---|
| RL1 | Central de regulação de leitos entre unidades, reserva de leito com prazo e faixa etária do leito |
| RL2 | Internação eletiva agendada com reserva de leito e censo hospitalar diário ([ADR-0185](./0185-internacao-eletiva-e-censo.md)) |
| RL3 | Laudo para solicitação de AIH e autorização interna, com o número da AIH ([ADR-0186](./0186-laudo-aih.md)) |
| **RL4 (esta)** | Prescrição hospitalar diária (medicamentos, dieta e cuidados), aprazamento e checagem pela enfermagem |
| RL5 | Ficha clínica do atendimento pré-hospitalar (APH) na ocorrência de urgência |

## Contexto

- **A checagem de medicamento** ([ADR-0064](./0064-administracao-de-medicamento-enfermagem-e-farmacia.md)) nasce da consulta do
  atendimento ambulatorial. Na internação não havia prescrição diária, dieta, cuidados de enfermagem nem aprazamento.
- **O STATUS** listava "Prescrição com aprazamento" na Enfermagem (domínio 8) e "Prescrição e dieta hospitalar" no domínio 12.
- **A rotina do hospital:** o médico prescreve na visita, para as próximas 24 horas, quase sempre repetindo a do dia anterior com
  ajustes; a enfermagem apraza os horários e checa cada um; a nutrição acompanha a aceitação da dieta.

## Decisão

1. **Prescrição hospitalar** (`TB_PRESCRICAO_HOSPITALAR`, permissão nova `PRESCRICAO.HOSPITALAR`, dada ao Médico):
   - **Vigência:** 24 horas a partir do início (padrão: a próxima hora cheia). O início vai de 2 horas atrás a 24 horas à frente (422).
   - **Uma vigente por internação:** a nova encerra a anterior no seu início, e os horários pendentes da anterior dali em diante ficam
     cancelados. Outra prescrição começando no mesmo início ou depois é 409. Internação com alta não recebe prescrição (422).
   - **Renovar:** a prescrição seguinte (padrão: do fim da anterior) com os itens não suspensos da anterior, os horários deslocados.
   - **Alta:** encerra a vigente e cancela os horários pendentes.
2. **Itens** (`TB_ITEM_PRESCRICAO`), não editados:
   - **Medicamento:** do catálogo da Farmácia (inativo é 422), com dose, via, intervalo (vazio: dose única) e primeiro horário, ou
     "se necessário" com a condição de uso; observação para diluição e infusão.
   - **Dieta:** zero, livre, branda, pastosa, líquida, líquida-pastosa, hipossódica, para diabético, enteral ou parenteral, com observação.
   - **Cuidado:** texto, com intervalo opcional.
   - **Suspensão:** o médico suspende com motivo; os horários pendentes ficam cancelados.
3. **Aprazamento** (`TB_HORARIO_PRESCRICAO`):
   - do primeiro horário (ou do início), de intervalo em intervalo, dentro da vigência;
   - dieta oral: as refeições (07h, 09h30, 12h, 15h, 18h e 21h); zero, nenhum; enteral e parenteral, pelo intervalo;
   - cuidado sem intervalo e "se necessário": nenhum horário;
   - **reaprazar:** a enfermagem muda o horário pendente para outro dentro da vigência, com motivo; o original fica guardado.
   - **Atraso:** pendente há mais de 1 hora.
4. **Checagem** (`TB_CHECAGEM_PRESCRICAO`):
   - **Quem:** permissão nova `PRESCRICAO.CHECAR` (Enfermeiro, Técnico de enfermagem e Coordenador de enfermagem), em todos os itens;
     papel novo **Nutricionista**, com `DIETA.CHECAR`, só na dieta.
   - **Medicamento:** administrado, com o lote opcional (do estoque da unidade e do mesmo medicamento; com lote, a saída sai no livro da
     Farmácia como na ADR-0064); ou não administrado, com os motivos da ADR-0064.
   - **Dieta:** ofertada com a aceitação (total, parcial, recusou) ou não ofertada com o motivo.
   - **Cuidado:** feito, ou não feito com o motivo.
   - **"Se necessário":** checado pelo item, sem horário, com a prescrição vigente.
   - **Horário já checado ou cancelado:** 422. Item suspenso: 422.
   - **Retificação** ([ADR-0062](./0062-registros-clinicos-imutaveis-com-retificacao.md)): a checagem não é editada; a retificação, com
     motivo, substitui a anterior, que fica guardada, e estorna a baixa do lote.
5. **Posto de enfermagem:** os horários das internações em curso de 6 horas antes a 6 depois (até 48), por unidade e setor, com os
   atrasados. Quem cuida do paciente (gerenciar a internação, alta, prescrever ou checar) vê todos os itens; quem só checa dieta vê a
   dieta.
6. **Telas:**
   - na gaveta da internação, a seção **Prescrição**: os itens com os horários (o pendente abre a checagem), "Registrar dose" no
     "se necessário", suspender item, nova prescrição e renovar;
   - aba **Horários** na tela Leitos: o posto, com checar, reaprazar e retificar;
   - **prontuário:** as prescrições e checagens de cada internação, recolhidas, e o número da AIH (ADR-0186).

## Alternativas descartadas

- **Editar a prescrição vigente:** quebraria a rastreabilidade do que estava valendo quando a enfermagem checou. Suspender o item e
  prescrever de novo guarda os dois.
- **Baixa obrigatória do lote em toda checagem:** no hospital, a dose costuma chegar já dispensada da farmácia; o lote fica opcional.
  A dispensação por dose individualizada fica para depois.
- **Horários das refeições configuráveis por unidade:** fixos por ora, com o padrão das rotinas de nutrição hospitalar.
- **Alerta de interação medicamentosa e alergia:** depende de base externa (domínio 20).

## Consequências

- **STATUS-DOS-DOMINIOS:** o domínio 12 fica ✅; "Prescrição com aprazamento" sai da Enfermagem (domínio 8).
- **Listagens da Farmácia:** os medicamentos passam a ser listados para quem prescreve e os lotes para quem checa.
- **Implantação:** conceder o papel Nutricionista quando a autorização for ligada.
- **Fora do escopo:** dispensação por dose individualizada; interação e alergia; balanço hídrico e sinais vitais estruturados; o plano de
  cuidados da sistematização da assistência de enfermagem (SAE).
- **Fica para a próxima fatia:** RL5.

## Testes

- **JUnit:**
  - `PrescricaoHospitalarControllerTest`:
    - aprazamento por intervalo, refeições, dose única, zero, cuidado contínuo e "se necessário"; validações (400) e prescrição repetida (409);
    - suspensão cancela os pendentes; renovar copia os itens ativos; a nova encerra a anterior no seu início;
    - checagem com baixa do lote, retificação com estorno e reaprazamento dentro e fora da vigência;
    - "se necessário", dieta com aceitação, atraso no posto e alta encerrando a prescrição;
  - `PrescricaoHospitalarAutorizacaoControllerTest`, com a segurança ligada: o médico prescreve; o técnico checa mas não prescreve; a
    nutricionista checa só a dieta e vê só a dieta no posto; a enfermeira de outro hospital recebe 403.
- **Robot:** `POST_prescricao_hospitalar.robot` (API, CT-001 a CT-005) e os casos CT-019 e CT-020 de `UI_leitos.robot`.
