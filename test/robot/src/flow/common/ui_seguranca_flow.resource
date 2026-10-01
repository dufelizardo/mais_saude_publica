*** Settings ***
Library    Collections
Resource    ../../pom/auth/login/login_pom.resource
Resource    ../../pom/auth/senha/senha_pom.resource
Resource    ../../pom/auth/eu/eu_pom.resource
Resource    ../acesso/acesso/acesso_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Apoio dos testes de interface com login, autorização e vínculo ligados (tag SEGURANCA, ADR-0077): entrar na API
    com token, criar usuário pela administração, conceder papel e trocar a senha provisória. O administrador inicial
    (bootstrap) vem de MSP_ADMIN_CPF/MSP_ADMIN_SENHA.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
${SEG_ADMIN_CPF}            %{MSP_ADMIN_CPF=}
${SEG_ADMIN_SENHA}          %{MSP_ADMIN_SENHA=}
${SEG_SENHA_PROVISORIA}     Robot#Provisoria1
${SEG_SENHA}                Robot#Definitiva1
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Pular Sem Administrador Configurado
    [Documentation]    Pula a suíte quando não há credenciais do administrador inicial.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Skip If    '${SEG_ADMIN_CPF}' == '${EMPTY}'
    ...    MSP_ADMIN_CPF/MSP_ADMIN_SENHA não definidas: a suíte precisa de uma API com login, autorização e vínculo ligados.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Entrar Na API Como
    [Documentation]    Faz login na API e passa a usar o token nas chamadas seguintes. Devolve o corpo do login.
    [Arguments]    ${cpf}    ${senha}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Definir Token Da Sessao De API    ${EMPTY}
    ${payload}=    Create Dictionary    tipo=CPF    identificador=${cpf}    senha=${senha}    manterConectado=${False}
    ${resposta}=    POST Login    ${payload}
    Should Be Equal As Integers    ${resposta.status_code}    200
    Definir Token Da Sessao De API    ${resposta.json()['token']}
    RETURN    ${resposta.json()}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Criar Usuario Pela Administracao
    [Documentation]    Cria um usuário com senha provisória (o token do administrador precisa estar ativo) e devolve
    ...    cpf e uuid.
    [Arguments]    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${cpf}=    CPF Valido Aleatorio
    ${payload}=    Create Dictionary    cpf=${cpf}    nome=${nome}    senha=${SEG_SENHA_PROVISORIA}
    ${criado}=    POST Create Usuario    ${payload}
    Should Be Equal As Integers    ${criado.status_code}    201
    ${lista}=    GET List Usuarios
    ${uuid}=    Evaluate    [u['uuid'] for u in $lista.json() if u['cpf'] == $cpf][0]
    RETURN    ${cpf}    ${uuid}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Conceder Papel
    [Documentation]    Concede um papel padrão a um usuário; sem unidade, na rede inteira.
    [Arguments]    ${usuarioId}    ${codigoPapel}    ${unidadeId}=${NONE}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${papelId}=    Buscar Papel Uuid Por Codigo    ${codigoPapel}
    ${payload}=    Create Dictionary    usuarioId=${usuarioId}    papelId=${papelId}
    IF    $unidadeId is not None
        Set To Dictionary    ${payload}    unidadeId=${unidadeId}
    END
    ${resposta}=    POST Create Atribuicao Acesso    ${payload}
    Should Be Equal As Integers    ${resposta.status_code}    201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Trocar Senha Provisoria
    [Documentation]    Entra com a senha provisória e define a senha definitiva (ADR-0069). Deixa o token do usuário ativo.
    [Arguments]    ${cpf}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Entrar Na API Como    ${cpf}    ${SEG_SENHA_PROVISORIA}
    ${payload}=    Create Dictionary    senhaAtual=${SEG_SENHA_PROVISORIA}    novaSenha=${SEG_SENHA}
    ...    manterConectado=${False}
    ${resposta}=    POST Auth Senha    ${payload}
    Should Be Equal As Integers    ${resposta.status_code}    200
    Definir Token Da Sessao De API    ${resposta.json()['token']}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Entrar Na API Com Token
    [Documentation]    Passa a chamar a API com um token já obtido (ex.: o de outro usuário).
    [Arguments]    ${token}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Definir Token Da Sessao De API    ${token}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Status De Quem Esta Logado
    [Documentation]    Chama GET /auth/eu com o token atual e devolve o status (200 com sessão válida, 401 sem).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${resposta}=    GET Auth Eu
    RETURN    ${resposta.status_code}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
