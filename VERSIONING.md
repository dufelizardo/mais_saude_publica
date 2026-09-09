# Versionamento

Este documento descreve como o projeto versiona código e artefatos. Para o fluxo de branches e o
pipeline de CI/CD (que é o mecanismo que valida cada mudança antes dela chegar em produção), ver
[ADR-0010](docs/adr/0010-fluxo-de-branches-e-pipeline-de-promocao.md) — este documento trata só de
**numeração de versão**, não repete o fluxo de branches.

## Versionamento Semântico (SemVer)

A versão do projeto (`<version>` em [`pom.xml`](pom.xml)) segue [SemVer](https://semver.org/lang/pt-BR/):
`MAJOR.MINOR.PATCH`.

| Parte | Quando incrementar |
|---|---|
| `MAJOR` | Mudança que quebra compatibilidade do contrato de API já publicado — ex.: renomear/remover um endpoint, mudar o formato de um payload existente. A renomeação da hierarquia genérica para as esferas do SUS ([ADR-0009](docs/adr/0009-renomear-hierarquia-para-esferas-de-gestao-do-sus.md)), quando implementada, é um exemplo real de mudança que vai exigir um `MAJOR`. |
| `MINOR` | Funcionalidade nova, compatível com o que já existe — ex.: um novo endpoint, um novo domínio (paciente/profissional/atendimento). |
| `PATCH` | Correção de bug, sem mudança de comportamento esperado da API. |

A versão `1.0.0` marca o primeiro contrato de API estável e versionado (4 esferas hierárquicas,
CRUD completo) — antes disso o projeto ficou em `0.0.1-SNAPSHOT` indefinidamente, sem nenhuma
versão real publicada.

## Tags

Cada versão publicada em `main` recebe uma tag git `vMAJOR.MINOR.PATCH` (ex.: `v1.0.0`), criada
depois que o `pom.xml` é atualizado e a mudança chega em `main` pelo fluxo normal de promoção. A
tag é o que aciona a geração de release notes (ver seção abaixo).

## Changelog / Release Notes

Não há changelog mantido manualmente. Em vez disso, cada tag gera uma *release* no GitHub com
notas geradas automaticamente a partir dos pull requests mergeados desde a tag anterior,
categorizadas pelas labels do PR — configuração em [`.github/release.yml`](.github/release.yml).
Para isso funcionar bem, todo PR deve ter pelo menos uma label (`enhancement`, `bug`,
`documentation` etc.).

## Mensagens de commit

Convenção adotada: [Conventional Commits](https://www.conventionalcommits.org/pt-br/) —
`tipo: descrição`, com os tipos mais comuns:

- `feat:` — funcionalidade nova
- `fix:` — correção de bug
- `docs:` — documentação (README, wiki, ADRs)
- `refactor:` — mudança de código sem alterar comportamento
- `test:` — testes
- `ci:` — pipeline, workflows, configuração de build
- `chore:` — manutenção geral (dependências, config)

Quando o commit resolve ou está relacionado a uma issue, referenciar no corpo da mensagem
(`Refs #N` ou `Closes #N`) — isso mantém a rastreabilidade bidirecional entre código e o
[GitHub Project](https://github.com/users/dufelizardo/projects/1).

> Esta convenção vale a partir de agora — o histórico de commits anterior a este documento não
> segue este padrão retroativamente (reescrever histórico de commits já publicado é uma operação
> destrutiva e não foi feita aqui).
