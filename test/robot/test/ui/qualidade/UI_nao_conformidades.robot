*** Settings ***
Resource    ../../../src/scenario/ui/qualidade/nao_conformidades_ui_scenario.resource
Resource    ../../../src/scenario/common/ui_sessao_scenario.resource
Suite Setup       UI - ABRIR SISTEMA
Suite Teardown    UI - FECHAR SISTEMA
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - UI Não conformidades
Metadata    Test Suite Description        Valida a tela das não conformidades: menu, registro e plano de ação (ADR-0204).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               UI    UiNaoConformidades    MaisSaudePublicaFrontend
Metadata    Test Suite Created On         2026-11-12
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Não conformidades pelo menu
    [Documentation]    O item do menu abre a tela.
    [Tags]    UI    UiNaoConformidades
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - NAO CONFORMIDADES - PELO MENU
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Registrar não conformidade
    [Documentation]    O registro abre o detalhe com o número.
    [Tags]    UI    UiNaoConformidades
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - NAO CONFORMIDADES - REGISTRAR
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Plano de ação
    [Documentation]    A ação entra no plano e a não conformidade fica em tratamento.
    [Tags]    UI    UiNaoConformidades
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - NAO CONFORMIDADES - PLANO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
