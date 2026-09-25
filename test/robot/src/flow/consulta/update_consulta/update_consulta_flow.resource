*** Settings ***
Resource    ../../../pom/consulta/update_consulta/update_consulta_pom.resource
Resource    ../create_consulta/create_consulta_flow.resource
Resource    ../get_consulta_by_id/get_consulta_by_id_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Consulta's PATCH replaces every editable field at once (mesmo contrato do Setor/Atendimento/Agendamento,
    ver ADR-0043) — corrigir diagnóstico/receituário é uma atualização comum.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Call PATCH Update Consulta
    [Documentation]    Calls the PATCH Update Consulta keyword from the POM.
    [Arguments]    ${uuid}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    PATCH Update Consulta    ${uuid}    ${payload}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Consulta - Status Code 200
    [Documentation]    Seeds a fresh Consulta and corrects its diagnostico.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${consultaId}    ${atendimentoId}    ${matricula}=    Seed Uma Consulta
    ${payload}=    Build Consulta Payload    ${atendimentoId}    ${matricula}    tipoConsulta=RETORNO

    ${response}=    Call PATCH Update Consulta    ${consultaId}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200

    ${getResponse}=    Call GET Get Consulta By Id    ${consultaId}
    Should Be Equal As Integers    ${getResponse.status_code}    200
    Should Be Equal As Strings    ${getResponse.json()['tipoConsulta']}    RETORNO

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Consulta - Status Code 404
    [Documentation]    Attempts to update a Consulta identified by a random, non-existent uuid, expecting
    ...    HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atendimentoId}    ${pacienteId}    ${matricula}    ${unidadeId}=    Seed Um Atendimento
    ${payload}=    Build Consulta Payload    ${atendimentoId}    ${matricula}
    ${uuidInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid

    ${response}=    Call PATCH Update Consulta    ${uuidInexistente}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Consulta - Update - PATCH - By HTTP Status Code
    [Documentation]    Executes the appropriate Update Consulta test scenario based on the provided HTTP
    ...    status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    PATCH Update Consulta - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    PATCH Update Consulta - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Update Consulta test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
