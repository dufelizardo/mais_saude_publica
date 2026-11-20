# Auditoria

> **Versão:** 1.15.0 · [English](../en/37-auditoria.md)

Menu: **Administração › Auditoria**. É a trilha do que aconteceu no sistema:

- as **leituras** do detalhe de dado de saúde ou pessoal;
- as **alterações;**
- os **acessos negados;**
- os **logins.**

Também mostra os **alertas** de padrões suspeitos.

**Quem usa:** quem tem a permissão da auditoria. Ela fica **fora de todos os perfis padrão**: quem administra o sistema não audita a
si mesmo, e a permissão é concedida à parte, a quem a gestão designar. O número no item do menu mostra os alertas abertos.

> **O evento não guarda o conteúdo:** nem os dados enviados, nem o texto clínico. O registro apontado continua no sistema, com o
> seu histórico.

## 1. Os eventos

**Os cartões:**

- os **Eventos**, do mais recente ao mais antigo;
- as **Leituras de dado de saúde:** os detalhes abertos (prontuário, atendimento, paciente…);
- as **Alterações:** as criações, as alterações, as retificações e as revogações;
- os **Acessos negados.**

**Filtrar:**

- pelo **CPF de quem fez;**
- pelo paciente (nome, CPF ou Cartão SUS);
- pela **Ação**, pelo **Resultado** (permitido ou negado) e pela **Unidade;**
- pelo período, até a data **Até.**

**Limpar filtros** volta ao começo.

Cada evento mostra **Quando**, **Quem**, **O quê**, o paciente, a unidade e o resultado. Ao abrir, aparecem:

- o **Motivo** (no acesso justificado);
- a **Origem (IP)**, a **Resposta** e a **Rota;**
- o **Registro** apontado.

Use **Tudo desta pessoa** ou **Tudo deste paciente** para ver a trilha inteira de um ou de outro.

**Exportar:** o filtro atual sai em CSV (abre no Excel). Filtrado por paciente, serve de relatório de acessos para o titular dos
dados (LGPD). A própria consulta e a exportação ficam registradas na auditoria.

## 2. Alertas

**Para que serve:** os padrões suspeitos achados na trilha. Cada alerta fica aberto até alguém concluir, com parecer.

**Os tipos:**

| Alerta | O que indica |
|--------|--------------|
| Recusas seguidas | muitos acessos recusados (sem permissão ou fora do escopo) em pouco tempo |
| Login recusado em sequência | muitas tentativas de login erradas: pode ser alguém tentando descobrir a senha |
| Leitura em massa | dados de muitos pacientes diferentes abertos em pouco tempo |
| Leitura de madrugada | leituras de dado de saúde de madrugada, fora de unidade que funciona 24 horas |
| Acesso justificado | prontuário aberto sem vínculo assistencial: a supervisão revisa o motivo |

**Os cartões:**

- os **Abertos**, aguardando análise;
- a **Severidade alta:** abertos e avisados por e-mail;
- os detectados nos **Últimos 7 dias;**
- os **Procedentes**, confirmados na análise.

**Analisar:**

1. Filtre pela situação, pelo tipo e pela severidade (alta ou média), e abra o alerta.
2. Confira os eventos: o primeiro, o último e quantos.
3. Escolha a **Conclusão:**
   - **Procedente:** o acesso não se justifica e pede providência;
   - **Improcedente:** havia motivo.
4. Escreva o **Parecer** (o que foi verificado e a providência) e clique em **Concluir análise**.

**Verificar agora** procura alertas novos na hora.

## 3. Mensagens comuns

| Situação | O que quer dizer | O que fazer |
|----------|------------------|-------------|
| *Nenhum evento* | os filtros não acharam evento | ajuste os filtros |
| *Nada aguardando análise no seu escopo.* | não há alerta aberto nas suas unidades | nada a fazer |
| *Não foi possível exportar a auditoria.* | a exportação falhou | tente de novo |
