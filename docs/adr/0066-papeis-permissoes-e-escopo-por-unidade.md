# 0066 — Papéis, permissões e escopo por unidade

## Status

Aceita e implementada (fatia 1 de 3: modelo, catálogo e cálculo). Implementa a forma da ADR-0054
para autorização. Exigir permissão nas rotas é a fatia 2, e a tela Usuários & Perfis é a fatia 3.

## Contexto

Com o login ligado (ADR-0055) e o profissional preenchido a partir dele (ADR-0065), o sistema sabe
**quem** está usando a tela. Ainda não sabe **o que essa pessoa pode fazer, e onde**. Qualquer usuário
logado pode:
- dispensar medicamento em qualquer unidade;
- receber uma transferência destinada a outra unidade;
- registrar classificação de risco;
- retificar o registro clínico de outro profissional.

A ADR-0054 já fixou a forma:
- `Usuario` separado de `Profissional`;
- papéis compostos por permissões `RECURSO.ACAO`, com o catálogo guardado como dado;
- escopo que reaproveita a hierarquia de `UnidadeDeSaude`;
- `Cargo` (RH) diferente de `Papel`;
- bootstrap pelo administrador da plataforma;
- administrar o sistema não dá acesso a dado de saúde.

Esta ADR decide como isso é implementado.

Ligar tudo de uma vez travaria as ~30 rotas existentes e as suítes de teste. Por isso a entrega foi
dividida em três fatias:
1. modelo e cálculo (esta ADR);
2. aplicação nas rotas, filtro por escopo e as regras pendentes: só o autor ou um supervisor retifica,
   e só a unidade destino recebe uma transferência;
3. tela de administração.

## Decisão

1. **Catálogo de 22 permissões `RECURSO.ACAO`** (`TB_PERMISSAO`), semeado na subida por
   `CatalogoDeAcesso`. Cada permissão tem uma **dimensão**, que deixa explícita a separação exigida
   pela ADR-0054:
   - `ADMINISTRACAO_DO_SISTEMA`: usuários, acessos, estrutura;
   - `OPERACAO`: cadastro, agenda, estoque, RH;
   - `ACESSO_AO_DADO_DE_SAUDE`: prontuário e registros clínicos.

   Uma permissão nova é uma linha no catálogo, sem enum novo.
2. **Oito papéis padrão** (`TB_PAPEL` + `TB_PAPEL_PERMISSAO`), também semeados:
   - `ADMINISTRADOR_PLATAFORMA`: usuários, acessos e organização; **nenhuma permissão de dado de
     saúde**.
   - `GESTOR`: RH, setor administrativo, consulta de estoque e concessão de acesso no seu escopo.
   - `RECEPCAO`: pacientes, agenda e abertura de atendimento (acolhimento).
   - `MEDICO`: consulta, prescrição e procedimento.
   - `ENFERMEIRO`: classificação de risco, evolução, procedimento e medicação.
   - `COORDENADOR_DE_ENFERMAGEM`: tudo o que o enfermeiro faz, mais
     `REGISTRO_CLINICO.RETIFICAR_DE_OUTROS` (supervisão).
   - `TECNICO_DE_ENFERMAGEM`: procedimento e medicação. **Não** faz classificação de risco nem
     evolução, que são privativas do enfermeiro (Lei 7.498/86; Resolução COFEN 661/2021).
   - `FARMACEUTICO`: estoque, dispensação, transferência e leitura da prescrição.

   A semente é **idempotente e não sobrescreve**: cria o que falta. Um papel padrão ajustado pela
   administração continua como foi ajustado. Outros papéis podem ser criados pela API. O código do
   papel não muda depois de criado.
3. **`AtribuicaoAcesso`** (`TB_ATRIBUICAO_ACESSO`) liga usuário, papel e escopo:
   - Escopo **sem unidade** vale para a rede inteira.
   - Escopo **com unidade** vale para ela e para todas as que estão abaixo, tanto pela
     `unidadeSuperior` quanto pela `supervisaoRegional`, recursivamente. Um acesso na Regional cobre
     as UBS dela. O caminho inverso não vale: um acesso na UBS não sobe para a Regional.
   - O período `inicio`/`fim` é opcional, para acesso temporário (ex.: plantão).
   - A atribuição **não é apagada**: ela é **revogada** com motivo, autor e hora, e o histórico de
     quem teve acesso a quê fica guardado. Usuário, papel, escopo e período não mudam depois da
     concessão; para mudar, revoga-se e concede-se de novo.
   - Quem concede fica em `concedidoPorCpf`.
4. **Regras da concessão:**

   | Situação | Resposta |
   |---|---|
   | Usuário, papel ou unidade inexistente | 404 |
   | Papel inativo | 422 |
   | Fim antes do início | 400 |
   | Mesmo papel no mesmo escopo já vigente | 409 |
   | Revogar um acesso já revogado | 422 |
   | Desativar o `ADMINISTRADOR_PLATAFORMA` ou tirar dele `ACESSO.GERENCIAR` | 422 |

   A última regra existe porque, sem ela, ninguém mais conseguiria conceder acesso.
5. **`AutorizacaoService`** responde "este CPF tem a permissão X nesta unidade?" e "quais são as
   permissões efetivas deste CPF?". Só conta atribuição não revogada, dentro do período e de papel
   ativo. Nesta fatia o serviço **só calcula**: nenhuma rota consulta o resultado ainda.
6. **Bootstrap:** o administrador inicial (ADR-0055) recebe `ADMINISTRADOR_PLATAFORMA` em toda a rede
   quando ainda não tem nenhuma atribuição. Isso vale também onde o usuário já existia antes dos
   papéis, como o `dev`.
7. **API** (tag "Acesso"):

   | Método e rota | O que faz |
   |---|---|
   | `GET /api/v1/permissao/` | Lista o catálogo de permissões |
   | `GET /api/v1/papel/` e `GET /api/v1/papel/{id}` | Lista papéis e busca um por id, com as permissões e o marcador `padrao` |
   | `POST /api/v1/papel/` e `PATCH /api/v1/papel/{id}` | Cria um papel e altera nome, descrição, situação e permissões |
   | `GET /api/v1/atribuicao-acesso/?usuarioId=` | Lista atribuições, incluindo as revogadas, marcadas com `vigente=false` |
   | `POST /api/v1/atribuicao-acesso/` | Concede um acesso |
   | `POST /api/v1/atribuicao-acesso/{id}/revogacao` | Revoga um acesso |
   | `GET /api/v1/usuario/` e `GET /api/v1/usuario/{id}` | Lista usuários e busca um por id, **nunca com o hash da senha** |

   As mutações seguem o padrão do projeto e devolvem `SuccessResponseDto`.
8. **`GET /auth/eu`** também informa os **acessos vigentes** (papel e unidade) e as **permissões
   efetivas**, para a interface mostrar só o que a pessoa pode usar.

## Trade-offs considerados

**Fatiar (escolhida)** × **modelo e aplicação juntos**
- ✅ O modelo entra sem risco para as telas e testes existentes. A aplicação nas rotas (fatia 2)
  chega com os papéis já semeados e o administrador já com acesso, sem precisar de migração de
  emergência.
- ❌ Durante a fatia 1, as permissões existem mas não protegem nada. Isso fica visível no `/auth/eu`
  e é registrado aqui como estado intermediário.

**Escopo herdado pela hierarquia (escolhida)** × **uma atribuição por unidade**
- ✅ Segue a ADR-0054: quem coordena uma Regional não precisa de dezenas de atribuições.
- ❌ Mover uma unidade de lugar na hierarquia muda quem enxerga essa unidade. É o comportamento
  esperado, mas precisa ser lembrado ao reorganizar unidades.

**Revogar (escolhida)** × **apagar a atribuição**
- ✅ Auditoria: responde quem tinha acesso a um prontuário numa certa data.
- ❌ A tabela só cresce. O volume é pequeno perto dos registros clínicos.

**Papéis padrão semeados sem sobrescrever (escolhida)** × **papéis fixos em código**
- ✅ Cada município ajusta os papéis à sua realidade sem redeploy, que é o catálogo como dado da
  ADR-0054.
- ❌ Uma correção nos papéis padrão depois da primeira subida não chega sozinha às bases existentes.
  Ela precisa ser aplicada pela API ou por um ajuste de dados explícito.

**Governança "ninguém concede acima do próprio nível"** continua como regra de governança
(ADR-0054, item 5), sem validação mecânica nesta fatia. A fatia 2 exige `ACESSO.GERENCIAR` no escopo
da unidade concedida, o que já impede conceder fora do próprio escopo.

## Consequências

**Positivas:**
- A matriz de quem faz o quê está documentada e semeada, com a restrição legal da enfermagem.
- O administrador técnico não lê dado de saúde.
- O histórico de acesso é auditável.

**Negativas / pendências:**
- **Fatia 2**:
  - `@PreAuthorize` (ou filtro equivalente) nas rotas;
  - filtro de listagens pelo escopo;
  - retificação só pelo autor, ou por quem tem `REGISTRO_CLINICO.RETIFICAR_DE_OUTROS`;
  - recebimento de transferência só na unidade destino;
  - concessão exigindo `ACESSO.GERENCIAR` no escopo concedido.
- **Fatia 3**: tela Usuários & Perfis, a partir do protótipo `Usuarios.html` do usuário, com cadastro
  e bloqueio de usuário (`USUARIO.GERENCIAR`).
- Com o toggle de segurança desligado (local e CI), `concedidoPorCpf`/`revogadoPorCpf` ficam nulos.
  É o mesmo comportamento de `registradoPorCpf`.

## Referências

- [ADR-0054](./0054-modelo-de-identidade-autorizacao-e-auditoria.md): forma do modelo (esta ADR a implementa).
- [ADR-0055](./0055-primeira-implementacao-de-login-usuario-jwt-e-toggle-por-ambiente.md): `Usuario`, login e bootstrap.
- [ADR-0065](./0065-profissional-preenchido-a-partir-do-login.md): `/auth/eu`.
- [ADR-0062](./0062-registros-clinicos-imutaveis-com-retificacao.md): retificação (regra de autoria na fatia 2).
- [ADR-0061](./0061-transferencia-em-duas-etapas-envio-e-recebimento.md): recebimento de transferência (regra de unidade na fatia 2).
