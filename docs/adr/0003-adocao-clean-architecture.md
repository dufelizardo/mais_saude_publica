# 0003 — Adoção de Clean Architecture (proposta futura)

## Status

Proposto — roadmap futuro, **não implementado**. Não confundir com a arquitetura atual do código (controller/service/repository/DTO em camadas, descrita na ADR 0001 e ADR 0002).

> ⚠️ **Nota de reconciliação**: este ADR foi originalmente escrito em `docs/adr/ADR.md` referenciando um pacote fictício `com.maisaude` e um "ADR-001 (Stack Tecnológico)" que não existe no repositório — o ADR 0001 real trata de outro assunto (restaurar/refatorar o código apagado). O pacote real do projeto é `com.edufelizardo.maissaudepublica`. Nada da estrutura abaixo foi criada ainda. Prioridade de implementação (decidida com o usuário): esta proposta de arquitetura, junto com segurança (0006) e deploy (0007), vem **antes** de novos domínios de negócio (paciente/profissional/atendimento etc., ver `DER.md`).

## Contexto

O projeto está em estágio inicial e usa hoje uma estrutura de camadas tradicional (controller → service → repository → DTO). Para evoluir com múltiplas entidades relacionadas (unidades, profissionais, pacientes, atendimentos, procedimentos, hierarquias), a proposta é reorganizar o código para ser mais manutenível, testável, flexível e independente de frameworks.

Forças e restrições consideradas: complexidade crescente do domínio de saúde pública, evolução contínua de requisitos e fontes de dados, equipe podendo crescer, e requisitos não-funcionais de performance/segurança/rastreabilidade.

## Decisão

Adotar **Clean Architecture**, estruturada em 3 camadas:

- **Domain** — regras de negócio puras, entidades, value objects, enums e exceções de domínio; sem dependências externas nem anotações de framework (JPA, Spring).
- **Application** — casos de uso (use cases), portas de entrada/saída (interfaces) e DTOs; orquestra as entidades de domínio.
- **Infrastructure** — controllers, repositórios JPA, adaptadores (CSV, segurança JWT), configuração; depende das camadas acima via inversão de dependência.

Estrutura de pacotes proposta (sob o pacote real do projeto, não `com.maisaude`):

```
domain/{entity, valueobject, enums, exception, factory}
application/{usecase/{unidade,profissional,paciente}, port/{input, output/{repository, external}}, dto/{request,response}, mapper}
infrastructure/{config, adapter/{input, output/{persistence, csv, security}}, entity, exception}
shared/{validation, utils, constants}
```

Fluxo de uma requisição: Controller (Infrastructure) → DTO → Porta de entrada → Use Case (Application) → Factory cria entidade de domínio → Porta de saída (Repository) → Adapter JPA (Infrastructure) → banco → mapeia de volta → Controller → DTO de resposta.

## Trade-offs considerados

- **Camadas tradicionais (Controller-Service-Repository)** — simples e é o que já existe hoje; rejeitada para esta proposta por acoplar regras de negócio à infraestrutura e dificultar testes isolados.
- **Arquitetura Hexagonal (Ports & Adapters)** — muito parecida com Clean Architecture; considerada, mas Clean Architecture foi preferida por ser mais didática para documentar/ensinar à equipe.
- **Clean Architecture (escolhida)** — melhor desacoplamento e testabilidade, ao custo de mais arquivos/classes e curva de aprendizado.
- **Microsserviços** — rejeitada: overhead operacional/de rede não se justifica para o tamanho atual do projeto.

## Consequências

**Positivas**: domínio protegido de mudanças tecnológicas; casos de uso testáveis com repositórios mockados; troca de banco/ORM implicaria só um novo adapter; estrutura preparada para crescer sem refatoração drástica.

**Negativas**: mais arquivos/classes que uma estrutura MVC tradicional; curva de aprendizado para quem só conhece controller-service-repository; overhead de setup inicial; risco de over-engineering para funcionalidades simples.

**Neutras**: exige padronização rígida da equipe; requer documentação/diagramas para onboarding; compatível com Spring Boot sem ferramentas adicionais.
