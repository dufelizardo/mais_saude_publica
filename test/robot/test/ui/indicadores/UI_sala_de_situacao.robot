*** Settings ***
Resource    ../../../src/scenario/ui/indicadores/sala_de_situacao_ui_scenario.resource
Resource    ../../../src/scenario/common/ui_sessao_scenario.resource
Suite Setup       UI - ABRIR SISTEMA
Suite Teardown    UI - FECHAR SISTEMA
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - UI Sala de Situação
Metadata    Test Suite Description        Valida a Sala de Situação, a produção assistencial por unidade (ADR-0143).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               UI    UiSalaDeSituacao    MaisSaudePublicaFrontend
Metadata    Test Suite Created On         2026-10-23
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Sala de Situação pelo menu
    [Documentation]    O item do menu abre a tela com os indicadores (ADR-0143).
    [Tags]    UI    UiSalaDeSituacao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - SALA - MENU
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Produção da unidade
    [Documentation]    A unidade escolhida mostra o atendimento concluído hoje (ADR-0143).
    [Tags]    UI    UiSalaDeSituacao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - SALA - UNIDADE
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Situação operacional
    [Documentation]    Alerta de escala, ocorrências e cartões da BI2 (ADR-0144).
    [Tags]    UI    UiSalaDeSituacao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - SALA - SITUACAO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
