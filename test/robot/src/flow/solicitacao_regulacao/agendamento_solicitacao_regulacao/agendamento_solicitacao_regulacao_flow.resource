*** Settings ***
Library     String
Library     Collections
Resource    ../../../pom/solicitacao_regulacao/agendamento_solicitacao_regulacao/agendamento_solicitacao_regulacao_pom.resource
Resource    ../create_solicitacao_regulacao/create_solicitacao_regulacao_flow.resource
Resource    ../get_solicitacao_regulacao_by_id/get_solicitacao_regulacao_by_id_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    A executante agenda a autorizada com o profissional que vai atender (ADR-0089).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
POST Agendar Solicitacao Regulacao - Status Code 200
    [Documentation]    Authorizes without the professional and schedules afterwards.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${matricula}    ${procedimentoId}    ${pacienteId}    ${unidadeId}=    Seed A Solicitacao Regulacao
    ${regulador}=    Seed A Profissional Matricula
    ${executante}=    Seed A Profissional Matricula
    ${unidadeExecutante}=    Seed A Unidade De Saude Uuid
    ${quando}=    Evaluate    (datetime.datetime.now() + datetime.timedelta(days=7)).replace(hour=9, minute=30, second=0, microsecond=0).isoformat()    modules=datetime
    ${autorizacao}=    Create Dictionary    profissionalMatricula=${regulador}    unidadeExecutanteId=${unidadeExecutante}    dataHoraPrevista=${quando}
    ${autorizada}=    POST Autorizar Solicitacao Regulacao    ${uuid}    ${autorizacao}
    Should Be Equal As Integers    ${autorizada.status_code}    200
    ${payload}=    Create Dictionary    profissionalMatricula=${regulador}    profissionalExecutanteMatricula=${executante}
    ${response}=    POST Agendar Solicitacao Regulacao    ${uuid}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${SUCCESS_RESPONSE_SCHEMA}
    ${lida}=    GET Get Solicitacao Regulacao By Id    ${uuid}
    Should Be Equal    ${lida.json()}[status]    AGENDADA
    Should Be Equal    ${lida.json()}[profissionalExecutanteMatricula]    ${executante}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Agendar Solicitacao Regulacao - Status Code 422 - Nao Autorizada
    [Documentation]    Schedules a request still in the queue, expecting HTTP 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${matricula}    ${procedimentoId}    ${pacienteId}    ${unidadeId}=    Seed A Solicitacao Regulacao
    ${executante}=    Seed A Profissional Matricula
    ${payload}=    Create Dictionary    profissionalMatricula=${matricula}    profissionalExecutanteMatricula=${executante}
    ${response}=    POST Agendar Solicitacao Regulacao    ${uuid}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Solicitacao Regulacao - Agendamento - POST - By HTTP Status Code
    [Documentation]    Executes the Solicitacao Regulacao - Agendamento - POST scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    POST Agendar Solicitacao Regulacao - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '422'    POST Agendar Solicitacao Regulacao - Status Code 422 - Nao Autorizada
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Solicitacao Regulacao - Agendamento - POST test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
