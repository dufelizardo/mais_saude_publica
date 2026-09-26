*** Settings ***
Resource    ../../../src/scenario/medicamento/update_medicamento/update_medicamento_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - PATCH Update Medicamento
Metadata    Test Suite Description        This test suite validates the PATCH update Medicamento endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               PATCH    UpdateMedicamento    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-26
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate PATCH Update Medicamento - HTTP 200 OK
    [Documentation]    Test case to validate the PATCH update Medicamento endpoint with HTTP 200 OK
    ...    response.
    [Tags]    PATCH    UpdateMedicamento    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    MEDICAMENTO - UPDATE - PATCH    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate PATCH Update Medicamento - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate the PATCH update Medicamento endpoint with HTTP 404 NOT
    ...    FOUND response.
    [Tags]    PATCH    UpdateMedicamento    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    MEDICAMENTO - UPDATE - PATCH    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
