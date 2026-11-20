# Estoque e almoxarifado

> **Versão:** 1.15.0 · [English](../en/30-estoque-e-almoxarifado.md)

Menu: **Administrativo › Estoque e almoxarifado**. São os materiais e os insumos gerais de cada unidade: os lotes, as entradas, o
consumo, as perdas, as requisições à central, as transferências, o inventário e os alertas.

> **Medicamento fica na Farmácia** (módulo 04); **vacina, na Sala de Vacina** (módulo 10).

**Quem usa:**

| Perfil | O que faz |
|--------|-----------|
| Almoxarife | dá entrada, registra consumo e perda, atende requisições, transfere e faz inventário |
| Coordenação de enfermagem | requisita à central e consulta |
| Gestor | mantém o catálogo e o estoque mínimo, e reserva para emergência |
| Compras e manutenção | consultam |

**Os cartões do alto:**

- os **Materiais em estoque** (com saldo ou mínimo nesta unidade);
- os **Abaixo do mínimo** (pedir reposição);
- os **Vencendo em 90 dias;**
- o **Valor do estoque**, pelo custo das entradas.

## 1. Dar entrada

1. Clique em **Nova entrada** e escolha o **Material**.
2. Informe a **Quantidade** e a **Origem**: nota fiscal, doação, saldo inicial ou outra.
3. Se o material controla lote, informe o **Lote** e a **Validade**.
4. Se quiser, o **Custo unitário (R$)** e o **Fornecedor**.
5. Salve. O recebimento de uma ordem de fornecimento em Compras também dá entrada aqui.

## 2. O estoque por material

A lista é por material, com os lotes. **A saída sai dos que vencem primeiro.**

Em cada material:

- **Saída:** o consumo, com a quantidade, o **Setor que consumiu** e o lote (opcional; sem escolha, os que vencem primeiro).
  Durante uma emergência, escolha **Consumo da emergência** para usar a reserva dela.
- **Perda:** a quantidade e o motivo (vencimento, avaria, extravio ou outro).
- **Ajustar:** a **Quantidade contada** e a justificativa.
- **Definir mínimo:** o **Estoque mínimo** da unidade.
- **Reservar para emergência** (gestor):
  - escolha a **Emergência**, a quantidade e o motivo (por exemplo, *kits para os abrigos*);
  - enquanto ela estiver ativa, o consumo comum, a perda de lote válido, a transferência avulsa e as requisições de fora da
    emergência não tiram do reservado;
  - a desmobilização libera o que restar, e **Liberar** devolve antes.

**Livro de movimentação:** os últimos 30 dias, do mais recente para o mais antigo. **Nada se edita nem se apaga.** Os tipos são:

- saldo inicial, entrada e saída por consumo;
- perda e ajuste de inventário;
- transferência (saída, entrada e estorno).

## 3. Requisições à central

A central de abastecimento é a unidade com a capacidade **Central de abastecimento** no Modelo operacional. Sem ela, a tela avisa.

**Pedir (unidade):**

1. Clique em **Nova requisição** e escolha a **Central**.
2. Escolha a **Prioridade** (normal, urgente ou emergência).
3. Use **Incluir material** para cada item, com a quantidade.
4. Informe a **Sua matrícula** e envie.

**Atender (central):**

1. Na requisição, clique em **Atender**.
2. A gaveta mostra o saldo da unidade, o consumo de 30 dias e o saldo da central.
3. Informe a quantidade de cada item. Saem os lotes que vencem primeiro. **Zero deixa o item sem atender.**
4. A requisição pode ficar **atendida em parte**. Use **Negar** com o motivo, se for o caso.

## 4. Transferências

As que saem e chegam nesta unidade.

**Enviar:**

1. Use **Transferir para outra unidade**.
2. Escolha o **Destino** e inclua os **Lotes** com as quantidades.
3. Envie. A requisição atendida também gera a transferência (**pela requisição**).

**Receber (no destino):**

1. Clique em **Receber** e confira cada item: o **Enviado** e o que **Chegou**.
2. Informe o **Motivo da diferença**. A falta volta à origem; a avaria vira perda no destino.

## 5. Inventário

1. Use **Abrir inventário**. **Abrir congela os lotes com saldo da unidade e para a movimentação** até ele ser fechado ou cancelado.
2. Conte cada lote e registre a quantidade contada. A lista mostra os lotes, os contados e os com diferença.
3. Feche o inventário. **O fechamento lança os ajustes.** Ou cancele, sem ajuste.

## 6. Alertas

A aba **Alertas** mostra:

- o que está abaixo do mínimo;
- o que vence em até 90 dias;
- o que está vencido com saldo;
- o **consumo sem saldo:** os insumos que a coleta ou a realização de exame usou sem saldo no estoque. Acerte a entrada ou o
  inventário e marque como **Conferido**.

## 7. Catálogo de materiais

O catálogo **vale para a rede inteira**.

1. Clique em **Novo material**.
2. Informe:
   - o **Código** e, se houver, o **CATMAT;**
   - o **Nome** (por exemplo, *Luva de procedimento M*);
   - a categoria: médico-hospitalar, EPI, odontológico, laboratório, limpeza e higiene, expediente, rouparia ou outro;
   - a **Unidade de medida** (unidade, caixa, pacote, par, rolo, litro, frasco…).
3. Marque **Controla lote e validade**, se for o caso. **Isso não muda depois da primeira entrada.**
4. Deixe **Ativo** marcado.

## 8. Mensagens comuns

| Situação | O que quer dizer | O que fazer |
|----------|------------------|-------------|
| *Nenhuma unidade tem a capacidade Central de abastecimento…* | não há central cadastrada | inclua a capacidade no Modelo operacional do almoxarifado central |
| Movimentação recusada | há inventário aberto na unidade | feche ou cancele o inventário |
| Saída recusada durante emergência | a quantidade está reservada | use o consumo da emergência ou libere a reserva |
| **Sem saldo** em Alertas | um exame usou insumo sem saldo | acerte a entrada ou o inventário e marque como conferido |
