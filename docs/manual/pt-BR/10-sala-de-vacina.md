# Sala de Vacina

> **Versão:** 1.15.0 · [English](../en/10-sala-de-vacina.md)

Menu: **Assistência › Sala de Vacina**. Reúne:

- o registro das doses;
- os atrasados para a busca ativa e a cobertura vacinal;
- o estoque de vacinas e os remanejamentos;
- a cadeia de frio e os alertas de lote;
- o calendário vacinal.

**Quem usa:**

| Perfil | O que faz |
|--------|-----------|
| Enfermeiro, técnico de enfermagem e coordenação de enfermagem | registram doses, cuidam do estoque e da cadeia de frio |
| Gestão da imunização: gestor, coordenação de enfermagem e vigilância epidemiológica | o calendário e as metas, as câmaras, a avaliação de lote sob suspeita e os alertas |

**Para começar:** escolha a **Unidade**. Sem ela, a tela pede a unidade para mostrar as doses do dia, os atrasados, o estoque e a
cadeia de frio.

## 1. Registrar uma dose

1. Clique em **Registrar vacinação**. Também dá pela caderneta do paciente (módulo 01, aba Vacinação).
2. Em **Paciente**, busque pelo nome, pelo CPF ou pelo Cartão SUS.
3. Escolha **Aplicada aqui**.
4. Confira a **Unidade** e a **Data e hora** (vazio é agora).
5. Escolha a **Vacina** e a **Dose**, e informe **Quem aplicou (matrícula)**.
6. Escolha o **Lote do estoque** de onde a dose saiu. Se não houver lote aplicável da vacina no estoque da unidade, a gaveta avisa.
7. Se quiser:
   - a **Via** e o **Local de aplicação** (por exemplo, deltoide direito ou esquerdo);
   - a **Estratégia**: rotina, campanha, bloqueio, intensificação ou especial (CRIE);
   - **Gestante**.
8. Dose fora do calendário pede a **Justificativa (dose fora do calendário)**.
9. Clique em **Registrar**.

A dose aparece em **Doses do dia**, com a hora, o paciente, a vacina, o lote e quem aplicou.

**Transcrever da caderneta:** para a dose aplicada em outro lugar (por exemplo, na maternidade):

1. escolha **Transcrita da caderneta**;
2. informe a **Data da dose**, a vacina e a dose;
3. informe **Onde foi aplicada** (por exemplo, *Maternidade Municipal*), o **Lote** e o **Fabricante**.

A transcrição não baixa estoque.

> **Frasco multidose:** o frasco se abre na primeira dose aplicada. A aba Estoque mostra quanto resta e até quando vale.

## 2. A caderneta do paciente

A caderneta (aqui ou no módulo 01) mostra o **Calendário** do paciente, com as doses previstas e a situação. **Mostrar futuras e
fora da idade** amplia a lista. Embaixo ficam as **Doses registradas**. Em cada dose:

- **Retificar:** corrige um dado, com o **Motivo da retificação**.
- **Anular:** só para o registro feito por engano (paciente errado, dose que não houve), com o motivo.
- **Notificar ESAVI:** abre a notificação do evento adverso pós-vacinação, que vai para a Vigilância.

**Contraindicação:** a dose contraindicada por ESAVI só volta ao calendário com o parecer da reavaliação no CRIE. Use **Revogar
(CRIE)** e escreva o **Parecer da reavaliação no CRIE**. A revogação é da reavaliação no CRIE (médico).

## 3. Atrasados e busca ativa

A aba **Atrasados** lista os adscritos às equipes da unidade com alguma dose do calendário atrasada, o atraso mais antigo
primeiro.

1. Filtre pela **Equipe** e pela **Microárea**.
2. Cada linha mostra a idade, a equipe e a microárea, onde mora e as **Doses atrasadas**.
3. **Imprimir lista** abre a lista de busca ativa para imprimir e entregar ao agente comunitário.

## 4. Cobertura vacinal

A aba **Cobertura** mostra, para a unidade, a equipe ou a microárea:

- por vacina: a coorte, as vacinadas, a cobertura, a meta, a homogeneidade e o abandono;
- no resumo: a **Homogeneidade entre vacinas**, as **Vacinas na meta** e as **Vacinas com abandono alto (10% ou mais)**.

## 5. Estoque de vacinas

**Os cartões:**

- as **Doses aplicadas no mês;**
- os **Frascos abertos agora;**
- a **Perda técnica no mês**, de frasco vencido;
- a **Perda física no mês.**

**Entrada de lote:**

1. Clique em **Entrada de lote**.
2. Informe a **Vacina**, o **Número do lote**, o **Fabricante** (opcional) e a **Validade**.
3. Informe as **Doses recebidas** e a **Câmara**. Na câmara, o lote fica sob suspeita se uma leitura sair da faixa.
4. Clique em **Registrar**.

**Em cada lote:**

- **Perda:** as **Doses perdidas** e o **Motivo**: quebra do frasco, vencimento do lote, falha do equipamento, falta de energia,
  problema no transporte, excursão de temperatura ou outro;
- **Ajuste:** o **Saldo contado**, comparado ao saldo no sistema, com a **Justificativa**;
- **Remanejar:** item 6;
- **Livro:** o extrato do lote.

As marcas da situação são: **Vencido**, **Vence em 30 dias** e **Suspenso na rede (ESAVI)**.

**Frascos multidose abertos:** a vacina e o lote, a hora em que abriu, as doses que restam e o prazo de validade do frasco
aberto.

## 6. Remanejamentos entre salas

**Enviar:**

1. No lote do estoque, clique em **Remanejar**.
2. Escolha a **Sala de vacina de destino** e informe as **Doses**.
3. Registre. As doses saem do estoque agora e ficam em trânsito até o destino receber ou recusar.

**Receber (no destino):** em **A receber**:

- **Receber:** informe a **Temperatura da caixa na chegada (°C)** e, se quiser, a câmara. Fora de 2 a 8 °C, o lote entra sob
  suspeita e espera a avaliação da coordenação.
- **Recusar:** com o motivo. As doses voltam ao estoque da origem.

A origem pode **Cancelar** enquanto o remanejamento está em trânsito. **Enviados e recebidos** mostra o histórico.

## 7. Cadeia de frio

**As câmaras:** **Nova câmara** cadastra a câmara com o nome (por exemplo, *Câmara 1*) e a temperatura **Mínima** e **Máxima**.
Quem cadastra é a gestão da imunização.

**As leituras:**

1. Na câmara, clique em **Registrar leitura**.
2. Informe a temperatura **Atual**, a **Mínima do período** e a **Máxima do período** (°C), e uma observação, se quiser.
3. Salve. A leitura aparece em **Leituras do dia**, com o turno (manhã ou tarde) e a marca **Na faixa** ou **Excursão**.

**Leituras pendentes** mostra o que ainda falta ler no dia.

**Lotes sob suspeita:** o lote da câmara que teve excursão, ou o que chegou fora de 2 a 8 °C, fica sob suspeita. **Não se aplica**
até a coordenação avaliar, com a consulta ao fabricante ou à rede de frio.

1. Clique em **Avaliar** (gestão da imunização).
2. Escreva o **Parecer** (por exemplo, *consulta ao fabricante, estabilidade, decisão da rede de frio*).
3. Decida: liberar o lote, ou **Inutilizar**, que tira o saldo inteiro do estoque como perda física por excursão de temperatura.

## 8. Alertas de lote (ESAVI)

**Para que serve:** a vigilância de lotes com eventos adversos. Entra em alerta o lote com **dois ou mais ESAVI graves em 30
dias**, ou com **um ESAVI de desvio de qualidade do produto (A2)**. A conta junta as doses do estoque e as de lote digitado.

**Quem usa:** a gestão da imunização. A suspensão do lote é da coordenação da rede, com acesso a todas as unidades.

- **Suspender na rede** (ou **Suspender outro lote**):
  1. informe a **Vacina**, o **Número do lote** e o **Motivo**;
  2. todos os lotes com esse número ficam sob suspeita em todas as salas, e a dose com o lote digitado é barrada, até a decisão.
- **Decidir:** escolha **Liberar** (os lotes voltam ao uso) ou **Inutilizar** (o saldo sai como perda em cada sala) e escreva o
  **Parecer**.

## 9. Calendário vacinal e metas

A aba **Calendário vacinal** (gestão da imunização) mostra o calendário em uso e a **Meta** de cobertura de cada vacina. Para
mudar a meta, clique na vacina, informe a **Meta (%)** e salve.

## 10. Mensagens comuns

| Mensagem | O que quer dizer | O que fazer |
|----------|------------------|-------------|
| *Preencha o paciente, a unidade, a vacina e a dose.* | o registro está incompleto | preencha os quatro campos |
| *Escolha o lote do estoque de onde a dose saiu.* | a dose aplicada está sem lote | escolha o lote |
| *Sem lote aplicável desta vacina no estoque da unidade.* | não há lote com saldo, dentro da validade e liberado | dê entrada no lote ou receba um remanejamento |
| *Seu acesso não inclui a sala de vacina desta unidade (VACINACAO.REGISTRAR).* | o seu perfil não vacina nesta unidade | peça o perfil, se a sua função precisa |
| *Seu acesso não inclui o calendário vacinal (IMUNIZACAO.GERENCIAR).* | o calendário é da gestão da imunização | peça à gestão |
| *Informe a temperatura da caixa na chegada.* | o recebimento está sem a temperatura | meça e informe |
| *Informe o motivo da recusa.* | a recusa está sem motivo | escreva o motivo |
| *Informe o parecer da reavaliação no CRIE.* | a revogação está sem parecer | escreva o parecer |
| *Informe a vacina, o lote e o motivo.* | a suspensão está incompleta | preencha os três campos |
| *A suspensão do lote é da coordenação da rede (acesso a todas as unidades).* | o seu acesso não cobre a rede inteira | peça à coordenação |
