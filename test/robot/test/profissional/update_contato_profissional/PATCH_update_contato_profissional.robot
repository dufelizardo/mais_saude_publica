*** Settings ***
Resource    ../../../src/scenario/profissional/update_contato_profissional/update_contato_profissional_scenario.resource
Resource    ../../../src/resource/config/driven/data_driven.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - PATCH Update Contato Profissional
Metadata    Test Suite Description        This test suite validates the PATCH update contato Profissional endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               PATCH    UpdateContatoProfissional    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-17
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate PATCH Update Contato Profissional - HTTP 200 OK
    [Documentation]    Test case to validate the PATCH update contato Profissional endpoint with HTTP 200 OK
    ...    response.
    [Tags]    PATCH    UpdateContatoProfissional    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROFISSIONAL - UPDATE CONTATO - PATCH    ${EMPTY}    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate PATCH Update Contato Profissional - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate the PATCH update contato Profissional endpoint with HTTP 404 NOT
    ...    FOUND response.
    [Tags]    PATCH    UpdateContatoProfissional    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROFISSIONAL - UPDATE CONTATO - PATCH    ${update_contato_profissional_nonexistent_cpf_404}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate PATCH Update Contato Profissional - HTTP 400 BAD REQUEST (Campos Obrigatorios Em Branco)
    [Documentation]    Test case to validate the PATCH update contato Profissional endpoint with HTTP 400 BAD
    ...    REQUEST response, when the email is left blank.
    [Tags]    PATCH    UpdateContatoProfissional    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROFISSIONAL - UPDATE CONTATO - PATCH - Campos Obrigatorios Em Branco
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate PATCH Update Contato Profissional - HTTP 400 BAD REQUEST (Corpo Ilegivel)
    [Documentation]    Test case to validate the PATCH update contato Profissional endpoint with HTTP 400 BAD
    ...    REQUEST response, when the request body is syntactically broken JSON (AQUAQE-215).
    [Tags]    PATCH    UpdateContatoProfissional    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROFISSIONAL - UPDATE CONTATO - PATCH - Corpo Ilegivel
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
