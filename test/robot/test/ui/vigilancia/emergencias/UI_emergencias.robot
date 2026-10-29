*** Settings ***
Resource    ../../../../src/scenario/ui/vigilancia/emergencias/emergencias_ui_scenario.resource
Resource    ../../../../src/scenario/common/ui_sessao_scenario.resource
Suite Setup       UI - ABRIR SISTEMA
Suite Teardown    UI - FECHAR SISTEMA
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - UI Emergências e desastres
Metadata    Test Suite Description        Valida a tela de emergências: evento, conversão, nível e avaliação de impacto (ADR-0156).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               UI    UiEmergencias    MaisSaudePublicaFrontend
Metadata    Test Suite Created On         2026-10-28
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Registrar evento e converter em emergência
    [Documentation]    A gaveta valida; o evento vira emergência no nível 1.
    [Tags]    UI    UiEmergencias
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - EMERGENCIAS - REGISTRAR E CONVERTER
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Mudar nível e avaliar impacto
    [Documentation]    O histórico mostra a elevação; a avaliação aparece na linha do tempo.
    [Tags]    UI    UiEmergencias
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - EMERGENCIAS - NIVEL E AVALIACAO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
