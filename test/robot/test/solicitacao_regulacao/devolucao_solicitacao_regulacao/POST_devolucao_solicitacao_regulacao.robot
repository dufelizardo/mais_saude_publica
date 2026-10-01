*** Settings ***
Resource    ../../../src/scenario/solicitacao_regulacao/devolucao_solicitacao_regulacao/devolucao_solicitacao_regulacao_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST DevolucaoSolicitacaoRegulacao
Metadata    Test Suite Description        This test suite validates the POST /api/v1/solicitacao-regulacao/{uuid}/devolucao endpoint of the Mais Saúde Pública API (ADR-0087).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    DevolucaoSolicitacaoRegulacao    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-10-01
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST DevolucaoSolicitacaoRegulacao - HTTP 200 OK
    [Documentation]    Devolução com motivo.
    [Tags]    POST    DevolucaoSolicitacaoRegulacao    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SOLICITACAO REGULACAO - DEVOLUCAO - POST    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST DevolucaoSolicitacaoRegulacao - HTTP 400 BAD REQUEST (Sem Motivo)
    [Documentation]    Devolução sem motivo.
    [Tags]    POST    DevolucaoSolicitacaoRegulacao    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SOLICITACAO REGULACAO - DEVOLUCAO - POST    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
