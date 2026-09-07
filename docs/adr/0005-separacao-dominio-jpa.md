# 0005 — Separação entre entidades de domínio e entidades JPA (proposta futura)

## Status

Proposto — depende das ADRs 0003 (Clean Architecture) e 0004 (padrões de projeto). Não implementado; hoje `UnidadeDeSaude` é ao mesmo tempo a entidade de domínio e a entidade JPA (ver ADR 0002).

## Contexto

A Clean Architecture (ADR 0003) exige que o domínio não conheça detalhes de infraestrutura, mas o Spring Data JPA exige anotações (`@Entity`, `@Table`, `@Column`, relacionamentos) diretamente nas classes. Hoje isso não é um problema porque o projeto não segue Clean Architecture — mas seria um conflito direto se essa adoção avançar.

## Decisão

Manter duas representações separadas:

- **Entidade de domínio** (`domain/entity/`) — Java puro, sem nenhuma anotação de framework, contendo regras de negócio e comportamentos (ex.: `unidade.ativar()`, `unidade.inativar()`).
- **Entidade JPA** (`infrastructure/entity/`) — anotada com JPA/Hibernate, responsável só pelo mapeamento para o banco.
- **Mapeamento entre as duas** via **MapStruct** (mapeadores gerados em tempo de compilação, sem reflection), com interfaces como `UnidadeEntityMapper.toEntity(domain)` / `.toDomain(entity)`.

## Trade-offs considerados

| Alternativa | Vantagens | Desvantagens | Decisão |
|---|---|---|---|
| Usar JPA diretamente no domínio (como é feito hoje) | Simples, menos classes | Domínio acoplado ao JPA — incompatível com Clean Architecture | ❌ Rejeitada para este roadmap |
| Separar domínio/JPA com mapeamento manual | Controle total | Muito código boilerplate | ⚠️ Considerada |
| Separar domínio/JPA com MapStruct | Automático, performático (bytecode gerado em compile-time), tipado | Nova dependência no `pom.xml`, geração de código no build | ✅ Escolhida |
| Mapeamento via reflection (ex.: ModelMapper/Dozer) | Sem código de mapeamento manual | Mais lento, quebra silenciosamente em refatorações | ❌ Rejeitada |

## Consequências

**Positivas**: domínio 100% desacoplado de JPA/banco; testável sem precisar de banco de dados; evolução do schema não afeta as regras de negócio.

**Negativas**: cada entidade de domínio passa a ter uma correspondente JPA e um mapper — manutenção duplicada quando o modelo muda; overhead de build por causa da geração de código do MapStruct; mais complexidade para quem só está acostumado com uma entidade JPA fazendo os dois papéis (como é hoje).

**Neutras**: adiciona MapStruct como dependência; exige que todos os mapeamentos sigam o mesmo padrão.
