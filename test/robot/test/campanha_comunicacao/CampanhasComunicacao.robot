*** Settings ***
Resource    ../../src/scenario/campanha_comunicacao/campanha_comunicacao_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - Campanhas de comunicação
Metadata    Test Suite Description        This test suite validates the communication campaigns and materials (ADR-0174).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               CampanhasComunicacao    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-11-08
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Campanhas - HTTP 201 (Criar)
    [Documentation]    Nasce planejada.
    [Tags]    CampanhasComunicacao    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    CAMPANHAS DE COMUNICACAO    201-criar
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate Campanhas - HTTP 200 (Ciclo E Portal)
    [Documentation]    Inicia, aparece no portal e encerra com o balanço.
    [Tags]    CampanhasComunicacao    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    CAMPANHAS DE COMUNICACAO    200-ciclo
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate Campanhas - HTTP 422 (Sem Canal)
    [Documentation]    Sem canal, não inicia.
    [Tags]    CampanhasComunicacao    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    CAMPANHAS DE COMUNICACAO    422-canal
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate Campanhas - HTTP 422 (Periodo)
    [Documentation]    O comunicado cabe no período da campanha.
    [Tags]    CampanhasComunicacao    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    CAMPANHAS DE COMUNICACAO    422-periodo
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - Validate Campanhas - HTTP 400 (Material)
    [Documentation]    Link do material só https.
    [Tags]    CampanhasComunicacao    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    CAMPANHAS DE COMUNICACAO    400-material
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

