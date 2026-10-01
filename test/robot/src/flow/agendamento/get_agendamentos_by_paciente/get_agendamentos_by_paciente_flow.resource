*** Settings ***
Resource    ../../../pom/agendamento/get_agendamentos_by_paciente/get_agendamentos_by_paciente_pom.resource
Resource    ../create_agendamento/create_agendamento_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Usado pela tela de Pacientes para a seção "Próximos atendimentos" (ver ADR-0052). Mesma convenção dos demais
    endpoints de listagem por entidade relacionada: lista vazia responde 404.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Call GET Get Agendamentos By Paciente
    [Documentation]    Calls the GET Get Agendamentos By Paciente keyword from the POM.
    [Arguments]    ${pacienteId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Get Agendamentos By Paciente    ${pacienteId}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Get Agendamentos By Paciente - Status Code 200
    [Documentation]    Seeds a fresh Agendamento and lists the Agendamentos of its Paciente, expecting HTTP 200
    ...    with only that Paciente's Agendamentos.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${agendamentoId}    ${pacienteId}    ${matricula}=    Seed Um Agendamento

    ${response}=    Call GET Get Agendamentos By Paciente    ${pacienteId}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Array Schema    ${response.json()}    ${AGENDAMENTO_SCHEMA}

    ${outrosPacientes}=    Evaluate    [a for a in $response.json() if a['pacienteUuid'] != '${pacienteId}']
    Should Be Empty    ${outrosPacientes}
    ${ids}=    Evaluate    [a['uuid'] for a in $response.json()]
    Should Contain    ${ids}    ${agendamentoId}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Get Agendamentos By Paciente - Status Code 404 - Paciente Sem Agendamentos
    [Documentation]    Seeds a fresh Paciente with no Agendamentos and lists them, expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${nomePaciente}=    Seed A Paciente

    ${response}=    Call GET Get Agendamentos By Paciente    ${pacienteId}
    Should Be Equal As Integers    ${response.status_code}    404
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Agendamento - Get By Paciente - GET - By HTTP Status Code
    [Documentation]    Executes the appropriate Get Agendamentos By Paciente test scenario based on the provided
    ...    HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    GET Get Agendamentos By Paciente - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    GET Get Agendamentos By Paciente - Status Code 404 - Paciente Sem Agendamentos
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Get Agendamentos By Paciente test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
