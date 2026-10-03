# 0104 — Equipes de saúde: tela

## Status

Aceita e implementada (parte 2 de 4 de Equipes e Escalas). Usa a API da
[ADR-0103](./0103-equipes-backend.md) e segue o protótipo `Equipes.html`. As escalas vêm nas ADRs 0105 e 0106.

## Contexto

- A ADR-0103 criou equipes, membros, composição mínima por tipo e apoio matricial, só com API.
- **A estrutura do protótipo é uma grade de cartões, com o detalhe da equipe embaixo.**
  - Em cima: indicadores, abas por tipo de equipe com contagem e filtros.
  - No meio: a grade `.teams-grid` de cartões `.team`. Cada cartão tem o marcador com a sigla e a cor do tipo,
    o nome, as funções com contagem, a barra de cobertura e os avatares.
  - Embaixo: o detalhe da equipe escolhida (`.detail`), com a tabela de membros, as microáreas, os programas,
    os indicadores e a reunião.
  - Diferente de Equipamentos (ADR-0102, lista e detalhe), aqui a grade de cartões é o elemento dominante, e é
    ela que a tela segue.
- Profissionais (ADR-0072) tinha o botão "Atribuir equipe" desligado, e Equipamentos tinha as equipes da
  unidade como "Em breve".

## Decisão

1. **Tela Equipes** em `/rh/equipes`, no grupo Recursos Humanos, logo depois de Profissionais.
   - Aparece com `EQUIPE.GERENCIAR`, `RH.CONSULTAR`, `RH.GERENCIAR` ou `ADMINISTRATIVO.CONSULTAR`, as mesmas
     permissões de leitura da API.
   - Carrega sob demanda.
   - Cadastro, edição e membros só aparecem com `EQUIPE.GERENCIAR`.
2. **Topo:** **Nova equipe**; **Sincronizar CNES** fica como "Em breve".
3. **Indicadores:**
   - equipes ativas sobre as cadastradas;
   - quantas são eSF (a cobertura do território fica "Em breve");
   - profissionais vinculados a equipes ativas;
   - equipes incompletas.
4. **Abas por tipo e filtros:**
   - abas Todas, eSF, eSB, eMulti, CAPS, eAP e Consultório na Rua, com a contagem já filtrada pelos demais
     filtros;
   - busca por nome, unidade, INE ou membro;
   - filtro de unidade, só com as unidades que têm equipe;
   - filtro de situação: ativas (padrão), incompletas, inativas ou todas;
   - a unidade e a equipe escolhidas ficam na URL (`?unidade=`, `?equipe=`).
5. **Cartão da equipe:**
   - marcador com a sigla e a cor do tipo. As cores `--amber` e `--pink` do protótipo não existem nos estilos
     e viram warn e alert;
   - nome, unidade, microáreas e "inativa";
   - funções com contagem: clínicos e, nas eSF e eAP, os ACS;
   - **a barra mostra a composição mínima atendida, não a cobertura.** A cobertura depende de famílias
     cadastradas, que não existem. Com a composição completa a barra fica verde; incompleta, fica âmbar e
     diz o que falta;
   - avatares dos membros;
   - o cartão é selecionável pelo teclado (`role="option"`, Enter ou espaço).
6. **Detalhe da equipe:**
   - cabeçalho com sigla, nome, unidade, coordenação e INE;
   - chips de tipo, composição (completa ou o que falta), inativa e microáreas;
   - ações **Editar** e **Adicionar membro**. **Ver escala** fica para a ADR-0106;
   - **membros:** tabela com profissional, conselho ou matrícula, microárea, função, cargo, jornada, data de
     entrada e situação (afastamento vigente do RH). Cada linha tem o botão **Saída**;
   - **membros anteriores**, recolhidos, com período e motivo da saída;
   - **microáreas:** os quadrinhos do protótipo, com os ACS de cada uma. Nas eSF e eAP, microárea sem ACS
     fica destacada em âmbar;
   - **apoio matricial:** as equipes que a eMulti apoia e as eMulti que apoiam a equipe, com link;
   - **reunião de equipe:** dia, horário e local;
   - **famílias, pacientes, programas e indicadores do território ficam "Em breve".**
7. **Gavetas:**
   - **Equipe:**
     - unidade e tipo só no cadastro, porque o tipo não muda (ADR-0103);
     - nome, INE (10 dígitos) e microáreas;
     - na edição: coordenação, escolhida entre os membros vigentes, e o interruptor "Ativa";
     - reunião (dia, início, fim e local), com o fim depois do início;
     - na eMulti: as eSF e eAP ativas que ela apoia.
   - **Membro:**
     - profissional, só entre quem tem lotação vigente na unidade da equipe;
     - função, microárea e data de entrada (vazia vale hoje).
   - **Saída:** motivo obrigatório e data (vazia vale hoje).
   - As regras da API (lotação, exclusividade da eSF e eAP, membro repetido) voltam como mensagem no topo da
     gaveta.
8. **Integrações:**
   - **Profissionais:** "Atribuir equipe" passa a levar à tela Equipes com a gaveta **Incluir em equipe**
     aberta (`?acao=membro&matricula=`). A gaveta lista as equipes ativas da unidade de lotação do
     profissional. O botão fica desligado sem lotação vigente ou sem `EQUIPE.GERENCIAR`.
   - **Equipamentos de Saúde:** a aba Equipes da unidade lista as equipes dela, com tipo, membros, microáreas,
     composição incompleta e link para a equipe. Sem acesso à leitura de equipes, mostra um aviso.

## Consequências

- As equipes passam a ser mantidas pela tela.
- O "Em breve" de equipes some de Profissionais e de Equipamentos.
- **Continuam "Em breve":**
  - território (famílias, pacientes, cobertura);
  - programas e indicadores assistenciais;
  - sincronização com o CNES;
  - escala da equipe (ADRs 0105 e 0106).

## Testes

- **Robot de interface** (`test/ui/rh/equipes/UI_equipes.robot`, 6 casos):
  - seleção no cartão, com o nome e as microáreas no detalhe;
  - cadastro de uma eSF pela gaveta;
  - nome vazio e INE curto recusados;
  - inclusão de um profissional lotado, conferida pela API;
  - saída com o motivo obrigatório, conferida pela API;
  - "Atribuir equipe" vindo de Profissionais, com a gaveta "Incluir em equipe".
