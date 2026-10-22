*** Settings ***
Resource    ../../../src/scenario/programas/indicador/indicador_programatico_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - Indicador Programatico
Metadata    Test Suite Description        This test suite validates the program goals and indicators (ADR-0141) of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               IndicadorProgramatico    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-10-22
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Indicador Programatico - HTTP 200 (Indicador Programatico Indicadores)
    [Documentation]    The HiperDia indicators in a fresh unit, with the schema.
    [Tags]    IndicadorProgramatico    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    INDICADOR PROGRAMATICO    200-indicadores
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate Indicador Programatico - HTTP 201 (Indicador Programatico Meta Cadastrada)
    [Documentation]    The goal is registered and the indicator brings it.
    [Tags]    IndicadorProgramatico    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    INDICADOR PROGRAMATICO    201-meta
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate Indicador Programatico - HTTP 409 (Indicador Programatico Meta Repetida)
    [Documentation]    One goal per indicator, unit and period.
    [Tags]    IndicadorProgramatico    HTTP409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    INDICADOR PROGRAMATICO    409-meta
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate Indicador Programatico - HTTP 422 (Indicador Programatico Meta De Outro Programa)
    [Documentation]    The prenatal indicator does not apply to HiperDia.
    [Tags]    IndicadorProgramatico    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    INDICADOR PROGRAMATICO    422-meta
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - Validate Indicador Programatico - HTTP 400 (Indicador Programatico Meta Acima De 100)
    [Documentation]    A percent goal cannot exceed 100.
    [Tags]    IndicadorProgramatico    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    INDICADOR PROGRAMATICO    400-meta
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-006 - Validate Indicador Programatico - HTTP 200 (Indicador Programatico Serie)
    [Documentation]    Twelve monthly points by default.
    [Tags]    IndicadorProgramatico    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    INDICADOR PROGRAMATICO    200-serie
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-007 - Validate Indicador Programatico - HTTP 200 (Indicador Programatico Relatorio Csv)
    [Documentation]    The report comes as CSV, with no patient.
    [Tags]    IndicadorProgramatico    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    INDICADOR PROGRAMATICO    200-csv
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-008 - Validate Indicador Programatico - HTTP 204 (Indicador Programatico Meta Excluida)
    [Documentation]    The goal is configuration and can be deleted.
    [Tags]    IndicadorProgramatico    HTTP204
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    INDICADOR PROGRAMATICO    204-meta
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
