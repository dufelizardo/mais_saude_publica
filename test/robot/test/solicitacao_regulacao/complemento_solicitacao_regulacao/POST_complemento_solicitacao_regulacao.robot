*** Settings ***
Resource    ../../../src/scenario/solicitacao_regulacao/complemento_solicitacao_regulacao/complemento_solicitacao_regulacao_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST ComplementoSolicitacaoRegulacao
Metadata    Test Suite Description        This test suite validates the POST /api/v1/solicitacao-regulacao/{uuid}/complemento endpoint of the Mais Saúde Pública API (ADR-0087).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    ComplementoSolicitacaoRegulacao    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-10-01
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST ComplementoSolicitacaoRegulacao - HTTP 200 OK
    [Documentation]    Complemento de solicitação devolvida.
    [Tags]    POST    ComplementoSolicitacaoRegulacao    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SOLICITACAO REGULACAO - COMPLEMENTO - POST    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST ComplementoSolicitacaoRegulacao - HTTP 422 UNPROCESSABLE ENTITY (Nao Devolvida)
    [Documentation]    Complemento sem devolução.
    [Tags]    POST    ComplementoSolicitacaoRegulacao    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SOLICITACAO REGULACAO - COMPLEMENTO - POST    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
