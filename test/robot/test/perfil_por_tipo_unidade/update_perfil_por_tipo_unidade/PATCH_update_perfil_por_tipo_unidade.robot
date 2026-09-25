*** Settings ***
Resource    ../../../src/scenario/perfil_por_tipo_unidade/update_perfil_por_tipo_unidade/update_perfil_por_tipo_unidade_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - PATCH Update Perfil Por Tipo Unidade
Metadata    Test Suite Description        This test suite validates the PATCH update Perfil Por Tipo Unidade endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               PATCH    UpdatePerfilPorTipoUnidade    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-23
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate PATCH Update Perfil Por Tipo Unidade - HTTP 200 OK (Reatribui Perfil)
    [Documentation]    Test case to validate the PATCH update Perfil Por Tipo Unidade endpoint with HTTP 200 OK
    ...    response, reassigning the Perfil Administrativo of an existing tipo association.
    [Tags]    PATCH    UpdatePerfilPorTipoUnidade    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PERFIL POR TIPO UNIDADE - UPDATE - PATCH    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate PATCH Update Perfil Por Tipo Unidade - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate the PATCH update Perfil Por Tipo Unidade endpoint with HTTP 404 NOT
    ...    FOUND response, when the uuid in the path doesn't exist.
    [Tags]    PATCH    UpdatePerfilPorTipoUnidade    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PERFIL POR TIPO UNIDADE - UPDATE - PATCH    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
