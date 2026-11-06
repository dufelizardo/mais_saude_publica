*** Settings ***
Resource    ../../../src/scenario/equipamentos/pontos_operacionais/pontos_operacionais_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - PontosOperacionais
Metadata    Test Suite Description        This test suite validates the operational points, routes and care setting (ADR-0166) of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               PontosOperacionais    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-11-06
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate PontosOperacionais - HTTP 200 (Unidade)
    [Documentation]    Ponto em unidade de atendimento: inclui, ajusta e encerra.
    [Tags]    PontosOperacionais    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PONTOS OPERACIONAIS    200-unidade
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate PontosOperacionais - HTTP 409 (Nome Repetido)
    [Documentation]    Nome único entre os ativos.
    [Tags]    PontosOperacionais    HTTP409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PONTOS OPERACIONAIS    409-nome
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate PontosOperacionais - HTTP 422 (Sem Perfil Movel)
    [Documentation]    Base e rota exigem o perfil Atenção móvel.
    [Tags]    PontosOperacionais    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PONTOS OPERACIONAIS    422-perfil
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate PontosOperacionais - HTTP 200 (Unidade Movel)
    [Documentation]    Base, rota em ordem, passagens e encerramento.
    [Tags]    PontosOperacionais    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PONTOS OPERACIONAIS    200-movel
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - Validate PontosOperacionais - HTTP 409 (Ponto Repetido)
    [Documentation]    O ponto não se repete na rota.
    [Tags]    PontosOperacionais    HTTP409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PONTOS OPERACIONAIS    409-rota
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-006 - Validate PontosOperacionais - HTTP 404 (Inexistente)
    [Documentation]    Ponto que não existe.
    [Tags]    PontosOperacionais    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PONTOS OPERACIONAIS    404-ponto
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-007 - Validate PontosOperacionais - HTTP 201 E 400 (Local Do Atendimento)
    [Documentation]    Local de realização com descrição e com ponto da unidade.
    [Tags]    PontosOperacionais    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PONTOS OPERACIONAIS    201-local
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

