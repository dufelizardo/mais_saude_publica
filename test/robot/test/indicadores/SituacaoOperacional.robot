*** Settings ***
Resource    ../../src/scenario/indicadores/situacao_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - Situacao Operacional
Metadata    Test Suite Description        This test suite validates the operational situation (ADR-0144) of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               SituacaoOperacional    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-10-23
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Situacao Operacional - HTTP 200 (Situacao Operacional Agora)
    [Documentation]    A walk-in atendimento in a fresh unit is waiting for triage, with the schema.
    [Tags]    SituacaoOperacional    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SITUACAO OPERACIONAL    200-agora
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate Situacao Operacional - HTTP 404 (Situacao Operacional Unidade Inexistente)
    [Documentation]    Unknown unit.
    [Tags]    SituacaoOperacional    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SITUACAO OPERACIONAL    404-unidade
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate Situacao Operacional - HTTP 200 (Situacao Operacional Espera)
    [Documentation]    The arrival is recorded and counted in the period.
    [Tags]    SituacaoOperacional    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SITUACAO OPERACIONAL    200-espera
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate Situacao Operacional - HTTP 400 (Situacao Operacional Espera Periodo Invertido)
    [Documentation]    The start cannot be after the end.
    [Tags]    SituacaoOperacional    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SITUACAO OPERACIONAL    400-espera
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - Validate Situacao Operacional - HTTP 200 (Situacao Operacional Ocupacao)
    [Documentation]    A fresh unit has no schedule slots.
    [Tags]    SituacaoOperacional    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SITUACAO OPERACIONAL    200-ocupacao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-006 - Validate Situacao Operacional - HTTP 400 (Situacao Operacional Ocupacao Horizonte Longo)
    [Documentation]    The horizon goes up to 31 days.
    [Tags]    SituacaoOperacional    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SITUACAO OPERACIONAL    400-ocupacao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-007 - Validate Situacao Operacional - HTTP 200 (Situacao Operacional Escala Sem Cobertura)
    [Documentation]    An open slot in the next hour is an occurrence and an alert.
    [Tags]    SituacaoOperacional    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SITUACAO OPERACIONAL    200-escala
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
