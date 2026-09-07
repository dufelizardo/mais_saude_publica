*** Settings ***
Resource    ../../common/mais_saude_publica_common.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    NOTE: the des-habilitar endpoint declares consumes=application/json but does NOT read the DTO from the
    request body (@RequestBody is absent on the controller parameter) — Spring binds "ativo" as a query
    param instead, and still 415s without a Content-Type header if none is sent. Confirmed against the
    running application before writing this keyword.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
${FEDERAL_ENDPOINT}        /federal
${CONTENT_TYPE}    application/json
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
DELETE Desabilitar Federal
    [Documentation]    Sends a DELETE request to enable/disable a Federal unit (ativo as a query param) and
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
    ...    ${BASE_URL}${FEDERAL_ENDPOINT}/des-habilitar/${nome}
    ...    headers=${headers}
    ...    params=${params}
    ...    expected_status=ANY

    Log    ${response.status_code}
    Log    ${response.text}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
