*** Settings ***
Resource    ../../../flow/profissional/create_profissional/create_profissional_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
PROFISSIONAL - CREATE - POST
    [Documentation]    Test scenario for creating a Profissional (POST /api/v1/profissional/) on the Mais Saúde
    ...    Pública API.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Profissional - Create - POST - By HTTP Status Code    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PROFISSIONAL - CREATE - POST - Campos Obrigatorios Em Branco
    [Documentation]    Test scenario for creating a Profissional with blank required fields (cpf, nome, email),
    ...    expecting the field-validation HTTP 400 (as opposed to the duplicate-cpf HTTP 400).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    POST Create Profissional - Status Code 400 - Campos Obrigatorios Em Branco
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PROFISSIONAL - CREATE - POST - Corpo Ilegivel
    [Documentation]    Test scenario for creating a Profissional with a syntactically broken JSON body,
    ...    expecting the standard error contract HTTP 400 (AQUAQE-215).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    POST Create Profissional - Status Code 400 - Corpo Ilegivel
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
