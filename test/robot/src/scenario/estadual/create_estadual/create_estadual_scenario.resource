*** Settings ***
Resource    ../../../flow/estadual/create_estadual/create_estadual_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
ESTADUAL - CREATE - POST
    [Documentation]    Test scenario for creating an Estadual unit (POST /api/v1/estadual/) on the Mais Saúde
    ...    Pública API.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Estadual - Create - POST - By HTTP Status Code    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

ESTADUAL - CREATE - POST - Campos Obrigatorios Em Branco
    [Documentation]    Test scenario for creating an Estadual unit with blank required fields (nome, email),
    ...    expecting the field-validation HTTP 400 (as opposed to the duplicate-name HTTP 400).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    POST Create Estadual - Status Code 400 - Campos Obrigatorios Em Branco
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

ESTADUAL - CREATE - POST - Corpo Ilegivel
    [Documentation]    Test scenario for creating an Estadual unit with a syntactically broken JSON body,
    ...    expecting the standard error contract HTTP 400 (AQUAQE-215).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    POST Create Estadual - Status Code 400 - Corpo Ilegivel
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

ESTADUAL - CREATE - POST - Superior Nivel Errado
    [Documentation]    Test scenario for creating an Estadual unit whose administracaoSuperior points at a unit
    ...    of the wrong hierarchy level (Estadual instead of Federal), expecting HTTP 422 (AQUAQE-22).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    POST Create Estadual - Status Code 422 - Superior Nivel Errado
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
