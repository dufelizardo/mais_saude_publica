*** Settings ***
Resource    ../../../src/scenario/triagem/update_triagem/update_triagem_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - PATCH Update Triagem
Metadata    Test Suite Description        This test suite validates the PATCH update Triagem endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               PATCH    UpdateTriagem    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-25
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate PATCH Update Triagem - HTTP 200 OK
    [Documentation]    Test case to validate the PATCH update Triagem endpoint with HTTP 200 OK response.
    [Tags]    PATCH    UpdateTriagem    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    TRIAGEM - UPDATE - PATCH    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate PATCH Update Triagem - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate the PATCH update Triagem endpoint with HTTP 404 NOT FOUND
    ...    response.
    [Tags]    PATCH    UpdateTriagem    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    TRIAGEM - UPDATE - PATCH    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
