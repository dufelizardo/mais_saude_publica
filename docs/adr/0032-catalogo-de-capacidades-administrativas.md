# 0032 — Catálogo de capacidades administrativas

## Status

Proposta.

## Contexto

O documento-fonte (ver [ESCOPO-ADMINISTRATIVO.md](../administrativo/ESCOPO-ADMINISTRATIVO.md))
define `CapacidadeAdministrativa` como uma funcionalidade/competência administrativa (ex.:
`PATRIMONIO`, `ESTOQUE`, `COMPRAS`, `GESTAO_DE_LEITOS`) que pode estar disponível para uma unidade,
associada a um `PerfilAdministrativo` (ADR-0031).

É importante deixar explícito: o projeto não tem hoje nenhum mecanismo de autenticação ou
autorização implementado (a ADR-0006, sobre segurança/JWT, é "Proposto — não implementado"). Existe
risco de confundir "capacidade habilitada para uma unidade" com "permissão de um usuário" — são
conceitos ortogonais e não devem ser fundidos nesta decisão.

## Decisão

- Nova entidade `CapacidadeAdministrativa`: `uuid`, `codigo`, `nome`, `descricao`, `ativo`.
- Associação N:N entre `PerfilAdministrativo` e `CapacidadeAdministrativa` (tabela de junção).
- `CapacidadeAdministrativa` é um **catálogo/feature-toggle de domínio** — determina se uma
  funcionalidade/processo administrativo está disponível para o tipo de unidade, não quem pode
  acessá-la. Quando o projeto implementar autenticação/autorização (ADR-0006), os dois mecanismos
  permanecem separados: capacidade decide "isso existe para esta unidade", autorização decidiria
  "este usuário pode operar isso".
- O catálogo inicial de capacidades (lista da seção 13 do documento-fonte) entra como **dado seed**
  em tabela, não como enum Java fixo — para que uma capacidade nova possa ser adicionada sem
  alteração de código/deploy.

## Trade-offs considerados

**Capacidade como linha de tabela (dado), não enum Java (escolhida)**
- ✅ Um tipo de unidade com necessidade nova não exige alteração de enum nem deploy de código, só
  inserção de dado — objetivo central do documento-fonte (seção 15/30, critério 10).
- ❌ Perde checagem de tipo em tempo de compilação; exige validação de unicidade/consistência em
  runtime ou seed controlado.

**Capacidade como enum Java fixo (rejeitada)**
- ✅ Checagem de tipo em compilação.
- ❌ Contradiz a motivação central do documento-fonte: cada capacidade nova exigiria alteração de
  código nos serviços administrativos, exatamente o que a proposta quer evitar.

## Consequências

**Positivas**: o catálogo evolui por dado, sem deploy; serve de base para a ADR-0033 (processos) e
para uma futura tela de administração de perfis/capacidades.

**Negativas / pendências**: exige eventualmente uma rotina/tela de administração do catálogo (fora
do escopo deste ADR); regras de unicidade de código e de quem pode alterar o catálogo ainda não
foram definidas — ficam para a implementação.

## Referências

- [ESCOPO-ADMINISTRATIVO.md](../administrativo/ESCOPO-ADMINISTRATIVO.md)
- [ADR-0031](./0031-perfil-administrativo-por-tipo-de-unidade.md)
- [ADR-0006](./0006-seguranca-jwt.md) — nota de não-confusão entre capacidade (catálogo/domínio) e
  autorização (ainda não implementada).
