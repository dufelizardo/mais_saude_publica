# Regulação

> **Versão:** 1.15.0 · [English](../en/05-regulacao.md)

Menu: **Assistência › Regulação**. É a Central de Regulação do Acesso: os encaminhamentos para consulta especializada, exame ou
procedimento, a fila por prioridade e a vaga na unidade que executa.

**O caminho de uma solicitação:**

1. o médico ou o dentista **solicita**;
2. a solicitação entra na **fila**;
3. o regulador **autoriza** com vaga, **devolve** para complementar ou **nega**;
4. a unidade executante **agenda**;
5. o paciente é **atendido**, com a contrarreferência, ou **falta**.

**Quem usa:**

| Perfil | O que faz |
|--------|-----------|
| Médico e cirurgião-dentista | solicitam, complementam e acompanham |
| Médico regulador | regula a fila e mantém o catálogo |
| Recepção | acompanha o andamento e agenda na unidade executante |

A tela tem três abas:

- **Fila;**
- **Solicitações;**
- **Procedimentos regulados.**

**Os cartões do alto:**

- **Na fila;**
- **Vermelho ou amarelo** aguardando na fila;
- **Devolvidas** aguardando complemento;
- **Autorizadas** com vaga marcada.

## 1. Solicitar um encaminhamento

**Quem usa:** médico e cirurgião-dentista. O botão **Encaminhar** de dentro do atendimento (módulo 03) abre a mesma gaveta, já
com o paciente e a unidade.

1. Clique em **Nova solicitação**.
2. Em **Paciente**, digite parte do nome ou do CPF e escolha a pessoa.
3. Escolha o **Procedimento** (do catálogo de procedimentos regulados) e a **Unidade que solicita**.
4. Informe o **CID-10** (por exemplo, *I20.9*) e a **Matrícula de quem solicita**.
5. Escolha a **Prioridade proposta**:

   | Cor | Significado |
   |-----|-------------|
   | Vermelho | emergência |
   | Amarelo | urgente |
   | Verde | não urgente |
   | Azul | eletivo |

   O regulador pode reclassificar enquanto a solicitação estiver na fila.
6. Escreva a **Justificativa clínica**, com pelo menos 10 caracteres: a história, os achados e o que se espera do
   encaminhamento.
7. Salve (*Solicitação registrada*).

## 2. A fila

**Para que serve:** a ordem de atendimento da Central, **por prioridade e, dentro dela, pela hora do pedido**. Cada regulador vê
só as unidades do seu escopo.

Cada linha mostra a **Posição**, a **Prioridade**, o paciente, o procedimento, quem solicitou e a **Espera**. A solicitação
ligada a uma emergência em curso aparece marcada com o nome dela. Filtre por procedimento e por prioridade.

## 3. Regular (médico regulador)

1. Na fila, clique em **Analisar**. A gaveta mostra o CID-10, a unidade solicitante, quem solicitou e quando, a justificativa
   clínica e o **Histórico**.
2. Em **Decisão da regulação**, escolha a **Decisão**:
   - **Autorizar com vaga** (ver abaixo);
   - **Devolver para complementar:** diga o que falta para regular;
   - **Negar:** informe o motivo da negativa;
   - **Reclassificar prioridade:** escolha a **Nova prioridade** (diferente da atual) e o motivo.
3. Informe a **Matrícula de quem regula** e confirme.

> **Quem solicitou não autoriza nem nega a própria solicitação.**

**Autorizar com vaga:**

1. Escolha a **Unidade executante**. A gaveta mostra a **referência da rede** do procedimento e avisa quando a escolha está
   **fora da referência**.
2. Se o procedimento exige uma capacidade (por exemplo, endodontia num CEO), só a unidade com ela é aceita.
3. Informe **Quem vai atender** (opcional) e a **Data e hora da vaga**. A gaveta mostra as vagas livres dos próximos 30 dias.
   - **Com a matrícula** de quem vai atender, a autorização já agenda.
   - **Sem ela**, a unidade executante agenda depois.
   - Sem vaga livre, só como encaixe pela Agenda.
4. **PPI:** a gaveta mostra a cota pactuada do procedimento. Sem saldo, marque **Autorizar como extracota** e escreva a
   **Justificativa da extracota**.
5. Se quiser, uma **Orientação ao paciente** (por exemplo, *levar os exames anteriores*).
6. Confirme: *Solicitação autorizada e agendada* ou *Solicitação autorizada*.

## 4. Complementar uma solicitação devolvida

**Quem usa:** quem solicita (médico e dentista).

1. Na aba **Solicitações**, as devolvidas aparecem com a ação de complementar.
2. Leia o que a regulação pediu, escreva a **Resposta ao regulador** e a **Matrícula de quem complementa**.
3. Salve. A solicitação volta à fila **na posição original**.

## 5. Acompanhar, agendar, registrar o atendimento ou a falta

A aba **Solicitações** mostra o andamento de cada encaminhamento, **sem o dado clínico**. O detalhe abre só para quem solicita
ou regula.

- **Busca e filtro:** por paciente, procedimento ou unidade, e pela situação: na fila, devolvida, autorizada, agendada,
  realizada, faltou, negada ou cancelada.
- **Agendar** (solicitação autorizada, sem agendamento; recepção ou regulador):
  1. informe a **Matrícula de quem vai atender** e a **Data e hora**;
  2. informe a **Matrícula de quem agenda**;
  3. o agendamento entra na agenda de quem vai atender, na unidade executante.
- **Atendido** (solicitação agendada; quem registra consulta ou procedimento):
  1. escreva a **Contrarreferência**, com pelo menos 10 caracteres: o que foi feito, o achado e a conduta para quem continua o
     cuidado;
  2. informe a **Matrícula de quem atendeu** e salve.
- **Faltou:** descreva **O que aconteceu** e informe a **Matrícula de quem registra**. O agendamento é fechado e a solicitação
  termina como falta. **Para atender o paciente, será preciso uma nova solicitação.**
- **Cancelar:** enquanto ainda cabe, com o motivo e a matrícula de quem cancela.

## 6. Procedimentos regulados (catálogo)

**Para que serve:** o que passa pela Central, como consultas de especialidade e exames. **O procedimento de acesso direto é
agendado na própria unidade** e não entra aqui.

**Quem usa:** o médico regulador.

1. Clique em **+ Procedimento**.
2. Informe o **Nome** (por exemplo, *Consulta em cardiologia*) e o **Tipo**: consulta especializada, exame ou procedimento.
3. Se quiser, a **Capacidade exigida da executante**, do Modelo Operacional. Assim a autorização só aceita executante com ela.
4. Deixe **Em uso** marcado. Fora de uso, ele não aparece na nova solicitação, e as solicitações antigas continuam.

## 7. Mensagens comuns

| Mensagem | O que quer dizer | O que fazer |
|----------|------------------|-------------|
| *Informe o CID-10 (ex.: I10, E11.9).* | a solicitação está sem o CID | informe o código |
| *Descreva a justificativa clínica (pelo menos 10 caracteres).* | a justificativa está curta | descreva a história e o que se espera |
| *A vaga não pode estar no passado.* | a data da vaga já passou | escolha uma vaga futura |
| *Sem saldo na PPI: marque extracota para autorizar.* | a cota pactuada acabou | marque extracota e justifique, ou escolha outra executante |
| *Escolha uma prioridade diferente da atual.* | a reclassificação repetiu a cor | escolha outra prioridade |
| *Diga o que falta para regular.* | a devolução está sem o pedido | escreva o que o solicitante precisa informar |
| *Informe o motivo da negativa.* | a negativa está sem motivo | escreva o motivo |
| *Responda ao que o regulador pediu.* | o complemento está vazio | responda ao pedido da regulação |
| *Escreva a contrarreferência (pelo menos 10 caracteres).* | o atendimento está sem a contrarreferência | descreva o que foi feito e a conduta |
| *Matrícula não encontrada.* | a matrícula não é de um profissional | confira o número |
| *Esta solicitação é de uma unidade fora do seu acesso.* | o seu perfil não vale para a unidade | peça o perfil, se a sua função precisa |
