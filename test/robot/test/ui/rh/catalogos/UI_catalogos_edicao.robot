*** Settings ***
Resource    ../../../../src/scenario/ui/rh/catalogos/catalogos_ui_scenario.resource
Resource    ../../../../src/scenario/common/ui_sessao_scenario.resource
Suite Setup       UI - ABRIR SISTEMA
Suite Teardown    UI - FECHAR SISTEMA
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - UI RH - Edição de catálogos
Metadata    Test Suite Description        Valida a edição de treinamento e de tipo de benefício pela tela (ADR-0083).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               UI    UiRhCatalogos    MaisSaudePublicaFrontend
Metadata    Test Suite Created On         2026-10-01
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Editar treinamento pela gaveta
    [Documentation]    Nome novo e aviso.
    [Tags]    UI    UiRhCatalogos
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - RH - EDITAR TREINAMENTO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Editar tipo de benefício pela gaveta
    [Documentation]    Nome novo e aviso.
    [Tags]    UI    UiRhCatalogos
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - RH - EDITAR BENEFICIO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
