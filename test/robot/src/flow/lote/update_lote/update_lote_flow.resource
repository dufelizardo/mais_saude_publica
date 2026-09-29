*** Settings ***
Resource    ../../../pom/lote/update_lote/update_lote_pom.resource
Resource    ../create_lote/create_lote_flow.resource
Resource    ../get_lote_by_id/get_lote_by_id_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Desde a ADR-0057, o PATCH de Lote só corrige numeroLote e validade. Quantidade muda apenas pelo livro de
    movimentação (/movimentacao-farmacia), e medicamento/unidade não mudam — o cenário 200 envia quantidade de
    propósito, para provar que ela é ignorada.
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
    [Documentation]    Seeds a fresh Lote (quantidade 100) and corrects its numeroLote and validade, sending a
    ...    different quantidade that must be ignored.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${loteId}    ${medicamentoId}    ${unidadeId}=    Seed Um Lote
    ${payload}    ${numeroLote}=    Build Lote Payload    ${medicamentoId}    ${unidadeId}    validade=2028-06-30
    ...    quantidade=${42}

    ${response}=    Call PATCH Update Lote    ${loteId}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200

    ${getResponse}=    Call GET Get Lote By Id    ${loteId}
    Should Be Equal As Integers    ${getResponse.status_code}    200
    Should Be Equal    ${getResponse.json()['numeroLote']}    ${numeroLote}
    Should Be Equal    ${getResponse.json()['validade']}    2028-06-30
    Should Be Equal As Integers    ${getResponse.json()['quantidade']}    100

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

PATCH Update Lote - Status Code 422 - Remessa Ja Existente
    [Documentation]    Seeds two Lotes of the same medicamento in the same unit and tries to correct the second
    ...    one into the first one's remessa, expecting HTTP 422 and no change (ADR-0060).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${loteId}    ${medicamentoId}    ${unidadeId}=    Seed Um Lote
    ${loteA}=    Call GET Get Lote By Id    ${loteId}
    ${payloadB}    ${numeroLoteB}=    Build Lote Payload    ${medicamentoId}    ${unidadeId}
    ${criado}=    POST Create Lote    ${payloadB}
    Should Be Equal As Integers    ${criado.status_code}    201
    ${loteBId}=    Buscar Lote Uuid Por NumeroLote    ${numeroLoteB}

    ${correcao}=    Create Dictionary
    ...    numeroLote=${loteA.json()['numeroLote']}
    ...    validade=${loteA.json()['validade']}
    ${response}=    Call PATCH Update Lote    ${loteBId}    ${correcao}
    Should Be Equal As Integers    ${response.status_code}    422
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    ${loteB}=    Call GET Get Lote By Id    ${loteBId}
    Should Be Equal    ${loteB.json()['numeroLote']}    ${numeroLoteB}

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
    ...    ELSE IF    '${meuHTTP}' == '422'    PATCH Update Lote - Status Code 422 - Remessa Ja Existente
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Update Lote test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
