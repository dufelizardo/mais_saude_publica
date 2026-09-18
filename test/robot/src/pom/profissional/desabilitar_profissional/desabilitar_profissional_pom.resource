*** Settings ***
Library    Collections
Resource    ../../common/mais_saude_publica_common.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Same quirk as the des-habilitar endpoints on every hierarchy level: consumes=application/json but no
    @RequestBody — Spring binds "dataDesligamento" as a query param, and still 415s without a Content-Type
    header if none is sent.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
${PROFISSIONAL_ENDPOINT}        /profissional
${CONTENT_TYPE}    application/json
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
DELETE Desabilitar Profissional
    [Documentation]    Sends a DELETE request to enable/disable a Profissional. dataDesligamento (query param,
    ...    see ADR-0017) alone decides the status: informed → disables and records the date; absent → re-enables
    ...    and clears the date.
    [Arguments]    ${cpf}    ${dataDesligamento}=${EMPTY}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session

    ${headers}=    Create Dictionary
    ...    Content-Type=${CONTENT_TYPE}

    ${params}=    Create Dictionary
    IF    '${dataDesligamento}' != '${EMPTY}'
        Set To Dictionary    ${params}    dataDesligamento=${dataDesligamento}
    END

    ${response}=    DELETE On Session
    ...    msp_session
    ...    ${BASE_URL}${PROFISSIONAL_ENDPOINT}/des-habilitar/${cpf}
    ...    headers=${headers}
    ...    params=${params}
    ...    expected_status=ANY

    Log    ${response.status_code}
    Log    ${response.text}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
