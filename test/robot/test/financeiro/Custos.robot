*** Settings ***
Resource    ../../src/scenario/financeiro/custos_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - Custos
Metadata    Test Suite Description        Centros de custo, apuração mensal e indicadores de custo (ADR-0220).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    PUT    GET    Custos    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-11-19
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Centros De Custo - HTTP 201 409 404 E 422
    [Documentation]    O geral da unidade, o do setor, o repetido 409, o setor inexistente 404 e o geral não se desativa.
    [Tags]    Custos    HTTP201    HTTP409    HTTP404    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    CUSTOS    CENTROS
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate Apuracao Da Competencia - HTTP 422 400 200 E 404
    [Documentation]    O mês corrente 422; o formato 400; apurar, refazer, fechar e não mudar mais.
    [Tags]    Custos    HTTP422    HTTP400    HTTP200    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    CUSTOS    APURACAO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
