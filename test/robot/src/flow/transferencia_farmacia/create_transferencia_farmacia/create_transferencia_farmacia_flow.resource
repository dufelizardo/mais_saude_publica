*** Settings ***
Library    Collections
Resource    ../../../pom/transferencia_farmacia/create_transferencia_farmacia/create_transferencia_farmacia_pom.resource
Resource    ../../lote/create_lote/create_lote_flow.resource
Resource    ../../lote/get_lote_by_id/get_lote_by_id_flow.resource
Resource    ../../profissional/get_profissional_by_matricula/get_profissional_by_matricula_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Transferência de estoque entre unidades (ADR-0059): saída no lote de origem e entrada no lote da mesma remessa
    na unidade de destino, pelo livro de movimentação. Seed Um Lote cria o lote de origem com quantidade 100.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Build Transferencia Payload
    [Documentation]    Builds a TransferenciaFarmaciaRequestDto payload.
    [Arguments]    ${loteOrigemId}    ${unidadeDestinoId}    ${matricula}    ${quantidade}=${30}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary
    ...    loteOrigemId=${loteOrigemId}
    ...    unidadeDestinoId=${unidadeDestinoId}
    ...    quantidade=${quantidade}
    ...    profissionalMatricula=${matricula}
    ...    observacao=Remanejamento para cobrir falta

    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Transferencia Farmacia - Status Code 201
    [Documentation]    Transfers 30 units from a fresh Lote (100) to another unit and checks the origin balance
    ...    dropped to 70.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${loteId}    ${medicamentoId}    ${unidadeId}=    Seed Um Lote
    ${unidadeDestinoId}=    Seed A Unidade De Saude Uuid
    ${matricula}=    Seed A Profissional Matricula
    ${payload}=    Build Transferencia Payload    ${loteId}    ${unidadeDestinoId}    ${matricula}

    ${response}=    POST Create Transferencia Farmacia    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201
    Validate Object Schema    ${response.json()}    ${SUCCESS_RESPONSE_SCHEMA}

    ${lote}=    Call GET Get Lote By Id    ${loteId}
    Should Be Equal As Integers    ${lote.json()['quantidade']}    70

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Transferencia Farmacia - Status Code 400 - Mesma Unidade
    [Documentation]    Attempts to transfer to the Lote's own unit, expecting HTTP 400 and an unchanged balance.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${loteId}    ${medicamentoId}    ${unidadeId}=    Seed Um Lote
    ${matricula}=    Seed A Profissional Matricula
    ${payload}=    Build Transferencia Payload    ${loteId}    ${unidadeId}    ${matricula}

    ${response}=    POST Create Transferencia Farmacia    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    ${lote}=    Call GET Get Lote By Id    ${loteId}
    Should Be Equal As Integers    ${lote.json()['quantidade']}    100

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Transferencia Farmacia - Status Code 422 - Maior Que O Saldo
    [Documentation]    Attempts to transfer more than the Lote's balance, expecting HTTP 422 and an unchanged
    ...    balance.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${loteId}    ${medicamentoId}    ${unidadeId}=    Seed Um Lote
    ${unidadeDestinoId}=    Seed A Unidade De Saude Uuid
    ${matricula}=    Seed A Profissional Matricula
    ${payload}=    Build Transferencia Payload    ${loteId}    ${unidadeDestinoId}    ${matricula}    quantidade=${500}

    ${response}=    POST Create Transferencia Farmacia    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    ${lote}=    Call GET Get Lote By Id    ${loteId}
    Should Be Equal As Integers    ${lote.json()['quantidade']}    100

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Transferencia Farmacia - Create - POST - By HTTP Status Code
    [Documentation]    Executes the appropriate Create Transferencia Farmacia test scenario based on the provided
    ...    HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201'    POST Create Transferencia Farmacia - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '400'    POST Create Transferencia Farmacia - Status Code 400 - Mesma Unidade
    ...    ELSE IF    '${meuHTTP}' == '422'    POST Create Transferencia Farmacia - Status Code 422 - Maior Que O Saldo
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Create Transferencia Farmacia test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
