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
