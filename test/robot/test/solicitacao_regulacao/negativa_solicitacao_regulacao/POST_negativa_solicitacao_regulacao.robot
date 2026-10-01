*** Settings ***
Resource    ../../../src/scenario/solicitacao_regulacao/negativa_solicitacao_regulacao/negativa_solicitacao_regulacao_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST NegativaSolicitacaoRegulacao
Metadata    Test Suite Description        This test suite validates the POST /api/v1/solicitacao-regulacao/{uuid}/negativa endpoint of the Mais Saúde Pública API (ADR-0087).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    NegativaSolicitacaoRegulacao    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-10-01
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST NegativaSolicitacaoRegulacao - HTTP 200 OK
    [Documentation]    Negativa com motivo.
    [Tags]    POST    NegativaSolicitacaoRegulacao    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SOLICITACAO REGULACAO - NEGATIVA - POST    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST NegativaSolicitacaoRegulacao - HTTP 422 UNPROCESSABLE ENTITY (Proprio Solicitante)
    [Documentation]    Solicitante tentando negar.
    [Tags]    POST    NegativaSolicitacaoRegulacao    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SOLICITACAO REGULACAO - NEGATIVA - POST    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
