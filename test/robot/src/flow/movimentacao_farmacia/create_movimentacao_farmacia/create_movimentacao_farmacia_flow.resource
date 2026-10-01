*** Settings ***
Library    Collections
Resource    ../../../pom/movimentacao_farmacia/create_movimentacao_farmacia/create_movimentacao_farmacia_pom.resource
Resource    ../../lote/create_lote/create_lote_flow.resource
Resource    ../../lote/get_lote_by_id/get_lote_by_id_flow.resource
Resource    ../../profissional/get_profissional_by_matricula/get_profissional_by_matricula_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Livro de movimentação do estoque (ADR-0057): só PERDA e AJUSTE_INVENTARIO são lançados pela API — entrada e
    dispensação são lançadas pelo próprio sistema. Seed Um Lote cria o lote com quantidade 100.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Build Perda Payload
    [Documentation]    Builds a PERDA MovimentacaoFarmaciaRequestDto payload.
    [Arguments]    ${loteId}    ${matricula}    ${quantidade}=${5}    ${motivoPerda}=VENCIMENTO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary
    ...    loteId=${loteId}
    ...    tipo=PERDA
    ...    quantidade=${quantidade}
    ...    motivoPerda=${motivoPerda}
    ...    profissionalMatricula=${matricula}

    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Movimentacao Farmacia - Status Code 201
    [Documentation]    Registers a PERDA of 5 on a fresh Lote (100) and checks the Lote's balance dropped to 95.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${loteId}    ${medicamentoId}    ${unidadeId}=    Seed Um Lote
    ${matricula}=    Seed A Profissional Matricula
    ${payload}=    Build Perda Payload    ${loteId}    ${matricula}

    ${response}=    POST Create Movimentacao Farmacia    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    ${lote}=    Call GET Get Lote By Id    ${loteId}
    Should Be Equal As Integers    ${lote.json()['quantidade']}    95

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Movimentacao Farmacia - Status Code 400 - Perda Sem Motivo
    [Documentation]    Attempts a PERDA without motivoPerda, expecting HTTP 400 and an unchanged balance.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${loteId}    ${medicamentoId}    ${unidadeId}=    Seed Um Lote
    ${matricula}=    Seed A Profissional Matricula
    ${payload}=    Build Perda Payload    ${loteId}    ${matricula}
    Remove From Dictionary    ${payload}    motivoPerda

    ${response}=    POST Create Movimentacao Farmacia    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    ${lote}=    Call GET Get Lote By Id    ${loteId}
    Should Be Equal As Integers    ${lote.json()['quantidade']}    100

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Movimentacao Farmacia - Status Code 422 - Perda Maior Que O Saldo
    [Documentation]    Attempts a PERDA larger than the Lote's balance, expecting HTTP 422 and an unchanged
    ...    balance.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${loteId}    ${medicamentoId}    ${unidadeId}=    Seed Um Lote
    ${matricula}=    Seed A Profissional Matricula
    ${payload}=    Build Perda Payload    ${loteId}    ${matricula}    quantidade=${500}

    ${response}=    POST Create Movimentacao Farmacia    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    ${lote}=    Call GET Get Lote By Id    ${loteId}
    Should Be Equal As Integers    ${lote.json()['quantidade']}    100

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Movimentacao Farmacia - Create - POST - By HTTP Status Code
    [Documentation]    Executes the appropriate Create Movimentacao Farmacia test scenario based on the provided
    ...    HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201'    POST Create Movimentacao Farmacia - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '400'    POST Create Movimentacao Farmacia - Status Code 400 - Perda Sem Motivo
    ...    ELSE IF    '${meuHTTP}' == '422'    POST Create Movimentacao Farmacia - Status Code 422 - Perda Maior Que O Saldo
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Create Movimentacao Farmacia test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
