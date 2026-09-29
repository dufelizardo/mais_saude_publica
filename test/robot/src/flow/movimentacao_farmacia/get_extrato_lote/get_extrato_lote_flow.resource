*** Settings ***
Resource    ../../../pom/movimentacao_farmacia/get_extrato_lote/get_extrato_lote_pom.resource
Resource    ../create_movimentacao_farmacia/create_movimentacao_farmacia_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
GET Get Extrato Do Lote - Status Code 200
    [Documentation]    Seeds a Lote (entrada de 100), registers a PERDA of 5 and checks the ledger lists both, in
    ...    order, with the resulting balances.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${loteId}    ${medicamentoId}    ${unidadeId}=    Seed Um Lote
    ${matricula}=    Seed A Profissional Matricula
    ${payload}=    Build Perda Payload    ${loteId}    ${matricula}
    ${perda}=    POST Create Movimentacao Farmacia    ${payload}
    Should Be Equal As Integers    ${perda.status_code}    201

    ${response}=    GET Get Extrato Do Lote    ${loteId}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Array Schema    ${response.json()}    ${MOVIMENTACAO_FARMACIA_SCHEMA}

    ${tipos}=    Evaluate    [m['tipo'] for m in $response.json()]
    ${saldos}=    Evaluate    [m['saldoApos'] for m in $response.json()]
    Should Be Equal    ${tipos}    ${{['ENTRADA', 'PERDA']}}
    Should Be Equal    ${saldos}    ${{[100, 95]}}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Get Extrato Do Lote - Status Code 404
    [Documentation]    Asks for the ledger of a random, non-existent Lote, expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuidInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${response}=    GET Get Extrato Do Lote    ${uuidInexistente}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Movimentacao Farmacia - Extrato Do Lote - GET - By HTTP Status Code
    [Documentation]    Executes the appropriate Extrato Do Lote test scenario based on the provided HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    GET Get Extrato Do Lote - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    GET Get Extrato Do Lote - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Extrato Do Lote test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
