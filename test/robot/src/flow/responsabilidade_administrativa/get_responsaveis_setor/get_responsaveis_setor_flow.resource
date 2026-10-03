*** Settings ***
Resource    ../../../pom/responsabilidade_administrativa/get_responsaveis_setor/get_responsaveis_setor_pom.resource
Resource    ../create_responsabilidade_administrativa/create_responsabilidade_administrativa_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Responsáveis de um setor (ADR-0083): vigentes primeiro; setor inexistente ou sem responsabilidade → 404.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
GET Responsaveis Do Setor - Status Code 200
    [Documentation]    Seeds a responsibility and lists the setor, expecting HTTP 200 with it.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${matricula}    ${setorId}=    Seed A Responsabilidade Administrativa
    ${response}=    GET Responsaveis Do Setor    ${setorId}
    Should Be Equal As Integers    ${response.status_code}    200
    Should Be Equal As Strings    ${response.json()}[0][setorUuid]    ${setorId}
    Should Be Equal As Strings    ${response.json()}[0][profissionalMatricula]    ${matricula}
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Responsaveis Do Setor - Status Code 404
    [Documentation]    Lists a random, non-existent setor, expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${setorId}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${response}=    GET Responsaveis Do Setor    ${setorId}
    Should Be Equal As Integers    ${response.status_code}    404
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Responsabilidade Administrativa - Responsaveis Do Setor - GET - By HTTP Status Code
    [Documentation]    Executes the scenario for the status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    GET Responsaveis Do Setor - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    GET Responsaveis Do Setor - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Responsaveis Do Setor test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
