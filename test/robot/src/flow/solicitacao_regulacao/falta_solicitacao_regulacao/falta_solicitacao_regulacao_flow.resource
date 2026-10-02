*** Settings ***
Library     String
Library     Collections
Resource    ../../../pom/solicitacao_regulacao/falta_solicitacao_regulacao/falta_solicitacao_regulacao_pom.resource
Resource    ../create_solicitacao_regulacao/create_solicitacao_regulacao_flow.resource
Resource    ../get_solicitacao_regulacao_by_id/get_solicitacao_regulacao_by_id_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    O paciente não compareceu ao agendamento (ADR-0089).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
POST Registrar Falta Solicitacao Regulacao - Status Code 200
    [Documentation]    Authorizes with the professional and records the absence.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${matricula}    ${procedimentoId}    ${pacienteId}    ${unidadeId}=    Seed A Solicitacao Regulacao
    ${regulador}=    Seed A Profissional Matricula
    ${executante}=    Seed A Profissional Matricula
    ${unidadeExecutante}=    Seed A Unidade De Saude Uuid
    ${quando}=    Evaluate    (datetime.datetime.now() + datetime.timedelta(days=7)).replace(hour=9, minute=30, second=0, microsecond=0).isoformat()    modules=datetime
    ${autorizacao}=    Create Dictionary    profissionalMatricula=${regulador}    unidadeExecutanteId=${unidadeExecutante}    dataHoraPrevista=${quando}    profissionalExecutanteMatricula=${executante}
    ${autorizada}=    POST Autorizar Solicitacao Regulacao    ${uuid}    ${autorizacao}
    Should Be Equal As Integers    ${autorizada.status_code}    200
    ${payload}=    Create Dictionary    profissionalMatricula=${executante}    motivo=Não compareceu
    ${response}=    POST Registrar Falta Solicitacao Regulacao    ${uuid}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200
    ${lida}=    GET Get Solicitacao Regulacao By Id    ${uuid}
    Should Be Equal    ${lida.json()}[status]    FALTOU
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Registrar Falta Solicitacao Regulacao - Status Code 422 - Nao Agendada
    [Documentation]    Records an absence for a request in the queue, expecting HTTP 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${matricula}    ${procedimentoId}    ${pacienteId}    ${unidadeId}=    Seed A Solicitacao Regulacao
    ${payload}=    Create Dictionary    profissionalMatricula=${matricula}    motivo=Não compareceu
    ${response}=    POST Registrar Falta Solicitacao Regulacao    ${uuid}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Solicitacao Regulacao - Falta - POST - By HTTP Status Code
    [Documentation]    Executes the Solicitacao Regulacao - Falta - POST scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    POST Registrar Falta Solicitacao Regulacao - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '422'    POST Registrar Falta Solicitacao Regulacao - Status Code 422 - Nao Agendada
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Solicitacao Regulacao - Falta - POST test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
