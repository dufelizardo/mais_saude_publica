*** Settings ***
Resource    ../common/mais_saude_publica_common.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Saúde Mental (ADR-0120): acolhimento, caso ligado ao programa, referência, avaliação e encerramento.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
POST Acolhimento Saude Mental
    [Documentation]    Registers a mental health reception (acolhimento).
    [Arguments]    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    POST On Session    msp_session    ${BASE_URL}/saude-mental/acolhimento    json=${payload}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Abrir Caso Saude Mental
    [Documentation]    Opens a mental health case.
    [Arguments]    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    POST On Session    msp_session    ${BASE_URL}/saude-mental/acompanhamento    json=${payload}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Caso Saude Mental
    [Documentation]    Reads a mental health case.
    [Arguments]    ${caso}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    GET On Session    msp_session    ${BASE_URL}/saude-mental/acompanhamento/${caso}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Casos Saude Mental
    [Documentation]    Lists the cases in force in the scope.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    GET On Session    msp_session    ${BASE_URL}/saude-mental/acompanhamentos    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Referencia Saude Mental
    [Documentation]    Changes the reference team or technician.
    [Arguments]    ${caso}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    POST On Session    msp_session    ${BASE_URL}/saude-mental/acompanhamento/${caso}/referencia    json=${payload}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Avaliacao Saude Mental
    [Documentation]    Registers an assessment.
    [Arguments]    ${caso}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    POST On Session    msp_session    ${BASE_URL}/saude-mental/acompanhamento/${caso}/avaliacao    json=${payload}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Encerrar Caso Saude Mental
    [Documentation]    Closes the case.
    [Arguments]    ${caso}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    POST On Session    msp_session    ${BASE_URL}/saude-mental/acompanhamento/${caso}/encerramento    json=${payload}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Tipos Intervencao Saude Mental
    [Documentation]    Lists the intervention types.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    GET On Session    msp_session    ${BASE_URL}/saude-mental/tipos-intervencao    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST PTS Saude Mental
    [Documentation]    Elaborates or revises the PTS of the case.
    [Arguments]    ${caso}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    POST On Session    msp_session    ${BASE_URL}/saude-mental/acompanhamento/${caso}/pts    json=${payload}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET PTS Saude Mental
    [Documentation]    Lists the PTS versions of the case.
    [Arguments]    ${caso}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    GET On Session    msp_session    ${BASE_URL}/saude-mental/acompanhamento/${caso}/pts    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Intervencao Saude Mental
    [Documentation]    Registers an intervention in the case.
    [Arguments]    ${caso}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    POST On Session    msp_session    ${BASE_URL}/saude-mental/acompanhamento/${caso}/intervencao    json=${payload}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Rede Apoio Saude Mental
    [Documentation]    Adds a person to the support network.
    [Arguments]    ${caso}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    POST On Session    msp_session    ${BASE_URL}/saude-mental/acompanhamento/${caso}/rede-apoio    json=${payload}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Encerrar Rede Apoio Saude Mental
    [Documentation]    Removes a person from the support network.
    [Arguments]    ${caso}    ${pessoa}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    POST On Session    msp_session    ${BASE_URL}/saude-mental/acompanhamento/${caso}/rede-apoio/${pessoa}/encerramento    json=${payload}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Encaminhamento Saude Mental
    [Documentation]    Refers the case through the Regulation.
    [Arguments]    ${caso}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    POST On Session    msp_session    ${BASE_URL}/saude-mental/acompanhamento/${caso}/encaminhamento    json=${payload}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Compartilhamento Saude Mental
    [Documentation]    Shares the care with another team.
    [Arguments]    ${caso}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    POST On Session    msp_session    ${BASE_URL}/saude-mental/acompanhamento/${caso}/compartilhamento    json=${payload}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Encerrar Compartilhamento Saude Mental
    [Documentation]    Ends a care sharing.
    [Arguments]    ${caso}    ${id}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    POST On Session    msp_session    ${BASE_URL}/saude-mental/acompanhamento/${caso}/compartilhamento/${id}/encerramento    json=${payload}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Transferencia Saude Mental
    [Documentation]    Proposes the care transfer.
    [Arguments]    ${caso}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    POST On Session    msp_session    ${BASE_URL}/saude-mental/acompanhamento/${caso}/transferencia    json=${payload}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Aceite Transferencia Saude Mental
    [Documentation]    Accepts a care transfer.
    [Arguments]    ${id}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    POST On Session    msp_session    ${BASE_URL}/saude-mental/transferencia/${id}/aceite    json=${payload}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Recusa Transferencia Saude Mental
    [Documentation]    Refuses a care transfer.
    [Arguments]    ${id}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    POST On Session    msp_session    ${BASE_URL}/saude-mental/transferencia/${id}/recusa    json=${payload}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Transferencias Saude Mental
    [Documentation]    Lists the pending care transfers.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    GET On Session    msp_session    ${BASE_URL}/saude-mental/transferencias    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Risco Saude Mental
    [Documentation]    Assesses a risk in the case.
    [Arguments]    ${caso}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    POST On Session    msp_session    ${BASE_URL}/saude-mental/acompanhamento/${caso}/risco    json=${payload}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Crise Saude Mental
    [Documentation]    Registers a crisis.
    [Arguments]    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    POST On Session    msp_session    ${BASE_URL}/saude-mental/crise    json=${payload}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Etapa Crise Saude Mental
    [Documentation]    Registers a crisis step.
    [Arguments]    ${crise}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    POST On Session    msp_session    ${BASE_URL}/saude-mental/crise/${crise}/etapa    json=${payload}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Encerrar Crise Saude Mental
    [Documentation]    Closes a crisis.
    [Arguments]    ${crise}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    POST On Session    msp_session    ${BASE_URL}/saude-mental/crise/${crise}/encerramento    json=${payload}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Notificacao Crise Saude Mental
    [Documentation]    Registers that the compulsory notification was made.
    [Arguments]    ${crise}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    POST On Session    msp_session    ${BASE_URL}/saude-mental/crise/${crise}/notificacao    json=${payload}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Crises Saude Mental
    [Documentation]    Lists the crises in scope.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    GET On Session    msp_session    ${BASE_URL}/saude-mental/crises    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
