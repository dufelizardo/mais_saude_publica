*** Settings ***
Resource    ../../../pom/transferencia_farmacia/cancelamento_transferencia_farmacia/cancelamento_transferencia_farmacia_pom.resource
Resource    ../get_transferencia_farmacia_by_id/get_transferencia_farmacia_by_id_flow.resource
Resource    ../../lote/get_lote_by_id/get_lote_by_id_flow.resource
Resource    ../recebimento_transferencia_farmacia/recebimento_transferencia_farmacia_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Transferência em duas etapas (ADR-0061): a transferência sai EM_TRANSITO no envio e só entra no destino
    no recebimento conferido por outro profissional, ou volta à origem no cancelamento.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Build Cancelamento Payload
    [Documentation]    Builds a CancelamentoTransferenciaRequestDto payload.
    [Arguments]    ${matricula}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary
    ...    profissionalMatricula=${matricula}
    ...    motivo=Unidade de destino recebeu doação

    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Cancelar Transferencia Farmacia - Status Code 200
    [Documentation]    Sends 30 units (origin goes to 70), cancels the transfer and checks the origin Lote is back to 100.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${loteId}    ${matriculaEnvio}=    Seed Uma Transferencia
    ${payload}=    Build Cancelamento Payload    ${matriculaEnvio}

    ${response}=    POST Cancelar Transferencia Farmacia    ${uuid}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${SUCCESS_RESPONSE_SCHEMA}

    ${transferencia}=    GET Get Transferencia Farmacia By Id    ${uuid}
    Should Be Equal    ${transferencia.json()}[status]    CANCELADA
    ${origem}=    Call GET Get Lote By Id    ${loteId}
    Should Be Equal As Integers    ${origem.json()}[quantidade]    100

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Cancelar Transferencia Farmacia - Status Code 422 - Ja Recebida
    [Documentation]    Receives the transfer and then tries to cancel it, expecting HTTP 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${loteId}    ${matriculaEnvio}=    Seed Uma Transferencia
    ${matriculaRecebimento}=    Seed A Profissional Matricula
    ${recebimento}=    Build Recebimento Payload    ${30}    ${matriculaRecebimento}
    ${recebido}=    POST Receber Transferencia Farmacia    ${uuid}    ${recebimento}
    Should Be Equal As Integers    ${recebido.status_code}    200

    ${payload}=    Build Cancelamento Payload    ${matriculaEnvio}
    ${response}=    POST Cancelar Transferencia Farmacia    ${uuid}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Transferencia Farmacia - Cancelamento - POST - By HTTP Status Code
    [Documentation]    Executes the appropriate Cancelamento Transferencia Farmacia test scenario based on the provided HTTP
    ...    status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    POST Cancelar Transferencia Farmacia - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '422'    POST Cancelar Transferencia Farmacia - Status Code 422 - Ja Recebida
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Cancelamento Transferencia Farmacia test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

