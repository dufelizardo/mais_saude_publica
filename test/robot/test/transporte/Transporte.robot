*** Settings ***
Resource    ../../src/scenario/transporte/transporte_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - Transporte: frota e plantão
Metadata    Test Suite Description        This test suite validates the fleet, the crew shifts and the SAMU vehicle (ADR-0178).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               Transporte    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-11-08
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Transporte - HTTP 201 (Frota)
    [Documentation]    Veículo na unidade móvel, com o schema.
    [Tags]    Transporte    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    TRANSPORTE FROTA    201-frota
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate Transporte - HTTP 422 (Placa E Lotacao)
    [Documentation]    Placa inválida e viatura do SAMU fora da unidade móvel.
    [Tags]    Transporte    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    TRANSPORTE FROTA    422-lotacao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate Transporte - HTTP 201 (Plantao)
    [Documentation]    Tripulação mínima e tripulante em dois plantões.
    [Tags]    Transporte    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    TRANSPORTE FROTA    201-plantao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate Transporte - HTTP 200 (Viatura No Samu)
    [Documentation]    Empenho, em serviço no envio e livre no fim.
    [Tags]    Transporte    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    TRANSPORTE FROTA    200-samu
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
