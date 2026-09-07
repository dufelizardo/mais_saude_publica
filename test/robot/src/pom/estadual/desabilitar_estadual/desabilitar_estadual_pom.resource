*** Settings ***
Resource    ../../common/mais_saude_publica_common.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Same quirk as the Federal des-habilitar endpoint: consumes=application/json but no @RequestBody — Spring
    binds "ativo" as a query param, and still 415s without a Content-Type header if none is sent.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
${ESTADUAL_ENDPOINT}    /estadual
${CONTENT_TYPE}         application/json
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
DELETE Desabilitar Estadual
    [Documentation]    Sends a DELETE request to enable/disable an Estadual unit (ativo as a query param) and
    ...    returns the response.
    [Arguments]    ${nome}    ${ativo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session

    ${headers}=    Create Dictionary
    ...    Content-Type=${CONTENT_TYPE}

    ${params}=    Create Dictionary
    ...    ativo=${ativo}

    ${response}=    DELETE On Session
    ...    msp_session
    ...    ${BASE_URL}${ESTADUAL_ENDPOINT}/des-habilitar/${nome}
    ...    headers=${headers}
    ...    params=${params}
    ...    expected_status=ANY

    Log    ${response.status_code}
    Log    ${response.text}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
