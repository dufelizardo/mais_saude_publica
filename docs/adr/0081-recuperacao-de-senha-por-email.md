# 0081 — Recuperação de senha por e-mail

## Status

Aceita e implementada. Fica **desligada até o ambiente ter SMTP configurado**. Resolve a pendência
"recuperação de senha sem a administração" da [ADR-0069](./0069-troca-de-senha-e-senha-provisoria.md).

## Contexto

Quem esquece a senha hoje depende da administração: alguém com `USUARIO.GERENCIAR` define uma senha
provisória e a pessoa cria a sua no primeiro acesso (ADR-0069). Num sistema usado em plantão, à noite e no
fim de semana, isso deixa o profissional sem acesso até alguém da administração estar disponível.

O usuário (`Usuario`) não tem e-mail. Quem tem é o **profissional** (`Profissional.email`), mantido pelo
RH, que se liga ao usuário pelo CPF (ADR-0065).

## Decisão

1. **"Esqueci minha senha" no login** leva à tela de pedido. A pessoa informa o CPF e recebe por e-mail um
   link para "Criar senha nova".
2. **O link vai para o e-mail do profissional ativo de mesmo CPF.** Quem não tem profissional ou e-mail
   cadastrado continua com a senha provisória pela administração. A tela diz isso.
3. **Segurança do link:**
   - token aleatório de 256 bits, guardado só como hash SHA-256 (`TB_REDEFINICAO_SENHA`);
   - vale **30 minutos** e **uma vez**; pedir de novo invalida os links anteriores;
   - no máximo **3 pedidos por hora** por pessoa.
4. **A resposta do pedido é sempre a mesma,** tenha ou não cadastro: "Se o CPF tiver cadastro com e-mail,
   enviamos um link…". A tela não revela quem é usuário. **O que de fato aconteceu vai para a trilha de
   auditoria,** com a ação nova `RECUPERACAO_DE_SENHA`. O detalhe registra um destes resultados: link
   enviado para `a***@dominio`, sem e-mail, limite atingido ou sem usuário. A trilha nunca guarda o token
   nem o endereço inteiro.
5. **Criar a senha pelo link:**
   - aplica as mesmas regras da troca de senha (8 a 72 caracteres, não pode ser o CPF);
   - libera o bloqueio por tentativas;
   - **encerra todas as sessões abertas** (ADR-0078).

   A pessoa entra em seguida pela tela de login. Link usado, vencido ou substituído responde 422 e a tela
   oferece "Pedir outro link".
6. **Só fica disponível com:**
   - o login ligado;
   - o SMTP configurado (propriedades padrão do Spring: `spring.mail.*`);
   - o endereço do frontend (`app.security.recuperacao-senha.url-frontend`), usado para montar o link.

   O `GET /auth/status` informa `recuperacaoDeSenha`. Sem isso, o login mostra o "Esqueci minha senha"
   desativado e a API responde 422.
7. **Rotas públicas novas:** `POST /auth/senha/recuperacao` e `POST /auth/senha/redefinicao`. Telas
   públicas: `/recuperar-senha` e `/redefinir-senha?token=`.

## Trade-offs considerados

**E-mail do profissional (escolhida)** × **campo de e-mail no usuário**
- ✅ O dado já existe e é mantido pelo RH. Não há um segundo e-mail para cadastrar e manter igual.
- ❌ Usuário sem profissional (por exemplo, o administrador inicial) não recupera sozinho e continua pela
  administração.

**E-mail (escolhida)** × **gov.br** × **perguntas de segurança**
- ✅ É o mais simples de operar, e qualquer provedor SMTP serve.
- O gov.br fica como evolução, quando houver integração. Perguntas de segurança foram descartadas por
  serem fáceis de adivinhar.

## Consequências

- A pessoa recupera o acesso sozinha, a qualquer hora, quando o ambiente tiver SMTP.
- **Para ligar num ambiente:** um provedor SMTP com usuário e senha, guardados num Secret. O guia de
  ligar a autorização tem a seção.

## Testes

- **JUnit (`RecuperacaoSenhaControllerTest`, com o e-mail simulado):**
  - o fluxo completo: o link troca a senha, só uma vez, e derruba a sessão aberta antes;
  - a mesma resposta com ou sem cadastro, e envio só quando há e-mail;
  - um pedido novo invalida o anterior, com limite por hora;
  - link vencido e inexistente;
  - senha igual ao CPF;
  - trilha sem o token;
  - indisponível sem SMTP.
- **Robot de API:** 400 de validação e 422 sem SMTP.
- **Robot de interface:**
  - sem SMTP, o login não oferece o link e as telas avisam;
  - **com login ligado e SMTP de teste (Mailpit)**, o fluxo completo: pedido pela tela, e-mail recebido,
    link, senha nova e entrada com ela. O link já usado é recusado. O Mailpit roda como serviço no job
    `robot-ui`.
