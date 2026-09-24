*** Settings ***
Resource    ../../common/mais_saude_publica_common.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
${RESPONSABILIDADE_ADMINISTRATIVA_ENDPOINT}    /responsabilidade-administrativa
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
PATCH Encerrar Responsabilidade Administrativa
    [Documentation]    Sends a PATCH request to encerrar a Responsabilidade Administrativa (dataFim as a query
    ...    param, no body) and returns the response.
    [Arguments]    ${uuid}    ${dataFim}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session

    ${params}=    Create Dictionary
    ...    dataFim=${dataFim}

    ${response}=    PATCH On Session
    ...    msp_session
    ...    ${BASE_URL}${RESPONSABILIDADE_ADMINISTRATIVA_ENDPOINT}/${uuid}/encerrar
    ...    params=${params}
    ...    expected_status=ANY

    Log    ${response.status_code}
    Log    ${response.text}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
