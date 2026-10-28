*** Settings ***
Resource    ../../../../src/scenario/ui/administrativo/instituicoes_externas/instituicoes_externas_ui_scenario.resource
Resource    ../../../../src/scenario/common/ui_sessao_scenario.resource
Suite Setup       UI - ABRIR SISTEMA
Suite Teardown    UI - FECHAR SISTEMA
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - UI Instituições da rede intersetorial
Metadata    Test Suite Description        Valida a tela do catálogo de instituições externas (ADR-0154).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               UI    UiInstituicoesExternas    MaisSaudePublicaFrontend
Metadata    Test Suite Created On         2026-10-28
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Tela abre pelo menu
    [Documentation]    Administrativo · Rede intersetorial abre o catálogo.
    [Tags]    UI    UiInstituicoesExternas
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - INSTITUICOES EXTERNAS - ABRIR
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Cadastrar instituição
    [Documentation]    Validação da gaveta e a instituição na tabela.
    [Tags]    UI    UiInstituicoesExternas
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - INSTITUICOES EXTERNAS - CADASTRAR
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Inativar instituição
    [Documentation]    Some das ativas e aparece com as inativas.
    [Tags]    UI    UiInstituicoesExternas
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - INSTITUICOES EXTERNAS - INATIVAR
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
