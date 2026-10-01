# 0078 — Sessões encerradas na hora: versão de sessão no token

## Status

Aceita e implementada. Resolve a pendência "invalidação das sessões ativas" da
[ADR-0069](./0069-troca-de-senha-e-senha-provisoria.md) e do guia de ligar a autorização.

## Contexto

O login emite um JWT que vale 8 horas, ou 7 dias com "manter conectado"
([ADR-0055](./0055-primeira-implementacao-de-login-usuario-jwt-e-toggle-por-ambiente.md)). O filtro de
autenticação conferia só a assinatura e a validade do token, sem consultar o banco. Por isso:
- **um usuário desativado continuava usando o sistema** com o token que já tinha, até ele expirar;
- quem teve a senha redefinida pela administração continuava logado com a sessão antiga;
- trocar a própria senha não tirava do sistema um computador esquecido logado;
- não havia como a administração derrubar as sessões de alguém em caso de uso indevido.

Isso precisa estar resolvido antes de ligar a autorização em qualquer ambiente.

## Decisão

1. **O usuário passa a ter uma versão de sessões** (`Usuario.versaoSessao`, inteiro, 0 por padrão nas
   linhas que já existiam). Ela vai no token como o claim `sv`.
2. **O filtro confere o usuário a cada requisição.** Ele precisa:
   - existir;
   - estar ativo;
   - ter a mesma versão de sessões do token.

   Se uma dessas condições falha, a requisição segue sem autenticação e a API responde **401**. O front já
   trata o 401: limpa o token e leva à tela de login com o `returnUrl`.
3. **A versão sobe, encerrando todas as sessões abertas, quando:**
   - a administração **desativa** o usuário. Reativar não ressuscita o token antigo;
   - a administração **redefine a senha** (senha provisória);
   - a pessoa **troca a própria senha**. As outras sessões caem e só o token novo, devolvido na troca,
     continua valendo;
   - a administração **encerra as sessões**: rota nova `POST /usuario/{uuid}/encerramento-de-sessoes`, com
     `USUARIO.GERENCIAR`. A senha não muda e a pessoa entra de novo normalmente. Não vale para o próprio
     usuário (422), que deve trocar a senha.
4. **Tokens emitidos antes desta ADR**, sem o claim, contam como versão 0. Eles continuam valendo até a
   primeira vez que a versão do usuário subir, então ninguém é deslogado no deploy.
5. **Na tela Usuários & Perfis,** a ficha do usuário ganha o botão **Encerrar sessões**, escondido para o
   próprio usuário. O texto da senha provisória passa a dizer que as sessões abertas são encerradas.

## Trade-offs considerados

**Versão de sessão no token (escolhida)** × **lista de tokens revogados** × **token curto com renovação**
- ✅ É uma coluna e um claim, e a comparação é exata. Não há tabela de tokens para limpar nem janela de
  segundos, como haveria comparando datas de emissão.
- ✅ Também passa a barrar o usuário desativado, que antes não era conferido.
- ❌ Encerra **todas** as sessões da pessoa de uma vez, sem escolher uma. A tela "Sessões ativas", com a
  lista de dispositivos, continua "Em breve" e pediria guardar cada sessão.

**Consulta ao banco por requisição** × **cache**
- ✅ O efeito é imediato e o código é simples. É uma leitura por CPF, indexada.
- ❌ Há uma consulta a mais por requisição. Se pesar, um cache curto (segundos) resolve, ao custo de
  atrasar o encerramento por esse tempo.

## Consequências

**Positivas:**
- Desativar, redefinir a senha, trocar a senha e encerrar sessões valem na hora.
- Fecha um dos itens que bloqueiam ligar a autorização num ambiente.

**Pendências:**
- Recuperação de senha sem a administração (próxima entrega do pacote de segurança).
- Lista de sessões por dispositivo ("Sessões ativas"), MFA e política de senha seguem "Em breve".

## Testes

- **JUnit (`SessaoControllerTest`, login e autorização ligados):**
  - desativar derruba a sessão;
  - reativar não ressuscita o token antigo;
  - redefinir a senha derruba a sessão;
  - trocar a própria senha derruba as outras sessões e mantém a nova;
  - o encerramento pela administração funciona (200), dá 422 para o próprio usuário e 404 para usuário
    inexistente, e exige `USUARIO.GERENCIAR`;
  - um token sem versão continua valendo, e um token de usuário inexistente não.
- **Robot de API:** `POST_encerrar_sessoes_usuario.robot` (200, 404).
- **Robot de interface (tag `SEGURANCA`):** `UI_usuarios_encerrar_sessoes.robot`. O token da pessoa vale;
  depois do botão, a API responde 401.

## Referências

- [ADR-0055](./0055-primeira-implementacao-de-login-usuario-jwt-e-toggle-por-ambiente.md): login e JWT.
- [ADR-0069](./0069-troca-de-senha-e-senha-provisoria.md): troca de senha e senha provisória.
- [ADR-0068](./0068-tela-usuarios-e-perfis.md): tela Usuários & Perfis.
