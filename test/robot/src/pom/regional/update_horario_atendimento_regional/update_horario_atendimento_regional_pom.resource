*** Settings ***
Resource    ../../common/mais_saude_publica_common.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
${REGIONAL_ENDPOINT}        /regional
${CONTENT_TYPE}    application/json
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
PATCH Update Horario De Atendimento Regional
    [Documentation]    Sends a PATCH request to update a Regional unit's horario de atendimento and returns the
    ...    response.
    [Arguments]    ${nome}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session

    ${headers}=    Create Dictionary
    ...    Content-Type=${CONTENT_TYPE}

    ${response}=    PATCH On Session
    ...    msp_session
    ...    ${BASE_URL}${REGIONAL_ENDPOINT}/horario-de-atendimento/${nome}
    ...    json=${payload}
    ...    headers=${headers}
    ...    expected_status=ANY

    Log    ${response.status_code}
    Log    ${response.text}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Horario De Atendimento Regional - Raw Body
    [Documentation]    Sends a PATCH request with a raw (possibly malformed) string body, bypassing JSON
    ...    serialization — used to exercise HttpMessageNotReadableException (AQUAQE-215).
    [Arguments]    ${nome}    ${rawBody}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session

    ${headers}=    Create Dictionary
    ...    Content-Type=${CONTENT_TYPE}

    ${response}=    PATCH On Session
    ...    msp_session
    ...    ${BASE_URL}${REGIONAL_ENDPOINT}/horario-de-atendimento/${nome}
    ...    data=${rawBody}
    ...    headers=${headers}
    ...    expected_status=ANY

    Log    ${response.status_code}
    Log    ${response.text}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
