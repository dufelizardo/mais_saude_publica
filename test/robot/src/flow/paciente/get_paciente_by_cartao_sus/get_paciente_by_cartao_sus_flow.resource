*** Settings ***
Resource    ../../../pom/paciente/get_paciente_by_cartao_sus/get_paciente_by_cartao_sus_pom.resource
Resource    ../create_paciente/create_paciente_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Call GET Get Paciente By Cartao Sus
    [Documentation]    Calls the GET Get Paciente By Cartao Sus keyword from the POM.
    [Arguments]    ${cartaoSus}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Get Paciente By Cartao Sus    ${cartaoSus}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Get Paciente By Cartao Sus - Status Code 200
    [Documentation]    Seeds a fresh Paciente (com CNS) e fetches it by cartaoSus, expecting HTTP 200 with a
    ...    one-item array.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${cpf}    ${cartaoSus}    ${nome}=    Seed A Paciente
    ${response}=    Call GET Get Paciente By Cartao Sus    ${cartaoSus}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Array Schema    ${response.json()}    ${PACIENTE_SCHEMA}
    Should Be Equal As Strings    ${response.json()[0]['nome']}    ${nome}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Get Paciente By Cartao Sus - Status Code 404
    [Documentation]    Fetches a Paciente by a random, non-existent cartaoSus, expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${suffix}=    Generate Random String    15    [NUMBERS]
    ${response}=    Call GET Get Paciente By Cartao Sus    ${suffix}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Paciente - Get By Cartao Sus - GET - By HTTP Status Code
    [Documentation]    Executes the appropriate Get Paciente By Cartao Sus test scenario based on the provided
    ...    HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    GET Get Paciente By Cartao Sus - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    GET Get Paciente By Cartao Sus - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Get Paciente By Cartao Sus test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
