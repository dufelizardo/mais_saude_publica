*** Settings ***
Library     String
Library     Collections
Resource    ../../../pom/solicitacao_regulacao/negativa_solicitacao_regulacao/negativa_solicitacao_regulacao_pom.resource
Resource    ../create_solicitacao_regulacao/create_solicitacao_regulacao_flow.resource
Resource    ../get_solicitacao_regulacao_by_id/get_solicitacao_regulacao_by_id_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Negar exige motivo; quem solicitou não nega a própria.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
POST Negar Solicitacao Regulacao - Status Code 200
    [Documentation]    Denies with a reason.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${matricula}    ${procedimentoId}    ${pacienteId}    ${unidadeId}=    Seed A Solicitacao Regulacao
    ${regulador}=    Seed A Profissional Matricula
    ${payload}=    Create Dictionary    profissionalMatricula=${regulador}    motivo=Critério de encaminhamento não atendido
    ${response}=    POST Negar Solicitacao Regulacao    ${uuid}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200
    ${lida}=    GET Get Solicitacao Regulacao By Id    ${uuid}
    Should Be Equal    ${lida.json()}[status]    NEGADA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Negar Solicitacao Regulacao - Status Code 422 - Proprio Solicitante
    [Documentation]    The requester tries to deny, expecting HTTP 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${matricula}    ${procedimentoId}    ${pacienteId}    ${unidadeId}=    Seed A Solicitacao Regulacao
    ${payload}=    Create Dictionary    profissionalMatricula=${matricula}    motivo=Não indicado
    ${response}=    POST Negar Solicitacao Regulacao    ${uuid}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Solicitacao Regulacao - Negativa - POST - By HTTP Status Code
    [Documentation]    Executes the Solicitacao Regulacao - Negativa - POST scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    POST Negar Solicitacao Regulacao - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '422'    POST Negar Solicitacao Regulacao - Status Code 422 - Proprio Solicitante
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Solicitacao Regulacao - Negativa - POST test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
