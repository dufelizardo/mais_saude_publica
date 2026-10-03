*** Settings ***
Resource    ../../../src/scenario/tipo_beneficio/update_tipo_beneficio/update_tipo_beneficio_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - PATCH UpdateTipoBeneficio
Metadata    Test Suite Description        Validates PATCH /api/v1/tipo-beneficio/{uuid} (ADR-0083).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               PATCH    UpdateTipoBeneficio    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-10-01
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate PATCH UpdateTipoBeneficio - HTTP 200 OK
    [Documentation]    Edição de um item novo.
    [Tags]    PATCH    UpdateTipoBeneficio    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    TIPO BENEFICIO - UPDATE - PATCH    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate PATCH UpdateTipoBeneficio - HTTP 404 NOT FOUND
    [Documentation]    Item inexistente.
    [Tags]    PATCH    UpdateTipoBeneficio    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    TIPO BENEFICIO - UPDATE - PATCH    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
