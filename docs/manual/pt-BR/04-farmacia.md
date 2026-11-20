# Farmácia

> **Versão:** 1.15.0 · [English](../en/04-farmacia.md)

Menu: **Assistência › Farmácia**. Controla o estoque de medicamentos por lote em cada unidade, a dispensação ao paciente, as
transferências entre unidades e o livro de estoque, onde cada movimento fica registrado.

> **O saldo é mantido pelo livro de estoque.** Nada se apaga: entrada, dispensação, perda, ajuste e transferência viram linhas do
> livro. Para corrigir um saldo, registre uma perda ou um ajuste de inventário.

**Quem usa:** o farmacêutico, em tudo. O gestor consulta. A tela tem cinco abas:

- **Estoque por lote;**
- **Dispensações;**
- **Transferências;**
- **Medicamentos;**
- **Livro de estoque.**

**Os cartões do alto:** os **Medicamentos ativos**, os **Lotes com saldo**, os que **Vencem em até 90 dias** e as **Dispensações
hoje**.

## 1. Cadastrar um medicamento

**Para que serve:** o catálogo usado nas entradas de lote.

1. Clique em **Novo medicamento**.
2. Preencha:
   - o **Nome** (por exemplo, *Dipirona 500mg*);
   - e, se quiser, o **Princípio ativo** (*Dipirona sódica*), a **Apresentação** (*Comprimido*) e o **Código**.
3. Salve (*Medicamento cadastrado*).

Na aba **Medicamentos**, o lápis edita. O medicamento **inativo** não aparece em novas entradas de lote.

## 2. Registrar a entrada de um lote

1. Clique em **Entrada de lote**.
2. Escolha o **Medicamento** e a **Unidade**.
3. Informe o **Número do lote** (por exemplo, *L2026-01*), a **Validade** e a **Quantidade** (pode ser zero).
4. Se quiser, a **Matrícula do responsável** pela entrada.
5. Salve (*Entrada de lote registrada*). A entrada vai sozinha para o livro de estoque.

> **Mesmo lote, mesma validade, na mesma unidade:** a entrada soma no lote que já existe, sem criar outro. Lote vencido não entra
> no estoque.

## 3. Ver o estoque

Na aba **Estoque por lote**, cada linha mostra o medicamento, o lote, a unidade, a validade e o saldo.

- Busque por **medicamento, princípio ativo ou lote** e filtre pela unidade ou por **Vence em até 90 dias**.
- As ações de cada lote são:

  | Ação | O que faz |
  |------|-----------|
  | **Dispensar** | entrega ao paciente (item 4) |
  | **Transferir para outra unidade** | o item 6 |
  | **Perda ou ajuste** | o item 5 |
  | **Extrato do lote** | abre o lote no **Livro de estoque** |
  | **Corrigir lote** | corrige só o número do lote e a validade |

> **Corrigir lote** não muda o saldo. Para o saldo, use **Perda ou ajuste**.

## 4. Dispensar ao paciente

**Para que serve:** registrar a entrega do medicamento ao paciente. **Cada dispensação baixa o lote no livro de estoque.**

1. Clique em **Dispensar** no alto ou na linha do lote.
2. Escolha o **Lote**. Só aparecem os lotes com saldo, e a gaveta mostra o saldo disponível. Se o lote está vencido, a gaveta
   avisa e sugere registrar a perda por vencimento.
3. Em **Paciente**, digite parte do nome ou do CPF para filtrar e escolha a pessoa.
4. Informe a **Quantidade** (maior que zero), a **Data e hora** e a **Matrícula do profissional**.
5. Se houver, o **ID da consulta**, que liga a dispensação à consulta de origem.
6. Clique em registrar (*Dispensação registrada*).

> **Dispensações não podem ser editadas nem excluídas.** Confira o lote, o paciente e a quantidade antes de registrar.

A aba **Dispensações** lista todas, com a busca por paciente, medicamento ou lote. A coluna **Consulta** diz se a dispensação
está **Vinculada** ou **Sem consulta**.

## 5. Registrar perda ou ajuste de inventário

1. Na linha do lote, clique em **Perda ou ajuste**, ou use **Registrar perda ou ajuste** no Livro de estoque.
2. Escolha o **Tipo de registro**:
   - **Perda:**
     - a **Quantidade perdida**;
     - o **Motivo da perda**: vencimento, avaria, extravio ou outro.
   - **Ajuste de inventário:**
     - o **Saldo contado fisicamente**;
     - o sistema calcula a diferença para o livro e mostra *No livro*, *Contado* e *Diferença*.
3. Escreva a **Justificativa**. Ela é obrigatória no ajuste e na perda com motivo *Outro*.
4. Salve (*Perda registrada no livro* ou *Ajuste de inventário registrado*).

## 6. Transferir para outra unidade

**Como funciona:** a transferência tem duas etapas. A quantidade **sai agora** do lote de origem e fica **em trânsito** até outro
profissional conferir o recebimento no destino.

**Enviar:**

1. Na linha do lote, clique em **Transferir para outra unidade**, ou use **Nova transferência** na aba **Transferências**.
2. Escolha o **Lote de origem**. Só aparecem os lotes com saldo e dentro da validade.
3. Escolha a **Unidade de destino** (diferente da origem) e informe a **Quantidade**.
4. Informe a **Matrícula de quem envia** e, se quiser, uma **Observação** (por exemplo, *remanejamento para cobrir falta*).
5. Envie. A transferência fica **Em trânsito**.

**Conferir o recebimento (no destino):**

1. Na aba **Transferências**, clique em **Conferir recebimento**.
2. Em **Quantidade que chegou**, conte o que chegou, não o que diz a guia.
3. Informe a **Matrícula de quem conferiu**. Precisa ser outro profissional, não quem enviou.
4. Se chegou menos, informe:
   - o **Motivo da divergência**: avaria, extravio ou outro;
   - **O que aconteceu** (por exemplo, *3 frascos quebrados na caixa*).
5. Salve: *Recebimento registrado* ou *Recebimento registrado com divergência*. O lote entra no destino.

**Cancelar:** se o medicamento não saiu ou voltou para a origem, use **Cancelar transferência**, com o motivo. A quantidade
volta ao lote de origem, e a transferência não pode mais ser recebida.

**Situações:** em trânsito, recebida, recebida com divergência ou cancelada. Os filtros da aba separam por situação e por
unidade de destino.

## 7. O Livro de estoque

**Para que serve:** o extrato de um lote, do registro mais antigo ao mais recente.

1. Escolha o lote (ou use **Extrato do lote** na aba Estoque).
2. Cada linha mostra a data, o tipo de movimento, o detalhe, o responsável e a quantidade:

   | Tipo | Quando aparece |
   |------|----------------|
   | Saldo inicial | o saldo antes do livro existir |
   | Entrada | a entrada do lote |
   | Dispensação | a entrega ao paciente |
   | Perda / Ajuste de inventário | os registros do item 5 |
   | Transferência enviada, recebida ou cancelada | o item 6 |
   | Administração ao paciente / retificada | a checagem de medicação no atendimento |
   | Incorporado a outro lote / Lote duplicado incorporado | a mesma remessa cadastrada duas vezes na unidade, unificada |

> **Registro imutável.** Para corrigir o saldo, registre um ajuste de inventário.

## 8. Mensagens comuns

| Mensagem | O que quer dizer | O que fazer |
|----------|------------------|-------------|
| *Informe o nome do medicamento.* | o cadastro está sem o nome | escreva o nome |
| *Informe o número do lote.* / *Informe a validade.* | a entrada está incompleta | preencha o lote e a validade |
| *Use um número inteiro igual ou maior que zero.* | a quantidade da entrada é inválida | use um número inteiro, zero ou mais |
| *O lote já está vencido; não entra no estoque.* | a validade já passou | confira a validade; lote vencido não entra |
| *A quantidade precisa ser maior que zero.* | a dispensação está sem quantidade | informe quantas unidades saíram |
| *Com motivo “Outro”, a justificativa é obrigatória.* | a perda com *Outro* precisa do porquê | escreva a justificativa |
| *A justificativa é obrigatória no ajuste.* | o ajuste está sem justificativa | explique a diferença encontrada |
| *Informe o saldo contado (0 ou mais).* | o ajuste está sem a contagem | informe o que foi contado na prateleira |
| *A unidade de destino precisa ser diferente da unidade do lote de origem.* | o destino é a própria unidade | escolha outra unidade |
| *O recebimento precisa ser conferido por outro profissional, não por quem enviou.* | quem enviou tentou conferir | outro profissional confere |
| *Descreva o que aconteceu com o que não chegou.* | há divergência sem explicação | descreva a avaria ou o extravio |
| *Esta transferência é de unidades fora do seu acesso.* | o seu perfil não vale para a origem nem para o destino | peça o perfil, se a sua função precisa |
