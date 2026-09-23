*** Settings ***
Resource    ../../../pom/perfil_por_tipo_unidade/create_perfil_por_tipo_unidade/create_perfil_por_tipo_unidade_pom.resource
Resource    ../../../pom/perfil_por_tipo_unidade/get_perfil_por_tipo_unidade_by_tipo/get_perfil_por_tipo_unidade_by_tipo_pom.resource
Resource    ../../perfil_administrativo/create_perfil_administrativo/create_perfil_administrativo_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Cada TipoUnidadeDeSaude só pode ter uma associação (constraint de unicidade em `tipo`) e não há
    endpoint de exclusão — por isso "Seed A Perfil Por Tipo Unidade" é idempotente (reaproveita a
    associação se ela já existir) para tornar os cenários de GET/UPDATE seguros em reexecuções
    locais. O cenário de "create 201" usa um tipo dedicado (POLICLINICA) que nenhum outro teste
    toca, e só é garantido em banco novo (como o Postgres efêmero do CI, ver ADR-0010).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Build Perfil Por Tipo Unidade Payload
    [Documentation]    Builds a valid PerfilPorTipoUnidadeRequestDto payload.
    [Arguments]    ${tipo}    ${perfilAdministrativoId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary
    ...    tipo=${tipo}
    ...    perfilAdministrativoId=${perfilAdministrativoId}

    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed A Perfil Por Tipo Unidade
    [Documentation]    Ensures a Perfil Por Tipo Unidade association exists for ${tipo}, reusing it if a previous
    ...    run already created one (idempotent — see comments above). Returns the association's uuid and its
    ...    current perfilAdministrativoUuid.
    [Arguments]    ${tipo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${existente}=    GET Get Perfil Por Tipo Unidade By Tipo    ${tipo}
    IF    ${existente.status_code} == 200
        RETURN    ${existente.json()['uuid']}    ${existente.json()['perfilAdministrativoUuid']}
    END

    ${perfilId}    ${codigo}=    Seed A Perfil Administrativo
    ${payload}=    Build Perfil Por Tipo Unidade Payload    ${tipo}    ${perfilId}
    ${response}=    POST Create Perfil Por Tipo Unidade    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    ${criado}=    GET Get Perfil Por Tipo Unidade By Tipo    ${tipo}
    Should Be Equal As Integers    ${criado.status_code}    200

    RETURN    ${criado.json()['uuid']}    ${perfilId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Perfil Por Tipo Unidade - Status Code 201
    [Documentation]    Creates a fresh Perfil Administrativo and associates it to POLICLINICA (dedicated tipo,
    ...    only guaranteed on a fresh database), expecting HTTP 201.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${perfilId}    ${codigo}=    Seed A Perfil Administrativo
    ${payload}=    Build Perfil Por Tipo Unidade Payload    POLICLINICA    ${perfilId}

    ${response}=    POST Create Perfil Por Tipo Unidade    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Perfil Por Tipo Unidade - Status Code 404 - Perfil Inexistente
    [Documentation]    Attempts to associate a non-existent Perfil Administrativo id, expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${perfilIdInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${payload}=    Build Perfil Por Tipo Unidade Payload    CENTRO_REABILITACAO    ${perfilIdInexistente}

    ${response}=    POST Create Perfil Por Tipo Unidade    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Perfil Por Tipo Unidade - Status Code 409
    [Documentation]    Ensures CENTRO_REABILITACAO already has an association, then attempts to create another
    ...    one for the same tipo, expecting a duplicate-tipo conflict (HTTP 409).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Seed A Perfil Por Tipo Unidade    CENTRO_REABILITACAO
    ${perfilId}    ${codigo}=    Seed A Perfil Administrativo
    ${payload}=    Build Perfil Por Tipo Unidade Payload    CENTRO_REABILITACAO    ${perfilId}

    ${response}=    POST Create Perfil Por Tipo Unidade    ${payload}
    Should Be Equal As Integers    ${response.status_code}    409
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Perfil Por Tipo Unidade - Create - POST - By HTTP Status Code
    [Documentation]    Executes the appropriate Create Perfil Por Tipo Unidade test scenario based on the
    ...    provided HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201'    POST Create Perfil Por Tipo Unidade - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '404'    POST Create Perfil Por Tipo Unidade - Status Code 404 - Perfil Inexistente
    ...    ELSE IF    '${meuHTTP}' == '409'    POST Create Perfil Por Tipo Unidade - Status Code 409
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Create Perfil Por Tipo Unidade test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
