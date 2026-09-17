*** Settings ***
Resource    ../../../src/scenario/profissional/desabilitar_profissional/desabilitar_profissional_scenario.resource
Resource    ../../../src/resource/config/driven/data_driven.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - DELETE Desabilitar Profissional
Metadata    Test Suite Description        This test suite validates the DELETE des-habilitar Profissional endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               DELETE    DesabilitarProfissional    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-17
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate DELETE Desabilitar Profissional - HTTP 200 OK
    [Documentation]    Test case to validate the DELETE des-habilitar Profissional endpoint with HTTP 200 OK
    ...    response.
    [Tags]    DELETE    DesabilitarProfissional    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROFISSIONAL - DESABILITAR - DELETE    ${EMPTY}    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate DELETE Desabilitar Profissional - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate the DELETE des-habilitar Profissional endpoint with HTTP 404 NOT
    ...    FOUND response.
    [Tags]    DELETE    DesabilitarProfissional    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROFISSIONAL - DESABILITAR - DELETE    ${desabilitar_profissional_nonexistent_cpf_404}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate DELETE Desabilitar Profissional - HTTP 400 BAD REQUEST (URL Malformada)
    [Documentation]    Test case to validate the DELETE des-habilitar Profissional endpoint with HTTP 400 BAD
    ...    REQUEST response, when the cpf in the URL contains a raw null byte (AQUAQE-216).
    [Tags]    DELETE    DesabilitarProfissional    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROFISSIONAL - DESABILITAR - DELETE    ${EMPTY}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
