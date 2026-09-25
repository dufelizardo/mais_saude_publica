# 0046 — Telas de frontend da onda Assistência

## Status

Aceita e implementada (F1 a F6 — [PR #215](https://github.com/dufelizardo/mais_saude_publica/pull/215),
[#216](https://github.com/dufelizardo/mais_saude_publica/pull/216),
[#217](https://github.com/dufelizardo/mais_saude_publica/pull/217),
[#218](https://github.com/dufelizardo/mais_saude_publica/pull/218),
[#219](https://github.com/dufelizardo/mais_saude_publica/pull/219),
[#220](https://github.com/dufelizardo/mais_saude_publica/pull/220)).

## Contexto

O backend da onda "Operação Assistencial" está completo e mergeado em `developer`: `Paciente`
(ADR-0040), `Atendimento` (ADR-0041), `Agendamento` (ADR-0042), `Consulta` (ADR-0043),
`Procedimento` (ADR-0044) e o endpoint de agregação `Prontuário` (ADR-0045). Nenhuma tela de
frontend existia até agora. O módulo Administrativo já tem um precedente forte e documentado
([ADR-0018](./0018-app-shell-e-decisoes-de-frontend-do-modulo-rh.md),
[ADR-0038](./0038-app-shell-dinamico-e-telas-de-frontend-do-setor-administrativo.md),
[`docs/frontend/PADRAO-TELAS-INTERNAS.md`](../frontend/PADRAO-TELAS-INTERNAS.md)): componentes
standalone, sem NgModules, sem biblioteca de componentes própria, `<app-modal>` compartilhado,
FKs por identificador memorizável (matrícula) como input de texto simples, FKs pra catálogo já
carregado como `<select>`. Este ADR aplica esse mesmo padrão às 6 telas da Assistência
(`Paciente`, `Atendimento`, `Agendamento`, `Consulta`, `Procedimento`, `Prontuário`) e documenta as
decisões que não têm precedente direto no código existente.

## Decisão

1. **Novo `nav-group` "Assistência"** na sidebar, crescendo um item por fase conforme cada tela é
   entregue (F1 Pacientes, F2 Atendimentos, F3 Agendamentos, F4 Consultas, F5 Procedimentos, F6
   Prontuário) — nunca um link pra rota que ainda não existe, diferente do que a ADR-0038 registra
   para o Administrativo (onde o grupo inteiro nasceu de uma vez).
2. **`profissionalMatricula`** (presente em `Atendimento`, `Agendamento`, `Consulta` e
   `Procedimento`) é sempre um input de texto simples, validado só no submit via o banner de erro
   padrão — mesmo padrão de `Setor.matriculaResponsavel` (ADR-0038 decisão 2), mesmo sendo
   obrigatório aqui (lá era opcional). Na edição, `profissionalNome` (já vem na resposta) aparece
   como dica somente-leitura ao lado do campo.
3. **FKs pra entidades sem identificador memorizável** (`pacienteId`, `unidadeId`, `setorId`,
   `agendamentoId`, `atendimentoId`, `consultaId`) são sempre `<select>` populado por `listar()`
   do service correspondente — padrão dominante já usado em `profissional-cadastro.html`
   (`unidadeId`/`cargoId`). Campos opcionais (`setorId` em `Atendimento`, `agendamentoId` em
   `Atendimento`) ganham uma opção "Nenhum" no topo da lista.
4. **Sem filtro de tipo no `<select>` de `unidadeId`** — diferente do filtro "só tipos
   assistenciais" que `PerfilPorTipoUnidade` aplica sobre `TipoUnidadeDeSaude` (ADR-0038 decisão
   5). `UnidadeSaudeResponseDto` do frontend hoje só expõe `{uuid, nome}` (sem `tipo`); replicar o
   filtro exigiria estender esse DTO no frontend (e o endpoint que o alimenta), e o único outro
   call site desse mesmo `<select>` (`profissional-cadastro.ts`) também não filtra. Nenhum
   requisito concreto pede essa restrição agora.
5. **`Procedimento.tipo` é input de texto livre**, não um `<select>` — reflete a própria decisão do
   backend (ADR-0044): é um catálogo aberto ("CIRURGIA, EXAME, etc."), diferente dos enums fechados
   de `TipoAtendimento`/`TipoAgendamento`/`TipoConsulta`.
6. **`Prontuário` é uma tela de busca-e-exibe, somente leitura** — sem `<app-modal>`, sem
   criar/editar (o backend não tem POST/PATCH pra isso, ADR-0045). Busca por CPF (input de texto,
   mesmo padrão de "buscar identificador" já usado em `responsabilidades-administrativas.ts` com
   matrícula), resolvendo `PacienteService.buscarPorCpf(cpf)` → `uuid` →
   `ProntuarioService.buscarPorPacienteId(uuid)`. Também aceita chegar via query param
   `pacienteId`, lido em `ngOnInit` (mesmo padrão de `profissional-perfil.ts` lendo `cpf` da URL) —
   usado pelo link "Ver prontuário" que a tela de Pacientes ganha por linha (mesmo padrão do "Ver
   perfil" de `profissionais-lista.html`). Exibição é uma árvore aninhada
   Atendimento → Consultas → Procedimentos, sem paginação.
7. **`Paciente.ativo`** é um campo comum do formulário de criar/editar (checkbox), não uma ação
   dedicada — mesmo padrão de `Setor.ativo` (ADR-0038 decisão 7), já que o backend também não tem
   endpoint de desabilitar próprio pra `Paciente`.
8. **Reaproveitado o `CepService`/`formatCpf`/`formatTelefone`** já existentes (usados em
   `profissional-cadastro.ts`) no formulário de `Paciente` — autopreenchimento de endereço por CEP
   e máscara de CPF/telefone, sem criar nenhum utilitário novo.
9. **Sem testes automatizados de frontend** (`.spec.ts`) — nenhuma tela de RH/Administrativo tem;
   o único `.spec.ts` do projeto é o scaffold padrão do Angular CLI. Verificação é manual
   (`ng serve` + navegador).

## Trade-offs considerados

**Grupo de menu "Assistência" crescendo fase a fase (escolhida)**
- ✅ Nunca existe uma janela com link morto na sidebar.
- ❌ Pequena divergência do precedente do Administrativo (ADR-0038), que criou o grupo inteiro de
  uma vez na F0b — aceitável, é uma melhoria de segurança de navegação, não uma inconsistência.

**Filtrar `unidadeId` por tipo assistencial, replicando `PerfilPorTipoUnidade` (rejeitada por
ora)**
- ✅ Evitaria selecionar por engano uma unidade puramente administrativa (Federal/Estadual/
  Municipal/Regional) pra um atendimento.
- ❌ Exigiria expor `tipo` em `UnidadeSaudeResponseDto` do frontend e no endpoint que o alimenta —
  mudança de escopo maior que o pedido ("telas para isso"); nenhum requisito concreto força isso
  agora, e o backend também não restringe.

**Prontuário com paginação/lazy-load das listas aninhadas (rejeitada)**
- ✅ Escalaria melhor pra um paciente com histórico muito extenso.
- ❌ Nenhum requisito concreto pede isso; o volume esperado por paciente é baixo nesta fase do
  projeto — revisitar se algum paciente real acumular centenas de atendimentos.

## Consequências

**Positivas**: as 6 telas seguem exatamente o padrão já validado pelos módulos RH e Administrativo,
sem introduzir nenhum componente, biblioteca ou convenção nova; reaproveita utilitários existentes
(`CepService`, máscaras de CPF/telefone) em vez de duplicá-los.

**Negativas / pendências**: nenhuma pendência conhecida além do trabalho normal de implementar cada
tela (ver [`ESCOPO-ASSISTENCIA.md`](../assistencia/ESCOPO-ASSISTENCIA.md), seção "Estado do
frontend", preenchida fase a fase).

## Referências

- [`ESCOPO-ASSISTENCIA.md`](../assistencia/ESCOPO-ASSISTENCIA.md)
- [ADR-0018](./0018-app-shell-e-decisoes-de-frontend-do-modulo-rh.md) e
  [ADR-0038](./0038-app-shell-dinamico-e-telas-de-frontend-do-setor-administrativo.md) —
  precedentes diretos de todo o padrão de tela/modal/service/rota seguido aqui.
- [`docs/frontend/PADRAO-TELAS-INTERNAS.md`](../frontend/PADRAO-TELAS-INTERNAS.md) — regras de CSS
  e estrutura de tela seguidas em toda tela nova.
- [ADR-0040](./0040-paciente-primeira-entidade-da-assistencia.md) a
  [ADR-0045](./0045-prontuario-agregacao-de-leitura.md) — backend agregado por estas telas.
