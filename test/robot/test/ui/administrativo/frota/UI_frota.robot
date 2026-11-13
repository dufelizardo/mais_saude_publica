*** Settings ***
Resource    ../../../../src/scenario/ui/administrativo/frota/frota_ui_scenario.resource
Resource    ../../../../src/scenario/common/ui_sessao_scenario.resource
Suite Setup       UI - ABRIR SISTEMA
Suite Teardown    UI - FECHAR SISTEMA
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - UI Frota e transporte
Metadata    Test Suite Description        Valida a frota, o pedido de transporte e a viagem pela tela (ADR-0178 e ADR-0190).
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

CT-002 - Pedir e cancelar transporte
    [Documentation]    Sem paciente, a validação; o pedido entra na fila e se cancela com motivo.
    [Tags]    UI    UiFrota
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - FROTA - PEDIR E CANCELAR TRANSPORTE
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Programar viagem até a chegada
    [Documentation]    A viagem com o paciente da fila: saída, embarque e chegada com a quilometragem.
    [Tags]    UI    UiFrota
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - FROTA - VIAGEM ATE A CHEGADA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Ceder veículo à emergência e devolver
    [Documentation]    O pedido da emergência na aba Frota: ceder a van e devolver com a quilometragem.
    [Tags]    UI    UiFrota
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - FROTA - CEDER A EMERGENCIA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
