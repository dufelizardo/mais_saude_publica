*** Settings ***
Resource    ../common/mais_saude_publica_common.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Saúde Bucal, fatia SB5 (ADR-0140): saúde bucal coletiva.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
PUT Saude Bucal Da Acao
    [Documentation]    Fills the oral health detail of a collective action.
    [Arguments]    ${acao}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    PUT On Session    msp_session    ${BASE_URL}/acao-programatica/${acao}/saude-bucal    json=${payload}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Avaliacao Bucal Coletiva
    [Documentation]    Registers the collective oral health assessment.
    [Arguments]    ${acao}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    POST On Session    msp_session    ${BASE_URL}/acao-programatica/${acao}/avaliacao-bucal    json=${payload}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET A Convocar Saude Bucal
    [Documentation]    Lists who must be called for dental treatment in the unit.
    [Arguments]    ${unidade}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    GET On Session    msp_session    ${BASE_URL}/saude-bucal/a-convocar    params=unidadeId=${unidade}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
