# Escopo do domínio Enfermagem — roadmap e estado

## 1. Contexto e como ler este documento

Enfermagem (domínio #8 do [`MAPA-DE-DOMINIOS.md`](../MAPA-DE-DOMINIOS.md)) é o primeiro domínio
implementado depois da onda "Operação Assistencial" (`Paciente`/`Atendimento`/`Agendamento`/
`Consulta`/`Procedimento`/`Prontuário` — ver
[`ESCOPO-ASSISTENCIA.md`](../assistencia/ESCOPO-ASSISTENCIA.md), onda essa já fechada). Mesmo tipo
de guarda-chuva por módulo que `ESCOPO-RH.md`, `ESCOPO-ADMINISTRATIVO.md` e
`ESCOPO-ASSISTENCIA.md` já são pros seus módulos.

Diferente da onda Assistência (toda planejada de uma vez via ADR-0039), Enfermagem é implementada
"conforme requisito real" — uma fatia por vez, sem roadmap fechado de antemão (mesma disciplina do
Administrativo Fase 7+, ADR-0037).

## 2. Princípio arquitetural

Mesma convenção flat de toda a plataforma (`models/`, `services/version1/`,
`controllers/version1/`). `Triagem` é filha de `Atendimento` (Assistência) — mesmo papel
estrutural de `Consulta` — e referencia `Profissional` (RH) por matrícula, nunca por uuid interno
(ADR-0034). Ver [ADR-0047](../adr/0047-triagem-primeira-entidade-da-enfermagem.md) para a
reconciliação com o esboço original do domínio.

## 3. Relação com Assistência e RH

`Triagem` referencia `Atendimento` (Assistência) por uuid e `Profissional` (RH) por matrícula.
`Prontuário` (ADR-0045) foi estendido para agregar `Triagem` junto de `Consulta`, dentro de cada
Atendimento.

## 4. Estado de implementação (backend)

| Fase | Escopo | ADR | PR(s) | Status |
|---|---|---|---|---|
| 1 | `Triagem`, CRUD em `/api/v1/triagem/`, testes JUnit + Robot, extensão do Prontuário | [0047](../adr/0047-triagem-primeira-entidade-da-enfermagem.md) | [#226](https://github.com/dufelizardo/mais_saude_publica/pull/226) | ✅ |
| 2 | `EvolucaoEnfermagem`, CRUD em `/api/v1/evolucao-enfermagem/`, testes JUnit + Robot, extensão do Prontuário | [0048](../adr/0048-evolucao-de-enfermagem-segunda-entidade-da-enfermagem.md) | *(em aberto)* | 🔵 |

Ambas as fases mergeadas (ou em PR) em `developer`. Promoção a `qaa`/`homologacao`/`main` ainda não
solicitada.

Próximos candidatos do domínio (`AdministracaoDeMedicamento` — depende de Farmácia (#9) existir
primeiro —, `Cuidado`, `Escala`) — sem ADR, sem implementação, só quando houver requisito real.

## 5. Estado do frontend

Não iniciado — nenhuma tela para Triagem ainda.

## 6. Referências

- [`MAPA-DE-DOMINIOS.md`](../MAPA-DE-DOMINIOS.md) — domínio #8, posição no roadmap da plataforma.
- [`ESCOPO-ASSISTENCIA.md`](../assistencia/ESCOPO-ASSISTENCIA.md) — onda anterior, de onde
  `Atendimento` (referenciado aqui) e o Prontuário (estendido aqui) vêm.
- [ADR-0047](../adr/0047-triagem-primeira-entidade-da-enfermagem.md) — decisão de design da fase 1 (`Triagem`).
- [ADR-0048](../adr/0048-evolucao-de-enfermagem-segunda-entidade-da-enfermagem.md) — decisão de design da fase 2 (`EvolucaoEnfermagem`).
- [ADR-0034](../adr/0034-integracao-administrativo-rh-sem-duplicar-profissional.md) — padrão de FK
  por matrícula, reaproveitado aqui.
