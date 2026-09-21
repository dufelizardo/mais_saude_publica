*** Settings ***
Resource    ../../../src/scenario/setor/update_setor/update_setor_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - PATCH Update Setor
Metadata    Test Suite Description        This test suite validates the PATCH update Setor endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               PATCH    UpdateSetor    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-21
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate PATCH Update Setor - HTTP 200 OK (Edita Campos E Desativa)
    [Documentation]    Test case to validate the PATCH update Setor endpoint with HTTP 200 OK response, editing
    ...    nome and setting ativo=False (there's no dedicated des-habilitar endpoint for Setor — see ADR-0030).
    [Tags]    PATCH    UpdateSetor    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SETOR - UPDATE - PATCH    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate PATCH Update Setor - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate the PATCH update Setor endpoint with HTTP 404 NOT FOUND response,
    ...    when the uuid in the path doesn't exist.
    [Tags]    PATCH    UpdateSetor    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SETOR - UPDATE - PATCH    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
