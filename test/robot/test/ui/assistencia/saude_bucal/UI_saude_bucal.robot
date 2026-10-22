*** Settings ***
Resource    ../../../../src/scenario/ui/assistencia/saude_bucal/saude_bucal_ui_scenario.resource
Resource    ../../../../src/scenario/common/ui_sessao_scenario.resource
Suite Setup       UI - ABRIR SISTEMA
Suite Teardown    UI - FECHAR SISTEMA
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - UI Assistência - Saúde Bucal
Metadata    Test Suite Description        Valida a avaliação odontológica no atendimento e o catálogo de especialidades (ADR-0134).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               UI    UiSaudeBucal    MaisSaudePublicaFrontend
Metadata    Test Suite Created On         2026-10-21
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Avaliação odontológica pela tela
    [Documentation]    Risco obrigatório; avaliação registrada e retificada (ADR-0134).
    [Tags]    UI    UiSaudeBucal
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - SAUDE BUCAL - AVALIACAO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Catálogo de especialidades
    [Documentation]    Incluir e desativar uma especialidade (ADR-0134).
    [Tags]    UI    UiSaudeBucal
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - SAUDE BUCAL - ESPECIALIDADES
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Marcar o odontograma
    [Documentation]    Cárie na face e dente ausente, salvos na avaliação e no histórico (ADR-0135).
    [Tags]    UI    UiSaudeBucal
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - SAUDE BUCAL - ODONTOGRAMA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Registrar a restauração pelo procedimento
    [Documentation]    A restauração em resina na oclusal do 36, pelo catálogo, grava Restaurado no odontograma (ADR-0136).
    [Tags]    UI    UiSaudeBucal
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - SAUDE BUCAL - PROCEDIMENTO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - Catálogo de procedimentos
    [Documentation]    Incluir um procedimento odontológico no catálogo e desativá-lo (ADR-0136).
    [Tags]    UI    UiSaudeBucal
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - SAUDE BUCAL - CATALOGO DE PROCEDIMENTOS
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-006 - Plano de tratamento
    [Documentation]    Abrir o plano, executar a restauração pela gaveta, cancelar o flúor e concluir o tratamento (ADR-0137).
    [Tags]    UI    UiSaudeBucal
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - SAUDE BUCAL - PLANO DE TRATAMENTO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-007 - Encaminhar o item do plano pela Regulação
    [Documentation]    A pulpotomia do 46 encaminhada à endodontia do CEO pela tela Regulação; o item fica encaminhado (ADR-0138).
    [Tags]    UI    UiSaudeBucal
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - SAUDE BUCAL - ENCAMINHAR PELA REGULACAO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-008 - Tratamento protético
    [Documentation]    A coroa do 46: moldagem, laboratório contratado, entrega e conclusão (ADR-0139).
    [Tags]    UI    UiSaudeBucal
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - SAUDE BUCAL - PROTESE
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-009 - Saúde bucal coletiva na escola
    [Documentation]    A escovação supervisionada e o flúor na ação do PSE, com a escola e os kits (ADR-0140).
    [Tags]    UI    UiSaudeBucal
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - SAUDE BUCAL - COLETIVA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
