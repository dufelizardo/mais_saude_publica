*** Settings ***
Resource    ../../../src/scenario/estadual/desabilitar_estadual/desabilitar_estadual_scenario.resource
Resource    ../../../src/resource/config/driven/data_driven.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - DELETE Desabilitar Estadual
Metadata    Test Suite Description        This test suite validates the DELETE des-habilitar Estadual endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               DELETE    DesabilitarEstadual    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-07
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - AQUAQE-144: Validate DELETE Desabilitar Estadual - HTTP 200 OK
    [Documentation]    Test case to validate the DELETE des-habilitar Estadual endpoint with HTTP 200 OK response.
    [Tags]    DELETE    DesabilitarEstadual    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ESTADUAL - DESABILITAR - DELETE    ${EMPTY}    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - AQUAQE-144: Validate DELETE Desabilitar Estadual - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate the DELETE des-habilitar Estadual endpoint with HTTP 404 NOT FOUND
    ...    response.
    [Tags]    DELETE    DesabilitarEstadual    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ESTADUAL - DESABILITAR - DELETE    ${desabilitar_estadual_nonexistent_nome_404}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - AQUAQE-144: Validate DELETE Desabilitar Estadual - HTTP 400 BAD REQUEST (URL Malformada)
    [Documentation]    Test case to validate the DELETE des-habilitar Estadual endpoint with HTTP 400 BAD REQUEST
    ...    response, when the nome in the URL contains a raw null byte (AQUAQE-216).
    [Tags]    DELETE    DesabilitarEstadual    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ESTADUAL - DESABILITAR - DELETE    ${EMPTY}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
