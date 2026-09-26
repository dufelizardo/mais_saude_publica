# Escopo do domínio Farmácia — roadmap e estado

## 1. Contexto e como ler este documento

Farmácia (domínio #9 do [`MAPA-DE-DOMINIOS.md`](../MAPA-DE-DOMINIOS.md)) é o domínio implementado
depois de Enfermagem (#8 — ver [`ESCOPO-ENFERMAGEM.md`](../enfermagem/ESCOPO-ENFERMAGEM.md)). Mesmo
tipo de guarda-chuva por módulo que `ESCOPO-RH.md`, `ESCOPO-ADMINISTRATIVO.md`,
`ESCOPO-ASSISTENCIA.md` e `ESCOPO-ENFERMAGEM.md` já são pros seus módulos.

Como Enfermagem, Farmácia é implementada "conforme requisito real" — uma fatia por vez, sem
roadmap fechado de antemão (mesma disciplina do Administrativo Fase 7+, ADR-0037).

## 2. Princípio arquitetural

Mesma convenção flat de toda a plataforma (`models/`, `services/version1/`,
`controllers/version1/`). `Medicamento` é raiz do domínio, sem FK — mesmo papel estrutural de
`Paciente` na Assistência. Ver [ADR-0049](../adr/0049-medicamento-primeira-entidade-da-farmacia.md)
para a pesquisa de domínio (Hórus, padrão de movimentação de estoque) que embasou o desenho.

## 3. Relação com Enfermagem

`Medicamento` desbloqueia `AdministracaoDeMedicamento` (Enfermagem, #8), adiado nas ADRs 0047/0048
por depender desta entidade existir primeiro. Nenhuma implementação ainda referencia
`Medicamento` por FK.

## 4. Estado de implementação (backend)

| Fase | Escopo | ADR | PR(s) | Status |
|---|---|---|---|---|
| 1 | `Medicamento`, CRUD em `/api/v1/medicamento/`, testes JUnit + Robot | [0049](../adr/0049-medicamento-primeira-entidade-da-farmacia.md) | *(em aberto)* | 🔵 |

Próximos candidatos do domínio (`Lote`, `Dispensacao`, `MovimentacaoFarmacia`,
`TransferenciaEntreUnidades`, `Perda`, `InventarioFarmacia`) — sem ADR, sem implementação, só
quando houver requisito real.

## 5. Estado do frontend

Não iniciado — nenhuma tela para Medicamento ainda.

## 6. Referências

- [`MAPA-DE-DOMINIOS.md`](../MAPA-DE-DOMINIOS.md) — domínio #9, posição no roadmap da plataforma.
- [`ESCOPO-ENFERMAGEM.md`](../enfermagem/ESCOPO-ENFERMAGEM.md) — domínio anterior;
  `AdministracaoDeMedicamento` referenciado ali é desbloqueado por este domínio.
- [ADR-0049](../adr/0049-medicamento-primeira-entidade-da-farmacia.md) — decisão de design desta
  fase, incluindo a pesquisa sobre Hórus e movimentação de estoque farmacêutico.
