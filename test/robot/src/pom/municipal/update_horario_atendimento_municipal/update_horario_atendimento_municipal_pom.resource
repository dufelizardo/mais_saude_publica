*** Settings ***
Resource    ../../common/mais_saude_publica_common.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
${MUNICIPAL_ENDPOINT}    /municipal
${CONTENT_TYPE}          application/json
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
PATCH Update Horario De Atendimento Municipal
    [Documentation]    Sends a PATCH request to update a Municipal unit's horario de atendimento and returns the
    ...    response.
    [Arguments]    ${nome}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session

    ${headers}=    Create Dictionary
    ...    Content-Type=${CONTENT_TYPE}

    ${response}=    PATCH On Session
    ...    msp_session
    ...    ${BASE_URL}${MUNICIPAL_ENDPOINT}/horario-de-atendimento/${nome}
    ...    json=${payload}
    ...    headers=${headers}
    ...    expected_status=ANY

    Log    ${response.status_code}
    Log    ${response.text}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Horario De Atendimento Municipal - Raw Body
    [Documentation]    Sends a PATCH request with a raw (possibly malformed) string body, bypassing JSON
    ...    serialization — used to exercise HttpMessageNotReadableException (AQUAQE-215).
    [Arguments]    ${nome}    ${rawBody}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session

    ${headers}=    Create Dictionary
    ...    Content-Type=${CONTENT_TYPE}

    ${response}=    PATCH On Session
    ...    msp_session
    ...    ${BASE_URL}${MUNICIPAL_ENDPOINT}/horario-de-atendimento/${nome}
    ...    data=${rawBody}
    ...    headers=${headers}
    ...    expected_status=ANY

    Log    ${response.status_code}
    Log    ${response.text}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
