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
POST Agendar Solicitacao Regulacao
    [Documentation]    Sends POST /{uuid}/agendamento and returns the response.
    [Arguments]    ${uuid}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${response}=    Post On Session    msp_session    ${BASE_URL}${SOLICITACAO_REGULACAO_ENDPOINT}/${uuid}/agendamento    json=${payload}    headers=${headers}    expected_status=ANY
    Log    ${response.status_code}
    Log    ${response.text}
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Autorizar Solicitacao Regulacao
    [Documentation]    Sends POST /{uuid}/autorizacao and returns the response.
    [Arguments]    ${uuid}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${response}=    Post On Session    msp_session    ${BASE_URL}${SOLICITACAO_REGULACAO_ENDPOINT}/${uuid}/autorizacao    json=${payload}    headers=${headers}    expected_status=ANY
    Log    ${response.status_code}
    Log    ${response.text}
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
