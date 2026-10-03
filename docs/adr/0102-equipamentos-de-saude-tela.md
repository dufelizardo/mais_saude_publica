# 0102 — Equipamentos de Saúde: tela

## Status

Aceita e implementada (parte 2 de 2). Usa a API da [ADR-0101](./0101-equipamentos-de-saude-backend.md) e segue o
protótipo `Equipamentos.html`.

## Contexto

- As unidades só eram cadastradas pela API, e o protótipo de Equipamentos de Saúde não tinha tela
  correspondente.
- **A estrutura do protótipo é lista e detalhe, não grade de cartões.**
  - Em cima, duas colunas: um mapa grande e a lista de unidades em linhas (`unit-row`), com marcador por
    tipo, nome, linha de dados, barra de ocupação e um número à direita.
  - Embaixo, o painel da unidade selecionada (`detail-card`), com cabeçalho, chips, ações e blocos de
    informação (dados gerais, horário, capacidade, status) e equipes.
  - O desenho aprovado falava em "cartões". Ao estudar o protótipo, a lista com detalhe se mostrou o elemento
    dominante, e é ela que a tela segue.
- A tela de Pacientes (ADR-0052) já adaptou essa mesma família de protótipos, com lista à esquerda e painel de
  detalhe fixo à direita (`split`, `detail-card`, `tabs`, `tl`, `empty-state`).

## Decisão

1. **Tela Equipamentos de Saúde** em `/administrativo/equipamentos`, no grupo Administrativo, antes de Modelo
   administrativo.
   - Aparece com `ORGANIZACAO.GERENCIAR` ou `ADMINISTRATIVO.CONSULTAR`.
   - Carrega sob demanda.
2. **Topo:**
   - **Nova unidade**, só com `ORGANIZACAO.GERENCIAR`;
   - **Exportar** e **Sincronizar CNES** como "Em breve".
3. **Indicadores do protótipo:**
   - unidades ativas sobre o total (e quantas fora de operação);
   - UBS (e o percentual da rede);
   - UPA, hospital, CAPS e especializadas;
   - em manutenção, obra ou inoperantes.
4. **Filtros:**
   - busca por nome ou CNES;
   - regional (supervisão regional);
   - tipo (unidades de atendimento por padrão, ou um tipo, ou os níveis de gestão);
   - situação.
   - O seletor **Mapa / Lista** fica com o Mapa em "Em breve", porque não há coordenadas.
5. **Lista e painel:**
   - **sem mapa, a lista ocupa a coluna principal** e o painel da unidade fica ao lado, fixo, como em
     Pacientes;
   - cada linha mostra:
     - o marcador com a sigla e a cor do tipo, ou de manutenção e fechada;
     - o nome;
     - uma linha com tipo, CNES, profissionais ou situação, e endereço;
     - a **barra de ocupação dos leitos**, quando há leitos;
     - à direita, a ocupação, o número de profissionais ou a situação;
   - a unidade escolhida fica na URL (`?unidade=`).
6. **Painel da unidade:**
   - cabeçalho com a sigla, o nome, o CNES, a supervisão e o responsável;
   - chips de tipo, situação, 24 horas e inativa;
   - ações **Editar** e **Ver agenda**;
   - aviso da situação quando não está em operação;
   - abas:
     - **Dados gerais:** nome, CNES, tipo, endereço, contato e responsável;
     - **Operacional:**
       - horário de funcionamento por dia, com hoje destacado e os dias fechados (a tabela do protótipo);
       - capacidade, com a ocupação dos leitos (consultórios e vagas como "Em breve");
       - status, com o motivo e a previsão;
       - os botões **Editar horário** e **Mudar situação**;
     - **Equipes:**
       - os profissionais lotados (lotação vigente do RH), com os avatares do protótipo e a lista com cargo,
         conselho e jornada;
       - as equipes de Saúde da Família como "Em breve";
     - **Vinculações:** unidade superior, supervisão regional, unidades vinculadas (que abrem no painel),
       setores e leitos, com os links para as telas;
     - **Histórico:** as mudanças de situação na linha do tempo, com o link para a trilha completa na
       Auditoria.
7. **Gavetas:**
   - **Unidade:**
     - nome, tipo (só no cadastro) e CNES;
     - unidade superior só no cadastro, com a lista limitada ao nível acima do tipo;
     - supervisão regional, contato, endereço e CPF do responsável;
     - o endereço vai inteiro ou não vai: com algum campo preenchido, a gaveta pede os obrigatórios, porque
       a API exige o endereço completo.
   - **Horário:**
     - 24 horas;
     - por dia: aberto, 1º turno e 2º turno opcional (para o intervalo de almoço).
   - **Situação:**
     - as quatro situações, com motivo e previsão fora de operação;
     - avisa que obra e inoperante fecham a agenda.
8. A ocupação no protótipo é de capacidade geral. Aqui ela é **a dos leitos**, o único dado de ocupação que
   existe por unidade. As vagas da agenda são de cada profissional.

## Consequências

- A rede de unidades passa a ser mantida pela tela, sem as rotas por nome.
- A tela e Pacientes usam o mesmo arranjo de lista e detalhe.
- **Continuam "Em breve":** mapa e coordenadas, sincronização com o CNES, exportação, consultórios, vagas
  semanais da unidade, microáreas, acessibilidade e equipes de Saúde da Família (em `PENDENCIAS.md`).

## Testes

- **Robot de interface** (`test/ui/administrativo/equipamentos/UI_equipamentos.robot`, 6 casos):
  - seleção na lista, com o nome e o CNES no painel;
  - cadastro com endereço completo;
  - nome vazio e CNES curto recusados;
  - horário de segunda salvo;
  - unidade posta em obra, com o aviso;
  - edição do nome.
