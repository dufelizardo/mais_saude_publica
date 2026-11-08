*** Settings ***
Resource    ../../src/scenario/educacao_permanente/educacao_permanente_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - Educação permanente
Metadata    Test Suite Description        This test suite validates the competencies and teaching material of trainings (ADR-0173).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               EducacaoPermanente    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-11-08
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Educacao Permanente - HTTP 200 E 409 (Catalogo)
    [Documentation]    Carga das competências e código repetido.
    [Tags]    EducacaoPermanente    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    EDUCACAO PERMANENTE    200-409-catalogo
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate Educacao Permanente - HTTP 200 (Ligar)
    [Documentation]    Competência e conteúdo revisado no treinamento.
    [Tags]    EducacaoPermanente    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    EDUCACAO PERMANENTE    200-ligar
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate Educacao Permanente - HTTP 422 (Sem Revisao)
    [Documentation]    Conteúdo sem revisão não entra.
    [Tags]    EducacaoPermanente    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    EDUCACAO PERMANENTE    422-rascunho
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate Educacao Permanente - HTTP 200 (Profissional)
    [Documentation]    Competência pela participação.
    [Tags]    EducacaoPermanente    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    EDUCACAO PERMANENTE    200-profissional
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - Validate Educacao Permanente - HTTP 404 (Profissional)
    [Documentation]    Profissional que não existe.
    [Tags]    EducacaoPermanente    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    EDUCACAO PERMANENTE    404-profissional
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

