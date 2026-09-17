*** Settings ***
Resource    ../../../flow/unidade_saude/update_responsavel_unidade_saude/update_responsavel_unidade_saude_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UNIDADE SAUDE - UPDATE RESPONSAVEL - PATCH
    [Documentation]    Test scenario for updating a Unidade de Saúde unit's responsavelCpf
    ...    (PATCH /api/v1/unidade-saude/responsavel/{nome}) on the Mais Saúde Pública API.
    [Arguments]    ${meuNome}    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Unidade Saude - Update Responsavel - PATCH - By HTTP Status Code    ${meuNome}    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UNIDADE SAUDE - UPDATE RESPONSAVEL - PATCH - Campos Obrigatorios Em Branco
    [Documentation]    Test scenario for updating a Unidade de Saúde unit's responsavelCpf with a blank value,
    ...    expecting the field-validation HTTP 400 (as opposed to the not-found HTTP 404).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PATCH Update Responsavel Unidade Saude - Status Code 400 - Campos Obrigatorios Em Branco
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UNIDADE SAUDE - UPDATE RESPONSAVEL - PATCH - Corpo Ilegivel
    [Documentation]    Test scenario for updating a Unidade de Saúde unit's responsavelCpf with a syntactically
    ...    broken JSON body, expecting the standard error contract HTTP 400 (AQUAQE-215).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PATCH Update Responsavel Unidade Saude - Status Code 400 - Corpo Ilegivel
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
