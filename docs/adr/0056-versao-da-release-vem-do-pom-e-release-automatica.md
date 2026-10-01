# 0056 — Versão da release vem do `pom.xml`, e a release no GitHub passa a ser automática

## Status

Aceita e implementada.

## Contexto

O mockup da tela de Login e a landing page exibiam "Versão 2026.1", um número inventado. O usuário
pediu que ali apareça a versão real da release, que só passa a existir quando o código sobe para
produção.

Levantamento do estado real:
- O `VERSIONING.md` define o `<version>` do `pom.xml` como a versão do projeto (SemVer) e manda
  criar a tag `vX.Y.Z` depois que ele é atualizado e o código chega em `main`.
- As releases no GitHub foram até **v1.3.0**, mas o `pom.xml` ficou em **`1.0.0`** desde a v1.0.0 —
  o passo manual de atualizá-lo foi pulado três vezes.
- As tags só são alcançáveis a partir de `main`: `git describe --tags` em `developer`, `qaa` e
  `homologacao` não encontra nenhuma.
- A tag é criada **depois** do push em `main`, enquanto a imagem de prod é gerada **no** push.

## Decisão

1. **O `pom.xml` é a única fonte da versão.** Passa direto para `1.4.0` (próxima release: o login
   da ADR-0055 é funcionalidade nova e compatível, MINOR). Incrementá-lo em `developer` é o único
   passo manual do processo.
2. **Versão exibida calculada no CI** (`publish-image.yml`) e gravada na imagem do frontend como
   `/version.json` (build-arg `APP_VERSION`, servido pelo nginx com `Cache-Control: no-store`). Em
   `main` é a própria versão (`1.4.0`); nas demais branches, metadado de build SemVer
   (`1.4.0+developer.2f3fb87`). O `VersaoService` do frontend lê o arquivo; sem ele (build local),
   nenhuma versão é exibida.
3. **Release automática:** um job `release` no `publish-image.yml`, só em `main`, cria a tag
   `vX.Y.Z` e a release no GitHub (`--generate-notes`, categorias de `.github/release.yml`) quando a
   versão do `pom.xml` ainda não tem release. Se já tiver, só emite um aviso no run.

## Trade-offs considerados

**Versão a partir de `git describe --tags` (rejeitada)**
- ✅ Não exige manter o número em arquivo nenhum.
- ❌ As tags não são alcançáveis a partir de `developer`/`qaa`/`homologacao`, e a tag é criada depois
  do build de prod — prod mostraria a versão anterior.

**Mostrar a última release (`1.3.0`) em todos os ambientes (rejeitada)**
- ❌ `dev` roda código posterior à 1.3.0; exibir "1.3.0" lá seria afirmar uma versão que não é
  aquela.

**Pré-release SemVer (`1.4.0-developer...`) em vez de metadado de build (`+`) (rejeitada)**
- ❌ Em SemVer um sufixo `-` indica versão *anterior* à 1.4.0 na ordem de precedência — o metadado
  `+` identifica o build sem implicar ordem.

**Calcular a próxima versão automaticamente a partir dos Conventional Commits (adiada)**
- ✅ Eliminaria até o incremento manual do `pom.xml`.
- ❌ Bem mais peça móvel no pipeline (semantic-release ou similar) para um projeto de uma pessoa só;
  o aviso no job `release` já pega o esquecimento.

## Consequências

**Positivas:** a versão da tela nunca é inventada; prod mostra exatamente a release; `dev`/`qaa`
deixam claro que aquele código ainda não é release; tag e release deixam de depender de lembrar.

**Negativas / pendências:**
- Continua sendo preciso incrementar o `pom.xml` antes de promover para `main` — se esquecer, a
  release não é criada (com aviso no run) e prod segue mostrando a versão anterior.
- O título da release gerada é só `vX.Y.Z`; o subtítulo descritivo usado até a v1.3.0 precisa ser
  editado à mão na página da release, se desejado.
- O backend não expõe a versão (não havia consumidor); a da tela vem da imagem do frontend, gerada
  do mesmo commit e com o mesmo `APP_VERSION`.

## Referências

- [`VERSIONING.md`](../../VERSIONING.md) — processo de versionamento, atualizado por esta ADR.
- [ADR-0010](./0010-fluxo-de-branches-e-pipeline-de-promocao.md) — fluxo de promoção entre branches.
- [ADR-0015](./0015-pinar-imagens-por-sha-para-sincronizacao-automatica-do-argocd.md) — o mesmo
  `publish-image.yml` que ganha os passos de versão e release.
- [ADR-0055](./0055-primeira-implementacao-de-login-usuario-jwt-e-toggle-por-ambiente.md) — tela de
  Login, onde a versão é exibida.
