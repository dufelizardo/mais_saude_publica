*** Settings ***
Resource    ../../../pom/agendamento/update_agendamento/update_agendamento_pom.resource
Resource    ../create_agendamento/create_agendamento_flow.resource
Resource    ../get_agendamento_by_id/get_agendamento_by_id_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Agendamento's PATCH replaces every editable field at once (mesmo contrato do Setor/Atendimento, ver
    ADR-0042) — avançar o status (AGENDADO → CONFIRMADO → REALIZADO/CANCELADO) é uma atualização comum.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Call PATCH Update Agendamento
    [Documentation]    Calls the PATCH Update Agendamento keyword from the POM.
    [Arguments]    ${uuid}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    PATCH Update Agendamento    ${uuid}    ${payload}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Agendamento - Status Code 200
    [Documentation]    Seeds a fresh Agendamento and advances its status to CONFIRMADO.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${agendamentoId}    ${pacienteId}    ${matricula}=    Seed Um Agendamento
    ${payload}=    Build Agendamento Payload    ${pacienteId}    ${matricula}    status=CONFIRMADO

    ${response}=    Call PATCH Update Agendamento    ${agendamentoId}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200

    ${getResponse}=    Call GET Get Agendamento By Id    ${agendamentoId}
    Should Be Equal As Integers    ${getResponse.status_code}    200
    Should Be Equal As Strings    ${getResponse.json()['status']}    CONFIRMADO

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Agendamento - Status Code 404
    [Documentation]    Attempts to update an Agendamento identified by a random, non-existent uuid, expecting
    ...    HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${matricula}=    Seed A Profissional Matricula
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${nomePaciente}=    Seed A Paciente
    ${payload}=    Build Agendamento Payload    ${pacienteId}    ${matricula}
    ${uuidInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid

    ${response}=    Call PATCH Update Agendamento    ${uuidInexistente}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Agendamento - Update - PATCH - By HTTP Status Code
    [Documentation]    Executes the appropriate Update Agendamento test scenario based on the provided HTTP
    ...    status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    PATCH Update Agendamento - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    PATCH Update Agendamento - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Update Agendamento test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
