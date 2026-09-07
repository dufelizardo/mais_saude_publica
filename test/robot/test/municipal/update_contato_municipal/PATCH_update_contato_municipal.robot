*** Settings ***
Resource    ../../../src/scenario/municipal/update_contato_municipal/update_contato_municipal_scenario.resource
Resource    ../../../src/resource/config/driven/data_driven.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - PATCH Update Contato Municipal
Metadata    Test Suite Description        This test suite validates the PATCH update contato Municipal endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               PATCH    UpdateContatoMunicipal    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-07
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - AQUAQE-145: Validate PATCH Update Contato Municipal - HTTP 200 OK
    [Documentation]    Test case to validate the PATCH update contato Municipal endpoint with HTTP 200 OK
    ...    response.
    [Tags]    PATCH    UpdateContatoMunicipal    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    MUNICIPAL - UPDATE CONTATO - PATCH    ${EMPTY}    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - AQUAQE-145: Validate PATCH Update Contato Municipal - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate the PATCH update contato Municipal endpoint with HTTP 404 NOT FOUND
    ...    response.
    [Tags]    PATCH    UpdateContatoMunicipal    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    MUNICIPAL - UPDATE CONTATO - PATCH    ${update_contato_municipal_nonexistent_nome_404}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - AQUAQE-145: Validate PATCH Update Contato Municipal - HTTP 400 BAD REQUEST (Campos Obrigatorios Em Branco)
    [Documentation]    Test case to validate the PATCH update contato Municipal endpoint with HTTP 400 BAD
    ...    REQUEST response, when the email is left blank.
    [Tags]    PATCH    UpdateContatoMunicipal    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    MUNICIPAL - UPDATE CONTATO - PATCH - Campos Obrigatorios Em Branco
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - AQUAQE-145: Validate PATCH Update Contato Municipal - HTTP 400 BAD REQUEST (Corpo Ilegivel)
    [Documentation]    Test case to validate the PATCH update contato Municipal endpoint with HTTP 400 BAD
    ...    REQUEST response, when the request body is syntactically broken JSON (AQUAQE-215).
    [Tags]    PATCH    UpdateContatoMunicipal    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    MUNICIPAL - UPDATE CONTATO - PATCH - Corpo Ilegivel
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
