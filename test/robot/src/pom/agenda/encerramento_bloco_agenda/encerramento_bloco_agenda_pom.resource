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
POST Encerrar Bloco Agenda
    [Documentation]    Ends an agenda block.
    [Arguments]    ${uuid}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${response}=    Post On Session    msp_session    ${BASE_URL}${AGENDA_ENDPOINT}/bloco/${uuid}/encerramento    json=${payload}    headers=${headers}    expected_status=ANY
    Log    ${response.status_code}
    Log    ${response.text}
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Blocos Do Profissional
    [Documentation]    Lists the blocks of a professional.
    [Arguments]    ${matricula}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    GET On Session    msp_session    ${BASE_URL}${AGENDA_ENDPOINT}/bloco    params=profissionalMatricula=${matricula}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
