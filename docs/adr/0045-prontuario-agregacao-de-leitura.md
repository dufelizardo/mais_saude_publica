# 0045 — Prontuário como agregação de leitura no backend

## Status

Aceita e implementada (sexta e última fatia da onda Operação Assistencial — [PR #214](https://github.com/dufelizardo/mais_saude_publica/pull/214)).

## Contexto

Com `Paciente` (ADR-0040), `Atendimento` (ADR-0041), `Agendamento` (ADR-0042), `Consulta`
(ADR-0043) e `Procedimento` (ADR-0044) implementados, falta fechar a onda com o `Prontuário`
(#6) — já decidido na ADR-0039 (decisão 6) como **agregação de leitura**, não uma entidade
própria, citando como precedente o "Histórico funcional consolidado" (ADR-0026) do RH.

Reler a ADR-0026 com atenção revelou um detalhe que a ADR-0039 não capturou por completo: o
histórico funcional do RH não é só "uma agregação em vez de uma tabela nova" — é uma agregação
**inteiramente no frontend** (`computed()` do Angular sobre sinais que a tela `profissional-perfil.ts`
já carregava), com **zero código novo no backend**. A própria ADR-0026 chegou a rejeitar
explicitamente um endpoint de agregação no backend, pelo motivo de que os 4 endpoints-fonte já
estavam sendo consumidos pela mesma tela — um quinto endpoint seria "trabalho redundante sem ganho
real".

Essa condição não se repete aqui: a onda Assistência não tem **nenhuma tela** ainda (nenhum
frontend foi construído para Paciente/Atendimento/Agendamento/Consulta/Procedimento). Não existe
uma tela já carregando os 3 endpoints-fonte para um `computed()` combinar — construir 3 telas só
para depois combinar seus dados no cliente seria ordens de magnitude mais trabalho do que um
endpoint de leitura simples no backend.

## Decisão

- Novo endpoint **somente leitura** `GET /api/v1/prontuario/{pacienteId}`, sem POST/PATCH/DELETE —
  não existe entidade própria para criar ou atualizar.
- `ProntuarioService` monta a resposta sob demanda: busca o `Paciente`, todos os `Atendimento`s
  dele (`AtendimentoRepository.findByPacienteUuid`), para cada um todas as `Consulta`s
  (`ConsultaRepository.findByAtendimentoUuid`), e para cada uma todos os `Procedimento`s
  (`ProcedimentoRepository.findByConsultaUuid`) — três novos métodos de busca, nenhuma tabela nova.
- DTOs de composição (`ProntuarioResponseDto` → `ProntuarioAtendimentoDto` → `ProntuarioConsultaDto`)
  reaproveitam `AtendimentoResponseDto`/`ConsultaResponseDto`/`ProcedimentoResponseDto` sem duplicar
  campos — só aninham.
- **Desvio consciente do mecanismo exato da ADR-0026** (agregação no backend, não no frontend) —
  mantendo o princípio que ela e a ADR-0039 estabeleceram (nenhuma tabela nova), mas adaptando o
  *onde* a agregação acontece à realidade de que a Assistência ainda não tem frontend algum.
  Quando as telas de Paciente/Atendimento/Consulta/Procedimento forem construídas, reavaliar se o
  endpoint de Prontuário continua sendo o caminho certo ou se vira um `computed()` como o do RH —
  não é uma decisão definitiva, é a que faz sentido dado o que existe hoje.

## Trade-offs considerados

**Agregação só no frontend, esperando as telas de Atendimento/Consulta/Procedimento existirem
(rejeitada por ora)**
- ✅ Seguiria o mecanismo exato da ADR-0026 (zero código de agregação no backend).
- ❌ Bloquearia qualquer uso do Prontuário até 3 telas novas serem construídas — nenhum requisito
  concreto pede isso agora, e o backend já tem tudo que precisa para responder essa pergunta com
  3 queries simples.

**Endpoint de escrita/cache que persiste o Prontuário consolidado (rejeitada)**
- ✅ Uma leitura rápida sem recalcular a cada chamada.
- ❌ Reintroduziria exatamente o problema que a ADR-0039 decisão 6 já rejeitou: duplicar dado que já
  vive em `Atendimento`/`Consulta`/`Procedimento`, com risco de ficar desatualizado.

## Consequências

**Positivas**: fecha a onda "Operação Assistencial" (Paciente → Atendimento → Agendamento →
Consulta/Procedimento → Prontuário) sem nenhuma tabela nova além das 5 entidades já decididas;
qualquer cliente (futura tela, integração) já pode consultar o histórico completo de um paciente
numa única chamada.

**Negativas / pendências**: 3 consultas em cascata (`findBy...`) por chamada — aceitável para o
volume esperado agora; se o volume de atendimentos por paciente crescer muito, revisitar com
paginação ou uma consulta com `JOIN FETCH`. Nenhuma tela consome este endpoint ainda.

## Referências

- [ADR-0039](./0039-mapa-de-dominios-e-prioridades-de-arquitetura.md) — decisão 6 (Prontuário como
  agregação), que esta ADR implementa e refina.
- [ADR-0026](./0026-historico-funcional-consolidado.md) — precedente cujo mecanismo exato
  (agregação no frontend) não se aplica aqui por falta de telas na Assistência, mas cujo princípio
  (sem tabela nova) é mantido.
- [ADR-0040](./0040-paciente-primeira-entidade-da-assistencia.md),
  [ADR-0041](./0041-atendimento-registra-entrada-do-paciente-na-rede.md),
  [ADR-0043](./0043-consulta-registrada-durante-o-atendimento.md),
  [ADR-0044](./0044-procedimento-realizado-durante-a-consulta.md) — as três entidades-fonte
  agregadas aqui.
