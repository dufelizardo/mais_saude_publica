# Vigilância sanitária

> **Versão:** 1.15.0 · [English](../en/14-vigilancia-sanitaria.md)

Menu: **Vigilância › Vigilância sanitária**. Cobre:

- os estabelecimentos regulados e o licenciamento;
- as inspeções e as denúncias;
- o processo sanitário: auto de infração, defesa, decisão e recurso;
- as medidas cautelares: apreensão e interdição.

**Quem usa:**

| Perfil | O que faz |
|--------|-----------|
| Fiscal sanitário | cadastra estabelecimentos, inspeciona, registra denúncias e lavra autos |
| Coordenação da vigilância sanitária | tudo do fiscal, mais licenças, medidas cautelares e o julgamento (decisão e recurso) |
| Gestor | acompanha e mantém a coordenação |

**Os cartões do alto:**

- as **Licenças pendentes:** sem licença, vencida ou vencendo;
- os **Prazos de correção vencidos:** irregularidades abertas;
- as **Denúncias paradas** há mais de 30 dias;
- os **Processos com pendência:** ciência, revelia, decisão ou encerramento.

A tela tem as abas:

- **Estabelecimentos**, **Inspeções**, **Denúncias** e **Licenças;**
- **Processos** e **Medidas;**
- **Pendências.**

## 1. Cadastrar um estabelecimento

1. Clique em **Novo estabelecimento**.
2. Informe:
   - o **CNPJ ou CPF**, a **Razão social ou nome**, o **Nome fantasia**, o **Endereço** e o telefone;
   - o **Responsável legal** e o **Responsável técnico**, com o **Registro no conselho**.
3. Em **Atividades (CNAE) e grau de risco**, use **Adicionar atividade** para cada uma: o **CNAE** (*0000-0/00*), a descrição e o
   grau de risco.

   | Grau | Licenciamento |
   |------|---------------|
   | I · baixo | dispensa de licenciamento, sem validade (RDC 153/2017; Lei 13.874/2019) |
   | II · médio | licença sanitária |
   | III · alto | licença com uma inspeção satisfatória nos últimos 12 meses |

4. Deixe **Ativo** marcado e salve.

No estabelecimento aberto ficam:

- as irregularidades, as inspeções, as licenças e os processos;
- os **Documentos:** o tipo, o título e o arquivo PDF, JPG ou PNG de até 10 MB;
- as ações **Alterar cadastro**, **Inspecionar**, **Lavrar auto de infração** e **Abrir não conformidade**.

## 2. Licenciar

1. Na linha do estabelecimento, use **Licença** (coordenação).
2. Conforme o risco:
   - **Dispensa de licenciamento** (risco I);
   - **Conceder licença sanitária**, com a validade. Vazio, vale um ano. No risco III, a licença pede uma inspeção satisfatória nos
     últimos 12 meses.
3. Salve. A licença ganha um número.

A aba **Licenças** lista o número, a emissão e a validade. Em **Pendências**, o **Licenciamento** mostra o que está sem licença,
vencido ou vencendo.

## 3. Inspecionar

1. Clique em **Registrar inspeção** (ou **Inspecionar** no estabelecimento ou na denúncia).
2. Escolha o **Estabelecimento**, o motivo e, se houver, a **Denúncia** ou a **Inspeção de origem** (o retorno para conferir as
   correções).
3. Se quiser, escolha um **Roteiro** e responda os **Itens do roteiro**.
4. Registre as **Irregularidades** com **Adicionar irregularidade**: a gravidade, o enquadramento e o prazo **Corrigir até**. No
   retorno, marque as **Irregularidades corrigidas**.
5. Informe as **Matrículas dos fiscais**, separadas por vírgula, incluindo a sua.
6. Escreva o **Relatório** e salve.

O termo da inspeção sai em PDF (**Termo**), e a linha mostra **assinada**.

## 4. Denúncias

1. Clique em **Registrar denúncia** e escolha o **Canal**.
2. Escolha o **Estabelecimento cadastrado** ou, se ele não estiver cadastrado, descreva o **Estabelecimento denunciado** (o nome e
   o endereço).
3. Escreva a denúncia. Marque **Anônima**, ou informe o **Denunciante** e o **Contato**.

   > Os dados do denunciante só aparecem para a coordenação da vigilância sanitária.

4. Salve. A denúncia recebe um **Protocolo**.

Depois, use **Inspecionar** e, ao fim, **Concluir**: **Procedente** ou **Improcedente**, com a justificativa.

## 5. Processo sanitário

**Lavrar auto de infração:**

1. Use **Lavrar auto de infração** no estabelecimento ou na aba **Processos**.
2. Marque as **Irregularidades abertas a autuar**.
3. Informe o **Dispositivo legal infringido** (por exemplo, *Lei 6.437/1977, art. 10, IV*) e a **Descrição da infração**.
4. Escolha a gravidade. Vazia, vale a da maior irregularidade.
5. Informe as **Matrículas dos fiscais** e salve.

**O andamento do processo:**

1. **Ciência do auto:** a forma e a data.
2. **Defesa:** a data do protocolo e o resumo. Sem defesa no prazo, o processo segue à **revelia**.
3. **Decisão** (coordenação):
   - o **Resultado:** **Aplicar penalidades** ou **Arquivar**;
   - a **Fundamentação**, com os atenuantes (art. 7º) e os agravantes (art. 8º);
   - as **Penalidades (art. 2º)**, com o valor (R$) ou o prazo da suspensão.
4. **Ciência da decisão.**
5. **Recurso** (o protocolo e o resumo) e o **Julgamento do recurso**: **Improvido**, **Parcialmente provido** ou **Provido**,
   com as penalidades que ficam.
6. **Encerrar processo.**

Os **Termos** de cada etapa saem em PDF (**Baixar**).

> *A próxima etapa não está no seu acesso*: a decisão e o julgamento são da coordenação.

## 6. Medidas cautelares

Na aba **Medidas** (filtro **Vigentes** ou **Encerradas**):

- **Apreender produto:**
  1. informe o **Produto**, o lote, a **Quantidade** e o **Local de guarda**, e o motivo da apreensão;
  2. depois, registre a **Destinação do produto**.
- **Interdição cautelar:**
  1. escolha o estabelecimento e descreva o que é interditado e por quê;
  2. marque **Interdição total**, se for o caso, e informe o **Início** e o **Até** (vazio, 90 dias).
  3. **Desinterditar**, com o motivo, encerra a interdição.

Em **Pendências**, as **Interdições cautelares no fim** avisam o que vence em breve.

## 7. Conferir um documento da vigilância sanitária

Qualquer pessoa, **sem login**, confere na página **Verificar documento da vigilância sanitária**:

- um **termo** (auto de infração, decisão, interdição, apreensão ou desinterdição), pelo código do rodapé. A página mostra o tipo,
  a data, o estabelecimento e o **Hash do PDF (SHA-256)**: o SHA-256 do PDF recebido precisa ser igual;
- uma **licença sanitária**, pelo número. A página mostra a emissão e a validade.

## 8. Mensagens comuns

| Mensagem | O que quer dizer | O que fazer |
|----------|------------------|-------------|
| *Informe o CNPJ ou CPF, a razão social e o endereço.* | o cadastro está incompleto | preencha os três campos |
| *Informe o CNAE e a descrição de cada atividade.* | há atividade sem CNAE ou descrição | complete as atividades |
| *Informe a matrícula de ao menos um fiscal.* | a inspeção ou o auto está sem fiscal | informe as matrículas |
| *Escreva o relatório da inspeção.* | a inspeção está sem relatório | escreva o relatório |
| *Marque ao menos uma irregularidade aberta.* | o auto está sem irregularidade | marque as irregularidades a autuar |
| *Informe o dispositivo legal e a descrição.* | o auto está incompleto | informe a lei e descreva a infração |
| *Informe a fundamentação.* / *Informe ao menos uma penalidade.* | a decisão está incompleta | escreva a fundamentação e as penalidades |
| *Informe o produto e a quantidade.* | a apreensão está incompleta | preencha o produto e a quantidade |
| *A próxima etapa não está no seu acesso.* | a etapa é da coordenação | peça à coordenação |
