*** Settings ***
Resource    ../../../../src/scenario/ui/administrativo/coordenacoes/coordenacoes_ui_scenario.resource
Resource    ../../../../src/scenario/common/ui_sessao_scenario.resource
Suite Setup       UI - ABRIR SISTEMA
Suite Teardown    UI - FECHAR SISTEMA
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - UI Coordenações e gerência
Metadata    Test Suite Description        Valida a estrutura de gestão pela tela (ADR-0175).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               UI    UiCoordenacoes    MaisSaudePublicaFrontend
Metadata    Test Suite Created On         2026-11-08
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Criar gerência e designar o titular
    [Documentation]    Validação do nome, organograma e histórico.
    [Tags]    UI    UiCoordenacoes
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - COORDENACOES - GERENCIA E TITULAR
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
