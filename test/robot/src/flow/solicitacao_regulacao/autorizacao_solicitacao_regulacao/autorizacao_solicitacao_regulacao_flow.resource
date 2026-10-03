*** Settings ***
Library     String
Library     Collections
Resource    ../../../pom/solicitacao_regulacao/autorizacao_solicitacao_regulacao/autorizacao_solicitacao_regulacao_pom.resource
Resource    ../create_solicitacao_regulacao/create_solicitacao_regulacao_flow.resource
Resource    ../get_solicitacao_regulacao_by_id/get_solicitacao_regulacao_by_id_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Autorizar registra a vaga; quem solicitou não autoriza a própria.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
POST Autorizar Solicitacao Regulacao - Status Code 200
    [Documentation]    Authorizes with unit and date by another professional.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${matricula}    ${procedimentoId}    ${pacienteId}    ${unidadeId}=    Seed A Solicitacao Regulacao
    ${regulador}=    Seed A Profissional Matricula
    ${unidadeExecutante}=    Seed A Unidade De Saude Uuid
    ${quando}=    Evaluate    (datetime.datetime.now() + datetime.timedelta(days=7)).replace(hour=9, minute=30, second=0, microsecond=0).isoformat()    modules=datetime
    ${payload}=    Create Dictionary    profissionalMatricula=${regulador}    unidadeExecutanteId=${unidadeExecutante}    dataHoraPrevista=${quando}
    ${response}=    POST Autorizar Solicitacao Regulacao    ${uuid}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200
    ${lida}=    GET Get Solicitacao Regulacao By Id    ${uuid}
    Should Be Equal    ${lida.json()}[status]    AUTORIZADA
    Should Be Equal    ${lida.json()}[unidadeExecutanteId]    ${unidadeExecutante}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Autorizar Solicitacao Regulacao - Status Code 422 - Proprio Solicitante
    [Documentation]    The requester tries to authorize, expecting HTTP 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${matricula}    ${procedimentoId}    ${pacienteId}    ${unidadeId}=    Seed A Solicitacao Regulacao
    ${unidadeExecutante}=    Seed A Unidade De Saude Uuid
    ${quando}=    Evaluate    (datetime.datetime.now() + datetime.timedelta(days=7)).replace(hour=9, minute=30, second=0, microsecond=0).isoformat()    modules=datetime
    ${payload}=    Create Dictionary    profissionalMatricula=${matricula}    unidadeExecutanteId=${unidadeExecutante}    dataHoraPrevista=${quando}
    ${response}=    POST Autorizar Solicitacao Regulacao    ${uuid}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Solicitacao Regulacao - Autorizacao - POST - By HTTP Status Code
    [Documentation]    Executes the Solicitacao Regulacao - Autorizacao - POST scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    POST Autorizar Solicitacao Regulacao - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '422'    POST Autorizar Solicitacao Regulacao - Status Code 422 - Proprio Solicitante
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Solicitacao Regulacao - Autorizacao - POST test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
