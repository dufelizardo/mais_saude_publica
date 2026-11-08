*** Settings ***
Resource    ../../../../src/scenario/ui/administrativo/frota/frota_ui_scenario.resource
Resource    ../../../../src/scenario/common/ui_sessao_scenario.resource
Suite Setup       UI - ABRIR SISTEMA
Suite Teardown    UI - FECHAR SISTEMA
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - UI Frota e transporte
Metadata    Test Suite Description        Valida a frota pela tela (ADR-0178).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               UI    UiFrota    MaisSaudePublicaFrontend
Metadata    Test Suite Created On         2026-11-08
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Cadastrar veículo e pôr em manutenção
    [Documentation]    Placa inválida, cadastro, ficha e manutenção com histórico.
    [Tags]    UI    UiFrota
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - FROTA - VEICULO E MANUTENCAO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
