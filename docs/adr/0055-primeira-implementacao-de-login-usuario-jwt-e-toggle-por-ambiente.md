# 0055 — Primeira implementação de login: `Usuario`, JWT e toggle por ambiente

## Status

Aceita e implementada (parte backend — ver Consequências para o que ainda falta).

## Contexto

O usuário perguntou se dava para ligar login a partir de `developer` em diante, com o ambiente
local desligado, "como se fosse um feature toggle", e trouxe um mockup estático da tela de Login
(duas colunas: painel de marca + formulário CPF/Matrícula, "manter conectado", botão gov.br, aviso
de segurança, barra de acessibilidade) a ser seguido 100% visualmente.

Este é o primeiro código de segurança do projeto. ADR-0006 (mecanismo JWT) e ADR-0054 (modelo
`Usuario`/`Papel`/`Permissao`/`EscopoAcesso`/`AtribuicaoAcesso`/Auditoria) documentaram a decisão de
**forma**, mas ambas dizem explicitamente "nenhuma implementação começa agora". Esta ADR começa a
implementação, mas com um corte deliberadamente menor que o modelo completo da ADR-0054:
**autenticação (login + JWT + toggle), sem ainda RBAC granular**
(`Papel`/`Permissao`/`EscopoAcesso`/`AtribuicaoAcesso`). As ~30 entidades/controllers já
implementados não têm nenhuma anotação de segurança hoje, e a onda de RH/Administrativo/
Assistência/Enfermagem/Farmácia inteira foi construída sem isso — ligar RBAC completo agora
travaria os ~150 testes JUnit/Robot existentes sem nenhum ganho real ainda (ninguém usa
papéis/escopo ainda). Autenticação (você está logado ou não) é uma decisão independente de
autorização (o que você pode fazer) — dá para entregar a primeira sem a segunda.

Investigação confirmou:
- Não existe nenhum Spring Profile por ambiente de deploy hoje — só `dev` (local), `test` (CI) e
  `prod` (usado identicamente pelos 4 overlays `k8s/overlays/{dev,qaa,homologacao,prod}`,
  diferenciados só por `DATABASE_URL`/namespace). O toggle não podia ser um Spring Profile — uma
  property custom (`app.security.enabled`) é o mecanismo certo, aproveitando o
  `configMapGenerator` de `app-config` que cada overlay já usa.
- `Profissional.cpf` não é único (ADR-0017, reconciliação de múltiplos vínculos empregatícios) —
  isso não bloqueia CPF como identificador de login: `Usuario.cpf` é um campo **próprio e único**
  da nova entidade, independente do histórico de `Profissional`. A reconciliação
  `Usuario ↔ Profissional` (quando precisar exibir dados profissionais associados a um login)
  reaproveita `ProfissionalRepository.findByCpfAndAtivoTrue`, mesmo padrão de vínculo fraco por CPF
  da ADR-0014. `findByMatricula` (já existente, único por ADR-0017) resolve o login pela aba
  "Matrícula" do mockup.

## Decisão

1. **`Usuario`** (`models/Usuario.java`): `uuid`, `cpf` (único), `nome`, `senhaHash` (BCrypt),
   `ativo`, `tentativasFalhas`, `bloqueadoAte`, `ultimoAcessoEm`. Sem entidade `Credencial` separada
   nesta fase (YAGNI — só existe um tipo de credencial até "Entrar com gov.br" sair do "Em breve").
2. **`POST /api/v1/auth/login`**: aceita `{ tipo: CPF|MATRICULA, identificador, senha,
   manterConectado }`. `MATRICULA` resolve via `ProfissionalRepository.findByMatricula` → cpf →
   mesmo fluxo de `CPF`. 5 tentativas erradas bloqueiam a conta por 15 minutos
   (`Usuario.tentativasFalhas`/`bloqueadoAte`) — atende ao aviso de segurança do mockup. Mensagem de
   erro sempre genérica ("CPF/matrícula ou senha inválidos"), nunca revela qual parte está errada.
3. **`GET /api/v1/auth/status`**: sempre pública, `{ securityEnabled: boolean }` — fonte única de
   verdade que o frontend consulta para decidir se exige login, sem precisar de `environments/` no
   Angular nem de builds diferentes por ambiente (nenhum existia antes desta ADR).
4. **`SecurityConfig`** (`config/`): um único `SecurityFilterChain`, ramificado por
   `app.security.enabled`. `false` (default local/CI): `permitAll()` em tudo — comportamento
   idêntico ao que o projeto sempre teve, zero impacto nos ~150 testes existentes. `true`: exige JWT
   válido em todo `/api/**`, exceto `/api/v1/auth/**` e Swagger. Sem `@PreAuthorize` em nenhum
   controller ainda — é "autenticado ou não", não "pode fazer X".
5. **JWT** via `io.jsonwebtoken:jjwt` (mesma escolha "stateless" da ADR-0006, sem subir a
   complexidade de um Authorization Server completo). `JwtService` gera uma chave efêmera em
   memória se `app.security.jwt-secret` não estiver configurado — nenhum segredo fica hardcoded no
   git; o custo é que tokens não sobrevivem a um restart até um segredo real ser provisionado
   (variável de ambiente / `Secret` do k8s).
6. **Bootstrap `AdministradorPlataforma`** (ADR-0054 decisão 5):
   `AdministradorPlataformaBootstrap` (`ApplicationRunner`) cria a primeira identidade a partir de
   `app.security.bootstrap.cpf`/`senha` (env vars), só se nenhuma delas estiver vazia e nenhum
   `Usuario` com aquele CPF já existir. Sem papel/permissão associada ainda.
7. **"Manter conectado"**: controla, no frontend, `localStorage` (token de vida mais longa,
   `app.security.jwt-expiration-hours-remember-me`, default 168h) vs. `sessionStorage` (token
   curto, default 8h) — resolve o caso de uso real sem implementar refresh token.
8. **Toggle por ambiente, sequenciamento seguro**: `app.security.enabled=false` em
   `application.properties` (herdado por `dev`/`test`/`prod` — nenhum perfil sobrescreve). **Nenhum
   overlay k8s liga o toggle nesta entrega** — a wiring do secret JWT (`APP_SECURITY_JWT_SECRET`,
   `optional: true` no `k8s/base/deployment-app.yaml`) está pronta, mas `APP_SECURITY_ENABLED` só
   será adicionado ao `configMapGenerator` de `app-config` do overlay `dev` depois que a tela de
   Login (frontend, próxima entrega) estiver mesclada e confirmada — ligar agora quebraria a UI já
   implantada em `developer` (sem tela de login ainda, todo `/api/**` passaria a exigir token).
   Overlays `qaa`/`homologacao`/`prod` só recebem o toggle quando essas branches forem promovidas,
   com sinal explícito do usuário (mesma regra já seguida a sessão inteira).

## Trade-offs considerados

**Property custom + configMap por overlay (escolhida)** — ver investigação acima; nenhum Spring
Profile por ambiente de deploy existia para reaproveitar.

**Ligar `APP_SECURITY_ENABLED=true` no overlay `dev` já nesta entrega (rejeitada)**
- ✅ "Feature completa" de uma vez só.
- ❌ Quebraria a UI já implantada em `developer/dev` entre esta entrega (backend) e a próxima
  (frontend) — todo `/api/**` passaria a exigir token sem nenhuma tela de login existir ainda para
  obtê-lo. Sequenciamento em duas entregas evita essa janela de regressão.

**Segredo JWT hardcoded em `application.properties` para uso local (rejeitada)**
- ✅ Determinístico entre restarts locais.
- ❌ Segredo (mesmo que "só de desenvolvimento") commitado em texto plano no git é um mau hábito que
  se propaga; a chave efêmera gerada em memória resolve o caso de uso local/CI sem esse risco.

**RBAC completo (Papel/Permissao/EscopoAcesso) já nesta entrega (rejeitada)**
- ✅ Implementaria a ADR-0054 por inteiro de uma vez.
- ❌ Nenhum controller tem anotação de autorização para consumir isso ainda — seria um monte de
  tabelas novas sem nenhum comportamento real dependendo delas, e ainda exigiria decidir a matriz de
  permissões por papel (documento-fonte seções 10–18) antes de ter qualquer papel real em uso.
  Autenticação sozinha já entrega o pedido do usuário (login ligado/desligado por ambiente).

## Consequências

**Positivas**: login funcional (CPF ou matrícula), toggle seguro por ambiente sem quebrar nenhum
teste existente, bootstrap do primeiro usuário sem exigir Papel/Permissao ainda, nenhum segredo
commitado no git.

**Negativas / pendências**:
- RBAC granular (`Papel`/`Permissao`/`EscopoAcesso`/`AtribuicaoAcesso`, ADR-0054) continua não
  implementado — quando o toggle estiver ligado, qualquer usuário autenticado acessa qualquer rota.
- **Frontend ainda não implementado** (tela de Login, `AuthService`, guard, interceptor) — próxima
  entrega desta mesma ADR.
- **"Entrar com gov.br"** e **"Esqueci minha senha"** ficam "Em breve" no frontend — integração
  OAuth externa e recuperação por e-mail/SMS são escopo à parte.
- Provisionamento manual necessário do usuário, fora do alcance deste agente: valor real do secret
  JWT e das credenciais do `AdministradorPlataforma` no cluster K3s, para os ambientes que forem de
  fato promovidos; e a decisão de quando de fato ligar `APP_SECURITY_ENABLED=true` em cada overlay.

## Referências

- [ADR-0006](./0006-seguranca-jwt.md) — mecanismo JWT, substituído em parte pela ADR-0054/0055.
- [ADR-0054](./0054-modelo-de-identidade-autorizacao-e-auditoria.md) — modelo completo de
  Identidade/Autorização/Auditoria; esta ADR implementa só a fatia de autenticação dele.
- [ADR-0014](./0014-modulo-profissional-rh-com-vinculo-fraco-por-reconciliacao.md) — padrão de
  vínculo fraco por CPF, reaproveitado para `Usuario ↔ Profissional`.
- [ADR-0017](./0017-numero-de-matricula-automatico-e-cpf-nao-unico.md) — CPF não-único em
  `Profissional`; motivo de `Usuario.cpf` ser um campo independente e único.
- [ADR-0032](./0032-catalogo-de-capacidades-administrativas.md) — precedente de decisão consciente
  sobre o que fica como dado vs. o que fica hardcoded.
