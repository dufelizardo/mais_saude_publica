*** Settings ***
Resource    ../../../../src/scenario/ui/assistencia/urgencia/urgencia_ui_scenario.resource
Resource    ../../../../src/scenario/common/ui_sessao_scenario.resource
Suite Setup       UI - ABRIR SISTEMA
Suite Teardown    UI - FECHAR SISTEMA
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - UI Regulação de urgência
Metadata    Test Suite Description        Valida a central de urgência pela tela (ADR-0176).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               UI    UiUrgencia    MaisSaudePublicaFrontend
Metadata    Test Suite Created On         2026-11-08
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Abrir o chamado, regular e enviar o recurso
    [Documentation]    Validação da queixa, regulação com USA e o primeiro passo do deslocamento.
    [Tags]    UI    UiUrgencia
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - URGENCIA - CHAMADO E REGULACAO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
