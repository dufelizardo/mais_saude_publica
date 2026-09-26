*** Settings ***
Resource    ../../../src/scenario/lote/update_lote/update_lote_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - PATCH Update Lote
Metadata    Test Suite Description        This test suite validates the PATCH update Lote endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               PATCH    UpdateLote    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-26
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate PATCH Update Lote - HTTP 200 OK
    [Documentation]    Test case to validate the PATCH update Lote endpoint with HTTP 200 OK response.
    [Tags]    PATCH    UpdateLote    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    LOTE - UPDATE - PATCH    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate PATCH Update Lote - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate the PATCH update Lote endpoint with HTTP 404 NOT FOUND
    ...    response.
    [Tags]    PATCH    UpdateLote    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    LOTE - UPDATE - PATCH    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
