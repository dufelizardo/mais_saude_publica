# Pacientes

> **Versão:** 1.15.0 · [English](../en/01-pacientes.md)

Menu: **Assistência › Pacientes**. É o cadastro único de quem é atendido na rede: os dados pessoais, o endereço e, num painel ao
lado, o resumo clínico, os programas, a vacinação e os documentos.

## 1. Encontrar um paciente

**Para que serve:** achar a pessoa antes de agendar, atender ou conferir o cadastro.

**Quem usa:** quem atende ou acompanha pacientes: recepção, médico, enfermagem, farmácia, laboratório, regulação, saúde bucal,
saúde mental, assistência social, vigilância epidemiológica e gestor.

**Passo a passo:**

1. Digite o nome ou o CPF em **Buscar por nome ou CPF…**. A lista filtra enquanto você digita.
2. Se precisar, use os filtros ao lado:
   - **Programa:** só quem é acompanhado no programa, nas unidades do seu acesso;
   - **Faixa etária:** 0–11, 12–17, 18–59 ou 60+ anos;
   - **Status:** ativo ou inativo.
3. Escolha como ver o resultado: **Lista** (tabela com nome, CPF, idade, sexo, Cartão SUS e status) ou **Cartões**.
4. Clique no paciente para abrir o painel de detalhes, à direita.

Os cartões do alto mostram os **pacientes ativos**, os **novos cadastros do mês** e quantos estão **em programas de saúde**.

**Exportar:** o botão **Exportar** baixa a lista filtrada em CSV (planilha). O arquivo traz o nome, o CPF, o Cartão SUS, a
data de nascimento, a idade, o sexo, o telefone, o e-mail, a cidade, a UF, o status e a data de cadastro.

> O arquivo exportado tem dado pessoal. Guarde-o só enquanto precisar e não o envie por canais abertos.

## 2. Cadastrar ou editar um paciente

**Para que serve:** registrar uma pessoa nova ou corrigir os dados de quem já está cadastrado.

**Quem usa:** recepção e agente comunitário de saúde. Quem não tem essa permissão não vê o botão **Novo paciente** nem o lápis
de edição.

**Passo a passo:**

1. Clique em **Novo paciente** ou, com o paciente aberto no painel, no lápis (**Editar**).
2. Preencha os campos. Os marcados com **\*** são obrigatórios:
   - **Nome completo \***;
   - **CPF \*** (os pontos e o traço entram sozinhos);
   - **Cartão Nacional de Saúde (CNS)**, opcional;
   - **Data de nascimento \***;
   - **Sexo \***: masculino, feminino ou ignorado;
   - **Telefone \*** e **E-mail** (opcional).
3. No **Endereço**, digite o **CEP \***. O sistema busca o logradouro, o bairro, a cidade e o estado. Confira e complete o
   **Número \*** e o **Complemento**.
4. Deixe **Ativo** marcado. Desmarque quando o cadastro não deve mais aparecer como paciente ativo da rede.
5. Clique em **Salvar**. **Cancelar** fecha a janela sem gravar.

**Dicas:**

- Antes de cadastrar, busque pelo CPF para não criar um cadastro duplicado.
- Se o CEP não for encontrado, preencha o endereço à mão.

## 3. O painel do paciente

Ao clicar num paciente, o painel da direita mostra o nome, a idade, o sexo, o CPF e o CNS, e cinco abas.

### Resumo

- **Sinais vitais · última triagem:** pressão arterial, frequência cardíaca, temperatura e peso da última triagem.
- **Equipe de referência:** a equipe de saúde da família que cuida do paciente (ver o item 4).
- **Programas vinculados:** os programas em que ele está, com a unidade, a equipe, a data de entrada e a situação. **Atrasado**
  indica acompanhamento fora do prazo.
- **Próximos atendimentos:** os agendamentos futuros. O link **Agenda →** abre os agendamentos do paciente na tela **Atendimentos**.
- **Histórico recente:** os últimos registros clínicos. **Ver histórico completo** abre a aba Histórico.

### Histórico

A linha do tempo dos registros clínicos (triagens, consultas, evoluções e procedimentos), do mais novo para o mais antigo.
**Ver prontuário completo** abre o prontuário na tela **Atendimentos**.

### Programas

Os programas vigentes e o histórico dos encerrados, as linhas de cuidado e as ações programáticas (por exemplo, uma consulta ou
um exame previsto, com a data). Quem acompanha programas (médico, enfermagem, saúde bucal, psicologia e assistência social) tem
três ações em cada programa:

| Ação | O que pede | Para quê |
|------|------------|----------|
| **Marcar faltoso** | o motivo | o paciente deixou de comparecer |
| **Voltar a ativo** | o motivo | o faltoso voltou ao acompanhamento |
| **Saída** | o motivo da saída, a descrição e, se quiser, a data (não pode ser no futuro) | encerrar o acompanhamento; **alta** conclui, os outros motivos encerram |

A saída fica no histórico; nada se apaga. O botão **Inscrever em um programa** leva à tela **Programas de Saúde**. No programa de
saúde mental, o link **Abrir em Saúde Mental** leva à tela própria.

Os programas **sensíveis** (marcados em roxo) de outra unidade não aparecem; o painel diz só quantos são.

### Vacinação

A caderneta de vacinação do paciente. Quem vacina (enfermagem) registra a dose por aqui. Quem não lê o prontuário nem vacina vê
o aviso de que a caderneta é dado de saúde.

### Anexos

Os documentos do paciente, como exames de fora, laudos e termos assinados (ver o item 5).

## 4. Equipe de referência (adscrição)

**Para que serve:** saber qual equipe é responsável pelo paciente e, quando preciso, ligá-lo a uma equipe à mão.

**Como funciona:**

- **Pelo domicílio:** quem mora num domicílio cadastrado de uma área com equipe já aparece com essa equipe. O link **ver no
  Território** mostra onde.
- **Sem equipe de referência:** o paciente não mora num domicílio cadastrado de área com equipe.
- **Manual:** para quem não mora num domicílio do território (por exemplo, a população em situação de rua) ou escolheu outra
  equipe.

**Quem usa a adscrição manual:** enfermagem e agente comunitário de saúde.

**Passo a passo:**

1. No Resumo, em **Equipe de referência**, clique em **Adscrever manualmente**.
2. Escolha a **Equipe** e escreva o **Motivo**.
3. Salve. Para desfazer, use **Encerrar adscrição manual**, também com o motivo. O paciente volta à equipe do domicílio, se
   houver.

O **Histórico de adscrição** guarda as equipes anteriores.

## 5. Anexar documentos

**Para que serve:** guardar no cadastro um documento do paciente, como um exame de outro serviço, um laudo ou um termo assinado.

**Quem usa:** quem anexa (recepção, médico, enfermagem, psicologia, assistência social e gestor) e quem lê o prontuário, que pode
abrir os arquivos.

> Anexar não dá direito de abrir: quem só anexa vê os documentos que ele mesmo anexou. Abrir os arquivos é de quem lê o
> prontuário.

**Passo a passo:**

1. Na aba **Anexos**, clique em **Anexar documento**.
2. Preencha o **Tipo**, o **Título**, a **Unidade** e, se quiser, a **Data** do documento.
3. Em **Arquivo**, escolha um PDF, JPG ou PNG de até **10 MB**.
4. Clique em **Enviar**.

Na lista de documentos:

- **Baixar:** abre o arquivo.
- **Nova versão:** troca o arquivo e mantém as versões anteriores, que continuam para baixar.
- **Retirar:** tira o documento da lista, com o motivo. Nada se apaga: o documento passa para **Retirados**.

## 6. Paciente sem vínculo: acessar com justificativa

Quando a exigência de vínculo assistencial está ligada no seu ambiente, o prontuário só abre para quem tem vínculo com o
paciente: um atendimento na sua unidade, ou ser o profissional do atendimento ou do agendamento. Sem vínculo, o painel mostra
**Sem vínculo assistencial com este paciente**.

Quando é necessário ver mesmo assim:

1. Clique em **Acessar com justificativa**.
2. Escolha o **Motivo**: **Emergência**, **Continuidade do cuidado** ou **Regulação ou encaminhamento**.
3. Escreva a **Justificativa**, com pelo menos 20 caracteres.
4. Confirme. O acesso vale por **4 horas**, e o painel mostra *Acesso justificado até…*.

> O acesso justificado fica registrado na auditoria, com o motivo e o texto. Use só quando o cuidado exige.

## 7. Mensagens comuns

| Mensagem | O que quer dizer | O que fazer |
|----------|------------------|-------------|
| *Nome é obrigatório.* (e os outros campos com **\***) | falta um campo obrigatório | preencha o campo marcado em vermelho |
| *CEP não encontrado — preencha o endereço manualmente.* | o CEP não está na base de endereços | digite o logradouro, o bairro, a cidade e o estado |
| *Não foi possível salvar o paciente. Tente novamente.* | o cadastro não foi gravado | confira os dados (por exemplo, se o CPF já está cadastrado) e salve de novo |
| *Sem acesso ao prontuário deste paciente.* | o seu perfil ou o vínculo não permite ver os dados clínicos | se for preciso, use o acesso com justificativa (item 6) |
| *Seu acesso não inclui os programas do paciente.* | o seu perfil não vê programas | peça o perfil, se a sua função precisa |
| *O arquivo vai até 10 MB.* | o arquivo é grande demais | reduza o arquivo ou digitalize em resolução menor |
| *Descreva a justificativa com pelo menos 20 caracteres.* | a justificativa está curta | explique melhor o motivo do acesso |
| *A saída não pode ser no futuro.* | a data da saída do programa é depois de hoje | use a data de hoje ou uma anterior |
