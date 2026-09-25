*** Settings ***
Resource    ../../../src/scenario/consulta/update_consulta/update_consulta_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - PATCH Update Consulta
Metadata    Test Suite Description        This test suite validates the PATCH update Consulta endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               PATCH    UpdateConsulta    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-25
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate PATCH Update Consulta - HTTP 200 OK
    [Documentation]    Test case to validate the PATCH update Consulta endpoint with HTTP 200 OK response.
    [Tags]    PATCH    UpdateConsulta    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    CONSULTA - UPDATE - PATCH    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate PATCH Update Consulta - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate the PATCH update Consulta endpoint with HTTP 404 NOT FOUND
    ...    response.
    [Tags]    PATCH    UpdateConsulta    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    CONSULTA - UPDATE - PATCH    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
