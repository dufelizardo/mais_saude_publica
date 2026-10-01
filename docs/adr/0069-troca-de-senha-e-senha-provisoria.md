# 0069 — Troca de senha e senha provisória

## Status

Aceita e implementada. Resolve a principal pendência da
[ADR-0068](./0068-tela-usuarios-e-perfis.md): quem cadastra um usuário conhece a senha inicial dele.

## Contexto

Até aqui, nenhuma senha podia ser trocada depois de criada:
- **O cadastro de usuário (ADR-0068)** entrega uma senha inicial definida pela administração. Quem
  cadastrou continua sabendo a senha.
- **O administrador inicial (ADR-0055)** nasce do `Secret` só na primeira subida. Trocar o `Secret`
  depois não troca a senha de quem já existe. No `dev`, essa senha chegou a circular fora do sistema.
- **"Esqueci minha senha"** está "Em breve" na tela de login. Não há serviço de e-mail para recuperação.

## Decisão

1. **Senha provisória.** `Usuario.trocarSenha` marca uma senha que a pessoa ainda não escolheu. Ela é
   provisória em dois casos:
   - o **cadastro** feito pela administração;
   - a **redefinição** feita pela administração (decisão 4).

   Linhas que já existiam ficam com `false`, pelo default da coluna.
2. **Sessão restrita até a troca.** No login, o token de quem tem senha provisória leva o claim
   `trocarSenha`.
   - Enquanto esse claim existir, a API só atende `/api/v1/auth/**`. Todo o resto responde **403**:
     "Troque a senha provisória antes de continuar".
   - Vale com ou sem a exigência de permissão ligada (ADR-0067). Basta o login estar ligado.
   - Por ficar no token, a regra não custa uma consulta ao banco por requisição.
   - O frontend leva direto da tela de login para a troca, e o guard das telas internas faz o mesmo.
     Não há como pular.
3. **`POST /api/v1/auth/senha`** (quem está logado troca a própria senha):
   - confere a senha atual (**422** se não confere);
   - a nova precisa ter de 8 a 72 caracteres, ser diferente da atual e não ser o CPF (**400**);
   - grava `senhaAlteradaEm` e devolve um **token novo**, sem o claim, com a mesma escolha de "manter
     conectado";
   - sem login, **401**.

   Também é o caminho para trocar a senha a qualquer momento, pelo botão da chave na barra superior.
4. **`POST /api/v1/usuario/{id}/redefinicao-senha`** (`USUARIO.GERENCIAR`): a administração define uma
   senha provisória.
   - Também libera o bloqueio por tentativas.
   - É o "esqueci minha senha" enquanto não há recuperação por e-mail.
   - A própria senha não se redefine por aqui (**422**). Ela se troca pela decisão 3.
5. **Telas:**
   - **Trocar senha** (`/trocar-senha`): o formulário do Login em coluna única, com os requisitos
     marcados enquanto a pessoa digita. Com senha provisória, oferece só "Sair". Na troca voluntária,
     oferece "Voltar".
   - **Usuários & Perfis:**
     - a gaveta do usuário mostra a situação da senha (provisória, trocada em tal data ou definida no
       cadastro);
     - ganha a ação **Definir senha provisória**;
     - o cadastro avisa que a senha inicial é provisória.

## Trade-offs considerados

**Claim no token (escolhida)** × **consultar o banco a cada requisição**
- ✅ Sem custo por requisição, e a regra vive no mesmo lugar da autenticação.
- ❌ Um token antigo, emitido antes de uma redefinição, continua sem o claim até expirar (8 h, ou 7 dias
  com "manter conectado"). Aceitável: a redefinição atende quem *esqueceu* a senha, não quem teve a
  sessão roubada. Invalidar sessões ativas fica para a aba "Sessões ativas" (ADR-0068), ainda "Em
  breve". *Resolvido pela [ADR-0078](./0078-sessoes-encerradas-na-hora.md): redefinir, trocar a senha e
  desativar encerram as sessões na hora.*

**Redefinição pela administração (escolhida)** × **recuperação por e-mail**
- ✅ Funciona hoje, sem serviço de e-mail, e a senha que a administração conhece é provisória.
- ❌ A pessoa depende da administração da unidade para voltar a entrar. A recuperação por e-mail ou
  gov.br continua pendente (ADR-0055).

**Regras de senha mínimas (escolhida)** × **política configurável** (maiúscula, símbolo, histórico,
validade)
- ✅ Comprimento e "não é o CPF" cobrem o pior caso sem atrapalhar o uso. A política configurável é a
  aba "Política de acesso" da ADR-0068, ainda "Em breve".

## Consequências

**Positivas:**
- Quem cadastra deixa de conhecer a senha de uso, e o "esqueci minha senha" ganha um caminho.
- A senha exposta do `dev` pode ser trocada pelo próprio administrador na barra superior. O guia
  [`GUIA-LIGAR-AUTORIZACAO.md`](../acesso/GUIA-LIGAR-AUTORIZACAO.md) foi simplificado com isso.

**Negativas / pendências:**
- ~~Invalidar sessões ativas ao redefinir ou desativar.~~ Feito pela [ADR-0078](./0078-sessoes-encerradas-na-hora.md).
- Recuperação de senha sem a administração (e-mail ou gov.br).
- Política de senha configurável.

## Referências

- [ADR-0055](./0055-primeira-implementacao-de-login-usuario-jwt-e-toggle-por-ambiente.md): login, JWT, bloqueio por tentativas.
- [ADR-0068](./0068-tela-usuarios-e-perfis.md): cadastro de usuário e tela Usuários & Perfis.
- [ADR-0067](./0067-autorizacao-aplicada-nas-rotas-e-no-escopo.md): interceptor onde a restrição da senha provisória é aplicada.
