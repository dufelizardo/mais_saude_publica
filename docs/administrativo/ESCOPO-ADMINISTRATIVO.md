# Escopo do domínio Administrativo — roadmap e ideias

**Data:** 2026-09-20 (criado) · atualizado em 2026-09-24 (frontend das Fases 1-6 implementado)
**Status:** Vivo. **Fases 1-6 implementadas (backend e frontend) e mergeadas em `developer`** — ver
seções 5 e 6. Nenhuma fase
promovida a `qaa`/`homologacao`/`main` ainda (regra do módulo completo, mesma do RH). Rastreado no
Jira sob o épico [AQUAQE-318](https://edufelizardo.atlassian.net/browse/AQUAQE-318) ("Setor
Administrativo Adaptativo"), com uma História por ADR/fase (0030→AQUAQE-319, 0031→AQUAQE-320,
0032→AQUAQE-321, 0033→AQUAQE-322, 0034→AQUAQE-323, 0035→AQUAQE-324, 0036→AQUAQE-325,
0037→AQUAQE-326).

## 1. Contexto e como ler este documento

Este documento nasceu de uma "Especificação Preliminar do Setor Administrativo Adaptativo" (v0.1,
20/09/2026), trazida pelo usuário como material externo. O próprio documento já declara (seção 32)
que não é "uma decisão definitiva sobre cada entidade ou relacionamento", mas sim uma base
conceitual para produzir ADRs.

Esse é o mesmo tratamento já dado ao material externo que originou o módulo de RH (ver
[ESCOPO-RH.md, seção 1](../rh/ESCOPO-RH.md#1-contexto-e-como-ler-este-documento)): serve como
**checklist de vocabulário e de estrutura de decisão**, não como especificação literal. Ao
confrontar com o código real desta vez, dois pontos precisaram de ajuste:

- O documento usa "Vínculo" como conceito genérico de RH; o código já tem `Lotacao` cobrindo
  exatamente esse papel (histórico profissional↔unidade↔cargo). Nenhuma entidade `Vinculo` nova
  será criada — ver [ADR-0034](../adr/0034-integracao-administrativo-rh-sem-duplicar-profissional.md).
- O documento cita equipamentos (UPA, Laboratório, CAPS, Centro de Especialidades, Centro de
  Reabilitação, Policlínica) que ainda não existem como valores de `TipoUnidadeDeSaude` — hoje o
  enum só tem `FEDERAL, ESTADUAL, MUNICIPAL, REGIONAL, UBS, HOSPITAL`. A extensão desse enum faz
  parte da decisão da [ADR-0031](../adr/0031-perfil-administrativo-por-tipo-de-unidade.md), seguindo
  o mesmo padrão de generalização já usado pela
  [ADR-0013](../adr/0013-implementar-5-nivel-unidade-de-saude-e-supervisao-regional.md).
- O documento propõe `NecessidadeDePessoal` fluindo da Administração para o RH; o RH já tem `Vaga`
  (recrutamento, fatia 8c). As duas entidades não colidem — ver
  [ADR-0036](../adr/0036-necessidade-de-pessoal-encaminhada-ao-rh.md).

O projeto segue avançando por **incrementos pequenos e discutidos**, não adotando o roadmap
inteiro de uma vez (mesma prática já registrada em `ESCOPO-RH.md`, seção 1). Por isso o ADR 0037
segue com status **Proposta**: a direção arquitetural está definida, mas a implementação é fatia
por fatia, cada uma merecendo sua própria conversa e PR. As ADRs 0030 (Fase 1), 0032 (Fase 2), 0031
(Fase 3), 0033 (Fase 4), 0034 (Fase 5) e 0035/0036 (Fase 6) já foram implementadas e passam a
"Aceita".

## 2. Princípio arquitetural

```text
UnidadeDeSaude
      │
      ▼
TipoUnidadeDeSaude
      │
      ▼
PerfilAdministrativo
      │
      ▼
CapacidadeAdministrativa
      │
      ▼
ProcessoAdministrativo
```

O tipo da unidade não determina uma implementação específica do setor administrativo — determina
qual configuração (perfil → capacidades → processos) está disponível para aquela unidade. Ver
detalhamento em [ADR-0030](../adr/0030-setor-administrativo-e-relacao-com-unidade-de-saude.md) a
[ADR-0033](../adr/0033-processos-administrativos-por-capacidade.md).

## 3. Relação com o RH

A Administração nunca duplica `Profissional`, `Cargo` ou `Lotacao` — apenas referencia por FK
direta (ver [ADR-0034](../adr/0034-integracao-administrativo-rh-sem-duplicar-profissional.md)).
Dois conceitos novos, específicos do domínio Administrativo, também referenciam o RH sem
duplicá-lo:

- `ResponsabilidadeAdministrativa` (profissional responde por um setor/recurso, distinto de estar
  lotado nele) — [ADR-0035](../adr/0035-responsabilidade-administrativa-separada-da-lotacao.md).
- `NecessidadeDePessoal` (a unidade identifica necessidade operacional, RH conduz o ciclo de
  recrutamento) — [ADR-0036](../adr/0036-necessidade-de-pessoal-encaminhada-ao-rh.md).

## 4. Fases planejadas (roadmap, ainda não implementado)

Ordem de implementação combinada, adaptada da estratégia da especificação original (sua seção 27):

| Fase | Entrega | ADR(s) |
|---|---|---|
| 1 ✅ | `Setor`/`TipoSetor` ligados a `UnidadeDeSaude` | [0030](../adr/0030-setor-administrativo-e-relacao-com-unidade-de-saude.md) |
| 2 ✅ | `CapacidadeAdministrativa` (catálogo) | [0032](../adr/0032-catalogo-de-capacidades-administrativas.md) |
| 3 ✅ | `PerfilAdministrativo` + extensão de `TipoUnidadeDeSaude` | [0031](../adr/0031-perfil-administrativo-por-tipo-de-unidade.md) |
| 4 ✅ | `ProcessoAdministrativo` | [0033](../adr/0033-processos-administrativos-por-capacidade.md) |
| 5 ✅ | Integração com RH (referências diretas) — `Setor.responsavel` por matrícula | [0034](../adr/0034-integracao-administrativo-rh-sem-duplicar-profissional.md) |
| 6 ✅ | `ResponsabilidadeAdministrativa` | [0035](../adr/0035-responsabilidade-administrativa-separada-da-lotacao.md) |
| 6 ✅ | `NecessidadeDePessoal` | [0036](../adr/0036-necessidade-de-pessoal-encaminhada-ao-rh.md) |
| 7+ | Recursos administrativos (Patrimônio, Estoque, Compras, Fornecedores, Contratos, Manutenção, Documentos, Transporte) e especializações assistenciais (Leitos, Internação, Centro Cirúrgico, Laboratório, Coleta) — só quando houver requisito real | [0037](../adr/0037-criterio-para-especializacao-administrativa.md) |

Cada fase, na sua vez, terá sua própria conversa/PR e (quando implementada) uma atualização de
status nesta tabela e nos ADRs correspondentes — mesmo padrão do módulo de RH.

## 5. Estado de implementação

Preenchida com PRs conforme cada fase é construída (mesmo formato da tabela em
[ESCOPO-RH.md, seção 6](../rh/ESCOPO-RH.md#6-estado-do-frontend)).

| Fase | Escopo | PR(s) | Status |
|---|---|---|---|
| 1 | `Setor`/`TipoSetor` ligados a `UnidadeDeSaude`, CRUD em `/api/v1/setor/`, testes JUnit + Robot (ver [ADR-0030](../adr/0030-setor-administrativo-e-relacao-com-unidade-de-saude.md)) | [#181](https://github.com/dufelizardo/mais_saude_publica/pull/181) | ✅ |
| 2 | `CapacidadeAdministrativa` (catálogo/feature-toggle, dado em tabela), CRUD em `/api/v1/capacidade-administrativa/`, testes JUnit + Robot (ver [ADR-0032](../adr/0032-catalogo-de-capacidades-administrativas.md)) | [#183](https://github.com/dufelizardo/mais_saude_publica/pull/183) | ✅ |
| 3 | Extensão de `TipoUnidadeDeSaude` (UPA/Laboratório/CAPS/Centro de Especialidades/Centro de Reabilitação/Policlínica), `PerfilAdministrativo` (catálogo) e `PerfilPorTipoUnidade` (resolve o perfil de cada tipo), CRUD em `/api/v1/perfil-administrativo/` e `/api/v1/perfil-por-tipo-unidade/`, testes JUnit + Robot (ver [ADR-0031](../adr/0031-perfil-administrativo-por-tipo-de-unidade.md)) | [#185](https://github.com/dufelizardo/mais_saude_publica/pull/185) | ✅ |
| 4 | `ProcessoAdministrativo` (detalhamento opcional de uma capacidade, 1:N, sem processos concretos cadastrados ainda), CRUD em `/api/v1/processo-administrativo/`, testes JUnit + Robot (ver [ADR-0033](../adr/0033-processos-administrativos-por-capacidade.md)) | [#187](https://github.com/dufelizardo/mais_saude_publica/pull/187) | ✅ |
| 5 | `Setor.responsavel` (opcional, `Profissional` referenciado por matrícula — nunca por uuid interno, que `ProfissionalResponseDto` não expõe), testes JUnit + Robot (ver [ADR-0034](../adr/0034-integracao-administrativo-rh-sem-duplicar-profissional.md)) | [#189](https://github.com/dufelizardo/mais_saude_publica/pull/189) | ✅ |
| 6 | `ResponsabilidadeAdministrativa` (histórico com múltiplas vigentes simultâneas, `PATCH .../encerrar`, ver [ADR-0035](../adr/0035-responsabilidade-administrativa-separada-da-lotacao.md)) e `NecessidadeDePessoal` (CRUD full-replace + `PATCH .../vincular-vaga` informativo a `Vaga` do RH, ver [ADR-0036](../adr/0036-necessidade-de-pessoal-encaminhada-ao-rh.md)), testes JUnit + Robot | [#191](https://github.com/dufelizardo/mais_saude_publica/pull/191) | ✅ |
| 7+ | Recursos administrativos e especializações assistenciais, só quando houver requisito real (ver [ADR-0037](../adr/0037-criterio-para-especializacao-administrativa.md)) | — | ⏳ Não iniciada |

## 6. Estado do frontend

Backend completo (seção acima); frontend construído fase a fase, seguindo o mesmo padrão do módulo
de RH (ver [ADR-0038](../adr/0038-app-shell-dinamico-e-telas-de-frontend-do-setor-administrativo.md)
pras decisões de frontend específicas deste módulo).

| Fase | Escopo | PR(s) | Status |
|---|---|---|---|
| F0a | Backend: `GET /api/v1/profissional/matricula/{matricula}` — pré-requisito pras telas de Setor e Responsabilidades Administrativas resolverem nome a partir da matrícula (ver ADR-0038) | [#193](https://github.com/dufelizardo/mais_saude_publica/pull/193) | ✅ |
| F0b | Breadcrumb dinâmico (campo `area` em toda rota) + grupo de menu "Administrativo" no `AppShell` + ADR-0038 | [#194](https://github.com/dufelizardo/mais_saude_publica/pull/194) | ✅ |
| F1 | Tela **Setores** (`administrativo/setores`) | [#195](https://github.com/dufelizardo/mais_saude_publica/pull/195) | ✅ |
| F2 | Tela **Capacidades Administrativas** (`administrativo/capacidades`) | [#196](https://github.com/dufelizardo/mais_saude_publica/pull/196) | ✅ |
| F3 | Telas **Perfis Administrativos** (`administrativo/perfis`) + **Perfil por Tipo de Unidade** (`administrativo/perfis-por-tipo-unidade`) | [#197](https://github.com/dufelizardo/mais_saude_publica/pull/197) | ✅ |
| F4 | Tela **Processos Administrativos** (`administrativo/processos`) | [#198](https://github.com/dufelizardo/mais_saude_publica/pull/198) | ✅ |
| F5 | Tela **Responsabilidades Administrativas** (`administrativo/responsabilidades`) | [#199](https://github.com/dufelizardo/mais_saude_publica/pull/199) | ✅ |
| F6 | Tela **Necessidades de Pessoal** (`administrativo/necessidades-de-pessoal`) | [#200](https://github.com/dufelizardo/mais_saude_publica/pull/200) | ✅ |

## 7. Referências

- [MODELO-RH.md](../rh/MODELO-RH.md) e [ESCOPO-RH.md](../rh/ESCOPO-RH.md) — precedente direto de
  como este documento e os ADRs 0030-0037 foram estruturados.
- [ADR-0013](../adr/0013-implementar-5-nivel-unidade-de-saude-e-supervisao-regional.md) — precedente
  de generalização de `TipoUnidadeDeSaude`/`getTiposAceitos()` e de rejeição de uma proposta externa
  de separar `Administracao`/`UnidadeDeSaude`.
- [ADR-0014](../adr/0014-modulo-profissional-rh-com-vinculo-fraco-por-reconciliacao.md) e
  [ADR-0017](../adr/0017-numero-de-matricula-automatico-e-cpf-nao-unico.md) — contexto de por que a
  integração com RH (ADR-0034) usa referência direta em vez do vínculo fraco por CPF.
- ADRs [0030](../adr/0030-setor-administrativo-e-relacao-com-unidade-de-saude.md) a
  [0037](../adr/0037-criterio-para-especializacao-administrativa.md) — decisões individuais
  derivadas deste documento.
- O texto original da "Especificação Preliminar do Setor Administrativo Adaptativo" (v0.1) não foi
  anexado a este repositório — este documento é a versão curada e confrontada com o código real.
