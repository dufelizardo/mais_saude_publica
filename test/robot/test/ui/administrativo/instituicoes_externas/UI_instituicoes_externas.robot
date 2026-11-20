*** Settings ***
Resource    ../../../../src/scenario/ui/administrativo/instituicoes_externas/instituicoes_externas_ui_scenario.resource
Resource    ../../../../src/scenario/common/ui_sessao_scenario.resource
Suite Setup       UI - ABRIR SISTEMA
Suite Teardown    UI - FECHAR SISTEMA
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - UI Instituições da rede intersetorial
Metadata    Test Suite Description        Valida a tela do catálogo de instituições externas (ADR-0154) e o painel da rede intersetorial (ADR-0211).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               UI    UiInstituicoesExternas    MaisSaudePublicaFrontend
Metadata    Test Suite Created On         2026-10-28
Metadata    Test Suite Last Modified      2026-11-18
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

CT-004 - Painel da rede e cobrança
    [Documentation]    Instituição com encaminhamento sem retorno e a lista de cobrança.
    [Tags]    UI    UiInstituicoesExternas
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - INSTITUICOES EXTERNAS - PAINEL
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - Alerta na Sala de Situação
    [Documentation]    Três sem retorno viram alerta, que abre o painel.
    [Tags]    UI    UiInstituicoesExternas
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - INSTITUICOES EXTERNAS - ALERTA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
