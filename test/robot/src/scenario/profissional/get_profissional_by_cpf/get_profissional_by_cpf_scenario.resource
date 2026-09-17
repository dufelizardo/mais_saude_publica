*** Settings ***
Resource    ../../../flow/profissional/get_profissional_by_cpf/get_profissional_by_cpf_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
PROFISSIONAL - GET BY CPF - GET
    [Documentation]    Test scenario for fetching a Profissional by cpf (GET /api/v1/profissional/{cpf}) on the
    ...    Mais Saúde Pública API.
    [Arguments]    ${meuCpf}    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Profissional - Get By Cpf - GET - By HTTP Status Code    ${meuCpf}    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
