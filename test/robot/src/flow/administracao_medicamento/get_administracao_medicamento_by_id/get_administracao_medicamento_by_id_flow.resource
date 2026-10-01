*** Settings ***
Resource    ../create_administracao_medicamento/create_administracao_medicamento_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
GET Get Administracao Medicamento By Id - Status Code 200
    [Documentation]    Administers a fresh prescription and fetches the AdministracaoMedicamento by id, expecting HTTP 200.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atendimentoId}    ${consultaId}    ${medicamentoId}    ${loteId}    ${matricula}=    Seed Uma Prescricao Com Estoque
    ${payload}=    Build Administracao Payload    ${atendimentoId}    ${consultaId}    ${medicamentoId}    ${loteId}    ${matricula}
    ${criada}=    POST Create Administracao Medicamento    ${payload}
    Should Be Equal As Integers    ${criada.status_code}    201
    ${uuid}=    Buscar Administracao Uuid Por Atendimento    ${atendimentoId}

    ${response}=    GET Get Administracao Medicamento By Id    ${uuid}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${ADMINISTRACAO_MEDICAMENTO_SCHEMA}
    Should Be Equal    ${response.json()}[situacao]    ADMINISTRADO
    Should Be Equal As Strings    ${response.json()}[loteUuid]    ${loteId}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Get Administracao Medicamento By Id - Status Code 404
    [Documentation]    Fetches an AdministracaoMedicamento by a random, non-existent uuid, expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuidInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${response}=    GET Get Administracao Medicamento By Id    ${uuidInexistente}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Administracao Medicamento - Get By Id - GET - By HTTP Status Code
    [Documentation]    Executes the appropriate Get Administracao Medicamento By Id test scenario based on the provided HTTP
    ...    status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    GET Get Administracao Medicamento By Id - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    GET Get Administracao Medicamento By Id - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Get Administracao Medicamento By Id test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

