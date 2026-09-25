*** Settings ***
Resource    ../../../src/scenario/procedimento/update_procedimento/update_procedimento_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - PATCH Update Procedimento
Metadata    Test Suite Description        This test suite validates the PATCH update Procedimento endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               PATCH    UpdateProcedimento    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-25
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate PATCH Update Procedimento - HTTP 200 OK
    [Documentation]    Test case to validate the PATCH update Procedimento endpoint with HTTP 200 OK
    ...    response — covers advancing status (AGENDADO → REALIZADO).
    [Tags]    PATCH    UpdateProcedimento    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROCEDIMENTO - UPDATE - PATCH    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate PATCH Update Procedimento - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate the PATCH update Procedimento endpoint with HTTP 404 NOT FOUND
    ...    response.
    [Tags]    PATCH    UpdateProcedimento    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROCEDIMENTO - UPDATE - PATCH    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
