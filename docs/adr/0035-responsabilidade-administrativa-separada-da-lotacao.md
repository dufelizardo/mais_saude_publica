# 0035 — Responsabilidade administrativa separada da lotação

## Status

Aceita e implementada (Fase 6 — [PR #191](https://github.com/dufelizardo/mais_saude_publica/pull/191)).

## Contexto

O documento-fonte (ver [ESCOPO-ADMINISTRATIVO.md](../administrativo/ESCOPO-ADMINISTRATIVO.md)),
seção 20, distingue lotação (onde o profissional está alocado) de responsabilidade (o que ele
responde por — ex.: um profissional lotado na Administração pode ser especificamente "Fiscal de
Contrato" ou "Gestão de Patrimônio"). Essa é a Regra 4 do documento: "lotação não é
responsabilidade".

O RH já tem um precedente de histórico que permite múltiplas vigências simultâneas para o mesmo
profissional: `AjusteIndividual` (diferente de `Lotacao`, que permite no máximo uma vigente por
vez). Esse é o padrão mais próximo do comportamento que `ResponsabilidadeAdministrativa` precisa —
uma pessoa pode acumular mais de uma responsabilidade ao mesmo tempo, como no próprio exemplo do
documento-fonte.

## Decisão

- Nova entidade `ResponsabilidadeAdministrativa`: `uuid`, `profissional` (`@ManyToOne Profissional`,
  FK direta por matrícula — ver ADR-0034), `setor` (`@ManyToOne Setor`, obrigatório — único alvo
  possível até que recursos administrativos existam, Etapa 7), `tipo` (texto livre, ex.: "Fiscal de
  Contrato"), `descricao` (opcional), `dataInicio`, `dataFim` (nullable = vigente).
- Regra de vigência: como `AjusteIndividual`, permite **múltiplas** `ResponsabilidadeAdministrativa`
  vigentes simultâneas para o mesmo profissional.
- Encerrar uma responsabilidade é sempre preencher `dataFim` — nunca deletar o registro, preservando
  histórico (Regra 8 do documento-fonte, mesmo princípio já aplicado a `Lotacao`/`TabelaSalarial`/
  `AjusteIndividual` no RH).

## Trade-offs considerados

**Histórico com múltiplas vigentes, no padrão de `AjusteIndividual` (escolhida)**
- ✅ Reflete a realidade descrita no próprio documento-fonte (seção 20, exemplos com duas
  responsabilidades simultâneas para o mesmo profissional).
- ❌ Não há regra de "no máximo uma" para impor duplicidade acidental do mesmo tipo — validação fica
  a cargo da aplicação, não do schema (mesma característica já aceita para `AjusteIndividual`).

**Reaproveitar `Lotacao`, estendendo com um campo "tipo de responsabilidade" (rejeitada)**
- ✅ Uma tabela a menos.
- ❌ Contraria explicitamente a Regra 4 do documento-fonte e misturaria dois conceitos com regras de
  vigência incompatíveis (uma vigente vs. várias vigentes) na mesma entidade.

## Consequências

**Positivas**: representa a realidade operacional descrita no documento sem alterar `Lotacao`/RH;
reaproveita um padrão de histórico já validado (`AjusteIndividual`).

**Negativas / pendências**: o alvo da responsabilidade hoje é só `Setor` — quando o primeiro recurso
administrativo for implementado (Etapa 7 do roadmap), o schema pode precisar de um campo adicional
nullable para cobrir esse segundo tipo de alvo.

## Referências

- [ESCOPO-ADMINISTRATIVO.md](../administrativo/ESCOPO-ADMINISTRATIVO.md)
- [ADR-0030](./0030-setor-administrativo-e-relacao-com-unidade-de-saude.md) — `Setor` como possível
  alvo da responsabilidade.
- [ADR-0034](./0034-integracao-administrativo-rh-sem-duplicar-profissional.md) — FK direta a
  `Profissional`.
- [MODELO-RH.md](../rh/MODELO-RH.md) — padrão de `AjusteIndividual` reaproveitado aqui.
