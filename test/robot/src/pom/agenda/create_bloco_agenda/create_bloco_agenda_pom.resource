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
POST Create Bloco Agenda
    [Documentation]    Creates a recurring agenda block and returns the response.
    [Arguments]    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${response}=    Post On Session    msp_session    ${BASE_URL}${AGENDA_ENDPOINT}/bloco    json=${payload}    headers=${headers}    expected_status=ANY
    Log    ${response.status_code}
    Log    ${response.text}
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Vagas Agenda
    [Documentation]    Lists the free slots of a professional at a unit in the period.
    [Arguments]    ${matricula}    ${unidadeId}    ${segunda}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${params}=    Create Dictionary    profissionalMatricula=${matricula}    unidadeId=${unidadeId}    de=${segunda}    ate=${segunda}
    ${response}=    GET On Session    msp_session    ${BASE_URL}${AGENDA_ENDPOINT}/vagas    params=${params}    expected_status=ANY
    Log    ${response.text}
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
