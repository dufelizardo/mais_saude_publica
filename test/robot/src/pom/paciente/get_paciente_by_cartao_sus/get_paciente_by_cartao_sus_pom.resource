*** Settings ***
Resource    ../../common/mais_saude_publica_common.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
${PACIENTE_ENDPOINT}    /paciente
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
GET Get Paciente By Cartao Sus
    [Documentation]    Sends a GET request to fetch every Paciente matching a Cartão Nacional de Saúde (CNS)
    ...    and returns the response.
    [Arguments]    ${cartaoSus}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session

    ${response}=    GET On Session
    ...    msp_session
    ...    ${BASE_URL}${PACIENTE_ENDPOINT}/cartao-sus/${cartaoSus}
    ...    expected_status=ANY

    Log    ${response.status_code}
    Log    ${response.text}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
