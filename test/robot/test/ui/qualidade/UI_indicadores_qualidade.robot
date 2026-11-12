*** Settings ***
Resource    ../../../src/scenario/ui/qualidade/indicadores_qualidade_ui_scenario.resource
Resource    ../../../src/scenario/common/ui_sessao_scenario.resource
Suite Setup       UI - ABRIR SISTEMA
Suite Teardown    UI - FECHAR SISTEMA
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - UI Indicadores de qualidade
Metadata    Test Suite Description        Valida a tela dos indicadores de qualidade: menu, meta e auditoria de protocolo (ADR-0205).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               UI    UiIndicadoresQualidade    MaisSaudePublicaFrontend
Metadata    Test Suite Created On         2026-11-12
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Indicadores de qualidade pelo menu
    [Documentation]    O item do menu abre a tela com o painel.
    [Tags]    UI    UiIndicadoresQualidade
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - INDICADORES QUALIDADE - PELO MENU
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Definir meta
    [Documentation]    A meta de adesão da unidade entra no histórico.
    [Tags]    UI    UiIndicadoresQualidade
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - INDICADORES QUALIDADE - META
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Auditoria de protocolo
    [Documentation]    O checklist respondido entra com a adesão.
    [Tags]    UI    UiIndicadoresQualidade
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - INDICADORES QUALIDADE - AUDITORIA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
