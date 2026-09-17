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
