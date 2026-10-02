*** Settings ***
Resource    ../../../src/scenario/solicitacao_regulacao/falta_solicitacao_regulacao/falta_solicitacao_regulacao_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST FaltaSolicitacaoRegulacao
Metadata    Test Suite Description        This test suite validates the POST /api/v1/solicitacao-regulacao/{uuid}/falta endpoint of the Mais Saúde Pública API (ADR-0087).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    FaltaSolicitacaoRegulacao    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-10-01
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST FaltaSolicitacaoRegulacao - HTTP 200 OK
    [Documentation]    Falta registrada.
    [Tags]    POST    FaltaSolicitacaoRegulacao    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SOLICITACAO REGULACAO - FALTA - POST    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST FaltaSolicitacaoRegulacao - HTTP 422 UNPROCESSABLE ENTITY (Nao Agendada)
    [Documentation]    Falta sem agendamento.
    [Tags]    POST    FaltaSolicitacaoRegulacao    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SOLICITACAO REGULACAO - FALTA - POST    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
