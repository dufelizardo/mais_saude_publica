*** Settings ***
Resource    ../../../src/scenario/atendimento/update_atendimento/update_atendimento_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - PATCH Update Atendimento
Metadata    Test Suite Description        This test suite validates the PATCH update Atendimento endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               PATCH    UpdateAtendimento    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-25
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate PATCH Update Atendimento - HTTP 200 OK
    [Documentation]    Test case to validate the PATCH update Atendimento endpoint with HTTP 200 OK response —
    ...    covers advancing status (AGENDADO → CONCLUIDO).
    [Tags]    PATCH    UpdateAtendimento    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ATENDIMENTO - UPDATE - PATCH    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate PATCH Update Atendimento - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate the PATCH update Atendimento endpoint with HTTP 404 NOT FOUND
    ...    response.
    [Tags]    PATCH    UpdateAtendimento    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ATENDIMENTO - UPDATE - PATCH    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
