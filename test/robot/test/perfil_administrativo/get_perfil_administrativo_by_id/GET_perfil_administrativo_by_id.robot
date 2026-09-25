*** Settings ***
Resource    ../../../src/scenario/perfil_administrativo/get_perfil_administrativo_by_id/get_perfil_administrativo_by_id_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - GET Perfil Administrativo By Id
Metadata    Test Suite Description        This test suite validates the GET Perfil Administrativo by id endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               GET    GetPerfilAdministrativoById    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-23
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate GET Get Perfil Administrativo By Id - HTTP 200 OK
    [Documentation]    Test case to validate the GET Perfil Administrativo by id endpoint with HTTP 200 OK response.
    [Tags]    GET    GetPerfilAdministrativoById    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PERFIL ADMINISTRATIVO - GET BY ID - GET    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate GET Get Perfil Administrativo By Id - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate the GET Perfil Administrativo by id endpoint with HTTP 404 NOT FOUND
    ...    response.
    [Tags]    GET    GetPerfilAdministrativoById    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PERFIL ADMINISTRATIVO - GET BY ID - GET    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
