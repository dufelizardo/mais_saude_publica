*** Settings ***
Resource    ../../../src/scenario/municipal/desabilitar_municipal/desabilitar_municipal_scenario.resource
Resource    ../../../src/resource/config/driven/data_driven.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - DELETE Desabilitar Municipal
Metadata    Test Suite Description        This test suite validates the DELETE des-habilitar Municipal endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               DELETE    DesabilitarMunicipal    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-07
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - AQUAQE-145: Validate DELETE Desabilitar Municipal - HTTP 200 OK
    [Documentation]    Test case to validate the DELETE des-habilitar Municipal endpoint with HTTP 200 OK
    ...    response.
    [Tags]    DELETE    DesabilitarMunicipal    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    MUNICIPAL - DESABILITAR - DELETE    ${EMPTY}    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - AQUAQE-145: Validate DELETE Desabilitar Municipal - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate the DELETE des-habilitar Municipal endpoint with HTTP 404 NOT FOUND
    ...    response.
    [Tags]    DELETE    DesabilitarMunicipal    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    MUNICIPAL - DESABILITAR - DELETE    ${desabilitar_municipal_nonexistent_nome_404}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - AQUAQE-145: Validate DELETE Desabilitar Municipal - HTTP 400 BAD REQUEST (URL Malformada)
    [Documentation]    Test case to validate the DELETE des-habilitar Municipal endpoint with HTTP 400 BAD
    ...    REQUEST response, when the nome in the URL contains a raw null byte (AQUAQE-216).
    [Tags]    DELETE    DesabilitarMunicipal    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    MUNICIPAL - DESABILITAR - DELETE    ${EMPTY}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
