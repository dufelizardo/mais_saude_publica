*** Settings ***
Resource    ../../../pom/perfil_por_tipo_unidade/get_perfil_por_tipo_unidade_by_tipo/get_perfil_por_tipo_unidade_by_tipo_pom.resource
Resource    ../create_perfil_por_tipo_unidade/create_perfil_por_tipo_unidade_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    UPA é reservado exclusivamente para o cenário 404 ("tipo não configurado") — nenhum outro
    keyword desta suíte cria associação pra UPA, então ele fica seguro em qualquer reexecução.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Call GET Get Perfil Por Tipo Unidade By Tipo
    [Documentation]    Calls the GET Get Perfil Por Tipo Unidade By Tipo keyword from the POM.
    [Arguments]    ${tipo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Get Perfil Por Tipo Unidade By Tipo    ${tipo}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Get Perfil Por Tipo Unidade By Tipo - Status Code 200
    [Documentation]    Ensures CENTRO_ESPECIALIDADES has an association (seeding it if necessary) and fetches it
    ...    by tipo, expecting HTTP 200.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Seed A Perfil Por Tipo Unidade    CENTRO_ESPECIALIDADES
    ${response}=    Call GET Get Perfil Por Tipo Unidade By Tipo    CENTRO_ESPECIALIDADES
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${PERFIL_POR_TIPO_UNIDADE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Get Perfil Por Tipo Unidade By Tipo - Status Code 404
    [Documentation]    Fetches UPA (never configured by this suite), expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    Call GET Get Perfil Por Tipo Unidade By Tipo    UPA
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Perfil Por Tipo Unidade - Get By Tipo - GET - By HTTP Status Code
    [Documentation]    Executes the appropriate Get Perfil Por Tipo Unidade By Tipo test scenario based on the
    ...    provided HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    GET Get Perfil Por Tipo Unidade By Tipo - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    GET Get Perfil Por Tipo Unidade By Tipo - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Get Perfil Por Tipo Unidade By Tipo test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
