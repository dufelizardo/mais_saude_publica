# 0188 — Regulação e Leitos: ficha clínica do atendimento pré-hospitalar

## Status

Aceita e implementada. Fatia **RL5**, a última do plano da [ADR-0184](./0184-regulacao-de-leitos.md) para fechar os domínios 11
(Regulação) e 12 (Leitos e Internação). Com ela, o domínio 11 fica completo; SISREG, SIGTAP e a telefonia do 192 seguem no domínio 20.

| Fatia | O quê |
|---|---|
| RL1 | Central de regulação de leitos entre unidades, reserva de leito com prazo e faixa etária do leito |
| RL2 | Internação eletiva agendada com reserva de leito e censo hospitalar diário ([ADR-0185](./0185-internacao-eletiva-e-censo.md)) |
| RL3 | Laudo para solicitação de AIH e autorização interna, com o número da AIH ([ADR-0186](./0186-laudo-aih.md)) |
| RL4 | Prescrição hospitalar, dieta e checagem pela enfermagem ([ADR-0187](./0187-prescricao-hospitalar.md)) |
| **RL5 (esta)** | Ficha clínica do atendimento pré-hospitalar (APH) na ocorrência de urgência |

## Contexto

- **A ocorrência do 192** ([ADR-0176](./0176-regulacao-de-urgencia.md)) registra o chamado, a regulação médica, a viatura empenhada
  ([ADR-0178](./0178-transporte-sanitario.md)), os deslocamentos e o recebimento no destino.
- **O que a equipe faz na cena não era registrado:** avaliação, sinais vitais, procedimentos, medicamentos, desfecho e a entrega no
  hospital. Na rotina do SAMU, isso é a ficha de atendimento que segue com o paciente.

## Decisão

1. **Ficha do APH** (`TB_FICHA_APH`, uma por ocorrência):
   - **Quando:** só nas ocorrências com recurso enviado (envio de recurso ou transferência com recurso); nas demais e na cancelada, 422.
   - **Quem preenche:** permissão nova `APH.REGISTRAR`, no escopo da central, dada ao papel novo **Equipe de intervenção do SAMU**
     (médico intervencionista, enfermeiro e técnico da viatura).
   - **Cena:** natureza (clínica, trauma, obstétrica, psiquiátrica, pediátrica), o mecanismo no trauma e a situação encontrada.
   - **Avaliação primária (XABCDE):** hemorragia exsanguinante, via aérea, respiração, circulação, Glasgow por componente (ocular 1–4,
     verbal 1–5, motor 1–6, com o total calculado), pupilas e exposição.
   - **Procedimentos** (`TB_FICHA_APH_PROCEDIMENTO`): oxigênio, intubação, acesso venoso, colar cervical, prancha, tala, curativo ou
     compressão, torniquete, RCP, desfibrilação, monitorização, parto e outro (com a descrição, 400 sem ela).
   - **Impressão diagnóstica** e CID opcional.
   - **Desfecho na cena:** removido ao destino, atendido e liberado no local, recusa de remoção, óbito no local ou evasão; a entrega no
     destino (quem recebeu e a hora).
2. **Sinais vitais em série** (`TB_SINAIS_VITAIS_APH`): pressão, frequências cardíaca e respiratória, saturação, temperatura, glicemia, dor
   e Glasgow, cada um com a faixa válida (400 fora dela); ao menos uma medida; a hora não pode estar no futuro (422).
3. **Medicamentos** (`TB_MEDICAMENTO_APH`): nome, dose, via e hora, em texto.
4. **Finalização:** pede a natureza e o desfecho; no óbito, a hora e o médico que constatou; na recusa, quem recusou; na remoção, quem
   recebeu no destino (422 com o que falta). Finalizada, a ficha não muda (422), nem recebe sinais e medicamentos.
5. **Adendo** (`TB_ADENDO_FICHA_APH`): a correção ou o complemento da ficha finalizada, com o profissional e a hora.
6. **Quem lê:** é dado clínico. A central (a equipe, o atendente e o regulador) e a unidade de destino veem a ficha; quem só acompanha a
   urgência (`URGENCIA.CONSULTAR`) não vê.
7. **PDF:** a ficha para entregar ao hospital de destino e anexar ao prontuário físico.
8. **Telas:**
   - na tela Urgência, a seção **Atendimento na cena** (para a equipe): as ocorrências com recurso em curso e as concluídas há até 24
     horas com a ficha por finalizar, e a gaveta da ficha com cena, avaliação, procedimentos, desfecho, sinais vitais em série,
     medicamentos, finalização, adendo e PDF;
   - no detalhe da ocorrência, o resumo da ficha e o PDF (regulador, atendente e destino);
   - no prontuário, o PDF da ficha junto de cada ocorrência com recurso.

## Alternativas descartadas

- **Medicamentos do catálogo e baixa no estoque da viatura:** a viatura ainda não é um local de estoque; o texto registra o que foi
  dado sem depender disso. O estoque da viatura fica para o Estoque (ES3) e o Transporte (T3).
- **Ficha editável depois de finalizada:** quebraria a rastreabilidade do documento entregue no hospital; o adendo guarda as duas.
- **Desfecho da ficha encerrando a ocorrência:** encerrar é ato do regulador (ADR-0176); a ficha informa, o regulador encerra.

## Consequências

- **STATUS-DOS-DOMINIOS:** o domínio 11 fica ✅; a telefonia e a integração com o 192, SISREG e SIGTAP seguem no domínio 20.
- **Implantação:** conceder o papel Equipe de intervenção do SAMU na central quando a autorização for ligada.
- **Fora do escopo:** telefonia e gravação do 192; estoque de medicamentos da viatura; transmissão de eletrocardiograma; aplicativo
  off-line para tablet da viatura.
- **Plano RL1 a RL5 concluído:** os domínios 11 e 12 estão completos.

## Testes

- **JUnit:**
  - `FichaAphControllerTest`:
    - ficha só com recurso enviado; lista da equipe; Glasgow e procedimento "outro";
    - sinais vitais em série (faixa, sem medida, hora no futuro) e medicamentos;
    - finalização trava a ficha; adendo só depois; PDF;
    - óbito pede a hora e o médico; recusa pede quem recusou;
  - `FichaAphAutorizacaoControllerTest`, com a segurança ligada: a equipe da central preenche; o regulador consulta mas não preenche; o
    gestor (só acompanha) não vê; a equipe de outra central recebe 403 e não vê os atendimentos.
- **Robot:** `FichaAph.robot` (API, CT-001 a CT-004) e o caso CT-002 de `UI_urgencia.robot`.
