*** Settings ***
Resource    ../../../src/scenario/perfil_por_tipo_unidade/create_perfil_por_tipo_unidade/create_perfil_por_tipo_unidade_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST Create Perfil Por Tipo Unidade
Metadata    Test Suite Description        This test suite validates the POST create Perfil Por Tipo Unidade endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    CreatePerfilPorTipoUnidade    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-23
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST Create Perfil Por Tipo Unidade - HTTP 201 CREATED
    [Documentation]    Test case to validate the POST create Perfil Por Tipo Unidade endpoint with HTTP 201
    ...    CREATED response.
    [Tags]    POST    CreatePerfilPorTipoUnidade    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PERFIL POR TIPO UNIDADE - CREATE - POST    201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST Create Perfil Por Tipo Unidade - HTTP 404 NOT FOUND (Perfil Inexistente)
    [Documentation]    Test case to validate the POST create Perfil Por Tipo Unidade endpoint with HTTP 404 NOT
    ...    FOUND response, when perfilAdministrativoId points at a Perfil Administrativo that doesn't exist.
    [Tags]    POST    CreatePerfilPorTipoUnidade    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PERFIL POR TIPO UNIDADE - CREATE - POST    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate POST Create Perfil Por Tipo Unidade - HTTP 409 CONFLICT
    [Documentation]    Test case to validate the POST create Perfil Por Tipo Unidade endpoint with HTTP 409
    ...    CONFLICT response, when tipo already has an association.
    [Tags]    POST    CreatePerfilPorTipoUnidade    HTTP409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PERFIL POR TIPO UNIDADE - CREATE - POST    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
