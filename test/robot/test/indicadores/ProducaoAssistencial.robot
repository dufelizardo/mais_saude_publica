*** Settings ***
Resource    ../../src/scenario/indicadores/producao_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - Producao Assistencial
Metadata    Test Suite Description        This test suite validates the care production by unit (ADR-0143) of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               ProducaoAssistencial    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-10-23
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Producao Assistencial - HTTP 200 (Producao Assistencial Producao Da Unidade)
    [Documentation]    A concluded atendimento in a fresh unit is counted, with the schema.
    [Tags]    ProducaoAssistencial    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PRODUCAO ASSISTENCIAL    200-producao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate Producao Assistencial - HTTP 400 (Producao Assistencial Periodo Invertido)
    [Documentation]    The start cannot be after the end.
    [Tags]    ProducaoAssistencial    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PRODUCAO ASSISTENCIAL    400-periodo
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate Producao Assistencial - HTTP 400 (Producao Assistencial Periodo Acima De Um Ano)
    [Documentation]    The period goes up to one year.
    [Tags]    ProducaoAssistencial    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PRODUCAO ASSISTENCIAL    400-ano
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate Producao Assistencial - HTTP 404 (Producao Assistencial Unidade Inexistente)
    [Documentation]    Unknown unit.
    [Tags]    ProducaoAssistencial    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PRODUCAO ASSISTENCIAL    404-unidade
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - Validate Producao Assistencial - HTTP 200 (Producao Assistencial Serie)
    [Documentation]    Thirty daily points and twelve monthly points by default.
    [Tags]    ProducaoAssistencial    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PRODUCAO ASSISTENCIAL    200-serie
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-006 - Validate Producao Assistencial - HTTP 400 (Producao Assistencial Serie Com Pontos Demais)
    [Documentation]    The daily series goes up to 90 points.
    [Tags]    ProducaoAssistencial    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PRODUCAO ASSISTENCIAL    400-serie
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-007 - Validate Producao Assistencial - HTTP 200 (Producao Assistencial Ranking)
    [Documentation]    The fresh unit is in the ranking with its count.
    [Tags]    ProducaoAssistencial    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PRODUCAO ASSISTENCIAL    200-unidades
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-008 - Validate Producao Assistencial - HTTP 200 (Producao Assistencial Exportacao)
    [Documentation]    The summary comes as CSV, with no patient.
    [Tags]    ProducaoAssistencial    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PRODUCAO ASSISTENCIAL    200-csv
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
