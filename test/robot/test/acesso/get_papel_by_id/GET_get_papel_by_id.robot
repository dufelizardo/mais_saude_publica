*** Settings ***
Resource    ../../../src/scenario/acesso/acesso/acesso_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - GET GetPapelById
Metadata    Test Suite Description        This test suite validates the GET /api/v1/papel/{uuid} endpoint (ADR-0066) of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               GET    GetPapelById    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-30
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate GET GetPapelById - HTTP 200 OK
    [Documentation]    Test case to validate fetching the platform administrator Papel (no health-data permission).
    [Tags]    GET    GetPapelById    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ACESSO - PAPEL BY ID - GET    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate GET GetPapelById - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate that a non-existent Papel returns HTTP 404.
    [Tags]    GET    GetPapelById    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ACESSO - PAPEL BY ID - GET    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
