*** Settings ***
Resource    ../../common/mais_saude_publica_common.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Internação eletiva agendada e censo diário (ADR-0185).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
POST Agendar Eletiva
    [Documentation]    Schedules an elective admission.
    [Arguments]    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${response}=    Post On Session    msp_session    ${BASE_URL}/internacao-eletiva/    json=${payload}    headers=${headers}    expected_status=ANY
    Log    ${response.status_code}
    Log    ${response.text}
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Acao Eletiva
    [Documentation]    Runs an action on an elective admission: reserva, admissao, falta, remarcacao or cancelamento.
    [Arguments]    ${uuid}    ${acao}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${response}=    Post On Session    msp_session    ${BASE_URL}/internacao-eletiva/${uuid}/${acao}    json=${payload}    headers=${headers}    expected_status=ANY
    Log    ${response.status_code}
    Log    ${response.text}
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Eletiva
    [Documentation]    One elective admission, with its events.
    [Arguments]    ${uuid}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    GET On Session    msp_session    ${BASE_URL}/internacao-eletiva/${uuid}    expected_status=ANY
    Log    ${response.status_code}
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Agenda Eletivas
    [Documentation]    The elective admissions of a unit in a period.
    [Arguments]    ${params}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    GET On Session    msp_session    ${BASE_URL}/internacao-eletiva/    params=${params}    expected_status=ANY
    Log    ${response.status_code}
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Censo Do Dia
    [Documentation]    The census of a day: the closed one, or a preview when not closed.
    [Arguments]    ${unidadeId}    ${data}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${params}=    Create Dictionary    unidadeId=${unidadeId}    data=${data}
    ${response}=    GET On Session    msp_session    ${BASE_URL}/censo-diario/    params=${params}    expected_status=ANY
    Log    ${response.status_code}
    Log    ${response.text}
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Fechar Censo
    [Documentation]    Closes the census of a day.
    [Arguments]    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${response}=    Post On Session    msp_session    ${BASE_URL}/censo-diario/fechamento    json=${payload}    headers=${headers}    expected_status=ANY
    Log    ${response.status_code}
    Log    ${response.text}
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Reabrir Censo
    [Documentation]    Reopens a closed census, with a reason.
    [Arguments]    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${response}=    Post On Session    msp_session    ${BASE_URL}/censo-diario/reabertura    json=${payload}    headers=${headers}    expected_status=ANY
    Log    ${response.status_code}
    Log    ${response.text}
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
