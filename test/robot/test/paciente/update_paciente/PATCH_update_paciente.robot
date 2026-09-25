*** Settings ***
Resource    ../../../src/scenario/paciente/update_paciente/update_paciente_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - PATCH Update Paciente
Metadata    Test Suite Description        This test suite validates the PATCH update Paciente endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               PATCH    UpdatePaciente    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-25
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate PATCH Update Paciente - HTTP 200 OK
    [Documentation]    Test case to validate the PATCH update Paciente endpoint with HTTP 200 OK response —
    ...    covers both editing cadastro fields and inativar (ativo=False).
    [Tags]    PATCH    UpdatePaciente    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PACIENTE - UPDATE - PATCH    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate PATCH Update Paciente - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate the PATCH update Paciente endpoint with HTTP 404 NOT FOUND
    ...    response.
    [Tags]    PATCH    UpdatePaciente    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PACIENTE - UPDATE - PATCH    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
