*** Settings ***
Resource    ../../../../src/scenario/ui/administrativo/modelo_administrativo/modelo_administrativo_ui_scenario.resource
Resource    ../../../../src/scenario/common/ui_sessao_scenario.resource
Suite Setup       UI - ABRIR SISTEMA
Suite Teardown    UI - FECHAR SISTEMA
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - UI Modelo administrativo
Metadata    Test Suite Description        Valida a tela Modelo administrativo do frontend (ADR-0074).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               UI    UiModeloAdministrativo    MaisSaudePublicaFrontend
Metadata    Test Suite Created On         2026-09-30
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Tela Modelo administrativo abre na aba Capacidades
    [Documentation]    Título e aba inicial.
    [Tags]    UI    UiModeloAdministrativo
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - MODELO ADMINISTRATIVO - ABRIR
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Rota antiga de processos abre a aba Processos
    [Documentation]    Redirecionamento.
    [Tags]    UI    UiModeloAdministrativo
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - MODELO ADMINISTRATIVO - ROTA ANTIGA    /administrativo/processos    Processos
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Rota antiga de perfil por tipo abre a aba certa
    [Documentation]    Redirecionamento.
    [Tags]    UI    UiModeloAdministrativo
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - MODELO ADMINISTRATIVO - ROTA ANTIGA    /administrativo/perfis-por-tipo-unidade    Perfil por tipo de unidade
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Gaveta de capacidade valida os campos obrigatórios
    [Documentation]    Erros de código e nome.
    [Tags]    UI    UiModeloAdministrativo
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - MODELO ADMINISTRATIVO - VALIDAR GAVETA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - Cadastrar capacidade pela gaveta
    [Documentation]    Aviso de sucesso e capacidade na lista.
    [Tags]    UI    UiModeloAdministrativo
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - MODELO ADMINISTRATIVO - CADASTRAR CAPACIDADE
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
