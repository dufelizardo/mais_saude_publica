# 0007 — Estratégia de deploy com Docker e CI/CD (proposta futura)

## Status

Proposto — **não implementado**. O repositório tem um `Dockerfile` real (restaurado pela ADR 0001) mas não há `docker-compose.yml` nem pipeline de CI/CD hoje.

> Prioridade de implementação (decidida com o usuário): esta proposta entra no grupo prioritário (junto com Clean Architecture e JWT), antes de novos domínios de negócio.

## Contexto

O deploy precisa ser consistente entre desenvolvimento, homologação e produção, fácil de rodar para novos desenvolvedores, e preparado para escalar. Dependências como banco de dados precisam ser isoladas e reproduzíveis.

## Decisão

Adotar **Docker** + **Docker Compose** para containerização local/homologação, com **GitHub Actions** para CI/CD.

- **Dockerfile multi-stage**: build com `maven:3.8-openjdk-17`, imagem final leve `eclipse-temurin:17-jre-alpine`, com healthcheck em `/actuator/health`.
- **docker-compose.yml** com serviços: `postgres` (15-alpine), `redis` (7-alpine, cache), `app` (a API), e `adminer` (UI de banco, só em perfis `dev`/`test`).
- Perfis de ambiente via `application-{dev,homol,prod}.yml`/properties, com `ddl-auto: update` em dev e `validate` em homol/prod, e Flyway para migrations.
- Pipeline GitHub Actions (`test` → `build/push para ghcr.io` → `deploy via SSH`), disparado em push para `main`/`develop` e PRs para `main`.

> ⚠️ **Nota de reconciliação**: o `Dockerfile` real do projeto usa `.properties` (não `.yml`) para configuração, e a versão do Spring Boot é 4.0.3 (não 3.x como assumido aqui) — ajustar a base da imagem Maven/JDK e os nomes de arquivo de configuração ao implementar, para não divergir do que já existe no `pom.xml` e em `src/main/resources/`.

## Trade-offs considerados

| Alternativa | Vantagens | Desvantagens | Decisão |
|---|---|---|---|
| Deploy manual (JAR) | Simples, sem overhead | Inconsistente entre ambientes, difícil escalar | ❌ Rejeitada |
| Docker + Compose | Consistente, fácil setup (`docker-compose up`), escalável | Curva de aprendizado, overhead inicial | ✅ Escolhida |
| Kubernetes | Orquestração avançada, alta disponibilidade | Complexo demais para o estágio atual | ⚠️ Futuro |
| PaaS (Heroku, Fly.io) | Simplicidade, gerenciado | Custo recorrente, vendor lock-in | ❌ Rejeitada |

## Consequências

**Positivas**: onboarding rápido (`docker-compose up` e o projeto roda); isola dependências (não precisa instalar Postgres/Redis localmente); portável entre ambientes; pipeline de CI/CD desde o início.

**Negativas**: equipe precisa aprender Docker/Compose; overhead de recursos em desenvolvimento; mais arquivos de configuração para manter (Dockerfile, compose, perfis, workflow).

**Neutras**: portas fixas ocupadas (8080 app, 5432 Postgres, 6379 Redis, 8081 Adminer); dados do banco persistem via volumes nomeados.
