*** Settings ***
Resource    ../../../../src/scenario/ui/administrativo/ppi/ppi_ui_scenario.resource
Resource    ../../../../src/scenario/common/ui_sessao_scenario.resource
Suite Setup       UI - ABRIR SISTEMA
Suite Teardown    UI - FECHAR SISTEMA
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - UI Rede e pactuação
Metadata    Test Suite Description        Valida a oferta e as cotas da PPI pela tela (ADR-0179).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               UI    UiPpi    MaisSaudePublicaFrontend
Metadata    Test Suite Created On         2026-11-08
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Cadastrar a oferta e pactuar a cota
    [Documentation]    Procedimento obrigatório, cota acima do que sobra e a cota válida na oferta.
    [Tags]    UI    UiPpi
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - PPI - OFERTA E COTA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
