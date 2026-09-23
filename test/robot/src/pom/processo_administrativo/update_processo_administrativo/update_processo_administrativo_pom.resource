*** Settings ***
Resource    ../../common/mais_saude_publica_common.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
${PROCESSO_ADMINISTRATIVO_ENDPOINT}    /processo-administrativo
${CONTENT_TYPE}    application/json
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
PATCH Update Processo Administrativo
    [Documentation]    Sends a PATCH request to replace a Processo Administrativo's editable fields
    ...    (capacidadeId/codigo/nome/descricao/ativo) and returns the response.
    [Arguments]    ${uuid}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session

    ${headers}=    Create Dictionary
    ...    Content-Type=${CONTENT_TYPE}

    ${response}=    PATCH On Session
    ...    msp_session
    ...    ${BASE_URL}${PROCESSO_ADMINISTRATIVO_ENDPOINT}/${uuid}
    ...    json=${payload}
    ...    headers=${headers}
    ...    expected_status=ANY

    Log    ${response.status_code}
    Log    ${response.text}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
