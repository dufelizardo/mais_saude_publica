*** Settings ***
Resource    ../../src/scenario/gestao/gestao_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - Coordenações e gerência
Metadata    Test Suite Description        This test suite validates the management structure and the regional scope of indicators (ADR-0175).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               Gestao    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-11-08
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Gestao - HTTP 201 (Coordenacao Regional)
    [Documentation]    Gerência com coordenação regional.
    [Tags]    Gestao    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ESTRUTURA DE GESTAO    201-coordenacao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate Gestao - HTTP 409 (Regional Repetida)
    [Documentation]    Uma coordenação ativa por regional.
    [Tags]    Gestao    HTTP409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ESTRUTURA DE GESTAO    409-regional
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate Gestao - HTTP 422 (Hierarquia)
    [Documentation]    Supervisão sem superior e regional que não é regional.
    [Tags]    Gestao    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ESTRUTURA DE GESTAO    422-hierarquia
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate Gestao - HTTP 200 (Titular)
    [Documentation]    Troca de titular com histórico.
    [Tags]    Gestao    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ESTRUTURA DE GESTAO    200-titular
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - Validate Gestao - HTTP 200 (Indicadores Da Regional)
    [Documentation]    Indicadores pelo recorte da regional.
    [Tags]    Gestao    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ESTRUTURA DE GESTAO    200-indicadores
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

