*** Settings ***
Resource    ../../../../src/scenario/ui/administrativo/comunicacao/comunicacao_ui_scenario.resource
Resource    ../../../../src/scenario/common/ui_sessao_scenario.resource
Suite Setup       UI - ABRIR SISTEMA
Suite Teardown    UI - FECHAR SISTEMA
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - UI Administrativo - Comunicacao
Metadata    Test Suite Description        Valida a tela Comunicação e os avisos na página pública (ADR-0168).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               UI    UiComunicacao    MaisSaudePublicaFrontend
Metadata    Test Suite Created On         2026-11-06
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Publicar comunicado no portal
    [Documentation]    Mensagem do campo, rascunho, publicação e aviso na página pública.
    [Tags]    UI    UiComunicacao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - COMUNICACAO - PUBLICAR
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Conteúdo com revisão técnica
    [Documentation]    Rascunho, revisão, aprovação e publicação.
    [Tags]    UI    UiComunicacao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - COMUNICACAO - CONTEUDO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Encerrar comunicado
    [Documentation]    Sem motivo, a mensagem; com motivo, encerra.
    [Tags]    UI    UiComunicacao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - COMUNICACAO - ENCERRAR
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

