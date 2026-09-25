# 0037 — Critério para especialização administrativa

## Status

Proposta.

## Contexto

O documento-fonte (ver [ESCOPO-ADMINISTRATIVO.md](../administrativo/ESCOPO-ADMINISTRATIVO.md)), na
sua Regra 7 ("especialização somente quando houver comportamento realmente diferente") e na seção
29 ("ADR — Especializações Administrativas"), pede um critério explícito para decidir quando uma
diferença entre equipamentos deve virar capacidade/configuração (ADR-0032/0033) e quando exige uma
especialização real de domínio (ex.: Gestão de Leitos, Internação, Centro Cirúrgico, Laboratório
clínico, Coleta — mencionados na seção 4 do documento-fonte como assistenciais, fora do escopo
administrativo inicial).

Sem esse critério registrado, cada capacidade nova viraria uma decisão ad-hoc, com risco de
inflar o núcleo administrativo com condicionais por tipo de unidade — exatamente o que a Regra 6 do
documento-fonte (e a motivação original de toda a proposta) busca evitar.

## Decisão

Critério de 4 pontos, registrado como governança (não como mecanismo técnico):

1. Se a diferença entre equipamentos pode ser expressa como "processo habilitado ou não" (dado),
   fica em `CapacidadeAdministrativa`/`ProcessoAdministrativo` (ADR-0032/0033).
2. Se a diferença exige comportamento de negócio novo — novas regras de cálculo, novo ciclo de
   vida, entidades com relacionamentos próprios (ex.: um leito tem estados; uma internação tem
   admissão/alta) — não cabe em configuração; é uma **especialização de domínio**, com suas
   próprias entidades/serviços, habilitada por uma capacidade (a capacidade continua sendo o
   "interruptor"; a especialização é o comportamento atrás dele).
3. Especialização nova só é implementada quando houver requisito funcional real — não antecipar
   Gestão de Leitos/Centro Cirúrgico/Laboratório clínico agora (Etapa 7 do roadmap em
   [ESCOPO-ADMINISTRATIVO.md](../administrativo/ESCOPO-ADMINISTRATIVO.md)).
4. Nenhuma especialização deve exigir condicional por tipo de unidade dentro do código do núcleo
   administrativo (`Setor`/`Perfil`/`Capacidade`) — ela vive ao lado, referenciando o núcleo, nunca
   dentro dele.

## Trade-offs considerados

**Critério registrado como ADR de governança, sem mecanismo técnico novo (escolhida)**
- ✅ Resolve a ambiguidade "capacidade vs. especialização" sem inventar uma abstração técnica para
  decidir isso automaticamente.
- ❌ É uma regra aplicada por julgamento humano em cada caso futuro, não uma validação automática.

**Mecanismo técnico (ex.: flag `requerEspecializacao` em `CapacidadeAdministrativa`) (rejeitada)**
- ✅ Tentaria formalizar a distinção em dado.
- ❌ Prematuro sem nenhuma especialização real implementada ainda para validar o modelo — risco de
  over-engineering, contrariando a diretriz de não construir abstração para caso hipotético.

## Consequências

**Positivas**: dá um critério objetivo para decisões futuras, evitando recriar o debate a cada
capacidade nova; mantém o núcleo administrativo livre de condicionais por tipo de unidade,
cumprindo a Regra 6 do documento-fonte.

**Negativas / pendências**: nenhuma — é uma ADR de critério, sem código associado.

## Referências

- [ESCOPO-ADMINISTRATIVO.md](../administrativo/ESCOPO-ADMINISTRATIVO.md)
- [ADR-0030](./0030-setor-administrativo-e-relacao-com-unidade-de-saude.md)
- [ADR-0032](./0032-catalogo-de-capacidades-administrativas.md)
- [ADR-0033](./0033-processos-administrativos-por-capacidade.md)
