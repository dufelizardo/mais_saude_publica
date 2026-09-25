*** Settings ***
Resource    ../../../pom/perfil_por_tipo_unidade/update_perfil_por_tipo_unidade/update_perfil_por_tipo_unidade_pom.resource
Resource    ../../../pom/perfil_por_tipo_unidade/get_perfil_por_tipo_unidade_by_tipo/get_perfil_por_tipo_unidade_by_tipo_pom.resource
Resource    ../create_perfil_por_tipo_unidade/create_perfil_por_tipo_unidade_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    PATCH troca qual PerfilAdministrativo vale para um tipo já configurado (ex.: reconfigurar
    LABORATORIO pra outro perfil) — não muda o próprio tipo em si, embora o campo seja aceito.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Call PATCH Update Perfil Por Tipo Unidade
    [Documentation]    Calls the PATCH Update Perfil Por Tipo Unidade keyword from the POM.
    [Arguments]    ${uuid}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    PATCH Update Perfil Por Tipo Unidade    ${uuid}    ${payload}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Perfil Por Tipo Unidade - Status Code 200
    [Documentation]    Ensures LABORATORIO has an association, then reassigns it to a freshly seeded Perfil
    ...    Administrativo, expecting HTTP 200 and the new perfil reflected on a subsequent GET by tipo.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${perfilAntigoId}=    Seed A Perfil Por Tipo Unidade    LABORATORIO
    ${perfilNovoId}    ${codigo}=    Seed A Perfil Administrativo
    ${payload}=    Build Perfil Por Tipo Unidade Payload    LABORATORIO    ${perfilNovoId}

    ${response}=    Call PATCH Update Perfil Por Tipo Unidade    ${uuid}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200

    ${getResponse}=    GET Get Perfil Por Tipo Unidade By Tipo    LABORATORIO
    Should Be Equal As Integers    ${getResponse.status_code}    200
    Should Be Equal As Strings    ${getResponse.json()['perfilAdministrativoUuid']}    ${perfilNovoId}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Perfil Por Tipo Unidade - Status Code 404
    [Documentation]    Attempts to update an association identified by a random, non-existent uuid, expecting
    ...    HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${perfilId}    ${codigo}=    Seed A Perfil Administrativo
    ${payload}=    Build Perfil Por Tipo Unidade Payload    LABORATORIO    ${perfilId}
    ${uuidInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid

    ${response}=    Call PATCH Update Perfil Por Tipo Unidade    ${uuidInexistente}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Perfil Por Tipo Unidade - Update - PATCH - By HTTP Status Code
    [Documentation]    Executes the appropriate Update Perfil Por Tipo Unidade test scenario based on the
    ...    provided HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    PATCH Update Perfil Por Tipo Unidade - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    PATCH Update Perfil Por Tipo Unidade - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Update Perfil Por Tipo Unidade test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
