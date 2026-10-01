*** Settings ***
Resource    ../../../src/scenario/acesso/acesso/acesso_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - PATCH UpdatePapel
Metadata    Test Suite Description        This test suite validates the PATCH /api/v1/papel/{uuid} endpoint (ADR-0066) of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               PATCH    UpdatePapel    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-30
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate PATCH UpdatePapel - HTTP 200 OK
    [Documentation]    Test case to validate updating a custom Papel.
    [Tags]    PATCH    UpdatePapel    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ACESSO - PAPEL - PATCH    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate PATCH UpdatePapel - HTTP 422 UNPROCESSABLE ENTITY
    [Documentation]    Test case to validate that the platform administrator Papel cannot be deactivated.
    [Tags]    PATCH    UpdatePapel    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ACESSO - PAPEL - PATCH    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate PATCH UpdatePapel - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate that a non-existent Papel returns HTTP 404.
    [Tags]    PATCH    UpdatePapel    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ACESSO - PAPEL - PATCH    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
