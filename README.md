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

Plataforma de gestão de uma **rede pública de saúde** (SUS): a estrutura da rede e os equipamentos, as pessoas
que trabalham nela, a assistência ao cidadão e a gestão da operação. Backend em Spring Boot e frontend em Angular,
com auditoria, controle de acesso por perfil e unidade, e testes de aceitação de API e de interface.

> **Quer ver como o sistema vai ficar?** Navegue pelo
> [protótipo das telas](https://dufelizardo.github.io/mais_saude_publica/): páginas estáticas com
> dados fictícios, sem precisar instalar nada (detalhes em [`prototipo/`](prototipo/)).

## O que já funciona

| Área | O que tem |
|---|---|
| **Organização da rede** | hierarquia federal → estadual → municipal → regional → unidade; Equipamentos de Saúde com horário estruturado e situação operacional |
| **Recursos Humanos** | profissionais, lotação, cargos e salários, benefícios, desenvolvimento, recrutamento, folha, perfil; **equipes de saúde** e **escalas** com modelos de jornada |
| **Administrativo** | setores, modelo administrativo, processos, necessidades de pessoal |
| **Assistência** | pacientes, atendimentos, agenda do profissional, prontuário, enfermagem (triagem, evolução, medicação), farmácia (estoque, dispensação, transferência) |
| **Rede assistencial** | laboratório (pedido, coleta, resultado, laudo), regulação do acesso (fila, agendamento, contrarreferência), leitos e internação |
| **Governança** | login (JWT), perfis e permissões com escopo por unidade, trilha de auditoria com alertas, prontuário por vínculo assistencial |

O que cada domínio já tem e o que falta está em [`docs/STATUS-DOS-DOMINIOS.md`](docs/STATUS-DOS-DOMINIOS.md). O mapa
completo da plataforma (31 itens) está em [`docs/MAPA-DE-DOMINIOS.md`](docs/MAPA-DE-DOMINIOS.md).

> **Ambientes:** o login, a exigência de permissão e o prontuário por vínculo são ligados por ambiente. Hoje o login
> está ligado só em `dev`. O estado de cada ambiente está em [`docs/PENDENCIAS.md`](docs/PENDENCIAS.md), e o passo a
> passo para ligar, em [`docs/acesso/GUIA-LIGAR-AUTORIZACAO.md`](docs/acesso/GUIA-LIGAR-AUTORIZACAO.md).

## Domínio

A plataforma é organizada em domínios com fronteiras explícitas: cada domínio é dono de uma parte do negócio, e os
demais o referenciam sem duplicar (por exemplo, o RH é dono do profissional; Equipes, Escalas e a Agenda só o usam).
A unidade de saúde é uma entidade única autorreferenciada
([ADR-0002](docs/adr/0002-modelar-hierarquia-como-entidade-unica-autorreferenciada.md)), com as esferas de gestão do
SUS ([ADR-0009](docs/adr/0009-renomear-hierarquia-para-esferas-de-gestao-do-sus.md)). O comportamento de cada
equipamento vem de perfis e capacidades, e não de tipos
([ADR-0115](docs/adr/0115-modelo-operacional-dos-equipamentos.md)).

## Endpoints

A API está documentada no Swagger (abaixo). Todas as rotas ficam em `/api/v1/`, com a permissão exigida declarada em
cada uma ([ADR-0067](docs/adr/0067-autorizacao-aplicada-nas-rotas-e-no-escopo.md)).

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

**Índice de toda a documentação: [docs/README.md](docs/README.md).**

Decisões de arquitetura e roadmap técnico: [docs/adr/](docs/adr/README.md).

O que falta e o estado de cada ambiente (o que está ligado ou desligado): [docs/PENDENCIAS.md](docs/PENDENCIAS.md).
O que cada domínio já tem e o que falta, para decidir os próximos passos: [docs/STATUS-DOS-DOMINIOS.md](docs/STATUS-DOS-DOMINIOS.md).
