*** Settings ***
Resource    ../../../../pom/ui/administracao/usuarios/usuarios_ui_pom.resource
Resource    ../../../common/ui_seguranca_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Encerrar sessões pela tela Usuários & Perfis (ADR-0078). Precisa da API com login e autorização ligados (tag
    SEGURANCA): o administrador inicial entra na tela, e a pessoa alvo tem uma sessão aberta pela API.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Preparar Cenario Do Encerramento De Sessoes
    [Documentation]    Cria uma pessoa com sessão aberta (token guardado) e abre o navegador logado como administrador.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Pular Sem Administrador Configurado
    Entrar Na API Como    ${SEG_ADMIN_CPF}    ${SEG_ADMIN_SENHA}
    ${sufixo}=    Evaluate    uuid.uuid4().hex[:6]    modules=uuid
    ${nome}=    Set Variable    Robot Sessao ${sufixo}
    ${cpf}    ${uuid}=    Criar Usuario Pela Administracao    ${nome}
    ${login}=    Entrar Na API Como    ${cpf}    ${SEG_SENHA_PROVISORIA}
    Definir Token Da Sessao De API    ${EMPTY}
    Set Suite Variable    ${SESSAO_NOME}    ${nome}
    Set Suite Variable    ${SESSAO_TOKEN}    ${login['token']}
    Abrir Navegador Do Mais Saude Publica
    Entrar No Sistema Pela Tela De Login    ${SEG_ADMIN_CPF}    ${SEG_ADMIN_SENHA}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Usuarios - Encerrar Sessoes Derruba O Token
    [Documentation]    Antes, o token da pessoa vale; o botão "Encerrar sessões" mostra o aviso e o token deixa de valer.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Entrar Na API Com Token    ${SESSAO_TOKEN}
    ${antes}=    Status De Quem Esta Logado
    Should Be Equal As Integers    ${antes}    200

    UI Usuarios - Abrir Tela
    UI Usuarios - Buscar    ${SESSAO_NOME}
    UI Usuarios - Abrir Usuario    ${SESSAO_NOME}
    Clicar Botao    Encerrar sessões
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Sessões encerradas

    ${depois}=    Status De Quem Esta Logado
    Should Be Equal As Integers    ${depois}    401
    Definir Token Da Sessao De API    ${EMPTY}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
