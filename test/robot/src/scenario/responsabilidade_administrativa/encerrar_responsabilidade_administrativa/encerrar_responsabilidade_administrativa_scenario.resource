*** Settings ***
Resource    ../../../flow/responsabilidade_administrativa/encerrar_responsabilidade_administrativa/encerrar_responsabilidade_administrativa_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
RESPONSABILIDADE ADMINISTRATIVA - ENCERRAR - PATCH
    [Documentation]    Test scenario for encerrar-ing a Responsabilidade Administrativa
    ...    (PATCH /api/v1/responsabilidade-administrativa/{uuid}/encerrar) on the Mais Saúde Pública API.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Responsabilidade Administrativa - Encerrar - PATCH - By HTTP Status Code    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
