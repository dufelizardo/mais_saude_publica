*** Settings ***
Resource    ../../../src/scenario/treinamento/update_treinamento/update_treinamento_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - PATCH UpdateTreinamento
Metadata    Test Suite Description        Validates PATCH /api/v1/treinamento/{uuid} (ADR-0083).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               PATCH    UpdateTreinamento    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-10-01
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate PATCH UpdateTreinamento - HTTP 200 OK
    [Documentation]    Edição de um item novo.
    [Tags]    PATCH    UpdateTreinamento    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    TREINAMENTO - UPDATE - PATCH    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate PATCH UpdateTreinamento - HTTP 404 NOT FOUND
    [Documentation]    Item inexistente.
    [Tags]    PATCH    UpdateTreinamento    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    TREINAMENTO - UPDATE - PATCH    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
