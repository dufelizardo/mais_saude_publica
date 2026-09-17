*** Settings ***
Resource    ../../../flow/unidade_saude/update_supervisao_regional_unidade_saude/update_supervisao_regional_unidade_saude_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UNIDADE SAUDE - UPDATE SUPERVISAO REGIONAL - PATCH
    [Documentation]    Test scenario for updating a Unidade de Saúde unit's supervisaoRegional link
    ...    (PATCH /api/v1/unidade-saude/supervisao-regional/{nome}) on the Mais Saúde Pública API.
    [Arguments]    ${meuNome}    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Unidade Saude - Update Supervisao Regional - PATCH - By HTTP Status Code    ${meuNome}    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UNIDADE SAUDE - UPDATE SUPERVISAO REGIONAL - PATCH - Supervisao Regional Inexistente
    [Documentation]    Test scenario for linking a Unidade de Saúde unit's supervisaoRegional to a nome that
    ...    doesn't exist anywhere, expecting HTTP 404 (as opposed to the unidade-not-found HTTP 404).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PATCH Update Supervisao Regional Unidade Saude - Status Code 404 - Supervisao Regional Inexistente
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UNIDADE SAUDE - UPDATE SUPERVISAO REGIONAL - PATCH - Campos Obrigatorios Em Branco
    [Documentation]    Test scenario for updating a Unidade de Saúde unit's supervisaoRegional with a blank
    ...    value, expecting the field-validation HTTP 400 (as opposed to the not-found HTTP 404).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PATCH Update Supervisao Regional Unidade Saude - Status Code 400 - Campos Obrigatorios Em Branco
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UNIDADE SAUDE - UPDATE SUPERVISAO REGIONAL - PATCH - Corpo Ilegivel
    [Documentation]    Test scenario for updating a Unidade de Saúde unit's supervisaoRegional with a
    ...    syntactically broken JSON body, expecting the standard error contract HTTP 400 (AQUAQE-215).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PATCH Update Supervisao Regional Unidade Saude - Status Code 400 - Corpo Ilegivel
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
