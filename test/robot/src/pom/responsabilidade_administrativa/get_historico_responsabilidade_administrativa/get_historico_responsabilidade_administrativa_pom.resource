*** Settings ***
Resource    ../../common/mais_saude_publica_common.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
${RESPONSABILIDADE_ADMINISTRATIVA_ENDPOINT}    /responsabilidade-administrativa
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
GET Get Historico Responsabilidade Administrativa
    [Documentation]    Sends a GET request to fetch the Responsabilidade Administrativa history of a
    ...    Profissional by matrícula and returns the response.
    [Arguments]    ${matricula}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session

    ${response}=    GET On Session
    ...    msp_session
    ...    ${BASE_URL}${RESPONSABILIDADE_ADMINISTRATIVA_ENDPOINT}/profissional/${matricula}
    ...    expected_status=ANY

    Log    ${response.status_code}
    Log    ${response.text}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
