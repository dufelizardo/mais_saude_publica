*** Settings ***
Resource    ../../../src/scenario/ui/qualidade/seguranca_do_paciente_ui_scenario.resource
Resource    ../../../src/scenario/common/ui_sessao_scenario.resource
Suite Setup       UI - ABRIR SISTEMA
Suite Teardown    UI - FECHAR SISTEMA
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - UI Segurança do paciente
Metadata    Test Suite Description        Valida a tela da segurança do paciente: menu, notificação anônima e classificação pelo núcleo (ADR-0203).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               UI    UiSegurancaDoPaciente    MaisSaudePublicaFrontend
Metadata    Test Suite Created On         2026-11-12
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Segurança do paciente pelo menu
    [Documentation]    O item do menu abre a tela.
    [Tags]    UI    UiSegurancaDoPaciente
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - SEGURANCA - PELO MENU
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Notificar incidente anônimo
    [Documentation]    O protocolo aparece depois da notificação anônima.
    [Tags]    UI    UiSegurancaDoPaciente
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - SEGURANCA - NOTIFICAR
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Classificar pelo núcleo
    [Documentation]    O incidente classificado entra em análise.
    [Tags]    UI    UiSegurancaDoPaciente
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - SEGURANCA - CLASSIFICAR
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
