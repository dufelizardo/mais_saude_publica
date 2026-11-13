*** Settings ***
Resource    ../../src/scenario/qualidade/nao_conformidade_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - Não Conformidades
Metadata    Test Suite Description        Não conformidade, plano 5W2H e verificação de eficácia (ADR-0204).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    GET    NaoConformidade    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-11-12
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Origem Indicador - HTTP 400
    [Documentation]    A origem indicador sem o indicador e a competência dá 400 (ADR-0205).
    [Tags]    POST    NaoConformidade    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    NAO CONFORMIDADE    INDICADOR
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate Ciclo Eficaz - HTTP 200
    [Documentation]    Análise, ação concluída e a verificação eficaz encerra.
    [Tags]    POST    NaoConformidade    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    NAO CONFORMIDADE    EFICAZ
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate Nao Eficaz Abre Ciclo - HTTP 200
    [Documentation]    A não eficaz pede a observação e abre o ciclo 2.
    [Tags]    POST    NaoConformidade    HTTP200    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    NAO CONFORMIDADE    NAO_EFICAZ
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate Painel - HTTP 200
    [Documentation]    O painel conta as não conformidades.
    [Tags]    GET    NaoConformidade    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    NAO CONFORMIDADE    PAINEL
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
