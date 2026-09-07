*** Settings ***
Resource    ../../../flow/municipal/create_municipal/create_municipal_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
MUNICIPAL - CREATE - POST
    [Documentation]    Test scenario for creating a Municipal unit (POST /api/v1/municipal/) on the Mais Saúde
    ...    Pública API.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Municipal - Create - POST - By HTTP Status Code    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

MUNICIPAL - CREATE - POST - Campos Obrigatorios Em Branco
    [Documentation]    Test scenario for creating a Municipal unit with blank required fields (nome, email),
    ...    expecting the field-validation HTTP 400 (as opposed to the duplicate-name HTTP 400).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    POST Create Municipal - Status Code 400 - Campos Obrigatorios Em Branco
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

MUNICIPAL - CREATE - POST - Corpo Ilegivel
    [Documentation]    Test scenario for creating a Municipal unit with a syntactically broken JSON body,
    ...    expecting the standard error contract HTTP 400 (AQUAQE-215).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    POST Create Municipal - Status Code 400 - Corpo Ilegivel
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

MUNICIPAL - CREATE - POST - Superior Nivel Errado
    [Documentation]    Test scenario for creating a Municipal unit whose administracaoSuperior points at a unit
    ...    of the wrong hierarchy level (Municipal instead of Estadual), expecting HTTP 422 (AQUAQE-22).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    POST Create Municipal - Status Code 422 - Superior Nivel Errado
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
