*** Settings ***
Resource    ../../../src/scenario/ui/imunizacao/sala_de_vacina_ui_scenario.resource
Resource    ../../../src/scenario/common/ui_sessao_scenario.resource
Suite Setup       UI - ABRIR SISTEMA
Suite Teardown    UI - FECHAR SISTEMA
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - UI Sala de Vacina
Metadata    Test Suite Description        Valida a Sala de Vacina e a caderneta de vacinação do paciente (ADR-0147).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               UI    UiSalaDeVacina    MaisSaudePublicaFrontend
Metadata    Test Suite Created On         2026-10-27
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Registrar vacinação na sala
    [Documentation]    A dose registrada pela tela aparece nas doses do dia (ADR-0147).
    [Tags]    UI    UiSalaDeVacina
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - VACINA - SALA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Caderneta do paciente
    [Documentation]    A aba Vacinação mostra a dose e o calendário (ADR-0147).
    [Tags]    UI    UiSalaDeVacina
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - VACINA - CADERNETA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Entrada de lote e baixa da dose
    [Documentation]    O lote recebido pela aba Estoque é escolhido na dose, que sai do estoque e abre o frasco (ADR-0148).
    [Tags]    UI    UiSalaDeVacina
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - VACINA - ESTOQUE
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Excursão de temperatura e avaliação do lote
    [Documentation]    A leitura fora da faixa põe o lote sob suspeita; a coordenação libera com o parecer (ADR-0148).
    [Tags]    UI    UiSalaDeVacina
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - VACINA - CADEIA DE FRIO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
