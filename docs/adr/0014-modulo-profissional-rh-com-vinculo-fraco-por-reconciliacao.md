# 0014 — Módulo Profissional/RH com vínculo fraco resolvido por reconciliação

## Status

Aceita e implementada.

## Contexto

A RN02 do estudo externo trazido pelo usuário (ver ADR-0013) apontava que toda unidade de saúde
precisa de um "responsável" — o que exige um domínio de RH que hoje não existe no projeto. Um
requisito real e comum nesse tipo de cadastro apareceu junto: a unidade de saúde e o profissional
responsável por ela nem sempre são cadastrados na mesma hora, na mesma ordem. Exigir o profissional
já existente como pré-condição para criar a unidade adicionaria fricção sem necessidade.

A decisão do usuário, confirmada explicitamente: a unidade de saúde pode ser cadastrada **sem** o
responsável, informando só o CPF dele; o vínculo com o cadastro real do profissional (quando ele
existir) se resolve depois, automaticamente. Foi confirmado também que o módulo de RH fica no
**mesmo monólito** Spring Boot — não vira um serviço separado, o que evitaria a complexidade de
infraestrutura (deploy, autenticação entre serviços) desproporcional ao pedido original.

## Decisão

- Nova entidade `Profissional` (`TB_PROFISSIONAL`, `cpf` único), flat-by-layer no mesmo padrão do
  resto do projeto (sem subpacote dedicado) — `models/Profissional.java`,
  `repositories/ProfissionalRepository.java`, `services/version1/ProfissionalService.java`,
  `controllers/version1/ProfissionalController.java` (`/api/v1/profissional/`).
- `UnidadeDeSaude` ganha dois campos novos: `responsavelCpf` (String, sempre gravável, mesmo sem
  `Profissional` correspondente) e `responsavel` (`@ManyToOne` para `Profissional`, nullable —
  só preenchido quando o vínculo é resolvido).
- **Vínculo fraco resolvido por reconciliação síncrona nos dois sentidos, mais um job agendado
  como rede de segurança:**
  1. Ao criar uma `UnidadeDeSaude` (ou atualizar seu responsável via `PATCH
     responsavel/{nome}`) com um `responsavelCpf`, tenta achar um `Profissional` já existente com
     aquele CPF e vincula na mesma transação (`UnidadeSaudeService.vincularResponsavelSeJaExistir`).
  2. Ao criar um `Profissional`, busca toda `UnidadeDeSaude` com aquele CPF pendente
     (`responsavelCpf` preenchido, `responsavel` nulo) e vincula todas na mesma transação
     (`ProfissionalService.reconciliarUnidadesPendentes`, via
     `UnidadeDeSaudeRepository.findByResponsavelCpfAndResponsavelIsNull`).
  3. `ReconciliacaoResponsavelScheduler` (`@Component`, `@Scheduled(cron =
     "${reconciliacao.responsavel.cron:0 0 * * * *}")`, hourly por padrão) varre toda unidade com
     vínculo pendente e resolve o que os dois casos síncronos não cobriram — só necessário para
     condições de corrida ou dados corrigidos manualmente no banco. Exigiu adicionar
     `@EnableScheduling` em `MaissaudepublicaApplication.java` (não existia antes no projeto).
- `UnidadeSaudeResponseDto` expõe tanto `responsavelCpf` (sempre, se setado) quanto
  `responsavelNome` (só preenchido depois que o vínculo resolve) — permite ver o estado
  pendente-vs-resolvido numa única consulta, sem precisar de um endpoint separado de status.

## Trade-offs considerados

**Reconciliação síncrona nos dois sentidos + job como rede de segurança (escolhida)**
- ✅ Cobre o caso comum (cadastro em qualquer ordem) instantaneamente, sem esperar o próximo tick
  do cron — o custo da consulta já seria pago de qualquer forma (precisa checar se o CPF já
  existe).
- ✅ O job agendado é só a rede de segurança para os casos raros (corrida entre duas transações
  concorrentes, ou um `responsavel_cpf` corrigido direto no banco) — não é o mecanismo principal,
  então pode rodar com baixa frequência (hourly) sem prejudicar a experiência normal.
- ❌ Mais lógica do que um vínculo obrigatório simples — mas é a lógica que a decisão de negócio
  (permitir cadastro sem responsável) exige de qualquer forma.

**Cadastro de unidade de saúde exigir o profissional já existente (rejeitada)**
- ✅ Mais simples: `responsavel` seria uma FK obrigatória, sem CPF solto nem reconciliação.
- ❌ Rejeitada a pedido explícito do usuário: força uma ordem de cadastro que nem sempre reflete a
  realidade operacional (a unidade pode existir antes do responsável estar formalmente cadastrado
  no sistema).

**Só o job agendado, sem reconciliação síncrona (rejeitada)**
- ✅ Menos pontos de código tocando o vínculo.
- ❌ Rejeitada: o caso comum (cadastrar profissional minutos depois da unidade, ou vice-versa)
  ficaria com o vínculo visivelmente "pendente" até o próximo tick do cron (até 1h de atraso no
  pior caso, com o cron padrão) — latência desnecessária para o caminho mais frequente.

**Módulo de RH como serviço separado (rejeitada)**
- ✅ Isolamento total do domínio de saúde.
- ❌ Rejeitada a pedido explícito do usuário: exigiria infraestrutura nova (deploy próprio,
  contrato de API ou autenticação entre serviços) fora do escopo do pedido original — o monólito
  atual já tem workspace, testes e pipeline prontos para receber o domínio novo.

## Consequências

**Positivas**
- Unidade de saúde e profissional podem ser cadastrados em qualquer ordem, sem bloquear um pelo
  outro — reflete o fluxo operacional real.
- O vínculo se resolve automaticamente, sem exigir uma segunda chamada manual de "revincular"
  depois que o profissional é cadastrado (embora `PATCH responsavel/{nome}` continue disponível
  para corrigir/trocar o CPF manualmente).
- Nenhuma infraestrutura nova: RH vive no mesmo monólito, mesmo banco, mesma pipeline de CI/CD.

**Negativas / pendências**
- `responsavelCpf` não tem validação de formato (dígito verificador, máscara) — só `@NotBlank`
  quando enviado via `UnidadeSaudeResponsavelRequestDto`; igual ao padrão do resto do projeto
  (nenhum outro campo textual do domínio hoje valida formato).
- O job agendado varre toda a tabela `UnidadeDeSaude` a cada execução (`findAll()`) — aceitável no
  volume atual do projeto; se a tabela crescer muito, precisaria de uma consulta filtrada
  (`responsavel IS NULL AND responsavel_cpf IS NOT NULL`) em vez de varrer tudo em memória.
- `docs/adr/DER-atual.md` fica desatualizado (não inclui `TB_PROFISSIONAL` nem as colunas novas em
  `TB_UNIDADE_DE_SAUDE`) — mesma pendência já registrada na ADR-0013, não resolvida nesta rodada.
