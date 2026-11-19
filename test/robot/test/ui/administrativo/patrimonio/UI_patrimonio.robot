*** Settings ***
Resource    ../../../../src/scenario/ui/administrativo/patrimonio/patrimonio_ui_scenario.resource
Resource    ../../../../src/scenario/common/ui_sessao_scenario.resource
Suite Setup       UI - ABRIR SISTEMA
Suite Teardown    UI - FECHAR SISTEMA
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - UI Patrimonio
Metadata    Test Suite Description        Valida a tela Patrimônio: menu, cadastro, transferência com aceite, tombamento do bem comprado (ADR-0215), chamado e ordem de serviço (ADR-0216).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               UI    UiPatrimonio    MaisSaudePublicaFrontend
Metadata    Test Suite Created On         2026-11-19
Metadata    Test Suite Last Modified      2026-11-19
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Patrimônio pelo menu
    [Documentation]    O item do menu abre a tela.
    [Tags]    UI    UiPatrimonio
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - PATRIMONIO - PELO MENU
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Cadastro do bem
    [Documentation]    Cadastra o bem doado, que entra tombado e aparece na lista.
    [Tags]    UI    UiPatrimonio
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - PATRIMONIO - CADASTRO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Transferência e aceite
    [Documentation]    Envia o bem pela ficha e aceita na aba Transferências.
    [Tags]    UI    UiPatrimonio
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - PATRIMONIO - TRANSFERENCIA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Tombamento do bem comprado
    [Documentation]    O bem aceito no recebimento aparece em A tombar e recebe o número.
    [Tags]    UI    UiPatrimonio
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - PATRIMONIO - TOMBAMENTO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - Chamado pela ficha
    [Documentation]    A ficha do bem abre o chamado corretivo.
    [Tags]    UI    UiPatrimonio
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - PATRIMONIO - CHAMADO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-006 - Ordem de serviço
    [Documentation]    A aba Manutenção inicia e conclui a ordem do chamado.
    [Tags]    UI    UiPatrimonio
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - PATRIMONIO - ORDEM DE SERVICO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

