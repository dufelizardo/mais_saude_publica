*** Settings ***
Resource    ../../../src/scenario/procedimento_regulado/update_procedimento_regulado/update_procedimento_regulado_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - PATCH UpdateProcedimentoRegulado
Metadata    Test Suite Description        This test suite validates the PATCH /api/v1/procedimento-regulado/{uuid} endpoint of the Mais Saúde Pública API (ADR-0087).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               PATCH    UpdateProcedimentoRegulado    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-10-01
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate PATCH UpdateProcedimentoRegulado - HTTP 200 OK
    [Documentation]    Edição de nome, tipo e ativo.
    [Tags]    PATCH    UpdateProcedimentoRegulado    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROCEDIMENTO REGULADO - UPDATE - PATCH    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate PATCH UpdateProcedimentoRegulado - HTTP 404 NOT FOUND
    [Documentation]    Item inexistente.
    [Tags]    PATCH    UpdateProcedimentoRegulado    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROCEDIMENTO REGULADO - UPDATE - PATCH    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
