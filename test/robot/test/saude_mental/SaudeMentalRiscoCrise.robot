*** Settings ***
Resource    ../../src/scenario/saude_mental/saude_mental_risco_crise_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - Saude Mental Risco Crise
Metadata    Test Suite Description        This test suite validates risk, crisis and psychiatric admission (ADR-0123) of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               SaudeMentalRiscoCrise    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-10-20
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Saude Mental Risco Crise - HTTP 201 (Risco)
    [Documentation]    Risco alto com medidas e reavaliação.
    [Tags]    SaudeMentalRiscoCrise    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SAUDE MENTAL RISCO CRISE    201-risco
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate Saude Mental Risco Crise - HTTP 400 (Risco Sem Medidas)
    [Documentation]    Moderado sem medidas.
    [Tags]    SaudeMentalRiscoCrise    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SAUDE MENTAL RISCO CRISE    400-risco
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate Saude Mental Risco Crise - HTTP 201 (Crise)
    [Documentation]    Crise ligada ao caso, notificação sugerida.
    [Tags]    SaudeMentalRiscoCrise    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SAUDE MENTAL RISCO CRISE    201-crise
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate Saude Mental Risco Crise - HTTP 422 (Encerrar Sem Pos Crise)
    [Documentation]    Encerrar pede o pós-crise.
    [Tags]    SaudeMentalRiscoCrise    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SAUDE MENTAL RISCO CRISE    422-encerrar
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - Validate Saude Mental Risco Crise - HTTP 200 (Notificacao)
    [Documentation]    Notificação registrada uma vez.
    [Tags]    SaudeMentalRiscoCrise    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SAUDE MENTAL RISCO CRISE    200-notificacao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-006 - Validate Saude Mental Risco Crise - HTTP 200 (Crises)
    [Documentation]    Lista de crises.
    [Tags]    SaudeMentalRiscoCrise    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SAUDE MENTAL RISCO CRISE    200-crises
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-007 - Validate Saude Mental Risco Crise - HTTP 400 (Compulsoria Sem Ordem)
    [Documentation]    Compulsória sem ordem judicial.
    [Tags]    SaudeMentalRiscoCrise    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SAUDE MENTAL RISCO CRISE    400-compulsoria
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-008 - Validate Saude Mental Risco Crise - HTTP 200 (Involuntaria)
    [Documentation]    Involuntária comunicada ao MP.
    [Tags]    SaudeMentalRiscoCrise    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SAUDE MENTAL RISCO CRISE    200-involuntaria
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
