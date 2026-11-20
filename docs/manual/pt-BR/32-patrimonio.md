# Patrimônio

> **Versão:** 1.15.0 · [English](../en/32-patrimonio.md)

Menu: **Administrativo › Patrimônio**. Cuida dos bens permanentes da rede:

- o tombamento e o responsável;
- as transferências entre unidades;
- a manutenção (planos, chamados e ordens de serviço);
- o inventário e a baixa.

**Quem usa:**

| Perfil | O que faz |
|--------|-----------|
| Patrimônio e gestor | tombam, transferem, abrem inventário e pedem baixa |
| Manutenção e gestor | executam as ordens de serviço |
| Gestor | confirma a baixa acima de R$ 10.000,00 |
| Todo responsável por bem | vê os bens pelo item **Meus bens** do menu e abre chamado |
| Membro de comissão | confere o inventário pelo item **Meus inventários** do menu |

A tela tem as abas:

- os bens, **A tombar** e as transferências;
- **Manutenção**, **Inventários** e **Painel;**
- **Meus bens.**

## 1. Tombar e cadastrar bens

**A tombar:** os bens permanentes aceitos no recebimento definitivo de Compras, à espera do número de tombamento e do responsável.

1. Clique em **Tombar** no bem.
2. Informe o número de tombamento e a **Matrícula do responsável**.

**Cadastrar bem:** para o bem que não veio de uma compra pelo sistema (doação, cessão, transferência de outro ente ou o acervo
antigo). Ele entra já tombado.

1. Clique em **Cadastrar bem**.
2. Informe:
   - a **Descrição** e a **Classe:** equipamento médico-hospitalar, informática, mobiliário, veículo, eletrodoméstico ou outro;
   - a **Origem:** compra, doação, cessão ou comodato, transferência de outro ente;
   - a **Marca**, o **Modelo** e o **Número de série;**
   - a **Data de aquisição**, o **Valor (R$)** e o **Documento** (a nota, o termo de doação…);
   - a **Matrícula do responsável** e uma observação, se quiser.
3. Clique em **Cadastrar**.

**O bem:** a ficha mostra:

- a identificação, o responsável e o **Valor contábil**, estimado pela depreciação linear da classe;
- a manutenção, os **Responsáveis** e a baixa;
- no veículo, o **Veículo da frota** ligado.

## 2. Responsável e transferência

- **Trocar responsável:** a matrícula do novo responsável. O responsável assina o termo, e o bem aparece em **Meus bens** dele.
- **Transferir para outra unidade:**
  1. use **Enviar para**, com a unidade e o motivo;
  2. o destino usa **Aceitar** (com o **Setor no destino** e a **Matrícula do novo responsável**) ou recusa, com o motivo;
  3. a origem pode **Cancelar envio** enquanto está à espera do aceite.

> Enquanto houver inventário aberto na unidade, os bens dela não são transferidos.

## 3. Manutenção

**Abrir chamado (qualquer responsável):** no bem, use **Abrir chamado: defeito**. Descreva o **Defeito** e marque **Bem parado**,
se ele não funciona. **Meus chamados** mostra os que você abriu.

**Planos de manutenção (preventiva e calibração):**

1. No bem, use **Novo plano**: o tipo (preventiva, calibração ou segurança elétrica) e **A cada (dias)**. A corretiva não tem
   plano, e cada bem tem um plano ativo por tipo.
2. Se quiser, a **Última execução**.
3. Clique em **Criar plano**. Vencido o plano, a ordem de serviço aparece sozinha, uma por vencimento. **Desativar** para o
   plano, e as ordens já abertas continuam.

**As ordens de serviço (manutenção):**

1. **Triagem e início:**
   - a prioridade (baixa, normal, alta ou urgente) e **Salvar prioridade;**
   - quem executa: a **Equipe interna** ou o **Contrato de serviço** (com o contrato e o técnico);
   - **Iniciar.**
2. **Conclusão:**
   - o **Serviço realizado** e a **Mão de obra (R$);**
   - na calibração, o **Certificado** e a data **Válida até;**
   - as peças: o **Material do Estoque** (que dá saída) ou a peça trazida pelo contrato, com a descrição, a quantidade e o custo;
   - **Concluir.**
3. **Cancelar ordem:** com o motivo.

**Os avisos:**

- os **Planos vencendo em 30 dias;**
- as **calibrações vencidas;**
- as ordens abertas, em execução e com o bem parado.

## 4. Inventário

**Abrir (patrimônio):**

1. Use **Abrir inventário**, escolha a **Unidade** e a **Comissão**.
2. Os bens tombados da unidade entram na lista. Enquanto o inventário estiver aberto, eles não são transferidos.

**Conferir (comissão, por Meus inventários):**

1. Em cada bem, clique em **Conferir** e escolha o **Resultado:** encontrado, não encontrado ou em outro local.
2. Escolha a **Conservação** (bom, regular, ruim ou inservível) e, se estava fora, a **Unidade onde estava** e o **Setor onde
   estava**.
3. Clique em **Registrar**.
4. **Achados a mais:** o **Bem achado fora da lista**, com o local, por **Registrar achado**.

**Fechar inventário:** o fechamento propõe as providências:

- o setor corrigido;
- a transferência aberta, para o bem em outra unidade;
- a baixa sugerida, para o não encontrado ou o inservível.

## 5. Baixa

1. No bem, use **Pedir baixa** e escolha o motivo:
   - inservível, extravio ou furto ou roubo;
   - doação a terceiro, alienação ou devolução da cessão.
2. Informe o **Processo**, a **Data** e, no furto ou roubo, o **Boletim de ocorrência**.
3. **Acima de R$ 10.000,00, a baixa espera a confirmação da gestão.**
   - Em **Baixas à espera da confirmação**, o gestor usa **Confirmar** ou **Recusar**, com o motivo da recusa.

## 6. Painel

O painel mostra:

- os bens tombados, o valor de aquisição e o valor contábil estimado;
- os ociosos, os a tombar, os inventários abertos e as baixas no ano;
- **Por unidade:** os bens, a aquisição, o contábil e o último inventário (com aviso **há mais de 12 meses**);
- **Por classe;**
- **Manutenção:** os bens que mais custaram.

## 7. Mensagens comuns

| Situação | O que quer dizer | O que fazer |
|----------|------------------|-------------|
| Transferência recusada | há inventário aberto na unidade | feche o inventário antes |
| **calibração vencida** no bem | o certificado passou da validade | abra a ordem de calibração |
| Baixa **À espera da confirmação** | o valor passa de R$ 10.000,00 | aguarde a confirmação da gestão |
| *Nenhum bem sob a sua responsabilidade.* | você não é responsável por bem | nada a fazer |
| Último inventário **há mais de 12 meses** | a unidade está sem inventário recente | abra o inventário |
