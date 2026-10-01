*** Settings ***
Resource    ../../../../src/scenario/ui/administrativo/setores/setores_ui_scenario.resource
Resource    ../../../../src/scenario/common/ui_sessao_scenario.resource
Suite Setup       UI - ABRIR SISTEMA
Suite Teardown    UI - FECHAR SISTEMA
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - UI Setores
Metadata    Test Suite Description        Valida a tela Setores do frontend (ADR-0074).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               UI    UiSetores    MaisSaudePublicaFrontend
Metadata    Test Suite Created On         2026-09-30
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Tela Setores abre na aba Setores
    [Documentation]    A tela abre com o título e a aba Setores.
    [Tags]    UI    UiSetores
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - SETORES - ABRIR
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Gaveta de setor valida os campos obrigatórios
    [Documentation]    Erros de unidade, nome e código.
    [Tags]    UI    UiSetores
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - SETORES - VALIDAR GAVETA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Cadastrar setor pela gaveta
    [Documentation]    Setor cadastrado aparece na lista com aviso.
    [Tags]    UI    UiSetores
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - SETORES - CADASTRAR
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Rota antiga de responsabilidades abre a aba
    [Documentation]    Redirecionamento para ?aba=responsabilidades.
    [Tags]    UI    UiSetores
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - SETORES - ROTA ANTIGA DE RESPONSABILIDADES
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
