*** Settings ***
Resource    ../../../src/scenario/regional/desabilitar_regional/desabilitar_regional_scenario.resource
Resource    ../../../src/resource/config/driven/data_driven.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - DELETE Desabilitar Regional
Metadata    Test Suite Description        This test suite validates the DELETE des-habilitar Regional endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               DELETE    DesabilitarRegional    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-07
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - AQUAQE-146: Validate DELETE Desabilitar Regional - HTTP 200 OK
    [Documentation]    Test case to validate the DELETE des-habilitar Regional endpoint with HTTP 200 OK response.
    [Tags]    DELETE    DesabilitarRegional    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    REGIONAL - DESABILITAR - DELETE    ${EMPTY}    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - AQUAQE-146: Validate DELETE Desabilitar Regional - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate the DELETE des-habilitar Regional endpoint with HTTP 404 NOT FOUND
    ...    response.
    [Tags]    DELETE    DesabilitarRegional    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    REGIONAL - DESABILITAR - DELETE    ${desabilitar_regional_nonexistent_nome_404}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - AQUAQE-146: Validate DELETE Desabilitar Regional - HTTP 400 BAD REQUEST (URL Malformada)
    [Documentation]    Test case to validate the DELETE des-habilitar Regional endpoint with HTTP 400 BAD REQUEST
    ...    response, when the nome in the URL contains a raw null byte (AQUAQE-216).
    [Tags]    DELETE    DesabilitarRegional    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    REGIONAL - DESABILITAR - DELETE    ${EMPTY}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
