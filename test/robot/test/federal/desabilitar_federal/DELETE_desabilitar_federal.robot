*** Settings ***
Resource    ../../../src/scenario/federal/desabilitar_federal/desabilitar_federal_scenario.resource
Resource    ../../../src/resource/config/driven/data_driven.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - DELETE Desabilitar Federal
Metadata    Test Suite Description        This test suite validates the DELETE des-habilitar Federal endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               DELETE    DesabilitarFederal    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-07
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - AQUAQE-143: Validate DELETE Desabilitar Federal - HTTP 200 OK
    [Documentation]    Test case to validate the DELETE des-habilitar Federal endpoint with HTTP 200 OK response.
    [Tags]    DELETE    DesabilitarFederal    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    FEDERAL - DESABILITAR - DELETE    ${EMPTY}    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - AQUAQE-143: Validate DELETE Desabilitar Federal - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate the DELETE des-habilitar Federal endpoint with HTTP 404 NOT FOUND
    ...    response.
    [Tags]    DELETE    DesabilitarFederal    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    FEDERAL - DESABILITAR - DELETE    ${desabilitar_federal_nonexistent_nome_404}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - AQUAQE-143: Validate DELETE Desabilitar Federal - HTTP 400 BAD REQUEST (URL Malformada)
    [Documentation]    Test case to validate the DELETE des-habilitar Federal endpoint with HTTP 400 BAD REQUEST
    ...    response, when the nome in the URL contains a raw null byte (AQUAQE-216).
    [Tags]    DELETE    DesabilitarFederal    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    FEDERAL - DESABILITAR - DELETE    ${EMPTY}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
