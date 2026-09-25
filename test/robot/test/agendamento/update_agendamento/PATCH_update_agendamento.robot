*** Settings ***
Resource    ../../../src/scenario/agendamento/update_agendamento/update_agendamento_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - PATCH Update Agendamento
Metadata    Test Suite Description        This test suite validates the PATCH update Agendamento endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               PATCH    UpdateAgendamento    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-25
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate PATCH Update Agendamento - HTTP 200 OK
    [Documentation]    Test case to validate the PATCH update Agendamento endpoint with HTTP 200 OK response
    ...    — covers advancing status (AGENDADO → CONFIRMADO).
    [Tags]    PATCH    UpdateAgendamento    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    AGENDAMENTO - UPDATE - PATCH    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate PATCH Update Agendamento - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate the PATCH update Agendamento endpoint with HTTP 404 NOT FOUND
    ...    response.
    [Tags]    PATCH    UpdateAgendamento    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    AGENDAMENTO - UPDATE - PATCH    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
