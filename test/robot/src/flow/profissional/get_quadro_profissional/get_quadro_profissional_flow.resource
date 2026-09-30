*** Settings ***
Resource    ../../../pom/profissional/get_quadro_profissional/get_quadro_profissional_pom.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Quadro de profissionais (ADR-0072). Lista vazia responde 404 (convenção das listagens); com profissionais, cada item
    traz o profissional e, quando houver, a lotação vigente e o afastamento em curso.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
GET Quadro Profissional - Status Code 200
    [Documentation]    Lists the staff board, expecting HTTP 200 with valid items (or 404 when the environment has no profissional).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Quadro Profissional
    Should Be True    ${response.status_code} in (200, 404)
    IF    ${response.status_code} == 200
        Validate Array Schema    ${response.json()}    ${QUADRO_PROFISSIONAL_SCHEMA}
        Validate Object Schema    ${response.json()}[0][profissional]    ${PROFISSIONAL_SCHEMA}
    END

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Profissional - Quadro - GET - By HTTP Status Code
    [Documentation]    Executes the appropriate Quadro Profissional test scenario based on the provided HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    GET Quadro Profissional - Status Code 200
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Quadro Profissional test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

