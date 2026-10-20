*** Settings ***
Resource    ../../../../src/scenario/ui/assistencia/saude_mental/saude_mental_ui_scenario.resource
Resource    ../../../../src/scenario/common/ui_sessao_scenario.resource
Suite Setup       UI - ABRIR SISTEMA
Suite Teardown    UI - FECHAR SISTEMA
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - UI Assistência - Saúde Mental
Metadata    Test Suite Description        Valida a tela Saúde Mental: acolhimento, avaliação, referência e saída (ADR-0120).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               UI    UiSaudeMental    MaisSaudePublicaFrontend
Metadata    Test Suite Created On         2026-10-19
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Acolher abrindo o caso
    [Documentation]    Sem paciente, a mensagem; com a referência, o caso abre no programa.
    [Tags]    UI    UiSaudeMental
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - SAUDE MENTAL - ACOLHER
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Nova avaliação
    [Documentation]    Sem contexto, a mensagem; com os dados, a avaliação aparece na aba.
    [Tags]    UI    UiSaudeMental
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - SAUDE MENTAL - AVALIAR
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Trocar referência
    [Documentation]    A referência anterior fica no histórico.
    [Tags]    UI    UiSaudeMental
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - SAUDE MENTAL - TROCAR REFERENCIA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Encerrar o caso
    [Documentation]    Alta conclui o caso.
    [Tags]    UI    UiSaudeMental
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - SAUDE MENTAL - ENCERRAR
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
