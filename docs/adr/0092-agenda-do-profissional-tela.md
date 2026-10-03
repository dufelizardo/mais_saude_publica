# 0092 — Agenda do profissional: tela

## Status

Aceita e implementada (parte 2 de 2). Usa a API da [ADR-0091](./0091-agenda-do-profissional.md) e segue o
protótipo `prototipo/Agenda.html`.

## Contexto

- A ADR-0091 criou o backend: blocos por profissional e unidade, bloqueios, férias e afastamentos do RH,
  vagas, encaixe e falta, a agenda dia a dia e o resumo.
- O protótipo mostra:
  - calendário semanal em grade de horas;
  - indicadores de marcações, ocupação contra a meta, encaixes e faltas;
  - filtros de profissional, unidade, especialidade e tipo;
  - mini-mês, próximas marcações de hoje e legenda.
- Na Regulação, o regulador digitava a data da vaga sem ver a agenda de quem ia atender.

## Decisão

1. **Tela Agenda** em `/assistencia/agenda`, no grupo Assistência, entre Pacientes e Atendimentos.
   - Aparece com `AGENDAMENTO.GERENCIAR`, `ATENDIMENTO.GERENCIAR` ou `REGULACAO.REGULAR`.
   - Carrega sob demanda.
2. **A agenda é de um profissional numa unidade.**
   - Sem os dois, a tela pede a escolha.
   - Abre na agenda de quem está logado, quando é profissional.
   - A URL guarda profissional, unidade, data e visão.
3. **Topo:**
   - navegação de datas (anterior, Hoje, próximo);
   - visões **Dia** e **Semana**;
   - indicadores do período: marcações (com as de hoje), ocupação contra a meta operacional de 85%,
     encaixes e faltas.
4. **Calendário**, portado do protótipo:
   - grade de horas que cobre das 7h às 19h e se amplia para caber o que houver fora disso;
   - linha de "agora" no dia de hoje.
   - **Itens:**
     - a vaga livre é clicável e abre a marcação naquele horário;
     - a consulta aparece em azul;
     - o procedimento e o exame, em lilás;
     - o encaixe, tracejado;
     - a falta, riscada;
     - bloqueio, férias e afastamento, em hachura.
   - Itens que se sobrepõem dividem a largura só entre si.
   - Clicar numa marcação abre a gaveta com **Registrar falta** e **Cancelar marcação**. A vaga cancelada
     volta para a agenda.
5. **Painel lateral:**
   - mini-mês, que navega pelos dias;
   - próximas marcações de hoje;
   - **agenda recorrente** (blocos, com "+ Bloco" e "Encerrar");
   - **bloqueios do período** (com "+ Bloqueio" e "Remover");
   - legenda.
   - O backend ganhou `GET /agenda/bloqueio`, para listar os bloqueios do período.
6. **Gavetas:**
   - **nova marcação:** na vaga clicada; sem vaga, já vem como encaixe;
   - **marcação:** detalhe, falta e cancelamento;
   - **bloco:** mostra quantas vagas por dia ele gera;
   - **encerrar bloco:** com o último dia;
   - **bloqueio:** do profissional nesta unidade ou da unidade inteira.
7. **Regulação:** na autorização (com "Quem vai atender") e no agendamento da executante, **"Ver vagas
   livres de quem vai atender"** lista as próximas vagas da agenda daquele profissional na unidade
   executante. Um clique preenche a data.
8. **Em breve**, porque dependem de domínios que não existem: visão Mês, exportação, filtro de
   especialidade e atividade coletiva na legenda.

## Consequências

- A agenda passa a ser o lugar de marcar.
- A lista de agendamentos em Atendimentos continua como estava. As marcações feitas lá, sem unidade,
  aparecem na agenda do profissional.
- O regulador agenda numa vaga que existe, em vez de digitar uma data.

## Testes

- **Robot de interface** (`test/ui/assistencia/agenda/UI_agenda.robot`):
  - marcar clicando na vaga;
  - encaixe;
  - falta;
  - criar bloco e ver as vagas;
  - bloquear;
  - estado sem filtro.
- **Regulação** (`UI_regulacao.robot`): autorizar escolhendo a vaga livre da agenda.
- **JUnit e Robot de API** da listagem de bloqueios.
