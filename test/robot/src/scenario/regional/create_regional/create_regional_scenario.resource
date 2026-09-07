*** Settings ***
Resource    ../../../flow/regional/create_regional/create_regional_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
REGIONAL - CREATE - POST
    [Documentation]    Test scenario for creating a Regional unit (POST /api/v1/regional/) on the Mais Saúde
    ...    Pública API.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Regional - Create - POST - By HTTP Status Code    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

REGIONAL - CREATE - POST - Campos Obrigatorios Em Branco
    [Documentation]    Test scenario for creating a Regional unit with blank required fields (nome, email),
    ...    expecting the field-validation HTTP 400 (as opposed to the duplicate-name HTTP 400).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    POST Create Regional - Status Code 400 - Campos Obrigatorios Em Branco
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

REGIONAL - CREATE - POST - Corpo Ilegivel
    [Documentation]    Test scenario for creating a Regional unit with a syntactically broken JSON body,
    ...    expecting the standard error contract HTTP 400 (AQUAQE-215).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    POST Create Regional - Status Code 400 - Corpo Ilegivel
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

REGIONAL - CREATE - POST - Superior Nivel Errado
    [Documentation]    Test scenario for creating a Regional unit whose administracaoSuperior points at a unit
    ...    of the wrong hierarchy level (Federal instead of Municipal), expecting HTTP 422 (AQUAQE-22).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    POST Create Regional - Status Code 422 - Superior Nivel Errado
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
