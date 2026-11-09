*** Settings ***
Resource    ../../src/scenario/urgencia/urgencia_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - Regulação de urgência
Metadata    Test Suite Description        This test suite validates the emergency regulation center (ADR-0176).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               Urgencia    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-11-08
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Urgencia - HTTP 200 (Do Chamado Ao Destino)
    [Documentation]    Chamado, regulação com recurso, deslocamentos, vaga zero e recebimento.
    [Tags]    Urgencia    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    REGULACAO DE URGENCIA    200-ciclo
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate Urgencia - HTTP 422 (Ordem Do Deslocamento)
    [Documentation]    Chegada antes do envio e passo repetido.
    [Tags]    Urgencia    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    REGULACAO DE URGENCIA    422-deslocamento
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate Urgencia - HTTP 400 (Regras Da Decisao)
    [Documentation]    Recurso sem tipo, vaga zero sem justificativa, central sem capacidade.
    [Tags]    Urgencia    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    REGULACAO DE URGENCIA    400-decisao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate Urgencia - HTTP 200 (Orientacao E Painel)
    [Documentation]    Orientação fecha; painel com aguardando primeiro.
    [Tags]    Urgencia    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    REGULACAO DE URGENCIA    200-orientacao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - Validate Urgencia - HTTP 201 (Acionamento Pela Crise)
    [Documentation]    A crise aciona o SAMU; segundo acionamento aberto conflita.
    [Tags]    Urgencia    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    REGULACAO DE URGENCIA    201-crise
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
