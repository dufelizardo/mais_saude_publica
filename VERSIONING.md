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

## Tags e releases (automáticas)

O `<version>` do `pom.xml` é a **única fonte** da versão (ver
[ADR-0056](docs/adr/0056-versao-da-release-vem-do-pom-e-release-automatica.md)). O único passo
manual é incrementá-lo em `developer` enquanto se prepara a próxima release — o resto é do CI
(`publish-image.yml`):

- **Tag e release:** quando um `pom.xml` com versão nova chega em `main`, o job `release` cria a tag
  `vMAJOR.MINOR.PATCH` e a release no GitHub, com notas geradas a partir dos PRs (ver seção abaixo).
  Se a versão já tem release, não cria nada e deixa um aviso no run — sinal de que código novo foi
  promovido sem incrementar a versão. O título gerado é só `vX.Y.Z`; um subtítulo descritivo (como
  nas releases anteriores) pode ser editado depois na página da release.
- **Versão exibida na tela** (rodapé do Login, landing page): gravada na imagem do frontend como
  `/version.json`. Em `main` é a própria versão (`1.4.0`); nas outras branches ganha metadado de
  build SemVer, ex.: `1.4.0+developer.2f3fb87` — "código a caminho da 1.4.0, commit 2f3fb87", ainda
  não uma release publicada. Em build local o arquivo não existe e a tela não mostra versão.

> Até a v1.3.0 as tags foram criadas à mão e o passo de atualizar o `pom.xml` foi pulado a partir
> da v1.1.0 (o `pom.xml` ficou em `1.0.0`). Corrigido junto com a automação: o `pom.xml` passou
> direto para `1.4.0`, a próxima release.

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
