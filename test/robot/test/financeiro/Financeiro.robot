*** Settings ***
Resource    ../../src/scenario/financeiro/financeiro_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - Financeiro
Metadata    Test Suite Description        Dotações, empenhos e a ordem de fornecimento com o empenho (ADR-0218).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    GET    Financeiro    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-11-19
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Dotacao - HTTP 400 409 E 201
    [Documentation]    Exercício fora do intervalo dá 400; a dotação é única.
    [Tags]    Financeiro    HTTP400    HTTP409    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    FINANCEIRO    DOTACAO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate Alteracao Da Dotacao - HTTP 201 E 422
    [Documentation]    A suplementação soma; a anulação acima do disponível dá 422.
    [Tags]    Financeiro    HTTP201    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    FINANCEIRO    ALTERACAO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate Empenho - HTTP 422 409 E 201
    [Documentation]    Acima do disponível dá 422; o número é único no exercício.
    [Tags]    Financeiro    HTTP422    HTTP409    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    FINANCEIRO    EMPENHO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate Ordem Com Empenho - HTTP 422 E 201
    [Documentation]    A ordem consome o saldo do empenho; acima dele dá 422.
    [Tags]    Financeiro    HTTP422    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    FINANCEIRO    ORDEM
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

