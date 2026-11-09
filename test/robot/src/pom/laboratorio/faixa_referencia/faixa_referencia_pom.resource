*** Settings ***
Resource    ../../common/mais_saude_publica_common.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Laboratório: faixas de referência por sexo e idade e evolução do exame (ADR-0180).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
${FAIXA_EXAME_ENDPOINT}         /exame-laboratorial
${EVOLUCAO_PRONTUARIO_ENDPOINT}    /prontuario
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
POST Create Faixa Referencia
    [Documentation]    Adds a sex and age reference range to an exam; returns the exam with its ranges.
    [Arguments]    ${exameId}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${response}=    Post On Session    msp_session    ${BASE_URL}${FAIXA_EXAME_ENDPOINT}/${exameId}/faixa    json=${payload}    headers=${headers}    expected_status=ANY
    Log    ${response.status_code}
    Log    ${response.text}
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PUT Update Faixa Referencia
    [Documentation]    Edits a reference range.
    [Arguments]    ${faixaId}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${response}=    PUT On Session    msp_session    ${BASE_URL}${FAIXA_EXAME_ENDPOINT}/faixa/${faixaId}    json=${payload}    headers=${headers}    expected_status=ANY
    Log    ${response.status_code}
    Log    ${response.text}
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

DELETE Faixa Referencia
    [Documentation]    Removes a reference range.
    [Arguments]    ${faixaId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    DELETE On Session    msp_session    ${BASE_URL}${FAIXA_EXAME_ENDPOINT}/faixa/${faixaId}    expected_status=ANY
    Log    ${response.status_code}
    Log    ${response.text}
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Exame Laboratorial By Id
    [Documentation]    Reads one exam of the catalog, with its ranges.
    [Arguments]    ${exameId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    GET On Session    msp_session    ${BASE_URL}${FAIXA_EXAME_ENDPOINT}/${exameId}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Evolucao Do Exame
    [Documentation]    Reads the evolution of a numeric exam of the patient (prontuário rules).
    [Arguments]    ${pacienteId}    ${exameId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    GET On Session    msp_session    ${BASE_URL}${EVOLUCAO_PRONTUARIO_ENDPOINT}/${pacienteId}/exame/${exameId}/evolucao    expected_status=ANY
    Log    ${response.status_code}
    Log    ${response.text}
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
