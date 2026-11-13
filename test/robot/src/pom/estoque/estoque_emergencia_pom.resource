*** Settings ***
Resource    ../common/mais_saude_publica_common.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Estoque nas emergências (ADR-0191): a reserva para a emergência e as emergências abertas.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
POST Estoque Emergencia
    [Documentation]    Posts to /estoque/{rota}: reserva and reserva/{id}/liberacao.
    [Arguments]    ${rota}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${response}=    Post On Session    msp_session    ${BASE_URL}/estoque/${rota}    json=${payload}    headers=${headers}    expected_status=ANY
    Log    ${response.status_code}
    Log    ${response.text}
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Estoque Emergencia
    [Documentation]    Gets /estoque/{rota}: reserva, emergencias, saldo.
    [Arguments]    ${rota}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    GET On Session    msp_session    ${BASE_URL}/estoque/${rota}    expected_status=ANY
    Log    ${response.status_code}
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
