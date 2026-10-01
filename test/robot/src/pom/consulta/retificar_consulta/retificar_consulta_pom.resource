*** Settings ***
Resource    ../../common/mais_saude_publica_common.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
${CONSULTA_ENDPOINT}    /consulta
${CONTENT_TYPE}    application/json
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
POST Retificar Consulta
    [Documentation]    Sends a POST request creating a new version of a Consulta that corrects the given one (ADR-0062)
    ...    and returns the response.
    [Arguments]    ${uuid}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session

    ${headers}=    Create Dictionary
    ...    Content-Type=${CONTENT_TYPE}

    ${response}=    POST On Session
    ...    msp_session
    ...    ${BASE_URL}${CONSULTA_ENDPOINT}/${uuid}/retificacao
    ...    json=${payload}
    ...    headers=${headers}
    ...    expected_status=ANY

    Log    ${response.status_code}
    Log    ${response.text}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

