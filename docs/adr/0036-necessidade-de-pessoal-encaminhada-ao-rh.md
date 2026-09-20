# 0036 — Necessidade de pessoal encaminhada ao RH

## Status

Proposta.

## Contexto

O documento-fonte (ver [ESCOPO-ADMINISTRATIVO.md](../administrativo/ESCOPO-ADMINISTRATIVO.md)),
seção 21, propõe `NecessidadeDePessoal` (unidade, setor, cargo, quantidade, jornada, competências
necessárias, justificativa) como o jeito de a unidade identificar necessidade operacional de
pessoal, encaminhada ao RH para recrutamento/contratação/lotação.

A investigação no código encontrou que o RH **já tem** `Vaga` implementada (fatia 8c de
recrutamento, ver [ESCOPO-RH.md](../rh/ESCOPO-RH.md)): `unidade` (FK), `cargo` (FK), `quantidade`,
`status`. Criar `NecessidadeDePessoal` do zero sem considerar `Vaga` duplicaria conceito já
existente — é preciso decidir como as duas se relacionam sem colisão.

## Decisão

- `NecessidadeDePessoal` é uma entidade do domínio **Administrativo** (não do RH): `uuid`, `unidade`
  (FK `UnidadeDeSaude`), `setor` (FK `Setor`, nullable), `cargo` (FK `Cargo` do RH — referência,
  nunca cópia, ver ADR-0034), `quantidade`, `jornadaSemanalHoras`, `competenciasNecessarias`
  (texto), `justificativa`, `dataRegistro`, `vagaAssociada` (FK `Vaga`, nullable).
- Fluxo: a Administração registra a necessidade; quando o RH decide abrir recrutamento a partir
  dela, cria uma `Vaga` (fluxo/tela do próprio RH, sem alterar o contrato de `Vaga`) e a referência
  é gravada em `vagaAssociada` — um link informativo, não um gatilho automático. Não há escrita
  automática cross-domain.
- `NecessidadeDePessoal` não substitui nem se confunde com `Vaga`: a primeira é o "pedido
  operacional da unidade" (mais granular — por setor, com jornada/competências/justificativa que
  `Vaga` não tem e não precisa ganhar); a segunda é o "processo seletivo aberto pelo RH".

## Trade-offs considerados

**Nova entidade no domínio Administrativo, com referência opcional a `Vaga` (escolhida)**
- ✅ Preserva a granularidade que o documento-fonte pede (setor, jornada, competências,
  justificativa) sem inflar `Vaga` com campos que só fazem sentido antes de a vaga existir de fato.
- ❌ Duas entidades para um único fluxo conceitual (necessidade → vaga) — aceitável porque
  representam dois momentos e duas autoridades diferentes (Regra 3 do documento-fonte: Admin
  identifica necessidade operacional, RH conduz o ciclo funcional).

**Estender `Vaga` com campos de setor/jornada/competências/justificativa (rejeitada)**
- ✅ Uma entidade só.
- ❌ Mistura autoridade dos dois domínios na mesma tabela (Administrativo escrevendo em entidade do
  RH) e infla `Vaga` com campos que só importam antes de a vaga existir — contraria a fronteira
  definida na ADR-0034.

## Consequências

**Positivas**: a Administração ganha um jeito de registrar necessidade operacional sem tocar em
tabela do RH; rastreável até a `Vaga` quando ela for aberta.

**Negativas / pendências**: nenhuma automação de "aprovar necessidade → criar vaga" nesta rodada —
é decisão de processo/fluxo de aprovação, não de dado, e fica para quando esse fluxo for desenhado.

## Referências

- [ESCOPO-ADMINISTRATIVO.md](../administrativo/ESCOPO-ADMINISTRATIVO.md)
- [ADR-0034](./0034-integracao-administrativo-rh-sem-duplicar-profissional.md)
- `Vaga` (`src/main/java/.../models/Vaga.java`) — fatia 8c do RH, ver
  [ESCOPO-RH.md](../rh/ESCOPO-RH.md).
