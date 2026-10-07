# Mais Saúde Pública

[![Pipeline de Promoção](https://github.com/dufelizardo/mais_saude_publica/actions/workflows/pipeline.yml/badge.svg?branch=developer)](https://github.com/dufelizardo/mais_saude_publica/actions/workflows/pipeline.yml)
[![CodeQL](https://github.com/dufelizardo/mais_saude_publica/actions/workflows/codeql.yml/badge.svg)](https://github.com/dufelizardo/mais_saude_publica/actions/workflows/codeql.yml)
[![Release](https://img.shields.io/github/v/release/dufelizardo/mais_saude_publica)](https://github.com/dufelizardo/mais_saude_publica/releases/latest)
[![Licença](https://img.shields.io/github/license/dufelizardo/mais_saude_publica)](LICENSE)
[![Protótipo navegável](https://img.shields.io/badge/prot%C3%B3tipo-naveg%C3%A1vel-1351b4?logo=githubpages&logoColor=white)](https://dufelizardo.github.io/mais_saude_publica/)

![Java 17](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot 4.0](https://img.shields.io/badge/Spring_Boot-4.0-6DB33F?logo=springboot&logoColor=white)
![Angular 22](https://img.shields.io/badge/Angular-22-DD0031?logo=angular&logoColor=white)
![PostgreSQL 16](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)
![visitors](https://visitor-badge.laobi.icu/badge?page_id=dufelizardo.visitor-mais_saude_publica)
![GitHub followers](https://img.shields.io/github/followers/dufelizardo?style=social)

API REST para gestão da hierarquia de unidades de saúde do SUS — Federal, Estadual, Municipal e
Regional —, construída em Spring Boot. Cada esfera expõe CRUD completo (criação, busca, listagem,
atualização de dados de contato/horários e desabilitação) sobre suas próprias instituições.

> **Quer ver como o sistema vai ficar?** Navegue pelo
> [protótipo das telas](https://dufelizardo.github.io/mais_saude_publica/): páginas estáticas com
> dados fictícios, sem precisar instalar nada (detalhes em [`prototipo/`](prototipo/)).

## Domínio

A hierarquia é modelada como uma única entidade autorreferenciada (`UnidadeDeSaude`, ver
[ADR-0002](docs/adr/0002-modelar-hierarquia-como-entidade-unica-autorreferenciada.md)), com 4
níveis mapeados às esferas reais de gestão do SUS
(ver [ADR-0009](docs/adr/0009-renomear-hierarquia-para-esferas-de-gestao-do-sus.md)):

| Esfera | Vincula-se a | Campo geográfico próprio |
|---|---|---|
| Federal | — (topo da hierarquia) | — |
| Estadual | Federal | `estado` |
| Municipal | Estadual | `municipio` |
| Regional | Municipal | `regiao` |

## Endpoints

Cada domínio expõe o mesmo conjunto de 8 operações em `/api/v1/{federal,estadual,municipal,regional}/`:

| Método | Path | Descrição |
|---|---|---|
| `POST` | `/` | Cria uma instituição |
| `GET` | `/` | Lista todas as instituições do domínio |
| `GET` | `/{nome}` | Busca uma instituição pelo nome |
| `PATCH` | `/{nome}` | Atualiza o nome |
| `PATCH` | `/contato/{nome}` | Atualiza e-mail/telefones |
| `PATCH` | `/horario-de-funcionamento/{nome}` | Atualiza horário de funcionamento |
| `PATCH` | `/horario-de-atendimento/{nome}` | Atualiza horário de atendimento |
| `DELETE` | `/des-habilitar/{nome}` | Desabilita a instituição |

Estadual, Municipal e Regional exigem também `administracaoSuperior` (o `nome` da instituição do
nível acima, à qual essa unidade se vincula) no corpo de criação.

**Exemplo — `POST /api/v1/federal/`:**

```json
{
  "nome": "Ministério da Saúde",
  "tipo": "FEDERAL",
  "email": "contato@saude.gov.br",
  "telefones": ["6134451000"],
  "endereco": {
    "cep": "70058-900",
    "logradouro": "Esplanada dos Ministérios Bloco G",
    "numeroLogradouro": "S/N",
    "bairro": "Zona Cívico-Administrativa",
    "cidade": "Brasília",
    "estado": "DF",
    "ddd": "61"
  },
  "horarioFuncionamento": { "MONDAY": "08:00 - 18:00" },
  "horarioAtendimento": { "MONDAY": "08:00 - 17:00" }
}
```

> **Autenticação:** não implementada nesta versão da API. Existe uma proposta em
> [ADR-0006](docs/adr/0006-seguranca-jwt.md) (ainda não implementada) para autenticação/autorização
> via JWT.

## Documentação interativa (Swagger)

Com a aplicação no ar: `http://localhost:8080/swagger-ui.html` (UI) e
`http://localhost:8080/v3/api-docs` (spec OpenAPI cru).

## Rodando localmente

Requer PostgreSQL (ver [ADR-0010](docs/adr/0010-fluxo-de-branches-e-pipeline-de-promocao.md) —
o projeto usa Postgres em todos os ambientes, dev incluso).

```bash
# cria o banco local (uma vez só)
createdb -U postgres saudepublica_dev

# roda a aplicação com o profile de dev
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

A aplicação sobe em `http://localhost:8080` por padrão.

## Rodando com Docker

```bash
docker build -t msp-app .
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DATABASE_URL=jdbc:postgresql://<host>:5432/<banco> \
  -e DATABASE_USERNAME=<usuario> \
  -e DATABASE_PASSWORD=<senha> \
  msp-app
```

## Testes

- **JUnit** (`src/test/java`, roda in-process contra a aplicação): `./mvnw test`
- **Robot Framework** (`test/robot/`, roda de fora pra dentro contra a aplicação real, no padrão LKDF):
  testes de aceitação da **API** (via HTTP) e do **frontend** (via navegador, Browser library —
  [ADR-0077](docs/adr/0077-testes-de-frontend-com-robot-framework.md)). Ver [test/robot/README.md](test/robot/README.md)

## Branches e pipeline de CI/CD

Fluxo de promoção `developer → qaa → homologacao → main` (prod), com gate automatizado (build + JUnit +
suíte Robot Framework) antes de cada promoção — detalhes em
[ADR-0010](docs/adr/0010-fluxo-de-branches-e-pipeline-de-promocao.md).

## Mais documentação

Decisões de arquitetura e roadmap técnico: [docs/adr/](docs/adr/README.md).

O que falta e o estado de cada ambiente (o que está ligado ou desligado): [docs/PENDENCIAS.md](docs/PENDENCIAS.md).
O que cada domínio já tem e o que falta, para decidir os próximos passos: [docs/STATUS-DOS-DOMINIOS.md](docs/STATUS-DOS-DOMINIOS.md).
