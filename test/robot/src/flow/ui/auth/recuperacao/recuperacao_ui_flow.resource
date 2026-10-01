*** Settings ***
Library    String
Resource    ../../../../pom/ui/auth/recuperacao/recuperacao_ui_pom.resource
Resource    ../../../../pom/common/mailpit_pom.resource
Resource    ../../../common/ui_seguranca_flow.resource
Resource    ../../../profissional/create_profissional/create_profissional_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Sem SMTP (CI fase 1 e local), as telas avisam que a recuperação está indisponível. Com login, autorização e SMTP de teste (Mailpit, fase 2 do robot-ui), o fluxo completo: pedido, e-mail, link, senha nova e login.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Recuperacao - Indisponivel Sem Smtp
    [Documentation]    Sem SMTP: o login não oferece o link, a página avisa e o link sem token pede outro.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Ir Para Rota Do Frontend    /login
    Wait Until Keyword Succeeds    5s    250ms    Links Esqueci Devem Ser    0
    Ir Para Rota Do Frontend    /recuperar-senha
    Esperar Texto Aparecer    A recuperação de senha por e-mail não está disponível neste ambiente.
    Ir Para Rota Do Frontend    /redefinir-senha
    Esperar Texto Aparecer    Este link está incompleto.
    ${links}=    Get Element Count    role=link[name="Pedir outro link"]
    Should Be Equal As Integers    ${links}    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Links Esqueci Devem Ser
    [Documentation]    Confere quantos links "Esqueci minha senha" o login mostra.
    [Arguments]    ${quantidade}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${n}=    UI Recuperacao - Contar Links Esqueci
    Should Be Equal As Integers    ${n}    ${quantidade}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Preparar Pessoa Com Email
    [Documentation]    Cria a pessoa (usuário) e, pelo apoio (Gestor), o profissional de mesmo CPF com um e-mail único.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Pular Sem Administrador Configurado
    Skip If    '${MAILPIT_URL}' == '${EMPTY}'    MSP_MAILPIT_URL não definida: sem caixa de e-mail de teste.
    Entrar Na API Como    ${SEG_ADMIN_CPF}    ${SEG_ADMIN_SENHA}
    ${cpf}    ${id}=    Criar Usuario Pela Administracao    Robot Pessoa Recuperacao
    ${cpfApoio}    ${idApoio}=    Criar Usuario Pela Administracao    Robot Apoio Recuperacao
    Conceder Papel    ${idApoio}    GESTOR
    Trocar Senha Provisoria    ${cpfApoio}
    ${sufixo}=    Generate Random String    8    [LOWER][NUMBERS]
    ${email}=    Set Variable    robot.${sufixo}@teste.local
    ${payload}=    Build Profissional Payload    ${cpf}    Robot Pessoa Recuperacao
    Set To Dictionary    ${payload}    email=${email}
    ${criado}=    POST Create Profissional    ${payload}
    Should Be Equal As Integers    ${criado.status_code}    201
    Definir Token Da Sessao De API    ${EMPTY}
    Set Suite Variable    ${RECUP_CPF}    ${cpf}
    Set Suite Variable    ${RECUP_EMAIL}    ${email}
    Abrir Navegador Do Mais Saude Publica
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Token Do Email Recebido
    [Documentation]    Espera o e-mail chegar no Mailpit e devolve o token do link.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${id}=    Wait Until Keyword Succeeds    20s    1s    Id Da Mensagem Para    ${RECUP_EMAIL}
    ${mensagem}=    GET Mensagem    ${id}
    Should Be Equal As Integers    ${mensagem.status_code}    200
    ${texto}=    Set Variable    ${mensagem.json()}[Text]
    ${token}=    Evaluate    __import__('re').search(r'redefinir-senha[?]token=([A-Za-z0-9_-]+)', $texto).group(1)
    RETURN    ${token}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Id Da Mensagem Para
    [Documentation]    ID da mensagem mais recente para o endereço (falha se ainda não chegou).
    [Arguments]    ${email}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${resposta}=    GET Mensagens Para    ${email}
    Should Be Equal As Integers    ${resposta.status_code}    200
    ${mensagens}=    Set Variable    ${resposta.json()}[messages]
    Should Not Be Empty    ${mensagens}
    RETURN    ${mensagens}[0][ID]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Recuperacao - Fluxo Completo Pelo Email
    [Documentation]    Pede o link pela tela, lê o e-mail, cria a senha nova e entra com ela.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Ir Para Rota Do Frontend    /login
    Wait Until Keyword Succeeds    5s    250ms    Links Esqueci Devem Ser    1
    UI Recuperacao - Clicar Esqueci Minha Senha
    UI Recuperacao - Pedir Link    ${RECUP_CPF}
    Esperar Texto Aparecer    Se o CPF tiver cadastro com e-mail
    ${token}=    Token Do Email Recebido
    Set Suite Variable    ${RECUP_TOKEN}    ${token}
    Ir Para Rota Do Frontend    /redefinir-senha?token=${token}
    UI Recuperacao - Criar Senha    Recuperada#123
    Esperar Texto Aparecer    Senha criada.
    Entrar No Sistema Pela Tela De Login    ${RECUP_CPF}    Recuperada#123
    ${url}=    Ler Url Atual
    Should Not Contain    ${url}    /login
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Recuperacao - Link Usado Nao Vale
    [Documentation]    Abrir o mesmo link de novo e tentar: a tela diz que não vale e oferece pedir outro.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Fechar Navegador Do Mais Saude Publica
    Abrir Navegador Do Mais Saude Publica
    Ir Para Rota Do Frontend    /redefinir-senha?token=${RECUP_TOKEN}
    UI Recuperacao - Criar Senha    OutraSenha#456
    Esperar Texto Aparecer    Este link não vale mais
    ${links}=    Get Element Count    role=link[name="Pedir outro link"]
    Should Be Equal As Integers    ${links}    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
