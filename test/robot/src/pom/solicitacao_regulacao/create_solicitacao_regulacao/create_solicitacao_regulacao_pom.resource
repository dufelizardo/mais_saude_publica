*** Settings ***
Resource    ../../common/mais_saude_publica_common.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Regulação do acesso (ADR-0087).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
${SOLICITACAO_REGULACAO_ENDPOINT}    /solicitacao-regulacao
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
POST Create Solicitacao Regulacao
    [Documentation]    Creates a SolicitacaoRegulacao and returns the response.
    [Arguments]    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${response}=    Post On Session    msp_session    ${BASE_URL}${SOLICITACAO_REGULACAO_ENDPOINT}/    json=${payload}    headers=${headers}    expected_status=ANY
    Log    ${response.status_code}
    Log    ${response.text}
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET List Solicitacao Regulacao Do Paciente
    [Documentation]    Lists the requests of one patient (no clinical data).
    [Arguments]    ${pacienteId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    GET On Session    msp_session    ${BASE_URL}${SOLICITACAO_REGULACAO_ENDPOINT}/    params=pacienteId=${pacienteId}    expected_status=ANY
    Log    ${response.status_code}
    Log    ${response.text}
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
