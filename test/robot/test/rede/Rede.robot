*** Settings ***
Resource    ../../src/scenario/rede/rede_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - Gestão da Rede: oferta e PPI
Metadata    Test Suite Description        This test suite validates the offer, the PPI quotas and their use by the regulation (ADR-0179).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               Rede    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-11-08
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Rede - HTTP 201 (Oferta E Cota)
    [Documentation]    Oferta, cota, oferta repetida e cotas acima da oferta.
    [Tags]    Rede    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    GESTAO DA REDE PPI    201-oferta
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate Rede - HTTP 200 (Regulacao Consome A Cota)
    [Documentation]    A autorização consome a cota; sem saldo, só extracota justificada.
    [Tags]    Rede    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    GESTAO DA REDE PPI    200-regulacao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate Rede - HTTP 200 (Painel E Copia)
    [Documentation]    Painel do mês e cópia para o mês seguinte.
    [Tags]    Rede    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    GESTAO DA REDE PPI    200-painel
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
