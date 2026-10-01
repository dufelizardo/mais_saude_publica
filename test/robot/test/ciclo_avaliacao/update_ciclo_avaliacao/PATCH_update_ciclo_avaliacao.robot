*** Settings ***
Resource    ../../../src/scenario/ciclo_avaliacao/update_ciclo_avaliacao/update_ciclo_avaliacao_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - PATCH UpdateCicloAvaliacao
Metadata    Test Suite Description        Validates PATCH /api/v1/ciclo-avaliacao/{uuid} (ADR-0083).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               PATCH    UpdateCicloAvaliacao    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-10-01
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate PATCH UpdateCicloAvaliacao - HTTP 200 OK
    [Documentation]    Edição de um item novo.
    [Tags]    PATCH    UpdateCicloAvaliacao    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    CICLO AVALIACAO - UPDATE - PATCH    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate PATCH UpdateCicloAvaliacao - HTTP 404 NOT FOUND
    [Documentation]    Item inexistente.
    [Tags]    PATCH    UpdateCicloAvaliacao    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    CICLO AVALIACAO - UPDATE - PATCH    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate PATCH UpdateCicloAvaliacao - HTTP 400 BAD REQUEST
    [Documentation]    Fim antes do início.
    [Tags]    PATCH    UpdateCicloAvaliacao    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    CICLO AVALIACAO - UPDATE - PATCH    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
