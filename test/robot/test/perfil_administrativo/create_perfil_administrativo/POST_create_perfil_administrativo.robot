*** Settings ***
Resource    ../../../src/scenario/perfil_administrativo/create_perfil_administrativo/create_perfil_administrativo_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST Create Perfil Administrativo
Metadata    Test Suite Description        This test suite validates the POST create Perfil Administrativo endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    CreatePerfilAdministrativo    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-23
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST Create Perfil Administrativo - HTTP 201 CREATED
    [Documentation]    Test case to validate the POST create Perfil Administrativo endpoint with HTTP 201 CREATED
    ...    response.
    [Tags]    POST    CreatePerfilAdministrativo    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PERFIL ADMINISTRATIVO - CREATE - POST    201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST Create Perfil Administrativo - HTTP 409 CONFLICT
    [Documentation]    Test case to validate the POST create Perfil Administrativo endpoint with HTTP 409 CONFLICT
    ...    response, when codigo is already registered.
    [Tags]    POST    CreatePerfilAdministrativo    HTTP409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PERFIL ADMINISTRATIVO - CREATE - POST    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate POST Create Perfil Administrativo - HTTP 400 BAD REQUEST (Campos Obrigatorios Em Branco)
    [Documentation]    Test case to validate the POST create Perfil Administrativo endpoint with HTTP 400 BAD
    ...    REQUEST response, when required fields (codigo, nome, ativo) are left blank/omitted.
    [Tags]    POST    CreatePerfilAdministrativo    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PERFIL ADMINISTRATIVO - CREATE - POST    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
