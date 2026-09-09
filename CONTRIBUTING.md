# Contribuindo para Mais Saúde Pública

Obrigado por considerar contribuir para o projeto **Mais Saúde Pública**! Este documento descreve o processo real de contribuição, alinhado com o que já está em produção — para o "porquê" por trás de cada decisão, ver os [ADRs](docs/adr/README.md) e a [wiki](https://github.com/dufelizardo/mais_saude_publica/wiki).

## Como posso contribuir?

- **Reportando bugs ou sugerindo funcionalidades:** abra uma [Issue](https://github.com/dufelizardo/mais_saude_publica/issues).
- **Corrigindo bugs ou implementando funcionalidades:** veja as issues abertas (ou o [board do projeto](https://github.com/users/dufelizardo/projects/1)) e envie um Pull Request.
- **Melhorando a documentação:** README, wiki, ADRs — tudo aceita revisão.

## Requisitos

- [Git](https://git-scm.com/) instalado.
- Java 17+ (o `mvnw`/`mvnw.cmd` do repositório já resolve o Maven, não precisa instalar à parte).
- PostgreSQL local, ou Docker (ver [README.md](README.md#rodando-localmente) e [README.md](README.md#rodando-com-docker)).

## Fluxo de trabalho

1. Parta da branch `developer` atualizada.
2. Faça suas alterações. Rode os testes localmente antes de abrir o PR:
   ```bash
   ./mvnw test
   ```
3. Commit seguindo [Conventional Commits](https://www.conventionalcommits.org/pt-br/) (ver [VERSIONING.md](VERSIONING.md#mensagens-de-commit) para os tipos usados) e referenciando a issue relacionada quando houver:
   ```bash
   git commit -m "fix: corrige X (Refs #123)"
   ```
4. Envie a branch e abra um Pull Request para `developer` (ou para a branch de destino da mudança).
5. Aguarde o gate automatizado (build + JUnit + suíte Robot Framework) passar — acompanhe na aba Actions.
6. PRs de contribuidores externos exigem 1 aprovação antes de mergear (Ruleset de proteção — ver [ADR-0011](docs/adr/0011-fase-2-melhorias-de-pipeline-e-branching.md)).

Detalhes completos do fluxo de branches (`developer → qaa → homologacao → main`) e do pipeline de CI/CD: [ADR-0010](docs/adr/0010-fluxo-de-branches-e-pipeline-de-promocao.md) e a página [Onboarding e Desenvolvimento](https://github.com/dufelizardo/mais_saude_publica/wiki/Onboarding-e-Desenvolvimento) da wiki.

## Estilo de código

Sem linter/formatter automatizado configurado ainda no `pom.xml` — siga o estilo já presente no código (indentação, nomenclatura em português para o domínio de negócio, inglês para termos técnicos genéricos). Comentários só onde o "porquê" não é óbvio a partir do código.

## Testes

Todo PR passa automaticamente pelas duas suítes na pipeline — não é obrigatório rodar as duas localmente antes de abrir o PR, mas economiza um ciclo de CI:

| Suíte | Local | Como rodar |
|---|---|---|
| **JUnit** | `src/test/java` | `./mvnw test` |
| **Robot Framework** (118 casos de aceitação) | `test/robot/` | ver [test/robot/README.md](test/robot/README.md) |

Novos recursos ou correções devem vir acompanhados de teste correspondente.

## Reporte de issues

- **Título:** descritivo.
- **Descrição:** o que aconteceu, o esperado, como reproduzir.
- **Label:** classifique como `bug`, `enhancement`, `documentation` etc. — isso também alimenta a geração automática de release notes (ver [VERSIONING.md](VERSIONING.md)).

## Código de Conduta

Siga o [Código de Conduta](CODE_OF_CONDUCT.md) em todas as interações.
