*** Settings ***
Resource    ../../../../src/scenario/ui/rh/perfil/perfil_ui_scenario.resource
Resource    ../../../../src/scenario/common/ui_sessao_scenario.resource
Suite Setup       UI - ABRIR SISTEMA
Suite Teardown    UI - FECHAR SISTEMA
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - UI RH - Perfil do profissional (Dados a Ajustes)
Metadata    Test Suite Description        Valida as gavetas das abas Dados e Ajustes do perfil (ADR-0084).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               UI    UiRhPerfil    MaisSaudePublicaFrontend
Metadata    Test Suite Created On         2026-10-01
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Registrar ajuste individual pela gaveta
    [Documentation]    Aviso e linha nova na tabela.
    [Tags]    UI    UiRhPerfil
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - PERFIL - REGISTRAR AJUSTE
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Editar contato pela gaveta
    [Documentation]    Aviso e e-mail novo na aba Dados.
    [Tags]    UI    UiRhPerfil
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - PERFIL - EDITAR CONTATO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Gaveta de ajuste recusa envio sem valor
    [Documentation]    Mensagem do campo obrigatório e gaveta continua aberta.
    [Tags]    UI    UiRhPerfil
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - PERFIL - AJUSTE SEM VALOR
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
