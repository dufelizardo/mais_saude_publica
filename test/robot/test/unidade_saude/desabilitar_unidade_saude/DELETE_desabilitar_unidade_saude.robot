*** Settings ***
Resource    ../../../src/scenario/unidade_saude/desabilitar_unidade_saude/desabilitar_unidade_saude_scenario.resource
Resource    ../../../src/resource/config/driven/data_driven.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - DELETE Desabilitar Unidade Saude
Metadata    Test Suite Description        This test suite validates the DELETE des-habilitar Unidade de Saúde endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               DELETE    DesabilitarUnidadeSaude    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-17
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate DELETE Desabilitar Unidade Saude - HTTP 200 OK
    [Documentation]    Test case to validate the DELETE des-habilitar Unidade de Saúde endpoint with HTTP 200
    ...    OK response.
    [Tags]    DELETE    DesabilitarUnidadeSaude    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UNIDADE SAUDE - DESABILITAR - DELETE    ${EMPTY}    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate DELETE Desabilitar Unidade Saude - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate the DELETE des-habilitar Unidade de Saúde endpoint with HTTP 404
    ...    NOT FOUND response.
    [Tags]    DELETE    DesabilitarUnidadeSaude    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UNIDADE SAUDE - DESABILITAR - DELETE    ${desabilitar_unidade_saude_nonexistent_nome_404}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate DELETE Desabilitar Unidade Saude - HTTP 400 BAD REQUEST (URL Malformada)
    [Documentation]    Test case to validate the DELETE des-habilitar Unidade de Saúde endpoint with HTTP 400
    ...    BAD REQUEST response, when the nome in the URL contains a raw null byte (AQUAQE-216).
    [Tags]    DELETE    DesabilitarUnidadeSaude    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UNIDADE SAUDE - DESABILITAR - DELETE    ${EMPTY}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
