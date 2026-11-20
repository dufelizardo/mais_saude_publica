# 0222 — Manual do usuário em português e inglês (M1)

## Status

Aceita e implementada.

- **M1:** a estrutura, as regras e os primeiros passos.
- **M2:** as áreas do menu, 37 módulos nas duas línguas, com o sumário na ordem do menu. Os módulos 01 a 12 saíram um por PR; os
  demais, com o fechamento, num PR só, porque o texto já estava pronto e a CI de cada PR não testa documentação.

## Contexto

- **A documentação do repositório é técnica:** ADRs, modelos de domínio, status, pendências e o DER. Ela explica o porquê e o
  como do código, não o uso da tela.
- **Quem usa a plataforma não tem um texto de apoio:** a recepção, a enfermagem, o médico, a farmácia, a regulação e a gestão.
  O único passo a passo (`docs/acesso/GUIA-LIGAR-AUTORIZACAO.md`) é para a operação dos ambientes.
- **A plataforma tem 31 domínios no ar,** e o manual foi pedido em português e em inglês.

## Decisão

1. **Onde:** `docs/manual/`, com `pt-BR/` e `en/`. Fica junto das outras docs e entra na wiki numa seção própria.
2. **Para quem:** o usuário final. O manual não fala de endpoint, tabela nem permissão técnica pelo código; fala da tela, do botão
   e da mensagem que a pessoa vê.
3. **Organização:** pela ordem do menu da aplicação, porque é assim que o usuário procura.
   - `00-primeiros-passos.md`: o acesso, a senha, o menu, o perfil e a unidade, os padrões das telas e as mensagens comuns;
   - um arquivo por área, numerado na ordem do menu, nas fatias seguintes.
   - Cada tela tem quatro partes: **para que serve**, **quem usa**, **passo a passo** e **mensagens comuns**.
4. **As duas línguas:**
   - o português é a fonte; o inglês espelha, com os **mesmos nomes de arquivo**;
   - cada arquivo começa pelo título e pela linha da versão (`> **Versão:** X.Y.Z` / `> **Version:** X.Y.Z`), com o link para
     a outra língua;
   - a versão é a da release que o texto descreve; as duas línguas têm a mesma.
5. **Verificação na CI** (`.github/scripts/verificar_manual.py`, no job `test`, que é obrigatório):
   - cada arquivo existe nas duas línguas;
   - a linha da versão existe, é `X.Y.Z`, é igual nas duas línguas e não passa da versão do `pom.xml`;
   - os links relativos apontam para arquivos que existem.
6. **Regra para frente:** o PR que muda uma tela já coberta pelo manual atualiza o texto nas duas línguas, no mesmo PR. A caixa
   entra no modelo de PR.
7. **Fatias:**
   - **M1** (esta): a estrutura, o índice, os primeiros passos, a verificação e a seção na wiki;
   - **M2 em diante:** as áreas, começando pelo fluxo assistencial (Pacientes, Agenda, Atendimentos, Enfermagem, Farmácia), depois
     a Regulação, os cuidados, a vigilância e a gestão;
   - **por último:** as capturas de tela geradas pelos testes Robot de interface, em `docs/manual/img/`.

## Alternativas descartadas

- **Manual dentro da aplicação (ajuda contextual):** mais caro, e o texto ficaria preso ao código do frontend. Fica para depois,
  e pode ler estes mesmos arquivos.
- **Só a wiki:** a wiki não passa pela CI nem pelo PR; o texto envelheceria sem ninguém ver.
- **Nomes de arquivo traduzidos no inglês:** a verificação e os links entre línguas ficariam mais frágeis; o título já está na
  língua do leitor.
- **Capturas de tela feitas à mão:** envelhecem a cada mudança de layout. Ficam para a fatia das capturas automáticas.

## Consequências

- **O usuário final ganha um texto de apoio** versionado com o código e publicado na wiki.
- **Cada mudança de tela coberta pelo manual custa também o texto** nas duas línguas.
- **Pendências:** as capturas de tela automáticas.

## Testes

- **CI:** `verificar_manual.py` no job `test`, com as quatro checagens da decisão 5.
- **Sem endpoint nem tela nova:** não há JUnit nem Robot nesta fatia.
