*** Settings ***
Resource    ../../../../src/scenario/ui/administrativo/territorio/territorio_ui_scenario.resource
Resource    ../../../../src/scenario/common/ui_sessao_scenario.resource
Suite Setup       UI - ABRIR SISTEMA
Suite Teardown    UI - FECHAR SISTEMA
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - UI Administrativo - Territorio
Metadata    Test Suite Description        Valida a tela Território (ADR-0125): área com equipe, microárea com ACS e troca de ACS.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               UI    UiTerritorio    MaisSaudePublicaFrontend
Metadata    Test Suite Created On         2026-10-20
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Cadastrar área e atribuir a equipe
    [Documentation]    Nova área pela gaveta e a equipe responsável.
    [Tags]    UI    UiTerritorio
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - TERRITORIO - AREA E EQUIPE
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Cadastrar microárea e atribuir o ACS
    [Documentation]    Microárea com aviso de população e o ACS da equipe.
    [Tags]    UI    UiTerritorio
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - TERRITORIO - MICROAREA E ACS
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Trocar o ACS com motivo
    [Documentation]    Sem motivo, a mensagem; com motivo, o histórico.
    [Tags]    UI    UiTerritorio
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - TERRITORIO - TROCA DE ACS
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
