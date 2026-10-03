*** Settings ***
Library     String
Library     Collections
Resource    ../../../pom/solicitacao_regulacao/complemento_solicitacao_regulacao/complemento_solicitacao_regulacao_pom.resource
Resource    ../create_solicitacao_regulacao/create_solicitacao_regulacao_flow.resource
Resource    ../get_solicitacao_regulacao_by_id/get_solicitacao_regulacao_by_id_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Complementar devolve à fila, na posição original.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
POST Complementar Solicitacao Regulacao - Status Code 200
    [Documentation]    Returns the request and then complements it.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${matricula}    ${procedimentoId}    ${pacienteId}    ${unidadeId}=    Seed A Solicitacao Regulacao
    ${regulador}=    Seed A Profissional Matricula
    ${payload}=    Create Dictionary    profissionalMatricula=${regulador}    motivo=Anexar o ECG
    ${devolvida}=    POST Devolver Solicitacao Regulacao    ${uuid}    ${payload}
    Should Be Equal As Integers    ${devolvida.status_code}    200
    ${complemento}=    Create Dictionary    profissionalMatricula=${matricula}    complemento=ECG com infradesnivelamento de ST.
    ${response}=    POST Complementar Solicitacao Regulacao    ${uuid}    ${complemento}
    Should Be Equal As Integers    ${response.status_code}    200
    ${lida}=    GET Get Solicitacao Regulacao By Id    ${uuid}
    Should Be Equal    ${lida.json()}[status]    SOLICITADA
    Length Should Be    ${lida.json()}[eventos]    3
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Complementar Solicitacao Regulacao - Status Code 422 - Nao Devolvida
    [Documentation]    Complements a request still in the queue, expecting HTTP 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${matricula}    ${procedimentoId}    ${pacienteId}    ${unidadeId}=    Seed A Solicitacao Regulacao
    ${complemento}=    Create Dictionary    profissionalMatricula=${matricula}    complemento=Sem pedido de complemento
    ${response}=    POST Complementar Solicitacao Regulacao    ${uuid}    ${complemento}
    Should Be Equal As Integers    ${response.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Solicitacao Regulacao - Complemento - POST - By HTTP Status Code
    [Documentation]    Executes the Solicitacao Regulacao - Complemento - POST scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    POST Complementar Solicitacao Regulacao - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '422'    POST Complementar Solicitacao Regulacao - Status Code 422 - Nao Devolvida
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Solicitacao Regulacao - Complemento - POST test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
