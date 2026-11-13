*** Settings ***
Resource    ../../src/scenario/imunizacao/imunizacao_rede_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - Imunização na Rede
Metadata    Test Suite Description        Remanejamento entre salas, suspensão do lote na rede e revogação da contraindicação pelo CRIE (ADR-0206).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    GET    ImunizacaoRede    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-11-13
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Remanejamento Recebido - HTTP 200
    [Documentation]    Recusado, as doses voltam; recebido na faixa, o lote nasce no destino.
    [Tags]    POST    ImunizacaoRede    HTTP200    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IMUNIZACAO REDE    RECEBIDO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate Recebido Fora da Faixa - HTTP 200
    [Documentation]    A caixa fora de 2 a 8 °C deixa o lote sob suspeita.
    [Tags]    POST    ImunizacaoRede    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IMUNIZACAO REDE    FORA_DA_FAIXA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate Regras do Envio - HTTP 422
    [Documentation]    A própria unidade dá 400; doses além do lote, 422.
    [Tags]    POST    ImunizacaoRede    HTTP400    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IMUNIZACAO REDE    REGRAS
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate Suspensao na Rede - HTTP 201
    [Documentation]    O lote fica sob suspeita (409 ao repetir), aparece nos alertas e volta ao liberar.
    [Tags]    POST    GET    ImunizacaoRede    HTTP201    HTTP409    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IMUNIZACAO REDE    SUSPENSAO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - Validate Revogacao pelo CRIE - HTTP 200
    [Documentation]    A revogação pede o parecer; revogar de novo dá 422.
    [Tags]    POST    ImunizacaoRede    HTTP200    HTTP400    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IMUNIZACAO REDE    CRIE
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
