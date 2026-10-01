*** Settings ***
Resource    ../../../pom/procedimento/status_procedimento/status_procedimento_pom.resource
Resource    ../create_procedimento/create_procedimento_flow.resource
Resource    ../get_procedimento_by_id/get_procedimento_by_id_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Desfecho de um procedimento agendado (ADR-0062): acontece uma única vez, de AGENDADO para REALIZADO ou
    CANCELADO. Depois disso, correção é retificação.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed Um Procedimento Agendado
    [Documentation]    Seeds a fresh Consulta and an AGENDADO Procedimento in it, returning its uuid and the matrícula.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${procedimentoId}    ${consultaId}    ${matricula}=    Seed Um Procedimento
    ${agendado}=    Build Procedimento Payload    ${consultaId}    ${matricula}    status=AGENDADO
    ${response}=    POST Create Procedimento    ${agendado}
    Should Be Equal As Integers    ${response.status_code}    201
    ${lista}=    GET Get All Procedimentos
    ${id}=    Evaluate    [p['uuid'] for p in $lista.json() if p['consultaUuid'] == $consultaId and p['status'] == 'AGENDADO'][0]

    RETURN    ${id}    ${matricula}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Status Procedimento - Status Code 200
    [Documentation]    Registers a scheduled Procedimento as REALIZADO and checks the new status and the kept dataPrevista.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${id}    ${matricula}=    Seed Um Procedimento Agendado
    ${payload}=    Create Dictionary
    ...    status=REALIZADO
    ...    profissionalMatricula=${matricula}
    ...    dataRealizacao=2026-01-02T10:30:00

    ${response}=    POST Status Procedimento    ${id}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${SUCCESS_RESPONSE_SCHEMA}

    ${procedimento}=    Call GET Get Procedimento By Id    ${id}
    Should Be Equal As Strings    ${procedimento.json()}[status]    REALIZADO
    Should Not Be Empty    ${procedimento.json()}[dataPrevista]

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Status Procedimento - Status Code 422 - Desfecho Ja Registrado
    [Documentation]    Registers the outcome twice: the second attempt finds the Procedimento no longer AGENDADO, expecting
    ...    HTTP 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${id}    ${matricula}=    Seed Um Procedimento Agendado
    ${payload}=    Create Dictionary
    ...    status=CANCELADO
    ...    profissionalMatricula=${matricula}
    ...    justificativa=Paciente não compareceu
    ${primeira}=    POST Status Procedimento    ${id}    ${payload}
    Should Be Equal As Integers    ${primeira.status_code}    200

    ${response}=    POST Status Procedimento    ${id}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Procedimento - Status - POST - By HTTP Status Code
    [Documentation]    Executes the appropriate Status Procedimento test scenario based on the provided HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    POST Status Procedimento - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '422'    POST Status Procedimento - Status Code 422 - Desfecho Ja Registrado
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Status Procedimento test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

