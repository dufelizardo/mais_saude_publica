# 0068 — Tela Usuários & Perfis e menu conforme as permissões

## Status

Aceita e implementada. Fatia 3 de 3 de papéis e escopo
([ADR-0066](./0066-papeis-permissoes-e-escopo-por-unidade.md) e
[ADR-0067](./0067-autorizacao-aplicada-nas-rotas-e-no-escopo.md)).

## Contexto

As fatias 1 e 2 deixaram papéis, atribuições e a exigência de permissão prontos na API, atrás do toggle
`app.security.authorization.enabled`. Faltavam três coisas:
- **uma tela** para cadastrar usuários e conceder ou revogar acesso. Sem ela, só pelo Swagger;
- **cadastrar usuário**: até aqui, o único usuário era o administrador inicial, criado no bootstrap;
- **o menu** não oferecer o que a pessoa não pode usar. Com a exigência ligada, esses itens abririam
  telas que respondem 403.

O usuário trouxe o protótipo `Usuarios.html`. Ele já era a referência do padrão de listagem
(`docs/frontend/PADRAO-TELAS-INTERNAS.md`, seção 2). O protótipo tem:
- cabeçalho com Exportar, Política de senha e Novo usuário;
- quatro indicadores;
- abas: Usuários, Perfis & permissões, Sessões ativas e Política de acesso;
- barra de filtros com etiquetas (perfil ativo, unidade, status, MFA);
- tabela de usuários com paginação, ao lado da lista de perfis RBAC;
- detalhe do perfil selecionado com a **matriz de permissões por módulo** (✓ / ~ / ×).

## Decisão

1. **Tela `Usuários & Perfis`** em `/administracao/usuarios`, num grupo novo do menu, **Administração**.
   Segue a estrutura do protótipo:
   - **Indicadores**, calculados com dados reais. MFA e sessões não existem, então os indicadores
     viraram:
     - usuários ativos;
     - com acesso vigente;
     - acessos vigentes (quantos na rede inteira e quantos com prazo);
     - bloqueados e inativos.
   - **Aba Usuários**:
     - busca por nome, CPF ou matrícula;
     - filtros de unidade e status;
     - tabela paginada com avatar, perfil principal (+N), escopo, número de acessos, último acesso e
       status (Ativo, Bloqueado, Inativo ou Sem acesso).
     - A lista de perfis RBAC fica ao lado. Escolher um perfil **filtra a tabela** e **mostra a
       matriz** abaixo, como no protótipo. A etiqueta "Perfil: X · n ×" desfaz o filtro.
   - **Aba Perfis & permissões**: a lista de perfis e o detalhe do perfil selecionado lado a lado.
   - **Matriz de permissões:** a do protótipo é CRUD por módulo, e o nosso catálogo é `RECURSO.ACAO`
     (ADR-0066). A matriz agrupa as 22 permissões em **9 módulos**, nas colunas **Visualizar /
     Registrar / Gerenciar**:
     - ✓ quando o perfil tem todas as permissões da célula, ~ quando tem parte, × quando não tem
       nenhuma, — quando a célula não se aplica ao módulo;
     - a dica de cada célula lista os códigos com ✓ ou ×;
     - uma permissão nova que ainda não tenha módulo aparece em "Outras permissões". Nada some da
       tela.
   - **Chips do perfil**:
     - quantidade de usuários;
     - código;
     - padrão do sistema ou criado pela administração;
     - **acessa dado de saúde ou não**, pela dimensão das permissões (ADR-0054);
     - ativo ou inativo.
   - **Gavetas:**
     - **usuário** (larga): dados, vínculo com o profissional pelo CPF, acessos vigentes com
       Revogar, "+ Conceder acesso" e o histórico com o motivo de cada revogação;
     - **novo usuário**: busca opcional pela matrícula, que preenche CPF e nome a partir do quadro;
       depois, CPF, nome e senha inicial com confirmação;
     - **editar usuário**: nome e situação;
     - **conceder acesso**: perfil; escopo, que pode ser rede inteira ou uma unidade de qualquer
       nível; início e fim opcionais;
     - **revogar**: motivo obrigatório;
     - **perfil** (novo, editar, duplicar): código, nome, descrição, situação e as permissões
       marcadas por módulo, com o selo "dado de saúde".
   - **Sem backend, "Em breve"** (mesmo padrão da ADR-0052): Exportar, Política de senha, Ações em
     massa, filtro MFA e as abas Sessões ativas e Política de acesso.
2. **Backend novo** (`USUARIO.GERENCIAR`):

   | Rota | O que faz | Erros |
   |---|---|---|
   | `POST /api/v1/usuario/` | Cadastra um usuário | CPF inválido → 400; CPF repetido → 409 |
   | `PATCH /api/v1/usuario/{id}` | Altera nome e situação | Desativar o próprio usuário → 422 |
   | `POST /api/v1/usuario/{id}/desbloqueio` | Libera o bloqueio por tentativas antes do prazo (ADR-0055) | Usuário não bloqueado → 422 |

   - No cadastro, o CPF é conferido com os dígitos verificadores, e a senha inicial tem de 8 a 72
     caracteres.
   - O usuário nasce **sem acesso**. O acesso vem por atribuição.
   - A resposta do usuário ganhou `bloqueado`.
   - **`GET /api/v1/atribuicao-acesso/escopos`** (`ACESSO.GERENCIAR`) lista as unidades de todos os
     níveis, de Federal a UBS, com a unidade acima de cada uma. Só aparecem as do escopo de quem
     consulta. Existe porque as listagens de Regional, Municipal, Estadual e Federal não expõem o
     `uuid`, que a concessão exige.
   - **`GET /auth/status`** passou a informar `authorizationEnabled`.
3. **Menu conforme as permissões.**
   - O `AuthService.acessoDaInterface()` junta o status e o `/auth/eu`.
   - Com a autorização desligada, que hoje vale para todos os ambientes, **nada é escondido**.
   - Ligada, cada item do menu aparece se a pessoa tem uma das permissões do item, pela tabela `MENU`
     do `AppShell`. Um grupo sem nenhum item visível some.
   - Estrutura e quadro de profissionais continuam abertos (ADR-0067).
   - Esconder é só para não oferecer o que responderia 403. Quem decide continua sendo a API.
   - As ações da tela Usuários & Perfis seguem a mesma regra:
     - "Novo usuário" e "Editar usuário" só com `USUARIO.GERENCIAR`;
     - conceder, revogar e mexer em perfil só com `ACESSO.GERENCIAR`.
4. **Rota carregada sob demanda** (`loadComponent`). O bundle inicial já estava no limite de 1 MB do
   orçamento. Telas de administração, pouco usadas, são as primeiras candidatas.

## Trade-offs considerados

**Matriz por módulo × Visualizar/Registrar/Gerenciar (escolhida)** × **lista plana das 22
permissões**
- ✅ Mantém o elemento dominante do protótipo e responde "o que este perfil faz" de relance. A lista
  plana fica na gaveta de edição, onde a precisão importa.
- ❌ O agrupamento é uma tabela no frontend. Uma permissão nova precisa ser colocada num módulo, e até
  lá aparece em "Outras permissões".

**Senha inicial definida por quem cadastra (escolhida)** × **convite por e-mail**
- ✅ Funciona hoje, sem serviço de e-mail. É o mesmo corte de "recuperação de senha em breve" da
  ADR-0055.
- ❌ Quem cadastra conhece a senha inicial. A pendência é a **troca obrigatória no primeiro acesso**.
  A tela orienta a entregar a senha por um canal próprio.

**Busca pela matrícula no cadastro (escolhida)** × **usuário sempre vinculado a um profissional**
- ✅ Na maioria dos casos, a identidade é de alguém do quadro, e o CPF e o nome vêm certos. Contas
  técnicas continuam possíveis (ADR-0054, item 1).

## Consequências

**Positivas:**
- Com esta fatia, a exigência de permissão pode ser ligada num ambiente: conceder os papéis pela tela
  e depois ligar `APP_SECURITY_AUTHORIZATION_ENABLED=true` no overlay.
- O menu passa a acompanhar os acessos de cada pessoa.

**Negativas / pendências:**
- Troca de senha obrigatória no primeiro acesso, e "Esqueci minha senha" (ADR-0055).
- MFA, sessões ativas, política de senha, exportação e ações em massa continuam "Em breve".
- Os botões de registro dentro das outras telas (ex.: "+ Triagem" para quem não é enfermeiro)
  continuam visíveis, e a API responde 403 com uma mensagem clara. Escondê-los tela a tela é
  evolução, usando o mesmo `acessoDaInterface()`.

## Referências

- [`docs/acesso/GUIA-LIGAR-AUTORIZACAO.md`](../acesso/GUIA-LIGAR-AUTORIZACAO.md): passo a passo para ligar a exigência num ambiente.
- [ADR-0066](./0066-papeis-permissoes-e-escopo-por-unidade.md): modelo e catálogo.
- [ADR-0067](./0067-autorizacao-aplicada-nas-rotas-e-no-escopo.md): aplicação nas rotas e no escopo.
- [ADR-0055](./0055-primeira-implementacao-de-login-usuario-jwt-e-toggle-por-ambiente.md): usuário, login e bloqueio.
- [ADR-0052](./0052-tela-de-pacientes-lista-mais-painel-de-detalhe.md): padrão "Em breve".
- [`PADRAO-TELAS-INTERNAS.md`](../frontend/PADRAO-TELAS-INTERNAS.md): estrutura de listagem e menu por permissão.
