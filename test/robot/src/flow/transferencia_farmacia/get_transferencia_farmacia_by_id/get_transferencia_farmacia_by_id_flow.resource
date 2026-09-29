*** Settings ***
Resource    ../../../pom/transferencia_farmacia/get_transferencia_farmacia_by_id/get_transferencia_farmacia_by_id_pom.resource
Resource    ../create_transferencia_farmacia/create_transferencia_farmacia_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed Uma Transferencia
    [Documentation]    Transfers 30 units from a fresh Lote (100) to a fresh Unidade de Saúde, returning the
    ...    transferência uuid and the origin Lote uuid.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${loteId}    ${medicamentoId}    ${unidadeId}=    Seed Um Lote
    ${unidadeDestinoId}=    Seed A Unidade De Saude Uuid
    ${matricula}=    Seed A Profissional Matricula
    ${payload}=    Build Transferencia Payload    ${loteId}    ${unidadeDestinoId}    ${matricula}

    ${response}=    POST Create Transferencia Farmacia    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    ${lista}=    GET List Transferencias Farmacia
    Should Be Equal As Integers    ${lista.status_code}    200
    ${uuid}=    Set Variable    ${NONE}
    FOR    ${t}    IN    @{lista.json()}
        IF    '${t}[loteOrigemId]' == '${loteId}'
            ${uuid}=    Set Variable    ${t}[uuid]
            BREAK
        END
    END
    Should Not Be Equal    ${uuid}    ${NONE}

    RETURN    ${uuid}    ${loteId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Get Transferencia Farmacia By Id - Status Code 200
    [Documentation]    Seeds a fresh TransferenciaFarmacia and fetches it by id, expecting HTTP 200.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${loteId}=    Seed Uma Transferencia
    ${response}=    GET Get Transferencia Farmacia By Id    ${uuid}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${TRANSFERENCIA_FARMACIA_SCHEMA}
    Should Be Equal As Strings    ${response.json()}[loteOrigemId]    ${loteId}
    Should Be Equal As Integers    ${response.json()}[quantidade]    30

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Get Transferencia Farmacia By Id - Status Code 404
    [Documentation]    Fetches a TransferenciaFarmacia by a random, non-existent uuid, expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuidInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${response}=    GET Get Transferencia Farmacia By Id    ${uuidInexistente}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Transferencia Farmacia - Get By Id - GET - By HTTP Status Code
    [Documentation]    Executes the appropriate Get Transferencia Farmacia By Id test scenario based on the provided
    ...    HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    GET Get Transferencia Farmacia By Id - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    GET Get Transferencia Farmacia By Id - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Get Transferencia Farmacia By Id test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
