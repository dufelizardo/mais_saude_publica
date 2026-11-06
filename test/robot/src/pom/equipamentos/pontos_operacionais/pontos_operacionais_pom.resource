*** Settings ***
Resource    ../../common/mais_saude_publica_common.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Pontos operacionais (ADR-0166): pontos da unidade, rotas e localização da unidade móvel, e locais de realização.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
${PONTO_UNIDADE_ENDPOINT}    /unidade-saude/id
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
GET Pontos Da Unidade
    [Documentation]    Lists the operational points of a unit.
    [Arguments]    ${uuid}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    GET On Session    msp_session    ${BASE_URL}${PONTO_UNIDADE_ENDPOINT}/${uuid}/pontos    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Locais De Realizacao
    [Documentation]    Lists the active points of the unit and the patient's home.
    [Arguments]    ${uuid}    ${paciente}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${params}=    Create Dictionary    pacienteId=${paciente}
    ${response}=    GET On Session    msp_session    ${BASE_URL}${PONTO_UNIDADE_ENDPOINT}/${uuid}/locais-de-realizacao    params=${params}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Incluir Ponto
    [Documentation]    Adds an operational point to the unit.
    [Arguments]    ${uuid}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    POST On Session    msp_session    ${BASE_URL}${PONTO_UNIDADE_ENDPOINT}/${uuid}/ponto    json=${payload}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Ponto
    [Documentation]    Adjusts an operational point.
    [Arguments]    ${uuid}    ${ponto}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    PATCH On Session    msp_session    ${BASE_URL}${PONTO_UNIDADE_ENDPOINT}/${uuid}/ponto/${ponto}    json=${payload}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Encerrar Ponto
    [Documentation]    Ends an operational point.
    [Arguments]    ${uuid}    ${ponto}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    POST On Session    msp_session    ${BASE_URL}${PONTO_UNIDADE_ENDPOINT}/${uuid}/ponto/${ponto}/encerramento    json=${payload}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Incluir Rota
    [Documentation]    Adds a scheduled route to the mobile unit.
    [Arguments]    ${uuid}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    POST On Session    msp_session    ${BASE_URL}${PONTO_UNIDADE_ENDPOINT}/${uuid}/rota    json=${payload}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Encerrar Rota
    [Documentation]    Ends a scheduled route.
    [Arguments]    ${uuid}    ${rota}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    POST On Session    msp_session    ${BASE_URL}${PONTO_UNIDADE_ENDPOINT}/${uuid}/rota/${rota}/encerramento    json=${payload}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Registrar Posicao
    [Documentation]    Registers where the mobile unit is.
    [Arguments]    ${uuid}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    POST On Session    msp_session    ${BASE_URL}${PONTO_UNIDADE_ENDPOINT}/${uuid}/posicao    json=${payload}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

