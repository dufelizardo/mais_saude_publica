# 0224 — Licença GPL-3.0-only declarada e aviso de autoria

## Status

Aceita e implementada.

## Contexto

- **O repositório é público e traz o texto da GPL-3.0 no `LICENSE`,** mas o projeto nunca disse de quem é a obra:
  - o aviso `Copyright (C) <ano> <nome>`, que a própria GPL pede para aplicar, não estava preenchido em lugar nenhum;
  - o `<licenses>` e o `<developers>` do `pom.xml` estavam vazios;
  - o `package.json` não tinha o campo `license`.
- **Sem o aviso de autoria,** fica mais difícil cobrar de uma cópia o crédito e o cumprimento da licença. A busca de cópias
  ([ADR-0223](./0223-busca-de-copias.md)) acha a cópia; a licença com o titular declarado é o que permite cobrar.

## Decisão

1. **Titular:** Eduardo Felizardo Cândido, desde o primeiro commit (dezembro de 2023): `Copyright (C) 2023-2026 Eduardo Felizardo
   Cândido`.
2. **Licença:** **GPL-3.0-only**, a mesma de hoje, agora declarada:
   - **`LICENSE`:** o texto oficial da GPL-3.0, sem alteração;
   - **`NOTICE`:** o aviso de autoria e as condições de redistribuição (manter a licença, o aviso e o crédito, e publicar o
     código-fonte);
   - **`README.md`:** a seção Licença;
   - **`pom.xml`:** o `<licenses>` (GPL-3.0-only) e o `<developers>`;
   - **`frontend/package.json`:** `"license": "GPL-3.0-only"` e o `author`.
3. **"Only", e não "or later":** a versão fica fixa na 3. Uma versão futura da GPL não se aplica sozinha ao projeto; mudar de
   versão ou de licença fica com o titular.
4. **A troca para a AGPL-3.0 não foi decidida aqui:** ela fecha a brecha do uso como serviço web sem publicar o código, e fica
   como escolha do titular no PENDENCIAS.

## Alternativas descartadas

- **Aviso de autoria em cada arquivo de código:** são milhares de arquivos, e o aviso no `NOTICE`, no `README` e nos manifestos
  basta para identificar a obra.
- **Editar o `LICENSE`:** o texto da GPL não pode ser alterado; o aviso vai em arquivo próprio.
- **GPL-3.0-or-later:** daria a terceiros a escolha de versões futuras da GPL, que o titular ainda não conhece.

## Consequências

- **A obra tem titular declarado** no repositório, nos manifestos do backend e do frontend e no `README`.
- **Quem redistribuir** precisa manter o `NOTICE` e o crédito.
- **As cópias anteriores,** como o fork de outubro de 2025, seguem a licença da versão que copiaram.
- **Pendência:** decidir entre continuar na GPL-3.0 ou passar à AGPL-3.0.

## Testes

- **CI:** o `pom.xml` continua compilando e o `package.json` continua válido; não há mudança de código.
