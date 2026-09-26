*** Settings ***
Resource    ../../common/mais_saude_publica_common.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
${EVOLUCAO_ENFERMAGEM_ENDPOINT}    /evolucao-enfermagem
${CONTENT_TYPE}    application/json
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
PATCH Update Evolucao Enfermagem
    [Documentation]    Sends a PATCH request to replace an EvolucaoEnfermagem's editable fields and
    ...    returns the response.
    [Arguments]    ${uuid}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session

    ${headers}=    Create Dictionary
    ...    Content-Type=${CONTENT_TYPE}

    ${response}=    PATCH On Session
    ...    msp_session
    ...    ${BASE_URL}${EVOLUCAO_ENFERMAGEM_ENDPOINT}/${uuid}
    ...    json=${payload}
    ...    headers=${headers}
    ...    expected_status=ANY

    Log    ${response.status_code}
    Log    ${response.text}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
