*** Settings ***
Resource    ../common/mais_saude_publica_common.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Saúde Mental, fatia SM5 (ADR-0153): a RAPS e o acolhimento noturno do CAPS.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
GET Raps
    [Documentation]    The psychosocial care network.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    GET On Session    msp_session    ${BASE_URL}/saude-mental/raps    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Acolhimento Noturno
    [Documentation]    The unit's night shelter.
    [Arguments]    ${unidade}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${params}=    Create Dictionary    unidadeId=${unidade}
    Get Mais Saude Publica Session
    ${response}=    GET On Session    msp_session    ${BASE_URL}/saude-mental/acolhimento-noturno    params=${params}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PUT Configuracao Acolhimento
    [Documentation]    Configures the beds and the maximum stay.
    [Arguments]    ${unidade}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${params}=    Create Dictionary    unidadeId=${unidade}
    Get Mais Saude Publica Session
    ${response}=    PUT On Session    msp_session    ${BASE_URL}/saude-mental/acolhimento-noturno/configuracao    json=${payload}    params=${params}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Entrada Acolhimento
    [Documentation]    Registers the entry into the night shelter.
    [Arguments]    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    POST On Session    msp_session    ${BASE_URL}/saude-mental/acolhimento-noturno    json=${payload}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Saida Acolhimento
    [Documentation]    Registers the exit from the night shelter.
    [Arguments]    ${permanencia}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    POST On Session    msp_session    ${BASE_URL}/saude-mental/acolhimento-noturno/${permanencia}/saida    json=${payload}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
