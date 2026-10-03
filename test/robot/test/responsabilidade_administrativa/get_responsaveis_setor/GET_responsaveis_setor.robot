*** Settings ***
Resource    ../../../src/scenario/responsabilidade_administrativa/get_responsaveis_setor/get_responsaveis_setor_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - GET ResponsaveisDoSetor
Metadata    Test Suite Description        Validates GET /api/v1/responsabilidade-administrativa/setor/{setorId} (ADR-0083).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               GET    ResponsaveisDoSetor    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-10-01
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate GET ResponsaveisDoSetor - HTTP 200 OK
    [Documentation]    Setor com responsabilidade.
    [Tags]    GET    ResponsaveisDoSetor    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    RESPONSABILIDADE ADMINISTRATIVA - RESPONSAVEIS DO SETOR - GET    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate GET ResponsaveisDoSetor - HTTP 404 NOT FOUND
    [Documentation]    Setor inexistente.
    [Tags]    GET    ResponsaveisDoSetor    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    RESPONSABILIDADE ADMINISTRATIVA - RESPONSAVEIS DO SETOR - GET    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
