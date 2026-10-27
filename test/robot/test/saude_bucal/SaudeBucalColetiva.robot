*** Settings ***
Resource    ../../src/scenario/saude_bucal/saude_bucal_coletiva_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - Saude Bucal Coletiva
Metadata    Test Suite Description        This test suite validates the collective oral health (ADR-0140) of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               SaudeBucalColetiva    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-10-22
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Saude Bucal Coletiva - HTTP 200 (Saude Bucal Coletiva Detalhe)
    [Documentation]    Fills the detail of a PSE collective action.
    [Tags]    SaudeBucalColetiva    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SAUDE BUCAL COLETIVA    200-detalhe
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate Saude Bucal Coletiva - HTTP 422 (Saude Bucal Coletiva Programa Sem Odontologia)
    [Documentation]    The detail is only for oral health and PSE programs.
    [Tags]    SaudeBucalColetiva    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SAUDE BUCAL COLETIVA    422-programa
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate Saude Bucal Coletiva - HTTP 400 (Saude Bucal Coletiva Sem Atividade)
    [Documentation]    The detail needs at least one activity.
    [Tags]    SaudeBucalColetiva    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SAUDE BUCAL COLETIVA    400-atividade
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate Saude Bucal Coletiva - HTTP 200 (Saude Bucal Coletiva Avaliacao E A Convocar)
    [Documentation]    The assessment with urgency puts the participant in the list to be called.
    [Tags]    SaudeBucalColetiva    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SAUDE BUCAL COLETIVA    200-avaliacao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - Validate Saude Bucal Coletiva - HTTP 422 (Saude Bucal Coletiva Sem Lista Nominal)
    [Documentation]    Without the participants list, there is no individual assessment.
    [Tags]    SaudeBucalColetiva    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SAUDE BUCAL COLETIVA    422-lista
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
