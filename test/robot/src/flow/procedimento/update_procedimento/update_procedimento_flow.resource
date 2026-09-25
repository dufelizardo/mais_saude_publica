*** Settings ***
Resource    ../../../pom/procedimento/update_procedimento/update_procedimento_pom.resource
Resource    ../create_procedimento/create_procedimento_flow.resource
Resource    ../get_procedimento_by_id/get_procedimento_by_id_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Procedimento's PATCH replaces every editable field at once (mesmo contrato das demais entidades desta
    onda, ver ADR-0044) — avançar o status (AGENDADO → REALIZADO/CANCELADO) é uma atualização comum.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Call PATCH Update Procedimento
    [Documentation]    Calls the PATCH Update Procedimento keyword from the POM.
    [Arguments]    ${uuid}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    PATCH Update Procedimento    ${uuid}    ${payload}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Procedimento - Status Code 200
    [Documentation]    Seeds a fresh Procedimento and advances its status to REALIZADO.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${procedimentoId}    ${consultaId}    ${matricula}=    Seed Um Procedimento
    ${payload}=    Build Procedimento Payload    ${consultaId}    ${matricula}    status=REALIZADO

    ${response}=    Call PATCH Update Procedimento    ${procedimentoId}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200

    ${getResponse}=    Call GET Get Procedimento By Id    ${procedimentoId}
    Should Be Equal As Integers    ${getResponse.status_code}    200
    Should Be Equal As Strings    ${getResponse.json()['status']}    REALIZADO

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Procedimento - Status Code 404
    [Documentation]    Attempts to update a Procedimento identified by a random, non-existent uuid,
    ...    expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${consultaId}    ${atendimentoId}    ${matricula}=    Seed Uma Consulta
    ${payload}=    Build Procedimento Payload    ${consultaId}    ${matricula}
    ${uuidInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid

    ${response}=    Call PATCH Update Procedimento    ${uuidInexistente}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Procedimento - Update - PATCH - By HTTP Status Code
    [Documentation]    Executes the appropriate Update Procedimento test scenario based on the provided
    ...    HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    PATCH Update Procedimento - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    PATCH Update Procedimento - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Update Procedimento test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
