*** Settings ***
Resource    ../../../../src/scenario/ui/auth/recuperacao/recuperacao_ui_scenario.resource
Resource    ../../../../src/scenario/common/ui_sessao_scenario.resource
Suite Setup       UI - RECUPERACAO - PREPARAR
Suite Teardown    UI - FECHAR SISTEMA
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - UI Recuperação de senha por e-mail
Metadata    Test Suite Description        Fluxo completo com SMTP de teste (Mailpit): pedido, e-mail, senha nova e login (ADR-0081).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               UI    SEGURANCA    UiRecuperacaoSenha    MaisSaudePublicaFrontend
Metadata    Test Suite Created On         2026-10-01
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Recuperar a senha pelo link do e-mail e entrar com a senha nova
    [Documentation]    Pedido pela tela, link do e-mail, senha nova e login.
    [Tags]    UI    SEGURANCA    UiRecuperacaoSenha
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - RECUPERACAO - FLUXO COMPLETO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - O link usado não vale de novo
    [Documentation]    A tela diz que o link não vale e oferece pedir outro.
    [Tags]    UI    SEGURANCA    UiRecuperacaoSenha
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - RECUPERACAO - LINK USADO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
