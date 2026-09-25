*** Settings ***
Resource    ../../common/mais_saude_publica_common.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
${SETOR_ENDPOINT}    /setor
${CONTENT_TYPE}    application/json
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
POST Create Setor
    [Documentation]    Sends a POST request to create a Setor linked to a Unidade de Saúde and returns the
    ...    response.
    [Arguments]    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session

    ${headers}=    Create Dictionary
    ...    Content-Type=${CONTENT_TYPE}

    ${response}=    POST On Session
    ...    msp_session
    ...    ${BASE_URL}${SETOR_ENDPOINT}/
    ...    json=${payload}
    ...    headers=${headers}
    ...    expected_status=ANY

    Log    ${response.status_code}
    Log    ${response.text}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
