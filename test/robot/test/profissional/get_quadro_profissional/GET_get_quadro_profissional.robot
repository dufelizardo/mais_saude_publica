*** Settings ***
Resource    ../../../src/scenario/profissional/get_quadro_profissional/get_quadro_profissional_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - GET GetQuadroProfissional
Metadata    Test Suite Description        This test suite validates the GET /api/v1/profissional/quadro endpoint (ADR-0072) of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               GET    GetQuadroProfissional    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-10-01
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate GET GetQuadroProfissional - HTTP 200 OK
    [Documentation]    Test case to validate listing the staff board with current lotação and afastamento.
    [Tags]    GET    GetQuadroProfissional    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROFISSIONAL - QUADRO - GET    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
