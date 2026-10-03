# 0097 — Alertas da auditoria: tela

## Status

Aceita e implementada (parte 2 de 2). Usa a API da [ADR-0096](./0096-alertas-da-auditoria.md). Fecha o item 4 da
sequência pós-v1.4.0.

## Contexto

- A ADR-0096 detecta padrões suspeitos na trilha e guarda os alertas até alguém analisar.
- Sem tela, o alerta só existia na API e no e-mail, e ninguém tinha onde concluir a análise.

## Decisão

1. **A tela Auditoria ganha abas:**
   - **Eventos**: a trilha, como estava;
   - **Alertas**, com o número de abertos na própria aba.
   - O "Exportar CSV" aparece só na aba Eventos. `?aba=alertas` abre direto nos alertas, que é o link do
     e-mail de aviso.
2. **Aba Alertas** (componente `alertas-auditoria`, filho da tela):
   - indicadores: abertos, abertos de severidade alta, detectados nos últimos 7 dias e procedentes;
   - filtros de situação (abre em **Abertos**), tipo e severidade;
   - **Verificar agora**, que roda a detecção na hora (a rotina continua a cada 5 minutos);
   - tabela: quando foi detectado, tipo e severidade, quem (nome ou CPF, ou a origem no login sem CPF),
     unidade, período e quantidade de eventos, situação, e o botão **Analisar** ou **Ver**.
3. **Gaveta do alerta:**
   - o que a regra quer dizer e a descrição do caso;
   - quem, unidade, primeiro e último evento, quantidade, data da detecção e paciente, quando houver;
   - **Ver eventos de …** volta para a aba Eventos com a trilha filtrada pelo CPF e pelos dias do alerta.
     Para isso, a aba Eventos passa a aceitar `?desde=` e `?ate=`;
   - no alerta aberto: **Procedente** ou **Improcedente** e o parecer (de 10 a 2000 caracteres). No
     analisado: o parecer, quem analisou e quando;
   - abrir a gaveta relê o alerta pela API, e essa leitura entra na trilha.
4. **Contador no menu:** o item Auditoria mostra os alertas abertos para quem pode ver a tela.
   - Um serviço único (`AlertaAuditoriaService.abertos`) guarda o número. O menu lê dele, e a aba atualiza
     o número depois de cada análise.
   - Sem acesso, o contador não aparece.
5. A trilha passa a mostrar o recurso **"Alerta da auditoria"** nas leituras e análises de alerta.

## Consequências

- Quem audita vê o que precisa de atenção logo ao entrar no sistema e conclui a análise no mesmo lugar onde
  investiga os eventos.
- O contador é lido ao entrar no sistema e quando a aba Alertas é aberta. Ele não se atualiza sozinho; a
  atualização em tempo real fica em `PENDENCIAS.md`.

## Testes

- **Robot de interface** (`test/ui/administracao/auditoria/UI_auditoria_alertas.robot`, 4 casos):
  - análise procedente pela gaveta;
  - a gaveta exige conclusão e parecer;
  - "Ver eventos" leva à aba Eventos filtrada pelo CPF;
  - o contador aparece no menu.
