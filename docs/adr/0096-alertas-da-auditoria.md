# 0096 — Alertas da auditoria: detecção e análise

## Status

Aceita e implementada (parte 1 de 2). A tela está na [ADR-0097](./0097-alertas-da-auditoria-tela.md). Resolve a pendência de alertas das ADRs 0071,
0076 e 0082.

## Contexto

- A trilha registra alterações, leituras de dado de saúde, recusas e logins (ADR-0070). Ela pode ser
  consultada (ADR-0071), exportada e tem retenção definida (ADR-0082).
- Ninguém era avisado de um padrão suspeito. Para achar, era preciso alguém procurar.
- Na prática da "quebra de vidro" (ADR-0076), o acesso justificado ao prontuário precisa ser **revisado
  depois** pela supervisão, e isso não tinha onde acontecer.

## Decisão

1. **Detecção periódica**, a cada 5 minutos (`app.auditoria.alertas.cron`):
   - roda sobre os eventos recentes, por consultas agregadas, sem pesar na gravação da trilha;
   - também pode rodar na hora, por `POST /auditoria/alerta/deteccao` ("Verificar agora");
   - ganha o índice `ix_auditoria_acao_data` (ação e data) na trilha.
2. **Regras.** Todos os limites são configuráveis em `app.auditoria.alertas.*`.

   | Tipo | Quando | Severidade |
   |---|---|---|
   | `RECUSAS_SEGUIDAS` | 10 recusas (403) do mesmo usuário em 15 minutos | alta |
   | `LOGIN_RECUSADO` | 5 logins recusados do mesmo CPF, ou da mesma origem sem CPF, em 15 minutos | alta |
   | `LEITURA_EM_MASSA` | dados de 30 pacientes diferentes abertos pelo mesmo usuário em 1 hora | alta |
   | `LEITURA_FORA_DO_HORARIO` | 3 leituras de dado de saúde entre 22h e 6h numa noite, fora de unidade 24 horas | média |
   | `ACESSO_JUSTIFICADO` | todo prontuário aberto sem vínculo, por acesso justificado | média |

   - **Unidade 24 horas** (`app.auditoria.alertas.unidades-24h`, padrão hospital e UPA): ler de madrugada
     é normal ali.
   - Quando o evento não tem unidade (o prontuário, por exemplo), vale onde a pessoa tem acesso. Quem tem
     acesso em unidade 24 horas não gera esse alerta.
3. **Sem alerta repetido:**
   - nas regras de janela (recusas, login e leitura em massa), enquanto o padrão continua, o alerta aberto
     do mesmo sujeito cresce em vez de abrir outro;
   - nas demais, uma chave única por episódio: por noite e usuário, ou por evento de acesso justificado.
4. **Alerta** (`TB_ALERTA_AUDITORIA`, migração V5):
   - campos: tipo, severidade, sujeito (CPF, ou "IP ..." no login sem CPF), unidade, paciente quando
     houver, primeiro e último evento, quantidade e descrição;
   - guarda só referências e contagem, nunca conteúdo;
   - **não se apaga**: começa **aberto** e termina **procedente** ou **improcedente**, com parecer (10 a 2000
     caracteres), quem analisou e quando.
5. **Unidade do alerta:** a do evento, quando há uma só. Senão, a unidade do acesso de quem foi alertado,
   para que quem audita aquela unidade veja.
6. **Acesso:**
   - a mesma permissão e o mesmo escopo da trilha (`AUDITORIA.CONSULTAR`, ADR-0071);
   - alerta sem unidade (como login de CPF desconhecido) só para quem audita a rede inteira;
   - **ninguém analisa alerta sobre si mesmo** (422);
   - o detalhe é leitura auditada, e a análise entra na trilha.
7. **API** em `/api/v1/auditoria/alerta/`:
   - `GET` (filtros de situação, tipo e severidade);
   - `GET resumo` (abertos, abertos de severidade alta, últimos 7 dias e procedentes);
   - `GET {uuid}`;
   - `POST {uuid}/analise`;
   - `POST deteccao`.
8. **Aviso por e-mail** dos alertas de severidade alta para quem audita a unidade (ou a rede inteira). O
   endereço é o do cadastro de profissional de mesmo CPF.
   - Só funciona com SMTP configurado; sem ele, o alerta fica só na tela.
   - O e-mail não traz nome de paciente.

## Consequências

- O padrão suspeito chega a quem audita em até 5 minutos, e o acesso justificado passa a ter revisão.
- Os limites são um ponto de partida: cada rede ajusta nas propriedades conforme o volume real, para não
  ter alerta demais.
- **Fora do escopo** (em `PENDENCIAS.md`):
  - detecção em tempo real;
  - regras configuráveis pela tela;
  - integração com SIEM;
  - aviso ao titular dos dados.

## Testes

- **JUnit, regras** (`DeteccaoAlertasAuditoriaTest`):
  - as recusas abrem um alerta, que cresce enquanto continuam; abaixo do limite, nada;
  - o login recusado é agrupado por CPF ou pela origem;
  - leitura em massa;
  - leitura de madrugada em UBS gera alerta, e no hospital não; de dia não conta; não repete na mesma noite;
  - acesso justificado vira um alerta só.
- **JUnit com autorização ligada** (`AlertaAuditoriaControllerTest`):
  - o auditor vê só os alertas da sua unidade, e alerta sem unidade fica de fora;
  - o de outra unidade recebe 403, e quem não audita também;
  - o parecer curto dá 400;
  - o alerta sobre o próprio auditor dá 422;
  - a segunda análise dá 422, e o alerta inexistente, 404.
- **Robot de API** (`test/auditoria/alerta_auditoria`): 5 casos.
  - Cinco logins recusados viram um alerta.
  - Lista e resumo.
  - 404.
  - Análise, e a análise repetida.
  - Parecer curto.
