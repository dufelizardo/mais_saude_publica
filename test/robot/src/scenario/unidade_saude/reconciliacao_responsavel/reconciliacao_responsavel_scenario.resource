*** Settings ***
Resource    ../../../flow/unidade_saude/reconciliacao_responsavel/reconciliacao_responsavel_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UNIDADE SAUDE - RECONCILIACAO RESPONSAVEL - GET
    [Documentation]    Test scenario proving the synchronous "profissional chega depois" reconciliation
    ...    direction described in ADR-0014, end-to-end via GET /api/v1/unidade-saude/{nome} before and after
    ...    POST /api/v1/profissional/.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Reconciliacao Responsavel Unidade Saude - Antes E Depois Do Profissional Existir
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
