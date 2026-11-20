# Compras

> **Versão:** 1.15.0 · [English](../en/31-compras.md)

Menu: **Administrativo › Compras**. Cobre o caminho da compra pública (Lei 14.133/2021):

1. a **solicitação** e a **pesquisa de preços;**
2. a **contratação** (pregão, dispensa, adesão a ata…) e os **contratos e atas;**
3. a **ordem de fornecimento**, o **recebimento** e as **ocorrências** com o fornecedor;
4. o cadastro de **fornecedores**, com as certidões e as sanções.

**Quem usa:**

| Perfil | O que faz |
|--------|-----------|
| Quem pede (gestor, almoxarife, farmacêutico) | cria a solicitação de compra |
| Compras (setor de compras, gestor, financeiro) | analisa, pesquisa preços, contrata, gere contratos e ordens |
| Gestor e fiscal do contrato | recebem a entrega pelo item **Meus contratos** do menu |

A tela tem as abas:

- **Solicitações** e **Fornecedores;**
- **Contratações** e **Contratos;**
- **Painel.**

## 1. Solicitação de compra

1. Clique em **Nova solicitação**.
2. Escolha a **Unidade**, o **Setor** (opcional) e a **Prioridade** (normal ou urgente).
3. Escreva a **Justificativa da necessidade**.
4. Inclua os itens com **Adicionar item**: o **Tipo** (material ou medicamento), o item, a **Unidade de medida** e a **Quantidade**.
   **Sugerir pelo estoque** propõe o que está abaixo do mínimo.
5. **Salvar rascunho** e, quando pronta, **Enviar para análise**.

**O ciclo:** rascunho → enviada → aprovada (ou devolvida, ou rejeitada) → em cotação → cotada.

**Analisar (Compras):** **Aprovar**, **Devolver** ou **Rejeitar**, com o motivo para devolver ou rejeitar.

## 2. Pesquisa de preços

Na solicitação aprovada:

1. Use **Nova cotação** para cada item:
   - a **Fonte:** proposta de fornecedor, contratação pública similar, site ou outra;
   - a **Referência** (o contrato, o site ou a fonte);
   - o **Preço unitário (R$)** e a **Data.**
2. Clique em **Registrar cotação**.
3. Com menos de 3 cotações num item, escreva a **Justificativa para menos de 3 cotações**.
4. **Concluir pesquisa de preços.** A solicitação fica **Cotada** e pode entrar numa contratação.

## 3. Contratação

1. Na aba **Contratações**, use **Nova contratação** (ou **Abrir contratação** a partir das **Solicitações cotadas**).
2. Informe:
   - a **Modalidade:** pregão eletrônico, concorrência, dispensa, inexigibilidade ou adesão a ata;
   - o **Número do processo**, o **Fundamento legal** (por exemplo, *Art. 75, II*) e o **Objeto;**
   - na adesão, a **Ata de origem** (o órgão gerenciador e o número).
3. Ligue as solicitações cotadas.
4. **Resultado:** para cada item, o fornecedor e o **Preço unitário**. Acima da referência da pesquisa, escreva a **Justificativa do
   preço acima da referência**. Clique em **Registrar resultado**.
5. **Homologação:**
   - escolha o **Instrumento** (ata de registro de preços ou contrato) e marque **Serviço contínuo**, se for;
   - informe a **Matrícula do gestor** e a **Matrícula do fiscal** do contrato;
   - clique em **Homologar.**

A homologação gera as **Atas e contratos**. **Cancelar contratação** também é possível, com o motivo.

## 4. Contratos e atas

A aba **Contratos** mostra a vigência, o valor atual e os **Alertas**: o vencimento, o saldo baixo e as ocorrências.

No contrato:

- o **Gestor e fiscal;**
- o **Saldo** e o **Preço** de cada item, com a marca **baixo** abaixo de 20%;
- os **Aditivos:**
  - use **Novo aditivo:** prorrogação de prazo, acréscimo ou supressão de quantidade, ou reequilíbrio de preço;
  - informe a data em que foi **Assinado em**, o **Novo fim** ou o **Novo preço;**
  - clique em **Registrar aditivo.**

## 5. Ordem de fornecimento e recebimento

**Emitir a ordem (Compras):**

1. No contrato, use **Nova ordem de fornecimento**.
2. Escolha a **Unidade de entrega** e os itens com as quantidades.
3. Escolha o **Empenho** do Financeiro, ou digite o **Número do empenho**.
4. Informe o **Prazo (dias)** e clique em **Emitir ordem**.

**Receber (gestor ou fiscal do contrato, por Meus contratos):**

1. Use **Receber entrega (provisório)**:
   - a **Nota fiscal** e a **Data da nota;**
   - por item, o que chegou e, se houver, o **Lote** e a **Validade.**
2. Clique em **Registrar recebimento provisório**.
3. Depois de conferir, use **Confirmar recebimento definitivo**, ou recuse com o **Motivo da recusa**.

A ordem mostra, por item, o **Pedido**, o **Aceito**, o **Em recebimento** e a **Falta**.

> O recebimento definitivo dá a entrada no Estoque (material) ou na Farmácia (medicamento), com o custo pelo preço do contrato, e
> entra na fila de liquidação do Financeiro.

**Ocorrências:** use **Nova ocorrência** para atraso, entrega parcial, item fora da especificação ou outra, com a descrição. **Cancelar ordem**, com o
motivo.

## 6. Fornecedores

1. Clique em **Cadastrar fornecedor**: o **CPF ou CNPJ**, a **Razão social ou nome**, o **Nome fantasia**, o **E-mail**, o
   **Telefone** e o **Endereço**.
2. **Certidões:** use **Nova certidão**, com o tipo, a data em que foi **Emitida em** e a data **Válida até**. A coluna
   **Certidões** mostra **Em dia** ou o que venceu.
3. **Sanções** (art. 156 da Lei 14.133/2021):
   - use **Nova sanção:** o tipo, o **Processo**, o **Início**, o **Fim** e o **Valor (R$)**;
   - a sanção **Vigente** pode ser **Revogada.**

## 7. Painel

Os cartões do painel mostram:

- o **Valor contratado** e as **Ordens em aberto;**
- os **Contratos vencendo** em até 90 dias;
- os **Itens com saldo baixo**, abaixo de 20%;
- as **Ocorrências abertas** com fornecedores;
- as **Solicitações em andamento;**
- as **Ordens atrasadas.**

## 8. Mensagens comuns

| Situação | O que quer dizer | O que fazer |
|----------|------------------|-------------|
| Item com menos de 3 cotações | a lei pede a justificativa | escreva a justificativa ou registre mais cotações |
| Preço acima da referência | o resultado passou da pesquisa | escreva a justificativa do preço |
| Item com saldo **baixo** | o contrato está acabando para o item | providencie aditivo ou nova contratação |
| *Nenhuma solicitação cotada fora de contratação.* | não há solicitação pronta para contratar | conclua a pesquisa de preços |
