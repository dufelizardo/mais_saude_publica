*** Settings ***
Resource    ../../../../src/scenario/ui/rh/perfil/perfil_ui_scenario.resource
Resource    ../../../../src/scenario/common/ui_sessao_scenario.resource
Suite Setup       UI - ABRIR SISTEMA
Suite Teardown    UI - FECHAR SISTEMA
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - UI RH - Perfil do profissional (Treinamentos a Histórico)
Metadata    Test Suite Description        Valida as gavetas das abas Treinamentos, Avaliações, Benefícios e Desligamento do perfil (ADR-0086).
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
CT-001 - Registrar participação em treinamento pela gaveta
    [Documentation]    Aviso e treinamento na tabela.
    [Tags]    UI    UiRhPerfil
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - PERFIL - REGISTRAR PARTICIPACAO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Aderir e encerrar benefício pelas gavetas
    [Documentation]    Adesão registrada e depois encerrada.
    [Tags]    UI    UiRhPerfil
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - PERFIL - ADERIR E ENCERRAR BENEFICIO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Registrar avaliação pela gaveta
    [Documentation]    Aviso e ciclo na tabela.
    [Tags]    UI    UiRhPerfil
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - PERFIL - REGISTRAR AVALIACAO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Profissional ativo não oferece cálculo de rescisão
    [Documentation]    Botão ausente enquanto ativo.
    [Tags]    UI    UiRhPerfil
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - PERFIL - ATIVO SEM CALCULO DE RESCISAO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
