*** Settings ***
Resource    ../../../pom/evolucao_enfermagem/get_evolucao_enfermagem_by_id/get_evolucao_enfermagem_by_id_pom.resource
Resource    ../create_evolucao_enfermagem/create_evolucao_enfermagem_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Call GET Get Evolucao Enfermagem By Id
    [Documentation]    Calls the GET Get Evolucao Enfermagem By Id keyword from the POM.
    [Arguments]    ${uuid}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Get Evolucao Enfermagem By Id    ${uuid}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Get Evolucao Enfermagem By Id - Status Code 200
    [Documentation]    Seeds a fresh EvolucaoEnfermagem (unless ${uuid} is informed) and fetches it by id,
    ...    expecting HTTP 200.
    [Arguments]    ${uuid}=${EMPTY}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${uuid}' == '${EMPTY}'
        ${uuid}    ${atendimentoId}    ${matricula}=    Seed Uma Evolucao Enfermagem
    END
    ${response}=    Call GET Get Evolucao Enfermagem By Id    ${uuid}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${EVOLUCAO_ENFERMAGEM_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Get Evolucao Enfermagem By Id - Status Code 404
    [Documentation]    Fetches an EvolucaoEnfermagem by a random, non-existent uuid, expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuidInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${response}=    Call GET Get Evolucao Enfermagem By Id    ${uuidInexistente}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Evolucao Enfermagem - Get By Id - GET - By HTTP Status Code
    [Documentation]    Executes the appropriate Get Evolucao Enfermagem By Id test scenario based on the
    ...    provided HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    GET Get Evolucao Enfermagem By Id - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    GET Get Evolucao Enfermagem By Id - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Get Evolucao Enfermagem By Id test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
