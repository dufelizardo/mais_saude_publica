*** Settings ***
Resource    ../../../pom/lote/update_lote/update_lote_pom.resource
Resource    ../create_lote/create_lote_flow.resource
Resource    ../get_lote_by_id/get_lote_by_id_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Lote's PATCH replaces every editable field at once (mesmo contrato do resto da plataforma, ver
    ADR-0050) — corrigir a quantidade após dispensação ou perda é uma atualização comum.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Call PATCH Update Lote
    [Documentation]    Calls the PATCH Update Lote keyword from the POM.
    [Arguments]    ${uuid}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    PATCH Update Lote    ${uuid}    ${payload}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Lote - Status Code 200
    [Documentation]    Seeds a fresh Lote and corrects its quantidade.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${loteId}    ${medicamentoId}    ${unidadeId}=    Seed Um Lote
    ${payload}    ${numeroLote}=    Build Lote Payload    ${medicamentoId}    ${unidadeId}    quantidade=${42}

    ${response}=    Call PATCH Update Lote    ${loteId}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200

    ${getResponse}=    Call GET Get Lote By Id    ${loteId}
    Should Be Equal As Integers    ${getResponse.status_code}    200
    Should Be Equal As Integers    ${getResponse.json()['quantidade']}    42

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Lote - Status Code 404
    [Documentation]    Attempts to update a Lote identified by a random, non-existent uuid, expecting
    ...    HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${medicamentoId}    ${nomeMedicamento}=    Seed Um Medicamento
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${payload}    ${numeroLote}=    Build Lote Payload    ${medicamentoId}    ${unidadeId}
    ${uuidInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid

    ${response}=    Call PATCH Update Lote    ${uuidInexistente}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Lote - Update - PATCH - By HTTP Status Code
    [Documentation]    Executes the appropriate Update Lote test scenario based on the provided HTTP
    ...    status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    PATCH Update Lote - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    PATCH Update Lote - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Update Lote test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
