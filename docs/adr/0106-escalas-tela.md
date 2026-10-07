# 0106 — Escalas: tela

## Status

Aceita e implementada (parte 4 de 4 de Equipes e Escalas). Usa a API da [ADR-0105](./0105-escalas-backend.md) e
segue o protótipo `Escalas.html`.

## Contexto

- A ADR-0105 criou os turnos com data, as vagas, a designação, a cópia de semana e a semana da unidade, só com API.
- **A estrutura do protótipo:**
  - indicadores;
  - abas de visualização: Jornada semanal, Plantões 24h, Férias & licenças e Banco de horas;
  - barra com a navegação por semana e os filtros de unidade, equipe e função;
  - a **grade semanal** (`sched`): uma linha por profissional, uma coluna por dia, hoje e o fim de semana destacados,
    e blocos coloridos por tipo de turno, férias, licença, capacitação e folga;
  - o resumo da semana;
  - embaixo, duas colunas: os próximos plantões (`plantao`) e as férias e licenças (`vrow`), com uma linha do tempo
    de 30 dias.
- A grade é o elemento dominante, e é ela que a tela segue.

## Decisão

1. **Tela Escalas** em `/rh/escalas`, no grupo Recursos Humanos, depois de Equipes.
   - Aparece com `ESCALA.GERENCIAR`, `EQUIPE.GERENCIAR`, `RH.CONSULTAR`, `RH.GERENCIAR` ou `ADMINISTRATIVO.CONSULTAR`,
     as mesmas permissões de leitura da API.
   - Carrega sob demanda.
   - Criar, editar, designar, remover e copiar só aparecem com `ESCALA.GERENCIAR`.
2. **Topo:**
   - **Novo turno** e **Copiar semana**;
   - **Exportar** fica como "Em breve".
3. **Indicadores:**
   - horas previstas na semana, com o número de profissionais;
   - cobertura dos turnos (turnos com profissional sobre o total), com as vagas sem cobertura;
   - plantões e sobreavisos;
   - pessoas em férias ou licença na semana.
4. **Abas:**
   - Jornada semanal;
   - Plantões;
   - Férias & licenças;
   - **Banco de horas** desligada ("Em breve"), porque depende do ponto.
5. **Barra:**
   - navegação de semana (anterior, esta semana, próxima);
   - unidade: só unidades de atendimento, sem os níveis de gestão. Sem acesso à rede, vale a lista simples de
     unidades;
   - equipe da unidade;
   - busca por profissional ou cargo;
   - a unidade, a semana e a equipe ficam na URL (`?unidade=&semana=&equipe=`).
6. **Grade semanal:**
   - cabeçalho com os dias, hoje e o fim de semana destacados como no protótipo;
   - **linha do profissional:**
     - avatar, nome e cargo;
     - horas da semana sobre a jornada contratada, em vermelho quando passa da jornada;
     - um marcador "⚠" com a quantidade de alertas de jornada e descanso, com o texto no `title` e no `aria-label`;
   - **célula do dia:**
     - férias, licença ou afastamento do RH. **Licença médica aparece só como "Licença"**;
     - os turnos com horário, atividade ou tipo e horas, nas cores do protótipo. Sobreaviso e vaga ganharam blocos
       tracejados próprios;
     - turno de outra unidade aparece esmaecido, com o nome dela, e não abre;
     - sem turno nem ausência, a **folga**;
   - **ações na grade:**
     - clicar na folga de hoje em diante abre o **Novo turno** já com o profissional e o dia;
     - clicar num turno ainda não terminado abre a edição;
   - **linha "Vagas abertas"** no fim, com cada vaga ("Sem médico"). Clicar abre a designação;
   - a grade é uma tabela para leitores de tela (`role="table"`, linhas com `display: contents`), e os blocos clicáveis
     são botões com rótulo.
7. **Resumo da semana:**
   - total previsto e média por profissional;
   - **realizado "Em breve"**, porque depende do ponto;
   - em férias ou licença;
   - não coberto, com a contagem de pessoas com alerta.
8. **Plantões:**
   - os turnos de noite, plantão e sobreaviso da unidade, **agrupados por horário, tipo e local**: um plantão do
     protótipo tem vários profissionais;
   - cada cartão mostra:
     - a data;
     - o tipo e o local;
     - o horário e a equipe em avatares;
     - "Sem médico" com **Designar** quando há vaga, ou "Coberto" com **Trocar**.
9. **Férias & licenças:**
   - os afastamentos do RH dos próximos 30 dias, com tipo, período e dias;
   - a linha do tempo de 30 dias do protótipo, com até três faixas;
   - a "cobertura" de quem está fora fica "Em breve".
10. **Gavetas:**
    - **Turno:**
      - no cadastro, profissional (ou vaga aberta);
      - tipo: noite e plantão desligados em unidade que não é 24 horas;
      - função, obrigatória na vaga;
      - data, início e fim. O fim antes do início cai no dia seguinte, e cada tipo sugere o seu horário;
      - equipe e atividade ou local;
      - na edição: **Trocar** (ou **Designar**) **profissional** e **Remover turno**, com confirmação em dois cliques.
    - **Designação ou troca:** o turno, o profissional (com as horas da semana) e o motivo da troca.
    - **Copiar semana:**
      - o destino é a semana da tela, e a origem é qualquer dia da semana a copiar (sugere a anterior);
      - o resultado fica na própria gaveta: quantos turnos entraram e os que ficaram de fora, com o motivo.
    - As regras da API voltam como mensagem no topo da gaveta. Os **alertas** do turno salvo aparecem no aviso.
11. **Integrações:**
    - **Profissionais:** "Ver escala" leva à escala da unidade de lotação.
    - **Equipes:** "Ver escala" do detalhe leva à escala da unidade já filtrada pela equipe.

## Consequências

- A escala passa a ser montada pela tela.
- O "Em breve" de escala some de Profissionais e de Equipes.
- **Continuam "Em breve":**
  - banco de horas e horas realizadas;
  - exportação;
  - cobertura de férias;
  - filtro por função.

## Testes

- **Robot de interface** (`test/ui/rh/escalas/UI_escalas.robot`, 7 casos):
  - o profissional lotado na grade e a troca de semana;
  - turno criado clicando na folga;
  - vaga sem função recusada;
  - vaga designada, conferida pela API;
  - cópia da semana anterior, conferida pela API;
  - remoção com confirmação, conferida pela API;
  - aba Plantões vazia numa unidade sem plantão.
