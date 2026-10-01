*** Settings ***
Library    RequestsLibrary
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
${HOST_URL}    %{MSP_HOST_URL=http://localhost:8080}
${BASE_URL}    /api/v1
# Token da sessão de API (Bearer). Vazio = sem login, como no CI e no local. Os testes com segurança ligada
# (tag SEGURANCA) o preenchem com Definir Token Da Sessao De API depois de entrar.
${MSP_API_TOKEN_ATUAL}    ${EMPTY}
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Get Mais Saude Publica Session
    [Documentation]    Creates (or reuses) the shared RequestsLibrary session for the Mais Saúde Pública API.
    ...    Override the host via the MSP_HOST_URL environment variable (e.g. when 8080 is taken locally).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${MSP_API_TOKEN_ATUAL}' != '${EMPTY}'
        ${autorizacao}=    Create Dictionary    Authorization=Bearer ${MSP_API_TOKEN_ATUAL}
        Create Session    msp_session    ${HOST_URL}    verify=False    headers=${autorizacao}
    ELSE
        Create Session    msp_session    ${HOST_URL}    verify=False
    END
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Definir Token Da Sessao De API
    [Documentation]    Passa a mandar este token (Bearer) em toda chamada de API da suíte. Vazio volta a chamar sem login.
    [Arguments]    ${token}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Set Global Variable    ${MSP_API_TOKEN_ATUAL}    ${token}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
