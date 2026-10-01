# 0067 — Autorização aplicada nas rotas e no escopo da unidade

## Status

Aceita e implementada. É a fatia 2 de 3 da [ADR-0066](./0066-papeis-permissoes-e-escopo-por-unidade.md);
a fatia 3 (tela e menu) é a [ADR-0068](./0068-tela-usuarios-e-perfis.md).
O código está pronto, mas a exigência de permissão fica **desligada em todos os ambientes** até ser
ligada de propósito (ver decisão 1).

## Contexto

A ADR-0066 criou o catálogo de permissões, os papéis padrão, as atribuições com escopo e o cálculo
"este usuário pode fazer X nesta unidade?". Nenhuma rota consultava esse cálculo ainda. Ficaram
pendentes quatro regras:
- retificação de registro clínico só pelo autor ou pela supervisão (ADR-0062);
- recebimento de transferência só na unidade destino (ADR-0061);
- listagens restritas à unidade de quem consulta;
- concessão de acesso dentro do próprio escopo (governança da ADR-0054).

## Decisão

1. **Toggle próprio:** `app.security.authorization.enabled`, que só age com o login ligado
   (`app.security.enabled`, ADR-0055). Hoje o login está ligado no `dev`, e lá só o administrador
   inicial tem papel. Esse papel não dá acesso a dado de saúde (ADR-0066). Ligar a exigência junto
   com o código tiraria as telas clínicas de quem testa o `dev` até essa pessoa se conceder papéis, e
   a tela para isso só chega na fatia 3. Por isso o default é `false` em todos os ambientes. Liga-se
   por ambiente quando os acessos estiverem concedidos. Desligada, nada muda: local, CI e ambientes
   continuam como hoje.
2. **Permissão da rota:** `@RequerPermissao({...})` no método do controller. Basta uma das
   permissões listadas, em qualquer escopo. Um `HandlerInterceptor` confere a permissão antes do
   controller.
   - `@LiberadoParaAutenticados` marca o que qualquer usuário logado pode acessar: leitura da
     estrutura (unidades e setores), quadro de profissionais e `/auth`.
   - **Negado por padrão:** uma rota sem nenhuma das duas anotações responde 403. Um endpoint novo
     esquecido não fica aberto.
   - Um teste percorre todas as rotas `/api/**` e falha se alguma não tiver definição ou citar uma
     permissão fora do catálogo.
3. **Matriz das rotas** (leitura / escrita):

   | Área | Leitura | Escrita |
   |---|---|---|
   | RH (24 controllers) | `RH.CONSULTAR`/`GERENCIAR` | `RH.GERENCIAR` |
   | Setor administrativo | `ADMINISTRATIVO.CONSULTAR`/`GERENCIAR` | `ADMINISTRATIVO.GERENCIAR` |
   | Unidades e setores | liberada | `ORGANIZACAO.GERENCIAR` |
   | Profissional | liberada | `RH.GERENCIAR` |
   | Paciente | `PACIENTE.CONSULTAR` | `PACIENTE.CADASTRAR` |
   | Agendamento | `AGENDAMENTO.GERENCIAR` ou `ATENDIMENTO.GERENCIAR` | `AGENDAMENTO.GERENCIAR` |
   | Atendimento | `ATENDIMENTO.GERENCIAR` ou `PRONTUARIO.CONSULTAR` | `ATENDIMENTO.GERENCIAR` |
   | Triagem / evolução / consulta / procedimento / medicação | `PRONTUARIO.CONSULTAR` | `TRIAGEM.` / `EVOLUCAO.` / `CONSULTA.` / `PROCEDIMENTO.REGISTRAR`, `MEDICACAO.ADMINISTRAR` |
   | Prontuário | `PRONTUARIO.CONSULTAR` | — |
   | Medicamento | quem consulta, dispensa, prescreve ou administra | `FARMACIA.GERENCIAR_ESTOQUE` |
   | Lote | `FARMACIA.CONSULTAR`/`DISPENSAR` ou `MEDICACAO.ADMINISTRAR` | `FARMACIA.GERENCIAR_ESTOQUE` |
   | Livro, dispensação, transferência | `FARMACIA.CONSULTAR` (+ a de escrita) | `GERENCIAR_ESTOQUE` / `DISPENSAR` / `TRANSFERIR` |
   | Papéis, permissões e atribuições | `ACESSO.GERENCIAR` | `ACESSO.GERENCIAR` |
   | Usuários | `ACESSO.GERENCIAR` ou `USUARIO.GERENCIAR` | — |

4. **Escopo da unidade nos serviços.** A rota só confere se a pessoa tem a permissão em algum
   escopo. Cada serviço confere a unidade do registro:
   - **Registrar**: exige a permissão numa unidade coberta pelo acesso, ou nas unidades acima dela.
     A unidade conferida é:
     - no atendimento, a unidade informada, na criação e na alteração;
     - na triagem, evolução, consulta e medicação, a unidade do atendimento;
     - no procedimento, a unidade do atendimento da consulta;
     - na entrada de lote, perda/ajuste e dispensação, a unidade do lote;
     - no envio e no cancelamento de transferência, a unidade de origem;
     - **no recebimento de transferência, a unidade de destino**.
   - **Listar e detalhar:** cada listagem mostra só os registros de unidades cobertas. Quem tem
     acesso na rede inteira vê tudo. O detalhe de um registro de fora do escopo responde 403. A
     transferência aparece para a origem e para o destino.
   - **Prontuário por paciente: rede inteira** para quem tem `PRONTUARIO.CONSULTAR`. O paciente do SUS
     circula pela rede, e a continuidade do cuidado exige ver o que foi registrado em outra unidade.
     O que se restringe é **registrar** fora da unidade, não ler o histórico do paciente atendido.
     Paciente e agendamento também não têm unidade e são da rede.
5. **Retificação:** só o **autor** (`registradoPorCpf` igual ao CPF logado) ou quem tem
   `REGISTRO_CLINICO.RETIFICAR_DE_OUTROS` na unidade do registro. Um registro sem autor gravado
   (feito antes do login) só é retificado pela supervisão. A permissão de registrar na unidade também
   é exigida.
6. **Concessão de acesso:**
   - Conceder ou revogar exige `ACESSO.GERENCIAR` num escopo que cubra a unidade; sem unidade, na rede
     inteira.
   - Um papel com qualquer permissão de **administração do sistema** só é concedido por quem gerencia
     acesso na rede inteira. O gestor de uma UBS concede enfermeiro ou farmacêutico na UBS dele, mas
     não cria outro administrador.
   - Criar ou alterar papéis exige `ACESSO.GERENCIAR` na rede inteira, porque o papel vale para todas
     as unidades.
   - A listagem de atribuições mostra só as do escopo de quem consulta.
7. **403** (`ResourceForbiddenException`) para "autenticado, mas sem permissão ou fora do escopo", com
   uma mensagem que diz qual permissão ou unidade faltou. O 401 continua sendo "sem login".

## Trade-offs considerados

**Anotação por rota com interceptor (escolhida)** × **`@PreAuthorize` do Spring Security**
- ✅ Uma linha visível em cada método, sem expressão SpEL. O "negado por padrão" fica fácil de
  garantir e de testar.
- ❌ Não é o mecanismo padrão do Spring. É pouco código próprio, e o teste de cobertura protege contra
  esquecimento.

**Escopo conferido no serviço (escolhida)** × **na rota**
- ✅ A unidade vem do registro (atendimento, lote, transferência), que só o serviço conhece.
- ❌ A regra fica espalhada pelos serviços. O `ControleDeAcesso` concentra as chamadas em poucas
  formas: `exigir`, `exigirVisivel`, `filtrar` e `exigirAutoriaOuSupervisao`.

**Prontuário na rede (escolhida)** × **prontuário só da unidade**
- ✅ Continuidade do cuidado: o médico da UPA vê a triagem feita na UBS.
- ❌ Alcance de leitura maior. O mitigador é a auditoria de leitura, que é a parte pendente da
  ADR-0054 (item 6).

**Toggle desligado por padrão (escolhida)** × **ligar junto com o login**
- ✅ Nenhum ambiente fica sem acesso por surpresa.
- ❌ Até ser ligada, a regra não protege nada. Ligar exige conceder os papéis antes (pela API/Swagger
  ou pela tela da fatia 3) e acrescentar `APP_SECURITY_AUTHORIZATION_ENABLED=true` no overlay do
  ambiente.

## Consequências

**Positivas:**
- As regras de autoria e unidade destino que ficaram pendentes nas ADRs 0061 e 0062 agora existem.
- As listagens respeitam a unidade.
- O técnico de enfermagem não consegue registrar classificação de risco.
- Nenhuma rota nova nasce aberta.

**Negativas / pendências:**
- **Fatia 3**:
  - tela Usuários & Perfis, para conceder e revogar sem Swagger;
  - esconder no frontend o que a pessoa não pode usar, a partir das `permissoes` do `/auth/eu`.

  Até lá, com a exigência ligada, uma tela sem permissão mostra o erro 403 da API.
- Algumas telas combinam áreas. A recepção, por exemplo, abre Atendimentos, mas não lê triagem. Nesse
  caso a tela mostra só parte do conteúdo até ser adaptada na fatia 3.
- Auditoria de leitura de prontuário (ADR-0054, item 6): registro feito pela
  [ADR-0070](./0070-trilha-de-auditoria.md); a consulta vem na fatia seguinte.

## Referências

- [`docs/acesso/GUIA-LIGAR-AUTORIZACAO.md`](../acesso/GUIA-LIGAR-AUTORIZACAO.md): passo a passo para ligar num ambiente.
- [ADR-0066](./0066-papeis-permissoes-e-escopo-por-unidade.md): modelo e cálculo (fatia 1).
- [ADR-0054](./0054-modelo-de-identidade-autorizacao-e-auditoria.md): forma do modelo.
- [ADR-0055](./0055-primeira-implementacao-de-login-usuario-jwt-e-toggle-por-ambiente.md): toggle de login.
- [ADR-0061](./0061-transferencia-em-duas-etapas-envio-e-recebimento.md): recebimento no destino.
- [ADR-0062](./0062-registros-clinicos-imutaveis-com-retificacao.md): retificação.
