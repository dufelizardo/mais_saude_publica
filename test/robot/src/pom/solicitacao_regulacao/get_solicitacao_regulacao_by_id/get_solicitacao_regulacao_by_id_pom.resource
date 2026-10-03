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
GET Get Solicitacao Regulacao By Id
    [Documentation]    Fetches the clinical detail of a request.
    [Arguments]    ${uuid}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    GET On Session    msp_session    ${BASE_URL}${SOLICITACAO_REGULACAO_ENDPOINT}/${uuid}    expected_status=ANY
    Log    ${response.status_code}
    Log    ${response.text}
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
