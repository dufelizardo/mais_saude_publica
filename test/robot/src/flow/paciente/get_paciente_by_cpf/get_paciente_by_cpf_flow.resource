*** Settings ***
Resource    ../../../pom/paciente/get_paciente_by_cpf/get_paciente_by_cpf_pom.resource
Resource    ../create_paciente/create_paciente_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    CPF não é único (ver ADR-0017/DER.md), então este endpoint sempre retorna uma lista — mesmo quando só
    existe uma ficha com o CPF informado.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Call GET Get Paciente By Cpf
    [Documentation]    Calls the GET Get Paciente By Cpf keyword from the POM.
    [Arguments]    ${cpf}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Get Paciente By Cpf    ${cpf}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Get Paciente By Cpf - Status Code 200
    [Documentation]    Seeds a fresh Paciente and fetches it by cpf, expecting HTTP 200 with a one-item array.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${cpf}    ${cartaoSus}    ${nome}=    Seed A Paciente
    ${response}=    Call GET Get Paciente By Cpf    ${cpf}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Array Schema    ${response.json()}    ${PACIENTE_SCHEMA}
    Should Be Equal As Strings    ${response.json()[0]['nome']}    ${nome}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Get Paciente By Cpf - Status Code 404
    [Documentation]    Fetches a Paciente by a random, non-existent cpf, expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${suffix}=    Generate Random String    10    [NUMBERS]
    ${response}=    Call GET Get Paciente By Cpf    ${suffix}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Paciente - Get By Cpf - GET - By HTTP Status Code
    [Documentation]    Executes the appropriate Get Paciente By Cpf test scenario based on the provided HTTP
    ...    status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    GET Get Paciente By Cpf - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    GET Get Paciente By Cpf - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Get Paciente By Cpf test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
