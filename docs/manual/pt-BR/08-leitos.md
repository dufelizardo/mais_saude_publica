# Leitos

> **Versão:** 1.15.0 · [English](../en/08-leitos.md)

Menu: **Assistência › Leitos**. Cobre a vida do leito e da internação:

- o mapa de leitos;
- internar, trocar de leito, dar alta e higienizar;
- a prescrição hospitalar e os horários do posto;
- as internações eletivas;
- o censo diário;
- o laudo e a autorização de AIH;
- a central de leitos entre unidades e o cadastro de leitos.

**Quem usa e o que cada um vê:** as abas aparecem conforme o perfil.

| Aba | Quem usa |
|-----|----------|
| **Mapa de leitos** e **Internações** | quem consulta internação: recepção, médico, enfermagem, nutrição, NIR, regulação de leitos e médico autorizador |
| **Horários** | enfermagem (checa a prescrição), nutricionista (checa a dieta) e médico |
| **Eletivas** e **A receber** | médico, enfermagem e o NIR (gestor de leitos) |
| **Censo diário** | quem consulta; fecha a coordenação de enfermagem e o NIR |
| **Autorização de AIH** | médico autorizador; as faixas de numeração são do gestor |
| **Central de leitos** | regulador de leitos |
| **Cadastro de leitos** | enfermagem, coordenação de enfermagem e o NIR |

**Os cartões do alto:**

- a **Taxa de ocupação;**
- os **Livres**, prontos para internar;
- a **Higienização e bloqueio**: em limpeza e bloqueados;
- a **Média de permanência.**

**Avisos no alto:**

- **Comunicação ao Ministério Público pendente:** a internação psiquiátrica involuntária precisa ser comunicada em até
  **72 horas** da admissão e da alta (Lei 10.216/2001).
- **Internações de urgência sem laudo de AIH há mais de 48 horas.**

## 1. O mapa de leitos

O mapa mostra os leitos por unidade e setor, com a situação de cada um: livre, ocupado, em higienização ou bloqueado.

| No leito… | Ação |
|-----------|------|
| livre | **Internar** ou **Bloquear** (com o motivo, por exemplo *manutenção da cama*; o leito sai do mapa de vagas até ser desbloqueado) |
| ocupado | **Abrir** a internação |
| em higienização | **Liberar**, quando a limpeza terminou |
| bloqueado | **Desbloquear** |

## 2. Internar na própria unidade

**Quem usa:** médico, enfermagem e o NIR. O botão **Internar** de dentro do atendimento (módulo 03) já traz o paciente e liga a
internação ao atendimento.

1. Clique em **Internar** no leito livre, ou use o botão do alto.
2. Em **Paciente**, digite parte do nome ou do CPF e escolha a pessoa.
3. Escolha o **Leito livre**. Só aparecem os livres e compatíveis com o sexo do paciente.
4. Informe o **CID-10 principal** (por exemplo, *J18.9*) e a **Matrícula do médico responsável**.
5. Escreva o **Motivo da internação**: o quadro clínico e por que precisa ficar internado.
6. Escolha o caráter (**Urgência** ou **Eletiva**) e, se quiser, a **Previsão de alta**.
7. Se o leito for de outra faixa etária, escreva a **Justificativa de faixa etária**. As faixas são: neonatal até 28 dias,
   pediátrico até 13 anos incompletos, adulto a partir de 13.
8. Salve.

## 3. A internação aberta

Na aba **Internações** (ou ao abrir um leito ocupado), clique em **Ver**. A lista não mostra o motivo nem o sumário: eles abrem no
detalhe, com a leitura registrada.

O detalhe mostra o médico responsável, o CID-10, o motivo, os **Movimentos** e as ações:

- **Trocar de leito:**
  1. escolha o **Leito de destino** e escreva o **Motivo** (por exemplo, *isolamento de contato*);
  2. informe a **Matrícula de quem faz a troca**.

  O leito atual vai para higienização e só fica livre quando a limpeza for registrada.
- **Pedir transporte:** abre o pedido de transporte sanitário.
- **Dar alta** (médico):
  1. escolha o **Tipo de alta**: melhorado, a pedido, transferência para outra unidade, evasão ou óbito;
  2. informe a **Data e hora** (vazio é agora; não pode ser no futuro);
  3. escreva o **Sumário de alta**: o diagnóstico, o tratamento, a evolução e as orientações para quem continua o cuidado;
  4. informe a **Matrícula do médico que dá a alta**.

  **A alta encerra a internação, que não muda mais.** O leito vai para higienização.
- **Internação psiquiátrica:**
  1. escolha a **Modalidade**: voluntária, involuntária ou compulsória (por ordem judicial, com o número dela);
  2. na involuntária, use **Registrar comunicação** para a comunicação ao Ministério Público, com o protocolo. Vencido o prazo, o
     detalhe mostra *prazo vencido*.

## 4. Prescrição hospitalar e horários

**Prescrever (médico):**

1. No detalhe da internação, em **Prescrição**, clique em **Nova prescrição**, ou em **Renovar** para copiar a anterior (os itens
   vêm em **Itens copiados**).
2. Em cada item, escolha:
   - **Medicamento:** a dose (por exemplo, *1 g*), a via e o **Intervalo (horas)**; com **Se necessário**, a condição de uso
     (por exemplo, *febre acima de 37,8*);
   - **Dieta:** zero (jejum), livre, branda, pastosa, líquida, líquida-pastosa, hipossódica, para diabético, enteral ou parenteral;
   - **Cuidado:** a descrição.
3. Informe a **Matrícula do médico** e salve. A prescrição vale por 24 horas.

**Suspender** um item cancela os horários ainda pendentes dele.

**Horários (posto de enfermagem):** a aba **Horários** mostra os horários aprazados das últimas e das próximas 6 horas, por
setor. **Pendente há mais de 1 hora fica atrasado.**

- **Checar:**
  - no medicamento, registre a dose. Com o **Lote**, ela baixa no estoque; escolha **Sem baixa** quando a dose já foi
    dispensada;
  - na dieta (nutricionista), a **Aceitação**;
  - se não foi feito, o **Motivo**.
- **Reaprazar:** o **Novo horário**.
- **Retificar:** a checagem, com o **Motivo da retificação**.

## 5. Internações eletivas

A aba **Eletivas** mostra as cirurgias e internações programadas dos próximos 30 dias, e as passadas ainda sem desfecho.

**Agendar internação eletiva:**

1. Use o botão do alto **Agendar internação eletiva**.
2. Informe:
   - o paciente, a **Unidade** e a **Data prevista** (não pode ser no passado);
   - o **CID-10** e o **Procedimento ou cirurgia**;
   - a **Previsão de permanência (dias)**.

**O leito:** **Reservar leito** guarda um leito a partir de dois dias antes. A reserva vale até o fim do dia previsto.

**No dia:**

- **Admitir:** o paciente chegou;
- **Mais › O que registrar:**
  - **Remarcar**, com a nova data;
  - **Falta do paciente;**
  - **Cancelar.**

O **Laudo AIH** também pode ser preenchido daqui.

## 6. Laudo e autorização de AIH

**Preencher o laudo (médico):**

1. No detalhe da internação ou na eletiva, use **Preencher laudo de AIH**.
2. Preencha:
   - os **Principais sinais e sintomas clínicos;**
   - as **Condições que justificam a internação;**
   - os **Principais resultados de provas diagnósticas;**
   - o **Diagnóstico inicial** e os CID-10 (principal, secundário e causa associada);
   - o **Procedimento solicitado**: busque pelo nome ou pelo código SIGTAP;
   - a **Clínica** e, se houver, a **Causa externa**: acidente de trânsito, de trabalho típico ou no trajeto, ou outra;
   - o **Médico solicitante (matrícula)**.
3. O laudo pode ser **Corrigido**, **Impresso** ou **Cancelado** enquanto não é autorizado.

**Autorizar (médico autorizador):** a aba **Autorização de AIH** mostra os laudos aguardando, urgência primeiro.

- **Autorizar:** o laudo recebe o próximo número da faixa de AIH vigente do ano.
- **Devolver** (com o que falta) ou **Negar**.

> **Quem solicitou não autoriza o próprio laudo.**

**Faixas de numeração (gestor):**

1. Em **Cadastrar faixa**, informe o **Ano** e os **12 primeiros dígitos** do início e do fim da faixa recebida do gestor, sem o
   dígito verificador. O sistema calcula o dígito (módulo 11).
2. A autorização usa o próximo número livre da faixa ativa do ano. **Sem faixa, não autoriza.**

## 7. Censo diário

**Para que serve:** o movimento do dia de cada unidade:

- os leitos operacionais e os internados às 0h;
- as admissões, as altas, as evasões e os óbitos, e as transferências internas;
- os internados às 24h, o paciente-dia e a ocupação.

1. Escolha a unidade e o **Dia** e clique em **Ver censo**. A lista **Censos não fechados** mostra o que falta fechar.
2. Confira o **Censo nominal (às 24h)**: quem estava internado, em que leito e setor, e há quantos dias.
3. Feche o censo (coordenação de enfermagem ou NIR).

**Fechado, o censo fica guardado.** Para corrigir, use **Reabrir censo** com o **Motivo da reabertura** e feche de novo.

## 8. Central de leitos (entre unidades)

**Pedir vaga em outra unidade (médico):**

1. Use o botão do alto **Pedir vaga em outra unidade**, ou **Pedir vaga** de dentro do atendimento.
2. Informe:
   - o paciente e a **Unidade de origem**;
   - o tipo de leito pedido e a prioridade;
   - o **CID-10** e o **Quadro clínico**: os sinais vitais, a gravidade e por que precisa de leito;
   - a **Matrícula de quem pede**.
3. O pedido vai para a central de leitos, que reserva um leito compatível em outra unidade.

> Para internar na própria unidade, use **Internar** (item 2).

**Regular (regulador de leitos):** a aba **Central de leitos** tem a fila por prioridade e hora do pedido. Em **Regular**:

- **Reservar** um dos **Leitos compatíveis**, por um tempo (**Reservar por (horas)**);
- **Vaga zero:** o paciente vai sem leito livre confirmado, e a unidade escolhe o leito na admissão. Escolha a **Unidade que
  recebe** e escreva a **Justificativa**;
- **Negar**, com o motivo.

Informe também o **Transporte** (por exemplo, *USA 02 do SAMU*) e a **Matrícula do regulador**.

**Receber (unidade de destino):** a aba **A receber** mostra os pacientes que a central mandou para a sua unidade, com leito
reservado ou em vaga zero.

- **Admitir** na chegada: escolha o **Leito livre da unidade** e informe o **Médico responsável (matrícula)**.
- **Recusar**, com o motivo. O pedido volta para a fila da central na posição original, e o leito volta a livre.

A origem pode **Cancelar pedido** enquanto ele está na fila. A reserva não usada vence no prazo e aparece como *Reserva vencida*.

## 9. Cadastro de leitos

**Quem usa:** enfermagem, coordenação de enfermagem e o NIR.

1. Clique em **+ Leito**.
2. Escolha a **Unidade** e o **Setor assistencial**. O leito fica em setor assistencial da unidade.
3. Informe a **Identificação** (por exemplo, *Enf. 2 · Leito 03*) e o **Tipo**: clínico, cirúrgico, pediátrico, obstétrico, UTI
   adulto, UTI pediátrica, UTI neonatal, observação ou isolamento.
4. Escolha o sexo (masculino, feminino ou misto) e a **Faixa etária**.
5. Salve.

**O leito não se apaga:** ele sai de uso, e o histórico continua. Fora de uso, ele sai do mapa. **Leito ocupado não sai de
uso.**

> O número de leitos autorizados por capacidade é definido pela gestão. Acima dele, o cadastro é bloqueado.

## 10. Mensagens comuns

| Mensagem | O que quer dizer | O que fazer |
|----------|------------------|-------------|
| *Escolha um leito livre.* | a internação está sem leito | escolha um leito livre e compatível |
| *Informe o CID-10 principal (ex.: J18.9).* | falta o diagnóstico | informe o CID |
| *Informe o motivo da internação.* | falta o motivo | descreva o quadro e por que internar |
| *A alta não pode ser no futuro.* | a data da alta é depois de agora | use agora ou uma hora anterior |
| *Informe o motivo do bloqueio.* | o bloqueio está sem motivo | escreva o motivo |
| *Descreva o quadro clínico.* | o pedido de vaga está sem o quadro | descreva os sinais e a gravidade |
| *Escolha um dos leitos compatíveis.* | a reserva está sem leito | escolha um leito da lista ou avalie a vaga zero |
| *Informe o motivo da recusa.* | a recusa no destino está sem motivo | escreva o motivo |
| *A data não pode ser no passado.* | a eletiva está com data passada | escolha hoje ou uma data futura |
| *Informe a condição de uso.* | o item *Se necessário* está sem a condição | descreva quando dar |
| *Informe os 12 primeiros dígitos, sem o verificador.* | a faixa de AIH está incompleta | digite os 12 primeiros dígitos |
| *O fim não pode ser menor que o início.* | a faixa de AIH está invertida | corrija o início e o fim |
| *Informe o motivo da reabertura.* | a reabertura do censo está sem motivo | escreva o que será corrigido |
