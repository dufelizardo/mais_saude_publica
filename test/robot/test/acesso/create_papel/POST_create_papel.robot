*** Settings ***
Resource    ../../../src/scenario/acesso/acesso/acesso_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST CreatePapel
Metadata    Test Suite Description        This test suite validates the POST /api/v1/papel/ endpoint (ADR-0066) of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    CreatePapel    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-30
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST CreatePapel - HTTP 201 CREATED
    [Documentation]    Test case to validate creating a custom Papel.
    [Tags]    POST    CreatePapel    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ACESSO - PAPEL - POST    201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST CreatePapel - HTTP 409 CONFLICT
    [Documentation]    Test case to validate that a repeated Papel code returns HTTP 409.
    [Tags]    POST    CreatePapel    HTTP409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ACESSO - PAPEL - POST    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate POST CreatePapel - HTTP 400 BAD REQUEST
    [Documentation]    Test case to validate that a permission outside the catalog returns HTTP 400.
    [Tags]    POST    CreatePapel    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ACESSO - PAPEL - POST    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
