*** Settings ***
Resource    ../../../src/scenario/acesso/acesso/acesso_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - GET ListEscoposAcesso
Metadata    Test Suite Description        This test suite validates the GET /api/v1/atribuicao-acesso/escopos endpoint (ADR-0068) of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               GET    ListEscoposAcesso    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-30
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate GET ListEscoposAcesso - HTTP 200 OK
    [Documentation]    Test case to validate listing the units where an access can be granted.
    [Tags]    GET    ListEscoposAcesso    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ACESSO - ESCOPOS - GET    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
