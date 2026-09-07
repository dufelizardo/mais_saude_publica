*** Settings ***
Resource    ../../../src/scenario/regional/update_contato_regional/update_contato_regional_scenario.resource
Resource    ../../../src/resource/config/driven/data_driven.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - PATCH Update Contato Regional
Metadata    Test Suite Description        This test suite validates the PATCH update contato Regional endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               PATCH    UpdateContatoRegional    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-07
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - AQUAQE-146: Validate PATCH Update Contato Regional - HTTP 200 OK
    [Documentation]    Test case to validate the PATCH update contato Regional endpoint with HTTP 200 OK response.
    [Tags]    PATCH    UpdateContatoRegional    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    REGIONAL - UPDATE CONTATO - PATCH    ${EMPTY}    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - AQUAQE-146: Validate PATCH Update Contato Regional - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate the PATCH update contato Regional endpoint with HTTP 404 NOT FOUND
    ...    response.
    [Tags]    PATCH    UpdateContatoRegional    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    REGIONAL - UPDATE CONTATO - PATCH    ${update_contato_regional_nonexistent_nome_404}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - AQUAQE-146: Validate PATCH Update Contato Regional - HTTP 400 BAD REQUEST (Campos Obrigatorios Em Branco)
    [Documentation]    Test case to validate the PATCH update contato Regional endpoint with HTTP 400 BAD REQUEST
    ...    response, when the email is left blank.
    [Tags]    PATCH    UpdateContatoRegional    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    REGIONAL - UPDATE CONTATO - PATCH - Campos Obrigatorios Em Branco
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - AQUAQE-146: Validate PATCH Update Contato Regional - HTTP 400 BAD REQUEST (Corpo Ilegivel)
    [Documentation]    Test case to validate the PATCH update contato Regional endpoint with HTTP 400 BAD REQUEST
    ...    response, when the request body is syntactically broken JSON (AQUAQE-215).
    [Tags]    PATCH    UpdateContatoRegional    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    REGIONAL - UPDATE CONTATO - PATCH - Corpo Ilegivel
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
