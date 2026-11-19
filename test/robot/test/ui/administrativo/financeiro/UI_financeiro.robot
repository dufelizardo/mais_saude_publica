*** Settings ***
Resource    ../../../../src/scenario/ui/administrativo/financeiro/financeiro_ui_scenario.resource
Resource    ../../../../src/scenario/common/ui_sessao_scenario.resource
Suite Setup       UI - ABRIR SISTEMA
Suite Teardown    UI - FECHAR SISTEMA
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - UI Financeiro
Metadata    Test Suite Description        Valida a tela Financeiro: menu, dotação e empenho (ADR-0218).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               UI    UiFinanceiro    MaisSaudePublicaFrontend
Metadata    Test Suite Created On         2026-11-19
Metadata    Test Suite Last Modified      2026-11-19
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Financeiro pelo menu
    [Documentation]    O item do menu abre a tela.
    [Tags]    UI    UiFinanceiro
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - FINANCEIRO - PELO MENU
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Nova dotação
    [Documentation]    Registra a dotação do exercício.
    [Tags]    UI    UiFinanceiro
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - FINANCEIRO - NOVA DOTACAO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Registrar empenho
    [Documentation]    Registra o empenho e abre a gaveta com o valor.
    [Tags]    UI    UiFinanceiro
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - FINANCEIRO - EMPENHO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

