*** Settings ***
Resource    ../../src/scenario/indicadores/indicadores_rede_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - Indicadores Da Rede
Metadata    Test Suite Description        This test suite validates the network indicators (ADR-0146) of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               IndicadoresDaRede    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-10-26
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Indicadores Da Rede - HTTP 200 (Indicadores Da Rede Programas)
    [Documentation]    HiperDia in a fresh unit with one enrolled patient, with the schema.
    [Tags]    IndicadoresDaRede    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    INDICADORES DA REDE    200-programas
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate Indicadores Da Rede - HTTP 400 (Indicadores Da Rede Periodo Invertido)
    [Documentation]    The start cannot be after the end.
    [Tags]    IndicadoresDaRede    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    INDICADORES DA REDE    400-programas
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate Indicadores Da Rede - HTTP 200 (Indicadores Da Rede Programa Por Unidade)
    [Documentation]    The fresh unit is listed in the program by unit.
    [Tags]    IndicadoresDaRede    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    INDICADORES DA REDE    200-por-unidade
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate Indicadores Da Rede - HTTP 404 (Indicadores Da Rede Programa Inexistente)
    [Documentation]    Unknown program.
    [Tags]    IndicadoresDaRede    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    INDICADORES DA REDE    404-programa
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - Validate Indicadores Da Rede - HTTP 200 (Indicadores Da Rede Vigilancia)
    [Documentation]    Surveillance with twelve epidemiological weeks, with the schema.
    [Tags]    IndicadoresDaRede    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    INDICADORES DA REDE    200-vigilancia
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-006 - Validate Indicadores Da Rede - HTTP 200 (Indicadores Da Rede Exportacao)
    [Documentation]    The summary comes as CSV, with no patient.
    [Tags]    IndicadoresDaRede    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    INDICADORES DA REDE    200-csv
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
