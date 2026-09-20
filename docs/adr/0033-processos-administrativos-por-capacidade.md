# 0033 — Processos administrativos por capacidade

## Status

Proposta.

## Contexto

O documento-fonte (ver [ESCOPO-ADMINISTRATIVO.md](../administrativo/ESCOPO-ADMINISTRATIVO.md))
detalha cada capacidade em processos (ex.: `PATRIMONIO` → Cadastro, Transferência, Inventário,
Manutenção, Baixa, Descarte). Nenhum recurso administrativo (patrimônio, estoque, contratos etc.)
existe implementado ainda — modelar processos concretos sem o recurso por trás seria cadastrar
funcionalidade que a interface sugere existir, mas que não tem endpoint/tela reais.

O próprio módulo de RH já enfrentou esse problema e resolveu com uma regra explícita (a "Regra da
fatia 8", registrada em
[ESCOPO-RH.md](../rh/ESCOPO-RH.md#regra-da-fatia-8-definida-pelo-usuário-vale-para-8a-8d)): a
interface não deve disponibilizar operação sem o endpoint correspondente existir de fato. O mesmo
princípio se aplica aqui a processos administrativos.

## Decisão

- Nova entidade `ProcessoAdministrativo`: `uuid`, `capacidade` (`@ManyToOne CapacidadeAdministrativa`),
  `codigo`, `nome`, `descricao`, `ativo` — relação 1:N de capacidade para processos.
- Processo é opcional: uma capacidade pode existir e estar habilitada para um perfil sem nenhum
  processo cadastrado ainda. Nesse estado, a capacidade está "declarada, não operacional".
- Processos concretos (ex.: "Cadastro de Patrimônio") só serão cadastrados quando o recurso
  administrativo correspondente for de fato implementado (Etapa 6/7 do roadmap em
  [ESCOPO-ADMINISTRATIVO.md](../administrativo/ESCOPO-ADMINISTRATIVO.md)) — nunca antes, pelo mesmo
  motivo da regra da fatia 8 do RH.

## Trade-offs considerados

**`ProcessoAdministrativo` como entidade própria, populada só quando o recurso existir (escolhida)**
- ✅ O catálogo nunca sugere disponibilidade de funcionalidade inexistente.
- ❌ As primeiras fases (perfil/capacidades) ficam temporariamente sem processos cadastrados —
  esperado e aceitável nesta etapa.

**Cadastrar todos os processos do documento-fonte agora, como catálogo estático (rejeitada)**
- ✅ Documentação mais completa desde já.
- ❌ Cria processos "fantasmas" sem implementação real por trás, reproduzindo o problema que a
  regra da fatia 8 do RH já identificou e corrigiu.

## Consequências

**Positivas**: o catálogo de processos cresce em paralelo com a implementação real, nunca à frente
dela.

**Negativas / pendências**: nenhum processo é cadastrado nesta rodada — é só o desenho da tabela. A
lista da seção 16 do documento-fonte serve de referência para quando cada recurso administrativo
for implementado.

## Referências

- [ESCOPO-ADMINISTRATIVO.md](../administrativo/ESCOPO-ADMINISTRATIVO.md)
- [ADR-0032](./0032-catalogo-de-capacidades-administrativas.md)
- [ESCOPO-RH.md](../rh/ESCOPO-RH.md#regra-da-fatia-8-definida-pelo-usuário-vale-para-8a-8d) — regra
  da fatia 8, mesmo princípio aplicado aqui.
