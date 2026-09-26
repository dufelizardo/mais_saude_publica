*** Settings ***
Resource    ../../../pom/medicamento/update_medicamento/update_medicamento_pom.resource
Resource    ../create_medicamento/create_medicamento_flow.resource
Resource    ../get_medicamento_by_id/get_medicamento_by_id_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Medicamento's PATCH replaces every editable field at once (mesmo contrato do resto da plataforma,
    ver ADR-0049) — corrigir a apresentação é uma atualização comum.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Call PATCH Update Medicamento
    [Documentation]    Calls the PATCH Update Medicamento keyword from the POM.
    [Arguments]    ${uuid}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    PATCH Update Medicamento    ${uuid}    ${payload}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Medicamento - Status Code 200
    [Documentation]    Seeds a fresh Medicamento and corrects its apresentacao.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${medicamentoId}    ${nome}=    Seed Um Medicamento
    ${payload}    ${nomeIgnorado}=    Build Medicamento Payload    nome=${nome}    apresentacao=Suspensão oral 250mg/5mL

    ${response}=    Call PATCH Update Medicamento    ${medicamentoId}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200

    ${getResponse}=    Call GET Get Medicamento By Id    ${medicamentoId}
    Should Be Equal As Integers    ${getResponse.status_code}    200
    Should Be Equal As Strings    ${getResponse.json()['apresentacao']}    Suspensão oral 250mg/5mL

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Medicamento - Status Code 404
    [Documentation]    Attempts to update a Medicamento identified by a random, non-existent uuid,
    ...    expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}    ${nome}=    Build Medicamento Payload
    ${uuidInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid

    ${response}=    Call PATCH Update Medicamento    ${uuidInexistente}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Medicamento - Update - PATCH - By HTTP Status Code
    [Documentation]    Executes the appropriate Update Medicamento test scenario based on the provided
    ...    HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    PATCH Update Medicamento - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    PATCH Update Medicamento - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Update Medicamento test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
