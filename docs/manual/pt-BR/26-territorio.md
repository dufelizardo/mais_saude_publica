# Território

> **Versão:** 1.15.0 · [English](../en/26-territorio.md)

Menu: **Administrativo › Território**. É o recorte geográfico da atenção primária:

- os **segmentos**, os **territórios**, as **áreas** e as **microáreas**;
- a equipe que responde pela área e o agente comunitário de saúde (ACS) que responde pela microárea, com histórico;
- o cadastro dos **imóveis**, dos **domicílios** e das **famílias**.

**Quem usa:**

| Perfil | O que faz |
|--------|-----------|
| Quem está logado | consulta o recorte do território |
| Gestor e coordenação de enfermagem | cadastram segmentos, territórios, áreas e microáreas, e atribuem a equipe e o ACS |
| Enfermeiro, coordenação de enfermagem e ACS | cadastram imóveis, domicílios e famílias |

**Os cartões do alto:**

- as **Áreas** e as **Microáreas;**
- as **Microáreas sem ACS**, sem agente comunitário atribuído;
- os **Sem desenho:** as áreas e microáreas sem o polígono no mapa.

## 1. Montar o recorte (gestão)

A ordem é **segmento → território → área → microárea**.

1. **Novo segmento:** um distrito sanitário, por exemplo. Informe o **Código** e o **Nome**.
2. **Novo território:** escolha o segmento e informe o código e o nome.
3. **+ Área:**
   - escolha o território e a **Unidade**, com o CNES;
   - escolha a **Equipe responsável.** Sem equipe, as microáreas não recebem ACS.
4. **Nova microárea:** na área, informe o código e escolha o **ACS**.

**O desenho no mapa:**

- desenhe o polígono da área e de cada microárea;
- veja **Esta área** ou o **Território inteiro;**
- sem polígono, salvar apaga o desenho.

**Trocar a equipe ou o ACS:** escolha a nova equipe ou o novo ACS e escreva o **Motivo da troca**. O **Histórico** guarda quem
respondeu antes.

Use **Buscar no território** para achar área, microárea, equipe ou ACS.

## 2. Imóveis e famílias

O cadastro domiciliar e familiar é de quem cadastra ou gere o território da área.

**O imóvel:**

1. Na área, abra **Imóveis e famílias** e clique em **Novo imóvel**.
2. Escolha o **Tipo** e a **Microárea**, ou use **Pelo ponto no mapa**. Com o ponto marcado, ele precisa cair dentro da microárea
   desenhada.
3. Informe o endereço: o **Logradouro**, o **Número** (*S/N* se não tiver), o **Bairro**, o **CEP**, a **Cidade** e a **UF**.
4. Se quiser, clique no mapa onde fica o imóvel.
5. Deixe **Ativo** marcado. O imóvel inativo (demolido, desocupado de vez) fica no histórico.

**O domicílio (dentro do imóvel):**

1. Use **Novo domicílio** e informe a **Identificação no imóvel** (por exemplo, *apto 12, casa dos fundos*).
2. Se quiser, informe:
   - a situação e a localização;
   - os cômodos;
   - o abastecimento de água, o escoamento sanitário e o destino do lixo;
   - a energia elétrica.
3. O domicílio inativo não recebe família.

**A família:**

1. Use **Nova família**: o responsável (que é um paciente cadastrado), a **Renda familiar** e o **Desde**.
2. As outras pessoas entram depois, por **Incluir pessoa**, com o parentesco. Busque pelo nome, pelo CPF ou pelo Cartão SUS. Se
   não achar, cadastre o paciente (módulo 01) e volte.
3. Se a pessoa tem endereço diferente no cadastro de paciente, use **Usar o endereço do imóvel**.

**Mudanças:**

- **Saída:** a pessoa sai da família, com o **Motivo** (por exemplo, *saiu de casa, óbito, mudou de cidade*) e a data. Sendo a
  última pessoa, a família deixa de morar ali.
- **Trocar responsável:** escolha o novo responsável.
- **Mudança:**
  - a família inteira muda para outro domicílio (**Área de destino**, **Imóvel** e **Domicílio**);
  - ela deixa de morar aqui na data e começa no destino com as mesmas pessoas;
  - para uma pessoa só, use a saída e inclua-a na outra família.

> Morar num domicílio de área com equipe faz o paciente ter a **equipe de referência** pelo domicílio (módulo 01).

## 3. Mensagens comuns

| Mensagem | O que quer dizer | O que fazer |
|----------|------------------|-------------|
| *Escolha a microárea ou marque o ponto no mapa.* | o imóvel está sem microárea | escolha a microárea ou marque o ponto |
| *Informe a latitude e a longitude juntas, ou nenhuma.* | só uma coordenada foi informada | informe as duas ou nenhuma |
| *Escolha o segmento.* / *Escolha o território.* | o cadastro está sem o nível de cima | escolha o segmento ou o território |
| *Escolha a equipe.* / *Escolha o ACS.* | a área ou a microárea está sem responsável | escolha a equipe ou o ACS |
| *Informe o motivo da troca.* | a troca está sem motivo | escreva o motivo |
| *Escolha o domicílio de destino.* | a mudança está sem destino | escolha a área, o imóvel e o domicílio |
| **Área sem equipe responsável.** | as microáreas não podem receber ACS | escolha a equipe da área |
