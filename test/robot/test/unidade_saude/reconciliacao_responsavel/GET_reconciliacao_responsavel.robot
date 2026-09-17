*** Settings ***
Resource    ../../../src/scenario/unidade_saude/reconciliacao_responsavel/reconciliacao_responsavel_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - GET Reconciliacao Responsavel Unidade Saude
Metadata    Test Suite Description        This test suite validates the synchronous CPF-based reconciliation between Unidade de Saúde and Profissional described in ADR-0014, on the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               GET    ReconciliacaoResponsavelUnidadeSaude    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-17
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Only exercises the synchronous "profissional chega depois" direction of the reconciliation (a Unidade de
    Saúde is created first, referencing a CPF that doesn't exist yet as Profissional; the Profissional is
    created afterwards, and ProfissionalService.create() reconciles it on the spot). The scheduled-job
    direction (ReconciliacaoResponsavelScheduler) depends on backend timing and can't be exercised from Robot.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Reconciliacao Responsavel Unidade Saude - Profissional Chega Depois
    [Documentation]    Test case to validate that a Unidade de Saúde created with a responsavelCpf pointing at
    ...    a Profissional that doesn't exist yet has an empty responsavelNome, and that creating the matching
    ...    Profissional afterwards reconciles the link so a subsequent GET shows responsavelNome populated.
    [Tags]    GET    ReconciliacaoResponsavelUnidadeSaude    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UNIDADE SAUDE - RECONCILIACAO RESPONSAVEL - GET
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
