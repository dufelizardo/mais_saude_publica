*** Settings ***
Resource    ../../../src/scenario/estadual/update_contato_estadual/update_contato_estadual_scenario.resource
Resource    ../../../src/resource/config/driven/data_driven.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - PATCH Update Contato Estadual
Metadata    Test Suite Description        This test suite validates the PATCH update contato Estadual endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               PATCH    UpdateContatoEstadual    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-07
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - AQUAQE-144: Validate PATCH Update Contato Estadual - HTTP 200 OK
    [Documentation]    Test case to validate the PATCH update contato Estadual endpoint with HTTP 200 OK response.
    [Tags]    PATCH    UpdateContatoEstadual    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ESTADUAL - UPDATE CONTATO - PATCH    ${EMPTY}    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - AQUAQE-144: Validate PATCH Update Contato Estadual - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate the PATCH update contato Estadual endpoint with HTTP 404 NOT FOUND
    ...    response.
    [Tags]    PATCH    UpdateContatoEstadual    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ESTADUAL - UPDATE CONTATO - PATCH    ${update_contato_estadual_nonexistent_nome_404}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - AQUAQE-144: Validate PATCH Update Contato Estadual - HTTP 400 BAD REQUEST (Campos Obrigatorios Em Branco)
    [Documentation]    Test case to validate the PATCH update contato Estadual endpoint with HTTP 400 BAD REQUEST
    ...    response, when the email is left blank.
    [Tags]    PATCH    UpdateContatoEstadual    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ESTADUAL - UPDATE CONTATO - PATCH - Campos Obrigatorios Em Branco
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - AQUAQE-144: Validate PATCH Update Contato Estadual - HTTP 400 BAD REQUEST (Corpo Ilegivel)
    [Documentation]    Test case to validate the PATCH update contato Estadual endpoint with HTTP 400 BAD REQUEST
    ...    response, when the request body is syntactically broken JSON (AQUAQE-215).
    [Tags]    PATCH    UpdateContatoEstadual    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ESTADUAL - UPDATE CONTATO - PATCH - Corpo Ilegivel
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
