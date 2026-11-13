# 0203 — Qualidade: segurança do paciente (Q1)

## Status

Aceita e implementada. Fatia **Q1** da Qualidade ([ADR-0202](./0202-qualidade-do-servico.md); detalhe em
[`qualidade/MODELO-QUALIDADE.md`](../qualidade/MODELO-QUALIDADE.md)).

## Contexto

- **RDC ANVISA 36/2013 e PNSP (Portaria MS 529/2013):** o serviço de saúde tem um Núcleo de Segurança do Paciente, recebe as notificações
  internas de incidentes, analisa e notifica os eventos adversos ao SNVS pelo Notivisa.
- **Prazos do Notivisa** (RDC 36/2013, art. 10): o óbito em 72 horas da ocorrência; os demais eventos adversos até o 15º dia útil do mês
  seguinte. O never event, por ser gravíssimo, segue o prazo do óbito.
- **Taxonomia:** a Classificação Internacional para a Segurança do Paciente da OMS (ICPS) separa circunstância de risco, quase erro,
  incidente sem dano e evento adverso, com o grau de dano (nenhum, leve, moderado, grave, óbito).

## Decisão

1. **Notificação** (`TB_INCIDENTE_SEGURANCA`), por qualquer profissional com `QUALIDADE.NOTIFICAR`, na unidade do acesso (403):
   - unidade, paciente opcional (404 se não existir), quando ocorreu (não futuro, 400), local, tipo, o dano percebido (opcional), o que
     aconteceu e o que foi feito na hora;
   - **anônima** ou identificada: na anônima, quem notificou **não é guardado**; na identificada, o nome só aparece para o núcleo;
   - o notificador recebe só o **protocolo** (`2026/000001`): a análise é do núcleo;
   - o tipo segue as metas internacionais e os protocolos básicos: identificação do paciente, comunicação, medicação, cirurgia ou
     procedimento, infecção, queda, lesão por pressão, dieta, equipamento, documentação, outro.
2. **Classificação pelo núcleo** (`QUALIDADE.ANALISAR`, na unidade do incidente): natureza e dano (ICPS), o tipo corrigido e o never event.
   Só o evento adverso tem dano; o evento adverso sem dano é incidente sem dano (400). Recebida ou em análise (422 nas outras); passa a
   **em análise**.
3. **Conclusão da análise:** os fatores contribuintes (paciente, profissional, equipe, tarefa, ambiente, organização, externo; ao menos um),
   a **causa raiz** (obrigatória no dano grave, no óbito e no never event, 400) e as lições aprendidas; só em análise (422).
4. **Descarte:** o que não é incidente (duplicado, fora do escopo), com o motivo; só aberto (422). Não entra no painel.
5. **Notivisa:** a obrigação é do evento adverso; o prazo é calculado (72 horas da ocorrência no óbito e no never event; o fim do 15º dia
   útil do mês seguinte nos demais, sem os feriados); vencido sem o registro, aparece em destaque. O registro guarda o número e a data (não
   futura, 400); só do evento adverso (422), uma vez (409). A notificação ao Notivisa é feita fora do sistema (domínio 20).
6. **Painel**, só com totais (`QUALIDADE.ANALISAR` ou `INDICADOR.CONSULTAR`, nas unidades do acesso): notificações, aguardando análise,
   eventos adversos, Notivisa pendente e vencido, por tipo, natureza e dano.
7. **Acesso:** `QUALIDADE.NOTIFICAR` em todo papel padrão, menos o administrador da plataforma; `QUALIDADE.ANALISAR` (dado de saúde) no papel
   novo **Núcleo de Segurança do Paciente**, com `PACIENTE.CONSULTAR` e `INDICADOR.CONSULTAR`. A leitura do incidente entra na trilha de
   auditoria com o paciente.
8. **Tela** `/qualidade/seguranca-do-paciente`, no menu principal: as abas Notificar (para todos que notificam), Incidentes (o núcleo, com
   a classificação, a análise, o Notivisa e o descarte na gaveta) e Painel.

### API

| Método | Caminho (`/api/v1/qualidade/`) | Permissão |
|---|---|---|
| POST | `incidente` | `QUALIDADE.NOTIFICAR` |
| GET | `incidentes` (`unidadeId`, `situacao`), `incidente/{id}` | `QUALIDADE.ANALISAR` |
| POST | `incidente/{id}/classificacao`, `/conclusao`, `/descarte`, `/notivisa` | `QUALIDADE.ANALISAR` |
| GET | `seguranca/painel` (`unidadeId`, `de`, `ate`) | `QUALIDADE.ANALISAR` ou `INDICADOR.CONSULTAR` |

### Migração

`V85__qualidade_seguranca_do_paciente.sql`: `tb_incidente_seguranca`.

## Alternativas descartadas

- **Notificação identificada obrigatória:** desestimula notificar (RDC 36/2013).
- **Guardar o prazo do Notivisa:** sai da ocorrência e da classificação; reclassificar mudaria o prazo guardado.
- **O notificador acompanhar a análise:** a devolutiva ao notificador é do núcleo, fora do sistema por ora; a anônima não teria a quem.

## Consequências

- **STATUS-DOS-DOMINIOS:** item 18 com a Q1 feita; faltam a Q2 e a Q3.
- **A trilha de auditoria guarda quem fez a requisição,** inclusive na notificação anônima; ela só é vista por quem tem
  `AUDITORIA.CONSULTAR`, fora dos papéis padrão (ADR-0071).
- **Fora do escopo:** o envio ao Notivisa, a devolutiva ao notificador, os feriados no prazo mensal.

## Testes

- **JUnit `IncidenteSegurancaControllerTest`:** ocorrência futura (400), paciente inexistente (404), a anônima com o protocolo; a natureza
  e o dano coerentes (400), concluir sem classificar (422), a causa raiz no dano grave (400), fatores sem repetição, nada depois de concluída
  (422); o quase erro sem Notivisa (422); o descarte fora do painel; o painel por tipo; o prazo do óbito (72 horas, vencido, registro 400 sem
  número ou futuro, 409 repetido) e o 15º dia útil de fevereiro de 2026.
- **JUnit `IncidenteSegurancaAutorizacaoControllerTest`,** com a segurança ligada: o administrador da plataforma não notifica (403); a
  recepção e o técnico de enfermagem notificam e não veem a lista; a anônima sem CPF guardado; o núcleo da unidade vê o notificador e o
  núcleo de outra unidade não vê nem classifica; o gestor vê o painel e não o incidente; sem login, 401.
- **Robot:** `SegurancaDoPaciente.robot` (CT-001 a CT-004) e `UI_seguranca_do_paciente.robot` (menu, notificação anônima, classificação).
