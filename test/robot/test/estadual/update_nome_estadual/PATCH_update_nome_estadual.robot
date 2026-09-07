*** Settings ***
Resource    ../../../src/scenario/estadual/update_nome_estadual/update_nome_estadual_scenario.resource
Resource    ../../../src/resource/config/driven/data_driven.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - PATCH Update Nome Estadual
Metadata    Test Suite Description        This test suite validates the PATCH update nome Estadual endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               PATCH    UpdateNomeEstadual    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-07
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - AQUAQE-144: Validate PATCH Update Nome Estadual - HTTP 200 OK
    [Documentation]    Test case to validate the PATCH update nome Estadual endpoint with HTTP 200 OK response.
    [Tags]    PATCH    UpdateNomeEstadual    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ESTADUAL - UPDATE NOME - PATCH    ${EMPTY}    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - AQUAQE-144: Validate PATCH Update Nome Estadual - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate the PATCH update nome Estadual endpoint with HTTP 404 NOT FOUND
    ...    response.
    [Tags]    PATCH    UpdateNomeEstadual    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ESTADUAL - UPDATE NOME - PATCH    ${update_nome_estadual_nonexistent_nome_404}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - AQUAQE-144: Validate PATCH Update Nome Estadual - HTTP 400 BAD REQUEST (Campos Obrigatorios Em Branco)
    [Documentation]    Test case to validate the PATCH update nome Estadual endpoint with HTTP 400 BAD REQUEST
    ...    response, when the novoNome is left blank.
    [Tags]    PATCH    UpdateNomeEstadual    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ESTADUAL - UPDATE NOME - PATCH - Campos Obrigatorios Em Branco
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - AQUAQE-144: Validate PATCH Update Nome Estadual - HTTP 409 CONFLICT
    [Documentation]    Test case to validate the PATCH update nome Estadual endpoint with HTTP 409 CONFLICT
    ...    response, when renaming to a nome that already belongs to another unit.
    [Tags]    PATCH    UpdateNomeEstadual    HTTP409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ESTADUAL - UPDATE NOME - PATCH    ${EMPTY}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - AQUAQE-144: Validate PATCH Update Nome Estadual - HTTP 400 BAD REQUEST (Corpo Ilegivel)
    [Documentation]    Test case to validate the PATCH update nome Estadual endpoint with HTTP 400 BAD REQUEST
    ...    response, when the request body is syntactically broken JSON (AQUAQE-215).
    [Tags]    PATCH    UpdateNomeEstadual    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ESTADUAL - UPDATE NOME - PATCH - Corpo Ilegivel
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
