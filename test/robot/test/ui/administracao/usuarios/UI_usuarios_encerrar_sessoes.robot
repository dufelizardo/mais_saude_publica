*** Settings ***
Resource    ../../../../src/scenario/ui/administracao/usuarios/usuarios_ui_scenario.resource
Resource    ../../../../src/scenario/common/ui_sessao_scenario.resource
Suite Setup       UI - USUARIOS - PREPARAR ENCERRAMENTO DE SESSOES
Suite Teardown    UI - FECHAR SISTEMA
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - UI Usuários - Encerrar sessões
Metadata    Test Suite Description        Valida o botão Encerrar sessões da tela Usuários & Perfis (ADR-0078), com login e autorização ligados.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               UI    SEGURANCA    UiUsuariosSessoes    MaisSaudePublicaFrontend
Metadata    Test Suite Created On         2026-10-01
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Encerrar sessões pela tela derruba o token da pessoa
    [Documentation]    O token aberto vale antes; depois do botão, a API responde 401 para ele.
    [Tags]    UI    SEGURANCA    UiUsuariosSessoes
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - USUARIOS - ENCERRAR SESSOES
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
