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
| 1 | `Medicamento`, CRUD em `/api/v1/medicamento/`, testes JUnit + Robot | [0049](../adr/0049-medicamento-primeira-entidade-da-farmacia.md) | [#228](https://github.com/dufelizardo/mais_saude_publica/pull/228) | ✅ |
| 2 | `Lote`, CRUD em `/api/v1/lote/` (FKs a Medicamento e UnidadeDeSaude), testes JUnit + Robot | [0050](../adr/0050-lote-segunda-entidade-da-farmacia.md) | [#229](https://github.com/dufelizardo/mais_saude_publica/pull/229) | ✅ |
| 3 | `Dispensacao`, só criação/leitura em `/api/v1/dispensacao/` (sem PATCH — imutável), debita `Lote.quantidade`, testes JUnit + Robot | [0051](../adr/0051-dispensacao-terceira-entidade-da-farmacia.md) | [#230](https://github.com/dufelizardo/mais_saude_publica/pull/230) | ✅ |
| 4 | Livro de movimentação (`MovimentacaoFarmacia`): entrada, dispensação, perda (com motivo) e ajuste de inventário lançados num histórico imutável com saldo resultante, responsável e hora do servidor; `Lote.quantidade` só muda pelo livro; edição de lote restrita a número/validade; trava de linha contra venda dupla; saldo inicial dos lotes antigos; extrato por lote em `/api/v1/movimentacao-farmacia/`; testes JUnit + Robot | [0057](../adr/0057-livro-de-movimentacao-do-estoque-da-farmacia.md) | *(em aberto)* | 🔵 |

Todas as fases mergeadas (ou em PR) em `developer`. Promoção a `qaa`/`homologacao`/`main` ainda não
solicitada.

A fase 4 cobre o que antes estava listado como candidatos separados `Perda` e `InventarioFarmacia`:
os dois viraram tipos de lançamento do mesmo livro (ADR-0057).

### Próximos passos (ordem combinada)

1. ~~**Telas da Farmácia**~~ — feito, ver seção 5.
2. **Transferência entre unidades** (backend) — saída no lote de origem e entrada no lote de destino,
   como dois lançamentos ligados no livro da fase 4, numa transação só.

## 5. Estado do frontend

| Fase | Escopo | ADR | PR(s) | Status |
|---|---|---|---|---|
| F1 | Tela `assistencia/farmacia` portada do mockup `Farmacia.html`: resumo, abas Estoque por lote (filtro por unidade e vencimento), Dispensações, Medicamentos e Livro de estoque (extrato); gavetas de novo/editar medicamento, entrada e correção de lote, dispensação, perda/ajuste e detalhe da dispensação; `pacienteCpf` na resposta da dispensação | [0058](../adr/0058-tela-da-farmacia-abas-gaveta-lateral-e-livro-de-estoque.md) | *(em aberto)* | 🔵 |

## 6. Referências

- [`MAPA-DE-DOMINIOS.md`](../MAPA-DE-DOMINIOS.md) — domínio #9, posição no roadmap da plataforma.
- [`ESCOPO-ENFERMAGEM.md`](../enfermagem/ESCOPO-ENFERMAGEM.md) — domínio anterior;
  `AdministracaoDeMedicamento` referenciado ali é desbloqueado por este domínio.
- [ADR-0049](../adr/0049-medicamento-primeira-entidade-da-farmacia.md) — decisão de design da fase 1
  (`Medicamento`), incluindo a pesquisa sobre Hórus e movimentação de estoque farmacêutico.
- [ADR-0050](../adr/0050-lote-segunda-entidade-da-farmacia.md) — decisão de design da fase 2
  (`Lote`), incluindo a nota sobre a colisão de nome com o candidato `Lote` do domínio Estoque (#13).
- [ADR-0051](../adr/0051-dispensacao-terceira-entidade-da-farmacia.md) — decisão de design da fase 3
  (`Dispensacao`), incluindo a decisão de ser create-only (sem PATCH) e debitar `Lote.quantidade`.
- [ADR-0057](../adr/0057-livro-de-movimentacao-do-estoque-da-farmacia.md) — decisão de design da fase 4
  (livro de movimentação), que revê o contador simples da ADR-0050.
