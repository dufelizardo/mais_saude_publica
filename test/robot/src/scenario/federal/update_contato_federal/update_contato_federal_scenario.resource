*** Settings ***
Resource    ../../../flow/federal/update_contato_federal/update_contato_federal_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
FEDERAL - UPDATE CONTATO - PATCH
    [Documentation]    Test scenario for updating a Federal unit's contato (PATCH /api/v1/federal/contato/{nome})
    ...    on the Mais Saúde Pública API.
    [Arguments]    ${meuNome}    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Federal - Update Contato - PATCH - By HTTP Status Code    ${meuNome}    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

FEDERAL - UPDATE CONTATO - PATCH - Campos Obrigatorios Em Branco
    [Documentation]    Test scenario for updating a Federal unit's contato with a blank email, expecting the
    ...    field-validation HTTP 400 (as opposed to the not-found HTTP 404).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PATCH Update Contato Federal - Status Code 400 - Campos Obrigatorios Em Branco
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

FEDERAL - UPDATE CONTATO - PATCH - Corpo Ilegivel
    [Documentation]    Test scenario for updating a Federal unit's contato with a syntactically broken JSON body,
    ...    expecting the standard error contract HTTP 400 (AQUAQE-215).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PATCH Update Contato Federal - Status Code 400 - Corpo Ilegivel
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
