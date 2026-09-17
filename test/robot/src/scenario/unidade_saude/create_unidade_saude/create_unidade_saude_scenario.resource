*** Settings ***
Resource    ../../../flow/unidade_saude/create_unidade_saude/create_unidade_saude_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UNIDADE SAUDE - CREATE - POST
    [Documentation]    Test scenario for creating a Unidade de Saúde unit (POST /api/v1/unidade-saude/) on the
    ...    Mais Saúde Pública API.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Unidade Saude - Create - POST - By HTTP Status Code    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UNIDADE SAUDE - CREATE - POST - Tipo HOSPITAL Com Supervisao Regional
    [Documentation]    Test scenario for creating a Unidade de Saúde unit with tipo=HOSPITAL and an optional
    ...    supervisaoRegional lateral link informed, expecting HTTP 201 CREATED. Covers both the HOSPITAL tipo
    ...    value and the "with supervisaoRegional" variant in a single scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Seed A Unidade Saude Unit With Supervisao Regional
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UNIDADE SAUDE - CREATE - POST - Campos Obrigatorios Em Branco
    [Documentation]    Test scenario for creating a Unidade de Saúde unit with blank required fields (nome,
    ...    email), expecting the field-validation HTTP 400 (as opposed to the duplicate-name HTTP 400).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    POST Create Unidade Saude - Status Code 400 - Campos Obrigatorios Em Branco
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UNIDADE SAUDE - CREATE - POST - Corpo Ilegivel
    [Documentation]    Test scenario for creating a Unidade de Saúde unit with a syntactically broken JSON body,
    ...    expecting the standard error contract HTTP 400 (AQUAQE-215).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    POST Create Unidade Saude - Status Code 400 - Corpo Ilegivel
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UNIDADE SAUDE - CREATE - POST - Superior Nivel Errado
    [Documentation]    Test scenario for creating a Unidade de Saúde unit whose administracaoSuperior points at
    ...    a unit of the wrong hierarchy level (Estadual instead of Municipal), expecting HTTP 422 (AQUAQE-22).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    POST Create Unidade Saude - Status Code 422 - Superior Nivel Errado
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
