*** Settings ***
Resource    ../../src/scenario/rede/cobertura_rede_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - GET Cobertura da Rede
Metadata    Test Suite Description        Cobertura da rede e indicadores de pactuação (ADR-0198).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               GET    CoberturaRede    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-11-12
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Cobertura Da Competencia - HTTP 200
    [Documentation]    Oferta com cota e uma autorização: pactuado, usado e o saldo da origem; o relatório do #19.
    [Tags]    GET    CoberturaRede    HTTP200
    COBERTURA DA REDE    COMPETENCIA
