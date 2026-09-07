# 0004 — Adoção de padrões de projeto: Factory, Builder, Strategy, Repository (proposta futura)

## Status

Proposto — depende da ADR 0003 (Clean Architecture) ser adotada primeiro. Não implementado.

## Contexto

Com a Clean Architecture proposta na ADR 0003, seria preciso padronizar como objetos complexos são criados, como múltiplas fontes de dados (CSV hoje, futuramente APIs governamentais como DATASUS/IBGE) são importadas, e como a persistência é abstraída — mantendo o domínio livre de detalhes técnicos.

## Decisão

Adotar 4 padrões de projeto:

- **Factory Method** — centraliza a criação de entidades de domínio complexas (ex.: `UnidadeSaudeFactory.criar(...)`), garantindo consistência (geração de ID, validações) em um único ponto.
- **Builder** — para entidades/DTOs/value objects com muitos atributos opcionais, via `@Builder` (Lombok) sobre classes imutáveis.
- **Strategy** — para algoritmos intercambiáveis, como importação de dados de fontes diferentes (`DataSourcePort` implementado por `CsvImportAdapter`, `DatasusApiAdapter`), injetados por Spring.
- **Repository** — abstrai o acesso a dados via interface na camada de aplicação (`UnidadeRepositoryPort`), implementada na infraestrutura (`UnidadeRepositoryJpa`/`UnidadeRepositoryImpl`), isolando o domínio do JPA.

## Trade-offs considerados

| Padrão | Problema que resolve | Alternativa descartada |
|---|---|---|
| Factory Method | Criação complexa de objetos com validação | Construtor direto sobrecarregado, sem validação |
| Builder | Objetos com muitos atributos opcionais | Construtores telescópicos (anti-pattern) |
| Strategy | Múltiplas fontes de dados (CSV, API) | `if/else` no caso de uso (viola Open/Closed) |
| Repository | Abstração de persistência | Acesso direto ao JPA a partir do domínio (viola inversão de dependência) |

## Consequências

**Positivas**: código mais expressivo (`factory.criar()`, `builder.build()`); adicionar uma nova fonte de dados vira só uma nova implementação de `DataSourcePort`, sem tocar nos casos de uso existentes; criação de objetos consistente em todo o sistema.

**Negativas**: mais classes/interfaces para gerenciar; curva de aprendizado inicial para quem não conhece os padrões.

**Neutras**: os 4 padrões já são amplamente usados pelo próprio Spring Boot, então não introduzem ferramentas novas — só convenção de uso.
