*** Settings ***
Library     String
Library     Collections
Resource    ../../../pom/solicitacao_regulacao/get_solicitacao_regulacao_by_id/get_solicitacao_regulacao_by_id_pom.resource
Resource    ../create_solicitacao_regulacao/create_solicitacao_regulacao_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    O detalhe traz CID, justificativa e eventos; a leitura entra na auditoria.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
GET Get Solicitacao Regulacao By Id - Status Code 200
    [Documentation]    Reads the detail with normalized ICD-10 and the first event.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${matricula}    ${procedimentoId}    ${pacienteId}    ${unidadeId}=    Seed A Solicitacao Regulacao
    ${response}=    GET Get Solicitacao Regulacao By Id    ${uuid}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${SOLICITACAO_REGULACAO_SCHEMA}
    Should Be Equal    ${response.json()}[cid]    I209
    Should Be Equal    ${response.json()}[eventos][0][tipo]    SOLICITACAO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Get Solicitacao Regulacao By Id - Status Code 404
    [Documentation]    Reads a random uuid, expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${response}=    GET Get Solicitacao Regulacao By Id    ${uuid}
    Should Be Equal As Integers    ${response.status_code}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Solicitacao Regulacao - Get By Id - GET - By HTTP Status Code
    [Documentation]    Executes the Solicitacao Regulacao - Get By Id - GET scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    GET Get Solicitacao Regulacao By Id - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    GET Get Solicitacao Regulacao By Id - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Solicitacao Regulacao - Get By Id - GET test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
