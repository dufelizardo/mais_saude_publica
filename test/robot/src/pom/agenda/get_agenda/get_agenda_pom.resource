*** Settings ***
Resource    ../../common/mais_saude_publica_common.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Agenda do profissional (ADR-0091).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
${AGENDA_ENDPOINT}    /agenda
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
GET Agenda
    [Documentation]    Fetches the agenda of a professional at a unit in the period.
    [Arguments]    ${matricula}    ${unidadeId}    ${de}    ${ate}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${params}=    Create Dictionary    profissionalMatricula=${matricula}    unidadeId=${unidadeId}    de=${de}    ate=${ate}
    ${response}=    GET On Session    msp_session    ${BASE_URL}${AGENDA_ENDPOINT}/    params=${params}    expected_status=ANY
    Log    ${response.text}
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
