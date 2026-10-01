*** Settings ***
Resource    ../../../src/scenario/ui/shell/menu_ui_scenario.resource
Resource    ../../../src/scenario/common/ui_sessao_scenario.resource
Suite Setup       UI - ABRIR SISTEMA
Suite Teardown    UI - FECHAR SISTEMA
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - UI Menu
Metadata    Test Suite Description        Valida o menu lateral em acordeão: recolhido por padrão, com o grupo da página aberto.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               UI    UiMenu    MaisSaudePublicaFrontend
Metadata    Test Suite Created On         2026-10-01
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Só o grupo da página atual começa aberto
    [Documentation]    Em Setores, só Administrativo aberto.
    [Tags]    UI    UiMenu
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - MENU - SO O GRUPO DA PAGINA ABRE
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Grupo aberto pela pessoa continua aberto ao navegar
    [Documentation]    Recursos Humanos aberto à mão segue aberto; o grupo da página nova abre sozinho.
    [Tags]    UI    UiMenu
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - MENU - GRUPO ABERTO CONTINUA ABERTO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - O grupo da página atual também pode ser recolhido
    [Documentation]    Clicar no grupo aberto recolhe.
    [Tags]    UI    UiMenu
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - MENU - RECOLHER O GRUPO DA PAGINA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
