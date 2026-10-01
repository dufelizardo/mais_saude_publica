*** Settings ***
Library     String
Library     Collections
Resource    ../../../pom/solicitacao_regulacao/cancelamento_solicitacao_regulacao/cancelamento_solicitacao_regulacao_pom.resource
Resource    ../create_solicitacao_regulacao/create_solicitacao_regulacao_flow.resource
Resource    ../get_solicitacao_regulacao_by_id/get_solicitacao_regulacao_by_id_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Cancelar vale até a autorização; depois de negada, não.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
POST Cancelar Solicitacao Regulacao - Status Code 200
    [Documentation]    Cancels a request in the queue.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${matricula}    ${procedimentoId}    ${pacienteId}    ${unidadeId}=    Seed A Solicitacao Regulacao
    ${payload}=    Create Dictionary    profissionalMatricula=${matricula}    motivo=Paciente mudou de município
    ${response}=    POST Cancelar Solicitacao Regulacao    ${uuid}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200
    ${lida}=    GET Get Solicitacao Regulacao By Id    ${uuid}
    Should Be Equal    ${lida.json()}[status]    CANCELADA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Cancelar Solicitacao Regulacao - Status Code 422 - Ja Cancelada
    [Documentation]    Cancels twice, expecting HTTP 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${matricula}    ${procedimentoId}    ${pacienteId}    ${unidadeId}=    Seed A Solicitacao Regulacao
    ${payload}=    Create Dictionary    profissionalMatricula=${matricula}    motivo=Paciente mudou de município
    ${primeiro}=    POST Cancelar Solicitacao Regulacao    ${uuid}    ${payload}
    Should Be Equal As Integers    ${primeiro.status_code}    200
    ${response}=    POST Cancelar Solicitacao Regulacao    ${uuid}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Solicitacao Regulacao - Cancelamento - POST - By HTTP Status Code
    [Documentation]    Executes the Solicitacao Regulacao - Cancelamento - POST scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    POST Cancelar Solicitacao Regulacao - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '422'    POST Cancelar Solicitacao Regulacao - Status Code 422 - Ja Cancelada
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Solicitacao Regulacao - Cancelamento - POST test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
