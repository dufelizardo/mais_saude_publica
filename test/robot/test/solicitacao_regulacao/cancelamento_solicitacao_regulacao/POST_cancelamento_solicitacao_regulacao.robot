*** Settings ***
Resource    ../../../src/scenario/solicitacao_regulacao/cancelamento_solicitacao_regulacao/cancelamento_solicitacao_regulacao_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST CancelamentoSolicitacaoRegulacao
Metadata    Test Suite Description        This test suite validates the POST /api/v1/solicitacao-regulacao/{uuid}/cancelamento endpoint of the Mais Saúde Pública API (ADR-0087).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    CancelamentoSolicitacaoRegulacao    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-10-01
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST CancelamentoSolicitacaoRegulacao - HTTP 200 OK
    [Documentation]    Cancelamento na fila.
    [Tags]    POST    CancelamentoSolicitacaoRegulacao    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SOLICITACAO REGULACAO - CANCELAMENTO - POST    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST CancelamentoSolicitacaoRegulacao - HTTP 422 UNPROCESSABLE ENTITY (Ja Cancelada)
    [Documentation]    Cancelamento repetido.
    [Tags]    POST    CancelamentoSolicitacaoRegulacao    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SOLICITACAO REGULACAO - CANCELAMENTO - POST    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
