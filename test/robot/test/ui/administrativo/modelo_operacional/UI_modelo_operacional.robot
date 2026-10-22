*** Settings ***
Resource    ../../../../src/scenario/ui/administrativo/modelo_operacional/modelo_operacional_ui_scenario.resource
Resource    ../../../../src/scenario/common/ui_sessao_scenario.resource
Suite Setup       UI - ABRIR SISTEMA
Suite Teardown    UI - FECHAR SISTEMA
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - UI Administrativo - Modelo Operacional
Metadata    Test Suite Description        Valida a aba Capacidades de Equipamentos de Saúde e a tela Modelo operacional (ADR-0117).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               UI    UiModeloOperacional    MaisSaudePublicaFrontend
Metadata    Test Suite Created On         2026-10-17
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Incluir perfil e capacidade na unidade
    [Documentation]    Atenção psicossocial e CAPS AD pela aba Capacidades.
    [Tags]    UI    UiModeloOperacional
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - CAPACIDADES - INCLUIR
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Encerrar capacidade com motivo
    [Documentation]    Sem motivo, a mensagem; com motivo, vai para o histórico.
    [Tags]    UI    UiModeloOperacional
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - CAPACIDADES - ENCERRAR
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Filtrar a rede por perfil
    [Documentation]    A unidade com o perfil aparece no filtro.
    [Tags]    UI    UiModeloOperacional
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - CAPACIDADES - FILTRO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Catálogo com a carga inicial
    [Documentation]    Perfis da carga e capacidades de um perfil.
    [Tags]    UI    UiModeloOperacional
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - MODELO OPERACIONAL - CARGA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - Cadastrar perfil no catálogo
    [Documentation]    Aviso e linha nova na tabela.
    [Tags]    UI    UiModeloOperacional
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - MODELO OPERACIONAL - CADASTRAR
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-006 - Gaveta recusa código inválido
    [Documentation]    Mensagens do código e do nome.
    [Tags]    UI    UiModeloOperacional
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - MODELO OPERACIONAL - VALIDACAO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
