*** Settings ***
Resource    ../../../flow/profissional/update_contato_profissional/update_contato_profissional_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
PROFISSIONAL - UPDATE CONTATO - PATCH
    [Documentation]    Test scenario for updating a Profissional's contato (PATCH /api/v1/profissional/contato/{cpf})
    ...    on the Mais Saúde Pública API.
    [Arguments]    ${meuCpf}    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Profissional - Update Contato - PATCH - By HTTP Status Code    ${meuCpf}    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PROFISSIONAL - UPDATE CONTATO - PATCH - Campos Obrigatorios Em Branco
    [Documentation]    Test scenario for updating a Profissional's contato with a blank email, expecting the
    ...    field-validation HTTP 400 (as opposed to the not-found HTTP 404).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PATCH Update Contato Profissional - Status Code 400 - Campos Obrigatorios Em Branco
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PROFISSIONAL - UPDATE CONTATO - PATCH - Corpo Ilegivel
    [Documentation]    Test scenario for updating a Profissional's contato with a syntactically broken JSON
    ...    body, expecting the standard error contract HTTP 400 (AQUAQE-215).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PATCH Update Contato Profissional - Status Code 400 - Corpo Ilegivel
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
