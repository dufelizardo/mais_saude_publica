*** Settings ***
Library     String
Library     Collections
Resource    ../../../pom/solicitacao_regulacao/realizacao_solicitacao_regulacao/realizacao_solicitacao_regulacao_pom.resource
Resource    ../create_solicitacao_regulacao/create_solicitacao_regulacao_flow.resource
Resource    ../get_solicitacao_regulacao_by_id/get_solicitacao_regulacao_by_id_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Atendido na executante, com a contrarreferência para a origem (ADR-0089).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
POST Realizar Solicitacao Regulacao - Status Code 200
    [Documentation]    Authorizes with the professional and records the counter-referral.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${matricula}    ${procedimentoId}    ${pacienteId}    ${unidadeId}=    Seed A Solicitacao Regulacao
    ${regulador}=    Seed A Profissional Matricula
    ${executante}=    Seed A Profissional Matricula
    ${unidadeExecutante}=    Seed A Unidade De Saude Uuid
    ${quando}=    Evaluate    (datetime.datetime.now() + datetime.timedelta(days=7)).replace(hour=9, minute=30, second=0, microsecond=0).isoformat()    modules=datetime
    ${autorizacao}=    Create Dictionary    profissionalMatricula=${regulador}    unidadeExecutanteId=${unidadeExecutante}    dataHoraPrevista=${quando}    profissionalExecutanteMatricula=${executante}
    ${autorizada}=    POST Autorizar Solicitacao Regulacao    ${uuid}    ${autorizacao}
    Should Be Equal As Integers    ${autorizada.status_code}    200
    ${payload}=    Create Dictionary    profissionalMatricula=${executante}    contrarreferencia=Ecocardiograma normal. Manter seguimento na UBS.
    ${response}=    POST Realizar Solicitacao Regulacao    ${uuid}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200
    ${lida}=    GET Get Solicitacao Regulacao By Id    ${uuid}
    Should Be Equal    ${lida.json()}[status]    REALIZADA
    Should Be Equal    ${lida.json()}[contrarreferencia]    Ecocardiograma normal. Manter seguimento na UBS.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Realizar Solicitacao Regulacao - Status Code 422 - Nao Agendada
    [Documentation]    Records the outcome of a request not yet scheduled, expecting HTTP 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${matricula}    ${procedimentoId}    ${pacienteId}    ${unidadeId}=    Seed A Solicitacao Regulacao
    ${payload}=    Create Dictionary    profissionalMatricula=${matricula}    contrarreferencia=Sem agendamento ainda.
    ${response}=    POST Realizar Solicitacao Regulacao    ${uuid}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Solicitacao Regulacao - Realizacao - POST - By HTTP Status Code
    [Documentation]    Executes the Solicitacao Regulacao - Realizacao - POST scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    POST Realizar Solicitacao Regulacao - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '422'    POST Realizar Solicitacao Regulacao - Status Code 422 - Nao Agendada
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Solicitacao Regulacao - Realizacao - POST test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
