*** Settings ***
Resource    ../../../../src/scenario/ui/vigilancia/vetores/controle_vetorial_ui_scenario.resource
Resource    ../../../../src/scenario/common/ui_sessao_scenario.resource
Suite Setup       UI - ABRIR SISTEMA
Suite Teardown    UI - FECHAR SISTEMA
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - UI Zoonoses e vetores
Metadata    Test Suite Description        Valida a tela de controle de vetores: vistoria com foco, índices do ciclo e ação de controle (ADR-0162).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               UI    UiControleVetorial    MaisSaudePublicaFrontend
Metadata    Test Suite Created On         2026-11-04
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Zoonoses e vetores pelo menu
    [Documentation]    O item do menu Vigilância abre a tela.
    [Tags]    UI    UiControleVetorial
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - CONTROLE VETORIAL - PELO MENU
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Registrar vistoria com foco
    [Documentation]    A vistoria com um foco aparece na tabela e a área fica em risco nos índices.
    [Tags]    UI    UiControleVetorial
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - CONTROLE VETORIAL - REGISTRAR VISTORIA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Ação de controle
    [Documentation]    A ação planejada na microárea e registrada como realizada.
    [Tags]    UI    UiControleVetorial
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - CONTROLE VETORIAL - ACAO DE CONTROLE
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Animal e observação do agressor
    [Documentation]    O cão registrado entra em observação (ADR-0163).
    [Tags]    UI    UiControleVetorial
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - CONTROLE VETORIAL - ANIMAL E OBSERVACAO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - Exame de leishmaniose
    [Documentation]    Coleta e resultado reagente com o desfecho do cão (ADR-0163).
    [Tags]    UI    UiControleVetorial
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - CONTROLE VETORIAL - EXAME DE LEISHMANIOSE
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-006 - Vacinação antirrábica
    [Documentation]    O posto de vacinação entra nos registros do ano (ADR-0163).
    [Tags]    UI    UiControleVetorial
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - CONTROLE VETORIAL - VACINACAO ANTIRRABICA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
