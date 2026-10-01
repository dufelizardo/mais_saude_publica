*** Settings ***
Resource    ../../../pom/transferencia_farmacia/recebimento_transferencia_farmacia/recebimento_transferencia_farmacia_pom.resource
Resource    ../get_transferencia_farmacia_by_id/get_transferencia_farmacia_by_id_flow.resource
Resource    ../../lote/get_lote_by_id/get_lote_by_id_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Transferência em duas etapas (ADR-0061): a transferência sai EM_TRANSITO no envio e só entra no destino
    no recebimento conferido por outro profissional, ou volta à origem no cancelamento.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Build Recebimento Payload
    [Documentation]    Builds a RecebimentoTransferenciaRequestDto payload.
    [Arguments]    ${quantidadeRecebida}    ${matricula}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary
    ...    quantidadeRecebida=${quantidadeRecebida}
    ...    profissionalMatricula=${matricula}

    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Receber Transferencia Farmacia - Status Code 200
    [Documentation]    Sends 30 units, receives all 30 by another profissional and checks the status and the destination Lote.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${loteId}    ${matriculaEnvio}=    Seed Uma Transferencia
    ${matriculaRecebimento}=    Seed A Profissional Matricula
    ${payload}=    Build Recebimento Payload    ${30}    ${matriculaRecebimento}

    ${response}=    POST Receber Transferencia Farmacia    ${uuid}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${SUCCESS_RESPONSE_SCHEMA}

    ${transferencia}=    GET Get Transferencia Farmacia By Id    ${uuid}
    Should Be Equal    ${transferencia.json()}[status]    RECEBIDA
    ${destino}=    Call GET Get Lote By Id    ${transferencia.json()}[loteDestinoId]
    Should Be Equal As Integers    ${destino.json()}[quantidade]    30

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Receber Transferencia Farmacia - Status Code 400 - Divergencia Sem Motivo
    [Documentation]    Receives less than was sent without motivoDivergencia, expecting HTTP 400 and the transfer still in transit.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${loteId}    ${matriculaEnvio}=    Seed Uma Transferencia
    ${matriculaRecebimento}=    Seed A Profissional Matricula
    ${payload}=    Build Recebimento Payload    ${27}    ${matriculaRecebimento}

    ${response}=    POST Receber Transferencia Farmacia    ${uuid}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    ${transferencia}=    GET Get Transferencia Farmacia By Id    ${uuid}
    Should Be Equal    ${transferencia.json()}[status]    EM_TRANSITO

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Receber Transferencia Farmacia - Status Code 422 - Mesmo Profissional Do Envio
    [Documentation]    Tries to receive with the same profissional who sent it, expecting HTTP 422 and the transfer still in
    ...    transit.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${loteId}    ${matriculaEnvio}=    Seed Uma Transferencia
    ${payload}=    Build Recebimento Payload    ${30}    ${matriculaEnvio}

    ${response}=    POST Receber Transferencia Farmacia    ${uuid}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    ${transferencia}=    GET Get Transferencia Farmacia By Id    ${uuid}
    Should Be Equal    ${transferencia.json()}[status]    EM_TRANSITO

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Transferencia Farmacia - Recebimento - POST - By HTTP Status Code
    [Documentation]    Executes the appropriate Recebimento Transferencia Farmacia test scenario based on the provided HTTP
    ...    status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    POST Receber Transferencia Farmacia - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '400'    POST Receber Transferencia Farmacia - Status Code 400 - Divergencia Sem Motivo
    ...    ELSE IF    '${meuHTTP}' == '422'    POST Receber Transferencia Farmacia - Status Code 422 - Mesmo Profissional Do Envio
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Recebimento Transferencia Farmacia test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

