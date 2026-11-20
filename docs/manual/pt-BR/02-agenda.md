# Agenda

> **Versão:** 1.15.0 · [English](../en/02-agenda.md)

Menu: **Assistência › Agenda**. A agenda é sempre **de um profissional numa unidade**: os blocos de atendimento geram as vagas, e
as marcações ocupam essas vagas.

## 1. Abrir a agenda de um profissional

**Para que serve:** ver as vagas, as marcações e os bloqueios de um profissional numa unidade.

**Quem usa:** quem marca (recepção, médico e cirurgião-dentista) e quem só consulta: enfermagem e o médico regulador.

**Passo a passo:**

1. Em **Profissional**, escolha o profissional; em **Unidade**, a unidade. Sem os dois, a tela pede *Escolha o profissional e a
   unidade*.
2. Se quiser, filtre por **Especialidade** e por **Tipo de marcação**.
3. Escolha a visão **Dia** ou **Semana**. As setas mudam o período, e **Hoje** volta para hoje. Na semana, clicar no dia abre
   aquele dia.
4. O minicalendário do painel ao lado também leva a qualquer data. Os dias com marcação aparecem destacados.

**Os cartões do alto:**

| Cartão | O que mostra |
|--------|--------------|
| Marcações | quantas há no dia ou na semana |
| **Ocupação** | as vagas ocupadas sobre as ofertadas, em porcentagem; a meta é 85% |
| **Encaixes** | os marcados fora das vagas |
| **Faltas** | as faltas registradas no período |

**Legenda das cores:** consulta marcada, procedimento ou exame, encaixe, vaga livre, falta registrada, e bloqueio, férias ou
afastamento.

No painel ao lado ficam as **Próximas marcações · hoje**, a **Agenda recorrente** (os blocos) e os **Bloqueios no período**.

## 2. Marcar um paciente

**Para que serve:** reservar um horário para o paciente.

**Quem usa:** recepção, médico e cirurgião-dentista.

**Passo a passo:**

1. Clique numa **vaga livre** da grade, ou em **Nova marcação**. A vaga já preenche a data, a hora e o tipo.
2. Em **Paciente**, digite parte do nome ou do CPF para filtrar e escolha a pessoa.
3. Confira **Data e hora** e o **Tipo**: consulta, procedimento ou retorno.
4. Marque **Encaixe** só se o horário não é uma vaga da agenda. Sem encaixe, o horário precisa ser uma vaga livre.
5. Em **Programa** (opcional), escolha o programa de saúde em que o paciente é acompanhado. Com o programa, a marcação cumpre a
   ação programática (a consulta programada) do paciente.
6. Escreva uma **Observação**, se precisar, e salve.

A confirmação aparece como *Marcação feita* ou *Encaixe marcado*.

> Vagas no passado não podem ser marcadas.

## 3. Cancelar, registrar falta ou pedir transporte

Clique na marcação na grade. A gaveta mostra o **Paciente**, **Quando**, o **Tipo** e a **Situação** (agendado, confirmado,
realizado, cancelado ou faltou). Enquanto está agendada ou confirmada:

- **Cancelar marcação:** libera a vaga (*Marcação cancelada*).
- **Registrar falta:** o paciente não veio (*Falta registrada*). A falta conta no cartão **Faltas**.
- **Pedir transporte:** abre o pedido de transporte sanitário para a consulta, já com o paciente e a marcação. Aparece para quem
  pede transporte (recepção, coordenação de enfermagem e assistência social).

## 4. Montar a agenda recorrente (blocos)

**Para que serve:** definir quando o profissional atende toda semana. **Sem bloco não há vagas: só encaixe.**

**Quem usa:** recepção, médico e cirurgião-dentista.

**Passo a passo:**

1. No painel **Agenda recorrente**, clique em **+ Bloco**.
2. Escolha o **Dia da semana** e o **Tipo de atendimento** (ou nenhum).
3. Informe o **Início**, o **Fim** e a **Duração da vaga (min)**, de 5 a 240 minutos.
4. Em **Vale a partir de**, a data em que o bloco começa a gerar vagas.
5. Salve (*Bloco criado*).

**Mudar um bloco:** o bloco não se edita. Encerre-o e crie outro, e o histórico da agenda fica.

1. No bloco, clique em **Encerrar**.
2. Informe o **Último dia do bloco**. Depois dessa data o bloco não gera vagas, e as marcações já feitas ficam.

**Regras que o sistema confere:**

- o fim precisa ser depois do início, e o bloco não pode ser menor que uma vaga;
- o bloco precisa caber no horário de funcionamento da unidade;
- o profissional não pode ter dois blocos que se sobrepõem no mesmo dia.

## 5. Bloquear a agenda

**Para que serve:** fechar horários por folga, reunião, capacitação ou unidade fechada.

**Quem usa:** recepção, médico e cirurgião-dentista.

**Passo a passo:**

1. No painel **Bloqueios no período**, clique em **+ Bloqueio**.
2. Em **Para quem**, escolha o profissional nesta unidade ou **A unidade inteira**.
3. Informe o **Início** e o **Fim**.
4. Escolha o **Motivo**: folga, reunião, capacitação, unidade fechada ou outro. Se quiser, uma **Descrição** (por exemplo,
   *reunião de equipe*).
5. Clique em **Bloquear** (*Agenda bloqueada*).

Para desfazer, clique em **Remover** no bloqueio. As vagas do período voltam para a agenda.

> **Férias e afastamentos aprovados no RH já fecham a agenda;** não precisam de bloqueio.

## 6. Mensagens comuns

| Mensagem | O que quer dizer | O que fazer |
|----------|------------------|-------------|
| *Selecione o paciente.* / *Informe a data e a hora.* | falta o paciente ou o horário | preencha o campo |
| *Esse horário não é uma vaga da agenda do profissional nessa unidade. Escolha uma vaga livre ou marque como encaixe.* | o horário não está em nenhum bloco | escolha uma vaga livre ou marque **Encaixe** |
| *Essa vaga já está ocupada…* | outra pessoa marcou o horário antes | escolha outra vaga ou marque como encaixe |
| *A agenda está bloqueada nesse horário (…)* | há um bloqueio no período | escolha outro horário, ou remova o bloqueio se ele não vale mais |
| *O profissional está afastado (…)* | há férias ou afastamento do RH no período | marque com outro profissional ou em outra data |
| *A … não funciona nesse horário.* | a unidade está fechada no horário | escolha um horário de funcionamento |
| *O fim precisa ser depois do início.* | no bloco ou no bloqueio, o fim veio antes | corrija os horários |
| *Use de 5 a 240 minutos.* | a duração da vaga está fora do limite | use uma duração entre 5 e 240 minutos |
| *O bloco é menor que uma vaga.* | o bloco não comporta nem uma vaga | aumente o bloco ou diminua a duração |
| *O profissional já tem um bloco na …* | o bloco se sobrepõe a outro | encerre o bloco antigo ou mude o horário |
| *Esta unidade está fora do seu acesso.* | o seu perfil não vale para a unidade | peça o perfil na unidade, se a sua função precisa |
