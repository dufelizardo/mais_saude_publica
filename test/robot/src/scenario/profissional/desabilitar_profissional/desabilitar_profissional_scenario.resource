*** Settings ***
Resource    ../../../flow/profissional/desabilitar_profissional/desabilitar_profissional_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
PROFISSIONAL - DESABILITAR - DELETE
    [Documentation]    Test scenario for enabling/disabling a Profissional
    ...    (DELETE /api/v1/profissional/des-habilitar/{cpf}) on the Mais Saúde Pública API.
    [Arguments]    ${meuCpf}    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Profissional - Desabilitar - DELETE - By HTTP Status Code    ${meuCpf}    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PROFISSIONAL - DESABILITAR - DELETE - Grava E Limpa Data De Desligamento
    [Documentation]    Test scenario for disabling a Profissional with a dataDesligamento and re-enabling it
    ...    afterward, expecting dataDesligamento cleared (ver ADR-0017).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    DELETE Desabilitar Profissional - Grava E Limpa Data De Desligamento
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
