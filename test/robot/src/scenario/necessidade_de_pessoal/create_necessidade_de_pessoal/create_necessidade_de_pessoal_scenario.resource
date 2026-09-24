*** Settings ***
Resource    ../../../flow/necessidade_de_pessoal/create_necessidade_de_pessoal/create_necessidade_de_pessoal_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
NECESSIDADE DE PESSOAL - CREATE - POST
    [Documentation]    Test scenario for creating a Necessidade De Pessoal (POST /api/v1/necessidade-de-pessoal/)
    ...    on the Mais Saúde Pública API.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Necessidade De Pessoal - Create - POST - By HTTP Status Code    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

NECESSIDADE DE PESSOAL - CREATE - POST - Cargo Inexistente
    [Documentation]    Test scenario for creating a Necessidade De Pessoal pointing at a cargoId that doesn't
    ...    exist, expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    POST Create Necessidade De Pessoal - Status Code 404 - Cargo Inexistente
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

NECESSIDADE DE PESSOAL - CREATE - POST - Setor Inexistente
    [Documentation]    Test scenario for creating a Necessidade De Pessoal pointing at a setorId that doesn't
    ...    exist, expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    POST Create Necessidade De Pessoal - Status Code 404 - Setor Inexistente
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
