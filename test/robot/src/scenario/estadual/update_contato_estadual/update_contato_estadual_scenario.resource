*** Settings ***
Resource    ../../../flow/estadual/update_contato_estadual/update_contato_estadual_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
ESTADUAL - UPDATE CONTATO - PATCH
    [Documentation]    Test scenario for updating an Estadual unit's contato (PATCH /api/v1/estadual/contato/{nome})
    ...    on the Mais Saúde Pública API.
    [Arguments]    ${meuNome}    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Estadual - Update Contato - PATCH - By HTTP Status Code    ${meuNome}    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

ESTADUAL - UPDATE CONTATO - PATCH - Campos Obrigatorios Em Branco
    [Documentation]    Test scenario for updating an Estadual unit's contato with a blank email, expecting the
    ...    field-validation HTTP 400 (as opposed to the not-found HTTP 404).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PATCH Update Contato Estadual - Status Code 400 - Campos Obrigatorios Em Branco
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

ESTADUAL - UPDATE CONTATO - PATCH - Corpo Ilegivel
    [Documentation]    Test scenario for updating an Estadual unit's contato with a syntactically broken JSON
    ...    body, expecting the standard error contract HTTP 400 (AQUAQE-215).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PATCH Update Contato Estadual - Status Code 400 - Corpo Ilegivel
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
