*** Settings ***
Resource    ../../../pom/profissional/get_profissional_by_matricula/get_profissional_by_matricula_pom.resource
Resource    ../create_profissional/create_profissional_flow.resource
Resource    ../get_profissional_by_cpf/get_profissional_by_cpf_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Busca por matrícula (chave única de verdade do domínio, ver ADR-0017) — usada por domínios fora
    do RH que referenciam Profissional só por matrícula, nunca por uuid (ProfissionalResponseDto não
    expõe uuid — ver ADR-0034), como o Setor Administrativo. "Seed A Profissional Matricula" é o
    ponto único de resolução de matrícula reaproveitado por esses domínios (ver
    create_setor_flow.resource).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Call GET Get Profissional By Matricula
    [Documentation]    Calls the GET Get Profissional By Matricula keyword from the POM.
    [Arguments]    ${matricula}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Get Profissional By Matricula    ${matricula}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed A Profissional Matricula
    [Documentation]    Seeds a fresh Profissional and returns its auto-generated matrícula, resolved via GET by
    ...    cpf (the create endpoint only returns a cpf-bearing SuccessResponseDto, not the matricula).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${cpf}=    Seed A Profissional Unit
    ${response}=    Call GET Get Profissional By Cpf    ${cpf}
    ${matricula}=    Set Variable    ${response.json()['matricula']}

    RETURN    ${matricula}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Get Profissional By Matricula - Status Code 200
    [Documentation]    Seeds a fresh Profissional (unless ${matricula} is informed) and fetches it by matrícula,
    ...    expecting HTTP 200.
    [Arguments]    ${matricula}=${EMPTY}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${matricula}' == '${EMPTY}'
        ${matricula}=    Seed A Profissional Matricula
    END
    ${response}=    Call GET Get Profissional By Matricula    ${matricula}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${PROFISSIONAL_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Get Profissional By Matricula - Status Code 404
    [Documentation]    Fetches a Profissional by a matrícula that doesn't exist, expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    Call GET Get Profissional By Matricula    00000000000000-00
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Profissional - Get By Matricula - GET - By HTTP Status Code
    [Documentation]    Executes the appropriate Get Profissional By Matricula test scenario based on the
    ...    provided HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    GET Get Profissional By Matricula - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    GET Get Profissional By Matricula - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Get Profissional By Matricula test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
