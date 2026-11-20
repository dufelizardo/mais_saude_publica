# Rede e pactuação

> **Versão:** 1.15.0 · [English](../en/35-rede-e-pactuacao.md)

Menu: **Administrativo › Rede e pactuação**. Cobre:

- a **PPI** (Programação Pactuada e Integrada): quanto cada executante oferta por procedimento no mês e como a oferta se reparte
  entre unidades e esferas;
- as **referências** da rede;
- a **cobertura;**
- as **redes temáticas.**

A Regulação consome a cota ao autorizar (módulo 05).

**Quem usa:** o gestor mantém. O médico regulador consulta.

## 1. Ofertas e cotas (PPI)

Escolha a competência (o mês). **Os cartões do alto:**

- o **Ofertado;**
- o **Pactuado** em cotas da PPI;
- o **Utilizado:** as autorizações no mês;
- as **Extracotas:** as autorizações sem saldo.

**Cadastrar a oferta:**

1. Clique em **Nova oferta**.
2. Escolha o **Procedimento** e a **Unidade executante**, e informe as **Vagas no mês**.
3. Salve. **Copiar do mês anterior** repete as ofertas da competência anterior.

**Pactuar a cota:**

1. Na oferta, use **Pactuar cota**.
2. Escolha a **Unidade ou esfera** beneficiária e informe a **Cota no mês**. A cota de um município ou de uma regional vale para as
   unidades abaixo dele que não têm cota própria.
3. Salve. A oferta mostra a beneficiária, a cota, o usado e o saldo, com a marca **Esgotada** quando acaba. **Ajustar** e
   **Remover** mudam a cota.

> **Sem oferta cadastrada, a Regulação autoriza sem controle de cota.** Com a cota esgotada, a autorização pede a extracota com
> justificativa.

## 2. Referências da rede

**Para que serve:** dizer para onde cada origem manda cada procedimento.

1. Clique em **Nova referência**.
2. Escolha o procedimento, a **Origem (unidade ou esfera)** e a **Executante**, e o **Início**.
3. Salve.

**As regras:**

- **a referência mais específica vale:** a da unidade e, sem ela, a do município ou da regional;
- se a origem já tem referência para o procedimento, a nova começa depois dela e a encerra na véspera;
- **Encerrar** termina a referência;
- sem referência, a Regulação escolhe a executante sem sugestão.

A lista mostra as **Autorizações com referência**, a vigência e a situação.

## 3. Cobertura

A aba **Cobertura** mostra, por procedimento com oferta na PPI:

- a oferta pactuada, a cota usada e o que foi **Fora da referência;**
- quantos estão **Na fila** e a **Espera (dias).**

**Por origem:**

- os adscritos e a cota **Por mil** adscritos;
- as solicitadas, as autorizadas e as aguardando;
- a marca **sem referência.**

Os **Alertas** apontam o que precisa de atenção.

## 4. Redes temáticas

A aba **Redes temáticas** mostra, por rede (por exemplo, a Rede de Atenção Psicossocial, RAPS), os componentes e os pontos de cada
um. Os pontos vêm do Modelo operacional das unidades (módulo 25). As **Lacunas** apontam os componentes sem ponto no seu acesso.

## 5. Mensagens comuns

| Situação | O que quer dizer | O que fazer |
|----------|------------------|-------------|
| *Nenhuma oferta nesta competência* | o mês não tem oferta cadastrada | cadastre as ofertas ou copie do mês anterior |
| Cota **Esgotada** | a cota do mês acabou | a Regulação usa a extracota com justificativa, ou ajuste a cota |
| **Sem pactuar** na oferta | a oferta não foi repartida em cotas | pactue as cotas |
| **sem referência** na origem | a origem não tem referência para o procedimento | cadastre a referência |
