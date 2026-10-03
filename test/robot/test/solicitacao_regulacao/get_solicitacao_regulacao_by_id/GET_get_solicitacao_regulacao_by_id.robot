*** Settings ***
Resource    ../../../src/scenario/solicitacao_regulacao/get_solicitacao_regulacao_by_id/get_solicitacao_regulacao_by_id_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - GET GetSolicitacaoRegulacaoById
Metadata    Test Suite Description        This test suite validates the GET /api/v1/solicitacao-regulacao/{uuid} endpoint of the Mais Saúde Pública API (ADR-0087).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               GET    GetSolicitacaoRegulacaoById    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-10-01
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate GET GetSolicitacaoRegulacaoById - HTTP 200 OK
    [Documentation]    Detalhe clínico com eventos.
    [Tags]    GET    GetSolicitacaoRegulacaoById    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SOLICITACAO REGULACAO - GET BY ID - GET    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate GET GetSolicitacaoRegulacaoById - HTTP 404 NOT FOUND
    [Documentation]    Solicitação inexistente.
    [Tags]    GET    GetSolicitacaoRegulacaoById    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SOLICITACAO REGULACAO - GET BY ID - GET    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
