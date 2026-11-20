# Atendimentos

> **Versão:** 1.15.0 · [English](../en/03-atendimentos.md)

Menu: **Assistência › Atendimentos**. É onde o cuidado acontece, do agendamento ao prontuário. O atendimento reúne a triagem, a
consulta, os procedimentos, a checagem de medicação e a evolução de enfermagem. A tela tem quatro abas:

- **Atendimentos;**
- **Agendamentos;**
- **Medicação;**
- **Prontuário.**

## 1. A aba Atendimentos

**Para que serve:** ver os atendimentos do dia e abrir um deles para registrar o cuidado.

**Quem usa:** recepção, médico, enfermagem e cirurgião-dentista. Quem só lê o prontuário também vê a lista.

**Os cartões do alto:**

| Cartão | O que mostra |
|--------|--------------|
| **Atendimentos hoje** | os do dia e quantos estão em andamento |
| **Aguardando atendimento** | os atendimentos ainda não iniciados |
| **Agendamentos hoje** | as marcações do dia |
| **Risco laranja ou vermelho** | os pacientes graves em atendimentos abertos |

**Como ler a lista:**

- cada linha traz a data e a hora, o paciente, o tipo, a unidade e o setor, o risco da triagem, os registros feitos e o status:
  aguardando, em andamento ou concluído;
- **via agendamento** indica que o atendimento nasceu de uma marcação;
- **sem triagem** indica que ninguém classificou o risco ainda.

Use **Buscar paciente…** e o filtro de status para achar um atendimento, e **Abrir** para entrar nele.

## 2. Começar um atendimento

Há três caminhos, pelos botões do alto:

| Botão | Quando usar |
|-------|-------------|
| **Acolhimento** | **demanda espontânea:** o paciente chegou sem marcação. Você encontra o paciente, inicia o atendimento e segue direto para a triagem. |
| **Iniciar atendimento** | um atendimento novo, com ou sem marcação |
| **Agendar** | marcar para depois (ver a aba Agendamentos) |

**Quem usa:** recepção, médico, enfermagem e cirurgião-dentista.

**Passo a passo do Acolhimento:**

1. Em **Buscar paciente por**, escolha **CPF** ou **Cartão SUS** e digite o número. Se a pessoa não tem cadastro, use
   **Cadastrar paciente**.
2. Confira o paciente e a idade.
3. Escolha a **Unidade** e, se quiser, o **Setor**.
4. Informe a **Matrícula de quem acolhe** e o **Tipo**: consulta, urgência ou internação.
5. Confirme. O atendimento abre, pronto para a triagem.

**No Iniciar atendimento,** além disso:

- **Agendamento de origem:** escolha a marcação do paciente ou **Sem agendamento (demanda espontânea)**. A marcação escolhida
  passa a *Realizado* sozinha.
- **Especialidade** (opcional): sem ela, vale a da marcação.
- **Local de realização** (opcional): sem ele, o atendimento é **na unidade**. Pode ser num **Ponto** de atendimento, no
  **Domicílio** ou em **Outro endereço**, com a descrição do local (por exemplo, *Praça da Sé, em frente à igreja*) e, se
  quiser, a latitude e a longitude.

## 3. Dentro do atendimento

O atendimento abre numa gaveta larga. No alto, o **fluxo do atendimento**: agendamento, atendimento, triagem, consulta,
procedimento e evolução. Embaixo, os dados (tipo, data e hora, profissional, unidade, setor e local) e as ações:

| Ação | O que faz | Quem vê |
|------|-----------|---------|
| **Editar atendimento** | corrige os dados do atendimento | recepção, médico, enfermagem, dentista |
| **Concluir atendimento** | encerra o atendimento | os mesmos |
| **Ver prontuário** | abre a aba Prontuário do paciente | quem lê o prontuário |
| **Encaminhar** | abre o pedido na **Regulação** | médico e dentista |
| **Pedir exames** | abre o pedido no **Laboratório e imagem** | médico e enfermeiro |
| **Internar** / **Pedir vaga** | leva a **Leitos**, para internar na unidade ou pedir vaga em outra | médico, enfermagem e o NIR / médico |

> Com registro clínico, o paciente do atendimento não pode mais ser trocado.

Cada registro tem a sua seção, com o botão **+**, e os registros já feitos aparecem com o botão **Retificar** (ver o item 9).

## 4. Triagem e classificação de risco

**Quem usa:** enfermeiro e coordenação de enfermagem.

1. No atendimento, em **Triagem**, clique em **+ Triagem**.
2. Escolha a **Classificação de risco**:

   | Cor | Significado |
   |-----|-------------|
   | Azul | não urgente |
   | Verde | pouco urgente |
   | Amarelo | urgente |
   | Laranja | muito urgente |
   | Vermelho | emergência |

3. Se tiver, registre os **Sinais vitais**: PA (mmHg, por exemplo 120/80), temperatura (°C), SpO₂ (%), FC (bpm) e peso (kg).
4. Escreva as **Observações** e salve (*Triagem registrada*).

O risco aparece na lista. Laranja e vermelho contam no cartão do alto.

## 5. Consulta

**Quem usa:** médico.

1. Em **Consultas**, clique em **+ Consulta**.
2. Escolha o **Tipo de consulta**: primeira consulta, retorno ou urgência.
3. Preencha:
   - a **Queixa principal**;
   - o **Diagnóstico** (inclua o CID quando houver);
   - o **Receituário**;
   - os **Exames solicitados**;
   - o **Retorno**.
4. Salve (*Consulta registrada*).

O receituário da consulta é a **prescrição** que a enfermagem checa na aba Medicação.

## 6. Procedimento

**Quem usa:** médico, enfermagem e saúde bucal.

1. Em **Procedimentos do atendimento**, clique em **+ Procedimento**.
2. Escolha o **Procedimento** do catálogo: os da especialidade do atendimento e os de uso geral. Se não houver, use **Outro
   (texto livre)** e escreva o tipo (por exemplo, *Curativo*, *Glicemia capilar*).
3. Informe a **Data**: a do dia, ou a prevista, se o procedimento está agendado. Escreva a **Descrição** e salve.

**Desfecho:** o procedimento previsto recebe o desfecho **uma única vez**, pelo botão **Registrar desfecho**:

- **Realizado**, com a data em que foi feito;
- **Cancelado**, com o motivo.

A data prevista fica guardada.

## 7. Checagem de medicação

**Para que serve:** registrar se o medicamento prescrito foi dado. **O que foi administrado baixa o estoque da unidade.**

**Quem usa:** enfermeiro, técnico de enfermagem e coordenação de enfermagem.

1. No atendimento, em **Medicação**, clique em **+ Checagem**. Também dá pela aba **Medicação** (item 10).
2. Escolha a **Prescrição** (o receituário da consulta) e o **Medicamento**.
3. Em **Checagem**, escolha:
   - **Administrado:**
     - o **Lote**, do estoque da unidade, com saldo e dentro da validade;
     - a **Dose** (por exemplo, *1 g*, *500 mg*, *10 gotas*) e a **Via**: oral, sublingual, intramuscular, intravenosa,
       subcutânea, tópica, inalatória, retal ou outra;
     - a **Quantidade retirada do lote**, em unidades do lote (ampolas, comprimidos, frascos).
   - **Não administrado:** o **Motivo**: recusa do paciente, paciente ausente, medicamento em falta, suspenso pelo médico ou
     outro. Com *Outro*, descreva na observação.
4. Informe a **Matrícula de quem checou** e salve.

> Sem consulta com prescrição não há o que checar: *Registre a consulta com a prescrição para checar a medicação.*

## 8. Evolução de enfermagem

**Quem usa:** enfermeiro e coordenação de enfermagem.

1. Em **Evolução de enfermagem**, clique em **+ Evolução**.
2. Escreva a **Descrição** e salve (*Evolução registrada*).

## 9. Corrigir um registro: retificação

**Registro clínico não é editado.** Triagem, consulta, procedimento, evolução, checagem e avaliação odontológica são corrigidos
por **retificação**:

1. No registro, clique em **Retificar**.
2. Corrija o que precisa e escreva o **Motivo da retificação**: o que está sendo corrigido e por quê.
3. Salve. A retificação grava uma **nova versão**. A anterior continua no prontuário, marcada como retificada.

Na checagem de medicação, se a checagem anterior baixou estoque, a quantidade volta ao lote antes da nova baixa.

> Só se retifica a versão vigente. A versão já retificada mostra qual a substituiu.

## 10. A aba Medicação

**Para que serve:** a lista dos atendimentos com medicamento a checar, para a enfermagem trabalhar em fila.

1. Filtre pela unidade e escolha **Em aberto** ou **Todos**.
2. Em cada atendimento, **Registrar checagem** abre a mesma gaveta do item 7, e **Abrir atendimento** abre o atendimento.

A checagem aparece na lista quando a consulta do atendimento é registrada.

## 11. A aba Agendamentos

**Para que serve:** a lista das marcações. **O agendamento é opcional:** o atendimento pode começar direto.

- Filtre por paciente. Em cada marcação, **Iniciar** abre o atendimento a partir dela. O lápis edita a marcação.
- **Agendar** cria uma marcação com o **Paciente**, a **Data e hora**, a **Matrícula do profissional**, o **Status** e a
  **Observação**.

> Para marcar dentro das vagas do profissional, use a tela **Agenda** (módulo 02). Ela confere os blocos, os bloqueios e os
> afastamentos.

## 12. A aba Prontuário

**Para que serve:** a leitura consolidada do paciente: os atendimentos com as triagens, as consultas, os procedimentos, as
evoluções e a medicação vigentes.

**Quem usa:** quem lê o prontuário: médico, enfermagem, farmácia, saúde bucal, psicologia, assistência social, vigilância
epidemiológica e os médicos da regulação e do diagnóstico.

1. Escolha o paciente na lista, ou busque pelo **CPF** ou pelo **Cartão SUS** e clique em **Buscar**.
2. O prontuário mostra o cabeçalho do paciente (com **Abrir em Pacientes**, **Agendar** e **Iniciar atendimento**) e, em
   seções, as **Internações**, as **Ocorrências de urgência** (com a **Ficha do APH**), os **Exames laboratoriais** e cada
   atendimento com os seus registros.

**Quem acessou os dados deste paciente:** para quem tem a permissão da auditoria, o atendimento mostra quem leu o prontuário e
quando, incluindo os acessos negados. **Ver tudo na auditoria** abre a trilha completa.

## 13. Saúde bucal no atendimento

Para o cirurgião-dentista e o técnico em saúde bucal, o atendimento tem também:

- **Avaliação odontológica** (dentista):
  - a anamnese e o exame clínico e os achados;
  - o diagnóstico e as condições bucais;
  - o **Risco em saúde bucal** e a **Necessidade de tratamento**;
  - o plano inicial.
- **Procedimentos odontológicos**, com o **Dente (FDI)** e as **Faces**. Os permanentes vão de 11 a 48 e os decíduos de 51 a 85.
  O procedimento realizado com dente ou face grava o **Odontograma**.
- **Plano de tratamento** e **Prótese** (dentista).
- **Odontograma:** o mapa dos dentes do paciente, com o histórico.

## 14. Mensagens comuns

| Mensagem | O que quer dizer | O que fazer |
|----------|------------------|-------------|
| *Busque o paciente pelo CPF ou pelo Cartão SUS.* | o acolhimento ainda não tem paciente | busque e escolha o paciente |
| *Selecione a unidade.* / *Informe a matrícula.* | falta a unidade ou a matrícula | preencha o campo |
| *Selecione a classificação de risco.* | a triagem está sem a cor | escolha a classificação |
| *Selecione o tipo de consulta.* | a consulta está sem o tipo | escolha primeira consulta, retorno ou urgência |
| *Escolha o procedimento do catálogo ou informe o tipo.* | o procedimento está vazio | escolha no catálogo ou use **Outro (texto livre)** |
| *Informe o dente (FDI).* / *Marque a face (ou as faces).* | o procedimento odontológico pede o dente e a face | preencha o dente e marque as faces |
| *Informe quando foi realizado.* / *Informe por que foi cancelado.* | o desfecho está incompleto | informe a data ou o motivo |
| *Selecione o lote.* / *Informe a dose.* / *Selecione a via.* | a checagem do administrado está incompleta | preencha o lote, a dose e a via |
| *A quantidade precisa ser maior que zero.* | a quantidade retirada está zerada | informe quantas unidades saíram do lote |
| *Com motivo “Outro”, descreva na observação.* | o não administrado com *Outro* pede a explicação | descreva o motivo |
| *Informe o motivo da retificação.* | a retificação precisa do porquê | escreva o que foi corrigido e por quê |
| *Esta … já foi retificada pela versão …* | você tentou retificar uma versão antiga | retifique a versão vigente |
| *O agendamento informado é de outro paciente.* | a marcação escolhida não é deste paciente | escolha a marcação certa ou **Sem agendamento** |
