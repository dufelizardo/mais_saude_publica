# Mais Saúde Pública

![visitors](https://visitor-badge.laobi.icu/badge?page_id=dufelizardo.visitor-mais_saude_publica) ![GitHub followers](https://img.shields.io/github/followers/dufelizardo.visitor-mais_saude_publica?style=social) <img src="https://img.shields.io/badge/public-Yes-green"/>

API REST para gestão da hierarquia de unidades de saúde do SUS — Federal, Estadual, Municipal e
Regional —, construída em Spring Boot. Cada esfera expõe CRUD completo (criação, busca, listagem,
atualização de dados de contato/horários e desabilitação) sobre suas próprias instituições.

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
- **Robot Framework** (`test/robot/`, roda de fora pra dentro via HTTP contra a aplicação real —
  118 casos de aceitação): ver [test/robot/README.md](test/robot/README.md)

## Branches e pipeline de CI/CD

Fluxo de promoção `developer → qa → cert → main` (prod), com gate automatizado (build + JUnit +
suíte Robot Framework) antes de cada promoção — detalhes em
[ADR-0010](docs/adr/0010-fluxo-de-branches-e-pipeline-de-promocao.md).

## Mais documentação

Decisões de arquitetura e roadmap técnico: [docs/adr/](docs/adr/README.md).
