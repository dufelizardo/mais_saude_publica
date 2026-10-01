*** Settings ***
Resource    ../../../../src/scenario/ui/auth/recuperacao/recuperacao_ui_scenario.resource
Resource    ../../../../src/scenario/common/ui_sessao_scenario.resource
Suite Setup       UI - ABRIR SISTEMA
Suite Teardown    UI - FECHAR SISTEMA
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - UI Recuperação de senha indisponível
Metadata    Test Suite Description        Sem SMTP, o login não oferece o link e as telas avisam (ADR-0081).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               UI    UiRecuperacaoSenha    MaisSaudePublicaFrontend
Metadata    Test Suite Created On         2026-10-01
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Sem SMTP, a recuperação aparece como indisponível
    [Documentation]    Login sem link, aviso e link incompleto.
    [Tags]    UI    UiRecuperacaoSenha
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - RECUPERACAO - INDISPONIVEL
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
