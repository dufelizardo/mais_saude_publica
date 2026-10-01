*** Settings ***
Resource    ../../../../src/scenario/ui/assistencia/pacientes/pacientes_ui_scenario.resource
Resource    ../../../../src/scenario/common/ui_sessao_scenario.resource
Suite Setup       UI - ABRIR SISTEMA
Suite Teardown    UI - FECHAR SISTEMA
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - UI Pacientes - Prontuário
Metadata    Test Suite Description        Valida a abertura do prontuário na tela Pacientes (ADR-0076, vínculo desligado).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               UI    UiPacientesProntuario    MaisSaudePublicaFrontend
Metadata    Test Suite Created On         2026-09-30
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Prontuário abre sem aviso de vínculo com a regra desligada
    [Documentation]    Busca o paciente, abre o detalhe e o histórico, sem aviso de vínculo.
    [Tags]    UI    UiPacientesProntuario
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - PACIENTES - PRONTUARIO SEM VINCULO LIGADO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
