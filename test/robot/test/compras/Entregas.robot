*** Settings ***
Resource    ../../src/scenario/compras/entregas_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - Entregas
Metadata    Test Suite Description        Ordens de fornecimento, recebimento, ocorrências e painel de Compras (ADR-0214).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    GET    Entregas    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-11-18
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Ordem Dentro Do Saldo - HTTP 422 E 201
    [Documentation]    Acima do saldo dá 422; a ordem reserva o saldo.
    [Tags]    Entregas    HTTP422    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ENTREGAS    ORDEM
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate Recebimento Provisorio E Definitivo - HTTP 200
    [Documentation]    Recusa parcial: ordem parcial, ocorrência e entrada no Estoque.
    [Tags]    Entregas    HTTP201    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ENTREGAS    RECEBIMENTO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate Ocorrencias - HTTP 400 E 200
    [Documentation]    O atraso não se registra à mão; outra ocorrência registra e encerra.
    [Tags]    Entregas    HTTP400    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ENTREGAS    OCORRENCIAS
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate Painel De Compras - HTTP 200 E 400
    [Documentation]    O painel responde; período invertido dá 400.
    [Tags]    Entregas    HTTP200    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ENTREGAS    PAINEL
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
