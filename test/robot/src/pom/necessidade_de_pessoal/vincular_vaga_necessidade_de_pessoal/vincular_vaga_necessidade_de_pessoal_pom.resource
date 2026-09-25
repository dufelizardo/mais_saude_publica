*** Settings ***
Resource    ../../common/mais_saude_publica_common.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
${NECESSIDADE_DE_PESSOAL_ENDPOINT}    /necessidade-de-pessoal
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
PATCH Vincular Vaga Necessidade De Pessoal
    [Documentation]    Sends a PATCH request to vincular a Vaga to a Necessidade De Pessoal (vagaId as a query
    ...    param, no body) and returns the response.
    [Arguments]    ${uuid}    ${vagaId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session

    ${params}=    Create Dictionary
    ...    vagaId=${vagaId}

    ${response}=    PATCH On Session
    ...    msp_session
    ...    ${BASE_URL}${NECESSIDADE_DE_PESSOAL_ENDPOINT}/${uuid}/vincular-vaga
    ...    params=${params}
    ...    expected_status=ANY

    Log    ${response.status_code}
    Log    ${response.text}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
