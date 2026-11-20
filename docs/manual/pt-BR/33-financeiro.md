# Financeiro

> **Versão:** 1.15.0 · [English](../en/33-financeiro.md)

Menu: **Administrativo › Financeiro**. Acompanha o Fundo Municipal de Saúde **ao lado do SIAFIC**. O sistema contábil oficial
continua sendo o SIAFIC; aqui ficam:

- as dotações e os empenhos, espelhados com o mesmo número;
- a liquidação e o pagamento;
- os restos a pagar e a execução;
- os custos por unidade, com o rateio.

**Quem usa:**

| Perfil | O que faz |
|--------|-----------|
| Financeiro | registra dotações, empenhos, liquidações e pagamentos, e apura os custos |
| Gestor e compras | consultam |

Escolha o **Exercício** no alto. A tela tem as abas:

- **Dotações** e **Empenhos;**
- **A liquidar**, **Liquidações** e **Restos a pagar;**
- **Execução** e **Custos.**

## 1. Dotações

1. Clique em **Nova dotação**.
2. Informe:
   - o **Programa de trabalho** (por exemplo, *10.301.0010.2045*) e a **Natureza da despesa** (por exemplo, *3.3.90.30*);
   - a **Descrição da ação**, a **Fonte** e o **Código da fonte;**
   - o **Valor da lei (R$).**
3. Clique em **Registrar**.

A dotação mostra o **autorizado**, o **empenhado** e o **disponível**. **Alterações:** o valor e o **Ato** (por exemplo, *Decreto
12/2026*), por **Registrar alteração**.

## 2. Empenhos

> **O empenho é emitido no SIAFIC;** aqui ele é registrado com o mesmo número para acompanhar a execução.

1. Clique em **Registrar empenho**.
2. Informe:
   - o **Número** (por exemplo, *2026NE000123*) e a **Data;**
   - a **Dotação**, o **Fornecedor** e, se houver, a **Ata ou contrato;**
   - o **Valor (R$).**
3. Clique em **Registrar**.

O empenho mostra o comprometido e o saldo, as **Ordens de fornecimento** ligadas (de Compras) e os **Reforços e anulações**, por
**Registrar movimento**, com o valor e o motivo.

## 3. Liquidação

**A liquidar:** os atestos à espera da liquidação. O recebimento definitivo em Compras vira atesto aqui.

1. No atesto, clique em **Liquidar**.
2. Confira o **Empenho** e a **Nota fiscal** e informe:
   - o **Valor bruto (R$);**
   - as retenções: **ISS**, **IRRF**, **INSS** e **Outras.**
3. O **Líquido** é calculado. Informe o **Vencimento** (vazio, 30 dias).

**Liquidação avulsa:** para a despesa que não passou pelo recebimento na plataforma.

1. Use **Liquidação avulsa** e informe o **Empenho**, a nota e os valores.
2. Escreva a **Justificativa (sem atesto na plataforma)** e a **Matrícula de quem atestou**.
3. Escolha o **Centro de custo**.

## 4. Pagamento

A aba **Liquidações** mostra o bruto e o líquido, o vencimento e a situação: **a pagar** ou **vencida**.

1. Clique em **Pagar** e informe a **Ordem bancária** e a data.
2. **Ordem cronológica:** se houver outra liquidação vencida antes desta, escreva a **Justificativa fora da ordem cronológica**.

## 5. Restos a pagar e execução

- **Restos a pagar:** os empenhos de exercícios anteriores:
  - os **Processados** (liquidados e não pagos);
  - os **Não processados** (empenhados e não liquidados).
- **Execução:**
  - por fonte e por dotação: o autorizado, o empenhado, o liquidado e o pago;
  - os **dias do atesto ao pagamento.**

## 6. Diárias do TFD

A diária e a ajuda de custo do Tratamento Fora do Domicílio são concedidas na tela **Frota e transporte**, na solicitação de TFD
autorizada:

- uma diária por beneficiário;
- o valor é os dias (de 1 a 30) vezes a diária da tabela do município.

Elas entram na execução do Financeiro e no custo da unidade.

## 7. Custos

**A competência:**

1. Escolha a **Competência** (só de mês encerrado) e apure.
2. **Refazer a apuração** recalcula enquanto a competência está aberta.
3. **Fechar a competência** congela os números. **Fechada, a competência não muda mais.**

**Os cartões:**

- o valor **sem centro** (a folha sem lotação, a avulsa antiga);
- as **saídas sem preço**, fora do total;
- o **não rateado:** o apoio e a administração sem destino no rateio. A apuração antiga mostra *apurada antes do rateio; refaça para
  ratear*.

**Por unidade:**

- o custo, os atendimentos e o custo **Por atendimento;**
- os pacientes-dia e o custo **Por paciente-dia;**
- a marca **acima da média**, para a unidade 30% acima da média do tipo.

Na unidade mista, o custo da internação vai ao paciente-dia, e o resto ao atendimento.

**Por centro de custo:** o **Direto**, o **Recebido**, o **Enviado** e o **Final** de cada centro.

**Rateio:** em degraus, pelo PNGC:

- os centros administrativos e depois os de apoio mandam o que têm aos centros que ainda não ratearam e aos produtivos da unidade;
- a tabela mostra o degrau, de onde, para onde, o critério e o valor.

**Centros de custo:**

1. Use **Novo centro do setor**: o **Setor** e o tipo (sem informar, pelo setor).
2. Escolha o **Critério do rateio**: pessoas, custo direto, área ou requisições. Sem informar, vale pessoas no administrativo e
   custo direto no apoio. **O produtivo não tem critério.**
3. No critério área, informe a **Área (m²)**.

## 8. Mensagens comuns

| Situação | O que quer dizer | O que fazer |
|----------|------------------|-------------|
| *Nenhum atesto à espera da liquidação.* | não há recebimento a liquidar | nada a fazer |
| Pagamento fora da ordem cronológica | há liquidação vencida antes | justifique ou pague a mais antiga primeiro |
| **sem centro** na apuração | há custo sem centro de custo | ligue a lotação a um setor ou escolha o centro na avulsa |
| *apurada antes do rateio; refaça para ratear* | a competência foi apurada antes do rateio existir | refaça a apuração, se ela ainda está aberta |
| *Nenhum custo na competência.* | a competência ainda não foi apurada | apure a competência |
