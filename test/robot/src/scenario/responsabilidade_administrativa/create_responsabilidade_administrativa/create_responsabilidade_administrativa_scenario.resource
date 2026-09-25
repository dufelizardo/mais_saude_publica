*** Settings ***
Resource    ../../../flow/responsabilidade_administrativa/create_responsabilidade_administrativa/create_responsabilidade_administrativa_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
RESPONSABILIDADE ADMINISTRATIVA - CREATE - POST
    [Documentation]    Test scenario for creating a Responsabilidade Administrativa
    ...    (POST /api/v1/responsabilidade-administrativa/) on the Mais Saúde Pública API.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Responsabilidade Administrativa - Create - POST - By HTTP Status Code    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

RESPONSABILIDADE ADMINISTRATIVA - CREATE - POST - Setor Inexistente
    [Documentation]    Test scenario for creating a Responsabilidade Administrativa pointing at a setorId that
    ...    doesn't exist, expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    POST Create Responsabilidade Administrativa - Status Code 404 - Setor Inexistente
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
