*** Settings ***
Resource    ../../../../src/scenario/ui/administrativo/compras/compras_ui_scenario.resource
Resource    ../../../../src/scenario/common/ui_sessao_scenario.resource
Suite Setup       UI - ABRIR SISTEMA
Suite Teardown    UI - FECHAR SISTEMA
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - UI Compras
Metadata    Test Suite Description        Valida a tela Compras: menu, solicitação, pesquisa de preços e fornecedor (ADR-0212).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               UI    UiCompras    MaisSaudePublicaFrontend
Metadata    Test Suite Created On         2026-11-18
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Compras pelo menu
    [Documentation]    O item do menu abre a tela.
    [Tags]    UI    UiCompras
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - COMPRAS - PELO MENU
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Nova solicitação
    [Documentation]    Monta, salva o rascunho e envia.
    [Tags]    UI    UiCompras
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - COMPRAS - NOVA SOLICITACAO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Cotação e conclusão
    [Documentation]    Três cotações e a pesquisa concluída.
    [Tags]    UI    UiCompras
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - COMPRAS - COTACAO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Fornecedor e certidão
    [Documentation]    Cadastro com as certidões pendentes e uma certidão.
    [Tags]    UI    UiCompras
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - COMPRAS - FORNECEDOR
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
