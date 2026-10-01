*** Settings ***
Resource    ../../../../src/scenario/ui/rh/perfil/perfil_ui_scenario.resource
Resource    ../../../../src/scenario/common/ui_sessao_scenario.resource
Suite Setup       UI - ABRIR SISTEMA
Suite Teardown    UI - FECHAR SISTEMA
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - UI RH - Perfil do profissional (Afastamentos a SST)
Metadata    Test Suite Description        Valida as gavetas das abas Afastamentos, Ponto, Folha e SST do perfil (ADR-0085).
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
CT-001 - Registrar afastamento pela gaveta
    [Documentation]    Aviso e linha nova na tabela.
    [Tags]    UI    UiRhPerfil
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - PERFIL - REGISTRAR AFASTAMENTO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Solicitar correção de ponto pela gaveta
    [Documentation]    Ponto registrado e correção pendente.
    [Tags]    UI    UiRhPerfil
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - PERFIL - SOLICITAR CORRECAO DE PONTO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Registrar exame ocupacional pela gaveta
    [Documentation]    Aviso e resultado na tabela.
    [Tags]    UI    UiRhPerfil
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - PERFIL - REGISTRAR EXAME
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Gaveta da folha recusa competência inválida
    [Documentation]    Mensagem do campo competência.
    [Tags]    UI    UiRhPerfil
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - PERFIL - FOLHA COM COMPETENCIA INVALIDA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
