*** Settings ***
Resource    ../../../flow/municipal/update_nome_municipal/update_nome_municipal_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
MUNICIPAL - UPDATE NOME - PATCH
    [Documentation]    Test scenario for updating a Municipal unit's nome (PATCH /api/v1/municipal/{nome}) on
    ...    the Mais Saúde Pública API.
    [Arguments]    ${meuNome}    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Municipal - Update Nome - PATCH - By HTTP Status Code    ${meuNome}    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

MUNICIPAL - UPDATE NOME - PATCH - Campos Obrigatorios Em Branco
    [Documentation]    Test scenario for renaming a Municipal unit to a blank nome, expecting the
    ...    field-validation HTTP 400 (as opposed to the not-found HTTP 404).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PATCH Update Nome Municipal - Status Code 400 - Campos Obrigatorios Em Branco
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

MUNICIPAL - UPDATE NOME - PATCH - Corpo Ilegivel
    [Documentation]    Test scenario for renaming a Municipal unit with a syntactically broken JSON body,
    ...    expecting the standard error contract HTTP 400 (AQUAQE-215).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PATCH Update Nome Municipal - Status Code 400 - Corpo Ilegivel
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
