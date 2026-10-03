*** Settings ***
Library     String
Library     Collections
Resource    ../../../pom/solicitacao_regulacao/devolucao_solicitacao_regulacao/devolucao_solicitacao_regulacao_pom.resource
Resource    ../create_solicitacao_regulacao/create_solicitacao_regulacao_flow.resource
Resource    ../get_solicitacao_regulacao_by_id/get_solicitacao_regulacao_by_id_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Devolver exige motivo; a solicitação sai da fila até o complemento.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
POST Devolver Solicitacao Regulacao - Status Code 200
    [Documentation]    Returns the request to the requester.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${matricula}    ${procedimentoId}    ${pacienteId}    ${unidadeId}=    Seed A Solicitacao Regulacao
    ${regulador}=    Seed A Profissional Matricula
    ${payload}=    Create Dictionary    profissionalMatricula=${regulador}    motivo=Anexar o ECG
    ${response}=    POST Devolver Solicitacao Regulacao    ${uuid}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${SUCCESS_RESPONSE_SCHEMA}
    ${lida}=    GET Get Solicitacao Regulacao By Id    ${uuid}
    Should Be Equal    ${lida.json()}[status]    DEVOLVIDA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Devolver Solicitacao Regulacao - Status Code 400 - Sem Motivo
    [Documentation]    Returns without a reason, expecting HTTP 400.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${matricula}    ${procedimentoId}    ${pacienteId}    ${unidadeId}=    Seed A Solicitacao Regulacao
    ${regulador}=    Seed A Profissional Matricula
    ${payload}=    Create Dictionary    profissionalMatricula=${regulador}    motivo=${EMPTY}
    ${response}=    POST Devolver Solicitacao Regulacao    ${uuid}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Solicitacao Regulacao - Devolucao - POST - By HTTP Status Code
    [Documentation]    Executes the Solicitacao Regulacao - Devolucao - POST scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    POST Devolver Solicitacao Regulacao - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '400'    POST Devolver Solicitacao Regulacao - Status Code 400 - Sem Motivo
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Solicitacao Regulacao - Devolucao - POST test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
