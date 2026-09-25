*** Settings ***
Resource    ../../../src/scenario/perfil_administrativo/update_perfil_administrativo/update_perfil_administrativo_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - PATCH Update Perfil Administrativo
Metadata    Test Suite Description        This test suite validates the PATCH update Perfil Administrativo endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               PATCH    UpdatePerfilAdministrativo    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-23
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate PATCH Update Perfil Administrativo - HTTP 200 OK (Edita Campos E Desativa)
    [Documentation]    Test case to validate the PATCH update Perfil Administrativo endpoint with HTTP 200 OK
    ...    response, editing nome and setting ativo=False.
    [Tags]    PATCH    UpdatePerfilAdministrativo    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PERFIL ADMINISTRATIVO - UPDATE - PATCH    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate PATCH Update Perfil Administrativo - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate the PATCH update Perfil Administrativo endpoint with HTTP 404 NOT
    ...    FOUND response, when the uuid in the path doesn't exist.
    [Tags]    PATCH    UpdatePerfilAdministrativo    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PERFIL ADMINISTRATIVO - UPDATE - PATCH    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
