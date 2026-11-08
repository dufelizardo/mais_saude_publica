*** Settings ***
Resource    ../../../../src/scenario/ui/administrativo/estoque/estoque_ui_scenario.resource
Resource    ../../../../src/scenario/common/ui_sessao_scenario.resource
Suite Setup       UI - ABRIR SISTEMA
Suite Teardown    UI - FECHAR SISTEMA
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - UI Estoque e almoxarifado
Metadata    Test Suite Description        Valida o estoque de materiais pela tela (ADR-0177).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               UI    UiEstoque    MaisSaudePublicaFrontend
Metadata    Test Suite Created On         2026-11-08
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Cadastrar material, dar entrada e saída
    [Documentation]    Código obrigatório, entrada com lote, saída maior que o saldo e saída válida.
    [Tags]    UI    UiEstoque
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - ESTOQUE - MATERIAL ENTRADA E SAIDA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
