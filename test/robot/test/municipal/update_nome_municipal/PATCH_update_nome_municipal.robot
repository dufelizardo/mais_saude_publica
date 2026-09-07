*** Settings ***
Resource    ../../../src/scenario/municipal/update_nome_municipal/update_nome_municipal_scenario.resource
Resource    ../../../src/resource/config/driven/data_driven.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - PATCH Update Nome Municipal
Metadata    Test Suite Description        This test suite validates the PATCH update nome Municipal endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               PATCH    UpdateNomeMunicipal    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-07
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - AQUAQE-145: Validate PATCH Update Nome Municipal - HTTP 200 OK
    [Documentation]    Test case to validate the PATCH update nome Municipal endpoint with HTTP 200 OK response.
    [Tags]    PATCH    UpdateNomeMunicipal    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    MUNICIPAL - UPDATE NOME - PATCH    ${EMPTY}    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - AQUAQE-145: Validate PATCH Update Nome Municipal - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate the PATCH update nome Municipal endpoint with HTTP 404 NOT FOUND
    ...    response.
    [Tags]    PATCH    UpdateNomeMunicipal    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    MUNICIPAL - UPDATE NOME - PATCH    ${update_nome_municipal_nonexistent_nome_404}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - AQUAQE-145: Validate PATCH Update Nome Municipal - HTTP 400 BAD REQUEST (Campos Obrigatorios Em Branco)
    [Documentation]    Test case to validate the PATCH update nome Municipal endpoint with HTTP 400 BAD REQUEST
    ...    response, when the novoNome is left blank.
    [Tags]    PATCH    UpdateNomeMunicipal    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    MUNICIPAL - UPDATE NOME - PATCH - Campos Obrigatorios Em Branco
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - AQUAQE-145: Validate PATCH Update Nome Municipal - HTTP 409 CONFLICT
    [Documentation]    Test case to validate the PATCH update nome Municipal endpoint with HTTP 409 CONFLICT
    ...    response, when renaming to a nome that already belongs to another unit.
    [Tags]    PATCH    UpdateNomeMunicipal    HTTP409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    MUNICIPAL - UPDATE NOME - PATCH    ${EMPTY}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - AQUAQE-145: Validate PATCH Update Nome Municipal - HTTP 400 BAD REQUEST (Corpo Ilegivel)
    [Documentation]    Test case to validate the PATCH update nome Municipal endpoint with HTTP 400 BAD REQUEST
    ...    response, when the request body is syntactically broken JSON (AQUAQE-215).
    [Tags]    PATCH    UpdateNomeMunicipal    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    MUNICIPAL - UPDATE NOME - PATCH - Corpo Ilegivel
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
