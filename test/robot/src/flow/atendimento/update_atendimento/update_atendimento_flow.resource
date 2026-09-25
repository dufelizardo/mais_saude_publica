*** Settings ***
Resource    ../../../pom/atendimento/update_atendimento/update_atendimento_pom.resource
Resource    ../create_atendimento/create_atendimento_flow.resource
Resource    ../get_atendimento_by_id/get_atendimento_by_id_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Atendimento's PATCH replaces every editable field at once (mesmo contrato do Setor, ver ADR-0041) —
    avançar o status (AGENDADO → EM_ANDAMENTO → CONCLUIDO) é uma atualização comum, sem endpoint dedicado.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Call PATCH Update Atendimento
    [Documentation]    Calls the PATCH Update Atendimento keyword from the POM.
    [Arguments]    ${uuid}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    PATCH Update Atendimento    ${uuid}    ${payload}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Atendimento - Status Code 200
    [Documentation]    Seeds a fresh Atendimento and advances its status to CONCLUIDO.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atendimentoId}    ${pacienteId}    ${matricula}    ${unidadeId}=    Seed Um Atendimento
    ${payload}=    Build Atendimento Payload    ${pacienteId}    ${matricula}    ${unidadeId}    status=CONCLUIDO

    ${response}=    Call PATCH Update Atendimento    ${atendimentoId}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200

    ${getResponse}=    Call GET Get Atendimento By Id    ${atendimentoId}
    Should Be Equal As Integers    ${getResponse.status_code}    200
    Should Be Equal As Strings    ${getResponse.json()['status']}    CONCLUIDO

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Atendimento - Status Code 404
    [Documentation]    Attempts to update an Atendimento identified by a random, non-existent uuid, expecting
    ...    HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${matricula}=    Seed A Profissional Matricula
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${nomePaciente}=    Seed A Paciente
    ${payload}=    Build Atendimento Payload    ${pacienteId}    ${matricula}    ${unidadeId}
    ${uuidInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid

    ${response}=    Call PATCH Update Atendimento    ${uuidInexistente}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Atendimento - Update - PATCH - By HTTP Status Code
    [Documentation]    Executes the appropriate Update Atendimento test scenario based on the provided HTTP
    ...    status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    PATCH Update Atendimento - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    PATCH Update Atendimento - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Update Atendimento test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
